package com.twoheart.dailyhotel.activity;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.twoheart.dailyhotel.MainActivity;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.GlobalFont;
import com.twoheart.dailyhotel.util.RenewalGaManager;
import com.twoheart.dailyhotel.util.WakeLock;

/**
 * ȭ���� OFF �����϶� GCM �޽����� �޴� ��� īī���� ó�� Ǫ�� ���̾�αװ� �˾���.
 * @author jangjunho
 *
 */
public class PushLockDialogActivity extends Activity implements OnClickListener,Constants{
	
	private Button btnOkButton;
	private Button btnCancelButton;
	private TextView tvMsg;
//	private TextView tvTitle;
	
	private String mMsg;
	private int mType;
	private MixpanelAPI mMixpanel;
	//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_push_lock_dialog_gcm);
		
		mMixpanel = MixpanelAPI.getInstance(getApplicationContext(), "791b366dadafcd37803f6cd7d8358373");
		
		mMsg = getIntent().getStringExtra(NAME_INTENT_EXTRA_DATA_PUSH_MSG);
		mType = getIntent().getIntExtra(NAME_INTENT_EXTRA_DATA_PUSH_TYPE, -1);
		String hotelName = getIntent().getStringExtra("hotelName");
		String paidPrice = getIntent().getStringExtra("paidPrice");
		
//		String title = null;
//		if (mType == PUSH_TYPE_NOTICE) {
//			title = "�˸�";
//		} else if (mType == PUSH_TYPE_ACCOUNT_COMPLETE) {
//			title = mMsg.substring(0, mMsg.indexOf("]")+1);
//		}
		
//		mMsg = mMsg.replace("]", "]\n");
		
		tvMsg = (TextView) findViewById(R.id.tv_push_lock_dialog_msg);
		
		if (mType == PUSH_TYPE_NOTICE) {
			tvMsg.setText(mMsg);
		} else if (mType == PUSH_TYPE_ACCOUNT_COMPLETE) {
			SharedPreferences pref = this.getSharedPreferences(NAME_DAILYHOTEL_SHARED_PREFERENCE, Context.MODE_PRIVATE);
			SimpleDateFormat dateFormat = new  SimpleDateFormat("yyMMDDHHmmss", java.util.Locale.getDefault());
			Date date = new Date();
			String strDate = dateFormat.format(date);
			int userIdx = Integer.parseInt(pref.getString(KEY_PREFERENCE_USER_IDX, "0"));
			String userIdxStr = String.format("%07d", userIdx);
			String transId = strDate + userIdxStr;
			
			RenewalGaManager.getInstance(getApplicationContext()).
			purchaseComplete(
					transId, 
					hotelName, 
					"unidentified", 
					Double.parseDouble(paidPrice)
					);
			
			SimpleDateFormat dateFormat2 = new  SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
			strDate = dateFormat2.format(date);
			
			mMixpanel.getPeople().identify(userIdxStr);
			
			JSONObject properties = new JSONObject();
			try {
				properties.put("hotelName", hotelName);
				properties.put("datetime", strDate); // �ŷ� �ð� = ��-��-��T��:��:��
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			mMixpanel.getPeople().trackCharge(Double.parseDouble(paidPrice), properties); // price = ���� �ݾ�
			
			JSONObject props = new JSONObject();
			try {
				props.put("hotelName", hotelName);
				props.put("price", Double.parseDouble(paidPrice));
				props.put("datetime", strDate);
				props.put("userId", userIdxStr);
				props.put("tranId", transId);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			mMixpanel.track("transaction", props);
			
			int index = mMsg.lastIndexOf("]");
			StringBuffer sb = new StringBuffer(mMsg); 
			String result = sb.replace( index, index+1, "]\n" ).toString();
			
			tvMsg.setText(result);
		}
		
		
//		tvTitle = (TextView) findViewById(R.id.tv_push_lock_dialog_title);
//		tvTitle.setText(title);

		
		btnOkButton = (Button) findViewById(R.id.btn_push_lock_dialog_show);
		btnCancelButton = (Button) findViewById(R.id.btn_push_lock_dialog_close);
		
		btnOkButton.setOnClickListener(this);
		btnCancelButton.setOnClickListener(this);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
		        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
		        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
		        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		
		WakeLock.releaseWakeLock();
		
		GlobalFont.apply((ViewGroup) getWindow().getDecorView());
		
	}
	
	@Override
	public void onClick(View v) {
		if(v.getId() == btnOkButton.getId()) {
			Intent intent = new Intent();
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			intent.setClass(this, MainActivity.class);
	        intent.putExtra(NAME_INTENT_EXTRA_DATA_PUSH_TYPE, mType); // ���ο�Ƽ��Ƽ -> ����Ȯ�θ���Ʈ -> �ֽ� ���� Ŭ��, 
			
			startActivity(intent);
			finish();
			
		} else if(v.getId() == btnCancelButton.getId()) {
			finish();
		}
	}
	
	@Override
	protected void onDestroy() {
		mMixpanel.flush();
		super.onDestroy();
	}
	
}
