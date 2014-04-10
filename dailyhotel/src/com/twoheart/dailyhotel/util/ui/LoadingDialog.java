/**
 * Copyright (c) 2014 Daily Co., Ltd. All rights reserved.
 *
 * LoadingDialog
 * 
 * Activity ��ü�� �ڵ��ϴ� �ε� ���̾�α� â�̴�. �ε� �۾��� �����ϴ�
 * ���� �ε� ���̾�α� â�� ���� ��ҽ� Activity�� onBackPressed
 * �޼��嵵 ���� ����ȴ�.
 *
 * @since 2014-02-24
 * @version 1
 * @author Mike Han(mike@dailyhotel.co.kr)
 */
package com.twoheart.dailyhotel.util.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.ViewGroup.LayoutParams;
import android.widget.ProgressBar;

import com.twoheart.dailyhotel.R;

public class LoadingDialog {
	private static Dialog loadingDialog = null;

	public static synchronized void showLoading(final Activity activity) {
		if (loadingDialog == null) {
			loadingDialog = new Dialog(activity, R.style.TransDialog);
			ProgressBar pb = new ProgressBar(activity);
			LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			loadingDialog.addContentView(pb, params);
			loadingDialog.setCancelable(true);
			loadingDialog
					.setOnCancelListener(new DialogInterface.OnCancelListener() {

						@Override
						public void onCancel(DialogInterface dialog) {
							hideLoading();
							activity.onBackPressed();

						}
					});
			loadingDialog.show();

		} else {
			hideLoading();
			showLoading(activity);
		}
	}

	public static void hideLoading() {
		if (loadingDialog != null) {
			loadingDialog.dismiss();
			loadingDialog = null;
		}
	}
}
