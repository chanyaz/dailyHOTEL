package com.twoheart.dailyhotel.activity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.fragment.BookingTabBookingFragment;
import com.twoheart.dailyhotel.model.Booking;
import com.twoheart.dailyhotel.model.Hotel;
import com.twoheart.dailyhotel.model.HotelDetail;
import com.twoheart.dailyhotel.util.GlobalFont;
import com.twoheart.dailyhotel.util.Log;
import com.twoheart.dailyhotel.util.TabActivity;
import com.twoheart.dailyhotel.util.network.request.DailyHotelJsonRequest;
import com.twoheart.dailyhotel.util.network.request.DailyHotelStringRequest;
import com.twoheart.dailyhotel.util.network.response.DailyHotelJsonResponseListener;
import com.twoheart.dailyhotel.util.network.response.DailyHotelStringResponseListener;
import com.twoheart.dailyhotel.util.ui.BaseActivity;
import com.twoheart.dailyhotel.widget.HotelViewPager;
import com.viewpagerindicator.TabPageIndicator;

public class PaymentWaitActivity extends BaseActivity implements DailyHotelJsonResponseListener {

	private final static String TAG = "PaymentWaitActivity";
	Booking booking;

	TextView tvHotelName;
	TextView tvAccount;
	TextView tvName;
	TextView tvPrice;
	TextView tvDeadline;
	TextView tvGuide1;
	TextView tvGuide2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		booking = new Booking();
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) booking = (Booking) bundle.getParcelable(NAME_INTENT_EXTRA_DATA_BOOKING);

		setActionBar("입금대기");
		setContentView(R.layout.activity_payment_wait);

		tvHotelName = (TextView) findViewById(R.id.tv_payment_wait_hotel_name);
		tvAccount = (TextView) findViewById(R.id.tv_payment_wait_account);
		tvName = (TextView) findViewById(R.id.tv_payment_wait_name);
		tvPrice = (TextView) findViewById(R.id.tv_payment_wait_price);
		tvDeadline = (TextView) findViewById(R.id.tv_payment_wait_deadline);
		tvGuide1 = (TextView) findViewById(R.id.tv_payment_wait_guide1);
		tvGuide2 = (TextView) findViewById(R.id.tv_payment_wait_guide2);

		tvHotelName.setText(booking.getHotel_name());

		String url = new StringBuilder(URL_DAILYHOTEL_SERVER)
		.append(URL_WEBAPI_RESERVE_MINE_DETAIL)
		.append("/").append(booking.getPayType()+"")
		.append("/").append(booking.getTid()+"").toString();
		
		lockUI();
		
		mQueue.add(new DailyHotelJsonRequest(Method.GET, url, null, this, this));
	}

	@Override
	public void onResponse(String url, JSONObject response) {

		if (url.contains(URL_WEBAPI_RESERVE_MINE_DETAIL)) {

			try {
				tvAccount.setText(response.getString("bank_name"));
				tvName.setText(response.getString("name"));

				DecimalFormat comma = new DecimalFormat("###,##0");
				tvPrice.setText(comma.format(response.getInt("amt"))+"원");
				
				Date date = new Date(response.getString("date"));
				String time = response.getString("time");
				String[] slice = time.split(":");
				
				tvDeadline.setText(date.getMonth()+"월 "+date.getDay()+"일 "+slice[0]+":"+slice[1]+"까지");

				tvGuide1.setText(response.getString("msg1"));
				tvGuide2.setText(response.getString("msg2"));
				unLockUI();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.payment_wait_actions, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.action_call:
			Intent i = new Intent(
					Intent.ACTION_DIAL,
					Uri.parse(new StringBuilder("tel:")
					.append(PHONE_NUMBER_DAILYHOTEL)
					.toString()));
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}


	}

}
