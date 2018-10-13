package com.smewise.camera2.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

import com.smewise.camera2.Config;

import java.util.concurrent.LinkedBlockingQueue;


/**
 * Created by wenzhe on 9/18/17.
 */

public class CameraThread extends Thread {
    private static final String TAG = Config.getTag(CameraThread.class);

    private volatile boolean mActive = true;
    private LinkedBlockingQueue<JobItem> mQueue;
    private Handler mMainHandler;

    public CameraThread() {
        mQueue = new LinkedBlockingQueue<>();
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    public void post(Runnable job) {
        JobItem item = new JobItem();
        item.job = job;
        item.callback = null;
        if (!mQueue.offer(item)) {
            Log.e(TAG, "failed to add job");
        }
        this.notifyJob();
    }

    public void post(Runnable job, JobItem.JobCallback callback) {
        JobItem item = new JobItem();
        item.job = job;
        item.callback = callback;
        if (!mQueue.offer(item)) {
            Log.e(TAG, "failed to add job");
        }
        this.notifyJob();
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
        while (mActive) {
            if (mQueue.isEmpty()) {
                if (mActive) {
                    waitWithoutInterrupt(this);
                } else {
                    break;
                }
            } else {
                final JobItem item = mQueue.poll();
                assert item != null;
                item.job.run();
                if (item.callback != null) {
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            item.callback.onJobDone();
                        }
                    });
                }
            }
        }
        // loop end
    }

    public synchronized void notifyJob() {
        notifyAll();
    }

    public synchronized void terminate() {
        mActive = false;
        mQueue.clear();
        notifyAll();
    }

    private synchronized void waitWithoutInterrupt(Object object) {
        try {
            object.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static class JobItem {
        public interface JobCallback {
            void onJobDone();
        }
        public Runnable job;
        public JobCallback callback;
    }

}
