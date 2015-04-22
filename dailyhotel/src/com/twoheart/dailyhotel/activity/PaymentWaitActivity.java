/**
 * Copyright (c) 2014 Daily Co., Ltd. All rights reserved.
 *
 * PaymentWaitActivity (입금대기 화면)
 * 
 * 계좌이체 결제 선택 후 입금대기 상태 화면
 * 가상계좌 정보를 보여주는 화면이다.
 * 
 */
package com.twoheart.dailyhotel.activity;

import java.text.DecimalFormat;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Booking;
import com.twoheart.dailyhotel.util.ExLog;
import com.twoheart.dailyhotel.util.network.request.DailyHotelJsonRequest;
import com.twoheart.dailyhotel.util.network.response.DailyHotelJsonResponseListener;
import com.twoheart.dailyhotel.util.ui.BaseActivity;

public class PaymentWaitActivity extends BaseActivity
{
	private Booking booking;

	private TextView tvHotelName;
	private TextView tvAccount;
	private TextView tvName;
	private TextView tvPrice;
	private TextView tvDeadline;
	private TextView tvGuide1;
	private TextView tvGuide2;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		booking = new Booking();
		Bundle bundle = getIntent().getExtras();
		if (bundle != null)
			booking = (Booking) bundle.getParcelable(NAME_INTENT_EXTRA_DATA_BOOKING);

		setContentView(R.layout.activity_payment_wait);
		setActionBar(getString(R.string.actionbar_title_payment_wait_activity));

		tvHotelName = (TextView) findViewById(R.id.tv_payment_wait_hotel_name);
		tvAccount = (TextView) findViewById(R.id.tv_payment_wait_account);
		tvName = (TextView) findViewById(R.id.tv_payment_wait_name);
		tvPrice = (TextView) findViewById(R.id.tv_payment_wait_price);
		tvDeadline = (TextView) findViewById(R.id.tv_payment_wait_deadline);
		tvGuide1 = (TextView) findViewById(R.id.tv_payment_wait_guide1);
		tvGuide2 = (TextView) findViewById(R.id.tv_payment_wait_guide2);

		tvHotelName.setText(booking.getHotel_name());

		String url = new StringBuilder(URL_DAILYHOTEL_SERVER).append(URL_WEBAPI_RESERVE_MINE_DETAIL).append("/").append(booking.getPayType() + "").append("/").append(booking.getTid() + "").toString();

		lockUI();

		ExLog.e(" / URL : " + url);
		mQueue.add(new DailyHotelJsonRequest(Method.GET, url, null, mReserveMineDetailJsonResponseListener, this));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.payment_wait_actions, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_call:
				Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse(new StringBuilder("tel:").append(PHONE_NUMBER_DAILYHOTEL).toString()));
				startActivity(i);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Listener
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private DailyHotelJsonResponseListener mReserveMineDetailJsonResponseListener = new DailyHotelJsonResponseListener()
	{

		@Override
		public void onResponse(String url, JSONObject response)
		{
			try
			{
				if (response == null)
				{
					throw new NullPointerException("response == null");
				}

				if (response.getBoolean("result") == false)
				{
					Intent intent = new Intent();
					intent.putExtra("msg", response.getString("msg"));
					setResult(CODE_RESULT_ACTIVITY_EXPIRED_PAYMENT_WAIT, intent);
					finish();
				} else
				{
					tvAccount.setText(response.getString("bank_name") + ", " + response.getString("account_num"));
					tvName.setText(response.getString("name"));

					DecimalFormat comma = new DecimalFormat("###,##0");
					String locale = sharedPreference.getString(KEY_PREFERENCE_LOCALE, null);

					if (locale.equals("한국어"))
					{
						tvPrice.setText(comma.format(response.getInt("amt")) + Html.fromHtml(getString(R.string.currency)));
					} else
					{
						tvPrice.setText(Html.fromHtml(getString(R.string.currency)) + comma.format(response.getInt("amt")));
					}

					String[] dateSlice = response.getString("date").split("/");
					String[] timeSlice = response.getString("time").split(":");

					if (locale.equals("한국어"))
					{
						tvDeadline.setText(Integer.parseInt(dateSlice[1]) + "월 " + Integer.parseInt(dateSlice[2]) + "일 " + timeSlice[0] + ":" + timeSlice[1] + "까지");
					} else
					{
						tvDeadline.setText("upto " + Integer.parseInt(dateSlice[1]) + "/ " + Integer.parseInt(dateSlice[2]) + " " + timeSlice[0] + ":" + timeSlice[1]);
					}

					tvGuide1.setText(response.getString("msg1"));
					tvGuide2.setText(response.getString("msg2"));
					unLockUI();
				}

			} catch (JSONException e)
			{
				ExLog.e(e.toString());
			}
		}
	};

	//	@Override
	//	public void onResponse(String url, JSONObject response) {
	//		ExLog.e(" / RESPONSE : " + response.toString());
	//		if (url.contains(URL_WEBAPI_RESERVE_MINE_DETAIL)) {
	//			try {
	//				if (!response.getBoolean("result")) {
	//					Intent intent = new Intent();
	//					intent.putExtra("msg", response.getString("msg"));
	//					setResult(CODE_RESULT_ACTIVITY_EXPIRED_PAYMENT_WAIT, intent);
	//					finish();
	//				} else {
	//					tvAccount.setText(response.getString("bank_name") +", "+ response.getString("account_num"));
	//					tvName.setText(response.getString("name"));
	//
	//					DecimalFormat comma = new DecimalFormat("###,##0");
	//					String locale = sharedPreference.getString(KEY_PREFERENCE_LOCALE, null);
	//					
	//					if (locale.equals("한국어"))	tvPrice.setText(comma.format(response.getInt("amt"))+Html.fromHtml(getString(R.string.currency)));
	//					else	tvPrice.setText(Html.fromHtml(getString(R.string.currency))+comma.format(response.getInt("amt")));
	//					
	//					String[] dateSlice = response.getString("date").split("/");
	//					String[] timeSlice = response.getString("time").split(":");
	//					
	//					if (locale.equals("한국어"))	tvDeadline.setText(Integer.parseInt(dateSlice[1])+"월 "+Integer.parseInt(dateSlice[2])+"일 "+timeSlice[0]+":"+timeSlice[1]+"까지");
	//					else	tvDeadline.setText("upto " + Integer.parseInt(dateSlice[1])+"/ "+Integer.parseInt(dateSlice[2])+" "+timeSlice[0]+":"+timeSlice[1]);
	//
	//					tvGuide1.setText(response.getString("msg1"));
	//					tvGuide2.setText(response.getString("msg2"));
	//					unLockUI();
	//				}
	//				
	//			} catch (JSONException e) {
	//				ExLog.e(e.toString());
	//			}
	//		}
	//	}
}
