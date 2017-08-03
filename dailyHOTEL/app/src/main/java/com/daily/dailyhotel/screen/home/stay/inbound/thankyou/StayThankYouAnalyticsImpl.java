package com.daily.dailyhotel.screen.home.stay.inbound.thankyou;

import android.app.Activity;

import com.daily.base.util.ExLog;
import com.daily.dailyhotel.entity.UserTracking;
import com.daily.dailyhotel.parcel.analytics.StayThankYouAnalyticsParam;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;

public class StayThankYouAnalyticsImpl implements StayThankYouPresenter.StayThankYouAnalyticsInterface
{
    private StayThankYouAnalyticsParam mAnalyticsParam;

    @Override
    public void setAnalyticsParam(StayThankYouAnalyticsParam analyticsParam)
    {
        mAnalyticsParam = analyticsParam;
    }

    @Override
    public StayThankYouAnalyticsParam getAnalyticsParam()
    {
        return mAnalyticsParam;
    }

    @Override
    public void onScreen(Activity activity)
    {
        if (activity == null)
        {
            return;
        }

        AnalyticsManager.getInstance(activity).recordScreen(activity, AnalyticsManager.Screen.DAILYHOTEL_PAYMENT_THANKYOU, null);
    }

    @Override
    public void onEventPayment(Activity activity)
    {
        if (activity == null || mAnalyticsParam == null || mAnalyticsParam.params == null)
        {
            return;
        }

        try
        {
            String paymentType = mAnalyticsParam.params.get(AnalyticsManager.KeyType.PAYMENT_TYPE);

            String discountType;

            // 고메는 적립금 사용 불가
            if ("true".equalsIgnoreCase(mAnalyticsParam.params.get(AnalyticsManager.KeyType.COUPON_REDEEM)) == true)
            {
                discountType = AnalyticsManager.Label.PAYMENTWITH_COUPON;
            } else
            {
                discountType = AnalyticsManager.Label.FULL_PAYMENT;
            }

            String productIndex = mAnalyticsParam.params.get(AnalyticsManager.KeyType.TICKET_INDEX);

            AnalyticsManager.getInstance(activity).recordEvent(AnalyticsManager.Category.HOTEL_BOOKINGS, AnalyticsManager.Action.END_PAYMENT, paymentType, null);
            AnalyticsManager.getInstance(activity).recordEvent(AnalyticsManager.Category.HOTEL_BOOKINGS, AnalyticsManager.Action.PAYMENT_USED, discountType, null);
            AnalyticsManager.getInstance(activity).recordEvent(AnalyticsManager.Category.HOTEL_BOOKINGS, AnalyticsManager.Action.PRODUCT_ID, productIndex, null);
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }
    }

    @Override
    public void onEventTracking(Activity activity, UserTracking userTracking)
    {
        if (activity == null || userTracking == null || mAnalyticsParam == null || mAnalyticsParam.params == null)
        {
            return;
        }

        String paymentType = mAnalyticsParam.params.get(AnalyticsManager.KeyType.PAYMENT_TYPE);
        boolean isFirstStayPurchase = userTracking.countOfStayPaymentCompleted == 1;
        boolean isCouponUsed = false;

        if (mAnalyticsParam.params != null && mAnalyticsParam.params.containsKey(AnalyticsManager.KeyType.COUPON_REDEEM) == true)
        {
            try
            {
                isCouponUsed = Boolean.parseBoolean(mAnalyticsParam.params.get(AnalyticsManager.KeyType.COUPON_REDEEM));
            } catch (Exception e)
            {
                ExLog.d(e.toString());
            }
        }

        if (isFirstStayPurchase == true)
        {
            AnalyticsManager.getInstance(activity).recordEvent(AnalyticsManager.Category.HOTEL_BOOKINGS, AnalyticsManager.Action.FIRST_PURCHASE_SUCCESS, paymentType, null);
            AnalyticsManager.getInstance(activity).recordScreen(activity, AnalyticsManager.Screen.DAILY_HOTEL_FIRST_PURCHASE_SUCCESS, null, mAnalyticsParam.params);
        }

        if (isCouponUsed == true)
        {
            mAnalyticsParam.params.put(AnalyticsManager.KeyType.FIRST_PURCHASE, isFirstStayPurchase ? "y" : "n");
            mAnalyticsParam.params.put(AnalyticsManager.KeyType.PLACE_TYPE, AnalyticsManager.ValueType.STAY);
            AnalyticsManager.getInstance(activity).purchaseWithCoupon(mAnalyticsParam.params);
        }
    }

    @Override
    public void onEventConfirmClick(Activity activity)
    {
        if (activity == null)
        {
            return;
        }

        AnalyticsManager.getInstance(activity).recordEvent(AnalyticsManager.Category.HOTEL_BOOKINGS//
            , AnalyticsManager.Action.THANKYOU_SCREEN_BUTTON_CLICKED, AnalyticsManager.Label.CLOSE_BUTTON_CLICKED, null);
    }

    @Override
    public void onEventStampClick(Activity activity)
    {
        if (activity == null)
        {
            return;
        }

        AnalyticsManager.getInstance(activity).recordEvent(AnalyticsManager.Category.NAVIGATION,//
            AnalyticsManager.Action.STAMP_DETAIL_CLICK, AnalyticsManager.Label.STAY_THANKYOU, null);
    }

    @Override
    public void onEventBackClick(Activity activity)
    {
        if (activity == null)
        {
            return;
        }

        AnalyticsManager.getInstance(activity).recordEvent(AnalyticsManager.Category.HOTEL_BOOKINGS//
            , AnalyticsManager.Action.THANKYOU_SCREEN_BUTTON_CLICKED, AnalyticsManager.Label.VIEW_BOOKING_STATUS_CLICKED, null);
    }
}
