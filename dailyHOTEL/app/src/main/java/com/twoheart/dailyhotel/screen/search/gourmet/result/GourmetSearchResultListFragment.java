package com.twoheart.dailyhotel.screen.search.gourmet.result;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.daily.dailyhotel.entity.GourmetSuggest;
import com.daily.dailyhotel.screen.common.dialog.call.CallDialogActivity;
import com.daily.dailyhotel.screen.common.dialog.wish.WishDialogActivity;
import com.twoheart.dailyhotel.DailyHotel;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Gourmet;
import com.twoheart.dailyhotel.model.GourmetSearchCuration;
import com.twoheart.dailyhotel.model.GourmetSearchParams;
import com.twoheart.dailyhotel.model.PlaceCuration;
import com.twoheart.dailyhotel.model.PlaceViewItem;
import com.twoheart.dailyhotel.place.base.BaseNetworkController;
import com.twoheart.dailyhotel.place.layout.PlaceListLayout;
import com.twoheart.dailyhotel.screen.gourmet.list.GourmetListFragment;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import retrofit2.Call;
import retrofit2.Response;

public class GourmetSearchResultListFragment extends GourmetListFragment
{
    boolean mResetCategory = true;
    boolean mIsDeepLink;

    public interface OnGourmetSearchResultListFragmentListener extends OnGourmetListFragmentListener
    {
        void onGourmetListCount(int count);

        void onResearchClick();

        void onRadiusClick();
    }

    @Override
    protected BaseNetworkController getNetworkController()
    {
        return new GourmetSearchResultListNetworkController(mBaseActivity, mNetworkTag, mNetworkControllerListener);
    }

    @Override
    protected int getLayoutResourceId()
    {
        return R.layout.fragment_gourmet_search_result_list;
    }

    @Override
    public PlaceListLayout getPlaceListLayout()
    {
        if (mPlaceListLayout == null)
        {
            mPlaceListLayout = new GourmetSearchResultListLayout(mBaseActivity, mEventListener);
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

        ((GourmetSearchResultListLayout) mPlaceListLayout).setLocationSearchType(GourmetSuggest.CATEGORY_LOCATION.equalsIgnoreCase(((GourmetSearchCuration) curation).getSuggest().categoryKey));
        ((GourmetSearchResultListLayout) mPlaceListLayout).setEmptyType(((GourmetSearchCuration) curation).getSuggest().categoryKey);
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
                ((GourmetSearchResultListLayout) mPlaceListLayout).updateResultCount(mViewType, -1, -1);
            }
        }

        if (mGourmetCuration == null || mGourmetCuration.getCurationOption() == null//
            || mGourmetCuration.getCurationOption().getSortType() == null//
            || (mGourmetCuration.getCurationOption().getSortType() == SortType.DISTANCE && mGourmetCuration.getLocation() == null) //
            || (((GourmetSearchCuration) mGourmetCuration).getRadius() != 0d && mGourmetCuration.getLocation() == null))
        {
            unLockUI();
            Util.restartApp(mBaseActivity);
            return;
        }

        GourmetSearchParams params = (GourmetSearchParams) mGourmetCuration.toPlaceParams(page, PAGENATION_LIST_SIZE, true);
        ((GourmetSearchResultListNetworkController) mNetworkController).requestGourmetSearchList(params);
    }

    protected void onGourmetList(List<Gourmet> list, int page, int totalCount, int maxCount, //
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
                mPlaceListLayout.addResultList(getChildFragmentManager(), mViewType, placeViewItems, sortType, mGourmetCuration.getGourmetBookingDay()//
                    , false);

                int size = mPlaceListLayout.getItemCount();
                if (size == 0)
                {
                    setVisibility(mViewType, EmptyStatus.EMPTY, true);
                } else
                {
                    mOnPlaceListFragmentListener.onShowMenuBar();
                }

                if (GourmetSuggest.CATEGORY_LOCATION.equalsIgnoreCase(((GourmetSearchCuration) mGourmetCuration).getSuggest().categoryKey) == true)
                {
                    mEventListener.onShowActivityEmptyView(false);
                } else
                {
                    mEventListener.onShowActivityEmptyView(size == 0);
                }
                break;
            }

            case MAP:
            {
                mPlaceListLayout.setList(getChildFragmentManager(), mViewType, placeViewItems, sortType, mGourmetCuration.getGourmetBookingDay()//
                    , false);

                int mapSize = mPlaceListLayout.getMapItemSize();
                if (mapSize == 0)
                {
                    setVisibility(mViewType, EmptyStatus.EMPTY, true);
                } else
                {
                    mOnPlaceListFragmentListener.onShowMenuBar();
                }

                if (GourmetSuggest.CATEGORY_LOCATION.equalsIgnoreCase(((GourmetSearchCuration) mGourmetCuration).getSuggest().categoryKey) == true)
                {
                    mEventListener.onShowActivityEmptyView(false);
                } else
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

    public void setIsDeepLink(boolean isDeepLink)
    {
        mIsDeepLink = isDeepLink;
    }

    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////   Listener   //////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////

    protected GourmetSearchResultListLayout.OnEventListener mEventListener = new GourmetSearchResultListLayout.OnEventListener()
    {
        @Override
        public void onPlaceClick(int position, View view, PlaceViewItem placeViewItem)
        {
            mWishPosition = position;

            ((OnGourmetListFragmentListener) mOnPlaceListFragmentListener).onGourmetClick(view, placeViewItem, getPlaceCount());
        }

        @Override
        public void onPlaceLongClick(int position, View view, PlaceViewItem placeViewItem)
        {
            mWishPosition = position;

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
            ((OnGourmetListFragmentListener) mOnPlaceListFragmentListener).onRegionClick();
        }

        @Override
        public void onCalendarClick()
        {
            ((OnGourmetListFragmentListener) mOnPlaceListFragmentListener).onCalendarClick();
        }

        @Override
        public void onWishClick(int position, PlaceViewItem placeViewItem)
        {
            if (placeViewItem == null || lockUiComponentAndIsLockUiComponent() == true)
            {
                return;
            }

            Gourmet gourmet = placeViewItem.getItem();

            mWishPosition = position;

            boolean currentWish = gourmet.myWish;

            if (DailyHotel.isLogin() == true)
            {
                onChangedWish(position, !currentWish);
            }

            mBaseActivity.startActivityForResult(WishDialogActivity.newInstance(mBaseActivity, ServiceType.GOURMET//
                , gourmet.index, !currentWish, position, AnalyticsManager.Screen.DAILYGOURMET_LIST), Constants.CODE_REQUEST_ACTIVITY_WISH_DIALOG);

            AnalyticsManager.getInstance(getActivity()).recordEvent(AnalyticsManager.Category.PRODUCT_LIST//
                , AnalyticsManager.Action.WISH_GOURMET, !currentWish ? AnalyticsManager.Label.ON.toLowerCase() : AnalyticsManager.Label.OFF.toLowerCase(), null);
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
            ((OnGourmetSearchResultListFragmentListener) mOnPlaceListFragmentListener).onResearchClick();
        }

        @Override
        public void onRadiusClick()
        {
            ((OnGourmetSearchResultListFragmentListener) mOnPlaceListFragmentListener).onRadiusClick();
        }
    };

    private GourmetSearchResultListNetworkController.OnNetworkControllerListener mNetworkControllerListener = new GourmetSearchResultListNetworkController.OnNetworkControllerListener()
    {
        @Override
        public void onGourmetList(ArrayList<Gourmet> list, int page, int totalCount, int maxCount, HashMap<String, Integer> categoryCodeMap, HashMap<String, Integer> categorySequenceMap)
        {
            if (mResetCategory == true)
            {
                mResetCategory = false;

                if (page <= 1)
                {
                    Observable.just(totalCount).subscribe(new Consumer<Integer>()
                    {
                        @Override
                        public void accept(@NonNull Integer integer) throws Exception
                        {
                            int soldOutCount = 0;
                            for (Gourmet gourmet : list)
                            {
                                if (gourmet.availableTicketNumbers == 0 || gourmet.availableTicketNumbers < gourmet.minimumOrderQuantity || gourmet.expired == true)
                                {
                                    soldOutCount++;
                                }
                            }

                            GourmetSearchCuration gourmetSearchCuration = ((GourmetSearchCuration) mGourmetCuration);

                            switch (gourmetSearchCuration.getSuggest().categoryKey)
                            {
                                case GourmetSuggest.CATEGORY_GOURMET:
                                case GourmetSuggest.CATEGORY_REGION:
                                    AnalyticsManager.getInstance(getContext()).recordEvent(AnalyticsManager.Category.AUTO_SEARCH_RESULT//
                                        , gourmetSearchCuration.getSuggest().displayName, integer.toString(), soldOutCount, null);
                                    break;


                                case GourmetSuggest.CATEGORY_LOCATION:
                                    AnalyticsManager.getInstance(getContext()).recordEvent(AnalyticsManager.Category.NEARBY_SEARCH_RESULT//
                                        , ((GourmetSearchCuration) mGourmetCuration).getSuggest().displayName, integer.toString(), soldOutCount, null);
                                    break;

                                case GourmetSuggest.CATEGORY_DIRECT:
                                    AnalyticsManager.getInstance(getContext()).recordEvent(AnalyticsManager.Category.KEYWORD_SEARCH_RESULT//
                                        , ((GourmetSearchCuration) mGourmetCuration).getSuggest().displayName, integer.toString(), soldOutCount, null);
                                    break;
                            }

                            //                            switch (mSearchType)
                            //                            {
                            //                                case AUTOCOMPLETE:
                            //                                    AnalyticsManager.getInstance(getContext()).recordEvent(AnalyticsManager.Category.AUTO_SEARCH_RESULT//
                            //                                        , ((GourmetSearchCuration) mGourmetCuration).getSuggest().displayName, integer.toString(), soldOutCount, null);
                            //                                    break;
                            //
                            //                                case LOCATION:
                            //                                    AnalyticsManager.getInstance(getContext()).recordEvent(AnalyticsManager.Category.NEARBY_SEARCH_RESULT//
                            //                                        , ((GourmetSearchCuration) mGourmetCuration).getSuggest().displayName, integer.toString(), soldOutCount, null);
                            //                                    break;
                            //
                            //                                case RECENTLY_KEYWORD:
                            //                                    AnalyticsManager.getInstance(getContext()).recordEvent(AnalyticsManager.Category.RECENT_SEARCH_RESULT//
                            //                                        , ((GourmetSearchCuration) mGourmetCuration).getKeyword().name, integer.toString(), soldOutCount, null);
                            //                                    break;
                            //
                            //                                case SEARCHES:
                            //                                    AnalyticsManager.getInstance(getContext()).recordEvent(AnalyticsManager.Category.KEYWORD_SEARCH_RESULT//
                            //                                        , ((GourmetSearchCuration) mGourmetCuration).getKeyword().name, integer.toString(), soldOutCount, null);
                            //                                    break;
                            //                            }
                        }
                    }, new Consumer<Throwable>()
                    {
                        @Override
                        public void accept(@NonNull Throwable throwable) throws Exception
                        {

                        }
                    });
                }

                ((OnGourmetSearchResultListFragmentListener) mOnPlaceListFragmentListener).onGourmetListCount(totalCount);

                if (GourmetSuggest.CATEGORY_LOCATION.equalsIgnoreCase(((GourmetSearchCuration) mGourmetCuration).getSuggest().categoryKey) == true)
                {
                    mOnPlaceListFragmentListener.onShowActivityEmptyView(false);

                    GourmetSearchResultListFragment.this.onGourmetList(list, page, totalCount, maxCount, categoryCodeMap, categorySequenceMap, false);
                } else
                {
                    if (list == null || list.size() == 0)
                    {
                        mOnPlaceListFragmentListener.onShowActivityEmptyView(true);
                    } else
                    {
                        mOnPlaceListFragmentListener.onShowActivityEmptyView(false);

                        GourmetSearchResultListFragment.this.onGourmetList(list, page, totalCount, maxCount, categoryCodeMap, categorySequenceMap, false);
                    }
                }
            } else
            {
                GourmetSearchResultListFragment.this.onGourmetList(list, page, totalCount, maxCount, categoryCodeMap, categorySequenceMap, false);
            }

            if (mViewType == ViewType.MAP)
            {
                ((GourmetSearchResultListLayout) mPlaceListLayout).setMapMyLocation(mGourmetCuration.getLocation(), mIsDeepLink == false);
            }

            if (page <= 1)
            {
                ((GourmetSearchResultListLayout) mPlaceListLayout).updateResultCount(mViewType, totalCount, maxCount);
                mOnPlaceListFragmentListener.onSearchCountUpdate(totalCount, maxCount);
            }
        }

        @Override
        public void onError(Call call, Throwable e, boolean onlyReport)
        {
            GourmetSearchResultListFragment.this.onError(call, e, onlyReport);
        }

        @Override
        public void onError(Throwable e)
        {
            GourmetSearchResultListFragment.this.onError(e);
        }

        @Override
        public void onErrorPopupMessage(int msgCode, String message)
        {
            GourmetSearchResultListFragment.this.onErrorPopupMessage(msgCode, message);
        }

        @Override
        public void onErrorToastMessage(String message)
        {
            GourmetSearchResultListFragment.this.onErrorToastMessage(message);
        }

        @Override
        public void onErrorResponse(Call call, Response response)
        {
            GourmetSearchResultListFragment.this.onErrorResponse(call, response);
        }
    };
}
