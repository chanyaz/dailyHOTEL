package com.twoheart.dailyhotel.screen.common;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ScreenUtils;
import com.daily.base.widget.DailyToast;
import com.daily.dailyhotel.parcel.analytics.NavigatorAnalyticsParam;
import com.daily.dailyhotel.screen.common.dialog.navigator.NavigatorDialogActivity;
import com.daily.dailyhotel.view.DailyToolbarView;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.MyLocationMarker;
import com.twoheart.dailyhotel.place.adapter.PlaceNameInfoWindowAdapter;
import com.twoheart.dailyhotel.place.base.BaseActivity;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.DailyLocationFactory;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;

public class ZoomMapActivity extends BaseActivity
{
    GoogleMap mGoogleMap;
    ImageView mMyLocationView;
    MarkerOptions mMyLocationMarkerOptions;
    Marker mMyLocationMarker, mPlaceLocationMarker;
    private Handler mHandler = new Handler();
    SourceType mSourceType;

    DailyLocationFactory mDailyLocationFactory;

    public enum SourceType
    {
        HOTEL,
        GOURMET,
        HOTEL_BOOKING,
        GOURMET_BOOKING
    }

    public static Intent newInstance(Context context, SourceType sourceType, String name, String address, double latitude, double longitude, boolean isOverseas)
    {
        Intent intent = new Intent(context, ZoomMapActivity.class);

        intent.putExtra(NAME_INTENT_EXTRA_DATA_TYPE, sourceType.name());
        intent.putExtra(NAME_INTENT_EXTRA_DATA_HOTELNAME, name);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_ADDRESS, address);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_LATITUDE, latitude);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_LONGITUDE, longitude);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_ISOVERSEAS, isOverseas);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        overridePendingTransition(R.anim.slide_in_right, R.anim.hold);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_zoom_map);

        Intent intent = getIntent();

        String placeName;
        String address;
        double latitude;
        double longitude;
        boolean isOverseas = false;

        if (intent != null)
        {
            if (intent.hasExtra(NAME_INTENT_EXTRA_DATA_HOTELNAME) == true)
            {
                placeName = intent.getStringExtra(NAME_INTENT_EXTRA_DATA_HOTELNAME);
            } else
            {
                placeName = intent.getStringExtra(NAME_INTENT_EXTRA_DATA_PLACENAME);
            }

            address = intent.getStringExtra(NAME_INTENT_EXTRA_DATA_ADDRESS);

            latitude = intent.getDoubleExtra(NAME_INTENT_EXTRA_DATA_LATITUDE, 0);
            longitude = intent.getDoubleExtra(NAME_INTENT_EXTRA_DATA_LONGITUDE, 0);

            try
            {
                mSourceType = SourceType.valueOf(intent.getStringExtra(NAME_INTENT_EXTRA_DATA_TYPE));
            } catch (Exception e)
            {
                Util.restartApp(this);
                return;
            }

            isOverseas = intent.getBooleanExtra(NAME_INTENT_EXTRA_DATA_ISOVERSEAS, false);
        } else
        {
            latitude = 0;
            longitude = 0;
            placeName = null;
            address = null;
        }

        if (placeName == null || address == null || latitude == 0 || longitude == 0 || mSourceType == null)
        {
            finish();
            return;
        }

        initToolbar(getString(R.string.frag_tab_map_title));
        initLayout(placeName, address, latitude, longitude, isOverseas);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (mSourceType == null)
        {
            Util.restartApp(this);
            return;
        }

        switch (mSourceType)
        {
            case HOTEL:
                AnalyticsManager.getInstance(this).recordScreen(this, AnalyticsManager.Screen.DAILYHOTEL_DETAIL_MAP, null);
                break;

            case GOURMET:
                AnalyticsManager.getInstance(this).recordScreen(this, AnalyticsManager.Screen.DAILYGOURMET_DETAIL_MAP, null);
                break;

            case HOTEL_BOOKING:
            case GOURMET_BOOKING:
                //                AnalyticsManager.getInstance(this).recordScreen(AnalyticsManager.Screen.BOOKING_DETAIL_MAP, null);
                break;
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if (mDailyLocationFactory != null)
        {
            mDailyLocationFactory.stopLocationMeasure();
        }
    }


    @Override
    public void finish()
    {
        super.finish();

        overridePendingTransition(R.anim.hold, R.anim.slide_out_right);
    }

    private void initToolbar(String title)
    {
        DailyToolbarView dailyToolbarView = findViewById(R.id.toolbarView);

        dailyToolbarView.setTitleText(title);
        dailyToolbarView.setOnBackClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }

    private void initLayout(final String placeName, final String address, final double latitude, final double longitude, final boolean isOverseas)
    {
        // 주소지
        final TextView hotelAddressTextView = findViewById(R.id.addressTextView);

        hotelAddressTextView.setText(address);

        View clipAddress = findViewById(R.id.copyAddressView);
        clipAddress.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DailyTextUtils.clipText(ZoomMapActivity.this, address);

                DailyToast.showToast(ZoomMapActivity.this, R.string.message_detail_copy_address, Toast.LENGTH_SHORT);
            }
        });

        View searchMapView = findViewById(R.id.searchMapView);
        searchMapView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (lockUiComponentAndIsLockUiComponent() == true)
                {
                    return;
                }

                switch (mSourceType)
                {
                    case HOTEL:
                    case HOTEL_BOOKING:
                    {
                        NavigatorAnalyticsParam analyticsParam = new NavigatorAnalyticsParam();
                        analyticsParam.category = AnalyticsManager.Category.HOTEL_BOOKINGS;
                        analyticsParam.action = AnalyticsManager.Action.HOTEL_DETAIL_NAVIGATION_APP_CLICKED;

                        startActivityForResult(NavigatorDialogActivity.newInstance(ZoomMapActivity.this, placeName//
                            , latitude, longitude, isOverseas, analyticsParam), Constants.CODE_REQUEST_ACTIVITY_NAVIGATOR);
                        break;
                    }

                    case GOURMET:
                    case GOURMET_BOOKING:
                    {
                        NavigatorAnalyticsParam analyticsParam = new NavigatorAnalyticsParam();
                        analyticsParam.category = AnalyticsManager.Category.GOURMET_BOOKINGS;
                        analyticsParam.action = AnalyticsManager.Action.GOURMET_DETAIL_NAVIGATION_APP_CLICKED;

                        startActivityForResult(NavigatorDialogActivity.newInstance(ZoomMapActivity.this, placeName//
                            , latitude, longitude, isOverseas, analyticsParam), Constants.CODE_REQUEST_ACTIVITY_NAVIGATOR);
                        break;
                    }
                }
            }
        });

        FrameLayout mapLayout = findViewById(R.id.mapLayout);
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();

        getSupportFragmentManager().beginTransaction().add(mapLayout.getId(), mapFragment).commitAllowingStateLoss();

        mapFragment.getMapAsync(new OnMapReadyCallback()
        {
            @Override
            public void onMapReady(GoogleMap googleMap)
            {
                mGoogleMap = googleMap;

                mGoogleMap.getUiSettings().setCompassEnabled(false);
                mGoogleMap.getUiSettings().setIndoorLevelPickerEnabled(false);
                mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
                mGoogleMap.getUiSettings().setRotateGesturesEnabled(false);
                mGoogleMap.getUiSettings().setTiltGesturesEnabled(false);
                mGoogleMap.getUiSettings().setZoomControlsEnabled(true);

                //                mGoogleMap.setMyLocationEnabled(false);

                relocationMyLocation();
                relocationZoomControl();
                addMarker(mGoogleMap, latitude, longitude, placeName);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        unLockUI();

        switch (requestCode)
        {
            case Constants.CODE_RESULT_ACTIVITY_SETTING_LOCATION:
            {
                searchMyLocation();
                break;
            }

            case Constants.CODE_REQUEST_ACTIVITY_PERMISSION_MANAGER:
            {
                if (resultCode == RESULT_OK)
                {
                    searchMyLocation();
                } else if (resultCode == CODE_RESULT_ACTIVITY_GO_HOME)
                {
                    setResult(resultCode);
                    finish();
                }
                break;
            }
        }
    }

    void relocationMyLocation()
    {
        mMyLocationView = findViewById(0x2);

        if (mMyLocationView != null)
        {
            mMyLocationView.setVisibility(View.VISIBLE);
            mMyLocationView.setOnClickListener(mOnMyLocationClickListener);
        }
    }

    void relocationZoomControl()
    {
        View zoomControl = findViewById(0x1);

        if (zoomControl != null && zoomControl.getLayoutParams() instanceof RelativeLayout.LayoutParams)
        {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) zoomControl.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);

            zoomControl.setPadding(zoomControl.getPaddingLeft(), ScreenUtils.dpToPx(this, 50), zoomControl.getPaddingRight(), zoomControl.getPaddingBottom());
            zoomControl.setLayoutParams(params);
        }
    }

    void addMarker(GoogleMap googleMap, double lat, double lng, String hotel_name)
    {
        if (googleMap != null)
        {
            mPlaceLocationMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(hotel_name));
            mPlaceLocationMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.info_ic_map_large));

            LatLng address = new LatLng(lat, lng);
            CameraPosition cp = new CameraPosition.Builder().target((address)).zoom(15).build();

            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cp));
            googleMap.setInfoWindowAdapter(new PlaceNameInfoWindowAdapter(this));
            googleMap.setOnMarkerClickListener(new OnMarkerClickListener()
            {
                @Override
                public boolean onMarkerClick(Marker marker)
                {
                    marker.showInfoWindow();
                    return true;
                }
            });

            mPlaceLocationMarker.hideInfoWindow();
            mHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    mPlaceLocationMarker.showInfoWindow();
                }
            });
        }
    }

    private void searchMyLocation()
    {
        lockUI();

        if (mDailyLocationFactory == null)
        {
            mDailyLocationFactory = new DailyLocationFactory(this);
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
                unLockUI();

                Intent intent = PermissionManagerActivity.newInstance(ZoomMapActivity.this, PermissionManagerActivity.PermissionType.ACCESS_FINE_LOCATION);
                startActivityForResult(intent, Constants.CODE_REQUEST_ACTIVITY_PERMISSION_MANAGER);
            }

            @Override
            public void onFailed()
            {
                unLockUI();
            }

            @Override
            public void onProviderDisabled()
            {
                unLockUI();

                if (isFinishing() == true)
                {
                    return;
                }

                // 현재 GPS 설정이 꺼져있습니다 설정에서 바꾸어 주세요.
                mDailyLocationFactory.stopLocationMeasure();

                showSimpleDialog(getString(R.string.dialog_title_used_gps), getString(R.string.dialog_msg_used_gps), getString(R.string.dialog_btn_text_dosetting), getString(R.string.dialog_btn_text_cancel), new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, Constants.CODE_RESULT_ACTIVITY_SETTING_LOCATION);
                    }
                }, null, true);
            }

            @Override
            public void onProviderEnabled()
            {
                mDailyLocationFactory.startLocationMeasure(mMyLocationView, new DailyLocationFactory.OnLocationListener()
                {
                    @Override
                    public void onFailed()
                    {
                        unLockUI();
                    }

                    @Override
                    public void onAlreadyRun()
                    {

                    }

                    @Override
                    public void onLocationChanged(Location location)
                    {
                        unLockUI();

                        if (isFinishing() == true || mGoogleMap == null)
                        {
                            return;
                        }

                        mDailyLocationFactory.stopLocationMeasure();

                        if (mMyLocationMarkerOptions == null)
                        {
                            mMyLocationMarkerOptions = new MarkerOptions();
                            mMyLocationMarkerOptions.icon(new MyLocationMarker(ZoomMapActivity.this).makeIcon());
                            mMyLocationMarkerOptions.anchor(0.5f, 0.5f);
                        }

                        if (mMyLocationMarker != null)
                        {
                            mMyLocationMarker.remove();
                        }

                        mMyLocationMarkerOptions.position(new LatLng(location.getLatitude(), location.getLongitude()));
                        mMyLocationMarker = mGoogleMap.addMarker(mMyLocationMarkerOptions);

                        LatLngBounds.Builder latLngBounds = new LatLngBounds.Builder();
                        latLngBounds.include(mPlaceLocationMarker.getPosition());
                        latLngBounds.include(mMyLocationMarker.getPosition());

                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds.build(), ScreenUtils.dpToPx(ZoomMapActivity.this, 50));
                        mGoogleMap.animateCamera(cameraUpdate);
                    }

                    @Override
                    public void onCheckSetting(ResolvableApiException exception)
                    {
                        unLockUI();

                        try
                        {
                            exception.startResolutionForResult(ZoomMapActivity.this, Constants.CODE_RESULT_ACTIVITY_SETTING_LOCATION);
                        }catch (Exception e)
                        {

                        }
                    }
                });
            }
        });
    }

    private final View.OnClickListener mOnMyLocationClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (mGoogleMap == null)
            {
                return;
            }

            Intent intent = PermissionManagerActivity.newInstance(ZoomMapActivity.this, PermissionManagerActivity.PermissionType.ACCESS_FINE_LOCATION);
            startActivityForResult(intent, Constants.CODE_REQUEST_ACTIVITY_PERMISSION_MANAGER);
        }
    };
}
