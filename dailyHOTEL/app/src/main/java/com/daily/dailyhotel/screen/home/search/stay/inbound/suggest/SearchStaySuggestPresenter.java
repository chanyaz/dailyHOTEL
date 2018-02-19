package com.daily.dailyhotel.screen.home.search.stay.inbound.suggest;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.view.View;

import com.daily.base.BaseAnalyticsInterface;
import com.daily.base.exception.DuplicateRunException;
import com.daily.base.exception.PermissionException;
import com.daily.base.exception.ProviderException;
import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ExLog;
import com.daily.base.widget.DailyToast;
import com.daily.dailyhotel.base.BaseExceptionPresenter;
import com.daily.dailyhotel.entity.GoogleAddress;
import com.daily.dailyhotel.entity.GourmetSuggest;
import com.daily.dailyhotel.entity.RecentlyPlace;
import com.daily.dailyhotel.entity.StayBookDateTime;
import com.daily.dailyhotel.entity.StayOutboundSuggest;
import com.daily.dailyhotel.entity.StaySuggest;
import com.daily.dailyhotel.parcel.GourmetSuggestParcel;
import com.daily.dailyhotel.parcel.StayOutboundSuggestParcel;
import com.daily.dailyhotel.parcel.StaySuggestParcel;
import com.daily.dailyhotel.repository.local.RecentlyLocalImpl;
import com.daily.dailyhotel.repository.remote.GoogleAddressRemoteImpl;
import com.daily.dailyhotel.repository.remote.RecentlyRemoteImpl;
import com.daily.dailyhotel.repository.remote.SuggestRemoteImpl;
import com.daily.dailyhotel.storage.preference.DailyPreference;
import com.daily.dailyhotel.storage.preference.DailyRemoteConfigPreference;
import com.daily.dailyhotel.util.DailyLocationExFactory;
import com.google.android.gms.common.api.ResolvableApiException;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Keyword;
import com.twoheart.dailyhotel.network.model.GourmetKeyword;
import com.twoheart.dailyhotel.network.model.StayKeyword;
import com.twoheart.dailyhotel.screen.common.PermissionManagerActivity;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.DailyRecentSearches;
import com.twoheart.dailyhotel.util.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by sheldon
 * Clean Architecture
 */
public class SearchStaySuggestPresenter //
    extends BaseExceptionPresenter<SearchStaySuggestActivity, SearchStaySuggestInterface> //
    implements SearchStaySuggestView.OnEventListener
{
    SearchStaySuggestAnalyticsInterface mAnalytics;

    private SuggestRemoteImpl mSuggestRemoteImpl;
    RecentlyRemoteImpl mRecentlyRemoteImpl;
    private RecentlyLocalImpl mRecentlyLocalImpl;
    GoogleAddressRemoteImpl mGoogleAddressRemoteImpl;
    private Disposable mSuggestDisposable;

    DailyRecentSearches mDailyRecentSearches;
    private StayBookDateTime mStayBookDateTime;
    private List<StaySuggest> mPopularAreaList; // 일단 형식만 맞추기 위해 - 기본 화면을 대신 적용
    private List<StaySuggest> mRecentlySuggestList;
    private List<StaySuggest> mSuggestList;
    private List<GourmetSuggest> mGourmetSuggestList;
    private List<StayOutboundSuggest> mStayOutboundSuggestList;
    ArrayList<String> mStayOutboundKeywordList;
    ArrayList<String> mGourmetKeywordList;
    StaySuggest mLocationSuggest;
    String mKeyword;

    DailyLocationExFactory mDailyLocationExFactory;

    public interface SearchStaySuggestAnalyticsInterface extends BaseAnalyticsInterface
    {
        void onSearchSuggestList(Activity activity, String keyword, boolean hasStaySuggestList);

        void onDeleteRecentlySearch(Activity activity, String keyword);

        void onVoiceSearchClick(Activity activity);

        void onGourmetSuggestClick(Activity activity, String keyword);

        void onStayOutboundSuggestClick(Activity activity, String keyword);

        void onLocationSearchNoAddressClick(Activity activity);

        void onRecentlySearchList(Activity activity, boolean hasData);

        void onRecentlyStayList(Activity activity, boolean hasData);

        void onDeleteRecentlyStay(Activity activity);

        void onScreen(Activity activity);
    }

    public SearchStaySuggestPresenter(@NonNull SearchStaySuggestActivity activity)
    {
        super(activity);
    }

    @NonNull
    @Override
    protected SearchStaySuggestInterface createInstanceViewInterface()
    {
        return new SearchStaySuggestView(getActivity(), this);
    }

    @Override
    public void constructorInitialize(SearchStaySuggestActivity activity)
    {
        setContentView(R.layout.activity_search_stay_suggest_data);

        setAnalytics(new SearchStaySuggestAnalyticsImpl());

        mSuggestRemoteImpl = new SuggestRemoteImpl(activity);
        mRecentlyRemoteImpl = new RecentlyRemoteImpl(activity);
        mRecentlyLocalImpl = new RecentlyLocalImpl(activity);
        mGoogleAddressRemoteImpl = new GoogleAddressRemoteImpl(activity);

        boolean isAgreeLocation = DailyPreference.getInstance(activity).isAgreeTermsOfLocation();

        mLocationSuggest = new StaySuggest(StaySuggest.MENU_TYPE_LOCATION, StaySuggest.CATEGORY_LOCATION //
            , null);
        mLocationSuggest.address = isAgreeLocation ? getString(R.string.label_search_nearby_empty_address) : getString(R.string.label_search_nearby_description);

        List<StaySuggest> popularList = new ArrayList<>();
        popularList.add(new StaySuggest(0, "", getString(R.string.label_search_suggest_recently_empty_description_type_stay)));
        setPopularAreaList(popularList);
        notifyDataSetChanged();

        addCompositeDisposable(Observable.zip(getStayOutboundKeywordList(), getGourmetKeywordList() //
            , new BiFunction<ArrayList<String>, ArrayList<String>, Boolean>()
            {
                @Override
                public Boolean apply(ArrayList<String> stayOutboundKeywordList, ArrayList<String> gourmetKeywordList) throws Exception
                {
                    mStayOutboundKeywordList = stayOutboundKeywordList;
                    mGourmetKeywordList = gourmetKeywordList;
                    return true;
                }
            }).subscribe(new Consumer<Boolean>()
        {
            @Override
            public void accept(Boolean aBoolean) throws Exception
            {

            }
        }, new Consumer<Throwable>()
        {
            @Override
            public void accept(Throwable throwable) throws Exception
            {
                mStayOutboundKeywordList = null;
                mGourmetKeywordList = null;
            }
        }));

        setRefresh(true);
    }

    @Override
    public void setAnalytics(BaseAnalyticsInterface analytics)
    {
        mAnalytics = (SearchStaySuggestAnalyticsInterface) analytics;
    }

    @Override
    public boolean onIntent(Intent intent)
    {
        if (intent == null)
        {
            return true;
        }

        mKeyword = intent.getStringExtra(SearchStaySuggestActivity.INTENT_EXTRA_DATA_KEYWORD);

        String checkInDate = intent.getStringExtra(SearchStaySuggestActivity.INTENT_EXTRA_DATA_CHECK_IN_DATE);
        String checkOutDate = intent.getStringExtra(SearchStaySuggestActivity.INTENT_EXTRA_DATA_CHECK_OUT_DATE);

        if (DailyTextUtils.isTextEmpty(checkInDate, checkOutDate) == true)
        {
            return false;
        }

        try
        {
            mStayBookDateTime = new StayBookDateTime();

            mStayBookDateTime.setCheckInDateTime(checkInDate);
            mStayBookDateTime.setCheckOutDateTime(checkOutDate);
        } catch (Exception e)
        {
            return false;
        }

        return true;
    }

    @Override
    public void onNewIntent(Intent intent)
    {

    }

    @Override
    public void onPostCreate()
    {
        if (getViewInterface() == null)
        {
            return;
        }

        if (DailyTextUtils.isTextEmpty(mKeyword) == false)
        {
            getViewInterface().setKeywordEditText(mKeyword);
            //            return;
        }

        String searchKeywordEditHint = DailyRemoteConfigPreference.getInstance(getActivity()).getKeyRemoteConfigSearchStaySuggestHint();
        if (DailyTextUtils.isTextEmpty(searchKeywordEditHint) == false)
        {
            getViewInterface().setKeywordEditHint(searchKeywordEditHint);
        }

        setCheckVoiceSearchEnabled();

        getViewInterface().showKeyboard();
    }

    @Override
    public void onStart()
    {
        super.onStart();

        if (isRefresh() == true)
        {
            onRefresh(true);
        }

        try
        {
            mAnalytics.onScreen(getActivity());
        } catch (Exception e)
        {
            ExLog.d(e.getMessage());
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (isRefresh() == true)
        {
            onRefresh(true);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onDestroy()
    {
        // 꼭 호출해 주세요.
        super.onDestroy();
    }

    @Override
    public boolean onBackPressed()
    {
        return super.onBackPressed();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        unLockAll();

        switch (requestCode)
        {
            case SearchStaySuggestActivity.REQUEST_CODE_SPEECH_INPUT:
            {
                if (resultCode == Activity.RESULT_OK && null != data)
                {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    mKeyword = result.get(0);
                    getViewInterface().setKeywordEditText(mKeyword);
                }
                break;
            }

            case SearchStaySuggestActivity.REQUEST_CODE_PERMISSION_MANAGER:
            {
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                        startSearchMyLocation(true);
                        break;

                    default:
                        // 권한 설정을 하지 않았을때 아무것도 하지 않음
                        break;
                }
                break;
            }

            case SearchStaySuggestActivity.REQUEST_CODE_SETTING_LOCATION:
                startSearchMyLocation(true);
                break;
        }
    }

    @Override
    protected synchronized void onRefresh(boolean showProgress)
    {
        if (getActivity().isFinishing() == true || isRefresh() == false)
        {
            return;
        }

        if (lock() == true)
        {
            return;
        }

        setRefresh(false);
        screenLock(showProgress);

        // 최근 본 업장
        Observable<ArrayList<RecentlyPlace>> ibObservable = mRecentlyLocalImpl.getRecentlyJSONObject( //
            SearchStaySuggestActivity.RECENTLY_PLACE_MAX_REQUEST_COUNT, Constants.ServiceType.HOTEL) //
            .observeOn(Schedulers.io()).flatMap(new Function<JSONObject, ObservableSource<ArrayList<RecentlyPlace>>>()
            {
                @Override
                public ObservableSource<ArrayList<RecentlyPlace>> apply(JSONObject jsonObject) throws Exception
                {
                    if (jsonObject == null || jsonObject.has("keys") == false)
                    {
                        return Observable.just(new ArrayList<>());
                    }

                    return mRecentlyRemoteImpl.getInboundRecentlyList(jsonObject);
                }
            });

        addCompositeDisposable(Observable.zip(ibObservable //
            , mRecentlyLocalImpl.getRecentlyIndexList(Constants.ServiceType.HOTEL) //
            , new BiFunction<ArrayList<RecentlyPlace>, ArrayList<Integer>, List<StaySuggest>>()
            {
                @Override
                public List<StaySuggest> apply(ArrayList<RecentlyPlace> stayList, ArrayList<Integer> expectedList) throws Exception
                {
                    if (expectedList != null && expectedList.size() > 0)
                    {
                        Collections.sort(stayList, new Comparator<RecentlyPlace>()
                        {
                            @Override
                            public int compare(RecentlyPlace o1, RecentlyPlace o2)
                            {
                                Integer position1 = expectedList.indexOf(o1.index);
                                Integer position2 = expectedList.indexOf(o2.index);

                                return position1.compareTo(position2);
                            }
                        });
                    }

                    // 최근 검색어
                    mDailyRecentSearches = new DailyRecentSearches(DailyPreference.getInstance(getActivity()).getHotelRecentSearches());
                    List<Keyword> keywordList = mDailyRecentSearches.getList();

                    List<StaySuggest> recentlySuggestList = getRecentlySuggestList(keywordList, stayList);
                    setRecentlySuggestList(recentlySuggestList);

                    try
                    {
                        mAnalytics.onRecentlySearchList(getActivity(), keywordList != null && keywordList.size() > 0);
                        mAnalytics.onRecentlyStayList(getActivity(), stayList != null && stayList.size() > 0);
                    } catch (Exception e)
                    {
                        ExLog.d(e.getMessage());
                    }

                    return recentlySuggestList;
                }
            }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<List<StaySuggest>>()
        {
            @Override
            public void accept(List<StaySuggest> staySuggests) throws Exception
            {
                notifyDataSetChanged();

                startSearchMyLocation(false);

                unLockAll();
            }
        }, new Consumer<Throwable>()
        {
            @Override
            public void accept(Throwable throwable) throws Exception
            {
                notifyDataSetChanged();

                startSearchMyLocation(false);

                unLockAll();
            }
        }));
    }

    private void setPopularAreaList(List<StaySuggest> popularAreaList)
    {
        mPopularAreaList = popularAreaList;
    }

    List<StaySuggest> getRecentlySuggestList(List<Keyword> keywordList, List<RecentlyPlace> recentlyPlaceList)
    {
        // 최근 검색어
        ArrayList<StaySuggest> recentlySuggestList = new ArrayList<>();

        if (keywordList != null && keywordList.size() > 0)
        {
            recentlySuggestList.add(new StaySuggest(StaySuggest.MENU_TYPE_RECENTLY_SEARCH //
                , null, getString(R.string.label_search_suggest_recently_search)));

            for (Keyword keyword : keywordList)
            {
                recentlySuggestList.add(new StaySuggest(keyword));
            }
        }

        // 최근 본 업장
        if (recentlyPlaceList != null && recentlyPlaceList.size() > 0)
        {
            recentlySuggestList.add(new StaySuggest(StaySuggest.MENU_TYPE_RECENTLY_STAY //
                , null, getString(R.string.label_recently_stay)));

            for (RecentlyPlace recentlyPlace : recentlyPlaceList)
            {
                recentlySuggestList.add(new StaySuggest(recentlyPlace));
            }
        }

        return recentlySuggestList;
    }

    void setRecentlySuggestList(List<StaySuggest> recentlySuggestList)
    {
        mRecentlySuggestList = recentlySuggestList;
    }

    void setSuggestList(List<StaySuggest> suggestList)
    {
        mSuggestList = suggestList;
    }

    void setGourmetSuggestList(List<GourmetSuggest> suggestList)
    {
        mGourmetSuggestList = suggestList;
    }

    void setStayOutboundSuggestList(List<StayOutboundSuggest> suggestList)
    {
        mStayOutboundSuggestList = suggestList;
    }

    void notifyDataSetChanged()
    {
        if (DailyTextUtils.isTextEmpty(mKeyword) == false)
        {
            if (mSuggestList == null || mSuggestList.size() == 0)
            {
                if (mGourmetSuggestList != null && mGourmetSuggestList.size() > 0)
                {
                    getViewInterface().setGourmetSuggests(mGourmetSuggestList);
                    return;
                }

                if (mStayOutboundSuggestList != null && mStayOutboundSuggestList.size() > 0)
                {
                    getViewInterface().setStayOutboundSuggests(mStayOutboundSuggestList);
                    return;
                }
            }

            getViewInterface().setStaySuggests(mSuggestList);
            return;
        }

        // 추천 검색어의 경우 검색어가 있을때만 작동 해야 함
        setSuggestList(null);
        setGourmetSuggestList(null);
        setStayOutboundSuggestList(null);

        if (mRecentlySuggestList != null && mRecentlySuggestList.size() > 0)
        {
            getViewInterface().setRecentlySuggests(mLocationSuggest, mRecentlySuggestList);
            return;
        }

        getViewInterface().setPopularAreaSuggests(mLocationSuggest, mPopularAreaList);
    }

    @Override
    public void onBackClick()
    {
        addCompositeDisposable(Observable.defer(new Callable<ObservableSource<Boolean>>()
        {
            @Override
            public ObservableSource<Boolean> call() throws Exception
            {
                if (getViewInterface() != null)
                {
                    getViewInterface().hideKeyboard();
                }

                return Observable.just(true);
            }
        }).subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
        {
            @Override
            public void accept(Boolean aBoolean) throws Exception
            {
                getActivity().onBackPressed();
            }
        }));
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onSearchSuggest(String keyword)
    {
        //        clearCompositeDisposable();
        removeCompositeDisposable(mSuggestDisposable);

        if (mStayBookDateTime == null)
        {
            Util.restartApp(getActivity());
            return;
        }

        String checkInDate;
        int nights;

        try
        {
            checkInDate = mStayBookDateTime.getCheckInDateTime("yyyy-MM-dd");
            nights = mStayBookDateTime.getNights();
        } catch (Exception e)
        {
            ExLog.e(e.toString());
            return;
        }

        mKeyword = keyword;

        getViewInterface().setProgressBarVisible(true);

        if (DailyTextUtils.isTextEmpty(keyword) == true)
        {
            setSuggestList(null);
            notifyDataSetChanged();

            getViewInterface().setProgressBarVisible(false);
            unLockAll();
        } else
        {
            mSuggestDisposable = getStaySuggestList(checkInDate, nights, keyword).flatMap(new Function<List<StaySuggest>, ObservableSource<List>>()
            {
                @Override
                public ObservableSource<List> apply(List<StaySuggest> staySuggestList) throws Exception
                {
                    if (staySuggestList == null || staySuggestList.size() == 0)
                    {
                        if (mGourmetKeywordList != null && mGourmetKeywordList.size() > 0 && mGourmetKeywordList.contains(keyword))
                        {
                            return getGourmetSuggestList(checkInDate, keyword);
                        }

                        if (mStayOutboundKeywordList != null && mStayOutboundKeywordList.size() > 0 && mStayOutboundKeywordList.contains(keyword))
                        {
                            return getStayOutboundSuggestList(keyword);
                        }
                    }

                    return Observable.just(staySuggestList);
                }
            }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<List>()
            {
                @Override
                public void accept(List list) throws Exception
                {
                    boolean hasStaySuggestList = false;

                    if (list != null && list.size() > 0)
                    {
                        if (list.get(0) instanceof StayOutboundSuggest)
                        {
                            setStayOutboundSuggestList(list);
                        } else if (list.get(0) instanceof GourmetSuggest)
                        {
                            setGourmetSuggestList(list);
                        } else
                        {
                            hasStaySuggestList = true;
                            setSuggestList(list);
                        }
                    } else
                    {
                        setSuggestList(list);
                    }

                    notifyDataSetChanged();

                    getViewInterface().setProgressBarVisible(false);
                    unLockAll();

                    try
                    {
                        mAnalytics.onSearchSuggestList(getActivity(), keyword, hasStaySuggestList);
                    } catch (Exception e)
                    {
                        ExLog.d(e.getMessage());
                    }
                }
            }, new Consumer<Throwable>()
            {
                @Override
                public void accept(Throwable throwable) throws Exception
                {
                    setSuggestList(null);
                    notifyDataSetChanged();

                    getViewInterface().setProgressBarVisible(false);
                    unLockAll();

                    try
                    {
                        mAnalytics.onSearchSuggestList(getActivity(), keyword, false);
                    } catch (Exception e)
                    {
                        ExLog.d(e.getMessage());
                    }
                }
            });

            addCompositeDisposable(mSuggestDisposable);
        }
    }

    private Observable<List<StaySuggest>> getStaySuggestList(String checkInDate, int nights, final String keyword)
    {
        return mSuggestRemoteImpl.getSuggestsByStayInbound(checkInDate, nights, keyword)//
            .delaySubscription(500, TimeUnit.MILLISECONDS).map(new Function<Pair<String, ArrayList<StayKeyword>>, List<StaySuggest>>()
            {
                @Override
                public List<StaySuggest> apply(Pair<String, ArrayList<StayKeyword>> stringArrayListPair) throws Exception
                {
                    ArrayList<StayKeyword> keywordList = stringArrayListPair.second;
                    ArrayList<StaySuggest> staySuggestList = new ArrayList<>();

                    if (keywordList == null || keywordList.size() == 0)
                    {
                        return staySuggestList;
                    }

                    String oldCategoryKey = null;

                    for (StayKeyword stayKeyword : keywordList)
                    {
                        StaySuggest staySuggest = new StaySuggest(stayKeyword);

                        if (DailyTextUtils.isTextEmpty(oldCategoryKey) || oldCategoryKey.equalsIgnoreCase(staySuggest.categoryKey) == false)
                        {
                            int resId;
                            if (StaySuggest.CATEGORY_STAY.equalsIgnoreCase(staySuggest.categoryKey))
                            {
                                resId = R.string.label_search_suggest_type_stay;
                                oldCategoryKey = staySuggest.categoryKey;
                            } else
                            {
                                resId = R.string.label_search_suggest_type_region;
                                oldCategoryKey = StaySuggest.CATEGORY_REGION;
                            }

                            staySuggestList.add(new StaySuggest(StaySuggest.MENU_TYPE_SUGGEST, null, getString(resId)));
                        }

                        staySuggestList.add(staySuggest);
                    }

                    return staySuggestList;
                }
            }).observeOn(Schedulers.io());
    }

    Observable getGourmetSuggestList(String visitDate, String keyword)
    {
        return mSuggestRemoteImpl.getSuggestsByGourmet(visitDate, keyword)//
            .map(new Function<Pair<String, ArrayList<GourmetKeyword>>, List<GourmetSuggest>>()
            {
                @Override
                public List<GourmetSuggest> apply(Pair<String, ArrayList<GourmetKeyword>> stringArrayListPair) throws Exception
                {
                    ArrayList<GourmetKeyword> keywordList = stringArrayListPair.second;
                    ArrayList<GourmetSuggest> gourmetSuggestList = new ArrayList<>();

                    if (keywordList == null || keywordList.size() == 0)
                    {
                        return gourmetSuggestList;
                    }

                    for (GourmetKeyword gourmetKeyword : keywordList)
                    {
                        GourmetSuggest gourmetSuggest = new GourmetSuggest(gourmetKeyword);

                        if (GourmetSuggest.CATEGORY_GOURMET.equalsIgnoreCase(gourmetSuggest.categoryKey))
                        {
                            gourmetSuggestList.add(gourmetSuggest);
                        }
                    }

                    return gourmetSuggestList;
                }
            }).observeOn(Schedulers.io());
    }

    Observable getStayOutboundSuggestList(String keyword)
    {
        return mSuggestRemoteImpl.getRegionSuggestsByStayOutbound(keyword).observeOn(Schedulers.io());
    }

    @Override
    public void onSuggestClick(StaySuggest staySuggest)
    {
        if (staySuggest == null)
        {
            return;
        }

        if (lock() == true)
        {
            return;
        }

        addRecentSearches(staySuggest);

        getViewInterface().setSuggest(staySuggest.displayName);
        startFinishAction(staySuggest, mKeyword, null);
    }

    @Override
    public void onSuggestClick(GourmetSuggest gourmetSuggest)
    {
        if (gourmetSuggest == null)
        {
            return;
        }

        if (lock() == true)
        {
            return;
        }

        if (GourmetSuggest.MENU_TYPE_DIRECT == gourmetSuggest.menuType)
        {
            StaySuggest staySuggest = new StaySuggest(StaySuggest.MENU_TYPE_DIRECT, StaySuggest.CATEGORY_DIRECT, gourmetSuggest.displayName);
            addRecentSearches(staySuggest);

            getViewInterface().setSuggest(staySuggest.displayName);
            startFinishAction(staySuggest, mKeyword, null);
            return;
        }

        getViewInterface().setSuggest(gourmetSuggest.displayName);

        try
        {
            mAnalytics.onGourmetSuggestClick(getActivity(), mKeyword);
        } catch (Exception e)
        {
            ExLog.d(e.getMessage());
        }

        startFinishAction(gourmetSuggest, mKeyword, null);
    }

    @Override
    public void onSuggestClick(StayOutboundSuggest stayOutboundSuggest)
    {
        if (stayOutboundSuggest == null)
        {
            return;
        }

        if (lock() == true)
        {
            return;
        }

        if (StayOutboundSuggest.MENU_TYPE_DIRECT == stayOutboundSuggest.menuType)
        {
            StaySuggest staySuggest = new StaySuggest(StaySuggest.MENU_TYPE_DIRECT, StaySuggest.CATEGORY_DIRECT, stayOutboundSuggest.display);
            addRecentSearches(staySuggest);

            getViewInterface().setSuggest(staySuggest.displayName);
            startFinishAction(staySuggest, mKeyword, null);
            return;
        }

        getViewInterface().setSuggest(stayOutboundSuggest.display);

        try
        {
            mAnalytics.onStayOutboundSuggestClick(getActivity(), mKeyword);
        } catch (Exception e)
        {
            ExLog.d(e.getMessage());
        }

        startFinishAction(stayOutboundSuggest, mKeyword, null);
    }

    @Override
    public void onRecentlySuggestClick(StaySuggest staySuggest)
    {
        if (staySuggest == null)
        {
            return;
        }

        if (lock() == true)
        {
            return;
        }

        addRecentSearches(staySuggest);

        getViewInterface().setSuggest(staySuggest.displayName);
        startFinishAction(staySuggest, mKeyword, null);
    }

    private Keyword getKeyword(StaySuggest staySuggest)
    {
        if (getActivity() == null || staySuggest == null)
        {
            return null;
        }

        int icon = Keyword.DEFAULT_ICON;
        if (StaySuggest.CATEGORY_STAY.equalsIgnoreCase(staySuggest.categoryKey))
        {
            icon = Keyword.HOTEL_ICON;
        }

        return new Keyword(icon, staySuggest.displayName);
    }

    private void addRecentSearches(StaySuggest staySuggest)
    {
        if (getActivity() == null || staySuggest == null)
        {
            return;
        }

        Keyword keyword = getKeyword(staySuggest);

        if (keyword == null)
        {
            return;
        }

        mDailyRecentSearches.addString(keyword);
        DailyPreference.getInstance(getActivity()).setHotelRecentSearches(mDailyRecentSearches.toString());
    }

    void startFinishAction(StaySuggest staySuggest, String keyword, String analyticsClickType)
    {
        Intent intent = new Intent();
        intent.putExtra(SearchStaySuggestActivity.INTENT_EXTRA_DATA_SUGGEST, new StaySuggestParcel(staySuggest));
        intent.putExtra(SearchStaySuggestActivity.INTENT_EXTRA_DATA_KEYWORD, keyword);

        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void startFinishAction(GourmetSuggest gourmetSuggest, String keyword, String analyticsClickType)
    {
        Intent intent = new Intent();
        intent.putExtra(SearchStaySuggestActivity.INTENT_EXTRA_DATA_SUGGEST, new GourmetSuggestParcel(gourmetSuggest));
        intent.putExtra(SearchStaySuggestActivity.INTENT_EXTRA_DATA_KEYWORD, keyword);

        setResult(Constants.CODE_RESULT_ACTIVITY_SEARCH_GOURMET, intent);
        finish();
    }

    void startFinishAction(StayOutboundSuggest stayOutboundSuggest, String keyword, String analyticsClickType)
    {
        Intent intent = new Intent();
        intent.putExtra(SearchStaySuggestActivity.INTENT_EXTRA_DATA_SUGGEST, new StayOutboundSuggestParcel(stayOutboundSuggest));
        intent.putExtra(SearchStaySuggestActivity.INTENT_EXTRA_DATA_KEYWORD, keyword);

        setResult(Constants.CODE_RESULT_ACTIVITY_SEARCH_STAYOUTBOUND, intent);
        finish();
    }

    @Override
    public void onDeleteRecentlySuggest(int position, StaySuggest staySuggest)
    {
        if (getViewInterface() == null || staySuggest == null || position < 0)
        {
            return;
        }

        if (lock() == true)
        {
            return;
        }

        getViewInterface().removeRecentlyItem(position);

        if (getViewInterface().getRecentlySuggestAllEntryCount() == 0)
        {
            setRecentlySuggestList(null);
            notifyDataSetChanged();
        }

        if (StaySuggest.MENU_TYPE_RECENTLY_STAY == staySuggest.menuType)
        {
            addCompositeDisposable(mRecentlyLocalImpl.deleteRecentlyItem(Constants.ServiceType.HOTEL, staySuggest.stayIndex) //
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
                {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception
                    {
                        unLockAll();
                    }
                }, new Consumer<Throwable>()
                {
                    @Override
                    public void accept(Throwable throwable) throws Exception
                    {
                        unLockAll();
                    }
                }));

            try
            {
                mAnalytics.onDeleteRecentlyStay(getActivity());
            } catch (Exception e)
            {
                ExLog.d(e.getMessage());
            }
        } else
        {
            // 최근 검색어
            Keyword keyword = getKeyword(staySuggest);
            if (keyword == null)
            {
                unLockAll();
                return;
            }

            mDailyRecentSearches.remove(keyword);
            DailyPreference.getInstance(getActivity()).setHotelRecentSearches(mDailyRecentSearches.toString());

            unLockAll();

            try
            {
                mAnalytics.onDeleteRecentlySearch(getActivity(), keyword.name);
            } catch (Exception e)
            {
                ExLog.d(e.getMessage());
            }
        }
    }

    @Override
    public void onVoiceSearchClick()
    {
        if (getViewInterface() == null || isVoiceSearchEnabled() == false)
        {
            return;
        }

        getViewInterface().hideKeyboard();

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.label_search_suggest_voice_search_title));

        try
        {
            startActivityForResult(intent, SearchStaySuggestActivity.REQUEST_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a)
        {
            DailyToast.showToast(getActivity(), R.string.message_search_suggest_voice_search_error, DailyToast.LENGTH_SHORT);
            getViewInterface().setVoiceSearchEnabled(false);
        }

        try
        {
            mAnalytics.onVoiceSearchClick(getActivity());
        } catch (Exception e)
        {
            ExLog.d(e.getMessage());
        }
    }

    @Override
    public void setCheckVoiceSearchEnabled()
    {
        if (getViewInterface() == null)
        {
            return;
        }

        getViewInterface().setVoiceSearchEnabled(isVoiceSearchEnabled());
    }

    @Override
    public void onNearbyClick(StaySuggest staySuggest)
    {
        startSearchMyLocation(true);
    }

    private boolean isVoiceSearchEnabled()
    {
        if (getActivity() == null)
        {
            return false;
        }

        List<ResolveInfo> activities = getActivity().getPackageManager() //
            .queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);

        return activities.size() > 0;
    }

    private Observable<ArrayList<String>> getStayOutboundKeywordList()
    {
        if (getActivity() == null)
        {
            return null;
        }

        return Observable.defer(new Callable<ObservableSource<ArrayList<String>>>()
        {
            @Override
            public ObservableSource<ArrayList<String>> call() throws Exception
            {
                String prefereceText = DailyRemoteConfigPreference.getInstance(getActivity()).getKeyRemoteConfigObSearchKeyword();
                if (DailyTextUtils.isTextEmpty(prefereceText) == true)
                {
                    return Observable.just(new ArrayList<>());
                }

                ArrayList<String> arrayList = new ArrayList<>();
                JSONArray jsonArray = new JSONArray(prefereceText);
                int length = jsonArray.length();
                for (int i = 0; i < length; i++)
                {
                    arrayList.add(jsonArray.getString(i));
                }

                return Observable.just(arrayList);
            }
        }).subscribeOn(Schedulers.io());
    }

    private Observable<ArrayList<String>> getGourmetKeywordList()
    {
        if (getActivity() == null)
        {
            return null;
        }

        return Observable.defer(new Callable<ObservableSource<ArrayList<String>>>()
        {
            @Override
            public ObservableSource<ArrayList<String>> call() throws Exception
            {
                String prefereceText = DailyRemoteConfigPreference.getInstance(getActivity()).getKeyRemoteConfigGourmetSearchKeyword();
                if (DailyTextUtils.isTextEmpty(prefereceText) == true)
                {
                    return Observable.just(new ArrayList<>());
                }

                ArrayList<String> arrayList = new ArrayList<>();
                JSONArray jsonArray = new JSONArray(prefereceText);
                int length = jsonArray.length();
                for (int i = 0; i < length; i++)
                {
                    arrayList.add(jsonArray.getString(i));
                }

                return Observable.just(arrayList);
            }
        }).subscribeOn(Schedulers.io());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    void startSearchMyLocation(boolean isUserClick)
    {
        Observable<Location> observable = searchMyLocation(isUserClick);

        if (observable == null)
        {
            ExLog.e("sam - observable is null");
            return;
        }

        if (isUserClick == true)
        {
            screenLock(true);
        }

        addCompositeDisposable(observable.subscribe(new Consumer<Location>()
        {
            @Override
            public void accept(Location location) throws Exception
            {
                mLocationSuggest.address = getString(R.string.label_search_nearby_empty_address);
                mLocationSuggest.latitude = location.getLatitude();
                mLocationSuggest.longitude = location.getLongitude();

                addCompositeDisposable(mGoogleAddressRemoteImpl.getLocationAddress(location.getLatitude(), location.getLongitude()) //
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<GoogleAddress>()
                    {
                        @Override
                        public void accept(GoogleAddress address) throws Exception
                        {
                            mLocationSuggest.address = address.address;
                            mLocationSuggest.displayName = address.shortAddress;

                            getViewInterface().setNearbyStaySuggest(mLocationSuggest);

                            if (isUserClick == false)
                            {
                                return;
                            }

                            unLockAll();

                            getViewInterface().setSuggest(mLocationSuggest.address);

                            if ("KR".equalsIgnoreCase(address.shortCountry))
                            {
                                startFinishAction(mLocationSuggest, mKeyword, null);
                            } else
                            {
                                StayOutboundSuggest stayOutboundSuggest = new StayOutboundSuggest(0, mLocationSuggest.address);
                                stayOutboundSuggest.categoryKey = StayOutboundSuggest.CATEGORY_LOCATION;
                                stayOutboundSuggest.menuType = StayOutboundSuggest.MENU_TYPE_LOCATION;
                                stayOutboundSuggest.latitude = mLocationSuggest.latitude;
                                stayOutboundSuggest.longitude = mLocationSuggest.longitude;
                                stayOutboundSuggest.country = address.country;
                                stayOutboundSuggest.city = address.shortAddress;

                                startFinishAction(stayOutboundSuggest, mKeyword, null);
                            }

                        }
                    }, new Consumer<Throwable>()
                    {
                        @Override
                        public void accept(Throwable throwable) throws Exception
                        {
                            getViewInterface().setNearbyStaySuggest(mLocationSuggest);

                            if (isUserClick == false)
                            {
                                return;
                            }

                            unLockAll();

                            getViewInterface().setSuggest(mLocationSuggest.address);

                            try
                            {
                                mAnalytics.onLocationSearchNoAddressClick(getActivity());
                            } catch (Exception e)
                            {
                                ExLog.d(e.getMessage());
                            }

                            startFinishAction(mLocationSuggest, mKeyword, null);
                        }
                    }));

            }
        }, new Consumer<Throwable>()
        {
            @Override
            public void accept(Throwable throwable) throws Exception
            {
                String address = null;

                if (throwable instanceof PermissionException)
                {
                    address = getString(R.string.label_search_nearby_description);
                }

                mLocationSuggest.address = address;

                getViewInterface().setNearbyStaySuggest(mLocationSuggest);

                if (isUserClick == false)
                {
                    return;
                }

                unLockAll();
            }
        }));
    }

    private Observable<Location> searchMyLocation(boolean isUserClick)
    {
        if (mDailyLocationExFactory == null)
        {
            mDailyLocationExFactory = new DailyLocationExFactory(getActivity());
        }

        if (mDailyLocationExFactory.measuringLocation() == true)
        {
            // 이미 동작 하고 있음
            return null;
        }

        return new Observable<Location>()
        {
            @Override
            protected void subscribeActual(Observer<? super Location> observer)
            {
                mDailyLocationExFactory.checkLocationMeasure(new DailyLocationExFactory.OnCheckLocationListener()
                {
                    @Override
                    public void onRequirePermission()
                    {
                        observer.onError(new PermissionException());
                    }

                    @Override
                    public void onFailed()
                    {
                        observer.onError(new Exception());
                    }

                    @Override
                    public void onProviderEnabled()
                    {
                        mDailyLocationExFactory.startLocationMeasure(new DailyLocationExFactory.OnLocationListener()
                        {
                            @Override
                            public void onFailed()
                            {
                                observer.onError(new Exception());
                            }

                            @Override
                            public void onAlreadyRun()
                            {
                                observer.onError(new DuplicateRunException());
                            }

                            @Override
                            public void onLocationChanged(Location location)
                            {
                                //                                unLockAll();

                                mDailyLocationExFactory.stopLocationMeasure();

                                if (location == null)
                                {
                                    observer.onError(new NullPointerException());
                                } else
                                {
                                    observer.onNext(location);
                                    observer.onComplete();
                                }
                            }

                            @Override
                            public void onCheckSetting(ResolvableApiException exception)
                            {
                                observer.onError(exception);
                            }
                        });
                    }

                    @Override
                    public void onProviderDisabled()
                    {
                        observer.onError(new ProviderException());
                    }
                });
            }
        }.doOnError(throwable ->
        {
            //            unLockAll();
            if (isUserClick == false)
            {
                // 화면 진입 시 처리 임
                return;
            }

            unLockAll();


            if (throwable instanceof PermissionException)
            {
                Intent intent = PermissionManagerActivity.newInstance(getActivity(), PermissionManagerActivity.PermissionType.ACCESS_FINE_LOCATION);
                startActivityForResult(intent, SearchStaySuggestActivity.REQUEST_CODE_PERMISSION_MANAGER);
            } else if (throwable instanceof ProviderException)
            {
                // 현재 GPS 설정이 꺼져있습니다 설정에서 바꾸어 주세요.
                View.OnClickListener positiveListener = new View.OnClickListener()//
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, SearchStaySuggestActivity.REQUEST_CODE_SETTING_LOCATION);
                    }
                };

                View.OnClickListener negativeListener = new View.OnClickListener()//
                {
                    @Override
                    public void onClick(View v)
                    {
                        getViewInterface().showToast(R.string.message_failed_mylocation, DailyToast.LENGTH_SHORT);
                    }
                };

                DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                        getViewInterface().showToast(R.string.message_failed_mylocation, DailyToast.LENGTH_SHORT);
                    }
                };

                getViewInterface().showSimpleDialog(//
                    getString(R.string.dialog_title_used_gps), getString(R.string.dialog_msg_used_gps), //
                    getString(R.string.dialog_btn_text_dosetting), //
                    getString(R.string.dialog_btn_text_cancel), //
                    positiveListener, negativeListener, cancelListener, null, true);
            } else if (throwable instanceof DuplicateRunException)
            {

            } else if (throwable instanceof ResolvableApiException)
            {
                try
                {
                    ((ResolvableApiException) throwable).startResolutionForResult(getActivity(), SearchStaySuggestActivity.REQUEST_CODE_SETTING_LOCATION);
                } catch (Exception e)
                {
                    getViewInterface().showToast(R.string.message_failed_mylocation, DailyToast.LENGTH_SHORT);
                }
            } else
            {
                getViewInterface().showToast(R.string.message_failed_mylocation, DailyToast.LENGTH_SHORT);
            }
        });
    }
}
