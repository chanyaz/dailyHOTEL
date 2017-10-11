package com.daily.dailyhotel.screen.home.stay.inbound.detail;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daily.dailyhotel.entity.StayRoom;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.databinding.LayoutStayOutboundDetailRoomDataBinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StayDetailRoomListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private Context mContext;
    private List<StayRoom> mStayRoomList;
    View.OnClickListener mOnClickListener;
    private int mSelectedPosition;
    private StayDetailPresenter.PriceType mPriceType;

    public StayDetailRoomListAdapter(Context context, List<StayRoom> arrayList, View.OnClickListener listener)
    {
        mContext = context;
        mOnClickListener = listener;

        addAll(arrayList);
        mPriceType = StayDetailPresenter.PriceType.AVERAGE;
    }

    public void addAll(Collection<? extends StayRoom> collection)
    {
        if (collection == null || collection.size() == 0)
        {
            return;
        }

        if (mStayRoomList == null)
        {
            mStayRoomList = new ArrayList<>(collection.size());
        }

        mStayRoomList.clear();
        mStayRoomList.addAll(collection);
    }

    public void setSelected(int position)
    {
        mSelectedPosition = position;
    }

    public StayRoom getItem(int position)
    {
        if (mStayRoomList.size() <= position)
        {
            return null;
        }

        return mStayRoomList.get(position);
    }

    public void setPriceType(StayDetailPresenter.PriceType priceType)
    {
        mPriceType = priceType;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutStayOutboundDetailRoomDataBinding dataBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.layout_stay_outbound_detail_room_data, parent, false);

        SaleRoomInformationViewHolder viewHolder = new SaleRoomInformationViewHolder(dataBinding);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        StayRoom stayRoom = getItem(position);

        if (stayRoom == null)
        {
            return;
        }

        SaleRoomInformationViewHolder saleRoomInformationViewHolder = (SaleRoomInformationViewHolder) holder;

        saleRoomInformationViewHolder.dataBinding.getRoot().setTag(position);

        if (mSelectedPosition == position)
        {
            saleRoomInformationViewHolder.dataBinding.getRoot().setSelected(true);
        } else
        {
            saleRoomInformationViewHolder.dataBinding.getRoot().setSelected(false);
        }

        saleRoomInformationViewHolder.dataBinding.roomTypeTextView.setText(stayRoom.name);

        String price, discountPrice;

        //        switch (mPriceType)
        //        {
        //            case TOTAL:
        //            {
        //                if (stayOutboundRoom.promotion == true)
        //                {
        //                    price = DailyTextUtils.getPriceFormat(mContext, stayOutboundRoom.base, false);
        //                } else
        //                {
        //                    price = null;
        //                }
        //
        //                discountPrice = DailyTextUtils.getPriceFormat(mContext, stayOutboundRoom.total, false);
        //                break;
        //            }
        //
        //            case AVERAGE:
        //            default:
        //            {
        //                if (stayOutboundRoom.promotion == true)
        //                {
        //                    price = DailyTextUtils.getPriceFormat(mContext, stayOutboundRoom.baseNightly, false);
        //                } else
        //                {
        //                    price = null;
        //                }
        //
        //                discountPrice = DailyTextUtils.getPriceFormat(mContext, stayOutboundRoom.nightly, false);
        //                break;
        //            }
        //        }
        //
        //        if (DailyTextUtils.isTextEmpty(price) == true)
        //        {
        //            saleRoomInformationViewHolder.dataBinding.priceTextView.setVisibility(View.GONE);
        //            saleRoomInformationViewHolder.dataBinding.priceTextView.setText(null);
        //        } else
        //        {
        //            saleRoomInformationViewHolder.dataBinding.priceTextView.setVisibility(View.VISIBLE);
        //            saleRoomInformationViewHolder.dataBinding.priceTextView.setPaintFlags(saleRoomInformationViewHolder.dataBinding.priceTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        //            saleRoomInformationViewHolder.dataBinding.priceTextView.setText(price);
        //        }
        //
        //        saleRoomInformationViewHolder.dataBinding.discountPriceTextView.setText(discountPrice);
        //
        //        String personOption;
        //
        //        if (stayOutboundRoom.quotedOccupancy == 0 || stayOutboundRoom.rateOccupancyPerRoom == 0)
        //        {
        //            personOption = null;
        //        } else if (stayOutboundRoom.quotedOccupancy == stayOutboundRoom.rateOccupancyPerRoom)
        //        {
        //            personOption = mContext.getString(R.string.label_stay_outbound_room_default_person, stayOutboundRoom.quotedOccupancy)//
        //                + "/" + mContext.getString(R.string.label_stay_outbound_room_max_person_free, stayOutboundRoom.rateOccupancyPerRoom);
        //        } else
        //        {
        //            personOption = mContext.getString(R.string.label_stay_outbound_room_default_person, stayOutboundRoom.quotedOccupancy)//
        //                + "/" + mContext.getString(R.string.label_stay_outbound_room_max_person_charge, stayOutboundRoom.rateOccupancyPerRoom);
        //        }
        //
        //        if (DailyTextUtils.isTextEmpty(personOption) == true)
        //        {
        //            saleRoomInformationViewHolder.dataBinding.optionTextView.setVisibility(View.GONE);
        //        } else
        //        {
        //            saleRoomInformationViewHolder.dataBinding.optionTextView.setVisibility(View.VISIBLE);
        //            saleRoomInformationViewHolder.dataBinding.optionTextView.setText(personOption);
        //        }
        //
        //        if (DailyTextUtils.isTextEmpty(stayOutboundRoom.valueAddName) == true)
        //        {
        //            saleRoomInformationViewHolder.dataBinding.amenitiesTextView.setVisibility(View.GONE);
        //        } else
        //        {
        //            saleRoomInformationViewHolder.dataBinding.amenitiesTextView.setVisibility(View.VISIBLE);
        //            saleRoomInformationViewHolder.dataBinding.amenitiesTextView.setText(stayOutboundRoom.valueAddName);
        //        }
        //
        //        if (DailyTextUtils.isTextEmpty(stayOutboundRoom.promotionDescription) == true)
        //        {
        //            saleRoomInformationViewHolder.dataBinding.benefitTextView.setVisibility(View.GONE);
        //        } else
        //        {
        //            saleRoomInformationViewHolder.dataBinding.benefitTextView.setVisibility(View.VISIBLE);
        //            saleRoomInformationViewHolder.dataBinding.benefitTextView.setText(stayOutboundRoom.promotionDescription);
        //        }
        //
        //        if (stayOutboundRoom.nonRefundable == false)
        //        {
        //            saleRoomInformationViewHolder.dataBinding.nrdTextView.setVisibility(View.GONE);
        //        } else
        //        {
        //            saleRoomInformationViewHolder.dataBinding.nrdTextView.setVisibility(View.VISIBLE);
        //            saleRoomInformationViewHolder.dataBinding.nrdTextView.setText(stayOutboundRoom.nonRefundableDescription);
        //        }
    }

    @Override
    public int getItemCount()
    {
        if (mStayRoomList == null)
        {
            return 0;
        }

        return mStayRoomList.size();
    }

    public class SaleRoomInformationViewHolder extends RecyclerView.ViewHolder
    {
        LayoutStayOutboundDetailRoomDataBinding dataBinding;

        public SaleRoomInformationViewHolder(LayoutStayOutboundDetailRoomDataBinding dataBinding)
        {
            super(dataBinding.getRoot());

            this.dataBinding = dataBinding;

            dataBinding.getRoot().setOnClickListener(mOnClickListener);
        }
    }
}
