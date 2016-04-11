package com.twoheart.dailyhotel.screen.gourmet.list;

import android.content.Context;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.EventBanner;
import com.twoheart.dailyhotel.model.Gourmet;
import com.twoheart.dailyhotel.model.PlaceViewItem;
import com.twoheart.dailyhotel.place.adapter.PlaceBannerViewPagerAdapter;
import com.twoheart.dailyhotel.place.adapter.PlaceListAdapter;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.widget.DailyLoopViewPager;
import com.twoheart.dailyhotel.widget.DailyViewPagerCircleIndicator;
import com.twoheart.dailyhotel.widget.PinnedSectionRecyclerView;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;

public class GourmetListAdapter extends PlaceListAdapter implements PinnedSectionRecyclerView.PinnedSectionListAdapter
{
    private Constants.SortType mSortType;
    private View.OnClickListener mOnClickListener;
    private View.OnClickListener mOnEventBannerClickListener;
    private int mLastEventBannerPosition;

    private Handler mEventBannerHandler;

    public GourmetListAdapter(Context context, ArrayList<PlaceViewItem> arrayList, View.OnClickListener listener, View.OnClickListener eventBannerListener)
    {
        super(context, arrayList);

        mOnClickListener = listener;
        mOnEventBannerClickListener = eventBannerListener;

        mEventBannerHandler = new EventBannerHandler(this);

        setSortType(Constants.SortType.DEFAULT);
    }

    public void addAll(Collection<? extends PlaceViewItem> collection, Constants.SortType sortType)
    {
        addAll(collection);

        setSortType(sortType);
    }

    public void setSortType(Constants.SortType sortType)
    {
        mSortType = sortType;
    }

    @Override
    public boolean isItemViewTypePinned(int viewType)
    {
        return viewType == PlaceViewItem.TYPE_SECTION;
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder)
    {
        super.onViewRecycled(holder);

        if (holder instanceof EventBannerViewHolder)
        {
            mEventBannerHandler.removeMessages(0);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        switch (viewType)
        {
            case PlaceViewItem.TYPE_SECTION:
            {
                View view = mInflater.inflate(R.layout.list_row_hotel_section, parent, false);

                return new SectionViewHolder(view);
            }

            case PlaceViewItem.TYPE_ENTRY:
            {
                View view = mInflater.inflate(R.layout.list_row_gourmet, parent, false);

                return new GourmetViewHolder(view);
            }

            case PlaceViewItem.TYPE_EVENT_BANNER:
            {
                View view = mInflater.inflate(R.layout.list_row_eventbanner, parent, false);

                return new EventBannerViewHolder(view);
            }
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        PlaceViewItem item = getItem(position);

        if (item == null)
        {
            return;
        }

        switch (item.getType())
        {
            case PlaceViewItem.TYPE_ENTRY:
                onBindViewHolder((GourmetViewHolder) holder, item);
                break;

            case PlaceViewItem.TYPE_SECTION:
                onBindViewHolder((SectionViewHolder) holder, item);
                break;

            case PlaceViewItem.TYPE_EVENT_BANNER:
                onBindViewHolder((EventBannerViewHolder) holder, item);
                break;
        }
    }

    private void onBindViewHolder(SectionViewHolder holder, PlaceViewItem placeViewItem)
    {
        holder.regionDetailName.setText(placeViewItem.<String>getItem());
    }

    private void onBindViewHolder(final EventBannerViewHolder holder, PlaceViewItem placeViewItem)
    {
        ArrayList<EventBanner> eventBannerList = placeViewItem.<ArrayList<EventBanner>>getItem();

        PlaceBannerViewPagerAdapter adapter = new PlaceBannerViewPagerAdapter(mContext, eventBannerList, mOnEventBannerClickListener);
        holder.dailyLoopViewPager.setOnPageChangeListener(null);
        holder.dailyLoopViewPager.setAdapter(adapter);
        holder.viewpagerCircleIndicator.setTotalCount(eventBannerList.size());
        holder.dailyLoopViewPager.setCurrentItem(mLastEventBannerPosition);
        holder.dailyLoopViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
            }

            @Override
            public void onPageSelected(int position)
            {
                mLastEventBannerPosition = position;

                holder.viewpagerCircleIndicator.setPosition(position);

                nextEventBannerPosition(holder, position);
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {
                if (state == DailyLoopViewPager.SCROLL_STATE_DRAGGING)
                {
                    mEventBannerHandler.removeMessages(0);
                }
            }
        });

        nextEventBannerPosition(holder, mLastEventBannerPosition);
    }

    private void nextEventBannerPosition(EventBannerViewHolder eventViewHolder, int position)
    {
        mEventBannerHandler.removeMessages(0);

        Message message = new Message();
        message.what = 0;
        message.arg1 = position;
        message.obj = eventViewHolder;
        mEventBannerHandler.sendMessageDelayed(message, 5000);
    }

    private void onBindViewHolder(GourmetViewHolder holder, PlaceViewItem placeViewItem)
    {
        final Gourmet gourmet = placeViewItem.<Gourmet>getItem();

        DecimalFormat comma = new DecimalFormat("###,##0");

        String strPrice = comma.format(gourmet.price);
        String strDiscount = comma.format(gourmet.discountPrice);

        String address = gourmet.addressSummary;

        if (address.indexOf('|') >= 0)
        {
            address = address.replace(" | ", "ㅣ");
        } else if (address.indexOf('l') >= 0)
        {
            address = address.replace(" l ", "ㅣ");
        }

        holder.addressView.setText(address);
        holder.nameView.setText(gourmet.name);

        // 인원
        if (gourmet.persons > 1)
        {
            holder.personsTextView.setVisibility(View.VISIBLE);
            holder.personsTextView.setText(mContext.getString(R.string.label_persions, gourmet.persons));
        } else
        {
            holder.personsTextView.setVisibility(View.GONE);
        }

        String currency = mContext.getResources().getString(R.string.currency);

        if (gourmet.price <= 0 || gourmet.price <= gourmet.discountPrice)
        {
            holder.priceView.setVisibility(View.INVISIBLE);
            holder.priceView.setText(null);
        } else
        {
            holder.priceView.setVisibility(View.VISIBLE);

            holder.priceView.setText(strPrice + currency);
            holder.priceView.setPaintFlags(holder.priceView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        // 만족도
        if (gourmet.satisfaction > 0)
        {
            holder.satisfactionView.setVisibility(View.VISIBLE);
            holder.satisfactionView.setText(gourmet.satisfaction + "%");
        } else
        {
            holder.satisfactionView.setVisibility(View.GONE);
        }

        holder.discountView.setText(strDiscount + currency);
        holder.nameView.setSelected(true); // Android TextView marquee bug

        if (Util.isOverAPI16() == true)
        {
            holder.gradientView.setBackground(mPaintDrawable);
        } else
        {
            holder.gradientView.setBackgroundDrawable(mPaintDrawable);
        }

        // grade
        if (Util.isTextEmpty(gourmet.category) == true)
        {
            holder.gradeView.setVisibility(View.GONE);
        } else
        {
            holder.gradeView.setVisibility(View.VISIBLE);
            holder.gradeView.setText(gourmet.category);
        }

        Util.requestImageResize(mContext, holder.gourmetImageView, gourmet.imageUrl);

        // SOLD OUT 표시
        if (gourmet.isSoldOut)
        {
            holder.soldOutView.setVisibility(View.VISIBLE);
        } else
        {
            holder.soldOutView.setVisibility(View.GONE);
        }

        if (mSortType == Constants.SortType.DISTANCE)
        {
            holder.distanceView.setVisibility(View.VISIBLE);
            holder.distanceView.setText(new DecimalFormat("#.#").format(gourmet.distance / 1000) + "km");
        } else
        {
            holder.distanceView.setVisibility(View.GONE);
        }
    }

    private class GourmetViewHolder extends RecyclerView.ViewHolder
    {
        View gradientView;
        com.facebook.drawee.view.SimpleDraweeView gourmetImageView;
        TextView nameView;
        TextView priceView;
        TextView discountView;
        View soldOutView;
        TextView addressView;
        TextView gradeView;
        TextView satisfactionView;
        TextView personsTextView;
        TextView distanceView;

        public GourmetViewHolder(View itemView)
        {
            super(itemView);

            gradientView = itemView.findViewById(R.id.gradientView);
            gourmetImageView = (com.facebook.drawee.view.SimpleDraweeView) itemView.findViewById(R.id.imageView);
            nameView = (TextView) itemView.findViewById(R.id.nameTextView);
            priceView = (TextView) itemView.findViewById(R.id.priceTextView);
            satisfactionView = (TextView) itemView.findViewById(R.id.satisfactionView);
            discountView = (TextView) itemView.findViewById(R.id.discountPriceTextView);
            soldOutView = itemView.findViewById(R.id.soldoutView);
            addressView = (TextView) itemView.findViewById(R.id.addressTextView);
            gradeView = (TextView) itemView.findViewById(R.id.gradeTextView);
            personsTextView = (TextView) itemView.findViewById(R.id.personsTextView);
            distanceView = (TextView) itemView.findViewById(R.id.distanceTextView);

            itemView.setOnClickListener(mOnClickListener);
        }
    }

    private class SectionViewHolder extends RecyclerView.ViewHolder
    {
        TextView regionDetailName;

        public SectionViewHolder(View itemView)
        {
            super(itemView);

            regionDetailName = (TextView) itemView.findViewById(R.id.hotelListRegionName);
        }
    }

    private class EventBannerViewHolder extends RecyclerView.ViewHolder
    {
        DailyLoopViewPager dailyLoopViewPager;
        DailyViewPagerCircleIndicator viewpagerCircleIndicator;

        public EventBannerViewHolder(View itemView)
        {
            super(itemView);

            dailyLoopViewPager = (DailyLoopViewPager) itemView.findViewById(R.id.loopViewPager);
            viewpagerCircleIndicator = (DailyViewPagerCircleIndicator) itemView.findViewById(R.id.viewpagerCircleIndicator);

            dailyLoopViewPager.setSlideTime(4);
        }
    }

    private static class EventBannerHandler extends Handler
    {
        private final WeakReference<GourmetListAdapter> mWeakReference;

        public EventBannerHandler(GourmetListAdapter adapter)
        {
            mWeakReference = new WeakReference<>(adapter);
        }

        @Override
        public void handleMessage(Message msg)
        {
            GourmetListAdapter gourmetListAdapter = mWeakReference.get();

            if (gourmetListAdapter == null)
            {
                return;
            }

            EventBannerViewHolder eventBannerViewHolder = (EventBannerViewHolder) msg.obj;

            if (eventBannerViewHolder != null)
            {
                eventBannerViewHolder.dailyLoopViewPager.setCurrentItem(msg.arg1 + 1);
            }
        }
    }
}
