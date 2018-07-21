package com.smewise.camera2.manager;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import com.smewise.camera2.Config;
import com.smewise.camera2.callback.RequestCallback;
import com.smewise.camera2.utils.CameraUtil;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class CameraSession extends Session {
    private final String TAG = Config.getTag(CameraSession.class);

    private Context mContext;
    private Handler mMainHandler;
    private CameraSettings mSettings;
    private CameraDevice mDevice;
    private RequestHelper mHelper;
    private RequestCallback mCallback;
    private SurfaceTexture mTexture;
    private Surface mSurface;
    private ImageReader mImageReader;
    private CameraCaptureSession mSession;
    private CaptureRequest.Builder mPreviewBuilder;
    private CaptureRequest.Builder mCaptureBuilder;
    private int mLatestAfState = -1;

    public CameraSession(Context context, Handler mainHandler, CameraSettings settings) {
        mContext = context;
        mMainHandler = mainHandler;
        mSettings = settings;
        mHelper = new RequestHelper(mContext, mMainHandler);
    }


    @Override
    public void applyRequest(int msg, Object value1, Object value2) {
        switch (msg) {
            case RQ_SET_DEVICE: {
                setCameraDevice((CameraDevice) value1);
                break;
            }
            case RQ_START_PREVIEW: {
                createPreviewSession((SurfaceTexture) value1, (RequestCallback) value2);
                break;
            }
            case RQ_AF_AE_REGIONS: {
                sendControlAfAeRequest((MeteringRectangle) value1, (MeteringRectangle) value2);
                break;
            }
            case RQ_FOCUS_MODE: {
                sendControlFocusModeRequest((int) value1);
                break;
            }
            case RQ_FOCUS_DISTANCE: {
                sendControlFocusDistanceRequest((float) value1);
                break;
            }
            case RQ_FLASH_MODE: {
                sendFlashRequest((String) value1);
                break;
            }
            case RQ_RESTART_PREVIEW: {
                sendRestartPreviewRequest();
                break;
            }
            case RQ_TAKE_PICTURE: {
                sendCaptureRequest((Integer) value1);
                break;
            }
            default: {
                Log.w(TAG, "invalid request code " + msg);
                break;
            }
        }
    }

    @Override
    public void setRequest(int msg, @Nullable Object value1, @Nullable Object value2) {
        switch (msg) {
            case RQ_SET_DEVICE: {
                break;
            }
            case RQ_START_PREVIEW: {
                break;
            }
            case RQ_AF_AE_REGIONS: {
                break;
            }
            case RQ_FOCUS_MODE: {
                break;
            }
            case RQ_FOCUS_DISTANCE: {
                break;
            }
            case RQ_FLASH_MODE: {
                mHelper.setFlashRequest((String) value1);
                break;
            }
            case RQ_RESTART_PREVIEW: {
                break;
            }
            case RQ_TAKE_PICTURE: {
                break;
            }
            default: {
                Log.w(TAG, "invalid request code " + msg);
                break;
            }
        }
    }

    @Override
    public void release() {
        if (mSession != null) {
            mSession.close();
            mSession = null;
        }
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
    }

    private void sendFlashRequest(String value) {
        Log.e(TAG, "flash value:" + value);
        mHelper.setFlashRequest(value);
        mHelper.applyPreviewRequest(getPreviewRequestBuilder(mSurface), mPreviewCallback);
    }

    private void setCameraDevice(CameraDevice device) {
        mDevice = device;
        // camera device may change, reset builder
        mPreviewBuilder = null;
        mCaptureBuilder = null;
    }

    /* need call after surface is available, after session configured
     * send preview request in callback */
    private void createPreviewSession(@NonNull SurfaceTexture texture, RequestCallback callback) {
        mCallback = callback;
        mTexture = texture;
        mSurface = new Surface(mTexture);
        try {
            mDevice.createCaptureSession(setOutputSize(mDevice.getId(), mTexture),
                    sessionStateCb, mMainHandler);
        } catch (CameraAccessException | IllegalStateException e) {
            e.printStackTrace();
        }

    }

    private void sendPreviewRequest() {
        mHelper.setPreviewRequest();
        mHelper.applyPreviewRequest(getPreviewRequestBuilder(mSurface), mPreviewCallback);
    }

    private void sendControlAfAeRequest(MeteringRectangle focusRect,
                                        MeteringRectangle meteringRect) {
        mHelper.setControlAfAeRequest(focusRect, meteringRect);
        mHelper.applyPreviewRequest(getPreviewRequestBuilder(mSurface), mPreviewCallback);
        mHelper.applyCaptureRequest(getPreviewRequestBuilder(mSurface), mPreviewCallback);
    }

    private void sendControlFocusModeRequest(int focusMode) {
        Log.d(TAG, "focusMode:" + focusMode);
        mHelper.setControlFocusModeRequest(focusMode);
        mHelper.applyPreviewRequest(getPreviewRequestBuilder(mSurface), mPreviewCallback);
        // cancel af trigger
        mHelper.applyCaptureRequest(getPreviewRequestBuilder(mSurface), mPreviewCallback);
    }

    private void sendCaptureRequest(int deviceRotation) {
        int jpegRotation = CameraUtil.getJpgRotation(getCharacteristics(), deviceRotation);
        mHelper.setCaptureRequest(jpegRotation);
        mHelper.applyCaptureRequest(getCaptureRequestBuilder(
                mImageReader.getSurface(), false), null);
    }

    private void sendRestartPreviewRequest() {
        Log.d(TAG, "need start preview :" + mSettings.needStartPreview());
        if (mSettings.needStartPreview()) {
            sendPreviewRequest();
        }
    }

    private void sendControlFocusDistanceRequest(float value) {
        mHelper.setControlFocusDistanceRequest(value);
        mHelper.applyPreviewRequest(getPreviewRequestBuilder(mSurface), mPreviewCallback);
    }


    private CameraCharacteristics getCharacteristics() {
        CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            return manager.getCameraCharacteristics(mDevice.getId());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private CaptureRequest.Builder getPreviewRequestBuilder(Surface surface) {
        if (mPreviewBuilder == null) {
            mPreviewBuilder = createBuilder(CameraDevice.TEMPLATE_PREVIEW, surface);
        }
        return mPreviewBuilder;
    }

    private CaptureRequest.Builder getCaptureRequestBuilder(Surface surface, boolean create) {
        if (create) {
            return createBuilder(CameraDevice.TEMPLATE_STILL_CAPTURE, surface);
        } else {
            if (mCaptureBuilder == null) {
                mCaptureBuilder = createBuilder(CameraDevice.TEMPLATE_STILL_CAPTURE, surface);
            }
            return mCaptureBuilder;
        }
    }

    private CaptureRequest.Builder createBuilder(int type, Surface surface) {
        try {
            CaptureRequest.Builder builder = mDevice.createCaptureRequest(type);
            builder.addTarget(surface);
            return builder;
        } catch (CameraAccessException | IllegalStateException e) {
            e.printStackTrace();
        }
        return null;
    }

    //config picture size and preview size
    private List<Surface> setOutputSize(String id, SurfaceTexture texture) {
        StreamConfigurationMap map = getCharacteristics()
                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        // parameters key
        String picKey = CameraSettings.KEY_PICTURE_SIZE;
        String preKey = CameraSettings.KEY_PREVIEW_SIZE;
        String formatKey = CameraSettings.KEY_PICTURE_FORMAT;
        // get value from setting
        int format = mSettings.getPicFormat(id, formatKey);
        Size previewSize = mSettings.getPreviewSize(id, preKey, map);
        texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
        Size pictureSize = mSettings.getPictureSize(id, picKey, map, format);
        // config surface
        Surface surface = new Surface(texture);
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
        mImageReader = ImageReader.newInstance(pictureSize.getWidth(),
                pictureSize.getHeight(), format, 1);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                mCallback.onDataBack(getByteFromReader(reader),
                        reader.getWidth(), reader.getHeight());
            }
        }, null);
        Size uiSize = CameraUtil.getPreviewUiSize(mContext, previewSize);
        mCallback.onViewChange(uiSize.getHeight(), uiSize.getWidth());
        return Arrays.asList(surface, mImageReader.getSurface());
    }

    private byte[] getByteFromReader(ImageReader reader) {
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

    //session callback
    private CameraCaptureSession.StateCallback sessionStateCb = new CameraCaptureSession
            .StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            Log.d(TAG, " session onConfigured id:" + session.getDevice().getId());
            mSession = session;
            mHelper.setCameraCaptureSession(mSession);
            sendPreviewRequest();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.d(TAG, "create session fail id:" + session.getDevice().getId());
        }
    };

    private CameraCaptureSession.CaptureCallback mPreviewCallback = new CameraCaptureSession
            .CaptureCallback() {

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull
                CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
            updateAfState(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull
                CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            updateAfState(result);
            mCallback.onRequestComplete();
        }
    };

    private void updateAfState(CaptureResult result) {
        Integer state = result.get(CaptureResult.CONTROL_AF_STATE);
        if (state != null && mLatestAfState != state) {
            mLatestAfState = state;
            mCallback.onAFStateChanged(state);
        }
    }

}
