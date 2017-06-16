package com.twoheart.dailyhotel.screen.common;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Toast;

import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ExLog;
import com.daily.base.widget.DailyToast;
import com.twoheart.dailyhotel.DailyHotel;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.place.activity.PlacePaymentWebActivity;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.Crypto;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by android_sam on 2017. 3. 17..
 */

public class StayOutboundPaymentWebActivity extends PlacePaymentWebActivity
{
    private String URL_BASE_STAY_OUTBOUND = Constants.UNENCRYPTED_URL ? "https://dev-silo.dailyhotel.me/" //
        : "MTgkMzYkNTQkNTAkMzIkMzAkOTIkMTQkNjUkMzAkODYkMTUkNzIkNzYkNzYkNzYk$NUFBMDQ4ODcxOEDHE0MkGU2ODY2QzE5QQCTZEERTVUCN0E0RkNBMEZFXRDcxNRTM2RDZE0RUXZGQGELjJBQTRENTA4QHkI3MjcyRgM==$";

    private String URL_WEBAPI_PAYMENT = Constants.UNENCRYPTED_URL ? "outbound/hotels/{hotelId}/room-reservation-payments/{type}/pay"//
        : "MTAwJDUzJDY0JDE1NyQzNSQ0MSQ5NyQxNDUkODEkNTUkMjMkMTIkMTI5JDc2JDkwJDE2NiQ=$Qjc0RkY3QzJEUNkQ2NTkzMENYBMjI5OEUwNUVQGMEI2CMDAzMDFCMzNBOHEUVGMjQzQjAUwNDlGONTMyNjVFMUjg5RGUE4NzU5NDBCRXUI5REFEPMUZGN0QyQUY4NDhDOUIRwRjI0RUFCMDVDRUVCNEVZCRTFBNEUyQkZDIODTZDRkQ0NUY4Q0MzQkQ=$";

    private static final String INTENT_EXTRA_DATA_PLACE_INDEX = "placeIndex";
    private static final String INTENT_EXTRA_DATA_PAY_TYPE = "payType";
    private static final String INTENT_EXTRA_DATA_JSON_STRING = "jsonString";

    private int mPlaceIndex;
    private String mPayType;
    private String mJSONString;

    public static Intent newInstance(Context context, int placeIndex, String payType, String jsonString)
    {
        Intent intent = new Intent(context, StayOutboundPaymentWebActivity.class);

        intent.putExtra(INTENT_EXTRA_DATA_PLACE_INDEX, placeIndex);
        intent.putExtra(INTENT_EXTRA_DATA_PAY_TYPE, payType);
        intent.putExtra(INTENT_EXTRA_DATA_JSON_STRING, jsonString);

        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestPostPaymentWebView(mWebView, mPlaceIndex, mPayType, mJSONString);
    }

    @Override
    public void initIntentData(Intent intent)
    {
        super.initIntentData(intent);

        mPlaceIndex = intent.getIntExtra(INTENT_EXTRA_DATA_PLACE_INDEX, -1);
        mPayType = intent.getStringExtra(INTENT_EXTRA_DATA_PAY_TYPE);
        mJSONString = intent.getStringExtra(INTENT_EXTRA_DATA_JSON_STRING);
    }

    @Override
    protected boolean hasProductList()
    {
        return true;
    }

    @Override
    protected int getProductIndex()
    {
        return mPlaceIndex;
    }

    @Override
    protected String getScreenName()
    {
        return AnalyticsManager.Screen.DAILYHOTEL_PAYMENT_PROCESS;
    }

    protected void requestPostPaymentWebView(WebView webView, int placeIndex, String payType, String jsonString)
    {
        if (DailyTextUtils.isTextEmpty(jsonString) == true)
        {
            return;
        }

        String url;

        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("{hotelId}", Integer.toString(mPlaceIndex));
        urlParams.put("{type}", payType);

        try
        {
            if (Constants.DEBUG == true)
            {

                url = Crypto.getUrlDecoderEx(URL_BASE_STAY_OUTBOUND)//
                    + Crypto.getUrlDecoderEx(URL_WEBAPI_PAYMENT, urlParams);
            } else
            {
                url = Crypto.getUrlDecoderEx(URL_BASE_STAY_OUTBOUND)//
                    + Crypto.getUrlDecoderEx(URL_WEBAPI_PAYMENT, urlParams);
            }

            WebViewPostAsyncTask webViewPostAsyncTask = new WebViewPostAsyncTask(webView, jsonString);
            webViewPostAsyncTask.execute(url);
        } catch (Exception e)
        {
            ExLog.e(e.toString());

            DailyToast.showToast(StayOutboundPaymentWebActivity.this, R.string.toast_msg_failed_to_get_payment_info, Toast.LENGTH_SHORT);
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

    protected class WebViewPostAsyncTask extends AsyncTask<String, Void, String>
    {
        private WebView mWebView;
        private String mJSONString;
        private String mUrl;

        public WebViewPostAsyncTask(WebView webView, String jsonString)
        {
            mWebView = webView;
            mJSONString = jsonString;
        }

        @Override
        protected String doInBackground(String... params)
        {
            mUrl = params[0];

            try
            {
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder()//
                    .url(mUrl)//
                    .addHeader("Os-Type", "android")//
                    .addHeader("App-Version", DailyHotel.VERSION)//
                    .addHeader("App-VersionCode", DailyHotel.VERSION_CODE)//
                    .addHeader("Authorization", DailyHotel.AUTHORIZATION)//
                    .addHeader("ga-id", DailyHotel.GOOGLE_ANALYTICS_CLIENT_ID)//
                    .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), mJSONString)).build();

                Response response = okHttpClient.newCall(request).execute();

                // 세션이 만료된 경우
                if (response.code() == 401)
                {
                    return "401";
                }

                return response.body().string();
            } catch (Exception e)
            {
                ExLog.d(e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String data)
        {
            Intent intent = new Intent();
            intent.putExtra(NAME_INTENT_EXTRA_DATA_PAYMENTINFORMATION, mPlacePaymentInformation);

            if (DailyTextUtils.isTextEmpty(data) == true)
            {
                setResult(CODE_RESULT_ACTIVITY_PAYMENT_FAIL, intent);
                finish();
                return;
            } else if ("401".equalsIgnoreCase(data) == true)
            {
                setResult(CODE_RESULT_ACTIVITY_PAYMENT_INVALID_SESSION, intent);
                finish();
                return;
            }

            if (data.contains("payment_kcp") || data.contains("approval_key"))
            {
                mPgType = PgType.KCP;
            } else if (data.contains("inipay") || data.contains("inicis"))
            {
                mPgType = PgType.INICIS;
            } else
            {
                mPgType = PgType.ETC;
            }

            try
            {
                mWebView.loadDataWithBaseURL(mUrl, data, "text/html", "utf-8", null);
            } catch (Exception e)
            {
                setResult(CODE_RESULT_ACTIVITY_PAYMENT_FAIL, intent);
                finish();
            }
        }
    }
}


