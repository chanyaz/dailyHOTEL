package com.daily.dailyhotel.view;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.constraint.ConstraintLayout;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.daily.base.util.DailyTextUtils;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.databinding.DailyViewPaymentRefundDataBinding;
import com.twoheart.dailyhotel.databinding.LayoutStayOutboundDetailInformationDataBinding;

import java.util.List;

public class DailyBookingRefundPolicyView extends ConstraintLayout
{
    private DailyViewPaymentRefundDataBinding mViewDataBinding;

    public DailyBookingRefundPolicyView(Context context)
    {
        super(context);

        initLayout(context);
    }

    public DailyBookingRefundPolicyView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        initLayout(context);
    }

    public DailyBookingRefundPolicyView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        initLayout(context);
    }

    private void initLayout(Context context)
    {
        mViewDataBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.daily_view_payment_refund_data, this, true);
    }

    public void setRefundPolicyList(List<String> refundPolicyList)
    {
        if (mViewDataBinding == null)
        {
            return;
        }

        if (refundPolicyList == null || refundPolicyList.size() == 0)
        {
            mViewDataBinding.refundPolicyTitleLayout.setVisibility(GONE);
            mViewDataBinding.refundPolicyListLayout.setVisibility(GONE);
        } else
        {
            mViewDataBinding.refundPolicyTitleLayout.setVisibility(VISIBLE);
            mViewDataBinding.refundPolicyListLayout.setVisibility(VISIBLE);
            mViewDataBinding.refundPolicyListLayout.removeAllViews();

            int size = refundPolicyList.size();

            for (int i = 0; i < size; i++)
            {
                if (DailyTextUtils.isTextEmpty(refundPolicyList.get(i)) == true)
                {
                    continue;
                }

                LayoutStayOutboundDetailInformationDataBinding detailInformationDataBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext())//
                    , R.layout.layout_stay_outbound_detail_information_data, mViewDataBinding.refundPolicyListLayout, true);

                detailInformationDataBinding.textView.setText(Html.fromHtml(refundPolicyList.get(i)));

                if (i == size - 1)
                {
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) detailInformationDataBinding.textView.getLayoutParams();
                    layoutParams.bottomMargin = 0;
                    detailInformationDataBinding.textView.setLayoutParams(layoutParams);
                }
            }
        }
    }
}