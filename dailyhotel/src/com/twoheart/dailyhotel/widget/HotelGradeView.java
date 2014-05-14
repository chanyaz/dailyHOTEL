package com.twoheart.dailyhotel.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.twoheart.dailyhotel.util.Util;

public class HotelGradeView extends FrameLayout {
	
	private TextView tvHotelGradeName;
	
	private String mHotelGradeName;
	private String mHotelGradeCode;
	private int mHotelGradeColor;
	
	public HotelGradeView(Context context) {
		super(context);
		init();
	}
	
	public HotelGradeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public HotelGradeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init() {
		Context context = getContext();
		tvHotelGradeName = new TextView(context);
		
		tvHotelGradeName.setTextColor(getResources().getColor(android.R.color.white));
		tvHotelGradeName.setTextSize((float) 11.5);
		tvHotelGradeName.setGravity(Gravity.CENTER);
		tvHotelGradeName.setSingleLine(true);
		
		addView(tvHotelGradeName);
		setPadding(Util.dpToPx(context, 5.5), 2, Util.dpToPx(context, 5.5), 1);
		
	}
	
	public void setHotelGradeCode(String hotelGradeCode) {
		mHotelGradeCode = hotelGradeCode;
		
		if (mHotelGradeCode.equals("biz") | mHotelGradeCode.equals("hostel") | mHotelGradeCode.equals("grade1") | 
				mHotelGradeCode.equals("grade2") | mHotelGradeCode.equals("grade3")) {
			setHotelGradeColor("#055870");
			
			if (mHotelGradeCode.equals("biz"))
				setHotelGradeName("�����Ͻ�");
			else if (mHotelGradeCode.equals("hostel"))
				setHotelGradeName("ȣ����");
			else if (mHotelGradeCode.equals("grade1"))
				setHotelGradeName("1��");
			else if (mHotelGradeCode.equals("grade2"))
				setHotelGradeName("2��");
			else if (mHotelGradeCode.equals("grade3"))
				setHotelGradeName("3��");

		} else if (mHotelGradeCode.equals("boutique")) {
			setHotelGradeColor("#9f2d58");
			setHotelGradeName("�ζ��");

		} else if (mHotelGradeCode.equals("residence")) {
			setHotelGradeColor("#407f67");
			setHotelGradeName("��������");

		} else if (mHotelGradeCode.equals("resort") | mHotelGradeCode.equals("pension") | mHotelGradeCode.equals("condo")) {
			setHotelGradeColor("#cf8d14");
			
			if (mHotelGradeCode.equals("resort"))
				setHotelGradeName("����Ʈ");
			else if (mHotelGradeCode.equals("pension"))
				setHotelGradeName("���");
			else if (mHotelGradeCode.equals("condo"))
				setHotelGradeName("�ܵ�");

		} else if (mHotelGradeCode.equals("special")) {
			setHotelGradeColor("#ab380a");
			setHotelGradeName("Ư��");

		} else {
			setHotelGradeColor("#808080");
			setHotelGradeName("����");
		}
		
	}
	
	private void setHotelGradeName(String hotelGradeName) {
		mHotelGradeName = hotelGradeName;
		tvHotelGradeName.setText(mHotelGradeName);
		tvHotelGradeName.requestLayout();
		
	}
	
	private void setHotelGradeColor(String hotelGradeColor) {
		mHotelGradeColor = Color.parseColor(hotelGradeColor);
		setBackgroundColor(mHotelGradeColor);
	}
	
	

}
