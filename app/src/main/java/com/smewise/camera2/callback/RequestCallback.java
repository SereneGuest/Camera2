package com.smewise.camera2.callback;


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

    public void onRecordStarted(boolean success) {
        // default empty implementation
    }

    public void onRecordStopped(String filePath, int width, int height) {
        // default empty implementation
    }
}
