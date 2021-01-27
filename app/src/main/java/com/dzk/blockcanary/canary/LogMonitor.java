package com.dzk.blockcanary.canary;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Printer;

import java.util.List;


public class LogMonitor implements Printer {
    private StackSimpler mStackSimpler;
    private Handler mLogHandler;
    private boolean mPrintStarted = false;
    private long mStartTimeStamp;
    //卡顿阈值
    private int mBlockThresholdMills = 3000;
    //采样频率
    private long mSampleInterval = 1000;

    public LogMonitor(){
        mStackSimpler = new StackSimpler(mSampleInterval);
        HandlerThread handlerThread = new HandlerThread("block-canary-io");
        handlerThread.start();
        mLogHandler = new Handler(handlerThread.getLooper());
    }
    @Override
    public void println(String s) {
        if (!mPrintStarted){
               mPrintStarted = true;
               mStartTimeStamp = System.currentTimeMillis();
               mStackSimpler.startDump();
        }else {
            mPrintStarted = false;
            final long endTimeStamp = System.currentTimeMillis();
            if (isBlock(endTimeStamp)){
                notifyBlockEvent(endTimeStamp);
            }
            mStackSimpler.stopDump();
        }
    }

    private void notifyBlockEvent(final long endTimeStamp) {
        mLogHandler.post(new Runnable() {
            @Override
            public void run() {
                List<String> stacks = mStackSimpler.getStacks(mStartTimeStamp,endTimeStamp);
                for (String stack : stacks) {
                    Log.e("block-canary", stack);
                }
            }
        });
    }

    private boolean isBlock(long endTimeStamp) {
        return endTimeStamp - mStartTimeStamp > mSampleInterval;
    }
}
