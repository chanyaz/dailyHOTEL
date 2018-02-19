package com.daily.dailyhotel.screen.home.search.stay.inbound.suggest;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daily.base.util.DailyTextUtils;
import com.daily.dailyhotel.entity.ObjectItem;
import com.daily.dailyhotel.entity.StaySuggest;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.databinding.ListRowSearchSuggestTypeNearbyDataBinding;
import com.twoheart.dailyhotel.databinding.ListRowSearchSuggestTypeRecommendDataBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by android_sam on 2018. 2. 2..
 */

public class StayPopularSuggestListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    public interface OnPopularSuggestListener
    {
        void onNearbyClick(StaySuggest staySuggest);
    }

    private Context mContext;
    OnPopularSuggestListener mListener;

    private List<ObjectItem> mSuggestList;

    public StayPopularSuggestListAdapter(Context context, OnPopularSuggestListener listener)
    {
        mContext = context;
        this.mListener = listener;

        setAll(null);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        switch (viewType)
        {
            case ObjectItem.TYPE_LOCATION_VIEW:
            {
                ListRowSearchSuggestTypeNearbyDataBinding dataBinding //
                    = DataBindingUtil.inflate(LayoutInflater.from(mContext) //
                    , R.layout.list_row_search_suggest_type_nearby_data, parent, false);

                LocationViewHolder locationViewHolder = new LocationViewHolder(dataBinding);

                return locationViewHolder;
            }

            case ObjectItem.TYPE_ENTRY:
            {
                ListRowSearchSuggestTypeRecommendDataBinding dataBinding //
                    = DataBindingUtil.inflate(LayoutInflater.from(mContext) //
                    , R.layout.list_row_search_suggest_type_recommend_data, parent, false);

                EntryViewHolder entryViewHolder = new EntryViewHolder(dataBinding);

                return entryViewHolder;
            }
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        ObjectItem item = getItem(position);

        if (item == null)
        {
            return;
        }

        switch (item.mType)
        {
            case ObjectItem.TYPE_LOCATION_VIEW:
                onBindViewHolder((LocationViewHolder) holder, item);
                break;

            case ObjectItem.TYPE_ENTRY:
                onBindViewHolder((EntryViewHolder) holder, item, position);
                break;
        }
    }

    @Override
    public int getItemCount()
    {
        if (mSuggestList == null)
        {
            return 0;
        } else
        {
            return mSuggestList.size();
        }
    }

    @Override
    public int getItemViewType(int position)
    {
        return mSuggestList.get(position).mType;
    }

    public void setAll(List<ObjectItem> objectItemList)
    {
        if (mSuggestList == null)
        {
            mSuggestList = new ArrayList<>();
        }

        mSuggestList.clear();

        if (objectItemList != null && objectItemList.size() > 0)
        {
            mSuggestList.addAll(objectItemList);
        }
    }

    public ObjectItem getItem(int position)
    {
        if (position < 0 || mSuggestList.size() <= position)
        {
            return null;
        }

        return mSuggestList.get(position);
    }

    public void setNearByStaySuggest(StaySuggest nearByStaySuggest)
    {
        if (mSuggestList == null || mSuggestList.size() == 0 || nearByStaySuggest == null)
        {
            return;
        }

        String descriptionText = mContext.getString(R.string.label_search_nearby_description);

        for (ObjectItem item : mSuggestList)
        {
            if (ObjectItem.TYPE_LOCATION_VIEW == item.mType)
            {
                StaySuggest staySuggest = item.getItem();

                staySuggest.displayName = nearByStaySuggest != null ? nearByStaySuggest.displayName : descriptionText;
                staySuggest.latitude = nearByStaySuggest.latitude;
                staySuggest.longitude = nearByStaySuggest.longitude;
                staySuggest.categoryKey = nearByStaySuggest.categoryKey;
                staySuggest.menuType = nearByStaySuggest.menuType;
                break;
            }
        }
    }

    private void onBindViewHolder(LocationViewHolder holder, ObjectItem item)
    {
        StaySuggest staySuggest = item.getItem();

        holder.itemView.getRootView().setTag(staySuggest);
        holder.itemView.getRootView().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (mListener == null)
                {
                    return;
                }

                mListener.onNearbyClick(staySuggest);
            }
        });

        holder.dataBinding.bottomDivider.setVisibility(View.VISIBLE);

        holder.dataBinding.descriptionTextView.setText(staySuggest.address);

        if (DailyTextUtils.isTextEmpty(staySuggest.address) == true)
        {
            holder.dataBinding.descriptionTextView.setVisibility(View.GONE);
        } else
        {
            holder.dataBinding.descriptionTextView.setVisibility(View.VISIBLE);
        }
    }

    private void onBindViewHolder(EntryViewHolder holder, ObjectItem item, int position)
    {
        StaySuggest staySuggest = item.getItem();

        holder.itemView.getRootView().setTag(staySuggest);

        holder.dataBinding.descriptionTextView.setText(staySuggest.displayName);
    }

    class LocationViewHolder extends RecyclerView.ViewHolder
    {
        ListRowSearchSuggestTypeNearbyDataBinding dataBinding;

        public LocationViewHolder(ListRowSearchSuggestTypeNearbyDataBinding dataBinding)
        {
            super(dataBinding.getRoot());

            this.dataBinding = dataBinding;
        }
    }

    class EntryViewHolder extends RecyclerView.ViewHolder
    {
        ListRowSearchSuggestTypeRecommendDataBinding dataBinding;

        public EntryViewHolder(ListRowSearchSuggestTypeRecommendDataBinding dataBinding)
        {
            super(dataBinding.getRoot());

            this.dataBinding = dataBinding;
        }
    }
}
