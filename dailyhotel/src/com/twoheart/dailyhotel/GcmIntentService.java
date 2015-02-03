package com.twoheart.dailyhotel;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.IntentService;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.twoheart.dailyhotel.activity.PushLockDialogActivity;
import com.twoheart.dailyhotel.activity.ScreenOnPushDialogActivity;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.RenewalGaManager;
import com.twoheart.dailyhotel.util.WakeLock;

/**
 * GCM �޽����� �� ��� ������ ó���ϴ� Ŭ����,
 * ����Ʈ���� �����ִ� ��� ����� �հ� ���̾�α׸� ���.
 * ����Ʈ���� ���������� �츮 ���� Ų ���¿��� ���� �Ϸ� �޽����� �޾Ҵٸ�, �����Ϸ� ���̾�α׸� ���.
 * ��Ƽ�����̼��� GCM�� ������ ��� ��쿡�� ��� ���.
 * 
 * case 1 : �޴����� ���������� ���� ���ϸ�ȣ���� �������� ���� ��Ȳ, => Ǫ�ø� �� 
 * case 2 : �޴����� �����ְ� ���ϸ�ȣ���� �����ִ� ��Ȳ => Ǫ��, ���̾�α��� Ǫ�� ��
 * case 3 : �޴����� �����ִ� ��� => ���̾�α��� Ǫ�ø� ��
 * @author jangjunho
 *
 */
public class GcmIntentService extends IntentService implements Constants{

	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	private boolean mIsBadge;
	private boolean mIsSound;
	private MixpanelAPI mMixpanel;

	public GcmIntentService() {
		super("GcmIntentService");
	}
	
	@Override
	public void onCreate() {
		mMixpanel = MixpanelAPI.getInstance(getApplicationContext(), "791b366dadafcd37803f6cd7d8358373");
		super.onCreate();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

		String messageType = gcm.getMessageType(intent);
		
		mIsBadge = false;
		mIsSound = true;

		if (!extras.isEmpty()) { 

			try {
				String collapseKey = intent.getStringExtra("collapse_key");
				Log.d("GcmIntentService", "collapseKey : " + collapseKey);
	            
				JSONObject jsonMsg = new JSONObject(extras.getString("message"));
				String msg = jsonMsg.getString("msg");
				Log.d("GcmIntentService", "jsonMsg : " + jsonMsg.toString());
				int type = -1;
				
				Log.d("GcmIntentService", "type : " + jsonMsg.getString("type") + " collapseKey : " + collapseKey);
				
				if (collapseKey.equals("do_not_collapse")) {
					Log.d("GcmIntentService", "do_not_collapse");
				}
				
				if (jsonMsg.getString("type").equals("notice")) type = PUSH_TYPE_NOTICE;
				else if (jsonMsg.getString("type").equals("account_complete")) type = PUSH_TYPE_ACCOUNT_COMPLETE;
				
				if (!jsonMsg.isNull("badge")) mIsBadge = jsonMsg.getBoolean("badge");
				if (!jsonMsg.isNull("sound")) mIsSound = jsonMsg.getBoolean("sound");
				SharedPreferences pref = this.getSharedPreferences(NAME_DAILYHOTEL_SHARED_PREFERENCE, Context.MODE_PRIVATE);
				
				Log.d("GcmIntentService", "in switch type : " + type);
				switch (type) {
				case PUSH_TYPE_ACCOUNT_COMPLETE:
//					sendPush(messageType, type, msg, "", "");
//					break;
					String tid = jsonMsg.getString("TID");
					String hotelName = jsonMsg.getString("hotelName");
					String paidPrice = jsonMsg.getString("paidPrice");
					
					if (collapseKey.equals(pref.getString("collapseKey", ""))) {
						break;
					} else {
						Editor editor = pref.edit();
						editor.putString("collapseKey", collapseKey);
						editor.apply();
						sendPush(messageType, type, msg);
						
						SimpleDateFormat dateFormat = new  SimpleDateFormat("yyMMddHHmmss", java.util.Locale.getDefault());
						Date date = new Date();
						String strDate = dateFormat.format(date);
						int userIdx = Integer.parseInt(pref.getString(KEY_PREFERENCE_USER_IDX, "0"));
						String userIdxStr = String.format("%07d", userIdx);
						String transId = strDate + userIdxStr; //��Ÿ ���������� �̰� transaction ID�� ����ϰ� ������ü�� ��� �Ѱܹ޴� tid���� �����. 
						
						RenewalGaManager.getInstance(getApplicationContext()).
						purchaseComplete(
								tid, 
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
							props.put("tranId", tid);
						} catch (JSONException e) {
							e.printStackTrace();
						}
						
						mMixpanel.track("transaction", props);
						break;
					}
//					Log.d("GcmIntentService", "purchase complete!!!");
					
				case PUSH_TYPE_NOTICE:
					Log.d("GcmIntentService", "notice complete!!!");
					if (collapseKey.equals(pref.getString("collapseKey", ""))) {
						break;
						
					} else {
						Editor editor = pref.edit();
						editor.putString("collapseKey", collapseKey);
						editor.apply();
						sendPush(messageType, type, msg);
						break;
					}
				}
				android.util.Log.e("GCM_MESSAGE",jsonMsg.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}
	
	public void sendPush(String messageType, int type, String msg) {
		Log.d("GcmIntentService", "sendPush");
		if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
			
			if (isScreenOn(this) && type != -1) { // ���ϸ�ȣ�� ���� �����ִ°��.
				
				ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
				ComponentName topActivity = am.getRunningTasks(1).get(0).topActivity;
				String className = topActivity.getClassName();

				android.util.Log.e("CURRENT_ACTIVITY_PACKAGE", className+" / "+className);
				
				if (className.contains("dailyhotel") && !className.contains("GcmLockDialogActivity") && !mIsBadge) {
					
					Intent i = new Intent(this, ScreenOnPushDialogActivity.class);
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					i.putExtra(NAME_INTENT_EXTRA_DATA_PUSH_TYPE, type);
					i.putExtra(NAME_INTENT_EXTRA_DATA_PUSH_MSG, msg);
					startActivity(i);
				}
				
			} else if (!isScreenOn(this) && !mIsBadge) { // ��ũ�� �����ִ°��
				
				WakeLock.acquireWakeLock(this, PowerManager.FULL_WAKE_LOCK
						| PowerManager.ACQUIRE_CAUSES_WAKEUP);	// PushDialogActivity���� release ����.
				KeyguardManager manager = (KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE);  
				KeyguardLock lock = manager.newKeyguardLock(Context.KEYGUARD_SERVICE);  
				lock.disableKeyguard();  // ������ ���ȭ���� disable

				Intent i = new Intent(this, PushLockDialogActivity.class);
				i.putExtra(NAME_INTENT_EXTRA_DATA_PUSH_MSG, msg);
				i.putExtra(NAME_INTENT_EXTRA_DATA_PUSH_TYPE, type);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | 
						Intent.FLAG_ACTIVITY_CLEAR_TOP);
				this.startActivity(i);
			}
			// ��Ƽ�����̼��� ���̽��� ������� �׻� �ߵ�����.
			sendNotification(type, msg);
		}
	}

	public boolean isScreenOn(Context context) {
		return ((PowerManager)context.getSystemService(Context.POWER_SERVICE)).isScreenOn();
	}

	private void sendNotification(int type, String msg) {
		mNotificationManager = (NotificationManager)
				this.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent intent = new Intent(this, MainActivity.class);
		if (type == PUSH_TYPE_ACCOUNT_COMPLETE) { 
			intent.putExtra(NAME_INTENT_EXTRA_DATA_PUSH_TYPE, PUSH_TYPE_ACCOUNT_COMPLETE);
		} else if (type == PUSH_TYPE_NOTICE) {
			intent.putExtra(NAME_INTENT_EXTRA_DATA_PUSH_TYPE, PUSH_TYPE_NOTICE);
		}
		
		// type�� notice Ÿ�԰� account_complete Ÿ���� ������. reservation�� ��� ����Ȯ�� â���� �̵�.

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		Uri uri = null;
		
		if (mIsSound) uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		else uri = null;

		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(this)
		.setSmallIcon(R.drawable.img_ic_appicon_feature)
		.setContentTitle(getString(R.string.app_name))
		.setAutoCancel(true)
		.setSound(uri)
		.setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
	
	@Override
	public void onDestroy() {
		mMixpanel.flush();
		super.onDestroy();
	}
	

}