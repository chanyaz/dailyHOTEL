package com.daily.dailyhotel.parcel.analytics;

import android.os.Parcel;
import android.os.Parcelable;

import com.daily.base.util.DailyTextUtils;
import com.daily.dailyhotel.entity.StayTown;
import com.daily.dailyhotel.parcel.StayTownParcel;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;

/**
 * Created by android_sam on 2017. 6. 23..
 */

public class StayDetailAnalyticsParam implements Parcelable
{
    public int price; // 정가
    public int discountPrice; // 표시가
    public int entryPosition = -1;
    public int totalListCount = -1;
    public boolean isDailyChoice;
    public String gradeName;

    private String mAddressAreaName; // addressSummary 의 split 이름 stay.addressSummary.split("\\||l|ㅣ|I")  index : 0;
    private StayTown mTown;
    private String mShowOriginalPriceYn = "N"; // stay.price <= 0 || stay.price <= stay.discountPrice ? "N" : "Y"

    public StayDetailAnalyticsParam()
    {
        super();
    }

    public StayDetailAnalyticsParam(Parcel in)
    {
        readFromParcel(in);
    }

    public void setTown(StayTown town)
    {
        mTown = town;
    }

    public StayTown getTown()
    {
        return mTown;
    }

    public String getProvinceName()
    {
        if (mTown == null)
        {
            return AnalyticsManager.ValueType.EMPTY;
        }

        return mTown.name;
    }

    public String getDistrictName()
    {
        if (mTown == null)
        {
            return AnalyticsManager.ValueType.EMPTY;
        }

        return mTown.index != StayTown.ALL ? mTown.name : AnalyticsManager.ValueType.ALL_LOCALE_KR;
    }

    public String getAddressAreaName()
    {
        return mAddressAreaName;
    }

    public void setAddressAreaName(String addressSummary)
    {
        if (DailyTextUtils.isTextEmpty(addressSummary) == true)
        {
            return;
        }

        String[] addressArray = addressSummary.split("\\||l|ㅣ|I");
        mAddressAreaName = addressArray[0].trim();
    }

    public String getShowOriginalPriceYn()
    {
        return mShowOriginalPriceYn;
    }

    public void setShowOriginalPriceYn(int originPrice, int discountPrice)
    {
        mShowOriginalPriceYn = originPrice <= 0 || originPrice <= discountPrice ? "N" : "Y";
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(mAddressAreaName);
        dest.writeInt(price);
        dest.writeInt(discountPrice);
        dest.writeString(mShowOriginalPriceYn);
        dest.writeInt(entryPosition);
        dest.writeInt(totalListCount);
        dest.writeInt(isDailyChoice == true ? 1 : 0);

        if (mTown == null)
        {
            dest.writeParcelable(null, flags);
        } else
        {
            dest.writeParcelable(new StayTownParcel(mTown), flags);
        }
    }

    protected void readFromParcel(Parcel in)
    {
        mAddressAreaName = in.readString();
        price = in.readInt();
        discountPrice = in.readInt();
        mShowOriginalPriceYn = in.readString();
        entryPosition = in.readInt();
        totalListCount = in.readInt();
        isDailyChoice = in.readInt() == 1 ? true : false;

        StayTownParcel stayTownParcel = in.readParcelable(StayTownParcel.class.getClassLoader());

        if (stayTownParcel != null)
        {
            mTown = stayTownParcel.getStayTown();
        }
    }

    public static final Creator CREATOR = new Creator()
    {
        @Override
        public StayDetailAnalyticsParam createFromParcel(Parcel source)
        {
            return new StayDetailAnalyticsParam(source);
        }

        @Override
        public StayDetailAnalyticsParam[] newArray(int size)
        {
            return new StayDetailAnalyticsParam[size];
        }
    };
}
