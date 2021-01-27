package com.dzk.blockcanary.canary;

import android.content.Context;
import android.os.Looper;

public class BlockCanary {
    public static void install(Context context){
        LogMonitor monitor = new LogMonitor();
        Looper.getMainLooper().setMessageLogging(monitor);
    }
}
