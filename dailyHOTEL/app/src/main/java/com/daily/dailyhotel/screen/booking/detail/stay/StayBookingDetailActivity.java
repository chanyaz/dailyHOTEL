package com.daily.dailyhotel.screen.booking.detail.stay;


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
public class StayBookingDetailActivity extends BaseActivity<StayBookingDetailPresenter>
{
    static final String NAME_INTENT_EXTRA_DATA_BOOKINGIDX = "bookingIndex";
    static final String NAME_INTENT_EXTRA_DATA_AGGREGATION_ID = "aggregationId";
    static final String NAME_INTENT_EXTRA_DATA_URL = "url";
    static final String NAME_INTENT_EXTRA_DATA_DEEPLINK = "deepLink";
    static final String NAME_INTENT_EXTRA_DATA_BOOKING_STATE = "bookingState";

    static final int REQUEST_CODE_ISSUING_RECEIPT = 10000;
    static final int REQUEST_CODE_ZOOMMAP = 10001;
    static final int REQUEST_CODE_DETAIL = 10002;
    static final int REQUEST_CODE_CALL = 10003;
    static final int REQUEST_CODE_HAPPYTALK = 10004;
    static final int REQUEST_CODE_PERMISSION_MANAGER = 10005;
    static final int REQUEST_CODE_SETTING_LOCATION = 10006;
    static final int REQUEST_CODE_REFUND = 10007;
    static final int REQUEST_CODE_NAVIGATOR = 10008;
    static final int REQUEST_CODE_FRONT_CALL = 10009;
    static final int REQUEST_CODE_FRONT_RESERVATION_CALL = 10010;
    static final int REQUEST_CODE_LOGIN = 10011;
    static final int REQUEST_CODE_REVIEW = 10012;
    static final int REQUEST_CODE_FAQ = 10013;
    static final int REQUEST_CODE_RECOMMEND_GOURMET_VIEW_ALL = 10014;
    static final int REQUEST_CODE_PREVIEW = 10015;
    static final int REQUEST_CODE_REFUND_CALL = 10016;


    public static Intent newInstance(Context context, int reservationIndex, String aggregationId, String imageUrl, boolean isDeepLink, int bookingState)
    {
        Intent intent = new Intent(context, StayBookingDetailActivity.class);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_BOOKINGIDX, reservationIndex);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_AGGREGATION_ID, aggregationId);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_URL, imageUrl);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_DEEPLINK, isDeepLink);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_BOOKING_STATE, bookingState);

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
    protected StayBookingDetailPresenter createInstancePresenter()
    {
        return new StayBookingDetailPresenter(this);
    }

    @Override
    public void finish()
    {
        super.finish();

        overridePendingTransition(R.anim.hold, R.anim.slide_out_right);
    }
}
