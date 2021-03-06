package com.daily.dailyhotel.screen.common.calendar.stay;

import android.app.Activity;

import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ExLog;
import com.twoheart.dailyhotel.util.DailyCalendar;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class StayCalendarAnalyticsImpl implements StayCalendarInterface.AnalyticsInterface
{
    private String mEnterCheckInDateTime, mEnterCheckOutDateTime;

    @Override
    public void setCheckInOutDateTime(String checkInDateTime, String checkOutDateTime)
    {
        mEnterCheckInDateTime = checkInDateTime;
        mEnterCheckOutDateTime = checkOutDateTime;
    }

    @Override
    public void onScreen(Activity activity)
    {
        if (activity == null)
        {
            return;
        }

        AnalyticsManager.getInstance(activity).recordScreen(activity, AnalyticsManager.Screen.DAILYHOTEL_LIST_CALENDAR, null);
    }

    @Override
    public void onEventCloseClick(Activity activity, String callByScreen)
    {
        if (activity == null)
        {
            return;
        }

        AnalyticsManager.getInstance(activity).recordEvent(AnalyticsManager.Category.NAVIGATION_//
            , AnalyticsManager.Action.HOTEL_BOOKING_CALENDAR_CLOSED, callByScreen, null);
    }

    @Override
    public void onEventConfirmClick(Activity activity, String callByScreen, String checkInDateTime, String checkOutDateTime, int stayIndex)
    {
        if (activity == null || DailyTextUtils.isTextEmpty(checkInDateTime, checkOutDateTime) == true)
        {
            return;
        }

        try
        {
            boolean changedDate = checkInDateTime.equalsIgnoreCase(mEnterCheckInDateTime) && checkOutDateTime.equalsIgnoreCase(mEnterCheckOutDateTime);

            String checkInDate = DailyCalendar.convertDateFormatString(checkInDateTime, DailyCalendar.ISO_8601_FORMAT, "yyyy.MM.dd(EEE)");
            String checkOutDate = DailyCalendar.convertDateFormatString(checkOutDateTime, DailyCalendar.ISO_8601_FORMAT, "yyyy.MM.dd(EEE)");
            int nights = DailyCalendar.compareDateDay(checkOutDateTime, checkInDateTime);

            Map<String, String> params = new HashMap<>();
            params.put(AnalyticsManager.KeyType.CHECK_IN_DATE, DailyCalendar.convertDateFormatString(checkInDateTime, DailyCalendar.ISO_8601_FORMAT, "yyyyMMdd"));
            params.put(AnalyticsManager.KeyType.CHECK_OUT_DATE, DailyCalendar.convertDateFormatString(checkOutDateTime, DailyCalendar.ISO_8601_FORMAT, "yyyyMMdd"));
            params.put(AnalyticsManager.KeyType.LENGTH_OF_STAY, Integer.toString(nights));
            params.put(AnalyticsManager.KeyType.SCREEN, callByScreen);

            String phoneDate = DailyCalendar.format(new Date(), "yyyy.MM.dd(EEE) HH시 mm분");

            AnalyticsManager.getInstance(activity).recordEvent(AnalyticsManager.Category.NAVIGATION_, AnalyticsManager.Action.HOTEL_BOOKING_DATE_CLICKED//
                , (changedDate ? AnalyticsManager.ValueType.CHANGED : AnalyticsManager.ValueType.NONE_) + "-" + checkInDate + "-" + checkOutDate + "-" + phoneDate, params);

            if (stayIndex > 0)
            {
                AnalyticsManager.getInstance(activity).recordEvent(AnalyticsManager.Category.DETAILVIEW_STAY, "filter_date_confirmed"//
                    , params.get(AnalyticsManager.KeyType.CHECK_IN_DATE) + "_" + params.get(AnalyticsManager.KeyType.CHECK_OUT_DATE) + "_" + nights, null);
            }
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }
}
