package com.twoheart.dailyhotel.place.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.twoheart.dailyhotel.DailyHotel;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Coupon;
import com.twoheart.dailyhotel.model.CreditCard;
import com.twoheart.dailyhotel.model.PlacePaymentInformation;
import com.twoheart.dailyhotel.model.SaleTime;
import com.twoheart.dailyhotel.network.DailyMobileAPI;
import com.twoheart.dailyhotel.place.base.BaseActivity;
import com.twoheart.dailyhotel.screen.mydaily.coupon.SelectStayCouponDialogActivity;
import com.twoheart.dailyhotel.screen.mydaily.creditcard.CreditCardListActivity;
import com.twoheart.dailyhotel.screen.mydaily.creditcard.RegisterCreditCardActivity;
import com.twoheart.dailyhotel.screen.mydaily.member.InputMobileNumberDialogActivity;
import com.twoheart.dailyhotel.screen.mydaily.member.LoginActivity;
import com.twoheart.dailyhotel.util.Crypto;
import com.twoheart.dailyhotel.util.DailyPreference;
import com.twoheart.dailyhotel.util.ExLog;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager.Action;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager.Label;

import org.json.JSONArray;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Response;

public abstract class PlacePaymentActivity extends BaseActivity
{
    protected static final int REQUEST_CODE_COUNTRYCODE_DIALOG_ACTIVITY = 10000;
    protected static final int REQUEST_CODE_PAYMETRESULT_ACTIVITY = 10001;
    protected static final int REQUEST_CODE_COUPONPOPUP_ACTIVITY = 10002;

    protected static final int PHONE_PAYMENT_LIMIT = 500000;

    protected PlacePaymentInformation mPaymentInformation;
    protected CreditCard mSelectedCreditCard;
    protected Dialog mFinalCheckDialog;
    protected SaleTime mCheckInSaleTime;

    private ProgressDialog mProgressDialog;

    protected boolean mDontReload;

    protected abstract void requestUserInformationForPayment();

    protected abstract void requestEasyPayment(PlacePaymentInformation paymentInformation, SaleTime checkInSaleTime);

    protected abstract void requestFreePayment(PlacePaymentInformation paymentInformation, SaleTime checkInSaleTime);

    protected abstract void requestPlacePaymentInformation(PlacePaymentInformation paymentInformation, SaleTime checkInSaleTime);

    protected abstract void setSimpleCardInformation(PlacePaymentInformation paymentInformation, CreditCard selectedCreditCard);

    protected abstract void setGuestInformation(String phoneNumber);

    protected abstract void changedPaymentType(PlacePaymentInformation.PaymentType paymentType, CreditCard creditCard);

    protected abstract boolean isChangedPrice();

    protected abstract boolean hasWarningMessage();

    protected abstract void showWarningMessageDialog();

    protected abstract void showChangedPriceDialog();

    protected abstract void showStopOnSaleDialog();

    protected abstract void showPaymentWeb(PlacePaymentInformation paymentInformation, SaleTime checkInSaleTime);

    protected abstract void showPaymentThankyou(PlacePaymentInformation paymentInformation, String imageUrl);

    protected abstract Dialog getEasyPaymentConfirmDialog();

    protected abstract Dialog getPaymentConfirmDialog(PlacePaymentInformation.PaymentType paymentType);

    protected abstract void onActivityPaymentResult(int requestCode, int resultCode, Intent intent);

    protected abstract void recordAnalyticsAgreeTermDialog(PlacePaymentInformation paymentInformation);

    protected abstract void recordAnalyticsPayment(PlacePaymentInformation paymentInformation);

    protected abstract void setCoupon(Coupon coupon);

    protected abstract void setCancelCoupon();

    protected abstract void recordPaymentInformation();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        overridePendingTransition(R.anim.slide_in_right, R.anim.hold);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (DailyHotel.isLogin() == false)
        {
            requestLogin();
        } else
        {
            if (mDontReload == true)
            {
                mDontReload = false;
            } else
            {
                lockUI();
                requestUserInformationForPayment();
            }
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if (mFinalCheckDialog != null && mFinalCheckDialog.isShowing() == true)
        {
            mFinalCheckDialog.dismiss();
        }

        mFinalCheckDialog = null;
    }

    @Override
    public void finish()
    {
        super.finish();

        overridePendingTransition(R.anim.hold, R.anim.slide_out_right);
    }

    @Override
    protected void onDestroy()
    {
        if (mFinalCheckDialog != null && mFinalCheckDialog.isShowing() == true)
        {
            mFinalCheckDialog.dismiss();
        }

        mFinalCheckDialog = null;

        hideProgressDialog();

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putParcelable(NAME_INTENT_EXTRA_DATA_PAYMENTINFORMATION, mPaymentInformation);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        mPaymentInformation = savedInstanceState.getParcelable(NAME_INTENT_EXTRA_DATA_PAYMENTINFORMATION);
    }

    @Override
    public void onErrorResponse(Call call, Response response)
    {
        super.onErrorResponse(call, response);

        hideProgressDialog();
    }

    @Override
    public void onError()
    {
        super.onError();

        setResult(CODE_RESULT_ACTIVITY_REFRESH);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);

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

            case REQUEST_CODE_COUNTRYCODE_DIALOG_ACTIVITY:
            {
                mDontReload = true;

                if (resultCode == RESULT_OK && intent != null)
                {
                    String mobileNumber = intent.getStringExtra(InputMobileNumberDialogActivity.INTENT_EXTRA_MOBILE_NUMBER);
                    setGuestInformation(mobileNumber);
                }
                break;
            }

            case CODE_REQUEST_ACTIVITY_CREDITCARD_MANAGER:
            {
                // 신용카드 간편 결제 선택후
                if (resultCode == Activity.RESULT_OK && intent != null)
                {
                    CreditCard creditCard = intent.getParcelableExtra(NAME_INTENT_EXTRA_DATA_CREDITCARD);

                    if (creditCard != null)
                    {
                        changedPaymentType(PlacePaymentInformation.PaymentType.EASY_CARD, creditCard);
                    }
                }
                break;
            }

            case REQUEST_CODE_PAYMETRESULT_ACTIVITY:
            {
                mDontReload = true;

                recordPaymentInformation();

                setResult(RESULT_OK);
                finish();
                return;
            }

            case CODE_REQUEST_ACTIVITY_REGISTERCREDITCARD_AND_PAYMENT:
            case CODE_REQUEST_ACTIVITY_REGISTERCREDITCARD:
            {
                // 간편 결제 실행후 카드가 없어 등록후에 돌아온경우.
                String msg = null;

                switch (resultCode)
                {
                    case CODE_RESULT_PAYMENT_BILLING_SUCCSESS:
                        if (requestCode == CODE_REQUEST_ACTIVITY_REGISTERCREDITCARD_AND_PAYMENT)
                        {
                            mDontReload = true;

                            // 신용카드 등록후에 바로 결제를 할경우.
                            DailyMobileAPI.getInstance(this).requestUserBillingCardList(mNetworkTag, mPaymentAfterRegisterCreditCardCallback);
                        } else
                        {
                            mDontReload = false;

                            changedPaymentType(PlacePaymentInformation.PaymentType.EASY_CARD, null);
                        }
                        return;

                    case CODE_RESULT_PAYMENT_BILLING_DUPLICATE:
                        msg = getString(R.string.message_billing_duplicate);
                        break;

                    case CODE_RESULT_PAYMENT_BILLING_FAIL:
                        msg = getString(R.string.message_billing_fail);
                        break;

                    case CODE_RESULT_ACTIVITY_PAYMENT_INVALID_SESSION:
                        restartExpiredSession();
                        return;

                    case CODE_RESULT_ACTIVITY_PAYMENT_FAIL:
                        msg = getString(R.string.act_toast_payment_fail);
                        break;

                    case CODE_RESULT_ACTIVITY_PAYMENT_NETWORK_ERROR:
                        msg = getString(R.string.act_toast_payment_network_error);
                        break;
                }

                if (Util.isTextEmpty(msg) == false)
                {
                    String title = getString(R.string.dialog_notice2);
                    String positive = getString(R.string.dialog_btn_text_confirm);

                    showSimpleDialog(title, msg, positive, null);
                }
                break;
            }

            case CODE_REQUEST_ACTIVITY_PAYMENT:
            {
                mDontReload = true;

                unLockUI();

                onActivityPaymentResult(requestCode, resultCode, intent);
                break;
            }

            case REQUEST_CODE_COUPONPOPUP_ACTIVITY:
            {
                mDontReload = true;

                unLockUI();

                if (resultCode == Activity.RESULT_OK && intent != null)
                {
                    Coupon coupon = intent.getParcelableExtra(SelectStayCouponDialogActivity.INTENT_EXTRA_SELECT_COUPON);

                    setCoupon(coupon);
                } else
                {
                    setCancelCoupon();
                }
                break;
            }

            default:
                break;
        }
    }

    protected void showProgressDialog()
    {
        hideProgressDialog();

        try
        {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.dialog_msg_processing_payment));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }
    }

    protected void hideProgressDialog()
    {
        if (mProgressDialog != null)
        {
            if (mProgressDialog.isShowing() == true)
            {
                mProgressDialog.dismiss();
            }

            mProgressDialog = null;
        }
    }

    /**
     * 실제 결제를 진행
     *
     * @param paymentInformation
     * @param saleTime
     */
    protected void processPayment(PlacePaymentInformation paymentInformation, SaleTime saleTime)
    {
        if (paymentInformation == null || saleTime == null || isFinishing() == true)
        {
            setResult(CODE_RESULT_ACTIVITY_REFRESH);
            finish();
            return;
        }

        if (mFinalCheckDialog != null && mFinalCheckDialog.isShowing() == true)
        {
            mFinalCheckDialog.dismiss();
        }

        // 실제 결제 금액이 0원인 경우에는 바로 결제로 넘어갈수 있도록 한다.
        if (paymentInformation.isFree == true)
        {
            showProgressDialog();

            requestFreePayment(paymentInformation, saleTime);
        } else
        {
            if (paymentInformation.paymentType == PlacePaymentInformation.PaymentType.EASY_CARD)
            {
                showProgressDialog();

                requestEasyPayment(paymentInformation, saleTime);
            } else
            {
                lockUI();

                showPaymentWeb(paymentInformation, saleTime);
            }
        }
    }

    protected void processAgreeTermDialog()
    {
        unLockUI();

        // 실제 결제 금액이 0원인 경우에는 바로 결제로 넘어갈수 있도록 한다.
        if (mPaymentInformation.isFree == true)
        {
            showAgreeTermDialog();
        } else
        {
            if (mPaymentInformation.paymentType == PlacePaymentInformation.PaymentType.EASY_CARD && mSelectedCreditCard == null)
            {
                Intent intent = new Intent(this, RegisterCreditCardActivity.class);
                startActivityForResult(intent, CODE_REQUEST_ACTIVITY_REGISTERCREDITCARD_AND_PAYMENT);
            } else
            {
                showAgreeTermDialog(mPaymentInformation.paymentType);
            }
        }
    }

    /**
     * 무료 결제인 경우 결제 팝업
     */
    protected void showAgreeTermDialog()
    {
        if (mFinalCheckDialog != null)
        {
            mFinalCheckDialog.cancel();
        }

        // 무료 결제인 경우 일반 카드와 동일한 확인 사항을 출력한다.
        mFinalCheckDialog = null;
        mFinalCheckDialog = getPaymentConfirmDialog(PlacePaymentInformation.PaymentType.CARD);

        if (mFinalCheckDialog == null || isFinishing() == true)
        {
            return;
        }

        mFinalCheckDialog.setOnDismissListener(new OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialog)
            {
                releaseUiComponent();
            }
        });

        mFinalCheckDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                AnalyticsManager.getInstance(PlacePaymentActivity.this).recordEvent(AnalyticsManager.Category.POPUP_BOXES//
                    , Action.PAYMENT_AGREEMENT_POPPEDUP, Label.CANCEL, null);
            }
        });

        try
        {
            mFinalCheckDialog.show();

            recordAnalyticsAgreeTermDialog(mPaymentInformation);
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }
    }

    protected void showAgreeTermDialog(PlacePaymentInformation.PaymentType paymentType)
    {
        if (paymentType == null)
        {
            return;
        }

        if (mFinalCheckDialog != null)
        {
            mFinalCheckDialog.cancel();
        }

        mFinalCheckDialog = null;

        switch (paymentType)
        {
            case EASY_CARD:
                mFinalCheckDialog = getEasyPaymentConfirmDialog();
                break;

            case CARD:
            case PHONE_PAY:
            case VBANK:
                mFinalCheckDialog = getPaymentConfirmDialog(paymentType);
                break;

            default:
                return;
        }

        if (mFinalCheckDialog == null || isFinishing() == true)
        {
            return;
        }

        mFinalCheckDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                unLockUI();

                AnalyticsManager.getInstance(PlacePaymentActivity.this).recordEvent(AnalyticsManager.Category.POPUP_BOXES//
                    , Action.PAYMENT_AGREEMENT_POPPEDUP, Label.CANCEL, null);
            }
        });

        try
        {
            WindowManager.LayoutParams layoutParams = Util.getDialogWidthLayoutParams(this, mFinalCheckDialog);

            mFinalCheckDialog.show();

            mFinalCheckDialog.getWindow().setAttributes(layoutParams);

            recordAnalyticsAgreeTermDialog(mPaymentInformation);
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }
    }

    protected void showCallDialog()
    {
        showDailyCallDialog(new OnCallDialogListener()
        {
            @Override
            public void onShowDialog()
            {
                AnalyticsManager.getInstance(PlacePaymentActivity.this).recordEvent( //
                    AnalyticsManager.Category.CALL_BUTTON_CLICKED, //
                    AnalyticsManager.Action.BOOKING_INITIALISE, Label.CLICK, null);
            }

            @Override
            public void onPositiveButtonClick(View v)
            {
                AnalyticsManager.getInstance(PlacePaymentActivity.this).recordEvent(//
                    AnalyticsManager.Category.CALL_BUTTON_CLICKED, //
                    AnalyticsManager.Action.BOOKING_INITIALISE, Label.CALL, null);
            }

            @Override
            public void onNativeButtonClick(View v)
            {
                AnalyticsManager.getInstance(PlacePaymentActivity.this).recordEvent(//
                    AnalyticsManager.Category.CALL_BUTTON_CLICKED,//
                    AnalyticsManager.Action.BOOKING_INITIALISE, Label.CANCEL, null);
            }
        });
    }

    protected void showChangedTimeDialog()
    {
        showChangedValueDialog(R.string.dialog_msg_changed_time, null);
    }

    protected void showChangedValueDialog(int messageResId, OnDismissListener onDismissListener)
    {
        if (onDismissListener == null)
        {
            onDismissListener = new OnDismissListener()
            {
                @Override
                public void onDismiss(DialogInterface dialog)
                {
                    setResult(CODE_RESULT_ACTIVITY_REFRESH);
                    finish();
                }
            };
        }

        showSimpleDialog(getString(R.string.dialog_notice2), getString(messageResId), getString(R.string.dialog_btn_text_confirm), null, null, null, null, onDismissListener, true);
    }

    protected void showChangedBonusDialog()
    {
        // 적립금이 변동된 경우.
        if (mFinalCheckDialog != null && mFinalCheckDialog.isShowing() == true)
        {
            mFinalCheckDialog.cancel();
            mFinalCheckDialog = null;
        }

        String title = getString(R.string.dialog_notice2);
        String msg = getString(R.string.dialog_msg_changed_bonus);
        String positive = getString(R.string.dialog_btn_text_confirm);

        showSimpleDialog(title, msg, positive, null, new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                lockUI();

                requestPlacePaymentInformation(mPaymentInformation, mCheckInSaleTime);
            }
        }, null, false);
    }

    protected void startCreditCardList()
    {
        Intent intent = new Intent(this, CreditCardListActivity.class);
        intent.setAction(Intent.ACTION_PICK);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_CREDITCARD, mSelectedCreditCard);

        startActivityForResult(intent, CODE_REQUEST_ACTIVITY_CREDITCARD_MANAGER);

        AnalyticsManager.getInstance(this).recordEvent(AnalyticsManager.Category.HOTEL_BOOKINGS//
            , Action.EDIT_BUTTON_CLICKED, Label.PAYMENT_CARD_EDIT, null);
    }

    protected void startInputMobileNumberDialog(String mobileNumber)
    {
        Intent intent = InputMobileNumberDialogActivity.newInstance(this, mobileNumber);
        startActivityForResult(intent, REQUEST_CODE_COUNTRYCODE_DIALOG_ACTIVITY);
    }

    private void requestLogin()
    {
        // 세션이 종료되어있으면 다시 로그인한다.
        DailyPreference.getInstance(this).removeUserInformation();

        Intent intent = LoginActivity.newInstance(this);
        startActivityForResult(intent, CODE_REQUEST_ACTIVITY_LOGIN);
    }

    protected void makeDialogMessages(ViewGroup viewGroup, int[] textResIds)
    {
        if (viewGroup == null || textResIds == null)
        {
            return;
        }

        int length = textResIds.length;

        for (int i = 0; i < length; i++)
        {
            View messageRow = LayoutInflater.from(this).inflate(R.layout.row_payment_agreedialog, viewGroup, false);

            TextView messageTextView = (TextView) messageRow.findViewById(R.id.messageTextView);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            if (i == length - 1)
            {
                layoutParams.setMargins(Util.dpToPx(this, 5), 0, 0, 0);
            } else
            {
                layoutParams.setMargins(Util.dpToPx(this, 5), 0, 0, Util.dpToPx(this, 10));
            }

            messageTextView.setLayoutParams(layoutParams);

            String message = getString(textResIds[i]);

            int startIndex = message.indexOf("<b>");

            if (startIndex >= 0)
            {
                message = message.replaceAll("<b>", "");

                int endIndex = message.indexOf("</b>");

                message = message.replaceAll("</b>", "");

                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(message);

                spannableStringBuilder.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.dh_theme_color)), //
                    startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableStringBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), //
                    startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                messageTextView.setText(spannableStringBuilder);
            } else
            {
                messageTextView.setText(message);
            }

            viewGroup.addView(messageRow);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Network Listener
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected retrofit2.Callback mUserCreditCardListCallback = new retrofit2.Callback<JSONObject>()
    {
        @Override
        public void onResponse(Call<JSONObject> call, Response<JSONObject> response)
        {
            if (response != null && response.isSuccessful() && response.body() != null)
            {
                try
                {
                    JSONObject responseJSONObject = response.body();

                    // TODO :  추후에 msgCode결과를 가지고 구분하는 코드가 필요할듯.
                    int msgCode = responseJSONObject.getInt("msg_code");

                    JSONArray dataJSONArray = responseJSONObject.getJSONArray("data");
                    int length = dataJSONArray.length();

                    if (length == 0)
                    {
                        mSelectedCreditCard = null;
                        setSimpleCardInformation(mPaymentInformation, null);
                    } else
                    {
                        if (mSelectedCreditCard == null)
                        {
                            JSONObject jsonObject = null;

                            String selectedSimpleCard = Crypto.urlDecrypt(DailyPreference.getInstance(PlacePaymentActivity.this).getSelectedSimpleCard());

                            if (Util.isTextEmpty(selectedSimpleCard) == true)
                            {
                                jsonObject = dataJSONArray.getJSONObject(0);
                            } else
                            {
                                for (int i = 0; i < length; i++)
                                {
                                    jsonObject = dataJSONArray.getJSONObject(i);

                                    if (selectedSimpleCard.equalsIgnoreCase(jsonObject.getString("billkey")) == true)
                                    {
                                        break;
                                    }
                                }
                            }

                            mSelectedCreditCard = new CreditCard(jsonObject.getString("card_name"), jsonObject.getString("print_cardno"), jsonObject.getString("billkey"), jsonObject.getString("cardcd"));
                        } else
                        {
                            boolean hasCreditCard = false;

                            for (int i = 0; i < length; i++)
                            {
                                JSONObject jsonObject = dataJSONArray.getJSONObject(i);

                                if (mSelectedCreditCard.billingkey.equals(jsonObject.getString("billkey")) == true)
                                {
                                    hasCreditCard = true;
                                    break;
                                }
                            }

                            // 기존에 선택한 카드를 지우고 돌아온 경우.
                            if (hasCreditCard == false)
                            {
                                JSONObject jsonObject = dataJSONArray.getJSONObject(0);

                                mSelectedCreditCard = new CreditCard(jsonObject.getString("card_name"), jsonObject.getString("print_cardno"), jsonObject.getString("billkey"), jsonObject.getString("cardcd"));
                            }
                        }

                        setSimpleCardInformation(mPaymentInformation, mSelectedCreditCard);
                    }

                    // 호텔 가격 정보가 변경되었습니다.
                    if (isChangedPrice() == true)
                    {
                        showChangedPriceDialog();
                        return;
                    }

                    if (hasWarningMessage() == true)
                    {
                        showWarningMessageDialog();
                    }

                    recordAnalyticsPayment(mPaymentInformation);
                } catch (Exception e)
                {
                    // 해당 화면 에러시에는 일반 결제가 가능해야 한다.
                    ExLog.e(e.toString());
                    setResult(CODE_RESULT_ACTIVITY_REFRESH);
                    finish();
                } finally
                {
                    unLockUI();
                }
            } else
            {
                PlacePaymentActivity.this.onErrorResponse(call, response);
            }
        }

        @Override
        public void onFailure(Call<JSONObject> call, Throwable t)
        {
            PlacePaymentActivity.this.onError(t);
        }
    };

    private retrofit2.Callback mPaymentAfterRegisterCreditCardCallback = new retrofit2.Callback<JSONObject>()
    {
        @Override
        public void onResponse(Call<JSONObject> call, Response<JSONObject> response)
        {
            if (response != null && response.isSuccessful() && response.body() != null)
            {
                try
                {
                    JSONObject responseJSONObject = response.body();

                    // TODO : 추후에 msgCode결과를 가지고 구분하는 코드가 필요할듯.
                    int msgCode = responseJSONObject.getInt("msg_code");

                    JSONArray dataJSONArray = responseJSONObject.getJSONArray("data");
                    int length = dataJSONArray.length();

                    if (length == 0)
                    {
                        mSelectedCreditCard = null;
                        setSimpleCardInformation(mPaymentInformation, null);
                    } else
                    {
                        JSONObject jsonObject = dataJSONArray.getJSONObject(0);

                        mSelectedCreditCard = new CreditCard(jsonObject.getString("card_name"), jsonObject.getString("print_cardno"), jsonObject.getString("billkey"), jsonObject.getString("cardcd"));
                        setSimpleCardInformation(mPaymentInformation, mSelectedCreditCard);

                        // final check 결제 화면을 보여준다.
                        showAgreeTermDialog(PlacePaymentInformation.PaymentType.EASY_CARD);
                    }
                } catch (Exception e)
                {
                    // 해당 화면 에러시에는 일반 결제가 가능해야 한다.
                    ExLog.e(e.toString());
                } finally
                {
                    unLockUI();
                }
            } else
            {
                PlacePaymentActivity.this.onErrorResponse(call, response);
            }
        }

        @Override
        public void onFailure(Call<JSONObject> call, Throwable t)
        {
            PlacePaymentActivity.this.onError(t);
        }
    };
}
