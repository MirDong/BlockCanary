package com.dzk.blockcanary.canary;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class StackSimpler {
    public static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("yy-MM-dd HH:mm:ss.SSS");
    public static final String SEPARATOR = "\r\n";
    private Map<Long,String> mStackMap = new LinkedHashMap<>();
    private Handler mSamplerHandler;
    private int mMaxCount = 100;
    private long mSampleInterval;
    //是否需要采样
    protected AtomicBoolean mShouldSample = new AtomicBoolean(false);
    public StackSimpler(long sampleInterval) {
        this.mSampleInterval = sampleInterval;
        HandlerThread handlerThread = new HandlerThread("block-canary-simpler");
        handlerThread.start();
        mSamplerHandler = new Handler(handlerThread.getLooper());
    }

    public void startDump() {
        //避免重复开始
        if (mShouldSample.get()){
            return;
        }
        mShouldSample.set(true);

        mSamplerHandler.removeCallbacks(mRunnable);
        mSamplerHandler.postDelayed(mRunnable,mSampleInterval);
    }

    public void stopDump() {
        //避免重复结束
        if (!mShouldSample.get()){
            return;
        }
        mShouldSample.set(false);
        mSamplerHandler.removeCallbacks(mRunnable);
    }

    public List<String> getStacks(long mStartTimeStamp, long endTimeStamp) {
        List<String> result = new ArrayList<>();
        synchronized (mStackMap){
            for (long entryTime:mStackMap.keySet()){
                if (mStartTimeStamp <entryTime && entryTime <endTimeStamp){
                    result.add(
                            TIME_FORMATTER.format(entryTime)
                            + SEPARATOR
                            + SEPARATOR
                            + mStackMap.get(entryTime)
                    );
                }
            }

        }
        return result;
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            StringBuilder sb = new StringBuilder();
            StackTraceElement[] stackTraces = Looper.getMainLooper().getThread().getStackTrace();
            for (StackTraceElement trace : stackTraces) {
                sb.append(trace.toString()).append("\n");
            }

            synchronized (mStackMap){
                if (mStackMap.size() == mMaxCount){
                    mStackMap.remove(mStackMap.keySet().iterator().next());
                }
                mStackMap.put(System.currentTimeMillis(),sb.toString());
            }

            if (mShouldSample.get()){
                mSamplerHandler.postDelayed(mRunnable,mSampleInterval);
            }
        }
    };
}
