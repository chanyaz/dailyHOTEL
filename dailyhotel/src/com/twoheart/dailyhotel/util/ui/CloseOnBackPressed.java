/**
 * Copyright (c) 2014 Daily Co., Ltd. All rights reserved.
 *
 * CloseOnBackPressed
 * 
 * Android ����̽��� Back ��ư�� �̿��� ������ ���ø����̼� �ν��Ͻ� ��
 * �Ḧ �����ϱ� ���� Ŭ�����̴�. �� Ŭ������ ������ Activity������ Back
 * ��ư�� ���� �� �� ���������� ���ᰡ �ȴ�.
 *
 * @since 2014-02-24
 * @version 1
 * @author Mike Han(mike@dailyhotel.co.kr)
 */
package com.twoheart.dailyhotel.util.ui;

import android.app.Activity;
import android.widget.Toast;

public class CloseOnBackPressed {
	
	private long backPressedTime = 0;
	private Toast toast;
	private Activity activity;
	
	public CloseOnBackPressed(Activity activity) {
		this.activity = activity;
		
	}
	
	public boolean onBackPressed() {
		
		if (System.currentTimeMillis() <= backPressedTime + 2000) {
			toast.cancel();
			return true;
		}
		
		backPressedTime = System.currentTimeMillis();
		showGuide();
		
		return false;
		
	}
	
	private void showGuide() {
		toast = Toast.makeText(activity, "\'�ڷ�\' ��ư�� �� �� �� �����ø� �����մϴ�.", Toast.LENGTH_SHORT);
		toast.show();
	}

}
