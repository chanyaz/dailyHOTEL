package com.twoheart.dailyhotel.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by android_sam on 2016. 6. 20..
 */
public class CouponHistory extends Coupon
{
    public boolean isExpired; // 유효기간 만료 여부 ,
    public boolean isRedeemed; // 사용 여부 ,
    public String disabledAt; // isExpired가 true이면 만료된 날짜, isRedeemed가 true이면 사용한 날짜. isExpired와 isRedeemed는 같은 값일 수 없음. (ISO-8601)

    public CouponHistory(Parcel in)
    {
        readFromParcel(in);
    }

    // Coupon History
    public CouponHistory(int amount, String title, String validFrom, String validTo, //
                         int amountMinimum, boolean isExpired, boolean isRedeemed, String disabledAt, //
                         boolean availableInStay, boolean availableInOutboundHotel, boolean availableInGourmet)
    {
        this.amount = amount; //
        this.title = title; //
        this.validFrom = validFrom; //
        this.validTo = validTo; //
        this.amountMinimum = amountMinimum; //
        this.isExpired = isExpired; //
        this.isRedeemed = isRedeemed; //
        this.disabledAt = disabledAt; //
        this.availableInOutboundHotel = availableInOutboundHotel;
        this.availableInStay = availableInStay;
        this.availableInGourmet = availableInGourmet;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        super.writeToParcel(dest, flags);

        dest.writeInt(isExpired == true ? 1 : 0);
        dest.writeInt(isRedeemed == true ? 1 : 0);
        dest.writeString(disabledAt);
    }

    @Override
    public void readFromParcel(Parcel in)
    {
        super.readFromParcel(in);

        isExpired = in.readInt() == 1;
        isRedeemed = in.readInt() == 1;
        disabledAt = in.readString();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        public Coupon createFromParcel(Parcel in)
        {
            return new CouponHistory(in);
        }

        @Override
        public CouponHistory[] newArray(int size)
        {
            return new CouponHistory[size];
        }
    };
}
