/**
 * Copyright (c) 2014 Daily Co., Ltd. All rights reserved.
 * <p>
 * RatingHotelFragment (호텔 만족도 조사 화면)
 * <p>
 * 호텔 만족도 조사를 위한 화면
 */
package com.twoheart.dailyhotel.screen.review;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.NestedScrollView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daily.base.util.ScreenUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.ReviewPickQuestion;
import com.twoheart.dailyhotel.model.ReviewScoreQuestion;
import com.twoheart.dailyhotel.place.base.BaseLayout;
import com.twoheart.dailyhotel.place.base.OnBaseEventListener;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.Util;

public class ReviewLayout extends BaseLayout implements View.OnClickListener, NestedScrollView.OnScrollChangeListener
{
    private static final int REQUEST_START_ANIMATION = 1;

    private View mToolbar, mImageDimView;
    private NestedScrollView mNestedScrollView;
    private ViewGroup mScrollLayout;
    private SimpleDraweeView mPlaceImageView;
    private TextView mPlaceNameTextView, mPeriodTextView;
    private TextView mToolbarTitle, mToolbarSubTitle;
    private TextView mConfirmTextView;

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case REQUEST_START_ANIMATION:
                    animationInVisible();
                    break;
            }
        }
    };

    public interface OnEventListener extends OnBaseEventListener
    {
        void onReviewScoreTypeClick(int position, int reviewScore);

        void onReviewPickTypeClick(int position, int selectedType);

        void onReviewCommentClick(int position, String comment);

        void onConfirmClick();

        void onBackPressed();

        void onReviewDetailAnimationEnd();
    }

    public ReviewLayout(Context context, OnBaseEventListener listener)
    {
        super(context, listener);
    }

    @Override
    protected void initLayout(View view)
    {
        initToolbar(view);

        mNestedScrollView = (NestedScrollView) view.findViewById(R.id.scrollView);
        mScrollLayout = (ViewGroup) mNestedScrollView.findViewById(R.id.scrollLayout);

        mImageDimView = view.findViewById(R.id.imageDimView);
        mImageDimView.setAlpha(0.0f);

        int imageHeight = ScreenUtils.getRatioHeightType4x3(ScreenUtils.getScreenWidth(mContext));
        mPlaceImageView = (com.facebook.drawee.view.SimpleDraweeView) view.findViewById(R.id.placeImageView);
        ViewGroup.LayoutParams layoutParams = mPlaceImageView.getLayoutParams();
        layoutParams.height = imageHeight;
        mPlaceImageView.setLayoutParams(layoutParams);

        mPlaceNameTextView = (TextView) view.findViewById(R.id.placeNameTextView);
        mPeriodTextView = (TextView) view.findViewById(R.id.periodTextView);

        mNestedScrollView.setOnScrollChangeListener(this);

        mConfirmTextView = (TextView) view.findViewById(R.id.confirmTextView);
        mConfirmTextView.setOnClickListener(this);
    }

    private void initToolbar(View view)
    {
        mToolbar = view.findViewById(R.id.toolbar);
        mToolbarTitle = (TextView) mToolbar.findViewById(R.id.toolbarTitle);
        mToolbarSubTitle = (TextView) mToolbar.findViewById(R.id.toolbarSubTitle);

        mToolbarTitle.setAlpha(0.0f);
        mToolbarSubTitle.setAlpha(0.0f);

        View closeView = mToolbar.findViewById(R.id.closeView);
        closeView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((OnEventListener) mOnEventListener).onBackPressed();
            }
        });
    }

    public void setPlaceImageUrl(Context context, String imageUrl)
    {
        if (mPlaceImageView == null)
        {
            return;
        }

        Util.requestImageResize(context, mPlaceImageView, imageUrl);
    }

    public void setPlaceInformation(String placeName, String period)
    {
        if (mPlaceNameTextView == null || mPeriodTextView == null)
        {
            return;
        }

        mPlaceNameTextView.setText(placeName);
        mPeriodTextView.setText(period);

        mToolbarTitle.setText(placeName);
        mToolbarSubTitle.setText(period);
    }

    public void addScrollLayout(View view)
    {
        if (mScrollLayout == null)
        {
            return;
        }

        mScrollLayout.addView(view);
    }

    public Object getReviewValue(int position)
    {
        if (mScrollLayout == null || mScrollLayout.getChildCount() <= position)
        {
            return null;
        }

        ReviewCardLayout reviewCardLayout = (ReviewCardLayout) mScrollLayout.getChildAt(position);

        return reviewCardLayout.getReviewValue();
    }

    public boolean nextFocusReview(int position)
    {
        boolean hasNext = false;

        int count = mScrollLayout.getChildCount();

        for (int i = position; i < count; i++)
        {
            final ReviewCardLayout childReviewCardLayout = (ReviewCardLayout) mScrollLayout.getChildAt(i);

            if (childReviewCardLayout.isChecked() == false)
            {
                int cardWidth = ScreenUtils.getScreenWidth(mContext) - ScreenUtils.dpToPx(mContext, 30);
                final int cardHeight = ScreenUtils.getRatioHeightType4x3(cardWidth);

                mNestedScrollView.smoothScrollTo(0, childReviewCardLayout.getTop() - cardHeight / 2);

                hasNext = true;
                break;
            }
        }

        if (hasNext == false)
        {
            for (int i = 0; i < position; i++)
            {
                final ReviewCardLayout childReviewCardLayout = (ReviewCardLayout) mScrollLayout.getChildAt(i);

                if (childReviewCardLayout.isChecked() == false)
                {
                    int cardWidth = ScreenUtils.getScreenWidth(mContext) - ScreenUtils.dpToPx(mContext, 30);
                    final int cardHeight = ScreenUtils.getRatioHeightType4x3(cardWidth);

                    mNestedScrollView.smoothScrollTo(0, childReviewCardLayout.getTop() - cardHeight / 2);

                    hasNext = true;
                    break;
                }
            }
        }

        return hasNext;
    }

    public void setReviewCommentView(String text)
    {
        View view = mScrollLayout.getChildAt(mScrollLayout.getChildCount() - 1);

        if (view instanceof ReviewCommentCardLayout)
        {
            ((ReviewCommentCardLayout) view).setReviewCommentView(text);
        }
    }

    public View getReviewScoreView(Context context, int position, ReviewScoreQuestion reviewScoreQuestion)
    {
        ReviewScoreCardLayout reviewScoreCardLayout = new ReviewScoreCardLayout(mContext, position, reviewScoreQuestion);
        reviewScoreCardLayout.setOnScoreClickListener(new com.twoheart.dailyhotel.screen.review.ReviewScoreCardLayout.OnScoreClickListener()
        {
            @Override
            public void onClick(ReviewCardLayout reviewCardLayout, int reviewScore)
            {
                ((OnEventListener) mOnEventListener).onReviewScoreTypeClick(reviewCardLayout.position, reviewScore);
            }
        });
        return reviewScoreCardLayout;
    }

    public View getReviewPickView(Context context, int position, ReviewPickQuestion reviewPickQuestion)
    {
        ReviewPickCardLayout reviewPickCardLayout = new ReviewPickCardLayout(mContext, position, reviewPickQuestion);
        reviewPickCardLayout.setOnPickClickListener(new ReviewPickCardLayout.OnPickClickListener()
        {
            @Override
            public void onClick(ReviewCardLayout reviewCardLayout, int selectedType)
            {
                ((OnEventListener) mOnEventListener).onReviewPickTypeClick(reviewCardLayout.position, selectedType);
            }
        });

        return reviewPickCardLayout;
    }

    public View getReviewCommentView(Context context, int position, Constants.PlaceType placeType)
    {
        ReviewCommentCardLayout reviewCommentCardLayout = new ReviewCommentCardLayout(mContext, position, placeType);
        reviewCommentCardLayout.setOnCommentClickListener(new ReviewCommentCardLayout.OnCommentClickListener()
        {
            @Override
            public void onClick(ReviewCardLayout reviewCardLayout, String comment)
            {
                ((OnEventListener) mOnEventListener).onReviewCommentClick(reviewCardLayout.position, comment);
            }
        });

        return reviewCommentCardLayout;
    }

    public void setVisibility(boolean visibility)
    {
        setVisibility(visibility == true ? View.VISIBLE : View.INVISIBLE);
    }

    protected void showReviewDetailAnimation()
    {
        final float y = ScreenUtils.getScreenHeight(mContext);

        // 리스트 높이 + 아이콘 높이(실제 화면에 들어나지 않기 때문에 높이가 정확하지 않아서 내부 높이를 더함)
        int height = ScreenUtils.getScreenHeight(mContext);

        mRootView.setTranslationY(height);

        final ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mRootView, "y", y, y - height);
        objectAnimator.setDuration(200);
        objectAnimator.addListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
                if (mRootView.getVisibility() != View.VISIBLE)
                {
                    mRootView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                objectAnimator.removeAllListeners();
                objectAnimator.removeAllListeners();

                ((OnEventListener) mOnEventListener).onReviewDetailAnimationEnd();
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

        objectAnimator.start();
    }

    protected void hideReviewDetailAnimation()
    {
        final float y = mRootView.getTop();

        final ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mRootView, "y", y, mRootView.getBottom());
        objectAnimator.setDuration(200);
        objectAnimator.addListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                objectAnimator.removeAllListeners();
                objectAnimator.removeAllUpdateListeners();

                mOnEventListener.finish();
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

        objectAnimator.start();
    }

    public void startAnimation()
    {
        if (mScrollLayout == null)
        {
            return;
        }

        mNestedScrollView.post(new Runnable()
        {
            @Override
            public void run()
            {
                animationInVisible();
            }
        });
    }

    public void stopAnimation()
    {
        if (mScrollLayout == null)
        {
            return;
        }

        int count = mScrollLayout.getChildCount();

        for (int i = 0; i < count; i++)
        {
            View view = mScrollLayout.getChildAt(i);

            if (view instanceof ReviewScoreCardLayout)
            {
                ((ReviewScoreCardLayout) view).stopEmoticonAnimation();
            }
        }
    }

    public void pauseAnimation()
    {
        if (mScrollLayout == null)
        {
            return;
        }

        int count = mScrollLayout.getChildCount();

        for (int i = 0; i < count; i++)
        {
            View view = mScrollLayout.getChildAt(i);

            if (view instanceof ReviewScoreCardLayout)
            {
                ((ReviewScoreCardLayout) view).pauseEmoticonAnimation();
            }
        }
    }

    public void resumeAnimation()
    {
        if (mScrollLayout == null)
        {
            return;
        }

        int count = mScrollLayout.getChildCount();

        for (int i = 0; i < count; i++)
        {
            View view = mScrollLayout.getChildAt(i);

            if (view instanceof ReviewScoreCardLayout)
            {
                ((ReviewScoreCardLayout) view).resumeEmoticonAnimation();
            }
        }
    }

    public boolean hasUncheckedReview()
    {
        if (mScrollLayout == null)
        {
            return false;
        }

        int count = mScrollLayout.getChildCount();

        for (int i = 0; i < count; i++)
        {
            ReviewCardLayout childReviewCardLayout = (ReviewCardLayout) mScrollLayout.getChildAt(i);

            if (childReviewCardLayout.isChecked() == false)
            {
                return true;
            }
        }

        return false;
    }

    public int getUncheckedReviewCount()
    {
        if (mScrollLayout == null)
        {
            return -1;
        }

        int count = mScrollLayout.getChildCount();
        int uncheckedReviewCount = 0;

        for (int i = 0; i < count; i++)
        {
            ReviewCardLayout childReviewCardLayout = (ReviewCardLayout) mScrollLayout.getChildAt(i);

            if (childReviewCardLayout.isChecked() == false)
            {
                uncheckedReviewCount++;
            }
        }

        return uncheckedReviewCount;
    }

    public void setConfirmTextView(String text, boolean enabled)
    {
        if (mConfirmTextView == null)
        {
            return;
        }

        mConfirmTextView.setText(text);
        mConfirmTextView.setEnabled(enabled);
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.confirmTextView:
                ((OnEventListener) mOnEventListener).onConfirmClick();
                break;
        }
    }

    @Override
    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY)
    {
        mHandler.removeMessages(REQUEST_START_ANIMATION);
        pauseAnimation();

        int toolbarHeight = mToolbar.getHeight();

        if (toolbarHeight >= scrollY)
        {
            mPlaceImageView.setTranslationY(-scrollY * 0.5f);

            final int halfToolbarHeight = toolbarHeight / 2;

            if (halfToolbarHeight > scrollY)
            {
                float vectorValue = (float) scrollY / halfToolbarHeight;

                mToolbar.setBackgroundColor(0x00000000);

                float textAlphaValue = 1 - vectorValue * 2;
                textAlphaValue = textAlphaValue < 0 ? 0 : textAlphaValue;
                mPlaceNameTextView.setAlpha(textAlphaValue);
                mPeriodTextView.setAlpha(textAlphaValue);

                mToolbarTitle.setAlpha(0.0f);
                mToolbarSubTitle.setAlpha(0.0f);
            } else
            {
                float vectorValue = (float) (scrollY - halfToolbarHeight) / halfToolbarHeight;

                int alphaValue = (int) (0x4d * vectorValue);
                mToolbar.setBackgroundColor((alphaValue << 24) & 0xff000000);

                mPlaceNameTextView.setAlpha(0.0f);
                mPeriodTextView.setAlpha(0.0f);

                mToolbarTitle.setAlpha(vectorValue);
                mToolbarSubTitle.setAlpha(vectorValue);
            }
        } else
        {
            mPlaceImageView.setTranslationY(-toolbarHeight * 0.5f);
            mToolbar.setBackgroundColor(0x4d000000);

            mPlaceNameTextView.setAlpha(0.0f);
            mPeriodTextView.setAlpha(0.0f);

            mToolbarTitle.setAlpha(1.0f);
            mToolbarSubTitle.setAlpha(1.0f);
        }

        // 배경 없어지는 애니메이션
        int scrollTopY = mScrollLayout.getPaddingTop() - ScreenUtils.dpToPx(mContext, 15);

        if (scrollTopY >= scrollY)
        {
            float vectorValue = (float) scrollY / scrollTopY;

            mImageDimView.setAlpha(vectorValue);
        } else
        {
            mImageDimView.setAlpha(1.0f);
        }

        mHandler.sendEmptyMessageDelayed(REQUEST_START_ANIMATION, 300);
    }

    /**
     * 카드가 화면에 보이는 경우에 애니메이션을 시작한다.
     */
    void animationInVisible()
    {
        int scrollY = mNestedScrollView.getScrollY();
        int height = scrollY + mNestedScrollView.getHeight();

        int count = mScrollLayout.getChildCount();

        for (int i = 0; i < count; i++)
        {
            View view = mScrollLayout.getChildAt(i);

            if (view instanceof ReviewScoreCardLayout)
            {
                if (scrollY < view.getTop() && height > view.getBottom())
                {
                    if (((ReviewScoreCardLayout) view).isStartedAnimation() == true)
                    {
                        ((ReviewScoreCardLayout) view).resumeEmoticonAnimation();
                    } else
                    {
                        ((ReviewScoreCardLayout) view).startEmoticonAnimation();
                    }
                }
            }
        }
    }
}
