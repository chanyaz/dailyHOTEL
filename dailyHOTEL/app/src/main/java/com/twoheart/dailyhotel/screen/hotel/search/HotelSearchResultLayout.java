package com.twoheart.dailyhotel.screen.hotel.search;

import android.content.Context;
import android.view.View;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.PlaceViewItem;
import com.twoheart.dailyhotel.place.adapter.PlaceListAdapter;
import com.twoheart.dailyhotel.place.base.OnBaseEventListener;
import com.twoheart.dailyhotel.place.layout.PlaceSearchResultLayout;

import java.util.ArrayList;

public class HotelSearchResultLayout extends PlaceSearchResultLayout
{
    private HotelSearchResultListAdapter mListAdapter;

    @Override
    protected PlaceListAdapter getListAdapter()
    {
        mListAdapter = new HotelSearchResultListAdapter(mContext, new ArrayList<PlaceViewItem>(), mOnItemClickListener);

        return mListAdapter;
    }

    public HotelSearchResultLayout(Context context, OnBaseEventListener listener)
    {
        super(context, listener);
    }

    public void setSearchResultList(ArrayList<PlaceViewItem> placeViewItemList)
    {
        mListAdapter.clear();

        if (placeViewItemList == null || placeViewItemList.size() == 0)
        {
            showEmptyLayout();
            mListAdapter.notifyDataSetChanged();
        } else
        {
            showListLayout();

            mListAdapter.addAll(placeViewItemList);
            mListAdapter.notifyDataSetChanged();

            updateResultCount(placeViewItemList.size());
        }
    }

    private View.OnClickListener mOnItemClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            int position = mRecyclerView.getChildAdapterPosition(v);

            if (position < 0)
            {
                return;
            }

            PlaceViewItem placeViewItem = mListAdapter.getItem(position);

            if (placeViewItem.getType() != PlaceViewItem.TYPE_ENTRY)
            {
                return;
            }

            ((PlaceSearchResultLayout.OnEventListener) mOnEventListener).onItemClick(placeViewItem);
        }
    };
}
