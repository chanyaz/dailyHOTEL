package com.twoheart.dailyhotel.screen.search.stay.result;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.daily.dailyhotel.entity.StaySuggest;
import com.daily.dailyhotel.screen.common.dialog.call.CallDialogActivity;
import com.daily.dailyhotel.screen.common.dialog.wish.WishDialogActivity;
import com.daily.dailyhotel.storage.preference.DailyRemoteConfigPreference;
import com.twoheart.dailyhotel.DailyHotel;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Category;
import com.twoheart.dailyhotel.model.PlaceCuration;
import com.twoheart.dailyhotel.model.PlaceViewItem;
import com.twoheart.dailyhotel.model.Stay;
import com.twoheart.dailyhotel.model.StaySearchCuration;
import com.twoheart.dailyhotel.model.StaySearchParams;
import com.twoheart.dailyhotel.place.activity.PlaceSearchResultActivity;
import com.twoheart.dailyhotel.place.base.BaseNetworkController;
import com.twoheart.dailyhotel.place.layout.PlaceListLayout;
import com.twoheart.dailyhotel.screen.hotel.list.StayListFragment;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import retrofit2.Call;
import retrofit2.Response;

public class StaySearchResultListFragment extends StayListFragment
{
    boolean mResetCategory = true;
    boolean mIsDeepLink;

    public interface OnStaySearchResultListFragmentListener extends OnStayListFragmentListener
    {
        void onCategoryList(List<Category> categoryList);

        void onStayListCount(int count);

        void onResearchClick();

        void onRadiusClick();
    }

    @Override
    protected BaseNetworkController getNetworkController()
    {
        return new StaySearchResultListNetworkController(mBaseActivity, mNetworkTag, onNetworkControllerListener);
    }

    @Override
    protected int getLayoutResourceId()
    {
        return R.layout.fragment_stay_search_result_list;
    }

    @Override
    public PlaceListLayout getPlaceListLayout()
    {
        if (mPlaceListLayout == null)
        {
            mPlaceListLayout = new StaySearchResultListLayout(mBaseActivity, mEventListener);
        }

        return mPlaceListLayout;
    }

    @Override
    public void setPlaceCuration(PlaceCuration curation)
    {
        if (mPlaceListLayout == null)
        {
            return;
        }

        super.setPlaceCuration(curation);

        ((StaySearchResultListLayout) mPlaceListLayout).setLocationSearchType(StaySuggest.CATEGORY_LOCATION.equalsIgnoreCase(((StaySearchCuration) curation).getSuggest().categoryKey));
        ((StaySearchResultListLayout) mPlaceListLayout).setEmptyType(((StaySearchCuration) curation).getSuggest().categoryKey);
    }

    @Override
    protected void refreshList(boolean isShowProgress, int page)
    {
        // 더보기 시 unlock 걸지않음
        if (page <= 1)
        {
            lockUI(isShowProgress);

            if (isShowProgress == true)
            {
                // 새로 검색이 될경우에는 결과개수를 보여주는 부분은 안보이게 한다.
                ((StaySearchResultListLayout) mPlaceListLayout).updateResultCount(mViewType, -1, -1);
            }
        }

        if (mStayCuration == null || mStayCuration.getCurationOption() == null//
            || mStayCuration.getCurationOption().getSortType() == null//
            || (mStayCuration.getCurationOption().getSortType() == SortType.DISTANCE && mStayCuration.getLocation() == null) //
            || (((StaySearchCuration) mStayCuration).getRadius() != 0d && mStayCuration.getLocation() == null))
        {
            unLockUI();
            Util.restartApp(mBaseActivity); // 제거 할 것인지 고민 필요.
            return;
        }

        StaySearchParams params = (StaySearchParams) mStayCuration.toPlaceParams(page, PAGENATION_LIST_SIZE, true);
        String abTestType = DailyRemoteConfigPreference.getInstance(getContext()).getKeyRemoteConfigStayRankTestType();

        ((StaySearchResultListNetworkController) mNetworkController).requestStaySearchList(params, abTestType);
    }

    @Override
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
                } else
                {
                    mOnPlaceListFragmentListener.onShowMenuBar();
                }

                Category category = mStayCuration.getCategory();

                if (StaySuggest.CATEGORY_LOCATION.equalsIgnoreCase(((StaySearchCuration) mStayCuration).getSuggest().categoryKey) == true)
                {
                    mEventListener.onShowActivityEmptyView(false);
                } else
                {
                    if (Category.ALL.code.equalsIgnoreCase(category.code) == true)
                    {
                        mEventListener.onShowActivityEmptyView(size == 0);
                    }
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
                } else
                {
                    mOnPlaceListFragmentListener.onShowMenuBar();
                }

                Category category = mStayCuration.getCategory();

                if (StaySuggest.CATEGORY_LOCATION.equalsIgnoreCase(((StaySearchCuration) mStayCuration).getSuggest().categoryKey) == true)
                {
                    mEventListener.onShowActivityEmptyView(mapSize == 0);
                } else
                {
                    if (Category.ALL.code.equalsIgnoreCase(category.code) == true)
                    {
                        mEventListener.onShowActivityEmptyView(mapSize == 0);
                    }
                }
                break;
            }

            default:
                break;
        }

        unLockUI();
        mPlaceListLayout.setSwipeRefreshing(false);
    }

    public void setIsDeepLink(boolean isDeepLink)
    {
        mIsDeepLink = isDeepLink;
    }


    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////   Listener   //////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////

    protected StaySearchResultListLayout.OnEventListener mEventListener = new StaySearchResultListLayout.OnEventListener()
    {
        @Override
        public void onPlaceClick(int position, View view, PlaceViewItem placeViewItem)
        {
            mWishPosition = position;

            ((OnStayListFragmentListener) mOnPlaceListFragmentListener).onStayClick(view, placeViewItem, getPlaceCount());
        }

        @Override
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

        @Override
        public void onResearchClick()
        {
            ((OnStaySearchResultListFragmentListener) mOnPlaceListFragmentListener).onResearchClick();
        }

        @Override
        public void onRadiusClick()
        {
            ((OnStaySearchResultListFragmentListener) mOnPlaceListFragmentListener).onRadiusClick();
        }
    };

    private StaySearchResultListNetworkController.OnNetworkControllerListener onNetworkControllerListener = new StaySearchResultListNetworkController.OnNetworkControllerListener()
    {
        @Override
        public void onStayList(ArrayList<Stay> list, int page, int totalCount, int maxCount, List<Category> categoryList, boolean activeReward)
        {
            // 첫페이지 호출시에 카테고리 목록 조절
            if (mResetCategory == true)
            {
                mResetCategory = false;

                if (page <= 1 && Category.ALL.code.equalsIgnoreCase(mStayCuration.getCategory().code) == true)
                {
                    ((OnStaySearchResultListFragmentListener) mOnPlaceListFragmentListener).onCategoryList(categoryList);
                    mOnPlaceListFragmentListener.onSearchCountUpdate(totalCount, maxCount);

                    Observable.just(totalCount).subscribe(new Consumer<Integer>()
                    {
                        @Override
                        public void accept(@NonNull Integer integer) throws Exception
                        {
                            int soldOutCount = 0;
                            for (Stay stay : list)
                            {
                                if (stay.availableRooms == 0)
                                {
                                    soldOutCount++;
                                }
                            }

                            StaySearchCuration staySearchCuration = ((StaySearchCuration) mStayCuration);

                            switch (staySearchCuration.getSuggest().categoryKey)
                            {
                                //                                case AUTOCOMPLETE:
                                case StaySuggest.CATEGORY_STAY:
                                case StaySuggest.CATEGORY_REGION:
                                    AnalyticsManager.getInstance(getContext()).recordEvent(AnalyticsManager.Category.AUTO_SEARCH_RESULT//
                                        , staySearchCuration.getSuggest().displayName, integer.toString(), soldOutCount, null);
                                    break;


                                //                                case LOCATION:
                                case StaySuggest.CATEGORY_LOCATION:
                                    AnalyticsManager.getInstance(getContext()).recordEvent(AnalyticsManager.Category.NEARBY_SEARCH_RESULT//
                                        , ((StaySearchCuration) mStayCuration).getSuggest().displayName, integer.toString(), soldOutCount, null);
                                    break;

                                //                                case RECENTLY_KEYWORD:
                                //                                    AnalyticsManager.getInstance(getContext()).recordEvent(AnalyticsManager.Category.RECENT_SEARCH_RESULT//
                                //                                        , ((StaySearchCuration) mStayCuration).getKeyword().name, integer.toString(), soldOutCount, null);
                                //                                    break;

                                //                                case SEARCHES:
                                case StaySuggest.CATEGORY_DIRECT:
                                    AnalyticsManager.getInstance(getContext()).recordEvent(AnalyticsManager.Category.KEYWORD_SEARCH_RESULT//
                                        , staySearchCuration.getSuggest().displayName, integer.toString(), soldOutCount, null);
                                    break;
                            }
                        }
                    }, new Consumer<Throwable>()
                    {
                        @Override
                        public void accept(@NonNull Throwable throwable) throws Exception
                        {

                        }
                    });
                }

                ((OnStaySearchResultListFragmentListener) mOnPlaceListFragmentListener).onStayListCount(totalCount);

                // 카테고리 개수가 실제로 존재하거나 혹은 주변 검색인데 필터, 반경이 디폴트 값이 아닌 경우
                if ((categoryList != null && categoryList.size() > 0)//
                    || ((StaySuggest.CATEGORY_LOCATION.equalsIgnoreCase(((StaySearchCuration) mStayCuration).getSuggest().categoryKey) == true//
                    && (mStayCuration.getCurationOption().isDefaultFilter() == false
                    || ((StaySearchCuration) mStayCuration).getRadius() != PlaceSearchResultActivity.DEFAULT_SEARCH_RADIUS))))
                {
                    StaySearchResultListFragment.this.onStayList(list, page, false, activeReward);
                }
            } else
            {
                StaySearchResultListFragment.this.onStayList(list, page, false, activeReward);
            }

            if (mViewType == ViewType.MAP)
            {
                ((StaySearchResultListLayout) mPlaceListLayout).setMapMyLocation(mStayCuration.getLocation(), mIsDeepLink == false);
            }

            if (page <= 1)
            {
                ((StaySearchResultListLayout) mPlaceListLayout).updateResultCount(mViewType, totalCount, maxCount);
            }
        }

        @Override
        public void onError(Call call, Throwable e, boolean onlyReport)
        {
            StaySearchResultListFragment.this.onError(call, e, onlyReport);
        }

        @Override
        public void onError(Throwable e)
        {
            StaySearchResultListFragment.this.onError(e);
        }

        @Override
        public void onErrorPopupMessage(int msgCode, String message)
        {
            StaySearchResultListFragment.this.onErrorPopupMessage(msgCode, message);
        }

        @Override
        public void onErrorToastMessage(String message)
        {
            StaySearchResultListFragment.this.onErrorToastMessage(message);
        }

        @Override
        public void onErrorResponse(Call call, Response response)
        {
            StaySearchResultListFragment.this.onErrorResponse(call, response);
        }
    };
}
