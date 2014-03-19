package com.twoheart.dailyhotel.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.twoheart.dailyhotel.GCMIntentService;
import com.twoheart.dailyhotel.MainActivity;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.R.layout;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.Log;
import com.twoheart.dailyhotel.util.network.DailyHotelJsonResponseListener;
import com.twoheart.dailyhotel.util.network.DailyHotelResponseListener;
import com.twoheart.dailyhotel.util.network.VolleyHttpClient;
import com.twoheart.dailyhotel.util.network.vo.DailyHotelJsonRequest;
import com.twoheart.dailyhotel.util.ui.BaseActivity;

public class SplashActivity extends BaseActivity implements Constants,
		DailyHotelJsonResponseListener, ErrorListener {

	private static final String TAG = "SplashActivity";
	private static final String appPlayStoreUrl = "market://details?id=com.twoheart.dailyhotel";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_splash);
		
		VolleyHttpClient.init(getApplicationContext());

		// sleep 2 second
		Handler h = new Handler();
		h.postDelayed(new Runnable() {
			public void run() {
				
				if (!sharedPreference.getBoolean(KEY_PREFERENCE_GCM, false)) {
					GCMIntentService.unregisterToken(getApplicationContext());
					GCMIntentService.registerToken(getApplicationContext());
				}

				RequestQueue queue = VolleyHttpClient.getRequestQueue();
				
				if (sharedPreference.getBoolean(KEY_PREFERENCE_AUTO_LOGIN, false)) {
					Map<String, String> loginParams = new HashMap<String, String>();
					loginParams.put("email", sharedPreference.getString(KEY_PREFERENCE_USER_ID, null));
					loginParams.put("pw", sharedPreference.getString(KEY_PREFERENCE_USER_PWD, null));
	
					queue.add(new DailyHotelJsonRequest(Method.POST, 
							new StringBuilder(URL_DAILYHOTEL_SERVER).append(URL_WEBAPI_USER_LOGIN).toString(),
							loginParams, SplashActivity.this, SplashActivity.this));
				}
				
				queue.add(new DailyHotelJsonRequest(Method.GET, 
						new StringBuilder(URL_DAILYHOTEL_SERVER).append(URL_WEBAPI_APP_VERSION).toString(),
						null, SplashActivity.this, SplashActivity.this));
				
				// start main
				startActivity(new Intent(SplashActivity.this, MainActivity.class));
				finish();

			}
		}, 2000);
		
	}

	@Override
	public void onResponse(String url, JSONObject response) {
		Log.d(TAG, url);
		
		if (url.contains(URL_WEBAPI_USER_LOGIN)) {
			
			try {
				if (!response.getString("login").equals("true")) {
					// �α��� ����
					// data �ʱ�ȭ
					SharedPreferences.Editor ed = sharedPreference.edit();
					ed.putBoolean(KEY_PREFERENCE_AUTO_LOGIN, false);
					ed.putString(KEY_PREFERENCE_USER_ID, null);
					ed.putString(KEY_PREFERENCE_USER_PWD, null);
					ed.commit();

				}
			} catch (JSONException e) {
				if (DEBUG)
					e.printStackTrace();
			} 
			
		} else if (url.contains(URL_WEBAPI_APP_VERSION)) {
//			ed.putString(PREFERENCE_NEW_EVENT, response.getString("new_event"));
			
			try {
				Log.d(TAG, response.getString("play_min"));
			} catch (JSONException e) {
				if (DEBUG)
					e.printStackTrace();
			}
				
			
			try {
				int maxVersion = Integer.parseInt(response.getString("play_max").replace(".", ""));
				int minVersion = Integer.parseInt(response.getString("play_min").replace(".", ""));
				int currentVersion = Integer.parseInt(this.getPackageManager()
						.getPackageInfo(this.getPackageName(), 0).versionName
						.replace(".", ""));

				if (minVersion > currentVersion) { // ���� ������Ʈ
					AlertDialog.Builder alertDialog = new AlertDialog.Builder(
							SplashActivity.this);
					alertDialog
							.setTitle("����")
							.setMessage("dailyHOTEL�� ���ο� ������ ��õǾ����ϴ�. ������Ʈ���ּ���")
							.setCancelable(false)
							.setPositiveButton("������Ʈ",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog,
												int which) {
											Intent marketLaunch = new Intent(
													Intent.ACTION_VIEW);
											marketLaunch.setData(Uri
													.parse(appPlayStoreUrl));
											startActivity(marketLaunch);
											finish();
										}
									})
							.setNegativeButton("���",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog,
												int which) {
											finish();
											return;
										}
									});
					AlertDialog alert = alertDialog.create();
					alert.show();
				}

			} catch (UnsupportedOperationException e) {
				Toast.makeText(this, "���� �÷��� ���񽺸� �̿��� �� �ִ� ����̾�� �մϴ�.",
						Toast.LENGTH_LONG).show();
				finish();
			} catch (Exception e) {
				if (DEBUG)
					e.printStackTrace();
			}
		}
	}

	@Override
	public void onErrorResponse(VolleyError error) {
		if (DEBUG)
			error.printStackTrace();
		Toast.makeText(getApplicationContext(), "��Ʈ��ũ ���¸� Ȯ�����ּ���",
				Toast.LENGTH_LONG).show();
		finish();
	}

}
