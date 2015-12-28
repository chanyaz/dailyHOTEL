package com.twoheart.dailyhotel.activity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.twoheart.dailyhotel.LauncherActivity;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.util.AnalyticsManager;
import com.twoheart.dailyhotel.util.AnalyticsManager.Screen;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.Util;

public class EventWebActivity extends WebViewActivity implements Constants
{
    private WebView mWebView;

    public static Intent newInstance(Context context, String url)
    {
        if (Util.isTextEmpty(url) == true)
        {
            return null;
        }

        Intent intent = new Intent(context, EventWebActivity.class);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_URL, url);

        return intent;
    }

    @JavascriptInterface
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        String url = intent.getStringExtra(NAME_INTENT_EXTRA_DATA_URL);

        if (Util.isTextEmpty(url) == true)
        {
            finish();
            return;
        }

        setContentView(R.layout.activity_event_web);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        initToolbar(toolbar, getString(R.string.actionbar_title_event_list_frag));

        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.getSettings().setAppCacheEnabled(false); // 7.4 캐시 정책 비활성화.
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.setAcceptThirdPartyCookies(mWebView, true);
        }

        // 추가
        mWebView.addJavascriptInterface(new JavaScriptExtention(), "android");
        mWebView.clearCache(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        setWebView(url);
    }

    @Override
    protected void onStart()
    {
        AnalyticsManager.getInstance(EventWebActivity.this).recordScreen(Screen.EVENT_WEB);
        super.onStart();
    }

    /**
     * JavaScript
     *
     * @author Dailier
     */
    private class JavaScriptExtention
    {
        @JavascriptInterface
        public void externalLink(String packageName, String uri)
        {
            Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
            marketLaunch.setData(Uri.parse(uri));

            if (marketLaunch.resolveActivity(getPackageManager()) == null)
            {
                String marketUrl;

                if (RELEASE_STORE == Stores.PLAY_STORE || RELEASE_STORE == Stores.N_STORE)
                {
                    marketUrl = String.format("https://play.google.com/store/apps/details?id=%s", packageName);
                    marketLaunch.setData(Uri.parse(marketUrl));
                }
            }

            try
            {
                startActivity(marketLaunch);
            } catch (ActivityNotFoundException e)
            {

            }
        }

        @JavascriptInterface
        public void interlLink(String uri)
        {
            Intent intent = new Intent(EventWebActivity.this, LauncherActivity.class);
            intent.setData(Uri.parse(uri));

            startActivity(intent);
        }

        @JavascriptInterface
        public void feed(String message)
        {
            if (isFinishing() == true)
            {
                return;
            }

            showSimpleDialog(getString(R.string.dialog_notice2), message, getString(R.string.dialog_btn_text_confirm), new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    finish();
                }
            });
        }
    }
}
