package com.twoheart.dailyhotel.screen.hotel.payment;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Toast;

import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ExLog;
import com.daily.base.widget.DailyToast;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Customer;
import com.twoheart.dailyhotel.model.Guest;
import com.twoheart.dailyhotel.model.PlacePaymentInformation;
import com.twoheart.dailyhotel.model.StayPaymentInformation;
import com.twoheart.dailyhotel.model.time.PlaceBookingDay;
import com.twoheart.dailyhotel.model.time.StayBookingDay;
import com.twoheart.dailyhotel.network.IDailyNetwork;
import com.twoheart.dailyhotel.network.model.StayProduct;
import com.twoheart.dailyhotel.place.activity.PlacePaymentWebActivity;
import com.twoheart.dailyhotel.util.Crypto;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;

import okhttp3.FormBody;

/**
 * Created by android_sam on 2017. 3. 17..
 */

public class StayPaymentWebActivity extends PlacePaymentWebActivity
{
    //    private SaleTime mSaleTime;
    //    private int mNights;
    private StayBookingDay mStayBookingDay;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestPostPaymentWebView(mWebView, mPlacePaymentInformation, mStayBookingDay);
    }

    @Override
    public void initIntentData(Intent intent)
    {
        super.initIntentData(intent);

        mStayBookingDay = intent.getParcelableExtra(NAME_INTENT_EXTRA_DATA_PLACEBOOKINGDAY);
        //        mSaleTime = intent.getParcelableExtra(NAME_INTENT_EXTRA_DATA_SALETIME);
        //        mNights = intent.getIntExtra(NAME_INTENT_EXTRA_DATA_NIGHTS, 1);
    }

    @Override
    protected boolean hasProductList()
    {
        if (mPlacePaymentInformation == null)
        {
            return false;
        }

        StayPaymentInformation stayPaymentInformation = (StayPaymentInformation) mPlacePaymentInformation;
        if (stayPaymentInformation == null)
        {
            return false;
        }

        return stayPaymentInformation.getSaleRoomInformation() != null;
    }

    @Override
    protected int getProductIndex()
    {
        if (mPlacePaymentInformation == null)
        {
            return 0;
        }

        StayPaymentInformation stayPaymentInformation = (StayPaymentInformation) mPlacePaymentInformation;
        if (stayPaymentInformation == null || stayPaymentInformation.getSaleRoomInformation() == null)
        {
            return 0;
        }

        return stayPaymentInformation.getSaleRoomInformation().roomIndex;
    }

    @Override
    protected String getScreenName()
    {
        return AnalyticsManager.Screen.DAILYHOTEL_PAYMENT_PROCESS;
    }

    @Override
    protected void requestPostPaymentWebView(WebView webView //
        , PlacePaymentInformation placePaymentInformation, PlaceBookingDay placeBookingDay)
    {
        if (placePaymentInformation == null)
        {
            return;
        }

        StayPaymentInformation stayPaymentInformation = (StayPaymentInformation) placePaymentInformation;
        if (stayPaymentInformation == null)
        {
            return;
        }

        StayBookingDay stayBookingDay = (StayBookingDay) placeBookingDay;
        if (stayBookingDay == null)
        {
            return;
        }

        String checkInDate = stayBookingDay.getCheckInDay("yyyyMMdd");
        String nights = "1";

        try
        {
            nights = Integer.toString(stayBookingDay.getNights());
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }

        String name;
        String phone;
        String email;

        boolean isOverseas = stayPaymentInformation.isOverSeas;
        Guest guest = stayPaymentInformation.getGuest();

        if (isOverseas == true)
        {
            name = guest.name;
            phone = guest.phone;
            email = guest.email;
        } else
        {
            if (guest == null)
            {
                Customer customer = stayPaymentInformation.getCustomer();

                name = customer.getName();
                phone = customer.getPhone();
                email = customer.getEmail();
            } else
            {
                name = guest.name;
                phone = guest.phone;
                email = guest.email;
            }
        }

        if (DailyTextUtils.isTextEmpty(name, phone, email) == true)
        {
            restartExpiredSession();
            return;
        }

        try
        {
            StayProduct stayProduct = stayPaymentInformation.getSaleRoomInformation();

            FormBody.Builder builder = new FormBody.Builder();
            builder.add("room_idx", String.valueOf(stayProduct.roomIndex));
            builder.add("payment_type", stayPaymentInformation.paymentType.name());
            builder.add("checkin_date", checkInDate);
            builder.add("nights", nights);

            switch (stayPaymentInformation.discountType)
            {
                case BONUS:
                    builder.add("bonus", Integer.toString(stayPaymentInformation.bonus));
                    break;

                case COUPON:
                    builder.add("user_coupon_code", stayPaymentInformation.getCoupon().userCouponCode);
                    break;
            }

            builder.add("guest_name", name);
            builder.add("guest_phone", phone.replace("-", ""));
            builder.add("guest_email", email);

            // 주차/도보
            if (StayPaymentInformation.VISIT_TYPE_PARKING.equalsIgnoreCase(stayPaymentInformation.visitType) == true)
            {
                builder.add("arrival_transportation", stayPaymentInformation.isVisitWalking == true ? "WALKING" : "CAR");
            } else if (StayPaymentInformation.VISIT_TYPE_NO_PARKING.equalsIgnoreCase(stayPaymentInformation.visitType) == true)
            {
                builder.add("arrival_transportation", "NO_PARKING");
            }

            String url = Crypto.getUrlDecoderEx(IDailyNetwork.URL_DAILYHOTEL_SERVER)//
                + Crypto.getUrlDecoderEx(IDailyNetwork.URL_WEBAPI_HOTEL_V1_PAYMENT_SESSION_COMMON);

            WebViewPostAsyncTask webViewPostAsyncTask = new WebViewPostAsyncTask(webView, builder);
            webViewPostAsyncTask.execute(url);

        } catch (Exception e)
        {
            ExLog.e(e.toString());

            DailyToast.showToast(StayPaymentWebActivity.this, R.string.toast_msg_failed_to_get_payment_info, Toast.LENGTH_SHORT);
            finish();
            return;
        }
    }

    @Override
    public void onFeed(String msg)
    {
        int resultCode;

        if (msg == null)
        {
            resultCode = CODE_RESULT_ACTIVITY_PAYMENT_FAIL;
        } else if (msg.equals("SUCCESS"))
        {
            resultCode = CODE_RESULT_ACTIVITY_PAYMENT_SUCCESS;
        } else if (msg.equals("INVALID_SESSION"))
        {
            resultCode = CODE_RESULT_ACTIVITY_PAYMENT_INVALID_SESSION;
        } else if (msg.equals("SOLD_OUT"))
        {
            resultCode = CODE_RESULT_ACTIVITY_PAYMENT_SOLD_OUT;
        } else if (msg.equals("PAYMENT_COMPLETE"))
        {
            resultCode = CODE_RESULT_ACTIVITY_PAYMENT_COMPLETE;
        } else if (msg.equals("INVALID_DATE"))
        {
            resultCode = CODE_RESULT_ACTIVITY_PAYMENT_INVALID_DATE;
        } else if (msg.equals("PAYMENT_CANCELED"))
        {
            resultCode = CODE_RESULT_ACTIVITY_PAYMENT_CANCELED;
        } else if (msg.equals("ACCOUNT_READY"))
        {
            resultCode = CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_READY;
        } else if (msg.equals("ACCOUNT_TIME_ERROR"))
        {
            resultCode = CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_TIME_ERROR;
        } else if (msg.equals("ACCOUNT_DUPLICATE"))
        {
            resultCode = CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_DUPLICATE;
        } else if (msg.equals("NOT_AVAILABLE"))
        {
            resultCode = CODE_RESULT_ACTIVITY_PAYMENT_NOT_AVAILABLE;
        } else if (msg.equals("PAYMENT_TIMEOVER"))
        {
            resultCode = CODE_RESULT_ACTIVITY_PAYMENT_TIMEOVER;
        } else
        {
            resultCode = CODE_RESULT_ACTIVITY_PAYMENT_FAIL;
        }

        Intent intent = new Intent();
        intent.putExtra(NAME_INTENT_EXTRA_DATA_PAYMENTINFORMATION, mPlacePaymentInformation);

        setResult(resultCode, intent);
        finish();
    }

    @Override
    public void onPaymentFeed(String result)
    {
        Intent intent = new Intent();
        intent.putExtra(NAME_INTENT_EXTRA_DATA_MESSAGE, result);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_PAYMENTINFORMATION, mPlacePaymentInformation);

        setResult(CODE_RESULT_ACTIVITY_PAYMENT_PRECHECK, intent);
        finish();
    }
}

// Old Version HotelPaymentWebActivity
//@SuppressLint("NewApi")
//public class HotelPaymentWebActivity extends BaseActivity implements Constants
//{
//    public static final int PROGRESS_STAT_NOT_START = 1;
//    public static final int PROGRESS_STAT_IN = 2;
//    public static final int PROGRESS_DONE = 3;
//    static String CARD_CD = "";
//    static String QUOTA = "";
//    public int m_nStat = PROGRESS_STAT_NOT_START;
//    StayPaymentInformation mStayPaymentInformation;
//
//    WebView mWebView;
//    Handler handler = new Handler();
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState)
//    {
//        overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
//
//        super.onCreate(savedInstanceState);
//
//        Intent intent = getIntent();
//
//        if (intent == null)
//        {
//            finish();
//            return;
//        }
//
//        mStayPaymentInformation = intent.getParcelableExtra(NAME_INTENT_EXTRA_DATA_PAYMENTINFORMATION);
//        StayBookingDay stayBookingDay = intent.getParcelableExtra(NAME_INTENT_EXTRA_DATA_PLACEBOOKINGDAY);
//
//        if (mStayPaymentInformation == null || stayBookingDay == null)
//        {
//            DailyToast.showToast(HotelPaymentWebActivity.this, R.string.toast_msg_failed_to_get_payment_info, Toast.LENGTH_SHORT);
//            finish();
//            return;
//        }
//
//        if (mStayPaymentInformation.getSaleRoomInformation().roomIndex == 0)
//        {
//            // 세션이 만료되어 재시작 요청.
//            restartExpiredSession();
//            return;
//        }
//
//        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//
//        // 앱에서 팝업이 띄어진 상태에서 Activity를 종료하면 발생하는 현상을 막기 위해서
//        Activity activity = this;
//        while (activity.getParent() != null)
//        {
//            activity = activity.getParent();
//        }
//
//        mWebView = new WebView(activity);
//
//        setContentView(mWebView, layoutParams);
//
//        // TODO  setWebContentsDebuggingEnabled
//        //        WebView.setWebContentsDebuggingEnabled(true);
//
//        mWebView.getSettings().setSavePassword(false);
//        mWebView.getSettings().setAppCacheEnabled(false); // 7.4 캐시 정책 비활성화.
//        mWebView.getSettings().setJavaScriptEnabled(true);
//        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//        {
//            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
//
//            CookieManager cookieManager = CookieManager.getInstance();
//            cookieManager.setAcceptCookie(true);
//            cookieManager.setAcceptThirdPartyCookies(mWebView, true);
//        }
//
//        mWebView.addJavascriptInterface(new KCPPayBridge(), "KCPPayApp");
//        // 하나SK 카드 선택시 User가 선택한 기본 정보를 가지고 오기위해 사용
//        mWebView.addJavascriptInterface(new KCPPayCardInfoBridge(), "KCPPayCardInfo");
//        mWebView.addJavascriptInterface(new KCPPayPinInfoBridge(), "KCPPayPinInfo"); // 페이핀 기능 추가
//        mWebView.addJavascriptInterface(new KCPPayPinReturn(), "KCPPayPinRet"); // 페이핀
//        // 기능
//        // 추가
//        mWebView.addJavascriptInterface(new JavaScriptExtention(), "android");
//
//        mWebView.addJavascriptInterface(new TeleditBridge(), "TeleditApp");
//
//        // webView.addJavascriptInterface(new HtmlObserver(), "HtmlObserver");
//
//        mWebView.setWebChromeClient(new DailyWebChromeClient());
//        mWebView.setWebViewClient(new DailyWebViewClient());
//
//        mWebView.setOnLongClickListener(new OnLongClickListener()
//        {
//            @Override
//            public boolean onLongClick(View v)
//            {
//                return true;
//            }
//        }); // 롱클릭 에러 방지.
//
//        requestPostPaymentWebView(mWebView, mStayPaymentInformation, stayBookingDay);
//    }
//
//    private void requestPostPaymentWebView(WebView webView //
//        , StayPaymentInformation stayPaymentInformation, StayBookingDay stayBookingDay)
//    {
//        String name;
//        String phone;
//        String email;
//
//        boolean isOverseas = stayPaymentInformation.isOverSeas;
//        Guest guest = stayPaymentInformation.getGuest();
//
//        if (isOverseas == true)
//        {
//            name = guest.name;
//            phone = guest.phone;
//            email = guest.email;
//        } else
//        {
//            if (guest == null)
//            {
//                Customer customer = stayPaymentInformation.getCustomer();
//
//                name = customer.getName();
//                phone = customer.getPhone();
//                email = customer.getEmail();
//            } else
//            {
//                name = guest.name;
//                phone = guest.phone;
//                email = guest.email;
//            }
//        }
//
//        if (com.daily.base.util.TextUtils.isTextEmpty(name, phone, email) == true)
//        {
//            restartExpiredSession();
//            return;
//        }
//
//        try
//        {
//            StayProduct stayProduct = stayPaymentInformation.getSaleRoomInformation();
//
//            FormBody.Builder builder = new FormBody.Builder();
//            builder.add("room_idx", String.valueOf(stayProduct.roomIndex));
//            builder.add("payment_type", stayPaymentInformation.paymentType.name());
//            builder.add("checkin_date", stayBookingDay.getCheckInDateTime("yyyyMMdd"));
//            builder.add("nights", Integer.toString(stayBookingDay.getNights()));
//
//            switch (stayPaymentInformation.discountType)
//            {
//                case BONUS:
//                    builder.add("bonus", Integer.toString(stayPaymentInformation.bonus));
//                    break;
//
//                case COUPON:
//                    builder.add("user_coupon_code", stayPaymentInformation.getCoupon().userCouponCode);
//                    break;
//            }
//
//            builder.add("guest_name", name);
//            builder.add("guest_phone", phone.replace("-", ""));
//            builder.add("guest_email", email);
//
//            // 주차/도보
//            if (StayPaymentInformation.VISIT_TYPE_PARKING.equalsIgnoreCase(stayPaymentInformation.visitType) == true)
//            {
//                builder.add("arrival_transportation", stayPaymentInformation.isVisitWalking == true ? "WALKING" : "CAR");
//            } else if (StayPaymentInformation.VISIT_TYPE_NO_PARKING.equalsIgnoreCase(stayPaymentInformation.visitType) == true)
//            {
//                builder.add("arrival_transportation", "NO_PARKING");
//            }
//
//            String url = Crypto.getUrlDecoderEx(IDailyNetwork.URL_DAILYHOTEL_SERVER)//
//                + Crypto.getUrlDecoderEx(IDailyNetwork.URL_WEBAPI_HOTEL_V1_PAYMENT_SESSION_COMMON);
//
//            WebViewPostAsyncTask webViewPostAsyncTask = new WebViewPostAsyncTask(webView, builder);
//            webViewPostAsyncTask.execute(url);
//        } catch (Exception e)
//        {
//            ExLog.e(e.toString());
//
//            DailyToast.showToast(HotelPaymentWebActivity.this, R.string.toast_msg_failed_to_get_payment_info, Toast.LENGTH_SHORT);
//            finish();
//            return;
//        }
//    }
//
//    @Override
//    protected void onStart()
//    {
//        AnalyticsManager.getInstance(this).recordScreen(this, Screen.DAILYHOTEL_PAYMENT_PROCESS, null);
//
//        super.onStart();
//    }
//
//    @JavascriptInterface
//    boolean url_scheme_intent(WebView view, String url)
//    {
//        // chrome 버젼 방식 : 2014.01 추가
//        if (url.startsWith("intent"))
//        {
//            // ILK 용
//            if (url.contains("com.lotte.lottesmartpay"))
//            {
//                try
//                {
//                    startActivity(Intent.parseUri(url, Intent.URI_INTENT_SCHEME));
//                } catch (URISyntaxException e)
//                {
//                    return false;
//                } catch (ActivityNotFoundException e)
//                {
//                    Util.installPackage(HotelPaymentWebActivity.this, "com.lotte.lottesmartpay");
//                    return false;
//                }
//            }
//            // ILK 용
//            else if (url.contains("com.ahnlab.v3mobileplus"))
//            {
//                try
//                {
//                    startActivity(Intent.parseUri(url, 0));
//                } catch (URISyntaxException e)
//                {
//                    return false;
//                } catch (ActivityNotFoundException e)
//                {
//                    Util.installPackage(HotelPaymentWebActivity.this, "com.ahnlab.v3mobileplus");
//                    return false;
//                }
//            } else
//            {
//                Intent intent;
//
//                try
//                {
//                    intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
//                } catch (URISyntaxException ex)
//                {
//                    return false;
//                }
//
//                // 앱설치 체크를 합니다.
//                if (getPackageManager().resolveActivity(intent, 0) == null)
//                {
//                    Util.installPackage(HotelPaymentWebActivity.this, intent.getPackage());
//                    return true;
//                }
//
//                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(intent.getDataString()));
//
//                try
//                {
//                    startActivity(intent);
//                } catch (ActivityNotFoundException e)
//                {
//                    Util.installPackage(HotelPaymentWebActivity.this, intent.getPackage());
//                    return true;
//                }
//            }
//        }
//        // 기존 방식
//        else
//        {
//            if (url.startsWith("ispmobile"))
//            { // 7.4 ISP 모듈 연동 테스트
//                if (!new PackageState(this).getPackageDownloadInstallState(PACKAGE_NAME_ISP))
//                {
//                    try
//                    {
//                        DailyToast.showToast(HotelPaymentWebActivity.this, R.string.toast_msg_retry_payment_after_install_app, Toast.LENGTH_LONG);
//                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL_STORE_PAYMENT_ISP)));
//                        view.goBack();
//                        return true;
//                    } catch (ActivityNotFoundException e)
//                    {
//                        Util.installPackage(HotelPaymentWebActivity.this, PACKAGE_NAME_ISP);
//                        return true;
//                    }
//                }
//            } else if (url.startsWith("kftc-bankpay"))
//            { // 7.9 이니시스 모듈 연동 테스트
//                if (!new PackageState(this).getPackageDownloadInstallState(PACKAGE_NAME_KFTC))
//                {
//                    try
//                    {
//                        DailyToast.showToast(HotelPaymentWebActivity.this, R.string.toast_msg_retry_payment_after_install_app, Toast.LENGTH_LONG);
//                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL_STORE_PAYMENT_KFTC)));
//                        view.goBack();
//                        return true;
//                    } catch (ActivityNotFoundException e)
//                    {
//                        Util.installPackage(HotelPaymentWebActivity.this, PACKAGE_NAME_KFTC);
//                        return true;
//                    }
//                }
//            } else if (url.startsWith("mpocket.online.ansimclick"))
//            {
//                if (!new PackageState(this).getPackageDownloadInstallState(PACKAGE_NAME_MPOCKET))
//                {
//                    try
//                    {
//                        DailyToast.showToast(HotelPaymentWebActivity.this, R.string.toast_msg_retry_payment_after_install_app, Toast.LENGTH_LONG);
//                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL_STORE_PAYMENT_MPOCKET)));
//                        return true;
//                    } catch (ActivityNotFoundException e)
//                    {
//                        Util.installPackage(HotelPaymentWebActivity.this, PACKAGE_NAME_MPOCKET);
//                        return true;
//                    }
//                }
//            } else if (url.startsWith("market") == true)
//            {
//                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
//                return true;
//            }
//
//            // 결제 모듈 실행.
//            try
//            {
//                Uri uri = Uri.parse(url);
//                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//
//                int requestCode = 0;
//                if (url.startsWith("kftc-bankpay"))
//                {
//                    requestCode = CODE_REQUEST_KFTC_BANKPAY;
//                } else if (url.startsWith("ispmobile"))
//                {
//                    requestCode = CODE_REQUEST_ISPMOBILE;
//                }
//
//                startActivityForResult(intent, requestCode);
//            } catch (ActivityNotFoundException e)
//            {
//                return false;
//            }
//        }
//
//        return true;
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
//    {
//        super.onActivityResult(requestCode, resultCode, intent);
//
//        String scriptForSkip = "javascript:";
//        if (requestCode == CODE_REQUEST_ISPMOBILE)
//        {
//            scriptForSkip += "submitIspAuthInfo('RUNSCHEME');"; // ISP 확인 버튼 콜
//        } else if (requestCode == CODE_REQUEST_KFTC_BANKPAY)
//        {
//            scriptForSkip += "returnUrltoMall();"; // KTFC 확인 버튼 콜
//        }
//
//        mWebView.loadUrl(scriptForSkip);
//    }
//
//    @Override
//    protected void onRestart()
//    {
//        super.onRestart();
//
//        // 하나 SK 모듈로 결제 이후 해당 카드 정보를 가지고 오기위해 사용
//        if (ResultRcvActivity.m_uriResult != null)
//        {// ResultRcvActivity
//            if (ResultRcvActivity.m_uriResult.getQueryParameter("realPan") != null && ResultRcvActivity.m_uriResult.getQueryParameter("cavv") != null && ResultRcvActivity.m_uriResult.getQueryParameter("xid") != null && ResultRcvActivity.m_uriResult.getQueryParameter("eci") != null)
//            {
//                //				ExLog.d("[PayDemoActivity] HANA SK Result = javascript:hanaSK('" + ResultRcvActivity.m_uriResult.getQueryParameter("realPan") + "', '" + ResultRcvActivity.m_uriResult.getQueryParameter("cavv") + "', '" + ResultRcvActivity.m_uriResult.getQueryParameter("xid") + "', '" + ResultRcvActivity.m_uriResult.getQueryParameter("eci") + "', '" + CARD_CD + "', '" + QUOTA + "');");
//
//                // 하나 SK 모듈로 인증 이후 승인을 하기위해 결제 함수를 호출 (주문자 페이지)
//                mWebView.loadUrl("javascript:hanaSK('" + ResultRcvActivity.m_uriResult.getQueryParameter("realPan") + "', '" + ResultRcvActivity.m_uriResult.getQueryParameter("cavv") + "', '" + ResultRcvActivity.m_uriResult.getQueryParameter("xid") + "', '" + ResultRcvActivity.m_uriResult.getQueryParameter("eci") + "', '" + CARD_CD + "', '" + QUOTA + "');");
//            }
//
//            if ((ResultRcvActivity.m_uriResult.getQueryParameter("res_cd") == null ? "" : ResultRcvActivity.m_uriResult.getQueryParameter("res_cd")).equals("999"))
//            {
//                //				ExLog.d("[PayDemoActivity] HANA SK Result = cancel");
//
//                m_nStat = 9;
//            }
//
//            if ((ResultRcvActivity.m_uriResult.getQueryParameter("isp_res_cd") == null ? "" : ResultRcvActivity.m_uriResult.getQueryParameter("isp_res_cd")).equals("0000"))
//            {
//                //				ExLog.d("[PayDemoActivity] ISP Result = 0000");
//
//                mWebView.loadUrl("http://pggw.kcp.co.kr/lds/smart_phone_linux_jsp/sample/card/samrt_res.jsp?result=OK&a=" + ResultRcvActivity.m_uriResult.getQueryParameter("a"));
//            } else
//            {
//                //				ExLog.d("[PayDemoActivity] ISP Result = cancel");
//            }
//        }
//
//        if (m_nStat == PROGRESS_STAT_IN)
//        {
//            checkFrom();
//        }
//
//        ResultRcvActivity.m_uriResult = null;
//    }
//
//    @JavascriptInterface
//    public void checkFrom()
//    {
//        try
//        {
//            if (ResultRcvActivity.m_uriResult != null)
//            {
//                m_nStat = PROGRESS_DONE;
//                String strResultInfo = ResultRcvActivity.m_uriResult.getQueryParameter("approval_key");
//
//                if (strResultInfo == null || strResultInfo.length() <= 4)
//                {
//                    finishActivity(getString(R.string.act_payment_isp_error));
//                    return;
//                }
//
//                String strResCD = strResultInfo.substring(strResultInfo.length() - 4);
//
//                //				ExLog.d("[PayDemoActivity] result=[" + strResultInfo + "]+" + "res_cd=[" + strResCD + "]");
//
//                if (strResCD.equals("0000") == true)
//                {
//                    String strApprovalKey = strResultInfo.substring(0, strResultInfo.length() - 4);
//
//                    //					ExLog.d("[PayDemoActivity] approval_key=[" + strApprovalKey + "]");
//
//                    mWebView.loadUrl("https://smpay.kcp.co.kr/app.do?ActionResult=app&approval_key=" + strApprovalKey);
//
//                } else if (strResCD.equals("3001") == true)
//                {
//                    finishActivity(getString(R.string.act_payment_isp_user_cancel));
//                } else
//                {
//                    finishActivity(getString(R.string.act_payment_isp_other_error));
//                }
//            }
//        } catch (Exception e)
//        {
//            ExLog.e(e.toString());
//        }
//    }
//
//    @Override
//    @JavascriptInterface
//    protected Dialog onCreateDialog(int id)
//    {
//        super.onCreateDialog(id);
//
//        View.OnClickListener posListener = new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View view)
//            {
//                finishActivity(getString(R.string.act_payment_user_cancel));
//            }
//        };
//
//        return createSimpleDialog(getString(R.string.dialog_btn_text_cancel), getString(R.string.dialog_msg_chk_cancel_payment_progress), getString(R.string.dialog_btn_text_yes), getString(R.string.dialog_btn_text_no), posListener, null);
//    }
//
//    @JavascriptInterface
//    public void finishActivity(String p_strFinishMsg)
//    {
//        int resultCode = CODE_RESULT_ACTIVITY_PAYMENT_FAIL;
//
//        if (p_strFinishMsg != null)
//        {
//            if (p_strFinishMsg.equals("NOT_AVAILABLE"))
//            {
//                resultCode = CODE_RESULT_ACTIVITY_PAYMENT_NOT_AVAILABLE;
//            } else if (p_strFinishMsg.contains(getString(R.string.act_payment_chk_contain)))
//            {
//                resultCode = CODE_RESULT_ACTIVITY_PAYMENT_CANCELED;// RESULT_CANCELED
//            }
//        }
//
//        Intent intent = new Intent();
//        intent.putExtra(NAME_INTENT_EXTRA_DATA_PAYMENTINFORMATION, mStayPaymentInformation);
//
//        setResult(resultCode, intent);
//        finish();
//    }
//
//    @Override
//    public void finish()
//    {
//        super.finish();
//
//        overridePendingTransition(R.anim.hold, R.anim.slide_out_right);
//    }
//
//    @Override
//    public void onBackPressed()
//    {
//        if (isFinishing() == true)
//        {
//            return;
//        }
//
//        View.OnClickListener posListener = new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View view)
//            {
//                finish();
//            }
//        };
//
//        showSimpleDialog(getString(R.string.dialog_title_payment), getString(R.string.dialog_msg_chk_cancel_payment), getString(R.string.dialog_btn_text_yes), getString(R.string.dialog_btn_text_no), posListener, null);
//    }
//
//    private class DailyWebChromeClient extends WebChromeClient
//    {
//        boolean isActionBarProgressBarShowing = false;
//
//        DailyWebChromeClient()
//        {
//        }
//
//        @Override
//        public void onProgressChanged(WebView view, int newProgress)
//        {
//            super.onProgressChanged(view, newProgress);
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
//            {
//                if (newProgress != 100)
//                {
//                    setActionBarProgressBar(true);
//                } else
//                {
//                    setActionBarProgressBar(false);
//                }
//            }
//        }
//
//        void setActionBarProgressBar(boolean show)
//        {
//            if (show != isActionBarProgressBarShowing)
//            {
//                setSupportProgressBarIndeterminateVisibility(show);
//                isActionBarProgressBarShowing = show;
//            }
//        }
//    }
//
//    private class DailyWebViewClient extends WebViewClient
//    {
//        DailyWebViewClient()
//        {
//        }
//
//        @Override
//        public boolean shouldOverrideUrlLoading(WebView view, String url)
//        {
//            if (url != null && !url.equals("about:blank"))
//            {
//                if (url.startsWith("http://") || url.startsWith("https://"))
//                {
//                    if (url.contains("http://market.android.com") || url.contains("http://m.ahnlab.com/kr/site/download") || url.endsWith(".apk"))
//                    {
//                        return url_scheme_intent(view, url);
//                    } else
//                    {
//                        view.loadUrl(url);
//                        return false;
//                    }
//                } else if (url.startsWith("mailto:"))
//                {
//                    return false;
//                } else if (url.startsWith("tel:"))
//                {
//                    String noCallMessage = HotelPaymentWebActivity.this.getResources().getString(R.string.toast_msg_no_call_web);
//
//                    if (Util.isTelephonyEnabled(HotelPaymentWebActivity.this) == true)
//                    {
//                        try
//                        {
//                            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(url)));
//                        } catch (ActivityNotFoundException e)
//                        {
//                            DailyToast.showToast(HotelPaymentWebActivity.this, noCallMessage, Toast.LENGTH_LONG);
//                            return false;
//                        }
//                    } else
//                    {
//                        DailyToast.showToast(HotelPaymentWebActivity.this, noCallMessage, Toast.LENGTH_LONG);
//                        return false;
//                    }
//                } else
//                {
//                    return url_scheme_intent(view, url);
//                }
//            }
//            // else if isp판단 , 없으면 isp 설치페이지로 이동.
//
//            return true;
//        }
//
//        // error 처리
//        @Override
//        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
//        {
//            super.onReceivedError(view, errorCode, description, failingUrl);
//
//            //			ExLog.e("ErrorCode / Description / failingUrl : " + errorCode + " / " + description + " / " + failingUrl);
//
//            mWebView.loadUrl("about:blank");
//
//            Intent intent = new Intent();
//            intent.putExtra(NAME_INTENT_EXTRA_DATA_PAYMENTINFORMATION, mStayPaymentInformation);
//
//            if (Util.isAvailableNetwork(HotelPaymentWebActivity.this) == true)
//            {
//                setResult(CODE_RESULT_ACTIVITY_PAYMENT_FAIL, intent);
//            } else
//            {
//                setResult(CODE_RESULT_ACTIVITY_PAYMENT_NETWORK_ERROR, intent);
//            }
//
//            finish();
//        }
//
//        @Override
//        public void onPageStarted(WebView view, String url, Bitmap favicon)
//        {
//            super.onPageStarted(view, url, favicon);
//
//            lockUI();
//            //			handler.removeCallbacks(networkCheckRunner); // 결제 완료시 항상 네트워크
//            // 불안정뜨므로, 네트워크 체크는
//            // 제거하도록 함.
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
//            {
//                setSupportProgressBarIndeterminateVisibility(true);
//            }
//        }
//
//        @Override
//        public void onPageFinished(WebView view, String url)
//        {
//            super.onPageFinished(view, url);
//
//            unLockUI();
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
//            {
//                setSupportProgressBarIndeterminateVisibility(false);
//            }
//        }
//    }
//
//    /**
//     * 다날 모바일 결제 관련 브릿지.
//     *
//     * @author jangjunho
//     */
//    private class TeleditBridge
//    {
//        TeleditBridge()
//        {
//        }
//
//        /**
//         * @param val 휴대폰 결제 완료 후 결과값.
//         */
//        @JavascriptInterface
//        public void Result(final String val)
//        {
//            Intent intent = new Intent();
//            intent.putExtra(NAME_INTENT_EXTRA_DATA_PAYMENTINFORMATION, mStayPaymentInformation);
//
//            setResult(CODE_RESULT_ACTIVITY_PAYMENT_COMPLETE, intent);
//            finish();
//        }
//
//        /**
//         * web에서 닫기를 콜 했을때 호출.
//         */
//        @JavascriptInterface
//        public void BestClose()
//        {
//            Intent intent = new Intent();
//            intent.putExtra(NAME_INTENT_EXTRA_DATA_PAYMENTINFORMATION, mStayPaymentInformation);
//
//            setResult(CODE_RESULT_ACTIVITY_PAYMENT_CANCELED, intent);
//            finish();
//        }
//    }
//
//    private class KCPPayPinReturn
//    {
//        KCPPayPinReturn()
//        {
//        }
//
//        @JavascriptInterface
//        public String getConfirm()
//        {
//            if (ResultRcvActivity.b_type)
//            {
//                ResultRcvActivity.b_type = false;
//                return "true";
//            } else
//            {
//                return "false";
//            }
//        }
//    }
//
//    private class KCPPayPinInfoBridge
//    {
//        KCPPayPinInfoBridge()
//        {
//        }
//
//        @JavascriptInterface
//        public void getPaypinInfo(final String url)
//        {
//            handler.post(new Runnable()
//            {
//                public void run()
//                {
//                    PackageState ps = new PackageState(HotelPaymentWebActivity.this);
//
//                    if (!ps.getPackageAllInstallState("com.skp.android.paypin"))
//                    {
//                        paypinConfim();
//                    } else
//                    {
//                        url_scheme_intent(null, url);
//                    }
//                }
//            });
//        }
//
//        @JavascriptInterface
//        void paypinConfim()
//        {
//            if (isFinishing() == true)
//            {
//                return;
//            }
//
//            View.OnClickListener posListener = new View.OnClickListener()
//            {
//                @Override
//                public void onClick(View view)
//                {
//                    if (!url_scheme_intent(null, "tstore://PRODUCT_VIEW/0000284061/0"))
//                    {
//                        url_scheme_intent(null, "market://details?id=com.skp.android.paypin&feature=search_result#?t=W251bGwsMSwxLDEsImNvbS5za3AuYW5kcm9pZC5wYXlwaW4iXQ.k");
//                    }
//                }
//            };
//
//            View.OnClickListener negaListener = new View.OnClickListener()
//            {
//                @Override
//                public void onClick(View view)
//                {
//                    DailyToast.showToast(HotelPaymentWebActivity.this, R.string.toast_msg_cancel_payment, Toast.LENGTH_SHORT);
//                }
//            };
//
//            showSimpleDialog(getString(R.string.dialog_btn_text_confirm), getString(R.string.dialog_msg_install_paypin), getString(R.string.dialog_btn_text_install), getString(R.string.dialog_btn_text_cancel), posListener, negaListener);
//        }
//    }
//
//    // 하나SK 카드 선택시 User가 선택한 기본 정보를 가지고 오기위해 사용
//    private class KCPPayCardInfoBridge
//    {
//        KCPPayCardInfoBridge()
//        {
//        }
//
//        @JavascriptInterface
//        public void getCardInfo(final String card_cd, final String quota)
//        {
//            handler.post(new Runnable()
//            {
//                public void run()
//                {
//                    CARD_CD = card_cd;
//                    QUOTA = quota;
//
//                    PackageState ps = new PackageState(HotelPaymentWebActivity.this);
//
//                    if (!ps.getPackageDownloadInstallState("com.skt.at"))
//                    {
//                        alertToNext();
//                    }
//                }
//            });
//        }
//
//        @JavascriptInterface
//        void alertToNext()
//        {
//            if (isFinishing() == true)
//            {
//                return;
//            }
//
//            View.OnClickListener posListener = new View.OnClickListener()
//            {
//                @Override
//                public void onClick(View view)
//                {
//                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://cert.hanaskcard.com/Ansim/HanaSKPay.apk"));
//
//                    m_nStat = PROGRESS_STAT_IN;
//
//                    startActivity(intent);
//                }
//            };
//
//            showSimpleDialog(getString(R.string.dialog_notice2), getString(R.string.dialog_msg_install_hana_sk), getString(R.string.dialog_btn_text_yes), getString(R.string.dialog_btn_text_no), posListener, null);
//        }
//    }
//
//    private class KCPPayBridge
//    {
//        KCPPayBridge()
//        {
//        }
//
//        @JavascriptInterface
//        public void launchMISP(final String arg)
//        {
//            handler.post(new Runnable()
//            {
//                public void run()
//                {
//                    PackageState ps = new PackageState(HotelPaymentWebActivity.this);
//
//                    String argUrl = arg;
//
//                    if (!arg.equals("Install") && !ps.getPackageDownloadInstallState("kvp.jjy.MispAndroid"))
//                    {
//                        argUrl = "Install";
//                    }
//
//                    String strUrl = (argUrl.equals("Install") == true) ? "market://details?id=kvp.jjy.MispAndroid320" : argUrl;
//
//                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(strUrl));
//
//                    m_nStat = PROGRESS_STAT_IN;
//
//                    try
//                    {
//                        startActivity(intent);
//                    } catch (Exception e)
//                    {
//                        Util.installPackage(HotelPaymentWebActivity.this, "kvp.jjy.MispAndroid320");
//                    }
//                }
//            });
//        }
//    }
//
//    private class JavaScriptExtention
//    {
//        JavaScriptExtention()
//        {
//        }
//
//        // 서버로부터 받은 결제 결과 메시지를 처리함.
//        // 각각의 경우에 맞는 resultCode를 넣어 BookingActivity로 finish시킴.
//        @JavascriptInterface
//        public void feed(final String msg)
//        {
//            int resultCode;
//
//            if (msg == null)
//            {
//                resultCode = CODE_RESULT_ACTIVITY_PAYMENT_FAIL;
//            } else if (msg.equals("SUCCESS"))
//            {
//                resultCode = CODE_RESULT_ACTIVITY_PAYMENT_SUCCESS;
//            } else if (msg.equals("INVALID_SESSION"))
//            {
//                resultCode = CODE_RESULT_ACTIVITY_PAYMENT_INVALID_SESSION;
//            } else if (msg.equals("SOLD_OUT"))
//            {
//                resultCode = CODE_RESULT_ACTIVITY_PAYMENT_SOLD_OUT;
//            } else if (msg.equals("PAYMENT_COMPLETE"))
//            {
//                resultCode = CODE_RESULT_ACTIVITY_PAYMENT_COMPLETE;
//            } else if (msg.equals("INVALID_DATE"))
//            {
//                resultCode = CODE_RESULT_ACTIVITY_PAYMENT_INVALID_DATE;
//            } else if (msg.equals("PAYMENT_CANCELED"))
//            {
//                resultCode = CODE_RESULT_ACTIVITY_PAYMENT_CANCELED;
//            } else if (msg.equals("ACCOUNT_READY"))
//            {
//                resultCode = CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_READY;
//            } else if (msg.equals("ACCOUNT_TIME_ERROR"))
//            {
//                resultCode = CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_TIME_ERROR;
//            } else if (msg.equals("ACCOUNT_DUPLICATE"))
//            {
//                resultCode = CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_DUPLICATE;
//            } else if (msg.equals("NOT_AVAILABLE"))
//            {
//                resultCode = CODE_RESULT_ACTIVITY_PAYMENT_NOT_AVAILABLE;
//            } else if (msg.equals("PAYMENT_TIMEOVER"))
//            {
//                resultCode = CODE_RESULT_ACTIVITY_PAYMENT_TIMEOVER;
//            } else
//            {
//                resultCode = CODE_RESULT_ACTIVITY_PAYMENT_FAIL;
//            }
//
//            Intent intent = new Intent();
//            intent.putExtra(NAME_INTENT_EXTRA_DATA_PAYMENTINFORMATION, mStayPaymentInformation);
//
//            setResult(resultCode, intent);
//            finish();
//        }
//
//        // 서버로부터 받은 결제 결과 메시지를 처리함.
//        // 각각의 경우에 맞는 resultCode를 넣어 BookingActivity로 finish시킴.
//        @JavascriptInterface
//        public void paymentFeed(String result)
//        {
//            Intent intent = new Intent();
//            intent.putExtra(NAME_INTENT_EXTRA_DATA_MESSAGE, result);
//            intent.putExtra(NAME_INTENT_EXTRA_DATA_PAYMENTINFORMATION, mStayPaymentInformation);
//
//            setResult(CODE_RESULT_ACTIVITY_PAYMENT_PRECHECK, intent);
//            finish();
//        }
//    }
//
//    class WebViewPostAsyncTask extends AsyncTask<String, Void, String>
//    {
//        private WebView mWebView;
//        private FormBody.Builder mBuilder;
//        private String mUrl;
//
//        public WebViewPostAsyncTask(WebView webView, FormBody.Builder builder)
//        {
//            mWebView = webView;
//            mBuilder = builder;
//        }
//
//        @Override
//        protected String doInBackground(String... params)
//        {
//            mUrl = params[0];
//
//            try
//            {
//                OkHttpClient okHttpClient = new OkHttpClient();
//                Request request = new Request.Builder()//
//                    .url(mUrl)//
//                    .addHeader("Os-Type", "android")//
//                    .addHeader("App-Version", DailyHotel.VERSION)//
//                    .addHeader("App-VersionCode", DailyHotel.VERSION_CODE)//
//                    .addHeader("Authorization", DailyHotel.AUTHORIZATION)//
//                    .addHeader("ga-id", DailyHotel.GOOGLE_ANALYTICS_CLIENT_ID)//
//                    .post(mBuilder.build()).build();
//
//                Response response = okHttpClient.newCall(request).execute();
//
//                // 세션이 만료된 경우
//                if (response.code() == 401)
//                {
//                    return "401";
//                }
//
//                return response.body().string();
//            } catch (Exception e)
//            {
//                ExLog.d(e.toString());
//            }
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String data)
//        {
//            Intent intent = new Intent();
//            intent.putExtra(NAME_INTENT_EXTRA_DATA_PAYMENTINFORMATION, mStayPaymentInformation);
//
//            if (com.daily.base.util.TextUtils.isTextEmpty(data) == true)
//            {
//                setResult(CODE_RESULT_ACTIVITY_PAYMENT_FAIL, intent);
//                finish();
//                return;
//            } else if ("401".equalsIgnoreCase(data) == true)
//            {
//                setResult(CODE_RESULT_ACTIVITY_PAYMENT_INVALID_SESSION, intent);
//                finish();
//                return;
//            }
//
//            try
//            {
//                mWebView.loadDataWithBaseURL(mUrl, data, "text/html", "utf-8", null);
//            } catch (Exception e)
//            {
//                setResult(CODE_RESULT_ACTIVITY_PAYMENT_FAIL, intent);
//                finish();
//            }
//        }
//    }
//}
