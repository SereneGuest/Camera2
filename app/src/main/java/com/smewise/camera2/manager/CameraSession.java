package com.smewise.camera2.manager;

import android.content.Context;
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
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import com.smewise.camera2.Config;
import com.smewise.camera2.callback.RequestCallback;
import com.smewise.camera2.utils.CameraUtil;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class CameraSession {
    private final String TAG = Config.getTag(CameraSession.class);

    private Context mContext;
    private Handler mMainHandler;
    private CameraSettings mSettings;
    private CameraDevice mDevice;
    private RequestHelper mHelper;
    private RequestCallback mCallback;
    private SurfaceTexture mTexture;
    private ImageReader mImageReader;
    private CameraCaptureSession mSession;
    private int mLatestAfState = -1;

    public CameraSession(Context context, Handler mainHandler, CameraSettings settings) {
        mContext = context;
        mMainHandler = mainHandler;
        mSettings = settings;
        mHelper = new RequestHelper(context, mainHandler);
    }

    public void setCameraDevice(CameraDevice device) {
        mDevice = device;
    }

    /* need call after surface is available, after session configured
     * send preview request in callback */
    public void createPreviewSession(@NonNull SurfaceTexture texture, RequestCallback callback) {
        mCallback = callback;
        mTexture = texture;
        try {
            mDevice.createCaptureSession(setOutputSize(mDevice.getId(), mTexture),
                    sessionStateCb, mMainHandler);
        } catch (CameraAccessException | IllegalStateException e) {
            e.printStackTrace();
        }

    }

    public void sendPreviewRequest() {
        mHelper.sendPreviewRequest(getPreviewRequestBuilder(new Surface(mTexture)),
                mSession, mPreviewCallback);
    }

    public void sendControlAfAeRequest(MeteringRectangle focusRect, MeteringRectangle
            meteringRectangle) {
        mHelper.sendControlAfAeRequest(getPreviewRequestBuilder(new Surface(mTexture)),
                focusRect, meteringRectangle, mSession, mPreviewCallback);
    }

    public void sendControlFocusModeRequest(int focusMode) {
        mHelper.sendFocusModeRequest(getPreviewRequestBuilder(new Surface(mTexture)),
                focusMode, mSession, mPreviewCallback);
    }

    public void sendCaptureRequest(int deviceRotation) {
        int mainRotation = CameraUtil.getJpgRotation(getCharacteristics(), deviceRotation);
        mHelper.sendCaptureRequest(getCaptureRequestBuilder(mImageReader.getSurface()),
                mainRotation, mSession, null);
    }

    public void restartPreviewAfterShot() {
        Log.d(TAG, "need start preview :" + mSettings.needStartPreview());
        if (mSettings.needStartPreview()) {
            sendPreviewRequest();
        }
    }

    public <T> void sendControlSettingRequest(CaptureRequest.Key<T> key, T value) {
        mHelper.sendControlSettingRequest(getPreviewRequestBuilder(new Surface(mTexture)),
                mSession, mPreviewCallback, key, value);
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
        return createBuilder(CameraDevice.TEMPLATE_PREVIEW, surface);
    }

    private CaptureRequest.Builder getCaptureRequestBuilder(Surface surface) {
        return createBuilder(CameraDevice.TEMPLATE_STILL_CAPTURE, surface);
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
                        reader.getWidth(), reader.getHeight()); }}, null);
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
}
