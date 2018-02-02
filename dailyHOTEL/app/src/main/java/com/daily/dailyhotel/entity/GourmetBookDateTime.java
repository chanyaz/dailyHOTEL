package com.daily.dailyhotel.entity;

import com.twoheart.dailyhotel.model.time.GourmetBookingDay;
import com.twoheart.dailyhotel.util.DailyCalendar;

public class GourmetBookDateTime extends PlaceBookDateTime
{
    public GourmetBookDateTime()
    {
    }

    public GourmetBookDateTime(String visitDateTime) throws Exception
    {
        setVisitDateTime(visitDateTime);
    }

    /**
     * @param dateTime ISO-8601
     */
    public void setVisitDateTime(String dateTime) throws Exception
    {
        setTimeInString(dateTime);
    }

    /**
     * @param dateTime ISO-8601
     */
    public void setVisitDateTime(String dateTime, int afterDay) throws Exception
    {
        setTimeInString(dateTime, afterDay);
    }

    public String getVisitDateTime(String format)
    {
        return getString(format);
    }

    public GourmetBookingDay getGourmetBookingDay() throws Exception
    {
        GourmetBookingDay gourmetBookingDay = new GourmetBookingDay();
        gourmetBookingDay.setVisitDay(getVisitDateTime(DailyCalendar.ISO_8601_FORMAT));

        return gourmetBookingDay;
    }
}
