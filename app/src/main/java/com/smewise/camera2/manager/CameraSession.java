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

public class CameraSession {
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
    // for reset AE/AF metering area
    private MeteringRectangle mResetRect = new MeteringRectangle(0, 0, 0, 0, 0);

    private ArrayMap<CaptureRequest.Key, Object> mPreviewSettings;
    private ArrayMap<CaptureRequest.Key, Object> mCaptureSettings;

    public CameraSession(Context context, Handler mainHandler, CameraSettings settings) {
        mContext = context;
        mMainHandler = mainHandler;
        mSettings = settings;
        mPreviewSettings = new ArrayMap<>();
        mCaptureSettings = new ArrayMap<>();
    }

    public void sendFlashRequest(String value) {
        Log.e(TAG, "flash value:" + value);
        switch (value) {
            case CameraSettings.FLASH_VALUE_ON:
                mCaptureSettings.put(CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
                mPreviewSettings.put(CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
                mCaptureSettings.remove(CaptureRequest.FLASH_MODE);
                break;
            case CameraSettings.FLASH_VALUE_OFF:
                mCaptureSettings.put(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                mCaptureSettings.put(CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON);
                mPreviewSettings.put(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                mCaptureSettings.put(CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON);
                break;
            case CameraSettings.FLASH_VALUE_AUTO:
                mPreviewSettings.put(CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
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
        mHelper.applyPreviewRequest(getPreviewRequestBuilder(mSurface),
                mPreviewSettings, mPreviewCallback);
    }

    public void setCameraDevice(CameraDevice device) {
        mDevice = device;
        // camera device may change, reset builder
        mPreviewBuilder = null;
        mCaptureBuilder = null;
    }

    /* need call after surface is available, after session configured
     * send preview request in callback */
    public void createPreviewSession(@NonNull SurfaceTexture texture, RequestCallback callback) {
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

    public void sendPreviewRequest() {
        mPreviewSettings.put(CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        mPreviewSettings.put(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE,
                CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_AUTO);
        mPreviewSettings.put(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        mPreviewSettings.put(CaptureRequest.CONTROL_AF_TRIGGER,
                CaptureRequest.CONTROL_AF_TRIGGER_IDLE);
        mHelper.applyPreviewRequest(getPreviewRequestBuilder(mSurface),
                mPreviewSettings, mPreviewCallback);
    }

    public void sendControlAfAeRequest(MeteringRectangle focusRect, MeteringRectangle
            meteringRect) {
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
        mHelper.applyPreviewRequest(getPreviewRequestBuilder(mSurface),
                mPreviewSettings, mPreviewCallback);
        mHelper.applyCaptureRequest(getPreviewRequestBuilder(mSurface),
                mCaptureSettings, mPreviewCallback);
    }

    public void sendControlFocusModeRequest(int focusMode) {
        Log.d(TAG, "focusMode:" + focusMode);
        mPreviewSettings.put(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        mPreviewSettings.put(CaptureRequest.CONTROL_AF_MODE, focusMode);
        MeteringRectangle[] rect = new MeteringRectangle[]{mResetRect};
        mPreviewSettings.put(CaptureRequest.CONTROL_AF_REGIONS, rect);
        mPreviewSettings.put(CaptureRequest.CONTROL_AE_REGIONS, rect);
        mHelper.applyPreviewRequest(getPreviewRequestBuilder(mSurface),
                mPreviewSettings, mPreviewCallback);
        // cancel af trigger
        mCaptureSettings.put(CaptureRequest.CONTROL_AF_TRIGGER,
                CaptureRequest.CONTROL_AF_TRIGGER_CANCEL);
        mHelper.applyCaptureRequest(getPreviewRequestBuilder(mSurface),
                mCaptureSettings, mPreviewCallback);
    }

    public void sendCaptureRequest(int deviceRotation) {
        int mainRotation = CameraUtil.getJpgRotation(getCharacteristics(), deviceRotation);
        mCaptureSettings.put(CaptureRequest.JPEG_ORIENTATION, mainRotation);
        mCaptureSettings.put(CaptureRequest.CONTROL_AF_TRIGGER,
                CaptureRequest.CONTROL_AF_TRIGGER_IDLE);
        mHelper.applyCaptureRequest(getCaptureRequestBuilder(
                mImageReader.getSurface(), false), mCaptureSettings, null);
    }

    public void restartPreviewAfterShot() {
        Log.d(TAG, "need start preview :" + mSettings.needStartPreview());
        if (mSettings.needStartPreview()) {
            sendPreviewRequest();
        }
    }

    public <T> void sendControlSettingRequest(CaptureRequest.Key<T> key, T value) {
        if (key == CaptureRequest.LENS_FOCUS_DISTANCE) {
            // preview
            mPreviewSettings.put(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_OFF);
            mPreviewSettings.put(CaptureRequest.LENS_FOCUS_DISTANCE, value);
            // capture
            mCaptureSettings.put(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_OFF);
            mCaptureSettings.put(CaptureRequest.LENS_FOCUS_DISTANCE, value);
            mHelper.applyPreviewRequest(getPreviewRequestBuilder(mSurface),
                    mPreviewSettings, mPreviewCallback);
        }
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
            mHelper = new RequestHelper(mContext, mMainHandler, mSession);
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
