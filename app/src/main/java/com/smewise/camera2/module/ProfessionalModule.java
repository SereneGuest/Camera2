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
import com.smewise.camera2.callback.RequestCallback;
import com.smewise.camera2.manager.CameraSession;
import com.smewise.camera2.manager.CameraSettings;
import com.smewise.camera2.manager.Controller;
import com.smewise.camera2.manager.DeviceManager;
import com.smewise.camera2.manager.FocusOverlayManager;
import com.smewise.camera2.manager.SingleDeviceManager;
import com.smewise.camera2.ui.CameraBaseUI;
import com.smewise.camera2.ui.ProfessionalUI;
import com.smewise.camera2.utils.FileSaver;
import com.smewise.camera2.utils.MediaFunc;

/**
 * Created by wenzhe on 16-3-8.
 */
public class ProfessionalModule extends CameraModule implements FileSaver.FileListener {

    private SurfaceTexture mSurfaceTexture;
    private ProfessionalUI mUI;
    private CameraSession mSession;
    private SingleDeviceManager mDeviceMgr;
    private FocusOverlayManager mFocusManager;

    private static final String TAG = Config.getTag(ProfessionalModule.class);

    @Override
    protected void init() {
        mUI = new ProfessionalUI(appContext, mainHandler, mCameraUiEvent);
        mUI.setCoverView(getCoverView());
        mFocusManager = new FocusOverlayManager(mUI.getFocusView(), mainHandler.getLooper());
        mFocusManager.setListener(mCameraUiEvent);
        mDeviceMgr = new SingleDeviceManager(appContext, getCameraThread(), mCameraEvent);
        mSession = new CameraSession(appContext, mainHandler, getSettingManager());
    }

    @Override
    public void start() {
        String cameraId = getSettingManager().getCameraId(CameraSettings.KEY_CAMERA_ID);
        mDeviceMgr.setCameraId(cameraId);
        mDeviceMgr.openCamera(mainHandler);
        // when module changed , need update listener
        fileSaver.setFileListener(this);
        addModuleView(mUI.getRootView());
        Log.d(TAG, "start module");
    }

    @Override
    public void resume() {

    }

    private DeviceManager.CameraEvent mCameraEvent = new DeviceManager.CameraEvent() {
        @Override
        public void onDeviceOpened(CameraDevice device) {
            super.onDeviceOpened(device);
            Log.d(TAG, "camera opened");
            mSession.setCameraDevice(device);
            enableState(Controller.CAMERA_STATE_OPENED);
            if (stateEnabled(Controller.CAMERA_STATE_UI_READY)) {
                mSession.createPreviewSession(mSurfaceTexture, mRequestCallback);
            }
        }

        @Override
        public void onDeviceClosed() {
            super.onDeviceClosed();
            disableState(Controller.CAMERA_STATE_OPENED);
            if (mUI != null) {
                mUI.resetFrameCount();
            }
            Log.d(TAG, "camera closed");
        }
    };

    private RequestCallback mRequestCallback = new RequestCallback() {
        @Override
        public void onDataBack(byte[] data, int width, int height) {
            super.onDataBack(data, width, height);
            saveFile(data, width, height, mDeviceMgr.getCameraId(),
                    CameraSettings.KEY_PICTURE_FORMAT, "CAMERA");
            mSession.restartPreviewAfterShot();
        }

        @Override
        public void onViewChange(int width, int height) {
            super.onViewChange(width, height);
            mUI.setTextureUIPreviewSize(width, height);
            mFocusManager.setPreviewSize(width, height);
        }

        @Override
        public void onAFStateChanged(int state) {
            super.onAFStateChanged(state);
        }
    };

    @Override
    public void stop() {
        getCoverView().showCover();
        mFocusManager.removeDelayMessage();
        mFocusManager.hideFocusUI();
        mSession.release();
        mDeviceMgr.releaseCamera();
        Log.d(TAG, "stop module");
    }

    private void takePicture() {
        mUI.setUIClickable(false);
        mSession.sendCaptureRequest(getToolKit().getOrientation());
    }

    @Override
    public void pause() {

    }

    /**
     * FileSaver.FileListener
     * @param uri image file uri
     * @param path image file path
     * @param thumbnail image thumbnail
     */
    @Override
    public void onFileSaved(Uri uri, String path, Bitmap thumbnail) {
        mUI.setUIClickable(true);
        mUI.setThumbnail(thumbnail);
        MediaFunc.setCurrentUri(uri);
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

    private CameraBaseUI.CameraUiEvent mCameraUiEvent = new CameraBaseUI.CameraUiEvent() {
        @Override
        public void onPreviewUiReady(SurfaceTexture mainSurface, SurfaceTexture auxSurface) {
            Log.d(TAG, "onSurfaceTextureAvailable");
            mSurfaceTexture = mainSurface;
            enableState(Controller.CAMERA_STATE_UI_READY);
            if (stateEnabled(Controller.CAMERA_STATE_OPENED)) {
                mSession.createPreviewSession(mSurfaceTexture, mRequestCallback);
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
            CameraCharacteristics c = mDeviceMgr.getCharacteristics();
            mSession.sendControlAfAeRequest(mFocusManager.getFocusArea(c, true),
                    mFocusManager.getFocusArea(c, false));
        }

        @Override
        public void resetTouchToFocus() {
            if (stateEnabled(Controller.CAMERA_MODULE_RUNNING)) {
                mSession.sendControlFocusModeRequest(
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            }
        }

        @Override
        public <T> void onSettingChange(CaptureRequest.Key<T> key, T value) {
            mSession.sendControlSettingRequest(key, value);
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
