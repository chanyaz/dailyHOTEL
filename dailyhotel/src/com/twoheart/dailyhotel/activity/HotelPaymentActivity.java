package com.twoheart.dailyhotel.activity;

import static com.twoheart.dailyhotel.util.AppConstants.CHECKIN;
import static com.twoheart.dailyhotel.util.AppConstants.DETAIL;
import static com.twoheart.dailyhotel.util.AppConstants.LOGIN;
import static com.twoheart.dailyhotel.util.AppConstants.PREFERENCE_AUTO_LOGIN;
import static com.twoheart.dailyhotel.util.AppConstants.PREFERENCE_HOTEL_DAY;
import static com.twoheart.dailyhotel.util.AppConstants.PREFERENCE_HOTEL_IDX;
import static com.twoheart.dailyhotel.util.AppConstants.PREFERENCE_HOTEL_MONTH;
import static com.twoheart.dailyhotel.util.AppConstants.PREFERENCE_HOTEL_NAME;
import static com.twoheart.dailyhotel.util.AppConstants.PREFERENCE_HOTEL_YEAR;
import static com.twoheart.dailyhotel.util.AppConstants.PREFERENCE_IS_LOGIN;
import static com.twoheart.dailyhotel.util.AppConstants.PREFERENCE_USER_ID;
import static com.twoheart.dailyhotel.util.AppConstants.PREFERENCE_USER_PWD;
import static com.twoheart.dailyhotel.util.AppConstants.RESERVE;
import static com.twoheart.dailyhotel.util.AppConstants.REST_URL;
import static com.twoheart.dailyhotel.util.AppConstants.SAVED_MONEY;
import static com.twoheart.dailyhotel.util.AppConstants.SHARED_PREFERENCES_NAME;
import static com.twoheart.dailyhotel.util.AppConstants.USERINFO;
import static com.twoheart.dailyhotel.util.AppConstants.USER_ALIVE;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.util.network.GeneralHttpTask;
import com.twoheart.dailyhotel.util.network.OnCompleteListener;
import com.twoheart.dailyhotel.util.network.Parameter;
import com.twoheart.dailyhotel.util.ui.BaseActivity;
import com.twoheart.dailyhotel.util.ui.LoadingDialog;
import com.twoheart.dailyhotel.view.Switch;

public class HotelPaymentActivity extends BaseActivity implements
		OnClickListener, OnCheckedChangeListener {

	private static final String TAG = "HotelPaymentActivity";

	private static final int HOTEL_PAYMENT_ACTIVITY = 1;

	private TextView tv_checkin, tv_checkout, tv_original_price, tv_credit,
			tv_price;
	private Button btn_payment;
	private Switch btn_on_off;

	private TextView tvReserverName, tvReserverNumber, tvReserverEmail;
	private LinearLayout llReserverInfoLabel, llReserverInfoEditable;
	private EditText etReserverName, etReserverNumber, etReserverEmail;
	private RadioGroup rgPaymentMethod;
	private RadioButton rbPaymentAccount, rbPaymentCard;
	private TextView tvPaymentInformation;

	private Boolean isBonus = false; // ������ ���
	private Boolean isFullBonus = false; // ��� �ݾ��� ���������� �Ҷ�

	private String hotel_name;
	private String year;
	private String month;
	private String day;
	private String hotel_idx;
	private String booking_idx;

	private String email;
	private String name;
	private String phone;

	private String credit;
	private String original_price;

	private String reserverName;
	private String reserverNumber;
	private String reserverEmail;

	private boolean isPayment = false; // ���� ��ư ������ true, session listener ��������

	private int reservCnt; // ���� ���� ������ cnt �񱳸� ���� �ٽ� �ѹ� �������� ����

	private SharedPreferences prefs;

	private Tracker mGaTracker;
	private GoogleAnalytics mGaInstance;

	// Jason | Google analytics
	@Override
	public void onStart() {
		super.onStart();
		HashMap<String, String> hitParameters = new HashMap<String, String>();
		hitParameters.put(Fields.HIT_TYPE, "appview");
		hitParameters.put(Fields.SCREEN_NAME, "Payment View");

		mGaTracker.send(hitParameters);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, 0);

		hotel_idx = prefs.getString(PREFERENCE_HOTEL_IDX, null);
		hotel_name = prefs.getString(PREFERENCE_HOTEL_NAME, null);
		year = prefs.getString(PREFERENCE_HOTEL_YEAR, null);
		month = prefs.getString(PREFERENCE_HOTEL_MONTH, null);
		day = prefs.getString(PREFERENCE_HOTEL_DAY, null);

		setContentView(R.layout.activity_hotel_payment);
		setActionBar(false);
		// setTitle(hotel_name);

		loadResource();

		// Google analytics
		mGaInstance = GoogleAnalytics.getInstance(this);
		mGaTracker = mGaInstance.getTracker("UA-43721645-1");

		LoadingDialog.showLoading(this);
		new GeneralHttpTask(sessionListener, getApplicationContext())
				.execute(REST_URL + USER_ALIVE);
	}

	public void loadResource() {
		tv_checkin = (TextView) findViewById(R.id.tv_hotel_payment_checkin);
		tv_checkout = (TextView) findViewById(R.id.tv_hotel_payment_checkout);
		tv_original_price = (TextView) findViewById(R.id.tv_hotel_payment_original_price);
		tv_credit = (TextView) findViewById(R.id.tv_hotel_payment_credit);
		tv_price = (TextView) findViewById(R.id.tv_hotel_payment_price);
		btn_payment = (Button) findViewById(R.id.btn_hotel_payment);
		btn_on_off = (Switch) findViewById(R.id.btn_on_off);

		tvReserverName = (TextView) findViewById(R.id.tv_hotel_payment_reserver_name);
		tvReserverNumber = (TextView) findViewById(R.id.tv_hotel_payment_reserver_number);
		tvReserverEmail = (TextView) findViewById(R.id.tv_hotel_payment_reserver_email);

		llReserverInfoLabel = (LinearLayout) findViewById(R.id.ll_reserver_info_label);
		llReserverInfoEditable = (LinearLayout) findViewById(R.id.ll_reserver_info_editable);

		etReserverName = (EditText) findViewById(R.id.et_hotel_payment_reserver_name);
		etReserverNumber = (EditText) findViewById(R.id.et_hotel_payment_reserver_number);
		etReserverEmail = (EditText) findViewById(R.id.et_hotel_payment_reserver_email);

		rgPaymentMethod = (RadioGroup) findViewById(R.id.rg_payment_method);
		rbPaymentAccount = (RadioButton) findViewById(R.id.rb_payment_account);
		rbPaymentCard = (RadioButton) findViewById(R.id.rb_payment_card);

		tvPaymentInformation = (TextView) findViewById(R.id.tv_payment_information);

		rgPaymentMethod.setOnCheckedChangeListener(this);
		btn_payment.setOnClickListener(this);
		btn_on_off.setOnClickListener(this);
	}

	public void dialog(String str) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setPositiveButton("Ȯ��", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss(); // �ݱ�
			}
		});
		alert.setMessage(str);
		alert.show();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == btn_payment.getId()) {

			if (rgPaymentMethod.getCheckedRadioButtonId() == rbPaymentAccount
					.getId()) {
				AlertDialog.Builder alert = new AlertDialog.Builder(this);
				alert.setPositiveButton("��ȭ",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent i = new Intent(Intent.ACTION_DIAL, Uri
										.parse("tel:070-4028-9331"));
								startActivity(i);
							}
						});
				alert.setNegativeButton("���",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss(); // �ݱ�
							}
						});

				alert.setMessage("������ �Ա��� ��ȭ ��ȭ�� ���� ����˴ϴ�. �Ա� ������ ���� ����Ǹ�, ���� Ȯ�� �� ���ڰ� �����մϴ�.");
				alert.show();

			} else if (rgPaymentMethod.getCheckedRadioButtonId() == rbPaymentCard
					.getId()) {
				isPayment = true;

				// ArrayList<Parameter> paramList = new ArrayList<Parameter>();
				//
				
				if (llReserverInfoEditable.getVisibility() == View.VISIBLE) {

					email = etReserverEmail.getText().toString();
					phone = etReserverNumber.getText().toString();
					name = etReserverName.getText().toString();
					
					if (!isEmptyTextField(new String[] {email, phone, name})) {
						Toast.makeText(getApplicationContext(), "�����ڿ� ����ó, �̸����� ��� �Է����ֽʽÿ�.",  Toast.LENGTH_LONG).show();
						return ;
					}
					
				} else if (llReserverInfoLabel.getVisibility() == View.VISIBLE) {
					
					email = tvReserverEmail.getText().toString();
					phone = tvReserverNumber.getText().toString();
					name = tvReserverName.getText().toString();
					

				}

				new GeneralHttpTask(sessionListener, getApplicationContext())
						.execute(REST_URL + USER_ALIVE);
			}

			Log.v("Pay", "click payment button");

		} else if (v.getId() == btn_on_off.getId()) {
			if (isBonus) { // ���������� ����

				tv_credit.setText("��0");
				DecimalFormat comma = new DecimalFormat("###,##0");
				String str = comma.format(Integer.parseInt(original_price));
				tv_price.setText("��" + str);
				isBonus = false;
				isFullBonus = false;

			} else { // ��������� ����
				DecimalFormat comma = new DecimalFormat("###,##0");

				String price;
				String creditStr = comma.format(Integer.parseInt(credit));

				if (Integer.parseInt(original_price) <= Integer
						.parseInt(credit)) {
					creditStr = original_price;
					price = "0";
					isFullBonus = true;
				} else {
					price = Integer
							.toString((Integer.parseInt(original_price) - Integer
									.parseInt(credit)));
					isFullBonus = false;
				}

				tv_credit.setText("-��" + creditStr);
				price = comma.format(Integer.parseInt(price));
				tv_price.setText("��" + price);
				isBonus = true;

			}
		}
	}
	
	private boolean isEmptyTextField(String... value) {
		
		for (int i=0; i<value.length; i++) {
			if (value[i] == null || value[i].equals(""))
				return false;
		}
		
		return true;
		
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
	protected void onActivityResult(int p_requestCode, int p_resultCode,
			Intent p_intentActivity) {

		if (p_requestCode == HOTEL_PAYMENT_ACTIVITY) {
			if (p_resultCode == RESULT_OK) {
				String result = p_intentActivity
						.getStringExtra("ActivityResult");
				if (result.equals("SUCCESS")) {
					// dialog("������ ���������� �̷�� �����ϴ�");

					Log.d(TAG, "SUCCESS");

					AlertDialog.Builder alert = new AlertDialog.Builder(
							HotelPaymentActivity.this);
					alert.setPositiveButton("Ȯ��",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss(); // �ݱ�
									setResult(RESULT_OK);
									finish();
								}
							});
					alert.setMessage("������ ���������� �̷�� �����ϴ�");
					alert.show();
				} else if (result.equals("INVALID_SESSION")) {
					// dialog("���ǿ����� ���� �Ǿ����ϴ�. �ٽ� �õ��� �ּ���");
					Log.d(TAG, "INVALID_SESSION");
				} else if (result.equals("SOLD_OUT")) {
					dialog("��� ������ �ǸŵǾ����ϴ�.\n������ �̿����ּ���.");
					Log.d(TAG, "SOLD_OUT");
				} else if (result.equals("PAYMENT_COMPLETE")) {
					// new ReLoginTask(getApplicationContext()).execute();
					LoadingDialog.showLoading(this);
					new GeneralHttpTask(postSessionListener,
							getApplicationContext()).execute(REST_URL
							+ USER_ALIVE);
					Log.d(TAG, "PAYMENT_COMPLETE");
				} else if (result.equals("NOT_AVAILABLE")) {
					dialog("���� �� �մ��� ���� ���Դϴ�.\n��� �� �ٽ� �õ����ּ���.");
					Log.d(TAG, "NOT_AVAILABLE");
				} else if (result.equals("NETWORK_ERROR")) {
					dialog("��Ʈ��ũ ������ Ȯ���� �ּ���");
					Log.d(TAG, "NETWORK_ERROR");
				} else if (result.equals("INVALID_DATE")) {
					Log.d(TAG, "INVALID_DATE");
				}

				Log.d("result", result);
				Log.i("HotelReservation", PaymentActivity.ACTIVITY_RESULT);

			} else {
				Log.i(TAG, "��� �˼� ���� (��� �Ǵ� ���� �߻�)");
			}
		}

		super.onActivityResult(p_requestCode, p_resultCode, p_intentActivity);
	}

	public void parseUserInfoJson(String str) {
		try {
			JSONObject obj = new JSONObject(str);
			reserverName = obj.getString("name");
			reserverNumber = obj.getString("phone");
			reserverEmail = obj.getString("email");

			if ((!reserverName.equals("")) && (!reserverNumber.equals(""))
					&& (!reserverEmail.equals(""))) {
				llReserverInfoLabel.setVisibility(View.VISIBLE);
				llReserverInfoEditable.setVisibility(View.GONE);
				etReserverName.setVisibility(View.GONE);
				etReserverNumber.setVisibility(View.GONE);
				etReserverEmail.setVisibility(View.GONE);

				tvReserverName.setText(reserverName);
				tvReserverNumber.setText(reserverNumber);
				tvReserverEmail.setText(reserverEmail);

			} else {
				llReserverInfoEditable.setVisibility(View.VISIBLE);
				llReserverInfoLabel.setVisibility(View.GONE);

				if (reserverName != null)
					etReserverName.setText(reserverName);
				if (reserverNumber != null)
					etReserverNumber.setText(reserverNumber);
				if (reserverEmail != null)
					etReserverEmail.setText(reserverEmail);

			}

			LoadingDialog.hideLoading();

		} catch (Exception e) {
			Log.d(TAG, "parseUserInfoJson " + e.toString());
			LoadingDialog.hideLoading();
			Toast.makeText(getApplicationContext(),
					"��Ʈ��ũ ���°� ���� �ʽ��ϴ�.\n��Ʈ��ũ ������ �ٽ� Ȯ�����ּ���.",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void parseCreditJson(String str) {
		credit = str.trim();

		// checkin out data �޾ƿ�
		new GeneralHttpTask(detailListener, getApplicationContext())
				.execute(REST_URL + DETAIL + hotel_idx + "/" + year + "/"
						+ month + "/" + day);
	}

	public void parseLoginJson(String str) {
		try {
			JSONObject obj = new JSONObject(str);
			if (obj.getString("login").equals("true")) {
				// �α��� ����
				if (isPayment)
					new GeneralHttpTask(preCntListener, getApplicationContext())
							.execute(REST_URL + RESERVE);
				else
					// credit ��û
					new GeneralHttpTask(creditListener, getApplicationContext())
							.execute(REST_URL + SAVED_MONEY);
			} else {
				// �α��� ����
				// �޼��� ����ϰ� activity ����
				Toast.makeText(getApplicationContext(), "�α����� �ʿ��մϴ�",
						Toast.LENGTH_SHORT).show();
				SharedPreferences.Editor ed = prefs.edit();
				ed.putBoolean(PREFERENCE_AUTO_LOGIN, false);
				ed.putBoolean(PREFERENCE_IS_LOGIN, false);
				ed.commit();

				isPayment = false;
				LoadingDialog.hideLoading();
				
				finish();
			}
		} catch (Exception e) {
			Log.d(TAG, "parseLoginJson " + e.toString());
			LoadingDialog.hideLoading();
			Toast.makeText(getApplicationContext(),
					"��Ʈ��ũ ���°� ���� �ʽ��ϴ�.\n��Ʈ��ũ ������ �ٽ� Ȯ�����ּ���.",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void parseCheckinJson(String str) {
		try {
			JSONObject obj = new JSONObject(str);
			String checkin = obj.getString("checkin");
			String checkout = obj.getString("checkout");

			String in[] = checkin.split("-");
			tv_checkin.setText("20" + in[0] + ". " + in[1] + ". " + in[2] + " "
					+ in[3] + "��");
			String out[] = checkout.split("-");
			tv_checkout.setText("20" + out[0] + ". " + out[1] + ". " + out[2]
					+ " " + out[3] + "��");

			// TODO: parseUserInfoJson(); userInfoListener
			new GeneralHttpTask(userInfoListener, getApplicationContext())
					.execute(REST_URL + USERINFO);

		} catch (Exception e) {
			Log.d("parseCheckinJson", "TagDataParser" + "->" + e.toString());
			Toast.makeText(getApplicationContext(),
					"��Ʈ��ũ ���°� ���� �ʽ��ϴ�.\n��Ʈ��ũ ������ �ٽ� Ȯ�����ּ���.",
					Toast.LENGTH_SHORT).show();
			LoadingDialog.hideLoading();
		}
	}

	public void parseDetailJson(String str) {
		try {
			JSONObject obj = new JSONObject(str);
			JSONArray detailArr = obj.getJSONArray("detail");
			JSONObject detailObj = detailArr.getJSONObject(0);

			original_price = Integer.toString(detailObj.getInt("discount"));

			DecimalFormat comma = new DecimalFormat("###,##0");
			tv_price.setText("��"
					+ comma.format(Integer.parseInt(original_price)));
			tv_original_price.setText("��"
					+ comma.format(Integer.parseInt(original_price)));
			booking_idx = Integer.toString(detailObj.getInt("idx"));

			// ������ ������ button ������
			if (Integer.parseInt(credit) > 0) {
				btn_on_off.performClick();
			}

			new GeneralHttpTask(checkinListener, getApplicationContext())
					.execute(REST_URL + CHECKIN + booking_idx);

		} catch (Exception e) {
			Log.d("parseDetailJson", "TagDataParser" + "->" + e.toString());
			Toast.makeText(getApplicationContext(),
					"��Ʈ��ũ ���°� ���� �ʽ��ϴ�.\n��Ʈ��ũ ������ �ٽ� Ȯ�����ּ���.",
					Toast.LENGTH_SHORT).show();
			LoadingDialog.hideLoading();
		}
	}

	public void parsePreCntJson(String str) {
		str.trim();
		if (str.indexOf("none") >= 0) {
			reservCnt = 0;

			Intent i = new Intent(this, PaymentActivity.class);
			i.putExtra("isBonus", isBonus);
			i.putExtra("isFullBonus", isFullBonus);
			i.putExtra("credit", credit);
			i.putExtra("booking_idx", booking_idx);
			i.putExtra("email", email);
			i.putExtra("name", name);
			i.putExtra("phone", phone);
			startActivityForResult(i, 1);

		} else {
			try {
				JSONObject obj = new JSONObject(str);
				JSONArray rsvArr = obj.getJSONArray("rsv");

				reservCnt = rsvArr.length();

				Intent i = new Intent(this, PaymentActivity.class);
				i.putExtra("isBonus", isBonus);
				i.putExtra("isFullBonus", isFullBonus);
				i.putExtra("credit", credit);
				i.putExtra("booking_idx", booking_idx);
				i.putExtra("email", email);
				i.putExtra("name", name);
				i.putExtra("phone", phone);
				startActivityForResult(i, 1);

			} catch (Exception e) {
				Log.d(TAG, "parsePreCntJson " + e.toString());
				Toast.makeText(getApplicationContext(),
						"��Ʈ��ũ ���°� ���� �ʽ��ϴ�.\n��Ʈ��ũ ������ �ٽ� Ȯ�����ּ���.",
						Toast.LENGTH_SHORT).show();
			}
		}

		Log.d("reservCnt", "reservCnt = " + Integer.toString(reservCnt));
		isPayment = false;
	}

	public void parsePostLogin(String str) {
		try {
			JSONObject obj = new JSONObject(str);
			if (obj.getString("login").equals("true")) {
				// �α��� ����
				new GeneralHttpTask(postCntListener, getApplicationContext())
						.execute(REST_URL + RESERVE);
			} else {
				// �α��� ����
				// �޼��� ����ϰ� activity ����
				LoadingDialog.hideLoading();
				Toast.makeText(getApplicationContext(), "�α����� �ʿ��մϴ�",
						Toast.LENGTH_SHORT).show();
				SharedPreferences.Editor ed = prefs.edit();
				ed.putBoolean(PREFERENCE_AUTO_LOGIN, false);
				ed.putBoolean(PREFERENCE_IS_LOGIN, false);
				ed.commit();
			}
		} catch (Exception e) {
			Log.d(TAG, "parseLoginJson " + e.toString());
			LoadingDialog.hideLoading();
			Toast.makeText(getApplicationContext(),
					"��Ʈ��ũ ���°� ���� �ʽ��ϴ�.\n��Ʈ��ũ ������ �ٽ� Ȯ�����ּ���.",
					Toast.LENGTH_SHORT).show();
		}
	}

	public boolean compareReservCnt(String str) {
		str.trim();

		int resultCnt = 0;

		if (str.indexOf("none") >= 0) {
			resultCnt = 0;

		} else {
			try {
				JSONObject obj = new JSONObject(str);
				JSONArray rsvArr = obj.getJSONArray("rsv");
				resultCnt = rsvArr.length();

			} catch (Exception e) {
				Log.d(TAG, "parsePostJson " + e.toString());
				Toast.makeText(getApplicationContext(),
						"��Ʈ��ũ ���°� ���� �ʽ��ϴ�.\n��Ʈ��ũ ������ �ٽ� Ȯ�����ּ���.",
						Toast.LENGTH_SHORT).show();
			}
		}

		Log.d("compare reservCnt", "resultCnt = " + Integer.toString(resultCnt)
				+ " reservCnt = " + Integer.toString(reservCnt));

		if (resultCnt > reservCnt)
			return true;
		else
			return false;
	}

	protected OnCompleteListener userInfoListener = new OnCompleteListener() {

		@Override
		public void onTaskFailed() {
			Log.d(TAG, "creditListener onTAskFailed");
			LoadingDialog.hideLoading();
			Toast.makeText(getApplicationContext(),
					"��Ʈ��ũ ���°� ���� �ʽ��ϴ�.\n��Ʈ��ũ ������ �ٽ� Ȯ�����ּ���.",
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onTaskComplete(String result) {
			parseUserInfoJson(result);
		}
	};

	protected OnCompleteListener sessionListener = new OnCompleteListener() {

		@Override
		public void onTaskFailed() {
			Log.d(TAG, "sessionListener onTAskFailed");
			LoadingDialog.hideLoading();
			Toast.makeText(getApplicationContext(),
					"��Ʈ��ũ ���°� ���� �ʽ��ϴ�.\n��Ʈ��ũ ������ �ٽ� Ȯ�����ּ���.",
					Toast.LENGTH_SHORT).show();
			finish();
		}

		@Override
		public void onTaskComplete(String result) {
			result = result.trim();
			if (result.equals("alive")) { // session alive

				if (isPayment)
					new GeneralHttpTask(preCntListener, getApplicationContext())
							.execute(REST_URL + RESERVE);
				else
					// credit ��û
					new GeneralHttpTask(creditListener, getApplicationContext())
							.execute(REST_URL + SAVED_MONEY);

			} else if (result.equals("dead")) { // session dead
				// ��α���
				
				// parameter setting
				ArrayList<Parameter> paramList = new ArrayList<Parameter>();
				paramList.add(new Parameter("email", prefs.getString(
						PREFERENCE_USER_ID, "")));
				paramList.add(new Parameter("pw", prefs.getString(
						PREFERENCE_USER_PWD, "")));

				// �α��� ��û
				new GeneralHttpTask(loginListener, paramList,
						getApplicationContext()).execute(REST_URL + LOGIN);

			} else {
				LoadingDialog.hideLoading();
				Toast.makeText(getApplicationContext(),
						"��Ʈ��ũ ���°� ���� �ʽ��ϴ�.\n��Ʈ��ũ ������ �ٽ� Ȯ�����ּ���.",
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	protected OnCompleteListener loginListener = new OnCompleteListener() {

		@Override
		public void onTaskFailed() {
			Log.d(TAG, "loginListener onTAskFailed");
			LoadingDialog.hideLoading();
			Toast.makeText(getApplicationContext(),
					"��Ʈ��ũ ���°� ���� �ʽ��ϴ�.\n��Ʈ��ũ ������ �ٽ� Ȯ�����ּ���.",
					Toast.LENGTH_SHORT).show();
			finish();
		}

		@Override
		public void onTaskComplete(String result) {
			parseLoginJson(result);
		}
	};

	protected OnCompleteListener creditListener = new OnCompleteListener() {

		@Override
		public void onTaskFailed() {
			Log.d(TAG, "creditListener onTAskFailed");
			LoadingDialog.hideLoading();
			Toast.makeText(getApplicationContext(),
					"��Ʈ��ũ ���°� ���� �ʽ��ϴ�.\n��Ʈ��ũ ������ �ٽ� Ȯ�����ּ���.",
					Toast.LENGTH_SHORT).show();
			finish();
		}

		@Override
		public void onTaskComplete(String result) {
			parseCreditJson(result);
		}
	};

	protected OnCompleteListener checkinListener = new OnCompleteListener() {

		@Override
		public void onTaskFailed() {
			Log.d(TAG, "creditListener onTAskFailed");
			LoadingDialog.hideLoading();
			Toast.makeText(getApplicationContext(),
					"��Ʈ��ũ ���°� ���� �ʽ��ϴ�.\n��Ʈ��ũ ������ �ٽ� Ȯ�����ּ���.",
					Toast.LENGTH_SHORT).show();
			finish();
		}

		@Override
		public void onTaskComplete(String result) {
			parseCheckinJson(result);
		}
	};

	protected OnCompleteListener detailListener = new OnCompleteListener() {

		@Override
		public void onTaskFailed() {
			Log.d(TAG, "creditListener onTAskFailed");
			LoadingDialog.hideLoading();
			Toast.makeText(getApplicationContext(),
					"��Ʈ��ũ ���°� ���� �ʽ��ϴ�.\n��Ʈ��ũ ������ �ٽ� Ȯ�����ּ���.",
					Toast.LENGTH_SHORT).show();
			finish();
		}

		@Override
		public void onTaskComplete(String result) {
			parseDetailJson(result);
		}
	};

	protected OnCompleteListener preCntListener = new OnCompleteListener() {

		@Override
		public void onTaskFailed() {
			Log.d(TAG, "preCntListener onTAskFailed");
			LoadingDialog.hideLoading();
			Toast.makeText(getApplicationContext(),
					"��Ʈ��ũ ���°� ���� �ʽ��ϴ�.\n��Ʈ��ũ ������ �ٽ� Ȯ�����ּ���.",
					Toast.LENGTH_SHORT).show();
			finish();
		}

		@Override
		public void onTaskComplete(String result) {
			parsePreCntJson(result);
		}
	};

	protected OnCompleteListener postCntListener = new OnCompleteListener() {

		@Override
		public void onTaskFailed() {
			Log.d(TAG, "postCntListener onTAskFailed");
			LoadingDialog.hideLoading();
			Toast.makeText(getApplicationContext(),
					"��Ʈ��ũ ���°� ���� �ʽ��ϴ�.\n��Ʈ��ũ ������ �ٽ� Ȯ�����ּ���.",
					Toast.LENGTH_SHORT).show();
			finish();
		}

		@Override
		public void onTaskComplete(String result) {

			LoadingDialog.hideLoading();

			if (compareReservCnt(result)) {
				AlertDialog.Builder alert = new AlertDialog.Builder(
						HotelPaymentActivity.this);
				alert.setPositiveButton("Ȯ��",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss(); // �ݱ�
								setResult(RESULT_OK);
								finish();
							}
						});
				alert.setMessage("������ ���������� �̷�� �����ϴ�");
				alert.show();
			} else {
				dialog("���� ���� : �����ڿ��� �����ϼ���");
			}
		}
	};

	protected OnCompleteListener postSessionListener = new OnCompleteListener() {

		@Override
		public void onTaskFailed() {
			Log.d(TAG, "postSessionListener onTAskFailed");
			LoadingDialog.hideLoading();
			Toast.makeText(getApplicationContext(),
					"��Ʈ��ũ ���°� ���� �ʽ��ϴ�.\n��Ʈ��ũ ������ �ٽ� Ȯ�����ּ���.",
					Toast.LENGTH_SHORT).show();
			finish();
		}

		@Override
		public void onTaskComplete(String result) {
			result = result.trim();
			if (result.equals("alive")) { // session alive
				new GeneralHttpTask(postCntListener, getApplicationContext())
						.execute(REST_URL + RESERVE);

			} else if (result.equals("dead")) { // session dead
				// ��α���

				// parameter setting
				ArrayList<Parameter> paramList = new ArrayList<Parameter>();
				paramList.add(new Parameter("email", prefs.getString(
						PREFERENCE_USER_ID, "")));
				paramList.add(new Parameter("pw", prefs.getString(
						PREFERENCE_USER_PWD, "")));

				// �α��� ��û
				new GeneralHttpTask(postLoginListener, paramList,
						getApplicationContext()).execute(REST_URL + LOGIN);

			} else {
				LoadingDialog.hideLoading();
				Toast.makeText(getApplicationContext(),
						"��Ʈ��ũ ���°� ���� �ʽ��ϴ�.\n��Ʈ��ũ ������ �ٽ� Ȯ�����ּ���.",
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	protected OnCompleteListener postLoginListener = new OnCompleteListener() {

		@Override
		public void onTaskFailed() {
			Log.d(TAG, "postLoginListener onTAskFailed");
			LoadingDialog.hideLoading();
			Toast.makeText(getApplicationContext(),
					"��Ʈ��ũ ���°� ���� �ʽ��ϴ�.\n��Ʈ��ũ ������ �ٽ� Ȯ�����ּ���.",
					Toast.LENGTH_SHORT).show();
			finish();
		}

		@Override
		public void onTaskComplete(String result) {
			parsePostLogin(result);
		}
	};

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (group.getId() == rgPaymentMethod.getId()) {

			if (checkedId == rbPaymentAccount.getId()) {
				tvPaymentInformation
						.setText("��������: 206037-04-005094 | �������� | (��)���ϸ�");

			} else if (checkedId == rbPaymentCard.getId()) {
				tvPaymentInformation.setText("���� ���� Ư�� �� ��� �� ȯ���� �Ұ��մϴ�.");

			}

		}

	}
}
