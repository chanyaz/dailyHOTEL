package com.twoheart.dailyhotel.place.layout;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ExLog;
import com.daily.base.util.ScreenUtils;
import com.daily.base.widget.DailyTextView;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.PlaceDetail;
import com.twoheart.dailyhotel.network.model.ImageInformation;
import com.twoheart.dailyhotel.place.adapter.PlaceDetailImageViewPagerAdapter;
import com.twoheart.dailyhotel.place.base.BaseLayout;
import com.twoheart.dailyhotel.place.base.OnBaseEventListener;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.widget.DailyLineIndicator;
import com.twoheart.dailyhotel.widget.DailyLoopViewPager;
import com.twoheart.dailyhotel.widget.DailyPlaceDetailScrollView;

import java.util.ArrayList;

public abstract class PlaceDetailLayout extends BaseLayout
{
    public static final int STATUS_NONE = 0;
    public static final int STATUS_SELECT_PRODUCT = 1;
    public static final int STATUS_BOOKING = 2;
    public static final int STATUS_SOLD_OUT = 3;

    private static final int BOOKING_TEXT_VIEW_DURATION = 150;

    protected PlaceDetail mPlaceDetail;
    protected DailyLoopViewPager mViewPager;
    protected DailyLineIndicator mDailyLineIndicator;
    protected View mMoreIconView;

    protected DailyPlaceDetailScrollView mScrollView;

    protected PlaceDetailImageViewPagerAdapter mImageAdapter;
    protected TextView mBookingTextView;
    protected TextView mSoldoutTextView;
    protected TextView mDescriptionTextView;
    protected View mPriceOptionLayout;
    protected RadioGroup mPriceRadioGroup;

    protected int mBookingStatus; // 예약 진행 상태로 객실 찾기, 없음, 예약 진행

    protected View mBottomLayout;
    AnimatorSet mWishPopupAnimatorSet;
    int mStatusBarHeight;

    protected com.facebook.drawee.view.SimpleDraweeView mTransSimpleDraweeView;
    protected TextView mTransTotalGradeTextView, mTransPlaceNameTextView;
    protected View mTransTitleLayout, mTransGradientBottomView, mTransGradientTopView;

    protected DailyTextView mWishButtonTextView;
    protected DailyTextView mWishPopupTextView;
    protected View mWishPopupScrollView;
    protected View mTrueVRTooltipView;

    protected ValueAnimator mBookingTextViewAnimator;

    public enum WishPopupState
    {
        ADD,
        DELETE,
        GONE
    }

    public interface OnEventListener extends OnBaseEventListener
    {
        void showActionBar(boolean isAnimation);

        void hideActionBar(boolean isAnimation);

        void onClickImage(PlaceDetail placeDetail);

        void onSelectedImagePosition(int position);

        void onConciergeClick();

        void showMap();

        void finish();

        void clipAddress(String address);

        void showNavigatorDialog();

        void onCalendarClick();

        void doBooking();

        void onDownloadCouponClick();

        void onWishClick();

        void releaseUiComponent();

        void onTrueVRTooltipClick();
    }

    protected abstract String getProductTypeTitle();

    protected abstract View getTitleLayout();

    public abstract void setTrueReviewCount(int count);

    public abstract void setBookingStatus(int status);

    public static int getImageLayoutHeight(Context context)
    {
        return ScreenUtils.getRatioHeightType4x3(ScreenUtils.getScreenWidth(context));
    }

    public PlaceDetailLayout(Context context, OnBaseEventListener listener)
    {
        super(context, listener);
    }

    @Override
    protected void initLayout(View view)
    {
        mTransSimpleDraweeView = (com.facebook.drawee.view.SimpleDraweeView) view.findViewById(R.id.transImageView);
        mTransGradientBottomView = view.findViewById(R.id.transGradientView);
        mTransGradientTopView = view.findViewById(R.id.transGradientTopView);

        mTransTitleLayout = view.findViewById(R.id.transTitleLayout);
        mTransTotalGradeTextView = (TextView) mTransTitleLayout.findViewById(R.id.transGradeTextView);
        mTransPlaceNameTextView = (TextView) mTransTitleLayout.findViewById(R.id.transNameTextView);

        mScrollView = (DailyPlaceDetailScrollView) view.findViewById(R.id.placeScrollView);
        mScrollView.setOnScrollChangedListener(mOnScrollChangedListener);

        // 이미지 ViewPage 넣기.
        mDailyLineIndicator = (DailyLineIndicator) view.findViewById(R.id.viewpagerIndicator);

        mTrueVRTooltipView = view.findViewById(R.id.trueVRTooltipView);
        mTrueVRTooltipView.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mTrueVRTooltipView.getVisibility() == View.VISIBLE)
                {
                    ((OnEventListener) mOnEventListener).onTrueVRTooltipClick();
                }
            }
        });

        mViewPager = (DailyLoopViewPager) view.findViewById(R.id.defaultLoopViewPager);

        mImageAdapter = new PlaceDetailImageViewPagerAdapter(mContext);
        mViewPager.setAdapter(mImageAdapter);

        mDescriptionTextView = (TextView) view.findViewById(R.id.descriptionTextView);
        mDailyLineIndicator.setViewPager(mViewPager);

        mViewPager.setOnPageChangeListener(mOnPageChangeListener);
        mDailyLineIndicator.setOnPageChangeListener(mOnPageChangeListener);

        ViewGroup.LayoutParams layoutParams = mViewPager.getLayoutParams();
        layoutParams.height = getImageLayoutHeight(mContext);
        mViewPager.setLayoutParams(layoutParams);

        mMoreIconView = view.findViewById(R.id.moreIconView);
        mBottomLayout = view.findViewById(R.id.bottomLayout);

        mBookingTextView = (TextView) mBottomLayout.findViewById(R.id.bookingTextView);
        mSoldoutTextView = (TextView) mBottomLayout.findViewById(R.id.soldoutTextView);

        mWishPopupScrollView = view.findViewById(R.id.wishListPopupScrollView);
        mWishPopupTextView = (DailyTextView) view.findViewById(R.id.wishListPopupView);
        mWishButtonTextView = (DailyTextView) view.findViewById(R.id.wishListButtonView);
        mWishButtonTextView.setTag(false);
        mWishButtonTextView.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((OnEventListener) mOnEventListener).onWishClick();
            }
        });

        setBookingStatus(STATUS_NONE);
        showWishButton();
        setUpdateWishPopup(WishPopupState.GONE);
    }

    public void setIsUsedMultiTransitions(boolean enabled)
    {
        if (enabled == true)
        {
            setTransVisibility(View.VISIBLE);
            mTransTitleLayout.setVisibility(View.VISIBLE);

            mTransSimpleDraweeView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getImageLayoutHeight(mContext)));
            mTransSimpleDraweeView.setTransitionName(mContext.getString(R.string.transition_place_image));

            mTransGradientBottomView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getImageLayoutHeight(mContext)));
            mTransGradientBottomView.setTransitionName(mContext.getString(R.string.transition_gradient_bottom_view));
            mTransGradientBottomView.setBackground(makeShaderFactory());

            mTransGradientTopView.setTransitionName(mContext.getString(R.string.transition_gradient_top_view));
        } else
        {
            setTransVisibility(View.GONE);
            mTransTitleLayout.setVisibility(View.GONE);
        }
    }

    private PaintDrawable makeShaderFactory()
    {
        // 그라디에이션 만들기.
        final int colors[] = {Color.parseColor("#ED000000"), Color.parseColor("#E8000000"), Color.parseColor("#E2000000"), Color.parseColor("#66000000"), Color.parseColor("#00000000")};
        final float positions[] = {0.0f, 0.01f, 0.02f, 0.17f, 0.60f};

        PaintDrawable paintDrawable = new PaintDrawable();
        paintDrawable.setShape(new RectShape());

        ShapeDrawable.ShaderFactory sf = new ShapeDrawable.ShaderFactory()
        {
            @Override
            public Shader resize(int width, int height)
            {
                return new LinearGradient(0, height, 0, 0, colors, positions, Shader.TileMode.CLAMP);
            }
        };

        paintDrawable.setShaderFactory(sf);

        return paintDrawable;
    }

    public void setScrollViewTop()
    {
        if (mScrollView == null)
        {
            return;
        }

        mScrollView.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                mScrollView.fullScroll(View.FOCUS_UP);
            }
        }, 50);
    }

    public boolean isScrollViewTop()
    {
        if (mScrollView == null)
        {
            return true;
        }

        return mScrollView.getScrollY() == 0; // 임시
    }

    public void setTransVisibility(int visibility)
    {
        mTransSimpleDraweeView.setVisibility(visibility);
        mTransGradientBottomView.setVisibility(visibility);
        mTransGradientTopView.setVisibility(visibility);
    }

    public void setTransBottomGradientBackground(int resId)
    {
        mTransGradientBottomView.setBackgroundResource(resId);
    }

    public void setTransImageView(String url)
    {
        if (mTransSimpleDraweeView.getVisibility() == View.VISIBLE)
        {
            Util.requestImageResize(mContext, mTransSimpleDraweeView, url);
        }
    }

    public void setDefaultImage(String url)
    {
        if (DailyTextUtils.isTextEmpty(url) == true)
        {
            setLineIndicatorVisible(false);
            return;
        }

        setLineIndicatorVisible(true);

        if (mImageAdapter == null)
        {
            mImageAdapter = new PlaceDetailImageViewPagerAdapter(mContext);
        }

        ArrayList<ImageInformation> arrayList = new ArrayList<>();
        ImageInformation imageInformation = new ImageInformation();
        imageInformation.setImageUrl(url);
        arrayList.add(imageInformation);

        mImageAdapter.setData(arrayList);
        mViewPager.setAdapter(mImageAdapter);

        mDailyLineIndicator.setViewPager(mViewPager);
    }

    public void setStatusBarHeight(Activity activity)
    {
        final Window window = activity.getWindow();

        mScrollView.post(new Runnable()
        {
            @Override
            public void run()
            {
                Rect rect = new Rect();
                window.getDecorView().getWindowVisibleDisplayFrame(rect);

                mStatusBarHeight = rect.top;
            }
        });
    }

    public int getStatusBarHeight()
    {
        return mStatusBarHeight;
    }

    public void setTrueVRTooltipVisibility(boolean visibility)
    {
        if (mTrueVRTooltipView == null)
        {
            return;
        }

        if (visibility == true)
        {
            mTrueVRTooltipView.setVisibility(View.VISIBLE);
        } else
        {
            mTrueVRTooltipView.setVisibility(View.GONE);
        }
    }

    public void hideAnimationTooltip()
    {
        if (mTrueVRTooltipView == null || mTrueVRTooltipView.getTag() != null)
        {
            return;
        }

        final ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mTrueVRTooltipView, "alpha", 1.0f, 0.0f);

        mTrueVRTooltipView.setTag(objectAnimator);

        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.setDuration(300);
        objectAnimator.addListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animator)
            {

            }

            @Override
            public void onAnimationEnd(Animator animator)
            {
                objectAnimator.removeAllListeners();
                objectAnimator.removeAllUpdateListeners();

                mTrueVRTooltipView.setTag(null);
                setTrueVRTooltipVisibility(false);
            }

            @Override
            public void onAnimationCancel(Animator animator)
            {

            }

            @Override
            public void onAnimationRepeat(Animator animator)
            {

            }
        });

        objectAnimator.start();
    }

    public boolean isTrueVRTooltipVisibility()
    {
        if (mTrueVRTooltipView == null)
        {
            return false;
        }

        return mTrueVRTooltipView.getVisibility() == View.VISIBLE;
    }

    public int getBookingStatus()
    {
        return mBookingStatus;
    }

    public int getCurrentImage()
    {
        if (mViewPager != null)
        {
            return mViewPager.getCurrentItem();
        }

        return 0;
    }

    public void setCurrentImage(int position)
    {
        if (mImageAdapter == null || mViewPager == null)
        {
            return;
        }

        mViewPager.setCurrentItem(position, true);
    }

    public int getTotalImage()
    {
        if (mImageAdapter != null)
        {
            return mImageAdapter.getCount();
        }

        return 0;
    }


    public void setImageInformation(String description)
    {
        if (DailyTextUtils.isTextEmpty(description) == false)
        {
            mDescriptionTextView.setVisibility(View.VISIBLE);
            mDescriptionTextView.setText(description);
        } else
        {
            mDescriptionTextView.setVisibility(View.INVISIBLE);
        }
    }

    public void setLineIndicatorVisible(boolean isShow)
    {
        mMoreIconView.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
        mDailyLineIndicator.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
    }

    public void setWishButtonSelected(boolean isSelected)
    {
        int imageResId = isSelected == true ? R.drawable.ic_heart_fill_s : R.drawable.ic_heart_stroke_s;
        mWishButtonTextView.setCompoundDrawablesWithIntrinsicBounds(0, imageResId, 0, 0);
        mWishButtonTextView.setTag(isSelected);
    }

    public void setWishButtonCount(int count)
    {
        String buttonText;
        if (count <= 0)
        {
            buttonText = mContext.getResources().getString(R.string.label_wishlist);
        } else if (count > 9999)
        {
            buttonText = mContext.getResources().getString(R.string.wishlist_count_over_10_thousand);
        } else
        {
            buttonText = mContext.getResources().getString(R.string.wishlist_count_format, count);
        }

        mWishButtonTextView.setText(buttonText);
    }

    public void setUpdateWishPopup(final WishPopupState state)
    {
        if (WishPopupState.GONE == state)
        {
            //            mWishPopupTextView.setVisibility(View.GONE);
            mWishPopupScrollView.setVisibility(View.GONE);

            if (mWishPopupAnimatorSet != null && mWishPopupAnimatorSet.isRunning() == true)
            {
                mWishPopupAnimatorSet.cancel();
            }
        } else
        {
            if (mWishPopupAnimatorSet != null && mWishPopupAnimatorSet.isRunning() == true)
            {
                ExLog.d("WishPopup is Already running");
                return;
            }

            if (WishPopupState.ADD == state)
            {
                mWishPopupTextView.setText(R.string.wishlist_detail_add_message);
                mWishPopupTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_heart_fill_l, 0, 0);
                mWishPopupTextView.setBackgroundResource(R.drawable.shape_filloval_ccdb2453);
            } else
            {
                mWishPopupTextView.setText(R.string.wishlist_detail_delete_message);
                mWishPopupTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_heart_stroke_l, 0, 0);
                mWishPopupTextView.setBackgroundResource(R.drawable.shape_filloval_75000000);
            }

            ObjectAnimator objectAnimator1 = ObjectAnimator.ofPropertyValuesHolder(mWishPopupTextView //
                , PropertyValuesHolder.ofFloat("scaleX", 0.8f, 1.2f, 1.0f) //
                , PropertyValuesHolder.ofFloat("scaleY", 0.8f, 1.2f, 1.0f) //
                , PropertyValuesHolder.ofFloat("alpha", 0.5f, 1.0f, 1.0f) //
            );
            objectAnimator1.setInterpolator(new AccelerateInterpolator());
            objectAnimator1.setDuration(300);


            ObjectAnimator objectAnimator2 = ObjectAnimator.ofPropertyValuesHolder(mWishPopupTextView //
                , PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.0f) //
                , PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.0f) //
                , PropertyValuesHolder.ofFloat("alpha", 1.0f, 1.0f) //
            );
            objectAnimator2.setDuration(600);


            ObjectAnimator objectAnimator3 = ObjectAnimator.ofPropertyValuesHolder(mWishPopupTextView //
                , PropertyValuesHolder.ofFloat("scaleX", 1.0f, 0.7f) //
                , PropertyValuesHolder.ofFloat("scaleY", 1.0f, 0.7f) //
                , PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.0f) //
            );
            objectAnimator3.setDuration(200);

            mWishPopupAnimatorSet = new AnimatorSet();
            mWishPopupAnimatorSet.playSequentially(objectAnimator1, objectAnimator2, objectAnimator3);
            mWishPopupAnimatorSet.addListener(new Animator.AnimatorListener()
            {
                @Override
                public void onAnimationStart(Animator animation)
                {
                    mWishPopupScrollView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation)
                {
                    mWishPopupAnimatorSet.removeAllListeners();
                    mWishPopupAnimatorSet = null;

                    mWishPopupScrollView.setVisibility(View.INVISIBLE);
                    ((OnEventListener) mOnEventListener).releaseUiComponent();
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

            mWishPopupAnimatorSet.start();
        }
    }

    public void showWishButton()
    {
        if (mBookingTextViewAnimator != null && mBookingTextViewAnimator.isRunning() == true)
        {
            mBookingTextViewAnimator.cancel();
        }

        if (mBookingTextView == null)
        {
            return;
        }

        final int end = mContext.getResources().getDimensionPixelOffset(R.dimen.detail_button_max_left_margin);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mBookingTextView.getLayoutParams();
        params.leftMargin = end;
        mBookingTextView.setLayoutParams(params);
    }

    public void hideWishButton()
    {
        if (mBookingTextViewAnimator != null && mBookingTextViewAnimator.isRunning() == true)
        {
            mBookingTextViewAnimator.cancel();
        }

        final int end = mContext.getResources().getDimensionPixelOffset(R.dimen.detail_button_min_left_margin);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mBookingTextView.getLayoutParams();
        params.leftMargin = end;
        mBookingTextView.setLayoutParams(params);
    }

    public void showWishButtonAnimation()
    {
        final int start = mContext.getResources().getDimensionPixelOffset(R.dimen.detail_button_min_left_margin);
        final int end = mContext.getResources().getDimensionPixelOffset(R.dimen.detail_button_max_left_margin);

        startWishButtonAnimation(start, end);
    }

    public void hideWishButtonAnimation()
    {
        final int start = mContext.getResources().getDimensionPixelOffset(R.dimen.detail_button_max_left_margin);
        final int end = mContext.getResources().getDimensionPixelOffset(R.dimen.detail_button_min_left_margin);

        startWishButtonAnimation(start, end);
    }

    private void startWishButtonAnimation(final int start, final int end)
    {
        if (mBookingTextViewAnimator != null && mBookingTextViewAnimator.isRunning() == true)
        {
            mBookingTextViewAnimator.cancel();
        }

        mBookingTextViewAnimator = ValueAnimator.ofInt(start, end);
        mBookingTextViewAnimator.setInterpolator(new LinearInterpolator());
        mBookingTextViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator)
            {
                int value = (int) valueAnimator.getAnimatedValue();
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mBookingTextView.getLayoutParams();
                params.leftMargin = value;
                mBookingTextView.setLayoutParams(params);
            }
        });

        mBookingTextViewAnimator.addListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mBookingTextView.getLayoutParams();
                params.leftMargin = start;
                mBookingTextView.setLayoutParams(params);
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                mBookingTextViewAnimator.removeAllListeners();
                mBookingTextViewAnimator.removeAllUpdateListeners();
                mBookingTextViewAnimator = null;

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mBookingTextView.getLayoutParams();
                params.leftMargin = end;
                mBookingTextView.setLayoutParams(params);
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

        mBookingTextViewAnimator.setDuration(BOOKING_TEXT_VIEW_DURATION);
        mBookingTextViewAnimator.start();
    }

    private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener()
    {
        private int mScrollState = -1;
        private int mScrollPosition = -1;

        @Override
        public void onPageSelected(int position)
        {
            ((OnEventListener) mOnEventListener).onSelectedImagePosition(position);

            ImageInformation imageInformation = (ImageInformation) mPlaceDetail.getImageList().get(position);
            setImageInformation(imageInformation.description);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
        {
            if (mScrollState == -1 || Float.compare(positionOffset, 0.0f) == 0 || positionOffsetPixels == 0)
            {
                return;
            }

            if (mScrollPosition == -1)
            {
                mScrollPosition = position;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state)
        {
            mScrollState = state;

            switch (state)
            {
                case ViewPager.SCROLL_STATE_IDLE:
                    mScrollPosition = -1;
                    break;

                case ViewPager.SCROLL_STATE_DRAGGING:
                    break;

                case ViewPager.SCROLL_STATE_SETTLING:
                    break;
            }
        }
    };

    private DailyPlaceDetailScrollView.OnScrollChangedListener mOnScrollChangedListener = new DailyPlaceDetailScrollView.OnScrollChangedListener()
    {
        @Override
        public void onScrollChanged(ScrollView scrollView, int l, int t, int oldl, int oldt)
        {
            if (getBookingStatus() == STATUS_BOOKING)
            {
                return;
            }

            final int TOOLBAR_HEIGHT = mContext.getResources().getDimensionPixelSize(R.dimen.toolbar_height);

            int viewpagerHeight = getImageLayoutHeight(mContext);

            if (t >= viewpagerHeight - TOOLBAR_HEIGHT)
            {
                ((OnEventListener) mOnEventListener).showActionBar(true);
            } else
            {
                ((OnEventListener) mOnEventListener).hideActionBar(true);
            }
        }
    };

    protected View.OnTouchListener mEmptyViewOnTouchListener = new View.OnTouchListener()
    {
        private int mMoveState;
        private float mPrevX, mPrevY;

        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            switch (event.getAction() & MotionEventCompat.ACTION_MASK)
            {
                case MotionEvent.ACTION_DOWN:
                {
                    mPrevX = event.getX();
                    mPrevY = event.getY();

                    mMoveState = 0;
                    mScrollView.setScrollEnabled(false);

                    try
                    {
                        mViewPager.onTouchEvent(event);
                    } catch (Exception e)
                    {
                    }
                    break;
                }

                case MotionEvent.ACTION_UP:
                {
                    int touchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();

                    int x = (int) (mPrevX - event.getX());
                    int y = (int) (mPrevY - event.getY());

                    int distance = (int) Math.sqrt(x * x + y * y);

                    if (distance < touchSlop)
                    {
                        ((OnEventListener) mOnEventListener).onClickImage(mPlaceDetail);

                        mMoveState = 0;

                        try
                        {
                            mViewPager.onTouchEvent(event);
                        } catch (Exception e)
                        {
                            event.setAction(MotionEvent.ACTION_CANCEL);
                            event.setLocation(mViewPager.getScrollX(), mViewPager.getScrollY());
                            mViewPager.onTouchEvent(event);
                        }

                        mScrollView.setScrollEnabled(true);
                        break;
                    }
                }
                case MotionEvent.ACTION_CANCEL:
                {
                    mMoveState = 0;

                    try
                    {
                        mViewPager.onTouchEvent(event);
                    } catch (Exception e)
                    {
                        event.setAction(MotionEvent.ACTION_CANCEL);
                        event.setLocation(mViewPager.getScrollX(), mViewPager.getScrollY());
                        mViewPager.onTouchEvent(event);
                    }

                    mScrollView.setScrollEnabled(true);
                    break;
                }

                case MotionEvent.ACTION_MOVE:
                {
                    float x = event.getX();
                    float y = event.getY();

                    if (mMoveState == 0)
                    {
                        if (Math.abs(x - mPrevX) == Math.abs(y - mPrevY))
                        {

                        } else if (Math.abs(x - mPrevX) > Math.abs(y - mPrevY))
                        {
                            // x 축으로 이동한 경우.
                            mMoveState = 100;

                            try
                            {
                                mViewPager.onTouchEvent(event);
                            } catch (Exception e)
                            {
                                event.setAction(MotionEvent.ACTION_CANCEL);
                                event.setLocation(mViewPager.getScrollX(), mViewPager.getScrollY());
                                mViewPager.onTouchEvent(event);
                            }
                        } else
                        {
                            // y축으로 이동한 경우.
                            mMoveState = 10;
                            mScrollView.setScrollEnabled(true);
                            return true;
                        }
                    } else if (mMoveState == 100)
                    {
                        try
                        {
                            mViewPager.onTouchEvent(event);
                        } catch (Exception e)
                        {
                            event.setAction(MotionEvent.ACTION_CANCEL);
                            event.setLocation(mViewPager.getScrollX(), mViewPager.getScrollY());
                            mViewPager.onTouchEvent(event);
                        }
                    }
                    break;
                }
            }

            return false;
        }
    };
}