package com.twoheart.dailyhotel.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.twoheart.dailyhotel.util.DailyCalendar;
import com.twoheart.dailyhotel.util.ExLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SaleTime implements Parcelable
{
    public static final long MILLISECOND_IN_A_DAY = 3600 * 24 * 1000;

    private Date mCurrentTime;
    private Date mDailyTime;
    private int mDayOfDays; // 데이즈 날짜. currentTime으로 부터 몇일.

    public SaleTime()
    {
        super();
    }

    public SaleTime(Parcel in)
    {
        readFromParcel(in);
    }

    //    public String toString()
    //    {
    //        SimpleDateFormat sFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss", Locale.KOREA);
    //        sFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    //
    //        StringBuilder stringBuilder = new StringBuilder();
    //
    //        stringBuilder.append("currentTime : " + sFormat.format(mCurrentTime));
    //        stringBuilder.append("\ndailyTime : " + sFormat.format(mDailyTime));
    //        stringBuilder.append("\nmDayOfDays : " + mDayOfDays));
    //
    //        return stringBuilder.toString();
    //    }

    //    public String getDailyDateFormat(String format)
    //    {
    //        return getTimezonedDateFormat(format).format(getDayOfDaysDate());
    //    }

    public Date getDayOfDaysDate()
    {
        return new Date(mDailyTime.getTime() + MILLISECOND_IN_A_DAY * mDayOfDays);
    }

    public String getDayOfDaysDateFormat(String format)
    {
        //        return getTimezonedDateFormat(format).format(getDayOfDaysDate());
        return DailyCalendar.format(getDayOfDaysDate().getTime(), format, TimeZone.getTimeZone("GMT"));
    }

    /**
     * DailyTime 기준으로 offsetDay가 된다.
     *
     * @param nextDay
     * @return
     */
    public SaleTime getClone(int nextDay)
    {
        SaleTime nextSaleTime = new SaleTime();

        nextSaleTime.mDayOfDays = nextDay;
        nextSaleTime.mCurrentTime = new Date(mCurrentTime.getTime());
        nextSaleTime.mDailyTime = new Date(mDailyTime.getTime());

        return nextSaleTime;
    }

    public SaleTime getClone()
    {
        SaleTime nextSaleTime = new SaleTime();

        nextSaleTime.mDayOfDays = mDayOfDays;
        nextSaleTime.mCurrentTime = new Date(mCurrentTime.getTime());
        nextSaleTime.mDailyTime = new Date(mDailyTime.getTime());

        return nextSaleTime;
    }

    //    public SimpleDateFormat getTimezonedDateFormat(String datePattern)
    //    {
    //        SimpleDateFormat sFormat = new SimpleDateFormat(datePattern, Locale.KOREA);
    //        sFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    //        return sFormat;
    //    }

    public boolean isDayOfDaysDateEquals(SaleTime saleTime)
    {
        return mDailyTime.getTime() == saleTime.mDailyTime.getTime() && mDayOfDays == saleTime.mDayOfDays;
    }

    public void setCurrentTime(long currentTime)
    {
        mCurrentTime = new Date(currentTime);
    }

    public long getDailyTime()
    {
        return mDailyTime.getTime();
    }

    public void setDailyTime(long dailyTime)
    {
        mDailyTime = new Date(dailyTime);
    }

    public int getOffsetDailyDay()
    {
        return mDayOfDays;
    }

    public void setOffsetDailyDay(int dayOfDays)
    {
        mDayOfDays = dayOfDays;
    }

    /**
     * yyyyMMdd의 날짜를 기존의 SaleTime에 적용한다.
     *
     * @param saleTime
     * @param date
     */
    public static SaleTime changeDateSaleTime(SaleTime saleTime, String date)
    {
        return changeDateSaleTime(saleTime, date, 0);
    }

    public static SaleTime changeDateSaleTime(SaleTime saleTime, String date, int defaultPreviousOffsetDay)
    {
        SaleTime changedSaleTime = null;

        try
        {
            SimpleDateFormat format = new java.text.SimpleDateFormat("yyyyMMdd", Locale.KOREA);
            Date schemeDate = format.parse(date);
            Date dailyDate = format.parse(saleTime.getDayOfDaysDateFormat("yyyyMMdd"));

            int dailyDayOfDays = (int) ((schemeDate.getTime() - dailyDate.getTime()) / SaleTime.MILLISECOND_IN_A_DAY);

            if (dailyDayOfDays >= 0)
            {
                changedSaleTime = saleTime.getClone(dailyDayOfDays);
            } else
            {
                changedSaleTime = saleTime.getClone(defaultPreviousOffsetDay);
            }
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }

        return changedSaleTime;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        if (mCurrentTime == null)
        {
            mCurrentTime = new Date();
        }

        if (mDailyTime == null)
        {
            mDailyTime = new Date();
        }

        dest.writeLong(mCurrentTime.getTime());
        dest.writeLong(mDailyTime.getTime());
        dest.writeInt(mDayOfDays);
    }

    private void readFromParcel(Parcel in)
    {
        mCurrentTime = new Date(in.readLong());
        mDailyTime = new Date(in.readLong());
        mDayOfDays = in.readInt();
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        public SaleTime createFromParcel(Parcel in)
        {
            return new SaleTime(in);
        }

        @Override
        public SaleTime[] newArray(int size)
        {
            return new SaleTime[size];
        }

    };
}