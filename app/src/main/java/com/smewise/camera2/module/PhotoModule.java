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
import com.smewise.camera2.callback.CameraUiEvent;
import com.smewise.camera2.callback.RequestCallback;
import com.smewise.camera2.manager.CameraSession;
import com.smewise.camera2.manager.CameraSettings;
import com.smewise.camera2.manager.Controller;
import com.smewise.camera2.manager.DeviceManager;
import com.smewise.camera2.manager.FocusOverlayManager;
import com.smewise.camera2.manager.SingleDeviceManager;
import com.smewise.camera2.ui.CameraBaseMenu;
import com.smewise.camera2.ui.PhotoUI;
import com.smewise.camera2.utils.FileSaver;
import com.smewise.camera2.utils.MediaFunc;

/**
 * Created by wenzhe on 16-3-8.
 */
public class PhotoModule extends CameraModule implements FileSaver.FileListener,
        CameraBaseMenu.OnMenuClickListener {

    private SurfaceTexture mSurfaceTexture;
    private PhotoUI mUI;
    private CameraSession mSession;
    private SingleDeviceManager mDeviceMgr;
    private FocusOverlayManager mFocusManager;

    private static final String TAG = Config.getTag(PhotoModule.class);

    @Override
    protected void init() {
        mUI = new PhotoUI(appContext, mainHandler, mCameraUiEvent);
        mUI.setCoverView(getCoverView());
        mFocusManager = new FocusOverlayManager(getBaseUI().getFocusView(), mainHandler.getLooper());
        mFocusManager.setListener(mCameraUiEvent);
        setCameraMenu(R.xml.menu_preference, this);
        mSession = new CameraSession(appContext, mainHandler, getSettingManager());
        mDeviceMgr = new SingleDeviceManager(appContext, getCameraThread(), mCameraEvent);
    }

    @Override
    public void start() {
        String cameraId = getSettingManager().getCameraId(CameraSettings.KEY_CAMERA_ID);
        mDeviceMgr.setCameraId(cameraId);
        mDeviceMgr.openCamera(mainHandler);
        //dump support info
        CameraCharacteristics c = mDeviceMgr.getCharacteristics();
        getSettingManager().dumpSupportInfo(c);
        // when module changed , need update listener
        fileSaver.setFileListener(this);
        getBaseUI().setCameraUiEvent(mCameraUiEvent);
        addModuleView(mUI.getRootView());
        Log.d(TAG, "start module");
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
            getBaseUI().updateUiSize(width, height);
            mFocusManager.setPreviewSize(width, height);
        }

        @Override
        public void onAFStateChanged(int state) {
            super.onAFStateChanged(state);
            updateAFState(state, mFocusManager);
        }
    };

    @Override
    public void stop() {
        cameraMenu.close();
        getBaseUI().setCameraUiEvent(null);
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
        getBaseUI().setThumbnail(thumbnail);
        Log.d(TAG, "uri:" + uri.toString());
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

    private CameraUiEvent mCameraUiEvent = new CameraUiEvent() {

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
                case CameraUiEvent.ACTION_CLICK:
                    handleClick((View) value);
                    break;
                case CameraUiEvent.ACTION_CHANGE_MODULE:
                    setNewModule((Integer) value);
                    break;
                case CameraUiEvent.ACTION_SWITCH_CAMERA:
                    break;
                case CameraUiEvent.ACTION_PREVIEW_READY:
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
            case R.id.camera_menu:
                cameraMenu.show(getBaseUI().getBottomView(), 0, getBaseUI().getBottomView().getHeight());
                break;
            default:
                break;
        }
    }

    /**
     * CameraBaseMenu.OnMenuClickListener
     * @param key clicked menu key
     * @param value clicked menu value
     */
    @Override
    public void onMenuClick(String key, String value) {
        switch (key) {
            case CameraSettings.KEY_SWITCH_CAMERA:
                switchCamera(value);
                break;
            default:
                break;
        }
    }

    private void switchCamera(String cameraId) {
        if(!mDeviceMgr.getCameraId().equals(cameraId) && getSettingManager()
                .setCameraIdPref(CameraSettings.KEY_CAMERA_ID, cameraId)) {
            stopModule();
            startModule();
        }
    }
}
