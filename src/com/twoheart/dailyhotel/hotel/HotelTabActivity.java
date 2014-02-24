package com.twoheart.dailyhotel.hotel;

import static com.twoheart.dailyhotel.AppConstants.*;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.asynctask.GeneralHttpTask;
import com.twoheart.dailyhotel.asynctask.onCompleteListener;
import com.twoheart.dailyhotel.common.view.ViewPagerCustom;
import com.twoheart.dailyhotel.payment.HotelPaymentActivity;
import com.twoheart.dailyhotel.setting.LoginActivity;
import com.viewpagerindicator.TabPageIndicator;

public class HotelTabActivity extends SherlockFragmentActivity implements OnClickListener{
	
	private static final String TAG = "HotelTabActivity";
	
	private static final int HOTEL_TAB_ACTIVITY = 1;
	
	private ViewPagerCustom pager;
	private TabPageIndicator indicator;
	
	String hotel_name;
	int hotel_idx;
	int avail_cnt;
	
	private Button tv_soldout;
	private Button btn_booking;
	
	private SharedPreferences prefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hotel_tab);
		
		// 선택된 호텔의 name, idx, avail_cnt 받음
		Intent intent = getIntent();
		avail_cnt = intent.getIntExtra("available_cnt", 0);
		
		prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
		SharedPreferences.Editor ed = prefs.edit();
		ed.putString(PREFERENCE_HOTEL_IDX, Integer.toString(intent.getIntExtra("hotel_idx", 0)));
		ed.putString(PREFERENCE_HOTEL_NAME, intent.getStringExtra("hotel_name"));
		ed.putString(PREFERENCE_HOTEL_YEAR, intent.getStringExtra("year"));
		ed.putString(PREFERENCE_HOTEL_MONTH, intent.getStringExtra("month"));
		ed.putString(PREFERENCE_HOTEL_DAY, intent.getStringExtra("day"));
		ed.commit();
		
		loadResource();
		
		// actionbar setting
		setTitle("");
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setIcon(R.drawable.dh_ic_menu_back);
		Drawable myDrawable;
		Resources res = getResources();
		try {
		   myDrawable = Drawable.createFromXml(res, res.getXml(R.drawable.dh_actionbar_background));
		   getSupportActionBar().setBackgroundDrawable(myDrawable);
		} catch (Exception ex) {
		   Log.e(TAG, "Exception loading drawable"); 
		}
		
		// create viewpager
		FragmentPagerAdapter adapter = new HotelTabAdapter(getSupportFragmentManager());
		pager.setOffscreenPageLimit(3);
		pager.setAdapter(adapter);
		
		indicator.setViewPager(pager);
	}
	
	public void loadResource() {
		pager = (ViewPagerCustom) findViewById(R.id.pager);
		indicator = (TabPageIndicator) findViewById(R.id.indicator);
		
		tv_soldout = (Button) findViewById(R.id.tv_hotel_tab_soldout);
		btn_booking = (Button) findViewById(R.id.btn_hotel_tab_booking);
		btn_booking.setOnClickListener(this);
		
		// 호텔 sold out시
		if(avail_cnt == 0) {
			btn_booking.setVisibility(View.GONE);
			tv_soldout.setVisibility(View.VISIBLE);
		}
	}
	
	public void parseJson(String str) {
		Log.d("aa","Aaa");
		
		
		//create viewpage
		FragmentPagerAdapter adapter = new HotelTabAdapter(getSupportFragmentManager());
		pager.setOffscreenPageLimit(3);
		pager.setAdapter(adapter);
		indicator.setViewPager(pager);
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	@Override
	public void onClick(View v) {
		if(v.getId() == btn_booking.getId()) {
			
			if(checkLogin()) {
				Intent i = new Intent(this, HotelPaymentActivity.class);
				startActivityForResult(i, HOTEL_TAB_ACTIVITY);
				overridePendingTransition(R.anim.slide_in_right,R.anim.hold);
			} else {
				
				Intent i = new Intent(this, LoginActivity.class);
				startActivity(i);
				overridePendingTransition(R.anim.slide_in_right,R.anim.hold);
			}
		}
	}
	
	public boolean checkLogin() {
		prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
		return prefs.getBoolean(PREFERENCE_IS_LOGIN, false);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if(requestCode == HOTEL_TAB_ACTIVITY) {
			if(resultCode == RESULT_OK) {
				setResult(RESULT_OK);
				finish();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
