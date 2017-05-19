package com.twoheart.dailyhotel.screen.home.category.list;

import com.daily.base.util.DailyTextUtils;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Category;
import com.twoheart.dailyhotel.model.Place;
import com.twoheart.dailyhotel.model.PlaceViewItem;
import com.twoheart.dailyhotel.model.Province;
import com.twoheart.dailyhotel.model.Stay;
import com.twoheart.dailyhotel.model.StayCategoryParams;
import com.twoheart.dailyhotel.model.time.StayBookingDay;
import com.twoheart.dailyhotel.place.base.BaseNetworkController;
import com.twoheart.dailyhotel.place.layout.PlaceListLayout;
import com.twoheart.dailyhotel.screen.hotel.list.StayListFragment;
import com.twoheart.dailyhotel.util.Util;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by android_sam on 2017. 5. 15..
 */

public class StayCategoryListFragment extends StayListFragment
{
    private boolean mIsShowLocalPlus;
    private ArrayList<Stay> mLocalPlusList;

    @Override
    protected BaseNetworkController getNetworkController()
    {
        return new StayCategoryListNetworkController(mBaseActivity, mNetworkTag, mNetworkControllerListener);
    }

    @Override
    protected PlaceListLayout getPlaceListLayout()
    {
        if (mStayListLayout == null)
        {
            mStayListLayout = new StayCategoryListLayout(mBaseActivity, mEventListener);
        }
        return mStayListLayout;
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
            // 처음 페이지 요청시 광고 BM 초기화
            mLocalPlusList = null;
        }

        StayBookingDay stayBookingDay = mStayCuration.getStayBookingDay();
        Province province = mStayCuration.getProvince();

        if (province == null || stayBookingDay == null)
        {
            unLockUI();
            Util.restartApp(mBaseActivity);
            return;
        }

        if (mIsShowLocalPlus == true && page <= 1 && mStayCuration.getCurationOption().getSortType() == SortType.DEFAULT)
        {
            // 광고 BM 사용 이고 페이지가 처음이면서 정렬이 Default 일때 광고 BM 요청
            requestLocalPlusList(page, stayBookingDay, province);
        } else
        {
            // 기본 리스트 요청
            requestStayCategoryList(page);
        }

    }

    public void setIsShowLocalPlus(boolean isShowLocalPlus)
    {
        mIsShowLocalPlus = isShowLocalPlus;
    }

    private void requestStayCategoryList(int page)
    {
        if (mStayCuration == null || mStayCuration.getCurationOption() == null//
            || mStayCuration.getCurationOption().getSortType() == null//
            || (mStayCuration.getCurationOption().getSortType() == SortType.DISTANCE && mStayCuration.getLocation() == null))
        {
            unLockUI();
            Util.restartApp(mBaseActivity);
            return;
        }

        StayCategoryParams params = (StayCategoryParams) mStayCuration.toPlaceParams(page, PAGENATION_LIST_SIZE, true);
        ((StayCategoryListNetworkController) mNetworkController).requestStayCategoryList(params);
    }

    private void requestLocalPlusList(int page, StayBookingDay stayBookingDay, Province province)
    {
        //        if (province == null || stayBookingDay == null)
        //        {
        //            unLockUI();
        //            Util.restartApp(mBaseActivity);
        //            return;
        //        }

        if (mStayCuration == null || mStayCuration.getCurationOption() == null//
            || mStayCuration.getCurationOption().getSortType() == null//
            || (mStayCuration.getCurationOption().getSortType() == SortType.DISTANCE && mStayCuration.getLocation() == null))
        {
            unLockUI();
            Util.restartApp(mBaseActivity);
            return;
        }

        StayCategoryParams params = (StayCategoryParams) mStayCuration.toPlaceParams(page, PAGENATION_LIST_SIZE, true);

        //        HashMap<String, Object> hashMap = new HashMap<>();
        //
        //        int nights = 1;
        //        int areaIdx = 0;
        //
        //        String dateCheckIn = stayBookingDay.getCheckInDay("yyyy-MM-dd");
        //
        //        try
        //        {
        //            nights = stayBookingDay.getNights();
        //        } catch (Exception e)
        //        {
        //            ExLog.e(e.toString());
        //        }
        //
        //        int provinceIdx = province.getProvinceIndex();
        //
        //        if (province instanceof Area)
        //        {
        //            areaIdx = ((Area) province).index;
        //        }
        //
        //        hashMap.put("dateCheckIn", dateCheckIn);
        //        hashMap.put("stays", nights);
        //        hashMap.put("provinceIdx", provinceIdx);
        //        hashMap.put("category", mStayCuration.getCategory().code);
        //
        //        if (areaIdx != 0)
        //        {
        //            hashMap.put("areaIdx", areaIdx);
        //        }

        ((StayCategoryListNetworkController) mNetworkController).requestLocalPlusList(params);
    }

    /**
     * 지역 플러스를 포함한 StayList 처리
     *
     * @param localPlusList
     * @param list
     * @param page
     * @param hasSection
     */
    protected void onStayList(ArrayList<Stay> localPlusList, ArrayList<Stay> list, int page, boolean hasSection)
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

        SortType sortType = mStayCuration.getCurationOption().getSortType();
        ArrayList<PlaceViewItem> placeViewItems = makePlaceList(page, localPlusList, list, sortType, hasSection);

        switch (mViewType)
        {
            case LIST:
            {
                mPlaceListLayout.addResultList(getChildFragmentManager(), mViewType, placeViewItems, sortType, mStayCuration.getStayBookingDay());

                int size = mPlaceListLayout.getItemCount();
                if (size == 0)
                {
                    setVisibility(ViewType.GONE, true);
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
                mPlaceListLayout.setList(getChildFragmentManager(), mViewType, placeViewItems, sortType, mStayCuration.getStayBookingDay());

                int mapSize = mPlaceListLayout.getMapItemSize();
                if (mapSize == 0)
                {
                    setVisibility(ViewType.GONE, true);
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

    protected ArrayList<PlaceViewItem> makeLocalPlusList(List<? extends Place> localPlusList, SortType sortType)
    {
        ArrayList<PlaceViewItem> placeViewItemList = new ArrayList<>();

        if (localPlusList == null || localPlusList.size() == 0)
        {
            return placeViewItemList;
        }

        int entryPosition = 1;

        if (SortType.DEFAULT == sortType)
        {
            // 지역순에만 광고 BM 존재함
            PlaceViewItem section = new PlaceViewItem(PlaceViewItem.TYPE_SECTION, mBaseActivity.getResources().getString(R.string.label_local_plus));
            placeViewItemList.add(section);

            for (Place place : localPlusList)
            {
                place.entryPosition = entryPosition;
                placeViewItemList.add(new PlaceViewItem(PlaceViewItem.TYPE_ENTRY, place));
                entryPosition++;
            }
        }

        return placeViewItemList;
    }

    protected ArrayList<PlaceViewItem> makePlaceList(int page, List<? extends Place> localPlusList //
        , List<? extends Place> placeList, SortType sortType, boolean hasSection)
    {
        ArrayList<PlaceViewItem> placeViewItemList = new ArrayList<>();

        int entryPosition = 1;

        if (page <= 1 && SortType.DEFAULT == sortType && localPlusList != null && hasSection == true)
        {
            int localPlusListSize = localPlusList.size();
            entryPosition += localPlusListSize;

            //            mPlaceCount += localPlusListSize; // 광고 BM 에 있는 업장은 기존 리스트에서 내려오는 목록임으로 총 개수는 더하지 않음
            placeViewItemList.addAll(makeLocalPlusList(localPlusList, sortType));
        }

        if (placeList == null || placeList.size() == 0)
        {
            return placeViewItemList;
        }

        mPlaceCount += placeList.size();

        String previousRegion = null;
        boolean hasDailyChoice = false;

        if (mPlaceListLayout != null)
        {
            ArrayList<PlaceViewItem> oldList = new ArrayList<>(mPlaceListLayout.getList());

            int oldListSize = oldList == null ? 0 : oldList.size();
            if (oldListSize > 0)
            {
                int start = oldList == null ? 0 : oldList.size() - 1;
                int end = oldList == null ? 0 : oldListSize - 5;
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

        if (hasSection == true)
        {
            for (Place place : placeList)
            {
                // 지역순에만 section 존재함
                if (SortType.DEFAULT == sortType)
                {
                    String region = place.districtName;

                    if (DailyTextUtils.isTextEmpty(region) == true)
                    {
                        continue;
                    }

                    if (localPlusList != null && localPlusList.size() > 0)
                    {
                        int placeIndex = place.index;
                        boolean isAlreadyShowPlace = false;

                        for (Place localPlace : localPlusList)
                        {
                            // 기존 광고 BM 에 추가 되어있으면 바로 검색 끝
                            if (placeIndex == localPlace.index)
                            {
                                isAlreadyShowPlace = true;
                                break;
                            }
                        }

                        // 기존에 노출 되어있으면 다음 리스트로 넘김
                        if (isAlreadyShowPlace == true)
                        {
                            continue;
                        }
                    }

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
                        if (DailyTextUtils.isTextEmpty(previousRegion) == true || region.equalsIgnoreCase(previousRegion) == false)
                        {
                            previousRegion = region;

                            PlaceViewItem section = new PlaceViewItem(PlaceViewItem.TYPE_SECTION, region);
                            placeViewItemList.add(section);
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

        return placeViewItemList;
    }

    private StayCategoryListNetworkController.OnNetworkControllerListener mNetworkControllerListener = new StayCategoryListNetworkController.OnNetworkControllerListener()
    {
        @Override
        public void onStayList(ArrayList<Stay> list, int page)
        {
            // 지역플러스를 사용해야 함
            StayCategoryListFragment.this.onStayList(mLocalPlusList, list, page, true);
        }

        @Override
        public void onLocalPlusList(ArrayList<Stay> list)
        {
            mLocalPlusList = list;

            // 리퀘스트를 요청하기 이전에 초기 상태일때만 요청함으로 첫페이지 고정
            int page = mViewType == ViewType.LIST ? 1 : 0;
            requestStayCategoryList(page);
        }

        @Override
        public void onError(Call call, Throwable e, boolean onlyReport)
        {
            if (mPlaceListLayout.isRefreshing() == true)
            {
                mPlaceListLayout.setSwipeRefreshing(false);
            }

            StayCategoryListFragment.this.onError(call, e, onlyReport);
        }

        @Override
        public void onError(Throwable e)
        {
            if (mPlaceListLayout.isRefreshing() == true)
            {
                mPlaceListLayout.setSwipeRefreshing(false);
            }

            StayCategoryListFragment.this.onError(e);
        }

        @Override
        public void onErrorPopupMessage(int msgCode, String message)
        {
            if (mPlaceListLayout.isRefreshing() == true)
            {
                mPlaceListLayout.setSwipeRefreshing(false);
            }

            StayCategoryListFragment.this.onErrorPopupMessage(msgCode, message);
        }

        @Override
        public void onErrorToastMessage(String message)
        {
            if (mPlaceListLayout.isRefreshing() == true)
            {
                mPlaceListLayout.setSwipeRefreshing(false);
            }

            StayCategoryListFragment.this.onErrorToastMessage(message);
        }

        @Override
        public void onErrorResponse(Call call, Response response)
        {
            if (mPlaceListLayout.isRefreshing() == true)
            {
                mPlaceListLayout.setSwipeRefreshing(false);
            }

            StayCategoryListFragment.this.onErrorResponse(call, response);
        }
    };
}
