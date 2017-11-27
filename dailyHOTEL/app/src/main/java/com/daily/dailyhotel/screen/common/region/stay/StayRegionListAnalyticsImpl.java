package com.daily.dailyhotel.screen.common.region.stay;

import android.app.Activity;

import com.daily.base.util.DailyTextUtils;
import com.daily.dailyhotel.entity.StayBookDateTime;
import com.twoheart.dailyhotel.DailyHotel;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.DailyCategoryType;
import com.twoheart.dailyhotel.util.DailyCalendar;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StayRegionListAnalyticsImpl implements StayRegionListPresenter.StayRegionListAnalyticsInterface
{
    @Override
    public void onScreen(Activity activity, String categoryCode)
    {
        if (activity == null)
        {
            return;
        }

        Map<String, String> params = new HashMap<>();

        if (DailyHotel.isLogin() == false)
        {
            params.put(AnalyticsManager.KeyType.IS_SIGNED, AnalyticsManager.ValueType.GUEST);
        } else
        {
            params.put(AnalyticsManager.KeyType.IS_SIGNED, AnalyticsManager.ValueType.MEMBER);
        }

        params.put(AnalyticsManager.KeyType.PLACE_TYPE, AnalyticsManager.ValueType.STAY);
        params.put(AnalyticsManager.KeyType.CATEGORY, categoryCode);

        AnalyticsManager.getInstance(activity).recordScreen(activity, AnalyticsManager.Screen.DAILYHOTEL_LIST_REGION_DOMESTIC, null, params);

    }

    @Override
    public void onEventSearchClick(Activity activity, DailyCategoryType dailyCategoryType)
    {
        if (activity == null || dailyCategoryType == null)
        {
            return;
        }

        String label;
        switch (dailyCategoryType)
        {
            case STAY_ALL:
                label = AnalyticsManager.Label.STAY_LOCATION_LIST;
                break;
            case STAY_HOTEL:
                label = AnalyticsManager.Label.HOTEL_LOCATION_LIST;
                break;
            case STAY_BOUTIQUE:
                label = AnalyticsManager.Label.BOUTIQUE_LOCATION_LIST;
                break;
            case STAY_PENSION:
                label = AnalyticsManager.Label.PENSION_LOCATION_LIST;
                break;
            case STAY_RESORT:
                label = AnalyticsManager.Label.RESORT_LOCATION_LIST;
                break;
            default:
                label = AnalyticsManager.ValueType.EMPTY;
                break;
        }

        AnalyticsManager.getInstance(activity).recordEvent(AnalyticsManager.Category.SEARCH//
            , AnalyticsManager.Action.SEARCH_BUTTON_CLICK, label, null);
    }

    @Override
    public void onEventChangedProvinceClick(Activity activity, String previousProvinceName, String previousAreaName, String changedProvinceName, String changedAreaName, StayBookDateTime stayBookDateTime)
    {
        if (activity == null)
        {
            return;
        }

        String previousLabel = getAnalyticsRegionLabel(activity.getString(R.string.label_domestic), previousProvinceName, previousAreaName);
        String changedLabel = getAnalyticsRegionLabel(activity.getString(R.string.label_domestic), changedProvinceName, changedAreaName);

        String checkInDate = stayBookDateTime.getCheckInDateTime("yyyy.MM.dd(EEE)");
        String checkOutDate = stayBookDateTime.getCheckOutDateTime("yyyy.MM.dd(EEE)");

        String label = previousLabel + "-" + changedLabel + "-" + checkInDate + "-" + checkOutDate + "-" + DailyCalendar.format(new Date(), "yyyy.MM.dd(EEE) HH시 mm분");

        AnalyticsManager.getInstance(activity).recordEvent(AnalyticsManager.Category.NAVIGATION_//
            , AnalyticsManager.Action.HOTEL_BOOKING_DATE_CHANGED, label, null);
    }

    @Override
    public void onEventChangedDateClick(Activity activity)
    {
        if (activity == null)
        {
            return;
        }

        AnalyticsManager.getInstance(activity).recordEvent(AnalyticsManager.Category.NAVIGATION_//
            , AnalyticsManager.Action.HOTEL_BOOKING_CALENDAR_CLICKED, AnalyticsManager.Label.CHANGE_LOCATION, null);
    }

    @Override
    public void onEventChangedRegionClick(Activity activity, String provinceName, String areaName)
    {
        if (activity == null)
        {
            return;
        }

        AnalyticsManager.getInstance(activity).recordEvent(AnalyticsManager.Category.NAVIGATION_//
            , AnalyticsManager.Action.HOTEL_LOCATIONS_CLICKED, getAnalyticsRegionLabel(activity.getString(R.string.label_domestic), provinceName, areaName), null);
    }

    @Override
    public void onEventClosedClick(Activity activity, String stayCategory)
    {
        if (activity == null)
        {
            return;
        }

        AnalyticsManager.getInstance(activity).recordEvent(AnalyticsManager.Category.NAVIGATION//
            , AnalyticsManager.Action.LOCATION_LIST_CLOSE, stayCategory, null);
    }

    @Override
    public void onEventAroundSearchClick(Activity activity, DailyCategoryType dailyCategoryType)
    {
        if (activity == null)
        {
            return;
        }

        String label;
        switch (dailyCategoryType)
        {
            case STAY_ALL:
                label = AnalyticsManager.Label.STAY_LOCATION_LIST;
                break;
            case STAY_HOTEL:
                label = AnalyticsManager.Label.HOTEL_LOCATION_LIST;
                break;
            case STAY_BOUTIQUE:
                label = AnalyticsManager.Label.BOUTIQUE_LOCATION_LIST;
                break;
            case STAY_PENSION:
                label = AnalyticsManager.Label.PENSION_LOCATION_LIST;
                break;
            case STAY_RESORT:
                label = AnalyticsManager.Label.RESORT_LOCATION_LIST;
                break;
            default:
                label = AnalyticsManager.ValueType.EMPTY;
                break;
        }

        AnalyticsManager.getInstance(activity).recordEvent(AnalyticsManager.Category.NAVIGATION, //
            AnalyticsManager.Action.STAY_NEARBY_SEARCH, label, null);
    }

    private String getAnalyticsRegionLabel(String overseasName, String provinceName, String areaName)
    {
        if (DailyTextUtils.isTextEmpty(provinceName) == true)
        {
            provinceName = "None";
        }

        if (DailyTextUtils.isTextEmpty(areaName) == true)
        {
            areaName = "None";
        }

        return String.format(Locale.KOREA, "%s-%s-%s", overseasName, provinceName, areaName);
    }
}
