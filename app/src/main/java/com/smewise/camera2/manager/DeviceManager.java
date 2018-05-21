package com.smewise.camera2.manager;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;

import com.smewise.camera2.Config;

/**
 * Use for get basic camera info, not for open camera
 */
public class DeviceManager {
    private final String TAG = Config.getTag(DeviceManager.class);

    CameraManager cameraManager;

    public DeviceManager(Context context) {
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    public CameraCharacteristics getCharacteristics(String cameraId) {
        try {
            return cameraManager.getCameraCharacteristics(cameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String[] getCameraIdList() {
        try {
            return cameraManager.getCameraIdList();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public StreamConfigurationMap getConfigMap(String cameraId) {
        try {
            CameraCharacteristics c = cameraManager.getCameraCharacteristics(cameraId);
            return c.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * For camera open/close event
     */
    public static abstract class CameraEvent {
        public void onDeviceOpened(CameraDevice device) {
            // default empty implementation
        }

        public void onAuxDeviceOpened(CameraDevice device) {
            // default empty implementation
        }

        public void onDeviceClosed() {
            // default empty implementation
        }
    }
}
