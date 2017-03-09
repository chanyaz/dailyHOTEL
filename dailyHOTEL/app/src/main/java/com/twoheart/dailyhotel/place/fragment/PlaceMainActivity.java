package com.twoheart.dailyhotel.place.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.PlaceCuration;
import com.twoheart.dailyhotel.place.base.BaseActivity;
import com.twoheart.dailyhotel.place.layout.PlaceMainLayout;
import com.twoheart.dailyhotel.place.networkcontroller.PlaceMainNetworkController;
import com.twoheart.dailyhotel.screen.common.PermissionManagerActivity;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.DailyLocationFactory;
import com.twoheart.dailyhotel.util.ExLog;
import com.twoheart.dailyhotel.util.Util;

public abstract class PlaceMainActivity extends BaseActivity
{
    protected boolean mDontReloadAtOnResume, mIsDeepLink;
    protected ViewType mViewType = ViewType.LIST;

    protected PlaceMainLayout mPlaceMainLayout;
    protected PlaceMainNetworkController mPlaceMainNetworkController;

    protected abstract PlaceMainLayout getPlaceMainLayout(Context context);

    protected abstract PlaceMainNetworkController getPlaceMainNetworkController(Context context);

    protected abstract void onRegionActivityResult(int resultCode, Intent data);

    protected abstract void onCalendarActivityResult(int resultCode, Intent data);

    protected abstract void onCurationActivityResult(int resultCode, Intent data);

    protected abstract void onLocationFailed();

    protected abstract void onLocationProviderDisabled();

    protected abstract void onLocationChanged(Location location);

    protected abstract PlaceCuration getPlaceCuration();

    protected abstract void changeViewType();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        overridePendingTransition(R.anim.slide_in_right, R.anim.hold);

        super.onCreate(savedInstanceState);

        mPlaceMainLayout = getPlaceMainLayout(this);
        mPlaceMainNetworkController = getPlaceMainNetworkController(this);

        setContentView(mPlaceMainLayout.onCreateView(R.layout.activity_place_main));
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (isFinishing() == true)
        {
            return;
        }

        if (mDontReloadAtOnResume == true)
        {
            mDontReloadAtOnResume = false;
        } else
        {
            lockUI();
            mPlaceMainNetworkController.requestDateTime();
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();

        mDontReloadAtOnResume = true;

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                mPlaceMainLayout.showAppBarLayout(false);
            }
        }, 200);
    }

    @Override
    public void onBackPressed()
    {
        // 맵인 경우에 하단에 정보를 보고 있으면 백키를 누를 경우 정보를 사라지게 해준다.(편의성 제공)
        if (mViewType == ViewType.MAP)
        {
            try
            {
                PlaceListFragment placeListFragment = mPlaceMainLayout.getCurrentPlaceListFragment();

                if (mPlaceMainLayout.getCurrentPlaceListFragment().getPlaceListLayout().getListMapFragment().isShowPlaceInformation() == true)
                {
                    mPlaceMainLayout.getCurrentPlaceListFragment().getPlaceListLayout().getListMapFragment().clickMap();
                } else
                {
                    changeViewType();
                }

                return;
            } catch (Exception e)
            {
                ExLog.d(e.toString());
            }
        }

        super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (isFinishing() == true)
        {
            return;
        }

        unLockUI();

        switch (requestCode)
        {
            // 지역을 선택한 후에 되돌아 온경우.
            case CODE_REQUEST_ACTIVITY_REGIONLIST:
            {
                if (mIsDeepLink == false)
                {
                    mDontReloadAtOnResume = true;
                } else
                {
                    mDontReloadAtOnResume = false;
                    mIsDeepLink = false;
                }

                onRegionActivityResult(resultCode, data);
                break;
            }

            case CODE_REQUEST_ACTIVITY_CALENDAR:
            {
                mDontReloadAtOnResume = true;

                onCalendarActivityResult(resultCode, data);
                break;
            }

            case CODE_REQUEST_ACTIVITY_STAYCURATION:
            {
                mDontReloadAtOnResume = true;

                onCurationActivityResult(resultCode, data);
                break;
            }

            case CODE_REQUEST_ACTIVITY_GOURMETCURATION:
            {
                mDontReloadAtOnResume = true;

                onCurationActivityResult(resultCode, data);
                break;
            }

            case CODE_RESULT_ACTIVITY_SETTING_LOCATION:
            {
                mDontReloadAtOnResume = true;

                if (mViewType == ViewType.MAP)
                {
                    PlaceListFragment placeListFragment = mPlaceMainLayout.getCurrentPlaceListFragment();
                    placeListFragment.onActivityResult(requestCode, resultCode, data);
                } else
                {
                    searchMyLocation();
                }
                break;
            }

            case Constants.CODE_REQUEST_ACTIVITY_PERMISSION_MANAGER:
            {
                mDontReloadAtOnResume = true;

                if (mViewType == ViewType.MAP)
                {
                    PlaceListFragment placeListFragment = mPlaceMainLayout.getCurrentPlaceListFragment();
                    placeListFragment.onActivityResult(requestCode, resultCode, data);
                } else
                {
                    if (resultCode == Activity.RESULT_OK)
                    {
                        searchMyLocation();
                    } else
                    {
                        onLocationFailed();
                    }
                }
                break;
            }

            case CODE_REQUEST_ACTIVITY_EVENTWEB:
            case CODE_REQUEST_ACTIVITY_GOURMET_DETAIL:
            case CODE_REQUEST_ACTIVITY_STAY_DETAIL:
            case CODE_REQUEST_ACTIVITY_SEARCH:
            case CODE_REQUEST_ACTIVITY_SEARCH_RESULT:
            case CODE_REQUEST_ACTIVITY_COLLECTION:
            {
                if (resultCode == Activity.RESULT_OK || resultCode == CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_READY)
                {
                    setResult(resultCode);
                    finish();
                } else if (resultCode == CODE_RESULT_ACTIVITY_GO_HOME)
                {
                    setResult(CODE_RESULT_ACTIVITY_GO_HOME);
                    finish();
                } else
                {
                    if (mIsDeepLink == false)
                    {
                        switch (resultCode)
                        {
                            case CODE_RESULT_ACTIVITY_REFRESH:
                            case CODE_RESULT_ACTIVITY_PAYMENT_TIMEOVER:
                                mDontReloadAtOnResume = false;
                                break;

                            default:
                                mDontReloadAtOnResume = true;
                                break;
                        }
                    } else
                    {
                        mIsDeepLink = false;

                        mDontReloadAtOnResume = false;
                    }
                }
                break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void finish()
    {
        super.finish();

        overridePendingTransition(R.anim.hold, R.anim.slide_out_right);
    }

    protected void refreshCurrentFragment(boolean isClearList)
    {
        if (isFinishing() == true)
        {
            return;
        }

        if (mPlaceMainLayout.getPlaceListFragment() == null)
        {
            Util.restartApp(this);
            return;
        }

        if (isClearList == true)
        {
            for (PlaceListFragment placeListFragment : mPlaceMainLayout.getPlaceListFragment())
            {
                // 메인의 클리어 리스트의 경우 타화면에 영향을 줌으로 전체 리스트 데이터를 클리어함
                placeListFragment.clearList();
                // 해당 리스트의 viewType이 gone일 수 있음, 해당 경우 메인의 viewType을 따름
                placeListFragment.setViewType(mViewType);
            }
        }

        PlaceListFragment currentListFragment = mPlaceMainLayout.getCurrentPlaceListFragment();
        if (currentListFragment != null)
        {
            currentListFragment.setPlaceCuration(getPlaceCuration());
            currentListFragment.refreshList(true);
        }
    }

    protected void searchMyLocation()
    {
        if (isFinishing() || lockUiComponentAndIsLockUiComponent() == true)
        {
            return;
        }

        lockUI();

        DailyLocationFactory.getInstance(this).startLocationMeasure(this, null, new DailyLocationFactory.LocationListenerEx()
        {
            @Override
            public void onRequirePermission()
            {
                unLockUI();

                if (isFinishing() == true)
                {
                    return;
                }

                Intent intent = PermissionManagerActivity.newInstance(PlaceMainActivity.this, PermissionManagerActivity.PermissionType.ACCESS_FINE_LOCATION);
                startActivityForResult(intent, Constants.CODE_REQUEST_ACTIVITY_PERMISSION_MANAGER);
            }

            @Override
            public void onFailed()
            {
                unLockUI();

                if (isFinishing() == true)
                {
                    return;
                }

                onLocationFailed();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras)
            {
                unLockUI();
            }

            @Override
            public void onProviderEnabled(String provider)
            {
                unLockUI();
            }

            @Override
            public void onProviderDisabled(String provider)
            {
                unLockUI();

                if (isFinishing() == true)
                {
                    return;
                }

                // 현재 GPS 설정이 꺼져있습니다 설정에서 바꾸어 주세요.
                DailyLocationFactory.getInstance(PlaceMainActivity.this).stopLocationMeasure();

                showSimpleDialog(getString(R.string.dialog_title_used_gps)//
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
                            onLocationProviderDisabled();
                        }
                    }, false);
            }

            @Override
            public void onLocationChanged(Location location)
            {
                unLockUI();

                if (isFinishing() == true)
                {
                    return;
                }

                DailyLocationFactory.getInstance(PlaceMainActivity.this).stopLocationMeasure();

                PlaceMainActivity.this.onLocationChanged(location);
            }
        });
    }

    protected void setScrollListTop()
    {
        if (isFinishing() == true)
        {
            return;
        }

        PlaceListFragment placeListFragment = mPlaceMainLayout.getCurrentPlaceListFragment();
        if (placeListFragment != null)
        {
            placeListFragment.setScrollListTop();
        }
    }
}
