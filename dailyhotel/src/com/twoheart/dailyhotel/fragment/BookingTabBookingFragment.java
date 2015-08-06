/**
 * Copyright (c) 2014 Daily Co., Ltd. All rights reserved.
 *
 * BookingTabBookingFragment (예약한 호텔의 예약 탭)
 * 
 * 예약한 호텔 탭 중 예약 탭 프래그먼트
 * 
 */
package com.twoheart.dailyhotel.fragment;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.activity.IssuingReceiptActivity;
import com.twoheart.dailyhotel.activity.LoginActivity;
import com.twoheart.dailyhotel.model.Booking;
import com.twoheart.dailyhotel.model.BookingHotelDetail;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.network.VolleyHttpClient;
import com.twoheart.dailyhotel.util.network.request.DailyHotelJsonRequest;
import com.twoheart.dailyhotel.util.network.request.DailyHotelStringRequest;
import com.twoheart.dailyhotel.util.network.response.DailyHotelJsonResponseListener;
import com.twoheart.dailyhotel.util.network.response.DailyHotelStringResponseListener;
import com.twoheart.dailyhotel.util.ui.BaseActivity;
import com.twoheart.dailyhotel.util.ui.BaseFragment;

public class BookingTabBookingFragment extends BaseFragment implements Constants
{
	private static final String KEY_BUNDLE_ARGUMENTS_HOTEL_DETAIL = "hotel_detail";
	private static final String KEY_BUNDLE_ARGUMENTS_BOOKING = "booking";

	private TextView tvCustomerName, tvCustomerPhone, tvBedtype, tvHotelName,
			tvAddress;
	private TextView tvCheckIn, tvCheckOut;

	private Booking mBooking;
	private BookingHotelDetail mHotelDetail;

	public static BookingTabBookingFragment newInstance(BookingHotelDetail hotelDetail, Booking booking, String title)
	{
		BookingTabBookingFragment newFragment = new BookingTabBookingFragment();

		//관련 정보는 BookingTabActivity에서 넘겨받음. 
		Bundle arguments = new Bundle();
		arguments.putParcelable(KEY_BUNDLE_ARGUMENTS_HOTEL_DETAIL, hotelDetail);
		arguments.putParcelable(KEY_BUNDLE_ARGUMENTS_BOOKING, booking);

		newFragment.setArguments(arguments);
		newFragment.setTitle(title);

		return newFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		mHotelDetail = (BookingHotelDetail) getArguments().getParcelable(KEY_BUNDLE_ARGUMENTS_HOTEL_DETAIL);
		mBooking = (Booking) getArguments().getParcelable(KEY_BUNDLE_ARGUMENTS_BOOKING);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		BaseActivity baseActivity = (BaseActivity) getActivity();

		if (baseActivity == null)
		{
			return null;
		}

		View view = inflater.inflate(R.layout.fragment_booking_tab_booking, container, false);
		tvCustomerName = (TextView) view.findViewById(R.id.tv_booking_tab_user_name);
		tvCustomerPhone = (TextView) view.findViewById(R.id.tv_booking_tab_user_phone);
		tvHotelName = (TextView) view.findViewById(R.id.tv_booking_tab_hotel_name);
		tvAddress = (TextView) view.findViewById(R.id.tv_booking_tab_address);
		tvBedtype = (TextView) view.findViewById(R.id.tv_booking_tab_bedtype);
		tvCheckIn = (TextView) view.findViewById(R.id.tv_booking_tab_checkin);
		tvCheckOut = (TextView) view.findViewById(R.id.tv_booking_tab_checkout);

		tvHotelName.setText(mBooking.getHotelName());
		tvAddress.setText(mHotelDetail.getHotel().getAddress());
		tvBedtype.setText(mHotelDetail.roomName);
		tvCustomerName.setText(mHotelDetail.guestName);
		tvCustomerPhone.setText(mHotelDetail.guestPhone);
		tvCheckIn.setText(mHotelDetail.checkInDay);
		tvCheckOut.setText(mHotelDetail.checkOutDay);

		// Android Marquee bug...
		tvCustomerName.setSelected(true);
		tvCustomerPhone.setSelected(true);
		tvHotelName.setSelected(true);
		tvAddress.setSelected(true);
		tvBedtype.setSelected(true);
		tvCheckIn.setSelected(true);
		tvCheckOut.setSelected(true);

		// 영수증 발급
		TextView viewReceiptTextView = (TextView) view.findViewById(R.id.viewReceiptTextView);
		TextView guideReceiptTextView = (TextView) view.findViewById(R.id.guideReceiptTextView);

		if (mBooking.isUsed == true)
		{
			viewReceiptTextView.setTextColor(getResources().getColor(R.color.white));
			viewReceiptTextView.setBackgroundResource(R.drawable.shape_button_common_background);
			guideReceiptTextView.setText(R.string.message_can_issuing_receipt);

			viewReceiptTextView.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					BaseActivity baseActivity = (BaseActivity) getActivity();

					if (baseActivity == null)
					{
						return;
					}

					Intent intent = new Intent(baseActivity, IssuingReceiptActivity.class);
					intent.putExtra(NAME_INTENT_EXTRA_DATA_BOOKINGIDX, mBooking.reservationIndex);
					startActivity(intent);
				}
			});
		} else
		{
			viewReceiptTextView.setTextColor(getResources().getColor(R.color.black_a25));
			viewReceiptTextView.setBackgroundResource(R.drawable.btn_confirm_normal);
			guideReceiptTextView.setText(R.string.message_cant_issuing_receipt);
		}

		lockUI();
		mQueue.add(new DailyHotelStringRequest(Method.GET, new StringBuilder(URL_DAILYHOTEL_SERVER).append(URL_WEBAPI_USER_ALIVE).toString(), null, mUserAliveStringResponseListener, baseActivity));

		return view;
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Listener
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private DailyHotelJsonResponseListener mUserLoginJsonResponseListener = new DailyHotelJsonResponseListener()
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

				if (response.getString("login").equals("true") == false)
				{
					// 로그인 실패
					// data 초기화
					SharedPreferences.Editor ed = baseActivity.sharedPreference.edit();
					ed.putBoolean(KEY_PREFERENCE_AUTO_LOGIN, false);
					ed.putString(KEY_PREFERENCE_USER_ID, null);
					ed.putString(KEY_PREFERENCE_USER_PWD, null);
					ed.commit();

					showToast(getString(R.string.toast_msg_failed_to_login), Toast.LENGTH_SHORT, true);
				} else
				{
					VolleyHttpClient.createCookie();
				}
			} catch (JSONException e)
			{
				onError(e);
			} finally
			{
				unLockUI();
			}
		}
	};

	private DailyHotelStringResponseListener mUserAliveStringResponseListener = new DailyHotelStringResponseListener()
	{

		@Override
		public void onResponse(String url, String response)
		{
			BaseActivity baseActivity = (BaseActivity) getActivity();

			if (baseActivity == null)
			{
				return;
			}

			String result = null;

			if (TextUtils.isEmpty(response) == false)
			{
				result = response.trim();
			}

			if ("alive".equalsIgnoreCase(result) == true)
			{
			} else if ("dead".equalsIgnoreCase(result) == true)
			{ // session dead
				// 재로그인
				if (baseActivity.sharedPreference.getBoolean(KEY_PREFERENCE_AUTO_LOGIN, false))
				{
					Map<String, String> loginParams = new HashMap<String, String>();
					loginParams.put("email", baseActivity.sharedPreference.getString(KEY_PREFERENCE_USER_ID, null));
					loginParams.put("pw", baseActivity.sharedPreference.getString(KEY_PREFERENCE_USER_PWD, null));

					mQueue.add(new DailyHotelJsonRequest(Method.POST, new StringBuilder(URL_DAILYHOTEL_SERVER).append(URL_WEBAPI_USER_LOGIN).toString(), loginParams, mUserLoginJsonResponseListener, baseActivity));
				} else
				{
					startActivity(new Intent(baseActivity, LoginActivity.class));
				}
			} else
			{
				unLockUI();
			}
		}
	};
}
