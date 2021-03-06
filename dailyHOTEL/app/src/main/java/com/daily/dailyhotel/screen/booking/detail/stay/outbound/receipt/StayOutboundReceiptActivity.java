package com.daily.dailyhotel.screen.booking.detail.stay.outbound.receipt;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.daily.base.BaseActivity;
import com.twoheart.dailyhotel.R;

/**
 * Created by sheldon
 * Clean Architecture
 */
public class StayOutboundReceiptActivity extends BaseActivity<StayOutboundReceiptPresenter>
{
    static final String INTENT_EXTRA_DATA_BOOKING_INDEX = "bookingIndex";

    public static final int REQUEST_CODE_EMAIL = 10000;

    public static Intent newInstance(Context context, int bookingIndex)
    {
        Intent intent = new Intent(context, StayOutboundReceiptActivity.class);

        intent.putExtra(INTENT_EXTRA_DATA_BOOKING_INDEX, bookingIndex);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        overridePendingTransition(R.anim.slide_in_right, R.anim.hold);

        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    protected StayOutboundReceiptPresenter createInstancePresenter()
    {
        return new StayOutboundReceiptPresenter(this);
    }

    @Override
    public void finish()
    {
        super.finish();

        overridePendingTransition(R.anim.hold, R.anim.slide_out_right);
    }
}
