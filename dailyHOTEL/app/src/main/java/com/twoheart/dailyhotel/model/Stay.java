package com.twoheart.dailyhotel.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.util.DailyAssert;
import com.twoheart.dailyhotel.util.ExLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class Stay extends Place
{
    public String dBenefitText;
    public int nights;
    public double distance; // 정렬시에 보여주는 내용
    public String categoryCode;
    public String sday;

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
        dest.writeString(sday);
    }

    protected void readFromParcel(Parcel in)
    {
        super.readFromParcel(in);

        mGrade = (Grade) in.readSerializable();
        dBenefitText = in.readString();
        categoryCode = in.readString();
        sday = in.readString();
    }

    public Grade getGrade()
    {
        return mGrade;
    }

    public boolean setStay(JSONObject jsonObject, String imageUrl, int nights)
    {
        this.nights = nights;

        try
        {
            name = jsonObject.getString("name");
            price = jsonObject.getInt("price");
            discountPrice = jsonObject.getInt("discount"); // discountAvg ????
            addressSummary = jsonObject.getString("addrSummary");

            DailyAssert.assertNotNull(name);
            DailyAssert.assertNotNull(price);
            DailyAssert.assertNotNull(discountPrice);
            DailyAssert.assertNotNull(addressSummary);

            try
            {
                mGrade = Grade.valueOf(jsonObject.getString("grade"));
                DailyAssert.assertNotNull(mGrade);
            } catch (Exception e)
            {
                mGrade = Grade.etc;
            }

            index = jsonObject.getInt("hotelIdx");
            DailyAssert.assertNotNull(index);

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
            sday = jsonObject.getString("sday");
            distance = jsonObject.getDouble("distance");

            DailyAssert.assertNotNull(districtName);
            DailyAssert.assertNotNull(categoryCode);
            DailyAssert.assertNotNull(latitude);
            DailyAssert.assertNotNull(longitude);
            DailyAssert.assertNotNull(isDailyChoice);
            DailyAssert.assertNotNull(satisfaction);
            DailyAssert.assertNotNull(sday);
            DailyAssert.assertNotNull(distance);

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

                        DailyAssert.assertNotNull(pathJSONArray.getString(0));
                        break;
                    } catch (JSONException e)
                    {
                        DailyAssert.fail(e);
                    }
                }
            } catch (Exception e)
            {
                ExLog.d(e.toString());
                DailyAssert.fail(e);
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
            DailyAssert.fail(e);

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
