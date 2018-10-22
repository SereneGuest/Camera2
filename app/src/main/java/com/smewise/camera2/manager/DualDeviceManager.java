package com.smewise.camera2.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.smewise.camera2.Config;
import com.smewise.camera2.utils.JobExecutor;

public class DualDeviceManager extends  DeviceManager{
    private final String TAG = Config.getTag(DualDeviceManager.class);

    private CameraDevice mDevice;
    private CameraDevice mAuxDevice;
    private JobExecutor mExecutor;
    private String mCameraId = Config.MAIN_ID;
    private String mAuxCameraId = Config.AUX_ID;
    private CameraEvent mCameraEvent;

    public DualDeviceManager(Context context, JobExecutor executor, CameraEvent event) {
        super(context);
        mExecutor = executor;
        mCameraEvent = event;
    }

    public void setCameraId(@NonNull String id, @NonNull String auxId) {
        mCameraId = id;
        mAuxCameraId = auxId;
    }

    public String getCameraId(boolean isMain) {
        if (isMain) {
            return mCameraId;
        } else {
            return mAuxCameraId;
        }
    }

    public CameraDevice getCameraDevice(boolean isMain) {
        if (isMain) {
            return mDevice;
        } else {
            return mAuxDevice;
        }
    }

    public CameraCharacteristics getCharacteristics(boolean isMain) {
        String cameraId = isMain ? mCameraId : mAuxCameraId;
        try {
            return cameraManager.getCameraCharacteristics(cameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public StreamConfigurationMap getConfigMap(boolean isMain) {
        String cameraId = isMain ? mCameraId : mAuxCameraId;
        try {
            CameraCharacteristics c = cameraManager.getCameraCharacteristics(cameraId);
            return c.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void openCamera(final Handler mainHandler) {
        mExecutor.execute(new JobExecutor.Task<Void>() {
            @Override
            public Void run() {
                openDevice(mCameraId, mainHandler);
                openDevice(mAuxCameraId, mainHandler);
                return super.run();
            }
        });
    }

    public void releaseCamera() {
        mExecutor.execute(new JobExecutor.Task<Void>() {
            @Override
            public Void run() {
                closeDevice();
                return super.run();
            }
        });

    }

    @SuppressLint("MissingPermission")
    private synchronized void openDevice(String cameraId, Handler handler) {
        // no need to check permission, because we check permission in onStart() every time
        try {
            cameraManager.openCamera(cameraId, stateCallback, handler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private synchronized void closeDevice() {
        if (mDevice != null) {
            mDevice.close();
            mDevice = null;
        }
        if (mAuxDevice != null) {
            mAuxDevice.close();
            mAuxDevice = null;
        }
        mCameraEvent.onDeviceClosed();
    }

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.d(TAG, "device opened :" + camera.getId());
            if (camera.getId().equals(mCameraId)) {
                mDevice = camera;
            } else if (camera.getId().equals(mAuxCameraId)) {
                mAuxDevice = camera;
            } else {
                Log.e(TAG, "internal error, camera id not match any requested camera id");
            }
            if (mDevice != null && mAuxDevice != null) {
                mCameraEvent.onAuxDeviceOpened(mAuxDevice);
                mCameraEvent.onDeviceOpened(mDevice);
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.w(TAG, "onDisconnected");
            camera.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e(TAG, "error occur when open camera :" + camera.getId() + " error code:" + error);
            camera.close();
        }
    };
}
