package com.twoheart.dailyhotel.model;

import android.os.Parcel;
import android.os.Parcelable;

public class StayPaymentInformation extends PlacePaymentInformation
{
    public static final String VISIT_TYPE_NONE = "NONE"; // 아무것도 표시하지 않음
    public static final String VISIT_TYPE_PARKING = "PARKING"; // 도보/주차 표시
    public static final String VISIT_TYPE_NO_PARKING = "NO_PARKING"; // 주차 불가능

    private StayProduct mStayProduct;
    // Thankyou에 넘기기 위한 데이터 저장
    public long checkInDate;
    public long checkOutDate;
    public int nights;

    public String checkInDateFormat; // yyyy-MM-dd'T'HH:mm:ssZZZZZ , 쿠폰 요청시 사용
    public String checkOutDateFormat; // yyyy-MM-dd'T'HH:mm:ssZZZZZ , 쿠폰 요청시 사용

    //
    public String visitType = VISIT_TYPE_NONE; // 방문 형태로
    public boolean isVisitWalking = true; // 방문 방법 : 기본이 도보(visitType == "NONE", "NO_PARKING" 이면 서버로 아무것도 전송하지 않음)

    public StayPaymentInformation()
    {
        super();
    }

    public StayPaymentInformation(Parcel in)
    {
        readFromParcel(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        super.writeToParcel(dest, flags);

        dest.writeParcelable(mStayProduct, flags);
        dest.writeLong(checkInDate);
        dest.writeLong(checkOutDate);
        dest.writeInt(nights);
        dest.writeString(checkInDateFormat);
        dest.writeString(checkOutDateFormat);
        dest.writeString(visitType);
        dest.writeInt(isVisitWalking ? 1 : 0);
    }

    @Override
    protected void readFromParcel(Parcel in)
    {
        super.readFromParcel(in);

        mStayProduct = in.readParcelable(StayProduct.class.getClassLoader());
        checkInDate = in.readLong();
        checkOutDate = in.readLong();
        nights = in.readInt();
        checkInDateFormat = in.readString();
        checkOutDateFormat = in.readString();
        visitType = in.readString();
        isVisitWalking = in.readInt() == 1;
    }

    public StayProduct getSaleRoomInformation()
    {
        return mStayProduct;
    }

    public void setSaleRoomInformation(StayProduct information)
    {
        mStayProduct = information;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        public StayPaymentInformation createFromParcel(Parcel in)
        {
            return new StayPaymentInformation(in);
        }

        @Override
        public StayPaymentInformation[] newArray(int size)
        {
            return new StayPaymentInformation[size];
        }
    };
}
