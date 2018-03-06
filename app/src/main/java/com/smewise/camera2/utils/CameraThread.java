package com.smewise.camera2.utils;

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
    private LinkedBlockingQueue<Runnable> mQueue;

    public CameraThread() {
        mQueue = new LinkedBlockingQueue<>();
    }

    public void post(Runnable job) {
        if (!mQueue.offer(job)) {
            Log.e(TAG, "failed to add job");
        }
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
                Runnable job = mQueue.poll();
                job.run();
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

}
