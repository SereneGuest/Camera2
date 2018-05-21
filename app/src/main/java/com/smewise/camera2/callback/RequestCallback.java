package com.smewise.camera2.callback;

import android.hardware.camera2.CaptureResult;

public abstract class RequestCallback {
    public void onDataBack(byte[] data, int width, int height) {
        // default empty implementation
    }

    public void onRequestComplete() {
        // default empty implementation
    }

    public void onViewChange(int width, int height) {
        // default empty implementation
    }

    public void onAFStateChanged(int state) {
        // default empty implementation
    }
}
