package com.smewise.camera2.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.smewise.camera2.Config;
import com.smewise.camera2.utils.CameraThread;


/**
 * Created by wenzhe on 3/17/17.
 */

public class Camera2Manager implements CameraController {
    private final String TAG = Config.TAG_PREFIX + "Camera2Manager";
    private static Camera2Manager mManager;
    private Event mEvent;
    private CameraManager mCameraManager;
    private Handler mMainHandler;
    private CameraThread.CameraJob mJob;
    //camera devices
    private CameraDevice mDevice;
    private CameraDevice mAuxDevice;
    private String mCameraId = Config.MAIN_ID;
    private String mAuxCameraId = Config.AUX_ID;
    private volatile boolean mIsDualCamera = false;

    public interface Event {
        void onCameraOpen(CameraDevice device);
    }

    public static Camera2Manager getManager() {
        if (mManager == null) {
            mManager = new Camera2Manager();
        }
        return mManager;
    }

    private Camera2Manager() {
        mJob = new CameraThread.CameraJob();
    }

    /* this method will run in CameraThread*/
    @Override
    public void openCamera() {
        openCamera2();
    }

    @Override
    public void closeCamera() {
        closeDevice();
    }

    public void setDualCameraMode(boolean dualCameraMode) {
        mIsDualCamera = dualCameraMode;
    }

    public boolean isDualCamera() {
        return mIsDualCamera;
    }

    public void setCameraId(@NonNull String mainId, @Nullable String auxId) {
        mCameraId = mainId;
        if (auxId != null) {
            mAuxCameraId = auxId;
        }
    }

    public CameraDevice getCameraDevice(boolean isMain) {
        return isMain ? mDevice : mAuxDevice;
    }

    public String getCameraId() {
        return mCameraId;
    }

    public String getAuxCameraId() {
        return mAuxCameraId;
    }

    public CameraCharacteristics getCharacteristics() {
        try {
            return mCameraManager.getCameraCharacteristics(mCameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String[] getCameraIdList(Context context) {
        if (mCameraManager == null) {
            mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        }
        try {
            return mCameraManager.getCameraIdList();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public CameraCharacteristics getCharacteristics(String id) {
        try {
            return mCameraManager.getCameraCharacteristics(id);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public CameraCharacteristics getCharacteristics(Context context, String id) {
        if (mCameraManager == null) {
            mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        }
        try {
            return mCameraManager.getCameraCharacteristics(id);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public StreamConfigurationMap getConfigMap(Context context, String id) {
        if (mCameraManager == null) {
            mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        }
        try {
            CameraCharacteristics c = mCameraManager.getCameraCharacteristics(id);
            return c.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void openCamera(Context context, Event event, Handler mainHandler,
            CameraThread cameraThread) {
        mEvent = event;
        mMainHandler = mainHandler;
        if (mCameraManager == null) {
            mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        }
        if (Config.CLOSE_CAMERA_ASYNC) {
            mJob.jobCallback = this;
            mJob.jobType = CameraThread.OPEN_CAMERA;
            cameraThread.addCameraJob(mJob);
            cameraThread.notifyJob();
        } else {
            openCamera2();
        }
    }

    public void releaseCamera(CameraThread cameraThread) {
        if (Config.CLOSE_CAMERA_ASYNC) {
            mJob.jobCallback = this;
            mJob.jobType = CameraThread.CLOSE_CAMERA;
            cameraThread.addCameraJob(mJob);
            cameraThread.notifyJob();
        } else {
            closeDevice();
        }
    }

    private void closeDevice() {
        if (mDevice != null) {
            mDevice.close();
            mDevice = null;
        }
        if (mAuxDevice != null) {
            mAuxDevice.close();
            mAuxDevice = null;
        }
    }

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
                Log.d(TAG, "device opened");
            if (mCameraId.equals(camera.getId())) {
                mDevice = camera;
            } else {
                mAuxDevice = camera;
            }
            if (mIsDualCamera) {
                if (mDevice != null && mAuxDevice != null) {
                    mEvent.onCameraOpen(mDevice);
                }
            } else {
                mEvent.onCameraOpen(mDevice);
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.w(TAG, "onDisconnected");
            camera.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e(TAG, "error occur when open camera :" + error);
            camera.close();
        }
    };

    @SuppressLint("MissingPermission")
    private void openCamera2() {
        // no need to check permission, because we check permission in onStart() every time
        try {
            mCameraManager.openCamera(mCameraId, stateCallback, mMainHandler);
            if (mIsDualCamera) {
                mCameraManager.openCamera(mAuxCameraId, stateCallback, mMainHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
