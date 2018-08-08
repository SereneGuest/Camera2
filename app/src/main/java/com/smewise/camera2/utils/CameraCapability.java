package com.smewise.camera2.utils;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.util.Log;

import com.smewise.camera2.Config;

/**
 * Created by wenzhe on 9/21/17.
 */

public class CameraCapability {

    private static final String TAG = Config.getTag(CameraCapability.class);

    private CameraManager mManager;

    public CameraCapability(Context context) {
        mManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    private CameraCharacteristics getCharacteristics(String id) {
        try {
            return mManager.getCameraCharacteristics(id);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private int[] getAvailableAFMode(String cameraId) {
        return getCharacteristics(cameraId).get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
    }

    public int getSupportedAFMode(String cameraId, int targetMode) {
        int[] listSupported = getAvailableAFMode(cameraId);
        for (int support : listSupported) {
            if (support == targetMode) {
                return targetMode;
            }
        }
        Log.d(TAG, "not support af mode : " + targetMode);
        return listSupported[0];
    }

    public boolean isAERegionSupported(String cameraId) {
        Integer aeRegionNum = getCharacteristics(cameraId).get(CameraCharacteristics
                .CONTROL_MAX_REGIONS_AE);
        return aeRegionNum != null && aeRegionNum > 0;
    }

    public boolean isAFRegionSupported(String cameraId) {
        Integer afRegionNum = getCharacteristics(cameraId).get(CameraCharacteristics
                .CONTROL_MAX_REGIONS_AF);
        return afRegionNum != null && afRegionNum > 0;
    }

    public int getSupportedAntiBandingMode(String cameraId, int targetMode) {
        int[] listSupported = getCharacteristics(cameraId).get(CameraCharacteristics
                .CONTROL_AE_AVAILABLE_ANTIBANDING_MODES);
        if (listSupported == null) {
            return -1;
        }
        for (int support : listSupported) {
            if (support == targetMode) {
                return targetMode;
            }
        }
        Log.d(TAG, "not support anti banding mode : " + targetMode);
        return listSupported[0];
    }

    public Float getMiniMumFocusDistance(String cameraId) {
        return getCharacteristics(cameraId).get(CameraCharacteristics
                .LENS_INFO_MINIMUM_FOCUS_DISTANCE);
    }

}
