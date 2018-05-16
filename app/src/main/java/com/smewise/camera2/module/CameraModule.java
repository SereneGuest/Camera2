package com.smewise.camera2.module;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.smewise.camera2.Config;
import com.smewise.camera2.manager.CameraSettings;
import com.smewise.camera2.manager.CameraToolKit;
import com.smewise.camera2.manager.Controller;
import com.smewise.camera2.ui.CameraBaseMenu;
import com.smewise.camera2.ui.CameraMenu;
import com.smewise.camera2.ui.CameraTab;
import com.smewise.camera2.ui.CoverView;
import com.smewise.camera2.utils.CameraThread;
import com.smewise.camera2.utils.FileSaver;

/**
 * Created by wenzhe on 16-3-9.
 */
public abstract class CameraModule {

    private static final String TAG = Config.getTag(CameraModule.class);

    Handler mainHandler;
    FileSaver fileSaver;
    private int mCameraState = Controller.CAMERA_MODULE_STOP;
    //flag for status
    boolean isSurfaceAvailable = false;
    boolean isCameraOpened = false;

    RelativeLayout rootView;
    CameraMenu cameraMenu;

    private Controller mController;
    Context appContext;

    public void init(Context context, Controller controller) {
        // just need init once
        if (mController != null) { return; }
        appContext = context;
        mController = controller;
        mainHandler = getToolKit().getMainHandler();
        fileSaver = getToolKit().getFileSaver();
        rootView = controller.getBaseUI().getRootView();
        // call subclass init()
        init();
    }

    boolean isAndTrue(int param1, int param2) {
        return (param1 & param2) != 0;
    }

    private void enableState(int state) {
        mCameraState = mCameraState | state;
    }

    private void disableState(int state) {
        mCameraState = mCameraState & (~state);
    }

    boolean stateEnabled(int state) {
        return isAndTrue(mCameraState, state);
    }

    public void startModule() {
        if (isAndTrue(mCameraState, Controller.CAMERA_MODULE_STOP)) {
            disableState(Controller.CAMERA_MODULE_STOP);
            enableState(Controller.CAMERA_MODULE_PAUSE);
            start();
        }
    }

    public void stopModule() {
        if (isAndTrue(mCameraState, Controller.CAMERA_MODULE_PAUSE)) {
            disableState(Controller.CAMERA_MODULE_PAUSE);
            enableState(Controller.CAMERA_MODULE_STOP);
            stop();
        }
    }

    public void pauseModule() {
        if (isAndTrue(mCameraState, Controller.CAMERA_MODULE_RUNNING)) {
            disableState(Controller.CAMERA_MODULE_RUNNING);
            enableState(Controller.CAMERA_MODULE_PAUSE);
            pause();
        }
    }

    public void resumeModule() {
        if (isAndTrue(mCameraState, Controller.CAMERA_MODULE_PAUSE)) {
            disableState(Controller.CAMERA_MODULE_PAUSE);
            enableState(Controller.CAMERA_MODULE_RUNNING);
            resume();
        }
    }

    protected abstract void init();

    protected abstract void start();

    protected abstract void stop();

    protected abstract void pause();

    protected abstract void resume();

    protected void addModuleView(View view) {
        if (rootView.getChildAt(0) != view) {
            if (rootView.getChildCount() > 0) {
                rootView.removeAllViews();
            }
            rootView.addView(view);
        }
    }

    private void setCameraState(int state) {
        mCameraState = state;
        Log.d(TAG, "camera state:" + mCameraState);
    }

    protected int getCameraState() {
        return mCameraState;
    }

    void setNewModule(int index) {
        mController.getBaseUI().changeModule(index);
    }

    CameraToolKit getToolKit() {
        return mController.getToolKit();
    }

    CoverView getCoverView() {
        return mController.getBaseUI().getCoverView();
    }

    protected CameraTab getTabView() {
        return mController.getBaseUI().getCameraTab();
    }

    protected CameraSettings getSettingManager() {
        return mController.getSettingManager();
    }

    CameraThread getCameraThread() {
        return getToolKit().getCameraThread();
    }

    protected void runOnUiThread(Runnable runnable) {
        getToolKit().getMainHandler().post(runnable);
    }

    void showSetting(boolean stopModule) {
        mController.showSetting(stopModule);
    }

    void setCameraMenu(int resId, CameraBaseMenu.OnMenuClickListener listener) {
        cameraMenu = new CameraMenu(appContext, resId, listener);
    }

}
