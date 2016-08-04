package com.twoheart.dailyhotel.screen.search.stay.result;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.SaleTime;
import com.twoheart.dailyhotel.place.adapter.PlaceListFragmentPagerAdapter;
import com.twoheart.dailyhotel.place.base.OnBaseEventListener;
import com.twoheart.dailyhotel.place.fragment.PlaceListFragment;
import com.twoheart.dailyhotel.place.layout.PlaceSearchResultLayout;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;

import java.util.Collections;
import java.util.Map;

public class StaySearchResultLayout extends PlaceSearchResultLayout
{
    private StaySearchResultListFragmentPagerAdapter mStaySearchResultListFragmentPagerAdapter;

    public StaySearchResultLayout(Context context, OnBaseEventListener listener)
    {
        super(context, listener);
    }

    protected void setCalendarText(SaleTime checkInSaleTime, SaleTime checkOutSaleTime)
    {
        String checkInDay = checkInSaleTime.getDayOfDaysDateFormat("yyyy.MM.dd(EEE)");
        String checkOutDay = checkOutSaleTime.getDayOfDaysDateFormat("yyyy.MM.dd(EEE)");
        int nights = checkOutSaleTime.getOffsetDailyDay() - checkInSaleTime.getOffsetDailyDay();

        setCalendarText(String.format("%s - %s, %d박", checkInDay, checkOutDay, nights));
    }

    @Override
    protected int getEmptyIconResourceId()
    {
        return R.drawable.no_hotel_ic;
    }

    @Override
    protected synchronized PlaceListFragmentPagerAdapter getPlaceListFragmentPagerAdapter(FragmentManager fragmentManager, int count, View bottomOptionLayout, PlaceListFragment.OnPlaceListFragmentListener listener)
    {
        if (mStaySearchResultListFragmentPagerAdapter == null)
        {
            mStaySearchResultListFragmentPagerAdapter = new StaySearchResultListFragmentPagerAdapter(fragmentManager, count, bottomOptionLayout, listener);
        }

        return mStaySearchResultListFragmentPagerAdapter;
    }

    @Override
    protected void onAnalyticsCategoryFlicking(String category)
    {
        Map<String, String> params = Collections.singletonMap(AnalyticsManager.KeyType.SCREEN, AnalyticsManager.Screen.SEARCH_RESULT);
        AnalyticsManager.getInstance(mContext).recordEvent(AnalyticsManager.Category.NAVIGATION//
            , AnalyticsManager.Action.DAILY_HOTEL_CATEGORY_FLICKING, category, params);
    }

    @Override
    protected void onAnalyticsCategoryClick(String category)
    {
        Map<String, String> params = Collections.singletonMap(AnalyticsManager.KeyType.SCREEN, AnalyticsManager.Screen.SEARCH_RESULT);
        AnalyticsManager.getInstance(mContext).recordEvent(AnalyticsManager.Category.NAVIGATION//
            , AnalyticsManager.Action.HOTEL_CATEGORY_CLICKED, category, params);
    }
}
