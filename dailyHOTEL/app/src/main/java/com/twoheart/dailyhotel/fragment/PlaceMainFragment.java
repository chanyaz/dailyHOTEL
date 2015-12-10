package com.twoheart.dailyhotel.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.twoheart.dailyhotel.MainActivity;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.activity.BaseActivity;
import com.twoheart.dailyhotel.model.Area;
import com.twoheart.dailyhotel.model.AreaItem;
import com.twoheart.dailyhotel.model.Province;
import com.twoheart.dailyhotel.model.SaleTime;
import com.twoheart.dailyhotel.network.DailyNetworkAPI;
import com.twoheart.dailyhotel.network.response.DailyHotelJsonResponseListener;
import com.twoheart.dailyhotel.util.ExLog;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.view.PlaceViewItem;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public abstract class PlaceMainFragment extends BaseFragment
{
    protected SaleTime mTodaySaleTime;
    protected OnUserActionListener mOnUserActionListener;
    protected VIEW_TYPE mViewType = VIEW_TYPE.LIST;
    protected boolean mMapEnabled;

    private boolean mMenuEnabled;
    private boolean mDontReloadAtOnResume;

    public enum VIEW_TYPE
    {
        LIST,
        MAP,
        GONE, // 목록이 비어있는 경우.
    }

    public enum TYPE
    {
        HOTEL,
        FNB, // 절대로 바꾸면 안됨 서버에서 fnb로 내려옴
    }

    public interface OnUserActionListener
    {
        public void selectPlace(PlaceViewItem baseListViewItem, SaleTime checkSaleTime);

        public void selectPlace(int index, long dailyTime, int dailyDayOfDays, int nights);

        public void selectDay(SaleTime checkInSaleTime, boolean isListSelectionTop);

        public void toggleViewType();

        public void onClickActionBarArea();

        public void setMapViewVisible(boolean isVisible);

        public void refreshAll();
    }

    protected abstract View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    protected abstract void activityResult(int requestCode, int resultCode, Intent data);

    protected abstract void hideSlidingDrawer();

    protected abstract void showSlidingDrawer();

    protected abstract void onNavigationItemSelected(Province province, boolean isSelectionTop);

    protected abstract void setNavigationItemSelected(Province province);

    protected abstract void requestProvinceList(BaseActivity baseActivity);

    protected abstract void refreshList(Province province, boolean isSelectionTop);

    protected abstract void setActionBarAnimationLock(boolean enabled);

    protected abstract boolean isEnabledRegionMenu();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mViewType = VIEW_TYPE.LIST;

        mTodaySaleTime = new SaleTime();

        View view = createView(inflater, container, savedInstanceState);

        setHasOptionsMenu(true);//프래그먼트 내에서 옵션메뉴를 지정하기 위해

        hideSlidingDrawer();

        return view;
    }

    @Override
    public void onResume()
    {
        BaseActivity baseActivity = (BaseActivity) getActivity();

        if (baseActivity == null)
        {
            return;
        }

        if (mDontReloadAtOnResume == true)
        {
            mDontReloadAtOnResume = false;
        } else
        {
            lockUI();
            DailyNetworkAPI.getInstance().requestCommonDatetime(mNetworkTag, mDateTimeJsonResponseListener, baseActivity);
        }

        super.onResume();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        BaseActivity baseActivity = (BaseActivity) getActivity();

        if (baseActivity == null)
        {
            return;
        }

        MenuInflater inflater = baseActivity.getMenuInflater();

        menu.clear();

        if (mMenuEnabled == true)
        {
            switch (mViewType)
            {
                case LIST:
                    inflater.inflate(R.menu.actionbar_icon_map, menu);
                    break;

                case MAP:
                    inflater.inflate(R.menu.actionbar_icon_list, menu);
                    break;

                default:
                    break;
            }
        }
    }

    public void setMenuEnabled(boolean enabled)
    {
        BaseActivity baseActivity = (BaseActivity) getActivity();

        if (baseActivity == null || (enabled == true && mMapEnabled == false))
        {
            return;
        }

        if (enabled == true)
        {
            baseActivity.setActionBarRegionEnable(isEnabledRegionMenu());
        }

        if (mMenuEnabled == enabled)
        {
            return;
        }

        mMenuEnabled = enabled;

        baseActivity.invalidateOptionsMenu();

        // 메뉴가 열리는 시점이다.
        setActionBarAnimationLock(enabled);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        BaseActivity baseActivity = (BaseActivity) getActivity();

        if (baseActivity == null)
        {
            return false;
        }

        switch (item.getItemId())
        {
            case R.id.action_list:
            {
                boolean isInstalledGooglePlayServices = Util.installGooglePlayService(baseActivity);

                if (isInstalledGooglePlayServices == true)
                {
                    if (mOnUserActionListener != null)
                    {
                        mOnUserActionListener.toggleViewType();
                    }

                    baseActivity.invalidateOptionsMenu();
                }
                return true;
            }

            case R.id.action_map:
            {
                boolean isInstalledGooglePlayServices = Util.installGooglePlayService(baseActivity);

                if (isInstalledGooglePlayServices == true)
                {
                    if (mOnUserActionListener != null)
                    {
                        mOnUserActionListener.toggleViewType();
                    }

                    baseActivity.invalidateOptionsMenu();
                }
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        BaseActivity baseActivity = (BaseActivity) getActivity();

        if (baseActivity == null)
        {
            return;
        }

        unLockUI();

        switch (requestCode)
        {
            case CODE_REQUEST_FRAGMENT_PLACE_MAIN:
            {
                if (resultCode == Activity.RESULT_OK)
                {
                    ((MainActivity) baseActivity).selectMenuDrawer(((MainActivity) baseActivity).menuBookingListFragment);
                } else if (resultCode == CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_READY)
                {
                    ((MainActivity) baseActivity).selectMenuDrawer(((MainActivity) baseActivity).menuBookingListFragment);
                }
                break;
            }

            // 지역을 선택한 후에 되돌아 온경우.
            case CODE_REQUEST_ACTIVITY_SELECT_AREA:
            {
                mDontReloadAtOnResume = true;

                if (resultCode == Activity.RESULT_OK)
                {
                    if (data != null)
                    {
                        if (data.hasExtra(NAME_INTENT_EXTRA_DATA_PROVINCE) == true)
                        {
                            Province province = data.getParcelableExtra(NAME_INTENT_EXTRA_DATA_PROVINCE);

                            setNavigationItemSelected(province);

                            if (mOnUserActionListener != null)
                            {
                                mOnUserActionListener.refreshAll();
                            }
                        } else if (data.hasExtra(NAME_INTENT_EXTRA_DATA_AREA) == true)
                        {
                            Province province = data.getParcelableExtra(NAME_INTENT_EXTRA_DATA_AREA);

                            setNavigationItemSelected(province);

                            if (mOnUserActionListener != null)
                            {
                                mOnUserActionListener.refreshAll();
                            }
                        }
                    }
                }
                break;
            }

            default:
            {
                activityResult(requestCode, resultCode, data);
                break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    protected ArrayList<AreaItem> makeAreaItemList(ArrayList<Province> provinceList, ArrayList<Area> areaList)
    {
        ArrayList<AreaItem> arrayList = new ArrayList<AreaItem>(provinceList.size());

        for (Province province : provinceList)
        {
            AreaItem item = new AreaItem();

            item.setProvince(province);
            item.setAreaList(new ArrayList<Area>());

            if (areaList != null)
            {
                for (Area area : areaList)
                {
                    if (province.getProvinceIndex() == area.getProvinceIndex())
                    {
                        ArrayList<Area> areaArrayList = item.getAreaList();

                        if (areaArrayList.size() == 0)
                        {
                            Area totalArea = new Area();

                            totalArea.index = -1;
                            totalArea.name = province.name + " 전체";
                            totalArea.setProvince(province);
                            totalArea.sequence = -1;
                            totalArea.tag = totalArea.name;
                            totalArea.setProvinceIndex(province.getProvinceIndex());

                            areaArrayList.add(totalArea);
                        }

                        area.setProvince(province);
                        areaArrayList.add(area);
                    }
                }
            }

            arrayList.add(item);
        }

        return arrayList;
    }

    protected void setOnUserActionListener(OnUserActionListener listener)
    {
        mOnUserActionListener = listener;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UserActionListener
    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    // NetworkActionListener
    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected DailyHotelJsonResponseListener mDateTimeJsonResponseListener = new DailyHotelJsonResponseListener()
    {
        @Override
        public void onResponse(String url, JSONObject response)
        {
            BaseActivity baseActivity = (BaseActivity) getActivity();

            if (baseActivity == null)
            {
                return;
            }

            try
            {
                if (response == null)
                {
                    throw new NullPointerException("response == null");
                }

                mTodaySaleTime.setCurrentTime(response.getLong("currentDateTime"));
                mTodaySaleTime.setDailyTime(response.getLong("dailyDateTime"));

                showSlidingDrawer();

                if (baseActivity.sharedPreference.contains(KEY_PREFERENCE_BY_SHARE) == true)
                {
                    String param = baseActivity.sharedPreference.getString(KEY_PREFERENCE_BY_SHARE, null);
                    baseActivity.sharedPreference.edit().remove(KEY_PREFERENCE_BY_SHARE).apply();

                    if (param != null)
                    {
                        unLockUI();

                        try
                        {
                            String previousType = Util.getValueForLinkUrl(param, "fnbIndex");

                            if (Util.isTextEmpty(previousType) == false)
                            {
                                // 이전 타입의 화면 이동
                                int fnbIndex = Integer.parseInt(previousType);
                                long dailyTime = Long.parseLong(Util.getValueForLinkUrl(param, "dailyTime"));
                                int dailyDayOfDays = Integer.parseInt(Util.getValueForLinkUrl(param, "dailyDayOfDays"));
                                int nights = Integer.parseInt(Util.getValueForLinkUrl(param, "nights"));

                                if (nights != 1 || dailyDayOfDays < 0)
                                {
                                    throw new NullPointerException("nights != 1 || dailyDayOfDays < 0");
                                }

                                if (mOnUserActionListener != null)
                                {
                                    mOnUserActionListener.selectPlace(fnbIndex, dailyTime, dailyDayOfDays, nights);
                                }

                            } else
                            {
                                // 신규 타입의 화면이동
                                int fnbIndex = Integer.parseInt(Util.getValueForLinkUrl(param, "idx"));
                                long dailyTime = mTodaySaleTime.getDailyTime();
                                int nights = Integer.parseInt(Util.getValueForLinkUrl(param, "nights"));

                                String date = Util.getValueForLinkUrl(param, "date");
                                SimpleDateFormat format = new java.text.SimpleDateFormat("yyyyMMdd");
                                Date schemeDate = format.parse(date);
                                Date dailyDate = format.parse(mTodaySaleTime.getDayOfDaysHotelDateFormat("yyyyMMdd"));

                                int dailyDayOfDays = (int) ((schemeDate.getTime() - dailyDate.getTime()) / SaleTime.MILLISECOND_IN_A_DAY);

                                if (nights != 1 || dailyDayOfDays < 0)
                                {
                                    throw new NullPointerException("nights != 1 || dailyDayOfDays < 0");
                                }

                                if (mOnUserActionListener != null)
                                {
                                    mOnUserActionListener.selectPlace(fnbIndex, dailyTime, dailyDayOfDays, nights);
                                }
                            }
                        } catch (Exception e)
                        {
                            ExLog.d(e.toString());

                            // 지역 리스트를 가져온다
                            requestProvinceList(baseActivity);
                        }
                    }
                } else
                {
                    // 지역 리스트를 가져온다
                    requestProvinceList(baseActivity);
                }
            } catch (Exception e)
            {
                onError(e);
                unLockUI();
            }
        }
    };
}
