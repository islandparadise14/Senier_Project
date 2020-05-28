package com.example.senier_project.feature

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.SystemClock
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.senier_project.R
import com.example.senier_project.global.Consts
import com.example.senier_project.global.MosWord
import com.example.senier_project.global.StopWord
import com.example.senier_project.koin.repository.SharedPrefRepository
import com.example.senier_project.utils.*
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), LifecycleOwner {
    private val sharedPrefRepository: SharedPrefRepository by inject()
    private lateinit var mMosHandler: Handler
    private lateinit var mTextHandler: Handler
    private lateinit var mBlockHandler: Handler

    private val analyzedWords: HashMap<String, Int> = HashMap()
    private val analyzedWordList: ArrayList<Map.Entry<String, Int>> = ArrayList()
    private lateinit var sortedAnalyzedWord: List<Map.Entry<String, Int>>

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

    private lateinit var preview: Preview
    private lateinit var imageCapture: ImageCapture
    private lateinit var analyzerUseCase: ImageAnalysis
    private var mMode: ModeState = ModeState.NORMAL

    private val MIN_CLICK_INTERVAL: Long = 3000

    //마지막으로 클릭한 시간
    private var mLastClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initPermission()
        initView()
    }

    private fun initPermission() {
        viewFinder = findViewById(R.id.view_finder)

        // Request camera permissions
        if (allPermissionsGranted()) {
            viewFinder.post { startCamera() }
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Every time the provided texture view changes, recompute layout
        viewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }
    }

    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var viewFinder: TextureView

    private fun startCamera() {
        // Create configuration object for the viewfinder use case
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetResolution(Size(640, 480))
        }.build()


        // Build the viewfinder use case
        preview = Preview(previewConfig)

        // Every time the viewfinder is updated, recompute layout
        preview.setOnPreviewOutputUpdateListener {

            // To update the SurfaceTexture, we have to remove it and re-add it
            val parent = viewFinder.parent as ViewGroup
            parent.removeView(viewFinder)
            parent.addView(viewFinder, 0)

            viewFinder.surfaceTexture = it.surfaceTexture
            updateTransform()
        }

        val imageCaptureConfig = ImageCaptureConfig.Builder()
            .apply {
                // We don't set a resolution for image capture; instead, we
                // select a capture mode which will infer the appropriate
                // resolution based on aspect ration and requested mode
                setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
            }.build()

        // Build the image capture use case and attach button click listener
        imageCapture = ImageCapture(imageCaptureConfig)

        // Setup image analysis pipeline that computes average pixel luminance
        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
            // In our analysis, we care more about the latest image than
            // analyzing *every* image
            setImageReaderMode(
                ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
        }.build()

        // Build the image analysis use case and instantiate our analyzer
        analyzerUseCase = ImageAnalysis(analyzerConfig).apply {
            setAnalyzer(executor, LuminosityAnalyzer())
        }

        // Bind use cases to lifecycle
        // If Android Studio complains about "this" being not a LifecycleOwner
        // try rebuilding the project or updating the appcompat dependency to
        // version 1.1.0 or higher.
        CameraX.bindToLifecycle(this, preview, imageCapture, analyzerUseCase)
    }

    private fun updateTransform() {
        val matrix = Matrix()

        // Compute the center of the view finder
        val centerX = viewFinder.width / 2f
        val centerY = viewFinder.height / 2f

        // Correct preview output to account for display rotation
        val rotationDegrees = when(viewFinder.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        // Finally, apply transformations to our TextureView
        viewFinder.setTransform(matrix)
    }

    /**
     * Process result from permission request dialog box, has the request
     * been granted? If yes, start Camera. Otherwise display a toast
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                viewFinder.post { startCamera() }
            } else {
                this.toastLong("Permissions not granted by the user.")
                finish()
            }
        }
    }

    /**
     * Check if all permission specified in the manifest have been granted
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    /*--------------------------------------------------------------------------------------------*/

    inner class LuminosityAnalyzer : ImageAnalysis.Analyzer {

        private fun degreesToFirebaseRotation(degrees: Int): Int = when(degrees) {
            0 -> FirebaseVisionImageMetadata.ROTATION_0
            90 -> FirebaseVisionImageMetadata.ROTATION_90
            180 -> FirebaseVisionImageMetadata.ROTATION_180
            270 -> FirebaseVisionImageMetadata.ROTATION_270
            else -> throw Exception("Rotation must be 0, 90, 180, or 270.")
        }

        override fun analyze(imageProxy: ImageProxy?, degrees: Int) {
            val currentClickTime: Long = SystemClock.uptimeMillis()
            //이전에 클릭한 시간과 현재시간의 차이
            val elapsedTime = currentClickTime - mLastClickTime

            if (elapsedTime <= MIN_CLICK_INTERVAL) return

            //마지막클릭시간 업데이트
            mLastClickTime = currentClickTime

            if (mMode == ModeState.ANALYZERING) {
                val mediaImage = imageProxy?.image
                val imageRotation = degreesToFirebaseRotation(degrees)
                if (mediaImage != null) {
                    val image = FirebaseVisionImage.fromMediaImage(mediaImage, imageRotation)
                    // Pass image to an ML Kit Vision API
                    val detector = FirebaseVision.getInstance().onDeviceTextRecognizer
                    detector.processImage(image).addOnSuccessListener { firebaseVisionText ->
                        // Task completed successfully
                        for (block in firebaseVisionText.textBlocks) {
                            val resultList = ArrayList<String>()
                            for (line in block.lines) {
                                val lineText = line.text
                                resultList.add(lineText)
                            }
                            for ((index, item) in resultList.withIndex()) {
                                Timber.d("text: index$index = $item")
                                for (word in item.replaceSign().split(" ")) {
                                    if (!StopWord.set.contains(word) && word != "")
                                        analyzedWords[word] = analyzedWords[word] ?: 0 + 1
                                }
                                if (mMode == ModeState.ANALYZERING) {
                                    textView.text = textView2.text.toString()
                                    textView2.text = textView3.text.toString()
                                    textView3.text = textView4.text.toString()
                                    textView4.text = textView5.text.toString()
                                    textView5.text = textView6.text.toString()
                                    textView6.text = textView7.text.toString()
                                    textView7.text = textView8.text.toString()
                                    textView8.text = textView9.text.toString()
                                    textView9.text = textView10.text.toString()
                                    textView10.text = textView11.text.toString()
                                    textView11.text = textView12.text.toString()
                                    textView12.text = item
                                } else {
                                    clearText()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /*--------------------------------------------------------------------------------------------*/

    @SuppressLint("SetTextI18n")
    private fun initView() {
        Thread(Runnable {
            while (true) {
                Thread.sleep(1000)
                if (mMode == ModeState.ANALYZERING) {
                    this@MainActivity.vibrateSystem()
                }
            }
        }).start()
        startMos.setOnClickListener {
            when (mMode) {
                ModeState.NORMAL -> {
                    (it as Button).text = "End of analysis & text conversion"
                    analyzedWords.clear()
                    analyzedWordList.clear()
                    analyzedText.text = ""
                    mosText.text = ""
                    mMode = ModeState.ANALYZERING
                }
                ModeState.ANALYZERING -> {
                    (it as Button).text = "Tab screen to start analysis"
                    mMode = ModeState.NORMAL
                    clearText()
                    profiling()
                }
            }
        }
        setting.setOnClickListener {
            navigateToSetting()
        }
        mMosHandler = Handler(Handler.Callback { message ->
            MosMessage.stringToMos(message.data.getString(Consts.MOS_KEY)).let {
                when (it) {
                    MosMessage.MOS_SHORT -> {
                        mosText.text = mosText.text.toString() + MosWord.mosShort
                        return@Callback true
                    }
                    MosMessage.MOS_LONG -> {
                        mosText.text = mosText.text.toString() + MosWord.mosLong
                        return@Callback true
                    }
                    MosMessage.BLANK -> {
                        mosText.text = ""
                        return@Callback true
                    }
                }
            }
        })
        mTextHandler = Handler(Handler.Callback { msg: Message ->
            val string = msg.data.getString(Consts.TEXT_KEY) ?: ""
            mosText.text = mosText.text.toString() + " "
            analyzedText.text = analyzedText.text.toString() + string
            return@Callback true
        })
        mBlockHandler = Handler(Handler.Callback { msg: Message ->
            if (msg.data.getBoolean(Consts.BLOCK_KEY))
                blockView.visibility = View.VISIBLE
            else blockView.visibility = View.GONE

            return@Callback true
        })
    }

    private fun clearText() {
        textView.text = ""
        textView2.text = ""
        textView3.text = ""
        textView4.text = ""
        textView5.text = ""
        textView6.text = ""
        textView7.text = ""
        textView8.text = ""
        textView9.text = ""
        textView10.text = ""
        textView11.text = ""
        textView12.text = ""
    }

    private fun profiling() {
        analyzedWords.forEach { entry ->
            analyzedWordList.add(entry)
        }
        sortedAnalyzedWord = analyzedWordList.sortedWith(Comparator { o1, o2 ->
            return@Comparator o2.value - o1.value
        })
        startConvertToMosVibrator()
    }

    private fun startConvertToMosVibrator() {
        Thread(Runnable {
            mBlockHandler.sendMessage(Message().apply { this.data.putBoolean(Consts.BLOCK_KEY, true) })
            Thread.sleep(1000)
            mBlockHandler.sendMessage(Message().apply { this.data.putBoolean(Consts.BLOCK_KEY, false) })
            sortedAnalyzedWord.mapIndexed { index, entry ->
                if (index < 5) "${entry.key} " else ""
            }.join().forEach {
                val message = Message().apply {
                    this.data.putString(Consts.TEXT_KEY, it.toString())
                }
                mTextHandler.sendMessage(message)
                it.enToMosNumber().forEach { char ->
                    mMosHandler.sendMessage(Message().apply {
                        val mos = when (char) {
                            '1' -> MosMessage.MOS_SHORT.text
                            '2' -> MosMessage.MOS_LONG.text
                            else -> " "
                        }
                        this.data.putString(Consts.MOS_KEY, mos)
                    })
                    char.mosToVibrate(this, sharedPrefRepository.getPrefsIntValue(Consts.SPEED_SETTING, 10))
                }
                Thread.sleep(300)
            }
        }).start()
    }

    private fun navigateToSetting() = startActivity(Intent(this, SettingActivity::class.java))
        .run { this@MainActivity.vibrateSystem() }
}
