package com.twoheart.dailyhotel.model;

import android.os.Parcel;
import android.os.Parcelable;

public abstract class PlacePaymentInformation implements Parcelable
{
    public int placeIndex; // 호텔 호텔인덱스 고메 고메 인덱스
    public int bonus; // 적립금
    public boolean isUsedBonus; // 적립금을 사용할 경우
    public boolean isUsedCoupon; // 쿠폰을 사용할 경우
    public PaymentType paymentType; //
    public boolean isDBenefit;

    private Coupon mCoupon;
    private Customer mCustomer;
    private Guest mGuest;

    public PlacePaymentInformation()
    {
        paymentType = PaymentType.EASY_CARD;
    }

    public PlacePaymentInformation(Parcel in)
    {
        readFromParcel(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(placeIndex);
        dest.writeInt(bonus);
        dest.writeInt(isUsedBonus ? 1 : 0);
        dest.writeString(paymentType.name());
        dest.writeInt(isDBenefit ? 1 : 0);
        dest.writeParcelable(mCustomer, flags);
        dest.writeParcelable(mGuest, flags);
        dest.writeParcelable(mCoupon, flags);
    }

    protected void readFromParcel(Parcel in)
    {
        placeIndex = in.readInt();
        bonus = in.readInt();
        isUsedBonus = in.readInt() == 1;
        paymentType = PaymentType.valueOf(in.readString());
        isDBenefit = in.readInt() == 1;
        mCustomer = in.readParcelable(Customer.class.getClassLoader());
        mGuest = in.readParcelable(Guest.class.getClassLoader());
        mCoupon = in.readParcelable(Coupon.class.getClassLoader());
    }

    public Customer getCustomer()
    {
        return mCustomer;
    }

    public void setCustomer(Customer customer)
    {
        this.mCustomer = customer;
    }

    public Coupon getCoupon()
    {
        return mCoupon;
    }

    public void setCoupon(Coupon coupon)
    {
        mCoupon = coupon;
    }

    public Guest getGuest()
    {
        return mGuest;
    }

    public void setGuest(Guest guest)
    {
        mGuest = guest;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    // 명칭 변경하면 안됨 서버와 약속되어있음.
    public enum PaymentType
    {
        EASY_CARD("EasyCardPay"),
        CARD("CardPay"),
        PHONE_PAY("PhoneBillPay"),
        VBANK("VirtualAccountPay");

        private String mName;

        PaymentType(String name)
        {
            mName = name;
        }

        public String getName()
        {
            return mName;
        }
    }
}
