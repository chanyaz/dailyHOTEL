/**
 * Copyright (c) 2014 Daily Co., Ltd. All rights reserved.
 *
 * WaitTimerFragment (���� ��� Ÿ�̸� ȭ��)
 * 
 * ���� ���� �ð� ���� ���̴� ȭ���̴�. Ÿ�̸ӿ� �Բ� �ȳ� ��Ʈ�� �ִ� ȭ��
 * ���μ� ���� �ð��� ī��Ʈ�ϸ� ���� �ð��� �˸���. Ÿ�̸��� ��� Handler
 * �� ���������, �����κ��� ���� ���� �ð��� ���� �ð��� ���´�. �׷� ��
 * ���� �ð����κ��� 1�ʾ� ���� ���� �ð����������� �Ǵ���� �Ѵ�.
 *
 * @since 2014-02-24
 * @version 1
 * @author Mike Han(mike@dailyhotel.co.kr)
 */
package com.twoheart.dailyhotel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.twoheart.dailyhotel.activity.EventWebActivity;
import com.twoheart.dailyhotel.model.SaleTime;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.GlobalFont;
import com.twoheart.dailyhotel.util.WakeLock;
import com.twoheart.dailyhotel.util.network.request.DailyHotelJsonRequest;
import com.twoheart.dailyhotel.util.network.response.DailyHotelJsonResponseListener;
import com.twoheart.dailyhotel.util.ui.BaseFragment;

public class WaitTimerFragment extends BaseFragment implements OnClickListener, Constants, DailyHotelJsonResponseListener {

	private final static String TAG = "WaitTimerFragment";
	private final static String KEY_BUNDLE_ARGUMENTS_SALETIME = "saletime";
	public static boolean isEnabledNotify;

	private static Handler sHandler;
	private TextView tvTimer, tvTitle;
	private Button btnNotify;

	private AlarmManager alarmManager;
	private PendingIntent pender;
	private Intent intent;
	private SaleTime mSaleTime;
	private long remainingTime;
	private ImageView ivNewEvent;
	private LinearLayout btnEvent;
	
	public static WaitTimerFragment newInstance(SaleTime saleTime) {
		
		WaitTimerFragment newFragment = new WaitTimerFragment();
		
		Bundle arguments = new Bundle();
		arguments.putParcelable(KEY_BUNDLE_ARGUMENTS_SALETIME, saleTime);
		
		newFragment.setArguments(arguments);
		
		return newFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_wait_timer, container, false);
		mSaleTime = (SaleTime) getArguments().getParcelable(KEY_BUNDLE_ARGUMENTS_SALETIME);
		alarmManager = (AlarmManager) mHostActivity.getApplicationContext()
				.getSystemService(Context.ALARM_SERVICE);
		intent = new Intent(mHostActivity.getApplicationContext(),
				AlarmBroadcastReceiver.class);
		pender = PendingIntent.getBroadcast(mHostActivity.getApplicationContext(), 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		tvTimer = (TextView) view.findViewById(R.id.tv_timer);
		tvTitle = (TextView) view.findViewById(R.id.tv_wait_timer_main);
		btnNotify = (Button) view.findViewById(R.id.btn_wait_timer_alram);
		btnNotify.setOnClickListener(this);
		
		ivNewEvent = (ImageView) view.findViewById(R.id.iv_new_event);
		btnEvent = (LinearLayout) view.findViewById(R.id.btn_event);
		btnEvent.setOnClickListener(this);

		mHostActivity.setActionBar("dailyHOTEL");
		tvTitle.setText(new SimpleDateFormat("aa H").format(mSaleTime.getOpenTime()) + "�� ������ ȣ���� �����˴ϴ�.");
		
		isEnabledNotify = false;
		setTimer();
		
		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		setNotify(isEnabledNotify);
		
		// ���ο� �̺�Ʈ Ȯ���� ���� ���� API ȣ��
		mQueue.add(new DailyHotelJsonRequest(Method.GET, 
				new StringBuilder(URL_DAILYHOTEL_SERVER).append(URL_WEBAPI_APP_VERSION).toString(),
				null, this, mHostActivity));
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == btnNotify.getId()) {
			setNotify(!isEnabledNotify);
		} else if (v.getId() == btnEvent.getId()) {
			Intent i = new Intent(mHostActivity, EventWebActivity.class);
			mHostActivity.startActivity(i);
			mHostActivity.overridePendingTransition(R.anim.slide_in_bottom,
					R.anim.hold);
		}
	}

	private void setNotify(boolean enable) {
		if (enable) {
			btnNotify.setText("�˶� ����");

			if (enable != isEnabledNotify) {
				alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + remainingTime,
						pender);
				
				showToast("�˶��� �����Ǿ����ϴ�", Toast.LENGTH_SHORT, true);
			}

		} else {
			btnNotify.setText("�˶� �ѱ�");

			if (enable != isEnabledNotify) {
				alarmManager.cancel(pender);
				
				showToast("�˶��� ��ҵǾ����ϴ�", Toast.LENGTH_SHORT, true);
			}

		}
		
		isEnabledNotify = enable;

	}

	private void setTimer() {
		
		Date currentDate = new Date(mSaleTime.getCurrentTime());
		Date dailyOpenDate = new Date(mSaleTime.getOpenTime());
		
		remainingTime = dailyOpenDate.getTime() - currentDate.getTime();
		printCurrentRemaingTime(remainingTime);
		
		WakeLock.acquireWakeLock(mHostActivity.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
		
		sHandler = new Handler() {
			public void handleMessage(Message msg) {
				remainingTime -= 1000;
				
				if (remainingTime > 0) {
					printCurrentRemaingTime(remainingTime);
					this.sendEmptyMessageDelayed(0, 1000);
					
				} else {
					this.removeMessages(0);
					WakeLock.releaseWakeLock();
					
					if (mHostActivity != null) {
	 					((MainActivity) mHostActivity)
								.replaceFragment(((MainActivity) mHostActivity)
										.getFragment(MainActivity.INDEX_HOTEL_LIST_FRAGMENT));
						
						mHostActivity = null;
					}
				}
			}
		};

		sHandler.sendEmptyMessageDelayed(0, 1000);

	}
	
	private void printCurrentRemaingTime(long remainingTime) {
		SimpleDateFormat displayTimeFormat = new SimpleDateFormat("HH:mm:ss");
		displayTimeFormat.setTimeZone(TimeZone.getTimeZone("KST"));

		tvTimer.setText(displayTimeFormat.format(remainingTime));
		
	}

	@Override
	public void onDestroy() {
		if (sHandler != null) {
			sHandler.removeMessages(0);
			WakeLock.releaseWakeLock();	
		}
		
		super.onDestroy();
	}

	@Override
	public void onResponse(String url, JSONObject response) {
		if (url.contains(URL_WEBAPI_APP_VERSION)) {
			try {
				if (response.getString("new_event").equals("1")) {
					
					if (ivNewEvent != null)
						ivNewEvent.setVisibility(View.VISIBLE);
					
				}
			} catch (Exception e) {
				onError(e);
			}
		}
	}

}
