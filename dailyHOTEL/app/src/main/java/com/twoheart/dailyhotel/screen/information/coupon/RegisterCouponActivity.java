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
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;

import java.util.HashMap;
import java.util.Map;

import static com.twoheart.dailyhotel.place.activity.PlaceSearchResultActivity.INTENT_EXTRA_DATA_CALL_BY_SCREEN;

/**
 * Created by android_sam on 2016. 9. 19..
 */
public class RegisterCouponActivity extends BaseActivity
{
    private RegisterCouponLayout mRegisterCouponLayout;
    private RegisterCouponNetworkController mNetworkController;

    private String mCallByScreen;

    public static Intent newInstance(Context context, String callByScreen)
    {
        Intent intent = new Intent(context, RegisterCouponActivity.class);
        intent.putExtra(INTENT_EXTRA_DATA_CALL_BY_SCREEN, callByScreen);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        overridePendingTransition(R.anim.slide_in_right, R.anim.hold);

        super.onCreate(savedInstanceState);

        initIntent(getIntent());

        mRegisterCouponLayout = new RegisterCouponLayout(this, mOnEventListener);
        mNetworkController = new RegisterCouponNetworkController(this, mNetworkTag, mNetworkControllerListener);

        setContentView(mRegisterCouponLayout.onCreateView(R.layout.activity_register_coupon));
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        AnalyticsManager.getInstance(RegisterCouponActivity.this).recordScreen(AnalyticsManager.Screen.MENU_COUPON_REGISTRATION);

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

        setResult(RESULT_OK);
        overridePendingTransition(R.anim.hold, R.anim.slide_out_right);
    }

    private void initIntent(Intent intent)
    {
        if (intent == null)
        {
            return;
        }

        if (intent.hasExtra(INTENT_EXTRA_DATA_CALL_BY_SCREEN) == true)
        {
            mCallByScreen = intent.getStringExtra(INTENT_EXTRA_DATA_CALL_BY_SCREEN);
        }
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
        String message = this.getResources().getString(R.string.dialog_message_register_coupon_login);
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
        Intent intent = LoginActivity.newInstance(this);
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
            if (lockUiComponentAndIsLockUiComponent() == true)
            {
                return;
            }

            mNetworkController.requestRegisterCoupon(couponCode);

            AnalyticsManager.getInstance(RegisterCouponActivity.this).recordEvent(//
                AnalyticsManager.Category.COUPON_BOX, AnalyticsManager.Action.REGISTRATION_CLICKED,//
                mCallByScreen, null);
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
        public void onRegisterCoupon(String couponCode, final boolean isSuccess, int msgCode, String message)
        {
            unLockUI();

            showSimpleDialog(RegisterCouponActivity.this.getString(R.string.dialog_notice2), //
                message, RegisterCouponActivity.this.getString(R.string.dialog_btn_text_confirm), //
                null, new DialogInterface.OnDismissListener()
                {
                    @Override
                    public void onDismiss(DialogInterface dialog)
                    {
                        if (isSuccess == true)
                        {
                            finish();
                        }
                    }
                });

            Map<String, String> params = new HashMap<>();
            params.put(AnalyticsManager.KeyType.COUPON_CODE, couponCode);
            params.put(AnalyticsManager.KeyType.STATUS_CODE, Integer.toString(msgCode));

            AnalyticsManager.getInstance(RegisterCouponActivity.this).recordEvent( //
                AnalyticsManager.Category.COUPON_BOX, //
                isSuccess == true ? AnalyticsManager.Action.REGISTRATION_COMPLETE : AnalyticsManager.Action.REGISTRATION_REJECTED //
                , couponCode, params);
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
