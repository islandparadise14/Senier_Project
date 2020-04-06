package com.example.senier_project.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.senier_project.global.Consts;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SharedPreferenceHelper {
    private volatile static SharedPreferenceHelper instance = null;
    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mPrefsEditor;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());

    private SharedPreferenceHelper(Context context) {
        mPrefs = context.getSharedPreferences(Consts.SENIER, 0);
    }

    // SharedPreferenceHelper is a singleton pattern
    public static SharedPreferenceHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (SharedPreferenceHelper.class) {
                if (instance == null) {
                    instance = new SharedPreferenceHelper(context);
                }
            }
        }
        return instance;
    }

    public void writePrefs(String key) {
        writePrefs(key, dateFormat.format(new Date()));
    }

    public void writePrefs(String key, Integer value) {
        if (value == null) {
            return;
        }
        mPrefsEditor = mPrefs.edit();
        mPrefsEditor.putInt(key, value).apply();
    }

    public void writePrefs(String key, Long value) {
        if (value == null) {
            return;
        }
        mPrefsEditor = mPrefs.edit();
        mPrefsEditor.putLong(key, value).apply();
    }

    public void writePrefs(String key, String value) {
        mPrefsEditor = mPrefs.edit();
        mPrefsEditor.putString(key, value).apply();
    }

    public void writePrefs(String key, boolean value) {
        mPrefsEditor = mPrefs.edit();
        mPrefsEditor.putBoolean(key, value).apply();
    }

    public boolean getPrefsBooleanValue(String key) {
        return mPrefs.getBoolean(key, false);
    }

    public boolean getPrefsBooleanValue(String key, boolean defaultValue) {
        return mPrefs.getBoolean(key, defaultValue);
    }

    public void resetPrefsIntValue(String key) {
        writePrefs(key, 0);
    }

    public int getPrefsIntValue(String key) {
        return mPrefs.getInt(key, 0);
    }

    public int getPrefsIntValue(String key, Integer defaultValue) {
        return mPrefs.getInt(key, defaultValue);
    }

    public long getPrefsLongValue(String key) {
        return mPrefs.getLong(key, 0L);
    }

    public String getPrefsStringValue(String key) {
        return mPrefs.getString(key, "");
    }

    public void removePrefsValue(String key) {
        if (mPrefs.contains(key)) {
            mPrefsEditor = mPrefs.edit();
            mPrefsEditor.remove(key).apply();
        }
    }
}
