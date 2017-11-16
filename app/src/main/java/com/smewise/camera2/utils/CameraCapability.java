package com.smewise.camera2.utils;

import android.hardware.camera2.CameraCharacteristics;
import android.support.annotation.Nullable;
import android.util.Log;

import com.smewise.camera2.Config;
import com.smewise.camera2.manager.Camera2Manager;

/**
 * Created by wenzhe on 9/21/17.
 */

public class CameraCapability {

    private static final String TAG = Config.TAG_PREFIX + " CamCapability";

    private CameraCharacteristics mMainParam;
    private CameraCharacteristics mAuxParam;


    public void setCameraCharacteristics(CameraCharacteristics mainC, @Nullable
            CameraCharacteristics auxC) {
        mMainParam = mainC;
        mAuxParam = auxC;
    }

    private CameraCharacteristics getCharacteristics(String id) {
        if (mAuxParam == null) {
            return mMainParam;
        }
        if (Camera2Manager.getManager().getCameraId().equals(id)) {
            return mMainParam;
        } else {
            return mAuxParam;
        }
    }

    public int[] getAvailableAFMode(String cameraId) {
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
        int aeRegionNum = getCharacteristics(cameraId).get(CameraCharacteristics
                .CONTROL_MAX_REGIONS_AE);
        return aeRegionNum > 0;
    }

    public boolean isAFRegionSupported(String cameraId) {
        int aeRegionNum = getCharacteristics(cameraId).get(CameraCharacteristics
                .CONTROL_MAX_REGIONS_AF);
        return aeRegionNum > 0;
    }

    public int getSupportedAntiBandingMode(String cameraId, int targetMode) {
        int[] listSupported = getCharacteristics(cameraId).get(CameraCharacteristics
                .CONTROL_AE_AVAILABLE_ANTIBANDING_MODES);
        for (int support : listSupported) {
            if (support == targetMode) {
                return targetMode;
            }
        }
        Log.d(TAG, "not support anti banding mode : " + targetMode);
        return listSupported[0];
    }

    public boolean isSupportedAutoFocus(String cameraId) {
        float focusLens = getCharacteristics(cameraId).get(CameraCharacteristics
                .LENS_INFO_MINIMUM_FOCUS_DISTANCE);
        return focusLens > 0;
    }

    public float getMiniMumFocusDistance(String cameraId) {
        return getCharacteristics(cameraId).get(CameraCharacteristics
                .LENS_INFO_MINIMUM_FOCUS_DISTANCE);
    }

    public float[] getSupportedAperture(String cameraId) {
        return getCharacteristics(cameraId).get(CameraCharacteristics
                .LENS_INFO_AVAILABLE_APERTURES);
    }

    public float[] getSupportedFocusLength(String cameraId) {
        float[] focusLens = getCharacteristics(cameraId).get(CameraCharacteristics
                .LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
        for (float length : focusLens) {
            Log.d(TAG, "support lens: " + length);
        }
        return focusLens;
    }
}
