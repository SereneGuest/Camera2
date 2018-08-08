package com.smewise.camera2.manager;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.os.Handler;
import android.util.ArrayMap;
import android.util.Log;

import com.smewise.camera2.Config;
import com.smewise.camera2.utils.CameraCapability;


/**
 * Created by wenzhe on 1/18/17.
 */

public class RequestHelper {
    private static final String TAG = Config.getTag(RequestHelper.class);
    private Handler mHandler;
    private CameraCapability mCapability;
    private CameraCaptureSession mSession;
    private ArrayMap<CaptureRequest.Key, Object> mPreviewSettings;
    private ArrayMap<CaptureRequest.Key, Object> mCaptureSettings;
    // for reset AE/AF metering area
    private MeteringRectangle mResetRect = new MeteringRectangle(0, 0, 0, 0, 0);

    public RequestHelper(Context context, Handler handler) {
        mCapability = new CameraCapability(context);
        mHandler = handler;
        mPreviewSettings = new ArrayMap<>();
        mCaptureSettings = new ArrayMap<>();
    }

    public void setCameraCaptureSession(CameraCaptureSession session) {
        mSession = session;
    }


    public void applyPreviewRequest(CaptureRequest.Builder builder,
            CameraCaptureSession.CaptureCallback captureCallback) {
        for (ArrayMap.Entry<CaptureRequest.Key, Object> entry : mPreviewSettings.entrySet()) {
            checkAndSetRequest(entry.getKey(), entry.getValue(), builder);
        }
        try {
            mSession.setRepeatingRequest(builder.build(), captureCallback, mHandler);
        } catch (CameraAccessException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void applyCaptureRequest(CaptureRequest.Builder builder,
            CameraCaptureSession.CaptureCallback captureCallback) {
        for (ArrayMap.Entry<CaptureRequest.Key, Object> entry : mCaptureSettings.entrySet()) {
            checkAndSetRequest(entry.getKey(), entry.getValue(), builder);
        }
        try {
            mSession.capture(builder.build(), captureCallback, mHandler);
        } catch (CameraAccessException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void setFlashRequest(String value) {
        switch (value) {
            case CameraSettings.FLASH_VALUE_ON:
                mCaptureSettings.put(CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
                mCaptureSettings.remove(CaptureRequest.FLASH_MODE);
                break;
            case CameraSettings.FLASH_VALUE_OFF:
                mCaptureSettings.put(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                mCaptureSettings.put(CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON);
                mPreviewSettings.put(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                mPreviewSettings.put(CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON);
                break;
            case CameraSettings.FLASH_VALUE_AUTO:
                mPreviewSettings.put(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                mCaptureSettings.put(CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                mCaptureSettings.remove(CaptureRequest.FLASH_MODE);
                break;
            case CameraSettings.FLASH_VALUE_TORCH:
                mPreviewSettings.put(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
                mCaptureSettings.put(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
                mPreviewSettings.put(CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON);
                break;
            default:
                Log.e(TAG, "error value for flash mode");
                break;
        }
    }

    public void setPreviewRequest() {
        mPreviewSettings.put(CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        mPreviewSettings.put(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE,
                CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_AUTO);
        mPreviewSettings.put(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        mPreviewSettings.put(CaptureRequest.CONTROL_AF_TRIGGER,
                CaptureRequest.CONTROL_AF_TRIGGER_IDLE);
    }

    public void setControlAfAeRequest(MeteringRectangle focusRect, MeteringRectangle meteringRect) {
        // repeating
        MeteringRectangle[] focusArea = new MeteringRectangle[]{focusRect};
        MeteringRectangle[] meteringArea = new MeteringRectangle[]{meteringRect};
        mPreviewSettings.put(CaptureRequest.CONTROL_AF_REGIONS, focusArea);
        mPreviewSettings.put(CaptureRequest.CONTROL_AE_REGIONS, meteringArea);
        mPreviewSettings.put(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
        mPreviewSettings.put(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        // capture
        mCaptureSettings.put(CaptureRequest.CONTROL_AF_TRIGGER,
                CaptureRequest.CONTROL_AF_TRIGGER_START);
    }

    public void setControlFocusModeRequest(int focusMode) {
        Log.d(TAG, "focusMode:" + focusMode);
        mPreviewSettings.put(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        mPreviewSettings.put(CaptureRequest.CONTROL_AF_MODE, focusMode);
        MeteringRectangle[] rect = new MeteringRectangle[]{mResetRect};
        mPreviewSettings.put(CaptureRequest.CONTROL_AF_REGIONS, rect);
        mPreviewSettings.put(CaptureRequest.CONTROL_AE_REGIONS, rect);

        // cancel af trigger
        mCaptureSettings.put(CaptureRequest.CONTROL_AF_TRIGGER,
                CaptureRequest.CONTROL_AF_TRIGGER_CANCEL);

    }

    public void setCaptureRequest(int jpegRotation) {
        mCaptureSettings.put(CaptureRequest.JPEG_ORIENTATION, jpegRotation);
        mCaptureSettings.put(CaptureRequest.CONTROL_AF_TRIGGER,
                CaptureRequest.CONTROL_AF_TRIGGER_IDLE);
    }

    public void setControlFocusDistanceRequest(float value) {
        // preview
        mPreviewSettings.put(CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_OFF);
        mPreviewSettings.put(CaptureRequest.LENS_FOCUS_DISTANCE, value);
        // capture
        mCaptureSettings.put(CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_OFF);
        mCaptureSettings.put(CaptureRequest.LENS_FOCUS_DISTANCE, value);
    }

    private <T> void checkAndSetRequest(CaptureRequest.Key<T> key, T value,
            CaptureRequest.Builder builder) {
        String cameraId = mSession.getDevice().getId();
        if (key == CaptureRequest.CONTROL_AF_MODE) {
            int afMode = mCapability.getSupportedAFMode(cameraId, (Integer) value);
            builder.set(CaptureRequest.CONTROL_AF_MODE, afMode);
        } else if (key == CaptureRequest.CONTROL_AE_ANTIBANDING_MODE) {
            int antiBandingMode = mCapability.getSupportedAntiBandingMode(cameraId, (Integer) value);
            if (antiBandingMode != -1) {
                builder.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, antiBandingMode);
            }
        } else if (key == CaptureRequest.CONTROL_MODE) {
            // TODO need query supported mode
            builder.set(CaptureRequest.CONTROL_MODE, (Integer) value);
        } else if (key == CaptureRequest.CONTROL_AE_REGIONS) {
            if (mCapability.isAFRegionSupported(cameraId)) {
                builder.set(CaptureRequest.CONTROL_AF_REGIONS, (MeteringRectangle[]) value);
            } else {
                Log.d(TAG, "not support af region camera id:" + cameraId);
            }
        } else if (key == CaptureRequest.CONTROL_AF_REGIONS) {
            if (mCapability.isAERegionSupported(mSession.getDevice().getId())) {
                builder.set(CaptureRequest.CONTROL_AE_REGIONS, (MeteringRectangle[]) value);
            } else {
                Log.d(TAG, "not support ae region id:" + mSession.getDevice().getId());
            }
        } else if (key == CaptureRequest.LENS_FOCUS_DISTANCE) {
            Float focusLength = mCapability.getMiniMumFocusDistance(cameraId);
            if (focusLength != null && focusLength > 0) {
                builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
                builder.set(CaptureRequest.LENS_FOCUS_DISTANCE, (Float) value * focusLength);
            } else {
                Log.d(TAG, "not support control focus distance id:" + cameraId);
            }
        } else {
            builder.set(key, value);
        }
    }

}
