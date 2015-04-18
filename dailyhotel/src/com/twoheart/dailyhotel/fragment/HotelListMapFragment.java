package com.twoheart.dailyhotel.fragment;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.adapter.HotelInfoWindowAdapter;
import com.twoheart.dailyhotel.model.Hotel;
import com.twoheart.dailyhotel.model.SaleTime;
import com.twoheart.dailyhotel.util.ui.HotelListViewItem;

public class HotelListMapFragment extends
		com.google.android.gms.maps.SupportMapFragment
{
	private GoogleMap googleMap;
	private ArrayList<HotelListViewItem> mHotelArrayList;
	private HotelInfoWindowAdapter mHotelInfoWindowAdapter;
	protected HotelMainFragment.UserActionListener mUserActionListener;
	private SaleTime mSaleTime;
	private boolean mIsCreateView = false;

	public HotelListMapFragment()
	{

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		googleMap = super.getMap();
		googleMap.setMyLocationEnabled(false);

		// 기본 위치 서울시청.
		//		서울시       : 37.540705, 126.956764
		//		인천광역시 : 37.469221, 126.573234
		//		광주광역시 : 35.126033, 126.831302
		//		대구광역시 : 35.798838, 128.583052
		//		울산광역시 : 35.519301, 129.239078
		//		대전광역시 : 36.321655, 127.378953
		//		부산광역시 : 35.198362, 129.053922
		//		경기도       : 37.567167, 127.190292
		//		강원도       : 37.555837, 128.209315
		//		충청남도    : 36.557229, 126.779757
		//		충청북도    : 36.628503, 127.929344
		//		경상북도    : 36.248647, 128.664734
		//		경상남도    : 35.259787, 128.664734
		//		전라북도    : 35.716705, 127.144185
		//		전라남도    : 34.819400, 126.893113
		//		제주도       : 33.364805, 126.542671

		LatLng address = new LatLng(37.540705, 126.956764);
		CameraPosition cp = new CameraPosition.Builder().target((address)).zoom(15).build();
		googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cp));

		mIsCreateView = true;

		makeMarker();

		return view;
	}

	public void setUserActionListener(HotelMainFragment.UserActionListener userActionLister)
	{
		mUserActionListener = userActionLister;
	}

	public void setHotelList(ArrayList<HotelListViewItem> hotelArrayList, SaleTime saleTime)
	{
		mHotelArrayList = hotelArrayList;
		mSaleTime = saleTime;

		// Marker 만들기.
		if (mIsCreateView == true)
		{
			makeMarker();
		}
	}

	private void makeMarker()
	{
		if (googleMap == null)
		{
			return;
		}

		googleMap.clear();

		if (mHotelArrayList == null)
		{
			return;
		}

		int count = 0;
		double latitude = 0.0;
		double longitude = 0.0;
		double i = 0.0;

		for (HotelListViewItem hotelListViewItem : mHotelArrayList)
		{
			if (hotelListViewItem.getType() == HotelListViewItem.TYPE_SECTION)
			{
				continue;
			}

			Hotel hotel = hotelListViewItem.getItem();

			if (hotel.mLatitude == 0.0)
			{
				hotel.mLatitude = 36.240562 + i / 1000;
			}

			if (hotel.mLongitude == 0.0)
			{
				hotel.mLongitude = 127.867222 + i / 1000;
			}

			i++;

			addMarker(hotel);

			latitude += hotel.mLatitude;
			longitude += hotel.mLongitude;

			count++;
		}

		latitude /= count;
		longitude /= count;

		LatLng address = new LatLng(latitude, longitude);

		CameraPosition cp = new CameraPosition.Builder().target((address)).zoom(15).build();

		googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cp));

		mHotelInfoWindowAdapter = new HotelInfoWindowAdapter(getActivity());

		googleMap.setInfoWindowAdapter(mHotelInfoWindowAdapter);
		googleMap.setOnMarkerClickListener(new OnMarkerClickListener()
		{
			@Override
			public boolean onMarkerClick(Marker marker)
			{
				LatLng latlng = marker.getPosition();

				int index = 0;

				for (HotelListViewItem hotelListViewItem : mHotelArrayList)
				{
					if (hotelListViewItem.getType() == HotelListViewItem.TYPE_SECTION)
					{
						continue;
					}

					Hotel hotel = hotelListViewItem.getItem();

					if (latlng.latitude == hotel.mLatitude && latlng.longitude == hotel.mLongitude)
					{
						mHotelInfoWindowAdapter.setHotelListViewItem(hotelListViewItem);
						mHotelInfoWindowAdapter.setHotelIndex(index);
						marker.showInfoWindow();
						break;
					}

					index++;
				}

				return false;
			}
		});

		googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener()
		{
			@Override
			public void onInfoWindowClick(Marker arg0)
			{
				HotelListViewItem hotelListViewItem = mHotelInfoWindowAdapter.getHotelListViewItem();
				int index = mHotelInfoWindowAdapter.getHotelIndex();

				if (mUserActionListener != null)
				{
					mUserActionListener.selectHotel(hotelListViewItem, index, mSaleTime);
				}
			}
		});
	}

	private void addMarker(Hotel hotel)
	{
		if (googleMap != null)
		{
			HotelPriceRenderer hotelPriceRenderer = new HotelPriceRenderer(hotel);

			googleMap.addMarker(new MarkerOptions().position(new LatLng(hotel.mLatitude, hotel.mLongitude)).title(hotel.getDiscount()).icon(hotelPriceRenderer.getBitmap()));
		}
	}

	private class HotelPriceRenderer
	{
		private String mPrice;
		private IconGenerator mIconGenerator;

		public HotelPriceRenderer(Hotel hotel)
		{
			int originalPrice = Integer.parseInt(hotel.getDiscount().replaceAll(",", ""));
			DecimalFormat comma = new DecimalFormat("###,##0");

			mPrice = "₩" + comma.format(originalPrice);

			mIconGenerator = new IconGenerator(getActivity());

			mIconGenerator.setTextColor(getResources().getColor(R.color.white));
			mIconGenerator.setColor(getResources().getColor(hotel.getCategory().getColorResId()));
		}

		public BitmapDescriptor getBitmap()
		{
			Bitmap icon = mIconGenerator.makeIcon(mPrice);

			return BitmapDescriptorFactory.fromBitmap(icon);
		}
	}
}
