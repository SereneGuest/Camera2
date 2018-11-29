package com.smewise.camera2.manager;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.util.Log;

import com.smewise.camera2.Config;

public class RequestManager {

    private final String TAG = Config.getTag(RequestManager.class);

    private CameraCharacteristics mCharacteristics;
    private MeteringRectangle[] mFocusArea;
    private MeteringRectangle[] mMeteringArea;
    // for reset AE/AF metering area
    private MeteringRectangle[] mResetRect = new MeteringRectangle[] {
            new MeteringRectangle(0, 0, 0, 0, 0)
    };

    public void setCharacteristics(CameraCharacteristics characteristics) {
        mCharacteristics = characteristics;
    }

    public CaptureRequest getPreviewRequest(CaptureRequest.Builder builder) {
        int afMode = getValidAFMode(CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        int antiBMode = getValidAntiBandingMode(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_AUTO);
        builder.set(CaptureRequest.CONTROL_AF_MODE, afMode);
        builder.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, antiBMode);
        builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE);
        return builder.build();
    }

    public CaptureRequest getTouch2FocusRequest(CaptureRequest.Builder builder,
            MeteringRectangle focus, MeteringRectangle metering) {
        int afMode = getValidAFMode(CaptureRequest.CONTROL_AF_MODE_AUTO);
        builder.set(CaptureRequest.CONTROL_AF_MODE, afMode);
        builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        if (mFocusArea == null) {
            mFocusArea = new MeteringRectangle[] {focus};
        } else {
            mFocusArea[0] = focus;
        }
        if (mMeteringArea == null) {
            mMeteringArea = new MeteringRectangle[] {metering};
        } else {
            mMeteringArea[0] = metering;
        }
        if (isMeteringSupport(true)) {
            builder.set(CaptureRequest.CONTROL_AF_REGIONS, mFocusArea);
        }
        if (isMeteringSupport(false)) {
            builder.set(CaptureRequest.CONTROL_AE_REGIONS, mMeteringArea);
        }
        builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE);
        return builder.build();
    }

    public CaptureRequest getFocusModeRequest(CaptureRequest.Builder builder, int focusMode) {
        int afMode = getValidAFMode(focusMode);
        builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        builder.set(CaptureRequest.CONTROL_AF_MODE, afMode);
        builder.set(CaptureRequest.CONTROL_AF_REGIONS, mResetRect);
        builder.set(CaptureRequest.CONTROL_AE_REGIONS, mResetRect);
        // cancel af trigger
        builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE);
        return builder.build();
    }

    public CaptureRequest getStillPictureRequest(CaptureRequest.Builder builder, int rotation) {
        builder.set(CaptureRequest.JPEG_ORIENTATION, rotation);
        return builder.build();
    }

    public CaptureRequest getFocusDistanceRequest(CaptureRequest.Builder builder, float distance) {
        int afMode = getValidAFMode(CaptureRequest.CONTROL_AF_MODE_OFF);
            // preview
        builder.set(CaptureRequest.CONTROL_AF_MODE, afMode);
        float miniDistance = getMinimumDistance();
        if (miniDistance > 0) {
            builder.set(CaptureRequest.LENS_FOCUS_DISTANCE, miniDistance * distance);
        }
        return builder.build();
    }

    public CaptureRequest getFlashRequest(CaptureRequest.Builder builder, String value) {
        if (!isFlashSupport()) {
            Log.w(TAG, " not support flash");
            return builder.build();
        }
        switch (value) {
            case CameraSettings.FLASH_VALUE_ON:
                builder.set(CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
                builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_SINGLE);
                break;
            case CameraSettings.FLASH_VALUE_OFF:
                builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                break;
            case CameraSettings.FLASH_VALUE_AUTO:
                builder.set(CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_SINGLE);
                break;
            case CameraSettings.FLASH_VALUE_TORCH:
                builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
                break;
            default:
                Log.e(TAG, "error value for flash mode");
                break;
        }
        builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE);
        return builder.build();
    }

    public void applyFlashRequest(CaptureRequest.Builder builder, String value) {
        if (!isFlashSupport()) {
            Log.w(TAG, " not support flash");
            return;
        }
        switch (value) {
            case CameraSettings.FLASH_VALUE_ON:
                builder.set(CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
                builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_SINGLE);
                break;
            case CameraSettings.FLASH_VALUE_OFF:
                builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                break;
            case CameraSettings.FLASH_VALUE_AUTO:
                builder.set(CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_SINGLE);
                break;
            case CameraSettings.FLASH_VALUE_TORCH:
                builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
                break;
            default:
                Log.e(TAG, "error value for flash mode");
                break;
        }
    }

    /* ------------------------- private function------------------------- */
    private int getValidAFMode(int targetMode) {
        int[] allAFMode = mCharacteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
        for (int mode : allAFMode) {
            if (mode == targetMode) {
                return targetMode;
            }
        }
        Log.i(TAG, "not support af mode:" + targetMode + " use mode:" + allAFMode[0]);
        return allAFMode[0];
    }

    private int getValidAntiBandingMode(int targetMode) {
        int[] allABMode = mCharacteristics.get(
                CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES);
        for (int mode : allABMode) {
            if (mode == targetMode) {
                return targetMode;
            }
        }
        Log.i(TAG, "not support anti banding mode:" + targetMode
                + " use mode:" + allABMode[0]);
        return allABMode[0];
    }

    private boolean isMeteringSupport(boolean focusArea) {
        int regionNum;
        if (focusArea) {
           regionNum = mCharacteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF);
        } else {
            regionNum = mCharacteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE);
        }
        return regionNum > 0;
    }

    private float getMinimumDistance() {
        Float distance = mCharacteristics.get(
                CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
        if (distance == null) {
            return 0;
        }
        return distance;
    }

    private boolean isFlashSupport() {
        Boolean support = mCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
        return support != null && support;
    }

    boolean canTriggerAf() {
        int[] allAFMode = mCharacteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
        return  allAFMode != null && allAFMode.length > 1;
    }

}
