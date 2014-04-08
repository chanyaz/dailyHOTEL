/**
 * Copyright (c) 2014 Daily Co., Ltd. All rights reserved.
 *
 * OnLoadCompleteListener
 * 
 * ��� �ε� �۾��� �Ϸ�Ǿ� ����ڿ��� ������ �� �ִ� �غ� �� ���¸� ��
 * ���ִ� �������̽��̴�. Activity�� ���ӵ� Fragment������ �۾��� �Ϸ�
 * �� Activity �� �ٸ� Fragment�� �ϰ������� �˸��� ���� ����ƴ�.
 *
 * @since 2014-02-24
 * @version 1
 * @author Mike Han(mike@dailyhotel.co.kr)
 */
package com.twoheart.dailyhotel.util.ui;

import android.support.v4.app.Fragment;

public interface OnLoadCompleteListener {
	public void onLoadComplete(Fragment fragment, boolean isSucceed);
	
}
