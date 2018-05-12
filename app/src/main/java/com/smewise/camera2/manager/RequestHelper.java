package com.smewise.camera2.manager;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.os.Handler;
import android.util.Log;

import com.smewise.camera2.Config;
import com.smewise.camera2.utils.CameraCapability;

import java.util.List;


/**
 * Created by wenzhe on 1/18/17.
 */

public class RequestHelper {
    private static final String TAG = Config.getTag(RequestHelper.class);
    private Handler mHandler;
    private CameraCapability mCapability;

    public RequestHelper(Handler backgroundHandler) {
        mHandler = backgroundHandler;
    }

    public void setCameraCapability(CameraCapability capability) {
        mCapability = capability;
    }

    public void sendPreviewRequest(CaptureRequest.Builder builder,
            CameraCaptureSession session, CameraCaptureSession.CaptureCallback captureCallback) {
        int afMode = mCapability.getSupportedAFMode(session.getDevice().getId(), CaptureRequest
                .CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        int antiBandingMode = mCapability.getSupportedAntiBandingMode(session.getDevice().getId(),
                CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_AUTO);
        try {
            if (antiBandingMode != -1) {
                builder.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, antiBandingMode);
            }
            builder.set(CaptureRequest.CONTROL_AF_MODE, afMode);
            builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
            builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE);
            session.setRepeatingRequest(builder.build(), captureCallback, mHandler);
        } catch (CameraAccessException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void sendRestartPreviewRequest(CaptureRequest.Builder builder,
           CameraCaptureSession session, CameraCaptureSession.CaptureCallback captureCallback) {
        int afMode = mCapability.getSupportedAFMode(session.getDevice().getId(), CaptureRequest
                .CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        builder.set(CaptureRequest.CONTROL_AF_MODE, afMode);
        try {
            session.setRepeatingRequest(builder.build(), captureCallback, mHandler);
        } catch (CameraAccessException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void sendControlAfAeRequest(CaptureRequest.Builder builder, MeteringRectangle
            focusRect, MeteringRectangle meteringRect, CameraCaptureSession session,
                                       CameraCaptureSession.CaptureCallback captureCallback) {
        int afMode = mCapability.getSupportedAFMode(session.getDevice().getId(), CaptureRequest
                .CONTROL_AF_MODE_AUTO);
        MeteringRectangle[] focusArea = new MeteringRectangle[]{focusRect};
        MeteringRectangle[] meteringArea = new MeteringRectangle[]{meteringRect};
        builder.set(CaptureRequest.CONTROL_AF_MODE, afMode);
        //AE
        if (mCapability.isAERegionSupported(session.getDevice().getId())) {
            builder.set(CaptureRequest.CONTROL_AE_REGIONS, meteringArea);
        } else {
            Log.d(TAG, "not support ae region id:" + session.getDevice().getId());
        }
        //AF
        if (mCapability.isAFRegionSupported(session.getDevice().getId())) {
            builder.set(CaptureRequest.CONTROL_AF_REGIONS, focusArea);
        } else {
            Log.d(TAG, "not support af region camera id:"+session.getDevice().getId());
        }
        // repeating for af ae region
        try {
            session.setRepeatingRequest(builder.build(), captureCallback, mHandler);
        } catch (CameraAccessException | IllegalStateException e) {
            e.printStackTrace();
        }
        // capture for af trigger
        builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
        try {
            session.capture(builder.build(), captureCallback, mHandler);
        } catch (CameraAccessException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void sendFocusModeRequest(CaptureRequest.Builder builder, int focusMode,
            CameraCaptureSession session, CameraCaptureSession.CaptureCallback captureCallback) {
        int afMode = mCapability.getSupportedAFMode(session.getDevice().getId(), focusMode);
        builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE);
        builder.set(CaptureRequest.CONTROL_AF_MODE, afMode);
        try {
            session.setRepeatingRequest(builder.build(), captureCallback, mHandler);
        } catch (CameraAccessException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void sendCaptureRequest(CaptureRequest.Builder builder, int rotation ,
            CameraCaptureSession session, CameraCaptureSession.CaptureCallback captureCallback) {
        try {
            builder.set(CaptureRequest.JPEG_ORIENTATION, rotation);
            session.capture(builder.build(), captureCallback, mHandler);
        } catch (CameraAccessException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void sendCaptureRequest(CaptureRequest.Builder builder, int rotation ,
                                   CameraCaptureSession session, CaptureRequest captureRequest) {
        try {
            builder.set(CaptureRequest.JPEG_ORIENTATION, rotation);
            // use same ae af mode as preview
            builder.set(CaptureRequest.CONTROL_AF_MODE,
                    captureRequest.get(CaptureRequest.CONTROL_AF_MODE));
            builder.set(CaptureRequest.CONTROL_AE_MODE,
                    captureRequest.get(CaptureRequest.CONTROL_AE_MODE));
            // if control focus distance, need set distance when capture
            builder.set(CaptureRequest.LENS_FOCUS_DISTANCE,
                    captureRequest.get(CaptureRequest.LENS_FOCUS_DISTANCE));
            session.capture(builder.build(), null, mHandler);
        } catch (CameraAccessException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public <T> void sendControlSettingRequest(CaptureRequest.Builder builder,
            CameraCaptureSession session, CameraCaptureSession.CaptureCallback captureCallback,
            CaptureRequest.Key<T> key, T value) {
        // control focus distance
        if (key.getName().equals(CaptureRequest.LENS_FOCUS_DISTANCE.getName())) {
            Float focusLength = mCapability.getMiniMumFocusDistance(session.getDevice().getId());
            if (focusLength != null && focusLength > 0) {
                builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
                builder.set(CaptureRequest.LENS_FOCUS_DISTANCE, (Float) value * focusLength);
            } else {
                Log.d(TAG, "not support control focus distance id:" + session.getDevice().getId());
            }
        }
        try {
            session.setRepeatingRequest(builder.build(), captureCallback, null);
        } catch (CameraAccessException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

}
