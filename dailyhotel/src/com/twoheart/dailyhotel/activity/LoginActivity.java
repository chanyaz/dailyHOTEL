package com.twoheart.dailyhotel.activity;

import java.util.Arrays;
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
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
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
		facebookLogin.setVisibility(View.GONE);

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

	private Session.StatusCallback statusCallback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);

			if (exception != null)
				exception.printStackTrace();
		}
	};

	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		if (state.isOpened()) {
			Log.d(TAG, "Facebook Login");
			session.requestNewReadPermissions(new Session.NewPermissionsRequest(
					LoginActivity.this, Arrays.asList("email")));

			makeMeRequest(session);

		} else if (state.isClosed()) {
			Log.d(TAG, "Facebook Logout");
		}
	}

	private void makeMeRequest(final Session session) {

		Request request = Request.newMeRequest(session,
				new Request.GraphUserCallback() {

					@Override
					public void onCompleted(GraphUser user, Response response) {
						if (session == Session.getActiveSession()) {
							if (user != null) {

								TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext()
										.getSystemService(
												Context.TELEPHONY_SERVICE);
								String phoneNumber = telephonyManager
										.getLine1Number();

								Map<String, String> loginParams = new HashMap<String, String>();
								loginParams.put("email",
										user.getProperty("email").toString());
								loginParams.put("pw", null);
								loginParams.put("name", user.getName());
								loginParams.put("phone", phoneNumber);
								loginParams.put("device",
										telephonyManager.getDeviceId());
								loginParams.put("accessToken", user.getId());

								etId.setText(user.getId());
								// TODO: �н������ �����κ��� ���Ƿ� ������ ���� �޵��� �Ѵ�.

								LoadingDialog.showLoading(LoginActivity.this);

								mQueue.add(new DailyHotelJsonRequest(
										Method.POST, new StringBuilder(
												URL_DAILYHOTEL_SERVER).append(
												URL_WEBAPI_USER_LOGIN_FACEBOOK)
												.toString(), loginParams,
										LoginActivity.this, LoginActivity.this));
							}
						}

						Session.getActiveSession()
								.closeAndClearTokenInformation();
						Session.setActiveSession(null);
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
			startActivityForResult(i, CODE_REQUEST_ACTIVITY_LOGIN);
			overridePendingTransition(R.anim.slide_in_right, R.anim.hold);

		} else if (v.getId() == btnLogin.getId()) { // �α���
			if (!isBlankFields())
				return;

			String md5 = Crypto.encrypt(etPwd.getText().toString()).replace(
					"\n", "");

			Map<String, String> loginParams = new LinkedHashMap<String, String>();
			loginParams.put("email", etId.getText().toString());
			loginParams.put("pw", md5);
			
			LoadingDialog.showLoading(this);

			mQueue.add(new DailyHotelJsonRequest(Method.POST,
					new StringBuilder(URL_DAILYHOTEL_SERVER).append(
							URL_WEBAPI_USER_LOGIN).toString(), loginParams,
					this, this));

		} else if (v.getId() == facebookLogin.getId()) {

			Session.openActiveSession(this, true, statusCallback);
		}
	}

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
			String id = etId.getText().toString();
			String pwd = Crypto.encrypt(etPwd.getText().toString()).replace(
					"\n", "");

			SharedPreferences.Editor ed = sharedPreference.edit();
			ed.putBoolean(KEY_PREFERENCE_AUTO_LOGIN, true);
			ed.putString(KEY_PREFERENCE_USER_ID, id);
			ed.putString(KEY_PREFERENCE_USER_PWD, pwd);
			ed.commit();
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == CODE_REQUEST_ACTIVITY_LOGIN) {
			if (resultCode == RESULT_OK) {
				setResult(RESULT_OK);
				finish();
			}
		} else {
			Session.getActiveSession().onActivityResult(this, requestCode,
					resultCode, data);
		}

	}

	@Override
	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.hold, R.anim.slide_out_right);
		super.onBackPressed();
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

				if (obj.getString("login").equals("true")) {

					if (obj.length() > 1)
						etPwd.setText(obj.getString("msg"));

					Log.d(TAG, "�α��� ����");
					Toast.makeText(getApplicationContext(), "�α��εǾ����ϴ�",
							Toast.LENGTH_SHORT).show();
					LoadingDialog.hideLoading();
					storeLoginInfo();

					setResult(RESULT_OK);
					finish();

				} else {
					// �α��� ����
					// ���� msg ���
					if (obj.length() > 1) {
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
					LoadingDialog.hideLoading();
				}

			} catch (Exception e) {
				e.printStackTrace();

				LoadingDialog.hideLoading();
				Toast.makeText(this, "��Ʈ��ũ ���°� ���� �ʽ��ϴ�.\n��Ʈ��ũ ������ �ٽ� Ȯ�����ּ���.",
						Toast.LENGTH_SHORT).show();
			}
		}
	}
}
