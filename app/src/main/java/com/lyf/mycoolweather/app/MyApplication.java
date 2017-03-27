package com.lyf.mycoolweather.app;

import android.app.Application;

import org.litepal.LitePal;

/**
 * Created by 刘亚飞 on 2017/3/27.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);
    }
}
