package com.twoheart.dailyhotel.model;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.clustering.ClusterManager;

public class StayClusterRenderer extends PlaceClusterRenderer
{
    public StayClusterRenderer(Context context, GoogleMap map, ClusterManager<PlaceClusterItem> clusterManager)
    {
        super(context, map, clusterManager);
    }

    @Override
    protected PlaceRenderer newInstancePlaceRenderer(Context context, Place place)
    {
        Stay stay = (Stay) place;

        return new PlaceRenderer(context, stay.averageDiscountPrice, stay.getGrade().getMarkerResId());
    }
}
