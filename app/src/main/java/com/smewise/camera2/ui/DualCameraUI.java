package com.smewise.camera2.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.smewise.camera2.Config;
import com.smewise.camera2.R;
import com.smewise.camera2.manager.ModuleManager;
import com.smewise.camera2.utils.CameraUtil;
import com.smewise.camera2.utils.MediaFunc;


/**
 * Created by wenzhe on 16-3-18.
 */
public class DualCameraUI extends CameraBaseUI implements View.OnClickListener,
        GestureTextureView.GestureListener {


    private final String TAG = this.getClass().getSimpleName();


    private CameraUiEvent eventListener;

    private RelativeLayout rootView;
    //bottom controls
    private ShutterButton btnShutter;
    private ImageButton btnSetting;
    private ImageButton btnFlashlight;

    private GestureTextureView mainPreviewTexture;
    private TextureView auxPreviewTexture;
    private CircleImageView mThumbnail;
    private RelativeLayout previewContainer;
    private LinearLayout mBottomContainer;
    private FocusView focusView;
    private CoverView mCoverView;

    private boolean isMainSurfaceOk = false;
    private boolean isAuxSurfaceOk = false;
    private Handler mMainHandler;


    public DualCameraUI(Context context, Handler mainHandler, CameraUiEvent event) {
        super(context, mainHandler, event);
        this.eventListener = event;
        mMainHandler = mainHandler;
        rootView = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout
                .dual_camera_layout, null);
        btnShutter = (ShutterButton) rootView.findViewById(R.id.btn_shutter);
        btnShutter.setOnClickListener(this);
        btnSetting = (ImageButton) rootView.findViewById(R.id.btn_setting);
        btnSetting.setOnClickListener(this);

        //btnFlashlight = (ImageButton) rootView.findViewById(R.id.btn_flashlight);
        //btnFlashlight.setOnClickListener(this);

        mainPreviewTexture = (GestureTextureView) rootView.findViewById(R.id.main_texture);
        mainPreviewTexture.setSurfaceTextureListener(mainListener);
        mainPreviewTexture.setGestureListener(this);

        auxPreviewTexture = (TextureView) rootView.findViewById(R.id.aux_texture);
        auxPreviewTexture.setSurfaceTextureListener(auxListener);
        //auxPreviewTexture.setOnTouchListener(this);

        mThumbnail = (CircleImageView) rootView.findViewById(R.id.thumbnail);
        mThumbnail.setOnClickListener(this);

        previewContainer = (RelativeLayout) rootView.findViewById(R.id.rl_preview_container);
        focusView = new FocusView(context);
        focusView.setVisibility(View.GONE);
        previewContainer.addView(focusView);
        mBottomContainer = (LinearLayout) rootView.findViewById(R.id.bottom_container);

        setImgBitmap(context, mainHandler);
    }

    @Override
    public void setUIClickable(boolean clickable) {
        mThumbnail.setClickable(clickable);
        btnSetting.setClickable(clickable);
        btnShutter.setClickable(clickable);
        mainPreviewTexture.setClickable(clickable);
    }

    public void setCoverView(CoverView coverView) {
        mCoverView = coverView;
    }

    @Override
    public RelativeLayout getRootView() {
        return rootView;
    }

    @Override
    public FocusView getFocusView() {
        return focusView;
    }

    public TextureView getMainTextureView() {
        return mainPreviewTexture;
    }

    public TextureView getAuxTextureView() {
        return auxPreviewTexture;
    }

    @Override
    public void onClick(View v) {
        eventListener.onClick(v);
    }


    public void setTextureUIPreviewSize(final int w, final int h) {
        int auxW = (int) (w * Config.AUX_PREVIEW_SCALE);
        int auxH = (int) (h * Config.AUX_PREVIEW_SCALE);
        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(auxW, auxH);
        auxPreviewTexture.setLayoutParams(params1);
        updateUiSize(w, h, mBottomContainer, previewContainer);
        focusView.initFocusArea(w, h);
    }

    public void setImgBitmap(final Context context, final Handler mainHandler) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                final Bitmap bitmap = MediaFunc.getThumb(context);
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (bitmap == null) {
                            mThumbnail.setEnabled(false);
                            return;
                        }
                        mThumbnail.setImageBitmap(bitmap);
                        mThumbnail.setEnabled(true);
                    }
                });
            }
        }.start();
    }

    private TextureView.SurfaceTextureListener mainListener = new TextureView
            .SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            isMainSurfaceOk = true;
            setSurfaceCallback();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            isMainSurfaceOk = false;
            setSurfaceCallback();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };
    private TextureView.SurfaceTextureListener auxListener = new TextureView
            .SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            isAuxSurfaceOk = true;
            setSurfaceCallback();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            isAuxSurfaceOk = false;
            setSurfaceCallback();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    private void setSurfaceCallback() {
        if (isMainSurfaceOk && isAuxSurfaceOk) {
            eventListener.onPreviewUiReady(mainPreviewTexture.getSurfaceTexture(),
                    auxPreviewTexture.getSurfaceTexture());
        } else if (!isMainSurfaceOk && !isAuxSurfaceOk) {
            eventListener.onPreviewUiDestroy();
        } else {
            Log.d(TAG, "main or aux surface not available");
        }
    }


    @Override
    public void onClick(float x, float y) {
        eventListener.onTouchToFocus(x, y);
    }

    @Override
    public void onSwipeLeft() {
        int newIndex = ModuleManager.getCurrentIndex() + 1;
        if (ModuleManager.isValidIndex(newIndex)) {
            mCoverView.setAlpha(1.0f);
            eventListener.onChangeModule(newIndex);
        }
    }

    @Override
    public void onSwipeRight() {
        int newIndex = ModuleManager.getCurrentIndex() - 1;
        if (ModuleManager.isValidIndex(newIndex)) {
            mCoverView.setAlpha(1.0f);
            eventListener.onChangeModule(newIndex);
        }
    }

    @Override
    public void onSwipe(float percent) {
        int newIndex;
        if (percent < 0) {
            newIndex = ModuleManager.getCurrentIndex() + 1;
        } else {
            newIndex = ModuleManager.getCurrentIndex() - 1;
        }
        if (ModuleManager.isValidIndex(newIndex)) {
            mCoverView.setMode(newIndex);
            mCoverView.setAlpha(Math.abs(percent));
            mCoverView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCancel() {
        mCoverView.setVisibility(View.GONE);
        mCoverView.setAlpha(1.0f);
    }
}
