package com.twoheart.dailyhotel.screen.information.coupon;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.android.volley.VolleyError;
import com.twoheart.dailyhotel.DailyHotel;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.place.base.BaseActivity;
import com.twoheart.dailyhotel.screen.information.member.LoginActivity;

/**
 * Created by android_sam on 2016. 9. 19..
 */
public class RegisterCouponActivity extends BaseActivity
{
    private RegisterCouponLayout mRegisterCouponLayout;
    private RegisterCouponNetworkController mNetworkController;

    public static Intent newInstance(Context context)
    {
        Intent intent = new Intent(context, RegisterCouponActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        overridePendingTransition(R.anim.slide_in_right, R.anim.hold);

        super.onCreate(savedInstanceState);

        mRegisterCouponLayout = new RegisterCouponLayout(this, mOnEventListener);
        mNetworkController = new RegisterCouponNetworkController(this, mNetworkTag, mNetworkControllerListener);

        setContentView(mRegisterCouponLayout.onCreateView(R.layout.activity_register_coupon));
    }

    @Override
    protected void onStart()
    {
        super.onStart();

//        AnalyticsManager.getInstance(RegisterCouponActivity.this).recordScreen(AnalyticsManager.Screen.MENU_COUPON_BOX);

        if (DailyHotel.isLogin() == false)
        {
            lockUI();
            showLoginDialog();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void finish()
    {
        super.finish();

        overridePendingTransition(R.anim.hold, R.anim.slide_out_right);
    }

    private void showLoginDialog()
    {
        // 로그인 필요
        View.OnClickListener positiveListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                lockUI();
                startLogin();
            }
        };

        View.OnClickListener negativeListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                RegisterCouponActivity.this.finish();
            }
        };

        String title = this.getResources().getString(R.string.dialog_notice2);
        String message = this.getResources().getString(R.string.dialog_message_coupon_list_login);
        String positive = this.getResources().getString(R.string.dialog_btn_text_yes);
        String negative = this.getResources().getString(R.string.dialog_btn_text_no);

        showSimpleDialog(title, message, positive, negative, positiveListener, negativeListener, new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                RegisterCouponActivity.this.finish();
            }
        }, null, true);
    }

    private void startLogin()
    {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, CODE_REQUEST_ACTIVITY_LOGIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case CODE_REQUEST_ACTIVITY_LOGIN:
            {
                if (resultCode != Activity.RESULT_OK)
                {
                    finish();
                }
                break;
            }
        }
    }

    // ////////////////////////////////////////////////////////
    // EventListener
    // ////////////////////////////////////////////////////////
    private RegisterCouponLayout.OnEventListener mOnEventListener = new RegisterCouponLayout.OnEventListener()
    {

        @Override
        public void onRegisterCoupon(String couponCode)
        {
            // 쿠폰 사용내역 이동
            if (lockUiComponentAndIsLockUiComponent() == true)
            {
                return;
            }

            // TODO : 쿠폰 등록 요청
            mNetworkController.requestRegisterCoupon(couponCode);
        }

        @Override
        public void finish()
        {
            RegisterCouponActivity.this.finish();
        }
    };

    // ///////////////////////////////////////////////////
    // NetworkController
    // ///////////////////////////////////////////////////
    private RegisterCouponNetworkController.OnNetworkControllerListener mNetworkControllerListener = new RegisterCouponNetworkController.OnNetworkControllerListener()
    {
        @Override
        public void onRegisterCoupon(String couponCode)
        {
            // TODO : 쿠폰 등록 완료 동작

        }

        @Override
        public void onErrorResponse(VolleyError volleyError)
        {
            RegisterCouponActivity.this.onErrorResponse(volleyError);
        }

        @Override
        public void onError(Exception e)
        {
            RegisterCouponActivity.this.onError(e);
        }

        @Override
        public void onErrorPopupMessage(int msgCode, String message)
        {
            RegisterCouponActivity.this.onErrorPopupMessage(msgCode, message);
        }

        @Override
        public void onErrorToastMessage(String message)
        {
            RegisterCouponActivity.this.onErrorToastMessage(message);
        }

    };
}
