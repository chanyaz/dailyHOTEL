/**
 * Copyright (c) 2014 Daily Co., Ltd. All rights reserved.
 *
 * SettingFragment (설정 화면)
 * 
 * 어플리케이션의 설정 화면이다. 뷰는 리스트뷰처럼 보이나 리스트뷰처럼 보이도록
 * 구성된 화면일 뿐이다. 이 화면에서 현재 로그인 상태를 가져오기 위해 네트워
 * 크 작업을 하기도 한다.
 *
 * @since 2014-02-24
 * @version 1
 * @author Mike Han(mike@dailyhotel.co.kr)
 */
package com.twoheart.dailyhotel;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.twoheart.dailyhotel.activity.AboutActivity;
import com.twoheart.dailyhotel.activity.FAQActivity;
import com.twoheart.dailyhotel.activity.LoginActivity;
import com.twoheart.dailyhotel.activity.NoticeActivity;
import com.twoheart.dailyhotel.activity.ProfileActivity;
import com.twoheart.dailyhotel.activity.VersionActivity;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.RenewalGaManager;
import com.twoheart.dailyhotel.util.network.VolleyHttpClient;
import com.twoheart.dailyhotel.util.network.request.DailyHotelJsonRequest;
import com.twoheart.dailyhotel.util.network.request.DailyHotelStringRequest;
import com.twoheart.dailyhotel.util.network.response.DailyHotelJsonResponseListener;
import com.twoheart.dailyhotel.util.network.response.DailyHotelStringResponseListener;
import com.twoheart.dailyhotel.util.ui.BaseFragment;

public class SettingFragment extends BaseFragment implements Constants, OnClickListener
{

	private MainActivity mHostActivity;
	private RequestQueue mQueue;

	private TextView tvNotice, tvHelp, tvMail, tvLogin, tvEmail, tvCall,
			tvAbout, tvVersion;
	private LinearLayout llVersion, llLogin;
	private String profileStr, loginStr;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{

		View view = inflater.inflate(R.layout.fragment_setting, container, false);

		// ActionBar Setting
		mHostActivity = (MainActivity) getActivity();
		mQueue = VolleyHttpClient.getRequestQueue();

		tvNotice = (TextView) view.findViewById(R.id.tv_setting_notice);
		tvVersion = (TextView) view.findViewById(R.id.tv_setting_version);
		llVersion = (LinearLayout) view.findViewById(R.id.ll_setting_version);
		tvHelp = (TextView) view.findViewById(R.id.tv_setting_help);
		tvMail = (TextView) view.findViewById(R.id.tv_setting_mail);
		llLogin = (LinearLayout) view.findViewById(R.id.ll_setting_login);
		tvLogin = (TextView) view.findViewById(R.id.tv_setting_login);
		tvEmail = (TextView) view.findViewById(R.id.tv_setting_email);
		tvCall = (TextView) view.findViewById(R.id.tv_setting_call);
		tvAbout = (TextView) view.findViewById(R.id.tv_setting_introduction);

		tvNotice.setOnClickListener(this);
		llVersion.setOnClickListener(this);
		tvHelp.setOnClickListener(this);
		tvMail.setOnClickListener(this);
		llLogin.setOnClickListener(this);
		tvCall.setOnClickListener(this);
		tvAbout.setOnClickListener(this);

		try
		{
			String currentVersion = mHostActivity.getPackageManager().getPackageInfo(mHostActivity.getPackageName(), 0).versionName;

			tvVersion.setText(currentVersion);
		} catch (NameNotFoundException e)
		{
			onError(e);
		}

		return view;
	}

	@Override
	public void onResume()
	{
		super.onResume();

		mHostActivity.setActionBar(getString(R.string.actionbar_title_setting_frag), false);
		profileStr = getString(R.string.frag_profile);
		loginStr = getString(R.string.frag_login);

		lockUI();
		mQueue.add(new DailyHotelStringRequest(Method.GET, new StringBuilder(URL_DAILYHOTEL_SERVER).append(URL_WEBAPI_USER_ALIVE).toString(), null, mUserAliveStringResponseListener, mHostActivity));

		RenewalGaManager.getInstance(mHostActivity.getApplicationContext()).recordScreen("setting", "/settings/");
	}

	@Override
	public void onClick(View v)
	{
		if (v.getId() == tvNotice.getId())
		{
			Intent i = new Intent(mHostActivity, NoticeActivity.class);
			startActivity(i);
			mHostActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);

		} else if (v.getId() == llVersion.getId())
		{
			Intent i = new Intent(mHostActivity, VersionActivity.class);
			startActivity(i);
			mHostActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);

		} else if (v.getId() == tvHelp.getId())
		{
			Intent i = new Intent(mHostActivity, FAQActivity.class);
			startActivity(i);
			mHostActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);

		} else if (v.getId() == tvMail.getId())
		{
			Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:help@dailyhotel.co.kr"));
			intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mail_text_subject));
			intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.mail_text_desc));
			intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK);

			startActivity(Intent.createChooser(intent, getString(R.string.mail_text_dialog_title)));
			RenewalGaManager.getInstance(mHostActivity.getApplicationContext()).recordEvent("click", "mailCS", null, null);
		} else if (v.getId() == llLogin.getId())
		{
			if (tvLogin.getText().equals(getString(R.string.frag_profile)))
			{
				// 로그인 되어 있는 상태
				Intent i = new Intent(mHostActivity, ProfileActivity.class);
				startActivity(i);
				mHostActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);

			} else
			{
				// 로그아웃 상태
				chgClickable(llLogin);
				Intent i = new Intent(mHostActivity, LoginActivity.class);
				startActivityForResult(i, CODE_REQUEST_ACTIVITY_LOGIN);
				mHostActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);
			}

		} else if (v.getId() == tvCall.getId())
		{
			Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse(new StringBuilder("tel:").append(PHONE_NUMBER_DAILYHOTEL).toString()));
			startActivity(i);
			RenewalGaManager.getInstance(mHostActivity.getApplicationContext()).recordEvent("click", "inquireCS", null, null);
		} else if (v.getId() == tvAbout.getId())
		{
			Intent i = new Intent(mHostActivity, AboutActivity.class);
			startActivity(i);
			mHostActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		super.onActivityResult(requestCode, resultCode, intent);

		if (requestCode == CODE_REQUEST_ACTIVITY_LOGIN)
		{
			chgClickable(llLogin);

			if (resultCode == Activity.RESULT_OK)
			{
				mHostActivity.selectMenuDrawer(mHostActivity.menuHotelListFragment);
			}
		}
	}

	private void invalidateLoginButton(boolean login, String email)
	{
		tvEmail.setText(email);

		if (login)
		{
			tvLogin.setText(profileStr);
			tvEmail.setVisibility(View.VISIBLE);
		} else
		{
			tvLogin.setText(loginStr);
			tvEmail.setVisibility(View.GONE);
		}

	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Listener
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private DailyHotelStringResponseListener mUserAliveStringResponseListener = new DailyHotelStringResponseListener()
	{

		@Override
		public void onResponse(String url, String response)
		{
			if(getActivity() == null)
			{
				return;
			}
			
			String result = null;

			if (TextUtils.isEmpty(response) == false)
			{
				result = response.trim();
			}

			if ("alive".equalsIgnoreCase(result) == true)
			{ // session alive
				// 사용자 정보 요청.
				mQueue.add(new DailyHotelJsonRequest(Method.GET, new StringBuilder(URL_DAILYHOTEL_SERVER).append(URL_WEBAPI_USER_INFO).toString(), null, mUserInfoJsonResponseListener, mHostActivity));

			} else
			{
				invalidateLoginButton(false, "");
				unLockUI();
			}
		}
	};

	private DailyHotelJsonResponseListener mUserInfoJsonResponseListener = new DailyHotelJsonResponseListener()
	{

		@Override
		public void onResponse(String url, JSONObject response)
		{
			if(getActivity() == null)
			{
				return;
			}
			
			try
			{
				if (response == null)
				{
					throw new NullPointerException("response == null");
				}

				String userEmail = response.getString("email");

				if ((userEmail != null) && !(userEmail.equals("")) && !(userEmail.equals("null")))
				{
					invalidateLoginButton(true, userEmail);
				} else
				{
					invalidateLoginButton(true, "");
				}

				unLockUI();

			} catch (Exception e)
			{
				onError(e);
				invalidateLoginButton(true, "");
			}
		}
	};

	//	@Override
	//	public void onResponse(String url, String response) {
	//		if (url.contains(URL_WEBAPI_USER_ALIVE)) {
	//			String result = response.trim();
	//
	//			if (result.equals("alive")) { // session alive
	//				// 사용자 정보 요청.
	//				mQueue.add(new DailyHotelJsonRequest(Method.GET,
	//						new StringBuilder(URL_DAILYHOTEL_SERVER).append(
	//								URL_WEBAPI_USER_INFO).toString(), null, this,
	//						mHostActivity));
	//
	//			} else {
	//				invalidateLoginButton(false, "");
	//				unLockUI();
	//			}
	//		}
	//	}
	//
	//	@Override
	//	public void onResponse(String url, JSONObject response) {
	//		if (url.contains(URL_WEBAPI_USER_INFO)) {
	//			try {
	//				JSONObject obj = response;
	//				String userEmail = obj.getString("email");
	//				
	//				if ((userEmail != null) && !(userEmail.equals("")) && !(userEmail.equals("null"))) invalidateLoginButton(true, userEmail);
	//				else invalidateLoginButton(true, "");
	//				
	//				unLockUI();
	//
	//			} catch (Exception e) {
	//				onError(e);
	//				invalidateLoginButton(true, "");
	//			}
	//
	//		}
	//
	//	}
}
