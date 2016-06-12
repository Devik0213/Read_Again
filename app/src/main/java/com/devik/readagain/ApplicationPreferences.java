/*
 * @(#)ContentLanguage.class $version 2014. 4. 22.
 *
 * Copyright 2014 Naver Corp. All rights Reserved.
 * NAVER PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.devik.readagain;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.crash.FirebaseCrash;


public class ApplicationPreferences extends Application {
    private static final String KEY_NAME_APP = "preferences_app";

    public static final String KEY_SAVE_ALARM = "save_alarm";
    public static final String KEY_SAVE_FREQ_DAY = "save_freq";
    public static final String KEY_READ_ALARM = "read_alarm";
    public static final String KEY_READ_FREQ_DAY = "read_freq";

    private static ApplicationPreferences instance;
    private SharedPreferences preferences;
    private SharedPreferences defaultSharedPreference;
    private int readTime;
    private int saveTime;
    private int daySaveFreq;
    private int datReadFreq;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static void initialize(Context context) {
        instance = new ApplicationPreferences(context);
    }


    private ApplicationPreferences(Context context) {
        if (instance == null) {
            instance = new ApplicationPreferences(context);
        }
        loadPreference(context);
    }

    public void loadPreference(Context context) {
        defaultSharedPreference = PreferenceManager.getDefaultSharedPreferences(context);
        preferences = context.getSharedPreferences(KEY_NAME_APP, Context.MODE_PRIVATE);

        // defaultSharedPreference는 PreferenceScreen의 값을 참조하기 위한 용도로 preferences 와 구분해서 사용한다.
        readTime = defaultSharedPreference.getInt(KEY_READ_ALARM, 0);
        datReadFreq = defaultSharedPreference.getInt(KEY_READ_FREQ_DAY, 0);
        saveTime = defaultSharedPreference.getInt(KEY_SAVE_ALARM, 0);
        daySaveFreq = defaultSharedPreference.getInt(KEY_SAVE_FREQ_DAY, 0);
    }

    public int getReadTime() {
        return readTime;
    }

    public void setReadTime(int readTime) {
        this.readTime = readTime;
        preferences.edit().putInt(KEY_READ_ALARM, readTime).commit();
    }

    public int getSaveTime() {
        return saveTime;
    }

    public void setSaveTime(int saveTime) {
        this.saveTime = saveTime;
        preferences.edit().putInt(KEY_SAVE_ALARM, saveTime).commit();
    }

    public void setDaySaveFreq(int daySaveFreq) {
        this.daySaveFreq = daySaveFreq;
        preferences.edit().putInt(KEY_SAVE_FREQ_DAY, daySaveFreq).commit();
    }

    public int getDaySaveFreq() {
        return daySaveFreq;
    }

    public void setDatReadFreq(int datReadFreq) {
        this.datReadFreq = datReadFreq;
        preferences.edit().putInt(KEY_READ_FREQ_DAY, datReadFreq).commit();
    }

    public int getDatReadFreq() {
        return datReadFreq;
    }
}
