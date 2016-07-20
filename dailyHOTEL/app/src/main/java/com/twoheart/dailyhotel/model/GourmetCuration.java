package com.twoheart.dailyhotel.model;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

public class GourmetCuration extends PlaceCuration
{
    private SaleTime mSaleTime;

    private GourmetCurationOption mGourmetCurationOption;

    public GourmetCuration()
    {
        mGourmetCurationOption = new GourmetCurationOption();

        clear();
    }

    public void clear()
    {
        mGourmetCurationOption.clear();

        mProvince = null;
        mLocation = null;
        mSaleTime = null;
    }

    public GourmetCurationOption getGourmetCurationOption()
    {
        return mGourmetCurationOption;
    }

    public void setSaleTime(long currentDateTime, long dailyDateTime)
    {
        if (mSaleTime == null)
        {
            mSaleTime = new SaleTime();
        }

        mSaleTime.setCurrentTime(currentDateTime);
        mSaleTime.setDailyTime(dailyDateTime);
    }

    public void setSaleTime(SaleTime saleTime)
    {
        mSaleTime = saleTime;
    }

    public SaleTime getSaleTime()
    {
        return mSaleTime;
    }

    public GourmetCuration(Parcel in)
    {
        readFromParcel(in);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(mProvince, flags);
        dest.writeParcelable(mLocation, flags);
        dest.writeParcelable(mSaleTime, flags);
        dest.writeParcelable(mGourmetCurationOption, flags);
    }

    protected void readFromParcel(Parcel in)
    {
        mProvince = in.readParcelable(Province.class.getClassLoader());
        mLocation = in.readParcelable(Location.class.getClassLoader());
        mSaleTime = in.readParcelable(SaleTime.class.getClassLoader());
        mGourmetCurationOption = in.readParcelable(GourmetCurationOption.class.getClassLoader());
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        public GourmetCuration createFromParcel(Parcel in)
        {
            return new GourmetCuration(in);
        }

        @Override
        public GourmetCuration[] newArray(int size)
        {
            return new GourmetCuration[size];
        }
    };
}
