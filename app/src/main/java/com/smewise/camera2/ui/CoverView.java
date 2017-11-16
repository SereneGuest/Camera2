package com.smewise.camera2.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smewise.camera2.R;

/**
 * Created by wenzhe on 9/13/17.
 */

public class CoverView extends LinearLayout implements ViewTreeObserver.OnGlobalLayoutListener {

    private ImageView mCoverIcon;
    private TextView mTextView;

    private Animator mShowAnimator;
    private Animator mHideAnimator;
    private int[] mModuleSrc = new int[]{
            R.mipmap.cover_icon_photo,
            R.mipmap.cover_icon_dual,
            R.mipmap.cover_icon_photo,
    };
    private String[] mModuleString;


    public CoverView(Context context) {
        this(context, null);
    }

    public CoverView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoverView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getViewTreeObserver().addOnGlobalLayoutListener(this);
        setClickable(true);
        mModuleString = getResources().getStringArray(R.array.module_list);
    }

    public void setMode(int index) {
        if (mTextView != null && mCoverIcon != null) {
            mTextView.setText(mModuleString[index]);
            mCoverIcon.setImageResource(mModuleSrc[index]);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCoverIcon = (ImageView) this.findViewById(R.id.cover_icon);
        mTextView = (TextView) this.findViewById(R.id.cover_text);
    }

    private Animator createAnimation(boolean isShow) {
        Animator animator;
        if (isShow) {
            //5个参数作用分别是 操作的view  圆心x坐标 圆心y坐标 动画开始半径 动画结束半径
            animator = ViewAnimationUtils.createCircularReveal(
                    this, getWidth() / 2, getHeight() / 2, 0, getHeight() / 2);
        } else {
            animator = ViewAnimationUtils.createCircularReveal(
                    this, getWidth() / 2, getHeight() / 2, getHeight() / 2, 0);
        }
        animator.setDuration(700);
        return animator;
    }

    private Animator createAlphaAnimation() {
        ObjectAnimator animator = new ObjectAnimator();
        animator.setTarget(this);
        animator.setPropertyName("alpha");
        animator.setFloatValues(0, 1.0f);
        animator.setDuration(500);
        return animator;
    }

    public void showCoverWithAnimation() {
        if (!mShowAnimator.isRunning()) {
            //mShowAnimator.start();
            setVisibility(VISIBLE);
        }
    }
    public void showCover() {
        setVisibility(VISIBLE);
    }


    public void hideCoverWithAnimation() {
        if (!mHideAnimator.isRunning()) {
            mHideAnimator.start();
        }
    }

    @Override
    public void onGlobalLayout() {
        mShowAnimator = createAlphaAnimation();
        mHideAnimator = createAnimation(false);
        mHideAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                CoverView.this.setVisibility(GONE);
            }
        });
    }
}
