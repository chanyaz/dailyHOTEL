package com.twoheart.dailyhotel.activity;

import android.os.Bundle;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.util.ui.WebViewActivity;

public class PrivacyActivity extends WebViewActivity {
	
	private static final String TAG = "PersonalInfoActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBar("����������޹�ħ");
		setContentView(R.layout.activity_term);
		setWebView(URL_WEB_PRIVACY);
		
	}
	
}
