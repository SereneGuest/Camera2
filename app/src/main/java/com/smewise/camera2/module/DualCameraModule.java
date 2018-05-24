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
import com.smewise.camera2.manager.DualDeviceManager;
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
    private CameraSession mSession;
    private CameraSession mAuxSession;
    private DualDeviceManager mDeviceMgr;
    private FocusOverlayManager mFocusManager;
    private int mPicCount = 0;

    @Override
    protected void init() {
        mUI = new DualCameraUI(appContext, mainHandler, mCameraUiEvent);
        mUI.setCoverView(getCoverView());
        mFocusManager = new FocusOverlayManager(mUI.getFocusView(), mainHandler.getLooper());
        mFocusManager.setListener(mCameraUiEvent);
        mDeviceMgr = new DualDeviceManager(appContext, getCameraThread(), mCameraEvent);
        mSession = new CameraSession(appContext, mainHandler, getSettingManager());
        mAuxSession = new CameraSession(appContext, mainHandler, getSettingManager());
    }

    @Override
    public void start() {
        String[] idList = mDeviceMgr.getCameraIdList();
        String mainId = getSettingManager().getCameraId(CameraSettings.KEY_MAIN_CAMERA_ID);
        String auxId = getSettingManager().getCameraId(CameraSettings.KEY_AUX_CAMERA_ID,
                idList[idList.length - 1]);
        mDeviceMgr.setCameraId(mainId, auxId);
        mDeviceMgr.openCamera(mainHandler);
        // when module changed , need update listener
        fileSaver.setFileListener(this);
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
                mSession.createPreviewSession(mainSurfaceTexture, mRequestCallback);
                mAuxSession.createPreviewSession(auxSurfaceTexture, mAuxRequestCb);
            }
        }

        @Override
        public void onAuxDeviceOpened(CameraDevice device) {
            super.onAuxDeviceOpened(device);
            // method will be called before onDeviceOpened(CameraDevice device)
            mAuxSession.setCameraDevice(device);
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
            saveFile(data, width, height, mDeviceMgr.getCameraId(true),
                    CameraSettings.KEY_PICTURE_FORMAT, "MAIN");
            enableUiAfterShot();
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
            updateAFState(state, mFocusManager);
        }
    };

    private RequestCallback mAuxRequestCb = new RequestCallback() {
        @Override
        public void onDataBack(byte[] data, int width, int height) {
            super.onDataBack(data, width, height);
            saveFile(data, width, height, mDeviceMgr.getCameraId(false),
                    CameraSettings.KEY_PICTURE_FORMAT, "AUX");
            enableUiAfterShot();
        }
    };

    private void enableUiAfterShot() {
        if (mPicCount == 2) {
            mUI.setUIClickable(true);
            mPicCount = 0;
            mSession.restartPreviewAfterShot();
            mAuxSession.restartPreviewAfterShot();
        }
    }

    @Override
    public void stop() {
        getCoverView().showCover();
        mFocusManager.hideFocusUI();
        mFocusManager.removeDelayMessage();
        mSession.release();
        mAuxSession.release();
        mDeviceMgr.releaseCamera();
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
        mSession.sendCaptureRequest(getToolKit().getOrientation());
        mAuxSession.sendCaptureRequest(getToolKit().getOrientation());
    }

    private CameraBaseUI.CameraUiEvent mCameraUiEvent = new CameraBaseUI.CameraUiEvent() {

        @Override
        public void onPreviewUiReady(SurfaceTexture mainSurface, SurfaceTexture auxSurface) {
            Log.d(TAG, "onSurfaceTextureAvailable");
            mainSurfaceTexture = mainSurface;
            auxSurfaceTexture = auxSurface;
            enableState(Controller.CAMERA_STATE_UI_READY);
            if (stateEnabled(Controller.CAMERA_STATE_OPENED)) {
                mSession.createPreviewSession(mainSurface, mRequestCallback);
                mAuxSession.createPreviewSession(auxSurface, mAuxRequestCb);
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
            CameraCharacteristics main = mDeviceMgr.getCharacteristics(true);
            CameraCharacteristics aux = mDeviceMgr.getCharacteristics(false);
            mSession.sendControlAfAeRequest(mFocusManager.getFocusArea(main, true),
                    mFocusManager.getFocusArea(main, false));
            mAuxSession.sendControlAfAeRequest(mFocusManager.getFocusArea(main, true),
                    mFocusManager.getFocusArea(aux, false));
        }

        @Override
        public void resetTouchToFocus() {
            if (stateEnabled(Controller.CAMERA_MODULE_RUNNING)) {
                mSession.sendControlFocusModeRequest(
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                mAuxSession.sendControlFocusModeRequest(
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
