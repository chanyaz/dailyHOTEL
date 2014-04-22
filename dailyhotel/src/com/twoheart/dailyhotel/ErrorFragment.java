/**
 * Copyright (c) 2014 Daily Co., Ltd. All rights reserved.
 *
 * ErrorFragment (���� ȭ��)
 * 
 * ��Ʈ��ũ ���� �� ������ �߻����� �� �������� ȭ���̴�. �� ȭ���� ���� ȭ
 * �� ����(MainActivity)���� ���Ǵ� ���� ȭ�� ����(Fragment)�̴�.
 *
 * @since 2014-02-24
 * @version 1
 * @author Mike Han(mike@dailyhotel.co.kr)
 */
package com.twoheart.dailyhotel;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.twoheart.dailyhotel.util.network.VolleyHttpClient;
import com.twoheart.dailyhotel.util.ui.BaseFragment;

public class ErrorFragment extends BaseFragment implements OnClickListener {

	private MainActivity mHostActivity;
	private Button btnRetry;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_error, container, false);
		mHostActivity = (MainActivity) getActivity();
		mHostActivity.setActionBar("dailyHOTEL");

		btnRetry = (Button) view.findViewById(R.id.btn_error);
		btnRetry.setOnClickListener(this);
		
		return view;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == btnRetry.getId()) {

			// network ������ �ȵ�������
			if (!VolleyHttpClient.isAvailableNetwork()) {
				Toast.makeText(mHostActivity, "��Ʈ��ũ ���¸� Ȯ���� �ּ���",
						Toast.LENGTH_SHORT).show();
				return;
			} else {
				int index = mHostActivity.indexLastFragment;
				mHostActivity.replaceFragment(mHostActivity.getFragment(index));
//				mHostActivity.removeFragment(this);
				
			}

		}
	}
}
