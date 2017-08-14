package com.twoheart.dailyhotel.screen.search;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.databinding.ListRowSearchCardItemBinding;
import com.twoheart.dailyhotel.model.SearchCardItem;

import java.util.ArrayList;

/**
 * Created by iseung-won on 2017. 8. 10..
 */

public class SearchCardItemListAdapter extends RecyclerView.Adapter<SearchCardItemListAdapter.ItemViewHolder>
{
    private Context mContext;
    private LayoutInflater mInflater;
    private View.OnClickListener mOnClickListener;
    private ArrayList<SearchCardItem> mItemList;

    public SearchCardItemListAdapter(Context context, ArrayList<SearchCardItem> list, View.OnClickListener onClickListener)
    {
        mContext = context;
        mOnClickListener = onClickListener;
        setData(list);

        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        ListRowSearchCardItemBinding dataBinding = DataBindingUtil.inflate(mInflater, R.layout.list_row_search_card_item, parent, false);
        return new ItemViewHolder(dataBinding);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position)
    {
        SearchCardItem item = getItem(position);

        holder.itemView.setTag(item);

        holder.dataBinding.iconImageView.setImageResource(item.iconResId);
        holder.dataBinding.itemTextView.setText(item.itemText);
    }

    public void setData(ArrayList<SearchCardItem> list)
    {
        if (mItemList == null)
        {
            mItemList = new ArrayList<>();
        }

        clear();

        if (list == null || list.size() == 0)
        {
            return;
        }

        mItemList.addAll(list);
    }

    public void clear()
    {
        if (mItemList == null)
        {
            return;
        }

        mItemList.clear();
    }

    public SearchCardItem getItem(int position)
    {
        if (position < 0 || mItemList.size() <= position)
        {
            return null;
        }

        return mItemList.get(position);
    }

    @Override
    public int getItemCount()
    {
        return mItemList == null ? 0 : mItemList.size();
    }

    protected class ItemViewHolder extends RecyclerView.ViewHolder
    {
        public ListRowSearchCardItemBinding dataBinding;

        public ItemViewHolder(ListRowSearchCardItemBinding dataBinding)
        {
            super(dataBinding.getRoot());

            this.dataBinding = dataBinding;

            itemView.setOnClickListener(mOnClickListener);
        }
    }
}