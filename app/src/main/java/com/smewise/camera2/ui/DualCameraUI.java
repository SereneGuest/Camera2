package com.smewise.camera2.ui;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.widget.RelativeLayout;

import com.smewise.camera2.Config;
import com.smewise.camera2.R;


/**
 * Created by wenzhe on 16-3-18.
 */
public class DualCameraUI extends CameraBaseUI implements GestureTextureView.GestureListener {

    private final String TAG = this.getClass().getSimpleName();

    private RelativeLayout mRootView;
    private GestureTextureView mMainPreviewTexture;
    private TextureView mAuxPreviewTexture;

    private int mCountFlag = 0;


    public DualCameraUI(Context context, Handler mainHandler, CameraUiEvent event) {
        super(context, mainHandler, event);
        mRootView = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout
                .dual_camera_layout, null);
        initView(mRootView, context, mainHandler);

        mMainPreviewTexture = (GestureTextureView) mRootView.findViewById(R.id.main_texture);
        mMainPreviewTexture.setSurfaceTextureListener(mMainListener);
        mMainPreviewTexture.setGestureListener(this);
        mAuxPreviewTexture = (TextureView) mRootView.findViewById(R.id.aux_texture);
        mAuxPreviewTexture.setSurfaceTextureListener(mAuxListener);
    }

    @Override
    public void setUIClickable(boolean clickable) {
        super.setUIClickable(clickable);
        mMainPreviewTexture.setClickable(clickable);
        mAuxPreviewTexture.setClickable(clickable);
    }

    @Override
    public RelativeLayout getRootView() {
        return mRootView;
    }

    public void setTextureUIPreviewSize(final int w, final int h) {
        int auxW = (int) (w * Config.AUX_PREVIEW_SCALE);
        int auxH = (int) (h * Config.AUX_PREVIEW_SCALE);
        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(auxW, auxH);
        mAuxPreviewTexture.setLayoutParams(params1);
        updateUiSize(w, h);
    }

    private TextureView.SurfaceTextureListener mMainListener = new TextureView
            .SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            mCountFlag++;
            setSurfaceCallback();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            mCountFlag--;
            setSurfaceCallback();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };
    private TextureView.SurfaceTextureListener mAuxListener = new TextureView
            .SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            mCountFlag++;
            setSurfaceCallback();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            mCountFlag--;
            setSurfaceCallback();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    private void setSurfaceCallback() {
        if (mCountFlag == 2) {
            uiEvent.onPreviewUiReady(mMainPreviewTexture.getSurfaceTexture(),
                    mAuxPreviewTexture.getSurfaceTexture());
        } else if (mCountFlag == 0) {
            uiEvent.onPreviewUiDestroy();
        } else {
            Log.d(TAG, "main or aux surface not available");
        }
    }

}
