package com.twoheart.dailyhotel.screen.information.wishlist;

import android.content.Context;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Gourmet;
import com.twoheart.dailyhotel.model.Place;
import com.twoheart.dailyhotel.model.PlaceViewItem;
import com.twoheart.dailyhotel.place.base.OnBaseEventListener;

import java.util.ArrayList;

/**
 * Created by android_sam on 2016. 11. 1..
 */

public class GourmetWishListLayout extends PlaceWishListLayout
{
    public GourmetWishListLayout(Context context, OnBaseEventListener listener)
    {
        super(context, listener);
    }

    @Override
    protected PlaceWishListAdapter getWishListAdapter(Context context, ArrayList<PlaceViewItem> list, PlaceWishListAdapter.OnPlaceWishListItemListener listener)
    {
        return new GourmetWishListAdapter(context, list, listener);
    }

    @Override
    protected int getEmptyMessageResId()
    {
        return R.string.wishlist_list_empty_message02_gourmet;
    }

    @Override
    protected int getEmptyButtonTextResId()
    {
        return R.string.wishlist_list_empty_button_message_gourmet;
    }

    @Override
    protected ArrayList<PlaceViewItem> makePlaceViewItemList(ArrayList<? extends Place> list)
    {
        if (list == null || list.size() == 0)
        {
            return new ArrayList<>();
        }

        ArrayList<Gourmet> gourmetList = (ArrayList<Gourmet>) list;
        ArrayList<PlaceViewItem> placeViewItems = new ArrayList<>();
        for (Gourmet gourmet : gourmetList)
        {
            placeViewItems.add(new PlaceViewItem(PlaceViewItem.TYPE_ENTRY, gourmet));
        }

        placeViewItems.add(new PlaceViewItem(PlaceViewItem.TYPE_FOOTER_VIEW, null));

        return placeViewItems;
    }
}
