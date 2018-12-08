package com.smewise.camera2.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.Button;

import com.smewise.camera2.R;

/**
 * Created by wenzhe on 16-7-12.
 */

public class ShutterButton extends Button {

    private Paint mPaint;
    private int outerRadius;
    private int innerRadius;
    private int outerCircleColor;
    private int innerCircleColor;
    private int recordColor;
    private RectF rect;
    private String TAG = ShutterButton.class.getSimpleName();

    private String currentMode = PHOTO_MODE;
    public static final String PHOTO_MODE = "photo_mode";
    public static final String VIDEO_MODE = "video_mode";
    public static final String VIDEO_RECORDING_MODE = "video_record_mode";

    public ShutterButton(Context context) {
        this(context, null);
    }

    public ShutterButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShutterButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        outerRadius = (int) context.getResources().getDimension(R.dimen.shutter_outer_radius);
        outerCircleColor = context.getResources().getColor(R.color.outer_circle_color);
        innerCircleColor = context.getResources().getColor(R.color.inner_circle_color);
        recordColor = context.getResources().getColor(R.color.color_record);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        rect = new RectF();
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        innerRadius = (int) (getWidth() / 2 - 2.5 * outerRadius);
        int innerSmallRadius = getWidth() / 10;
        rect.left = getWidth() / 2 - innerSmallRadius;
        rect.top = getHeight() / 2 - innerSmallRadius;
        rect.right = getWidth() / 2 + innerSmallRadius;
        rect.bottom = getHeight() / 2 + innerSmallRadius;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        switch (currentMode) {
            case PHOTO_MODE:
                drawOuterCircle(canvas, outerCircleColor);
                drawInnerCircle(canvas, innerCircleColor);
                break;
            case VIDEO_MODE:
                drawOuterCircle(canvas, recordColor);
                drawInnerCircle(canvas, innerCircleColor);
                break;
            case VIDEO_RECORDING_MODE:
                drawOuterCircle(canvas, innerCircleColor);
                drawInnerCircle(canvas, recordColor);
                break;
        }
    }

    public void setMode(String mode) {
        currentMode = mode;
        invalidate();
    }

    private void drawOuterCircle(Canvas canvas, int color) {
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(outerRadius);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2 - outerRadius, mPaint);
    }

    private void drawInnerCircle(Canvas canvas, int color) {
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, innerRadius, mPaint);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            setAlpha(1.0f);
        } else {
            setAlpha(0.5f);
        }
    }

    @Override
    public void setClickable(boolean clickable) {
        super.setClickable(clickable);
        if (clickable) {
            setAlpha(1.0f);
        } else {
            setAlpha(0.5f);
        }
    }
}
