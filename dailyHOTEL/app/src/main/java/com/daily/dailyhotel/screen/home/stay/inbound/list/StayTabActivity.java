package com.daily.dailyhotel.screen.home.stay.inbound.list;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.daily.base.BaseActivity;
import com.daily.base.util.DailyTextUtils;
import com.twoheart.dailyhotel.R;

/**
 * Created by sheldon
 * Clean Architecture
 */
public class StayTabActivity extends BaseActivity<StayTabPresenter>
{
    static final int REQUEST_CODE_CALENDAR = 10000;
    static final int REQUEST_CODE_FILTER = 10001;
    static final int REQUEST_CODE_PERMISSION_MANAGER = 10002;
    static final int REQUEST_CODE_SETTING_LOCATION = 10003;
    static final int REQUEST_CODE_PREVIEW = 10004;
    static final int REQUEST_CODE_WISH_DIALOG = 10005;
    static final int REQUEST_CODE_SEARCH = 10006;
    static final int REQUEST_CODE_SEARCH_RESULT = 10007;
    static final int REQUEST_CODE_REGION = 10008;

    public static Intent newInstance(Context context, String deepLink)
    {
        Intent intent = new Intent(context, StayTabActivity.class);

        if (DailyTextUtils.isTextEmpty(deepLink) == false)
        {
            intent.putExtra(INTENT_EXTRA_DATA_DEEPLINK, deepLink);
        }

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        overridePendingTransition(R.anim.slide_in_right, R.anim.hold);

        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    protected StayTabPresenter createInstancePresenter()
    {
        return new StayTabPresenter(this);
    }

    @Override
    public void finish()
    {
        super.finish();

        overridePendingTransition(R.anim.hold, R.anim.slide_out_right);
    }
}
