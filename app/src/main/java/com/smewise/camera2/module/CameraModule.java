package com.smewise.camera2.module;

import android.content.Context;
import android.graphics.ImageFormat;
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
import com.smewise.camera2.ui.CoverView;
import com.smewise.camera2.utils.FileSaver;
import com.smewise.camera2.utils.JobExecutor;
import com.smewise.camera2.utils.MediaFunc;

/**
 * Created by wenzhe on 16-3-9.
 */
public abstract class CameraModule {

    private static final String TAG = Config.getTag(CameraModule.class);

    Handler mainHandler;
    FileSaver fileSaver;
    private int mCameraState = Controller.CAMERA_MODULE_STOP;

    RelativeLayout rootView;

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
        getExecutor().execute(new JobExecutor.Task<Void>() {
            @Override
            public Void run() {
                int format = getSettings().getPicFormat(cameraId, formatKey);
                int saveType = MediaFunc.MEDIA_TYPE_IMAGE;
                if (format != ImageFormat.JPEG) {
                    saveType = MediaFunc.MEDIA_TYPE_YUV;
                }
                fileSaver.saveFile(width, height, getToolKit().getOrientation(), data, tag, saveType);
                return super.run();
            }
        });
    }

    void setNewModule(int index) {
        mController.changeModule(index);
        getBaseUI().getIndicatorView().select(index);
    }

    CameraToolKit getToolKit() {
        return mController.getToolKit();
    }

    CoverView getCoverView() {
        return mController.getBaseUI().getCoverView();
    }


    protected CameraSettings getSettings() {
        return mController.getCameraSettings(appContext);
    }

    JobExecutor getExecutor() {
        return getToolKit().getExecutor();
    }

    AppBaseUI getBaseUI() {
        return mController.getBaseUI();
    }

    protected void runOnUiThread(Runnable runnable) {
        getToolKit().getMainHandler().post(runnable);
    }

    protected void runOnUiThreadDelay(Runnable runnable, long delay) {
        getToolKit().getMainHandler().postDelayed(runnable, delay);
    }

    void showSetting() {
        mController.showSetting();
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
