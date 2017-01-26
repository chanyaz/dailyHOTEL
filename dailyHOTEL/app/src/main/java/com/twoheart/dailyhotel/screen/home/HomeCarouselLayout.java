package com.twoheart.dailyhotel.screen.home;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Place;
import com.twoheart.dailyhotel.util.EdgeEffectColor;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.widget.DailyTextView;
import com.twoheart.dailyhotel.widget.shimmer.Shimmer;
import com.twoheart.dailyhotel.widget.shimmer.ShimmerView;

import java.util.ArrayList;

import static com.twoheart.dailyhotel.util.Util.dpToPx;

/**
 * Created by android_sam on 2017. 1. 16..
 */

public class HomeCarouselLayout extends RelativeLayout
{
    private Context mContext;
    private DailyTextView mTitleTextView;
    private DailyTextView mCountTextView;
    private DailyTextView mViewAllTextView;
    private OnCarouselListener mCarouselListenter;
    private ArrayList<? extends Place> mPlaceList;
    private RecyclerView mRecyclerView;
    private HomeCarouselAdapter mRecyclerAdapter;

    private Shimmer mShimmer;
    private View mCoverview;

    public interface OnCarouselListener
    {
        void onViewAllClick();
    }

    public HomeCarouselLayout(Context context)
    {
        super(context);

        mContext = context;
        initLayout();
    }

    public HomeCarouselLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        mContext = context;
        initLayout();
    }

    public HomeCarouselLayout(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        mContext = context;
        initLayout();
    }

    public HomeCarouselLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);

        mContext = context;
        initLayout();
    }

    private void initLayout()
    {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_row_home_carousel_layout, this);

        mTitleTextView = (DailyTextView) view.findViewById(R.id.titleTextView);
        mCountTextView = (DailyTextView) view.findViewById(R.id.countTextView);
        mViewAllTextView = (DailyTextView) view.findViewById(R.id.viewAllTextView);

        mViewAllTextView.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mCarouselListenter != null)
                {
                    mCarouselListenter.onViewAllClick();
                }
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManager.setAutoMeasureEnabled(true);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.horizontalRecyclerView);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(false);

        EdgeEffectColor.setEdgeGlowColor(mRecyclerView, mContext.getResources().getColor(R.color.default_over_scroll_edge));

        mCoverview = view.findViewById(R.id.coverView);
        SimpleDraweeView coverImageView = (SimpleDraweeView) mCoverview.findViewById(R.id.contentImageView);
        coverImageView.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
        coverImageView.getHierarchy().setPlaceholderImage(R.drawable.layerlist_placeholder);

        int width = coverImageView.getWidth() == 0 ? dpToPx(mContext, 239) : coverImageView.getWidth();
        int height = Util.getRatioHeightType16x9(width);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
        coverImageView.setLayoutParams(layoutParams);

        mShimmer = new Shimmer();
    }

    public void startShimmer()
    {
        if (mShimmer == null)
        {
            mShimmer = new Shimmer();
        }

        if (mCoverview == null)
        {
            return;
        }

        ShimmerView shimmerView1 = (ShimmerView) mCoverview.findViewById(R.id.shimmerView1);
        ShimmerView shimmerView2 = (ShimmerView) mCoverview.findViewById(R.id.shimmerView2);
        ShimmerView shimmerView3 = (ShimmerView) mCoverview.findViewById(R.id.shimmerView3);

        mShimmer.start(shimmerView1);
        mShimmer.start(shimmerView2);
        mShimmer.start(shimmerView3);
    }

    public void stopShimmer()
    {
        if (mShimmer != null && mShimmer.isAnimating() == true)
        {
            mShimmer.cancel();
        }
    }

    public void setCarouselListener(OnCarouselListener listener)
    {
        mCarouselListenter = listener;
    }

    public void setData(ArrayList<? extends Place> list)
    {
        if (list == null || list.size() == 0)
        {
            list = new ArrayList<>();
        }

        stopShimmer();
        mCoverview.setVisibility(View.GONE);

        if (mRecyclerAdapter == null)
        {
            mRecyclerAdapter = new HomeCarouselAdapter(mContext, list, mRecyclerItemClcikListner);
            mRecyclerView.setAdapter(mRecyclerAdapter);
        } else
        {
            mRecyclerAdapter.setData(list);
            mRecyclerAdapter.notifyDataSetChanged();
        }
    }

    private HomeCarouselAdapter.ItemClickListener mRecyclerItemClcikListner = new HomeCarouselAdapter.ItemClickListener()
    {
        @Override
        public void onItemClick(View view, int position)
        {
            // TODO : 아이템 클릭 시 이동하는 부분 생성
        }
    };
}
