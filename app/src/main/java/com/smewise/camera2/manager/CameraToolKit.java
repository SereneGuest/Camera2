package com.smewise.camera2.manager;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.view.OrientationEventListener;

import com.smewise.camera2.module.SettingFragment;
import com.smewise.camera2.ui.AppBaseUI;
import com.smewise.camera2.ui.CameraTab;
import com.smewise.camera2.ui.CoverView;
import com.smewise.camera2.utils.CameraThread;
import com.smewise.camera2.utils.FileSaver;

/**
 * Created by wenzhe on 9/12/17.
 */

public class CameraToolKit {

    private Context mContext;
    private CameraThread mCameraThread;
    private Handler mMainHandler;
    private MyOrientationListener mOrientationListener;
    private FileSaver mFileSaver;
    private int mRotation = 0;
    private AppBaseUI mBaseUI;
    private SettingFragment mSettingFragment;

    public CameraToolKit(Context context, AppBaseUI appBaseUI) {
        mContext = context;
        mMainHandler = new Handler(Looper.getMainLooper());
        mFileSaver = new FileSaver(mContext.getContentResolver(), mMainHandler);
        mBaseUI = appBaseUI;
        setOrientationListener();
        startCameraThread();
        mSettingFragment = new SettingFragment();
    }

    public void destroy() {
        if (mFileSaver != null) {
            mFileSaver.release();
        }
        disableOrientationListener();
        stopCameraThread();
    }

    public FileSaver getFileSaver() {
        return mFileSaver;
    }

    public int getOrientation() {
        return mRotation;
    }

    public Handler getMainHandler() {
        return mMainHandler;
    }

    public AppBaseUI getBaseUI() {
        return mBaseUI;
    }

    public CoverView getCoverView() {
        return mBaseUI.getCoverView();
    }

    public CameraTab getTabView() {
        return mBaseUI.getCameraTab();
    }

    public CameraThread getCameraThread() {
        return mCameraThread;
    }

    public SettingFragment getSettingFragment() {
        return mSettingFragment;
    }

    private class MyOrientationListener extends OrientationEventListener {

        MyOrientationListener(Context context, int rate) {
            super(context, rate);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (((orientation + 45) / 90 * 90)!= mRotation) {
                mRotation = (orientation + 45) / 90 * 90;
            }
        }
    }

    private void setOrientationListener() {
        mOrientationListener = new MyOrientationListener(mContext, SensorManager.SENSOR_DELAY_UI);
        if (mOrientationListener.canDetectOrientation()) {
            mOrientationListener.enable();
        } else {
            mOrientationListener.disable();
        }
    }

    private void disableOrientationListener() {
        mOrientationListener.disable();
    }

    private void startCameraThread() {
        if (mCameraThread == null) {
            mCameraThread = new CameraThread();
            mCameraThread.start();
        }
    }

    private void stopCameraThread() {
        if (mCameraThread != null) {
            mCameraThread.terminate();
            try {
                mCameraThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mCameraThread = null;
        }
    }
}
