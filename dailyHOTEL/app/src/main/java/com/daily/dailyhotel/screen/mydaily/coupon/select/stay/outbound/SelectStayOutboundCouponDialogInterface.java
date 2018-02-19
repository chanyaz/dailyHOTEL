package com.daily.dailyhotel.screen.mydaily.coupon.select.stay.outbound;

import android.content.DialogInterface;
import android.view.View;

import com.daily.base.BaseAnalyticsInterface;
import com.daily.base.BaseDialogViewInterface;
import com.daily.base.OnBaseEventListener;
import com.daily.dailyhotel.entity.Coupon;

import java.util.List;

public interface SelectStayOutboundCouponDialogInterface
{
    interface ViewInterface extends BaseDialogViewInterface
    {
        void setVisible(boolean visible);

        void showCouponListDialog(String title, List<Coupon> couponList, View.OnClickListener positiveListener//
            , View.OnClickListener negativeListener, DialogInterface.OnCancelListener cancelListener);
    }

    interface OnEventListener extends OnBaseEventListener
    {
        void onConfirm(Coupon coupon);

        void onDownloadCouponClick(Coupon coupon);
    }

    interface AnalyticsInterface extends BaseAnalyticsInterface
    {
    }
}