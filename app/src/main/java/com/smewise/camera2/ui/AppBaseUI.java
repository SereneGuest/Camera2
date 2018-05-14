package com.smewise.camera2.ui;

import android.app.Activity;
import android.support.design.widget.TabLayout;
import android.widget.RelativeLayout;

import com.smewise.camera2.R;
import com.smewise.camera2.manager.Controller;
import com.smewise.camera2.manager.ModuleManager;

/**
 * Created by wenzhe on 9/15/17.
 */

public class AppBaseUI implements TabLayout.OnTabSelectedListener {
    private CoverView mCoverView;
    private CameraTab mCameraTab;
    private Controller mController;
    private RelativeLayout mRootView;

    public AppBaseUI(Activity activity, Controller controller) {
        mController = controller;
        mCoverView = activity.findViewById(R.id.cover_view);
        mCameraTab = activity.findViewById(R.id.tab_view);
        mCameraTab.setSelected(ModuleManager.getCurrentIndex());
        mCameraTab.addOnTabSelectedListener(this);
        mRootView = activity.findViewById(R.id.container);
    }

    public RelativeLayout getRootView() {
        return mRootView;
    }

    public CoverView getCoverView() {
        return mCoverView;
    }

    public CameraTab getCameraTab() {
        return mCameraTab;
    }

    public void changeModule(int index) {
        mCameraTab.setSelected(index);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (tab.getPosition() != ModuleManager.getCurrentIndex()) {
            mCoverView.setMode(tab.getPosition());
            mController.changeModule(tab.getPosition());
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}
