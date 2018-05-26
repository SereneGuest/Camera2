package com.smewise.camera2.module;

import android.content.Context;
import android.hardware.camera2.CaptureResult;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout;

import com.smewise.camera2.Config;
import com.smewise.camera2.manager.CameraSettings;
import com.smewise.camera2.manager.CameraToolKit;
import com.smewise.camera2.manager.Controller;
import com.smewise.camera2.manager.FocusOverlayManager;
import com.smewise.camera2.ui.AppBaseUI;
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

    void enableState(int state) {
        mCameraState = mCameraState | state;
    }

    void disableState(int state) {
        mCameraState = mCameraState & (~state);
    }

    boolean stateEnabled(int state) {
        return isAndTrue(mCameraState, state);
    }

    public void startModule() {
        if (isAndTrue(mCameraState, Controller.CAMERA_MODULE_STOP)) {
            disableState(Controller.CAMERA_MODULE_STOP);
            enableState(Controller.CAMERA_MODULE_RUNNING);
            start();
        }
    }

    public void stopModule() {
        if (isAndTrue(mCameraState, Controller.CAMERA_MODULE_RUNNING)) {
            disableState(Controller.CAMERA_MODULE_RUNNING);
            enableState(Controller.CAMERA_MODULE_STOP);
            stop();
        }
    }

    protected abstract void init();

    protected abstract void start();

    protected abstract void stop();

    void addModuleView(View view) {
        if (rootView.getChildAt(0) != view) {
            if (rootView.getChildCount() > 1) {
                rootView.removeViewAt(0);
            }
            rootView.addView(view, 0);
        }
    }

    void saveFile(final byte[] data, final int width, final int height, final String cameraId,
                  final String formatKey, final String tag) {
        getCameraThread().post(new Runnable() {
            @Override
            public void run() {
                int format = getSettingManager().getPicFormat(cameraId, formatKey);
                fileSaver.saveFile(width, height, getToolKit().getOrientation(), data, tag, format);
            }
        });
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

    AppBaseUI getBaseUI() {
        return mController.getBaseUI();
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

    void updateAFState(int state, FocusOverlayManager overlayManager) {
        switch (state) {
            case CaptureResult.CONTROL_AF_STATE_ACTIVE_SCAN:
                overlayManager.startFocus();
                break;
            case CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED:
                overlayManager.focusSuccess();
                break;
            case CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED:
                overlayManager.focusFailed();
                break;
            case CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED:
                overlayManager.focusSuccess();
                break;
            case CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN:
                overlayManager.autoFocus();
                break;
            case CaptureResult.CONTROL_AF_STATE_PASSIVE_UNFOCUSED:
                overlayManager.focusFailed();
                break;
            case CaptureResult.CONTROL_AF_STATE_INACTIVE:
                overlayManager.hideFocusUI();
                break;
        }
    }

}
