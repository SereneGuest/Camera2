package com.smewise.camera2.manager;

import android.app.FragmentManager;
import android.content.Context;

import com.smewise.camera2.ui.AppBaseUI;

public interface Controller {
    int CAMERA_MODULE_STOP = 1;
    int CAMERA_MODULE_RUNNING = 1 << 1;
    int CAMERA_STATE_OPENED = 1 << 2;
    int CAMERA_STATE_UI_READY = 1 << 3;
    int CAMERA_STATE_START_RECORD = 1 << 4;
    int CAMERA_STATE_PAUSE_RECORD = 1 << 5;


    void changeModule(int module);

    CameraToolKit getToolKit();

    void showSetting();

    CameraSettings getCameraSettings(Context context);

    AppBaseUI getBaseUI();
}
