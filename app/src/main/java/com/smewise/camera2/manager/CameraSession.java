package com.smewise.camera2.manager;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import com.smewise.camera2.Config;
import com.smewise.camera2.callback.RequestCallback;
import com.smewise.camera2.utils.CameraUtil;

import java.util.Arrays;
import java.util.List;

public class CameraSession extends Session {
    private final String TAG = Config.getTag(CameraSession.class);

    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAITING_LOCK = 1;
    private static final int STATE_WAITING_PRE_CAPTURE = 2;
    private static final int STATE_WAITING_NON_PRE_CAPTURE = 3;
    private static final int STATE_PICTURE_TAKEN = 4;
    private int mState = STATE_PREVIEW;

    private Handler mMainHandler;
    private RequestManager mRequestMgr;
    private RequestCallback mCallback;
    private SurfaceTexture mTexture;
    private Surface mSurface;
    private ImageReader mImageReader;
    private CaptureRequest.Builder mPreviewBuilder;
    private CaptureRequest.Builder mCaptureBuilder;
    private int mLatestAfState = -1;
    private CaptureRequest mOriginPreviewRequest;
    private int mDeviceRotation;

    public CameraSession(Context context, Handler mainHandler, CameraSettings settings) {
        super(context, settings);
        mMainHandler = mainHandler;
        mRequestMgr = new RequestManager();
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
                mDeviceRotation = (Integer) value1;
                runCaptureStep();
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
                mRequestMgr.applyFlashRequest(getPreviewBuilder(), (String) value1);
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
        if (cameraSession != null) {
            cameraSession.close();
            cameraSession = null;
        }
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
    }

    private void sendFlashRequest(String value) {
        Log.d(TAG, "flash value:" + value);
        CaptureRequest request = mRequestMgr.getFlashRequest(getPreviewBuilder(), value);
        sendRepeatingRequest(request, mPreviewCallback, mMainHandler);
    }

    private void setCameraDevice(CameraDevice device) {
        cameraDevice = device;
        // device changed, get new Characteristics
        initCharacteristics();
        mRequestMgr.setCharacteristics(characteristics);
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
            cameraDevice.createCaptureSession(setOutputSize(cameraDevice.getId(), mTexture),
                    sessionStateCb, mMainHandler);
        } catch (CameraAccessException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void sendPreviewRequest() {
        CaptureRequest request = mRequestMgr.getPreviewRequest(getPreviewBuilder());
        if (mOriginPreviewRequest == null) {
            mOriginPreviewRequest = request;
        }
        sendRepeatingRequest(request, mPreviewCallback, mMainHandler);
    }

    private void sendControlAfAeRequest(MeteringRectangle focusRect,
                                        MeteringRectangle meteringRect) {
        CaptureRequest.Builder builder = getPreviewBuilder();
        CaptureRequest request = mRequestMgr
                .getTouch2FocusRequest(builder, focusRect, meteringRect);
        sendRepeatingRequest(request, mPreviewCallback, mMainHandler);
        // trigger af
        builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
        sendCaptureRequest(builder.build(), null, mMainHandler);
    }

    private void sendControlFocusModeRequest(int focusMode) {
        Log.d(TAG, "focusMode:" + focusMode);
        CaptureRequest request = mRequestMgr.getFocusModeRequest(getPreviewBuilder(), focusMode);
        sendRepeatingRequest(request, mPreviewCallback, mMainHandler);
    }

    private void sendStillPictureRequest() {
        int jpegRotation = CameraUtil.getJpgRotation(characteristics, mDeviceRotation);
        CaptureRequest.Builder builder = getCaptureBuilder(false, mImageReader.getSurface());
        Integer aeFlash = getPreviewBuilder().get(CaptureRequest.CONTROL_AE_MODE);
        Integer afMode = getPreviewBuilder().get(CaptureRequest.CONTROL_AF_MODE);
        Integer flashMode = getPreviewBuilder().get(CaptureRequest.FLASH_MODE);
        builder.set(CaptureRequest.CONTROL_AE_MODE, aeFlash);
        builder.set(CaptureRequest.CONTROL_AF_MODE, afMode);
        builder.set(CaptureRequest.FLASH_MODE, flashMode);
        CaptureRequest request = mRequestMgr.getStillPictureRequest(
                getCaptureBuilder(false, mImageReader.getSurface()), jpegRotation);
        sendCaptureRequestWithStop(request, mCaptureCallback, mMainHandler);
    }

    private void sendRestartPreviewRequest() {
        Log.d(TAG, "need start preview :" + cameraSettings.needStartPreview());
        if (cameraSettings.needStartPreview()) {
            sendPreviewRequest();
        }
    }

    private void sendControlFocusDistanceRequest(float value) {
        CaptureRequest request = mRequestMgr.getFocusDistanceRequest(getPreviewBuilder(), value);
        sendRepeatingRequest(request, mPreviewCallback, mMainHandler);
    }

    private void updateRequestFromSetting() {
        String flashValue = cameraSettings.getGlobalPref(CameraSettings.KEY_FLASH_MODE);
        mRequestMgr.getFlashRequest(getPreviewBuilder(), flashValue);
        // TODO: need load more settings
    }

    private void resetTriggerState() {
        mState = STATE_PREVIEW;
        CaptureRequest.Builder builder = getPreviewBuilder();
        builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE);
        builder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_IDLE);
        sendRepeatingRequest(builder.build(), mPreviewCallback, mMainHandler);
        sendCaptureRequest(builder.build(), mPreviewCallback, mMainHandler);
    }

    private CaptureRequest.Builder getPreviewBuilder() {
        if (mPreviewBuilder == null) {
            mPreviewBuilder = createBuilder(CameraDevice.TEMPLATE_PREVIEW, mSurface);
        }
        return mPreviewBuilder;
    }

    private CaptureRequest.Builder getCaptureBuilder( boolean create, Surface surface) {
        if (create) {
            return createBuilder(CameraDevice.TEMPLATE_STILL_CAPTURE, surface);
        } else {
            if (mCaptureBuilder == null) {
                mCaptureBuilder = createBuilder(CameraDevice.TEMPLATE_STILL_CAPTURE, surface);
            }
            return mCaptureBuilder;
        }
    }

    //config picture size and preview size
    private List<Surface> setOutputSize(String id, SurfaceTexture texture) {
        StreamConfigurationMap map = characteristics
                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        // parameters key
        String picKey = CameraSettings.KEY_PICTURE_SIZE;
        String preKey = CameraSettings.KEY_PREVIEW_SIZE;
        String formatKey = CameraSettings.KEY_PICTURE_FORMAT;
        // get value from setting
        int format = cameraSettings.getPicFormat(id, formatKey);
        Size previewSize = cameraSettings.getPreviewSize(id, preKey, map);
        texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
        Size pictureSize = cameraSettings.getPictureSize(id, picKey, map, format);
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
        Size uiSize = CameraUtil.getPreviewUiSize(appContext, previewSize);
        mCallback.onViewChange(uiSize.getHeight(), uiSize.getWidth());
        return Arrays.asList(surface, mImageReader.getSurface());
    }

    //session callback
    private CameraCaptureSession.StateCallback sessionStateCb = new CameraCaptureSession
            .StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            Log.d(TAG, " session onConfigured id:" + session.getDevice().getId());
            cameraSession = session;
            //mHelper.setCameraCaptureSession(cameraSession);
            updateRequestFromSetting();
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
            processPreCapture(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull
                CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            updateAfState(result);
            processPreCapture(result);
            mCallback.onRequestComplete();
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull
                CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            Log.w(TAG, "onCaptureFailed reason:" + failure.getReason());
        }
    };

    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession
            .CaptureCallback() {

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull
                CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull
                CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Log.i(TAG, "capture complete");
            resetTriggerState();
        }
    };

    private void processPreCapture(CaptureResult result) {
        switch (mState) {
            case STATE_PREVIEW: {
                // We have nothing to do when the camera preview is working normally.
                break;
            }
            case STATE_WAITING_LOCK: {
                Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                if (afState == null) {
                    sendStillPictureRequest();
                } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                        CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                        mState = STATE_PICTURE_TAKEN;
                        sendStillPictureRequest();
                    } else {
                        triggerAECaptureSequence();
                    }
                }
                break;
            }
            case STATE_WAITING_PRE_CAPTURE: {
                // CONTROL_AE_STATE can be null on some devices
                Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                if (aeState == null ||
                        aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                        aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                    mState = STATE_WAITING_NON_PRE_CAPTURE;
                }
                break;
            }
            case STATE_WAITING_NON_PRE_CAPTURE: {
                // CONTROL_AE_STATE can be null on some devices
                Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                    mState = STATE_PICTURE_TAKEN;
                    sendStillPictureRequest();
                }
                break;
            }
        }
    }

    private void triggerAECaptureSequence() {
        CaptureRequest.Builder builder = getPreviewBuilder();
        builder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
        mState = STATE_WAITING_PRE_CAPTURE;
        sendCaptureRequest(builder.build(), mPreviewCallback, mMainHandler);
    }

    private void triggerAFCaptureSequence() {
        CaptureRequest.Builder builder = getPreviewBuilder();
        builder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                CaptureRequest.CONTROL_AF_TRIGGER_START);
        mState = STATE_WAITING_LOCK;
        sendCaptureRequest(builder.build(), mPreviewCallback, mMainHandler);
    }

    private void runCaptureStep() {
        String flashValue = cameraSettings.getGlobalPref(CameraSettings.KEY_FLASH_MODE);
        boolean isFlashOn = !CameraSettings.FLASH_VALUE_OFF.equals(flashValue)
                && !CameraSettings.FLASH_VALUE_TORCH.equals(flashValue);
        if (mRequestMgr.canTriggerAf() && isFlashOn) {
            triggerAFCaptureSequence();
        } else {
            sendStillPictureRequest();
        }
    }

    private void updateAfState(CaptureResult result) {
        Integer state = result.get(CaptureResult.CONTROL_AF_STATE);
        if (state != null && mLatestAfState != state) {
            mLatestAfState = state;
            mCallback.onAFStateChanged(state);
        }
    }

}
