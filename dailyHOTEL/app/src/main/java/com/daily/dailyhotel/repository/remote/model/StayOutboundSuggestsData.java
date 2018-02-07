package com.daily.dailyhotel.repository.remote.model;

import android.content.Context;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.daily.base.util.DailyTextUtils;
import com.daily.dailyhotel.entity.StayOutboundSuggest;
import com.twoheart.dailyhotel.R;

import java.util.ArrayList;
import java.util.List;

@JsonObject
public class StayOutboundSuggestsData
{
    @JsonField(name = "station")
    public List<StayOutboundSuggestData> stationSuggestDataList;

    @JsonField(name = "hotel")
    public List<StayOutboundSuggestData> hotelSuggestDataList;

    @JsonField(name = "region")
    public List<StayOutboundSuggestData> regionSuggestDataList;

    @JsonField(name = "point")
    public List<StayOutboundSuggestData> pointSuggestDataList;

    @JsonField(name = "airport")
    public List<StayOutboundSuggestData> airportSuggestDataList;

    public StayOutboundSuggestsData()
    {

    }

    public List<StayOutboundSuggest> getSuggestList(Context context)
    {
        final int SUGGEST_MAX_COUNT = 5;

        List<StayOutboundSuggest> list = new ArrayList<>();

        if (context == null)
        {
            return list;
        }

        List<StayOutboundSuggest> regionList = getSuggestList(context.getString(R.string.label_stay_outbound_suggest_region), regionSuggestDataList, SUGGEST_MAX_COUNT);

        if (regionList != null)
        {
            list.addAll(regionList);
        }

        List<StayOutboundSuggest> hotelList = getSuggestList(context.getString(R.string.label_stay_outbound_suggest_hotel), hotelSuggestDataList, SUGGEST_MAX_COUNT);

        if (hotelList != null)
        {
            list.addAll(hotelList);
        }

        List<StayOutboundSuggest> pointList = getSuggestList(context.getString(R.string.label_stay_outbound_suggest_point), pointSuggestDataList, SUGGEST_MAX_COUNT);

        if (pointList != null)
        {
            list.addAll(pointList);
        }

        List<StayOutboundSuggest> airportList = getSuggestList(context.getString(R.string.label_stay_outbound_suggest_airport), airportSuggestDataList, SUGGEST_MAX_COUNT);

        if (airportList != null)
        {
            list.addAll(airportList);
        }

        List<StayOutboundSuggest> stationList = getSuggestList(context.getString(R.string.label_stay_outbound_suggest_station), stationSuggestDataList, SUGGEST_MAX_COUNT);

        if (stationList != null)
        {
            list.addAll(stationList);
        }

        return list;
    }

    public List<StayOutboundSuggest> getRegionSuggestList(Context context)
    {
        List<StayOutboundSuggest> regionStayOutboundSuggestList = new ArrayList<>();

        if (context == null)
        {
            return regionStayOutboundSuggestList;
        }

        if (regionSuggestDataList == null || regionSuggestDataList.size() == 0)
        {
            return regionStayOutboundSuggestList;
        }

        for (StayOutboundSuggestData stayOutboundSuggestData : regionSuggestDataList)
        {
            StayOutboundSuggest stayOutboundSuggest = stayOutboundSuggestData.getSuggests();
            stayOutboundSuggest.menuType = StayOutboundSuggest.MENU_TYPE_POPULAR_AREA;
            regionStayOutboundSuggestList.add(stayOutboundSuggest);
        }

        return regionStayOutboundSuggestList;
    }

    private List<StayOutboundSuggest> getSuggestList(String title, List<StayOutboundSuggestData> stayOutboundSuggestDataList, int maxCount)
    {
        if (stayOutboundSuggestDataList == null || stayOutboundSuggestDataList.size() == 0 || DailyTextUtils.isTextEmpty(title) == true)
        {
            return null;
        }

        List<StayOutboundSuggest> list = new ArrayList<>();

        list.add(new StayOutboundSuggest(0, title));

        int size = Math.min(stayOutboundSuggestDataList.size(), maxCount);

        for (int i = 0; i < size; i++)
        {
            list.add(stayOutboundSuggestDataList.get(i).getSuggests());
        }

        return list;
    }
}
