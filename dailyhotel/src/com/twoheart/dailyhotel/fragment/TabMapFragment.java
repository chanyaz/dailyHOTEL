/**
 * Copyright (c) 2014 Daily Co., Ltd. All rights reserved.
 *
 * TabMapFragment (지도 탭)
 * 
 * 호텔 탭 중 지도 탭 프래그먼트
 * 
 */
package com.twoheart.dailyhotel.fragment;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.activity.ZoomMapActivity;
import com.twoheart.dailyhotel.adapter.HotelNameInfoWindowAdapter;
import com.twoheart.dailyhotel.model.HotelDetail;
import com.twoheart.dailyhotel.util.ui.BaseActivity;
import com.twoheart.dailyhotel.util.ui.BaseFragment;

public class TabMapFragment extends BaseFragment implements OnMapClickListener
{
	private static final String KEY_BUNDLE_ARGUMENTS_HOTEL_DETAIL = "hotel_detail";

	private HotelDetail mHotelDetail;
	private SupportMapFragment mMapFragment;
	private GoogleMap googleMap;
	private TextView tvName, tvAddress;
	private TextView hvGrade;
	private Marker mMarker;

	public static TabMapFragment newInstance(HotelDetail hotelDetail, String title)
	{
		TabMapFragment newFragment = new TabMapFragment();
		Bundle arguments = new Bundle();

		//관련 정보들은 HotelTabActivity, BookingTabActivity에서 넘겨받음. 
		arguments.putParcelable(KEY_BUNDLE_ARGUMENTS_HOTEL_DETAIL, hotelDetail);
		newFragment.setArguments(arguments);
		newFragment.setTitle(title);

		return newFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mHotelDetail = (HotelDetail) getArguments().getParcelable(KEY_BUNDLE_ARGUMENTS_HOTEL_DETAIL);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_hotel_tab_map, container, false);

		tvName = (TextView) view.findViewById(R.id.tv_hotel_tab_map_name);
		tvAddress = (TextView) view.findViewById(R.id.tv_hotel_tab_map_address);

		tvName.setText(mHotelDetail.getHotel().getName());
		tvName.setSelected(true);
		tvAddress.setText(mHotelDetail.getHotel().getAddress());
		tvAddress.setSelected(true);

		hvGrade = (TextView) view.findViewById(R.id.hv_hotel_grade);

		// grade
		hvGrade.setText(mHotelDetail.getHotel().getCategory().getName(getActivity()));
		hvGrade.setBackgroundResource(mHotelDetail.getHotel().getCategory().getColorResId());

		return view;
	}

	@Override
	public void onMapClick(LatLng latLng)
	{
		BaseActivity baseActivity = (BaseActivity) getActivity();

		if (baseActivity == null)
		{
			return;
		}

		Intent i = new Intent(baseActivity, ZoomMapActivity.class);
		i.putExtra(NAME_INTENT_EXTRA_DATA_HOTELDETAIL, mHotelDetail);
		startActivity(i);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.frag_map);
		googleMap = mMapFragment.getMap();

		if (googleMap != null)
		{
			googleMap.setOnMapClickListener(this);
			googleMap.setMyLocationEnabled(false);
		}

		addMarker(mHotelDetail.getLatitude(), mHotelDetail.getLongitude(), mHotelDetail.getHotel().getName());
	}

	@Override
	public void onResume()
	{
		if (mMarker != null)
		{
			mMarker.showInfoWindow();
		}

		super.onResume();
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
	public void onDestroyView()
	{
		BaseActivity baseActivity = (BaseActivity) getActivity();

		if (baseActivity == null)
		{
			return;
		}

		try
		{
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)
			{
				if (!baseActivity.isFinishing())
					baseActivity.getSupportFragmentManager().beginTransaction().remove(mMapFragment).commitAllowingStateLoss();
			} else
			{
				if (!baseActivity.isDestroyed())
					baseActivity.getSupportFragmentManager().beginTransaction().remove(mMapFragment).commitAllowingStateLoss();
			}

		} catch (IllegalStateException e)
		{
			onError(e);
		}

		super.onDestroyView();
	}

	// 마커 추가
	public void addMarker(Double lat, Double lng, String hotel_name)
	{
		BaseActivity baseActivity = (BaseActivity) getActivity();

		if (baseActivity == null)
		{
			return;
		}

		if (googleMap != null)
		{
			mMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(hotel_name));
			mMarker.showInfoWindow();

			LatLng address = new LatLng(lat, lng);
			CameraPosition cp = new CameraPosition.Builder().target((address)).zoom(15).build();
			googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cp));
			googleMap.setInfoWindowAdapter(new HotelNameInfoWindowAdapter(baseActivity));
			googleMap.setOnMarkerClickListener(new OnMarkerClickListener()
			{
				@Override
				public boolean onMarkerClick(Marker marker)
				{
					marker.showInfoWindow();
					return true;
				}
			});
		}
	}
}
