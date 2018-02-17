package com.lukasanda.popularmovies;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

/**
 * Created by lukas on 2/16/2018.
 */

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
    }
}