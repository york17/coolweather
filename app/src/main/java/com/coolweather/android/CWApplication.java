package com.coolweather.android;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePal;

/**
 * Created by lee on 2018/7/23.
 */

public class CWApplication extends Application {

    public static Context mContext;


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        LitePal.initialize(this);
    }

    public static Context getInstance() {
        return mContext;
    }
}
