package com.daily.dailyhotel.screen.home.search.gourmet.suggest;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daily.base.util.DailyTextUtils;
import com.daily.dailyhotel.entity.GourmetSuggest;
import com.daily.dailyhotel.entity.ObjectItem;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.databinding.ListRowSearchSuggestTypeDeleteDataBinding;
import com.twoheart.dailyhotel.databinding.ListRowSearchSuggestTypeEntryDataBinding;
import com.twoheart.dailyhotel.databinding.ListRowSearchSuggestTypeNearbyDataBinding;
import com.twoheart.dailyhotel.databinding.ListRowSearchSuggestTypeSectionDataBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by android_sam on 2018. 2. 1..
 */

public class RecentlySuggestListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    public interface OnRecentlySuggestListener
    {
        void onItemClick(int position, GourmetSuggest gourmetSuggest);

        void onDeleteClick(int position, GourmetSuggest gourmetSuggest);

        void onDeleteAllClick();

        void onNearbyClick(GourmetSuggest gourmetSuggest);
    }

    private Context mContext;
    private OnRecentlySuggestListener mListener;

    private List<ObjectItem> mSuggestList;

    public RecentlySuggestListAdapter(Context context, OnRecentlySuggestListener listener)
    {
        mContext = context;
        mListener = listener;

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

            case ObjectItem.TYPE_SECTION:
            {
                ListRowSearchSuggestTypeSectionDataBinding dataBinding //
                    = DataBindingUtil.inflate(LayoutInflater.from(mContext) //
                    , R.layout.list_row_search_suggest_type_section_data, parent, false);

                SectionViewHolder sectionViewHolder = new SectionViewHolder(dataBinding);

                return sectionViewHolder;
            }

            case ObjectItem.TYPE_FOOTER_VIEW:
            {
                ListRowSearchSuggestTypeDeleteDataBinding dataBinding //
                    = DataBindingUtil.inflate(LayoutInflater.from(mContext) //
                    , R.layout.list_row_search_suggest_type_delete_data, parent, false);

                FooterViewHolder footerViewHolder = new FooterViewHolder(dataBinding);

                return footerViewHolder;
            }

            case ObjectItem.TYPE_ENTRY:
            {
                ListRowSearchSuggestTypeEntryDataBinding dataBinding //
                    = DataBindingUtil.inflate(LayoutInflater.from(mContext) //
                    , R.layout.list_row_search_suggest_type_entry_data, parent, false);

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

            case ObjectItem.TYPE_SECTION:
                onBindViewHolder((SectionViewHolder) holder, item, position);
                break;

            case ObjectItem.TYPE_FOOTER_VIEW:
                onBindViewHolder((FooterViewHolder) holder);
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

    public int getEntryCount()
    {
        if (mSuggestList == null || mSuggestList.size() == 0)
        {
            return 0;
        }

        int count = 0;
        for (ObjectItem item : mSuggestList)
        {
            if (ObjectItem.TYPE_ENTRY == item.mType)
            {
                count++;
            }
        }

        return count;
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

    public GourmetSuggest removeItem(int position)
    {
        if (mSuggestList == null || mSuggestList.size() == 0)
        {
            return null;
        }

        if (position < 0 || position > mSuggestList.size() - 1)
        {
            return null;
        }

        ObjectItem removeItem = mSuggestList.remove(position);

        if (mSuggestList.size() == 1)
        {
            ObjectItem checkItem = mSuggestList.get(0);
            if (checkItem.mType == ObjectItem.TYPE_FOOTER_VIEW)
            {
                mSuggestList.remove(0);
            }
        }

        GourmetSuggest gourmetSuggest = removeItem.getItem();
        return gourmetSuggest;
    }

    public void removeSection(int menuType)
    {
        if (mSuggestList == null || mSuggestList.size() == 0)
        {
            return;
        }

        for (int i = 0; i < mSuggestList.size(); i++)
        {
            ObjectItem item = mSuggestList.get(i);
            if (ObjectItem.TYPE_SECTION != item.mType)
            {
                continue;
            }

            GourmetSuggest gourmetSuggest = item.getItem();
            if (gourmetSuggest == null || menuType != gourmetSuggest.menuType)
            {
                continue;
            }

            mSuggestList.remove(i);
        }

        if (mSuggestList.size() == 1)
        {
            ObjectItem checkItem = mSuggestList.get(0);
            if (checkItem.mType == ObjectItem.TYPE_FOOTER_VIEW)
            {
                mSuggestList.remove(0);
            }
        }
    }

    public void setNearByGourmetSuggest(GourmetSuggest nearByGourmetSuggest)
    {
        if (mSuggestList == null || mSuggestList.size() == 0 || nearByGourmetSuggest == null)
        {
            return;
        }

        String descriptionText = mContext.getString(R.string.label_search_nearby_description);

        for (ObjectItem item : mSuggestList)
        {
            if (ObjectItem.TYPE_LOCATION_VIEW == item.mType)
            {
                GourmetSuggest gourmetSuggest = item.getItem();

                gourmetSuggest.displayName = nearByGourmetSuggest != null ? nearByGourmetSuggest.displayName : descriptionText;
                gourmetSuggest.latitude = nearByGourmetSuggest.latitude;
                gourmetSuggest.longitude = nearByGourmetSuggest.longitude;
                gourmetSuggest.categoryKey = nearByGourmetSuggest.categoryKey;
                gourmetSuggest.menuType = nearByGourmetSuggest.menuType;
                break;
            }
        }
    }

    private void onBindViewHolder(LocationViewHolder holder, ObjectItem item)
    {
        GourmetSuggest gourmetSuggest = item.getItem();

        holder.itemView.getRootView().setTag(gourmetSuggest);
        holder.itemView.getRootView().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (mListener == null)
                {
                    return;
                }

                mListener.onNearbyClick(gourmetSuggest);
            }
        });

        holder.dataBinding.descriptionTextView.setText(gourmetSuggest.displayName);

        if (DailyTextUtils.isTextEmpty(gourmetSuggest.displayName) == true)
        {
            holder.dataBinding.descriptionTextView.setVisibility(View.GONE);
        } else
        {
            holder.dataBinding.descriptionTextView.setVisibility(View.VISIBLE);
        }
    }

    private void onBindViewHolder(SectionViewHolder holder, ObjectItem item, int position)
    {
        GourmetSuggest gourmetSuggest = item.getItem();

        if (DailyTextUtils.isTextEmpty(gourmetSuggest.displayName) == true)
        {
            holder.dataBinding.titleTextView.setVisibility(View.GONE);
        } else
        {
            holder.dataBinding.titleTextView.setVisibility(View.VISIBLE);
        }

        holder.dataBinding.titleTextView.setText(gourmetSuggest.displayName);
    }

    private void onBindViewHolder(FooterViewHolder holder)
    {
        int count = getEntryCount();
        if (count >= 2)
        {
            holder.dataBinding.deleteLayout.setVisibility(View.VISIBLE);
            holder.dataBinding.deleteTextView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if (mListener == null)
                    {
                        return;
                    }

                    mListener.onDeleteAllClick();
                }
            });
        } else
        {
            holder.dataBinding.deleteLayout.setVisibility(View.GONE);
            holder.dataBinding.deleteTextView.setOnClickListener(null);
        }
    }

    private void onBindViewHolder(EntryViewHolder holder, ObjectItem item, int position)
    {
        GourmetSuggest gourmetSuggest = item.getItem();

        holder.itemView.getRootView().setTag(gourmetSuggest);
        holder.itemView.getRootView().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (mListener == null)
                {
                    return;
                }

                mListener.onItemClick(position, gourmetSuggest);
            }
        });

        holder.dataBinding.titleTextView.setText(gourmetSuggest.displayName);

        holder.dataBinding.descriptionTextView.setVisibility(View.GONE);
        holder.dataBinding.priceTextView.setVisibility(View.GONE);
        holder.dataBinding.bottomDivider.setVisibility(View.GONE);
        holder.dataBinding.deleteImageView.setVisibility(View.VISIBLE);

        holder.dataBinding.deleteImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (mListener == null)
                {
                    return;
                }

                mListener.onDeleteClick(position, gourmetSuggest);
            }
        });

        switch (gourmetSuggest.categoryKey)
        {
            case GourmetSuggest.CATEGORY_GOURMET:
                holder.dataBinding.iconImageView.setVectorImageResource(R.drawable.vector_search_ic_02_hotel);
                break;

            case GourmetSuggest.CATEGORY_LOCATION:
                holder.dataBinding.iconImageView.setVectorImageResource(R.drawable.vector_search_ic_09_nearby);
                break;

            case GourmetSuggest.CATEGORY_REGION:
                holder.dataBinding.iconImageView.setVectorImageResource(R.drawable.vector_search_ic_01_region);
                break;

            default:
                holder.dataBinding.iconImageView.setVectorImageResource(R.drawable.vector_search_ic_07_recent);
                break;
        }
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

    class FooterViewHolder extends RecyclerView.ViewHolder
    {
        ListRowSearchSuggestTypeDeleteDataBinding dataBinding;

        public FooterViewHolder(ListRowSearchSuggestTypeDeleteDataBinding dataBinding)
        {
            super(dataBinding.getRoot());

            this.dataBinding = dataBinding;
        }
    }

    class SectionViewHolder extends RecyclerView.ViewHolder
    {
        ListRowSearchSuggestTypeSectionDataBinding dataBinding;

        public SectionViewHolder(ListRowSearchSuggestTypeSectionDataBinding dataBinding)
        {
            super(dataBinding.getRoot());

            this.dataBinding = dataBinding;
        }
    }

    class EntryViewHolder extends RecyclerView.ViewHolder
    {
        ListRowSearchSuggestTypeEntryDataBinding dataBinding;

        public EntryViewHolder(ListRowSearchSuggestTypeEntryDataBinding dataBinding)
        {
            super(dataBinding.getRoot());

            this.dataBinding = dataBinding;
        }
    }
}
