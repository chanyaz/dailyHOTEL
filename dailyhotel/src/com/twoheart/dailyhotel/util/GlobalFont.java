/**
 * Copyright (c) 2014 Daily Co., Ltd. All rights reserved.
 *
 * GlobalFont
 * 
 * ��׷��� ���ڷ� �޾� �ش� ��׷쿡 ���ԵǾ� �ִ� ��� ���ڵ�(�ؽ�Ʈ�� ��
 * Ʈ �Ӽ�)�� �̸� ���� Ŀ���� ��Ʈ�� �����Ѵ�. �� ��ƿ Ŭ������ ����ϱ�
 * ������ �̸� ����� ��Ʈ�� �����صξ�� �Ѵ�.
 *
 * @since 2014-04-01
 * @version 1
 * @author Mike Han(mike@dailyhotel.co.kr)
 */
package com.twoheart.dailyhotel.util;

import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.twoheart.dailyhotel.DailyHotel;

public class GlobalFont {

	public static void apply(ViewGroup root) {
		for (int i = 0; i < root.getChildCount(); i++) {
			View child = root.getChildAt(i);

			if (child instanceof TextView) {
				TextView fontTextView = ((TextView) child);
				
				fontTextView.setPaintFlags(((TextView) child).getPaintFlags()
						| Paint.SUBPIXEL_TEXT_FLAG);
				
				if (fontTextView.getTypeface() != null)
					if (fontTextView.getTypeface().equals(DailyHotel.getBoldTypeface()))
						continue;
				
				fontTextView.setTypeface(DailyHotel.getTypeface());
				fontTextView.invalidate();
			} else if (child instanceof ViewGroup)
				apply((ViewGroup) child);

		}

	}

}
