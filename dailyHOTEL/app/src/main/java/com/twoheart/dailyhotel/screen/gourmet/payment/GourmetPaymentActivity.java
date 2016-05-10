package com.twoheart.dailyhotel.screen.gourmet.payment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.CreditCard;
import com.twoheart.dailyhotel.model.Customer;
import com.twoheart.dailyhotel.model.GourmetPaymentInformation;
import com.twoheart.dailyhotel.model.Guest;
import com.twoheart.dailyhotel.model.PlacePaymentInformation;
import com.twoheart.dailyhotel.model.SaleTime;
import com.twoheart.dailyhotel.model.TicketInformation;
import com.twoheart.dailyhotel.network.DailyNetworkAPI;
import com.twoheart.dailyhotel.network.response.DailyHotelJsonResponseListener;
import com.twoheart.dailyhotel.place.activity.PlacePaymentActivity;
import com.twoheart.dailyhotel.screen.common.FinalCheckLayout;
import com.twoheart.dailyhotel.screen.information.creditcard.CreditCardListActivity;
import com.twoheart.dailyhotel.screen.information.member.InputMobileNumberDialogActivity;
import com.twoheart.dailyhotel.util.DailyCalendar;
import com.twoheart.dailyhotel.util.DailyPreference;
import com.twoheart.dailyhotel.util.EdgeEffectColor;
import com.twoheart.dailyhotel.util.ExLog;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;
import com.twoheart.dailyhotel.widget.DailySignatureView;
import com.twoheart.dailyhotel.widget.DailyToast;
import com.twoheart.dailyhotel.widget.DailyToolbarLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

@SuppressLint({"NewApi", "ResourceAsColor"})
public class GourmetPaymentActivity extends PlacePaymentActivity
{
    private GourmetPaymentLayout mGourmetPaymentLayout;
    //
    private boolean mIsChangedTime;
    private boolean mIsChangedPrice; // 가격이 변경된 경우.
    private String mPlaceImageUrl;
    private boolean mIsEditMode;

    public interface OnUserActionListener
    {
        void selectTicketTime(String selectedTime);

        void plusTicketCount();

        void minusTicketCount();

        void editUserInformation();

        void showCreditCardManager();

        void changedPaymentType(PlacePaymentInformation.PaymentType type);

        void doPayment();

        void showInputMobileNumberDialog(String mobileNumber);
    }

    public static Intent newInstance(Context context, TicketInformation ticketInformation, SaleTime checkInSaleTime//
        , String imageUrl, String category, int gourmetIndex, boolean isDBenefit)
    {
        Intent intent = new Intent(context, GourmetPaymentActivity.class);

        intent.putExtra(NAME_INTENT_EXTRA_DATA_TICKETINFORMATION, ticketInformation);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_SALETIME, checkInSaleTime);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_URL, imageUrl);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_GOURMETIDX, gourmetIndex);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_CATEGORY, category);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_DBENEFIT, isDBenefit);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_booking_place);

        Intent intent = getIntent();

        if (intent == null)
        {
            finish();
            return;
        }

        mPaymentInformation = new GourmetPaymentInformation();
        GourmetPaymentInformation gourmetPaymentInformation = (GourmetPaymentInformation) mPaymentInformation;

        gourmetPaymentInformation.setTicketInformation((TicketInformation) intent.getParcelableExtra(NAME_INTENT_EXTRA_DATA_TICKETINFORMATION));
        mCheckInSaleTime = intent.getParcelableExtra(NAME_INTENT_EXTRA_DATA_SALETIME);
        mPlaceImageUrl = intent.getStringExtra(NAME_INTENT_EXTRA_DATA_URL);
        gourmetPaymentInformation.placeIndex = intent.getIntExtra(NAME_INTENT_EXTRA_DATA_GOURMETIDX, -1);
        gourmetPaymentInformation.category = intent.getStringExtra(NAME_INTENT_EXTRA_DATA_CATEGORY);
        gourmetPaymentInformation.isDBenefit = intent.getBooleanExtra(NAME_INTENT_EXTRA_DATA_DBENEFIT, false);

        if (gourmetPaymentInformation.getTicketInformation() == null)
        {
            finish();
            return;
        }

        mIsChangedPrice = false;
        mIsChangedTime = false;

        initToolbar(gourmetPaymentInformation.getTicketInformation().placeName);
        initLayout();
    }

    private void initToolbar(String title)
    {
        View toolbar = findViewById(R.id.toolbar);
        DailyToolbarLayout dailyToolbarLayout = new DailyToolbarLayout(this, toolbar);
        dailyToolbarLayout.initToolbar(title, new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        dailyToolbarLayout.setToolbarMenu(R.drawable.navibar_ic_call, -1);
        dailyToolbarLayout.setToolbarMenuClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (lockUiComponentAndIsLockUiComponent() == true)
                {
                    return;
                }

                showCallDialog();
            }
        });
    }

    private void initLayout()
    {
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollLayout);
        EdgeEffectColor.setEdgeGlowColor(scrollView, getResources().getColor(R.color.over_scroll_edge));

        mGourmetPaymentLayout = new GourmetPaymentLayout(this, scrollView, mOnUserActionListener);
    }

    @Override
    protected void requestUserInformationForPayment()
    {
        DailyNetworkAPI.getInstance(this).requestUserInformationForPayment(mNetworkTag, mUserInformationJsonResponseListener, this);
    }

    @Override
    protected void requestEasyPayment(PlacePaymentInformation paymentInformation, SaleTime checkInSaleTime)
    {
        if (paymentInformation == null || checkInSaleTime == null)
        {
            return;
        }

        lockUI();

        GourmetPaymentInformation gourmetPaymentInformation = (GourmetPaymentInformation) paymentInformation;
        Guest guest;

        if (mIsEditMode == true)
        {
            guest = mGourmetPaymentLayout.getGuest();
        } else
        {
            guest = gourmetPaymentInformation.getGuest();
        }

        TicketInformation ticketInformation = gourmetPaymentInformation.getTicketInformation();

        Map<String, String> params = new HashMap<>();
        params.put("sale_reco_idx", String.valueOf(ticketInformation.index));
        params.put("billkey", mSelectedCreditCard.billingkey);
        params.put("ticket_count", String.valueOf(gourmetPaymentInformation.ticketCount));
        params.put("customer_name", guest.name);
        params.put("customer_phone", guest.phone.replace("-", ""));
        params.put("customer_email", guest.email);
        params.put("arrival_time", String.valueOf(gourmetPaymentInformation.ticketTime));

        //        if (DEBUG == true)
        //        {
        //            showSimpleDialog(null, params.toString(), getString(R.string.dialog_btn_text_confirm), null);
        //        }

        DailyNetworkAPI.getInstance(this).requestGourmetPayment(mNetworkTag, params, mPaymentEasyCreditCardJsonResponseListener, this);
    }

    @Override
    protected void requestPlacePaymentInfomation(PlacePaymentInformation paymentInformation, SaleTime checkInSaleTime)
    {
        DailyNetworkAPI.getInstance(this).requestGourmetPaymentInformation(mNetworkTag, //
            ((GourmetPaymentInformation) paymentInformation).getTicketInformation().index, //
            mGourmetPaymentInformationJsonResponseListener, this);
    }

    @Override
    protected void updatePaymentInformation(PlacePaymentInformation paymentInformation, CreditCard selectedCreditCard)
    {
        mGourmetPaymentLayout.updatePaymentInformation((GourmetPaymentInformation) paymentInformation, selectedCreditCard);
    }

    @Override
    protected void updateGuestInformation(String phoneNumber)
    {
        mPaymentInformation.getGuest().phone = phoneNumber;
        mGourmetPaymentLayout.updateUserInformationLayout(phoneNumber);
    }

    @Override
    protected void changedPaymentType(PlacePaymentInformation.PaymentType paymentType, CreditCard creditCard)
    {
        mSelectedCreditCard = creditCard;
        mOnUserActionListener.changedPaymentType(paymentType);
    }

    @Override
    protected boolean isChangedPrice()
    {
        return mIsChangedPrice;
    }

    @Override
    protected boolean hasWarningMessage()
    {
        return false;
    }

    @Override
    protected void showWarningMessageDialog()
    {

    }

    @Override
    protected void checkChangedBonusSwitch()
    {

    }

    @Override
    protected void showPaymentWeb(PlacePaymentInformation paymentInformation, SaleTime checkInSaleTime)
    {
        Intent intent = new Intent(this, GourmetPaymentWebActivity.class);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_PAYMENTINFORMATION, paymentInformation);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_SALETIME, checkInSaleTime);

        startActivityForResult(intent, CODE_REQUEST_ACTIVITY_PAYMENT);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);
    }

    @Override
    protected void showPaymentThankyou(PlacePaymentInformation paymentInformation, String imageUrl)
    {
        GourmetPaymentInformation gourmetPaymentInformation = (GourmetPaymentInformation) mPaymentInformation;
        TicketInformation ticketInformation = gourmetPaymentInformation.getTicketInformation();

        String placyType = String.format("%s X %d", ticketInformation.name, gourmetPaymentInformation.ticketCount);

        Calendar calendarTime = DailyCalendar.getInstance();
        calendarTime.setTimeZone(TimeZone.getTimeZone("GMT"));

        SimpleDateFormat formatDay = new SimpleDateFormat("HH시 mm분", Locale.KOREA);
        formatDay.setTimeZone(TimeZone.getTimeZone("GMT"));

        calendarTime.setTimeInMillis(gourmetPaymentInformation.ticketTime);
        String time = formatDay.format(calendarTime.getTime());
        String date = String.format("%s %s", gourmetPaymentInformation.checkInTime, time);

        Intent intent = GourmetPaymentThankyouActivity.newInstance(this, imageUrl, ticketInformation.placeName, placyType, date);

        startActivityForResult(intent, REQUEST_CODE_PAYMETRESULT_ACTIVITY);
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
    }

    @Override
    protected Dialog getEasyPaymentConfirmDialog()
    {
        final Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        Window window = dialog.getWindow();
        layoutParams.copyFrom(window.getAttributes());

        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(layoutParams);

        int[] messageResIds = {R.string.dialog_msg_gourmet_payment_message01//
            , R.string.dialog_msg_gourmet_payment_message02//
            , R.string.dialog_msg_gourmet_payment_message03//
            , R.string.dialog_msg_gourmet_payment_message08, R.string.dialog_msg_gourmet_payment_message07};

        final FinalCheckLayout finalCheckLayout = new FinalCheckLayout(this, messageResIds);
        final TextView agreeSinatureTextView = (TextView) finalCheckLayout.findViewById(R.id.agreeSinatureTextView);
        final View agreeLayout = finalCheckLayout.findViewById(R.id.agreeLayout);

        agreeLayout.setEnabled(false);

        finalCheckLayout.setOnUserActionListener(new DailySignatureView.OnUserActionListener()
        {
            @Override
            public void onConfirmSignature()
            {
                AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
                animation.setDuration(500);
                animation.setFillBefore(true);
                animation.setFillAfter(true);

                animation.setAnimationListener(new Animation.AnimationListener()
                {
                    @Override
                    public void onAnimationStart(Animation animation)
                    {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation)
                    {
                        agreeSinatureTextView.setAnimation(null);
                        agreeSinatureTextView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation)
                    {
                    }
                });

                agreeSinatureTextView.startAnimation(animation);

                TransitionDrawable transition = (TransitionDrawable) agreeLayout.getBackground();
                transition.startTransition(500);

                agreeLayout.setEnabled(true);
                agreeLayout.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        synchronized (GourmetPaymentActivity.this)
                        {
                            if (isLockUiComponent() == true)
                            {
                                return;
                            }

                            dialog.dismiss();

                            lockUI();

                            // 1. 세션이 살아있는지 검사 시작.
                            DailyNetworkAPI.getInstance(GourmetPaymentActivity.this).requestUserInformationForPayment(mNetworkTag, mUserInformationFinalCheckJsonResponseListener, GourmetPaymentActivity.this);

                            AnalyticsManager.getInstance(GourmetPaymentActivity.this).recordEvent(AnalyticsManager.Category.POPUP_BOXES//
                                , AnalyticsManager.Action.PAYMENT_AGREEMENT_POPPEDUP, AnalyticsManager.Label.AGREE, null);
                        }
                    }
                });
            }
        });

        dialog.setContentView(finalCheckLayout);

        return dialog;
    }

    @Override
    protected Dialog getPaymentConfirmDialog(PlacePaymentInformation.PaymentType paymentType)
    {
        final Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);

        View view = LayoutInflater.from(this).inflate(R.layout.fragment_dialog_confirm_payment, null);
        ViewGroup messageLayout = (ViewGroup) view.findViewById(R.id.messageLayout);

        int[] textResIds;

        switch (paymentType)
        {
            // 신용카드 일반 결제
            case CARD:
                textResIds = new int[]{R.string.dialog_msg_gourmet_payment_message01//
                    , R.string.dialog_msg_gourmet_payment_message02//
                    , R.string.dialog_msg_gourmet_payment_message03//
                    , R.string.dialog_msg_gourmet_payment_message06};
                break;

            // 핸드폰 결제
            case PHONE_PAY:
                textResIds = new int[]{R.string.dialog_msg_gourmet_payment_message01//
                    , R.string.dialog_msg_gourmet_payment_message02//
                    , R.string.dialog_msg_gourmet_payment_message03//
                    , R.string.dialog_msg_gourmet_payment_message04//
                    , R.string.dialog_msg_gourmet_payment_message06};
                break;

            // 계좌 이체
            case VBANK:
                textResIds = new int[]{R.string.dialog_msg_gourmet_payment_message01//
                    , R.string.dialog_msg_gourmet_payment_message02//
                    , R.string.dialog_msg_gourmet_payment_message03//
                    , R.string.dialog_msg_gourmet_payment_message05//
                    , R.string.dialog_msg_gourmet_payment_message06};
                break;

            default:
                return null;
        }

        int length = textResIds.length;

        for (int i = 0; i < length; i++)
        {
            View messageRow = LayoutInflater.from(this).inflate(R.layout.row_payment_agreedialog, messageLayout, false);

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

            messageLayout.addView(messageRow);
        }

        View agreeLayout = view.findViewById(R.id.agreeLayout);

        View.OnClickListener buttonOnClickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();

                synchronized (GourmetPaymentActivity.this)
                {
                    if (isLockUiComponent() == true)
                    {
                        return;
                    }

                    lockUI();

                    // 1. 세션이 살아있는지 검사 시작.
                    DailyNetworkAPI.getInstance(GourmetPaymentActivity.this).requestUserInformationForPayment(mNetworkTag, mUserInformationFinalCheckJsonResponseListener, GourmetPaymentActivity.this);

                    AnalyticsManager.getInstance(GourmetPaymentActivity.this).recordEvent(AnalyticsManager.Category.POPUP_BOXES//
                        , AnalyticsManager.Action.PAYMENT_AGREEMENT_POPPEDUP, AnalyticsManager.Label.AGREE, null);
                }
            }
        };

        agreeLayout.setOnClickListener(buttonOnClickListener);

        dialog.setContentView(view);

        return dialog;
    }

    @Override
    protected void onActivityPaymentResult(int requestCode, int resultCode, Intent intent)
    {
        String title = getString(R.string.dialog_title_payment);
        String msg;
        String posTitle = getString(R.string.dialog_btn_text_confirm);
        View.OnClickListener posListener = null;

        switch (resultCode)
        {
            // 결제가 성공한 경우 GA와 믹스패널에 등록
            case CODE_RESULT_ACTIVITY_PAYMENT_COMPLETE:
            case CODE_RESULT_ACTIVITY_PAYMENT_SUCCESS:
                // 가상계좌완료후에는 예약화면의 가상계좌 화면까지 이동한다.
                if (mPaymentInformation.paymentType == PlacePaymentInformation.PaymentType.VBANK)
                {
                    onActivityPaymentResult(requestCode, CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_READY, intent);
                } else
                {
                    recordAnalyticsPaymentComplete((GourmetPaymentInformation) mPaymentInformation);

                    showPaymentThankyou(mPaymentInformation, mPlaceImageUrl);
                }
                return;

            case CODE_RESULT_ACTIVITY_PAYMENT_SOLD_OUT:
                msg = getString(R.string.act_toast_payment_soldout);
                break;

            case CODE_RESULT_ACTIVITY_PAYMENT_NOT_AVAILABLE:
                title = getString(R.string.dialog_notice2);
                msg = getString(R.string.act_toast_payment_not_available);
                break;

            case CODE_RESULT_ACTIVITY_PAYMENT_NETWORK_ERROR:
                msg = getString(R.string.act_toast_payment_network_error);
                break;

            case CODE_RESULT_ACTIVITY_PAYMENT_INVALID_SESSION:
                restartExpiredSession();
                return;

            case CODE_RESULT_ACTIVITY_PAYMENT_INVALID_DATE:
                msg = getString(R.string.act_toast_payment_invalid_date);
                break;

            case CODE_RESULT_ACTIVITY_PAYMENT_FAIL:
                if (intent != null && intent.hasExtra(NAME_INTENT_EXTRA_DATA_RESULT) == true)
                {
                    msg = intent.getStringExtra(NAME_INTENT_EXTRA_DATA_RESULT);
                } else
                {
                    msg = getString(R.string.act_toast_payment_fail);
                }
                break;

            case CODE_RESULT_ACTIVITY_PAYMENT_CANCELED:
                msg = getString(R.string.act_toast_payment_canceled);

                posListener = new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                    }
                };
                break;

            case CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_READY:
                /**
                 * 가상계좌선택시 해당 가상계좌 정보를 보기위해 화면 스택을 쌓으면서 들어가야함. 이를 위한 정보를 셋팅.
                 * 예약 리스트 프래그먼트에서 찾아 들어가기 위해서 필요함. 들어간 후에는 다시 프리퍼런스를 초기화해줌.
                 * 플로우) 예약 액티비티 => 호텔탭 액티비티 => 메인액티비티 => 예약 리스트 프래그먼트 => 예약
                 * 리스트 갱신 후 최상단 아이템 인텐트
                 */
                DailyPreference.getInstance(this).setVirtuaAccountGourmetInformation((GourmetPaymentInformation) mPaymentInformation, mCheckInSaleTime);
                DailyPreference.getInstance(this).setVirtualAccountReadyFlag(CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_READY);

                if (intent != null && intent.hasExtra(NAME_INTENT_EXTRA_DATA_RESULT) == true)
                {
                    msg = intent.getStringExtra(NAME_INTENT_EXTRA_DATA_RESULT);
                } else
                {
                    msg = getString(R.string.dialog_msg_issuing_account);
                }

                posListener = new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        setResult(CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_READY);
                        finish();
                    }
                };
                break;

            case CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_TIME_ERROR:
                msg = getString(R.string.act_toast_payment_account_time_error);
                break;

            case CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_DUPLICATE:
                msg = getString(R.string.act_toast_payment_account_duplicate);
                break;

            case CODE_RESULT_ACTIVITY_PAYMENT_TIMEOVER:
                msg = getString(R.string.act_toast_payment_account_timeover);
                break;

            case CODE_RESULT_ACTIVITY_PAYMENT_UNKNOW_ERROR:
                if (intent != null && intent.hasExtra(NAME_INTENT_EXTRA_DATA_MESSAGE) == true)
                {
                    msg = intent.getStringExtra(NAME_INTENT_EXTRA_DATA_MESSAGE);
                } else
                {
                    msg = getString(R.string.act_toast_payment_fail);
                }
                break;

            case CODE_RESULT_ACTIVITY_PAYMENT_CANCEL:
            {
                if (intent.hasExtra(NAME_INTENT_EXTRA_DATA_RESULT) == true)
                {
                    msg = intent.getStringExtra(NAME_INTENT_EXTRA_DATA_RESULT);
                } else
                {
                    msg = getString(R.string.act_toast_payment_fail);
                }

                posListener = new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                    }
                };
                break;
            }

            default:
                return;
        }

        if (posListener == null)
        {
            posListener = new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    finish();
                }
            };
        }

        showSimpleDialog(title, msg, posTitle, null, posListener, null, false);
    }

    @Override
    protected void recordAnalyticsAgreeTermDialog(PlacePaymentInformation paymentInformation)
    {
        AnalyticsManager.getInstance(this).recordScreen(AnalyticsManager.Screen.DAILYGOURMET_PAYMENT_AGREEMENT_POPUP//
            , getMapPaymentInformation((GourmetPaymentInformation) paymentInformation));
    }

    private void requestValidateTicketPayment(GourmetPaymentInformation gourmetPaymentInformation, SaleTime checkInSaleTime)
    {
        if (gourmetPaymentInformation == null || checkInSaleTime == null)
        {
            Util.restartApp(this);
            return;
        }

        DailyNetworkAPI.getInstance(this).requestGourmetCheckTicket(mNetworkTag//
            , gourmetPaymentInformation.getTicketInformation().index//
            , checkInSaleTime.getDayOfDaysDateFormat("yyMMdd")//
            , gourmetPaymentInformation.ticketCount//
            , Long.toString(gourmetPaymentInformation.ticketTime), mCheckAvailableTicketJsonResponseListener, this);
    }

    private void recordAnalyticsPaymentComplete(GourmetPaymentInformation gourmetPaymentInformation)
    {
        try
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss", Locale.KOREA);
            Date date = new Date();
            String strDate = dateFormat.format(date);
            String userIndex = gourmetPaymentInformation.getCustomer().getUserIdx();
            String transId = strDate + '_' + userIndex;

            Map<String, String> params = getMapPaymentInformation(gourmetPaymentInformation);

            AnalyticsManager.getInstance(getApplicationContext()).purchaseCompleteGourmet(transId, params);
            AnalyticsManager.getInstance(getApplicationContext()).recordScreen(AnalyticsManager.Screen.DAILYGOURMET_PAYMENT_COMPLETE, null);
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    private void recordAnalyticsPayment(GourmetPaymentInformation gourmetPaymentInformation)
    {
        if (gourmetPaymentInformation == null)
        {
            return;
        }

        try
        {
            Map<String, String> params = new HashMap<>();
            params.put(AnalyticsManager.KeyType.NAME, gourmetPaymentInformation.getTicketInformation().placeName);
            params.put(AnalyticsManager.KeyType.PRICE, Integer.toString(gourmetPaymentInformation.getTicketInformation().discountPrice));
            params.put(AnalyticsManager.KeyType.PLACE_INDEX, Integer.toString(gourmetPaymentInformation.placeIndex));
            params.put(AnalyticsManager.KeyType.DATE, mCheckInSaleTime.getDayOfDaysDateFormat("yyyy-MM-dd"));
            params.put(AnalyticsManager.KeyType.TICKET_NAME, gourmetPaymentInformation.getTicketInformation().name);
            params.put(AnalyticsManager.KeyType.TICKET_INDEX, Integer.toString(gourmetPaymentInformation.getTicketInformation().index));
            params.put(AnalyticsManager.KeyType.CATEGORY, gourmetPaymentInformation.category);
            params.put(AnalyticsManager.KeyType.DBENEFIT, gourmetPaymentInformation.isDBenefit ? "yes" : "no");

            AnalyticsManager.getInstance(GourmetPaymentActivity.this).recordScreen(AnalyticsManager.Screen.DAILYGOURMET_PAYMENT, params);
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }
    }

    private Map<String, String> getMapPaymentInformation(GourmetPaymentInformation gourmetPaymentInformation)
    {
        Map<String, String> params = new HashMap<>();

        try
        {
            TicketInformation ticketInformation = gourmetPaymentInformation.getTicketInformation();

            params.put(AnalyticsManager.KeyType.NAME, ticketInformation.placeName);
            params.put(AnalyticsManager.KeyType.PLACE_INDEX, Integer.toString(gourmetPaymentInformation.placeIndex));
            params.put(AnalyticsManager.KeyType.PRICE, Integer.toString(ticketInformation.discountPrice));
            params.put(AnalyticsManager.KeyType.QUANTITY, Integer.toString(gourmetPaymentInformation.ticketCount));
            params.put(AnalyticsManager.KeyType.TOTAL_PRICE, Integer.toString(ticketInformation.discountPrice * gourmetPaymentInformation.ticketCount));
            params.put(AnalyticsManager.KeyType.PLACE_INDEX, Integer.toString(gourmetPaymentInformation.placeIndex));
            params.put(AnalyticsManager.KeyType.TICKET_NAME, ticketInformation.name);
            params.put(AnalyticsManager.KeyType.TICKET_INDEX, Integer.toString(ticketInformation.index));
            params.put(AnalyticsManager.KeyType.DATE, mCheckInSaleTime.getDayOfDaysDateFormat("yyyy-MM-dd"));
            params.put(AnalyticsManager.KeyType.PAYMENT_PRICE, Integer.toString(ticketInformation.discountPrice * gourmetPaymentInformation.ticketCount));
            params.put(AnalyticsManager.KeyType.USED_BOUNS, "0");
            params.put(AnalyticsManager.KeyType.CATEGORY, gourmetPaymentInformation.category);
            params.put(AnalyticsManager.KeyType.DBENEFIT, gourmetPaymentInformation.isDBenefit ? "yes" : "no");
            params.put(AnalyticsManager.KeyType.PAYMENT_TYPE, gourmetPaymentInformation.paymentType.getName());

            Calendar calendarTime = DailyCalendar.getInstance();
            calendarTime.setTimeZone(TimeZone.getTimeZone("GMT"));

            SimpleDateFormat formatDay = new SimpleDateFormat("HH:mm", Locale.KOREA);
            formatDay.setTimeZone(TimeZone.getTimeZone("GMT"));

            params.put(AnalyticsManager.KeyType.RESERVATION_TIME, formatDay.format(gourmetPaymentInformation.ticketTime));
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }

        return params;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // User ActionListener
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private OnUserActionListener mOnUserActionListener = new OnUserActionListener()
    {
        @Override
        public void selectTicketTime(String selectedTime)
        {
            if (lockUiComponentAndIsLockUiComponent() == true)
            {
                return;
            }

            final GourmetPaymentInformation gourmetPaymentInformation = (GourmetPaymentInformation) mPaymentInformation;

            Dialog dialog = Util.showDatePickerDialog(GourmetPaymentActivity.this//
                , getString(R.string.label_booking_select_ticket_time)//
                , gourmetPaymentInformation.getTicketTimes(), selectedTime, getString(R.string.dialog_btn_text_confirm), new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        int select = (Integer) v.getTag();

                        try
                        {
                            gourmetPaymentInformation.ticketTime = gourmetPaymentInformation.ticketTimes[select];
                            mGourmetPaymentLayout.setTicketTime(gourmetPaymentInformation.ticketTime);
                        } catch (Exception e)
                        {
                            ExLog.d(e.toString());

                            onError(e);
                        }
                    }
                });

            if (dialog != null)
            {
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
                {
                    @Override
                    public void onDismiss(DialogInterface dialog)
                    {
                        releaseUiComponent();
                    }
                });
            }
        }

        @Override
        public void plusTicketCount()
        {
            GourmetPaymentInformation gourmetPaymentInformation = (GourmetPaymentInformation) mPaymentInformation;

            int count = gourmetPaymentInformation.ticketCount;
            int maxCount = gourmetPaymentInformation.ticketMaxCount;

            if (count >= maxCount)
            {
                mGourmetPaymentLayout.setTicketCountPlusButtonEnabled(false);
                DailyToast.showToast(GourmetPaymentActivity.this, getString(R.string.toast_msg_maxcount_ticket, maxCount), Toast.LENGTH_LONG);
            } else
            {
                gourmetPaymentInformation.ticketCount = count + 1;
                mGourmetPaymentLayout.setTicketCount(gourmetPaymentInformation.ticketCount);
                mGourmetPaymentLayout.setTicketCountMinusButtonEnabled(true);

                // 결제 가격을 바꾸어야 한다.
                mGourmetPaymentLayout.updatePaymentInformationLayout(GourmetPaymentActivity.this, gourmetPaymentInformation);
            }
        }

        @Override
        public void minusTicketCount()
        {
            GourmetPaymentInformation gourmetPaymentInformation = (GourmetPaymentInformation) mPaymentInformation;

            int count = gourmetPaymentInformation.ticketCount;

            if (count <= 1)
            {
                mGourmetPaymentLayout.setTicketCountMinusButtonEnabled(false);
            } else
            {
                gourmetPaymentInformation.ticketCount = count - 1;
                mGourmetPaymentLayout.setTicketCount(gourmetPaymentInformation.ticketCount);
                mGourmetPaymentLayout.setTicketCountPlusButtonEnabled(true);

                // 결제 가격을 바꾸어야 한다.
                mGourmetPaymentLayout.updatePaymentInformationLayout(GourmetPaymentActivity.this, gourmetPaymentInformation);
            }
        }

        @Override
        public void editUserInformation()
        {
            if (mIsEditMode == true)
            {
                return;
            }

            mIsEditMode = true;
            mGourmetPaymentLayout.enabledEditUserInformation();
        }

        @Override
        public void showCreditCardManager()
        {
            if (lockUiComponentAndIsLockUiComponent() == true)
            {
                return;
            }

            if (mIsEditMode == true)
            {
                // 현재 수정 사항을 기억한다.
                Guest editGuest = mGourmetPaymentLayout.getGuest();
                mPaymentInformation.setGuest(editGuest);
            }

            AnalyticsManager.getInstance(GourmetPaymentActivity.this).recordEvent(AnalyticsManager.Category.GOURMET_BOOKINGS//
                , AnalyticsManager.Action.EDIT_BUTTON_CLICKED, AnalyticsManager.Label.PAYMENT_CARD_EDIT, null);

            Intent intent = new Intent(GourmetPaymentActivity.this, CreditCardListActivity.class);
            intent.setAction(Intent.ACTION_PICK);
            intent.putExtra(NAME_INTENT_EXTRA_DATA_CREDITCARD, mSelectedCreditCard);

            startActivityForResult(intent, CODE_REQUEST_ACTIVITY_CREDITCARD_MANAGER);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);
        }

        @Override
        public void changedPaymentType(PlacePaymentInformation.PaymentType paymentType)
        {
            mPaymentInformation.paymentType = paymentType;
            mGourmetPaymentLayout.checkPaymentType(paymentType);
        }

        @Override
        public void doPayment()
        {
            if (lockUiComponentAndIsLockUiComponent() == true)
            {
                return;
            }

            GourmetPaymentInformation gourmetPaymentInformation = (GourmetPaymentInformation) mPaymentInformation;

            if (gourmetPaymentInformation.ticketTime == 0)
            {
                releaseUiComponent();
                mGourmetPaymentLayout.scrollTop();

                DailyToast.showToast(GourmetPaymentActivity.this, R.string.toast_msg_please_select_reservationtime, Toast.LENGTH_SHORT);
                return;
            }

            // 수정 모드인 경우 데이터를 다시 받아와야 한다.
            if (mIsEditMode == true)
            {
                Guest guest = mGourmetPaymentLayout.getGuest();

                if (Util.isTextEmpty(guest.name) == true)
                {
                    releaseUiComponent();

                    mGourmetPaymentLayout.requestUserInformationFocus(GourmetPaymentLayout.UserInformationType.NAME);

                    DailyToast.showToast(GourmetPaymentActivity.this, R.string.message_gourmet_please_input_guest, Toast.LENGTH_SHORT);
                    return;
                } else if (Util.isTextEmpty(guest.phone) == true)
                {
                    releaseUiComponent();

                    mGourmetPaymentLayout.requestUserInformationFocus(GourmetPaymentLayout.UserInformationType.PHONE);

                    DailyToast.showToast(GourmetPaymentActivity.this, R.string.toast_msg_please_input_contact, Toast.LENGTH_SHORT);
                    return;
                } else if (Util.isTextEmpty(guest.email) == true)
                {
                    releaseUiComponent();

                    mGourmetPaymentLayout.requestUserInformationFocus(GourmetPaymentLayout.UserInformationType.EMAIL);

                    DailyToast.showToast(GourmetPaymentActivity.this, R.string.toast_msg_please_input_email, Toast.LENGTH_SHORT);
                    return;
                } else if (android.util.Patterns.EMAIL_ADDRESS.matcher(guest.email).matches() == false)
                {
                    releaseUiComponent();

                    mGourmetPaymentLayout.requestUserInformationFocus(GourmetPaymentLayout.UserInformationType.EMAIL);

                    DailyToast.showToast(GourmetPaymentActivity.this, R.string.toast_msg_wrong_email_address, Toast.LENGTH_SHORT);
                    return;
                }

                gourmetPaymentInformation.setGuest(guest);
            }

            if (gourmetPaymentInformation.paymentType == PlacePaymentInformation.PaymentType.VBANK && DailyPreference.getInstance(GourmetPaymentActivity.this).getNotificationUid() < 0)
            {
                // 가상계좌 결제시 푸쉬를 받지 못하는 경우
                String title = getString(R.string.dialog_notice2);
                String positive = getString(R.string.dialog_btn_text_confirm);
                String msg = getString(R.string.dialog_msg_none_gcmid);

                showSimpleDialog(title, msg, positive, new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        processAgreeTermDialog();
                    }
                }, new DialogInterface.OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                        unLockUI();
                    }
                });
            } else
            {
                processAgreeTermDialog();
            }

            String label = String.format("%s-%s", gourmetPaymentInformation.getTicketInformation().placeName, gourmetPaymentInformation.getTicketInformation().name);
            AnalyticsManager.getInstance(GourmetPaymentActivity.this).recordEvent(AnalyticsManager.Category.GOURMET_BOOKINGS//
                , AnalyticsManager.Action.PAYMENT_CLICKED, label, null);
        }

        @Override
        public void showInputMobileNumberDialog(String mobileNumber)
        {
            if (isFinishing() == true)
            {
                return;
            }

            mPaymentInformation.setGuest(mGourmetPaymentLayout.getGuest());

            Intent intent = InputMobileNumberDialogActivity.newInstance(GourmetPaymentActivity.this, mobileNumber);
            startActivityForResult(intent, REQUEST_CODE_COUNTRYCODE_DIALOG_ACTIVITY);
        }
    };

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Network Listener
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected DailyHotelJsonResponseListener mUserInformationJsonResponseListener = new DailyHotelJsonResponseListener()
    {
        @Override
        public void onErrorResponse(VolleyError volleyError)
        {

        }

        @Override
        public void onResponse(String url, JSONObject response)
        {
            try
            {
                int msg_code = response.getInt("msg_code");

                if (msg_code != 0)
                {
                    if (response.has("msg") == true)
                    {
                        String msg = response.getString("msg");

                        DailyToast.showToast(GourmetPaymentActivity.this, msg, Toast.LENGTH_SHORT);
                        finish();
                        return;
                    } else
                    {
                        throw new NullPointerException("response == null");
                    }
                }

                JSONObject jsonData = response.getJSONObject("data");

                String name = jsonData.getString("user_name");
                String phone = jsonData.getString("user_phone");
                String email = jsonData.getString("user_email");
                String userIndex = jsonData.getString("user_idx");
                int bonus = jsonData.getInt("user_bonus");

                if (bonus < 0)
                {
                    bonus = 0;
                }

                GourmetPaymentInformation gourmetPaymentInformation = (GourmetPaymentInformation) mPaymentInformation;
                gourmetPaymentInformation.bonus = bonus;

                Customer buyer = new Customer();
                buyer.setEmail(email);
                buyer.setName(name);
                buyer.setPhone(phone);
                buyer.setUserIdx(userIndex);

                gourmetPaymentInformation.setCustomer(buyer);

                if (mIsEditMode == false)
                {
                    Guest guest = new Guest();
                    guest.name = name;
                    guest.phone = phone;
                    guest.email = email;

                    gourmetPaymentInformation.setGuest(guest);
                }

                // 2. 화면 정보 얻기
                DailyNetworkAPI.getInstance(GourmetPaymentActivity.this).requestGourmetPaymentInformation(mNetworkTag//
                    , gourmetPaymentInformation.getTicketInformation().index//
                    , mGourmetPaymentInformationJsonResponseListener, GourmetPaymentActivity.this);
            } catch (Exception e)
            {
                onError(e);
            }
        }
    };

    private DailyHotelJsonResponseListener mGourmetPaymentInformationJsonResponseListener = new DailyHotelJsonResponseListener()
    {
        @Override
        public void onErrorResponse(VolleyError volleyError)
        {

        }

        @Override
        public void onResponse(String url, JSONObject response)
        {
            try
            {
                GourmetPaymentInformation gourmetPaymentInformation = (GourmetPaymentInformation) mPaymentInformation;

                int msgCode = response.getInt("msg_code");

                if (msgCode == 0)
                {
                    JSONObject jsonObject = response.getJSONObject("data");

                    //					jsonObject.getInt("fnb_sale_reco_idx");
                    //					jsonObject.getInt("is_sale_time_over");
                    //					jsonObject.getInt("name");
                    int discountPrice = jsonObject.getInt("discount");
                    long sday = jsonObject.getLong("sday");
                    //					jsonObject.getInt("available_ticket_count");
                    int maxCount = jsonObject.getInt("max_sale_count");

                    JSONArray timeJSONArray = jsonObject.getJSONArray("eating_time_list");

                    int length = timeJSONArray.length();
                    long[] times = new long[length];

                    for (int i = 0; i < length; i++)
                    {
                        times[i] = timeJSONArray.getLong(i);
                    }

                    if (gourmetPaymentInformation.ticketTime == 0)
                    {

                    } else
                    {
                        boolean isExistTime = false;

                        for (long time : times)
                        {
                            if (gourmetPaymentInformation.ticketTime == time)
                            {
                                isExistTime = true;
                                break;
                            }
                        }

                        // 시간 값이 없어진 경우
                        if (isExistTime == false)
                        {
                            mIsChangedTime = true;
                        }
                    }

                    gourmetPaymentInformation.ticketTimes = times;

                    // 가격이 변동 되었다.
                    if (gourmetPaymentInformation.getTicketInformation().discountPrice != discountPrice)
                    {
                        mIsChangedPrice = true;
                    }

                    gourmetPaymentInformation.getTicketInformation().discountPrice = discountPrice;
                    gourmetPaymentInformation.ticketMaxCount = maxCount;

                    Calendar calendarCheckin = DailyCalendar.getInstance();
                    calendarCheckin.setTimeZone(TimeZone.getTimeZone("GMT"));
                    calendarCheckin.setTimeInMillis(sday);

                    SimpleDateFormat formatDay = new SimpleDateFormat("yyyy.MM.dd (EEE)", Locale.KOREA);
                    formatDay.setTimeZone(TimeZone.getTimeZone("GMT"));

                    gourmetPaymentInformation.checkInTime = formatDay.format(calendarCheckin.getTime());

                    if (gourmetPaymentInformation.ticketTime == 0)
                    {
                        // 방문시간을 선택하지 않은 경우
                        DailyNetworkAPI.getInstance(GourmetPaymentActivity.this).requestUserBillingCardList(mNetworkTag, mUserCreditCardListJsonResponseListener, GourmetPaymentActivity.this);
                    } else
                    {
                        requestValidateTicketPayment(gourmetPaymentInformation, mCheckInSaleTime);
                    }

                    mGourmetPaymentLayout.updateTicketInformationLayout(GourmetPaymentActivity.this, gourmetPaymentInformation);
                    mGourmetPaymentLayout.updateUserInformationLayout(gourmetPaymentInformation);
                    mGourmetPaymentLayout.updatePaymentInformationLayout(GourmetPaymentActivity.this, gourmetPaymentInformation);

                    recordAnalyticsPayment(gourmetPaymentInformation);
                } else
                {
                    onErrorPopupMessage(msgCode, response.getString("msg"));
                }
            } catch (Exception e)
            {
                onError(e);
            }
        }
    };

    protected DailyHotelJsonResponseListener mUserInformationFinalCheckJsonResponseListener = new DailyHotelJsonResponseListener()
    {
        @Override
        public void onErrorResponse(VolleyError volleyError)
        {

        }

        @Override
        public void onResponse(String url, JSONObject response)
        {
            try
            {
                int msgCode = response.getInt("msg_code");

                if (msgCode == 0)
                {
                    JSONObject jsonData = response.getJSONObject("data");

                    int bonus = jsonData.getInt("user_bonus");

                    if (bonus < 0)
                    {
                        bonus = 0;
                    }

                    if (mPaymentInformation.isEnabledBonus == true && bonus != mPaymentInformation.bonus)
                    {
                        // 보너스 값이 변경된 경우
                        mPaymentInformation.bonus = bonus;
                        showChangedBonusDialog();
                        return;
                    }

                    DailyNetworkAPI.getInstance(GourmetPaymentActivity.this).requestGourmetPaymentInformation(mNetworkTag, //
                        ((GourmetPaymentInformation) mPaymentInformation).getTicketInformation().index, //
                        mFinalCheckPayJsonResponseListener, GourmetPaymentActivity.this);
                } else
                {
                    onErrorPopupMessage(msgCode, response.getString("msg"));
                }
            } catch (Exception e)
            {
                onError(e);
            }
        }
    };

    private DailyHotelJsonResponseListener mFinalCheckPayJsonResponseListener = new DailyHotelJsonResponseListener()
    {
        @Override
        public void onErrorResponse(VolleyError volleyError)
        {

        }

        @Override
        public void onResponse(String url, JSONObject response)
        {
            try
            {
                GourmetPaymentInformation gourmetPaymentInformation = (GourmetPaymentInformation) mPaymentInformation;

                int msgCode = response.getInt("msg_code");

                if (msgCode == 0)
                {
                    JSONObject jsonObject = response.getJSONObject("data");

                    //					jsonObject.getInt("fnb_sale_reco_idx");
                    //					jsonObject.getInt("is_sale_time_over");
                    //					jsonObject.getInt("name");
                    int discountPrice = jsonObject.getInt("discount");
                    //                    long sday = jsonObject.getLong("sday");
                    //					jsonObject.getInt("available_ticket_count");
                    //                    int maxCount = jsonObject.getInt("max_sale_count");

                    JSONArray timeJSONArray = jsonObject.getJSONArray("eating_time_list");

                    int length = timeJSONArray.length();
                    long[] times = new long[length];

                    for (int i = 0; i < length; i++)
                    {
                        times[i] = timeJSONArray.getLong(i);
                    }

                    if (gourmetPaymentInformation.ticketTime == 0)
                    {
                        mIsChangedTime = true;
                    } else
                    {
                        boolean isExistTime = false;

                        for (long time : times)
                        {
                            if (gourmetPaymentInformation.ticketTime == time)
                            {
                                isExistTime = true;
                                break;
                            }
                        }

                        // 시간 값이 없어진 경우
                        if (isExistTime == false)
                        {
                            mIsChangedTime = true;
                        }
                    }

                    gourmetPaymentInformation.ticketTimes = times;

                    TicketInformation ticketInformation = gourmetPaymentInformation.getTicketInformation();

                    // 가격이 변동 되었다.
                    if (ticketInformation.discountPrice != discountPrice)
                    {
                        mIsChangedPrice = true;
                    }

                    ticketInformation.discountPrice = discountPrice;

                    if (mIsChangedPrice == true)
                    {
                        mIsChangedPrice = false;

                        // 현재 있는 팝업을 없애도록 한다.
                        if (mFinalCheckDialog != null && mFinalCheckDialog.isShowing() == true)
                        {
                            mFinalCheckDialog.cancel();
                            mFinalCheckDialog = null;
                        }

                        showChangedPriceDialog();
                    } else if (mIsChangedTime == true)
                    {
                        mIsChangedTime = false;

                        // 현재 있는 팝업을 없애도록 한다.
                        if (mFinalCheckDialog != null && mFinalCheckDialog.isShowing() == true)
                        {
                            mFinalCheckDialog.cancel();
                            mFinalCheckDialog = null;
                        }

                        showChangedTimeDialog();
                    } else
                    {
                        processPayment(mPaymentInformation, mCheckInSaleTime);
                    }
                } else
                {
                    onErrorPopupMessage(msgCode, response.getString("msg"));
                }
            } catch (Exception e)
            {
                onError(e);
            }
        }
    };

    private DailyHotelJsonResponseListener mCheckAvailableTicketJsonResponseListener = new DailyHotelJsonResponseListener()
    {
        @Override
        public void onErrorResponse(VolleyError volleyError)
        {

        }

        @Override
        public void onResponse(String url, JSONObject response)
        {
            try
            {
                JSONObject jsonObject = response.getJSONObject("data");

                boolean isOnSale = jsonObject.getBoolean("on_sale");

                int msgCode = response.getInt("msg_code");

                if (isOnSale == true && msgCode == 0)
                {
                    DailyNetworkAPI.getInstance(GourmetPaymentActivity.this).requestUserBillingCardList(mNetworkTag, mUserCreditCardListJsonResponseListener, GourmetPaymentActivity.this);
                } else
                {
                    onErrorPopupMessage(msgCode, response.getString("msg"));
                }
            } catch (Exception e)
            {
                onError(e);
            }
        }
    };

    private DailyHotelJsonResponseListener mPaymentEasyCreditCardJsonResponseListener = new DailyHotelJsonResponseListener()
    {
        @Override
        public void onErrorResponse(VolleyError volleyError)
        {

        }

        @Override
        public void onResponse(String url, JSONObject response)
        {
            try
            {
                int msgCode = response.getInt("msg_code");

                hidePorgressDialog();

                if (msgCode == 0)
                {
                    // 결제 관련 로그 남기기
                    recordAnalyticsPaymentComplete((GourmetPaymentInformation) mPaymentInformation);

                    showPaymentThankyou(mPaymentInformation, mPlaceImageUrl);
                } else
                {
                    int resultCode;
                    Intent intent = new Intent();

                    if (response.has("msg") == false)
                    {
                        resultCode = CODE_RESULT_ACTIVITY_PAYMENT_FAIL;
                    } else
                    {
                        String msg = response.getString("msg");

                        String[] result = msg.split("\\^");

                        if (result.length >= 1)
                        {
                            intent.putExtra(NAME_INTENT_EXTRA_DATA_RESULT, result[1]);
                        }

                        if ("SUCCESS".equalsIgnoreCase(result[0]) == true)
                        {
                            resultCode = CODE_RESULT_ACTIVITY_PAYMENT_SUCCESS;
                        } else if ("FAIL".equalsIgnoreCase(result[0]) == true)
                        {
                            resultCode = CODE_RESULT_ACTIVITY_PAYMENT_CANCEL;
                        } else
                        {
                            resultCode = CODE_RESULT_ACTIVITY_PAYMENT_FAIL;
                        }
                    }

                    onActivityPaymentResult(CODE_REQUEST_ACTIVITY_PAYMENT, resultCode, intent);
                }
            } catch (Exception e)
            {
                onError(e);
            } finally
            {
                unLockUI();
            }
        }
    };
}
