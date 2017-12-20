package com.twoheart.dailyhotel.screen.hotel.list;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.daily.base.BaseActivity;
import com.daily.base.util.DailyTextUtils;
import com.daily.dailyhotel.entity.StayRegion;
import com.daily.dailyhotel.screen.common.dialog.call.CallDialogActivity;
import com.daily.dailyhotel.screen.common.dialog.wish.WishDialogActivity;
import com.daily.dailyhotel.screen.home.stay.inbound.detail.StayDetailActivity;
import com.daily.dailyhotel.storage.preference.DailyRemoteConfigPreference;
import com.twoheart.dailyhotel.DailyHotel;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Category;
import com.twoheart.dailyhotel.model.Place;
import com.twoheart.dailyhotel.model.PlaceCuration;
import com.twoheart.dailyhotel.model.PlaceCurationOption;
import com.twoheart.dailyhotel.model.PlaceViewItem;
import com.twoheart.dailyhotel.model.Stay;
import com.twoheart.dailyhotel.model.StayCuration;
import com.twoheart.dailyhotel.model.StayParams;
import com.twoheart.dailyhotel.model.time.StayBookingDay;
import com.twoheart.dailyhotel.place.base.BaseNetworkController;
import com.twoheart.dailyhotel.place.fragment.PlaceListFragment;
import com.twoheart.dailyhotel.place.fragment.PlaceListMapFragment;
import com.twoheart.dailyhotel.place.layout.PlaceListLayout;
import com.twoheart.dailyhotel.screen.hotel.preview.StayPreviewActivity;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class StayListFragment extends PlaceListFragment
{
    protected StayCuration mStayCuration;
    protected StayListLayout mStayListLayout;

    public interface OnStayListFragmentListener extends OnPlaceListFragmentListener
    {
        void onStayClick(View view, PlaceViewItem placeViewItem, int listCount);

        void onStayLongClick(View view, PlaceViewItem placeViewItem, int listCount);

        void onRegionClick();

        void onCalendarClick();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case Constants.CODE_REQUEST_ACTIVITY_WISH_DIALOG:
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                    case BaseActivity.RESULT_CODE_ERROR:
                        if (data != null)
                        {
                            onChangedWish(mWishPosition, data.getBooleanExtra(WishDialogActivity.INTENT_EXTRA_DATA_WISH, false));
                        }
                        break;
                }
                break;

            case CODE_REQUEST_ACTIVITY_STAY_DETAIL:
            case CODE_REQUEST_ACTIVITY_GOURMET_DETAIL:
            {
                if (resultCode == com.daily.base.BaseActivity.RESULT_CODE_REFRESH && data != null)
                {
                    if (data.hasExtra(StayDetailActivity.INTENT_EXTRA_DATA_WISH) == true)
                    {
                        onChangedWish(mWishPosition, data.getBooleanExtra(StayDetailActivity.INTENT_EXTRA_DATA_WISH, false));
                    }
                }
                break;
            }

            case CODE_REQUEST_ACTIVITY_PREVIEW:
                if (resultCode == Constants.CODE_RESULT_ACTIVITY_REFRESH)
                {
                    if (data != null && data.hasExtra(StayPreviewActivity.INTENT_EXTRA_DATA_WISH) == true)
                    {
                        onChangedWish(mWishPosition, data.getBooleanExtra(StayPreviewActivity.INTENT_EXTRA_DATA_WISH, false));
                    }
                }
                break;

            default:
                if (mViewType == ViewType.MAP)
                {
                    PlaceListMapFragment placeListMapFragment = mPlaceListLayout.getListMapFragment();

                    if (placeListMapFragment != null)
                    {
                        placeListMapFragment.onActivityResult(requestCode, resultCode, data);
                    }
                }
                break;
        }
    }

    @Override
    protected BaseNetworkController getNetworkController()
    {
        return new StayListNetworkController(mBaseActivity, mNetworkTag, mNetworkControllerListener);
    }

    @Override
    public PlaceListLayout getPlaceListLayout()
    {
        if (mStayListLayout == null)
        {
            mStayListLayout = new StayListLayout(mBaseActivity, mEventListener);
        }
        return mStayListLayout;
    }

    @Override
    protected int getLayoutResourceId()
    {
        return R.layout.fragment_hotel_list;
    }

    @Override
    public void setPlaceCuration(PlaceCuration curation)
    {
        if (mPlaceListLayout == null)
        {
            return;
        }

        mStayCuration = (StayCuration) curation;
        ((StayListLayout) mPlaceListLayout).setStayCuration(mStayCuration);
    }

    @Override
    protected void refreshList(boolean isShowProgress, int page)
    {
        if (mStayCuration == null)
        {
            unLockUI();
            Util.restartApp(mBaseActivity);
            return;
        }

        // 더보기 시 unlock 걸지않음
        if (page <= 1)
        {
            lockUI(isShowProgress);
        }

        StayBookingDay stayBookingDay = mStayCuration.getStayBookingDay();
        StayRegion region = mStayCuration.getRegion();

        if (region == null || stayBookingDay == null)
        {
            unLockUI();
            Util.restartApp(mBaseActivity);
            return;
        }

        if (mStayCuration == null || mStayCuration.getCurationOption() == null//
            || mStayCuration.getCurationOption().getSortType() == null//
            || (mStayCuration.getCurationOption().getSortType() == SortType.DISTANCE && mStayCuration.getLocation() == null))
        {
            unLockUI();
            Util.restartApp(mBaseActivity);
            return;
        }

        StayParams params = (StayParams) mStayCuration.toPlaceParams(page, PAGENATION_LIST_SIZE, true);
        String abTestType = DailyRemoteConfigPreference.getInstance(getContext()).getKeyRemoteConfigStayRankTestType();

        ((StayListNetworkController) mNetworkController).requestStayList(params, abTestType);
    }

    @Override
    protected void onChangedWish(int position, boolean wish)
    {
        if (position < 0)
        {
            return;
        }

        if (mPlaceListLayout == null)
        {
            Util.restartApp(getContext());
            return;
        }

        PlaceViewItem placeViewItem = mPlaceListLayout.getItem(position);

        if (placeViewItem == null)
        {
            return;
        }

        Stay stay = placeViewItem.getItem();
        stay.myWish = wish;
        mPlaceListLayout.notifyWishChanged(position, wish);
    }

    protected void onStayList(List<Stay> list, int page, boolean hasSection, boolean activeReward)
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

        SortType sortType = mStayCuration.getCurationOption().getSortType();

        ArrayList<PlaceViewItem> placeViewItems = makePlaceList(list, sortType, hasSection);

        switch (mViewType)
        {
            case LIST:
            {
                mPlaceListLayout.addResultList(getChildFragmentManager(), mViewType, placeViewItems//
                    , sortType, mStayCuration.getStayBookingDay(), activeReward);

                int size = mPlaceListLayout.getItemCount();
                if (size == 0)
                {
                    setVisibility(mViewType, EmptyStatus.EMPTY, true);
                }

                Category category = mStayCuration.getCategory();
                if (Category.ALL.code.equalsIgnoreCase(category.code))
                {
                    mEventListener.onShowActivityEmptyView(size == 0);
                }
                break;
            }

            case MAP:
            {
                mPlaceListLayout.setList(getChildFragmentManager(), mViewType, placeViewItems, sortType//
                    , mStayCuration.getStayBookingDay(), activeReward);

                int mapSize = mPlaceListLayout.getMapItemSize();
                if (mapSize == 0)
                {
                    setVisibility(mViewType, EmptyStatus.EMPTY, true);
                }

                Category category = mStayCuration.getCategory();
                if (Category.ALL.code.equalsIgnoreCase(category.code))
                {
                    mEventListener.onShowActivityEmptyView(mapSize == 0);
                }
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
        if (mStayCuration == null)
        {
            return true;
        }

        PlaceCurationOption placeCurationOption = mStayCuration.getCurationOption();
        if (placeCurationOption == null)
        {
            return true;
        }

        return placeCurationOption.isDefaultFilter();
    }

    @Override
    protected ArrayList<PlaceViewItem> makePlaceList(List<? extends Place> placeList, SortType sortType, boolean hasSection)
    {
        ArrayList<PlaceViewItem> placeViewItemList = new ArrayList<>();

        if (placeList == null || placeList.size() == 0)
        {
            return placeViewItemList;
        }

        int entryPosition = 1;

        if (mPlaceListLayout != null)
        {
            ArrayList<PlaceViewItem> oldList = new ArrayList<>(mPlaceListLayout.getList());

            int oldListSize = oldList.size();
            if (oldListSize > 0)
            {
                int start = oldList.size() - 1;
                int end = oldListSize - 5;
                end = end < 0 ? 0 : end;

                // 5번안에 검사 안끝나면 그냥 종료, 원래는 1번에 검사되어야 함
                for (int i = start; i >= end; i--)
                {
                    PlaceViewItem item = oldList.get(i);
                    if (item.mType == PlaceViewItem.TYPE_ENTRY)
                    {
                        Place place = item.getItem();
                        entryPosition = place.entryPosition + 1;
                        break;
                    }
                }
            }
        }

        String abTest = DailyRemoteConfigPreference.getInstance(getContext()).getKeyRemoteConfigStayRankTestName();
        String abTestType = DailyRemoteConfigPreference.getInstance(getContext()).getKeyRemoteConfigStayRankTestType();

        // 기존 방식 그대로
        //        if (DailyTextUtils.isTextEmpty(abTest, abTestType) == true)
        //        {
        //            if (hasSection == true)
        //            {
        //                String previousRegion = null;
        //                boolean hasDailyChoice = false;
        //
        //                for (Place place : placeList)
        //                {
        //                    // 지역순에만 section 존재함
        //                    if (SortType.DEFAULT == sortType)
        //                    {
        //                        String region = place.districtName;
        //
        //                        if (DailyTextUtils.isTextEmpty(region) == true)
        //                        {
        //                            continue;
        //                        }
        //
        //                        if (place.isDailyChoice == true)
        //                        {
        //                            if (hasDailyChoice == false)
        //                            {
        //                                hasDailyChoice = true;
        //
        //                                PlaceViewItem section = new PlaceViewItem(PlaceViewItem.TYPE_SECTION, mBaseActivity.getResources().getString(R.string.label_dailychoice));
        //                                placeViewItemList.add(section);
        //                            }
        //                        } else
        //                        {
        //                            if (DailyTextUtils.isTextEmpty(previousRegion) == true || region.equalsIgnoreCase(previousRegion) == false)
        //                            {
        //                                previousRegion = region;
        //
        //                                PlaceViewItem section = new PlaceViewItem(PlaceViewItem.TYPE_SECTION, region);
        //                                placeViewItemList.add(section);
        //                            }
        //                        }
        //                    }
        //
        //                    place.entryPosition = entryPosition;
        //                    placeViewItemList.add(new PlaceViewItem(PlaceViewItem.TYPE_ENTRY, place));
        //                    entryPosition++;
        //                }
        //            } else
        //            {
        //                for (Place place : placeList)
        //                {
        //                    place.entryPosition = entryPosition;
        //                    placeViewItemList.add(new PlaceViewItem(PlaceViewItem.TYPE_ENTRY, place));
        //                    entryPosition++;
        //                }
        //            }
        //        } else
        {
            // AB Test 시에는 첫번째 스테이가 데초면 섹션을 만들고 첫번째 스테이가 데초가 아니면 섹션이 없는 것으로 간주한다
            if (hasSection == true && placeList.get(0).isDailyChoice == true)
            {
                String previousRegion = null;
                boolean hasDailyChoice = false;

                for (Place place : placeList)
                {
                    // 지역순에만 section 존재함
                    if (SortType.DEFAULT == sortType)
                    {
                        if (place.isDailyChoice == true)
                        {
                            if (hasDailyChoice == false)
                            {
                                hasDailyChoice = true;

                                PlaceViewItem section = new PlaceViewItem(PlaceViewItem.TYPE_SECTION, mBaseActivity.getResources().getString(R.string.label_dailychoice));
                                placeViewItemList.add(section);
                            }
                        } else
                        {
                            // 처음에 대초여부로 인한 섹션여부 파악
                            if (placeViewItemList.size() > 0)
                            {
                                if (DailyTextUtils.isTextEmpty(previousRegion) == true)
                                {
                                    previousRegion = getString(R.string.label_all);

                                    PlaceViewItem section = new PlaceViewItem(PlaceViewItem.TYPE_SECTION, previousRegion);
                                    placeViewItemList.add(section);
                                }
                            } else
                            {
                                previousRegion = getString(R.string.label_all);
                            }
                        }
                    }

                    place.entryPosition = entryPosition;
                    placeViewItemList.add(new PlaceViewItem(PlaceViewItem.TYPE_ENTRY, place));
                    entryPosition++;
                }
            } else
            {
                for (Place place : placeList)
                {
                    place.entryPosition = entryPosition;
                    placeViewItemList.add(new PlaceViewItem(PlaceViewItem.TYPE_ENTRY, place));
                    entryPosition++;
                }
            }
        }

        return placeViewItemList;
    }

    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////   Listener   //////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////

    protected StayListLayout.OnEventListener mEventListener = new StayListLayout.OnEventListener()
    {
        @Override
        public void onPlaceClick(int position, View view, PlaceViewItem placeViewItem)
        {
            mWishPosition = position;

            ((OnStayListFragmentListener) mOnPlaceListFragmentListener).onStayClick(view, placeViewItem, getPlaceCount());
        }

        public void onPlaceLongClick(int position, View view, PlaceViewItem placeViewItem)
        {
            mWishPosition = position;

            ((OnStayListFragmentListener) mOnPlaceListFragmentListener).onStayLongClick(view, placeViewItem, getPlaceCount());
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
        public void onBottomOptionVisible(boolean visible)
        {
            mOnPlaceListFragmentListener.onBottomOptionVisible(visible);
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
        public void onShowCallDialog()
        {
            startActivity(CallDialogActivity.newInstance(getActivity()));
        }

        @Override
        public void onRegionClick()
        {
            ((OnStayListFragmentListener) mOnPlaceListFragmentListener).onRegionClick();
        }

        @Override
        public void onCalendarClick()
        {
            ((OnStayListFragmentListener) mOnPlaceListFragmentListener).onCalendarClick();
        }

        @Override
        public void onWishClick(int position, PlaceViewItem placeViewItem)
        {
            if (placeViewItem == null || lockUiComponentAndIsLockUiComponent() == true)
            {
                return;
            }

            Stay stay = placeViewItem.getItem();

            mWishPosition = position;

            boolean currentWish = stay.myWish;

            if (DailyHotel.isLogin() == true)
            {
                onChangedWish(position, !currentWish);
            }

            mBaseActivity.startActivityForResult(WishDialogActivity.newInstance(mBaseActivity, ServiceType.HOTEL//
                , stay.index, !currentWish, position, AnalyticsManager.Screen.DAILYHOTEL_LIST), Constants.CODE_REQUEST_ACTIVITY_WISH_DIALOG);

            AnalyticsManager.getInstance(getActivity()).recordEvent(AnalyticsManager.Category.PRODUCT_LIST//
                , AnalyticsManager.Action.WISH_STAY, !currentWish ? AnalyticsManager.Label.ON.toLowerCase() : AnalyticsManager.Label.OFF.toLowerCase(), null);
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

    private StayListNetworkController.OnNetworkControllerListener mNetworkControllerListener = new StayListNetworkController.OnNetworkControllerListener()
    {
        @Override
        public void onStayList(ArrayList<Stay> list, int page, boolean activeReward)
        {
            DailyRemoteConfigPreference.getInstance(mBaseActivity).setKeyRemoteConfigRewardStickerEnabled(activeReward);

            StayListFragment.this.onStayList(list, page, true, activeReward);
        }

        @Override
        public void onError(Call call, Throwable e, boolean onlyReport)
        {
            if (mPlaceListLayout.isRefreshing() == true)
            {
                mPlaceListLayout.setSwipeRefreshing(false);
            }

            StayListFragment.this.onError(call, e, onlyReport);
        }

        @Override
        public void onError(Throwable e)
        {
            if (mPlaceListLayout.isRefreshing() == true)
            {
                mPlaceListLayout.setSwipeRefreshing(false);
            }

            StayListFragment.this.onError(e);
        }

        @Override
        public void onErrorPopupMessage(int msgCode, String message)
        {
            if (mPlaceListLayout.isRefreshing() == true)
            {
                mPlaceListLayout.setSwipeRefreshing(false);
            }

            StayListFragment.this.onErrorPopupMessage(msgCode, message);
        }

        @Override
        public void onErrorToastMessage(String message)
        {
            if (mPlaceListLayout.isRefreshing() == true)
            {
                mPlaceListLayout.setSwipeRefreshing(false);
            }

            StayListFragment.this.onErrorToastMessage(message);
        }

        @Override
        public void onErrorResponse(Call call, Response response)
        {
            if (mPlaceListLayout.isRefreshing() == true)
            {
                mPlaceListLayout.setSwipeRefreshing(false);
            }

            StayListFragment.this.onErrorResponse(call, response);
        }
    };
}
