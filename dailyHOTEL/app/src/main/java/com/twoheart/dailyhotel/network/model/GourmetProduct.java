package com.twoheart.dailyhotel.network.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;
import com.twoheart.dailyhotel.util.ExLog;
import com.twoheart.dailyhotel.util.Util;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

@JsonObject
public class GourmetProduct implements Parcelable
{
    //    @JsonField(name = "idx")
    //    public int index;

    @JsonField
    public int saleIdx;

    @JsonField
    public String ticketName;

    @JsonField
    public int price;

    @JsonField(name = "discount")
    public int discountPrice;

    @JsonField
    public String worth;

    @JsonField
    public String checkList;

    //    @JsonField
    //    public String startEatingTime;
    //
    //    @JsonField
    //    public String endEatingTime;
    //
    //    @JsonField
    //    public int timeInterval;
    //
    @JsonField
    public String openTime;

    @JsonField
    public String closeTime;

    @JsonField
    public String lastOrderTime;

    //
    //    @JsonField
    //    public String expiryTime;

    @JsonField
    public List<ProductImageInformation> images;

    @JsonField
    public String menuSummary;

    @JsonField
    public String menuDetail;

    private int mDefaultImageIndex;

    private List<String> mMenuDetailList;

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

        if (Util.isTextEmpty(menuDetail) == false)
        {
            try
            {
                JSONArray jsonArray = new JSONArray(menuDetail);

                int length = jsonArray.length();

                mMenuDetailList = new ArrayList<>(length);

                for (int i = 0; i < length; i++)
                {
                    mMenuDetailList.add(jsonArray.getString(i));
                }
            } catch (Exception e)
            {
                ExLog.e(e.toString());
            }
        }

        // 기본 포맷은 HH:mm:ss
        if (Util.isTextEmpty(openTime) == false)
        {
            openTime = openTime.substring(0, openTime.length() - 3);
        }

        if (Util.isTextEmpty(closeTime) == false)
        {
            closeTime = closeTime.substring(0, closeTime.length() - 3);
        }

        if (Util.isTextEmpty(lastOrderTime) == false)
        {
            lastOrderTime = lastOrderTime.substring(0, lastOrderTime.length() - 3);
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
        return mMenuDetailList;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        //        dest.writeInt(index);
        dest.writeInt(saleIdx);
        dest.writeString(ticketName);
        dest.writeInt(price);
        dest.writeInt(discountPrice);
        dest.writeString(worth);
        dest.writeString(checkList);
        //        dest.writeString(startEatingTime);
        //        dest.writeString(endEatingTime);
        //        dest.writeInt(timeInterval);
        dest.writeString(openTime);
        dest.writeString(closeTime);
        dest.writeString(lastOrderTime);
        //        dest.writeString(expiryTime);
        dest.writeTypedList(images);
        dest.writeString(menuSummary);
        dest.writeString(menuDetail);
        dest.writeStringList(mMenuDetailList);
    }

    protected void readFromParcel(Parcel in)
    {
        //        index = in.readInt();
        saleIdx = in.readInt();
        ticketName = in.readString();
        price = in.readInt();
        discountPrice = in.readInt();
        worth = in.readString();
        checkList = in.readString();
        //        startEatingTime = in.readString();
        //        endEatingTime = in.readString();
        //        timeInterval = in.readInt();
        openTime = in.readString();
        closeTime = in.readString();
        lastOrderTime = in.readString();
        //        expiryTime = in.readString();
        images = in.createTypedArrayList(ProductImageInformation.CREATOR);
        menuSummary = in.readString();
        menuDetail = in.readString();
        mMenuDetailList = in.createStringArrayList();
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