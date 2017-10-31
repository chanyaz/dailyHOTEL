package com.twoheart.dailyhotel.screen.home.category.list;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.SharedElementCallback;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ExLog;
import com.daily.base.widget.DailyToast;
import com.daily.dailyhotel.parcel.analytics.StayDetailAnalyticsParam;
import com.daily.dailyhotel.screen.home.stay.inbound.detail.StayDetailActivity;
import com.daily.dailyhotel.storage.preference.DailyPreference;
import com.daily.dailyhotel.storage.preference.DailyUserPreference;
import com.daily.dailyhotel.view.DailyStayCardView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.twoheart.dailyhotel.DailyHotel;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Area;
import com.twoheart.dailyhotel.model.Category;
import com.twoheart.dailyhotel.model.DailyCategoryType;
import com.twoheart.dailyhotel.model.PlaceCuration;
import com.twoheart.dailyhotel.model.PlaceViewItem;
import com.twoheart.dailyhotel.model.Province;
import com.twoheart.dailyhotel.model.Stay;
import com.twoheart.dailyhotel.model.StayCategoryCuration;
import com.twoheart.dailyhotel.model.StayCurationOption;
import com.twoheart.dailyhotel.model.time.StayBookingDay;
import com.twoheart.dailyhotel.network.model.TodayDateTime;
import com.twoheart.dailyhotel.place.activity.PlaceRegionListActivity;
import com.twoheart.dailyhotel.place.base.BaseActivity;
import com.twoheart.dailyhotel.place.fragment.PlaceListFragment;
import com.twoheart.dailyhotel.place.fragment.PlaceMainActivity;
import com.twoheart.dailyhotel.place.layout.PlaceMainLayout;
import com.twoheart.dailyhotel.place.networkcontroller.PlaceMainNetworkController;
import com.twoheart.dailyhotel.screen.home.category.filter.StayCategoryCurationActivity;
import com.twoheart.dailyhotel.screen.home.category.nearby.StayCategoryNearByActivity;
import com.twoheart.dailyhotel.screen.home.category.region.HomeCategoryRegionListActivity;
import com.twoheart.dailyhotel.screen.hotel.filter.StayCalendarActivity;
import com.twoheart.dailyhotel.screen.hotel.list.StayListAdapter;
import com.twoheart.dailyhotel.screen.hotel.preview.StayPreviewActivity;
import com.twoheart.dailyhotel.screen.search.SearchActivity;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.DailyCalendar;
import com.twoheart.dailyhotel.util.DailyDeepLink;
import com.twoheart.dailyhotel.util.DailyExternalDeepLink;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by android_sam on 2017. 4. 19..
 */
public class StayCategoryTabActivity extends PlaceMainActivity
{
    StayCategoryCuration mStayCategoryCuration;
    DailyCategoryType mDailyCategoryType;
    DailyDeepLink mDailyDeepLink;

    public static Intent newInstance(Context context, DailyCategoryType categoryType, String deepLink)
    {
        Intent intent = new Intent(context, StayCategoryTabActivity.class);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_DAILY_CATEGORY_TYPE, (Parcelable) categoryType);

        if (DailyTextUtils.isTextEmpty(deepLink) == false)
        {
            intent.putExtra(Constants.NAME_INTENT_EXTRA_DATA_DEEPLINK, deepLink);
        }

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // category 정보를 layout 넘기기 위해 먼저 진행 되어야 함
        Intent intent = getIntent();
        initIntent(intent);

        super.onCreate(savedInstanceState);

        mStayCategoryCuration = new StayCategoryCuration();

        if (mDailyCategoryType == null //
            || DailyCategoryType.STAY_NEARBY == mDailyCategoryType //
            || DailyCategoryType.NONE == mDailyCategoryType)
        {
            Util.restartApp(this);
            return;
        }

        String name = getResources().getString(mDailyCategoryType.getNameResId());
        String code = getResources().getString(mDailyCategoryType.getCodeResId());
        mStayCategoryCuration.setCategory(new Category(name, code));

        initDeepLink(intent);
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);

        initDeepLink(intent);
    }

    private void initIntent(Intent intent)
    {
        if (intent == null)
        {
            return;
        }

        mDailyCategoryType = intent.getParcelableExtra(NAME_INTENT_EXTRA_DATA_DAILY_CATEGORY_TYPE);

        if (mDailyCategoryType == null)
        {
            mDailyCategoryType = DailyCategoryType.NONE;
        }
    }

    private void initDeepLink(Intent intent)
    {
        if (intent == null || intent.hasExtra(Constants.NAME_INTENT_EXTRA_DATA_DEEPLINK) == false)
        {
            return;
        }

        try
        {
            mDailyDeepLink = DailyDeepLink.getNewInstance(Uri.parse(intent.getStringExtra(Constants.NAME_INTENT_EXTRA_DATA_DEEPLINK)));
        } catch (Exception e)
        {
            mDailyDeepLink = null;
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        String label = StayCategoryTabActivity.this.getResources().getString(mDailyCategoryType.getCodeResId());

        AnalyticsManager.getInstance(StayCategoryTabActivity.this).recordEvent( //
            AnalyticsManager.Category.NAVIGATION, AnalyticsManager.Action.STAY_BACK_BUTTON_CLICK, label, null);
    }

    @Override
    protected PlaceMainLayout getPlaceMainLayout(Context context)
    {
        String titleText;
        try
        {
            titleText = context.getResources().getString(mDailyCategoryType.getNameResId());
        } catch (Exception e)
        {
            titleText = "";
        }

        return new StayCategoryTabLayout(this, titleText, mDailyCategoryType, mOnEventListener);
    }

    @Override
    protected PlaceMainNetworkController getPlaceMainNetworkController(Context context)
    {
        return new StayCategoryTabNetworkController(context, mNetworkTag, mOnNetworkControllerListener);
    }

    @Override
    protected void onRegionActivityResult(int resultCode, Intent data)
    {
        // 지역 선택하고 돌아온 경우
        if (resultCode == Activity.RESULT_OK && data != null)
        {
            if (data.hasExtra(NAME_INTENT_EXTRA_DATA_PROVINCE) == true)
            {
                StayCurationOption stayCurationOption = (StayCurationOption) mStayCategoryCuration.getCurationOption();
                stayCurationOption.clear();

                Province province = data.getParcelableExtra(NAME_INTENT_EXTRA_DATA_PROVINCE);
                mStayCategoryCuration.setProvince(province);

                String categoryName = this.getResources().getString(mDailyCategoryType.getNameResId());
                String categoryCode = this.getResources().getString(mDailyCategoryType.getCodeResId());

                Category category = new Category(categoryName, categoryCode);
                // subCategory 의 경우 category 를 preference에 저장 안함
                mStayCategoryCuration.setCategory(category);

                mPlaceMainLayout.setToolbarRegionText(province.name);
                mPlaceMainLayout.setOptionFilterSelected(stayCurationOption.isDefaultFilter() == false);

                // 기존에 설정된 지역과 다른 지역을 선택하면 해당 지역을 저장한다.
                JSONObject savedRegionJsonObject = DailyPreference.getInstance(this).getDailyRegion(mDailyCategoryType);
                JSONObject currentRegionJsonObject = Util.getDailyRegionJSONObject(province);

                if (savedRegionJsonObject == null || savedRegionJsonObject.equals(currentRegionJsonObject) == false)
                {
                    DailyPreference.getInstance(this).setDailyRegion(mDailyCategoryType, currentRegionJsonObject);

                    //                    String country = province.isOverseas ? AnalyticsManager.ValueType.OVERSEAS : AnalyticsManager.ValueType.DOMESTIC;
                    //                    String realProvinceName = Util.getRealProvinceName(province); // 대지역 반환
                    //                    DailyPreference.getInstance(this).setSelectedRegionTypeProvince(PlaceType.HOTEL, realProvinceName);
                    //                    AnalyticsManager.getInstance(this).onRegionChanged(country, realProvinceName);
                }

                StayBookingDay stayBookingDay = mStayCategoryCuration.getStayBookingDay();
                if (stayBookingDay == null)
                {
                    Crashlytics.log("StayCategoryTabActivity :: onRegionActivityResult : stayBookingDay is null , resultCode=" //
                        + resultCode + " , province index=" + province.index + " , category code=" + categoryCode);
                } else if (DailyTextUtils.isTextEmpty(stayBookingDay.getCheckInDay("yyyy-MM-dd")) == true)
                {
                    Crashlytics.log("StayCategoryTabActivity :: onRegionActivityResult : stayBookingDay.getCheckInDay(\"yyyy-MM-dd\") is empty , resultCode=" //
                        + resultCode + " , province index=" + province.index + " , category code=" + categoryCode);
                }

                ArrayList<Category> categoryList = new ArrayList<>();
                categoryList.add(mStayCategoryCuration.getCategory());

                mPlaceMainLayout.setCategoryTabLayout(getSupportFragmentManager(), categoryList, //
                    mStayCategoryCuration.getCategory(), mStayCategoryListFragmentListener);
            }
        } else if (resultCode == RESULT_CHANGED_DATE && data != null)
        {
            // 날짜 선택 화면으로 이동한다.
            if (data.hasExtra(NAME_INTENT_EXTRA_DATA_PROVINCE) == true)
            {
                StayCurationOption stayCurationOption = (StayCurationOption) mStayCategoryCuration.getCurationOption();
                stayCurationOption.clear();

                Province province = data.getParcelableExtra(NAME_INTENT_EXTRA_DATA_PROVINCE);
                StayBookingDay stayBookingDay = data.getParcelableExtra(NAME_INTENT_EXTRA_DATA_PLACEBOOKINGDAY);

                mStayCategoryCuration.setProvince(province);

                String categoryName = this.getResources().getString(mDailyCategoryType.getNameResId());
                String categoryCode = this.getResources().getString(mDailyCategoryType.getCodeResId());

                Category category = new Category(categoryName, categoryCode);
                // subCategory 의 경우 category 를 preference에 저장 안함
                mStayCategoryCuration.setCategory(category);

                mPlaceMainLayout.setToolbarRegionText(province.name);
                mPlaceMainLayout.setOptionFilterSelected(stayCurationOption.isDefaultFilter() == false);

                // 기존에 설정된 지역과 다른 지역을 선택하면 해당 지역을 저장한다.
                JSONObject savedRegionJsonObject = DailyPreference.getInstance(this).getDailyRegion(mDailyCategoryType);
                JSONObject currentRegionJsonObject = Util.getDailyRegionJSONObject(province);

                if (savedRegionJsonObject.equals(currentRegionJsonObject) == false)
                {
                    DailyPreference.getInstance(this).setDailyRegion(mDailyCategoryType, currentRegionJsonObject);

                    //                    String country = province.isOverseas ? AnalyticsManager.ValueType.OVERSEAS : AnalyticsManager.ValueType.DOMESTIC;
                    //                    String realProvinceName = Util.getRealProvinceName(province);
                    //                    DailyPreference.getInstance(this).setSelectedRegionTypeProvince(PlaceType.HOTEL, realProvinceName);
                    //                    AnalyticsManager.getInstance(this).onRegionChanged(country, realProvinceName);
                }

                mStayCategoryCuration.setStayBookingDay(stayBookingDay);

                ((StayCategoryTabLayout) mPlaceMainLayout).setToolbarDateText(stayBookingDay);

                startCalendar(AnalyticsManager.Label.CHANGE_LOCATION, mTodayDateTime);

                ArrayList<Category> categoryList = new ArrayList<>();
                categoryList.add(mStayCategoryCuration.getCategory());

                mPlaceMainLayout.setCategoryTabLayout(getSupportFragmentManager(), categoryList, //
                    mStayCategoryCuration.getCategory(), mStayCategoryListFragmentListener);
            }
        } else if (resultCode == RESULT_ARROUND_SEARCH_LIST && data != null)
        {
            // 검색 결과 화면으로 이동한다.
            String region = data.getStringExtra(NAME_INTENT_EXTRA_DATA_RESULT);
            String callByScreen = AnalyticsManager.Screen.DAILYHOTEL_LIST_REGION_DOMESTIC;

            if (PlaceRegionListActivity.Region.DOMESTIC.name().equalsIgnoreCase(region) == true)
            {
                callByScreen = AnalyticsManager.Screen.DAILYHOTEL_LIST_REGION_DOMESTIC;
            } else if (PlaceRegionListActivity.Region.GLOBAL.name().equalsIgnoreCase(region) == true)
            {
                callByScreen = AnalyticsManager.Screen.DAILYHOTEL_LIST_REGION_GLOBAL;
            }

            startAroundSearchResult(this, mTodayDateTime, mStayCategoryCuration.getStayBookingDay(), null, callByScreen);
        } else if (resultCode == CODE_RESULT_ACTIVITY_GO_HOME)
        {
            setResult(resultCode);
            finish();
        }
    }

    @Override
    protected void onCalendarActivityResult(int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK && data != null)
        {
            StayBookingDay stayBookingDay = data.getParcelableExtra(NAME_INTENT_EXTRA_DATA_PLACEBOOKINGDAY);

            if (stayBookingDay == null)
            {
                return;
            }

            mStayCategoryCuration.setStayBookingDay(stayBookingDay);

            ((StayCategoryTabLayout) mPlaceMainLayout).setToolbarDateText(stayBookingDay);

            refreshCurrentFragment(true);
        }
    }

    @Override
    protected void onCurationActivityResult(int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK && data != null)
        {
            PlaceCuration placeCuration = data.getParcelableExtra(NAME_INTENT_EXTRA_DATA_PLACECURATION);

            if ((placeCuration instanceof StayCategoryCuration) == false)
            {
                return;
            }

            StayCategoryCuration changedStayCuration = (StayCategoryCuration) placeCuration;
            StayCurationOption changedStayCurationOption = (StayCurationOption) changedStayCuration.getCurationOption();

            mStayCategoryCuration.setCurationOption(changedStayCurationOption);
            mPlaceMainLayout.setOptionFilterSelected(changedStayCurationOption.isDefaultFilter() == false);

            if (changedStayCurationOption.getSortType() == SortType.DISTANCE)
            {
                mStayCategoryCuration.setLocation(changedStayCuration.getLocation());

                if (mStayCategoryCuration.getLocation() != null)
                {
                    lockUI();

                    onLocationChanged(mStayCategoryCuration.getLocation());
                } else
                {
                    searchMyLocation();
                }
            } else
            {
                refreshCurrentFragment(true);
            }
        } else if (resultCode == CODE_RESULT_ACTIVITY_GO_HOME)
        {
            setResult(resultCode);
            finish();
        }
    }

    @Override
    protected void onLocationFailed()
    {
        StayCurationOption stayCurationOption = (StayCurationOption) mStayCategoryCuration.getCurationOption();

        stayCurationOption.setSortType(SortType.DEFAULT);
        mPlaceMainLayout.setOptionFilterSelected(stayCurationOption.isDefaultFilter() == false);

        refreshCurrentFragment(true);
    }

    @Override
    protected void onLocationProviderDisabled()
    {
        StayCurationOption stayCurationOption = (StayCurationOption) mStayCategoryCuration.getCurationOption();

        stayCurationOption.setSortType(SortType.DEFAULT);
        mPlaceMainLayout.setOptionFilterSelected(stayCurationOption.isDefaultFilter() == false);

        refreshCurrentFragment(true);
    }

    @Override
    protected void onLocationChanged(Location location)
    {
        StayCurationOption stayCurationOption = (StayCurationOption) mStayCategoryCuration.getCurationOption();

        if (stayCurationOption.getSortType() == SortType.DISTANCE)
        {
            if (location == null)
            {
                // 이전에 가지고 있던 데이터를 사용한다.
                if (mStayCategoryCuration.getLocation() != null)
                {
                    refreshCurrentFragment(true);
                } else
                {
                    DailyToast.showToast(this, R.string.message_failed_mylocation, Toast.LENGTH_SHORT);

                    stayCurationOption.setSortType(SortType.DEFAULT);
                    refreshCurrentFragment(true);
                }
            } else
            {
                mStayCategoryCuration.setLocation(location);
                refreshCurrentFragment(true);
            }
        }
    }

    void startCalendar(String callByScreen, TodayDateTime todayDateTime)
    {
        if (todayDateTime == null || isFinishing() == true || lockUiComponentAndIsLockUiComponent() == true)
        {
            return;
        }

        Intent intent = StayCalendarActivity.newInstance(this, todayDateTime, mStayCategoryCuration.getStayBookingDay() //
            , StayCalendarActivity.DEFAULT_DOMESTIC_CALENDAR_DAY_OF_MAX_COUNT, callByScreen, true, true);

        if (intent == null)
        {
            Util.restartApp(this);
            return;
        }

        startActivityForResult(intent, CODE_REQUEST_ACTIVITY_CALENDAR);

        //        AnalyticsManager.getInstance(this).recordEvent(AnalyticsManager.Category.NAVIGATION_//
        //            , AnalyticsManager.Action.HOTEL_BOOKING_CALENDAR_CLICKED, AnalyticsManager.ValueType.LIST, null);
    }

    private void startAroundSearchResult(Context context, TodayDateTime todayDateTime, StayBookingDay stayBookingDay, Location location, String callByScreen)
    {
        if (todayDateTime == null || stayBookingDay == null || isFinishing() == true || lockUiComponentAndIsLockUiComponent() == true)
        {
            return;
        }

        lockUI();

        Intent intent = StayCategoryNearByActivity.newInstance(context, todayDateTime //
            , stayBookingDay, location, mDailyCategoryType, callByScreen);
        startActivityForResult(intent, CODE_REQUEST_ACTIVITY_SEARCH_RESULT);
    }

    @Override
    protected PlaceCuration getPlaceCuration()
    {
        return mStayCategoryCuration;
    }

    // GA 주석 처리
    void recordAnalyticsStayList(String screen)
    {
        if (AnalyticsManager.Screen.DAILYHOTEL_LIST_MAP.equalsIgnoreCase(screen) == false //
            && AnalyticsManager.Screen.DAILYHOTEL_LIST.equalsIgnoreCase(screen) == false)
        {
            return;
        }

        StayBookingDay stayBookingDay = mStayCategoryCuration.getStayBookingDay();
        Map<String, String> params = new HashMap<>();

        try
        {
            params.put(AnalyticsManager.KeyType.CHECK_IN, stayBookingDay.getCheckInDay("yyyy-MM-dd"));
            params.put(AnalyticsManager.KeyType.CHECK_OUT, stayBookingDay.getCheckOutDay("yyyy-MM-dd"));
            params.put(AnalyticsManager.KeyType.LENGTH_OF_STAY, Integer.toString(stayBookingDay.getNights()));

            if (DailyHotel.isLogin() == false)
            {
                params.put(AnalyticsManager.KeyType.IS_SIGNED, AnalyticsManager.ValueType.GUEST);
                params.put(AnalyticsManager.KeyType.MEMBER_TYPE, AnalyticsManager.ValueType.EMPTY);
            } else
            {
                params.put(AnalyticsManager.KeyType.IS_SIGNED, AnalyticsManager.ValueType.MEMBER);
                switch (DailyUserPreference.getInstance(this).getType())
                {
                    case Constants.DAILY_USER:
                        params.put(AnalyticsManager.KeyType.MEMBER_TYPE, AnalyticsManager.UserType.EMAIL);
                        break;

                    case Constants.KAKAO_USER:
                        params.put(AnalyticsManager.KeyType.MEMBER_TYPE, AnalyticsManager.UserType.KAKAO);
                        break;

                    case Constants.FACEBOOK_USER:
                        params.put(AnalyticsManager.KeyType.MEMBER_TYPE, AnalyticsManager.UserType.FACEBOOK);
                        break;

                    default:
                        params.put(AnalyticsManager.KeyType.MEMBER_TYPE, AnalyticsManager.ValueType.EMPTY);
                        break;
                }
            }

            params.put(AnalyticsManager.KeyType.PLACE_TYPE, AnalyticsManager.ValueType.STAY);
            params.put(AnalyticsManager.KeyType.PLACE_HIT_TYPE, AnalyticsManager.ValueType.STAY);
            params.put(AnalyticsManager.KeyType.CATEGORY, mStayCategoryCuration.getCategory().code);
            params.put(AnalyticsManager.KeyType.FILTER, mStayCategoryCuration.getCurationOption().toAdjustString());
            params.put(AnalyticsManager.KeyType.PUSH_NOTIFICATION, DailyUserPreference.getInstance(this).isBenefitAlarm() ? "on" : "off");

            params.put(AnalyticsManager.KeyType.VIEW_TYPE //
                , AnalyticsManager.Screen.DAILYHOTEL_LIST_MAP.equalsIgnoreCase(screen) == true //
                    ? AnalyticsManager.ValueType.MAP : AnalyticsManager.ValueType.LIST);

            Province province = mStayCategoryCuration.getProvince();

            if (province == null)
            {
                Util.restartApp(this);
                return;
            }

            if (province instanceof Area)
            {
                Area area = (Area) province;
                params.put(AnalyticsManager.KeyType.COUNTRY, area.getProvince().isOverseas ? AnalyticsManager.ValueType.OVERSEAS : AnalyticsManager.ValueType.DOMESTIC);
                params.put(AnalyticsManager.KeyType.PROVINCE, area.getProvince().name);
                params.put(AnalyticsManager.KeyType.DISTRICT, area.name);
            } else
            {
                params.put(AnalyticsManager.KeyType.COUNTRY, province.isOverseas ? AnalyticsManager.ValueType.OVERSEAS : AnalyticsManager.ValueType.DOMESTIC);
                params.put(AnalyticsManager.KeyType.PROVINCE, province.name);
                params.put(AnalyticsManager.KeyType.DISTRICT, AnalyticsManager.ValueType.ALL_LOCALE_KR);
            }

            AnalyticsManager.getInstance(this).recordScreen(this, screen, null, params);
            // 숏컷 리스트 진입용 GA Screen 중복 발송

            String shortcutScreen = getCallByScreen(mDailyCategoryType);
            if (DailyTextUtils.isTextEmpty(shortcutScreen) == false)
            {
                AnalyticsManager.getInstance(this).recordScreen(this, shortcutScreen, null, params);
            }

        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    private String getCallByScreen(DailyCategoryType dailyCategoryType)
    {
        if (dailyCategoryType == null)
        {
            return null;
        }

        String shortcutScreen = "";
        switch (dailyCategoryType)
        {
            case STAY_HOTEL:
                shortcutScreen = AnalyticsManager.Screen.STAY_LIST_SHORTCUT_HOTEL;
                break;
            case STAY_BOUTIQUE:
                shortcutScreen = AnalyticsManager.Screen.STAY_LIST_SHORTCUT_BOUTIQUE;
                break;
            case STAY_PENSION:
                shortcutScreen = AnalyticsManager.Screen.STAY_LIST_SHORTCUT_PENSION;
                break;
            case STAY_RESORT:
                shortcutScreen = AnalyticsManager.Screen.STAY_LIST_SHORTCUT_RESORT;
                break;
            case STAY_NEARBY:
                shortcutScreen = AnalyticsManager.Screen.STAY_LIST_SHORTCUT_NEARBY;
                break;
        }

        return shortcutScreen;
    }

    @Override
    protected void changeViewType()
    {
        if (isFinishing() == true || lockUiComponentAndIsLockUiComponent() == true)
        {
            return;
        }

        if (mPlaceMainLayout.getPlaceListFragment() == null)
        {
            Util.restartApp(StayCategoryTabActivity.this);
            return;
        }

        lockUI();

        StayCategoryListFragment currentFragment = (StayCategoryListFragment) mPlaceMainLayout.getCurrentPlaceListFragment();

        switch (mViewType)
        {
            case LIST:
            {
                // 고메 쪽에서 보여지는 메세지로 Stay의 경우도 동일한 처리가 필요해보여서 추가함
                if (currentFragment.hasSalesPlace() == false)
                {
                    unLockUI();

                    DailyToast.showToast(StayCategoryTabActivity.this, R.string.toast_msg_solodout_area, Toast.LENGTH_SHORT);
                    return;
                }

                mViewType = ViewType.MAP;

                //                AnalyticsManager.getInstance(StaySubCategoryActivity.this).recordEvent(AnalyticsManager.Category.NAVIGATION_, AnalyticsManager.Action.CHANGE_VIEW, AnalyticsManager.Label._HOTEL_MAP, null);
                break;
            }

            case MAP:
            {
                mViewType = ViewType.LIST;

                //                AnalyticsManager.getInstance(StaySubCategoryActivity.this).recordEvent(AnalyticsManager.Category.NAVIGATION_, AnalyticsManager.Action.CHANGE_VIEW, AnalyticsManager.Label._HOTEL_LIST, null);
                break;
            }
        }

        mPlaceMainLayout.setOptionViewTypeView(mViewType);

        // 현재 페이지 선택 상태를 Fragment에게 알려준다.
        for (PlaceListFragment placeListFragment : mPlaceMainLayout.getPlaceListFragment())
        {
            boolean isCurrentFragment = placeListFragment == currentFragment;
            placeListFragment.setVisibility(mViewType, isCurrentFragment);
        }

        refreshCurrentFragment(false);

        unLockUI();
    }

    @Override
    protected void onPlaceDetailClickByLongPress(View view, PlaceViewItem placeViewItem, int listCount)
    {
        if (view == null || placeViewItem == null || mStayCategoryListFragmentListener == null)
        {
            return;
        }

        mStayCategoryListFragmentListener.onStayClick(view, placeViewItem, listCount);
    }

    @Override
    protected void onRegionClick()
    {
        if (isFinishing() == true || lockUiComponentAndIsLockUiComponent() == true)
        {
            return;
        }

        startActivityForResult(HomeCategoryRegionListActivity.newInstance( //
            StayCategoryTabActivity.this, mDailyCategoryType, mStayCategoryCuration.getStayBookingDay()) //
            , Constants.CODE_REQUEST_ACTIVITY_REGIONLIST);

        switch (mViewType)
        {
            case LIST:
                //                    AnalyticsManager.getInstance(StaySubCategoryActivity.this).recordEvent(AnalyticsManager.Category.NAVIGATION_, AnalyticsManager.Action.CHANGE_LOCATION, AnalyticsManager.Label._HOTEL_LIST, null);
                break;

            case MAP:
                //                    AnalyticsManager.getInstance(StaySubCategoryActivity.this).recordEvent(AnalyticsManager.Category.NAVIGATION_, AnalyticsManager.Action.CHANGE_LOCATION, AnalyticsManager.Label._HOTEL_MAP, null);
                break;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Listener
    //////////////////////////////////////////////////////////////////////////////////////////////////////////


    PlaceMainLayout.OnEventListener mOnEventListener = new PlaceMainLayout.OnEventListener()
    {
        @Override
        public void onCategoryTabSelected(TabLayout.Tab tab)
        {
            Category category = (Category) tab.getTag();
            mStayCategoryCuration.setCategory(StayCategoryTabActivity.this, category);

            mPlaceMainLayout.setCurrentItem(tab.getPosition());

            refreshCurrentFragment(false);
        }

        @Override
        public void onCategoryTabUnselected(TabLayout.Tab tab)
        {
            // do nothing!
        }

        @Override
        public void onCategoryTabReselected(TabLayout.Tab tab)
        {
            setScrollListTop();
        }

        @Override
        public void onSearchClick()
        {
            Intent intent = SearchActivity.newInstance(StayCategoryTabActivity.this, PlaceType.HOTEL, mStayCategoryCuration.getStayBookingDay());
            startActivityForResult(intent, CODE_REQUEST_ACTIVITY_SEARCH);

            switch (mViewType)
            {
                case LIST:
                {
                    String label = "";
                    switch (mDailyCategoryType)
                    {
                        case STAY_HOTEL:
                            label = AnalyticsManager.Label.HOTEL_LIST;
                            break;
                        case STAY_BOUTIQUE:
                            label = AnalyticsManager.Label.BOUTIQUE_LIST;
                            break;
                        case STAY_PENSION:
                            label = AnalyticsManager.Label.PENSION_LIST;
                            break;
                        case STAY_RESORT:
                            label = AnalyticsManager.Label.RESORT_LIST;
                            break;
                    }

                    AnalyticsManager.getInstance(StayCategoryTabActivity.this).recordEvent(AnalyticsManager.Category.SEARCH//
                        , AnalyticsManager.Action.SEARCH_BUTTON_CLICK, label, null);
                    break;
                }

                case MAP:
                {
                    String label = "";
                    switch (mDailyCategoryType)
                    {
                        case STAY_HOTEL:
                            label = AnalyticsManager.Label.HOTEL_LIST_MAP;
                            break;
                        case STAY_BOUTIQUE:
                            label = AnalyticsManager.Label.BOUTIQUE_LIST_MAP;
                            break;
                        case STAY_PENSION:
                            label = AnalyticsManager.Label.PENSION_LIST_MAP;
                            break;
                        case STAY_RESORT:
                            label = AnalyticsManager.Label.RESORT_LIST_MAP;
                            break;
                    }

                    AnalyticsManager.getInstance(StayCategoryTabActivity.this).recordEvent(AnalyticsManager.Category.SEARCH//
                        , AnalyticsManager.Action.SEARCH_BUTTON_CLICK, label, null);
                    break;
                }
            }
        }

        @Override
        public void onDateClick()
        {
            startCalendar(AnalyticsManager.ValueType.LIST, mTodayDateTime);
        }

        @Override
        public void onRegionClick()
        {
            StayCategoryTabActivity.this.onRegionClick();
        }

        @Override
        public void onViewTypeClick()
        {
            mPlaceMainLayout.showAppBarLayout(false);

            changeViewType();
        }

        @Override
        public void onFilterClick()
        {
            if (isFinishing() == true || lockUiComponentAndIsLockUiComponent() == true)
            {
                return;
            }

            Province province = mStayCategoryCuration.getProvince();

            if (province == null)
            {
                releaseUiComponent();
                return;
            }

            Intent intent = StayCategoryCurationActivity.newInstance(StayCategoryTabActivity.this, mViewType, mStayCategoryCuration);
            startActivityForResult(intent, CODE_REQUEST_ACTIVITY_STAYCURATION);

            //            String viewType = AnalyticsManager.Label.VIEWTYPE_LIST;
            //
            //            switch (mViewType)
            //            {
            //                case LIST:
            //                    viewType = AnalyticsManager.Label.VIEWTYPE_LIST;
            //                    break;
            //
            //                case MAP:
            //                    viewType = AnalyticsManager.Label.VIEWTYPE_MAP;
            //                    break;
            //            }
            //
            //            AnalyticsManager.getInstance(StaySubCategoryActivity.this).recordEvent(AnalyticsManager.Category.NAVIGATION_//
            //                , AnalyticsManager.Action.HOTEL_SORT_FILTER_BUTTON_CLICKED, viewType, null);
        }

        @Override
        public void onPageScroll()
        {

        }

        @Override
        public void onPageSelected(int changedPosition, int prevPosition)
        {

        }

        @Override
        public void finish()
        {
            StayCategoryTabActivity.this.finish();
        }
    };

    private PlaceMainNetworkController.OnNetworkControllerListener mOnNetworkControllerListener //
        = new PlaceMainNetworkController.OnNetworkControllerListener()
    {
        @Override
        public void onDateTime(TodayDateTime todayDateTime)
        {
            if (isFinishing() == true)
            {
                return;
            }

            mTodayDateTime = todayDateTime;

            try
            {
                StayBookingDay stayBookingDay = mStayCategoryCuration.getStayBookingDay();

                // 체크인 시간이 설정되어 있지 않는 경우 기본값을 넣어준다.
                if (stayBookingDay == null)
                {
                    stayBookingDay = new StayBookingDay();

                    stayBookingDay.setCheckInDay(mTodayDateTime.dailyDateTime);
                    stayBookingDay.setCheckOutDay(mTodayDateTime.dailyDateTime, 1);

                } else
                {
                    // 예외 처리로 보고 있는 체크인/체크아웃 날짜가 지나 간경우 다음 날로 변경해준다.
                    // 체크인 날짜 체크

                    // 날짜로 비교해야 한다.
                    Calendar todayCalendar = DailyCalendar.getInstance(mTodayDateTime.dailyDateTime, true);
                    Calendar checkInCalendar = DailyCalendar.getInstance(stayBookingDay.getCheckInDay(DailyCalendar.ISO_8601_FORMAT), true);
                    Calendar checkOutCalendar = DailyCalendar.getInstance(stayBookingDay.getCheckOutDay(DailyCalendar.ISO_8601_FORMAT), true);

                    // 하루가 지나서 체크인 날짜가 전날짜 인 경우
                    if (todayCalendar.getTimeInMillis() > checkInCalendar.getTimeInMillis())
                    {
                        stayBookingDay.setCheckInDay(mTodayDateTime.dailyDateTime);

                        checkInCalendar = DailyCalendar.getInstance(stayBookingDay.getCheckInDay(DailyCalendar.ISO_8601_FORMAT), true);
                    }

                    // 체크인 날짜가 체크 아웃 날짜와 같거나 큰경우.
                    if (checkInCalendar.getTimeInMillis() >= checkOutCalendar.getTimeInMillis())
                    {
                        stayBookingDay.setCheckOutDay(stayBookingDay.getCheckInDay(DailyCalendar.ISO_8601_FORMAT), 1);
                    }
                }

                mStayCategoryCuration.setStayBookingDay(stayBookingDay);

                ((StayCategoryTabLayout) mPlaceMainLayout).setToolbarDateText(mStayCategoryCuration.getStayBookingDay());

                mPlaceMainNetworkController.requestRegionList(mDailyCategoryType.getCodeString(StayCategoryTabActivity.this));

            } catch (Exception e)
            {
                onError(e);
                unLockUI();
            }
        }

        @Override
        public void onRegionList(List<Province> provinceList, List<Area> areaList)
        {
            if (isFinishing() == true || provinceList == null || areaList == null)
            {
                return;
            }

            Province selectedProvince = mStayCategoryCuration.getProvince();

            if (selectedProvince == null)
            {
                selectedProvince = searchLastRegion(StayCategoryTabActivity.this, provinceList, areaList);
            }

            // 여러가지 방식으로 지역을 검색했지만 찾지 못하는 경우.
            if (selectedProvince == null)
            {
                selectedProvince = provinceList.get(0);
            }

            //                String country = selectedProvince.isOverseas ? AnalyticsManager.ValueType.OVERSEAS : AnalyticsManager.ValueType.DOMESTIC;
            //                AnalyticsManager.getInstance(StaySubCategoryActivity.this).onRegionChanged(country, selectedProvince.name);

            DailyPreference.getInstance(StayCategoryTabActivity.this).setDailyRegion(mDailyCategoryType, Util.getDailyRegionJSONObject(selectedProvince));

            mStayCategoryCuration.setProvince(selectedProvince);

            if (mDailyDeepLink != null && mDailyDeepLink.isValidateLink() == true //
                && processDeepLinkByRegionList(StayCategoryTabActivity.this, provinceList, areaList, mTodayDateTime, mDailyDeepLink) == true)
            {

            } else
            {
                ArrayList<Category> categoryList = new ArrayList<>();
                categoryList.add(mStayCategoryCuration.getCategory());

                mPlaceMainLayout.setToolbarRegionText(selectedProvince.name);
                mPlaceMainLayout.setCategoryTabLayout(getSupportFragmentManager(), categoryList, //
                    mStayCategoryCuration.getCategory(), mStayCategoryListFragmentListener);
            }
        }

        @Override
        public void onError(Call call, Throwable e, boolean onlyReport)
        {
            StayCategoryTabActivity.this.onError(call, e, onlyReport);
        }

        @Override
        public void onError(Throwable e)
        {
            StayCategoryTabActivity.this.onError(e);
        }

        @Override
        public void onErrorPopupMessage(int msgCode, String message)
        {
            StayCategoryTabActivity.this.onErrorPopupMessage(msgCode, message);
        }

        @Override
        public void onErrorToastMessage(String message)
        {
            StayCategoryTabActivity.this.onErrorToastMessage(message);
        }

        @Override
        public void onErrorResponse(Call call, Response response)
        {
            StayCategoryTabActivity.this.onErrorResponse(call, response);
        }

        private boolean processDeepLinkByRegionList(BaseActivity baseActivity //
            , List<Province> provinceList, List<Area> areaList, TodayDateTime todayDateTime //
            , DailyDeepLink dailyDeepLink)
        {
            if (dailyDeepLink == null)
            {
                return false;
            }

            if (dailyDeepLink.isExternalDeepLink() == true)
            {
                DailyExternalDeepLink externalDeepLink = (DailyExternalDeepLink) dailyDeepLink;

                if (externalDeepLink.isShortcutView() == true)
                {
                    unLockUI();

                    return moveDeepLinkShortcutList(provinceList, areaList, todayDateTime, externalDeepLink);
                } else
                {
                    externalDeepLink.clear();
                }
            } else
            {

            }

            return false;
        }

        private Province searchLastRegion(BaseActivity baseActivity, //
                                          List<Province> provinceList, //
                                          List<Area> areaList)
        {
            // 마지막으로 선택한 지역을 가져온다.
            JSONObject lastRegionJsonObject = DailyPreference.getInstance(baseActivity).getDailyRegion(mDailyCategoryType);
            String lastProvinceName = Util.getDailyProvinceString(lastRegionJsonObject);
            String lastAreaName = Util.getDailyAreaString(lastRegionJsonObject);

            if (DailyTextUtils.isTextEmpty(lastProvinceName) == true)
            {
                return null;
            }

            if (DailyTextUtils.isTextEmpty(lastAreaName) == false)
            {
                for (Area area : areaList)
                {
                    if (area.name.equals(lastAreaName) == true)
                    {
                        for (Province province : provinceList)
                        {
                            if (area.getProvinceIndex() == province.index)
                            {
                                area.isOverseas = province.isOverseas;
                                area.setProvince(province);
                                break;
                            }
                        }

                        return area;
                    }
                }
            }

            for (Province province : provinceList)
            {
                if (province.name.equals(lastProvinceName) == true)
                {
                    return province;
                }
            }

            return null;
        }
    };

    StayCategoryListFragment.OnStayListFragmentListener mStayCategoryListFragmentListener = new StayCategoryListFragment.OnStayListFragmentListener()
    {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onStayClick(View view, PlaceViewItem placeViewItem, int listCount)
        {
            if (isFinishing() == true || placeViewItem == null || lockUiComponentAndIsLockUiComponent() == true)
            {
                return;
            }

            switch (placeViewItem.mType)
            {
                case PlaceViewItem.TYPE_ENTRY:
                {
                    Stay stay = placeViewItem.getItem();
                    Province province = mStayCategoryCuration.getProvince();

                    JSONObject lastRegionObject = DailyPreference.getInstance(StayCategoryTabActivity.this).getDailyRegion(mDailyCategoryType);
                    boolean isSameProvince = Util.isSameProvinceName(province, lastRegionObject);

                    if (isSameProvince == false)
                    {
                        DailyPreference.getInstance(StayCategoryTabActivity.this).setDailyRegion(mDailyCategoryType, Util.getDailyRegionJSONObject(province));
                    }

                    StayDetailAnalyticsParam analyticsParam = new StayDetailAnalyticsParam();
                    analyticsParam.setAddressAreaName(stay.addressSummary);
                    analyticsParam.discountPrice = stay.discountPrice;
                    analyticsParam.price = stay.price;
                    analyticsParam.setShowOriginalPriceYn(analyticsParam.price, analyticsParam.discountPrice);
                    analyticsParam.setProvince(province);
                    analyticsParam.entryPosition = stay.entryPosition;
                    analyticsParam.totalListCount = listCount;
                    analyticsParam.isDailyChoice = stay.isDailyChoice;
                    analyticsParam.gradeName = stay.getGrade().getName(StayCategoryTabActivity.this);

                    StayBookingDay stayBookingDay = mStayCategoryCuration.getStayBookingDay();

                    if (Util.isUsedMultiTransition() == true)
                    {
                        setExitSharedElementCallback(new SharedElementCallback()
                        {
                            @Override
                            public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots)
                            {
                                super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);

                                for (View view : sharedElements)
                                {
                                    if (view instanceof SimpleDraweeView)
                                    {
                                        view.setVisibility(View.VISIBLE);
                                        break;
                                    }
                                }
                            }
                        });

                        ActivityOptionsCompat optionsCompat;
                        Intent intent;

                        if (view instanceof DailyStayCardView == true)
                        {
                            optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(StayCategoryTabActivity.this, ((DailyStayCardView) view).getOptionsCompat());

                            intent = StayDetailActivity.newInstance(StayCategoryTabActivity.this //
                                , stay.index, stay.name, stay.imageUrl, stay.discountPrice//
                                , stayBookingDay.getCheckInDay(DailyCalendar.ISO_8601_FORMAT)//
                                , stayBookingDay.getCheckOutDay(DailyCalendar.ISO_8601_FORMAT)//
                                , true, StayDetailActivity.TRANS_GRADIENT_BOTTOM_TYPE_LIST, analyticsParam);
                        } else
                        {
                            View simpleDraweeView = view.findViewById(R.id.imageView);
                            View nameTextView = view.findViewById(R.id.nameTextView);
                            View gradientTopView = view.findViewById(R.id.gradientTopView);
                            View gradientBottomView = view.findViewById(R.id.gradientView);

                            intent = StayDetailActivity.newInstance(StayCategoryTabActivity.this //
                                , stay.index, stay.name, stay.imageUrl, stay.discountPrice//
                                , stayBookingDay.getCheckInDay(DailyCalendar.ISO_8601_FORMAT)//
                                , stayBookingDay.getCheckOutDay(DailyCalendar.ISO_8601_FORMAT)//
                                , true, StayDetailActivity.TRANS_GRADIENT_BOTTOM_TYPE_MAP, analyticsParam);

                            optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(StayCategoryTabActivity.this,//
                                android.support.v4.util.Pair.create(simpleDraweeView, getString(R.string.transition_place_image)),//
                                android.support.v4.util.Pair.create(nameTextView, getString(R.string.transition_place_name)),//
                                android.support.v4.util.Pair.create(gradientTopView, getString(R.string.transition_gradient_top_view)),//
                                android.support.v4.util.Pair.create(gradientBottomView, getString(R.string.transition_gradient_bottom_view)));
                        }

                        startActivityForResult(intent, CODE_REQUEST_ACTIVITY_STAY_DETAIL, optionsCompat.toBundle());
                    } else
                    {
                        Intent intent = StayDetailActivity.newInstance(StayCategoryTabActivity.this //
                            , stay.index, stay.name, stay.imageUrl, stay.discountPrice//
                            , stayBookingDay.getCheckInDay(DailyCalendar.ISO_8601_FORMAT)//
                            , stayBookingDay.getCheckOutDay(DailyCalendar.ISO_8601_FORMAT)//
                            , false, StayDetailActivity.TRANS_GRADIENT_BOTTOM_TYPE_NONE, analyticsParam);

                        startActivityForResult(intent, CODE_REQUEST_ACTIVITY_STAY_DETAIL);

                        overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
                    }

                    if (mViewType == ViewType.LIST)
                    {
                        AnalyticsManager.getInstance(StayCategoryTabActivity.this).recordEvent(AnalyticsManager.Category.NAVIGATION//
                            , AnalyticsManager.Action.STAY_ITEM_CLICK, String.format(Locale.KOREA, "%d_%d", stay.entryPosition, stay.index), null);

                        AnalyticsManager.getInstance(StayCategoryTabActivity.this).recordEvent(AnalyticsManager.Category.NAVIGATION//
                            , AnalyticsManager.Action.STAY_DAILYCHOICE_CLICK, stay.isDailyChoice ? AnalyticsManager.Label.Y : AnalyticsManager.Label.N, null);

                        // 할인 쿠폰이 보이는 경우
                        if (DailyTextUtils.isTextEmpty(stay.couponDiscountText) == false)
                        {
                            AnalyticsManager.getInstance(StayCategoryTabActivity.this).recordEvent(AnalyticsManager.Category.PRODUCT_LIST//
                                , AnalyticsManager.Action.COUPON_STAY, Integer.toString(stay.index), null);
                        }

                        if (stay.reviewCount > 0)
                        {
                            AnalyticsManager.getInstance(StayCategoryTabActivity.this).recordEvent(AnalyticsManager.Category.PRODUCT_LIST//
                                , AnalyticsManager.Action.TRUE_REVIEW_STAY, Integer.toString(stay.index), null);
                        }

                        if (stay.truevr == true)
                        {
                            AnalyticsManager.getInstance(StayCategoryTabActivity.this).recordEvent(AnalyticsManager.Category.NAVIGATION//
                                , AnalyticsManager.Action.STAY_ITEM_CLICK_TRUE_VR, Integer.toString(stay.index), null);
                        }

                        if (stay.isLocalPlus == true)
                        {
                            AnalyticsManager.getInstance(StayCategoryTabActivity.this).recordEvent(AnalyticsManager.Category.NAVIGATION//
                                , AnalyticsManager.Action.STAY_ITEM_CLICK_BOUTIQUE_AD, Integer.toString(stay.index), null);
                        }
                    }
                    break;
                }

                default:
                    unLockUI();
                    break;
            }
        }

        @Override
        public void onStayLongClick(View view, PlaceViewItem placeViewItem, int listCount)
        {
            if (isFinishing() == true || placeViewItem == null || lockUiComponentAndIsLockUiComponent() == true)
            {
                return;
            }

            switch (placeViewItem.mType)
            {
                case PlaceViewItem.TYPE_ENTRY:
                {
                    mPlaceMainLayout.setBlurVisibility(StayCategoryTabActivity.this, true);

                    // 기존 데이터를 백업한다.
                    mViewByLongPress = view;
                    mPlaceViewItemByLongPress = placeViewItem;
                    mListCountByLongPress = listCount;

                    Stay stay = placeViewItem.getItem();
                    Intent intent = StayPreviewActivity.newInstance(StayCategoryTabActivity.this, mStayCategoryCuration.getStayBookingDay(), stay);

                    startActivityForResult(intent, CODE_REQUEST_ACTIVITY_PREVIEW);
                    break;
                }

                default:
                    unLockUI();
                    break;
            }
        }

        @Override
        public void onRegionClick()
        {
            mOnEventListener.onRegionClick();
        }

        @Override
        public void onCalendarClick()
        {
            mOnEventListener.onDateClick();
        }

        @Override
        public void onActivityCreated(PlaceListFragment placeListFragment)
        {
            if (mPlaceMainLayout == null || placeListFragment == null)
            {
                return;
            }

            PlaceListFragment currentPlaceListFragment = mPlaceMainLayout.getCurrentPlaceListFragment();

            if (currentPlaceListFragment == placeListFragment)
            {
                currentPlaceListFragment.setVisibility(mViewType, true);
                currentPlaceListFragment.setPlaceCuration(mStayCategoryCuration);
                currentPlaceListFragment.refreshList(true);
            } else
            {
                placeListFragment.setVisibility(mViewType, false);
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy)
        {
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState)
        {
            switch (newState)
            {
                case RecyclerView.SCROLL_STATE_IDLE:
                {
                    if (recyclerView.computeVerticalScrollOffset() + recyclerView.computeVerticalScrollExtent() >= recyclerView.computeVerticalScrollRange())
                    {
                        StayListAdapter stayListAdapter = (StayListAdapter) recyclerView.getAdapter();

                        if (stayListAdapter != null)
                        {
                            int count = stayListAdapter.getItemCount();

                            if (count == 0)
                            {
                            } else
                            {
                                PlaceViewItem placeViewItem = stayListAdapter.getItem(stayListAdapter.getItemCount() - 1);

                                if (placeViewItem != null && placeViewItem.mType == PlaceViewItem.TYPE_FOOTER_VIEW)
                                {
                                    mPlaceMainLayout.showAppBarLayout(true);
                                    mPlaceMainLayout.showBottomLayout();
                                }
                            }
                        }
                    }
                    break;
                }

                case RecyclerView.SCROLL_STATE_DRAGGING:
                    break;

                case RecyclerView.SCROLL_STATE_SETTLING:
                    break;
            }
        }

        @Override
        public void onShowMenuBar()
        {
            if (mPlaceMainLayout == null)
            {
                return;
            }

            mPlaceMainLayout.showBottomLayout();
        }

        @Override
        public void onBottomOptionVisible(boolean visible)
        {
            if (mPlaceMainLayout == null)
            {
                return;
            }

            mPlaceMainLayout.setBottomOptionVisible(visible);
        }

        @Override
        public void onUpdateFilterEnabled(boolean isShowFilterEnabled)
        {
            if (mPlaceMainLayout == null)
            {
                return;
            }

            mPlaceMainLayout.setOptionFilterEnabled(isShowFilterEnabled);
        }

        @Override
        public void onUpdateViewTypeEnabled(boolean isShowViewTypeEnabled)
        {
            if (mPlaceMainLayout == null)
            {
                return;
            }

            mPlaceMainLayout.setOptionViewTypeEnabled(isShowViewTypeEnabled);
        }

        @Override
        public void onFilterClick()
        {
            mOnEventListener.onFilterClick();
        }

        @Override
        public void onShowActivityEmptyView(boolean isShow)
        {
            if (isShow == true)
            {
                mPlaceMainLayout.hideBottomLayout();
                mPlaceMainLayout.setOptionFilterEnabled(false);
            } else
            {
                mPlaceMainLayout.showBottomLayout();
                mPlaceMainLayout.setOptionFilterEnabled(true);
            }
        }

        @Override
        public void onRecordAnalytics(ViewType viewType)
        {
            try
            {
                if (viewType == ViewType.MAP)
                {
                    recordAnalyticsStayList(AnalyticsManager.Screen.DAILYHOTEL_LIST_MAP);
                } else
                {
                    recordAnalyticsStayList(AnalyticsManager.Screen.DAILYHOTEL_LIST);
                }
            } catch (Exception e)
            {
                // GA 수집시에 메모리 해지 에러는 버린다.
            }
        }

        @Override
        public void onSearchCountUpdate(int searchCount, int searchMaxCount)
        {

        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Deep Link - 해당 경우는 고려하지 않아도 되나 임시로 넣어 둠
    ////////////////////////////////////////////////////////////////////////////////////////////////

    //    boolean moveDeepLinkDetail(BaseActivity baseActivity, TodayDateTime todayDateTime, DailyDeepLink dailyDeepLink)
    //    {
    //        if (dailyDeepLink == null)
    //        {
    //            return false;
    //        }
    //
    //        try
    //        {
    //            if (dailyDeepLink.isExternalDeepLink() == true)
    //            {
    //                DailyExternalDeepLink externalDeepLink = (DailyExternalDeepLink) dailyDeepLink;
    //
    //                // 신규 타입의 화면이동
    //                int hotelIndex = Integer.parseInt(externalDeepLink.getIndex());
    //                int nights = 1;
    //
    //                try
    //                {
    //                    nights = Integer.parseInt(externalDeepLink.getNights());
    //                } catch (Exception e)
    //                {
    //                    ExLog.d(e.toString());
    //                } finally
    //                {
    //                    if (nights <= 0)
    //                    {
    //                        nights = 1;
    //                    }
    //                }
    //
    //                String date = externalDeepLink.getDate();
    //                int datePlus = externalDeepLink.getDatePlus();
    //                boolean isShowCalendar = externalDeepLink.isShowCalendar();
    //                boolean isShowVR = externalDeepLink.isShowVR();
    //                int ticketIndex = externalDeepLink.getOpenTicketIndex();
    //                boolean overseas = externalDeepLink.getIsOverseas();
    //
    //                StayBookingDay stayBookingDay = new StayBookingDay();
    //
    //                if (DailyTextUtils.isTextEmpty(date) == false)
    //                {
    //                    Date checkInDate = DailyCalendar.convertDate(date, "yyyyMMdd", TimeZone.getTimeZone("GMT+09:00"));
    //                    stayBookingDay.setCheckInDay(DailyCalendar.format(checkInDate, DailyCalendar.ISO_8601_FORMAT));
    //                } else if (datePlus >= 0)
    //                {
    //                    stayBookingDay.setCheckInDay(todayDateTime.dailyDateTime, datePlus);
    //                } else
    //                {
    //                    stayBookingDay.setCheckInDay(todayDateTime.dailyDateTime);
    //                }
    //
    //                stayBookingDay.setCheckOutDay(stayBookingDay.getCheckInDay(DailyCalendar.ISO_8601_FORMAT), nights);
    //
    //                mStayCategoryCuration.setStayBookingDay(stayBookingDay);
    //
    //                Intent intent = StayDetailActivity.newInstance(baseActivity, stayBookingDay, overseas, hotelIndex, ticketIndex, isShowCalendar, isShowVR, false);
    //                baseActivity.startActivityForResult(intent, CODE_REQUEST_ACTIVITY_STAY_DETAIL);
    //
    //                overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
    //
    //                mIsDeepLink = true;
    //            } else
    //            {
    //
    //            }
    //        } catch (Exception e)
    //        {
    //            ExLog.e(e.toString());
    //            return false;
    //        } finally
    //        {
    //            dailyDeepLink.clear();
    //        }
    //
    //        return true;
    //    }

    private Province searchDeeLinkRegion(int provinceIndex, int areaIndex, boolean isOverseas, //
                                         List<Province> provinceList, List<Area> areaList)
    {
        if (provinceIndex < 0 && areaIndex < 0)
        {
            return null;
        }

        Province selectedProvince = null;

        try
        {
            if (areaIndex == -1)
            {
                // 전체 지역으로 이동
                for (Province province : provinceList)
                {
                    if (province.index == provinceIndex && province.isOverseas == isOverseas)
                    {
                        selectedProvince = province;
                        break;
                    }
                }
            } else
            {
                // 소지역으로 이동
                for (Area area : areaList)
                {
                    if (area.index == areaIndex)
                    {
                        for (Province province : provinceList)
                        {
                            if (area.getProvinceIndex() == province.index)
                            {
                                area.setProvince(province);
                                break;
                            }
                        }

                        selectedProvince = area;
                        break;
                    }
                }
            }
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }

        return selectedProvince;
    }

    boolean moveDeepLinkShortcutList(List<Province> provinceList, List<Area> areaList//
        , TodayDateTime todayDateTime, DailyDeepLink dailyDeepLink)
    {
        if (dailyDeepLink == null)
        {
            return false;
        }

        try
        {
            if (dailyDeepLink.isExternalDeepLink() == true)
            {
                DailyExternalDeepLink externalDeepLink = (DailyExternalDeepLink) dailyDeepLink;

                String categoryCode = externalDeepLink.getCategoryCode();
                String date = externalDeepLink.getDate();
                int datePlus = externalDeepLink.getDatePlus();

                int nights = 1;
                int provinceIndex;
                int areaIndex;

                try
                {
                    nights = Integer.parseInt(externalDeepLink.getNights());
                } catch (Exception e)
                {
                    ExLog.d(e.toString());
                } finally
                {
                    if (nights <= 0)
                    {
                        nights = 1;
                    }
                }

                try
                {
                    provinceIndex = Integer.parseInt(externalDeepLink.getProvinceIndex());
                } catch (Exception e)
                {
                    provinceIndex = -1;
                }

                try
                {
                    areaIndex = Integer.parseInt(externalDeepLink.getAreaIndex());
                } catch (Exception e)
                {
                    areaIndex = -1;
                }

                // 지역이 있는 경우 지역을 디폴트로 잡아주어야 한다
                Province selectedProvince = searchDeeLinkRegion(provinceIndex, areaIndex, false, provinceList, areaList);

                if (selectedProvince == null)
                {
                    selectedProvince = mStayCategoryCuration.getProvince();
                }

                mStayCategoryCuration.setProvince(selectedProvince);

                DailyPreference.getInstance(StayCategoryTabActivity.this).setDailyRegion(mDailyCategoryType, Util.getDailyRegionJSONObject(selectedProvince));

                mPlaceMainLayout.setToolbarRegionText(selectedProvince.name);

                // 카테고리가 있는 경우 카테고리를 디폴트로 잡아주어야 한다
                if (DailyTextUtils.isTextEmpty(categoryCode) == false)
                {
                    for (Category category : selectedProvince.getCategoryList())
                    {
                        if (category.code.equalsIgnoreCase(categoryCode) == true)
                        {
                            mStayCategoryCuration.setCategory(StayCategoryTabActivity.this, category);
                            break;
                        }
                    }
                }

                StayBookingDay stayBookingDay = new StayBookingDay();

                if (DailyTextUtils.isTextEmpty(date) == false)
                {
                    Date checkInDate = DailyCalendar.convertDate(date, "yyyyMMdd", TimeZone.getTimeZone("GMT+09:00"));
                    stayBookingDay.setCheckInDay(DailyCalendar.format(checkInDate, DailyCalendar.ISO_8601_FORMAT));
                } else if (datePlus >= 0)
                {
                    stayBookingDay.setCheckInDay(todayDateTime.dailyDateTime, datePlus);
                } else
                {
                    stayBookingDay.setCheckInDay(todayDateTime.dailyDateTime);
                }

                stayBookingDay.setCheckOutDay(stayBookingDay.getCheckInDay(DailyCalendar.ISO_8601_FORMAT), nights);

                mStayCategoryCuration.setStayBookingDay(stayBookingDay);

                ((StayCategoryTabLayout) mPlaceMainLayout).setToolbarDateText(stayBookingDay);

                ArrayList<Category> categoryList = new ArrayList<>();
                categoryList.add(mStayCategoryCuration.getCategory());

                mPlaceMainLayout.setToolbarRegionText(selectedProvince.name);
                mPlaceMainLayout.setCategoryTabLayout(getSupportFragmentManager(), categoryList, //
                    mStayCategoryCuration.getCategory(), mStayCategoryListFragmentListener);
            } else
            {

            }
        } catch (Exception e)
        {
            ExLog.e(e.toString());
            return false;
        } finally
        {
            dailyDeepLink.clear();
        }

        return true;
    }
}
