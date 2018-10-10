package com.gribanskij.miserplus;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.support.v7.preference.PreferenceManager;
import android.util.DisplayMetrics;

import java.util.Locale;

/**
 * Created by SESA175711 on 27.09.2017.
 */

public class MainApplication extends Application {


    private final String PREF_LANG_KEY = "language";
    private final String PREF_LANG_DEFAULT_VALUE = "sys";
    private final String PREF_LANG_RUS_VALUE = "ru";


    @Override
    protected void attachBaseContext(Context base) {

        PreferenceManager.setDefaultValues(base, R.xml.pref_miser, false);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(base);
        String lang = sharedPreferences.getString(PREF_LANG_KEY, PREF_LANG_DEFAULT_VALUE);

        if (lang.equals(PREF_LANG_DEFAULT_VALUE)) {
            super.attachBaseContext(base);
            return;
        }

        Locale locale = new Locale(PREF_LANG_RUS_VALUE);
        Locale.setDefault(locale);

        Resources resources = base.getResources();
        Configuration configuration = resources.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale);
            configuration.setLayoutDirection(locale);
            base.createConfigurationContext(configuration);
            super.attachBaseContext(base);
        } else {
            DisplayMetrics displayMetrics = resources.getDisplayMetrics();
            configuration.locale = locale;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                configuration.setLayoutDirection(locale);
            }
            resources.updateConfiguration(configuration, displayMetrics);
            super.attachBaseContext(base);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        Context context = getBaseContext();
        PreferenceManager.setDefaultValues(context, R.xml.pref_miser, false);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String lang = sharedPreferences.getString(PREF_LANG_KEY, PREF_LANG_DEFAULT_VALUE);

        if (lang.equals(PREF_LANG_DEFAULT_VALUE)) {
            super.onConfigurationChanged(newConfig);
            return;
        }

        Locale locale = new Locale(PREF_LANG_RUS_VALUE);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale);
            configuration.setLayoutDirection(locale);
            context.createConfigurationContext(configuration);
        } else {
            DisplayMetrics displayMetrics = resources.getDisplayMetrics();
            configuration.locale = locale;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                configuration.setLayoutDirection(locale);
            }
            resources.updateConfiguration(configuration, displayMetrics);
        }

        //super.onConfigurationChanged(newConfig);
    }
}
