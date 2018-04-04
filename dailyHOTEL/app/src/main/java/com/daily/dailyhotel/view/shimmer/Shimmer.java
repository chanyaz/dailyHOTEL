package com.daily.dailyhotel.view.shimmer;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Build;
import android.view.View;

public class Shimmer
{

    public static final int ANIMATION_DIRECTION_LTR = 0;
    public static final int ANIMATION_DIRECTION_RTL = 1;

    private static final int DEFAULT_REPEAT_COUNT = ValueAnimator.INFINITE;
    private static final long DEFAULT_DURATION = 1500;
    private static final long DEFAULT_START_DELAY = 0;
    private static final int DEFAULT_DIRECTION = ANIMATION_DIRECTION_LTR;
    private static final int DEFAULT_SIMMER_WIDTH = -1;

    int repeatCount;
    long duration;
    long startDelay;
    int direction;
    int simmerWidth;
    Animator.AnimatorListener animatorListener;

    ObjectAnimator animator;

    public Shimmer()
    {
        repeatCount = DEFAULT_REPEAT_COUNT;
        duration = DEFAULT_DURATION;
        startDelay = DEFAULT_START_DELAY;
        direction = DEFAULT_DIRECTION;
        simmerWidth = DEFAULT_SIMMER_WIDTH;
    }

    public int getRepeatCount()
    {
        return repeatCount;
    }

    public Shimmer setRepeatCount(int repeatCount)
    {
        this.repeatCount = repeatCount;
        return this;
    }

    public long getDuration()
    {
        return duration;
    }

    public Shimmer setDuration(long duration)
    {
        this.duration = duration;
        return this;
    }

    public int getSimmerWidth()
    {
        return simmerWidth;
    }

    public Shimmer setSimmerWidth(int width)
    {
        this.simmerWidth = width;
        return this;
    }

    public long getStartDelay()
    {
        return startDelay;
    }

    public Shimmer setStartDelay(long startDelay)
    {
        this.startDelay = startDelay;
        return this;
    }

    public int getDirection()
    {
        return direction;
    }

    public Shimmer setDirection(int direction)
    {

        if (direction != ANIMATION_DIRECTION_LTR && direction != ANIMATION_DIRECTION_RTL)
        {
            throw new IllegalArgumentException("The animation direction must be either ANIMATION_DIRECTION_LTR or ANIMATION_DIRECTION_RTL");
        }

        this.direction = direction;
        return this;
    }

    public Animator.AnimatorListener getAnimatorListener()
    {
        return animatorListener;
    }

    public Shimmer setAnimatorListener(Animator.AnimatorListener animatorListener)
    {
        this.animatorListener = animatorListener;
        return this;
    }

    public <V extends View & ShimmerViewBase> void start(final V shimmerView)
    {

        if (isAnimating())
        {
            return;
        }

        final Runnable animate = new Runnable()
        {
            @Override
            public void run()
            {

                shimmerView.setShimmering(true);

                float fromX = 0;
                float toX = simmerWidth == DEFAULT_SIMMER_WIDTH ? shimmerView.getWidth() : simmerWidth;
                if (direction == ANIMATION_DIRECTION_RTL)
                {
                    fromX = shimmerView.getWidth();
                    toX = 0;
                }

                animator = ObjectAnimator.ofFloat(shimmerView, "gradientX", fromX, toX);
                animator.setRepeatCount(repeatCount);
                animator.setDuration(duration);
                animator.setStartDelay(startDelay);
                animator.addListener(new Animator.AnimatorListener()
                {
                    @Override
                    public void onAnimationStart(Animator animation)
                    {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation)
                    {
                        shimmerView.setShimmering(false);

                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
                        {
                            shimmerView.postInvalidate();
                        } else
                        {
                            shimmerView.postInvalidateOnAnimation();
                        }

                        animator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation)
                    {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation)
                    {

                    }
                });

                if (animatorListener != null)
                {
                    animator.addListener(animatorListener);
                }

                animator.start();
            }
        };

        if (!shimmerView.isSetUp())
        {
            shimmerView.setAnimationSetupCallback(new ShimmerViewHelper.AnimationSetupCallback()
            {
                @Override
                public void onSetupAnimation(final View target)
                {
                    animate.run();
                }
            });
        } else
        {
            animate.run();
        }
    }

    public void cancel()
    {
        if (animator != null)
        {
            animator.cancel();
        }
    }

    public boolean isAnimating()
    {
        return animator != null && animator.isRunning();
    }
}


