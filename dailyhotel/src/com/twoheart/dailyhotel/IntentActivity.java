/**
 * Copyright (c) 2014 Daily Co., Ltd. All rights reserved.
 *
 * MainActivity (메인화면)
 * 
 * 어플리케이션의 주 화면으로서 최초 실행 시 보여지는 화면이다. 이 화면은 어플리케이션
 * 최초 실행 시 SplashActivity를 먼저 띄우며, 대부분의 어플리케이션 초기화 작업을 
 * SplashActivity에게 넘긴다. 그러나, 일부 초기화 작업도 수행하며, 로그인 세션관리와
 * 네비게이션 메뉴를 표시하는 일을 하는 화면이다.
 *
 * @since 2014-02-24
 * @version 1
 * @author Mike Han(mike@dailyhotel.co.kr)
 */
package com.twoheart.dailyhotel;

import com.facebook.FacebookSdk;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class IntentActivity extends Activity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		FacebookSdk.sdkInitialize(getApplicationContext());

		Intent intent = getIntent();

		if (intent == null)
		{
			return;
		}

		Intent newIntent = new Intent(getApplicationContext(), MainActivity.class);

		Uri uri = getIntent().getData();

		if (uri != null)
		{
			final String KAKAOLINK = "kakaolink";
			final String DAILYHOTEL = "dailyhotel";

			String link = uri.toString();

			// link로 진입한 경우
			if (link.indexOf(KAKAOLINK) >= 0 || link.indexOf(DAILYHOTEL) >= 0)
			{
				newIntent.setData(uri);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			}
		}

		startActivity(newIntent);
		overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
		finish();
	}
}
