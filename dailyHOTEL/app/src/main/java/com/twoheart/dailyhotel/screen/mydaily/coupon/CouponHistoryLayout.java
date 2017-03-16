package com.twoheart.dailyhotel.screen.mydaily.coupon;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.CouponHistory;
import com.twoheart.dailyhotel.place.base.BaseLayout;
import com.twoheart.dailyhotel.place.base.OnBaseEventListener;
import com.twoheart.dailyhotel.util.EdgeEffectColor;
import com.twoheart.dailyhotel.widget.DailyToolbarLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sam Lee on 2016. 5. 23..
 */
public class CouponHistoryLayout extends BaseLayout
{
    private RecyclerView mRecyclerView;
    private View mEmptyView;
    private CouponHistoryListAdapter mListAdapter;

    public interface OnEventListener extends OnBaseEventListener
    {
        void onHomeClick();
    }

    public CouponHistoryLayout(Context context, OnBaseEventListener listener)
    {
        super(context, listener);
    }

    @Override
    protected void initLayout(View view)
    {
        initToolbar(view);
        initListView(view);
    }

    private void initToolbar(View view)
    {
        View toolbar = view.findViewById(R.id.toolbar);

        DailyToolbarLayout dailyToolbarLayout = new DailyToolbarLayout(mContext, toolbar);
        dailyToolbarLayout.initToolbar(mContext.getString(R.string.actionbar_title_coupon_history), new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mOnEventListener.finish();
            }
        });
    }

    private void initListView(View view)
    {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.couponHistoryRecyclerView);

        mEmptyView = view.findViewById(R.id.emptyView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        mRecyclerView.setLayoutManager(layoutManager);

        EdgeEffectColor.setEdgeGlowColor(mRecyclerView, mContext.getResources().getColor(R.color.default_over_scroll_edge));

        View homeButtonView = view.findViewById(R.id.homeButtonView);
        homeButtonView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((OnEventListener) mOnEventListener).onHomeClick();
            }
        });
    }

    public void setData(List<CouponHistory> list)
    {
        if (list != null && list.size() != 0)
        {
            mListAdapter = new CouponHistoryListAdapter(mContext, list);
            mEmptyView.setVisibility(View.GONE);
        } else
        {
            mListAdapter = new CouponHistoryListAdapter(mContext, new ArrayList<CouponHistory>());
            mEmptyView.setVisibility(View.VISIBLE);
        }

        mRecyclerView.setAdapter(mListAdapter);
    }
}
