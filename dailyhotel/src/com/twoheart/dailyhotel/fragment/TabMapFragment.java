package com.twoheart.dailyhotel.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.activity.ZoomMapActivity;
import com.twoheart.dailyhotel.model.HotelDetail;
import com.twoheart.dailyhotel.util.TabActivity;
import com.twoheart.dailyhotel.util.ui.BaseFragment;
import com.twoheart.dailyhotel.widget.HotelGradeView;

public class TabMapFragment extends BaseFragment implements OnMapClickListener {

	private static final String TAG = "HotelTabMapFragment";
	private static final String KEY_BUNDLE_ARGUMENTS_HOTEL_DETAIL = "hotel_detail";
	
	private HotelDetail mHotelDetail;
	private SupportMapFragment mMapFragment;
	private GoogleMap googleMap;
	private TextView tvName, tvAddress;
	private HotelGradeView hvGrade;
	
	public static TabMapFragment newInstance(HotelDetail hotelDetail) {
		
		TabMapFragment newFragment = new TabMapFragment();
		Bundle arguments = new Bundle();
		
		arguments.putParcelable(KEY_BUNDLE_ARGUMENTS_HOTEL_DETAIL, hotelDetail);
		newFragment.setArguments(arguments);
		newFragment.setTitle("����");
		
		return newFragment;
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHotelDetail = (HotelDetail) getArguments().getParcelable(KEY_BUNDLE_ARGUMENTS_HOTEL_DETAIL);
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View view = inflater.inflate(R.layout.fragment_hotel_tab_map,
				container, false);

		tvName = (TextView) view.findViewById(R.id.tv_hotel_tab_map_name);
		tvAddress = (TextView) view.findViewById(R.id.tv_hotel_tab_map_address);

		tvName.setText(mHotelDetail.getHotel().getName());
		tvName.setSelected(true);
		tvAddress.setText(mHotelDetail.getHotel().getAddress());
		tvAddress.setSelected(true);

		hvGrade = (HotelGradeView) view.findViewById(R.id.hv_hotel_grade);
//		gradeBackground = (FrameLayout) view
//				.findViewById(R.id.fl_hotel_row_grade);
//		gradeText = (TextView) view.findViewById(R.id.tv_hotel_row_grade);

		String category = mHotelDetail.getHotel().getCategory();
		hvGrade.setHotelGradeCode(category);

		return view;
	}

	@Override
	public void onMapClick(LatLng latLng) {
		Intent i = new Intent(mHostActivity, ZoomMapActivity.class);
		i.putExtra(NAME_INTENT_EXTRA_DATA_HOTELDETAIL, mHotelDetail);
		startActivity(i);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mMapFragment = (SupportMapFragment) mHostActivity
				.getSupportFragmentManager().findFragmentById(R.id.frag_map);
		googleMap = mMapFragment.getMap();
		
		if (googleMap != null) {
			googleMap.setOnMapClickListener(this);
			googleMap.setMyLocationEnabled(false);	
		}

		addMarker(mHotelDetail.getLatitude(), mHotelDetail.getLongitude(),
				mHotelDetail.getHotel().getName());

	}

	@Override
	public void onDestroyView() {
		if (!mHostActivity.isFinishing())
			mHostActivity.getSupportFragmentManager().beginTransaction()
					.remove(mMapFragment).commitAllowingStateLoss();
		super.onDestroyView();
	}

	// ��Ŀ �߰�
	public void addMarker(Double lat, Double lng, String hotel_name) {
		if (googleMap != null) {
			googleMap.addMarker(new MarkerOptions().position(
					new LatLng(lat, lng)).title(hotel_name));
			LatLng address = new LatLng(lat, lng);
			CameraPosition cp = new CameraPosition.Builder().target((address))
					.zoom(15).build();
			googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cp));
		}
	}
}
