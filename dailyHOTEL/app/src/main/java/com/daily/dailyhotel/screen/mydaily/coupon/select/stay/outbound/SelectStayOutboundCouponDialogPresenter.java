package com.daily.dailyhotel.screen.mydaily.coupon.select.stay.outbound;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.daily.base.BaseAnalyticsInterface;
import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ExLog;
import com.daily.dailyhotel.base.BaseExceptionPresenter;
import com.daily.dailyhotel.entity.Coupons;
import com.daily.dailyhotel.entity.StayBookDateTime;
import com.daily.dailyhotel.repository.remote.CouponRemoteImpl;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.util.DailyCalendar;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

/**
 * Created by sheldon
 * Clean Architecture
 */
public class SelectStayOutboundCouponDialogPresenter extends BaseExceptionPresenter<SelectStayOutboundCouponDialogActivity, SelectStayOutboundCouponDialogInterface.ViewInterface> implements SelectStayOutboundCouponDialogInterface.OnEventListener
{
    private SelectStayOutboundCouponDialogInterface.AnalyticsInterface mAnalytics;

    CouponRemoteImpl mCouponRemoteImpl;

    int mStayIndex;
    String mStayName;
    StayBookDateTime mStayBookDateTime;
    String mRateCode, mRateKey, mRoomTypeCode;
    int mMaxCouponAmount;

    public SelectStayOutboundCouponDialogPresenter(@NonNull SelectStayOutboundCouponDialogActivity activity)
    {
        super(activity);
    }

    @NonNull
    @Override
    protected SelectStayOutboundCouponDialogInterface.ViewInterface createInstanceViewInterface()
    {
        return new SelectStayOutboundCouponDialogView(getActivity(), this);
    }

    @Override
    public void constructorInitialize(SelectStayOutboundCouponDialogActivity activity)
    {
        setContentView(R.layout.activity_select_coupon_dialog_data);

        setAnalytics(new SelectStayOutboundCouponDialogAnalyticsImpl());

        mCouponRemoteImpl = new CouponRemoteImpl(activity);

        setRefresh(true);
    }

    @Override
    public void setAnalytics(BaseAnalyticsInterface analytics)
    {
        mAnalytics = (SelectStayOutboundCouponDialogInterface.AnalyticsInterface) analytics;
    }

    @Override
    public boolean onIntent(Intent intent)
    {
        if (intent == null)
        {
            return true;
        }

        mStayIndex = intent.getIntExtra(SelectStayOutboundCouponDialogActivity.INTENT_EXTRA_DATA_STAY_INDEX, -1);

        if (mStayIndex <= 0)
        {
            return false;
        }

        mStayName = intent.getStringExtra(SelectStayOutboundCouponDialogActivity.INTENT_EXTRA_DATA_STAY_NAME);

        try
        {
            mStayBookDateTime = new StayBookDateTime(intent.getStringExtra(SelectStayOutboundCouponDialogActivity.INTENT_EXTRA_DATA_CHECK_IN_DATE_TIME), intent.getStringExtra(SelectStayOutboundCouponDialogActivity.INTENT_EXTRA_DATA_CHECK_OUT_DATE_TIME));
        } catch (Exception e)
        {
            ExLog.e(e.toString());

            return false;
        }

        mRateCode = intent.getStringExtra(SelectStayOutboundCouponDialogActivity.INTENT_EXTRA_DATA_RATE_CODE);
        mRateKey = intent.getStringExtra(SelectStayOutboundCouponDialogActivity.INTENT_EXTRA_DATA_RATE_KEY);
        mRoomTypeCode = intent.getStringExtra(SelectStayOutboundCouponDialogActivity.INTENT_EXTRA_DATA_ROOM_TYPE_CODE);


        if (DailyTextUtils.isTextEmpty(mRateCode, mRateKey, mRoomTypeCode) == true)
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

        addCompositeDisposable(mCouponRemoteImpl.getStayOutboundCouponListByPayment(mStayIndex, mRateCode, mRateKey, mRoomTypeCode//
            , mStayBookDateTime.getCheckInDateTime(DailyCalendar.ISO_8601_FORMAT)//
            , mStayBookDateTime.getCheckOutDateTime(DailyCalendar.ISO_8601_FORMAT)).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Coupons>()
        {
            @Override
            public void accept(Coupons coupons) throws Exception
            {
                if (coupons == null || coupons.coupons == null || coupons.coupons.size() == 0)
                {
                    getViewInterface().setVisible(false);

                    getViewInterface().showSimpleDialog(getString(R.string.label_booking_select_coupon), getString(R.string.message_select_coupon_empty), //
                        getString(R.string.dialog_btn_text_confirm), null, new DialogInterface.OnDismissListener()
                        {
                            @Override
                            public void onDismiss(DialogInterface dialog)
                            {
                                finish();
                            }
                        });

                } else
                {
                    mMaxCouponAmount = coupons.maxCouponAmount;

                    getViewInterface().setVisible(true);
                    getViewInterface().showCouponListDialog(getString(R.string.label_select_coupon), coupons.coupons, new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {

                        }
                    }, new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {

                        }
                    }, new DialogInterface.OnCancelListener()
                    {
                        @Override
                        public void onCancel(DialogInterface dialog)
                        {

                        }
                    });
                }

                unLockAll();
            }
        }, new Consumer<Throwable>()
        {
            @Override
            public void accept(Throwable throwable) throws Exception
            {
                onHandleErrorAndFinish(throwable);
            }
        }));
    }

    @Override
    public void onBackClick()
    {
        getActivity().onBackPressed();
    }

}
