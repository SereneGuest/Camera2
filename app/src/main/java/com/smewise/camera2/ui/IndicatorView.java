package com.smewise.camera2.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.smewise.camera2.R;
import com.smewise.camera2.utils.CameraUtil;

import java.util.LinkedList;

public class IndicatorView extends View {

    private static final String TAG = "IndicatorView";

    private Paint mPaint;
    private int mNormalTextColor;
    private int mActiveTextColor;
    private int mPaddingHor;
    private int mPaddingVer;
    private int mCurrentIndex;
    private int mCurrentTranslate;
    private LinkedList<Item> mIndicatorItems = new LinkedList<>();
    // for gesture
    private  float mClickDistance;
    private  float mMaxDistance;
    private  final long DELAY_TIME = 200;

    private IndicatorListener mListener;
    private float mDownX;
    private float mDownY;
    private long mTouchTime;
    private long mDownTime;
    private ValueAnimator mAnimation;
    private Point mDisplayPoint;

    public interface IndicatorListener {
        void onPositionChanged(int index);
    }

    public IndicatorView(Context context) {
        this(context, null);
    }

    public IndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttributeAndPaint(context, attrs, defStyleAttr);
        mDisplayPoint = CameraUtil.getDisplaySize(context);
        mClickDistance = mDisplayPoint.x / 20;
        mMaxDistance = mDisplayPoint.x / 10;
        // init animation
        mAnimation = new ValueAnimator();
        mAnimation.addUpdateListener(mUpdateListener);
    }

    public void setIndicatorListener(IndicatorListener listener) {
        mListener = listener;
    }

    public void addItem(String name) {
        Rect rect = new Rect();
        mPaint.getTextBounds(name, 0, name.length(), rect);
        mIndicatorItems.add(new Item(name, rect.width(), rect.height()));
        requestLayout();
        postInvalidate();
    }

    public void select(int index) {
        if (index >= mIndicatorItems.size()
                || index < 0 || index == mCurrentIndex) {
            Log.w(TAG, "invalid index or index not change:" + index);
            return;
        }
        mCurrentIndex = index;
        mCurrentTranslate = calcTranslate(index);
        postInvalidate();
    }

    public void selectWithAnimation(int index) {
        if (index >= mIndicatorItems.size()
                || index < 0 || index == mCurrentIndex) {
            Log.w(TAG, "invalid index or index not change:" + index);
            return;
        }
        mCurrentIndex = index;
        int from = mCurrentTranslate;
        int to = calcTranslate(index);
        selectWithAnimation(from, to);
    }


    public int getItemCount() {
        return mIndicatorItems.size();
    }

    private void initAttributeAndPaint(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray array = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.IndicatorView, defStyleAttr, 0);
        mNormalTextColor = array.getColor(R.styleable.IndicatorView_textColor, Color.GRAY);
        mActiveTextColor = array.getColor(R.styleable.IndicatorView_activeColor, Color.RED);
        float mTextSize = array.getDimension(R.styleable.IndicatorView_textSize, 13);
        mPaddingHor = (int) array.getDimension(R.styleable.IndicatorView_paddingHor, 0);
        mPaddingVer = (int) array.getDimension(R.styleable.IndicatorView_paddingVer, 0);
        array.recycle();
        // init paint
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mActiveTextColor);
        mPaint.setTextSize(mTextSize);
    }

    private void drawIndicatorText(Canvas canvas) {
        if (mIndicatorItems.size() == 0) {
            Log.e(TAG, "no item to draw text");
            return;
        }
        int startX = 0;
        int textWidth;
        int startY = (mIndicatorItems.get(0).height + getMeasuredHeight()) / 2;
        if (mCurrentTranslate == 0) {
            mCurrentTranslate = calcTranslate(mCurrentIndex);
        }
        canvas.translate(mCurrentTranslate, 0);
        for (int i = 0; i < mIndicatorItems.size(); i++) {
            startX = startX + mPaddingHor;
            int color = i == mCurrentIndex ? mActiveTextColor : mNormalTextColor;
            mPaint.setColor(color);
            String text = mIndicatorItems.get(i).name;
            textWidth = mIndicatorItems.get(i).width;
            canvas.drawText(text, 0, text.length(), startX,startY, mPaint);
            startX = startX + textWidth + mPaddingHor;
        }
    }

    private int calcTranslate(int index) {
        int halfWidth = mDisplayPoint.x / 2;
        int currentOffset = 0;
        for (int i = 0; i < index; i++) {
            currentOffset +=  mPaddingHor * 2 + mIndicatorItems.get(i).width;
        }
        currentOffset += mIndicatorItems.get(index).width / 2 + mPaddingHor;
        return halfWidth - currentOffset;
    }

    private void selectWithAnimation(int from, int to) {
        mAnimation.cancel();
        mAnimation.setIntValues(from, to);
        mAnimation.setDuration(200);
        mAnimation.start();
    }

    private ValueAnimator.AnimatorUpdateListener mUpdateListener =
            new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mCurrentTranslate = (Integer) animation.getAnimatedValue();
            postInvalidate();
        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int myWidth, myHeight;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY) {
            myWidth = MeasureSpec.getSize(widthMeasureSpec);
        } else if(widthMode == MeasureSpec.AT_MOST) {
            myWidth = getContentWidth();
        } else {
            myWidth = Math.max(getContentWidth(), MeasureSpec.getSize(widthMeasureSpec));
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            myHeight = MeasureSpec.getSize(heightMeasureSpec);
        } else if (heightMode == MeasureSpec.AT_MOST) {
            myHeight = getContentHeight();
        } else {
            myHeight = Math.max(getContentHeight(), MeasureSpec.getSize(heightMeasureSpec));
        }
        setMeasuredDimension(myWidth, myHeight);
    }

    private int getContentWidth() {
        int textWidth = 0;
        for (Item item : mIndicatorItems) {
            textWidth = textWidth + item.width + mPaddingHor * 2;
        }
        return textWidth;
    }

    private int getContentHeight() {
        int textHeight = 0;
        if (mIndicatorItems.size() != 0) {
            textHeight = mIndicatorItems.get(0).height  + mPaddingVer * 2;
        }
        return textHeight;
    }

    private int calcClickIndex(float dx) {
        int realClickX = (int) dx - mCurrentTranslate;
        int currentStart = 0;
        for (int i = 0; i < mIndicatorItems.size(); i++) {
            int start = currentStart;
            int end = currentStart + mIndicatorItems.get(i).width + mPaddingHor * 2;
            if (realClickX > start && realClickX < end) {
                return i;
            }
            currentStart = end;
        }
        return mCurrentIndex;
    }

    private void checkAndChangePosition(int index, boolean useAnimation) {
        if (mListener == null || index >= mIndicatorItems.size()
                || index == mCurrentIndex || index < 0 ) {
            return;
        }
        // index change, select and notify
        if (useAnimation) {
            selectWithAnimation(index);
        } else {
            select(index);
        }
        mListener.onPositionChanged(mCurrentIndex);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawIndicatorText(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mAnimation.isRunning()) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownTime = System.currentTimeMillis();
                mDownX = event.getX();
                mDownY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                //detectSwipe(mDownX, event.getX());
                break;
            case MotionEvent.ACTION_UP:
                mTouchTime = System.currentTimeMillis() - mDownTime;
                detectGesture(mDownX, event.getX(), mDownY, event.getY());
                break;
        }
        return true;
    }

    private void detectGesture(float downX, float upX, float downY, float upY) {
        if (mListener == null) {
            return;
        }
        float distanceX = upX - downX;
        float distanceY = upY - downY;
        if (Math.abs(distanceX) < mClickDistance
                && Math.abs(distanceY) < mClickDistance
                && mTouchTime < DELAY_TIME) {
            // click event, change index
            checkAndChangePosition(calcClickIndex(upX), true);
            return;
        }
        if (Math.abs(distanceX) > mMaxDistance) {
            int index = mCurrentIndex;
            if (distanceX > 0) {
                index--;
            } else {
                index++;
            }
            // swipe gesture large distance, change index
            checkAndChangePosition(index, true);
        } else if (mTouchTime < DELAY_TIME) {
            int index = mCurrentIndex;
            if (distanceX > 0) {
                index--;
            } else {
                index++;
            }
            // swipe gesture fast speed, change index
            checkAndChangePosition(index, true);
        }
        /*if (Math.abs(distanceX) < mMaxDistance && mTouchTime > DELAY_TIME) {
            // gesture cancel, not used
            //mListener.onCancel();
        }*/
    }

    /*private void detectSwipe(float downX, float moveX) {
        if (mListener == null) {
            return;
        }
        float alpha;
        if (Math.abs(moveX - downX) > mClickDistance) {
            alpha = ((int) (moveX - downX)) / mMaxDistance;
            if (alpha > 1f) {
                alpha = 1f;
            }
            if (alpha < -1f) {
                alpha = -1f;
            }
            //mListener.onSwipe(alpha);
        }
    }*/

    private static class Item {
        String name;
        int width;
        int height;
        Item(String n, int w, int h) {
            name = n;
            width = w;
            height = h;
        }
    }
}
