package com.smewise.camera2.callback;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.CaptureRequest;
import android.support.annotation.Nullable;

public abstract class CameraUiEvent {

    public static final String ACTION_CLICK = "camera.action.click";
    public static final String ACTION_CHANGE_MODULE = "camera.action.change.module";
    public static final String ACTION_SWITCH_CAMERA = "camera.action.switch.camera";
    public static final String ACTION_PREVIEW_READY = "camera.action.preview.ready";

    public void onPreviewUiReady(SurfaceTexture mainSurface, @Nullable SurfaceTexture auxSurface) {
        // default empty implementation
    }

    public void onPreviewUiDestroy() {
        // default empty implementation
    }

    public void onTouchToFocus(float x, float y) {
        // default empty implementation
    }

    public void resetTouchToFocus() {
        // default empty implementation
    }

    public <T> void onSettingChange(CaptureRequest.Key<T> key, T value) {
        // default empty implementation
    }

    public <T> void onAction(String type, T value) {
        // default empty implementation
    }
}
