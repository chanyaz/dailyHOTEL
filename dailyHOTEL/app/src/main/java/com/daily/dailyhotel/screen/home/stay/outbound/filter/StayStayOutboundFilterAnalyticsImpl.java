package com.daily.dailyhotel.screen.home.stay.outbound.filter;

import android.app.Activity;

import com.daily.dailyhotel.entity.StayOutboundFilters;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;

public class StayStayOutboundFilterAnalyticsImpl implements StayOutboundFilterPresenter.StayOutboundFilterAnalyticsInterface
{
    @Override
    public void onSortClick(Activity activity, StayOutboundFilters.SortType sortType)
    {
        if (activity == null || sortType == null)
        {
            return;
        }

        String sort = null;

        switch (sortType)
        {
            case RECOMMENDATION:
                sort = "popular";
                break;

            case DISTANCE:
                sort = "distance";
                break;

            case LOW_PRICE:
                sort = "lowprice";
                break;

            case HIGH_PRICE:
                sort = "highprice";
                break;

            case SATISFACTION:
                sort = "rate";
                break;
        }

        AnalyticsManager.getInstance(activity).recordEvent(AnalyticsManager.Category.NAVIGATION//
            , AnalyticsManager.Action.SORT, sort, null);
    }

    @Override
    public void onRatingClick(Activity activity, int rating)
    {
        if (activity == null)
        {
            return;
        }

        AnalyticsManager.getInstance(activity).recordEvent(AnalyticsManager.Category.NAVIGATION//
            , AnalyticsManager.Action.FILTER, Integer.toString(rating), null);
    }
}
