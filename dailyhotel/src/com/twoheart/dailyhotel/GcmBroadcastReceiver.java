package com.twoheart.dailyhotel;

import com.twoheart.dailyhotel.util.Constants;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
/**
 * GCM �޽����� �� ��� �̸� �޾� ������ ó���ϴ� GcmItentService �� ������.
 * @author jangjunho
 *
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver implements Constants{
    @Override
    public void onReceive(Context context, Intent intent) {
    	SharedPreferences pref = context.getSharedPreferences(NAME_DAILYHOTEL_SHARED_PREFERENCE, Context.MODE_PRIVATE);
    	// gcm_id �� empty��� �ش� ���� �α׾ƿ� �� �����̹Ƿ�, GCM�� ���� �ʵ��� �Ѵ�.
    	if (pref.getString(KEY_PREFERENCE_GCM_ID, "").isEmpty()) {
    		android.util.Log.e("Ignore Push,","true");
    	} else {
    		ComponentName comp = new ComponentName(context.getPackageName(),GcmIntentService.class.getName());
            startWakefulService(context, (intent.setComponent(comp)));

            setResultCode(Activity.RESULT_OK);

    	}
    }
}

