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
    private Animator mShowAnimator;
    private Animator mHideAnimator;
    private boolean mIsAnimatorEnd = true;
    private int[] mModuleSrc = new int[]{
            R.mipmap.cover_icon_photo,
            R.mipmap.cover_icon_dual,
            R.mipmap.cover_icon_photo,
    };

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
    }

    public void setMode(int index) {
        if (mCoverIcon != null) {
            mCoverIcon.setImageResource(mModuleSrc[index]);
        }
    }

    private Animator createAnimation(boolean isShow) {
        Animator animator;
        if (isShow) {
            animator = ViewAnimationUtils.createCircularReveal(
                    this, getWidth() / 2, getHeight() / 2, 0, getHeight() / 2);
        } else {
            animator = ViewAnimationUtils.createCircularReveal(
                    this, getWidth() / 2, getHeight() / 2, getHeight() / 2, 0);
        }
        animator.setDuration(600);
        return animator;
    }

    private Animator createAlphaAnimation() {
        ObjectAnimator animator = new ObjectAnimator();
        animator.setTarget(this);
        animator.setPropertyName("alpha");
        animator.setFloatValues(0, 1.0f);
        animator.setDuration(300);
        return animator;
    }

    public void showCoverWithAnimation() {
        if (!mShowAnimator.isRunning()) {
            mShowAnimator.start();
            setVisibility(VISIBLE);
        }
    }
    public void showCover() {
        setVisibility(VISIBLE);
    }


    public void hideCoverWithAnimation() {
        if (mIsAnimatorEnd) {
            mIsAnimatorEnd = false;
            mHideAnimator.start();
        }
    }

    @Override
    public void onGlobalLayout() {
        mCoverIcon = (ImageView) this.findViewById(R.id.cover_icon);
        mShowAnimator = createAlphaAnimation();
        mHideAnimator = createAnimation(false);
        mHideAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                CoverView.this.setVisibility(GONE);
                mIsAnimatorEnd = true;
            }
        });
    }
}
