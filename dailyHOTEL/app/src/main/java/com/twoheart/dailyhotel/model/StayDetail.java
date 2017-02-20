package com.twoheart.dailyhotel.model;

import android.content.Context;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.network.model.ImageInformation;
import com.twoheart.dailyhotel.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StayDetail extends PlaceDetail<RoomInformation>
{
    public int nights;
    public Stay.Grade grade;
    private ArrayList<RoomInformation> mSaleRoomList;
    //
    public String categoryCode;
    public boolean isSingleStay; // 연박 불가인지 아닌지

    public String name;
    public String address;
    public boolean isOverseas; // 0 : 국내 , 1 : 해외
    public String benefit;
    public int ratingPersons;
    public int ratingValue;
    public double latitude;
    public double longitude;
    public boolean hasCoupon;
    public boolean myWish; // 위시리스트 클릭 상태
    public int wishCount; // 위시리스트 카운트

    private ArrayList<ImageInformation> mImageInformationList;
    private ArrayList<DetailInformation> mInformationList;
    private ArrayList<String> mBenefitInformation;
    private ArrayList<Pictogram> mPictogramList;

    public StayDetail(int hotelIndex, int nights, int entryIndex, String isShowOriginalPrice, int listCount, boolean isDailyChoice)
    {
        this.index = hotelIndex;
        this.nights = nights;
        this.entryPosition = entryIndex;
        this.isShowOriginalPrice = isShowOriginalPrice;
        this.listCount = listCount;
        this.isDailyChoice = isDailyChoice;
    }

    public void setData(JSONObject jsonObject) throws Exception
    {
        try
        {
            grade = Stay.Grade.valueOf(jsonObject.getString("grade"));
        } catch (Exception e)
        {
            grade = Stay.Grade.etc;
        }

        name = jsonObject.getString("name");
        address = jsonObject.getString("address");

        longitude = jsonObject.getDouble("longitude");
        latitude = jsonObject.getDouble("latitude");
        isOverseas = jsonObject.getBoolean("overseas");

        boolean ratingShow = jsonObject.getBoolean("ratingShow");

        if (ratingShow == true)
        {
            ratingValue = jsonObject.getInt("ratingValue");
            ratingPersons = jsonObject.getInt("ratingPersons");
        }

        if (jsonObject.has("singleStay") == true)
        {
            isSingleStay = jsonObject.getBoolean("singleStay");
        } else
        {
            isSingleStay = false;
        }

        // Pictogram
        if (mPictogramList == null)
        {
            mPictogramList = new ArrayList<>();
        }

        mPictogramList.clear();

        // 주차
        if (jsonObject.getBoolean("parking") == true)
        {
            mPictogramList.add(Pictogram.parking);
        }

        // 주차금지
        if (jsonObject.getBoolean("noParking") == true)
        {
            mPictogramList.add(Pictogram.noParking);
        }

        // 수영장
        if (jsonObject.getBoolean("pool") == true)
        {
            mPictogramList.add(Pictogram.pool);
        }

        // 피트니스
        if (jsonObject.getBoolean("fitness") == true)
        {
            mPictogramList.add(Pictogram.fitness);
        }

        // 애완동물
        if (jsonObject.getBoolean("pet") == true)
        {
            mPictogramList.add(Pictogram.pet);
        }

        // 바베큐
        if (jsonObject.getBoolean("sharedBbq") == true)
        {
            mPictogramList.add(Pictogram.sharedBbq);
        }

        // Image Url
        String imageUrl = jsonObject.getString("imgUrl");
        JSONObject pathUrlJSONObject = jsonObject.getJSONObject("imgPath");

        Iterator<String> iterator = pathUrlJSONObject.keys();
        while (iterator.hasNext())
        {
            String key = iterator.next();

            try
            {
                JSONArray pathJSONArray = pathUrlJSONObject.getJSONArray(key);

                int length = pathJSONArray.length();
                mImageInformationList = new ArrayList<>(pathJSONArray.length());

                for (int i = 0; i < length; i++)
                {
                    JSONObject imageInformationJSONObject = pathJSONArray.getJSONObject(i);

                    ImageInformation imageInformation = new ImageInformation();
                    imageInformation.description = imageInformationJSONObject.getString("description");
                    imageInformation.name = imageInformationJSONObject.getString("name");
                    imageInformation.setImageUrl(imageUrl + key + imageInformation.name);

                    mImageInformationList.add(imageInformation);
                }
                break;
            } catch (JSONException e)
            {
            }
        }

        // benefit
        if (jsonObject.has("benefit") == true)
        {
            benefit = jsonObject.getString("benefit");

            if (Util.isTextEmpty(benefit) == false && jsonObject.has("benefitContents") == true && jsonObject.isNull("benefitContents") == false)
            {
                JSONArray benefitJSONArray = jsonObject.getJSONArray("benefitContents");

                int length = benefitJSONArray.length();

                if (length > 0)
                {
                    mBenefitInformation = new ArrayList<>(length);

                    for (int i = 0; i < length; i++)
                    {
                        mBenefitInformation.add(benefitJSONArray.getString(i));
                    }
                } else
                {
                    mBenefitInformation = new ArrayList<>();
                }

                if (jsonObject.has("benefitWarning") == true && jsonObject.isNull("benefitWarning") == false)
                {
                    String benefitWarning = jsonObject.getString("benefitWarning");

                    if (Util.isTextEmpty(benefitWarning) == false)
                    {
                        mBenefitInformation.add(benefitWarning);
                    }
                }
            }
        }

        // Detail
        JSONArray detailJSONArray = jsonObject.getJSONArray("details");
        int detailLength = detailJSONArray.length();

        mInformationList = new ArrayList<>(detailLength);

        for (int i = 0; i < detailLength; i++)
        {
            mInformationList.add(new DetailInformation(detailJSONArray.getJSONObject(i)));
        }

        // Room Sale Info

        if (jsonObject.has("rooms") == true && jsonObject.isNull("rooms") == false)
        {
            JSONArray saleRoomJSONArray = jsonObject.getJSONArray("rooms");

            int saleRoomLength = saleRoomJSONArray.length();

            mSaleRoomList = new ArrayList<>(saleRoomLength);

            for (int i = 0; i < saleRoomLength; i++)
            {
                RoomInformation roomInformation = new RoomInformation(name, saleRoomJSONArray.getJSONObject(i), isOverseas, nights);
                roomInformation.grade = grade;
                roomInformation.address = address;
                mSaleRoomList.add(roomInformation);
            }
        } else
        {
            mSaleRoomList = new ArrayList<>();
        }

        if (jsonObject.has("myWish") == true)
        {
            myWish = jsonObject.getBoolean("myWish");
        }

        if (jsonObject.has("wishCount") == true)
        {
            wishCount = jsonObject.getInt("wishCount");
        }
    }

    @Override
    public List<RoomInformation> getProductList()
    {
        return mSaleRoomList;
    }

    @Override
    public RoomInformation getProduct(int index)
    {
        if (mSaleRoomList == null || mSaleRoomList.size() <= index)
        {
            return null;
        }

        return mSaleRoomList.get(index);
    }

    @Override
    public List<Pictogram> getPictogramList()
    {
        if (mPictogramList == null)
        {
            mPictogramList = new ArrayList<>();
        }

        return mPictogramList;
    }

    @Override
    public List<ImageInformation> getImageList()
    {
        return mImageInformationList;
    }

    @Override
    public List<DetailInformation> getDetailList()
    {
        return mInformationList;
    }

    @Override
    public List<String> getBenefitList()
    {
        return mBenefitInformation;
    }

    public enum Pictogram
    {
        parking(R.string.label_parking, R.drawable.ic_detail_facilities_01_parking),
        noParking(R.string.label_unabled_parking, R.drawable.ic_detail_facilities_02_no_parking),
        pool(R.string.label_pool, R.drawable.ic_detail_facilities_03_pool),
        fitness(R.string.label_fitness, R.drawable.ic_detail_facilities_04_fitness),
        pet(R.string.label_allowed_pet, R.drawable.ic_detail_facilities_05_pet),
        sharedBbq(R.string.label_allowed_barbecue, R.drawable.ic_detail_facilities_06_bbq),
        none(0, 0);

        private int mNameResId;
        private int mImageResId;

        Pictogram(int nameResId, int imageResId)
        {
            mNameResId = nameResId;
            mImageResId = imageResId;
        }

        public String getName(Context context)
        {
            if (mNameResId == 0)
            {
                return null;
            }

            return context.getString(mNameResId);
        }

        public int getImageResId()
        {
            return mImageResId;
        }
    }
}