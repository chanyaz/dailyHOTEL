package com.twoheart.dailyhotel.screen.information.terms;

import android.os.Bundle;
import android.view.View;

import com.daily.base.widget.DailyWebView;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.screen.common.WebViewActivity;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.Crypto;
import com.twoheart.dailyhotel.util.DailyPreference;
import com.twoheart.dailyhotel.util.DailyRemoteConfigPreference;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;
import com.twoheart.dailyhotel.widget.DailyToolbarLayout;

public class LocationTermsActivity extends WebViewActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_term);
        setWebView(Crypto.getUrlDecoderEx(DailyRemoteConfigPreference.getInstance(this).getKeyRemoteConfigStaticUrlLocation()));

        initToolbar();
        initLayout((DailyWebView) mWebView);
    }

    private void initToolbar()
    {
        View toolbar = findViewById(R.id.toolbar);
        DailyToolbarLayout dailyToolbarLayout = new DailyToolbarLayout(this, toolbar);
        dailyToolbarLayout.initToolbar(getString(R.string.actionbar_title_locationterms_activity), new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }

    private void initLayout(final DailyWebView dailyWebView)
    {
        final View topButtonView = findViewById(R.id.topButtonView);
        topButtonView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                smoothScrollTop(dailyWebView);
            }
        });

        topButtonView.setVisibility(View.GONE);

        dailyWebView.setOnScrollListener(new DailyWebView.OnScrollListener()
        {
            @Override
            public void onScroll(int l, int t, int oldl, int oldt)
            {
                if (t == 0)
                {
                    topButtonView.setVisibility(View.GONE);
                } else
                {
                    topButtonView.setVisibility(View.VISIBLE);
                }
            }
        });

        View homeButtonView = findViewById(R.id.homeButtonView);
        homeButtonView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setResult(Constants.CODE_RESULT_ACTIVITY_GO_HOME);
                finish();
            }
        });
    }

    @Override
    protected void onStart()
    {
        AnalyticsManager.getInstance(this).recordScreen(this, AnalyticsManager.Screen.TERMSOFLOCATION, null);

        super.onStart();
    }
}
