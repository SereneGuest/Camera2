package com.smewise.camera2;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.smewise.camera2.manager.Camera2Manager;
import com.smewise.camera2.manager.CameraSettings;
import com.smewise.camera2.module.CameraModule;
import com.smewise.camera2.manager.CameraToolKit;
import com.smewise.camera2.manager.ModuleManager;
import com.smewise.camera2.ui.AppBaseUI;
import com.smewise.camera2.utils.Permission;


public class CameraActivity extends AppCompatActivity {

    private static final String TAG = Config.getTag(CameraActivity.class);
    private CameraToolKit mToolKit;
    private ModuleManager mModuleManager;
    private AppBaseUI mBaseUI;
    private CameraSettings mSettings;
    private boolean mIsSettingShow = false;
    private boolean mOpenSettingFromShortcut = false;

    public static final String SETTING_ACTION = "com.smewise.camera2.setting";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mModuleManager = new ModuleManager();
        setWindowFlag();
        setContentView(R.layout.main_layout);
        mBaseUI = new AppBaseUI(this, mController);
        mToolKit = new CameraToolKit(getApplicationContext(), mBaseUI);
        mSettings = new CameraSettings(getApplicationContext());
        if (SETTING_ACTION.equals(getIntent().getAction())) {
            mOpenSettingFromShortcut = true;
        }
    }

    private void setWindowFlag() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        WindowManager.LayoutParams params = window.getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE;
        window.setAttributes(params);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Permission.checkPermission(this)) {
            initCameraModule();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startModule();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopModule();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mToolKit.destroy();
        Camera2Manager.getManager().destroy();
    }

    private void startModule() {
        // use shortcut open setting
        if (mOpenSettingFromShortcut) {
            mOpenSettingFromShortcut = false;
            mController.showSetting(false);
            return;
        }
        // if setting ui is show, ignore life circle
        if (mModuleManager.getCurrentModule() != null
                && !mIsSettingShow) {
            mModuleManager.getCurrentModule().start();
        }
    }

    private void stopModule() {
        // if setting ui is show, ignore life circle
        if (mModuleManager.getCurrentModule() != null
                && !mIsSettingShow) {
            mModuleManager.getCurrentModule().stop();
        }
    }

    private CameraModule.Controller mController = new CameraModule.Controller() {
        @Override
        public void changeModule(int index) {
            if (mModuleManager.needChangeModule(index)) {
                mModuleManager.getCurrentModule().stop();
                CameraModule module = mModuleManager.getNewModule();
                module.init(getApplicationContext(), this);
                module.start();
            }
        }

        @Override
        public CameraToolKit getToolKit() {
            return mToolKit;
        }

        @Override
        public FragmentManager getFragmentManager() {
            return CameraActivity.this.getFragmentManager();
        }

        @Override
        public void showSetting(boolean stopModule) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.setting_container, getToolKit().getSettingFragment());
            transaction.commit();
            if (stopModule) {
                stopModule();
            }
            mIsSettingShow = true;
        }

        @Override
        public CameraSettings getSettingManager() {
            return mSettings;
        }
    };

    public CameraModule.Controller getController() {
        return mController;
    }

    private void initCameraModule() {
        if (mModuleManager.getCurrentModule() == null) {
            Log.d(TAG, "init module");
            CameraModule cameraModule = mModuleManager.getNewModule();
            cameraModule.init(getApplicationContext(), mController);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Permission.REQUEST_CODE:
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        Permission.showPermissionDenyDialog(CameraActivity.this);
                        return;
                    }
                }
                initCameraModule();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mToolKit.getSettingFragment().isAdded()) {
            removeSettingFragment();
            return;
        }
        super.onBackPressed();
    }

    private void removeSettingFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.remove(mToolKit.getSettingFragment());
        transaction.commit();
        mIsSettingShow = false;
        startModule();
    }
}
