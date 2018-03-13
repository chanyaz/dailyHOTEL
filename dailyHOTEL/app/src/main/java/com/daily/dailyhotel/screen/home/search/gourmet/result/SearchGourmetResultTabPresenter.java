package com.daily.dailyhotel.screen.home.search.gourmet.result;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.daily.base.BaseActivity;
import com.daily.base.BaseAnalyticsInterface;
import com.daily.base.util.ExLog;
import com.daily.dailyhotel.base.BaseExceptionPresenter;
import com.daily.dailyhotel.entity.CommonDateTime;
import com.daily.dailyhotel.repository.remote.CommonRemoteImpl;
import com.daily.dailyhotel.util.DailyIntentUtils;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.util.DailyDeepLink;

import io.reactivex.functions.Consumer;

/**
 * Created by sheldon
 * Clean Architecture
 */
public class SearchGourmetResultTabPresenter extends BaseExceptionPresenter<SearchGourmetResultTabActivity, SearchGourmetResultTabInterface.ViewInterface> implements SearchGourmetResultTabInterface.OnEventListener
{
    private SearchGourmetResultTabInterface.AnalyticsInterface mAnalytics;

    private CommonRemoteImpl mCommonRemoteImpl;

    SearchGourmetResultViewModel mViewModel;

    DailyDeepLink mDailyDeepLink;

    public enum ViewType
    {
        NONE,
        LIST,
        MAP,
    }

    public SearchGourmetResultTabPresenter(@NonNull SearchGourmetResultTabActivity activity)
    {
        super(activity);
    }

    @NonNull
    @Override
    protected SearchGourmetResultTabInterface.ViewInterface createInstanceViewInterface()
    {
        return new SearchGourmetResultTabView(getActivity(), this);
    }

    @Override
    public void constructorInitialize(SearchGourmetResultTabActivity activity)
    {
        setContentView(R.layout.activity_search_gourmet_result_tab_data);

        setAnalytics(new SearchGourmetResultTabAnalyticsImpl());

        mCommonRemoteImpl = new CommonRemoteImpl(activity);

        initViewModel(activity);

        setRefresh(true);
    }

    private void initViewModel(BaseActivity activity)
    {
        if (activity == null)
        {
            return;
        }

        mViewModel = ViewModelProviders.of(activity, new SearchGourmetResultViewModel.SearchGourmetViewModelFactory()).get(SearchGourmetResultViewModel.class);

        mViewModel.viewType.observe(activity, new Observer<ViewType>()
        {
            @Override
            public void onChanged(@Nullable ViewType viewType)
            {
                switch (viewType)
                {
                    case LIST:
                        getViewInterface().setViewType(ViewType.MAP);
                        break;

                    case MAP:
                        getViewInterface().setViewType(ViewType.LIST);
                        break;
                }
            }
        });

        mViewModel.commonDateTime.observe(activity, new Observer<CommonDateTime>()
        {
            @Override
            public void onChanged(@Nullable CommonDateTime commonDateTime)
            {

            }
        });
    }

    @Override
    public void setAnalytics(BaseAnalyticsInterface analytics)
    {
        mAnalytics = (SearchGourmetResultTabInterface.AnalyticsInterface) analytics;
    }

    @Override
    public boolean onIntent(Intent intent)
    {
        if (intent == null)
        {
            return true;
        }

        if (DailyIntentUtils.hasDeepLink(intent) == true)
        {
            try
            {
                mDailyDeepLink = DailyIntentUtils.getDeepLink(intent);
                parseDeepLink(mDailyDeepLink);
            } catch (Exception e)
            {
                ExLog.e(e.toString());
                clearDeepLink();
            }
        } else
        {
            try
            {
                parseIntent(intent);
            } catch (Exception e)
            {
                ExLog.e(e.toString());
            }
        }

        return true;
    }

    private void parseDeepLink(DailyDeepLink dailyDeepLink)
    {
        if (dailyDeepLink == null)
        {
            throw new NullPointerException("dailyDeepLink == null");
        }

        if (dailyDeepLink.isInternalDeepLink() == true)
        {

        } else if (dailyDeepLink.isExternalDeepLink() == true)
        {

        } else
        {
            throw new RuntimeException("Invalid DeepLink : " + dailyDeepLink.getDeepLink());
        }
    }

    private void clearDeepLink()
    {
        if (mDailyDeepLink == null)
        {
            return;
        }

        mDailyDeepLink.clear();
        mDailyDeepLink = null;
    }

    private void parseIntent(Intent intent) throws Exception
    {
        if (intent == null)
        {
            throw new NullPointerException("intent == null");
        }

        
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

        addCompositeDisposable(mCommonRemoteImpl.getCommonDateTime().subscribe(new Consumer<CommonDateTime>()
        {
            @Override
            public void accept(CommonDateTime commonDateTime) throws Exception
            {

            }
        }, new Consumer<Throwable>()
        {
            @Override
            public void accept(Throwable throwable) throws Exception
            {

            }
        }));
    }

    @Override
    public void onBackClick()
    {
        getActivity().onBackPressed();
    }

}
