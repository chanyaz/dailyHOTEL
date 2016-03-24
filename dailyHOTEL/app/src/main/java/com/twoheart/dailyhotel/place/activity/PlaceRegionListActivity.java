package com.twoheart.dailyhotel.place.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.view.View;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Province;
import com.twoheart.dailyhotel.model.RegionViewItem;
import com.twoheart.dailyhotel.screen.common.BaseActivity;
import com.twoheart.dailyhotel.view.widget.DailyToolbarLayout;

import java.util.List;

public abstract class PlaceRegionListActivity extends BaseActivity
{
    protected abstract void initPrepare();

    protected abstract void initIntent(Intent intent);

    protected abstract void initTabLayout(TabLayout tabLayout);

    protected abstract void initViewPager(TabLayout tabLayout);

    protected abstract void showSearch();

    protected abstract void requestRegionList();

    public interface OnUserActionListener
    {
        void onRegionClick(Province province);
    }

    public interface OnResponsePresenterListener
    {
        void onRegionListResponse(List<RegionViewItem> domesticList, List<RegionViewItem> globalList);

        void onInternalError();

        void onInternalError(String message);
    }

    public enum Region
    {
        DOMESTIC,
        GLOBAL
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_region_list);

        initPrepare();

        initIntent(getIntent());

        // 지역로딩시에 백버튼 누르면 종료되도록 수정
        setLockUICancelable(true);
        initLayout();
    }

    protected void initLayout()
    {
        initToolbar();

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        initTabLayout(tabLayout);
        initViewPager(tabLayout);
    }

    private void initToolbar()
    {
        View toolbar = findViewById(R.id.toolbar);

        DailyToolbarLayout dailyToolbarLayout = new DailyToolbarLayout(this, toolbar);
        dailyToolbarLayout.initToolbar(getString(R.string.label_selectarea_area));
        dailyToolbarLayout.setToolbarMenu(R.drawable.navibar_ic_search_black, -1);
        dailyToolbarLayout.setToolbarMenuClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showSearch();
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        lockUI();

        requestRegionList();
    }

    @Override
    public void onBackPressed()
    {
        setResult(RESULT_CANCELED);

        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case CODE_REQUEST_ACTIVITY_SEARCH:
            {
                if (resultCode == Activity.RESULT_OK || resultCode == CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_READY)
                {
                    setResult(resultCode);
                    finish();
                }
                break;
            }
        }
    }
}