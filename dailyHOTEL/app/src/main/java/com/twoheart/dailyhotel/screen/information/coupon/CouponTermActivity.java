package com.twoheart.dailyhotel.screen.information.coupon;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.network.request.DailyHotelRequest;
import com.twoheart.dailyhotel.screen.common.WebViewActivity;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;
import com.twoheart.dailyhotel.widget.DailyToolbarLayout;

/**
 * Created by android_sam on 2016. 5. 30..
 */
public class CouponTermActivity extends WebViewActivity
{
    private static final String INTENT_EXTRA_DATA_COUPON_IDX = "coupon_idx";

    private String mCouponIdx = "";

    /**
     * 공통 쿠폰 유의 사항
     *
     * @param context
     * @return
     */
    public static Intent newInstance(Context context)
    {
        Intent intent = new Intent(context, CouponTermActivity.class);
        intent.putExtra(INTENT_EXTRA_DATA_COUPON_IDX, "");
        return intent;
    }

    /**
     * 개별 쿠폰 유의 사항
     *
     * @param context
     * @param couponIdx 쿠폰 번호 ,  null 일때 공통 쿠폰 유의사항으로 이동
     * @return
     */
    public static Intent newInstance(Context context, String couponIdx)
    {
        Intent intent = new Intent(context, CouponTermActivity.class);
        intent.putExtra(INTENT_EXTRA_DATA_COUPON_IDX, Util.isTextEmpty(couponIdx) ? "" : couponIdx);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bonus_n_coupon_term);

        Intent intent = getIntent();

        if (intent == null)
        {
            finish();
            return;
        }

        if (intent.hasExtra(INTENT_EXTRA_DATA_COUPON_IDX))
        {
            mCouponIdx = intent.getStringExtra(INTENT_EXTRA_DATA_COUPON_IDX);
        }

        initToolbar();
    }

    private void initToolbar()
    {
        int titleString;

        if (Util.isTextEmpty(mCouponIdx) == false)
        {
            titleString = R.string.coupon_notice_text;
        } else
        {
            titleString = R.string.coupon_use_notice_text;
        }

        View toolbar = findViewById(R.id.toolbar);
        DailyToolbarLayout dailyToolbarLayout = new DailyToolbarLayout(this, toolbar);
        dailyToolbarLayout.initToolbar(getString(titleString), new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (Util.isTextEmpty(mCouponIdx) == true)
        {
            AnalyticsManager.getInstance(CouponTermActivity.this).recordScreen(AnalyticsManager.Screen.MENU_COUPON_GENERAL_TERMS_OF_USE);
        } else
        {
            AnalyticsManager.getInstance(CouponTermActivity.this).recordScreen(AnalyticsManager.Screen.MENU_COUPON_INDIVIDUAL_TERMS_OF_USE);
        }

    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (Util.isTextEmpty(mCouponIdx) == true)
        {
            setWebView(DailyHotelRequest.getUrlDecoderEx(URL_WEB_COMMON_COUPON_TERMS));
        } else
        {
            setWebView(DailyHotelRequest.getUrlDecoderEx(URL_WEB_EACH_COUPON_TERMS) + mCouponIdx);
        }
    }
}
