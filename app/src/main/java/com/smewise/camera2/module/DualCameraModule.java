package com.smewise.camera2.module;


import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.smewise.camera2.Config;
import com.smewise.camera2.R;
import com.smewise.camera2.manager.Camera2Manager;
import com.smewise.camera2.manager.CameraSettings;
import com.smewise.camera2.manager.Controller;
import com.smewise.camera2.manager.SessionManager;
import com.smewise.camera2.manager.FocusOverlayManager;
import com.smewise.camera2.ui.CameraBaseUI;
import com.smewise.camera2.ui.DualCameraUI;
import com.smewise.camera2.utils.FileSaver;
import com.smewise.camera2.utils.MediaFunc;


/**
 * Created by wenzhe on 16-3-8.
 */
public class DualCameraModule extends CameraModule implements FileSaver.FileListener {

    private static final String TAG = Config.getTag(DualCameraModule.class);

    private SurfaceTexture mainSurfaceTexture;
    private SurfaceTexture auxSurfaceTexture;

    private DualCameraUI mUI;
    private SessionManager sessionManager;

    private FocusOverlayManager mFocusManager;
    private int mPicCount = 0;

    @Override
    protected void init() {
        mUI = new DualCameraUI(appContext, mainHandler, mCameraUiEvent);
        mUI.setCoverView(getCoverView());
        mFocusManager = new FocusOverlayManager(mUI.getFocusView(), mainHandler.getLooper());
        mFocusManager.setListener(mCameraUiEvent);
    }

    @Override
    public void start() {
        String[] idList = Camera2Manager.getManager().getCameraIdList(appContext);
        String mainId = getSettingManager().getCameraId(CameraSettings.KEY_MAIN_CAMERA_ID);
        String auxId = getSettingManager().getCameraId(CameraSettings.KEY_AUX_CAMERA_ID,
                idList[idList.length - 1]);
        Camera2Manager.getManager().setCameraId(mainId, auxId);
        Camera2Manager.getManager().setDualCameraMode(true);
        Camera2Manager.getManager().openCamera(
                appContext, cameraEvent, mainHandler, getCameraThread());
        if (sessionManager == null) {
            sessionManager = new SessionManager(appContext, mainHandler,
                    mFocusManager, getSettingManager());
        }
        // when module changed , need update listener
        fileSaver.setFileListener(this);
        addModuleView(mUI.getRootView());
        Log.d(TAG, "start module");
    }

    @Override
    public void resume() {

    }

    private Camera2Manager.Event cameraEvent = new Camera2Manager.Event() {
        @Override
        public void onCameraOpen(CameraDevice device) {
            enableState(Controller.CAMERA_STATE_OPENED);
            if (stateEnabled(Controller.CAMERA_STATE_UI_READY)) {
                sessionManager.createPreviewSession(
                        mainSurfaceTexture, auxSurfaceTexture, mCallback);
            }
        }

        @Override
        public void onCameraClosed() {
            disableState(Controller.CAMERA_STATE_OPENED);
            if (mUI != null) {
                mUI.resetFrameCount();
            }
        }
    };

    private SessionManager.Callback mCallback = new SessionManager.Callback() {
        @Override
        public void onMainData(byte[] data, int width, int height) {
            Log.d(TAG, "main data complete");
            saveFile(data, width, height, CameraSettings.KEY_MAIN_PICTURE_FORMAT, "MAIN");
            mPicCount++;
            enableUiAfterShot();
        }

        @Override
        public void onAuxData(final byte[] data, final int width, final int height) {
            Log.d(TAG, "aux data complete");
            saveFile(data, width, height, CameraSettings.KEY_AUX_PICTURE_FORMAT, "AUX");
            mPicCount++;
            enableUiAfterShot();
        }

        @Override
        public void onRequestComplete() {}

        @Override
        public void onViewChange(int width, int height) {
            mUI.setTextureUIPreviewSize(width, height);
            mFocusManager.setPreviewSize(width, height);
        }
    };

    private void enableUiAfterShot() {
        if (mPicCount == 2) {
            mUI.setUIClickable(true);
            mPicCount = 0;
            sessionManager.restartPreviewAfterShot();
        }
    }

    @Override
    public void pause() {

    }

    @Override
    public void stop() {
        getCoverView().showCover();
        mFocusManager.hideFocusUI();
        mFocusManager.removeDelayMessage();
        sessionManager.release();
        Camera2Manager.getManager().releaseCamera(getCameraThread());
        Log.d(TAG, "stop module");
    }

    /**
     * FileSaver.FileListener
     * @param uri image file uri
     * @param path image file path
     * @param thumbnail image thumbnail
     */
    @Override
    public void onFileSaved(Uri uri, String path, Bitmap thumbnail) {
        MediaFunc.setCurrentUri(uri);
        mUI.setUIClickable(true);
        mUI.setThumbnail(thumbnail);
    }

    /**
     * callback for file save error
     * @param msg error msg
     */
    @Override
    public void onFileSaveError(String msg) {
        Toast.makeText(appContext,msg, Toast.LENGTH_LONG).show();
        mUI.setUIClickable(true);
    }

    private void takePicture() {
        mUI.setUIClickable(false);
        sessionManager.sendCaptureRequest(getToolKit().getOrientation());
    }

    private CameraBaseUI.CameraUiEvent mCameraUiEvent = new CameraBaseUI.CameraUiEvent() {

        @Override
        public void onPreviewUiReady(SurfaceTexture mainSurface, SurfaceTexture auxSurface) {
            Log.d(TAG, "onSurfaceTextureAvailable");
            mainSurfaceTexture = mainSurface;
            auxSurfaceTexture = auxSurface;
            enableState(Controller.CAMERA_STATE_UI_READY);
            if (stateEnabled(Controller.CAMERA_STATE_OPENED)) {
                sessionManager.createPreviewSession(mainSurface, auxSurface, mCallback);
            }
        }

        @Override
        public void onPreviewUiDestroy() {
            disableState(Controller.CAMERA_STATE_UI_READY);
            Log.d(TAG, "onSurfaceTextureDestroyed");
        }

        @Override
        public void onTouchToFocus(float x, float y) {
            mFocusManager.startFocus(x, y);
            CameraCharacteristics main =
                    Camera2Manager.getManager().getCharacteristics(Config.MAIN_ID);
            sessionManager.sendControlAfAeRequest(
                    mFocusManager.getFocusArea(main, true), mFocusManager.getFocusArea(main,
                            false));
        }

        @Override
        public void resetTouchToFocus() {
            if (stateEnabled(Controller.CAMERA_MODULE_RUNNING)) {
                sessionManager.sendControlFocusModeRequest(
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            }
        }

        @Override
        public <T> void onSettingChange(CaptureRequest.Key<T> key, T value) {

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
                case CameraBaseUI.ACTION_PREVIEW_READY:
                    getCoverView().hideCoverWithAnimation();
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
                showSetting(true);
                break;
            case R.id.thumbnail:
                MediaFunc.goToGallery(appContext);
                break;
        }
    }
}
