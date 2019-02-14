package com.smewise.camera2.ui;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.smewise.camera2.R;

import java.util.LinkedList;

public class SeekView extends View {

    private static final String TAG = "SeekView";

    private static final int TICK_MARK_SIZE = 10;

    private Paint mPaint;
    private int mNormalTextColor;
    private int mActiveTextColor;
    private int mPaddingHor;
    private int mPaddingVer;
    private int mMarkHeight;
    private int mCurrentTranslate;
    private int mTickMarkWidth;
    private int mTickMarkHeight;
    private int mTickMarkMargin;
    private int mItemTickMarkWidth;
    private int mRealViewWidth;
    private float mStrokeWidth;
    private LinkedList<Item> mIndicatorItems = new LinkedList<>();

    private SeekListener mListener;
    private int mLastPosition = -1;

    private ValueAnimator mAnimation;
    private GestureDetector mGesture;

    public interface SeekListener {
        void onPositionChanged(int index);

        void onPercentChanged(float percent);
    }

    public SeekView(Context context) {
        this(context, null);
    }

    public SeekView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SeekView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttributeAndPaint(context, attrs, defStyleAttr);
        //Point point = CameraUtil.getDisplaySize(context);
        // init animation
        mAnimation = new ValueAnimator();
        mAnimation.addUpdateListener(mUpdateListener);
        mAnimation.addListener(mAnimatorListener);
        mAnimation.setInterpolator(new DecelerateInterpolator());
        mGesture = new GestureDetector(context, mGestureListener);
    }

    private void initAttributeAndPaint(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray array = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.SeekView, defStyleAttr, 0);
        mNormalTextColor = array.getColor(R.styleable.SeekView_textColor, Color.GRAY);
        mActiveTextColor = array.getColor(R.styleable.SeekView_activeColor, Color.RED);
        float mTextSize = array.getDimension(R.styleable.SeekView_textSize, 13);
        mPaddingHor = (int) array.getDimension(R.styleable.SeekView_paddingHor, 0);
        mPaddingVer = (int) array.getDimension(R.styleable.SeekView_paddingVer, 0);
        mTickMarkWidth = (int) array.getDimension(R.styleable.SeekView_tickMarkWidth, 5);
        mTickMarkHeight = (int) array.getDimension(R.styleable.SeekView_tickMarkHeight, 10);
        mTickMarkMargin = (int) array.getDimension(R.styleable.SeekView_tickMarkMargin, 10);
        mItemTickMarkWidth = mTickMarkMargin * TICK_MARK_SIZE + mTickMarkMargin;
        array.recycle();
        mMarkHeight = mPaddingVer / 2;
        // init paint
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mNormalTextColor);
        mPaint.setTextSize(mTextSize);
        mStrokeWidth = mPaint.getStrokeWidth();
    }

    public void setContent(String[] items) {
        mIndicatorItems.clear();
        for (String item : items) {
            addItem(item);
        }
        mRealViewWidth = getContentWidth();
        requestLayout();
        postInvalidate();
    }

    public void setSeekListener(SeekListener listener) {
        mListener = listener;
    }


    private void addItem(String name) {
        Rect rect = new Rect();
        mPaint.getTextBounds(name, 0, name.length(), rect);
        mIndicatorItems.add(new Item(name, rect.width(), rect.height(), mItemTickMarkWidth));
    }

    private void updateCallback() {
        // check position and call callback
        if (mListener == null) {return;}
        int absPosition = getMeasuredWidth() / 2 - mCurrentTranslate;
        int index = getSelectedIndex(absPosition);
        if (index != -1 && mLastPosition != index) {
            mLastPosition = index;
            mListener.onPositionChanged(index);
        }
        float percent = (float) absPosition / getContentWidth();
        mListener.onPercentChanged(percent);
    }

    private void drawAllView(Canvas canvas) {
        if (mIndicatorItems.size() == 0) {
            Log.e(TAG, "no item to draw");
            return;
        }
        int startX = 0;
        int startTextY;
        int startLineY = (getMeasuredHeight() - mTickMarkHeight) / 2;
        mPaint.setStrokeWidth(mStrokeWidth);
        for (int i = 0; i < mIndicatorItems.size(); i++) {
            Item item = mIndicatorItems.get(i);
            startTextY = (item.height + getMeasuredHeight()) / 2;
            // draw text
            startX = startX + mPaddingHor;
            drawText(canvas, item, startX, startTextY);
            startX = startX + item.width + mPaddingHor;
            if (i == mIndicatorItems.size() - 1) {
                // last one no need draw tick mark
                break;
            }
            // draw tick mark
            drawTickMark(canvas, startX, startLineY);
            startX = startX + item.tickMarkWidth;
        }
    }

    private void drawTickMark(Canvas canvas, int startX, int startY) {
        mPaint.setColor(mNormalTextColor);
        mPaint.setStrokeWidth(mTickMarkWidth);
        for (int i = 0; i < TICK_MARK_SIZE; i++) {
            startX += mTickMarkMargin;
            canvas.drawLine(startX, startY, startX, startY + mTickMarkHeight, mPaint);
        }
    }

    private void drawText(Canvas canvas, Item item,  int startX, int startY) {
        canvas.drawText(item.name, 0, item.name.length(), startX, startY, mPaint);
    }

    private int calcTranslate(int clickX) {
        int halfWidth = getMeasuredWidth() / 2;
        int currentPosition = clickX - mCurrentTranslate;
        int seekPosition = 0;
        int itemWidth;
        for (Item item : mIndicatorItems) {
            //seekPosition += mPaddingHor + item.tickMarkWidth;
            itemWidth = item.width + mPaddingHor * 2;
            if (currentPosition > seekPosition && currentPosition < seekPosition + itemWidth) {
                 currentPosition = seekPosition + itemWidth / 2;
                 break;
            }
            seekPosition += itemWidth + item.tickMarkWidth;
        }
        // halfWidth - clickX + mCurrentTranslate
        return getValidTranslate(halfWidth - currentPosition);
    }

    private int getSelectedIndex(int absolutePosition) {
        // TODO calculate once
        int seekPosition = 0;
        int itemWidth;
        for (int i = 0; i < mIndicatorItems.size(); i++) {
            itemWidth = mIndicatorItems.get(i).width + mPaddingHor * 2;
            if (absolutePosition > seekPosition
                    && absolutePosition < seekPosition + itemWidth) {
                return i;
            }
            seekPosition += itemWidth + mIndicatorItems.get(i).tickMarkWidth;
        }
        return  -1;
    }

    private void selectWithAnimation(int from, int to, long duration) {
        mAnimation.cancel();
        mAnimation.setIntValues(from, to);
        mAnimation.setDuration(duration);
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

    private Animator.AnimatorListener mAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            updateCallback();
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

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
        int width = 0;
        for (Item item : mIndicatorItems) {
            width += item.width + item.tickMarkWidth + 2 * mPaddingHor;
        }
        width -= mIndicatorItems.getLast().tickMarkWidth;
        return width;
    }

    private int getContentHeight() {
        int textHeight = 0;
        if (mIndicatorItems.size() != 0) {
            textHeight = mIndicatorItems.get(0).height  + mPaddingVer * 2;
        }
        return textHeight;
    }

    private void checkAndChangePosition(int newTranslate, boolean useAnimation, long duration) {
        if (!isTranslateValid(newTranslate)) {
            // invalid translate
            Log.w(TAG, "invalid translate");
            return;
        }
        // index change, select and notify
        if (useAnimation) {
            selectWithAnimation(mCurrentTranslate, newTranslate, duration);
        } else {
            mCurrentTranslate = newTranslate;
            postInvalidate();
            updateCallback();
        }
    }

    private int getValidTranslate(int newTranslate) {
        int maxTranslate = getMeasuredWidth() / 2;
        int minTranslate = maxTranslate - mRealViewWidth;
        if (newTranslate > maxTranslate) {
            return maxTranslate;
        } else if (newTranslate < minTranslate) {
            return minTranslate;
        } else {
            return newTranslate;
        }
    }

    private boolean isTranslateValid(int newTranslate) {
        int maxTranslate = getMeasuredWidth() / 2;
        int minTranslate = maxTranslate - mRealViewWidth;
        return newTranslate <= maxTranslate && newTranslate >= minTranslate;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(mActiveTextColor);
        int centerX = getMeasuredWidth() / 2;
        canvas.drawLine(centerX, 0, centerX, mMarkHeight, mPaint);
        canvas.drawLine(centerX, getMeasuredHeight() - mMarkHeight,
                centerX, getMeasuredHeight(), mPaint);
        mPaint.setColor(mNormalTextColor);
        canvas.save();
        canvas.translate(mCurrentTranslate, 0);
        drawAllView(canvas);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mAnimation.isRunning()) {
            return true;
        }
        return mGesture.onTouchEvent(event);
    }

    private GestureDetector.OnGestureListener mGestureListener = new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            checkAndChangePosition(calcTranslate((int) e.getX()), true, 500);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            int newTranslate = mCurrentTranslate - (int) distanceX;
            checkAndChangePosition(newTranslate, false, 0);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            int newTranslate = (int) (mCurrentTranslate +  (velocityX / 10));
            checkAndChangePosition(getValidTranslate(newTranslate), true, 400);
            return true;
        }
    };

    private static class Item {
        String name;
        int width;
        int height;
        int tickMarkWidth;
        Item(String n, int w, int h, int t) {
            name = n;
            width = w;
            height = h;
            tickMarkWidth = t;
        }
    }
}
