package com.smewise.camera2.ui;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.smewise.camera2.R;
import com.smewise.camera2.callback.CameraUiEvent;


/**
 * Created by wenzhe on 16-3-18.
 */
public class ProfessionalUI extends CameraBaseUI implements TextureView.SurfaceTextureListener {

    private final String TAG = this.getClass().getSimpleName();

    private RelativeLayout mRootView;

    private GestureTextureView mPreviewTexture;
    private AppCompatSeekBar mFocusLensBar;
    private SeekView mFocusDistanceView;

    public ProfessionalUI(Context context, Handler handler, CameraUiEvent event) {
        super(event);
        mRootView = (RelativeLayout) LayoutInflater.from(context)
                .inflate(R.layout.module_professional_layout, null);
        mPreviewTexture = mRootView.findViewById(R.id.texture_preview);
        mPreviewTexture.setSurfaceTextureListener(this);
        mPreviewTexture.setGestureListener(this);

        // TODO: common ui
        mFocusDistanceView = mRootView.findViewById(R.id.focus_distance_view);
        String[] items = {"AUTO", "10", "20", "30", "40", "50", "60", "70", "80", "90", "100"};
        mFocusDistanceView.setContent(items);
        mFocusDistanceView.setVisibility(View.INVISIBLE);
        //mFocusDistanceView.setSeekListener(mListener);
        //mFocusLensBar = mRootView.findViewById(R.id.sb_focus_length);
        //mFocusLensBar.setOnSeekBarChangeListener(mFocusLensChangerListener);
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
        // preview frame is ready when receive second frame
        if (frameCount == 2) {return;}
        frameCount++;
        if (frameCount == 2) {
            uiEvent.onAction(CameraUiEvent.ACTION_PREVIEW_READY, null);
        }
    }

    private SeekBar.OnSeekBarChangeListener mFocusLensChangerListener =
            new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            float value = progress / (float) seekBar.getMax();
            uiEvent.onSettingChange(CaptureRequest.LENS_FOCUS_DISTANCE, value);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

}
