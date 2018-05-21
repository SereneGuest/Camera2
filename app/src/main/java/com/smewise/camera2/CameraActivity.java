package com.smewise.camera2;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.smewise.camera2.manager.CameraSettings;
import com.smewise.camera2.manager.Controller;
import com.smewise.camera2.module.CameraModule;
import com.smewise.camera2.manager.CameraToolKit;
import com.smewise.camera2.manager.ModuleManager;
import com.smewise.camera2.ui.AppBaseUI;
import com.smewise.camera2.utils.Permission;
import com.smewise.camera2.utils.PermissionDialog;


public class CameraActivity extends AppCompatActivity {

    private static final String TAG = Config.getTag(CameraActivity.class);
    private CameraToolKit mToolKit;
    private ModuleManager mModuleManager;
    private AppBaseUI mBaseUI;
    private CameraSettings mSettings;
    private boolean mIsSettingShow = false;

    public static final String SETTING_ACTION = "com.smewise.camera2.setting";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mModuleManager = new ModuleManager();
        setWindowFlag();
        setContentView(R.layout.main_layout);
        mBaseUI = new AppBaseUI(this, mController);
        mToolKit = new CameraToolKit(getApplicationContext());
        mSettings = new CameraSettings(getApplicationContext());
    }

    private boolean isSettingShortcut() {
        return SETTING_ACTION.equals(getIntent().getAction());
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
        if (isSettingShortcut()) {
            showSettingFragment(false);
            getIntent().setAction(null);
        } else if (mModuleManager.getCurrentModule() != null && !mIsSettingShow) {
            mModuleManager.getCurrentModule().startModule();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mModuleManager.getCurrentModule() != null && !mIsSettingShow) {
            mModuleManager.getCurrentModule().resumeModule();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mModuleManager.getCurrentModule() != null) {
            mModuleManager.getCurrentModule().pauseModule();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mModuleManager.getCurrentModule() != null) {
            mModuleManager.getCurrentModule().stopModule();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mToolKit.destroy();
    }

    private Controller mController = new Controller() {
        @Override
        public void changeModule(int index) {
            if (mModuleManager.needChangeModule(index)) {
                mModuleManager.getCurrentModule().pauseModule();
                mModuleManager.getCurrentModule().stopModule();
                CameraModule module = mModuleManager.getNewModule();
                module.init(getApplicationContext(), this);
                module.startModule();
                module.resumeModule();
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
            showSettingFragment(stopModule);
        }

        @Override
        public CameraSettings getSettingManager() {
            return mSettings;
        }

        @Override
        public AppBaseUI getBaseUI() {
            return mBaseUI;
        }
    };

    public Controller getController() {
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
        if (requestCode == Permission.REQUEST_CODE) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    showPermissionDenyDialog();
                    return;
                }
            }
            initCameraModule();
            // after granted permission, onResume() will call,
            // so we need start module before onResume()
            if (!isSettingShortcut() && !mIsSettingShow) {
                mModuleManager.getCurrentModule().startModule();
            }
        }
    }

    private void showPermissionDenyDialog() {
        PermissionDialog dialog = new PermissionDialog();
        dialog.show(getFragmentManager(), "PermissionDeny");
    }

    @Override
    public void onBackPressed() {
        if (mToolKit.getSettingFragment().isAdded()) {
            removeSettingFragment();
            return;
        }
        super.onBackPressed();
    }

    public void removeSettingFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.remove(mToolKit.getSettingFragment());
        transaction.commit();
        if (mModuleManager.getCurrentModule() != null) {
            mModuleManager.getCurrentModule().startModule();
            mModuleManager.getCurrentModule().resumeModule();
        }
        mIsSettingShow = false;
    }

    public void showSettingFragment(boolean stopModule) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.setting_container, mToolKit.getSettingFragment());
        transaction.commit();
        if (mModuleManager.getCurrentModule() != null) {
            mModuleManager.getCurrentModule().pauseModule();
            if (stopModule) {
                mModuleManager.getCurrentModule().stopModule();
            }
        }
        mIsSettingShow = true;
    }
}
