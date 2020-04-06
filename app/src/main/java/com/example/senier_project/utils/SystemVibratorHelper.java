package com.example.senier_project.utils;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class SystemVibratorHelper {
    public static SystemVibratorHelper getInstance() {
        return LazyHolder.INSTANCE;
    }

    private static class LazyHolder {
        static SystemVibratorHelper INSTANCE = new SystemVibratorHelper();
    }

    private Boolean isRunning = false;

    public void startVibrator(final Context context, final int speed) {
        if (!isRunning) {
            isRunning = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                    assert vibrator != null;
                    vibrator.vibrate(VibrationEffect.createOneShot(50, 40));
                    try {
                        Thread.sleep((1200 - 10 * (speed + 10))/3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    isRunning = false;
                }
            }).start();
        }
    }
}
