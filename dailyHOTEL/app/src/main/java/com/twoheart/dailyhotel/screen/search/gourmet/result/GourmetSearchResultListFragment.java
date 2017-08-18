package com.twoheart.dailyhotel.screen.search.gourmet.result;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Gourmet;
import com.twoheart.dailyhotel.model.GourmetSearchCuration;
import com.twoheart.dailyhotel.model.GourmetSearchParams;
import com.twoheart.dailyhotel.model.PlaceCuration;
import com.twoheart.dailyhotel.place.base.BaseNetworkController;
import com.twoheart.dailyhotel.place.layout.PlaceListLayout;
import com.twoheart.dailyhotel.screen.gourmet.list.GourmetListFragment;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import retrofit2.Call;
import retrofit2.Response;

public class GourmetSearchResultListFragment extends GourmetListFragment
{
    boolean mResetCategory = true;
    boolean mIsDeepLink;
    private SearchType mSearchType;

    public interface OnGourmetSearchResultListFragmentListener extends OnGourmetListFragmentListener
    {
        void onGourmetListCount(int count);
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

    public void setSearchType(SearchType searchType)
    {
        mSearchType = searchType;
    }

    @Override
    public void setPlaceCuration(PlaceCuration curation)
    {
        if (mPlaceListLayout == null)
        {
            return;
        }

        super.setPlaceCuration(curation);

        ((GourmetSearchResultListLayout) mPlaceListLayout).setSearchType(mSearchType);
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

    public void setIsDeepLink(boolean isDeepLink)
    {
        mIsDeepLink = isDeepLink;
    }

    private GourmetSearchResultListNetworkController.OnNetworkControllerListener mNetworkControllerListener = new GourmetSearchResultListNetworkController.OnNetworkControllerListener()
    {
        @Override
        public void onGourmetList(ArrayList<Gourmet> list, int page, int totalCount, int maxCount, HashMap<String, Integer> categoryCodeMap, HashMap<String, Integer> categorySequenceMap)
        {
            if (mResetCategory == true)
            {
                mResetCategory = false;

                ((OnGourmetSearchResultListFragmentListener) mOnPlaceListFragmentListener).onGourmetListCount(totalCount);

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

                            switch (mSearchType)
                            {
                                case AUTOCOMPLETE:
                                    AnalyticsManager.getInstance(getContext()).recordEvent(AnalyticsManager.Category.AUTO_SEARCH_RESULT//
                                        , ((GourmetSearchCuration) mGourmetCuration).getKeyword().name, integer.toString(), soldOutCount, null);
                                    break;

                                case LOCATION:
                                    AnalyticsManager.getInstance(getContext()).recordEvent(AnalyticsManager.Category.NEARBY_SEARCH_RESULT//
                                        , ((GourmetSearchCuration) mGourmetCuration).getKeyword().name, integer.toString(), soldOutCount, null);
                                    break;

                                case RECENTLY_KEYWORD:
                                    AnalyticsManager.getInstance(getContext()).recordEvent(AnalyticsManager.Category.RECENT_SEARCH_RESULT//
                                        , ((GourmetSearchCuration) mGourmetCuration).getKeyword().name, integer.toString(), soldOutCount, null);
                                    break;

                                case SEARCHES:
                                    AnalyticsManager.getInstance(getContext()).recordEvent(AnalyticsManager.Category.KEYWORD_SEARCH_RESULT//
                                        , ((GourmetSearchCuration) mGourmetCuration).getKeyword().name, integer.toString(), soldOutCount, null);
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
            }

            GourmetSearchResultListFragment.this.onGourmetList(list, page, totalCount, maxCount, categoryCodeMap, categorySequenceMap, false);

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
