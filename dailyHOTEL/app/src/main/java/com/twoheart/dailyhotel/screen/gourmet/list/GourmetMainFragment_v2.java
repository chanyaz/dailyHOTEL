package com.twoheart.dailyhotel.screen.gourmet.list;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.design.widget.TabLayout;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Area;
import com.twoheart.dailyhotel.model.Category;
import com.twoheart.dailyhotel.model.EventBanner;
import com.twoheart.dailyhotel.model.GourmetCurationOption;
import com.twoheart.dailyhotel.model.PlaceCurationOption;
import com.twoheart.dailyhotel.model.PlaceViewItem;
import com.twoheart.dailyhotel.model.Province;
import com.twoheart.dailyhotel.model.SaleTime;
import com.twoheart.dailyhotel.place.base.BaseActivity;
import com.twoheart.dailyhotel.place.fragment.PlaceListFragment;
import com.twoheart.dailyhotel.place.fragment.PlaceMainFragment;
import com.twoheart.dailyhotel.place.layout.PlaceMainLayout;
import com.twoheart.dailyhotel.place.networkcontroller.PlaceMainNetworkController;
import com.twoheart.dailyhotel.screen.event.EventWebActivity;
import com.twoheart.dailyhotel.screen.gourmet.detail.GourmetDetailActivity;
import com.twoheart.dailyhotel.screen.gourmet.filter.GourmetCalendarActivity;
import com.twoheart.dailyhotel.screen.gourmet.filter.GourmetCurationActivity;
import com.twoheart.dailyhotel.screen.gourmet.region.GourmetRegionListActivity;
import com.twoheart.dailyhotel.screen.gourmet.search.GourmetSearchActivity;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.DailyDeepLink;
import com.twoheart.dailyhotel.util.DailyPreference;
import com.twoheart.dailyhotel.util.ExLog;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;
import com.twoheart.dailyhotel.widget.DailyToast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GourmetMainFragment_v2 extends PlaceMainFragment
{
    @Override
    protected PlaceMainLayout getPlaceMainLayout(Context context)
    {
        return new GourmetMainLayout(context, mOnEventListener);
    }

    @Override
    protected PlaceMainNetworkController getPlaceMainNetworkController(Context context)
    {
        return new GourmetMainNetworkController(context, mNetworkTag, mOnNetworkControllerListener);
    }



    @Override
    protected void onRegionActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK && data != null)
        {
            if (data.hasExtra(NAME_INTENT_EXTRA_DATA_PROVINCE) == true)
            {
                Province province = data.getParcelableExtra(NAME_INTENT_EXTRA_DATA_PROVINCE);
                GourmetCurationManager.getInstance().setProvince(province);

                mPlaceMainLayout.setToolbarRegionText(province.name);

                DailyPreference.getInstance(mBaseActivity).setSelectedOverseaRegion(PlaceType.FNB, province.isOverseas);
                DailyPreference.getInstance(mBaseActivity).setSelectedRegion(PlaceType.FNB, province.name);

                refreshCurrentFragment();
            }
        }
    }

    @Override
    protected void onCurationActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK && data != null)
        {
            PlaceCurationOption placeCurationOption = data.getParcelableExtra(GourmetCurationActivity.INTENT_EXTRA_DATA_CURATION_OPTIONS);

            if (placeCurationOption instanceof GourmetCurationOption == false)
            {
                return;
            }

            GourmetCurationOption changedGourmetCurationOption = (GourmetCurationOption) placeCurationOption;
            GourmetCurationOption gourmetCurationOption = GourmetCurationManager.getInstance().getGourmetCurationOption();

            gourmetCurationOption.setSortType(changedGourmetCurationOption.getSortType());
            gourmetCurationOption.setFilterMap(changedGourmetCurationOption.getFilterMap());
            gourmetCurationOption.flagTimeFilter = changedGourmetCurationOption.flagTimeFilter;
            gourmetCurationOption.flagAmenitiesFilters = changedGourmetCurationOption.flagAmenitiesFilters;

            if (changedGourmetCurationOption.getSortType() == SortType.DISTANCE)
            {
                searchMyLocation();
            } else
            {
                refreshCurrentFragment();
            }
        }
    }

    @Override
    protected void onLocationFailed()
    {
        GourmetCurationManager.getInstance().getGourmetCurationOption().setSortType(SortType.DEFAULT);
        refreshCurrentFragment();
    }

    @Override
    protected void onLocationProviderDisabled()
    {
        GourmetCurationManager.getInstance().getGourmetCurationOption().setSortType(SortType.DEFAULT);
        refreshCurrentFragment();
    }

    @Override
    protected void onLocationChanged(Location location)
    {
        if (location == null)
        {
            GourmetCurationManager.getInstance().getGourmetCurationOption().setSortType(SortType.DEFAULT);
            refreshCurrentFragment();
        } else
        {
            GourmetCurationManager.getInstance().setLocation(location);

            if (GourmetCurationManager.getInstance().getGourmetCurationOption().getSortType() == SortType.DISTANCE)
            {
                refreshCurrentFragment();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        switch(mViewType)
        {
            case LIST:
            {
                if (requestCode == Constants.REQUEST_CODE_PERMISSIONS_ACCESS_FINE_LOCATION)
                {
                    if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    {
                        searchMyLocation();
                    } else
                    {
                        // 퍼미션 허락하지 않음.
                    }
                }
                break;
            }

            case MAP:
            {
                PlaceListFragment placeListFragment = mPlaceMainLayout.getCurrentPlaceListFragment();

                if (placeListFragment != null)
                {
                    placeListFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
                }
                break;
            }

            case GONE:
                break;
        }
    }

    private void refreshCurrentFragment()
    {
        if (isFinishing() == true)
        {
            return;
        }

        PlaceListFragment placeListFragment = mPlaceMainLayout.getCurrentPlaceListFragment();

        if (placeListFragment != null)
        {
            placeListFragment.refreshList();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // EventListener
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private PlaceMainLayout.OnEventListener mOnEventListener = new PlaceMainLayout.OnEventListener()
    {
        @Override
        public void onCategoryTabSelected(TabLayout.Tab tab)
        {
            // stay는 현재 카테고리 상태를 저장한다.
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
            Intent intent = GourmetSearchActivity.newInstance(mBaseActivity, GourmetCurationManager.getInstance().getSaleTime());
            mBaseActivity.startActivityForResult(intent, CODE_REQUEST_ACTIVITY_SEARCH);

            switch (mViewType)
            {
                case LIST:
                    AnalyticsManager.getInstance(getContext()).recordEvent(AnalyticsManager.Category.NAVIGATION//
                        , AnalyticsManager.Action.GOURMET_SEARCH_BUTTON_CLICKED, AnalyticsManager.Label.GOURMET_LIST, null);

                    AnalyticsManager.getInstance(getContext()).recordEvent(AnalyticsManager.Category.NAVIGATION//
                        , AnalyticsManager.Action.GOURMET_BOOKING_CALENDAR_POPPEDUP, AnalyticsManager.Label.GOURMET_LIST, null);
                    break;

                case MAP:
                    AnalyticsManager.getInstance(getContext()).recordEvent(AnalyticsManager.Category.NAVIGATION//
                        , AnalyticsManager.Action.GOURMET_SEARCH_BUTTON_CLICKED, AnalyticsManager.Label.GOURMET_MAP, null);

                    AnalyticsManager.getInstance(getContext()).recordEvent(AnalyticsManager.Category.NAVIGATION//
                        , AnalyticsManager.Action.GOURMET_BOOKING_CALENDAR_POPPEDUP, AnalyticsManager.Label.GOURMET_MAP, null);
                    break;
            }
        }

        @Override
        public void onDateClick()
        {
            if (lockUiComponentAndIsLockUiComponent() == true)
            {
                return;
            }

            Intent intent = GourmetCalendarActivity.newInstance(getContext(), GourmetCurationManager.getInstance().getSaleTime(), AnalyticsManager.ValueType.LIST, true, true);
            startActivityForResult(intent, Constants.CODE_REQUEST_ACTIVITY_CALENDAR);
        }

        @Override
        public void onRegionClick()
        {
            if (lockUiComponentAndIsLockUiComponent() == true)
            {
                return;
            }

            SaleTime saleTime = GourmetCurationManager.getInstance().getSaleTime();
            Province province = GourmetCurationManager.getInstance().getProvince();

            Intent intent = GourmetRegionListActivity.newInstance(getContext(), province, saleTime);
            startActivityForResult(intent, CODE_REQUEST_ACTIVITY_REGIONLIST);
        }

        @Override
        public void onViewTypeClick()
        {
            if (isFinishing() == true || lockUiComponentAndIsLockUiComponent() == true)
            {
                return;
            }

            GourmetListFragment_v2 gourmetListFragment = (GourmetListFragment_v2) mPlaceMainLayout.getCurrentPlaceListFragment();

            switch (mViewType)
            {
                case LIST:
                {
                    // 맵리스트 진입시에 솔드아웃은 맵에서 보여주지 않기 때문에 맵으로 진입시에 아무것도 볼수 없다.
                    if (gourmetListFragment.hasSalesPlace() == false)
                    {
                        unLockUI();

                        DailyToast.showToast(mBaseActivity, R.string.toast_msg_solodout_area, Toast.LENGTH_SHORT);
                        return;
                    }

                    mViewType = ViewType.MAP;
                    AnalyticsManager.getInstance(mBaseActivity).recordScreen(AnalyticsManager.Screen.DAILYGOURMET_LIST_MAP);
                    break;
                }

                case MAP:
                {
                    mViewType = ViewType.LIST;
                    AnalyticsManager.getInstance(mBaseActivity).recordScreen(AnalyticsManager.Screen.DAILYGOURMET_LIST);

                    Map<String, String> parmas = new HashMap<>();
                    Province province = GourmetCurationManager.getInstance().getProvince();

                    if (province instanceof Area)
                    {
                        Area area = (Area) province;
                        parmas.put(AnalyticsManager.KeyType.PROVINCE, area.getProvince().name);
                        parmas.put(AnalyticsManager.KeyType.DISTRICT, area.name);

                    } else
                    {
                        parmas.put(AnalyticsManager.KeyType.PROVINCE, province.name);
                        parmas.put(AnalyticsManager.KeyType.DISTRICT, AnalyticsManager.ValueType.EMPTY);
                    }

                    AnalyticsManager.getInstance(getContext()).recordScreen(AnalyticsManager.Screen.DAILYGOURMET_LIST, parmas);
                    break;
                }
            }

            for (PlaceListFragment placeListFragment : mPlaceMainLayout.getPlaceListFragment())
            {
                boolean isCurrentFragment = placeListFragment == gourmetListFragment;

                placeListFragment.setVisibility(mViewType, isCurrentFragment);
            }

            refreshCurrentFragment();
        }

        @Override
        public void onFilterClick()
        {
            if (isFinishing() == true || lockUiComponentAndIsLockUiComponent() == true)
            {
                return;
            }

            Province province = GourmetCurationManager.getInstance().getProvince();

            if (province == null)
            {
                releaseUiComponent();
                return;
            }

            Intent intent = GourmetCurationActivity.newInstance(mBaseActivity, province.isOverseas, mViewType, GourmetCurationManager.getInstance().getGourmetCurationOption());
            startActivityForResult(intent, CODE_REQUEST_ACTIVITY_GOURMETCURATION);

            String viewType = AnalyticsManager.Label.VIEWTYPE_LIST;

            switch (mViewType)
            {
                case LIST:
                    viewType = AnalyticsManager.Label.VIEWTYPE_LIST;
                    break;

                case MAP:
                    viewType = AnalyticsManager.Label.VIEWTYPE_MAP;
                    break;
            }

            AnalyticsManager.getInstance(mBaseActivity).recordEvent(AnalyticsManager.Category.NAVIGATION//
                , AnalyticsManager.Action.GOURMET_SORT_FILTER_BUTTON_CLICKED, viewType, null);
        }

        @Override
        public void finish()
        {
            mBaseActivity.finish();
        }
    };

    private PlaceMainNetworkController.OnNetworkControllerListener mOnNetworkControllerListener = new PlaceMainNetworkController.OnNetworkControllerListener()
    {
        @Override
        public void onDateTime(long currentDateTime, long dailyDateTime)
        {
            GourmetCurationManager.getInstance().setSaleTime(currentDateTime, dailyDateTime);

            if (DailyDeepLink.getInstance().isValidateLink() == true //
                && processDeepLinkByDateTime(mBaseActivity) == true)
            {
                // 딥링크 이동
            } else
            {
                mPlaceMainNetworkController.requestEventBanner();
            }
        }

        @Override
        public void onEventBanner(List<EventBanner> eventBannerList)
        {
            GourmetEventBannerManager.getInstance().setList(eventBannerList);

            mPlaceMainNetworkController.requestRegionList();
        }

        @Override
        public void onRegionList(List<Province> provinceList, List<Area> areaList)
        {
            if (provinceList == null || areaList == null)
            {
                return;
            }

            Province selectedProvince = GourmetCurationManager.getInstance().getProvince();

            if (selectedProvince == null)
            {
                selectedProvince = searchLastRegion(mBaseActivity, provinceList, areaList);
            }

            // 여러가지 방식으로 지역을 검색했지만 찾지 못하는 경우.
            if (selectedProvince == null)
            {
                selectedProvince = provinceList.get(0);
            }

            boolean mIsProvinceSetting = DailyPreference.getInstance(mBaseActivity).isSettingRegion(PlaceType.FNB);
            DailyPreference.getInstance(mBaseActivity).setSettingRegion(PlaceType.FNB, true);

            // 마지막으로 지역이 Area로 되어있으면 Province로 바꾸어 준다.
            if (mIsProvinceSetting == false && selectedProvince instanceof Area)
            {
                int provinceIndex = selectedProvince.getProvinceIndex();

                for (Province province : provinceList)
                {
                    if (province.getProvinceIndex() == provinceIndex)
                    {
                        selectedProvince = province;
                        break;
                    }
                }
            }

            GourmetCurationManager.getInstance().setProvince(selectedProvince);

            if (DailyDeepLink.getInstance().isValidateLink() == true//
                && processDeepLinkByRegionList(mBaseActivity, provinceList, areaList) == true)
            {

            } else
            {
                // 리스트 요청하면 됨.
                String dateText = GourmetCurationManager.getInstance().getSaleTime().getDayOfDaysDateFormat("M.d(EEE)");
                mPlaceMainLayout.setToolbarDateText(dateText);
                mPlaceMainLayout.setCategoryTabLayout(getFragmentManager(), new ArrayList<Category>(), null, new PlaceListFragment.OnPlaceListFragmentListener()
                {
                    @Override
                    public void onPlaceClick(PlaceViewItem placeViewItem, SaleTime saleTime)
                    {

                    }

                    @Override
                    public void onEventBannerClick(EventBanner eventBanner)
                    {

                    }

                    @Override
                    public void onAttach()
                    {
                        mPlaceMainLayout.getCurrentPlaceListFragment().refreshList();
                    }
                });
            }
        }

        @Override
        public void onErrorResponse(VolleyError volleyError)
        {
            mBaseActivity.onErrorResponse(volleyError);
        }

        @Override
        public void onError(Exception e)
        {
            mBaseActivity.onError(e);
        }

        @Override
        public void onErrorPopupMessage(int msgCode, String message)
        {
            mBaseActivity.onErrorPopupMessage(msgCode, message);
        }

        @Override
        public void onErrorToastMessage(String message)
        {
            mBaseActivity.onErrorToastMessage(message);
        }

        private boolean processDeepLinkByDateTime(BaseActivity baseActivity)
        {
            if (DailyDeepLink.getInstance().isGourmetDetailView() == true)
            {
                unLockUI();

                return moveDeepLinkDetail(baseActivity);
            } else if (DailyDeepLink.getInstance().isGourmetEventBannerWebView() == true)
            {
                unLockUI();

                return moveDeepLinkEventBannerWeb(baseActivity);
            } else if (DailyDeepLink.getInstance().isGourmetRegionListView() == true)
            {
                unLockUI();

                return moveDeepLinkRegionList(baseActivity);
            } else
            {
                // 더이상 진입은 없다.
                if (DailyDeepLink.getInstance().isGourmetListView() == false)
                {
                    DailyDeepLink.getInstance().clear();
                }
            }

            return false;
        }

        private boolean processDeepLinkByRegionList(BaseActivity baseActivity, List<Province> provinceList, List<Area> areaList)
        {
            if (DailyDeepLink.getInstance().isGourmetListView() == true)
            {
                unLockUI();

                return moveDeepLinkGourmetList(provinceList, areaList);
            } else
            {
                DailyDeepLink.getInstance().clear();
            }

            return false;
        }

        private Province searchLastRegion(BaseActivity baseActivity, List<Province> provinceList, List<Area> areaList)
        {
            Province selectedProvince = null;

            // 마지막으로 선택한 지역을 가져온다.
            String regionName = DailyPreference.getInstance(baseActivity).getSelectedRegion(PlaceType.FNB);

            if (Util.isTextEmpty(regionName) == true)
            {
                selectedProvince = provinceList.get(0);
            }

            if (selectedProvince == null)
            {
                for (Province province : provinceList)
                {
                    if (province.name.equals(regionName) == true)
                    {
                        selectedProvince = province;
                        break;
                    }
                }

                if (selectedProvince == null)
                {
                    for (Area area : areaList)
                    {
                        if (area.name.equals(regionName) == true)
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

                            selectedProvince = area;
                            break;
                        }
                    }
                }
            }

            return selectedProvince;
        }
    };

    private PlaceListFragment.OnPlaceListFragmentListener mOnPlaceListFragmentListener = new PlaceListFragment.OnPlaceListFragmentListener()
    {
        @Override
        public void onPlaceClick(PlaceViewItem placeViewItem, SaleTime saleTime)
        {

        }

        @Override
        public void onEventBannerClick(EventBanner eventBanner)
        {

        }

        @Override
        public void onAttach()
        {

        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Deep Link
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void startGourmetDetailByDeepLink(int gourmetIndex, long dailyTime, int dailyDayOfDays)
    {
        BaseActivity baseActivity = (BaseActivity) mBaseActivity;

        if (baseActivity == null || gourmetIndex < 0)
        {
            return;
        }

        if (isLockUiComponent() == true || baseActivity.isLockUiComponent() == true)
        {
            return;
        }

        lockUI();

        Intent intent = new Intent(baseActivity, GourmetDetailActivity.class);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_TYPE, "share");
        intent.putExtra(NAME_INTENT_EXTRA_DATA_PLACEIDX, gourmetIndex);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_DAILYTIME, dailyTime);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_DAYOFDAYS, dailyDayOfDays);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_NIGHTS, 1);

        baseActivity.startActivityForResult(intent, CODE_REQUEST_ACTIVITY_PLACE_DETAIL);
    }

    private boolean moveDeepLinkDetail(BaseActivity baseActivity)
    {
        try
        {
            // 신규 타입의 화면이동
            SaleTime saleTime = GourmetCurationManager.getInstance().getSaleTime();
            int gourmetIndex = Integer.parseInt(DailyDeepLink.getInstance().getIndex());
            long dailyTime = saleTime.getDailyTime();

            String date = DailyDeepLink.getInstance().getDate();
            int datePlus = DailyDeepLink.getInstance().getDatePlus();

            // date가 비어 있는 경우
            if (Util.isTextEmpty(date) == true)
            {
                if (datePlus >= 0)
                {
                    startGourmetDetailByDeepLink(gourmetIndex, dailyTime, datePlus);
                } else
                {
                    return false;
                }
            } else
            {
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
                Date schemeDate = format.parse(date);
                Date dailyDate = format.parse(saleTime.getDayOfDaysDateFormat("yyyyMMdd"));

                int dailyDayOfDays = (int) ((schemeDate.getTime() - dailyDate.getTime()) / SaleTime.MILLISECOND_IN_A_DAY);

                if (dailyDayOfDays < 0)
                {
                    return false;
                }

                startGourmetDetailByDeepLink(gourmetIndex, dailyTime, dailyDayOfDays);
            }

            mIsDeepLink = true;
        } catch (Exception e)
        {
            ExLog.d(e.toString());
            return false;
        } finally
        {
            DailyDeepLink.getInstance().clear();
        }

        return true;
    }

    private boolean moveDeepLinkEventBannerWeb(BaseActivity baseActivity)
    {
        String url = DailyDeepLink.getInstance().getUrl();
        DailyDeepLink.getInstance().clear();

        if (Util.isTextEmpty(url) == false)
        {
            Intent intent = EventWebActivity.newInstance(baseActivity, EventWebActivity.SourceType.GOURMET_BANNER, url, null, GourmetCurationManager.getInstance().getSaleTime());
            baseActivity.startActivityForResult(intent, CODE_REQUEST_ACTIVITY_EVENTWEB);
            mIsDeepLink = true;

            return true;
        } else
        {
            return false;
        }
    }

    private boolean moveDeepLinkRegionList(BaseActivity baseActivity)
    {
        int provinceIndex = -1;
        int areaIndex = -1;

        try
        {
            provinceIndex = Integer.parseInt(DailyDeepLink.getInstance().getProvinceIndex());
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }

        try
        {
            areaIndex = Integer.parseInt(DailyDeepLink.getInstance().getAreaIndex());
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }

        Intent intent = GourmetRegionListActivity.newInstance(baseActivity, provinceIndex, areaIndex, GourmetCurationManager.getInstance().getSaleTime());
        startActivityForResult(intent, CODE_REQUEST_ACTIVITY_REGIONLIST);

        DailyDeepLink.getInstance().clear();
        mIsDeepLink = true;
        return true;
    }

    private boolean moveDeepLinkGourmetList(List<Province> provinceList, List<Area> areaList)
    {
        String date = DailyDeepLink.getInstance().getDate();
        int datePlus = DailyDeepLink.getInstance().getDatePlus();
        GourmetCurationManager.getInstance().getGourmetCurationOption().setSortType(DailyDeepLink.getInstance().getSorting());

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

        // 지역이 있는 경우 지역을 디폴트로 잡아주어야 한다
        Province selectedProvince = searchDeeLinkRegion(provinceIndex, areaIndex, provinceList, areaList);

        if (selectedProvince == null)
        {
            selectedProvince = GourmetCurationManager.getInstance().getProvince();
        }

        GourmetCurationManager.getInstance().setProvince(selectedProvince);
        mPlaceMainLayout.setToolbarRegionText(selectedProvince.name);
        DailyDeepLink.getInstance().clear();

        // 날짜가 있는 경우 디폴트로 3번째 탭으로 넘어가야 한다
        if (Util.isTextEmpty(date) == false)
        {
            try
            {
                SimpleDateFormat format = new java.text.SimpleDateFormat("yyyyMMdd");
                SaleTime saleTime = GourmetCurationManager.getInstance().getSaleTime();
                Date schemeDate = format.parse(date);
                Date dailyDate = format.parse(saleTime.getDayOfDaysDateFormat("yyyyMMdd"));

                int dailyDayOfDays = (int) ((schemeDate.getTime() - dailyDate.getTime()) / SaleTime.MILLISECOND_IN_A_DAY);

                if (dailyDayOfDays >= 0)
                {
                    SaleTime deepLinkSaleTime = saleTime.getClone(dailyDayOfDays);

                    // 리스트 요청하면 됨.
                    String dateText = deepLinkSaleTime.getDayOfDaysDateFormat("M.d(EEE)");
                    mPlaceMainLayout.setToolbarDateText(dateText);

                    GourmetCurationManager.getInstance().setSaleTime(deepLinkSaleTime);

                    // 특정 날짜 고메 리스트 요청
                    refreshCurrentFragment();
                } else
                {
                    return false;
                }
            } catch (Exception e)
            {
                return false;
            }
        } else if (datePlus >= 0)
        {
            SaleTime saleTime = GourmetCurationManager.getInstance().getSaleTime();

            try
            {
                SaleTime deepLinkSaleTime = saleTime.getClone(datePlus);

                // 리스트 요청하면 됨.
                String dateText = deepLinkSaleTime.getDayOfDaysDateFormat("M.d(EEE)");
                mPlaceMainLayout.setToolbarDateText(dateText);

                GourmetCurationManager.getInstance().setSaleTime(deepLinkSaleTime);

                refreshCurrentFragment();
            } catch (Exception e)
            {
                return false;
            }
        } else
        {
            return false;
        }

        return true;
    }

    private Province searchDeeLinkRegion(int provinceIndex, int areaIndex, List<Province> provinceList, List<Area> areaList)
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
                    if (province.index == provinceIndex)
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
}
