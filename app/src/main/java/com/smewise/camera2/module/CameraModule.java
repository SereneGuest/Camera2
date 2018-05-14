package com.smewise.camera2.module;

import android.content.Context;
import android.os.Handler;
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
    //flag for status
    boolean isSurfaceAvailable = false;
    boolean isCameraOpened = false;
    boolean isModulePause = true;

    RelativeLayout rootView;
    CameraMenu cameraMenu;

    private Controller mController;
    Context appContext;

    public void init(Context context, Controller controller) {
        // just need init once
        if(mController != null) {return;}
        appContext = context;
        mController = controller;
        mainHandler = getToolKit().getMainHandler();
        fileSaver = getToolKit().getFileSaver();
        rootView = controller.getBaseUI().getRootView();
        // call subclass init()
        init();
    }

    protected abstract void init();

    public abstract void start();

    public abstract void stop();

    protected void addModuleView(View view) {
        if (rootView.getChildAt(0) != view) {
            if (rootView.getChildCount() > 0) {
                rootView.removeAllViews();
            }
            rootView.addView(view);
        }
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
