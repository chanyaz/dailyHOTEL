/**
 * Copyright (c) 2014 Daily Co., Ltd. All rights reserved.
 *
 * BookingActivity (���� ȭ��)
 * 
 * ���� ȭ������ �Ѿ�� �� ���� ������ �����ְ� ��������� ������ �� �ִ� ȭ�� 
 * 
 */
package com.twoheart.dailyhotel.activity;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import kr.co.kcp.android.payment.standard.ResultRcvActivity;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Credit;
import com.twoheart.dailyhotel.model.Customer;
import com.twoheart.dailyhotel.model.HotelDetail;
import com.twoheart.dailyhotel.model.Pay;
import com.twoheart.dailyhotel.model.SaleTime;
import com.twoheart.dailyhotel.util.GlobalFont;
import com.twoheart.dailyhotel.util.Log;
import com.twoheart.dailyhotel.util.RenewalGaManager;
import com.twoheart.dailyhotel.util.SimpleAlertDialog;
import com.twoheart.dailyhotel.util.network.VolleyHttpClient;
import com.twoheart.dailyhotel.util.network.request.DailyHotelJsonRequest;
import com.twoheart.dailyhotel.util.network.request.DailyHotelStringRequest;
import com.twoheart.dailyhotel.util.network.response.DailyHotelJsonResponseListener;
import com.twoheart.dailyhotel.util.network.response.DailyHotelStringResponseListener;
import com.twoheart.dailyhotel.util.ui.BaseActivity;
import com.twoheart.dailyhotel.widget.Switch;

/**
 * 
 * @author jangjunho
 *
 */
@SuppressLint({ "NewApi", "ResourceAsColor" })
public class BookingActivity extends BaseActivity implements
DailyHotelStringResponseListener, DailyHotelJsonResponseListener, OnClickListener, OnCheckedChangeListener,
android.widget.CompoundButton.OnCheckedChangeListener {

	private static final String TAG = "HotelPaymentActivity";
	private static final int DIALOG_CONFIRM_PAYMENT_CARD = 0;
	private static final int DIALOG_CONFIRM_PAYMENT_HP = 1;
	private static final int DIALOG_CONFIRM_PAYMENT_ACCOUNT = 2;
	private static final int DIALOG_CONFIRM_PAYMENT_NO_RSERVE = 3;

	private ScrollView svBooking;
	private TextView tvCheckIn, tvCheckOut, tvOriginalPriceValue,
	tvCreditValue, tvOriginalPrice, tvCredit, tvPrice;
	private Button btnPay;
	private Switch swCredit;
	private TextView tvReserverName, tvReserverNumber, tvReserverEmail;
	private LinearLayout llReserverInfoLabel, llReserverInfoEditable;
	private EditText etReserverName, etReserverNumber, etReserverEmail;
	private RadioGroup rgPaymentMethod;
	private RadioButton rbPaymentAccount, rbPaymentCard, rbPaymentHp;
	
	private Pay mPay;

	private SaleTime saleTime;
	private int mReqCode;
	private int mResCode;
	private Intent mResIntent;
	protected String mAliveCallSource;
	
	private String locale;
	private int mHotelIdx;
	
	private MixpanelAPI mMixpanel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_booking);
		
		mMixpanel = MixpanelAPI.getInstance(this, "791b366dadafcd37803f6cd7d8358373"); // ��� ��� ���

		mPay = new Pay();
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			mPay.setHotelDetail((HotelDetail) bundle
					.getParcelable(NAME_INTENT_EXTRA_DATA_HOTELDETAIL));
			mHotelIdx = bundle.getInt(NAME_INTENT_EXTRA_DATA_HOTELIDX);
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
		rbPaymentHp = (RadioButton) findViewById(R.id.rb_payment_hp);

		rbPaymentAccount.setOnClickListener(this);
		rbPaymentCard.setOnClickListener(this);
		rbPaymentHp.setOnClickListener(this);

		rgPaymentMethod.setOnCheckedChangeListener(this);
		btnPay.setOnClickListener(this);
		swCredit.setOnCheckedChangeListener(this);

		rbPaymentCard.setChecked(true);

		saleTime = new SaleTime();
		locale = sharedPreference.getString(KEY_PREFERENCE_LOCALE, null);
	
		// ������ �κ� �⺻ ��ȭ ǥ��.
		tvCreditValue.setText(Html.fromHtml(getString(R.string.currency)) + "0");
		
		// �ѱ�, ���� ���� ���� ����.
		if(locale.equals("�ѱ���")) {
			
			rgPaymentMethod.setVisibility(View.VISIBLE);
		}
		else {
			
			rgPaymentMethod.setVisibility(View.GONE);
			mPay.setPayType("PAYPAL");
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// ������ ����ġ �ʱ�ȭ
//		swCredit.setChecked(false);

		lockUI();
		// credit ��û
		mQueue.add(new DailyHotelStringRequest(Method.GET, new StringBuilder(
				URL_DAILYHOTEL_SERVER).append(URL_WEBAPI_RESERVE_SAVED_MONEY)
				.toString(), null, this, this));
		
		String region = sharedPreference.getString(KEY_PREFERENCE_REGION_SELECT_GA, null);
		String hotelName = sharedPreference.getString(KEY_PREFERENCE_HOTEL_NAME_GA, null);
		
		RenewalGaManager.getInstance(getApplicationContext()).recordScreen("bookingDetail", "/todays-hotels/" + region + "/" + hotelName + "/booking-detail");
	}

	private void updatePayPrice(boolean applyCredit) {

		int originalPrice = Integer.parseInt(mPay.getHotelDetail().getHotel()
				.getDiscount().replaceAll(",", ""));
		int credit = Integer.parseInt(mPay.getCredit().getBonus()
				.replaceAll(",", ""));

		DecimalFormat comma = new DecimalFormat("###,##0");
		
		if (locale.equals("�ѱ���"))	tvOriginalPriceValue.setText(comma.format(originalPrice)+Html.fromHtml(getString(R.string.currency)));
		else	tvOriginalPriceValue.setText(Html.fromHtml(getString(R.string.currency))+comma.format(originalPrice));

		if (applyCredit) {
			int payPrice = originalPrice - credit;
			payPrice = payPrice < 0 ? 0: payPrice;
			mPay.setPayPrice(payPrice);
			mPay.setOriginalPrice(originalPrice);
			
			if (credit >= originalPrice) credit = originalPrice;
			if (locale.equals("�ѱ���"))	tvCreditValue.setText("-"+comma.format(credit)+Html.fromHtml(getString(R.string.currency)));
			else	tvCreditValue.setText(Html.fromHtml(getString(R.string.currency)) + "-" +comma.format(credit));

		}
		else {
			if (locale.equals("�ѱ���"))	tvCreditValue.setText("0"+Html.fromHtml(getString(R.string.currency)));
			else	tvCreditValue.setText(Html.fromHtml(getString(R.string.currency))+"0"); 
			
			mPay.setPayPrice(originalPrice);
//			mPay.setOriginalPrice(originalPrice);
		}

		if (locale.equals("�ѱ���"))	tvPrice.setText(comma.format(mPay.getPayPrice())+Html.fromHtml(getString(R.string.currency)));
		else	tvPrice.setText(Html.fromHtml(getString(R.string.currency))+comma.format(mPay.getPayPrice()));

	}

	@Override
	public void onClick(final View v) {
		if (v.getId() == btnPay.getId()) {

			if (llReserverInfoEditable.getVisibility() == View.VISIBLE) {
				Customer buyer = new Customer();

				buyer.setEmail(etReserverEmail.getText().toString());
				buyer.setPhone(etReserverNumber.getText().toString());
				buyer.setName(etReserverName.getText().toString());

				if (isEmptyTextField(new String[] {
						buyer.getEmail(),
						buyer.getPhone(),
						buyer.getName() })) {
					
					android.util.Log.e("BUYER",buyer.getEmail()+" / "+buyer.getPhone()+" / "+buyer.getName());
					showToast(getString(R.string.toast_msg_please_input_booking_user_infos), Toast.LENGTH_LONG, false);
				} else { //
					Map<String, String> updateParams =new HashMap<String, String>();
					if (etReserverEmail.isFocusable())
						updateParams.put("user_email", buyer.getEmail());
					if (etReserverName.isFocusable())
						updateParams.put("user_name", buyer.getName());
					if (etReserverNumber.isFocusable())
						updateParams.put("user_phone", buyer.getPhone());

					android.util.Log.e("FACEBOOK UPDATE", updateParams.toString());

					lockUI();
					mQueue.add(new DailyHotelJsonRequest(Method.POST,
							new StringBuilder(URL_DAILYHOTEL_SERVER).append(
									URL_WEBAPI_USER_UPDATE_FACEBOOK).toString(),
									updateParams, this, this));
				}

			} //ȣ�� ������ ���� ������ �̺�Ʈ ȣ�ڿ����� ������ ����� ���ϰ� ����. 
			else if (mPay.isSaleCredit() && (mPay.getOriginalPrice() < 10000) &&
					Integer.parseInt(mPay.getCredit().getBonus().replaceAll(",", "")) != 0) {
				getPaymentConfirmDialog(DIALOG_CONFIRM_PAYMENT_NO_RSERVE).show();
				
			} else {
				Dialog dialog = null;
				
				if (rgPaymentMethod.getCheckedRadioButtonId() == rbPaymentCard
						.getId()) { // �ſ�ī�带 �������� ���

					dialog = getPaymentConfirmDialog(DIALOG_CONFIRM_PAYMENT_CARD);
					RenewalGaManager.getInstance(getApplicationContext()).recordEvent("radio", "choosePaymentWay", "�ſ�ī��", (long) 1);
				} else if (rgPaymentMethod.getCheckedRadioButtonId() == rbPaymentHp
						.getId()) { // �ڵ����� �������� ���

					dialog = getPaymentConfirmDialog(DIALOG_CONFIRM_PAYMENT_HP);
					RenewalGaManager.getInstance(getApplicationContext()).recordEvent("radio", "choosePaymentWay", "�޴���", (long) 2);
				} else if (rgPaymentMethod.getCheckedRadioButtonId() == rbPaymentAccount
						.getId()) { // ������� �Ա��� �������� ���

					dialog = getPaymentConfirmDialog(DIALOG_CONFIRM_PAYMENT_ACCOUNT);
					RenewalGaManager.getInstance(getApplicationContext()).recordEvent("radio", "choosePaymentWay", "������ü", (long) 3);
				}

				dialog.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						v.setClickable(true);
						v.setEnabled(true);
					}
				});
				
				dialog.show();
				
				v.setClickable(false);
				v.setEnabled(false);
				
				String region = sharedPreference.getString(KEY_PREFERENCE_REGION_SELECT_GA, null);
				String hotelName = sharedPreference.getString(KEY_PREFERENCE_HOTEL_NAME_GA, null);
				
				RenewalGaManager.getInstance(getApplicationContext()).recordScreen("paymentAgreement", "/todays-hotels/" + region + "/" + hotelName + "/booking-detail/payment-agreement");
				RenewalGaManager.getInstance(getApplicationContext()).recordEvent("click", "requestPayment", mPay.getHotelDetail().getHotel().getName(), (long) mHotelIdx);
			}


		} else if (v.getId() == rbPaymentAccount.getId() | v.getId() == rbPaymentCard.getId()) {
			svBooking.fullScroll(View.FOCUS_DOWN);

		}
	}
	/**
	 * ���� ���ܿ� �˸��� ���� ���� Ȯ�� ���̾�α׸� �����.
	 * @param type CARD, ACCOUNT, HP  ������ Ÿ�� ����.
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

		String msg = "";
		if (type == DIALOG_CONFIRM_PAYMENT_HP) msg = getString(R.string.dialog_msg_payment_confirm_hp);
		else if (type == DIALOG_CONFIRM_PAYMENT_NO_RSERVE) {
			msg = getString(R.string.dialog_btn_payment_no_reserve);
			btnProceed.setVisibility(View.GONE);
		}
		else msg = getString(R.string.dialog_msg_payment_confirm);
		
		tvMsg.setText(Html.fromHtml(msg));
		btnProceed.setText(Html.fromHtml(getString(R.string.dialog_btn_payment_confirm)));

		onClickProceed = new OnClickListener() {
			@Override
			public void onClick(View v) {
				lockUI();
				mAliveCallSource = "PAYMENT"; 
				mQueue.add(new DailyHotelStringRequest(Method.GET,
						new StringBuilder(URL_DAILYHOTEL_SERVER).append(
								URL_WEBAPI_USER_ALIVE).toString(), null,
								BookingActivity.this, BookingActivity.this));
				dialog.dismiss();
				RenewalGaManager.getInstance(getApplicationContext()).recordEvent("click", "agreePayment", mPay.getHotelDetail().getHotel().getName(), (long) mHotelIdx);
			}
		};

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

	// ���� ȭ������ �̵� 
	private void moveToPayStep() {

		android.util.Log.e("Sale credit / Pay Price ",mPay.isSaleCredit()+" / "+mPay.getPayPrice());

		Intent intent = new Intent(this, com.twoheart.dailyhotel.activity.PaymentActivity.class);
		intent.putExtra(NAME_INTENT_EXTRA_DATA_PAY, mPay);
		
		startActivityForResult(intent, CODE_REQUEST_ACTIVITY_PAYMENT);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);
	}

	private void moveToLoginProcess() {
		if (sharedPreference.getBoolean(KEY_PREFERENCE_AUTO_LOGIN, false)) {

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
		unLockUI();

		mReqCode = requestCode;
		mResCode = resultCode;
		mResIntent = intent;

		mAliveCallSource = "ACTIVITY_RESULT";
		lockUI();
		mQueue.add(new DailyHotelStringRequest(Method.GET,
				new StringBuilder(URL_DAILYHOTEL_SERVER).append(
						URL_WEBAPI_USER_ALIVE).toString(), null,
						BookingActivity.this, BookingActivity.this));

	}

	private void activityResulted(int requestCode, int resultCode, Intent intent) {
		//������ ���� �� ȣ���. 
		if (requestCode == CODE_REQUEST_ACTIVITY_PAYMENT) {
			Log.d(TAG, Integer.toString(resultCode));

			String title = getString(R.string.dialog_title_payment);
			String msg = "";
			String posTitle = getString(R.string.dialog_btn_text_confirm);
			android.content.DialogInterface.OnClickListener posListener = null;
			
			String region = sharedPreference.getString(KEY_PREFERENCE_REGION_SELECT_GA, null);
			String hotelName = sharedPreference.getString(KEY_PREFERENCE_HOTEL_NAME_GA, null);

			switch (resultCode) {
			// ������ ������ ��� GA�� �ͽ��гο� ��� 
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

				SimpleDateFormat dateFormat = new  SimpleDateFormat("yyMMddHHmmss", java.util.Locale.getDefault());
				Date date = new Date();
				String strDate = dateFormat.format(date);
				int userIdx = Integer.parseInt(mPay.getCustomer().getUserIdx());
				String userIdxStr = String.format("%07d", userIdx);
				String transId = strDate + userIdxStr;
				
				RenewalGaManager.getInstance(getApplicationContext()).
				purchaseComplete(
						transId, 
						mPay.getHotelDetail().getHotel().getName(), 
						mPay.getHotelDetail().getHotel().getCategory(), 
						(double) mPay.getPayPrice()
						);
				
				SimpleDateFormat dateFormat2 = new  SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
				strDate = dateFormat2.format(date);
				
				mMixpanel.getPeople().identify(userIdxStr);
				
				JSONObject properties = new JSONObject();
				try {
					properties.put("hotelName", mPay.getHotelDetail().getHotel().getName());
					properties.put("datetime", strDate); // �ŷ� �ð� = ��-��-��T��:��:��
					android.util.Log.e("BookingActivity", "properties hotel name : " + mPay.getHotelDetail().getHotel().getName() + " datetime : " + strDate);
				} catch (JSONException e) {
					e.printStackTrace();
					android.util.Log.e("BookingActivity", e.toString());
				}
				
				mMixpanel.getPeople().trackCharge(mPay.getPayPrice(), properties); // price = ���� �ݾ�
				
				JSONObject props = new JSONObject();
				try {
					props.put("hotelName", mPay.getHotelDetail().getHotel().getName());
					props.put("price",mPay.getPayPrice());
					props.put("datetime", strDate);
					props.put("userId", userIdxStr);
					props.put("tranId", transId);
					android.util.Log.e("BookingActivity", "props hotelName : " + mPay.getHotelDetail().getHotel().getName() + " price : " + mPay.getPayPrice() + " datetime : " + strDate);
				} catch (JSONException e) {
					e.printStackTrace();
					android.util.Log.e("BookingActivity", e.toString());
				}
				
				mMixpanel.track("transaction", props);
				
				RenewalGaManager.getInstance(getApplicationContext()).recordScreen("paymentConfirmation", "/todays-hotels/" + region + "/" + hotelName + "/booking-detail/payment-confirm");
				
				posListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						dialog.dismiss(); // �ݱ�
						RenewalGaManager.getInstance(getApplicationContext()).recordEvent("click", "confirmPayment", mPay.getHotelDetail().getHotel().getName(), (long)mHotelIdx);
						setResult(RESULT_OK);
						BookingActivity.this.finish();
					}
				};

				msg = getString(R.string.act_toast_payment_success);
				break;
			case CODE_RESULT_ACTIVITY_PAYMENT_SOLD_OUT:
				msg = getString(R.string.act_toast_payment_soldout);
				break;
			case CODE_RESULT_ACTIVITY_PAYMENT_NOT_AVAILABLE:
				msg = getString(R.string.act_toast_payment_not_available);
				break;
			case CODE_RESULT_ACTIVITY_PAYMENT_NETWORK_ERROR:
				msg = getString(R.string.act_toast_payment_network_error);
				break;
			case CODE_RESULT_ACTIVITY_PAYMENT_INVALID_SESSION:
				VolleyHttpClient.createCookie();	// ��Ű�� �ٽ� ���� �õ�
				return;
			case CODE_RESULT_ACTIVITY_PAYMENT_INVALID_DATE:
				msg = getString(R.string.act_toast_payment_invalid_date);
				break;
			case CODE_RESULT_ACTIVITY_PAYMENT_FAIL:
				msg = getString(R.string.act_toast_payment_fail);
				break;
			case CODE_RESULT_ACTIVITY_PAYMENT_CANCELED:
				msg = getString(R.string.act_toast_payment_canceled);
				break;
			case CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_READY:
				/**
				 * ������¼��ý� �ش� ������� ������ �������� ȭ�� ������ �����鼭 ������.
				 * �̸� ���� ������ ����. ���� ����Ʈ �����׸�Ʈ���� ã�� ���� ���ؼ� �ʿ���.
				 * �� �Ŀ��� �ٽ� �����۷����� �ʱ�ȭ����.
				 * �÷ο�) ���� ��Ƽ��Ƽ => ȣ���� ��Ƽ��Ƽ => ���ξ�Ƽ��Ƽ => ���� ����Ʈ �����׸�Ʈ => ���� ����Ʈ ���� �� �ֻ�� ������ ����Ʈ
				 */
				if (intent != null) {
					if (intent.getParcelableExtra(NAME_INTENT_EXTRA_DATA_PAY) != null) {
						Pay payData = intent.getParcelableExtra(NAME_INTENT_EXTRA_DATA_PAY);

						Editor editor = sharedPreference.edit();
						editor.putString(KEY_PREFERENCE_USER_IDX, payData.getCustomer().getUserIdx());
						Log.d("GcmIntentService", "category? " + payData.getHotelDetail().getHotel().getCategory());
						editor.commit();
					}
				}
				
				Editor editor = sharedPreference.edit();
				editor.putInt(KEY_PREFERENCE_ACCOUNT_READY_FLAG, CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_READY);
				editor.apply();

				setResult(CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_READY);
				finish();
				return;
			case CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_TIME_ERROR:
				msg = getString(R.string.act_toast_payment_account_time_error);
				break;
			case CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_DUPLICATE:
				msg = getString(R.string.act_toast_payment_account_duplicate);
				break;
			case CODE_RESULT_ACTIVITY_PAYMENT_TIMEOVER:
				msg = getString(R.string.act_toast_payment_account_timeover);
				break;
			default:
				return;
			}

			SimpleAlertDialog.build(this, title, msg, posTitle, posListener).show();

		} else if (requestCode == CODE_REQUEST_ACTIVITY_LOGIN) {
			if (resultCode == RESULT_OK) moveToPayStep();	
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (group.getId() == rgPaymentMethod.getId()) {
			if (checkedId == rbPaymentCard.getId()) mPay.setPayType("CARD");
			else if (checkedId == rbPaymentHp.getId()) mPay.setPayType("PHONE_PAY");
			else if (checkedId == rbPaymentAccount.getId()) mPay.setPayType("VBANK");
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
				RenewalGaManager.getInstance(getApplicationContext()).recordEvent("toggle action", "applyCredit", "off", null);
				

			} else { // ��������� ����
				tvOriginalPrice.setEnabled(true);
				tvCredit.setEnabled(true);
				tvOriginalPriceValue.setEnabled(true);
				tvCreditValue.setEnabled(true);
				RenewalGaManager.getInstance(getApplicationContext()).recordEvent("toggle action", "applyCredit", "on", null);
				
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
				android.util.Log.e("responSE!", response.toString());
				int sdk = android.os.Build.VERSION.SDK_INT;

				Customer buyer = new Customer();
				buyer.setEmail(obj.getString("email"));
				buyer.setName(obj.getString("name"));
				buyer.setPhone(obj.getString("phone"));
				buyer.setAccessToken(obj.getString("accessToken"));
				buyer.setUserIdx(obj.getString("idx"));

				mPay.setCustomer(buyer);
				buyer = mPay.getCustomer();

				/**
				 * �ؽ�Ʈ �ʵ尡 �ϳ��� ��������� �ش� ������ �Է� �޵��� ��.
				 */
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

					android.util.Log.e("buyer",buyer.getName()+" / " +buyer.getPhone()+" / "+buyer.getEmail() );

					if (!isEmptyTextField(buyer.getName())) {
						etReserverName.setText(buyer.getName());
						etReserverName.setKeyListener(null);
						etReserverName.setFocusable(false);
						if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
							etReserverName.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
						} else {
							etReserverName.setBackground(new ColorDrawable(Color.TRANSPARENT));
						}
					} 

					if (!isEmptyTextField(buyer.getPhone())) {
						etReserverNumber.setText(buyer.getPhone());
						etReserverNumber.setKeyListener(null);
						etReserverNumber.setFocusable(false);
						if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
							etReserverNumber.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
						} else {
							etReserverNumber.setBackground(new ColorDrawable(Color.TRANSPARENT));
						}
					} else {
						TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext()
								.getSystemService(Context.TELEPHONY_SERVICE);

						etReserverNumber.setText(telephonyManager
								.getLine1Number());
					}

					if (!isEmptyTextField(buyer.getEmail())) {
						etReserverEmail.setText(buyer.getEmail());
						etReserverEmail.setKeyListener(null);
						etReserverEmail.setFocusable(false);
						etReserverEmail.setBackgroundColor(Color.TRANSPARENT);
						if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
							etReserverEmail.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
						} else {
							etReserverEmail.setBackground(new ColorDrawable(Color.TRANSPARENT));
						}
					} 

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
				
				if(locale.equals("�ѱ���")){
					tvCheckIn.setText("20" + in[0] + getString(R.string.frag_booking_tab_year) + in[1] + getString(R.string.frag_booking_tab_month)
							+ in[2] + getString(R.string.frag_booking_tab_day) + " " + in[3] + getString(R.string.frag_booking_tab_hour));
				} else {
					tvCheckIn.setText("20" + in[0] + "-" + in[1] + "-"
							+ in[2] + " " + in[3] + ":00");
				}
		
				String out[] = checkout.split("-");
				
				if(locale.equals("�ѱ���")){
					tvCheckOut.setText("20" + out[0] + getString(R.string.frag_booking_tab_year) + out[1] + getString(R.string.frag_booking_tab_month) 
							+ out[2] + getString(R.string.frag_booking_tab_day) + " "+ out[3] + getString(R.string.frag_booking_tab_hour));
				} else {
					tvCheckOut.setText("20" + out[0] + "-" + out[1] + "-" 
							+ out[2] + " " + out[3] + ":00");
				}
					
				unLockUI();
			} catch (Exception e) {
				onError(e);
			}
		} else if (url.contains(URL_WEBAPI_USER_LOGIN)) {
			try {
				if (response.getBoolean("login")) {
					unLockUI();
					VolleyHttpClient.createCookie();
					mQueue.add(new DailyHotelStringRequest(Method.GET, new StringBuilder(
							URL_DAILYHOTEL_SERVER).append(URL_WEBAPI_APP_TIME)
							.toString(), null, BookingActivity.this,
							BookingActivity.this));
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
					if (llReserverInfoLabel.getVisibility() == View.VISIBLE) {

						buyer.setEmail(tvReserverEmail.getText().toString());
						buyer.setPhone(tvReserverNumber.getText().toString());
						buyer.setName(tvReserverName.getText().toString());

					}

					mPay.setCustomer(buyer);
					moveToPayStep();

				} else {
					android.content.DialogInterface.OnClickListener posListener = new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							setResult(CODE_RESULT_ACTIVITY_PAYMENT_SALES_CLOSED);
							finish();
						}
					};

					SimpleAlertDialog.build(this, getString(R.string.dialog_notice2), getString(R.string.dialog_msg_sales_closed), getString(R.string.dialog_btn_text_confirm), posListener).show();

				}

			} catch (JSONException e) {
				onError(e);
			}

		} else if (url.contains(URL_WEBAPI_USER_UPDATE_FACEBOOK)) {
			android.util.Log.e("UPDATE_FACEBOOK_RESULT",response.toString());
			unLockUI();
			try {
				if(!response.getBoolean("result")) {
					showToast(response.getString("message"), Toast.LENGTH_LONG, false);
				} else {
					llReserverInfoLabel.setVisibility(View.VISIBLE);
					llReserverInfoEditable.setVisibility(View.GONE);
					etReserverName.setVisibility(View.GONE);
					etReserverNumber.setVisibility(View.GONE);
					etReserverEmail.setVisibility(View.GONE);

					tvReserverName.setText(etReserverName.getText().toString());
					tvReserverNumber.setText(etReserverNumber.getText().toString());
					tvReserverEmail.setText(etReserverEmail.getText().toString());
					
					btnPay.performClick();
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
				
				int originalPrice = Integer.parseInt(mPay.getHotelDetail().getHotel()
						.getDiscount().replaceAll(",", ""));
				DecimalFormat comma = new DecimalFormat("###,##0");
				
				Log.d(TAG, locale);
				if (locale.equals("�ѱ���"))	{
					tvOriginalPriceValue.setText(comma.format(originalPrice)+Html.fromHtml(getString(R.string.currency)));
					tvPrice.setText(comma.format(originalPrice)+Html.fromHtml(getString(R.string.currency)));
				}
				else	{
					tvOriginalPriceValue.setText(Html.fromHtml(getString(R.string.currency))+comma.format(originalPrice));
					tvPrice.setText(Html.fromHtml(getString(R.string.currency))+comma.format(originalPrice));
				}
					
				mPay.setPayPrice(originalPrice);
				
				swCredit.setChecked(false);
				
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
		} else if(url.contains(URL_WEBAPI_USER_ALIVE)) {
			android.util.Log.e("USER_ALIVE / CALL_RESOURCE",response.toString()+" / "+mAliveCallSource);
			unLockUI();
			/**
			 * ALIVE CALL�� �ҽ��� ������,
			 * 1. BookingActivity => PaymentActivity�� �Ѿ��
			 * 2. PaymentActivity => BookingActivity�� �Ѿ������
			 */
			if (response.equals("alive")) {
				if (mAliveCallSource.equals("PAYMENT")) {//1�� 
					mQueue.add(new DailyHotelStringRequest(Method.GET, new StringBuilder(
							URL_DAILYHOTEL_SERVER).append(URL_WEBAPI_APP_TIME)
							.toString(), null, BookingActivity.this,
							BookingActivity.this));
				} else if(mAliveCallSource.equals("ACTIVITY_RESULT")) {//2�� 
					activityResulted(mReqCode, mResCode, mResIntent);	
				}

			} else {
				if (sharedPreference.getBoolean(
						KEY_PREFERENCE_AUTO_LOGIN, false)) {
					String id = sharedPreference.getString(
							KEY_PREFERENCE_USER_ID, null);
					String accessToken = sharedPreference
							.getString(KEY_PREFERENCE_USER_ACCESS_TOKEN, null);
					String pw = sharedPreference.getString(
							KEY_PREFERENCE_USER_PWD, null);

					Map<String, String> loginParams = new HashMap<String, String>();

					if (accessToken != null) loginParams.put("accessToken",accessToken);
					else loginParams.put("email", id);
					loginParams.put("pw", pw);
					android.util.Log.e("LOGIN PARAMS",loginParams.toString());

					mQueue.add(new DailyHotelJsonRequest(Method.POST,
							new StringBuilder(URL_DAILYHOTEL_SERVER).append(
									URL_WEBAPI_USER_LOGIN).toString(),
									loginParams, BookingActivity.this,
									BookingActivity.this));
				}
			}
		}

	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.payment_wait_actions, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.action_call:
			RenewalGaManager.getInstance(getApplicationContext()).recordEvent("click", "callHotel", mPay.getHotelDetail().getHotel().getName(), (long)mHotelIdx);
			Intent i = new Intent(
					Intent.ACTION_DIAL,
					Uri.parse(new StringBuilder("tel:")
					.append(PHONE_NUMBER_DAILYHOTEL)
					.toString()));
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	protected void onDestroy() {
		mMixpanel.flush();
		super.onDestroy();
	}
}
