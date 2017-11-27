package com.smewise.camera2.ui;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;


import com.smewise.camera2.R;


/**
 * Created by wenzhe on 16-3-18.
 */
public class PhotoUI extends CameraBaseUI implements TextureView.SurfaceTextureListener {

    private final String TAG = this.getClass().getSimpleName();

    private RelativeLayout mRootView;
    private GestureTextureView mPreviewTexture;
    private ImageButton mShowMenu;

    public PhotoUI(Context context, Handler handler, CameraBaseUI.CameraUiEvent event) {
        super(context, handler, event);
        mRootView = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.photo_layout,
                null);
        initView(mRootView, context, handler);

        mPreviewTexture = (GestureTextureView) mRootView.findViewById(R.id.texture_preview);
        mPreviewTexture.setSurfaceTextureListener(this);
        mPreviewTexture.setGestureListener(this);

        mShowMenu = (ImageButton) mRootView.findViewById(R.id.camera_menu);
        mShowMenu.setOnClickListener(this);
    }

    @Override
    public void setUIClickable(boolean clickable) {
        super.setUIClickable(clickable);
        mPreviewTexture.setClickable(clickable);
    }

    @Override
    public RelativeLayout getRootView() {
        return mRootView;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        uiEvent.onPreviewUiReady(surface, null);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        uiEvent.onPreviewUiDestroy();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public void setTextureUIPreviewSize(int w, int h) {
        updateUiSize(w, h);
    }

}
