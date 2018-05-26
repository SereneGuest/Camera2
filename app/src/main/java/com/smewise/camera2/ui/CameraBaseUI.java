package com.smewise.camera2.ui;

import android.view.View;
import android.widget.RelativeLayout;

import com.smewise.camera2.callback.CameraUiEvent;
import com.smewise.camera2.manager.ModuleManager;

/**
 * Created by wenzhe on 3/3/17.
 */

public abstract class CameraBaseUI implements GestureTextureView.GestureListener, View
        .OnClickListener {
    private final String TAG = this.getClass().getSimpleName();
    CameraUiEvent uiEvent;
    int frameCount = 0;
    private CoverView mCoverView;

    CameraBaseUI(CameraUiEvent event) {
        uiEvent = event;
    }

    public abstract RelativeLayout getRootView();

    public void setUIClickable(boolean clickable) {
    }

    public void resetFrameCount() {
        frameCount = 0;
    }

    public void setCoverView(CoverView coverView) {
        mCoverView = coverView;
    }

    /* View.OnClickListener*/
    @Override
    public void onClick(View v) {
        uiEvent.onAction(CameraUiEvent.ACTION_CLICK, v);
    }

    /* GestureTextureView.GestureListener */
    @Override
    public void onClick(float x, float y) {
        uiEvent.onTouchToFocus(x, y);
    }

    @Override
    public void onSwipeLeft() {
        int newIndex = ModuleManager.getCurrentIndex() + 1;
        if (ModuleManager.isValidIndex(newIndex)) {
            mCoverView.setAlpha(1.0f);
            uiEvent.onAction(CameraUiEvent.ACTION_CHANGE_MODULE, newIndex);
        }
    }

    @Override
    public void onSwipeRight() {
        int newIndex = ModuleManager.getCurrentIndex() - 1;
        if (ModuleManager.isValidIndex(newIndex)) {
            mCoverView.setAlpha(1.0f);
            uiEvent.onAction(CameraUiEvent.ACTION_CHANGE_MODULE, newIndex);
        }
    }

    @Override
    public void onSwipe(float percent) {
        int newIndex;
        if (percent < 0) {
            newIndex = ModuleManager.getCurrentIndex() + 1;
        } else {
            newIndex = ModuleManager.getCurrentIndex() - 1;
        }
        if (ModuleManager.isValidIndex(newIndex)) {
            mCoverView.setMode(newIndex);
            mCoverView.setAlpha(Math.abs(percent));
            mCoverView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCancel() {
        mCoverView.setVisibility(View.GONE);
        mCoverView.setAlpha(1.0f);
    }
}
