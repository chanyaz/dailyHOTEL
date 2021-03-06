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
import com.daily.dailyhotel.entity.StayBookDateTime;
import com.daily.dailyhotel.entity.StayOutboundSuggest;
import com.daily.dailyhotel.entity.StaySuggest;
import com.daily.dailyhotel.parcel.GourmetSuggestParcel;
import com.daily.dailyhotel.parcel.StayOutboundSuggestParcel;
import com.daily.dailyhotel.parcel.StaySuggestParcel;
import com.daily.dailyhotel.repository.local.RecentlyLocalImpl;
import com.daily.dailyhotel.repository.local.SuggestLocalImpl;
import com.daily.dailyhotel.repository.local.model.RecentlyDbPlace;
import com.daily.dailyhotel.repository.remote.GoogleAddressRemoteImpl;
import com.daily.dailyhotel.repository.remote.RecentlyRemoteImpl;
import com.daily.dailyhotel.repository.remote.SuggestRemoteImpl;
import com.daily.dailyhotel.storage.preference.DailyPreference;
import com.daily.dailyhotel.storage.preference.DailyRemoteConfigPreference;
import com.daily.dailyhotel.util.DailyLocationExFactory;
import com.google.android.gms.common.api.ResolvableApiException;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.screen.common.PermissionManagerActivity;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.Util;

import org.json.JSONArray;

import java.util.ArrayList;
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
    SuggestLocalImpl mSuggestLocalImpl;
    RecentlyRemoteImpl mRecentlyRemoteImpl;
    private RecentlyLocalImpl mRecentlyLocalImpl;
    GoogleAddressRemoteImpl mGoogleAddressRemoteImpl;
    private Disposable mSuggestDisposable;

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
    boolean mIsResearch;

    DailyLocationExFactory mDailyLocationExFactory;

    public interface SearchStaySuggestAnalyticsInterface extends BaseAnalyticsInterface
    {
        void onSearchSuggestList(Activity activity, String keyword, boolean hasStaySuggestList);

        void onDeleteRecentlySearch(Activity activity, String keyword);

        void onVoiceSearchClick(Activity activity);

        void onGourmetSuggestClick(Activity activity, String keyword);

        void onStayOutboundSuggestClick(Activity activity, String keyword);

        void onLocationSearchNoAddressClick(Activity activity);

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

        mAnalytics = new SearchStaySuggestAnalyticsImpl();

        mSuggestRemoteImpl = new SuggestRemoteImpl();
        mSuggestLocalImpl = new SuggestLocalImpl();
        mRecentlyRemoteImpl = new RecentlyRemoteImpl();
        mRecentlyLocalImpl = new RecentlyLocalImpl();
        mGoogleAddressRemoteImpl = new GoogleAddressRemoteImpl();

        boolean isAgreeLocation = DailyPreference.getInstance(activity).isAgreeTermsOfLocation();

        StaySuggest.Location location = new StaySuggest.Location();
        location.address = isAgreeLocation ? getString(R.string.label_search_nearby_empty_address) : getString(R.string.label_search_nearby_description);
        mLocationSuggest = new StaySuggest(StaySuggest.MenuType.LOCATION, location);

        List<StaySuggest> popularList = new ArrayList<>();
        popularList.add(new StaySuggest(StaySuggest.MenuType.UNKNOWN //
            , new StaySuggest.SuggestItem(getString(R.string.label_search_suggest_recently_empty_description_type_stay))));
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

        mIsResearch = intent.getBooleanExtra(SearchStaySuggestActivity.INTENT_EXTRA_DATA_IS_RESEARCH, false);

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


        addCompositeDisposable(Observable.zip(mRecentlyLocalImpl.getRecentlyTypeList(getActivity(), Constants.ServiceType.HOTEL) //
            , mSuggestLocalImpl.getRecentlyStaySuggestList(getActivity(), 10) //
            , new BiFunction<ArrayList<RecentlyDbPlace>, List<StaySuggest>, List<StaySuggest>>()
            {
                @Override
                public List<StaySuggest> apply(ArrayList<RecentlyDbPlace> placeList, List<StaySuggest> searchList) throws Exception
                {
                    List<StaySuggest> recentlySuggestList = getRecentlySuggestList(searchList, placeList);
                    setRecentlySuggestList(recentlySuggestList);

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

    List<StaySuggest> getRecentlySuggestList(List<StaySuggest> recentlySearchList, List<RecentlyDbPlace> recentlyPlaceList)
    {
        // 최근 검색어
        ArrayList<StaySuggest> recentlySuggestList = new ArrayList<>();

        if (recentlySearchList != null && recentlySearchList.size() > 0)
        {
            recentlySuggestList.add(new StaySuggest(StaySuggest.MenuType.RECENTLY_SEARCH //
                , new StaySuggest.Section(getString(R.string.label_search_suggest_recently_search))));

            recentlySuggestList.addAll(recentlySearchList);
        }

        // 최근 본 업장
        if (recentlyPlaceList != null && recentlyPlaceList.size() > 0)
        {
            recentlySuggestList.add(new StaySuggest(StaySuggest.MenuType.RECENTLY_STAY //
                , new StaySuggest.Section(getString(R.string.label_recently_stay))));

            int maxSize = Math.min(10, recentlyPlaceList.size());

            for (int i = 0; i < maxSize; i++)
            {
                RecentlyDbPlace recentlyPlace = recentlyPlaceList.get(i);

                StaySuggest.Stay stay = new StaySuggest.Stay();
                StaySuggest.AreaGroup areaGroup = new StaySuggest.AreaGroup();

                areaGroup.name = recentlyPlace.regionName;

                stay.index = recentlyPlace.index;
                stay.name = recentlyPlace.name;
                stay.areaGroup = areaGroup;

                recentlySuggestList.add(new StaySuggest(StaySuggest.MenuType.RECENTLY_STAY, stay));
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
            if (mIsResearch == false && (mSuggestList == null || mSuggestList.size() == 0))
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
            mSuggestDisposable = mSuggestRemoteImpl.getSuggestByStay(getActivity(), checkInDate, nights, keyword) //
                .delaySubscription(500, TimeUnit.MILLISECONDS).flatMap(new Function<List<StaySuggest>, ObservableSource<List>>()
                {
                    @Override
                    public ObservableSource<List> apply(List<StaySuggest> staySuggestList) throws Exception
                    {
                        if (mIsResearch == false && (staySuggestList == null || staySuggestList.size() == 0))
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

    Observable getGourmetSuggestList(String visitDate, String keyword)
    {
        return mSuggestRemoteImpl.getSuggestsByGourmet(getActivity(), visitDate, keyword).observeOn(Schedulers.io());
    }

    Observable getStayOutboundSuggestList(String keyword)
    {
        return mSuggestRemoteImpl.getSuggestsByStayOutbound(getActivity(), keyword).observeOn(Schedulers.io());
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

        addCompositeDisposable(mSuggestLocalImpl.addRecentlyStaySuggest(getActivity(), staySuggest, mKeyword) //
            .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
            {
                @Override
                public void accept(Boolean aBoolean) throws Exception
                {
                    getViewInterface().setSuggest(staySuggest.getText1());
                    startFinishAction(staySuggest, mKeyword);
                }
            }, new Consumer<Throwable>()
            {
                @Override
                public void accept(Throwable throwable) throws Exception
                {
                    getViewInterface().setSuggest(staySuggest.getText1());
                    startFinishAction(staySuggest, mKeyword);
                }
            }));
    }

    @Override
    public void onSuggestClick(GourmetSuggest gourmetSuggest)
    {
        if (gourmetSuggest == null)
        {
            return;
        }

        if (gourmetSuggest.getSuggestItem() == null)
        {
            return;
        }

        if (lock() == true)
        {
            return;
        }

        if (GourmetSuggest.MenuType.DIRECT == gourmetSuggest.menuType)
        {
            StaySuggest staySuggest = new StaySuggest(StaySuggest.MenuType.DIRECT, new StaySuggest.Direct(gourmetSuggest.getText1()));

            addCompositeDisposable(mSuggestLocalImpl.addRecentlyStaySuggest(getActivity(), staySuggest, mKeyword) //
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
                {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception
                    {
                        getViewInterface().setSuggest(staySuggest.getText1());
                        startFinishAction(staySuggest, mKeyword);
                    }
                }, new Consumer<Throwable>()
                {
                    @Override
                    public void accept(Throwable throwable) throws Exception
                    {
                        getViewInterface().setSuggest(staySuggest.getText1());
                        startFinishAction(staySuggest, mKeyword);
                    }
                }));
            return;
        }

        getViewInterface().setSuggest(gourmetSuggest.getText1());

        try
        {
            mAnalytics.onGourmetSuggestClick(getActivity(), mKeyword);
        } catch (Exception e)
        {
            ExLog.d(e.getMessage());
        }

        startFinishAction(gourmetSuggest, mKeyword);
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
            StaySuggest staySuggest = new StaySuggest(StaySuggest.MenuType.DIRECT, new StaySuggest.Direct(stayOutboundSuggest.display));

            addCompositeDisposable(mSuggestLocalImpl.addRecentlyStaySuggest(getActivity(), staySuggest, mKeyword) //
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
                {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception
                    {
                        getViewInterface().setSuggest(staySuggest.getText1());
                        startFinishAction(staySuggest, mKeyword);
                    }
                }, new Consumer<Throwable>()
                {
                    @Override
                    public void accept(Throwable throwable) throws Exception
                    {
                        getViewInterface().setSuggest(staySuggest.getText1());
                        startFinishAction(staySuggest, mKeyword);
                    }
                }));
            return;
        }

        getViewInterface().setSuggest(stayOutboundSuggest.displayText);

        try
        {
            mAnalytics.onStayOutboundSuggestClick(getActivity(), mKeyword);
        } catch (Exception e)
        {
            ExLog.d(e.getMessage());
        }

        startFinishAction(stayOutboundSuggest, mKeyword);
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

        addCompositeDisposable(mSuggestLocalImpl.addRecentlyStaySuggest(getActivity(), staySuggest, mKeyword) //
            .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
            {
                @Override
                public void accept(Boolean aBoolean) throws Exception
                {
                    getViewInterface().setSuggest(staySuggest.getText1());
                    startFinishAction(staySuggest, mKeyword);
                }
            }, new Consumer<Throwable>()
            {
                @Override
                public void accept(Throwable throwable) throws Exception
                {
                    getViewInterface().setSuggest(staySuggest.getText1());
                    startFinishAction(staySuggest, mKeyword);
                }
            }));
    }

    void startFinishAction(StaySuggest staySuggest, String keyword)
    {
        Intent intent = new Intent();
        intent.putExtra(SearchStaySuggestActivity.INTENT_EXTRA_DATA_SUGGEST, new StaySuggestParcel(staySuggest));
        intent.putExtra(SearchStaySuggestActivity.INTENT_EXTRA_DATA_KEYWORD, keyword);
        intent.putExtra(SearchStaySuggestActivity.INTENT_EXTRA_DATA_ORIGIN_SERVICE_TYPE, Constants.ServiceType.HOTEL.name());

        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void startFinishAction(GourmetSuggest gourmetSuggest, String keyword)
    {
        Intent intent = new Intent();
        intent.putExtra(SearchStaySuggestActivity.INTENT_EXTRA_DATA_SUGGEST, new GourmetSuggestParcel(gourmetSuggest));
        intent.putExtra(SearchStaySuggestActivity.INTENT_EXTRA_DATA_KEYWORD, keyword);
        intent.putExtra(SearchStaySuggestActivity.INTENT_EXTRA_DATA_ORIGIN_SERVICE_TYPE, Constants.ServiceType.HOTEL.name());

        setResult(Constants.CODE_RESULT_ACTIVITY_SEARCH_GOURMET, intent);
        finish();
    }

    void startFinishAction(StayOutboundSuggest stayOutboundSuggest, String keyword)
    {
        Intent intent = new Intent();
        intent.putExtra(SearchStaySuggestActivity.INTENT_EXTRA_DATA_SUGGEST, new StayOutboundSuggestParcel(stayOutboundSuggest));
        intent.putExtra(SearchStaySuggestActivity.INTENT_EXTRA_DATA_KEYWORD, keyword);
        intent.putExtra(SearchStaySuggestActivity.INTENT_EXTRA_DATA_ORIGIN_SERVICE_TYPE, Constants.ServiceType.HOTEL.name());

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

        StaySuggest.SuggestItem suggestItem = staySuggest.getSuggestItem();
        if (suggestItem == null)
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

        if (StaySuggest.MenuType.RECENTLY_STAY == staySuggest.menuType)
        {
            StaySuggest.Stay stay = (StaySuggest.Stay) suggestItem;

            addCompositeDisposable(mRecentlyLocalImpl.deleteRecentlyItem(getActivity(), Constants.ServiceType.HOTEL, stay.index) //
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
            addCompositeDisposable(mSuggestLocalImpl.deleteRecentlyStaySuggest(getActivity(), staySuggest) //
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
                {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception
                    {
                        unLockAll();

                        try
                        {
                            mAnalytics.onDeleteRecentlySearch(getActivity(), suggestItem.name);
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
                        unLockAll();
                    }
                }));
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

        StaySuggest.Location itemLocation = (StaySuggest.Location) mLocationSuggest.getSuggestItem();

        addCompositeDisposable(observable.subscribe(new Consumer<Location>()
        {
            @Override
            public void accept(Location location) throws Exception
            {
                itemLocation.name = getString(R.string.label_search_nearby_empty_address);
                itemLocation.address = getString(R.string.label_search_nearby_empty_address);
                itemLocation.latitude = location.getLatitude();
                itemLocation.longitude = location.getLongitude();

                addCompositeDisposable(mGoogleAddressRemoteImpl.getLocationAddress(location.getLatitude(), location.getLongitude()) //
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<GoogleAddress>()
                    {
                        @Override
                        public void accept(GoogleAddress address) throws Exception
                        {
                            itemLocation.address = address.address;
                            itemLocation.name = address.shortAddress;

                            getViewInterface().setNearbyStaySuggest(mLocationSuggest);

                            if (isUserClick == false)
                            {
                                return;
                            }


                            if ("KR".equalsIgnoreCase(address.shortCountry))
                            {
                                addCompositeDisposable(mSuggestLocalImpl.addRecentlyStaySuggest(getActivity(), mLocationSuggest, mKeyword) //
                                    .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
                                    {
                                        @Override
                                        public void accept(Boolean aBoolean) throws Exception
                                        {
                                            unLockAll();

                                            getViewInterface().setSuggest(itemLocation.address);
                                            startFinishAction(mLocationSuggest, mKeyword);
                                        }
                                    }, new Consumer<Throwable>()
                                    {
                                        @Override
                                        public void accept(Throwable throwable) throws Exception
                                        {
                                            unLockAll();

                                            getViewInterface().setSuggest(itemLocation.address);
                                            startFinishAction(mLocationSuggest, mKeyword);
                                        }
                                    }));
                            } else
                            {
                                if (mIsResearch == false)
                                {
                                    StayOutboundSuggest stayOutboundSuggest = new StayOutboundSuggest(0, itemLocation.address);
                                    stayOutboundSuggest.categoryKey = StayOutboundSuggest.CATEGORY_LOCATION;
                                    stayOutboundSuggest.menuType = StayOutboundSuggest.MENU_TYPE_LOCATION;
                                    stayOutboundSuggest.latitude = itemLocation.latitude;
                                    stayOutboundSuggest.longitude = itemLocation.longitude;
                                    stayOutboundSuggest.country = address.country;
                                    stayOutboundSuggest.city = address.shortAddress;

                                    unLockAll();

                                    getViewInterface().setSuggest(itemLocation.address);
                                    startFinishAction(stayOutboundSuggest, mKeyword);
                                } else
                                {
                                    // 재검색 시 에는 스위치 하지 않음
                                    unLockAll();

                                    getViewInterface().setSuggest(itemLocation.address);
                                    startFinishAction(mLocationSuggest, mKeyword);
                                }
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

                            addCompositeDisposable(mSuggestLocalImpl.addRecentlyStaySuggest(getActivity(), mLocationSuggest, mKeyword) //
                                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
                                {
                                    @Override
                                    public void accept(Boolean aBoolean) throws Exception
                                    {
                                        unLockAll();

                                        getViewInterface().setSuggest(itemLocation.address);
                                        startFinishAction(mLocationSuggest, mKeyword);
                                    }
                                }, new Consumer<Throwable>()
                                {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception
                                    {
                                        unLockAll();

                                        getViewInterface().setSuggest(itemLocation.address);
                                        startFinishAction(mLocationSuggest, mKeyword);
                                    }
                                }));

                            try
                            {
                                mAnalytics.onLocationSearchNoAddressClick(getActivity());
                            } catch (Exception e)
                            {
                                ExLog.d(e.getMessage());
                            }
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

                itemLocation.address = address;

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
        }.doOnError(throwable -> {
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
