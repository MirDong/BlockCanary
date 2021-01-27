package com.dzk.blockcanary.app;

import android.app.Application;

import com.dzk.blockcanary.canary.BlockCanary;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        BlockCanary.install(this);
    }
}
