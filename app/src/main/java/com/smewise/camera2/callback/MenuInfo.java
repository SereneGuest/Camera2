package com.smewise.camera2.callback;

/**
 * Interface for get information need for show menu
 */
public interface MenuInfo {
    String[] getCameraIdList();

    String getCurrentCameraId();

    String getCurrentValue(String key);

}
