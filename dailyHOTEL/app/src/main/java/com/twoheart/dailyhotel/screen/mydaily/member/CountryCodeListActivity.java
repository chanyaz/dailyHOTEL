/**
 * Copyright (c) 2014 Daily Co., Ltd. All rights reserved.
 * <p>
 * CreditListFragment (적립금 내역 화면)
 * <p>
 * 적립금 내역 리스트를 보여주는 화면이다.
 *
 * @version 1
 * @author Mike Han(mike@dailyhotel.co.kr)
 * @since 2014-02-24
 */
package com.twoheart.dailyhotel.screen.mydaily.member;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.daily.dailyhotel.view.DailyToolbarView;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.place.base.BaseActivity;
import com.twoheart.dailyhotel.util.CountryCodeNumber;

public class CountryCodeListActivity extends BaseActivity
{
    public static final String INTENT_EXTRA_COUNTRY_CODE = "countryCode";

    public interface OnUserActionListener
    {
        void selectCountry(String[] country);
    }

    public static Intent newInstance(Context context, String selectedCountryCode)
    {
        Intent intent = new Intent(context, CountryCodeListActivity.class);
        intent.putExtra(INTENT_EXTRA_COUNTRY_CODE, selectedCountryCode);

        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        overridePendingTransition(R.anim.slide_in_right, R.anim.hold);

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        if (intent != null)
        {
            String countryCode = intent.getStringExtra(INTENT_EXTRA_COUNTRY_CODE);

            initLayout(countryCode);
        } else
        {
            finish();
        }
    }

    private void initLayout(String countryCode)
    {
        CountryCodeListLayout countryCodeListLayout = new CountryCodeListLayout(this);
        setContentView(countryCodeListLayout.createView());

        initToolbar();

        countryCodeListLayout.setOnUserActionListener(mOnUserActionListener);

        CountryCodeNumber countryCodeNumber = new CountryCodeNumber();
        countryCodeListLayout.setData(countryCodeNumber.getCountryValue(), countryCode);
    }

    private void initToolbar()
    {
        DailyToolbarView dailyToolbarView = findViewById(R.id.toolbarView);
        dailyToolbarView.setTitleText(R.string.label_select_country);
        dailyToolbarView.setOnBackClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }

    @Override
    public void finish()
    {
        super.finish();

        overridePendingTransition(R.anim.hold, R.anim.slide_out_right);
    }

    private OnUserActionListener mOnUserActionListener = new OnUserActionListener()
    {
        @Override
        public void selectCountry(String[] country)
        {
            Intent intent = new Intent();
            intent.putExtra(INTENT_EXTRA_COUNTRY_CODE, country[0] + "\n" + country[1]);
            setResult(RESULT_OK, intent);
            finish();
        }
    };
}
