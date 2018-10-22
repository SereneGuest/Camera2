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

public class SingleDeviceManager  extends  DeviceManager{
    private final String TAG = Config.getTag(SingleDeviceManager.class);

    private CameraDevice mDevice;
    private JobExecutor mJobExecutor;
    private String mCameraId = Config.MAIN_ID;
    private CameraEvent mCameraEvent;

    public SingleDeviceManager(Context context, JobExecutor executor, CameraEvent event) {
        super(context);
        mJobExecutor = executor;
        mCameraEvent = event;
    }

    public void setCameraId(@NonNull String id) {
        mCameraId = id;
    }

    public String getCameraId() {
        return mCameraId;
    }

    public CameraDevice getCameraDevice() {
        return mDevice;
    }

    public CameraCharacteristics getCharacteristics() {
        try {
            return cameraManager.getCameraCharacteristics(mCameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public StreamConfigurationMap getConfigMap() {
        try {
            CameraCharacteristics c = cameraManager.getCameraCharacteristics(mCameraId);
            return c.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void openCamera(final Handler mainHandler) {
        mJobExecutor.execute(new JobExecutor.Task<Void>() {
            @Override
            public Void run() {
                openDevice(mainHandler);
                return super.run();
            }
        });
    }

    public void releaseCamera() {
        mJobExecutor.execute(new JobExecutor.Task<Void>() {
            @Override
            public Void run() {
                closeDevice();
                return super.run();
            }
        });
    }

    @SuppressLint("MissingPermission")
    private synchronized void openDevice(Handler handler) {
        // no need to check permission, because we check permission in onStart() every time
        try {
            cameraManager.openCamera(mCameraId, stateCallback, handler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private synchronized void closeDevice() {
        if (mDevice != null) {
            mDevice.close();
            mDevice = null;
        }
        mCameraEvent.onDeviceClosed();
    }

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.d(TAG, "device opened :" + camera.getId());
            mDevice = camera;
            mCameraEvent.onDeviceOpened(camera);
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
