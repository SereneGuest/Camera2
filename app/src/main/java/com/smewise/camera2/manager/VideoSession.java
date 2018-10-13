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
import android.media.MediaRecorder;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import com.smewise.camera2.Config;
import com.smewise.camera2.callback.RequestCallback;
import com.smewise.camera2.utils.CameraUtil;
import com.smewise.camera2.utils.MediaFunc;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class VideoSession extends Session {
    private final String TAG = Config.getTag(VideoSession.class);

    private Context mContext;
    private Handler mMainHandler;
    private CameraSettings mSettings;
    private CameraDevice mDevice;
    private RequestHelper mHelper;
    private RequestCallback mCallback;
    private SurfaceTexture mTexture;
    private Surface mSurface;
    private MediaRecorder mMediaRecorder;
    private CameraCaptureSession mSession;
    private CaptureRequest.Builder mPreviewBuilder;
    private CaptureRequest.Builder mVideoBuilder;
    private int mLatestAfState = -1;
    private Size mVideoSize;
    private Size mPreviewSize;
    private File mCurrentRecordFile;

    public VideoSession(Context context, Handler mainHandler, CameraSettings settings) {
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
            case RQ_START_RECORD: {
                createVideoSession((Integer) value1);
                break;
            }
            case RQ_STOP_RECORD: {
                if (mMediaRecorder != null) {
                    handleStopMediaRecorder();
                }
                createPreviewSession(mTexture, mCallback);
                break;
            }
            case RQ_PAUSE_RECORD: {
                // TODO pause feature
                break;
            }
            case RQ_RESUME_RECORD: {
                // TODO resume feature
                break;
            }
            default: {
                Log.w(TAG, "not used request code " + msg);
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
                Log.w(TAG, "not used set request code " + msg);
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
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    private void handleStopMediaRecorder() {
        try {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
        } catch (Exception e) {
            mMediaRecorder.reset();
            if (mCurrentRecordFile.exists()) {
                mCurrentRecordFile.delete();
            }
            Log.e(TAG, e.getMessage());
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
        mVideoBuilder = null;
    }

    /* need call after surface is available, after session configured
     * send preview request in callback */
    private void createPreviewSession(@NonNull SurfaceTexture texture, RequestCallback callback) {
        if (mSession != null) {
            mSession.close();
            mSession = null;
        }
        mCallback = callback;
        mTexture = texture;
        mSurface = new Surface(mTexture);
        try {
            mDevice.createCaptureSession(setPreviewOutputSize(mDevice.getId(), mTexture),
                    sessionStateCb, mMainHandler);
        } catch (CameraAccessException | IllegalStateException e) {
            e.printStackTrace();
        }

    }

    private void createVideoSession(int deviceRotation) {
        if (mSession != null) {
            mSession.close();
            mSession = null;
        }
        setUpMediaRecorder(deviceRotation);
        try {
            mDevice.createCaptureSession(setVideoOutputSize(
                    mTexture, mMediaRecorder.getSurface()), videoSessionStateCb, mMainHandler);
        } catch (CameraAccessException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void sendPreviewRequest() {
        mHelper.setPreviewRequest();
        mHelper.applyPreviewRequest(getPreviewRequestBuilder(mSurface), mPreviewCallback);
    }

    private void sendVideoPreviewRequest() {
        mHelper.setPreviewRequest();
        CaptureRequest.Builder builder =
                getVideoRequestBuilder(mSurface, mMediaRecorder.getSurface());
        mHelper.applyPreviewRequest(builder, mPreviewCallback);
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

    private CaptureRequest.Builder getVideoRequestBuilder(Surface surface, Surface surface2) {
        try {
            mVideoBuilder = mDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            mVideoBuilder.addTarget(surface);
            mVideoBuilder.addTarget(surface2);
        } catch (CameraAccessException | IllegalStateException e) {
            e.printStackTrace();
        }
        return mVideoBuilder;
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
    private List<Surface> setPreviewOutputSize(String id, SurfaceTexture texture) {
        StreamConfigurationMap map = getCharacteristics()
                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        // parameters key
        String videoSizKey = CameraSettings.KEY_VIDEO_SIZE;
        mVideoSize = mSettings.getVideoSize(id, videoSizKey, map);
        double videoRatio = mVideoSize.getWidth() / (double) (mVideoSize.getHeight());
        mPreviewSize = mSettings.getPreviewSizeByRatio(map, videoRatio);
        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

        // config surface
        Surface surface = new Surface(texture);
        Size uiSize = CameraUtil.getPreviewUiSize(mContext, mPreviewSize);
        mCallback.onViewChange(uiSize.getHeight(), uiSize.getWidth());
        return Collections.singletonList(surface);
    }

    // config video record size
    private List<Surface> setVideoOutputSize(SurfaceTexture texture, Surface videoSurface) {
        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        // config surface
        Surface surface = new Surface(texture);
        return Arrays.asList(surface, videoSurface);
    }

    private void setUpMediaRecorder(int deviceRotation) {
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }
        mCurrentRecordFile = MediaFunc.getOutputMediaFile(MediaFunc.MEDIA_TYPE_VIDEO, "VIDEO");
        if (mCurrentRecordFile == null) {
            Log.e(TAG, " get video file failed");
            return;
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(mCurrentRecordFile.getPath());
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        int rotation = CameraUtil.getJpgRotation(getCharacteristics(), deviceRotation);
        mMediaRecorder.setOrientationHint(rotation);
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "error prepare video record:" + e.getMessage());
        }
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

    //session callback
    private CameraCaptureSession.StateCallback videoSessionStateCb = new CameraCaptureSession
            .StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            Log.d(TAG, " session onConfigured id:" + session.getDevice().getId());
            mSession = session;
            mHelper.setCameraCaptureSession(mSession);
            sendVideoPreviewRequest();
            try {
                mMediaRecorder.start();
                mCallback.onRecordStarted(true);
            } catch (RuntimeException e) {
                mCallback.onRecordStarted(false);
                Log.e(TAG, "start record failed msg:" + e.getMessage());
            }
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
