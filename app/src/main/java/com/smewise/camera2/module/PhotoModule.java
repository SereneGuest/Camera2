package com.smewise.camera2.module;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import com.smewise.camera2.Config;
import com.smewise.camera2.R;
import com.smewise.camera2.manager.Camera2Manager;
import com.smewise.camera2.manager.CameraSettings;
import com.smewise.camera2.manager.FocusOverlayManager;
import com.smewise.camera2.manager.SessionManager;
import com.smewise.camera2.ui.CameraBaseUI;
import com.smewise.camera2.ui.PhotoUI;
import com.smewise.camera2.utils.FileSaver;
import com.smewise.camera2.utils.MediaFunc;

/**
 * Created by wenzhe on 16-3-8.
 */
public class PhotoModule extends CameraModule implements FileSaver.FileListener {

    private SurfaceTexture mSurfaceTexture;

    private PhotoUI mUI;

    private SessionManager mSessionManager;
    private FocusOverlayManager mFocusManager;

    private static final String TAG = Config.getTag(PhotoModule.class);

    @Override
    protected void init() {
        mUI = new PhotoUI(appContext, mainHandler, mCameraUiEvent);
        mUI.setCoverView(getCoverView());
        mFocusManager = new FocusOverlayManager(mUI.getFocusView(), mainHandler.getLooper());
        mFocusManager.setListener(mCameraUiEvent);
        setCameraMenu(R.xml.menu_preference);
    }

    @Override
    public void start() {
        String cameraId = getSettingManager().getCameraId(CameraSettings.KEY_CAMERA_ID);
        Camera2Manager.getManager().setCameraId(cameraId, null);
        Camera2Manager.getManager().setDualCameraMode(false);
        Camera2Manager.getManager().openCamera(
                appContext, cameraEvent, mainHandler, getCameraThread());
        if (mSessionManager == null) {
            mSessionManager = new SessionManager(appContext, mainHandler,
                    mFocusManager, getSettingManager());
        }
        //dump support info
        CameraCharacteristics c = Camera2Manager.getManager().getCharacteristics();
        getSettingManager().dumpSupportInfo(c);

        // when module changed , need update listener
        fileSaver.setFileListener(this);
        rootView.addView(mUI.getRootView());

        isModulePause = false;
        isFirstPreviewLoaded = false;
        Log.d(TAG, "start module");
    }

    private Camera2Manager.Event cameraEvent = new Camera2Manager.Event() {
        @Override
        public void onCameraOpen(CameraDevice device) {
            isCameraOpened = true;
            if (isSurfaceAvailable) {
                mSessionManager.createPreviewSession(mSurfaceTexture, null, mCallback);
            }
        }
    };

    private SessionManager.Callback mCallback = new SessionManager.Callback() {

        @Override
        public void onMainData(byte[] data, int width, int height) {
            fileSaver.saveFile(width, height, getToolKit().getOrientation(), data, "CAMERA",
                    getSettingManager().getPicFormat(CameraSettings.KEY_PICTURE_FORMAT));
            mSessionManager.restartPreviewAfterShot();
        }

        @Override
        public void onAuxData(byte[] data, int width, int height) {

        }

        @Override
        public void onRequestComplete() {
            hideCoverView();
        }

        @Override
        public void onViewChange(int width, int height) {
            mUI.setTextureUIPreviewSize(width, height);
            mFocusManager.setPreviewSize(width, height);
        }
    };

    @Override
    public void stop() {
        isModulePause = true;
        mFocusManager.removeDelayMessage();
        mFocusManager.hideFocusUI();
        mSessionManager.release();
        Camera2Manager.getManager().releaseCamera(getCameraThread());
        isCameraOpened = false;
        isFirstPreviewLoaded = false;
        // remove view
        rootView.removeAllViews();
        Log.d(TAG, "stop module");
    }

    private void takePicture() {
        mUI.setUIClickable(false);
        mSessionManager.sendCaptureRequest(getToolKit().getOrientation());
    }

    @Override
    public void onFileSaved(Uri uri, String path, Bitmap thumbnail) {
        mUI.setUIClickable(true);
        mUI.setThumbnail(thumbnail);
        MediaFunc.setCurrentUri(uri);
        Log.d(TAG, "uri:" + uri.toString());
    }

    private CameraBaseUI.CameraUiEvent mCameraUiEvent = new CameraBaseUI.CameraUiEvent() {

        @Override
        public void onPreviewUiReady(SurfaceTexture mainSurface, SurfaceTexture auxSurface) {
            Log.d(TAG, "onSurfaceTextureAvailable");
            mSurfaceTexture = mainSurface;
            isSurfaceAvailable = true;
            if (isCameraOpened) {
                mSessionManager.createPreviewSession(mSurfaceTexture, null, mCallback);
            }
        }

        @Override
        public void onPreviewUiDestroy() {
            isSurfaceAvailable = false;
            Log.d(TAG, "onSurfaceTextureDestroyed");
        }

        @Override
        public void onTouchToFocus(float x, float y) {
            mFocusManager.startFocus(x, y);
            CameraCharacteristics c = Camera2Manager.getManager().getCharacteristics();
            mSessionManager.sendControlAfAeRequest(
                    mFocusManager.getFocusArea(c, true), mFocusManager.getFocusArea(c, false));
        }

        @Override
        public void resetTouchToFocus() {
            if (!isModulePause) {
                mSessionManager.sendControlFocusModeRequest(
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            }
        }

        @Override
        public <T> void onSettingChange(CaptureRequest.Key<T> key, T value) {
            mSessionManager.sendControlSettingRequest(key, value);
        }

        @Override
        public <T> void onAction(String type, T value) {
            switch (type) {
                case CameraBaseUI.ACTION_CLICK:
                    handleClick((View) value);
                    break;
                case CameraBaseUI.ACTION_CHANGE_MODULE:
                    setNewModule((Integer) value);
                    break;
                case CameraBaseUI.ACTION_SWITCH_CAMERA:
                    break;
                default:
                    break;
            }
        }
    };

    private void handleClick(View view) {
        switch (view.getId()) {
            case R.id.btn_shutter:
                takePicture();
                break;
            case R.id.btn_setting:
                showSetting();
                break;
            case R.id.thumbnail:
                MediaFunc.goToGallery(appContext);
                break;
            case R.id.camera_menu:
                cameraMenu.show(mUI.getBottomView(), 0, mUI.getBottomView().getHeight());
                break;
            default:
                break;
        }
    }
}
