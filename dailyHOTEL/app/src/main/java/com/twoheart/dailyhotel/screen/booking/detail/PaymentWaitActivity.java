/**
 * Copyright (c) 2014 Daily Co., Ltd. All rights reserved.
 * <p>
 * PaymentWaitActivity (입금대기 화면)
 * <p>
 * 계좌이체 결제 선택 후 입금대기 상태 화면
 * 가상계좌 정보를 보여주는 화면이다.
 */
package com.twoheart.dailyhotel.screen.booking.detail;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Booking;
import com.twoheart.dailyhotel.network.DailyMobileAPI;
import com.twoheart.dailyhotel.place.base.BaseActivity;
import com.twoheart.dailyhotel.screen.common.HappyTalkCategoryDialog;
import com.twoheart.dailyhotel.screen.information.FAQActivity;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.DailyCalendar;
import com.twoheart.dailyhotel.util.EdgeEffectColor;
import com.twoheart.dailyhotel.util.ExLog;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;
import com.twoheart.dailyhotel.widget.DailyTextView;
import com.twoheart.dailyhotel.widget.DailyToast;
import com.twoheart.dailyhotel.widget.DailyToolbarLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

import retrofit2.Call;
import retrofit2.Response;

public class PaymentWaitActivity extends BaseActivity
{
    TextView mAccountTextView;
    private TextView mDailyTextView;
    private TextView mPriceTextView, mBonusTextView, mCouponTextView, mTotalPriceTextView;
    private TextView mDeadlineTextView;
    private ViewGroup mGuide1Layout;
    private View mBonusLayout, mCouponLayout;

    private Booking mBooking;

    public static Intent newInstance(Context context, Booking booking)
    {
        Intent intent = new Intent(context, PaymentWaitActivity.class);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_BOOKING, booking);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        mBooking = intent.getParcelableExtra(NAME_INTENT_EXTRA_DATA_BOOKING);

        if (mBooking == null)
        {
            Util.restartApp(this);
            return;
        }

        setContentView(R.layout.activity_payment_wait);

        initToolbar();
        initLayout(mBooking);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (mBooking != null)
        {
            switch (mBooking.placeType)
            {
                case HOTEL:
                    AnalyticsManager.getInstance(this).recordScreen(this, AnalyticsManager.Screen.DAILYHOTEL_DEPOSITWAITING, null);
                    break;

                case FNB:
                    AnalyticsManager.getInstance(this).recordScreen(this, AnalyticsManager.Screen.DAILYGOURMET_DEPOSITWAITING, null);
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case CODE_REQUEST_ACTIVITY_FAQ:
            {
                if (resultCode == CODE_RESULT_ACTIVITY_GO_HOME)
                {
                    setResult(CODE_RESULT_ACTIVITY_GO_HOME);
                    finish();
                }
                break;
            }
        }
    }

    private void initToolbar()
    {
        View toolbar = findViewById(R.id.toolbar);
        DailyToolbarLayout dailyToolbarLayout = new DailyToolbarLayout(this, toolbar);
        dailyToolbarLayout.initToolbar(getString(R.string.actionbar_title_payment_wait_activity), new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
        dailyToolbarLayout.setToolbarMenu(R.drawable.navibar_ic_help, -1);
        dailyToolbarLayout.setToolbarMenuClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showCallDialog();
            }
        });
    }

    private void initLayout(Booking booking)
    {
        if (booking == null)
        {
            return;
        }

        ScrollView scrollLayout = (ScrollView) findViewById(R.id.scrollLayout);
        EdgeEffectColor.setEdgeGlowColor(scrollLayout, getResources().getColor(R.color.default_over_scroll_edge));

        TextView placeNameTextView = (TextView) findViewById(R.id.paymentWaitHotelNameView);
        mAccountTextView = (TextView) findViewById(R.id.tv_payment_wait_account);
        mDailyTextView = (TextView) findViewById(R.id.tv_payment_wait_name);
        mPriceTextView = (TextView) findViewById(R.id.priceTextView);
        mBonusTextView = (TextView) findViewById(R.id.bonusTextView);
        mCouponTextView = (TextView) findViewById(R.id.couponTextView);
        mTotalPriceTextView = (TextView) findViewById(R.id.totalPriceTextView);
        mDeadlineTextView = (TextView) findViewById(R.id.tv_payment_wait_deadline);
        mGuide1Layout = (ViewGroup) findViewById(R.id.guide1Layout);

        mBonusLayout = findViewById(R.id.bonusLayout);
        mCouponLayout = findViewById(R.id.couponLayout);

        View view = findViewById(R.id.editLinearLayout);
        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mAccountTextView == null)
                {
                    return;
                }

                Util.clipText(PaymentWaitActivity.this, (String) mAccountTextView.getTag());

                DailyToast.showToast(PaymentWaitActivity.this, R.string.message_detail_copy_account_number, Toast.LENGTH_SHORT);
            }
        });

        placeNameTextView.setText(booking.placeName);

        lockUI();

        switch (booking.placeType)
        {
            case HOTEL:
            {
                DailyMobileAPI.getInstance(this).requestDepositWaitDetailInformation(mNetworkTag, booking.tid, mHotelReservationCallback);
                break;
            }

            case FNB:
            {
                DailyMobileAPI.getInstance(this).requestGourmetAccountInformation(mNetworkTag, booking.tid, mFnBReservationCallback);
                break;
            }
        }
    }

    void showCallDialog()
    {
        if (isFinishing())
        {
            return;
        }

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = layoutInflater.inflate(R.layout.view_dialog_contact_us_layout, null, false);

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(true);

        // 버튼
        View contactUs01Layout = dialogView.findViewById(R.id.contactUs01Layout);
        View contactUs02Layout = dialogView.findViewById(R.id.contactUs02Layout);
        contactUs02Layout.setVisibility(View.GONE);

        DailyTextView contactUs01TextView = (DailyTextView) contactUs01Layout.findViewById(R.id.contactUs01TextView);
        contactUs01TextView.setText(R.string.frag_faqs);
        contactUs01TextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.popup_ic_ops_05_faq, 0, 0, 0);

        contactUs01Layout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (dialog.isShowing() == true)
                {
                    dialog.dismiss();
                }

                startFAQ();
            }
        });

        View kakaoDailyView = dialogView.findViewById(R.id.kakaoDailyView);
        View callDailyView = dialogView.findViewById(R.id.callDailyView);

        kakaoDailyView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (dialog.isShowing() == true)
                {
                    dialog.dismiss();
                }

                startKakao();
            }
        });

        callDailyView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (dialog.isShowing() == true)
                {
                    dialog.dismiss();
                }

                startCall();
            }
        });

        View closeView = dialogView.findViewById(R.id.closeView);
        closeView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (dialog.isShowing() == true)
                {
                    dialog.dismiss();
                }
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialog)
            {
                unLockUI();
            }
        });

        try
        {
            dialog.setContentView(dialogView);

            WindowManager.LayoutParams layoutParams = Util.getDialogWidthLayoutParams(this, dialog);

            dialog.show();

            dialog.getWindow().setAttributes(layoutParams);
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }
    }

    private void setGuideText(ViewGroup viewGroups, String[] guides, boolean isImportant)
    {
        if (guides == null)
        {
            return;
        }

        for (String guide : guides)
        {
            View textLayout = LayoutInflater.from(this).inflate(R.layout.list_row_detail_text, viewGroups, false);
            TextView textView = (TextView) textLayout.findViewById(R.id.textView);
            textView.setText(guide.replace("\n", " ").trim() + ".");

            if (isImportant == true)
            {
                textView.setTextColor(getResources().getColor(R.color.dh_theme_color));
            }

            viewGroups.addView(textLayout);
        }
    }

    void setHotelReservationData(JSONObject jsonObject) throws JSONException, ParseException
    {
        JSONObject reservationJSONObject = jsonObject.getJSONObject("reservation");
        String accountNumber = reservationJSONObject.getString("vactNum");
        mAccountTextView.setText(reservationJSONObject.getString("bankName") + ", " + accountNumber);
        mAccountTextView.setTag(accountNumber);

        mDailyTextView.setText(reservationJSONObject.getString("vactName"));

        // 입금기한
        //        String validToDate = Util.simpleDateFormatISO8601toFormat(reservationJSONObject.getString("validTo"), "yyyy년 MM월 dd일 HH시 mm분 까지");
        String validToDate = DailyCalendar.convertDateFormatString(reservationJSONObject.getString("validTo"), DailyCalendar.ISO_8601_FORMAT, "yyyy년 MM월 dd일 HH시 mm분 까지");
        mDeadlineTextView.setText(validToDate);

        // 결재 금액 정보
        mPriceTextView.setText(Util.getPriceFormat(this, reservationJSONObject.getInt("price"), false));

        int bonus = reservationJSONObject.getInt("bonus");

        if (bonus > 0)
        {
            mBonusLayout.setVisibility(View.VISIBLE);
            mBonusTextView.setText("- " + Util.getPriceFormat(this, bonus, false));
        } else
        {
            mBonusLayout.setVisibility(View.GONE);
        }

        int coupon = reservationJSONObject.getInt("couponAmount");

        if (coupon > 0)
        {
            mCouponLayout.setVisibility(View.VISIBLE);
            mCouponTextView.setText("- " + Util.getPriceFormat(this, coupon, false));
        } else
        {
            mCouponLayout.setVisibility(View.GONE);
        }

        int paymentPrice = reservationJSONObject.getInt("amt");

        mTotalPriceTextView.setText(Util.getPriceFormat(this, paymentPrice, false));

        // 확인 사항
        String msg1 = jsonObject.getString("msg1");
        setGuideText(mGuide1Layout, msg1.split("\\."), false);

        String msg2 = getString(R.string.message__wait_payment03);
        setGuideText(mGuide1Layout, msg2.split("\\."), true);
    }

    void setGourmetReservationData(JSONObject jsonObject) throws JSONException
    {
        String accountNumber = jsonObject.getString("account_num");
        mAccountTextView.setText(jsonObject.getString("bank_name") + ", " + accountNumber);
        mAccountTextView.setTag(accountNumber);

        mDailyTextView.setText(jsonObject.getString("name"));

        String[] dateSlice = jsonObject.getString("date").split("/");
        String[] timeSlice = jsonObject.getString("time").split(":");

        String date = String.format("%s년 %s월 %s일", dateSlice[0], dateSlice[1], dateSlice[2]);

        mDeadlineTextView.setText(String.format("%s %s시 %s분 까지", date, timeSlice[0], timeSlice[1]));

        int coupon = jsonObject.getInt("coupon_amount");

        if (coupon > 0)
        {
            mCouponLayout.setVisibility(View.VISIBLE);
            mCouponTextView.setText("- " + Util.getPriceFormat(this, coupon, false));
        } else
        {
            mCouponLayout.setVisibility(View.GONE);
        }

        mPriceTextView.setText(Util.getPriceFormat(this, jsonObject.getInt("price"), false));

        int paymentPrice = jsonObject.getInt("amt");
        mTotalPriceTextView.setText(Util.getPriceFormat(this, paymentPrice, false));

        String msg1 = jsonObject.getString("msg1");
        setGuideText(mGuide1Layout, msg1.split("\\."), false);

        String msg2 = getString(R.string.message__wait_payment03);
        setGuideText(mGuide1Layout, msg2.split("\\."), true);
    }

    void startFAQ()
    {
        startActivityForResult(new Intent(this, FAQActivity.class), CODE_REQUEST_ACTIVITY_FAQ);
    }

    void startCall()
    {
        if (isFinishing() == true)
        {
            return;
        }

        showDailyCallDialog(new OnCallDialogListener()
        {
            @Override
            public void onShowDialog()
            {
                AnalyticsManager.getInstance(PaymentWaitActivity.this).recordEvent(//
                    AnalyticsManager.Category.CALL_BUTTON_CLICKED, AnalyticsManager.Action.DEPOSIT_WAITING,//
                    AnalyticsManager.Label.CLICK, null);
            }

            @Override
            public void onPositiveButtonClick(View v)
            {
                AnalyticsManager.getInstance(PaymentWaitActivity.this).recordEvent(//
                    AnalyticsManager.Category.CALL_BUTTON_CLICKED, AnalyticsManager.Action.DEPOSIT_WAITING,//
                    AnalyticsManager.Label.CALL, null);
            }

            @Override
            public void onNativeButtonClick(View v)
            {
                AnalyticsManager.getInstance(PaymentWaitActivity.this).recordEvent(//
                    AnalyticsManager.Category.CALL_BUTTON_CLICKED, AnalyticsManager.Action.DEPOSIT_WAITING,//
                    AnalyticsManager.Label.CANCEL, null);
            }
        });
    }

    void startKakao()
    {
        AnalyticsManager.getInstance(this).recordEvent(AnalyticsManager.Category.CALL_BUTTON_CLICKED,//
            AnalyticsManager.Action.BOOKING_DETAIL, AnalyticsManager.Label.KAKAO, null);

        switch (mBooking.placeType)
        {
            case HOTEL:
                startActivityForResult(HappyTalkCategoryDialog.newInstance(this, HappyTalkCategoryDialog.SCREEN_STAY_PAMENT_WAIT, 0, mBooking.reservationIndex), Constants.CODE_REQUEST_ACTIVITY_HAPPY_TALK);
                break;

            case FNB:
                startActivityForResult(HappyTalkCategoryDialog.newInstance(this, HappyTalkCategoryDialog.SCREEN_GOURMET_PAMENT_WAIT, 0, mBooking.reservationIndex), Constants.CODE_REQUEST_ACTIVITY_HAPPY_TALK);
                break;
        }

        //        try
        //        {
        //            switch (mBooking.placeType)
        //            {
        //                case HOTEL:
        //
        //                    startActivity(new Intent(Intent.ACTION_SEND, Uri.parse("kakaolink://friend/@%EB%8D%B0%EC%9D%BC%EB%A6%AC%ED%98%B8%ED%85%94")));
        //                    break;
        //
        //                case FNB:
        //                    startActivity(new Intent(Intent.ACTION_SEND, Uri.parse("kakaolink://friend/%40%EB%8D%B0%EC%9D%BC%EB%A6%AC%EA%B3%A0%EB%A9%94")));
        //                    break;
        //            }
        //        } catch (ActivityNotFoundException e)
        //        {
        //            try
        //            {
        //                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL_STORE_GOOGLE_KAKAOTALK)));
        //            } catch (ActivityNotFoundException e1)
        //            {
        //                Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
        //                marketLaunch.setData(Uri.parse(URL_STORE_GOOGLE_KAKAOTALK_WEB));
        //                startActivity(marketLaunch);
        //            }
        //        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Listener
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private retrofit2.Callback mHotelReservationCallback = new retrofit2.Callback<JSONObject>()
    {
        @Override
        public void onResponse(Call<JSONObject> call, Response<JSONObject> response)
        {
            if (response != null && response.isSuccessful() && response.body() != null)
            {
                try
                {
                    JSONObject responseJSONObject = response.body();

                    int msgCode = responseJSONObject.getInt("msgCode");

                    if (msgCode == 100)
                    {
                        JSONObject dataJSONObject = responseJSONObject.getJSONObject("data");

                        setHotelReservationData(dataJSONObject);
                    } else
                    {
                        Intent intent = new Intent();
                        intent.putExtra("msg", responseJSONObject.getString("msg"));
                        setResult(CODE_RESULT_ACTIVITY_EXPIRED_PAYMENT_WAIT, intent);
                        finish();
                    }
                } catch (Exception e)
                {
                    ExLog.e(e.toString());
                    finish();
                } finally
                {
                    unLockUI();
                }
            } else
            {
                PaymentWaitActivity.this.onErrorResponse(call, response);
            }
        }

        @Override
        public void onFailure(Call<JSONObject> call, Throwable t)
        {
            PaymentWaitActivity.this.onError(t);
        }
    };

    private retrofit2.Callback mFnBReservationCallback = new retrofit2.Callback<JSONObject>()
    {
        @Override
        public void onResponse(Call<JSONObject> call, Response<JSONObject> response)
        {
            if (response != null && response.isSuccessful() && response.body() != null)
            {
                try
                {
                    JSONObject responseJSONObject = response.body();

                    int msgCode = responseJSONObject.getInt("msg_code");

                    if (msgCode == 0)
                    {
                        JSONObject jsonObject = responseJSONObject.getJSONObject("data");

                        setGourmetReservationData(jsonObject);
                    } else
                    {
                        Intent intent = new Intent();
                        intent.putExtra("msg", responseJSONObject.getString("msg"));
                        setResult(CODE_RESULT_ACTIVITY_EXPIRED_PAYMENT_WAIT, intent);
                        finish();
                    }
                } catch (Exception e)
                {
                    onError(e);
                    finish();
                } finally
                {
                    unLockUI();
                }
            } else
            {
                PaymentWaitActivity.this.onErrorResponse(call, response);
            }
        }

        @Override
        public void onFailure(Call<JSONObject> call, Throwable t)
        {
            PaymentWaitActivity.this.onError(t);
        }
    };
}
