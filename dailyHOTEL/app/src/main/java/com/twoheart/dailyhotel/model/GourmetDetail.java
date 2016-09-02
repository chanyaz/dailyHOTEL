package com.twoheart.dailyhotel.model;

import android.content.Context;

import com.twoheart.dailyhotel.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class GourmetDetail extends PlaceDetail
{
    public Gourmet.Grade grade;
    public String category;
    public String subCategory;
    private ArrayList<Pictogram> mPictogramList;

    protected ArrayList<TicketInformation> mTicketInformationList;

    public GourmetDetail(int index, int entryPosition, String isShowOriginalPrice, int listCount)
    {
        this.index = index;
        this.entryPosition = entryPosition;
        this.isShowOriginalPrice = isShowOriginalPrice;
        this.listCount = listCount;
    }

    @Override
    public void setData(JSONObject jsonObject) throws Exception
    {
        grade = Gourmet.Grade.gourmet;

        category = jsonObject.getString("category");

        subCategory = jsonObject.getString("categorySub");

        name = jsonObject.getString("restaurantName");
        address = jsonObject.getString("address");

        longitude = jsonObject.getDouble("longitude");
        latitude = jsonObject.getDouble("latitude");

        boolean ratingShow = jsonObject.getBoolean("ratingShow");

        if (ratingShow == true)
        {
            ratingValue = jsonObject.getInt("ratingValue");
            ratingPersons = jsonObject.getInt("ratingPersons");
        }

        // TODO : pictogram
        mPictogramList = new ArrayList<>();

        // 주차가능
        if (jsonObject.getBoolean("parking") == true)
        {
            mPictogramList.add(Pictogram.parking);
        }
        // 발렛가능
        if (jsonObject.getBoolean("valet") == true)
        {
            mPictogramList.add(Pictogram.valet);
        }
        // 프라이빗룸
        if (jsonObject.getBoolean("privateRoom") == true)
        {
            mPictogramList.add(Pictogram.privateRoom);
        }
        // 단체예약
        if (jsonObject.getBoolean("groupBooking") == true)
        {
            mPictogramList.add(Pictogram.groupBooking);
        }
        // 베이비시트
        if (jsonObject.getBoolean("babySeat") == true)
        {
            mPictogramList.add(Pictogram.babySeat);
        }
        // 코르키지
        if (jsonObject.getBoolean("corkage") == true)
        {
            mPictogramList.add(Pictogram.corkage);
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

                    String description = imageInformationJSONObject.getString("description");
                    String imageFullUrl = imageUrl + key + imageInformationJSONObject.getString("name");
                    mImageInformationList.add(new ImageInformation(imageFullUrl, description));
                }
                break;
            } catch (JSONException e)
            {
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

        // Ticket Information
        JSONArray ticketInformationJSONArray = jsonObject.getJSONArray("restaurantTickets");
        int ticketInformationLength = ticketInformationJSONArray.length();

        mTicketInformationList = new ArrayList<>(ticketInformationLength);

        for (int i = 0; i < ticketInformationLength; i++)
        {
            mTicketInformationList.add(new TicketInformation(name, ticketInformationJSONArray.getJSONObject(i)));
        }
    }

    public ArrayList<TicketInformation> getTicketInformation()
    {
        return mTicketInformationList;
    }

    public ArrayList<Pictogram> getPictogramList()
    {
        return mPictogramList;
    }

    public enum Pictogram
    {
        parking(R.string.label_parking, R.drawable.ic_detail_facilities_01_parking),
        valet(R.string.label_valet_available, R.drawable.ic_detail_facilities_06_valet),
        privateRoom(R.string.label_private_room, R.drawable.ic_detail_facilities_07_private),
        groupBooking(R.string.label_group_booking, R.drawable.ic_detail_facilities_08_group),
        babySeat(R.string.label_baby_seat, R.drawable.ic_detail_facilities_09_babyseat),
        corkage(R.string.label_corkage, R.drawable.ic_detail_facilities_10_corkage),
        none(0, 0);

        private int nameResId;
        private int imageResId;

        Pictogram(int nameResId, int imageResId)
        {
            this.nameResId = nameResId;
            this.imageResId = imageResId;
        }

        public String getName(Context context)
        {
            if (nameResId <= 0)
            {
                return "";
            }
            return context.getString(nameResId);
        }

        public int getImageResId()
        {
            return imageResId;
        }
    }
}
