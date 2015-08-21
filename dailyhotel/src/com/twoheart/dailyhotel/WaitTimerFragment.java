/**
 * Copyright (c) 2014 Daily Co., Ltd. All rights reserved.
 *
 * WaitTimerFragment (오픈 대기 타이머 화면)
 * 
 * 영업 시작 시간 전에 보이는 화면이다. 타이머와 함께 안내 멘트가 있는 화면
 * 으로서 영업 시간을 카운트하며 영업 시간을 알린다. 타이머의 경우 Handler
 * 를 사용했으며, 서버로부터 영업 시작 시간과 현재 시간을 얻어온다. 그런 후
 * 현재 시간으로부터 1초씩 세어 영업 시간까지인지를 판단토록 한다.
 *
 * @since 2014-02-24
 * @version 1
 * @author Mike Han(mike@dailyhotel.co.kr)
 */
package com.twoheart.dailyhotel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONObject;

import com.android.volley.Request.Method;
import com.twoheart.dailyhotel.model.SaleTime;
import com.twoheart.dailyhotel.util.AnalyticsManager;
import com.twoheart.dailyhotel.util.AnalyticsManager.Screen;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.WakeLock;
import com.twoheart.dailyhotel.util.network.request.DailyHotelJsonRequest;
import com.twoheart.dailyhotel.util.network.response.DailyHotelJsonResponseListener;
import com.twoheart.dailyhotel.util.ui.BaseActivity;
import com.twoheart.dailyhotel.util.ui.BaseFragment;

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
import android.widget.TextView;
import android.widget.Toast;

public class WaitTimerFragment
		extends BaseFragment implements OnClickListener, Constants
{
	private final static String KEY_BUNDLE_ARGUMENTS_SALETIME = "saletime";
	public static boolean isEnabledNotify;

	private static Handler sHandler;
	private TextView tvTimer, tvTitle;
	private TextView btnNotify;

	private AlarmManager alarmManager;
	private PendingIntent pender;
	private Intent intent;
	private SaleTime mSaleTime;
	private long remainingTime;

	//	private ImageView ivNewEvent;
	//	private LinearLayout btnEvent;

	public static WaitTimerFragment newInstance(SaleTime saleTime)
	{
		WaitTimerFragment newFragment = new WaitTimerFragment();

		Bundle arguments = new Bundle();
		arguments.putParcelable(KEY_BUNDLE_ARGUMENTS_SALETIME, saleTime);

		newFragment.setArguments(arguments);

		return newFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		BaseActivity baseActivity = (BaseActivity) getActivity();

		if (baseActivity == null)
		{
			return null;
		}

		View view = null;

		try
		{
			view = inflater.inflate(R.layout.fragment_wait_timer, container, false);
		} catch (OutOfMemoryError errror)
		{
			Util.finishOutOfMemory(baseActivity);
			return null;
		}

		mSaleTime = (SaleTime) getArguments().getParcelable(KEY_BUNDLE_ARGUMENTS_SALETIME);
		alarmManager = (AlarmManager) baseActivity.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		intent = new Intent(baseActivity.getApplicationContext(), AlarmBroadcastReceiver.class);
		pender = PendingIntent.getBroadcast(baseActivity.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		tvTimer = (TextView) view.findViewById(R.id.tv_timer);
		tvTitle = (TextView) view.findViewById(R.id.tv_wait_timer_main);
		btnNotify = (TextView) view.findViewById(R.id.btn_wait_timer_alram);
		//		ivNewEvent = (ImageView) view.findViewById(R.id.iv_new_event);
		//		btnEvent = (LinearLayout) view.findViewById(R.id.btn_event);

		btnNotify.setOnClickListener(this);
		//		btnEvent.setOnClickListener(this);

		baseActivity.setActionBar(getString(R.string.actionbar_title_wait_timer_frag), false);

		SimpleDateFormat sFormat = new SimpleDateFormat("aa H", Locale.KOREA);
		sFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		tvTitle.setText(sFormat.format(mSaleTime.getOpenTime()) + getString(R.string.prefix_wait_timer_frag_todays_hotel_open));

		isEnabledNotify = false;
		setTimer();

		return view;
	}

	@Override
	public void onStart()
	{
		AnalyticsManager.getInstance(getActivity()).recordScreen(Screen.WAIT_TIMER);
		super.onStart();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		setNotify(isEnabledNotify);

		BaseActivity baseActivity = (BaseActivity) getActivity();

		if (baseActivity == null)
		{
			return;
		}

		// 새로운 이벤트 확인을 위해 버전 API 호출
		mQueue.add(new DailyHotelJsonRequest(Method.GET, new StringBuilder(URL_DAILYHOTEL_SERVER).append(URL_WEBAPI_APP_VERSION).toString(), null, mAppVersionResponseListener, baseActivity));
	}

	@Override
	public void onClick(View v)
	{
		if (v.getId() == btnNotify.getId())
		{
			setNotify(!isEnabledNotify);
		}
		//		else if (v.getId() == btnEvent.getId())
		//		{
		//			BaseActivity baseActivity = (BaseActivity) getActivity();
		//
		//			if (baseActivity == null)
		//			{
		//				return;
		//			}
		//
		//			Intent i = new Intent(baseActivity, EventWebActivity.class);
		//			baseActivity.startActivity(i);
		//			baseActivity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold);
		//		}
	}

	private void setNotify(boolean enable)
	{
		if (enable)
		{
			btnNotify.setText(getString(R.string.frag_wait_timer_off));

			if (enable != isEnabledNotify)
			{
				alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + remainingTime, pender);

				showToast(getString(R.string.frag_wait_timer_set), Toast.LENGTH_SHORT, true);
			}

		} else
		{
			btnNotify.setText(getString(R.string.frag_wait_timer_on));

			if (enable != isEnabledNotify)
			{
				alarmManager.cancel(pender);

				showToast(getString(R.string.frag_wait_timer_cancel), Toast.LENGTH_SHORT, true);
			}

		}

		isEnabledNotify = enable;

	}

	private void setTimer()
	{
		BaseActivity baseActivity = (BaseActivity) getActivity();

		if (baseActivity == null)
		{
			return;
		}

		Date currentDate = new Date(mSaleTime.getCurrentTime());
		Date dailyOpenDate = new Date(mSaleTime.getOpenTime());

		remainingTime = dailyOpenDate.getTime() - currentDate.getTime();
		printCurrentRemaingTime(remainingTime);

		WakeLock.acquireWakeLock(baseActivity.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

		sHandler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				BaseActivity baseActivity = (BaseActivity) getActivity();

				if (baseActivity == null || baseActivity.isFinishing() == true)
				{
					return;
				}

				remainingTime -= 1000;

				if (remainingTime > 0)
				{
					printCurrentRemaingTime(remainingTime);
					this.sendEmptyMessageDelayed(0, 1000);

				} else
				{
					this.removeMessages(0);
					WakeLock.releaseWakeLock();

					if (sHandler != null)
					{
						((MainActivity) baseActivity).replaceFragment(((MainActivity) baseActivity).getFragment(MainActivity.INDEX_HOTEL_LIST_FRAGMENT));
						sHandler = null;
					}
				}
			}
		};

		if (sHandler != null)
		{
			sHandler.sendEmptyMessageDelayed(0, 1000);
		}
	}

	private void printCurrentRemaingTime(long remainingTime)
	{
		SimpleDateFormat displayTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.KOREA);
		displayTimeFormat.setTimeZone(TimeZone.getTimeZone("KST"));

		tvTimer.setText(displayTimeFormat.format(remainingTime));

	}

	@Override
	public void onDestroy()
	{
		if (sHandler != null)
		{
			sHandler.removeMessages(0);
			WakeLock.releaseWakeLock();
		}

		super.onDestroy();
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Listener
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private DailyHotelJsonResponseListener mAppVersionResponseListener = new DailyHotelJsonResponseListener()
	{

		@Override
		public void onResponse(String url, JSONObject response)
		{
			if (getActivity() == null)
			{
				return;
			}

			try
			{
				if (response == null)
				{
					throw new NullPointerException("response == null");
				}

				if (response.getString("new_event").equals("1") == true)
				{
					//					if (ivNewEvent != null) ivNewEvent.setVisibility(View.VISIBLE);
				}
			} catch (Exception e)
			{
				onError(e);
			}
		}
	};
}
