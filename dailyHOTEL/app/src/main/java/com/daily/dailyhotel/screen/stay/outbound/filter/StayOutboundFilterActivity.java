package com.daily.dailyhotel.screen.stay.outbound.filter;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.daily.base.BaseActivity;
import com.daily.dailyhotel.entity.StayOutboundFilters;
import com.twoheart.dailyhotel.R;

/**
 * Created by sheldon
 * Clean Architecture
 */
public class StayOutboundFilterActivity extends BaseActivity<StayOutboundFilterPresenter>
{
    public static final String INTENT_EXTRA_DATA_SORT = "sort";
    public static final String INTENT_EXTRA_DATA_RATING = "rating";
    public static final String INTENT_EXTRA_DATA_ENABLEDLINES = "enabledLines";

    static final int REQUEST_CODE_STAYOUTBOUND_PERMISSION_MANAGER = 10000;
    static final int REQUEST_CODE_STAYOUTBOUND_SETTING_LOCATION = 10001;

    public static Intent newInstance(Context context, StayOutboundFilters stayOutboundFilters, boolean... enabledLines)
    {
        Intent intent = new Intent(context, StayOutboundFilterActivity.class);

        if (stayOutboundFilters != null)
        {
            intent.putExtra(INTENT_EXTRA_DATA_SORT, stayOutboundFilters.sortType.name());
            intent.putExtra(INTENT_EXTRA_DATA_RATING, stayOutboundFilters.rating);
            intent.putExtra(INTENT_EXTRA_DATA_ENABLEDLINES, enabledLines);
        }

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold);

        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    protected StayOutboundFilterPresenter createInstancePresenter()
    {
        return new StayOutboundFilterPresenter(this);
    }

    @Override
    public void finish()
    {
        super.finish();

        overridePendingTransition(R.anim.hold, R.anim.slide_out_bottom);
    }
}
