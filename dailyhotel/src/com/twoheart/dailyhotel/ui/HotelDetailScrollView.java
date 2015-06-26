package com.twoheart.dailyhotel.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class HotelDetailScrollView extends ScrollView
{
	private boolean mScrollable = true;

	public HotelDetailScrollView(Context context)
	{
		super(context);
		
		initLayout(context);
	}

	public HotelDetailScrollView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
		initLayout(context);
	}

	public HotelDetailScrollView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		
		initLayout(context);
	}

	public HotelDetailScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
	{
		super(context, attrs, defStyleAttr, defStyleRes);
		
		initLayout(context);
	}
	
	private void initLayout(Context context)
	{
	}

	public void setScrollEnabled(boolean enable)
	{
		mScrollable = enable;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
		if (mScrollable == false)
		{
			return false;
		} else
		{
			return super.onInterceptTouchEvent(ev);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if (mScrollable == false)
		{
			return false;
		}

		return super.onTouchEvent(event);
	}
}
