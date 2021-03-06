package com.twoheart.dailyhotel.network.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonIgnore;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;
import com.daily.base.util.DailyTextUtils;

import java.util.List;

@JsonObject
public class GourmetProduct implements Parcelable
{
    @JsonField(name = "idx")
    public int index;

    @JsonField(name = "saleIdx")
    public int saleIdx;

    @JsonField(name = "ticketName")
    public String ticketName;

    @JsonField(name = "price")
    public int price;

    @JsonField(name = "discount")
    public int discountPrice;

    @JsonField(name = "menuBenefit")
    public String menuBenefit;

    @JsonField(name = "needToKnow")
    public String needToKnow;

    @JsonField(name = "reserveCondition")
    public String reserveCondition;

    //    @JsonField
    //    public String startEatingTime;
    //
    //    @JsonField
    //    public String endEatingTime;
    //
    //    @JsonField
    //    public int timeInterval;
    //
    @JsonField(name = "openTime")
    public String openTime;

    @JsonField(name = "closeTime")
    public String closeTime;

    @JsonField(name = "lastOrderTime")
    public String lastOrderTime;

    //
    //    @JsonField
    //    public String expiryTime;

    @JsonField(name = "images")
    public List<ProductImageInformation> images;

    @JsonField(name = "menuSummary")
    public String menuSummary;

    @JsonField(name = "menuDetail")
    public List<String> menuDetail;

    @JsonIgnore
    private int mDefaultImageIndex;

    public GourmetProduct()
    {
    }

    public GourmetProduct(Parcel in)
    {
        readFromParcel(in);
    }

    @OnJsonParseComplete
    void onParseComplete()
    {
        if (images != null && images.size() > 0)
        {
            int size = images.size();

            for (int i = 0; i < size; i++)
            {
                if (images.get(i).isPrimary == true)
                {
                    mDefaultImageIndex = i;
                    break;
                }
            }
        }

        // 기본 포맷은 HH:mm:ss
        if (DailyTextUtils.isTextEmpty(openTime) == false)
        {
            openTime = openTime.substring(0, openTime.length() - 3);
        }

        if (DailyTextUtils.isTextEmpty(closeTime) == false)
        {
            closeTime = closeTime.substring(0, closeTime.length() - 3);

            // 00:00 -> 24:00
            if ("00:00".equalsIgnoreCase(closeTime) == true)
            {
                closeTime = "24:00";
            }
        }

        if (DailyTextUtils.isTextEmpty(lastOrderTime) == false)
        {
            lastOrderTime = lastOrderTime.substring(0, lastOrderTime.length() - 3);

            // 00:00 -> 24:00
            if ("00:00".equalsIgnoreCase(lastOrderTime) == true)
            {
                lastOrderTime = "24:00";
            }
        }
    }

    public List<ProductImageInformation> getImageList()
    {
        return images;
    }

    public ProductImageInformation getPrimaryImage()
    {
        if (images == null || images.size() == 0)
        {
            return null;
        }

        return images.get(mDefaultImageIndex);
    }

    public List<String> getMenuDetailList()
    {
        return menuDetail;
    }

    public int getPrimaryIndex()
    {
        return mDefaultImageIndex;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(index);
        dest.writeInt(saleIdx);
        dest.writeString(ticketName);
        dest.writeInt(price);
        dest.writeInt(discountPrice);
        dest.writeString(menuBenefit);
        dest.writeString(needToKnow);
        //        dest.writeString(startEatingTime);
        //        dest.writeString(endEatingTime);
        //        dest.writeInt(timeInterval);
        dest.writeString(openTime);
        dest.writeString(closeTime);
        dest.writeString(lastOrderTime);
        //        dest.writeString(expiryTime);
        dest.writeTypedList(images);
        dest.writeString(menuSummary);
        dest.writeStringList(menuDetail);
    }

    protected void readFromParcel(Parcel in)
    {
        index = in.readInt();
        saleIdx = in.readInt();
        ticketName = in.readString();
        price = in.readInt();
        discountPrice = in.readInt();
        menuBenefit = in.readString();
        needToKnow = in.readString();
        //        startEatingTime = in.readString();
        //        endEatingTime = in.readString();
        //        timeInterval = in.readInt();
        openTime = in.readString();
        closeTime = in.readString();
        lastOrderTime = in.readString();
        //        expiryTime = in.readString();
        images = in.createTypedArrayList(ProductImageInformation.CREATOR);
        menuSummary = in.readString();
        menuDetail = in.createStringArrayList();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Creator CREATOR = new Creator()
    {
        public GourmetProduct createFromParcel(Parcel in)
        {
            return new GourmetProduct(in);
        }

        @Override
        public GourmetProduct[] newArray(int size)
        {
            return new GourmetProduct[size];
        }
    };
}