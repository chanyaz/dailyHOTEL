package com.daily.dailyhotel.screen.mydaily.reward.history;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.daily.base.BaseAnalyticsInterface;
import com.daily.base.util.ExLog;
import com.daily.dailyhotel.base.BaseExceptionPresenter;
import com.daily.dailyhotel.entity.ObjectItem;
import com.daily.dailyhotel.entity.RewardHistory;
import com.daily.dailyhotel.entity.RewardHistoryDetail;
import com.daily.dailyhotel.repository.remote.RewardRemoteImpl;
import com.twoheart.dailyhotel.LauncherActivity;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.util.DailyCalendar;
import com.twoheart.dailyhotel.util.DailyInternalDeepLink;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Created by sheldon
 * Clean Architecture
 */
public class RewardHistoryPresenter extends BaseExceptionPresenter<RewardHistoryActivity, RewardHistoryInterface> implements RewardHistoryView.OnEventListener
{
    private RewardHistoryAnalyticsInterface mAnalytics;

    private RewardRemoteImpl mRewardRemoteImpl;

    private String mStickerValidity;

    public interface RewardHistoryAnalyticsInterface extends BaseAnalyticsInterface
    {
        void onViewReservationClick(Activity activity, String aggregationId);
    }

    public RewardHistoryPresenter(@NonNull RewardHistoryActivity activity)
    {
        super(activity);
    }

    @NonNull
    @Override
    protected RewardHistoryInterface createInstanceViewInterface()
    {
        return new RewardHistoryView(getActivity(), this);
    }

    @Override
    public void constructorInitialize(RewardHistoryActivity activity)
    {
        setContentView(R.layout.activity_reward_history_data);

        setAnalytics(new RewardHistoryAnalyticsImpl());

        mRewardRemoteImpl = new RewardRemoteImpl(activity);

        setRefresh(true);
    }

    @Override
    public void setAnalytics(BaseAnalyticsInterface analytics)
    {
        mAnalytics = (RewardHistoryAnalyticsInterface) analytics;
    }

    @Override
    public boolean onIntent(Intent intent)
    {
        if (intent == null)
        {
            return true;
        }

        return true;
    }

    @Override
    public void onPostCreate()
    {
        getViewInterface().setToolbarTitle(getString(R.string.label_reward_reward_history));


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

        addCompositeDisposable(mRewardRemoteImpl.getRewardHistoryDetail().map(new Function<RewardHistoryDetail, List<ObjectItem>>()
        {
            @Override
            public List<ObjectItem> apply(@io.reactivex.annotations.NonNull RewardHistoryDetail rewardHistoryDetail) throws Exception
            {
                setStickerValidity(rewardHistoryDetail.expiredAt);

                List<ObjectItem> objectItemList = new ArrayList<>();
                List<RewardHistory> rewardHistoryList = rewardHistoryDetail.getRewardHistoryList();

                if (rewardHistoryList != null && rewardHistoryList.size() > 0)
                {
                    objectItemList.add(new ObjectItem(ObjectItem.TYPE_HEADER_VIEW, null));

                    for (RewardHistory rewardHistory : rewardHistoryList)
                    {
                        objectItemList.add(new ObjectItem(ObjectItem.TYPE_ENTRY, rewardHistory));
                    }

                    objectItemList.add(new ObjectItem(ObjectItem.TYPE_FOOTER_VIEW, null));
                }

                return objectItemList;
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<List<ObjectItem>>()
        {
            @Override
            public void accept(List<ObjectItem> objectItemList) throws Exception
            {
                notifyStickerValidityChanged();

                onRewardHistoryList(objectItemList);

                unLockAll();
            }
        }, new Consumer<Throwable>()
        {
            @Override
            public void accept(Throwable throwable) throws Exception
            {
                onHandleError(throwable);
            }
        }));
    }

    @Override
    public void onBackClick()
    {
        getActivity().onBackPressed();
    }

    @Override
    public void onViewReservationClick(RewardHistory rewardHistory)
    {
        if (lock() == true)
        {
            return;
        }

        String deepLink = "dailyhotel://dailyhotel.co.kr?vc=20&v=bd&agi=" + rewardHistory.aggregationId + "&pt=stay";

        Intent intent = new Intent(getActivity(), LauncherActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse(deepLink));

        startActivity(intent);

        mAnalytics.onViewReservationClick(getActivity(), rewardHistory.aggregationId);
    }

    @Override
    public void onHomeClick()
    {
        if (lock() == true)
        {
            return;
        }

        startActivity(DailyInternalDeepLink.getHomeScreenLink(getActivity()));
        onBackClick();
    }

    private void setStickerValidity(String validity)
    {
        mStickerValidity = validity;
    }

    private void notifyStickerValidityChanged()
    {
        try
        {
            getViewInterface().setStickerValidityText(DailyCalendar.convertDateFormatString(mStickerValidity, DailyCalendar.ISO_8601_FORMAT, "yyyy.MM.dd"));
        } catch (ParseException e)
        {
            ExLog.e(e.toString());
        }
    }

    private void onRewardHistoryList(List<ObjectItem> objectItemList)
    {
        if (objectItemList == null || objectItemList.size() == 0)
        {
            return;
        }

        getViewInterface().setRewardHistoryList(objectItemList);
    }
}
