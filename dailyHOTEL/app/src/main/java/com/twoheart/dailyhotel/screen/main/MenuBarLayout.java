package com.twoheart.dailyhotel.screen.main;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.place.base.BaseActivity;

public class MenuBarLayout implements View.OnClickListener
{
    private static final int MENU_HOME_INDEX = 0;
    private static final int MENU_BOOKING_INDEX = 1;
    private static final int MENU_MYDAILY_INDEX = 2;
    private static final int MENU_INFORMATION_INDEX = 3;

    private static final int MENU_COUNT = 4;

    private View[] mMenuView;
    private int mSelectedMenuIndex = -1;
    private OnMenuBarSelectedListener mOnMenuBarSelectedListener;
    private BaseActivity mBaseActivity;
    private ViewGroup mViewGroup;
    private boolean mEnabled;
    ValueAnimator mValueAnimator;

    public static class MenuBarLayoutOnPageChangeListener
    {
        private MenuBarLayout mMenuBarLayout;

        public MenuBarLayoutOnPageChangeListener(MenuBarLayout menuBarLayout)
        {
            mMenuBarLayout = menuBarLayout;
        }

        public void onPageChangeListener(boolean isCallMenuBar, int index)
        {
            if (mMenuBarLayout == null)
            {
                return;
            }

            if (isCallMenuBar == false)
            {
                mMenuBarLayout.selectedMenu(index);
            }
        }
    }

    public interface OnMenuBarSelectedListener
    {
        void onMenuSelected(boolean isCallMenuBar, int index, int previousIndex);

        void onMenuUnselected(boolean isCallMenuBar, int index);

        void onMenuReselected(boolean isCallMenuBar, int index);
    }

    public MenuBarLayout(BaseActivity baseActivity, ViewGroup viewGroup, OnMenuBarSelectedListener listener)
    {
        mBaseActivity = baseActivity;
        mOnMenuBarSelectedListener = listener;
        mEnabled = true;
        mViewGroup = viewGroup;
        initLayout(viewGroup);
    }

    private void initLayout(ViewGroup viewGroup)
    {
        mMenuView = new View[MENU_COUNT];

        mMenuView[MENU_HOME_INDEX] = viewGroup.findViewById(R.id.homeLayout);
        mMenuView[MENU_HOME_INDEX].setOnClickListener(this);

        mMenuView[MENU_BOOKING_INDEX] = viewGroup.findViewById(R.id.bookingLayout);
        mMenuView[MENU_BOOKING_INDEX].setOnClickListener(this);

        mMenuView[MENU_MYDAILY_INDEX] = viewGroup.findViewById(R.id.myDailyLayout);
        mMenuView[MENU_MYDAILY_INDEX].setOnClickListener(this);

        mMenuView[MENU_INFORMATION_INDEX] = viewGroup.findViewById(R.id.informationLayout);
        mMenuView[MENU_INFORMATION_INDEX].setOnClickListener(this);

        selectedMenu(MENU_HOME_INDEX);
    }

    @Override
    public void onClick(View v)
    {
        if (mBaseActivity.isLockUiComponent() == true || mEnabled == false)
        {
            return;
        }

        switch (v.getId())
        {
            case R.id.homeLayout:
                selectedMenu(MENU_HOME_INDEX);
                break;

            case R.id.bookingLayout:
                selectedMenu(MENU_BOOKING_INDEX);
                break;

            case R.id.myDailyLayout:
                selectedMenu(MENU_MYDAILY_INDEX);
                break;

            case R.id.informationLayout:
                selectedMenu(MENU_INFORMATION_INDEX);
                break;
        }
    }

    void selectedMenu(int index)
    {
        if (mSelectedMenuIndex == index)
        {
            if (mOnMenuBarSelectedListener != null)
            {
                mOnMenuBarSelectedListener.onMenuReselected(true, index);
            }
        } else
        {
            if (mSelectedMenuIndex >= 0)
            {
                mMenuView[mSelectedMenuIndex].setSelected(false);

                if (mOnMenuBarSelectedListener != null)
                {
                    mOnMenuBarSelectedListener.onMenuUnselected(true, mSelectedMenuIndex);
                }
            }

            mMenuView[index].setSelected(true);

            if (mOnMenuBarSelectedListener != null && mSelectedMenuIndex >= 0)
            {
                mOnMenuBarSelectedListener.onMenuSelected(true, index, mSelectedMenuIndex);
            }

            mSelectedMenuIndex = index;
        }
    }

    public void setEnabled(boolean enabled)
    {
        mEnabled = enabled;
    }

    public void setVisibility(int visibility)
    {
        mViewGroup.setVisibility(visibility);
    }

    public boolean isVisibility()
    {
        return mViewGroup.getVisibility() == View.VISIBLE;
    }

    void setTranslationY(float translationY)
    {
        if (mViewGroup == null)
        {
            return;
        }

        mViewGroup.setTranslationY(translationY);
    }

    private float getTranslationY()
    {
        return mViewGroup.getTranslationY();
    }

    private int getHeight()
    {
        return mViewGroup.getHeight();
    }

    public void setMyDailyNewIconVisible(boolean isVisible)
    {
        mMenuView[MENU_MYDAILY_INDEX].findViewById(R.id.myDailyNewIconView).setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    public void setInformationNewIconVisible(boolean isVisible)
    {
        mMenuView[MENU_INFORMATION_INDEX].findViewById(R.id.informationNewIconView).setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    public String getName(int position)
    {
        switch (position)
        {
            case MENU_HOME_INDEX:
                return mBaseActivity.getString(R.string.menu_item_title_home);

            case MENU_BOOKING_INDEX:
                return mBaseActivity.getString(R.string.menu_item_title_bookings);

            case MENU_MYDAILY_INDEX:
                return mBaseActivity.getString(R.string.menu_item_title_mydaily);

            case MENU_INFORMATION_INDEX:
                return mBaseActivity.getString(R.string.menu_item_title_information);

            default:
                return null;
        }
    }

    public void showMenuBar()
    {
        if (getTranslationY() == 0)
        {
            return;
        }

        setTranslationY(0);
    }

    public void showMenuBarAnimation(boolean force)
    {
        if (mValueAnimator != null && mValueAnimator.isRunning() == true)
        {
            if (force == true)
            {
                mValueAnimator.cancel();
                mValueAnimator = null;
            } else
            {
                return;
            }
        }

        if (force == false && isVisibility() == true)
        {
            return;
        }

        mValueAnimator = ValueAnimator.ofFloat(getTranslationY(), 0.0f);
        mValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mValueAnimator.setDuration(300);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                if (animation == null)
                {
                    return;
                }

                float value = (float) animation.getAnimatedValue();

                setTranslationY(value);
            }
        });

        mValueAnimator.addListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
                setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                if (mValueAnimator != null)
                {
                    mValueAnimator.removeAllUpdateListeners();
                    mValueAnimator.removeAllListeners();
                    mValueAnimator = null;
                }
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

        mValueAnimator.start();
    }

    public void hideMenuBarAnimation()
    {
        if (mValueAnimator != null && mValueAnimator.isRunning() == true)
        {
            return;
        }

        if (isVisibility() == false)
        {
            return;
        }

        mValueAnimator = ValueAnimator.ofInt(0, mViewGroup.getHeight());
        mValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mValueAnimator.setDuration(300);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                if (animation == null)
                {
                    return;
                }

                int value = (int) animation.getAnimatedValue();

                setTranslationY(value);
            }
        });

        mValueAnimator.addListener(new Animator.AnimatorListener()
        {
            boolean isCanceled;

            @Override
            public void onAnimationStart(Animator animation)
            {
                isCanceled = false;
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                if (mValueAnimator != null)
                {
                    mValueAnimator.removeAllUpdateListeners();
                    mValueAnimator.removeAllListeners();
                    mValueAnimator = null;
                }

                if (isCanceled == false)
                {
                    setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation)
            {
                isCanceled = true;
            }

            @Override
            public void onAnimationRepeat(Animator animation)
            {

            }
        });

        mValueAnimator.start();
    }
}
