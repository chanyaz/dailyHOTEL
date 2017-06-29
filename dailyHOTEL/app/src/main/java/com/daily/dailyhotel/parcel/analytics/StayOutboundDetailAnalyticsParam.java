package com.daily.dailyhotel.parcel.analytics;

import android.os.Parcel;
import android.os.Parcelable;

public class StayOutboundDetailAnalyticsParam implements Parcelable
{
    public int index;
    public boolean benefit;
    public String grade;
    public int rankingPosition;
    public String rating;
    public int listCount;

    public StayOutboundDetailAnalyticsParam()
    {
    }

    public StayOutboundDetailAnalyticsParam(Parcel in)
    {
        readFromParcel(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(index);
        dest.writeInt(benefit == true ? 1 : 0);
        dest.writeString(grade);
        dest.writeInt(rankingPosition);
        dest.writeString(rating);
        dest.writeInt(listCount);
    }

    private void readFromParcel(Parcel in)
    {
        index = in.readInt();
        benefit = in.readInt() == 1 ? true : false;
        grade = in.readString();
        rankingPosition = in.readInt();
        rating = in.readString();
        listCount = in.readInt();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Creator CREATOR = new Creator()
    {
        public StayOutboundDetailAnalyticsParam createFromParcel(Parcel in)
        {
            return new StayOutboundDetailAnalyticsParam(in);
        }

        @Override
        public StayOutboundDetailAnalyticsParam[] newArray(int size)
        {
            return new StayOutboundDetailAnalyticsParam[size];
        }
    };
}
