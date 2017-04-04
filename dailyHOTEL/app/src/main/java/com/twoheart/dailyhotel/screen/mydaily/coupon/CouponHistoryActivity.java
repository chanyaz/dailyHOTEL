package com.twoheart.dailyhotel.screen.mydaily.coupon;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.CouponHistory;
import com.twoheart.dailyhotel.place.base.BaseActivity;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by Sam Lee on 2016. 5. 23..
 */
public class CouponHistoryActivity extends BaseActivity
{
    CouponHistoryLayout mCouponHistoryLayout;
    private CouponHistoryNetworkController mCouponHistoryNetworkController;

    public static Intent newInstance(Context context)
    {
        Intent intent = new Intent(context, CouponHistoryActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        overridePendingTransition(R.anim.slide_in_right, R.anim.hold);

        super.onCreate(savedInstanceState);

        mCouponHistoryLayout = new CouponHistoryLayout(this, mOnEventListener);
        mCouponHistoryNetworkController = new CouponHistoryNetworkController(this, mNetworkTag, mNetworkControllerListener);

        setContentView(mCouponHistoryLayout.onCreateView(R.layout.activity_coupon_history));
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        AnalyticsManager.getInstance(CouponHistoryActivity.this).recordScreen(this, AnalyticsManager.Screen.MENU_COUPON_HISTORY, null);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        lockUI();

        mCouponHistoryNetworkController.requestCouponHistoryList();
    }

    @Override
    public void finish()
    {
        super.finish();

        overridePendingTransition(R.anim.hold, R.anim.slide_out_right);
    }

    // ////////////////////////////////////////////////////////
    // EventListener
    // ////////////////////////////////////////////////////////
    private CouponHistoryLayout.OnEventListener mOnEventListener = new CouponHistoryLayout.OnEventListener()
    {
        @Override
        public void onHomeClick()
        {
            setResult(Constants.CODE_RESULT_ACTIVITY_GO_HOME);
            finish();
        }

        @Override
        public void finish()
        {
            CouponHistoryActivity.this.finish();
        }
    };

    // ///////////////////////////////////////////////////
    // NetworkController
    // ///////////////////////////////////////////////////
    private CouponHistoryNetworkController.OnNetworkControllerListener mNetworkControllerListener = new CouponHistoryNetworkController.OnNetworkControllerListener()
    {

        @Override
        public void onCouponHistoryList(List<CouponHistory> list)
        {
            mCouponHistoryLayout.setData(list);

            unLockUI();
        }

        @Override
        public void onError(Call call, Throwable e, boolean onlyReport)
        {
            CouponHistoryActivity.this.onError(call, e, onlyReport);
        }

        @Override
        public void onError(Throwable e)
        {
            CouponHistoryActivity.this.onError(e);
        }

        @Override
        public void onErrorPopupMessage(int msgCode, String message)
        {
            CouponHistoryActivity.this.onErrorPopupMessage(msgCode, message);
        }

        @Override
        public void onErrorToastMessage(String message)
        {
            CouponHistoryActivity.this.onErrorToastMessage(message);
        }

        @Override
        public void onErrorResponse(Call call, Response response)
        {
            CouponHistoryActivity.this.onErrorResponse(call, response);
        }
    };

}
