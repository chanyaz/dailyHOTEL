package com.daily.dailyhotel.screen.home.gourmet.thankyou;


import android.animation.Animator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.daily.base.BaseActivity;
import com.daily.base.BaseAnalyticsInterface;
import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ExLog;
import com.daily.dailyhotel.base.BaseExceptionPresenter;
import com.daily.dailyhotel.entity.GourmetBookDateTime;
import com.daily.dailyhotel.entity.UserTracking;
import com.daily.dailyhotel.parcel.analytics.GourmetThankYouAnalyticsParam;
import com.daily.dailyhotel.repository.remote.ProfileRemoteImpl;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.util.DailyInternalDeepLink;
import com.twoheart.dailyhotel.util.DailyUserPreference;

import io.reactivex.functions.Consumer;

/**
 * Created by sheldon
 * Clean Architecture
 */
public class GourmetThankYouPresenter extends BaseExceptionPresenter<GourmetThankYouActivity, GourmetThankYouInterface> implements GourmetThankYouView.OnEventListener
{
    private GourmetThankYouAnalyticsInterface mAnalytics;

    private ProfileRemoteImpl mProfileRemoteImpl;

    private String mAggregationId;
    private String mGourmetName;
    private String mImageUrl;
    private GourmetBookDateTime mGourmetBookDateTime;
    private String mMenuName;
    private int mMenuCount;

    public interface GourmetThankYouAnalyticsInterface extends BaseAnalyticsInterface
    {
        void setAnalyticsParam(GourmetThankYouAnalyticsParam analyticsParam);

        GourmetThankYouAnalyticsParam getAnalyticsParam();

        void onScreen(Activity activity);

        void onEventPayment(Activity activity);

        void onEventTracking(Activity activity, UserTracking userTracking);

        void onEventConfirmClick(Activity activity);

        void onEventBackClick(Activity activity);
    }

    public GourmetThankYouPresenter(@NonNull GourmetThankYouActivity activity)
    {
        super(activity);
    }

    @NonNull
    @Override
    protected GourmetThankYouInterface createInstanceViewInterface()
    {
        return new GourmetThankYouView(getActivity(), this);
    }

    @Override
    public void constructorInitialize(GourmetThankYouActivity activity)
    {
        lock();

        setContentView(R.layout.activity_stay_outbound_payment_thank_you_data);

        setAnalytics(new GourmetThankYouAnalyticsImpl());

        mProfileRemoteImpl = new ProfileRemoteImpl(activity);

        setRefresh(true);
    }

    @Override
    public void setAnalytics(BaseAnalyticsInterface analytics)
    {
        mAnalytics = (GourmetThankYouAnalyticsInterface) analytics;
    }

    @Override
    public boolean onIntent(Intent intent)
    {
        if (intent == null)
        {
            return true;
        }

        mGourmetName = intent.getStringExtra(GourmetThankYouActivity.INTENT_EXTRA_DATA_GOURMET_NAME);
        mImageUrl = intent.getStringExtra(GourmetThankYouActivity.INTENT_EXTRA_DATA_IMAGE_URL);

        String visitDateTime = intent.getStringExtra(GourmetThankYouActivity.INTENT_EXTRA_DATA_VISIT_DATE_TIME);

        setGourmetBookDateTime(visitDateTime);

        mMenuName = intent.getStringExtra(GourmetThankYouActivity.INTENT_EXTRA_DATA_MENU_NAME);
        mMenuCount = intent.getIntExtra(GourmetThankYouActivity.INTENT_EXTRA_DATA_MENU_COUNT, 0);
        mAggregationId = intent.getStringExtra(GourmetThankYouActivity.INTENT_EXTRA_DATA_AGGREGATION_ID);

        mAnalytics.setAnalyticsParam(intent.getParcelableExtra(BaseActivity.INTENT_EXTRA_DATA_ANALYTICS));

        mAnalytics.onEventPayment(getActivity());

        return true;
    }

    @Override
    public void onPostCreate()
    {
        getViewInterface().setToolbarTitle(getString(R.string.label_completed_payment));
        getViewInterface().setImageUrl(mImageUrl);

        String name = DailyUserPreference.getInstance(getActivity()).getName();
        getViewInterface().setUserName(name);

        final String DATE_FORMAT = "yyyy.MM.dd(EEE)";
        final String TIME_FORMAT = "HH:mm";

        try
        {
            String visitDate = mGourmetBookDateTime.getVisitDateTime(DATE_FORMAT);
            String visitTime = mGourmetBookDateTime.getVisitDateTime(TIME_FORMAT);

            getViewInterface().setBooking(visitDate, visitTime, mGourmetName, mMenuName, mMenuCount);
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }

        getViewInterface().startAnimation(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {

            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                unLockAll();
            }

            @Override
            public void onAnimationCancel(Animator animation)
            {

            }

            @Override
            public void onAnimationRepeat(Animator animation)
            {

            }
        });
    }

    @Override
    public void onStart()
    {
        super.onStart();

        mAnalytics.onScreen(getActivity());

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

        mAnalytics.onEventBackClick(getActivity());

        startActivity(DailyInternalDeepLink.getGourmetBookingDetailScreenLink(getActivity(), mAggregationId));

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

        addCompositeDisposable(mProfileRemoteImpl.getTracking().subscribe(new Consumer<UserTracking>()
        {
            @Override
            public void accept(@io.reactivex.annotations.NonNull UserTracking userTracking) throws Exception
            {
                mAnalytics.onEventTracking(getActivity(), userTracking);
            }
        }, new Consumer<Throwable>()
        {
            @Override
            public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception
            {
            }
        }));
    }

    @Override
    public void onBackClick()
    {
        getActivity().onBackPressed();
    }

    @Override
    public void onConfirmClick()
    {
        if (isLock() == true)
        {
            return;
        }

        mAnalytics.onEventConfirmClick(getActivity());

        startActivity(DailyInternalDeepLink.getGourmetBookingDetailScreenLink(getActivity(), mAggregationId));

        finish();
    }

    private void setGourmetBookDateTime(String visitDateTime)
    {
        if (DailyTextUtils.isTextEmpty(visitDateTime) == true)
        {
            return;
        }

        if (mGourmetBookDateTime == null)
        {
            mGourmetBookDateTime = new GourmetBookDateTime();
        }

        try
        {
            mGourmetBookDateTime.setVisitDateTime(visitDateTime);
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }
}
