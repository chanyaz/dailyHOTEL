package com.twoheart.dailyhotel.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

public class TicketInformation implements Parcelable
{
    public int index;
    public String name;
    public String option;
    public String benefit;
    public int price;
    public int discountPrice;
    public String placeName;
    public String thumbnailUrl;

    public TicketInformation(Parcel in)
    {
        readFromParcel(in);
    }

    public TicketInformation(String placeName, JSONObject jsonObject) throws Exception
    {
        index = jsonObject.getInt("saleIdx");
        name = jsonObject.getString("ticketName").trim();
        option = jsonObject.getString("option").trim();
        benefit = jsonObject.getString("benefit").trim();
        price = jsonObject.getInt("price");
        discountPrice = jsonObject.getInt("discount");

        this.placeName = placeName;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(index);
        dest.writeString(name);
        dest.writeString(option);
        dest.writeString(benefit);
        dest.writeInt(price);
        dest.writeInt(discountPrice);
        dest.writeString(placeName);
        dest.writeString(thumbnailUrl);
    }

    protected void readFromParcel(Parcel in)
    {
        index = in.readInt();
        name = in.readString();
        option = in.readString();
        benefit = in.readString();
        price = in.readInt();
        discountPrice = in.readInt();
        placeName = in.readString();
        thumbnailUrl = in.readString();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        public TicketInformation createFromParcel(Parcel in)
        {
            return new TicketInformation(in);
        }

        @Override
        public TicketInformation[] newArray(int size)
        {
            return new TicketInformation[size];
        }
    };
}
