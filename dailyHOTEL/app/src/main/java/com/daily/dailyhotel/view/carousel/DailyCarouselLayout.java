package com.daily.dailyhotel.view.carousel;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.SnapHelper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.daily.base.util.ScreenUtils;
import com.daily.dailyhotel.entity.CarouselListItem;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.databinding.LayoutCarouselDataBinding;
import com.twoheart.dailyhotel.util.EdgeEffectColor;
import com.twoheart.dailyhotel.widget.DailyPagerSnapHelper;

import java.util.ArrayList;

/**
 * Created by iseung-won on 2017. 8. 24..
 */

public class DailyCarouselLayout extends ConstraintLayout
{
    private Context mContext;
    private LayoutCarouselDataBinding mDataBinding;
    private DailyCarouselAdapter mAdapter;

    protected OnCarouselListener mCarouselListener;

    public interface OnCarouselListener
    {
        void onViewAllClick();

        void onItemClick(View view);

        void onItemLongClick(View view);
    }

    public DailyCarouselLayout(Context context)
    {
        super(context);

        mContext = context;
        initLayout();
    }

    public DailyCarouselLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        mContext = context;
        initLayout();
    }

    public DailyCarouselLayout(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        mContext = context;
        initLayout();
    }

    private void initLayout()
    {
        mDataBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.layout_carousel_data, this, true);

        setBackgroundResource(R.color.white);

        mDataBinding.viewAllTextView.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mCarouselListener == null)
                {
                    return;
                }

                mCarouselListener.onViewAllClick();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        //        layoutManager.setAutoMeasureEnabled(true);

        mDataBinding.recyclerView.setLayoutManager(layoutManager);
        EdgeEffectColor.setEdgeGlowColor(mDataBinding.recyclerView, mContext.getResources().getColor(R.color.default_over_scroll_edge));

        //        mDataBinding.recyclerView.setNestedScrollingEnabled(false);
        //        mDataBinding.recyclerView.setHasFixedSize(true);

        if (ScreenUtils.isTabletDevice((Activity) mContext) == true)
        {
            SnapHelper snapHelper = new DailyPagerSnapHelper();
            snapHelper.attachToRecyclerView(mDataBinding.recyclerView);
        } else
        {
            SnapHelper snapHelper = new PagerSnapHelper();
            snapHelper.attachToRecyclerView(mDataBinding.recyclerView);
        }
    }

    public void setCarouselListener(OnCarouselListener listener)
    {
        mCarouselListener = listener;
    }

    public void setTitleText(int resId)
    {
        if (mDataBinding == null)
        {
            return;
        }

        mDataBinding.titleTextView.setText(resId);
    }

    public boolean hasData()
    {
        if (mAdapter == null)
        {
            return false;
        }

        return mAdapter.getData() == null ? false : mAdapter.getData().size() > 0;
    }

    public ArrayList<CarouselListItem> getData()
    {
        if (mAdapter == null)
        {
            return null;
        }

        return mAdapter.getData();
    }

    public void setData(ArrayList<CarouselListItem> list)
    {
        mDataBinding.recyclerView.scrollToPosition(0);

        if (mAdapter == null)
        {
            mAdapter = new DailyCarouselAdapter(mContext, list, mItemClickListener);
            mDataBinding.recyclerView.setAdapter(mAdapter);
        } else
        {
            mAdapter.setData(list);
            mAdapter.notifyDataSetChanged();
        }
    }

    public CarouselListItem getItem(int position)
    {
        if (mAdapter == null)
        {
            return null;
        }

        return mAdapter.getItem(position);
    }

    private DailyCarouselAdapter.ItemClickListener mItemClickListener = new DailyCarouselAdapter.ItemClickListener()
    {
        @Override
        public void onItemClick(View view)
        {
            if (mCarouselListener == null)
            {
                return;
            }

            mCarouselListener.onItemClick(view);
        }

        @Override
        public void onItemLongClick(View view)
        {
            if (mCarouselListener == null)
            {
                return;
            }

            mCarouselListener.onItemLongClick(view);
        }
    };
}