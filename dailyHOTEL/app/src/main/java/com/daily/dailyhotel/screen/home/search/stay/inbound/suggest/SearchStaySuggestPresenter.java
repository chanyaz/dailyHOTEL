package com.daily.dailyhotel.screen.home.search.stay.inbound.suggest;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.daily.base.BaseAnalyticsInterface;
import com.daily.base.util.DailyTextUtils;
import com.daily.dailyhotel.base.BaseExceptionPresenter;
import com.daily.dailyhotel.entity.StayBookDateTime;
import com.daily.dailyhotel.entity.StayOutboundSuggest;
import com.twoheart.dailyhotel.R;

/**
 * Created by sheldon
 * Clean Architecture
 */
public class SearchStaySuggestPresenter extends BaseExceptionPresenter<SearchStaySuggestActivity, SearchStaySuggestInterface> implements SearchStaySuggestView.OnEventListener
{
    private SearchStaySuggestAnalyticsInterface mAnalytics;

    private StayBookDateTime mStayBookDateTime;

    public interface SearchStaySuggestAnalyticsInterface extends BaseAnalyticsInterface
    {
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
            mStayBookDateTime.getCheckOutDateTime(checkOutDate);
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
    }

    @Override
    public void onStart()
    {
        super.onStart();

        if (isRefresh() == true)
        {
            onRefresh(true);
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
    }

    @Override
    protected synchronized void onRefresh(boolean showProgress)
    {
        if (getActivity().isFinishing() == true || isRefresh() == false)
        {
            return;
        }

        setRefresh(false);
        screenLock(showProgress);
    }

    @Override
    public void onBackClick()
    {
        getActivity().onBackPressed();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onSearchSuggest(String keyword)
    {

    }

    @Override
    public void onSuggestClick(StayOutboundSuggest stayOutboundSuggest)
    {

    }

    @Override
    public void onRecentlySuggestClick(StayOutboundSuggest stayOutboundSuggest)
    {

    }

    @Override
    public void onPopularSuggestClick(StayOutboundSuggest stayOutboundSuggest)
    {

    }

    @Override
    public void onDeleteAllRecentlySuggest()
    {

    }

}