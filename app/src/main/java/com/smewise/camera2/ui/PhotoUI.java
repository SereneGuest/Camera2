package com.smewise.camera2.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.smewise.camera2.R;
import com.smewise.camera2.manager.ModuleManager;
import com.smewise.camera2.utils.CameraUtil;
import com.smewise.camera2.utils.MediaFunc;


/**
 * Created by wenzhe on 16-3-18.
 */
public class PhotoUI extends CameraBaseUI implements View.OnClickListener, TextureView
        .SurfaceTextureListener, GestureTextureView.GestureListener {

    private final String TAG = this.getClass().getSimpleName();

    private CameraBaseUI.CameraUiEvent eventListener;
    private RelativeLayout rootView;
    //bottom controls
    private ShutterButton btnShutter;
    private ImageButton btnSetting;
    private GestureTextureView previewTexture;
    private CircleImageView mThumbnail;
    private RelativeLayout previewContainer;
    private LinearLayout mBottomContainer;
    private FocusView focusView;
    private CoverView mCoverView;


    public PhotoUI(Context context, Handler handler, CameraBaseUI.CameraUiEvent event) {
        super(context, handler, event);
        this.eventListener = event;
        rootView = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.photo_layout,
                null);
        btnShutter = (ShutterButton) rootView.findViewById(R.id.btn_shutter);
        btnShutter.setOnClickListener(this);
        btnSetting = (ImageButton) rootView.findViewById(R.id.btn_setting);
        btnSetting.setOnClickListener(this);

        //btnFlashlight = (ImageButton) rootView.findViewById(R.id.btn_flashlight);
        //btnFlashlight.setOnClickListener(this);
        mBottomContainer = (LinearLayout) rootView.findViewById(R.id.bottom_container);

        previewTexture = (GestureTextureView) rootView.findViewById(R.id.texture_preview);
        previewTexture.setSurfaceTextureListener(this);
        previewTexture.setGestureListener(this);

        mThumbnail = (CircleImageView) rootView.findViewById(R.id.thumbnail);
        mThumbnail.setOnClickListener(this);

        previewContainer = (RelativeLayout) rootView.findViewById(R.id.rl_preview_container);
        focusView = new FocusView(context);
        focusView.setVisibility(View.GONE);
        previewContainer.addView(focusView);

        setImgBitmap(context, handler);
    }

    @Override
    public void setUIClickable(boolean clickable) {
        mThumbnail.setClickable(clickable);
        btnSetting.setClickable(clickable);
        btnShutter.setClickable(clickable);
        previewTexture.setClickable(clickable);
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

    public TextureView getTextureView() {
        return previewTexture;
    }

    @Override
    public void onClick(View v) {
        eventListener.onClick(v);
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        eventListener.onPreviewUiReady(surface, null);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        eventListener.onPreviewUiDestroy();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public void setTextureUIPreviewSize(int w, int h) {
        updateUiSize(w, h, mBottomContainer, previewContainer);
        focusView.initFocusArea(w, h);
    }

    public void setImgBitmap(final Context context, final Handler handler) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                final Bitmap bitmap = MediaFunc.getThumb(context);
                handler.post(new Runnable() {
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
