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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RequestQueue.RequestFilter;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.GlobalFont;
import com.twoheart.dailyhotel.util.network.VolleyHttpClient;
import com.twoheart.dailyhotel.util.network.request.DailyHotelRequest;

public class BaseActivity extends ActionBarActivity implements Constants, OnLoadListener, ErrorListener {

	private final static String TAG = "BaseActivity";

	public ActionBar actionBar;
	public SharedPreferences sharedPreference;
	
	protected RequestQueue mQueue;
	protected Toast mToast;
	
	private LoadingDialog mLockUI;

	private RequestFilter cancelAllRequestFilter;

	private Handler handler;

	private Runnable networkCheckRunner;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPreference = getSharedPreferences(NAME_DAILYHOTEL_SHARED_PREFERENCE, Context.MODE_PRIVATE);
		mQueue = VolleyHttpClient.getRequestQueue();
		mLockUI = new LoadingDialog(this);
		
		cancelAllRequestFilter = new RequestQueue.RequestFilter() {
		    @Override
	        public boolean apply(Request<?> request) {
	            return true;
	        }
	    };
	    
	    handler = new Handler();
		networkCheckRunner = new Runnable() {
			@Override
			public void run() {
				if(mLockUI.isVisible()) {
					android.util.Log.e("EXPIRED_UNLOCK","true");
					mQueue.cancelAll(cancelAllRequestFilter);
					unLockUI();
					onError();
				}
			}
		};
	    
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		// RequestQueue�� ��ϵ� ��� Request���� ����Ѵ�.
		if (mQueue != null)
			mQueue.cancelAll(cancelAllRequestFilter);
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		
		GlobalFont.apply((ViewGroup) findViewById(android.R.id.content).getRootView());
	}
	
	/**
	 * �׼ǹٸ� �����ϴ� �޼���μ�, ���ø����̼� �׼ǹ� �׸��� �����ϰ� ������ �����Ѵ�.
	 * 
	 * @param title �׼ǹٿ� ǥ���� ȭ���� ������ �޴´�.
	 */
	public void setActionBar(String title) {
		actionBar = getSupportActionBar();
		
//		int resType = DeviceResolutionUtil.getResolutionType(this);
		
		// bottom�� 1px ���м� �߰��� �� ���.
		actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_background));
		
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		
		actionBar.setIcon(R.drawable.img_ic_menu);
		actionBar.setTitle(title);
		
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
	}
	
	public void setActionBar(int strId) {
		setActionBar(getString(strId));
	}
	
	
	/**
	 * �׼ǹٿ� ProgressBar�� ǥ���� �� �ֵ��� �����Ѵ�.
	 */
	public void setActionBarProgressBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
			setSupportProgressBarIndeterminate(true);
		}
	}
	
	/**
	 * �׼ǹٸ� ���⵵�� �����Ѵ�.
	 * 
	 */
	public void setActionBarHide() {
		supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
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
		
		// ���� Activity�� �������� Toast�� �����Ѵ�.
		if (mToast != null)
			mToast.cancel();
		
		try {
			CookieSyncManager.getInstance().stopSync();
			
		} catch (Exception e) {
			CookieSyncManager.createInstance(getApplicationContext());
			CookieSyncManager.getInstance().stopSync();
			
		}
		
		super.onPause();
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		try {
			CookieSyncManager.getInstance().startSync();
		} catch (Exception e) {
			CookieSyncManager.createInstance(getApplicationContext());
			CookieSyncManager.getInstance().startSync();
		}
		
		com.facebook.AppEventsLogger.activateApp(this, getString(R.string.app_id));
		
	}
	
	@Override
	protected void onStop() {
		
		// ���� Activity�� ��ϵ� Request�� ����Ѵ�. 
		if (mQueue != null)
			mQueue.cancelAll(new RequestQueue.RequestFilter() {
			    @Override
		        public boolean apply(Request<?> request) {
			    		DailyHotelRequest<?> dailyHotelRequest = (DailyHotelRequest<?>) request;
			    		
			    		if (dailyHotelRequest != null && dailyHotelRequest.getTag() != null)
			    			if (dailyHotelRequest.getTag().equals(this)) {
			    				return true;
			    			}
			    				
		            return false;
		        }
		    });
		
		super.onStop();
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

	/**
	 * LoadingDialog�� ��� �ε� ������ ��Ÿ���� ����ڰ� UI�� ����� �� ������ �Ѵ�.
	 */
	@Override
	public void lockUI() {
		android.util.Log.e("LOCKED","a");
		mLockUI.show();
		// ���� ���ѽð��� �����µ��� ������Ʈ�� ������ �ʾҴٸ� Error �߻�.
		handler.postDelayed(networkCheckRunner, REQUEST_EXPIRE_JUDGE);
		
	}

	/**
	 * �ε��� �Ϸ�Ǿ� LoadingDialog�� �����ϰ� ���� ��Ʈ�� �����Ѵ�.
	 */
	@Override
	public void unLockUI() {
		android.util.Log.e("UNLOCKED","a");
		GlobalFont.apply((ViewGroup) findViewById(android.R.id.content).getRootView());
		mLockUI.hide();
		handler.removeCallbacks(networkCheckRunner);
		
	}

	@Override
	protected void onDestroy() {
		mLockUI.hide();
		super.onDestroy();
	}

	@Override
	public void onErrorResponse(VolleyError error) {
		if (DEBUG) {
			error.printStackTrace();
		}
		
		onError();
	}
	
	public void onError(Exception error) {
		if (DEBUG) {
			error.printStackTrace();
		}
		
		onError();
	}
	
	/**
	 * Error �߻� �� �б�Ǵ� �޼���
	 */
	public void onError() {
		// �߸��� ��Ʈ, ��� ������ �������� �����Ե�. ���� �ʿ�.
		showToast("���ͳ� ���� ���°� �Ҿ����մϴ�.\n���ͳ� ������ Ȯ���Ͻ� �� �ٽ� �õ����ּ���.", Toast.LENGTH_LONG, false);
	}
	
	/**
	 * Toast�� ���� ǥ�����ִ� �޼���μ�, ���� Context�δ� ApplicationContext�� ����Ѵ�. 
	 * �Ｚ �ܸ��⿡�� �Ｚ �׸��� ����ϱ� �����̴�.
	 * 
	 * @param message Toast�� ǥ���� ����
	 * @param length Toast�� ǥ�õǴ� �ð�. Toast.LENGTH_SHORT, Toast.LENGTH_LONG
	 * @param isAttachToActivity	���� Activity�� ����Ǹ� Toast�� ���������� �����Ѵ�
	 */
	public void showToast(String message, int length, boolean isAttachToActivity) {
		if (mToast != null)
			mToast.cancel();
		
		if (isAttachToActivity) {
			mToast = Toast.makeText(getApplicationContext(), message, length);
			mToast.show();
			
		} else {
			Toast.makeText(getApplicationContext(), message, length).show();
			
		}
	}
	
	/**
	 * ��ư ��Ÿ�� �����ϱ� ���� �޼���, ��ư�� Ŭ�� ���� ���θ� �ݴ�� ����.
	 * @param v Ÿ�� ��
	 */
	protected void chgClickable(View v) {
		v.setClickable(!v.isClickable());
	}
	

	protected void chgClickable(View v, boolean isClickable) {
		v.setClickable(isClickable);
	}
	
}
