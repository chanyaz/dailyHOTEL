package com.twoheart.dailyhotel.screen.mydaily.recentplace;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Pair;
import android.view.View;
import android.widget.LinearLayout;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.RecentPlaces;
import com.twoheart.dailyhotel.model.SaleTime;
import com.twoheart.dailyhotel.place.base.BaseActivity;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.ExLog;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;
import com.twoheart.dailyhotel.widget.DailyToolbarLayout;
import com.twoheart.dailyhotel.widget.DailyViewPager;
import com.twoheart.dailyhotel.widget.FontManager;

import java.lang.reflect.Field;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by android_sam on 2016. 10. 10..
 */

public class RecentPlacesTabActivity extends BaseActivity
{
    RecentPlaces mAllRecentPlaces;
    ArrayList<Pair<Integer, String>> mRecentStayList;
    ArrayList<Pair<Integer, String>> mRecentGourmetList;

    ArrayList<RecentPlacesListFragment> mFragmentList;

    private RecentStayListFragment mRecentStayListFragment;
    private RecentGourmetListFragment mRecentGourmetListFragment;

    private RecentPlacesFragmentPagerAdapter mPageAdapter;

    private RecentPlacesNetworkController mNetworkController;

    DailyViewPager mViewPager;
    private TabLayout mTabLayout;
    private View mEmptyView;

    private SourceType mSourceType;
    private PlaceType mPlaceType;

    private boolean mDontReloadAtOnResume; // TODO : 타 기능 구현 완료 후 처리 예정

    public enum SourceType
    {
        HOME,
        MYDAILY
    }

    public static Intent newInstance(Context context, SourceType sourceType, PlaceType placeType)
    {
        if (sourceType == null)
        {
            return null;
        }

        Intent intent = new Intent(context, RecentPlacesTabActivity.class);

        if (placeType != null)
        {
            intent.putExtra(Constants.NAME_INTENT_EXTRA_DATA_PLACETYPE, placeType.name());
        }

        intent.putExtra(NAME_INTENT_EXTRA_DATA_TYPE, sourceType.name());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        overridePendingTransition(R.anim.slide_in_right, R.anim.hold);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_recent_places);

        mNetworkController = new RecentPlacesNetworkController(this, mNetworkTag, mOnNetworkControllerListener);

        mAllRecentPlaces = new RecentPlaces(this);

        mRecentStayList = mAllRecentPlaces.getRecentTypeList(PlaceType.HOTEL);
        mRecentGourmetList = mAllRecentPlaces.getRecentTypeList(PlaceType.FNB);

        initIntent(getIntent());

        initLayout();
    }

    @Override
    protected void onResume()
    {
        if (mDontReloadAtOnResume == true)
        {
            mDontReloadAtOnResume = false;

            if (mFragmentList != null && mFragmentList.size() != 0)
            {
                for (RecentPlacesListFragment fragment : mFragmentList)
                {
                    fragment.setDontReload(true);
                }
            }
        } else
        {
            lockUI();
            mNetworkController.requestCommonDateTime();
        }

        super.onResume();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        switch (mSourceType)
        {
            case HOME:
                AnalyticsManager.getInstance(this).recordEvent(AnalyticsManager.Category.NAVIGATION,//
                    AnalyticsManager.Action.RECENTVIEW_BACK_BUTTON_CLICK, AnalyticsManager.Label.HOME, null);
                break;

            case MYDAILY:
                AnalyticsManager.getInstance(this).recordEvent(AnalyticsManager.Category.NAVIGATION,//
                    AnalyticsManager.Action.RECENTVIEW_BACK_BUTTON_CLICK, AnalyticsManager.Label.MYDAILY, null);
                break;
        }
    }

    @Override
    public void finish()
    {
        super.finish();

        overridePendingTransition(R.anim.hold, R.anim.slide_out_right);
    }

    private void initIntent(Intent intent)
    {
        if (intent == null)
        {
            return;
        }

        String placeTypeName = intent.getStringExtra(NAME_INTENT_EXTRA_DATA_PLACETYPE);

        if (Util.isTextEmpty(placeTypeName) == false)
        {
            try
            {
                mPlaceType = PlaceType.valueOf(placeTypeName);
            } catch (Exception e)
            {
                ExLog.d(e.getMessage());
            }
        }

        try
        {
            mSourceType = SourceType.valueOf(intent.getStringExtra(NAME_INTENT_EXTRA_DATA_TYPE));
        } catch (Exception e)
        {
            Util.restartApp(this);
            return;
        }
    }

    private void initLayout()
    {
        initToolbar();
        initTabLayout();

        mEmptyView = findViewById(R.id.emptyLayout);
        mViewPager = (DailyViewPager) findViewById(R.id.viewPager);

        mFragmentList = new ArrayList<>();

        mRecentStayListFragment = new RecentStayListFragment();
        mRecentStayListFragment.setRecentPlaceList(mRecentStayList);
        mRecentStayListFragment.setRecentPlaceListFragmentListener(mRecentPlaceListFragmentListener);

        mFragmentList.add(mRecentStayListFragment);

        mRecentGourmetListFragment = new RecentGourmetListFragment();
        mRecentGourmetListFragment.setRecentPlaceList(mRecentGourmetList);
        mRecentGourmetListFragment.setRecentPlaceListFragmentListener(mRecentPlaceListFragmentListener);

        mFragmentList.add(mRecentGourmetListFragment);

        mPageAdapter = new RecentPlacesFragmentPagerAdapter(getSupportFragmentManager(), mFragmentList);
    }

    private void initToolbar()
    {
        View toolbar = findViewById(R.id.toolbar);
        DailyToolbarLayout dailyToolbarLayout = new DailyToolbarLayout(this, toolbar);
        dailyToolbarLayout.initToolbar(getString(R.string.frag_recent_places), new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }

    protected void initTabLayout()
    {
        mTabLayout = (TabLayout) findViewById(R.id.tabLayout);

        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.label_hotel));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.label_fnb));
        //        mTabLayout.setOnTabSelectedListener(mOnTabSelectedListener);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mTabLayout.getLayoutParams();
        layoutParams.topMargin = 1 - Util.dpToPx(this, 1);

        mTabLayout.setLayoutParams(layoutParams);

        FontManager.apply(mTabLayout, FontManager.getInstance(this).getRegularTypeface());
    }

    void setTabLayout()
    {
        int position = 0;

        if (mPlaceType != null)
        {
            // deeplink type
            if (PlaceType.FNB.equals(mPlaceType) == true)
            {
                position = 1;
            }

            // deeplink 로 인한 처리 후 초기화
            mPlaceType = null;
        } else
        {
            if (isEmptyRecentStayPlace() == true && isEmptyRecentGourmetPlace() == true)
            {
                AnalyticsManager.getInstance(RecentPlacesTabActivity.this).recordScreen(this, AnalyticsManager.Screen.MENU_RECENT_VIEW_EMPTY, null);
            } else if (isEmptyRecentStayPlace() == true)
            {
                position = 1;
            }
        }

        mViewPager.removeAllViews();
        mViewPager.setOffscreenPageLimit(1);

        TabLayout.Tab selectedTab = mTabLayout.getTabAt(position);

        Class reflectionClass = ViewPager.class;

        try
        {
            Field mCurItem = reflectionClass.getDeclaredField("mCurItem");
            mCurItem.setAccessible(true);
            mCurItem.setInt(mViewPager, position);
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }

        if (selectedTab != null)
        {
            selectedTab.select();
        }

        mViewPager.setAdapter(mPageAdapter);
        mViewPager.clearOnPageChangeListeners();
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

        mTabLayout.setOnTabSelectedListener(mOnTabSelectedListener);
        //        mViewPager.setCurrentItem(position);
    }

    private boolean isEmptyRecentStayPlace()
    {
        return mRecentStayList == null || mRecentStayList.size() == 0;
    }

    private boolean isEmptyRecentGourmetPlace()
    {
        return mRecentGourmetList == null || mRecentGourmetList.size() == 0;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (isFinishing() == true)
        {
            return;
        }

        unLockUI();

        switch (requestCode)
        {
            case CODE_REQUEST_ACTIVITY_GOURMET_DETAIL:
            case CODE_REQUEST_ACTIVITY_STAY_DETAIL:
            {
                mDontReloadAtOnResume = true;

                setResult(resultCode);

                if (resultCode == Activity.RESULT_OK || resultCode == CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_READY)
                {
                    finish();
                }
                break;
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private TabLayout.OnTabSelectedListener mOnTabSelectedListener = new TabLayout.OnTabSelectedListener()
    {
        @Override
        public void onTabSelected(TabLayout.Tab tab)
        {
            if (mViewPager != null)
            {
                mViewPager.setCurrentItem(tab.getPosition(), true);
            }

            AnalyticsManager.getInstance(RecentPlacesTabActivity.this).recordEvent(//
                AnalyticsManager.Category.NAVIGATION_, //
                AnalyticsManager.Action.RECENT_VIEW_TAB_CHANGE, //
                tab.getPosition() == 1 ? AnalyticsManager.ValueType.GOURMET : AnalyticsManager.ValueType.HOTEL, null);
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab)
        {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab)
        {

        }
    };

    private RecentPlacesListFragment.OnRecentPlaceListFragmentListener mRecentPlaceListFragmentListener = new RecentPlacesListFragment.OnRecentPlaceListFragmentListener()
    {
        @Override
        public void onDeleteItemClick(Pair<Integer, String> deleteItem)
        {
            if (mAllRecentPlaces == null || mAllRecentPlaces.size() == 0)
            {
                return;
            }

            if (deleteItem != null)
            {
                String serviceType = deleteItem.second;

                if (RecentPlaces.getServiceType(PlaceType.HOTEL).equalsIgnoreCase(serviceType) == true)
                {
                    mRecentStayList.remove(deleteItem);
                } else if (RecentPlaces.getServiceType(PlaceType.FNB).equalsIgnoreCase(serviceType) == true)
                {
                    mRecentGourmetList.remove(deleteItem);
                }
            }

            mAllRecentPlaces.remove(deleteItem);
            mAllRecentPlaces.savePreference();

            if (mAllRecentPlaces.size() == 0)
            {
                AnalyticsManager.getInstance(RecentPlacesTabActivity.this).recordScreen(RecentPlacesTabActivity.this, AnalyticsManager.Screen.MENU_RECENT_VIEW_EMPTY, null);
            }
        }
    };

    private RecentPlacesNetworkController.OnNetworkControllerListener mOnNetworkControllerListener = new RecentPlacesNetworkController.OnNetworkControllerListener()
    {
        @Override
        public void onCommonDateTime(long currentDateTime, long dailyDateTime)
        {
            SaleTime saleTime = new SaleTime();
            saleTime.setCurrentTime(currentDateTime);
            saleTime.setDailyTime(dailyDateTime);
            saleTime.setOffsetDailyDay(0);

            if (mFragmentList != null)
            {
                for (RecentPlacesListFragment fragment : mFragmentList)
                {
                    fragment.setSaleTime(saleTime);
                }
            }

            setTabLayout();
        }

        @Override
        public void onError(Throwable e)
        {
            RecentPlacesTabActivity.this.onError(e);
        }

        @Override
        public void onErrorPopupMessage(int msgCode, String message)
        {
            RecentPlacesTabActivity.this.onErrorPopupMessage(msgCode, message);
        }

        @Override
        public void onErrorToastMessage(String message)
        {
            RecentPlacesTabActivity.this.onErrorToastMessage(message);
            finish();
        }

        @Override
        public void onErrorResponse(Call call, Response response)
        {
            RecentPlacesTabActivity.this.onErrorResponse(call, response);
            finish();
        }
    };
}
