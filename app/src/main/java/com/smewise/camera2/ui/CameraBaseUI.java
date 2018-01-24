package com.smewise.camera2.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.smewise.camera2.R;
import com.smewise.camera2.manager.ModuleManager;
import com.smewise.camera2.utils.CameraUtil;
import com.smewise.camera2.utils.MediaFunc;

/**
 * Created by wenzhe on 3/3/17.
 */

public abstract class CameraBaseUI implements GestureTextureView.GestureListener, View
        .OnClickListener {
    private final String TAG = this.getClass().getSimpleName();
    private Point mDisplaySize;
    private int mVirtualKeyHeight;
    private int mTopBarHeight;
    CameraUiEvent uiEvent;
    //bottom controls
    private ShutterButton mShutter;
    private ImageButton mSetting;
    private CircleImageView mThumbnail;
    private RelativeLayout mPreviewContainer;
    private LinearLayout mBottomContainer;
    private FocusView mFocusView;
    private CoverView mCoverView;
    // ui action
    public static final String ACTION_CLICK = "camera.action.click";
    public static final String ACTION_CHANGE_MODULE = "camera.action.change.module";
    public static final String ACTION_SWITCH_CAMERA = "camera.action.switch.camera";


    public interface CameraUiEvent {

        void onPreviewUiReady(SurfaceTexture mainSurface, SurfaceTexture auxSurface);

        void onPreviewUiDestroy();

        void onTouchToFocus(float x, float y);

        void resetTouchToFocus();

        <T> void onSettingChange(CaptureRequest.Key<T> key, T value);

        <T> void onAction(String type, T value);
    }

    CameraBaseUI(Context context, Handler handler, CameraBaseUI.CameraUiEvent event) {
        mDisplaySize = CameraUtil.getDisplaySize(context);
        mVirtualKeyHeight = CameraUtil.getVirtualKeyHeight(context);
        mTopBarHeight = context.getResources().getDimensionPixelSize(R.dimen.tab_layout_height);
        uiEvent = event;
    }

    void initView(View rootView, Context context, Handler handler) {
        mShutter = (ShutterButton) rootView.findViewById(R.id.btn_shutter);
        mShutter.setOnClickListener(this);
        mSetting = (ImageButton) rootView.findViewById(R.id.btn_setting);
        mSetting.setOnClickListener(this);

        mBottomContainer = (LinearLayout) rootView.findViewById(R.id.bottom_container);

        mThumbnail = (CircleImageView) rootView.findViewById(R.id.thumbnail);
        mThumbnail.setOnClickListener(this);

        mPreviewContainer = (RelativeLayout) rootView.findViewById(R.id.rl_preview_container);
        mFocusView = new FocusView(context);
        mFocusView.setVisibility(View.GONE);
        mPreviewContainer.addView(mFocusView);

        setImgBitmap(context, handler);
    }

    public abstract RelativeLayout getRootView();

    public FocusView getFocusView() {
        return mFocusView;
    }

    public void setUIClickable(boolean clickable) {
        mThumbnail.setClickable(clickable);
        mSetting.setClickable(clickable);
        mShutter.setClickable(clickable);
    }

    void updateUiSize(int width, int height) {
        //update preview ui size
        boolean is4x3 = Math.abs(height / (double) width - CameraUtil.RATIO_4X3) < CameraUtil
                .ASPECT_TOLERANCE;
        boolean is18x9 = (mDisplaySize.y + mVirtualKeyHeight) / (double) mDisplaySize.x > 1.8;
        RelativeLayout.LayoutParams preParams = new RelativeLayout.LayoutParams(width, height);
        RelativeLayout.LayoutParams bottomBarParams =
                (RelativeLayout.LayoutParams) mBottomContainer.getLayoutParams();
        int bottomHeight = CameraUtil.getBottomBarHeight(mDisplaySize.x);
        if (mVirtualKeyHeight == 0 && is4x3) {
            // 4:3 no virtual key
            preParams.setMargins(0, mTopBarHeight, 0, 0);
            bottomHeight -= mTopBarHeight;
        } else if (mVirtualKeyHeight == 0) {
            // 16:9 no virtual key
            bottomHeight -= mTopBarHeight;
        } else if (!is4x3) {
            // 16:9 has virtual key
            if (is18x9) {
                preParams.setMargins(0, mDisplaySize.y - height, 0, 0);
            }
            mBottomContainer.setPadding(0, 0, 0, (int) (mVirtualKeyHeight / 1.5f));
        } else {
            // 4:3 has virtual key
            if (is18x9) {
                preParams.setMargins(0, mTopBarHeight, 0, 0);
                bottomHeight = mDisplaySize.y - height - mTopBarHeight + mVirtualKeyHeight;
            }
            mBottomContainer.setPadding(0, 0, 0, (int) (mVirtualKeyHeight / 1.5f));
        }
        mPreviewContainer.setLayoutParams(preParams);
        bottomBarParams.height = bottomHeight;
        mBottomContainer.setLayoutParams(bottomBarParams);

        mFocusView.initFocusArea(width, height);
    }

    public LinearLayout getBottomView() {
        return mBottomContainer;
    }

    private void setImgBitmap(final Context context, final Handler handler) {
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

    public void setThumbnail(Bitmap bitmap) {
        if (bitmap != null) {
            mThumbnail.setImageBitmap(bitmap);
            mThumbnail.setEnabled(true);
        }
    }

    public void setCoverView(CoverView coverView) {
        mCoverView = coverView;
    }

    /* View.OnClickListener*/
    @Override
    public void onClick(View v) {
        uiEvent.onAction(ACTION_CLICK, v);
    }

    /* GestureTextureView.GestureListener */
    @Override
    public void onClick(float x, float y) {
        uiEvent.onTouchToFocus(x, y);
    }

    @Override
    public void onSwipeLeft() {
        int newIndex = ModuleManager.getCurrentIndex() + 1;
        if (ModuleManager.isValidIndex(newIndex)) {
            mCoverView.setAlpha(1.0f);
            uiEvent.onAction(ACTION_CHANGE_MODULE, newIndex);
        }
    }

    @Override
    public void onSwipeRight() {
        int newIndex = ModuleManager.getCurrentIndex() - 1;
        if (ModuleManager.isValidIndex(newIndex)) {
            mCoverView.setAlpha(1.0f);
            uiEvent.onAction(ACTION_CHANGE_MODULE, newIndex);
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
