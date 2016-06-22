package com.twoheart.dailyhotel.screen.hotel.list;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Area;
import com.twoheart.dailyhotel.model.Category;
import com.twoheart.dailyhotel.model.EventBanner;
import com.twoheart.dailyhotel.model.Hotel;
import com.twoheart.dailyhotel.model.HotelCurationOption;
import com.twoheart.dailyhotel.model.PlaceCurationOption;
import com.twoheart.dailyhotel.model.PlaceViewItem;
import com.twoheart.dailyhotel.model.Province;
import com.twoheart.dailyhotel.model.SaleTime;
import com.twoheart.dailyhotel.network.DailyNetworkAPI;
import com.twoheart.dailyhotel.network.response.DailyHotelJsonResponseListener;
import com.twoheart.dailyhotel.place.base.BaseActivity;
import com.twoheart.dailyhotel.place.base.BaseFragment;
import com.twoheart.dailyhotel.place.fragment.PlaceMainFragment;
import com.twoheart.dailyhotel.place.layout.PlaceMainLayout;
import com.twoheart.dailyhotel.place.networkcontroller.PlaceMainNetworkController;
import com.twoheart.dailyhotel.screen.event.EventWebActivity;
import com.twoheart.dailyhotel.screen.gourmet.detail.GourmetDetailActivity;
import com.twoheart.dailyhotel.screen.hotel.detail.HotelDetailActivity;
import com.twoheart.dailyhotel.screen.hotel.filter.HotelCurationActivity;
import com.twoheart.dailyhotel.screen.hotel.region.HotelRegionListActivity;
import com.twoheart.dailyhotel.screen.hotel.search.HotelSearchActivity;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.DailyCalendar;
import com.twoheart.dailyhotel.util.DailyDeepLink;
import com.twoheart.dailyhotel.util.DailyLocationFactory;
import com.twoheart.dailyhotel.util.DailyPreference;
import com.twoheart.dailyhotel.util.ExLog;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager.Action;
import com.twoheart.dailyhotel.widget.FontManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class StayMainFragment extends PlaceMainFragment
{
    private SaleTime mTodaySaleTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mTodaySaleTime = new SaleTime();

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        // 아마도 위치 정보
    }

    @Override
    protected PlaceMainLayout getPlaceMainLayout(Context context)
    {
        return new StayMainLayout(mBaseActivity, new PlaceMainLayout.OnEventListener()
        {
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
            public void onSearchClick()
            {

            }

            @Override
            public void onDateClick()
            {

            }

            @Override
            public void onRegionClick()
            {

            }

            @Override
            public void onViewTypeClick()
            {

            }

            @Override
            public void onFilterClick()
            {

            }

            @Override
            public void finish()
            {

            }
        });
    }

    @Override
    protected PlaceMainNetworkController getPlaceMainNetworkController(Context context)
    {
        return new StayMainNetworkController(mBaseActivity, mNetworkTag, new PlaceMainNetworkController.OnNetworkControllerListener()
        {
            @Override
            public void onDateTime(long currentDateTime, long dailyDateTime)
            {
                BaseActivity baseActivity = (BaseActivity) getActivity();

                if (baseActivity == null)
                {
                    return;
                }

                try
                {
                    mTodaySaleTime.setCurrentTime(currentDateTime);
                    mTodaySaleTime.setDailyTime(dailyDateTime);

                    if (DailyDeepLink.getInstance().isValidateLink() == true //
                        && processDeepLink(baseActivity) == true)
                    {

                    } else
                    {
                        // 지역 리스트를 가져온다
                        DailyNetworkAPI.getInstance(baseActivity).requestHotelRegionList(mNetworkTag, mRegionListJsonResponseListener, baseActivity);
                    }
                } catch (Exception e)
                {
                    onError(e);
                    unLockUI();
                }
            }

            @Override
            public void onEventBanner(List<EventBanner> eventBannerList)
            {

            }

            @Override
            public void onRegionList()
            {

            }

            @Override
            public void onErrorResponse(VolleyError volleyError)
            {

            }

            @Override
            public void onError(Exception e)
            {

            }

            @Override
            public void onErrorPopupMessage(int msgCode, String message)
            {

            }

            @Override
            public void onErrorToastMessage(String message)
            {

            }

            private boolean processDeepLink(BaseActivity baseActivity)
            {
                if (DailyDeepLink.getInstance().isHotelDetailView() == true)
                {
                    unLockUI();
                    deepLinkDetail(baseActivity);
                    return true;
                } else if (DailyDeepLink.getInstance().isHotelEventBannerWebView() == true)
                {
                    unLockUI();
                    deepLinkEventBannerWeb(baseActivity);
                    return true;
                } else
                {
                    // 더이상 진입은 없다.
                    if (DailyDeepLink.getInstance().isHotelListView() == false//
                        && DailyDeepLink.getInstance().isHotelRegionListView() == false)
                    {
                        DailyDeepLink.getInstance().clear();
                    }
                }

                return false;
            }
        });
    }

    @Override
    protected void onRegionActivityResult(int requestCode, int resultCode, Intent data)
    {

    }

    @Override
    protected void onCalendarActivityResult(int requestCode, int resultCode, Intent data)
    {

    }

    @Override
    protected void onCurationActivityResult(int requestCode, int resultCode, Intent data)
    {

    }

    @Override
    protected void onSettingLocationActivityResult(int requestCode, int resultCode, Intent data)
    {

    }

    @Override
    protected void onLocationFailed()
    {

    }

    @Override
    protected void onLocationProviderDisabled()
    {

    }

    @Override
    protected void onLocationChanged(Location location)
    {

    }

    private String makeTabDateFormat(SaleTime checkInSaleTime, SaleTime checkOutSaleTime)
    {
        String dateFormat;
        String tabDateFormat;

        if (Util.getLCDWidth(getContext()) < 720)
        {
            dateFormat = "M.d";
            tabDateFormat = "%s - %s";
        } else
        {
            dateFormat = "M월d일";
            tabDateFormat = "%s-%s";
        }

        String checkInDay = checkInSaleTime.getDayOfDaysDateFormat(dateFormat);
        String checkOutDay = checkOutSaleTime.getDayOfDaysDateFormat(dateFormat);

        return String.format(tabDateFormat, checkInDay, checkOutDay);
    }

    private void curationCurrentFragment()
    {
        HotelListFragment currentFragment = (HotelListFragment) mFragmentPagerAdapter.getItem(mViewPager.getCurrentItem());
        currentFragment.curationList(mViewType, mCurationOption);
    }

    private void refreshCurrentFragment(List<EventBanner> list)
    {
        HotelListFragment currentFragment = (HotelListFragment) mFragmentPagerAdapter.getItem(mViewPager.getCurrentItem());
        currentFragment.refreshList(list);
    }

    private void refreshCurrentFragment(Province province)
    {
        if (province == null)
        {
            return;
        }

        BaseActivity baseActivity = (BaseActivity) getActivity();

        if (baseActivity == null || baseActivity.isFinishing() == true)
        {
            return;
        }

        setProvince(province);

        mDailyToolbarLayout.setToolbarRegionText(province.name);
        mDailyToolbarLayout.setToolbarMenuVisibility(true);

        // 기존에 설정된 지역과 다른 지역을 선택하면 해당 지역을 저장한다.
        String savedRegion = DailyPreference.getInstance(baseActivity).getSelectedRegion(PlaceType.HOTEL);

        if (province.name.equalsIgnoreCase(savedRegion) == false)
        {
            DailyPreference.getInstance(baseActivity).setSelectedOverseaRegion(PlaceType.HOTEL, province.isOverseas);
            DailyPreference.getInstance(baseActivity).setSelectedRegion(PlaceType.HOTEL, province.name);
        }

        refreshCurrentFragment(mEventBannerList);
    }

    private void searchMyLocation()
    {
        BaseActivity baseActivity = (BaseActivity) getActivity();

        if (baseActivity == null || isLockUiComponent() == true)
        {
            return;
        }

        lockUI();

        DailyLocationFactory.getInstance(baseActivity).startLocationMeasure(baseActivity, null, new DailyLocationFactory.LocationListenerEx()
        {
            @Override
            public void onRequirePermission()
            {
                if (Util.isOverAPI23() == true)
                {
                    requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, Constants.REQUEST_CODE_PERMISSIONS_ACCESS_FINE_LOCATION);
                }

                unLockUI();
            }

            @Override
            public void onFailed()
            {
                unLockUI();

                //                recordAnalyticsSortTypeEvent(getContext(), mCurationOption.getSortType());

                if (Util.isOverAPI23() == true)
                {
                    BaseActivity baseActivity = (BaseActivity) getActivity();

                    if (baseActivity == null || baseActivity.isFinishing() == true)
                    {
                        return;
                    }

                    baseActivity.showSimpleDialog(getString(R.string.dialog_title_used_gps)//
                        , getString(R.string.dialog_msg_used_gps_android6)//
                        , getString(R.string.dialog_btn_text_dosetting)//
                        , getString(R.string.dialog_btn_text_cancel)//
                        , new View.OnClickListener()//
                        {
                            @Override
                            public void onClick(View v)
                            {
                                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, Constants.REQUEST_CODE_PERMISSIONS_ACCESS_FINE_LOCATION);
                            }
                        }, new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                mCurationOption.setSortType(SortType.DEFAULT);
                                curationCurrentFragment();
                            }
                        }, true);
                } else
                {
                    mCurationOption.setSortType(SortType.DEFAULT);
                    curationCurrentFragment();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProviderEnabled(String provider)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProviderDisabled(String provider)
            {
                unLockUI();

                BaseActivity baseActivity = (BaseActivity) getActivity();

                if (baseActivity == null || baseActivity.isFinishing() == true)
                {
                    return;
                }

                // 현재 GPS 설정이 꺼져있습니다 설정에서 바꾸어 주세요.
                DailyLocationFactory.getInstance(baseActivity).stopLocationMeasure();

                baseActivity.showSimpleDialog(getString(R.string.dialog_title_used_gps)//
                    , getString(R.string.dialog_msg_used_gps)//
                    , getString(R.string.dialog_btn_text_dosetting)//
                    , getString(R.string.dialog_btn_text_cancel)//
                    , new View.OnClickListener()//
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, Constants.CODE_RESULT_ACTIVITY_SETTING_LOCATION);
                        }
                    }, new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            mCurationOption.setSortType(SortType.DEFAULT);
                            curationCurrentFragment();

                            //                        recordAnalyticsSortTypeEvent(getContext(), mSortType);
                        }
                    }, false);
            }

            @Override
            public void onLocationChanged(Location location)
            {
                BaseActivity baseActivity = (BaseActivity) getActivity();

                if (baseActivity == null || baseActivity.isFinishing() == true)
                {
                    unLockUI();
                    return;
                }

                DailyLocationFactory.getInstance(baseActivity).stopLocationMeasure();

                if (location == null)
                {
                    mCurationOption.setSortType(SortType.DEFAULT);
                    curationCurrentFragment();
                } else
                {
                    mCurationOption.setLocation(location);

                    if (mCurationOption.getSortType() == SortType.DISTANCE)
                    {
                        curationCurrentFragment();
                    }
                }

                unLockUI();
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Deep Link
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void deepLinkDetail(BaseActivity baseActivity)
    {
        try
        {
            // 신규 타입의 화면이동
            int hotelIndex = Integer.parseInt(DailyDeepLink.getInstance().getIndex());
            long dailyTime = mTodaySaleTime.getDailyTime();
            int nights = Integer.parseInt(DailyDeepLink.getInstance().getNights());

            String date = DailyDeepLink.getInstance().getDate();
            int datePlus = DailyDeepLink.getInstance().getDatePlus();

            if (Util.isTextEmpty(date) == true)
            {
                if (datePlus >= 0)
                {
                    mOnCommunicateListener.selectHotel(hotelIndex, dailyTime, datePlus, nights);
                } else
                {
                    throw new NullPointerException("datePlus < 0");
                }
            } else
            {
                SimpleDateFormat format = new java.text.SimpleDateFormat("yyyyMMdd");
                Date schemeDate = format.parse(date);
                Date dailyDate = format.parse(mTodaySaleTime.getDayOfDaysDateFormat("yyyyMMdd"));

                int dailyDayOfDays = (int) ((schemeDate.getTime() - dailyDate.getTime()) / SaleTime.MILLISECOND_IN_A_DAY);

                if (nights <= 0 || dailyDayOfDays < 0)
                {
                    throw new NullPointerException("nights <= 0 || dailyDayOfDays < 0");
                }

                mOnCommunicateListener.selectHotel(hotelIndex, dailyTime, dailyDayOfDays, nights);
            }

            DailyDeepLink.getInstance().clear();
            mIsDeepLink = true;
        } catch (Exception e)
        {
            ExLog.d(e.toString());

            DailyDeepLink.getInstance().clear();

            //탭에 들어갈 날짜를 만든다.
            makeDateTabLayout();

            // 지역 리스트를 가져온다
            DailyNetworkAPI.getInstance(baseActivity).requestHotelRegionList(mNetworkTag, mRegionListJsonResponseListener, baseActivity);
        }
    }

    private void deepLinkEventBannerWeb(BaseActivity baseActivity)
    {
        String url = DailyDeepLink.getInstance().getUrl();
        DailyDeepLink.getInstance().clear();

        if (Util.isTextEmpty(url) == false)
        {
            Intent intent = EventWebActivity.newInstance(baseActivity, EventWebActivity.SourceType.HOTEL_BANNER, url, null, mTodaySaleTime);
            baseActivity.startActivityForResult(intent, CODE_REQUEST_ACTIVITY_EVENTWEB);
            mIsDeepLink = true;
        } else
        {
            //탭에 들어갈 날짜를 만든다.
            makeDateTabLayout();

            // 지역 리스트를 가져온다
            DailyNetworkAPI.getInstance(baseActivity).requestHotelRegionList(mNetworkTag, mRegionListJsonResponseListener, baseActivity);
        }
    }

    private Province searchDeeLinkRegion(ArrayList<Province> provinceList, ArrayList<Area> areaList)
    {
        Province selectedProvince = null;

        try
        {
            int provinceIndex = Integer.parseInt(DailyDeepLink.getInstance().getProvinceIndex());
            int areaIndex = -1;

            try
            {
                areaIndex = Integer.parseInt(DailyDeepLink.getInstance().getAreaIndex());
            } catch (Exception e)
            {
            }

            boolean isOverseas = DailyDeepLink.getInstance().getIsOverseas();

            if (areaIndex == -1)
            {
                // 전체 지역으로 이동
                for (Province province : provinceList)
                {
                    if (province.isOverseas == isOverseas && province.index == provinceIndex)
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

    private Province searchDeeLinkRegion(int provinceIndex, int areaIndex, boolean isOverseas, ArrayList<Province> provinceList, ArrayList<Area> areaList)
    {
        Province selectedProvince = null;

        if (provinceIndex < 0 && areaIndex < 0)
        {
            return searchDeeLinkRegion(provinceList, areaList);
        }

        try
        {
            if (areaIndex == -1)
            {
                // 전체 지역으로 이동
                for (Province province : provinceList)
                {
                    if (province.isOverseas == isOverseas && province.index == provinceIndex)
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

    private void deepLinkRegionList(BaseActivity baseActivity, ArrayList<Province> provinceList, ArrayList<Area> areaList)
    {
        Province selectedProvince = searchDeeLinkRegion(provinceList, areaList);

        if (selectedProvince == null)
        {
            selectedProvince = getProvince();
        }

        setProvince(selectedProvince);

        mDailyToolbarLayout.setToolbarRegionText(selectedProvince.name);
        mDailyToolbarLayout.setToolbarMenuVisibility(true);

        Intent intent = HotelRegionListActivity.newInstance(baseActivity, selectedProvince, mTodaySaleTime, 1);
        startActivityForResult(intent, CODE_REQUEST_ACTIVITY_REGIONLIST);

        DailyDeepLink.getInstance().clear();
        mIsDeepLink = true;
    }

    private void deepLinkRefreshBanner(final SaleTime checkInSaleTime, final SaleTime checkOutSaleTime)
    {
        DailyHotelJsonResponseListener deepLinkEventListener = new DailyHotelJsonResponseListener()
        {
            @Override
            public void onResponse(String url, JSONObject response)
            {
                setEventBannerJson(response);

                mOnCommunicateListener.selectDay(checkInSaleTime, checkOutSaleTime, true);
            }

            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                mOnCommunicateListener.selectDay(checkInSaleTime, checkOutSaleTime, true);
            }
        };

        DailyNetworkAPI.getInstance(getContext()).requestEventBannerList(mNetworkTag, "hotel", deepLinkEventListener, deepLinkEventListener);
    }

    private void deepLinkHotelList(ArrayList<Province> provinceList, ArrayList<Area> areaList)
    {
        String categoryCode = DailyDeepLink.getInstance().getCategoryCode();
        String date = DailyDeepLink.getInstance().getDate();
        int datePlus = DailyDeepLink.getInstance().getDatePlus();
        mCurationOption.setSortType(DailyDeepLink.getInstance().getSorting());

        updateFilteredFloatingActionButton();

        int night;

        try
        {
            night = Integer.parseInt(DailyDeepLink.getInstance().getNights());
        } catch (Exception e)
        {
            night = 1;
        }

        int provinceIndex;
        int areaIndex;

        try
        {
            provinceIndex = Integer.parseInt(DailyDeepLink.getInstance().getProvinceIndex());
        } catch (Exception e)
        {
            provinceIndex = -1;
        }

        try
        {
            areaIndex = Integer.parseInt(DailyDeepLink.getInstance().getAreaIndex());
        } catch (Exception e)
        {
            areaIndex = -1;
        }

        boolean isOverseas = DailyDeepLink.getInstance().getIsOverseas();

        // 지역이 있는 경우 지역을 디폴트로 잡아주어야 한다
        Province selectedProvince = searchDeeLinkRegion(provinceIndex, areaIndex, isOverseas, provinceList, areaList);

        if (selectedProvince == null)
        {
            selectedProvince = getProvince();
        }

        setProvince(selectedProvince);

        mDailyToolbarLayout.setToolbarRegionText(selectedProvince.name);
        mDailyToolbarLayout.setToolbarMenuVisibility(true);

        // 카테고리가 있는 경우 카테고리를 디폴트로 잡아주어야 한다
        if (Util.isTextEmpty(categoryCode) == false)
        {
            for (Category category : selectedProvince.getCategoryList())
            {
                if (category.code.equalsIgnoreCase(categoryCode) == true)
                {
                    setCategory(category);
                    break;
                }
            }
        }

        // 날짜가 있는 경우 디폴트로 3번째 탭으로 넘어가야 한다
        if (Util.isTextEmpty(date) == false)
        {
            try
            {
                mTabLayout.setOnTabSelectedListener(null);
                mTabLayout.setScrollPosition(2, 0f, true);
                mViewPager.setCurrentItem(2);
                mTabLayout.setOnTabSelectedListener(mOnTabSelectedListener);
                DailyDeepLink.getInstance().clear();

                SimpleDateFormat format = new java.text.SimpleDateFormat("yyyyMMdd");
                Date schemeDate = format.parse(date);
                Date dailyDate = format.parse(mTodaySaleTime.getDayOfDaysDateFormat("yyyyMMdd"));

                int dailyDayOfDays = (int) ((schemeDate.getTime() - dailyDate.getTime()) / SaleTime.MILLISECOND_IN_A_DAY);

                if (dailyDayOfDays >= 0)
                {
                    SaleTime checkInSaleTime = mTodaySaleTime.getClone(dailyDayOfDays);
                    SaleTime checkOutSaleTime = mTodaySaleTime.getClone(dailyDayOfDays + night);

                    HotelDaysListFragment hotelListFragment = (HotelDaysListFragment) mFragmentPagerAdapter.getItem(mViewPager.getCurrentItem());
                    hotelListFragment.setCheckInSaleTime(checkInSaleTime);
                    hotelListFragment.setCheckOutSaleTime(checkOutSaleTime);

                    deepLinkRefreshBanner(checkInSaleTime, checkOutSaleTime);
                } else
                {
                    DailyDeepLink.getInstance().clear();
                    refreshEventBanner();
                }
            } catch (Exception e)
            {
                mTabLayout.setOnTabSelectedListener(null);
                mTabLayout.setScrollPosition(0, 0f, true);
                mViewPager.setCurrentItem(0);
                mTabLayout.setOnTabSelectedListener(mOnTabSelectedListener);

                DailyDeepLink.getInstance().clear();
                refreshEventBanner();
            }
        } else if (datePlus >= 0)
        {
            try
            {
                mTabLayout.setOnTabSelectedListener(null);
                mTabLayout.setScrollPosition(2, 0f, true);
                mViewPager.setCurrentItem(2);
                mTabLayout.setOnTabSelectedListener(mOnTabSelectedListener);
                DailyDeepLink.getInstance().clear();

                SaleTime checkInSaleTime = mTodaySaleTime.getClone(datePlus);
                SaleTime checkOutSaleTime = mTodaySaleTime.getClone(datePlus + night);

                HotelDaysListFragment hotelListFragment = (HotelDaysListFragment) mFragmentPagerAdapter.getItem(mViewPager.getCurrentItem());
                hotelListFragment.setCheckInSaleTime(checkInSaleTime);
                hotelListFragment.setCheckOutSaleTime(checkOutSaleTime);

                deepLinkRefreshBanner(checkInSaleTime, checkOutSaleTime);
            } catch (Exception e)
            {
                mTabLayout.setOnTabSelectedListener(null);
                mTabLayout.setScrollPosition(0, 0f, true);
                mViewPager.setCurrentItem(0);
                mTabLayout.setOnTabSelectedListener(mOnTabSelectedListener);

                DailyDeepLink.getInstance().clear();
                refreshEventBanner();
            }
        } else
        {
            DailyDeepLink.getInstance().clear();
            refreshEventBanner();
        }
    }

    private void setEventBannerJson(JSONObject jsonObject)
    {
        try
        {
            int msgCode = jsonObject.getInt("msgCode");

            if (msgCode == 100)
            {
                JSONObject dataJSONObject = jsonObject.getJSONObject("data");

                String baseUrl = dataJSONObject.getString("imgUrl");

                JSONArray jsonArray = dataJSONObject.getJSONArray("eventBanner");

                if (mEventBannerList == null)
                {
                    mEventBannerList = new ArrayList<>();
                }

                mEventBannerList.clear();

                int length = jsonArray.length();
                for (int i = 0; i < length; i++)
                {
                    try
                    {
                        EventBanner eventBanner = new EventBanner(jsonArray.getJSONObject(i), baseUrl);
                        mEventBannerList.add(eventBanner);
                    } catch (Exception e)
                    {
                        ExLog.d(e.toString());
                    }
                }
            }
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Listener
    //////////////////////////////////////////////////////////////////////////////////////////////////////////

}
