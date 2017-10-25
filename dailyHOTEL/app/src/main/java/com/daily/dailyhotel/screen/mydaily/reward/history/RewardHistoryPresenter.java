package com.daily.dailyhotel.screen.mydaily.reward.history;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.daily.base.BaseAnalyticsInterface;
import com.daily.dailyhotel.base.BaseExceptionPresenter;
import com.daily.dailyhotel.entity.ObjectItem;
import com.daily.dailyhotel.entity.RewardHistory;
import com.twoheart.dailyhotel.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sheldon
 * Clean Architecture
 */
public class RewardHistoryPresenter extends BaseExceptionPresenter<RewardHistoryActivity, RewardHistoryInterface> implements RewardHistoryView.OnEventListener
{
    private RewardHistoryAnalyticsInterface mAnalytics;

    public interface RewardHistoryAnalyticsInterface extends BaseAnalyticsInterface
    {
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

        List<ObjectItem> list = new ArrayList<>();
        list.add(new ObjectItem(ObjectItem.TYPE_HEADER_VIEW, null));

        RewardHistory rewardHistoryA = new RewardHistory();
        rewardHistoryA.type = RewardHistory.Type.A;

        RewardHistory rewardHistoryB = new RewardHistory();
        rewardHistoryB.type = RewardHistory.Type.B;

        RewardHistory rewardHistoryC = new RewardHistory();
        rewardHistoryC.type = RewardHistory.Type.C;

        RewardHistory rewardHistoryD = new RewardHistory();
        rewardHistoryD.type = RewardHistory.Type.D;

        list.add(new ObjectItem(ObjectItem.TYPE_ENTRY, rewardHistoryA));
        list.add(new ObjectItem(ObjectItem.TYPE_ENTRY, rewardHistoryA));
        list.add(new ObjectItem(ObjectItem.TYPE_ENTRY, rewardHistoryB));
        list.add(new ObjectItem(ObjectItem.TYPE_ENTRY, rewardHistoryC));
        list.add(new ObjectItem(ObjectItem.TYPE_ENTRY, rewardHistoryD));
        list.add(new ObjectItem(ObjectItem.TYPE_ENTRY, rewardHistoryC));
        list.add(new ObjectItem(ObjectItem.TYPE_ENTRY, rewardHistoryD));
        list.add(new ObjectItem(ObjectItem.TYPE_ENTRY, rewardHistoryC));
        list.add(new ObjectItem(ObjectItem.TYPE_ENTRY, rewardHistoryA));
        list.add(new ObjectItem(ObjectItem.TYPE_ENTRY, rewardHistoryB));
        list.add(new ObjectItem(ObjectItem.TYPE_ENTRY, rewardHistoryD));
        list.add(new ObjectItem(ObjectItem.TYPE_ENTRY, rewardHistoryC));
        list.add(new ObjectItem(ObjectItem.TYPE_ENTRY, rewardHistoryA));
        list.add(new ObjectItem(ObjectItem.TYPE_ENTRY, rewardHistoryB));
        list.add(new ObjectItem(ObjectItem.TYPE_ENTRY, rewardHistoryD));
        list.add(new ObjectItem(ObjectItem.TYPE_FOOTER_VIEW, null));

        getViewInterface().setRewardHistoryData(list);
    }

    @Override
    public void onBackClick()
    {
        getActivity().onBackPressed();
    }

}
