package com.smewise.camera2.manager;

import android.app.FragmentManager;

import com.smewise.camera2.ui.AppBaseUI;

public interface Controller {

    void changeModule(int module);

    CameraToolKit getToolKit();

    FragmentManager getFragmentManager();

    void showSetting(boolean stopModule);

    CameraSettings getSettingManager();

    AppBaseUI getBaseUI();
}
