package com.twoheart.dailyhotel.activity;


import static com.twoheart.dailyhotel.util.AppConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.google.android.gcm.GCMRegistrar;
import com.twoheart.dailyhotel.MainActivity;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.R.anim;
import com.twoheart.dailyhotel.R.layout;
import com.twoheart.dailyhotel.util.AvailableNetwork;
import com.twoheart.dailyhotel.util.network.GeneralHttpTask;
import com.twoheart.dailyhotel.util.network.OnCompleteListener;

public class SplashActivity extends Activity {
	
	private final static String TAG = "SplashActivity";
	private final static String SENDER_ID = "288636757896";  // project ID
	
	private static final String CAMPAIGN_SOURCE_PARAM = "utm_source";
	
//	private ImageView iv_loading;
	private boolean isNewVersion;
	
	private Tracker mGaTracker;
	private GoogleAnalytics mGaInstance;
	
	SharedPreferences prefs;
	
	@Override
	protected void onStart() {
		super.onStart();
		HashMap<String, String> hitParameters = new HashMap<String, String>();
		hitParameters.put(Fields.HIT_TYPE, "appview");
		hitParameters.put(Fields.SCREEN_NAME, "Intro Screen");
		
		mGaTracker.send(hitParameters);
		
		// Get the intent that started this Activity.
//	    Intent intent = this.getIntent();
//	    Uri uri = intent.getData();

	    // Send a screenview using any available campaign or referrer data.
//	    MapBuilder.createAppView().setAll(getReferrerMapFromUri(uri));
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		
		// google analytics
		mGaInstance = GoogleAnalytics.getInstance(this);
		mGaTracker = mGaInstance.getTracker("UA-43721645-1");
		
		// loading image
//		iv_loading = (ImageView) findViewById(R.id.iv_splash_spinner);
		Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotate);
		rotation.setRepeatCount(Animation.INFINITE);
//		iv_loading.startAnimation(rotation);
		
		
		// sleep 2 second
		Handler h = new Handler();
		h.postDelayed(new Runnable() {
			public void run() {
				// network check
				if(checkNetwork()) {
					new GeneralHttpTask(versionListener, getApplicationContext()).execute(REST_URL + VERSION);
				} else {
					Toast.makeText(getApplicationContext(), "��Ʈ��ũ ���¸� Ȯ���� �ּ���", Toast.LENGTH_SHORT).show();
					finish();
				}
			}
		}, 2000);	
	}
	
	
	  /*
	   * Given a URI, returns a map of campaign data that can be sent with
	   * any GA hit.
	   *
	   * @param uri A hierarchical URI that may or may not have campaign data
	   *     stored in query parameters.
	   *
	   * @return A map that may contain campaign or referrer
	   *     that may be sent with any Google Analytics hit.
	   */
	  Map<String,String> getReferrerMapFromUri(Uri uri) {

	    MapBuilder paramMap = new MapBuilder();

	    // If no URI, return an empty Map.
	    if (uri == null) { return paramMap.build(); }

	    // Source is the only required campaign field. No need to continue if not
	    // present.
	    if (uri.getQueryParameter(CAMPAIGN_SOURCE_PARAM) != null) {

	      // MapBuilder.setCampaignParamsFromUrl parses Google Analytics campaign
	      // ("UTM") parameters from a string URL into a Map that can be set on
	      // the Tracker.
	      paramMap.setCampaignParamsFromUrl(uri.toString());

	     // If no source parameter, set authority to source and medium to
	     // "referral".
	     } else if (uri.getAuthority() != null) {

	       paramMap.set(Fields.CAMPAIGN_MEDIUM, "referral");
	       paramMap.set(Fields.CAMPAIGN_SOURCE, uri.getAuthority());

	     }

	     return paramMap.build();
	  }
	
	@Override
	public void onBackPressed() {
		return;
	}
	
	public Boolean checkNetwork() {
		boolean result = false;
		
	    AvailableNetwork net_status = AvailableNetwork.getInstance();  
        
	    switch (net_status.getNetType(getApplicationContext())) {
		    case AvailableNetwork.NET_TYPE_WIFI:  
		    	//WIFI �������
		    	result = true;
		        break;  
		    case AvailableNetwork.NET_TYPE_3G:  
		    	// 3G Ȥ�� LTE���� ����
		    	result = true;
		        break;  
		    case AvailableNetwork.NET_TYPE_NONE: 
		    	result = false;
		    	break;
	    }
	    return result;
	}
	
	public void checkAutoLogin() {
		prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
		
		if(prefs.getBoolean(PREFERENCE_AUTO_LOGIN, false)) {  //�ڵ� �α���
			new GeneralHttpTask(loginListener, getApplicationContext()).execute(REST_URL + LOGIN);
		} else {
			startMain();
		}
	}
	
	public void checkGCM() {
		prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
		
		if(!prefs.getBoolean(PREFERENCE_GCM, false)) {  // regId�� ������ ���� �ȵǾ� ������
			unregisterToken();
			registerToken();
		} else {
		}
	}
	
	// ���� ������ ���� ������ ��
	public void checkVersion() {

		try {
			
			prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
			
			int max_version = Integer.parseInt(prefs.getString(PREFERENCE_MAX_VERSION_NAME, null).replace(".", ""));
			int min_version = Integer.parseInt(prefs.getString(PREFERENCE_MIN_VERSION_NAME, null).replace(".", ""));
			int current_version = Integer.parseInt(this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName.replace(".", ""));
			
			if( (current_version >= max_version) && (current_version >= min_version) ) { 				// �ֽ� version
				
				checkAutoLogin();

			} else if(min_version > current_version){				// ���� ������Ʈ
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(SplashActivity.this);
				alertDialog.setTitle("����").setMessage("dailyHotel �ֽ� ������ ���Խ��ϴ�. ������Ʈ ���ּ���").setCancelable(false).setPositiveButton("������Ʈ",
				new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				    	Intent marketLaunch = new Intent(Intent.ACTION_VIEW); 
				    	marketLaunch.setData(Uri.parse("market://details?id=com.twoheart.dailyhotel")); 
				    	startActivity(marketLaunch);
				    	finish();
				    }
				}).setNegativeButton("���",
				new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				    	finish();
				    return;
				    }
				});
				AlertDialog alert = alertDialog.create();
				alert.show();
			}
			
		} catch (UnsupportedOperationException e) {
			Toast.makeText(this, "���� �÷��� ���񽺸� �̿��� �� �ִ� ����̾�� �մϴ�.", Toast.LENGTH_LONG).show();
			finish();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void registerToken() {
		
		// ����̽� GCM ��� �������� Ȯ��
		GCMRegistrar.checkDevice(this);
		// �Ŵ��佺Ʈ ������ �ùٸ��� Ȯ��
		GCMRegistrar.checkManifest(this);
		
		// registration ID������̽� ��ū) ����ϰ� ��ϵ��� ���� ��� GCM�� ���
		final String regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals("")) {
			GCMRegistrar.register(getBaseContext(), SENDER_ID);
			GCMRegistrar.setRegisteredOnServer(this, true);
			Log.v("TAG", "registered GCM");
		} else {
			if(GCMRegistrar.isRegisteredOnServer(this)) {
				Log.v("TAG", "Already registered");
				Log.v("TAG", regId);
			} else {
				GCMRegistrar.register(getBaseContext(), SENDER_ID);
				GCMRegistrar.setRegisteredOnServer(this, true);
				Log.v("TAG", "registered GCM");
			}
		}
	}
	
	public void unregisterToken() {
		if (GCMRegistrar.isRegistered(this)) {
			GCMRegistrar.unregister(this);
		}
	}
	
	public void parseVersionJson(String str) {
		try {
			JSONObject obj = new JSONObject(str);
			
			prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
			SharedPreferences.Editor ed = prefs.edit();
			
			ed.putString(PREFERENCE_MAX_VERSION_NAME, obj.getString("play_max"));
			ed.putString(PREFERENCE_MIN_VERSION_NAME, obj.getString("play_min"));
			ed.putString(PREFERENCE_NEW_EVENT, obj.getString("new_event"));
			ed.commit();
			
			checkVersion();
			
		} catch (Exception e) {
			Log.d(TAG, "parseVersionJson " + e.toString());
			Toast.makeText(getApplicationContext(), "��Ʈ��ũ ���¸� Ȯ���� �ּ���", Toast.LENGTH_SHORT).show();
			finish();
		}
	}
	
	public void parseLoginJson(String str) {
		try {
			JSONObject obj = new JSONObject(str);
			if ( obj.getString("login").equals("true") ) {
				// �α��� ����
				// �α������� true
				SharedPreferences.Editor ed = prefs.edit();
				ed.putBoolean(PREFERENCE_IS_LOGIN, true);
				ed.commit();
				
			} else {
				// �α��� ����
				// data �ʱ�ȭ
				SharedPreferences.Editor ed = prefs.edit();
				ed.putBoolean(PREFERENCE_AUTO_LOGIN, false);
				ed.putString(PREFERENCE_USER_ID, null);
				ed.putString(PREFERENCE_USER_PWD, null);
				ed.putBoolean(PREFERENCE_IS_LOGIN, false);
				ed.commit();
			}
		} catch (Exception e) {
			Log.d(TAG, e.toString());
		}
		
		startMain();
	}
	
	public void startMain() {
//		setDefaultRegion();
		checkGCM();
		
		// start main
		Intent intent = new Intent(SplashActivity.this, MainActivity.class);
		startActivity(intent);
//		iv_loading.clearAnimation();
		finish();
	}
	
	public void setDefaultRegion() {
		prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
		SharedPreferences.Editor ed = prefs.edit();
		ed.putString(PREFERENCE_REGION_DEFALUT, "����");
		ed.putString(PREFERENCE_REGION_SELECT, null);
		ed.commit();
	}
	
	// version check callback
	protected OnCompleteListener versionListener = new OnCompleteListener() {
		
		@Override
		public void onTaskFailed() {
			Log.d(TAG, "onTaskFailed");
			Toast.makeText(getApplicationContext(), "��Ʈ��ũ ���¸� Ȯ���� �ּ���", Toast.LENGTH_SHORT).show();
			finish();
		}
		
		@Override
		public void onTaskComplete(String result) {
			Log.d(TAG, result);
			parseVersionJson(result);
		}
	};
	
	protected OnCompleteListener loginListener = new OnCompleteListener() {
		
		@Override
		public void onTaskFailed() {
			Log.d(TAG, "onTaskFailed");
		}
		
		@Override
		public void onTaskComplete(String result) {
			parseLoginJson(result);
		}
	};

}
