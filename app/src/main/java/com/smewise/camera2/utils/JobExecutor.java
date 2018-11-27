package com.smewise.camera2.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.smewise.camera2.Config;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class JobExecutor {
    private static final String TAG = Config.getTag(JobExecutor.class);
    private ThreadPoolExecutor mExecutor;
    private Handler mHandler;

    public JobExecutor() {
        // init thread pool
        mExecutor = new ThreadPoolExecutor(1, 4, 10,
                TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(4),
                new ThreadPoolExecutor.DiscardOldestPolicy());
        mHandler = new Handler(Looper.getMainLooper());
    }

    public <T> void execute(final Task<T> task) {
        if (mExecutor != null) {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        T res = task.run();
                        postOnMainThread(task, res);
                        task.onJobThread(res);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "job execute error:" + e.getMessage());
                        task.onError(e.getMessage());
                    }
                }
            });
        }
    }

    public void destroy() {
        mExecutor.shutdown();
        mExecutor = null;
    }

    private <T> void postOnMainThread(final Task<T> task, final T res) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                task.onMainThread(res);
            }
        });
    }

    public static abstract class Task<T> {
        public T run() {
            return null;
        }
        public void onMainThread(T result) {
            // default no implementation
        }

        public void onJobThread(T result) {
            // default no implementation
        }

        public void onError(String msg) {
            // default no implementation
        }

    }
}
