package com.smewise.camera2.ui;

import android.content.Context;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.smewise.camera2.R;
import com.smewise.camera2.utils.CameraUtil;

/**
 * Created by wenzhe on 3/3/17.
 */

public abstract class CameraBaseUI {
    private final String TAG = this.getClass().getSimpleName();
    protected Point displaySize;
    protected int virtualKeyHeight;
    protected int topBarHeight;


    public interface CameraUiEvent {
        void onClick(View view);

        void onPreviewUiReady(SurfaceTexture mainSurface, SurfaceTexture auxSurface);

        void onPreviewUiDestroy();

        void onTouchToFocus(float x, float y);

        void resetTouchToFocus();

        void onChangeModule(int index);

        <T> void onSettingChange(CaptureRequest.Key<T> key, T value);
    }

    protected CameraBaseUI(Context context, Handler handler, CameraBaseUI.CameraUiEvent event){
        displaySize = CameraUtil.getDisplaySize(context);
        virtualKeyHeight = CameraUtil.getVirtualKeyHeight(context);
        topBarHeight = context.getResources().getDimensionPixelSize(R.dimen.tab_layout_height);
    }

    public abstract RelativeLayout getRootView();

    public abstract FocusView getFocusView();

    public abstract void setUIClickable(boolean clickable);

    protected void updateUiSize(int width, int height, LinearLayout bottomBar, RelativeLayout
            previewLayout) {
        //update preview ui size
        boolean is4x3 = Math.abs(height / (double) width - CameraUtil.RATIO_4X3) < CameraUtil
                .ASPECT_TOLERANCE;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        int bottomHeight = CameraUtil.getBottomBarHeight(displaySize.x);
        if (virtualKeyHeight == 0 && is4x3) {
            params.setMargins(0, topBarHeight, 0, 0);
            bottomHeight -= topBarHeight;
        }
        previewLayout.setLayoutParams(params);
        //update bottom bar size
        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams) bottomBar.getLayoutParams();
        layoutParams.height = bottomHeight;
        bottomBar.setPadding(0, 0, 0, (int) (virtualKeyHeight / 1.5f));
        bottomBar.setLayoutParams(layoutParams);
    }
}
