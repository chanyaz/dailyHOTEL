package com.twoheart.dailyhotel.activity;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.twoheart.dailyhotel.DailyHotel;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Credit;
import com.twoheart.dailyhotel.model.Customer;
import com.twoheart.dailyhotel.model.HotelDetail;
import com.twoheart.dailyhotel.model.Pay;
import com.twoheart.dailyhotel.util.Log;
import com.twoheart.dailyhotel.util.network.VolleyHttpClient;
import com.twoheart.dailyhotel.util.network.request.DailyHotelJsonRequest;
import com.twoheart.dailyhotel.util.network.request.DailyHotelStringRequest;
import com.twoheart.dailyhotel.util.network.response.DailyHotelJsonResponseListener;
import com.twoheart.dailyhotel.util.network.response.DailyHotelStringResponseListener;
import com.twoheart.dailyhotel.util.ui.BaseActivity;
import com.twoheart.dailyhotel.util.ui.LoadingDialog;
import com.twoheart.dailyhotel.widget.Switch;

@SuppressLint({ "NewApi", "ResourceAsColor" })
public class BookingActivity extends BaseActivity implements
		DailyHotelStringResponseListener, DailyHotelJsonResponseListener,
		ErrorListener, OnClickListener, OnCheckedChangeListener,
		android.widget.CompoundButton.OnCheckedChangeListener {

	private static final String TAG = "HotelPaymentActivity";
	private RequestQueue mQueue;

	private ScrollView svBooking;
	private TextView tvCheckIn, tvCheckOut, tvOriginalPriceValue,
			tvCreditValue, tvOriginalPrice, tvCredit, tvPrice;
	private Button btnPay;
	private Switch swCredit;
	private TextView tvReserverName, tvReserverNumber, tvReserverEmail;
	private LinearLayout llReserverInfoLabel, llReserverInfoEditable;
	private EditText etReserverName, etReserverNumber, etReserverEmail;
	private RadioGroup rgPaymentMethod;
	private RadioButton rbPaymentAccount, rbPaymentCard;
	private TextView tvPaymentInformation;

	private Pay mPay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_booking);
		DailyHotel.getGaTracker().set(Fields.SCREEN_NAME, TAG);

		mPay = new Pay();
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			mPay.setHotelDetail((HotelDetail) bundle
					.getParcelable(NAME_INTENT_EXTRA_DATA_HOTELDETAIL));
		}

		mQueue = VolleyHttpClient.getRequestQueue();
		setActionBar(mPay.getHotelDetail().getHotel().getName());

		svBooking = (ScrollView) findViewById(R.id.sv_booking);
		
		tvCheckIn = (TextView) findViewById(R.id.tv_hotel_payment_checkin);
		tvCheckOut = (TextView) findViewById(R.id.tv_hotel_payment_checkout);
		tvOriginalPrice = (TextView) findViewById(R.id.tv_hotel_payment_original_price);
		tvCredit = (TextView) findViewById(R.id.tv_hotel_payment_credit);
		tvOriginalPriceValue = (TextView) findViewById(R.id.tv_hotel_payment_original_price_value);
		tvCreditValue = (TextView) findViewById(R.id.tv_hotel_payment_credit_value);
		tvPrice = (TextView) findViewById(R.id.tv_hotel_payment_price);
		btnPay = (Button) findViewById(R.id.btn_hotel_payment);
		swCredit = (Switch) findViewById(R.id.btn_on_off);

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
		
		rbPaymentAccount.setOnClickListener(this);
		rbPaymentCard.setOnClickListener(this);

		rgPaymentMethod.setOnCheckedChangeListener(this);
		btnPay.setOnClickListener(this);
		swCredit.setOnCheckedChangeListener(this);

		rbPaymentCard.setChecked(true);

	}

	@Override
	protected void onResume() {
		super.onResume();
		// ������ ����ġ �ʱ�ȭ
		swCredit.setChecked(false);
		
		LoadingDialog.showLoading(this);

		// credit ��û
		mQueue.add(new DailyHotelStringRequest(Method.GET, new StringBuilder(
				URL_DAILYHOTEL_SERVER).append(URL_WEBAPI_RESERVE_SAVED_MONEY)
				.toString(), null, this, this));
	}

	private void updatePayPrice(boolean applyCredit) {

		int originalPrice = Integer.parseInt(mPay.getHotelDetail().getHotel()
				.getDiscount().replaceAll(",", ""));
		int credit = Integer.parseInt(mPay.getCredit().getBonus()
				.replaceAll(",", ""));

		DecimalFormat comma = new DecimalFormat("###,##0");
		tvOriginalPriceValue.setText("��" + comma.format(originalPrice));

		if (applyCredit) {
			mPay.setPayPrice(originalPrice - credit);

		} else {
			mPay.setPayPrice(originalPrice);

		}

		tvPrice.setText("��" + comma.format(mPay.getPayPrice()));

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
		if (v.getId() == btnPay.getId()) {

			if (rgPaymentMethod.getCheckedRadioButtonId() == rbPaymentAccount
					.getId()) { // ������ �Ա��� �������� ���

				AlertDialog.Builder alert = new AlertDialog.Builder(this);
				alert.setPositiveButton("��ȭ",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent i = new Intent(
										Intent.ACTION_DIAL,
										Uri.parse(new StringBuilder("tel:")
												.append(PHONE_NUMBER_DAILYHOTEL)
												.toString()));
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
					.getId()) { // �ſ�ī�带 �������� ���

				if (llReserverInfoEditable.getVisibility() == View.VISIBLE) {

					mPay.getCustomer().setEmail(
							etReserverEmail.getText().toString());
					mPay.getCustomer().setPhone(
							etReserverNumber.getText().toString());
					mPay.getCustomer().setName(
							etReserverName.getText().toString());

					if (isEmptyTextField(new String[] {
							mPay.getCustomer().getEmail(),
							mPay.getCustomer().getPhone(),
							mPay.getCustomer().getName() })) {
						Toast.makeText(this, "�����ڿ� ����ó, �̸����� ��� �Է����ֽʽÿ�.",
								Toast.LENGTH_LONG).show();
						return;
					}

				} else if (llReserverInfoLabel.getVisibility() == View.VISIBLE) {

					mPay.getCustomer().setEmail(
							tvReserverEmail.getText().toString());
					mPay.getCustomer().setPhone(
							tvReserverNumber.getText().toString());
					mPay.getCustomer().setName(
							tvReserverName.getText().toString());
					
				}

				moveToPayStep();

			}
		} else if (v.getId() == rbPaymentAccount.getId() | v.getId() == rbPaymentCard.getId()) {
			svBooking.fullScroll(View.FOCUS_DOWN);
			
		}
	}

	private boolean isEmptyTextField(String... fieldText) {

		for (int i = 0; i < fieldText.length; i++) {
			if (fieldText[i] == null || fieldText[i].equals("")
					|| fieldText[i].equals("null"))
				return true;
		}

		return false;

	}
	
	private void moveToPayStep() {
		Intent intent = new Intent(this, PaymentActivity.class);
		intent.putExtra(NAME_INTENT_EXTRA_DATA_PAY, mPay);
		startActivityForResult(intent,
				CODE_REQUEST_ACTIVITY_PAYMENT);
		overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
		
	}
	
	private void moveToLoginProcess() {
		if (sharedPreference.getBoolean(KEY_PREFERENCE_AUTO_LOGIN,
				false)) {

			String id = sharedPreference.getString(
					KEY_PREFERENCE_USER_ID, null);
			String accessToken = sharedPreference.getString(
					KEY_PREFERENCE_USER_ACCESS_TOKEN, null);
			String pw = sharedPreference.getString(
					KEY_PREFERENCE_USER_PWD, null);

			Map<String, String> loginParams = new HashMap<String, String>();

			if (accessToken != null) {
				loginParams.put("accessToken", accessToken);
			} else {
				loginParams.put("email", id);
			}

			loginParams.put("pw", pw);

			mQueue.add(new DailyHotelJsonRequest(Method.POST,
					new StringBuilder(URL_DAILYHOTEL_SERVER).append(
							URL_WEBAPI_USER_LOGIN).toString(),
					loginParams, this, this));
		} else {
			LoadingDialog.hideLoading();
			Toast.makeText(this, "�ٽ� �α������ּ���", Toast.LENGTH_SHORT)
					.show();
			
			startActivityForResult(new Intent(this, LoginActivity.class),
					CODE_REQUEST_ACTIVITY_LOGIN);
			overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
		}
		
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.hold, R.anim.slide_out_right);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		if (requestCode == CODE_REQUEST_ACTIVITY_PAYMENT) {
			Log.d(TAG, Integer.toString(resultCode));

			switch (resultCode) {
			case CODE_RESULT_ACTIVITY_PAYMENT_COMPLETE:
			case CODE_RESULT_ACTIVITY_PAYMENT_SUCCESS:
				AlertDialog.Builder alert = new AlertDialog.Builder(
						BookingActivity.this);
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

				break;
			case CODE_RESULT_ACTIVITY_PAYMENT_SOLD_OUT:
				dialog("��� ������ �ǸŵǾ����ϴ�.\n������ �̿����ּ���.");
				break;
			case CODE_RESULT_ACTIVITY_PAYMENT_NOT_AVAILABLE:
				dialog("���� �� �մ��� ���� ���Դϴ�.\n��� �� �ٽ� �õ����ּ���.");
				break;
			case CODE_RESULT_ACTIVITY_PAYMENT_NETWORK_ERROR:
				dialog("��Ʈ��ũ ������ �߻��߽��ϴ�. ��Ʈ��ũ ������ Ȯ�����ּ���.");
				break;
			case CODE_RESULT_ACTIVITY_PAYMENT_INVALID_SESSION:
//				LoadingDialog.showLoading(this);
//				
//				// ��Ű ���Ḧ ���� ������ �α׾ƿ� ������Ʈ
//				mQueue.add(new DailyHotelJsonRequest(
//						Method.GET,
//						new StringBuilder(
//								URL_DAILYHOTEL_SERVER)
//								.append(URL_WEBAPI_USER_LOGOUT)
//								.toString(), null,
//						this,
//						this));
				break;
			case CODE_RESULT_ACTIVITY_PAYMENT_INVALID_DATE:
			case CODE_RESULT_ACTIVITY_PAYMENT_FAIL:
				dialog("�� �� ���� ������ �߻��߽��ϴ�. �������ֽñ� �ٶ��ϴ�.");
				break;
			}
		} else if (requestCode == CODE_REQUEST_ACTIVITY_LOGIN) {
			if (resultCode == RESULT_OK)
				moveToPayStep();	
			
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (group.getId() == rgPaymentMethod.getId()) {

			if (checkedId == rbPaymentAccount.getId()) {
				btnPay.setText("��ȭ�� �����ϱ�");
				tvPaymentInformation
						.setText("��������: 206037-04-005094 | �������� | (��)���ϸ�");

			} else if (checkedId == rbPaymentCard.getId()) {
				btnPay.setText("�����ϱ�");
				tvPaymentInformation.setText("���� ���� Ư�� �� ��� �� ȯ���� �Ұ��մϴ�.");

			}

		}

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getId() == swCredit.getId()) {
			if (!isChecked) { // ���������� ����
				tvOriginalPrice.setEnabled(false);
				tvCredit.setEnabled(false);
				tvOriginalPriceValue.setEnabled(false);
				tvCreditValue.setEnabled(false);

			} else { // ��������� ����
				tvOriginalPrice.setEnabled(true);
				tvCredit.setEnabled(true);
				tvOriginalPriceValue.setEnabled(true);
				tvCreditValue.setEnabled(true);

			}

			mPay.setSaleCredit(isChecked);
			updatePayPrice(isChecked);
		}
	}

	@Override
	public void onErrorResponse(VolleyError error) {
		if (DEBUG)
			error.printStackTrace();
		
		Toast.makeText(this, "��Ʈ��ũ ���°� ���� �ʽ��ϴ�.\n��Ʈ��ũ ������ �ٽ� Ȯ�����ּ���.",
				Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onResponse(String url, JSONObject response) {
		if (url.contains(URL_WEBAPI_USER_INFO)) {
			try {
				JSONObject obj = response;

				mPay.setCustomer(new Customer());
				mPay.getCustomer().setEmail(obj.getString("email"));
				mPay.getCustomer().setName(obj.getString("name"));
				mPay.getCustomer().setPhone(obj.getString("phone"));
				mPay.getCustomer().setAccessToken(obj.getString("accessToken"));

				if (!isEmptyTextField(new String[] {
						mPay.getCustomer().getEmail(),
						mPay.getCustomer().getPhone(),
						mPay.getCustomer().getName() })) {
					llReserverInfoLabel.setVisibility(View.VISIBLE);
					llReserverInfoEditable.setVisibility(View.GONE);
					etReserverName.setVisibility(View.GONE);
					etReserverNumber.setVisibility(View.GONE);
					etReserverEmail.setVisibility(View.GONE);

					tvReserverName.setText(mPay.getCustomer().getName());
					tvReserverNumber.setText(mPay.getCustomer().getPhone());
					tvReserverEmail.setText(mPay.getCustomer().getEmail());

				} else {
					llReserverInfoEditable.setVisibility(View.VISIBLE);
					llReserverInfoLabel.setVisibility(View.GONE);

					if (!isEmptyTextField(mPay.getCustomer().getName()))
						etReserverName.setText(mPay.getCustomer().getName());

					if (!isEmptyTextField(mPay.getCustomer().getPhone()))
						etReserverNumber.setText(mPay.getCustomer().getPhone());
					else {
						TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext()
								.getSystemService(Context.TELEPHONY_SERVICE);

						etReserverNumber.setText(telephonyManager
								.getLine1Number());
					}

					if (!isEmptyTextField(mPay.getCustomer().getEmail()))
						etReserverEmail.setText(mPay.getCustomer().getEmail());

				}

				// üũ�� ���� ��û
				mQueue.add(new DailyHotelJsonRequest(Method.GET,
						new StringBuilder(URL_DAILYHOTEL_SERVER)
								.append(URL_WEBAPI_RESERVE_CHECKIN)
								.append(mPay.getHotelDetail().getSaleIdx())
								.toString(), null, this, this));

			} catch (Exception e) {
				if (DEBUG)
					e.printStackTrace();

				Toast.makeText(this, "��Ʈ��ũ ���°� ���� �ʽ��ϴ�.\n��Ʈ��ũ ������ �ٽ� Ȯ�����ּ���.",
						Toast.LENGTH_SHORT).show();
			}
		} else if (url.contains(URL_WEBAPI_RESERVE_CHECKIN)) {
			try {
				JSONObject obj = response;
				String checkin = obj.getString("checkin");
				String checkout = obj.getString("checkout");

				String in[] = checkin.split("-");
				tvCheckIn.setText("20" + in[0] + "�� " + in[1] + "�� " + in[2]
						+ "�� " + in[3] + "��");
				String out[] = checkout.split("-");
				tvCheckOut.setText("20" + out[0] + "�� " + out[1] + "�� "
						+ out[2] + "�� " + out[3] + "��");

				LoadingDialog.hideLoading();

			} catch (Exception e) {
				if (DEBUG)
					e.printStackTrace();

				Toast.makeText(this, "��Ʈ��ũ ���°� ���� �ʽ��ϴ�.\n��Ʈ��ũ ������ �ٽ� Ȯ�����ּ���.",
						Toast.LENGTH_SHORT).show();
			}
		} else if (url.contains(URL_WEBAPI_USER_LOGIN)) { // INVALID_SESSION ������
															// ��� ��α��� �� �ٽýõ��Ѵ�
			try {
				if (response.getBoolean("login")) {
					LoadingDialog.hideLoading();
					VolleyHttpClient.createCookie();
					moveToPayStep();

				} else {
					// ���� �� ��õ�
					moveToLoginProcess();
					
				}
			} catch (JSONException e) {
				if (DEBUG)
					e.printStackTrace();
			}

		} else if (url.contains(URL_WEBAPI_USER_LOGOUT)) {
			try {
				if (response.getBoolean("msg")) {
					VolleyHttpClient.destroyCookie();
					moveToLoginProcess();

				} else {
					// ���� �� ��õ�
					mQueue.add(new DailyHotelJsonRequest(
							Method.GET,
							new StringBuilder(
									URL_DAILYHOTEL_SERVER)
									.append(URL_WEBAPI_USER_LOGOUT)
									.toString(), null,
							this,
							this));
					
				}
			} catch (JSONException e) {
				if (DEBUG)
					e.printStackTrace();
			}
			
		}
	}

	@Override
	public void onResponse(String url, String response) {
		if (url.contains(URL_WEBAPI_RESERVE_SAVED_MONEY)) {
			try {
				String bonus = response.trim().replaceAll(",", "");
				mPay.setCredit(new Credit(null, bonus, null));

				DecimalFormat comma = new DecimalFormat("###,##0");
				String str = comma.format(Integer.parseInt(mPay.getCredit()
						.getBonus()));
				tvCreditValue.setText(new StringBuilder(str).append("��"));

				swCredit.toggle();
				// �������� ���ٸ� �� �� �� ���� �̺�Ʈ�� �ҷ� switch�� ����
				if (Integer.parseInt(mPay.getCredit().getBonus()) == 0) {
					swCredit.toggle();
				}

				// ����� ���� ��û.
				mQueue.add(new DailyHotelJsonRequest(Method.GET,
						new StringBuilder(URL_DAILYHOTEL_SERVER).append(
								URL_WEBAPI_USER_INFO).toString(), null, this,
						this));

			} catch (Exception e) {
				if (DEBUG)
					e.printStackTrace();

				Toast.makeText(this, "��Ʈ��ũ ���°� ���� �ʽ��ϴ�.\n��Ʈ��ũ ������ �ٽ� Ȯ�����ּ���.",
						Toast.LENGTH_SHORT).show();
			}
		}

	}

	@Override
	protected void onStart() {
		super.onStart();

		DailyHotel.getGaTracker().send(MapBuilder.createAppView().build());
	}
}
