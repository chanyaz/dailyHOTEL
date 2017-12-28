package com.daily.dailyhotel.screen.home.stay.inbound.calendar;

import android.app.Activity;

import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ExLog;
import com.twoheart.dailyhotel.util.DailyCalendar;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class StayCalendarAnalyticsImpl implements StayCalendarPresenter.StayCalendarPresenterAnalyticsInterface
{
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
    public void onCloseEventClick(Activity activity, String callByScreen)
    {
        if (activity == null)
        {
            return;
        }

        AnalyticsManager.getInstance(activity).recordEvent(AnalyticsManager.Category.NAVIGATION_//
            , AnalyticsManager.Action.HOTEL_BOOKING_CALENDAR_CLOSED, callByScreen, null);
    }

    @Override
    public void onConfirmClick(Activity activity, String callByScreen, boolean changedDate, String checkInDateTime, String checkOutDateTime)
    {
        if (activity == null || DailyTextUtils.isTextEmpty(checkInDateTime, checkOutDateTime) == true)
        {
            return;
        }

        try
        {
            String checkInDate = DailyCalendar.convertDateFormatString(checkInDateTime, DailyCalendar.ISO_8601_FORMAT, "yyyy.MM.dd(EEE)");
            String checkOutDate = DailyCalendar.convertDateFormatString(checkOutDateTime, DailyCalendar.ISO_8601_FORMAT, "yyyy.MM.dd(EEE)");
            int nights = DailyCalendar.compareDateDay(checkInDateTime, checkOutDateTime);

            Map<String, String> params = new HashMap<>();
            params.put(AnalyticsManager.KeyType.CHECK_IN_DATE, DailyCalendar.convertDateFormatString(checkInDateTime, DailyCalendar.ISO_8601_FORMAT, "yyyyMMdd");
            params.put(AnalyticsManager.KeyType.CHECK_OUT_DATE, DailyCalendar.convertDateFormatString(checkOutDateTime, DailyCalendar.ISO_8601_FORMAT, "yyyyMMdd");
            params.put(AnalyticsManager.KeyType.LENGTH_OF_STAY, Integer.toString(nights));
            params.put(AnalyticsManager.KeyType.SCREEN, callByScreen);

            String phoneDate = DailyCalendar.format(new Date(), "yyyy.MM.dd(EEE) HH시 mm분");

            AnalyticsManager.getInstance(activity).recordEvent(AnalyticsManager.Category.NAVIGATION_, AnalyticsManager.Action.HOTEL_BOOKING_DATE_CLICKED//
                , (changedDate ? AnalyticsManager.ValueType.CHANGED : AnalyticsManager.ValueType.NONE_) + "-" + checkInDate + "-" + checkOutDate + "-" + phoneDate, params);

        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }
}
}
