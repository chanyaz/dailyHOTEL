package com.twoheart.dailyhotel.screen.hotel.list;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.twoheart.dailyhotel.model.SaleTime;
import com.twoheart.dailyhotel.place.adapter.PlaceListFragmentPagerAdapter;
import com.twoheart.dailyhotel.place.fragment.PlaceListFragment;
import com.twoheart.dailyhotel.place.layout.PlaceMainLayout;

public class StayMainLayout extends PlaceMainLayout
{

    public StayMainLayout(Context context, OnEventListener mOnEventListener)
    {
        super(context, mOnEventListener);
    }

    @Override
    public void onClick(View v)
    {
        super.onClick(v);
    }

    @Override
    protected PlaceListFragmentPagerAdapter getPlaceListFragmentPagerAdapter(FragmentManager fragmentManager, int count, View bottomOptionLayout, PlaceListFragment.OnPlaceListFragmentListener listener)
    {
        return new StayListFragmentPagerAdapter(fragmentManager, count, bottomOptionLayout, listener);
    }

    protected void setToolbarDateText(SaleTime checkInSaleTime, SaleTime checkOutSaleTime)
    {
        String checkInDay = checkInSaleTime.getDayOfDaysDateFormat("M.d");
        String checkOutDay = checkOutSaleTime.getDayOfDaysDateFormat("M.d");

        setToolbarDateText(String.format("%s-%s", checkInDay, checkOutDay));
    }
}
