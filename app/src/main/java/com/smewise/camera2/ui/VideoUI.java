package com.smewise.camera2.ui;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.smewise.camera2.R;
import com.smewise.camera2.callback.CameraUiEvent;


/**
 * Created by wenzhe on 16-3-18.
 */
public class VideoUI extends CameraBaseUI implements TextureView.SurfaceTextureListener {

    private final String TAG = this.getClass().getSimpleName();

    private RelativeLayout mRootView;
    private GestureTextureView mPreviewTexture;
    private LinearLayout mRecTimerLayout;
    private Button mRecButton;
    private Chronometer mChronometer;
    private long mRecordingTime;

    public VideoUI(Context context, Handler handler, CameraUiEvent event) {
        super(event);
        mRootView = (RelativeLayout) LayoutInflater.from(context)
                .inflate(R.layout.module_video_layout, null);
        mRecTimerLayout = mRootView.findViewById(R.id.ll_record_timer);
        mRecTimerLayout.setOnClickListener(this);
        mChronometer = mRootView.findViewById(R.id.record_time);
        mRecButton = mRootView.findViewById(R.id.btn_record);
        mPreviewTexture = mRootView.findViewById(R.id.texture_preview);
        mPreviewTexture.setSurfaceTextureListener(this);
        mPreviewTexture.setGestureListener(this);
    }

    @Override
    public void setUIClickable(boolean clickable) {
        super.setUIClickable(clickable);
        mPreviewTexture.setClickable(clickable);
    }

    public void startVideoTimer() {
        mRecTimerLayout.setVisibility(View.VISIBLE);
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
    }

    public void pauseVideoTimer() {
        mChronometer.stop();
        mRecordingTime = SystemClock.elapsedRealtime() - mChronometer.getBase();
    }

    public void resumeVideoTimer() {
        mChronometer.setBase(SystemClock.elapsedRealtime() - mRecordingTime);
        mChronometer.start();
    }

    public void stopVideoTimer() {
        mChronometer.stop();
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mRecordingTime = 0;
        mRecTimerLayout.setVisibility(View.INVISIBLE);
    }

    public void refreshPauseButton(boolean recording) {
        if (recording) {
            mRecButton.setBackgroundResource(R.drawable.ic_vector_recoding);
        } else {
            mRecButton.setBackgroundResource(R.drawable.ic_vector_record_pause);
        }
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

}
