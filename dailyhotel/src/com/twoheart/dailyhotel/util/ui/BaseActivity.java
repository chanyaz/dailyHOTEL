/**
 * Copyright (c) 2014 Daily Co., Ltd. All rights reserved.
 *
 * BaseActivity
 * 
 * ActionBarCompat ���̺귯���� ActionBarActivity�� ��ӹ޴� A
 * ctivity�μ� ���ø����̼ǿ��� ���Ǵ� Activity���� UI�� �⺻������ ��
 * ���ϴµ� �ʿ��� API �޼������ �����Ѵ�. �Ӹ� �ƴ϶�, CookieSyncMana
 * ger�� �ν��Ͻ��� �����ϱ⵵ �ϸ�, ���ø����̼��� SharedPreference��
 * �����ϱ⵵ �Ѵ�.
 *
 * @since 2014-02-24
 * @version 1
 * @author Mike Han(mike@dailyhotel.co.kr)
 */
package com.twoheart.dailyhotel.util.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieSyncManager;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.GlobalFont;

public class BaseActivity extends ActionBarActivity implements Constants {

	private final static String TAG = "BaseActivity";

	public ActionBar actionBar;
	public SharedPreferences sharedPreference;
	public CookieSyncManager cookieSyncManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPreference = getSharedPreferences(NAME_DAILYHOTEL_SHARED_PREFERENCE, Context.MODE_PRIVATE);
		
	}
	
	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		
		GlobalFont.apply((ViewGroup) findViewById(android.R.id.content).getRootView());
	}
	
	/**
	 * setActionBar(String title)
	 * �׼ǹ� ���� �޼ҵ�
	 * 
	 * @param title
	 */
	public void setActionBar(String title) {
		actionBar = getSupportActionBar();
		
		actionBar.setBackgroundDrawable(new ColorDrawable(getResources().
				getColor(android.R.color.white)));
		
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		
		actionBar.setIcon(R.drawable.img_ic_menu);
		actionBar.setTitle(title);
		
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
	}
	
	/**
	 * setActionBarHide()
	 * �׼ǹٸ� �����ִ� �޼ҵ�
	 * 
	 */
	public void setActionBarHide() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			getSupportActionBar().hide();
		
	}
	  
	// �޴� ��ư�� ���ƹ���.
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ( keyCode == KeyEvent.KEYCODE_MENU ) {
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}   
	
	@Override
	protected void onPause() {
		if (cookieSyncManager != null)
			cookieSyncManager.stopSync();
		
		super.onPause();
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		try {
			if (cookieSyncManager != null)
				cookieSyncManager.startSync();
		} catch (Exception e) {
			if (DEBUG)
				e.printStackTrace();
			
			
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
