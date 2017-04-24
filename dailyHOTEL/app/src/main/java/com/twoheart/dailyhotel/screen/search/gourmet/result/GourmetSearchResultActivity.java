package com.twoheart.dailyhotel.screen.search.gourmet.result;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ExLog;
import com.daily.base.widget.DailyToast;
import com.google.android.gms.maps.model.LatLng;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Area;
import com.twoheart.dailyhotel.model.Category;
import com.twoheart.dailyhotel.model.Gourmet;
import com.twoheart.dailyhotel.model.GourmetCuration;
import com.twoheart.dailyhotel.model.GourmetCurationOption;
import com.twoheart.dailyhotel.model.GourmetSearchCuration;
import com.twoheart.dailyhotel.model.Keyword;
import com.twoheart.dailyhotel.model.PlaceCuration;
import com.twoheart.dailyhotel.model.PlaceViewItem;
import com.twoheart.dailyhotel.model.Province;
import com.twoheart.dailyhotel.model.time.GourmetBookingDay;
import com.twoheart.dailyhotel.network.model.TodayDateTime;
import com.twoheart.dailyhotel.place.activity.PlaceSearchResultActivity;
import com.twoheart.dailyhotel.place.fragment.PlaceListFragment;
import com.twoheart.dailyhotel.place.layout.PlaceSearchResultLayout;
import com.twoheart.dailyhotel.place.networkcontroller.PlaceSearchResultNetworkController;
import com.twoheart.dailyhotel.screen.gourmet.detail.GourmetDetailActivity;
import com.twoheart.dailyhotel.screen.gourmet.filter.GourmetCalendarActivity;
import com.twoheart.dailyhotel.screen.gourmet.list.GourmetListAdapter;
import com.twoheart.dailyhotel.screen.gourmet.list.GourmetListFragment;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class GourmetSearchResultActivity extends PlaceSearchResultActivity
{
    int mReceiveDataFlag; // 0 연동 전 , 1 데이터 리시브 상태, 2 로그 발송 상태

    String mInputText;
    String mAddress;

    SearchType mSearchType;
    GourmetSearchCuration mGourmetSearchCuration;

    private PlaceSearchResultNetworkController mNetworkController;

    public static Intent newInstance(Context context, TodayDateTime todayDateTime, GourmetBookingDay gourmetBookingDay, String inputText, Keyword keyword, SearchType searchType)
    {
        Intent intent = new Intent(context, GourmetSearchResultActivity.class);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_TODAYDATETIME, todayDateTime);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_PLACEBOOKINGDAY, gourmetBookingDay);
        intent.putExtra(INTENT_EXTRA_DATA_KEYWORD, keyword);
        intent.putExtra(INTENT_EXTRA_DATA_SEARCHTYPE, searchType.name());
        intent.putExtra(INTENT_EXTRA_DATA_INPUTTEXT, inputText);

        return intent;
    }

    public static Intent newInstance(Context context, TodayDateTime todayDateTime, GourmetBookingDay gourmetBookingDay, LatLng latLng, double radius, boolean isDeepLink)
    {
        Intent intent = new Intent(context, GourmetSearchResultActivity.class);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_TODAYDATETIME, todayDateTime);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_PLACEBOOKINGDAY, gourmetBookingDay);
        intent.putExtra(INTENT_EXTRA_DATA_LATLNG, latLng);
        intent.putExtra(INTENT_EXTRA_DATA_RADIUS, radius);
        intent.putExtra(INTENT_EXTRA_DATA_SEARCHTYPE, SearchType.LOCATION.name());
        intent.putExtra(INTENT_EXTRA_DATA_IS_DEEPLINK, isDeepLink);

        return intent;
    }

    public static Intent newInstance(Context context, TodayDateTime todayDateTime, GourmetBookingDay gourmetBookingDay, Keyword keyword, SearchType searchType)
    {
        return newInstance(context, todayDateTime, gourmetBookingDay, null, keyword, searchType);
    }

    public static Intent newInstance(Context context, TodayDateTime todayDateTime, GourmetBookingDay gourmetBookingDay, String text)
    {
        return newInstance(context, todayDateTime, gourmetBookingDay, null, new Keyword(0, text), SearchType.SEARCHES);
    }

    public static Intent newInstance(Context context, TodayDateTime todayDateTime, GourmetBookingDay gourmetBookingDay, Location location, String callByScreen)
    {
        Intent intent = new Intent(context, GourmetSearchResultActivity.class);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_TODAYDATETIME, todayDateTime);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_PLACEBOOKINGDAY, gourmetBookingDay);
        intent.putExtra(INTENT_EXTRA_DATA_LOCATION, location);
        intent.putExtra(INTENT_EXTRA_DATA_SEARCHTYPE, SearchType.LOCATION.name());
        intent.putExtra(INTENT_EXTRA_DATA_CALL_BY_SCREEN, callByScreen);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        lockUI();

        mNetworkController = new PlaceSearchResultNetworkController(this, mNetworkTag, mOnNetworkControllerListener);

        if (mSearchType == SearchType.LOCATION)
        {
            mPlaceSearchResultLayout.setViewTypeVisibility(true);

            mNetworkController.requestAddress(mGourmetSearchCuration.getLocation());
        } else
        {
            mPlaceSearchResultLayout.setViewTypeVisibility(false);
        }

        mPlaceSearchResultLayout.setCategoryTabLayoutVisibility(View.GONE);
        mPlaceSearchResultLayout.processListLayout();
    }

    @Override
    protected PlaceSearchResultLayout getPlaceSearchResultLayout(Context context)
    {
        return new GourmetSearchResultLayout(this, mOnEventListener);
    }

    @Override
    protected void onCalendarActivityResult(int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK && data != null)
        {
            GourmetBookingDay gourmetBookingDay = data.getParcelableExtra(NAME_INTENT_EXTRA_DATA_PLACEBOOKINGDAY);

            if (gourmetBookingDay == null)
            {
                return;
            }

            mGourmetSearchCuration.setGourmetBookingDay(gourmetBookingDay);

            ((GourmetSearchResultLayout) mPlaceSearchResultLayout).setCalendarText(gourmetBookingDay);

            // 날짜가 바뀌면 전체탭으로 이동하고 다시 재로딩.
            mGourmetSearchCuration.getCurationOption().clear();

            mPlaceSearchResultLayout.setOptionFilterSelected(false);
            mPlaceSearchResultLayout.clearCategoryTab();
            mPlaceSearchResultLayout.setCategoryTabLayoutVisibility(View.GONE);
            mPlaceSearchResultLayout.processListLayout();
            mPlaceSearchResultLayout.setCategoryAllTabLayout(getSupportFragmentManager(), mOnGourmetListFragmentListener);
        }
    }

    @Override
    protected void onCurationActivityResult(int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK && data != null)
        {
            PlaceCuration placeCuration = data.getParcelableExtra(NAME_INTENT_EXTRA_DATA_PLACECURATION);

            if ((placeCuration instanceof GourmetCuration) == false)
            {
                return;
            }

            GourmetCuration changedGourmetCuration = (GourmetCuration) placeCuration;
            GourmetCurationOption changedGourmetCurationOption = (GourmetCurationOption) changedGourmetCuration.getCurationOption();

            mGourmetSearchCuration.setCurationOption(changedGourmetCurationOption);
            mPlaceSearchResultLayout.setOptionFilterSelected(changedGourmetCurationOption.isDefaultFilter() == false);

            if (changedGourmetCurationOption.getSortType() == SortType.DISTANCE)
            {
                mGourmetSearchCuration.setLocation(changedGourmetCuration.getLocation());

                searchMyLocation();
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
        GourmetCurationOption gourmetCurationOption = (GourmetCurationOption) mGourmetSearchCuration.getCurationOption();

        gourmetCurationOption.setSortType(SortType.DEFAULT);
        mPlaceSearchResultLayout.setOptionFilterSelected(gourmetCurationOption.isDefaultFilter() == false);

        refreshCurrentFragment(true);
    }

    @Override
    protected void onLocationProviderDisabled()
    {
        GourmetCurationOption gourmetCurationOption = (GourmetCurationOption) mGourmetSearchCuration.getCurationOption();

        gourmetCurationOption.setSortType(SortType.DEFAULT);
        mPlaceSearchResultLayout.setOptionFilterSelected(gourmetCurationOption.isDefaultFilter() == false);

        refreshCurrentFragment(true);
    }

    @Override
    protected void onLocationChanged(Location location)
    {
        if (location == null)
        {
            mGourmetSearchCuration.getCurationOption().setSortType(SortType.DEFAULT);
            refreshCurrentFragment(true);
        } else
        {
            mGourmetSearchCuration.setLocation(location);

            // 만약 sort type이 거리가 아니라면 다른 곳에서 변경 작업이 일어났음으로 갱신하지 않음
            if (mGourmetSearchCuration.getCurationOption().getSortType() == SortType.DISTANCE)
            {
                refreshCurrentFragment(true);
            }
        }
    }

    @Override
    protected void initIntent(Intent intent)
    {
        GourmetBookingDay gourmetBookingDay;

        try
        {
            mTodayDateTime = intent.getParcelableExtra(Constants.NAME_INTENT_EXTRA_DATA_TODAYDATETIME);
            gourmetBookingDay = intent.getParcelableExtra(NAME_INTENT_EXTRA_DATA_PLACEBOOKINGDAY);
        } catch (Exception e)
        {
            Util.restartApp(this);
            return;
        }

        if (gourmetBookingDay == null)
        {
            finish();
            return;
        }

        Location location = null;
        Keyword keyword = null;
        double radius = DEFAULT_SEARCH_RADIUS;
        mGourmetSearchCuration = new GourmetSearchCuration();

        if (intent.hasExtra(INTENT_EXTRA_DATA_KEYWORD) == true)
        {
            keyword = intent.getParcelableExtra(INTENT_EXTRA_DATA_KEYWORD);
        } else if (intent.hasExtra(INTENT_EXTRA_DATA_LOCATION) == true)
        {
            location = intent.getParcelableExtra(INTENT_EXTRA_DATA_LOCATION);

            if (intent.hasExtra(INTENT_EXTRA_DATA_CALL_BY_SCREEN) == true)
            {
                mCallByScreen = intent.getStringExtra(INTENT_EXTRA_DATA_CALL_BY_SCREEN);
            }

            mGourmetSearchCuration.getCurationOption().setDefaultSortType(SortType.DISTANCE);
        } else if (intent.hasExtra(INTENT_EXTRA_DATA_LATLNG) == true)
        {
            LatLng latLng = intent.getParcelableExtra(INTENT_EXTRA_DATA_LATLNG);

            if (intent.hasExtra(INTENT_EXTRA_DATA_RADIUS) == true)
            {
                radius = intent.getDoubleExtra(INTENT_EXTRA_DATA_RADIUS, DEFAULT_SEARCH_RADIUS);
            }

            mIsDeepLink = intent.getBooleanExtra(INTENT_EXTRA_DATA_IS_DEEPLINK, false);

            location = new Location((String) null);
            location.setLatitude(latLng.latitude);
            location.setLongitude(latLng.longitude);

            // 고정 위치로 진입한 경우
            mIsFixedLocation = true;
            mGourmetSearchCuration.getCurationOption().setDefaultSortType(SortType.DISTANCE);
        } else
        {
            finish();
            return;
        }

        mSearchType = SearchType.valueOf(intent.getStringExtra(INTENT_EXTRA_DATA_SEARCHTYPE));
        mInputText = intent.getStringExtra(INTENT_EXTRA_DATA_INPUTTEXT);

        mGourmetSearchCuration.setKeyword(keyword);

        // 내주변 위치 검색으로 시작하는 경우에는 특정 반경과 거리순으로 시작해야한다.
        if (mSearchType == SearchType.LOCATION)
        {
            mGourmetSearchCuration.getCurationOption().setSortType(SortType.DISTANCE);
            mGourmetSearchCuration.setRadius(radius);
        }

        mGourmetSearchCuration.setLocation(location);
        mGourmetSearchCuration.setGourmetBookingDay(gourmetBookingDay);
    }

    @Override
    protected void initLayout()
    {
        if (mGourmetSearchCuration == null)
        {
            finish();
            return;
        }

        if (mSearchType == SearchType.LOCATION)
        {
            mPlaceSearchResultLayout.setToolbarTitle("");

            mPlaceSearchResultLayout.setSpinnerVisible(true);
        } else
        {
            mPlaceSearchResultLayout.setToolbarTitle(mGourmetSearchCuration.getKeyword().name);

            mPlaceSearchResultLayout.setSpinnerVisible(false);
        }

        mPlaceSearchResultLayout.setSelectionSpinner(mGourmetSearchCuration.getRadius());

        ((GourmetSearchResultLayout) mPlaceSearchResultLayout).setCalendarText(mGourmetSearchCuration.getGourmetBookingDay());

        mPlaceSearchResultLayout.setCategoryTabLayout(getSupportFragmentManager(), new ArrayList<Category>(), null, mOnGourmetListFragmentListener);
    }

    @Override
    protected Keyword getKeyword()
    {
        return mGourmetSearchCuration.getKeyword();
    }

    @Override
    protected PlaceCuration getPlaceCuration()
    {
        return mGourmetSearchCuration;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    void recordScreenSearchResult(String screen)
    {
        if (AnalyticsManager.Screen.SEARCH_RESULT.equalsIgnoreCase(screen) == false //
            && AnalyticsManager.Screen.SEARCH_RESULT_EMPTY.equalsIgnoreCase(screen) == false)
        {
            return;
        }

        try
        {
            GourmetBookingDay gourmetBookingDay = mGourmetSearchCuration.getGourmetBookingDay();
            Map<String, String> params = new HashMap<>();

            params.put(AnalyticsManager.KeyType.CHECK_IN, gourmetBookingDay.getVisitDay("yyyy-MM-dd"));

            params.put(AnalyticsManager.KeyType.PLACE_TYPE, AnalyticsManager.ValueType.GOURMET);
            params.put(AnalyticsManager.KeyType.PLACE_HIT_TYPE, AnalyticsManager.ValueType.GOURMET);

            Province province = mGourmetSearchCuration.getProvince();
            if (province instanceof Area)
            {
                Area area = (Area) province;
                params.put(AnalyticsManager.KeyType.COUNTRY, area.getProvince().isOverseas ? AnalyticsManager.ValueType.OVERSEAS : AnalyticsManager.ValueType.DOMESTIC);
                params.put(AnalyticsManager.KeyType.PROVINCE, area.getProvince().name);
                params.put(AnalyticsManager.KeyType.DISTRICT, area.name);
            } else if (province != null)
            {
                params.put(AnalyticsManager.KeyType.COUNTRY, province.isOverseas ? AnalyticsManager.ValueType.OVERSEAS : AnalyticsManager.ValueType.DOMESTIC);
                params.put(AnalyticsManager.KeyType.PROVINCE, province.name);
                params.put(AnalyticsManager.KeyType.DISTRICT, AnalyticsManager.ValueType.ALL_LOCALE_KR);
            }

            AnalyticsManager.getInstance(GourmetSearchResultActivity.this).recordScreen(GourmetSearchResultActivity.this, screen, null, params);
        } catch (Exception e)
        {
        }
    }

    void recordEventSearchResultByLocation(String address, boolean isEmpty, Map<String, String> params)
    {
        if (DailyTextUtils.isTextEmpty(address))
        {
            return;
        }

        try
        {
            String action = null;

            if (AnalyticsManager.Screen.SEARCH_MAIN.equalsIgnoreCase(mCallByScreen) == true)
            {
                action = (isEmpty == true) ? AnalyticsManager.Action.AROUND_SEARCH_NOT_FOUND : AnalyticsManager.Action.AROUND_SEARCH_CLICKED;

                params.put(AnalyticsManager.KeyType.SEARCH_PATH, AnalyticsManager.ValueType.AROUND);
                params.put(AnalyticsManager.KeyType.SEARCH_WORD, address);
                params.put(AnalyticsManager.KeyType.SEARCH_RESULT, address);
            } else if (AnalyticsManager.Screen.DAILYGOURMET_LIST_REGION_DOMESTIC.equalsIgnoreCase(mCallByScreen) == true)
            {
                action = (isEmpty == true) ? AnalyticsManager.Action.AROUND_SEARCH_NOT_FOUND_LOCATIONLIST : AnalyticsManager.Action.AROUND_SEARCH_CLICKED_LOCATIONLIST;
            }

            if (DailyTextUtils.isTextEmpty(action) == false)
            {
                AnalyticsManager.getInstance(this).recordEvent(AnalyticsManager.Category.SEARCH_//
                    , action, address, params);
            }
        } catch (Exception e)
        {
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // mOnEventListener
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private PlaceSearchResultLayout.OnEventListener mOnEventListener = new PlaceSearchResultLayout.OnEventListener()
    {
        @Override
        public void finish()
        {
            GourmetSearchResultActivity.this.finish();
        }

        @Override
        public void onCategoryTabSelected(TabLayout.Tab tab)
        {

        }

        @Override
        public void onCategoryTabUnselected(TabLayout.Tab tab)
        {

        }

        @Override
        public void onCategoryTabReselected(TabLayout.Tab tab)
        {

        }

        @Override
        public void onDateClick()
        {
            if (isFinishing() == true || lockUiComponentAndIsLockUiComponent() == true)
            {
                return;
            }

            GourmetBookingDay gourmetBookingDay = mGourmetSearchCuration.getGourmetBookingDay();

            Intent intent = GourmetCalendarActivity.newInstance(GourmetSearchResultActivity.this, //
                mTodayDateTime, gourmetBookingDay, AnalyticsManager.ValueType.SEARCH_RESULT, true, true);

            startActivityForResult(intent, CODE_REQUEST_ACTIVITY_CALENDAR);

            AnalyticsManager.getInstance(GourmetSearchResultActivity.this).recordEvent( //
                AnalyticsManager.Category.NAVIGATION_, AnalyticsManager.Action.GOURMET_BOOKING_CALENDAR_CLICKED,//
                AnalyticsManager.Label.SEARCH_RESULT_VIEW, null);
        }

        @Override
        public void onViewTypeClick()
        {
            if (isFinishing() == true || isLockUiComponent() == true)
            {
                return;
            }

            lockUI();

            GourmetSearchResultListFragment currentFragment = (GourmetSearchResultListFragment) mPlaceSearchResultLayout.getCurrentPlaceListFragment();

            if (currentFragment == null)
            {
                unLockUI();
                return;
            }

            switch (mViewType)
            {
                case LIST:
                {
                    // 맵리스트 진입시에 솔드아웃은 맵에서 보여주지 않기 때문에 맵으로 진입시에 아무것도 볼수 없다.
                    if (currentFragment.hasSalesPlace() == false)
                    {
                        unLockUI();

                        DailyToast.showToast(GourmetSearchResultActivity.this, R.string.toast_msg_solodout_area, Toast.LENGTH_SHORT);
                        return;
                    }

                    mViewType = ViewType.MAP;

                    AnalyticsManager.getInstance(GourmetSearchResultActivity.this).recordEvent(AnalyticsManager.Category.NAVIGATION_, AnalyticsManager.Action.CHANGE_VIEW, AnalyticsManager.Label._GOURMET_MAP, null);
                    break;
                }

                case MAP:
                {
                    mViewType = ViewType.LIST;

                    AnalyticsManager.getInstance(GourmetSearchResultActivity.this).recordEvent(AnalyticsManager.Category.NAVIGATION_, AnalyticsManager.Action.CHANGE_VIEW, AnalyticsManager.Label._GOURMET_LIST_, null);
                    break;
                }
            }

            // 고메는 리스트를 한번에 받기 때문에 계속 요청할 필요는 없다.
            mPlaceSearchResultLayout.setOptionViewTypeView(mViewType);

            for (PlaceListFragment placeListFragment : mPlaceSearchResultLayout.getPlaceListFragment())
            {
                boolean isCurrentFragment = (placeListFragment == currentFragment);
                placeListFragment.setVisibility(mViewType, isCurrentFragment);

                ((GourmetSearchResultListFragment) placeListFragment).setIsDeepLink(mIsDeepLink);
            }

            refreshCurrentFragment(false);

            unLockUI();
        }

        @Override
        public void onFilterClick()
        {
            if (isFinishing() == true || lockUiComponentAndIsLockUiComponent() == true)
            {
                return;
            }

            Intent intent = GourmetSearchResultCurationActivity.newInstance(GourmetSearchResultActivity.this,//
                mViewType, mSearchType, mGourmetSearchCuration, mIsFixedLocation);
            startActivityForResult(intent, CODE_REQUEST_ACTIVITY_GOURMETCURATION);

            AnalyticsManager.getInstance(GourmetSearchResultActivity.this).recordEvent( //
                AnalyticsManager.Category.NAVIGATION_, AnalyticsManager.Action.GOURMET_SORT_FILTER_BUTTON_CLICKED,//
                AnalyticsManager.Label.SEARCH_RESULT_VIEW, null);
        }

        @Override
        public void finish(int resultCode)
        {
            GourmetSearchResultActivity.this.finish(resultCode);

            if (resultCode == Constants.CODE_RESULT_ACTIVITY_HOME)
            {
                AnalyticsManager.getInstance(GourmetSearchResultActivity.this).recordEvent(AnalyticsManager.Category.SEARCH_//
                    , AnalyticsManager.Action.SEARCH_RESULT_VIEW, AnalyticsManager.Label.CANCEL_, null);
            } else
            {
                AnalyticsManager.getInstance(GourmetSearchResultActivity.this).recordEvent(AnalyticsManager.Category.SEARCH_//
                    , AnalyticsManager.Action.SEARCH_RESULT_VIEW, AnalyticsManager.Label.BACK_BUTTON, null);
            }
        }

        @Override
        public void research(int resultCode)
        {
            GourmetSearchResultActivity.this.finish(resultCode);

            AnalyticsManager.getInstance(GourmetSearchResultActivity.this).recordEvent(AnalyticsManager.Category.SEARCH_//
                , AnalyticsManager.Action.SEARCH_RESULT_VIEW, AnalyticsManager.Label.SEARCH_AGAIN, null);
        }

        @Override
        public void onShowCallDialog()
        {
            showDailyCallDialog(null);

            AnalyticsManager.getInstance(GourmetSearchResultActivity.this).recordEvent(AnalyticsManager.Category.SEARCH_//
                , AnalyticsManager.Action.SEARCH_RESULT_VIEW, AnalyticsManager.Label.CALL, null);
        }

        @Override
        public void onItemSelectedSpinner(double radius)
        {
            if (mGourmetSearchCuration == null)
            {
                return;
            }

            mGourmetSearchCuration.setRadius(radius);
            refreshCurrentFragment(true);
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // mOnNetworkControllerListener
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private PlaceSearchResultNetworkController.OnNetworkControllerListener mOnNetworkControllerListener = new PlaceSearchResultNetworkController.OnNetworkControllerListener()
    {
        @Override
        public void onResponseAddress(String address)
        {
            if (isFinishing() == true)
            {
                return;
            }

            if (SearchType.LOCATION == mSearchType)
            {
                synchronized (GourmetSearchResultActivity.this)
                {
                    if (mReceiveDataFlag == 0)
                    {
                        mReceiveDataFlag = 1;
                    } else if (mReceiveDataFlag == 1)
                    {
                        ArrayList<PlaceListFragment> placeListFragmentList = mPlaceSearchResultLayout.getPlaceListFragment();
                        if (placeListFragmentList != null || placeListFragmentList.size() > 0)
                        {
                            GourmetBookingDay gourmetBookingDay = mGourmetSearchCuration.getGourmetBookingDay();
                            Map<String, String> params = new HashMap<>();

                            try
                            {
                                params.put(AnalyticsManager.KeyType.CHECK_IN, gourmetBookingDay.getVisitDay("yyyy-MM-dd"));

                                params.put(AnalyticsManager.KeyType.PLACE_TYPE, AnalyticsManager.ValueType.GOURMET);
                                params.put(AnalyticsManager.KeyType.PLACE_HIT_TYPE, AnalyticsManager.ValueType.GOURMET);

                                Province province = mGourmetSearchCuration.getProvince();
                                if (province instanceof Area)
                                {
                                    Area area = (Area) province;
                                    params.put(AnalyticsManager.KeyType.COUNTRY, area.getProvince().isOverseas ? AnalyticsManager.ValueType.OVERSEAS : AnalyticsManager.ValueType.DOMESTIC);
                                    params.put(AnalyticsManager.KeyType.PROVINCE, area.getProvince().name);
                                    params.put(AnalyticsManager.KeyType.DISTRICT, area.name);
                                } else if (province != null)
                                {
                                    params.put(AnalyticsManager.KeyType.COUNTRY, province.isOverseas ? AnalyticsManager.ValueType.OVERSEAS : AnalyticsManager.ValueType.DOMESTIC);
                                    params.put(AnalyticsManager.KeyType.PROVINCE, province.name);
                                    params.put(AnalyticsManager.KeyType.DISTRICT, AnalyticsManager.ValueType.ALL_LOCALE_KR);
                                }

                                params.put(AnalyticsManager.KeyType.SEARCH_COUNT, Integer.toString(mSearchCount > mSearchMaxCount ? mSearchMaxCount : mSearchCount));
                            } catch (Exception e)
                            {

                            }

                            int placeCount = placeListFragmentList.get(0).getPlaceCount();
                            recordEventSearchResultByLocation(address, placeCount == 0, params);
                            mReceiveDataFlag = 2;
                        }
                    }
                }
            }

            mAddress = address;
            mPlaceSearchResultLayout.setToolbarTitle(address);
        }

        @Override
        public void onError(Call call, Throwable e, boolean onlyReport)
        {
            GourmetSearchResultActivity.this.onError(call, e, onlyReport);
        }

        @Override
        public void onError(Throwable e)
        {
            GourmetSearchResultActivity.this.onError(e);
        }

        @Override
        public void onErrorPopupMessage(int msgCode, String message)
        {
            GourmetSearchResultActivity.this.onErrorPopupMessage(msgCode, message);
        }

        @Override
        public void onErrorToastMessage(String message)
        {
            GourmetSearchResultActivity.this.onErrorToastMessage(message);
        }

        @Override
        public void onErrorResponse(Call call, Response response)
        {
            GourmetSearchResultActivity.this.onErrorResponse(call, response);
        }
    };

    private GourmetListFragment.OnGourmetListFragmentListener mOnGourmetListFragmentListener = new GourmetListFragment.OnGourmetListFragmentListener()
    {
        @Override
        public void onGourmetClick(View view, PlaceViewItem placeViewItem, int listCount)
        {
            if (placeViewItem == null || placeViewItem.mType != PlaceViewItem.TYPE_ENTRY)
            {
                return;
            }

            Gourmet gourmet = placeViewItem.getItem();

            if (Util.isUsedMultiTransition() == true)
            {
                Intent intent = GourmetDetailActivity.newInstance(GourmetSearchResultActivity.this,//
                    mGourmetSearchCuration.getGourmetBookingDay(), gourmet, listCount, true);

                View simpleDraweeView = view.findViewById(R.id.imageView);
                View nameTextView = view.findViewById(R.id.nameTextView);
                View gradientTopView = view.findViewById(R.id.gradientTopView);
                View gradientBottomView = view.findViewById(R.id.gradientView);

                Object mapTag = gradientBottomView.getTag();

                if (mapTag != null && "map".equals(mapTag) == true)
                {
                    intent.putExtra(NAME_INTENT_EXTRA_DATA_FROM_MAP, true);
                }

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(GourmetSearchResultActivity.this,//
                    android.support.v4.util.Pair.create(simpleDraweeView, getString(R.string.transition_place_image)),//
                    android.support.v4.util.Pair.create(nameTextView, getString(R.string.transition_place_name)),//
                    android.support.v4.util.Pair.create(gradientTopView, getString(R.string.transition_gradient_top_view)),//
                    android.support.v4.util.Pair.create(gradientBottomView, getString(R.string.transition_gradient_bottom_view)));

                startActivityForResult(intent, CODE_REQUEST_ACTIVITY_GOURMET_DETAIL, options.toBundle());
            } else
            {
                Intent intent = GourmetDetailActivity.newInstance(GourmetSearchResultActivity.this,//
                    mGourmetSearchCuration.getGourmetBookingDay(), gourmet, listCount, false);

                startActivityForResult(intent, CODE_REQUEST_ACTIVITY_GOURMET_DETAIL);

                overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
            }
        }

        @Override
        public void onGourmetCategoryFilter(int page, HashMap<String, Integer> categoryCodeMap, HashMap<String, Integer> categorySequenceMap)
        {
            if (page <= 1 && mGourmetSearchCuration.getCurationOption().isDefaultFilter() == true)
            {
                ((GourmetCurationOption) mGourmetSearchCuration.getCurationOption()).setCategoryCoderMap(categoryCodeMap);
                ((GourmetCurationOption) mGourmetSearchCuration.getCurationOption()).setCategorySequenceMap(categorySequenceMap);
            }
        }

        //        @Override
        //        public void onEventBannerClick(EventBanner eventBanner)
        //        {
        //
        //        }

        @Override
        public void onActivityCreated(PlaceListFragment placeListFragment)
        {
            if (mPlaceSearchResultLayout == null || placeListFragment == null)
            {
                return;
            }

            PlaceListFragment currentPlaceListFragment = mPlaceSearchResultLayout.getCurrentPlaceListFragment();

            if (currentPlaceListFragment == placeListFragment)
            {
                currentPlaceListFragment.setVisibility(mViewType, true);
                currentPlaceListFragment.setPlaceCuration(mGourmetSearchCuration);
                ((GourmetSearchResultListFragment) currentPlaceListFragment).setSearchType(mSearchType);
                currentPlaceListFragment.refreshList(true);
            } else
            {
                placeListFragment.setVisibility(mViewType, false);
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy)
        {
            mPlaceSearchResultLayout.calculationMenuBarLayoutTranslationY(dy);
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState)
        {
            switch (newState)
            {
                case RecyclerView.SCROLL_STATE_IDLE:
                {
                    mPlaceSearchResultLayout.animationMenuBarLayout();

                    //                    ExLog.d("offset : " + recyclerView.computeVerticalScrollOffset() + ", " + recyclerView.computeVerticalScrollExtent() + ", " + recyclerView.computeVerticalScrollRange());

                    if (recyclerView.computeVerticalScrollOffset() + recyclerView.computeVerticalScrollExtent() >= recyclerView.computeVerticalScrollRange())
                    {
                        GourmetListAdapter gourmetListAdapter = (GourmetListAdapter) recyclerView.getAdapter();

                        if (gourmetListAdapter != null)
                        {
                            int count = gourmetListAdapter.getItemCount();

                            if (count == 0)
                            {
                            } else
                            {
                                PlaceViewItem placeViewItem = gourmetListAdapter.getItem(gourmetListAdapter.getItemCount() - 1);

                                if (placeViewItem != null && placeViewItem.mType == PlaceViewItem.TYPE_FOOTER_VIEW)
                                {
                                    mPlaceSearchResultLayout.showBottomLayout(false);
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
        }

        @Override
        public void onFilterClick()
        {
            if (isFinishing() == true || lockUiComponentAndIsLockUiComponent() == true)
            {
                return;
            }

            Intent intent = GourmetSearchResultCurationActivity.newInstance(GourmetSearchResultActivity.this,//
                mViewType, mSearchType, mGourmetSearchCuration, mIsFixedLocation);
            startActivityForResult(intent, CODE_REQUEST_ACTIVITY_GOURMETCURATION);
        }

        @Override
        public void onUpdateFilterEnabled(boolean isShowFilterEnabled)
        {
            if (mPlaceSearchResultLayout == null)
            {
                return;
            }

            mPlaceSearchResultLayout.setOptionFilterEnabled(isShowFilterEnabled);
        }

        @Override
        public void onUpdateViewTypeEnabled(boolean isShowViewTypeEnabled)
        {
            if (mPlaceSearchResultLayout == null)
            {
                return;
            }

            mPlaceSearchResultLayout.setOptionViewTypeEnabled(isShowViewTypeEnabled);
        }

        @Override
        public void onShowActivityEmptyView(boolean isShow)
        {
            if (mPlaceSearchResultLayout == null)
            {
                return;
            }

            if (isShow == true)
            {
                mPlaceSearchResultLayout.setCategoryTabLayoutVisibility(View.GONE);
                mPlaceSearchResultLayout.showEmptyLayout();

                recordScreenSearchResult(AnalyticsManager.Screen.SEARCH_RESULT_EMPTY);
            } else
            {
                mPlaceSearchResultLayout.setCategoryTabLayoutVisibility(View.GONE);
                mPlaceSearchResultLayout.showListLayout();
            }

            GourmetBookingDay gourmetBookingDay = mGourmetSearchCuration.getGourmetBookingDay();
            Map<String, String> params = new HashMap<>();
            try
            {
                params.put(AnalyticsManager.KeyType.CHECK_IN, gourmetBookingDay.getVisitDay("yyyy-MM-dd"));

                params.put(AnalyticsManager.KeyType.PLACE_TYPE, AnalyticsManager.ValueType.GOURMET);
                params.put(AnalyticsManager.KeyType.PLACE_HIT_TYPE, AnalyticsManager.ValueType.GOURMET);

                Province province = mGourmetSearchCuration.getProvince();
                if (province instanceof Area)
                {
                    Area area = (Area) province;
                    params.put(AnalyticsManager.KeyType.COUNTRY, area.getProvince().isOverseas ? AnalyticsManager.ValueType.OVERSEAS : AnalyticsManager.ValueType.DOMESTIC);
                    params.put(AnalyticsManager.KeyType.PROVINCE, area.getProvince().name);
                    params.put(AnalyticsManager.KeyType.DISTRICT, area.name);
                } else if (province != null)
                {
                    params.put(AnalyticsManager.KeyType.COUNTRY, province.isOverseas ? AnalyticsManager.ValueType.OVERSEAS : AnalyticsManager.ValueType.DOMESTIC);
                    params.put(AnalyticsManager.KeyType.PROVINCE, province.name);
                    params.put(AnalyticsManager.KeyType.DISTRICT, AnalyticsManager.ValueType.ALL_LOCALE_KR);
                }

                params.put(AnalyticsManager.KeyType.SEARCH_COUNT, Integer.toString(mSearchCount > mSearchMaxCount ? mSearchMaxCount : mSearchCount));
            } catch (Exception e)
            {

            }

            Keyword keyword = mGourmetSearchCuration.getKeyword();

            if (mSearchType == SearchType.LOCATION)
            {
                synchronized (GourmetSearchResultActivity.this)
                {
                    if (mReceiveDataFlag == 0)
                    {
                        mReceiveDataFlag = 1;
                    } else if (mReceiveDataFlag == 1)
                    {
                        recordEventSearchResultByLocation(mAddress, isShow, params);
                        mReceiveDataFlag = 2;
                    }
                }
            } else if (mSearchType == SearchType.RECENT)
            {
                recordEventSearchResultByRecentKeyword(keyword, isShow, params);
            } else if (mSearchType == SearchType.AUTOCOMPLETE)
            {
                recordEventSearchResultByAutoSearch(keyword, mInputText, isShow, params);
            } else
            {
                recordEventSearchResultByKeyword(keyword, isShow, params);
            }
        }

        @Override
        public void onRecordAnalytics(ViewType viewType)
        {
            try
            {
                if (viewType == ViewType.LIST)
                {
                    recordScreenSearchResult(AnalyticsManager.Screen.SEARCH_RESULT);
                }
            } catch (Exception e)
            {
                ExLog.d(e.getMessage());
            }
        }

        @Override
        public void onSearchCountUpdate(int searchCount, int searchMaxCount)
        {
            mSearchCount = searchCount;
            mSearchMaxCount = searchMaxCount;
        }
    };
}