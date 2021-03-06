package com.daily.dailyhotel.repository.remote.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.daily.base.util.DailyTextUtils;
import com.daily.dailyhotel.entity.RecentlyPlace;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by android_sam on 2017. 9. 4..
 */
@JsonObject
public class RecentlyPlacesData
{
    @JsonField(name = "items")
    public List<RecentlyPlaceData> items;

    @JsonField(name = "imgUrl")
    public String imageBaseUrl;

    public List<RecentlyPlace> getRecentlyPlaceList()
    {
        List<RecentlyPlace> placeList = new ArrayList<>();

        if (items == null || items.size() == 0)
        {
            return placeList;
        }

        for (RecentlyPlaceData placeData : items)
        {
            RecentlyPlace recentlyPlace = placeData.getRecentlyPlace();
            if (DailyTextUtils.isTextEmpty(recentlyPlace.imageUrl) == false)
            {
                recentlyPlace.imageUrl = imageBaseUrl + recentlyPlace.imageUrl;
            }

            placeList.add(recentlyPlace);
        }

        return placeList;
    }
}