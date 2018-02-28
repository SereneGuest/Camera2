package com.smewise.camera2.manager;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
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
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import com.smewise.camera2.Config;
import com.smewise.camera2.utils.CameraCapability;
import com.smewise.camera2.utils.CameraUtil;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * Created by wenzhe on 3/20/17.
 */

public class SessionManager {

    private final String TAG = Config.getTag(SessionManager.class);

    public interface Callback {
        void onMainData(byte[] data, int width, int height);

        void onAuxData(byte[] data, int width, int height);

        void onRequestComplete();

        void onViewChange(int width, int height);
    }

    private Context mContext;
    private Callback mCallback;

    private String mMainId = Config.MAIN_ID;
    private String mAuxId = Config.AUX_ID;

    private CameraCaptureSession mainSession;
    private CameraCaptureSession auxSession;
    private SurfaceTexture mMainTexture;
    private SurfaceTexture mAuxTexture;
    private RequestHelper mHelper;
    private Handler mainHandler;

    private ImageReader mainImageReader;
    private ImageReader auxImageReader;
    private CaptureRequest mLatestPreviewRequest;
    private CaptureRequest mAuxPreviewRequest;
    private int mLatestAfState = -1;
    private FocusOverlayManager mFocusManager;
    private CameraSettings mSettings;
    private CameraCapability mCapability;

    public SessionManager(Context context, Handler handler, FocusOverlayManager manager,
                          CameraSettings settings) {
        mContext = context;
        mFocusManager = manager;
        mCapability= new CameraCapability();
        mHelper = new RequestHelper(handler);
        mHelper.setCameraCapability(mCapability);
        mainHandler = handler;
        mSettings = settings;
    }

    private Camera2Manager getManager() {
        return Camera2Manager.getManager();
    }

    private boolean isDualCamera() {
        return Camera2Manager.getManager().isDualCamera();
    }

    private CaptureRequest.Builder getPreviewRequestBuilder(String id, Surface surface) {
        return createBuilder(id, CameraDevice.TEMPLATE_PREVIEW, surface);
    }

    private CaptureRequest.Builder getCaptureRequestBuilder(String id, Surface surface) {
        return createBuilder(id, CameraDevice.TEMPLATE_STILL_CAPTURE, surface);
    }

    private CaptureRequest.Builder createBuilder(String id, int type, Surface surface) {
        boolean isMain = getManager().getCameraId().equals(id);
        try {
            CaptureRequest.Builder builder = getManager()
                    .getCameraDevice(isMain).createCaptureRequest(type);
            builder.addTarget(surface);
            return builder;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sendPreviewRequest() {
        mHelper.sendPreviewRequest(getPreviewRequestBuilder(mMainId, new Surface(mMainTexture)),
                mainSession, mPreviewCallback);
        if (!isDualCamera()) {return;}
        mHelper.sendPreviewRequest(getPreviewRequestBuilder(mAuxId, new Surface(mAuxTexture)),
                auxSession, mAuxPreviewCallback);
    }

    public void sendControlAfAeRequest(MeteringRectangle focusRect, MeteringRectangle
            meteringRectangle) {
        mHelper.sendControlAfAeRequest(getPreviewRequestBuilder(mMainId, new Surface(mMainTexture)),
                focusRect, meteringRectangle, mainSession, mPreviewCallback);
        if (!isDualCamera()) {return;}
        mHelper.sendControlAfAeRequest(getPreviewRequestBuilder(mAuxId, new Surface(mAuxTexture)),
                focusRect, meteringRectangle, auxSession, mAuxPreviewCallback);
    }

    public void sendControlFocusModeRequest(int focusMode) {
        mHelper.sendFocusModeRequest(getPreviewRequestBuilder(
                mMainId, new Surface(mMainTexture)), focusMode, mainSession, mPreviewCallback);
        if (!isDualCamera()) {return;}
        mHelper.sendFocusModeRequest(getPreviewRequestBuilder(
                mAuxId, new Surface(mAuxTexture)), focusMode, auxSession, mAuxPreviewCallback);
    }

    public void sendCaptureRequest(int deviceRotation) {
        int mainRotation = CameraUtil.getJpgRotation(
                getManager().getCharacteristics(mMainId), deviceRotation);
        mHelper.sendCaptureRequest(getCaptureRequestBuilder(mMainId, mainImageReader.getSurface()),
                mainRotation, mainSession, mLatestPreviewRequest);
        if (!isDualCamera()) {return;}
        int auxRotation = CameraUtil.getJpgRotation(
                getManager().getCharacteristics(mAuxId), deviceRotation);
        mHelper.sendCaptureRequest(getCaptureRequestBuilder(mAuxId, auxImageReader.getSurface()),
                auxRotation, auxSession, mAuxPreviewRequest);
    }

    public void restartPreviewAfterShot() {
        Log.d(TAG, "need start preview :" + mSettings.needStartPreview());
        if (mSettings.needStartPreview()) {
            sendPreviewRequest();
        }
    }

    public <T> void sendControlSettingRequest(CaptureRequest.Key<T> key, T value) {
        mHelper.sendControlSettingRequest(getPreviewRequestBuilder(mMainId,
                new Surface(mMainTexture)), mainSession, mPreviewCallback, key, value);
        if (!isDualCamera()) {return;}
        mHelper.sendControlSettingRequest(getPreviewRequestBuilder(mAuxId,
                new Surface(mAuxTexture)), auxSession, mAuxPreviewCallback, key, value);
    }

    /* need call after surface is available, after session configured
     * send preview request in callback */
    public void createPreviewSession(
            @NonNull SurfaceTexture main, @Nullable SurfaceTexture aux, Callback callback) {
        Log.d(TAG, " createPreviewSession");
        mCallback = callback;
        mMainTexture = main;
        mAuxTexture = aux;
        // camera id may change in setting
        mMainId = getManager().getCameraId();
        CameraCharacteristics auxC = null;
        if (isDualCamera()) {
            mAuxId = getManager().getAuxCameraId();
            auxC = getManager().getCharacteristics(mAuxId);
        }
        mCapability.setCameraCharacteristics(getManager().getCharacteristics(mMainId), auxC);
        //create Session
        //main
        try {
            getManager().getCameraDevice(true).createCaptureSession(
                    setOutputSize(mMainId, mMainTexture),
                    mainStateCallback, mainHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        if (!isDualCamera()) {return;}
        //aux
        try {
            getManager().getCameraDevice(false).createCaptureSession(
                    setOutputSize(mAuxId, mAuxTexture),
                    auxStateCallback, mainHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //config picture size and preview size
    private List<Surface> setOutputSize(String id, SurfaceTexture texture) {
        CameraCharacteristics characteristics = getManager().getCharacteristics(id);
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics
                .SCALER_STREAM_CONFIGURATION_MAP);
        // default for single camera
        String picKey = CameraSettings.KEY_PICTURE_SIZE;
        String preKey = CameraSettings.KEY_PREVIEW_SIZE;
        String formatKey = CameraSettings.KEY_PICTURE_FORMAT;
        if (id.equals(mAuxId) && isDualCamera()) {
            // dual camera, aux
            picKey = CameraSettings.KEY_AUX_PICTURE_SIZE;
            preKey = CameraSettings.KEY_AUX_PREVIEW_SIZE;
            formatKey = CameraSettings.KEY_AUX_PICTURE_FORMAT;
        } else if (id.equals(mMainId) && isDualCamera()) {
            // dual camera, main
            picKey = CameraSettings.KEY_MAIN_PICTURE_SIZE;
            preKey = CameraSettings.KEY_MAIN_PREVIEW_SIZE;
            formatKey = CameraSettings.KEY_MAIN_PICTURE_FORMAT;
        }
        Size previewSize = mSettings.getPreviewSize(preKey, map);
        texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
        Size pictureSize = mSettings.getPictureSize(picKey, map);
        Surface surface = new Surface(texture);
        int format = mSettings.getPicFormat(formatKey);
        if (id.equals(mMainId)) {
            Log.d(TAG, " main surface config");
            mainImageReader = ImageReader.newInstance(pictureSize.getWidth(),
                    pictureSize.getHeight(), format, 1);
            mainImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    mCallback.onMainData(getByteFromReader(reader),
                            reader.getWidth(), reader.getHeight());
                }
            }, null);
            Size uiSize = CameraUtil.getPreviewUiSize(mContext, previewSize);
            mCallback.onViewChange(uiSize.getHeight(), uiSize.getWidth());
            return Arrays.asList(surface, mainImageReader.getSurface());
        } else {
            Log.d(TAG, " aux surface config");
            auxImageReader = ImageReader.newInstance(pictureSize.getWidth(),
                    pictureSize.getHeight(), format, 1);
            auxImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    mCallback.onAuxData(getByteFromReader(reader),
                            reader.getWidth(), reader.getHeight());
                }
            }, null);
            return Arrays.asList(surface, auxImageReader.getSurface());
        }
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

    private void setSessionCallback() {
        if (!isDualCamera()) {
            sendPreviewRequest();
        } else if (mainSession != null && auxSession != null) {
            sendPreviewRequest();
        }
    }

    //session callback
    private CameraCaptureSession.StateCallback mainStateCallback = new CameraCaptureSession
            .StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            Log.d(TAG, " main onConfigured");
            mainSession = session;
            setSessionCallback();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.d(TAG, "create session fail -> main");
        }
    };

    private CameraCaptureSession.StateCallback auxStateCallback = new CameraCaptureSession
            .StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            Log.d(TAG, " aux onConfigured");
            auxSession = session;
            setSessionCallback();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.d(TAG, "create session fail -> aux");
        }
    };
    //capture callback
    private CameraCaptureSession.CaptureCallback mainCaptureCallback =
            new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull
                CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Log.d(TAG, "main capture complete");
        }
    };
    private CameraCaptureSession.CaptureCallback auxCaptureCallback =
            new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull
                CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Log.d(TAG, "aux capture complete");
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
            mLatestPreviewRequest = request;
            updateAfState(result);
            mCallback.onRequestComplete();
        }
    };

    private CameraCaptureSession.CaptureCallback mAuxPreviewCallback = new CameraCaptureSession
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
            mAuxPreviewRequest = request;
        }
    };

    private void updateAfState(CaptureResult result) {
        Integer state = result.get(CaptureResult.CONTROL_AF_STATE);
        if (mFocusManager == null || state == null || mLatestAfState == state) {
            return;
        }
        mLatestAfState = state;
        switch (state) {
            case CaptureResult.CONTROL_AF_STATE_ACTIVE_SCAN:
                mFocusManager.startFocus();
                break;
            case CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED:
                mFocusManager.focusSuccess();
                break;
            case CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED:
                mFocusManager.focusFailed();
                break;
            case CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED:
                mFocusManager.focusSuccess();
                break;
            case CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN:
                mFocusManager.autoFocus();
                break;
            case CaptureResult.CONTROL_AF_STATE_PASSIVE_UNFOCUSED:
                mFocusManager.focusFailed();
                break;
            case CaptureResult.CONTROL_AF_STATE_INACTIVE:
                mFocusManager.hideFocusUI();
                break;
        }
    }

    public void release() {
        if (mainSession != null) {
            mainSession.close();
            mainSession = null;
        }
        if (auxSession != null) {
            auxSession.close();
            auxSession = null;
        }
        if (mainImageReader != null) {
            mainImageReader.close();
        }
        if (auxImageReader != null) {
            auxImageReader.close();
        }
    }
}
