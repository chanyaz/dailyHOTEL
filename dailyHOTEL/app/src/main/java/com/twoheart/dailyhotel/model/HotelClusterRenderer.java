package com.twoheart.dailyhotel.model;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class HotelClusterRenderer extends DefaultClusterRenderer<HotelClusterItem>
{
    private Context mContext;
    private HotelClusterItem mSelectedHotelClusterItem;
    private OnSelectedClusterItemListener mOnSelectedClusterItemListener;
    private OnClusterRenderedListener mOnClusterRenderedListener;

    public HotelClusterRenderer(Context context, GoogleMap map, ClusterManager<HotelClusterItem> clusterManager)
    {
        super(context, map, clusterManager);

        mContext = context;
    }

    @Override
    protected void onBeforeClusterItemRendered(HotelClusterItem item, MarkerOptions markerOptions)
    {
        if (mOnClusterRenderedListener != null)
        {
            mOnClusterRenderedListener.onClusterRenderedListener(Renderer.CLUSTER_ITEM);
        }

        HotelRenderer hotelRenderer = new HotelRenderer(mContext, item.getHotel());

        BitmapDescriptor icon = hotelRenderer.getBitmap(false);

        if (icon != null)
        {
            markerOptions.icon(icon);
            markerOptions.anchor(0.0f, 1.0f);
        }
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<HotelClusterItem> cluster, MarkerOptions markerOptions)
    {
        if (mOnClusterRenderedListener != null)
        {
            mOnClusterRenderedListener.onClusterRenderedListener(Renderer.CLUSTER);
        }

        HotelRegionRenderer hotelRegionRenderer = new HotelRegionRenderer(mContext, cluster.getSize());

        BitmapDescriptor icon = hotelRegionRenderer.getBitmap();

        if (icon != null)
        {
            markerOptions.icon(icon).anchor(0.5f, 0.5f);
        }
    }

    @Override
    protected void onClusterItemRendered(HotelClusterItem clusterItem, Marker marker)
    {
        if (mSelectedHotelClusterItem != null)
        {
            LatLng selectedLatLng = mSelectedHotelClusterItem.getPosition();
            LatLng currentLatLng = clusterItem.getPosition();

            if (selectedLatLng.latitude == currentLatLng.latitude && selectedLatLng.longitude == currentLatLng.longitude)
            {
                mSelectedHotelClusterItem = null;

                if (mOnSelectedClusterItemListener != null)
                {
                    mOnSelectedClusterItemListener.onSelectedClusterItemListener(marker);
                }
            }
        }
    }

    public void setOnClusterRenderedListener(OnClusterRenderedListener listener)
    {
        mOnClusterRenderedListener = listener;
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<HotelClusterItem> cluster, float zoom)
    {
        return (Float.compare(zoom, 13.0f) < 0);
    }

    public void setSelectedClusterItem(HotelClusterItem hotelClusterItem)
    {
        mSelectedHotelClusterItem = hotelClusterItem;
    }

    public void setSelectedClusterItemListener(OnSelectedClusterItemListener listener)
    {
        mOnSelectedClusterItemListener = listener;
    }

    public enum Renderer
    {
        CLUSTER,
        CLUSTER_ITEM,
    }

    public interface OnSelectedClusterItemListener
    {
        void onSelectedClusterItemListener(Marker marker);
    }

    public interface OnClusterRenderedListener
    {
        void onClusterRenderedListener(Renderer renderer);
    }
}
