package com.smewise.camera2.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.smewise.camera2.R;
import com.smewise.camera2.callback.CameraUiEvent;
import com.smewise.camera2.utils.CameraUtil;
import com.smewise.camera2.utils.MediaFunc;

/**
 * Created by wenzhe on 9/15/17.
 */

public class AppBaseUI implements View.OnClickListener {
    private CoverView mCoverView;
    private RelativeLayout mPreviewRootView;
    private ShutterButton mShutter;
    private ImageButton mSetting;
    private CircleImageView mThumbnail;
    private LinearLayout mBottomContainer;
    private FocusView mFocusView;
    private LinearLayout mMenuContainer;
    private LinearLayout mProMenuContainer;
    private CameraUiEvent mEvent;
    private IndicatorView mIndicatorView;
    private CameraMenu mCameraMenu;

    private Point mDisplaySize;
    private int mVirtualKeyHeight;
    private int mTopBarHeight;

    public AppBaseUI(Context context, View rootView) {
        mCoverView = rootView.findViewById(R.id.cover_view);
        mCameraMenu = new CameraMenu(context, R.xml.menu_preference);

        mPreviewRootView = rootView.findViewById(R.id.preview_root_view);
        mShutter = rootView.findViewById(R.id.btn_shutter);
        mShutter.setOnClickListener(this);
        mSetting = rootView.findViewById(R.id.btn_setting);
        mSetting.setOnClickListener(this);
        mBottomContainer = rootView.findViewById(R.id.bottom_container);
        mThumbnail = rootView.findViewById(R.id.thumbnail);
        mThumbnail.setOnClickListener(this);
        mMenuContainer = rootView.findViewById(R.id.menu_container);
        mProMenuContainer = rootView.findViewById(R.id.professional_menu);
        // add common menu to layout
        mMenuContainer.addView(mCameraMenu.getView());
        mIndicatorView = rootView.findViewById(R.id.indicator_view);

        mDisplaySize = CameraUtil.getDisplaySize(context);
        mVirtualKeyHeight = CameraUtil.getVirtualKeyHeight(context);
        mTopBarHeight = context.getResources()
                .getDimensionPixelSize(R.dimen.menu_item_height);
        mFocusView = new FocusView(context);
        mFocusView.setVisibility(View.GONE);
        mPreviewRootView.addView(mFocusView);
    }

    public void setCameraUiEvent(CameraUiEvent event) {
        mEvent = event;
    }

    public RelativeLayout getRootView() {
        return mPreviewRootView;
    }

    public CoverView getCoverView() {
        return mCoverView;
    }

    public FocusView getFocusView() {
        return mFocusView;
    }

    public View getBottomView() {
        return mBottomContainer;
    }

    public CameraMenu getCameraMenu() {
        return mCameraMenu;
    }

    public IndicatorView getIndicatorView() {
        return mIndicatorView;
    }

    public void setShutterMode(String mode) {
        mShutter.setMode(mode);
    }

    public void addProMenu(View view) {
        mProMenuContainer.setVisibility(View.VISIBLE);
        mProMenuContainer.addView(view);
    }

    public void removeProMenu() {
        mProMenuContainer.setVisibility(View.GONE);
        mProMenuContainer.removeAllViews();
    }

    public LinearLayout getProMenuContainer() {
        return mProMenuContainer;
    }

    /*public void removeMenuView() {
        mMenuContainer.removeAllViews();
    }*/

    /**
     * Adjust layout when based on preview width
     * @param width preview screen width
     * @param height preview screen height
     */
    public void updateUiSize(int width, int height) {
        mFocusView.initFocusArea(width, height);
        int realHeight = mDisplaySize.y + mVirtualKeyHeight;
        int bottomHeight = CameraUtil.getBottomBarHeight(mDisplaySize.x);
        RelativeLayout.LayoutParams previewParams = new RelativeLayout.LayoutParams(width, height);
        RelativeLayout.LayoutParams bottomBarParams =
                (RelativeLayout.LayoutParams) mBottomContainer.getLayoutParams();
        int topMargin = 0;
        boolean needTopMargin = (height + 2 * mTopBarHeight) < realHeight;
        boolean needAlignCenter = width == height;
        if (needAlignCenter)  {
            topMargin = (realHeight - mTopBarHeight - mVirtualKeyHeight - height) / 2;
        } else if (needTopMargin) {
            topMargin = mTopBarHeight;
        }
        int reservedHeight = realHeight - topMargin - height;
        boolean needAdjustBottomBar = reservedHeight > bottomHeight;
        if (needAdjustBottomBar) {
            bottomHeight = reservedHeight;
        }
        // preview
        previewParams.setMargins(0, topMargin, 0, 0);
        mPreviewRootView.setLayoutParams(previewParams);
        // bottom bar
        bottomBarParams.height = bottomHeight;
        mBottomContainer.setPadding(0, 0, 0, mVirtualKeyHeight);
        mBottomContainer.setLayoutParams(bottomBarParams);
    }

    /* should not call in main thread */
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
        mIndicatorView.setClickable(clickable);
    }

    @Override
    public void onClick(View v) {
        if (mEvent != null) {
            mEvent.onAction(CameraUiEvent.ACTION_CLICK, v);
        }
    }
}
