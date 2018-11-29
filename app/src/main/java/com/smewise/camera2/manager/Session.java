package com.smewise.camera2.manager;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Surface;

import com.smewise.camera2.Config;

import java.nio.ByteBuffer;

public abstract class Session {

    private final String TAG = Config.getTag(Session.class);

    public static final int RQ_SET_DEVICE = 1;
    public static final int RQ_START_PREVIEW = 2;
    public static final int RQ_AF_AE_REGIONS = 3;
    public static final int RQ_FOCUS_MODE = 4;
    public static final int RQ_FOCUS_DISTANCE = 5;
    public static final int RQ_FLASH_MODE = 6;
    public static final int RQ_RESTART_PREVIEW = 7;
    public static final int RQ_TAKE_PICTURE = 8;
    public static final int RQ_START_RECORD = 9;
    public static final int RQ_STOP_RECORD = 10;
    public static final int RQ_PAUSE_RECORD = 11;
    public static final int RQ_RESUME_RECORD = 12;

    CameraCharacteristics characteristics;
    Context appContext;
    CameraDevice cameraDevice;
    CameraCaptureSession cameraSession;
    CameraSettings cameraSettings;

    Session(Context context, CameraSettings settings) {
        cameraSettings = settings;
        appContext = context;
    }


    public void applyRequest(int msg) {
        applyRequest(msg, null, null);
    }

    public void applyRequest(int msg, Object value) {
        applyRequest(msg, value, null);
    }

    public abstract void applyRequest(int msg, @Nullable Object value1, @Nullable Object value2);

    public void setRequest(int msg) {
        applyRequest(msg, null, null);
    }

    public void setRequest(int msg, Object value) {
        setRequest(msg, value, null);
    }

    public abstract void setRequest(int msg, @Nullable Object value1, @Nullable Object value2);

    public abstract void release();


    void initCharacteristics() {
        CameraManager manager = (CameraManager) appContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            assert manager != null;
            characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
        } catch (CameraAccessException e) {
            Log.e(TAG, "getCameraCharacteristics error:" + e.getMessage());
        }
    }

    byte[] getByteFromReader(ImageReader reader) {
        Image image = reader.acquireLatestImage();
        int totalSize = 0;
        for (Image.Plane plane : image.getPlanes()) {
            totalSize += plane.getBuffer().remaining();
        }
        ByteBuffer totalBuffer = ByteBuffer.allocate(totalSize);
        for (Image.Plane plane : image.getPlanes()) {
            totalBuffer.put(plane.getBuffer());
        }
        image.close();
        return totalBuffer.array();
    }


    CaptureRequest.Builder createBuilder(int type, Surface surface) {
        try {
            CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(type);
            builder.addTarget(surface);
            return builder;
        } catch (CameraAccessException | IllegalStateException e) {
            e.printStackTrace();
        }
        return null;
    }
    void sendRepeatingRequest(CaptureRequest request,
                                      CameraCaptureSession.CaptureCallback callback, Handler handler) {
        try {
            cameraSession.setRepeatingRequest(request, callback, handler);
        } catch (CameraAccessException | IllegalStateException e) {
            Log.e(TAG, "send repeating request error:" + e.getMessage());
        }
    }

    void sendCaptureRequest(CaptureRequest request,
                                    CameraCaptureSession.CaptureCallback callback, Handler handler) {
        try {
            cameraSession.capture(request, callback, handler);
        } catch (CameraAccessException | IllegalStateException e) {
            Log.e(TAG, "send capture request error:" + e.getMessage());
        }
    }

    void sendCaptureRequestWithStop(CaptureRequest request,
                            CameraCaptureSession.CaptureCallback callback, Handler handler) {
        try {
            cameraSession.stopRepeating();
            cameraSession.abortCaptures();
            cameraSession.capture(request, callback, handler);
        } catch (CameraAccessException | IllegalStateException e) {
            Log.e(TAG, "send capture request error:" + e.getMessage());
        }
    }

}
