package com.twoheart.dailyhotel.screen.gourmet.list;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Area;
import com.twoheart.dailyhotel.model.EventBanner;
import com.twoheart.dailyhotel.model.Gourmet;
import com.twoheart.dailyhotel.model.GourmetCurationOption;
import com.twoheart.dailyhotel.model.PlaceViewItem;
import com.twoheart.dailyhotel.model.Province;
import com.twoheart.dailyhotel.model.SaleTime;
import com.twoheart.dailyhotel.place.base.BaseLayout;
import com.twoheart.dailyhotel.place.base.OnBaseEventListener;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.EdgeEffectColor;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager.Screen;
import com.twoheart.dailyhotel.widget.PinnedSectionRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GourmetListLayout extends BaseLayout
{
    protected PinnedSectionRecyclerView mGourmetRecyclerView;
    protected GourmetListAdapter mGourmetAdapter;

    private View mEmptyView;
    private ViewGroup mMapLayout;
    private GourmetMapFragment mGourmetMapFragment;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    protected boolean mScrollListTop;
    protected List<Gourmet> mGourmetList = new ArrayList<>();

    public interface OnEventListener extends OnBaseEventListener
    {
        void onGourmetClick(PlaceViewItem placeViewItem, SaleTime saleTime);

        void onEventBannerClick(EventBanner eventBanner);

        void onRefreshAll(boolean isShowProgress);
    }

    public GourmetListLayout(Context context, OnEventListener mOnEventListener)
    {
        super(context, mOnEventListener);
    }

    @Override
    protected void initLayout(View view)
    {
        mGourmetRecyclerView = (PinnedSectionRecyclerView) view.findViewById(R.id.recycleView);
        mGourmetRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mGourmetRecyclerView.setTag("GourmetListFragment");
        EdgeEffectColor.setEdgeGlowColor(mGourmetRecyclerView, mContext.getResources().getColor(R.color.default_over_scroll_edge));

        mGourmetAdapter = new GourmetListAdapter(mContext, new ArrayList<PlaceViewItem>(), mOnItemClickListener, mOnEventBannerItemClickListener);
        mGourmetRecyclerView.setAdapter(mGourmetAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.dh_theme_color);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                ((OnEventListener) mOnEventListener).onRefreshAll(false);
            }
        });

        mEmptyView = view.findViewById(R.id.emptyLayout);

        mMapLayout = (ViewGroup) view.findViewById(R.id.mapLayout);

        mGourmetRecyclerView.setShadowVisible(false);
    }

    public boolean canScrollUp()
    {
        if (mSwipeRefreshLayout != null)
        {
            return mSwipeRefreshLayout.canChildScrollUp();
        }

        return true;
    }

    public void onPageSelected(String tabText)
    {
    }

    public void onPageUnSelected()
    {
    }

    public void setVisibility(FragmentManager fragmentManager, Constants.ViewType viewType, boolean isCurrentPage)
    {
        switch (viewType)
        {
            case LIST:
                mEmptyView.setVisibility(View.GONE);
                mMapLayout.setVisibility(View.GONE);

                if (mGourmetMapFragment != null)
                {
                    fragmentManager.beginTransaction().remove(mGourmetMapFragment).commitAllowingStateLoss();
                    mMapLayout.removeAllViews();
                    mGourmetMapFragment = null;
                }

                mSwipeRefreshLayout.setVisibility(View.VISIBLE);
                break;

            case MAP:
                mEmptyView.setVisibility(View.GONE);
                mMapLayout.setVisibility(View.VISIBLE);

                if (isCurrentPage == true && mGourmetMapFragment == null)
                {
                    mGourmetMapFragment = new GourmetMapFragment();
                    fragmentManager.beginTransaction().add(mMapLayout.getId(), mGourmetMapFragment).commitAllowingStateLoss();
                }

                mSwipeRefreshLayout.setVisibility(View.INVISIBLE);
                break;

            case GONE:
                AnalyticsManager.getInstance(mContext).recordScreen(Screen.DAILYGOURMET_LIST_EMPTY);

                mEmptyView.setVisibility(View.VISIBLE);
                mMapLayout.setVisibility(View.GONE);

                mSwipeRefreshLayout.setVisibility(View.INVISIBLE);
                break;
        }
    }

    public boolean isShowInformationAtMapView(Constants.ViewType viewType)
    {
        if (viewType == Constants.ViewType.MAP && mGourmetMapFragment != null)
        {
            return mGourmetMapFragment.isShowInformation();
        }

        return false;
    }

    public void setScrollListTop(boolean scrollListTop)
    {
        mScrollListTop = scrollListTop;
    }

    public void setList(FragmentManager fragmentManager, Constants.ViewType viewType, ArrayList<PlaceViewItem> list, Constants.SortType sortType)
    {
        if (mGourmetAdapter == null)
        {
            Util.restartApp(mContext);
            return;
        }

        mGourmetAdapter.clear();

        if (list == null || list.size() == 0)
        {
            mGourmetAdapter.notifyDataSetChanged();

            setVisibility(fragmentManager, Constants.ViewType.GONE, true);
        } else
        {
            setVisibility(fragmentManager, viewType, true);

            if (viewType == Constants.ViewType.MAP)
            {
                //                if (hasSalesPlace(list) == false)
                //                {
                //                    unLockUI();
                //
                //                    DailyToast.showToast(mContext, R.string.toast_msg_solodout_area, Toast.LENGTH_SHORT);
                //
                //                    mOnCommunicateListener.toggleViewType();
                //                    return;
                //                }
                //
                //                mGourmetMapFragment.setOnCommunicateListener(mOnCommunicateListener);
                //                mGourmetMapFragment.setPlaceViewItemList(list, mSaleTime, mScrollListTop);
                //
                //                AnalyticsManager.getInstance(getContext()).recordScreen(Screen.DAILYGOURMET_LIST_MAP);
            } else
            {
                AnalyticsManager.getInstance(mContext).recordScreen(Screen.DAILYGOURMET_LIST);

                Map<String, String> parmas = new HashMap<>();
                GourmetCurationOption gourmetCurationOption = GourmetCurationManager.getInstance().getGourmetCurationOption();
                Province province = GourmetCurationManager.getInstance().getProvince();

                if (province instanceof Area)
                {
                    Area area = (Area) province;
                    parmas.put(AnalyticsManager.KeyType.PROVINCE, area.getProvince().name);
                    parmas.put(AnalyticsManager.KeyType.DISTRICT, area.name);

                } else
                {
                    parmas.put(AnalyticsManager.KeyType.PROVINCE, province.name);
                    parmas.put(AnalyticsManager.KeyType.DISTRICT, AnalyticsManager.ValueType.EMPTY);
                }

                AnalyticsManager.getInstance(mContext).recordScreen(Screen.DAILYGOURMET_LIST, parmas);
            }

            if (sortType == Constants.SortType.DEFAULT)
            {
                if (GourmetEventBannerManager.getInstance().getCount() > 0)
                {
                    PlaceViewItem placeViewItem = new PlaceViewItem(PlaceViewItem.TYPE_EVENT_BANNER//
                        , GourmetEventBannerManager.getInstance().getList());
                    list.add(0, placeViewItem);
                }
            }

            mGourmetAdapter.addAll(list, sortType);
            mGourmetAdapter.notifyDataSetChanged();

            if (mScrollListTop == true)
            {
                mScrollListTop = false;
                mGourmetRecyclerView.scrollToPosition(0);
            }
        }
    }

    public boolean hasSalesPlace()
    {
        return hasSalesPlace(mGourmetAdapter.getAll());
    }

    private boolean hasSalesPlace(List<PlaceViewItem> gourmetListViewItemList)
    {
        boolean hasPlace = false;

        if (gourmetListViewItemList != null)
        {
            for (PlaceViewItem placeViewItem : gourmetListViewItemList)
            {
                if (placeViewItem.mType == PlaceViewItem.TYPE_ENTRY//
                    && placeViewItem.<Gourmet>getItem().isSoldOut == false)
                {
                    hasPlace = true;
                    break;
                }
            }
        }

        return hasPlace;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private View.OnClickListener mOnItemClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            int position = mGourmetRecyclerView.getChildAdapterPosition(view);

            if (position < 0)
            {
                ((OnEventListener) mOnEventListener).onGourmetClick(null, GourmetCurationManager.getInstance().getSaleTime());
                return;
            }

            PlaceViewItem gourmetViewItem = mGourmetAdapter.getItem(position);

            if (gourmetViewItem.mType == PlaceViewItem.TYPE_ENTRY)
            {
                ((OnEventListener) mOnEventListener).onGourmetClick(gourmetViewItem, GourmetCurationManager.getInstance().getSaleTime());
            }
        }
    };

    private View.OnClickListener mOnEventBannerItemClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {

            Integer index = (Integer) view.getTag(view.getId());

            if (index != null)
            {
                EventBanner eventBanner = GourmetEventBannerManager.getInstance().getEventBanner(index);
                ((OnEventListener) mOnEventListener).onEventBannerClick(eventBanner);
            }
        }
    };
}
