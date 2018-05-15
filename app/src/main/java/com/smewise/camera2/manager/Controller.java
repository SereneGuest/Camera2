package com.smewise.camera2.manager;

import android.app.FragmentManager;

import com.smewise.camera2.ui.AppBaseUI;

public interface Controller {
    int CAMERA_STATE_STOP = 0x10;
    int CAMERA_STATE_PAUSE = 0x11;
    int CAMERA_STATE_RUNNING = 0x12;

    void changeModule(int module);

    CameraToolKit getToolKit();

    FragmentManager getFragmentManager();

    void showSetting(boolean stopModule);

    CameraSettings getSettingManager();

    AppBaseUI getBaseUI();
}
