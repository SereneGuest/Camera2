package com.smewise.camera2.manager;

import android.graphics.Rect;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.MeteringRectangle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.smewise.camera2.Config;
import com.smewise.camera2.callback.CameraUiEvent;
import com.smewise.camera2.ui.CameraBaseUI;
import com.smewise.camera2.ui.FocusView;

import java.lang.ref.WeakReference;

/**
 * Created by wenzhe on 5/2/17.
 */

public class FocusOverlayManager {

    private static final String TAG = Config.getTag(FocusOverlayManager.class);

    private FocusView mFocusView;
    private MainHandler mHandler;
    private CameraUiEvent mListener;
    private int previewWidth;
    private int previewHeight;
    private float currentX;
    private float currentY;
    private final int AREA_SIZE = 200;

    private static final int HIDE_FOCUS_DELAY = 4000;
    private static final int MSG_HIDE_FOCUS = 0x10;


    private static class MainHandler extends Handler {
        final WeakReference<FocusOverlayManager> mManager;

        public MainHandler(FocusOverlayManager manager, Looper looper) {
            super(looper);
            mManager = new WeakReference<>(manager);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mManager.get() == null) {
                return;
            }
            switch (msg.what) {
                case MSG_HIDE_FOCUS:
                    mManager.get().hideFocusUI();
                    mManager.get().mListener.resetTouchToFocus();
                    break;
            }
        }
    }

    public FocusOverlayManager(FocusView focusView, Looper looper) {
        mFocusView = focusView;
        mHandler = new MainHandler(this, looper);
        mFocusView.resetToDefaultPosition();
    }

    public void setListener(CameraUiEvent listener) {
        mListener = listener;
    }

    public void setPreviewSize(int width, int height) {
        previewWidth = width;
        previewHeight = height;
    }
    /* just set focus view position, not start animation*/
    public void startFocus(float x, float y) {
        currentX = x;
        currentY = y;
        mHandler.removeMessages(MSG_HIDE_FOCUS);
        mFocusView.moveToPosition(x, y);
        //mFocusView.startFocus();
        mHandler.sendEmptyMessageDelayed(MSG_HIDE_FOCUS, HIDE_FOCUS_DELAY);
    }
    /* show focus view by af state */
    public void startFocus() {
        mHandler.removeMessages(MSG_HIDE_FOCUS);
        mFocusView.startFocus();
        mHandler.sendEmptyMessageDelayed(MSG_HIDE_FOCUS, HIDE_FOCUS_DELAY);
    }

    public void autoFocus() {
        mHandler.removeMessages(MSG_HIDE_FOCUS);
        mFocusView.resetToDefaultPosition();
        mFocusView.startFocus();
        mHandler.sendEmptyMessageDelayed(MSG_HIDE_FOCUS, 1000);
    }

    public void focusSuccess() {
        mFocusView.focusSuccess();
    }

    public void focusFailed() {
        mFocusView.focusFailed();
    }

    public void hideFocusUI() {
        mFocusView.resetToDefaultPosition();
        mFocusView.hideFocusView();
    }

    public void removeDelayMessage() {
        mHandler.removeMessages(MSG_HIDE_FOCUS);
    }

    public MeteringRectangle getFocusArea(CameraCharacteristics c, boolean isFocusArea) {
        if (isFocusArea) {
            return calcTapAreaForCamera2(c, previewWidth / 5, 1000);
        } else {
            return calcTapAreaForCamera2(c, previewWidth / 3, 800);
        }
    }

    private MeteringRectangle calcTapAreaForCamera2(CameraCharacteristics c, int areaSize, int
            weight) {
        Rect rect = c.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        Log.d(TAG, "active Rect:" + rect.toString());
        Rect newRect;
        int leftPos, topPos;
        float newX = currentY;
        float newY = previewWidth - currentX;
        leftPos = (int) ((newX / previewHeight) * rect.right);
        topPos = (int) ((newY / previewWidth) * rect.bottom);
        int left = clamp(leftPos - areaSize, 0, rect.right);
        int top = clamp(topPos - areaSize, 0, rect.bottom);
        int right = clamp(leftPos + areaSize, leftPos, rect.right);
        int bottom = clamp(topPos + areaSize, topPos, rect.bottom);
        newRect = new Rect(left, top, right, bottom);
        Log.d(TAG, newRect.toString());
        return new MeteringRectangle(newRect, weight);
    }

    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }
}
