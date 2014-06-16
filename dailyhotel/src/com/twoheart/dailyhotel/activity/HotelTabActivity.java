package com.twoheart.dailyhotel.activity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.fragment.HotelTabBookingFragment;
import com.twoheart.dailyhotel.model.Hotel;
import com.twoheart.dailyhotel.model.HotelDetail;
import com.twoheart.dailyhotel.model.SaleTime;
import com.twoheart.dailyhotel.util.Log;
import com.twoheart.dailyhotel.util.TabActivity;
import com.twoheart.dailyhotel.util.network.VolleyHttpClient;
import com.twoheart.dailyhotel.util.network.request.DailyHotelJsonRequest;
import com.twoheart.dailyhotel.util.network.request.DailyHotelStringRequest;
import com.twoheart.dailyhotel.util.network.response.DailyHotelJsonResponseListener;
import com.twoheart.dailyhotel.util.network.response.DailyHotelStringResponseListener;
import com.twoheart.dailyhotel.widget.HotelViewPager;
import com.viewpagerindicator.TabPageIndicator;

public class HotelTabActivity extends TabActivity implements OnClickListener,
		DailyHotelJsonResponseListener,
		DailyHotelStringResponseListener {

	private static final String TAG = "HotelTabActivity";
	
	protected SaleTime mSaleTime;
	
	private Button btnSoldOut;
	private Button btnBooking;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		hotelDetail = new HotelDetail();
		mSaleTime = new SaleTime();
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			hotelDetail.setHotel((Hotel) bundle
					.getParcelable(NAME_INTENT_EXTRA_DATA_HOTEL));
			mSaleTime = bundle.getParcelable(NAME_INTENT_EXTRA_DATA_SALETIME);
		}
		
		setContentView(R.layout.activity_hotel_tab);

		mViewPager = (HotelViewPager) findViewById(R.id.pager);
		mIndicator = (TabPageIndicator) findViewById(R.id.indicator);
		btnSoldOut = (Button) findViewById(R.id.tv_hotel_tab_soldout);
		btnBooking = (Button) findViewById(R.id.btn_hotel_tab_booking);
		btnBooking.setOnClickListener(this);

		setActionBar(hotelDetail.getHotel().getName());

		// ȣ�� sold out��
		if (hotelDetail.getHotel().getAvailableRoom() == 0) {
			btnBooking.setVisibility(View.GONE);
			btnSoldOut.setVisibility(View.VISIBLE);
		}

	}

	@Override
	protected void onPostSetCookie() {
		String url = new StringBuilder(URL_DAILYHOTEL_SERVER)
				.append(URL_WEBAPI_HOTEL_DETAIL)
				.append(hotelDetail.getHotel().getIdx()).append("/")
				.append(mSaleTime.getCurrentYear()).append("/")
				.append(mSaleTime.getCurrentMonth()).append("/")
				.append(mSaleTime.getCurrentDay()).toString();

		Log.d(TAG, url);

		lockUI();
		// ȣ�� ������ �����´�.
		mQueue.add(new DailyHotelJsonRequest(Method.GET, url, null, this, this));
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == btnBooking.getId()) {

			lockUI();
			mQueue.add(new DailyHotelStringRequest(Method.GET,
					new StringBuilder(URL_DAILYHOTEL_SERVER).append(
							URL_WEBAPI_USER_ALIVE).toString(), null, this, this));

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == CODE_REQUEST_ACTIVITY_BOOKING) {
			setResult(resultCode);

			if (resultCode == RESULT_OK) {
				finish();
			}
		} else if (requestCode == CODE_REQUEST_ACTIVITY_LOGIN) {
			if (resultCode == RESULT_OK)
				mQueue.add(new DailyHotelStringRequest(Method.GET, new StringBuilder(
						URL_DAILYHOTEL_SERVER).append(URL_WEBAPI_USER_ALIVE)
						.toString(), null, this, this));
		}


		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void loadFragments() {
		mFragments.add(HotelTabBookingFragment.newInstance(hotelDetail));
		super.loadFragments();

	}

	@Override
	public void onResponse(String url, String response) {
		if (url.contains(URL_WEBAPI_USER_ALIVE)) {
			unLockUI();
			
			String result = response.trim();
			if (result.equals("alive")) { // session alive
				Intent i = new Intent(this, BookingActivity.class);
				i.putExtra(NAME_INTENT_EXTRA_DATA_HOTELDETAIL, hotelDetail);
				startActivityForResult(i, CODE_REQUEST_ACTIVITY_BOOKING);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);

			} else if (result.equals("dead")) { // session dead
				
				// ��α���
				if (sharedPreference.getBoolean(
						KEY_PREFERENCE_AUTO_LOGIN, false)) {
					String id = sharedPreference.getString(
							KEY_PREFERENCE_USER_ID, null);
					String accessToken = sharedPreference.getString(
							KEY_PREFERENCE_USER_ACCESS_TOKEN, null);
					String pw = sharedPreference.getString(
							KEY_PREFERENCE_USER_PWD, null);

					Map<String, String> loginParams = new HashMap<String, String>();

					if (accessToken != null) {
						loginParams.put("accessToken", accessToken);
					} else {
						loginParams.put("email", id);
					}

					loginParams.put("pw", pw);

					mQueue.add(new DailyHotelJsonRequest(Method.POST,
							new StringBuilder(URL_DAILYHOTEL_SERVER).append(
									URL_WEBAPI_USER_LOGIN).toString(),
							loginParams, this, this));
				} else {
					loadLoginProcess();
				}

			} else {
				onError();
			}

		}
	}

	private void loadLoginProcess() {
		showToast("�α����� �ʿ��մϴ�", Toast.LENGTH_LONG, false);

		startActivityForResult(new Intent(this, LoginActivity.class),
				CODE_REQUEST_ACTIVITY_LOGIN);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);

	}

	@Override
	public void onResponse(String url, JSONObject response) {
		if (url.contains(URL_WEBAPI_HOTEL_DETAIL)) {
			try {
				JSONObject obj = response;
				JSONArray bookingArr = obj.getJSONArray("detail");
				JSONObject detailObj = bookingArr.getJSONObject(0);

				DecimalFormat comma = new DecimalFormat("###,##0");
				String strDiscount = comma.format(Integer.parseInt(detailObj
						.getString("discount")));
				String strPrice = comma.format(Integer.parseInt(detailObj
						.getString("price")));
				
				if (hotelDetail.getHotel() == null)
					 hotelDetail.setHotel(new Hotel());
				
				Hotel hotelBasic = hotelDetail.getHotel();

				hotelBasic.setAddress(detailObj.getString("address"));
				hotelBasic.setName(detailObj.getString("hotel_name"));
				hotelBasic.setDiscount(strDiscount);
				hotelBasic.setPrice(strPrice);
				hotelBasic.setCategory(detailObj.getString("cat"));
				hotelBasic.setBedType(detailObj.getString("bed_type"));
				
				hotelDetail.setHotel(hotelBasic);

				JSONArray imgArr = detailObj.getJSONArray("img");
				List<String> imageList = new ArrayList<String>();

				for (int i = 0; i < imgArr.length(); i++) {
					if (i == 0)
						continue;
					JSONObject imgObj = imgArr.getJSONObject(i);
					imageList.add(imgObj.getString("path"));
				}

				hotelDetail.setImageUrl(imageList);

				JSONArray specArr = obj.getJSONArray("spec");
				Map<String, List<String>> contentList = new LinkedHashMap<String, List<String>>();
				for (int i = 0; i < specArr.length(); i++) {

					JSONObject specObj = specArr.getJSONObject(i);
					String key = specObj.getString("key");
					JSONArray valueArr = specObj.getJSONArray("value");

					List<String> valueList = new ArrayList<String>();

					for (int j = 0; j < valueArr.length(); j++) {
						JSONObject valueObj = valueArr.getJSONObject(j);
						String value = valueObj.getString("value");
						valueList.add(value);
					}

					contentList.put(key, valueList);

				}
				hotelDetail.setSpecification(contentList);

				double latitude = detailObj.getDouble("lat");
				double longitude = detailObj.getDouble("lng");

				hotelDetail.setLatitude(latitude);
				hotelDetail.setLongitude(longitude);
				
				int saleIdx = detailObj.getInt("idx");
				hotelDetail.setSaleIdx(saleIdx);
				
				mFragments.clear();
				loadFragments();
				
				unLockUI();

			} catch (Exception e) {
				onError(e);
			}
		} else if (url.contains(URL_WEBAPI_USER_LOGIN)) {
			try {
				if (!response.getString("login").equals("true")) {
					// �α��� ����
					// data �ʱ�ȭ
					SharedPreferences.Editor ed = sharedPreference
							.edit();
					ed.putBoolean(KEY_PREFERENCE_AUTO_LOGIN, false);
					ed.putString(KEY_PREFERENCE_USER_ACCESS_TOKEN, null);
					ed.putString(KEY_PREFERENCE_USER_ID, null);
					ed.putString(KEY_PREFERENCE_USER_PWD, null);
					ed.commit();
					
					unLockUI();
					loadLoginProcess();
					
				} else {
					VolleyHttpClient.createCookie();
					
					mQueue.add(new DailyHotelStringRequest(Method.GET,
							new StringBuilder(URL_DAILYHOTEL_SERVER).append(
									URL_WEBAPI_USER_ALIVE).toString(), null, this, this));
					
				}
				
			} catch (JSONException e) {
				onError(e);
				unLockUI();
			}
		}
	}
}
