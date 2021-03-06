package com.twoheart.dailyhotel.screen.mydaily.wishlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.time.PlaceBookingDay;
import com.twoheart.dailyhotel.network.model.TodayDateTime;
import com.twoheart.dailyhotel.place.base.BaseActivity;
import com.twoheart.dailyhotel.place.base.BaseFragment;

/**
 * Created by android_sam on 2016. 11. 1..
 */

public abstract class PlaceWishListFragment extends BaseFragment
{
    private int mWishListCount;

    protected BaseActivity mBaseActivity;
    protected PlaceWishListLayout mListLayout;

    protected PlaceBookingDay mPlaceBookingDay;

    protected View mViewByLongPress;
    protected int mPositionByLongPress;

    protected OnWishListFragmentListener mWishListFragmentListener;

    protected abstract void setPlaceBookingDay(TodayDateTime todayDateTime);

    protected abstract PlaceWishListLayout getListLayout();

    protected abstract void requestWishList();

    protected abstract void requestRemoveWishListItem(int placeIndex);

    protected abstract PlaceType getPlaceType();

    public interface OnWishListFragmentListener
    {
        void onRemoveItemClick(PlaceType placeType, int position);
    }

    public void setWishListFragmentListener(OnWishListFragmentListener listener)
    {
        mWishListFragmentListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mBaseActivity = (BaseActivity) getActivity();
        mListLayout = getListLayout();

        return mListLayout.onCreateView(R.layout.fragment_wishlist_list, container);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (mListLayout != null && mListLayout.getBlurVisibility() == true)
        {
            mListLayout.setBlurVisibility(mBaseActivity, false);
        } else
        {
            if (mWishListCount == 0)
            {
                unLockUI();

                if (isFinishing() == true)
                {
                    return;
                }

                if (mListLayout == null)
                {
                    return;
                }

                mListLayout.setData(null, false);
            } else
            {
                requestWishList();
            }
        }
    }

    public void forceRefreshList()
    {
        if (mListLayout == null)
        {
            return;
        }

        mListLayout.setData(null, false, false);
        onResume();
    }

    public void setWishListCount(int count)
    {
        mWishListCount = count;
    }

    public int getWishListCount()
    {
        return mWishListCount < 0 ? 0 : mWishListCount;
    }
}
