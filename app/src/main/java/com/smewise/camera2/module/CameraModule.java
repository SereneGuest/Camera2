package com.smewise.camera2.module;

import android.app.FragmentManager;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.RelativeLayout;

import com.smewise.camera2.Config;
import com.smewise.camera2.manager.CameraSettings;
import com.smewise.camera2.manager.CameraToolKit;
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
    //flag for status
    boolean isSurfaceAvailable = false;
    boolean isCameraOpened = false;
    boolean isFirstPreviewLoaded = false;
    boolean isModulePause = true;

    RelativeLayout rootView;
    CameraMenu cameraMenu;

    public interface Controller {
        void changeModule(int module);

        CameraToolKit getToolKit();

        FragmentManager getFragmentManager();

        void showSetting(boolean stopModule);

        CameraSettings getSettingManager();
    }

    private Controller mController;
    Context appContext;

    public void init(Context context, Controller controller) {
        // no need re init
        if(rootView != null) {return;}
        appContext = context;
        mController = controller;
        mainHandler = getToolKit().getMainHandler();
        fileSaver = getToolKit().getFileSaver();
        rootView = getToolKit().getBaseUI().getRootView();
        init();
    }

    protected abstract void init();

    public abstract void start();

    public abstract void stop();

    void setNewModule(int index) {
        getToolKit().getBaseUI().changeModule(index);
    }

    CameraToolKit getToolKit() {
        return mController.getToolKit();
    }

    CoverView getCoverView() {
        return getToolKit().getCoverView();
    }

    protected CameraTab getTabView() {
        return getToolKit().getTabView();
    }

    protected CameraSettings getSettingManager() {
        return mController.getSettingManager();
    }

    CameraThread getCameraThread() {
        return getToolKit().getCameraThread();
    }

    protected void hideCoverView() {
        if (!isFirstPreviewLoaded) {
            Log.d(TAG, "hide cover view");
            getCoverView().hideCoverWithAnimation();
            isFirstPreviewLoaded = true;
        }
    }

    protected void runOnUiThread(Runnable runnable) {
        getToolKit().getMainHandler().post(runnable);
    }

    void showSetting() {
        mController.showSetting(true);
    }

    void setCameraMenu(int resId) {
        cameraMenu = new CameraMenu(appContext, resId);
    }

}
