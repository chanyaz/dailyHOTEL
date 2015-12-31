package com.twoheart.dailyhotel.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.network.request.DailyHotelRequest;
import com.twoheart.dailyhotel.util.AnalyticsManager;
import com.twoheart.dailyhotel.util.AnalyticsManager.Screen;
import com.twoheart.dailyhotel.view.widget.DailyToolbarLayout;

public class AboutActivity extends WebViewActivity
{
    private DailyToolbarLayout mDailyToolbarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        initToolbar();
    }

    private void initToolbar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mDailyToolbarLayout = new DailyToolbarLayout(this, toolbar);
        mDailyToolbarLayout.initToolbar(getString(R.string.actionbar_title_about_activity));
    }

    @Override
    protected void onStart()
    {
        AnalyticsManager.getInstance(this).recordScreen(Screen.ABOUT);

        super.onStart();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        setWebView(DailyHotelRequest.getUrlDecoderEx(URL_WEB_ABOUT));
    }
}
