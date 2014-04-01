package com.twoheart.dailyhotel.activity;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.Crypto;
import com.twoheart.dailyhotel.util.Log;
import com.twoheart.dailyhotel.util.network.VolleyHttpClient;
import com.twoheart.dailyhotel.util.network.request.DailyHotelJsonRequest;
import com.twoheart.dailyhotel.util.network.response.DailyHotelJsonResponseListener;
import com.twoheart.dailyhotel.util.ui.BaseActivity;
import com.twoheart.dailyhotel.util.ui.LoadingDialog;

public class LoginActivity extends BaseActivity implements Constants,
		OnClickListener, DailyHotelJsonResponseListener, ErrorListener {

	private static final String TAG = "LoginActivity";

	private RequestQueue mQueue;

	private EditText etId, etPwd;
	private CheckBox cbxAutoLogin;
	private Button btnLogin;
	private TextView tvSignUp, tvForgotPwd;
	private LoginButton facebookLogin;

	private Map<String, String> loginParams;
	private Map<String, String> snsSignupParams;

	public Session fbSession;
	public static final String appId = "268888829950610";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBar("�α���");
		setContentView(R.layout.activity_login);

		mQueue = VolleyHttpClient.getRequestQueue();

		etId = (EditText) findViewById(R.id.et_login_id);
		etPwd = (EditText) findViewById(R.id.et_login_pwd);
		cbxAutoLogin = (CheckBox) findViewById(R.id.cb_login_auto);
		tvSignUp = (TextView) findViewById(R.id.tv_login_signup);
		tvForgotPwd = (TextView) findViewById(R.id.tv_login_forgot);
		btnLogin = (Button) findViewById(R.id.btn_login);
		facebookLogin = (LoginButton) findViewById(R.id.authButton);

		tvSignUp.setOnClickListener(this);
		tvForgotPwd.setOnClickListener(this);
		btnLogin.setOnClickListener(this);
		facebookLogin.setOnClickListener(this);

		etPwd.setId(EditorInfo.IME_ACTION_DONE);
		etPwd.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				switch (actionId) {
				case EditorInfo.IME_ACTION_DONE:
					btnLogin.performClick();
					break;
				}
				return false;
			}
		});

	}

	private void makeMeRequest(final Session session) {

		Request request = Request.newMeRequest(session,
				new Request.GraphUserCallback() {

					@Override
					public void onCompleted(GraphUser user, Response response) {

						if (user != null) {
							TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext()
									.getSystemService(Context.TELEPHONY_SERVICE);

							String userEmail = user.getProperty("email")
									.toString();
							String userId = user.getId();
							String encryptedId = Crypto.encrypt(userId)
									.replace("\n", "");
							String userName = user.getName();
//							String phoneNumber = telephonyManager
//									.getLine1Number();
							String deviceId = telephonyManager.getDeviceId();

							snsSignupParams = new HashMap<String, String>();
							loginParams = new HashMap<String, String>();

							if (userEmail != null)
								snsSignupParams.put("email", userEmail);

							if (userId != null) {
								snsSignupParams.put("accessToken", userId);
								loginParams.put("accessToken", userId);
							}

							if (encryptedId != null) {
								snsSignupParams.put("pw", userId); // ȸ������
																	// �ÿ� ����
																	// ���̵忡��
																	// ��ȣȭ
								loginParams.put("pw", encryptedId);
							}

							if (userName != null)
								snsSignupParams.put("name", userName);

//							if (phoneNumber != null)
//								snsSignupParams.put("phone", phoneNumber);

							if (deviceId != null)
								snsSignupParams.put("device", deviceId);

 							mQueue.add(new DailyHotelJsonRequest(Method.POST,
									new StringBuilder(URL_DAILYHOTEL_SERVER)
											.append(URL_WEBAPI_USER_LOGIN)
											.toString(), loginParams,
									LoginActivity.this, LoginActivity.this));
						}
					}
				});

		request.executeAsync();

	}

	@Override
	public void onClick(View v) {
		if (v.getId() == tvForgotPwd.getId()) { // ��й�ȣ ã��
			Intent i = new Intent(this, ForgotPwdActivity.class);
			startActivity(i);
			overridePendingTransition(R.anim.slide_in_right, R.anim.hold);

		} else if (v.getId() == tvSignUp.getId()) { // ȸ������
			Intent i = new Intent(this, SignupActivity.class);
			startActivityForResult(i, CODE_REQEUST_ACTIVITY_SIGNUP);
			overridePendingTransition(R.anim.slide_in_right, R.anim.hold);

		} else if (v.getId() == btnLogin.getId()) { // �α���
			if (!isBlankFields())
				return;

			String md5 = Crypto.encrypt(etPwd.getText().toString()).replace(
					"\n", "");

			loginParams = new LinkedHashMap<String, String>();
			loginParams.put("email", etId.getText().toString());
			loginParams.put("pw", md5);

			LoadingDialog.showLoading(this);

			mQueue.add(new DailyHotelJsonRequest(Method.POST,
					new StringBuilder(URL_DAILYHOTEL_SERVER).append(
							URL_WEBAPI_USER_LOGIN).toString(), loginParams,
					this, this));

		} else if (v.getId() == facebookLogin.getId()) {
			LoadingDialog.showLoading(this);

			fbSession = new Session.Builder(this).setApplicationId(appId)
					.build();

			Session.OpenRequest or = new Session.OpenRequest(this);
			or.setPermissions("email");
			or.setCallback(statusCallback);

			fbSession.openForRead(or);
			Session.setActiveSession(fbSession);
		}
	}

	private Session.StatusCallback statusCallback = new Session.StatusCallback() {

		@Override
		public void call(Session session, SessionState state,
				Exception exception) {

			if (state.isOpened()) {
				makeMeRequest(session);
				
			} else if (state.isClosed()) {
				LoadingDialog.hideLoading();
				fbSession.closeAndClearTokenInformation();
				
				if (exception != null)
					Toast.makeText(LoginActivity.this, "����: " + exception.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
	};

	public boolean isBlankFields() {
		if (etId.getText().toString().trim().length() == 0) {
			Toast.makeText(this, "���̵� �Է����ּ���", Toast.LENGTH_SHORT).show();
			return false;
		}

		if (etPwd.getText().toString().trim().length() == 0) {
			Toast.makeText(this, "��й�ȣ�� �Է����ּ���", Toast.LENGTH_SHORT).show();
			return false;
		}

		return true;
	}

	public void storeLoginInfo() {

		// �ڵ� �α��� üũ��
		if (cbxAutoLogin.isChecked()) {
			String id = loginParams.get("email");
			String pwd = loginParams.get("pw");
			String accessToken = loginParams.get("accessToken");

			SharedPreferences.Editor ed = sharedPreference.edit();
			ed.putBoolean(KEY_PREFERENCE_AUTO_LOGIN, true);

			if (accessToken != null) {
				ed.putString(KEY_PREFERENCE_USER_ACCESS_TOKEN, accessToken);
			} else {
				ed.putString(KEY_PREFERENCE_USER_ID, id);
			}

			ed.putString(KEY_PREFERENCE_USER_PWD, pwd);
			ed.commit();
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == CODE_REQEUST_ACTIVITY_SIGNUP) {
			if (resultCode == RESULT_OK) {
				setResult(RESULT_OK);
				finish();
			}
		} else {
			fbSession.onActivityResult(this, requestCode, resultCode, data);

		}

	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.hold, R.anim.slide_out_right);
	}

	@Override
	public void onErrorResponse(VolleyError error) {
		if (DEBUG)
			error.printStackTrace();

		Toast.makeText(this, "��Ʈ��ũ ���°� ���� �ʽ��ϴ�.\n��Ʈ��ũ ������ �ٽ� Ȯ�����ּ���.",
				Toast.LENGTH_SHORT).show();
		LoadingDialog.hideLoading();

	}

	@Override
	public void onResponse(String url, JSONObject response) {
		if (url.contains(URL_WEBAPI_USER_LOGIN)) {
			JSONObject obj = response;

			try {
				String msg = null;

				if (obj.getBoolean("login")) {
					CookieSyncManager.getInstance().sync();

					// if (obj.length() > 1)
					// etPwd.setText(obj.getString("msg"));

					Log.d(TAG, "�α��� ����");
					Toast.makeText(this, "�α��εǾ����ϴ�", Toast.LENGTH_SHORT).show();
					LoadingDialog.hideLoading();
					storeLoginInfo();

					setResult(RESULT_OK);
					finish();

				} else {

					if (loginParams.containsKey("accessToken")) { // SNS �α����ε�
																	// �������� ���

						cbxAutoLogin.setChecked(true); // ȸ�������� ��� �⺻���� �ڵ� �α�����
														// ��å ��.

						mQueue.add(new DailyHotelJsonRequest(Method.POST,
								new StringBuilder(URL_DAILYHOTEL_SERVER)
										.append(URL_WEBAPI_USER_SIGNUP)
										.toString(), snsSignupParams, this,
								this));

					}

					// �α��� ����
					// ���� msg ���
					else if (obj.length() > 1) {
						LoadingDialog.hideLoading();

						msg = obj.getString("msg");
						AlertDialog.Builder alert = new AlertDialog.Builder(
								this);
						alert.setPositiveButton("Ȯ��",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss(); // �ݱ�
									}
								});
						alert.setMessage(msg);
						alert.show();
					}

				}

			} catch (Exception e) {
				e.printStackTrace();

				LoadingDialog.hideLoading();
				Toast.makeText(this, "��Ʈ��ũ ���°� ���� �ʽ��ϴ�.\n��Ʈ��ũ ������ �ٽ� Ȯ�����ּ���.",
						Toast.LENGTH_SHORT).show();
			}
		} else if (url.contains(URL_WEBAPI_USER_SIGNUP)) {
			try {
				JSONObject obj = response;

				String result = obj.getString("join");
				String msg = null;

				if (result.equals("true")) { // ȸ�����Կ� �����ϸ� ���� �α��� ����

					mQueue.add(new DailyHotelJsonRequest(Method.POST,
							new StringBuilder(URL_DAILYHOTEL_SERVER).append(
									URL_WEBAPI_USER_LOGIN).toString(),
							loginParams, LoginActivity.this, LoginActivity.this));
				} else {
					loginParams.clear();
					Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
				}

			} catch (Exception e) {
				if (DEBUG)
					e.printStackTrace();

				LoadingDialog.hideLoading();
				Toast.makeText(this, "��Ʈ��ũ ���°� ���� �ʽ��ϴ�.\n��Ʈ��ũ ������ �ٽ� Ȯ�����ּ���.",
						Toast.LENGTH_SHORT).show();
			}
		}
	}
}
