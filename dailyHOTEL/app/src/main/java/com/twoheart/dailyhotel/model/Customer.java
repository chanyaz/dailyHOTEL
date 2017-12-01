package com.twoheart.dailyhotel.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Customer implements Parcelable
{
    private String mEmail;
    private String mName;
    private String mPhone;
    private String mUserIdx;

    public Customer()
    {
    }

    public Customer(Parcel in)
    {
        readFromParcel(in);
    }

    public Customer(com.daily.dailyhotel.entity.User user)
    {
        if (user == null)
        {
            return;
        }

        mEmail = user.email;
        mName = user.name;
        mPhone = user.phone;
        mUserIdx = Integer.toString(user.index);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(mEmail);
        dest.writeString(mName);
        dest.writeString(mPhone);
        dest.writeString(mUserIdx);
    }

    private void readFromParcel(Parcel in)
    {
        mEmail = in.readString();
        mName = in.readString();
        mPhone = in.readString();
        mUserIdx = in.readString();
    }

    public String getEmail()
    {
        return mEmail;
    }

    public void setEmail(String email)
    {
        this.mEmail = email;
    }

    public String getName()
    {
        return mName;
    }

    public void setName(String name)
    {
        this.mName = name;
    }

    public String getPhone()
    {
        return mPhone;
    }

    public void setPhone(String phone)
    {
        this.mPhone = phone;
    }

    public String getUserIdx()
    {
        return mUserIdx;
    }

    public void setUserIdx(String userIdx)
    {
        this.mUserIdx = userIdx;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        public Customer createFromParcel(Parcel in)
        {
            return new Customer(in);
        }

        @Override
        public Customer[] newArray(int size)
        {
            return new Customer[size];
        }

    };
}
