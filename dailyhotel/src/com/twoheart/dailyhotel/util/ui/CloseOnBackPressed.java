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

import android.widget.Toast;

import com.twoheart.dailyhotel.MainActivity;

public class CloseOnBackPressed {
	
	private long backPressedTime = 0;
	private MainActivity mActivity;
	
	public CloseOnBackPressed(MainActivity activity) {
		mActivity = activity;
		
	}
	
	public boolean onBackPressed() {
		
		if (System.currentTimeMillis() <= backPressedTime + 2000) {
			mActivity.mToast.cancel();
			return true;
		}
		
		backPressedTime = System.currentTimeMillis();
		showGuide();
		
		return false;
		
	}
	
	private void showGuide() {
		mActivity.showToast("\'�ڷ�\' ��ư�� �� �� �� �����ø� ���� ����˴ϴ�", Toast.LENGTH_SHORT, true);
		
	}

}
