package com.twoheart.dailyhotel.screen.information.terms;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.daily.dailyhotel.view.DailyToolbarView;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.place.base.BaseActivity;
import com.twoheart.dailyhotel.util.Constants;

public class TermsNPolicyActivity extends BaseActivity implements View.OnClickListener
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        overridePendingTransition(R.anim.slide_in_right, R.anim.hold);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_terms_and_policy);

        initToolbar();
        initLayout();
    }

    private void initToolbar()
    {
        DailyToolbarView dailyToolbarView = (DailyToolbarView) findViewById(R.id.toolbarView);
        dailyToolbarView.setTitleText(R.string.frag_terms_and_policy);
        dailyToolbarView.setOnBackClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }

    private void initLayout()
    {
        View termsLayout = findViewById(R.id.termsLayout);
        View personalLayout = findViewById(R.id.personalLayout);
        View locationLayout = findViewById(R.id.locationLayout);
        View youthTermsLayout = findViewById(R.id.youthtermsLayout);
        View licenseLayout = findViewById(R.id.licenseLayout);

        termsLayout.setOnClickListener(this);
        personalLayout.setOnClickListener(this);
        locationLayout.setOnClickListener(this);
        youthTermsLayout.setOnClickListener(this);
        licenseLayout.setOnClickListener(this);

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
    protected void onResume()
    {
        super.onResume();

        unLockUI();
    }

    @Override
    public void finish()
    {
        super.finish();

        overridePendingTransition(R.anim.hold, R.anim.slide_out_right);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        unLockUI();

        switch (requestCode)
        {
            case Constants.CODE_REQUEST_ACTIVITY_TERMS_AND_POLICY:
                if (resultCode == Constants.CODE_RESULT_ACTIVITY_GO_HOME)
                {
                    setResult(Constants.CODE_RESULT_ACTIVITY_GO_HOME);
                    finish();
                }
                break;
        }
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.termsLayout:
            {
                if (isLockUiComponent() == true)
                {
                    return;
                }

                lockUiComponent();

                Intent intent = new Intent(this, TermActivity.class);
                startActivityForResult(intent, Constants.CODE_REQUEST_ACTIVITY_TERMS_AND_POLICY);
                break;
            }

            case R.id.personalLayout:
            {
                if (isLockUiComponent() == true)
                {
                    return;
                }

                lockUiComponent();

                Intent intent = new Intent(this, PrivacyActivity.class);
                startActivityForResult(intent, Constants.CODE_REQUEST_ACTIVITY_TERMS_AND_POLICY);
                break;
            }

            case R.id.locationLayout:
            {
                if (isLockUiComponent() == true)
                {
                    return;
                }

                lockUiComponent();

                Intent intent = new Intent(this, LocationTermsActivity.class);
                startActivityForResult(intent, Constants.CODE_REQUEST_ACTIVITY_TERMS_AND_POLICY);
                break;
            }

            case R.id.youthtermsLayout:
            {
                if (isLockUiComponent() == true)
                {
                    return;
                }

                lockUiComponent();

                Intent intent = new Intent(this, ProtectYouthTermsActivity.class);
                startActivityForResult(intent, Constants.CODE_REQUEST_ACTIVITY_TERMS_AND_POLICY);
                break;
            }

            case R.id.licenseLayout:
            {
                if (isLockUiComponent() == true)
                {
                    return;
                }

                lockUiComponent();

                Intent intent = new Intent(this, LicenseActivity.class);
                startActivityForResult(intent, Constants.CODE_REQUEST_ACTIVITY_TERMS_AND_POLICY);
                break;
            }
        }
    }
}
