package kr.co.kcp.android.payment.standard;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class ResultRcvActivity extends Activity {
	
	public static Uri m_uriResult;
	public static boolean b_type      = false;

	public static final String m_strLogTag = "PaySample";
	public static final String s_strLogTag = "PayACNTSample";
	public static Activity activity; //

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(m_strLogTag,
				"[ResultRcvActivity] called__onCreate");

		super.onCreate(savedInstanceState);
		Intent myIntent = getIntent();
		
		activity = ResultRcvActivity.this;

		Log.d(m_strLogTag, "[ResultRcvActivity] launch_uri=["
				+ myIntent.getData().toString() + "]");

		if (myIntent.getData().getScheme().equals("dailyHOTEL") == true) {
			m_uriResult = myIntent.getData();
		} else {
			m_uriResult = null;
		}
		finish();
	}
}