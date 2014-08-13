package com.twoheart.dailyhotel;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.twoheart.dailyhotel.activity.AccountCompleteDialogActivity;
import com.twoheart.dailyhotel.activity.GcmLockDialogActivity;
import com.twoheart.dailyhotel.activity.PushDialogActivity;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.WakeLock;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.IntentService;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.KeyguardManager.KeyguardLock;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

/**
 * GCM �޽����� �� ��� ������ ó���ϴ� Ŭ����,
 * ����Ʈ���� �����ִ� ��� ����� �հ� ���̾�α׸� ���.
 * ����Ʈ���� ���������� �츮 ���� Ų ���¿��� ���� �Ϸ� �޽����� �޾Ҵٸ�, �����Ϸ� ���̾�α׸� ���.
 * ��Ƽ�����̼��� GCM�� ������ ��� ��쿡�� ��� ���.
 * @author jangjunho
 *
 */
public class GcmIntentService extends IntentService implements Constants{

	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) { 

			try {
				JSONObject jsonMsg = new JSONObject(extras.getString("message"));
				String type = jsonMsg.getString("type");
				String msg = jsonMsg.getString("msg");

				android.util.Log.e("GCM_MESSAGE",jsonMsg.toString());
				
				if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
					
					if (isScreenOn(this) && type.equals("account_complete")) { // ���ϸ�ȣ�� ���� �����ִ°��.
						
						ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
						ComponentName topActivity = am.getRunningTasks(1).get(0).topActivity;
						String className = topActivity.getClassName();

						android.util.Log.e("CURRENT_ACTIVITY_PACKAGE", className+" / "+className);
						
						if (className.contains("dailyhotel") && !className.contains("GcmLockDialogActivity")) {
							
							Intent i = new Intent(this, AccountCompleteDialogActivity.class);
							i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							i.putExtra("msg", msg);
							startActivity(i);
						}
						
					} else if (!isScreenOn(this)) { // ��ũ�� �����ִ°��
						
						WakeLock.acquireWakeLock(this, PowerManager.FULL_WAKE_LOCK
								| PowerManager.ACQUIRE_CAUSES_WAKEUP);	// PushDialogActivity���� release ����.
						KeyguardManager manager = (KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE);  
						KeyguardLock lock = manager.newKeyguardLock(Context.KEYGUARD_SERVICE);  
						lock.disableKeyguard();  // ������ ���ȭ���� disable

						Intent i = new Intent(this, GcmLockDialogActivity.class);
						i.putExtra(NAME_INTENT_EXTRA_DATA_PUSH_MSG, msg);

						i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | 
								Intent.FLAG_ACTIVITY_CLEAR_TOP);
						this.startActivity(i);
					}
					// ��Ƽ�����̼��� ���̽��� ������� �׻� �ߵ�����.
					sendNotification(type, msg);

				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	public boolean isScreenOn(Context context) {
		return ((PowerManager)context.getSystemService(Context.POWER_SERVICE)).isScreenOn();
	}

	private void sendNotification(String type, String msg) {
		mNotificationManager = (NotificationManager)
				this.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent intent = new Intent(this, MainActivity.class);
		if (type.equals("account_complete")) intent.putExtra(NAME_INTENT_EXTRA_DATA_IS_INTENT_FROM_PUSH, true);
		// type�� notice Ÿ�԰� account_complete Ÿ���� ������. reservation�� ��� ����Ȯ�� â���� �̵�.

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(this)
		.setSmallIcon(R.drawable.img_ic_appicon)
		.setContentTitle(getString(R.string.app_name))
		.setAutoCancel(true)
		.setSound(uri)
		.setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
	

}