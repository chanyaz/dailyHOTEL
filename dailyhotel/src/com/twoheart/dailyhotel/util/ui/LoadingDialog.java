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

import android.app.Dialog;
import android.content.DialogInterface;
import android.view.ViewGroup.LayoutParams;
import android.widget.ProgressBar;

import com.twoheart.dailyhotel.R;

public class LoadingDialog {

	private Dialog mDialog;

	public LoadingDialog(final BaseActivity activity) {
		mDialog = new Dialog(activity, R.style.TransDialog);
		ProgressBar pb = new ProgressBar(activity);
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		mDialog.addContentView(pb, params);
		mDialog.setCancelable(true);
		mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				hide();
				activity.onBackPressed();

			}
		});
	}
	
	public boolean isVisible() {
		return mDialog.isShowing();
	}

	public void show() {
		if (!mDialog.isShowing()) mDialog.show();
	}

	public void hide() {
		if (mDialog.isShowing()) mDialog.dismiss();
	}
}
