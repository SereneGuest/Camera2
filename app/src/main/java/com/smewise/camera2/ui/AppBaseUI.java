package com.smewise.camera2.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.smewise.camera2.R;
import com.smewise.camera2.callback.CameraUiEvent;
import com.smewise.camera2.manager.Controller;
import com.smewise.camera2.manager.ModuleManager;
import com.smewise.camera2.utils.CameraUtil;
import com.smewise.camera2.utils.MediaFunc;

/**
 * Created by wenzhe on 9/15/17.
 */

public class AppBaseUI implements View.OnClickListener {
    private CoverView mCoverView;
    private LinearLayout mIndicatorContainer;
    private Controller mController;
    private RelativeLayout mPreviewRootView;
    private ShutterButton mShutter;
    private ImageButton mSetting;
    private CircleImageView mThumbnail;
    private LinearLayout mBottomContainer;
    private FocusView mFocusView;
    private LinearLayout mMenuContainer;
    private CameraUiEvent mEvent;

    private Point mDisplaySize;
    private int mVirtualKeyHeight;
    private int mTopBarHeight;


    public AppBaseUI(Activity activity, Controller controller) {
        mController = controller;
        mCoverView = activity.findViewById(R.id.cover_view);
        mIndicatorContainer = activity.findViewById(R.id.module_indicator_container);

        mPreviewRootView = activity.findViewById(R.id.preview_root_view);
        mShutter = activity.findViewById(R.id.btn_shutter);
        mShutter.setOnClickListener(this);
        mSetting = activity.findViewById(R.id.btn_setting);
        mSetting.setOnClickListener(this);
        mBottomContainer = activity.findViewById(R.id.bottom_container);
        mThumbnail = activity.findViewById(R.id.thumbnail);
        mThumbnail.setOnClickListener(this);
        mMenuContainer = activity.findViewById(R.id.menu_container);

        mDisplaySize = CameraUtil.getDisplaySize(activity);
        mVirtualKeyHeight = CameraUtil.getVirtualKeyHeight(activity);
        mTopBarHeight = activity.getResources().getDimensionPixelSize(R.dimen.tab_layout_height);
        mFocusView = new FocusView(activity);
        mFocusView.setVisibility(View.GONE);
        mPreviewRootView.addView(mFocusView);
    }

    public void setCameraUiEvent(CameraUiEvent event) {
        mEvent = event;
    }

    public void setIndicatorView(View view) {
        mIndicatorContainer.removeAllViews();
        mIndicatorContainer.addView(view);
    }

    public RelativeLayout getRootView() {
        return mPreviewRootView;
    }

    public CoverView getCoverView() {
        return mCoverView;
    }

    public CameraTab getCameraTab() {
        return null;
    }

    public FocusView getFocusView() {
        return mFocusView;
    }

    public View getBottomView() {
        return mBottomContainer;
    }

    public void setMenuView(View view) {
        mMenuContainer.removeAllViews();
        mMenuContainer.addView(view);
    }

    public void removeMenuView() {
        mMenuContainer.removeAllViews();
    }

    public void updateUiSize(int width, int height) {
        //update preview ui size
        boolean is4x3 = width * 4 == height * 3;
        boolean is18x9 = (mDisplaySize.y + mVirtualKeyHeight) * 9 == mDisplaySize.x * 18;
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
        mPreviewRootView.setLayoutParams(preParams);
        bottomBarParams.height = bottomHeight;
        mBottomContainer.setLayoutParams(bottomBarParams);

        mFocusView.initFocusArea(width, height);
    }

    public void updateThumbnail(Context context, Handler handler) {
        final Bitmap bitmap = MediaFunc.getThumb(context);
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (bitmap == null) {
                    mThumbnail.setClickable(false);
                    return;
                }
                mThumbnail.setImageBitmap(bitmap);
                mThumbnail.setClickable(true);
            }
        });
    }

    public void setThumbnail(Bitmap bitmap) {
        if (mThumbnail != null && bitmap != null) {
            mThumbnail.setImageBitmap(bitmap);
        }
    }

    public void setUIClickable(boolean clickable) {
        mShutter.setClickable(clickable);
        mThumbnail.setClickable(clickable);
        mSetting.setClickable(clickable);
        if (mMenuContainer.getChildCount() > 0) {
            mMenuContainer.getChildAt(0).setClickable(clickable);
        }
        mIndicatorContainer.getChildAt(0).setClickable(clickable);
    }

    @Override
    public void onClick(View v) {
        if (mEvent != null) {
            mEvent.onAction(CameraUiEvent.ACTION_CLICK, v);
        }
    }
}
