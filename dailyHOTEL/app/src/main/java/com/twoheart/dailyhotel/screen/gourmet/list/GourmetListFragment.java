package com.twoheart.dailyhotel.screen.gourmet.list;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Gourmet;
import com.twoheart.dailyhotel.model.GourmetCuration;
import com.twoheart.dailyhotel.model.GourmetParams;
import com.twoheart.dailyhotel.model.PlaceCuration;
import com.twoheart.dailyhotel.model.PlaceCurationOption;
import com.twoheart.dailyhotel.model.PlaceViewItem;
import com.twoheart.dailyhotel.model.Province;
import com.twoheart.dailyhotel.model.time.GourmetBookingDay;
import com.twoheart.dailyhotel.place.base.BaseNetworkController;
import com.twoheart.dailyhotel.place.fragment.PlaceListFragment;
import com.twoheart.dailyhotel.place.layout.PlaceListLayout;
import com.twoheart.dailyhotel.util.Util;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Response;

public class GourmetListFragment extends PlaceListFragment
{
    protected GourmetCuration mGourmetCuration;
    private GourmetListLayout mGourmetListLayout;

    public interface OnGourmetListFragmentListener extends OnPlaceListFragmentListener
    {
        void onGourmetClick(View view, PlaceViewItem placeViewItem, int listCount);

        void onGourmetLongClick(View view, PlaceViewItem placeViewItem, int listCount);

        void onGourmetCategoryFilter(int page, HashMap<String, Integer> categoryCodeMap, HashMap<String, Integer> categorySequenceMap);
    }

    @Override
    protected BaseNetworkController getNetworkController()
    {
        return new GourmetListNetworkController(mBaseActivity, mNetworkTag, mNetworkControllerListener);
    }

    @Override
    protected PlaceListLayout getPlaceListLayout()
    {
        if (mGourmetListLayout == null)
        {
            mGourmetListLayout = new GourmetListLayout(mBaseActivity, mEventListener);
        }

        return mGourmetListLayout;
    }

    @Override
    protected int getLayoutResourceId()
    {
        return R.layout.fragment_gourmet_list;
    }

    @Override
    public void setPlaceCuration(PlaceCuration curation)
    {
        if (mPlaceListLayout == null)
        {
            return;
        }

        mGourmetCuration = (GourmetCuration) curation;
        ((GourmetListLayout) mPlaceListLayout).setGourmetCuration(mGourmetCuration);
    }

    @Override
    protected void refreshList(boolean isShowProgress, int page)
    {
        // 더보기 시 unlock 걸지않음
        if (page <= 1)
        {
            lockUI(isShowProgress);
        }

        GourmetBookingDay gourmetBookingDay = mGourmetCuration.getGourmetBookingDay();
        Province province = mGourmetCuration.getProvince();

        if (province == null || gourmetBookingDay == null)
        {
            unLockUI();
            Util.restartApp(mBaseActivity);
            return;
        }

        if (mGourmetCuration == null || mGourmetCuration.getCurationOption() == null//
            || mGourmetCuration.getCurationOption().getSortType() == null//
            || (mGourmetCuration.getCurationOption().getSortType() == SortType.DISTANCE && mGourmetCuration.getLocation() == null))
        {
            unLockUI();
            Util.restartApp(mBaseActivity);
            return;
        }

        GourmetParams params = (GourmetParams) mGourmetCuration.toPlaceParams(page, PAGENATION_LIST_SIZE, true);
        ((GourmetListNetworkController) mNetworkController).requestGourmetList(params);
    }

    protected void onGourmetList(ArrayList<Gourmet> list, int page, int totalCount, int maxCount, //
                                 HashMap<String, Integer> categoryCodeMap, HashMap<String, Integer> categorySequenceMap, boolean hasSection)
    {
        if (isFinishing() == true)
        {
            unLockUI();
            return;
        }

        // 페이지가 전체데이터 이거나 첫페이지 이면 스크롤 탑
        if (page <= 1)
        {
            mPlaceCount = 0;
            mPlaceListLayout.clearList();

            if (mGourmetCuration.getCurationOption().isDefaultFilter() == true)
            {
                ((OnGourmetListFragmentListener) mOnPlaceListFragmentListener).onGourmetCategoryFilter(page, categoryCodeMap, categorySequenceMap);
            }
        }

        int listSize = list == null ? 0 : list.size();
        if (listSize > 0)
        {
            mLoadMorePageIndex = page;
            mIsLoadMoreFlag = true;
        } else
        {
            mIsLoadMoreFlag = false;
        }

        mPlaceCount += listSize;

        SortType sortType = mGourmetCuration.getCurationOption().getSortType();

        ArrayList<PlaceViewItem> placeViewItems = makePlaceList(list, sortType, hasSection);

        switch (mViewType)
        {
            case LIST:
            {
                mPlaceListLayout.addResultList(getChildFragmentManager(), mViewType, placeViewItems, sortType, mGourmetCuration.getGourmetBookingDay());

                int size = mPlaceListLayout.getItemCount();

                if (size == 0)
                {
                    setVisibility(ViewType.GONE, true);
                }

                mEventListener.onShowActivityEmptyView(size == 0);
                break;
            }

            case MAP:
            {
                mPlaceListLayout.setList(getChildFragmentManager(), mViewType, placeViewItems, sortType, mGourmetCuration.getGourmetBookingDay());

                int mapSize = mPlaceListLayout.getMapItemSize();
                if (mapSize == 0)
                {
                    setVisibility(ViewType.GONE, true);
                }

                mEventListener.onShowActivityEmptyView(mapSize == 0);
                break;
            }

            default:
                break;
        }

        unLockUI();
        mPlaceListLayout.setSwipeRefreshing(false);
    }

    @Override
    public boolean isDefaultFilter()
    {
        if (mGourmetCuration == null)
        {
            return true;
        }

        PlaceCurationOption placeCurationOption = mGourmetCuration.getCurationOption();
        if (placeCurationOption == null)
        {
            return true;
        }

        return placeCurationOption.isDefaultFilter();
    }

    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////   Listener   //////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////

    protected GourmetListLayout.OnEventListener mEventListener = new GourmetListLayout.OnEventListener()
    {
        @Override
        public void onPlaceClick(View view, PlaceViewItem placeViewItem)
        {
            ((OnGourmetListFragmentListener) mOnPlaceListFragmentListener).onGourmetClick(view, placeViewItem, getPlaceCount());
        }

        @Override
        public void onPlaceLongClick(View view, PlaceViewItem placeViewItem)
        {
            ((OnGourmetListFragmentListener) mOnPlaceListFragmentListener).onGourmetLongClick(view, placeViewItem, getPlaceCount());
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy)
        {
            mOnPlaceListFragmentListener.onScrolled(recyclerView, dx, dy);
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState)
        {
            mOnPlaceListFragmentListener.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onRefreshAll(boolean isShowProgress)
        {
            refreshList(isShowProgress, 1);

            mOnPlaceListFragmentListener.onShowMenuBar();
        }

        @Override
        public void onLoadMoreList()
        {
            addList(false);
        }

        @Override
        public void onFilterClick()
        {
            mOnPlaceListFragmentListener.onFilterClick();
        }

        @Override
        public void onUpdateFilterEnabled(boolean isEnabled)
        {
            mOnPlaceListFragmentListener.onUpdateFilterEnabled(isEnabled);
        }

        @Override
        public void onUpdateViewTypeEnabled(boolean isEnabled)
        {
            mOnPlaceListFragmentListener.onUpdateViewTypeEnabled(isEnabled);
        }

        @Override
        public void onShowActivityEmptyView(boolean isShow)
        {
            mOnPlaceListFragmentListener.onShowActivityEmptyView(isShow);
        }

        @Override
        public void onRecordAnalytics(ViewType viewType)
        {
            mOnPlaceListFragmentListener.onRecordAnalytics(viewType);
        }

        @Override
        public void finish()
        {
            if (mBaseActivity != null)
            {
                mBaseActivity.finish();
            }
        }
    };

    private GourmetListNetworkController.OnNetworkControllerListener mNetworkControllerListener = new GourmetListNetworkController.OnNetworkControllerListener()
    {
        @Override
        public void onGourmetList(ArrayList<Gourmet> list, int page, int totalCount, int maxCount, HashMap<String, Integer> categoryCodeMap, HashMap<String, Integer> categorySequenceMap)
        {
            GourmetListFragment.this.onGourmetList(list, page, totalCount, maxCount, categoryCodeMap, categorySequenceMap, true);
        }

        @Override
        public void onError(Call call, Throwable e, boolean onlyReport)
        {
            if (mPlaceListLayout.isRefreshing() == true)
            {
                mPlaceListLayout.setSwipeRefreshing(false);
            }

            GourmetListFragment.this.onError(call, e, onlyReport);
        }

        @Override
        public void onError(Throwable e)
        {
            if (mPlaceListLayout.isRefreshing() == true)
            {
                mPlaceListLayout.setSwipeRefreshing(false);
            }

            GourmetListFragment.this.onError(e);
        }

        @Override
        public void onErrorPopupMessage(int msgCode, String message)
        {
            if (mPlaceListLayout.isRefreshing() == true)
            {
                mPlaceListLayout.setSwipeRefreshing(false);
            }

            GourmetListFragment.this.onErrorPopupMessage(msgCode, message);
        }

        @Override
        public void onErrorToastMessage(String message)
        {
            if (mPlaceListLayout.isRefreshing() == true)
            {
                mPlaceListLayout.setSwipeRefreshing(false);
            }

            GourmetListFragment.this.onErrorToastMessage(message);
        }

        @Override
        public void onErrorResponse(Call call, Response response)
        {
            if (mPlaceListLayout.isRefreshing() == true)
            {
                mPlaceListLayout.setSwipeRefreshing(false);
            }

            GourmetListFragment.this.onErrorResponse(call, response);
        }
    };
}
