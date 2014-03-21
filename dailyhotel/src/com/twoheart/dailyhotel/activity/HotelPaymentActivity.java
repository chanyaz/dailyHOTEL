package com.twoheart.dailyhotel.activity;

import java.text.DecimalFormat;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.obj.Credit;
import com.twoheart.dailyhotel.obj.Customer;
import com.twoheart.dailyhotel.obj.HotelDetail;
import com.twoheart.dailyhotel.obj.Pay;
import com.twoheart.dailyhotel.util.Log;
import com.twoheart.dailyhotel.util.network.VolleyHttpClient;
import com.twoheart.dailyhotel.util.network.request.DailyHotelJsonRequest;
import com.twoheart.dailyhotel.util.network.request.DailyHotelRequest;
import com.twoheart.dailyhotel.util.network.response.DailyHotelJsonResponseListener;
import com.twoheart.dailyhotel.util.network.response.DailyHotelResponseListener;
import com.twoheart.dailyhotel.util.ui.BaseActivity;
import com.twoheart.dailyhotel.util.ui.LoadingDialog;

public class HotelPaymentActivity extends BaseActivity implements
		DailyHotelResponseListener, DailyHotelJsonResponseListener,
		ErrorListener, OnClickListener, OnCheckedChangeListener,
		android.widget.CompoundButton.OnCheckedChangeListener {

	private static final String TAG = "HotelPaymentActivity";

	private RequestQueue mQueue;

	private TextView tvCheckIn, tvCheckOut, tvOriginalPrice, tvCredit, tvPrice;
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
		setContentView(R.layout.activity_hotel_payment);

		mPay = new Pay();
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			mPay.setHotelDetail((HotelDetail) bundle
					.getParcelable(NAME_INTENT_EXTRA_DATA_HOTELDETAIL));
		}

		mQueue = VolleyHttpClient.getRequestQueue();
		setActionBar(mPay.getHotelDetail().getHotel().getName());

		tvCheckIn = (TextView) findViewById(R.id.tv_hotel_payment_checkin);
		tvCheckOut = (TextView) findViewById(R.id.tv_hotel_payment_checkout);
		tvOriginalPrice = (TextView) findViewById(R.id.tv_hotel_payment_original_price);
		tvCredit = (TextView) findViewById(R.id.tv_hotel_payment_credit);
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

		rgPaymentMethod.setOnCheckedChangeListener(this);
		btnPay.setOnClickListener(this);
		swCredit.setOnCheckedChangeListener(this);

		rbPaymentCard.setChecked(true);
		
		LoadingDialog.showLoading(this);

		mQueue.add(new DailyHotelRequest(Method.GET,
				new StringBuilder(URL_DAILYHOTEL_SERVER).append(
						URL_WEBAPI_USER_ALIVE).toString(), null, this, this));
	}
	
	private void updatePayPrice(boolean applyCredit) {
		
		int originalPrice = Integer.parseInt(mPay.getHotelDetail().getHotel().getDiscount().replaceAll(",", ""));
		int credit = Integer.parseInt(mPay.getCredit().getBonus());
		
		DecimalFormat comma = new DecimalFormat("###,##0");
		tvOriginalPrice.setText("��"
				+ comma.format(originalPrice));
		
		if (applyCredit) {
			mPay.setPayPrice(originalPrice - credit);
			
		} else {
			mPay.setPayPrice(originalPrice);
			
		}
		
		tvPrice.setText("��"
				+ comma.format(mPay.getPayPrice()));
		
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
					.getId()) {	// ������ �Ա��� �������� ���
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
					.getId()) {	//�ſ�ī�带 �������� ���

				if (llReserverInfoEditable.getVisibility() == View.VISIBLE) {

					mPay.getCustomer().setEmail(etReserverEmail.getText().toString());
					mPay.getCustomer().setPhone(etReserverNumber.getText().toString());
					mPay.getCustomer().setName(etReserverName.getText().toString());

					if (!isEmptyTextField(new String[] { mPay.getCustomer().getEmail(), 
							mPay.getCustomer().getPhone(), mPay.getCustomer().getName() })) {
						Toast.makeText(getApplicationContext(),
								"�����ڿ� ����ó, �̸����� ��� �Է����ֽʽÿ�.", Toast.LENGTH_LONG)
								.show();
						return;
					}

				} else if (llReserverInfoLabel.getVisibility() == View.VISIBLE) {

					mPay.getCustomer().setEmail(tvReserverEmail.getText().toString());
					mPay.getCustomer().setPhone(tvReserverNumber.getText().toString());
					mPay.getCustomer().setName(tvReserverName.getText().toString());

				}
				
				Intent intent = new Intent(this, PaymentActivity.class);
				intent.putExtra(NAME_INTENT_EXTRA_DATA_PAY, mPay);
				startActivityForResult(intent, CODE_REQUEST_ACTIVITY_PAYMENT);

			}
		}
	}

	private boolean isEmptyTextField(String... value) {

		for (int i = 0; i < value.length; i++) {
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
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		if (requestCode == CODE_REQUEST_ACTIVITY_PAYMENT) {
			Log.d(TAG, Integer.toString(resultCode));
			
			switch (resultCode) {
			case CODE_RESULT_ACTIVITY_PAYMENT_SUCCESS :
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
				
				break;
			case CODE_RESULT_ACTIVITY_PAYMENT_INVALID_SESSION :
				break;
			case CODE_RESULT_ACTIVITY_PAYMENT_SOLD_OUT :
				dialog("��� ������ �ǸŵǾ����ϴ�.\n������ �̿����ּ���.");
				
				break;
			case CODE_RESULT_ACTIVITY_PAYMENT_COMPLETE :
//				LoadingDialog.showLoading(this);
				
				break;
			case CODE_RESULT_ACTIVITY_PAYMENT_INVALID_DATE :
				break;
				
			case CODE_RESULT_ACTIVITY_PAYMENT_NOT_AVAILABLE :
				dialog("���� �� �մ��� ���� ���Դϴ�.\n��� �� �ٽ� �õ����ּ���.");
				break;
				
			case CODE_RESULT_ACTIVITY_PAYMENT_NETWORK_ERROR :
				dialog("��Ʈ��ũ ������ �߻��߽��ϴ�. ��Ʈ��ũ ������ Ȯ�����ּ���.");
				break;
				
			case CODE_RESULT_ACTIVITY_PAYMENT_FAIL :
				dialog("�� �� ���� ������ �߻��߽��ϴ�. �������ֽñ� �ٶ��ϴ�.");
				break;
			
			}
		} else if (requestCode == CODE_REQUEST_ACTIVITY_LOGIN) {
			if (resultCode != RESULT_OK)
				finish();				// �α��ε��� �ʾҴٸ� ����ϱ� ���� ��Ƽ��Ƽ ����.
			else
				mQueue.add(new DailyHotelRequest(Method.GET,
						new StringBuilder(URL_DAILYHOTEL_SERVER).append(
								URL_WEBAPI_USER_ALIVE).toString(), null, this, this));
		}

		
	}

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

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getId() == swCredit.getId()) {
			if (!isChecked) { // ���������� ����
				swCredit.setThumbResource(R.drawable.switch_thumb_holo_light);
				swCredit.setTextColor(android.R.color.white);

			} else { // ��������� ����
				swCredit.setThumbResource(R.drawable.switch_thumb_activated_holo_light);
				swCredit.setTextColor(android.R.color.white);

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
		LoadingDialog.hideLoading();

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
				
				if ((!mPay.getCustomer().getEmail().equals("")) && (!mPay.getCustomer().getName().equals(""))
						&& (!mPay.getCustomer().getPhone().equals(""))) {
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

					if (mPay.getCustomer().getName() != null)
						etReserverName.setText(mPay.getCustomer().getName());
					if (mPay.getCustomer().getPhone() != null)
						etReserverNumber.setText(mPay.getCustomer().getPhone());
					if (mPay.getCustomer().getEmail() != null)
						etReserverEmail.setText(mPay.getCustomer().getEmail());

				}

				// üũ�� ���� ��û
				mQueue.add(new DailyHotelJsonRequest(Method.GET, new StringBuilder(
						URL_DAILYHOTEL_SERVER)
						.append(URL_WEBAPI_RESERVE_CHECKIN).append(mPay.getHotelDetail().getSaleIdx()).toString(), null,
						this, this));

			} catch (Exception e) {
				if (DEBUG)
					e.printStackTrace();

				LoadingDialog.hideLoading();
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

			} catch (Exception e) {
				if (DEBUG)
					e.printStackTrace();
				
				Toast.makeText(this,
						"��Ʈ��ũ ���°� ���� �ʽ��ϴ�.\n��Ʈ��ũ ������ �ٽ� Ȯ�����ּ���.",
						Toast.LENGTH_SHORT).show();
			} finally {
				LoadingDialog.hideLoading();
			}
		}
	}

	@Override
	public void onResponse(String url, String response) {
		if (url.contains(URL_WEBAPI_USER_ALIVE)) {
			String result = response.trim();
			if (result.equals("alive")) { // session alive
				// credit ��û
				mQueue.add(new DailyHotelRequest(Method.GET, new StringBuilder(
						URL_DAILYHOTEL_SERVER).append(
						URL_WEBAPI_RESERVE_SAVED_MONEY).toString(), null, this,
						this));

			} else if (result.equals("dead")) { // session dead
				LoadingDialog.hideLoading();
				startActivityForResult(new Intent(this, LoginActivity.class), CODE_REQUEST_ACTIVITY_LOGIN);

			} else {
				LoadingDialog.hideLoading();
				Toast.makeText(this, "��Ʈ��ũ ���°� ���� �ʽ��ϴ�.\n��Ʈ��ũ ������ �ٽ� Ȯ�����ּ���.",
						Toast.LENGTH_SHORT).show();
			}

		} else if (url.contains(URL_WEBAPI_RESERVE_SAVED_MONEY)) {
			try {

				DecimalFormat comma = new DecimalFormat("###,##0");
				String str = comma.format(Integer.parseInt(response.trim()));
				mPay.setCredit(new Credit(null, str, null));
				
				tvCredit.setText(new StringBuilder(mPay.getCredit().getBonus()).append("��"));
				
				swCredit.performClick();
				// �������� ���ٸ� �� �� �� ���� �̺�Ʈ�� �ҷ� switch�� ����
				if (Integer.parseInt(mPay.getCredit().getBonus()) == 0) {
					swCredit.performClick();
				}
				
				// ����� ���� ��û.
				mQueue.add(new DailyHotelJsonRequest(Method.GET,
						new StringBuilder(URL_DAILYHOTEL_SERVER).append(
								URL_WEBAPI_USER_INFO).toString(), null, this,
						this));

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
