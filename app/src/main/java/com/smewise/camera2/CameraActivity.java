package com.smewise.camera2;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.smewise.camera2.manager.Controller;
import com.smewise.camera2.module.CameraFragment;
import com.smewise.camera2.module.SettingFragment;
import com.smewise.camera2.utils.Permission;
import com.smewise.camera2.utils.PermissionDialog;


public class CameraActivity extends AppCompatActivity {

    private static final String TAG = Config.getTag(CameraActivity.class);
    private CameraFragment mCameraFragment;
    public static final String SETTING_ACTION = "com.smewise.camera2.setting";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowFlag();
        setContentView(R.layout.camera_main_layout);
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
        Permission.checkPermission(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Permission.isPermissionGranted(this)) {
            initCameraFragment();
            if (isSettingShortcut()) {
                addSettingFragment();
                getIntent().setAction(null);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public Controller getController() {
        return mCameraFragment.getController();
    }

    private void initCameraFragment() {
        if (mCameraFragment == null) {
            mCameraFragment = new CameraFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.app_root, mCameraFragment);
            transaction.commit();
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
        }
    }

    private void showPermissionDenyDialog() {
        PermissionDialog dialog = new PermissionDialog();
        dialog.show(getFragmentManager(), "PermissionDeny");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void removeSettingFragment() {
        String tag = SettingFragment.class.getSimpleName();
        Fragment settingFragment = getFragmentManager().findFragmentByTag(tag);
        if (settingFragment == null) {
            return;
        }
        if (settingFragment.isAdded()) {
            getFragmentManager().popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    public void addSettingFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.app_root, new SettingFragment(),
                SettingFragment.class.getSimpleName());
        transaction.addToBackStack(SettingFragment.class.getSimpleName());
        transaction.commit();
    }
}
