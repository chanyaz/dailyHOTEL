package com.twoheart.dailyhotel.activity;

import com.twoheart.dailyhotel.util.Util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ExitActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (Util.isOverAPI21() == true)
		{
			finishAndRemoveTask();
		} else
		{
			finish();
		}
	}

	public static void exitApplication(Context context)
	{
		Intent intent = new Intent(context, ExitActivity.class);

		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

		context.startActivity(intent);
	}
}
