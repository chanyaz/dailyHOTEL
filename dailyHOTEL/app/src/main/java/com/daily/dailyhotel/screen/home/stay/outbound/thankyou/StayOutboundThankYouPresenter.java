package com.daily.dailyhotel.screen.home.stay.outbound.thankyou;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;

import com.daily.base.BaseActivity;
import com.daily.base.BaseAnalyticsInterface;
import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ExLog;
import com.daily.base.util.FontManager;
import com.daily.dailyhotel.base.BaseExceptionPresenter;
import com.daily.dailyhotel.entity.RewardInformation;
import com.daily.dailyhotel.entity.StayBookDateTime;
import com.daily.dailyhotel.parcel.analytics.StayOutboundThankYouAnalyticsParam;
import com.daily.dailyhotel.repository.remote.RewardRemoteImpl;
import com.daily.dailyhotel.storage.preference.DailyRemoteConfigPreference;
import com.daily.dailyhotel.storage.preference.DailyUserPreference;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.util.DailyInternalDeepLink;
import com.twoheart.dailyhotel.widget.CustomFontTypefaceSpan;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;

/**
 * Created by sheldon
 * Clean Architecture
 */
public class StayOutboundThankYouPresenter extends BaseExceptionPresenter<StayOutboundThankYouActivity, StayOutboundThankYouInterface> implements StayOutboundThankYouView.OnEventListener
{
    private StayOutboundThankYouAnalyticsInterface mAnalytics;

    private RewardRemoteImpl mRewardRemoteImpl;

    private String mAggregationId;
    private String mStayName;
    private String mImageUrl;
    private StayBookDateTime mStayBookDateTime;
    private String mCheckInTime;
    private String mCheckOutTime;
    private String mRoomType;
    private String mRewardDescriptionTitle;
    private String mRewardDescriptionMessage;

    public interface StayOutboundThankYouAnalyticsInterface extends BaseAnalyticsInterface
    {
        void setAnalyticsParam(StayOutboundThankYouAnalyticsParam analyticsParam);

        StayOutboundThankYouAnalyticsParam getAnalyticsParam();

        void onScreen(Activity activity);

        void onEventPayment(Activity activity);

        void onEventOrderComplete(Activity activity);
    }

    public StayOutboundThankYouPresenter(@NonNull StayOutboundThankYouActivity activity)
    {
        super(activity);
    }

    @NonNull
    @Override
    protected StayOutboundThankYouInterface createInstanceViewInterface()
    {
        return new StayOutboundThankYouView(getActivity(), this);
    }

    @Override
    public void constructorInitialize(StayOutboundThankYouActivity activity)
    {
        lock();

        setContentView(R.layout.activity_stay_outbound_payment_thank_you_data);

        setAnalytics(new StayOutboundThankYouAnalyticsImpl());

        mRewardRemoteImpl = new RewardRemoteImpl(activity);

        setRefresh(true);
    }

    @Override
    public void setAnalytics(BaseAnalyticsInterface analytics)
    {
        mAnalytics = (StayOutboundThankYouAnalyticsInterface) analytics;
    }

    @Override
    public boolean onIntent(Intent intent)
    {
        if (intent == null)
        {
            return true;
        }

        mStayName = intent.getStringExtra(StayOutboundThankYouActivity.INTENT_EXTRA_DATA_STAY_NAME);
        mImageUrl = intent.getStringExtra(StayOutboundThankYouActivity.INTENT_EXTRA_DATA_IMAGE_URL);
        mCheckInTime = intent.getStringExtra(StayOutboundThankYouActivity.INTENT_EXTRA_DATA_CHECK_IN_TIME);
        mCheckOutTime = intent.getStringExtra(StayOutboundThankYouActivity.INTENT_EXTRA_DATA_CHECK_OUT_TIME);

        String checkInDateTime = intent.getStringExtra(StayOutboundThankYouActivity.INTENT_EXTRA_DATA_CHECK_IN);
        String checkOutDateTime = intent.getStringExtra(StayOutboundThankYouActivity.INTENT_EXTRA_DATA_CHECK_OUT);

        setStayBookDateTime(checkInDateTime, checkOutDateTime);

        mRoomType = intent.getStringExtra(StayOutboundThankYouActivity.INTENT_EXTRA_DATA_ROOM_TYPE);
        mAggregationId = intent.getStringExtra(StayOutboundThankYouActivity.INTENT_EXTRA_DATA_AGGREGATION_ID);

        mRewardDescriptionTitle = intent.getStringExtra(StayOutboundThankYouActivity.INTENT_EXTRA_DATA_REWARD_DESCRIPTION_TITLE);
        mRewardDescriptionMessage = intent.getStringExtra(StayOutboundThankYouActivity.INTENT_EXTRA_DATA_REWARD_DESCRIPTION_MESSAGE);

        mAnalytics.setAnalyticsParam(intent.getParcelableExtra(BaseActivity.INTENT_EXTRA_DATA_ANALYTICS));

        return true;
    }

    @Override
    public void onPostCreate()
    {
        getViewInterface().setToolbarTitle(getString(R.string.label_completed_payment));
        getViewInterface().setImageUrl(mImageUrl);

        String name = DailyUserPreference.getInstance(getActivity()).getName();
        getViewInterface().setUserName(name);

        final String DATE_FORMAT = "yyyy.M.d(EEE)";

        try
        {
            String checkInTime = getString(R.string.label_stay_outbound_payment_hour, mCheckInTime.split(":")[0]);
            String checkInDate = mStayBookDateTime.getCheckInDateTime(DATE_FORMAT);

            SpannableString checkInDateSpannableString = new SpannableString(checkInDate + " " + checkInTime);
            checkInDateSpannableString.setSpan( //
                new CustomFontTypefaceSpan(FontManager.getInstance(getActivity()).getBoldTypeface()),//
                checkInDate.length(), checkInDate.length() + checkInTime.length() + 1,//
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            String checkOutTime = getString(R.string.label_stay_outbound_payment_hour, mCheckOutTime.split(":")[0]);
            String checkOutDate = mStayBookDateTime.getCheckOutDateTime(DATE_FORMAT);

            SpannableString checkOutDateSpannableString = new SpannableString(checkOutDate + " " + checkOutTime);
            checkOutDateSpannableString.setSpan( //
                new CustomFontTypefaceSpan(FontManager.getInstance(getActivity()).getBoldTypeface()),//
                checkOutDate.length(), checkOutDate.length() + checkOutTime.length() + 1,//
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            getViewInterface().setBooking(checkInDateSpannableString, checkOutDateSpannableString, mStayBookDateTime.getNights(), mStayName, mRoomType);
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();

        mAnalytics.onScreen(getActivity());
        mAnalytics.onEventPayment(getActivity());
        mAnalytics.onEventOrderComplete(getActivity());

        if (isRefresh() == true)
        {
            onRefresh(true);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

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
        if (isLock() == true)
        {
            return true;
        }

        startActivity(DailyInternalDeepLink.getStayOutboundBookingDetailScreenLink(getActivity(), mAggregationId));

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

        if (getViewInterface() == null)
        {
            return;
        }

        screenLock(showProgress);

        addCompositeDisposable(Observable.zip(getViewInterface().getReceiptAnimation(), mRewardRemoteImpl.getRewardStickerCount(), new BiFunction<Boolean, RewardInformation, RewardInformation>()
        {
            @Override
            public RewardInformation apply(Boolean aBoolean, RewardInformation rewardInformation) throws Exception
            {
                return rewardInformation;
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<RewardInformation>()
        {
            @Override
            public void accept(RewardInformation rewardInformation) throws Exception
            {
                onRewardInformation(rewardInformation);

                unLockAll();
            }
        }, new Consumer<Throwable>()
        {
            @Override
            public void accept(Throwable throwable) throws Exception
            {
                unLockAll();

                onReportError(throwable);
            }
        }));
    }

    @Override
    public void onBackClick()
    {
        getActivity().onBackPressed();
    }

    private void setStayBookDateTime(String checkInDateTime, String checkOutDateTime)
    {
        if (DailyTextUtils.isTextEmpty(checkInDateTime, checkOutDateTime) == true)
        {
            return;
        }

        if (mStayBookDateTime == null)
        {
            mStayBookDateTime = new StayBookDateTime();
        }

        try
        {
            mStayBookDateTime.setCheckInDateTime(checkInDateTime);
            mStayBookDateTime.setCheckOutDateTime(checkOutDateTime);
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    private void onRewardInformation(RewardInformation rewardInformation)
    {
        if (rewardInformation == null || mStayBookDateTime == null)
        {
            return;
        }

        getViewInterface().setDepositStickerCardVisible(rewardInformation.activeReward);

        if (rewardInformation.activeReward == true)
        {
            getViewInterface().setDepositStickerCard(DailyRemoteConfigPreference.getInstance(getActivity()).getKeyRemoteConfigRewardStickerCardTitleMessage()//
                , rewardInformation.rewardStickerCount, mRewardDescriptionTitle, mRewardDescriptionMessage);
        }
    }
}
