package com.twoheart.dailyhotel.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.daily.base.util.ExLog;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.network.model.Prices;
import com.twoheart.dailyhotel.network.model.StayWishDetails;
import com.twoheart.dailyhotel.network.model.StayWishItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class Stay extends Place
{
    public String dBenefitText;
    //    public int nights = 1;
    public double distance; // 정렬시에 보여주는 내용
    public String categoryCode;
    //    public String sday;
    public boolean isLocalPlus;

    protected Grade mGrade;

    public Stay()
    {
        super();
    }

    public Stay(Parcel in)
    {
        readFromParcel(in);
    }

    @Override
    public int getGradeMarkerResId()
    {
        return mGrade.getMarkerResId();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        super.writeToParcel(dest, flags);

        dest.writeSerializable(mGrade);
        dest.writeString(dBenefitText);
        dest.writeString(categoryCode);
        //        dest.writeString(sday);
        dest.writeInt(isLocalPlus == true ? 1 : 0);
    }

    protected void readFromParcel(Parcel in)
    {
        super.readFromParcel(in);

        mGrade = (Grade) in.readSerializable();
        dBenefitText = in.readString();
        categoryCode = in.readString();
        //        sday = in.readString();
        isLocalPlus = in.readInt() == 1 ? true : false;
    }

    public Grade getGrade()
    {
        return mGrade;
    }

    public void setGrade(Grade grade)
    {
        if (grade == null)
        {
            mGrade = Grade.etc;
        } else
        {
            mGrade = grade;
        }
    }

    public boolean setStay(JSONObject jsonObject, String imageUrl)
    {
        //        this.nights = nights;

        try
        {
            name = jsonObject.getString("name");
            price = jsonObject.getInt("price");
            discountPrice = jsonObject.getInt("discount"); // discountAvg ????
            addressSummary = jsonObject.getString("addrSummary");

            try
            {
                mGrade = Grade.valueOf(jsonObject.getString("grade"));
            } catch (Exception e)
            {
                mGrade = Grade.etc;
            }

            index = jsonObject.getInt("hotelIdx");

            if (jsonObject.has("isSoldOut") == true)
            {
                isSoldOut = jsonObject.getBoolean("isSoldOut"); //
            }

            districtName = jsonObject.getString("districtName");
            categoryCode = jsonObject.getString("category");
            latitude = jsonObject.getDouble("latitude");
            longitude = jsonObject.getDouble("longitude");
            isDailyChoice = jsonObject.getBoolean("isDailyChoice");
            satisfaction = jsonObject.getInt("rating"); // ratingValue ??
            //            sday = jsonObject.getString("sday");
            distance = jsonObject.getDouble("distance");

            if (jsonObject.has("truevr") == true)
            {
                truevr = jsonObject.getBoolean("truevr");
            }

            try
            {
                JSONObject imageJSONObject = jsonObject.getJSONObject("imgPathMain");

                Iterator<String> iterator = imageJSONObject.keys();
                while (iterator.hasNext())
                {
                    String key = iterator.next();

                    try
                    {
                        JSONArray pathJSONArray = imageJSONObject.getJSONArray(key);
                        this.imageUrl = imageUrl + key + pathJSONArray.getString(0);
                        break;
                    } catch (JSONException e)
                    {
                    }
                }
            } catch (Exception e)
            {
                ExLog.d(e.toString());
            }

            if (jsonObject.has("benefit") == true) // hotelBenefit ?
            {
                dBenefitText = jsonObject.getString("benefit");
            } else
            {
                dBenefitText = null;
            }
        } catch (JSONException e)
        {
            ExLog.d(e.toString());
            return false;
        }

        return true;
    }

    public boolean setStay(StayWishItem stayWishItem, String imageUrl)
    {
        try
        {
            name = stayWishItem.title;

            Prices prices = stayWishItem.prices;

            price = prices == null ? 0 : prices.normalPrice;
            discountPrice = prices == null ? 0 : prices.discountPrice;

            addressSummary = stayWishItem.addrSummary;

            StayWishDetails stayWishDetails = stayWishItem.getDetails();

            mGrade = stayWishDetails != null ? stayWishDetails.stayGrade : Grade.etc;

            index = stayWishItem.index;
            districtName = stayWishItem.regionName;
            categoryCode = stayWishDetails != null ? stayWishDetails.category : "";
            satisfaction = stayWishItem.rating;
            truevr = stayWishDetails != null ? stayWishDetails.isTrueVR : false;

            try
            {
                this.imageUrl = imageUrl + stayWishItem.imageUrl;
            } catch (Exception e)
            {
                ExLog.d(e.toString());
            }

            dBenefitText = null;
        } catch (Exception e)
        {
            ExLog.d(e.toString());
            return false;
        }

        return true;
    }

    public enum Grade
    {
        special(R.string.grade_special, R.color.grade_special, R.drawable.bg_hotel_price_special1),
        special1(R.string.grade_special1, R.color.grade_special, R.drawable.bg_hotel_price_special1),
        special2(R.string.grade_special2, R.color.grade_special, R.drawable.bg_hotel_price_special1),
        //
        biz(R.string.grade_biz, R.color.grade_business, R.drawable.bg_hotel_price_business),
        hostel(R.string.grade_hostel, R.color.grade_business, R.drawable.bg_hotel_price_business),
        grade1(R.string.grade_1, R.color.grade_business, R.drawable.bg_hotel_price_business),
        grade2(R.string.grade_2, R.color.grade_business, R.drawable.bg_hotel_price_business),
        grade3(R.string.grade_3, R.color.grade_business, R.drawable.bg_hotel_price_business),
        //
        resort(R.string.grade_resort, R.color.grade_resort_pension_condo, R.drawable.bg_hotel_price_pension),
        pension(R.string.grade_pension, R.color.grade_resort_pension_condo, R.drawable.bg_hotel_price_pension),
        fullvilla(R.string.grade_fullvilla, R.color.grade_resort_pension_condo, R.drawable.bg_hotel_price_pension),
        condo(R.string.grade_condo, R.color.grade_resort_pension_condo, R.drawable.bg_hotel_price_pension),
        //
        boutique(R.string.grade_boutique, R.color.grade_boutique, R.drawable.bg_hotel_price_boutique),
        motel(R.string.grade_motel, R.color.grade_boutique, R.drawable.bg_hotel_price_boutique),
        //
        design(R.string.grade_design, R.color.grade_design, R.drawable.bg_hotel_price_design),
        //
        residence(R.string.grade_residence, R.color.grade_residence, R.drawable.bg_hotel_price_residence),
        //
        guest_house(R.string.grade_guesthouse, R.color.grade_guesthouse, R.drawable.bg_hotel_price_gesthouse),
        //
        etc(R.string.grade_not_yet, R.color.grade_not_yet, R.drawable.bg_hotel_price_etc);

        private int mNameResId;
        private int mColorResId;
        private int mMarkerResId;

        Grade(int nameResId, int colorResId, int markerResId)
        {
            mNameResId = nameResId;
            mColorResId = colorResId;
            mMarkerResId = markerResId;
        }

        public String getName(Context context)
        {
            return context.getString(mNameResId);
        }

        public int getColorResId()
        {
            return mColorResId;
        }

        public int getMarkerResId()
        {
            return mMarkerResId;
        }
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        public Stay createFromParcel(Parcel in)
        {
            return new Stay(in);
        }

        @Override
        public Stay[] newArray(int size)
        {
            return new Stay[size];
        }

    };
}
