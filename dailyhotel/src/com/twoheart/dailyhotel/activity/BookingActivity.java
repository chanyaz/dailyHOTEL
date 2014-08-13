package com.twoheart.dailyhotel.activity;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.twoheart.dailyhotel.DailyHotel;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Credit;
import com.twoheart.dailyhotel.model.Customer;
import com.twoheart.dailyhotel.model.HotelDetail;
import com.twoheart.dailyhotel.model.Pay;
import com.twoheart.dailyhotel.model.SaleTime;
import com.twoheart.dailyhotel.util.GaManager;
import com.twoheart.dailyhotel.util.GlobalFont;
import com.twoheart.dailyhotel.util.Log;
import com.twoheart.dailyhotel.util.network.VolleyHttpClient;
import com.twoheart.dailyhotel.util.network.request.DailyHotelJsonRequest;
import com.twoheart.dailyhotel.util.network.request.DailyHotelStringRequest;
import com.twoheart.dailyhotel.util.network.response.DailyHotelJsonResponseListener;
import com.twoheart.dailyhotel.util.network.response.DailyHotelStringResponseListener;
import com.twoheart.dailyhotel.util.ui.BaseActivity;
import com.twoheart.dailyhotel.widget.Switch;

@SuppressLint({ "NewApi", "ResourceAsColor" })
public class BookingActivity extends BaseActivity implements
DailyHotelStringResponseListener, DailyHotelJsonResponseListener, OnClickListener, OnCheckedChangeListener,
android.widget.CompoundButton.OnCheckedChangeListener {

	private static final String TAG = "HotelPaymentActivity";
	private static final int DIALOG_CONFIRM_PAYMENT_CARD = 0;
	private static final int DIALOG_CONFIRM_PAYMENT_ACCOUNT = 1;

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

	private SaleTime saleTime;

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

		saleTime = new SaleTime();

	}

	@Override
	protected void onResume() {
		super.onResume();
		// ������ ����ġ �ʱ�ȭ
		swCredit.setChecked(false);

		lockUI();
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

		if (applyCredit) mPay.setPayPrice(originalPrice - credit);
		else mPay.setPayPrice(originalPrice);

		tvPrice.setText("��" + comma.format(mPay.getPayPrice()));

	}

	public void dialog(String str) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("�����˸�");
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
	public void onClick(final View v) {
		if (v.getId() == btnPay.getId()) {
			
			v.setClickable(false);
			v.setEnabled(false);
			
			Dialog dialog = null; 
			
			if (rgPaymentMethod.getCheckedRadioButtonId() == rbPaymentAccount
					.getId()) { // ������ �Ա��� �������� ���
				
				dialog = getPaymentConfirmDialog(DIALOG_CONFIRM_PAYMENT_ACCOUNT);
			} else if (rgPaymentMethod.getCheckedRadioButtonId() == rbPaymentCard
					.getId()) { // �ſ�ī�带 �������� ���
				
				dialog = getPaymentConfirmDialog(DIALOG_CONFIRM_PAYMENT_CARD);
			}
			
			dialog.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					v.setClickable(true);
					v.setEnabled(true);
				}
			});
			dialog.show();
			
		} else if (v.getId() == rbPaymentAccount.getId() | v.getId() == rbPaymentCard.getId()) {
			svBooking.fullScroll(View.FOCUS_DOWN);

		}
	}
	/**
	 * ���� ���ܿ� �˸��� ���� ���� Ȯ�� ���̾�α׸� �����.
	 * @param type CARD, ACCOUNT �ΰ��� Ÿ�� ����.
	 * @return Ÿ�Կ� �´� ���� ���� ���̾�α� ��ȯ.
	 */

	private Dialog getPaymentConfirmDialog(int type) {
		final Dialog dialog = new Dialog(this);
		
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		dialog.setCanceledOnTouchOutside(false);
		
		View view = LayoutInflater.from(this).inflate(R.layout.fragment_dialog_confirm_payment, null);
		
		TextView tvMsg = (TextView) view.findViewById(R.id.tv_confirm_payment_msg);
		Button btnProceed = (Button) view.findViewById(R.id.btn_confirm_payment_proceed);
		ImageView btnClose = (ImageView) view.findViewById(R.id.btn_confirm_payment_close);
		
		OnClickListener onClickProceed = null;
		
		if (type == DIALOG_CONFIRM_PAYMENT_CARD) {
			
			tvMsg.setText(
					Html.fromHtml(getString(R.string.dialog_msg_payment_confirm_card))
					);
			btnProceed.setText(
					Html.fromHtml(getString(R.string.dialog_btn_payment_confirm_card))
					);
			
			onClickProceed = new OnClickListener() {
				@Override
				public void onClick(View v) {
					
					lockUI();
					mQueue.add(new DailyHotelStringRequest(Method.GET, new StringBuilder(
							URL_DAILYHOTEL_SERVER).append(URL_WEBAPI_APP_TIME)
							.toString(), null, BookingActivity.this,
							BookingActivity.this));
					dialog.dismiss();
				}
			};
		} else if (type == DIALOG_CONFIRM_PAYMENT_ACCOUNT) {
			
			tvMsg.setText(
					Html.fromHtml(getString(R.string.dialog_msg_payment_confirm_account))
					);
			btnProceed.setText(
					Html.fromHtml(getString(R.string.dialog_btn_payment_confirm_account))
					);
			
			onClickProceed = new OnClickListener() {
				@Override
				public void onClick(View v) {
					lockUI();
					mQueue.add(new DailyHotelStringRequest(Method.GET, new StringBuilder(
							URL_DAILYHOTEL_SERVER).append(URL_WEBAPI_APP_TIME)
							.toString(), null, BookingActivity.this,
							BookingActivity.this));
					dialog.dismiss();
				}
			};
		}
		
		btnClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		btnProceed.setOnClickListener(onClickProceed);
		
 		dialog.setContentView(view);
		GlobalFont.apply((ViewGroup) view);
		
		return dialog;
	}

	private boolean isEmptyTextField(String... fieldText) {

		for (int i = 0; i < fieldText.length; i++) {
			if (fieldText[i] == null || fieldText[i].equals("") || fieldText[i].equals("null")) return true;
		}

		return false;

	}

	private void moveToPayStep() {
		Intent intent = new Intent(this, PaymentActivity.class);
		intent.putExtra(NAME_INTENT_EXTRA_DATA_PAY, mPay);

		startActivityForResult(intent,
				CODE_REQUEST_ACTIVITY_PAYMENT);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);

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

			if (accessToken != null) loginParams.put("accessToken", accessToken);
			else loginParams.put("email", id);

			loginParams.put("pw", pw);

			mQueue.add(new DailyHotelJsonRequest(Method.POST,
					new StringBuilder(URL_DAILYHOTEL_SERVER).append(
							URL_WEBAPI_USER_LOGIN).toString(),
							loginParams, this, this));
		} else {
			unLockUI();
			showToast(getString(R.string.toast_msg_retry_login), Toast.LENGTH_LONG, false);

			startActivityForResult(new Intent(this, LoginActivity.class),
					CODE_REQUEST_ACTIVITY_LOGIN);
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);
		}

	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.slide_out_left, R.anim.slide_out_right);

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
				if (intent != null) {
					if (intent.getParcelableExtra(NAME_INTENT_EXTRA_DATA_PAY) != null) {
						Pay payData = intent.getParcelableExtra(NAME_INTENT_EXTRA_DATA_PAY);

						Editor editor = sharedPreference.edit();
						editor.putString(KEY_PREFERENCE_HOTEL_NAME, payData.getHotelDetail().getHotel().getName());
						editor.putInt(KEY_PREFERENCE_HOTEL_SALE_IDX, payData.getHotelDetail().getSaleIdx());
						editor.putString(KEY_PREFERENCE_HOTEL_CHECKOUT, payData.getCheckOut());
						editor.putString(KEY_PREFERENCE_USER_IDX, payData.getCustomer().getUserIdx());
						editor.commit();
					}
				}

				AlertDialog.Builder alert = new AlertDialog.Builder(
						BookingActivity.this);
				alert.setTitle("�����˸�");
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
				alert.setMessage("������ ���������� �̷�������ϴ�");
				alert.show();

				GaManager.getInstance(getApplicationContext()).
				purchaseComplete(
						Integer.toString(mPay.getHotelDetail().getSaleIdx()), 
						mPay.getHotelDetail().getHotel().getName(), 
						mPay.getHotelDetail().getHotel().getCategory(), 
						(double) mPay.getPayPrice()
						);

				break;
			case CODE_RESULT_ACTIVITY_PAYMENT_SOLD_OUT:
				dialog("��� ������ �ǸŵǾ����ϴ�.\n������ �̿����ּ���.");
				break;
			case CODE_RESULT_ACTIVITY_PAYMENT_NOT_AVAILABLE:
				dialog("�ٸ� �մ��� ���� ���Դϴ�.\n��� �� �̿����ּ���.");
				break;
			case CODE_RESULT_ACTIVITY_PAYMENT_NETWORK_ERROR:
				dialog("��Ʈ��ũ ������ �߻��߽��ϴ�.\n��Ʈ��ũ ������ Ȯ�����ּ���.");
				break;
			case CODE_RESULT_ACTIVITY_PAYMENT_INVALID_SESSION:
				VolleyHttpClient.createCookie();	// ��Ű�� �ٽ� ���� �õ�
				break;
			case CODE_RESULT_ACTIVITY_PAYMENT_INVALID_DATE:
				dialog("�ǸŰ� �����Ǿ����ϴ�.");
				break;
			case CODE_RESULT_ACTIVITY_PAYMENT_FAIL:
				dialog("�� �� ���� ������ �߻��߽��ϴ�.\n�������ֽñ� �ٶ��ϴ�.");
				break;
			case CODE_RESULT_ACTIVITY_PAYMENT_CANCELED:
				dialog("������ ��ҵǾ����ϴ�.");
				break;
			case CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_READY:
				// ���� Ȯ�� ����Ʈ �����׸�Ʈ���� �ѹ� �� ���� ���� �÷��� ����
				Editor editor = sharedPreference.edit();
				editor.putInt(KEY_PREFERENCE_ACCOUNT_READY_FLAG, CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_READY);
				editor.apply();
				
				setResult(CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_READY);
				finish();
				break;
			case CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_TIME_ERROR:
				dialog("�Աݴ�� �ð��� �ʰ��Ǿ����ϴ�\n�ٽ� �õ����ּ���.");
				break;
			case CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_DUPLICATE:
				dialog("�̹� �Աݴ�� ���� ȣ���Դϴ�.");
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
				mPay.setPayType("VBANK");
//				btnPay.setText("��ȭ�� �����ϱ�");
//				tvPaymentInformation
//				.setText("��������: 206037-04-005094 | �������� | (��)���ϸ�");

			} else if (checkedId == rbPaymentCard.getId()) {
				mPay.setPayType(null);
//				btnPay.setText("�����ϱ�");
//				tvPaymentInformation.setText("���� ���� Ư�� �� ��� �� ȯ���� �Ұ��մϴ�.");
			}
			btnPay.setText("�����ϱ�");
			tvPaymentInformation.setText("���� ���� Ư�� �� ��� �� ȯ���� �Ұ��մϴ�.");


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
	public void onResponse(String url, JSONObject response) {
		if (url.contains(URL_WEBAPI_USER_INFO)) {
			try {
				JSONObject obj = response;

				Customer buyer = new Customer();
				buyer.setEmail(obj.getString("email"));
				buyer.setName(obj.getString("name"));
				buyer.setPhone(obj.getString("phone"));
				buyer.setAccessToken(obj.getString("accessToken"));
				buyer.setUserIdx(obj.getString("idx"));

				mPay.setCustomer(buyer);
				buyer = mPay.getCustomer();

				if (!isEmptyTextField(new String[] {
						buyer.getEmail(),
						buyer.getPhone(),
						buyer.getName() })) {
					llReserverInfoLabel.setVisibility(View.VISIBLE);
					llReserverInfoEditable.setVisibility(View.GONE);
					etReserverName.setVisibility(View.GONE);
					etReserverNumber.setVisibility(View.GONE);
					etReserverEmail.setVisibility(View.GONE);

					tvReserverName.setText(buyer.getName());
					tvReserverNumber.setText(buyer.getPhone());
					tvReserverEmail.setText(buyer.getEmail());

				} else {
					llReserverInfoEditable.setVisibility(View.VISIBLE);
					llReserverInfoLabel.setVisibility(View.GONE);

					if (!isEmptyTextField(buyer.getName())) etReserverName.setText(buyer.getName());

					if (!isEmptyTextField(buyer.getPhone())) {
						etReserverNumber.setText(buyer.getPhone());
					} else {
						TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext()
								.getSystemService(Context.TELEPHONY_SERVICE);

						etReserverNumber.setText(telephonyManager
								.getLine1Number());
					}

					if (!isEmptyTextField(buyer.getEmail())) etReserverEmail.setText(buyer.getEmail());

				}

				// üũ�� ���� ��û
				mQueue.add(new DailyHotelJsonRequest(Method.GET,
						new StringBuilder(URL_DAILYHOTEL_SERVER)
				.append(URL_WEBAPI_RESERVE_CHECKIN)
				.append(mPay.getHotelDetail().getSaleIdx())
				.toString(), null, this, this));

			} catch (Exception e) {
				onError(e);
			}
		} else if (url.contains(URL_WEBAPI_RESERVE_CHECKIN)) {
			try {
				JSONObject obj = response;
				String checkin = obj.getString("checkin");
				String checkout = obj.getString("checkout");
				mPay.setCheckOut(checkout);

				String in[] = checkin.split("-");
				tvCheckIn.setText("20" + in[0] + "�� " + in[1] + "�� " + in[2] + "�� " + in[3] + "��");
				
				String out[] = checkout.split("-");
				tvCheckOut.setText("20" + out[0] + "�� " + out[1] + "�� " + out[2] + "�� " + out[3] + "��");

				unLockUI();
			} catch (Exception e) {
				onError(e);
			}
		} else if (url.contains(URL_WEBAPI_USER_LOGIN)) { // INVALID_SESSION ������
			// ��� ��α��� �� �ٽýõ��Ѵ�
			try {
				if (response.getBoolean("login")) {
					unLockUI();
					VolleyHttpClient.createCookie();
					moveToPayStep();

				} else {
					// ���� �� ��õ�
					moveToLoginProcess();

				}
			} catch (JSONException e) {
				onError(e);
			}
		} else if (url.contains(URL_WEBAPI_APP_SALE_TIME)) {
			// �� �ð��� ���� �ð����� ũ�� Ŭ���� �ð����� ������� ���� �������� �̵�
			try {
				String open = response.getString("open");
				String close = response.getString("close");

				saleTime.setOpenTime(open);
				saleTime.setCloseTime(close);

				unLockUI();

				if( saleTime.isSaleTime() ) {

					Customer buyer = mPay.getCustomer();

					if (llReserverInfoEditable.getVisibility() == View.VISIBLE) {

						buyer.setEmail(etReserverEmail.getText().toString());
						buyer.setPhone(etReserverNumber.getText().toString());
						buyer.setName(etReserverName.getText().toString());

						if (isEmptyTextField(new String[] {
								buyer.getEmail(),
								buyer.getPhone(),
								buyer.getName() })) {

							showToast(getString(R.string.toast_msg_please_input_booking_user_infos), Toast.LENGTH_LONG, true);

							return;
						}

					} else if (llReserverInfoLabel.getVisibility() == View.VISIBLE) {

						buyer.setEmail(tvReserverEmail.getText().toString());
						buyer.setPhone(tvReserverNumber.getText().toString());
						buyer.setName(tvReserverName.getText().toString());

					}

					mPay.setCustomer(buyer);
					moveToPayStep();

				} else {
					Builder builder = new AlertDialog.Builder(
							BookingActivity.this);

					builder.setTitle("�˸�");
					builder
					.setMessage(getString(R.string.dialog_msg_sales_closed));
					builder.setCancelable(false);
					builder.setPositiveButton("Ȯ��",
							new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							setResult(RESULT_SALES_CLOSED);
							finish();
						}
					});

					AlertDialog alertDlg = builder.create();
					alertDlg.show();
				}

			} catch (JSONException e) {
				onError(e);
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
				String str = comma.format(Integer.parseInt(mPay.getCredit().getBonus()));
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
				onError(e);
			}
		} else if(url.contains(URL_WEBAPI_APP_TIME)) {
			saleTime.setCurrentTime(response);

			mQueue.add(new DailyHotelJsonRequest(Method.GET, new StringBuilder(
					URL_DAILYHOTEL_SERVER).append(URL_WEBAPI_APP_SALE_TIME)
					.toString(), null, BookingActivity.this,
					this));
		}

	}

	@Override
	protected void onStart() {
		super.onStart();

		DailyHotel.getGaTracker().send(MapBuilder.createAppView().build());
	}
}
