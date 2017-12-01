package com.daily.dailyhotel.screen.common.area.stay;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.View;

import com.daily.base.BaseActivity;
import com.daily.base.BaseAnalyticsInterface;
import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ExLog;
import com.daily.dailyhotel.base.BaseExceptionPresenter;
import com.daily.dailyhotel.entity.StayArea;
import com.daily.dailyhotel.entity.StayAreaGroup;
import com.daily.dailyhotel.entity.StayBookDateTime;
import com.daily.dailyhotel.entity.StayRegion;
import com.daily.dailyhotel.parcel.StayRegionParcel;
import com.daily.dailyhotel.repository.remote.StayRemoteImpl;
import com.daily.dailyhotel.storage.preference.DailyPreference;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.DailyCategoryType;
import com.twoheart.dailyhotel.screen.common.PermissionManagerActivity;
import com.twoheart.dailyhotel.screen.search.SearchActivity;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.DailyCalendar;
import com.twoheart.dailyhotel.util.DailyLocationFactory;

import org.json.JSONObject;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Created by sheldon
 * Clean Architecture
 */
public class StayAreaListPresenter extends BaseExceptionPresenter<StayAreaListActivity, StayAreaListInterface> implements StayAreaListView.OnEventListener
{
    private StayAreaListAnalyticsInterface mAnalytics;

    private StayRemoteImpl mStayRemoteImpl;

    private StayBookDateTime mStayBookDateTime;

    private String mCategoryCode;
    private List<StayAreaGroup> mAreaGroupList;
    private int mAreaGroupPosition = -1;
    private DailyCategoryType mDailyCategoryType;
    private StayRegion mStayRegion; // 기존에 저장된 정보

    DailyLocationFactory mDailyLocationFactory;

    public interface StayAreaListAnalyticsInterface extends BaseAnalyticsInterface
    {
        void onScreen(Activity activity, String categoryCode);

        void onEventSearchClick(Activity activity, DailyCategoryType dailyCategoryType);

        void onEventChangedDistrictClick(Activity activity, String previousDistrictName, String previousTownName//
            , String changedDistrictName, String changedTownName, StayBookDateTime stayBookDateTime);

        void onEventChangedDateClick(Activity activity);

        void onEventTownClick(Activity activity, String districtName, String townName);

        void onEventClosedClick(Activity activity, String stayCategory);

        void onEventAroundSearchClick(Activity activity, DailyCategoryType dailyCategoryType);
    }

    public StayAreaListPresenter(@NonNull StayAreaListActivity activity)
    {
        super(activity);
    }

    @NonNull
    @Override
    protected StayAreaListInterface createInstanceViewInterface()
    {
        return new StayAreaListView(getActivity(), this);
    }

    @Override
    public void constructorInitialize(StayAreaListActivity activity)
    {
        setContentView(R.layout.activity_stay_area_list_data);

        setAnalytics(new StayAreaListAnalyticsImpl());

        mStayRemoteImpl = new StayRemoteImpl(activity);

        setRefresh(true);
    }

    @Override
    public void setAnalytics(BaseAnalyticsInterface analytics)
    {
        mAnalytics = (StayAreaListAnalyticsInterface) analytics;
    }

    @Override
    public boolean onIntent(Intent intent)
    {
        if (intent == null)
        {
            return true;
        }

        String checkInDateTime = intent.getStringExtra(StayAreaListActivity.INTENT_EXTRA_DATA_CHECK_IN_DATE_TIME);
        String checkOutDateTime = intent.getStringExtra(StayAreaListActivity.INTENT_EXTRA_DATA_CHECK_OUT_DATE_TIME);

        mStayBookDateTime = new StayBookDateTime();
        mStayBookDateTime.getCheckInDateTime(checkInDateTime);
        mStayBookDateTime.getCheckOutDateTime(checkOutDateTime);

        // 카테고리로 넘어오는 경우
        mDailyCategoryType = DailyCategoryType.valueOf(intent.getStringExtra(StayAreaListActivity.INTENT_EXTRA_DATA_STAY_CATEGORY));


        // 이름으로 넘어오는 경우


        mCategoryCode = intent.getStringExtra(StayAreaListActivity.INTENT_EXTRA_DATA_CATEGORY_CODE);

        return true;
    }

    @Override
    public void onPostCreate()
    {
        getViewInterface().setToolbarTitle(getString(R.string.label_selectarea_stay_area));
        getViewInterface().setLocationTermVisible(DailyPreference.getInstance(getActivity()).isAgreeTermsOfLocation() == false);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        if (isRefresh() == true)
        {
            onRefresh(true);
        }

        if (mDailyCategoryType == DailyCategoryType.STAY_ALL)
        {
            mAnalytics.onScreen(getActivity(), mCategoryCode);
        } else
        {
            mAnalytics.onScreen(getActivity(), getString(mDailyCategoryType.getCodeResId()));
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (isRefresh() == true)
        {
            onRefresh(true);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onDestroy()
    {
        // 꼭 호출해 주세요.
        super.onDestroy();
    }

    @Override
    public boolean onBackPressed()
    {
        mAnalytics.onEventClosedClick(getActivity(), getString(mDailyCategoryType.getCodeResId()));

        return super.onBackPressed();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        unLockAll();

        switch (requestCode)
        {
            case StayAreaListActivity.REQUEST_CODE_PERMISSION_MANAGER:
                getViewInterface().setLocationTermVisible(false);

                if (resultCode == Activity.RESULT_OK)
                {
                    onAroundSearchClick();
                }
                break;

            case StayAreaListActivity.REQUEST_CODE_SEARCH:
                break;

            case StayAreaListActivity.REQUEST_CODE_SETTING_LOCATION:
                getViewInterface().setLocationTermVisible(false);

                onAroundSearchClick();
                break;
        }
    }

    @Override
    protected synchronized void onRefresh(boolean showProgress)
    {
        if (getActivity().isFinishing() == true || isRefresh() == false)
        {
            return;
        }

        setRefresh(false);
        screenLock(showProgress);

        addCompositeDisposable(mStayRemoteImpl.getRegionList(mDailyCategoryType).observeOn(AndroidSchedulers.mainThread()).flatMap(new Function<List<StayAreaGroup>, ObservableSource<Boolean>>()
        {
            @Override
            public ObservableSource<Boolean> apply(List<StayAreaGroup> areaGroupList) throws Exception
            {
                mAreaGroupList = areaGroupList;

                getViewInterface().setDistrictList(areaGroupList);

                Pair<String, String> namePair = getDistrictNTownNameByCategory(mDailyCategoryType);

                if (namePair != null)
                {
                    mAreaGroupPosition = getAreaGroupPosition(areaGroupList, namePair.first);
                } else
                {
                    mAreaGroupPosition = -1;
                }

                // 기존에 저장된 지역이 있는 경우
                if (mAreaGroupPosition >= 0)
                {
                    StayAreaGroup areaGroup = areaGroupList.get(mAreaGroupPosition);

                    mStayRegion = getRegion(areaGroup, namePair.second);
                } else
                {
                    // 기존에 저장된 지역이 없는 경우 첫번째 지역으로 한다.

                    StayAreaGroup areaGroup = areaGroupList.get(0);

                    mStayRegion = new StayRegion(areaGroup, new StayArea(areaGroup));

                    mAreaGroupPosition = 0;
                }

                return expandGroupWithAnimation(mAreaGroupPosition, false).subscribeOn(AndroidSchedulers.mainThread());
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
        {
            @Override
            public void accept(Boolean aBoolean) throws Exception
            {
                unLockAll();
            }
        }, new Consumer<Throwable>()
        {
            @Override
            public void accept(Throwable throwable) throws Exception
            {
                onHandleErrorAndFinish(throwable);
            }
        }));
    }

    @Override
    public void onBackClick()
    {
        getActivity().onBackPressed();
    }

    @Override
    public void onSearchClick()
    {
        if (lock() == true)
        {
            return;
        }

        Intent intent = SearchActivity.newInstance(getActivity(), Constants.PlaceType.HOTEL//
            , mStayBookDateTime.getCheckInDateTime(DailyCalendar.ISO_8601_FORMAT)//
            , mStayBookDateTime.getCheckOutDateTime(DailyCalendar.ISO_8601_FORMAT));
        startActivityForResult(intent, StayAreaListActivity.REQUEST_CODE_SEARCH);

        mAnalytics.onEventSearchClick(getActivity(), mDailyCategoryType);
    }

    @Override
    public void onAreaGroupClick(int groupPosition)
    {
        if (mAreaGroupList == null || mAreaGroupList.size() == 0 || groupPosition < 0 || lock() == true)
        {
            return;
        }

        // 하위 지역이 없으면 선택
        if (mAreaGroupList.get(groupPosition).getAreaCount() == 0)
        {
            onAreaClick(groupPosition, new StayArea(mAreaGroupList.get(groupPosition)));

            unLockAll();
        } else
        {
            // 하위 지역이 있으면 애니메이션
            if (mAreaGroupPosition == groupPosition)
            {
                addCompositeDisposable(collapseGroupWithAnimation(groupPosition, true).subscribeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
                {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception
                    {
                        mAreaGroupPosition = -1;

                        unLockAll();
                    }
                }, new Consumer<Throwable>()
                {
                    @Override
                    public void accept(Throwable throwable) throws Exception
                    {
                        unLockAll();

                        finish();
                    }
                }));
            } else
            {
                addCompositeDisposable(collapseGroupWithAnimation(mAreaGroupPosition, false).subscribeOn(AndroidSchedulers.mainThread()).flatMap(new Function<Boolean, ObservableSource<Boolean>>()
                {
                    @Override
                    public ObservableSource<Boolean> apply(Boolean aBoolean) throws Exception
                    {
                        return expandGroupWithAnimation(groupPosition, true).subscribeOn(AndroidSchedulers.mainThread());
                    }
                }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
                {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception
                    {
                        mAreaGroupPosition = groupPosition;

                        unLockAll();
                    }
                }, new Consumer<Throwable>()
                {
                    @Override
                    public void accept(Throwable throwable) throws Exception
                    {
                        unLockAll();
                    }
                }));
            }
        }
    }

    @Override
    public void onAreaClick(int groupPosition, StayArea area)
    {
        if (groupPosition < 0 || area == null)
        {
            finish();
            return;
        }

        StayAreaGroup areaGroup = mAreaGroupList.get(groupPosition);

        if (areaGroup == null)
        {
            finish();
            return;
        }

        final String areaGroupName = areaGroup.name;
        final String areaName = area.name;

        // 지역이 변경된 경우 팝업을 뛰어서 날짜 변경을 할것인지 물어본다.
        if (equalsAreaGroupName(mStayRegion, areaGroupName) == true)
        {
            setResult(Activity.RESULT_OK, mDailyCategoryType, areaGroup, area);
            finish();
        } else
        {
            String message = mStayBookDateTime.getCheckInDateTime("yyyy.MM.dd(EEE)") + "-" + mStayBookDateTime.getCheckOutDateTime("yyyy.MM.dd(EEE)") + "\n" + getString(R.string.message_region_search_date);
            final String previousAreaGroupName, previousAreaName;

            if (mStayRegion != null)
            {
                previousAreaGroupName = mStayRegion.getAreaGroupName();
                previousAreaName = mStayRegion.getAreaName();
            } else
            {
                previousAreaGroupName = null;
                previousAreaName = null;
            }

            getViewInterface().showSimpleDialog(getString(R.string.label_visit_date), message, getString(R.string.dialog_btn_text_yes), getString(R.string.label_region_change_date), new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    mAnalytics.onEventChangedDistrictClick(getActivity(), previousAreaGroupName, previousAreaName, areaGroupName, areaName, mStayBookDateTime);

                    setResult(Activity.RESULT_OK, mDailyCategoryType, areaGroup, area);
                    finish();
                }
            }, new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    mAnalytics.onEventChangedDistrictClick(getActivity(), previousAreaGroupName, previousAreaName, areaGroupName, areaName, mStayBookDateTime);
                    mAnalytics.onEventChangedDateClick(getActivity());

                    // 날짜 선택 화면으로 이동한다.
                    setResult(BaseActivity.RESULT_CODE_START_CALENDAR, mDailyCategoryType, areaGroup, area);
                    finish();
                }
            }, new DialogInterface.OnCancelListener()
            {
                @Override
                public void onCancel(DialogInterface dialog)
                {
                    unLockAll();
                }
            }, new DialogInterface.OnDismissListener()
            {
                @Override
                public void onDismiss(DialogInterface dialog)
                {
                    unLockAll();
                }
            }, true);
        }

        mAnalytics.onEventTownClick(getActivity(), areaGroupName, areaName);
    }

    @Override
    public void onAroundSearchClick()
    {
        if (lock() == true)
        {
            return;
        }

        if (mDailyLocationFactory == null)
        {
            mDailyLocationFactory = new DailyLocationFactory(getActivity());
        }

        if (mDailyLocationFactory.measuringLocation() == true)
        {
            return;
        }

        mDailyLocationFactory.checkLocationMeasure(new DailyLocationFactory.OnCheckLocationListener()
        {
            @Override
            public void onRequirePermission()
            {
                unLockAll();

                Intent intent = PermissionManagerActivity.newInstance(getActivity(), PermissionManagerActivity.PermissionType.ACCESS_FINE_LOCATION);
                startActivityForResult(intent, StayAreaListActivity.REQUEST_CODE_PERMISSION_MANAGER);
            }

            @Override
            public void onFailed()
            {
                unLockAll();
            }

            @Override
            public void onProviderEnabled()
            {
                unLockAll();

                setResult(BaseActivity.RESULT_CODE_START_AROUND_SEARCH, mDailyCategoryType, null, null);
                finish();
            }

            @Override
            public void onProviderDisabled()
            {
                unLockAll();

                // 현재 GPS 설정이 꺼져있습니다 설정에서 바꾸어 주세요.
                mDailyLocationFactory.stopLocationMeasure();

                getViewInterface().showSimpleDialog(getString(R.string.dialog_title_used_gps)//
                    , getString(R.string.dialog_msg_used_gps)//
                    , getString(R.string.dialog_btn_text_dosetting)//
                    , getString(R.string.dialog_btn_text_cancel)//
                    , new View.OnClickListener()//
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, StayAreaListActivity.REQUEST_CODE_SETTING_LOCATION);
                        }
                    }, null, false);
            }
        });

        mAnalytics.onEventAroundSearchClick(getActivity(), mDailyCategoryType);
    }

    /**
     * first : District 이름
     * second : Town 이름
     *
     * @param dailyCategoryType
     * @return
     */
    private Pair<String, String> getDistrictNTownNameByCategory(DailyCategoryType dailyCategoryType)
    {
        if (dailyCategoryType == null)
        {
            return null;
        }

        JSONObject jsonObject = DailyPreference.getInstance(getActivity()).getDailyRegion(dailyCategoryType);

        if (jsonObject == null)
        {
            return null;
        }

        try
        {
            return new Pair<>(jsonObject.getString(Constants.JSON_KEY_PROVINCE_NAME), jsonObject.getString(Constants.JSON_KEY_AREA_NAME));

        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }

        return null;
    }

    private void setCategoryRegion(DailyCategoryType dailyCategoryType, String districtName, String townName)
    {
        if (dailyCategoryType == null)
        {
            return;
        }

        JSONObject jsonObject;
        try
        {
            jsonObject = new JSONObject();
            jsonObject.put(Constants.JSON_KEY_PROVINCE_NAME, DailyTextUtils.isTextEmpty(districtName) ? "" : districtName);
            jsonObject.put(Constants.JSON_KEY_AREA_NAME, DailyTextUtils.isTextEmpty(townName) ? "" : townName);
            jsonObject.put(Constants.JSON_KEY_IS_OVER_SEAS, false);
        } catch (Exception e)
        {
            ExLog.e(e.toString());
            jsonObject = null;
        }

        DailyPreference.getInstance(getActivity()).setDailyRegion(dailyCategoryType, jsonObject);
    }

    private Observable<Boolean> collapseGroupWithAnimation(int groupPosition, boolean animation)
    {
        Observable<Boolean> observable = getViewInterface().collapseGroupWithAnimation(groupPosition, animation);

        if (observable == null)
        {
            observable = Observable.just(true);
        }

        return observable;
    }

    private Observable<Boolean> expandGroupWithAnimation(int groupPosition, boolean animation)
    {
        Observable<Boolean> observable = getViewInterface().expandGroupWithAnimation(groupPosition, animation);

        if (observable == null)
        {
            observable = Observable.just(true);
        }

        return observable;
    }

    private void setResult(int resultCode, DailyCategoryType categoryType, StayArea areaGroup, StayArea area)
    {
        if (categoryType == null)
        {
            return;
        }

        Intent intent = new Intent();

        if (areaGroup != null && area != null)
        {
            setCategoryRegion(categoryType, areaGroup.name, area.name);

            intent.putExtra(StayAreaListActivity.INTENT_EXTRA_DATA_REGION, new StayRegionParcel(new StayRegion(areaGroup, area)));
        }

        intent.putExtra(StayAreaListActivity.INTENT_EXTRA_DATA_STAY_CATEGORY, categoryType.name());

        if (areaGroup != null)
        {
            if (mStayRegion == null)
            {
                intent.putExtra(StayAreaListActivity.INTENT_EXTRA_DATA_CHANGED_AREA_GROUP, true);
            } else
            {
                if (areaGroup.name.equalsIgnoreCase(mStayRegion.getAreaGroupName()) == true)
                {
                    intent.putExtra(StayAreaListActivity.INTENT_EXTRA_DATA_CHANGED_AREA_GROUP, false);
                } else
                {
                    intent.putExtra(StayAreaListActivity.INTENT_EXTRA_DATA_CHANGED_AREA_GROUP, true);
                }
            }
        } else
        {
            intent.putExtra(StayAreaListActivity.INTENT_EXTRA_DATA_CHANGED_AREA_GROUP, true);
        }

        setResult(resultCode, intent);
    }

    private int getAreaGroupPosition(List<StayAreaGroup> areaGroupList, String areaName)
    {
        if (areaGroupList == null || areaGroupList.size() == 0 || DailyTextUtils.isTextEmpty(areaName) == true)
        {
            return -1;
        }

        int size = areaGroupList.size();

        for (int i = 0; i < size; i++)
        {
            if (areaGroupList.get(i).name.equalsIgnoreCase(areaName) == true)
            {
                return i;
            }
        }

        return -1;
    }

    private StayRegion getRegion(StayAreaGroup areaGroup, String areaName)
    {
        if (areaGroup == null || DailyTextUtils.isTextEmpty(areaName) == true)
        {
            return null;
        }

        if (areaGroup.getAreaCount() == 0)
        {
            return new StayRegion(areaGroup, new StayArea(areaGroup));
        } else
        {
            for (StayArea area : areaGroup.getAreaList())
            {
                if (area.name.equalsIgnoreCase(areaName) == true)
                {
                    return new StayRegion(areaGroup, area);
                }
            }
        }

        return null;
    }

    private boolean equalsAreaGroupName(StayRegion stayRegion, String areaName)
    {
        if (stayRegion == null || DailyTextUtils.isTextEmpty(areaName) == true)
        {
            return false;
        }

        return areaName.equalsIgnoreCase(stayRegion.getAreaGroupName());
    }
}