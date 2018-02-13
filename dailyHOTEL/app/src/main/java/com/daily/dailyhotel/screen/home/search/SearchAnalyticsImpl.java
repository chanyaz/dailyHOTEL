package com.daily.dailyhotel.screen.home.search;

import android.app.Activity;

import com.daily.dailyhotel.entity.GourmetSuggest;
import com.daily.dailyhotel.entity.StayOutboundSuggest;
import com.daily.dailyhotel.entity.StaySuggest;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;

public class SearchAnalyticsImpl implements SearchInterface.AnalyticsInterface
{
    @Override
    public void onScreen(Activity activity)
    {
        if (activity == null)
        {
            return;
        }

        AnalyticsManager.getInstance(activity).recordScreen(activity, AnalyticsManager.Screen.SEARCH_MAIN, null);
    }

    @Override
    public void onEventStayClick(Activity activity)
    {
        if (activity == null)
        {
            return;
        }

        AnalyticsManager.getInstance(activity).recordEvent(AnalyticsManager.Category.SEARCH_//
            , AnalyticsManager.Action.SEARCH_SCREEN, "SwitchingHotel", null);
    }

    @Override
    public void onEventStayOutboundClick(Activity activity)
    {
        if (activity == null)
        {
            return;
        }

        AnalyticsManager.getInstance(activity).recordEvent(AnalyticsManager.Category.SEARCH_//
            , AnalyticsManager.Action.SEARCH_SCREEN, "SwitchingOB", null);
    }

    @Override
    public void onEventGourmetClick(Activity activity)
    {
        if (activity == null)
        {
            return;
        }

        AnalyticsManager.getInstance(activity).recordEvent(AnalyticsManager.Category.SEARCH_//
            , AnalyticsManager.Action.SEARCH_SCREEN, "SwitchingGourmet", null);
    }

    @Override
    public void onEventStayDoSearch(Activity activity, StaySuggest suggest)
    {
        if (activity == null)
        {
            return;
        }

        String action = "searching_stay_";

        switch (suggest.menuType)
        {
            case StaySuggest.MENU_TYPE_DIRECT:
                action += "direct";
                break;

            case StaySuggest.MENU_TYPE_LOCATION:
                action += "around";
                break;

            case StaySuggest.MENU_TYPE_RECENTLY_SEARCH:
                action += "recent";
                break;

            case StaySuggest.MENU_TYPE_RECENTLY_STAY:
                action += "recent_checked";
                break;

            case StaySuggest.MENU_TYPE_SUGGEST:
                action += "auto";
                break;
        }

        AnalyticsManager.getInstance(activity).recordEvent(AnalyticsManager.Category.SEARCH_//
            , action, suggest.displayName, null);
    }

    @Override
    public void onEventStayCalendarClick(Activity activity)
    {
        if (activity == null)
        {
            return;
        }

        AnalyticsManager.getInstance(activity).recordEvent(AnalyticsManager.Category.NAVIGATION_//
            , AnalyticsManager.Action.HOTEL_BOOKING_CALENDAR_CLICKED, "search", null);
    }

    @Override
    public void onEventStayOutboundDoSearch(Activity activity, StayOutboundSuggest suggest)
    {
        if (activity == null)
        {
            return;
        }

        String action = "searching_ob_";

        switch (suggest.menuType)
        {
            case StaySuggest.MENU_TYPE_DIRECT:
                action += "direct";
                break;

            case StaySuggest.MENU_TYPE_LOCATION:
                action += "around";
                break;

            case StaySuggest.MENU_TYPE_RECENTLY_SEARCH:
                action += "recent";
                break;

            case StaySuggest.MENU_TYPE_RECENTLY_STAY:
                action += "recent_checked";
                break;

            case StaySuggest.MENU_TYPE_SUGGEST:
                action += "auto";
                break;
        }

        AnalyticsManager.getInstance(activity).recordEvent(AnalyticsManager.Category.SEARCH_//
            , action, suggest.display, null);
    }

    @Override
    public void onEventStayOutboundPeopleClick(Activity activity)
    {
        if (activity == null)
        {
            return;
        }

        AnalyticsManager.getInstance(activity).recordEvent(AnalyticsManager.Category.NAVIGATION//
            , AnalyticsManager.Action.MEMBER_SELECT, null, null);
    }

    @Override
    public void onEventGourmetDoSearch(Activity activity, GourmetSuggest suggest)
    {
        if (activity == null)
        {
            return;
        }

        String action = "searching_gourmet_";

        switch (suggest.menuType)
        {
            case StaySuggest.MENU_TYPE_DIRECT:
                action += "direct";
                break;

            case StaySuggest.MENU_TYPE_LOCATION:
                action += "around";
                break;

            case StaySuggest.MENU_TYPE_RECENTLY_SEARCH:
                action += "recent";
                break;

            case StaySuggest.MENU_TYPE_RECENTLY_STAY:
                action += "recent_checked";
                break;

            case StaySuggest.MENU_TYPE_SUGGEST:
                action += "auto";
                break;
        }

        AnalyticsManager.getInstance(activity).recordEvent(AnalyticsManager.Category.SEARCH_//
            , action, suggest.displayName, null);
    }
}
