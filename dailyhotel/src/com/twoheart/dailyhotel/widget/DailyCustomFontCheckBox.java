package com.twoheart.dailyhotel.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.internal.widget.TintCheckBox;
import android.util.AttributeSet;

public class DailyCustomFontCheckBox extends TintCheckBox
{
	public DailyCustomFontCheckBox(Context context)
	{
		super(context);
	}

	public DailyCustomFontCheckBox(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public DailyCustomFontCheckBox(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	@Override
	public void setTypeface(Typeface tf, int style)
	{
		switch (style)
		{
			case Typeface.NORMAL:
				setTypeface(FontManager.getInstance(getContext().getApplicationContext()).getNormalTypeface());
				//				if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1)
				//				{
				//					int flags = getPaintFlags() & ~Paint.FAKE_BOLD_TEXT_FLAG;
				//
				//					setPaintFlags(flags);
				//				}
				break;

			case Typeface.BOLD:
				setTypeface(FontManager.getInstance(getContext().getApplicationContext()).getBoldTypeface());
				//
				//				if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1)
				//				{
				//					int flags = getPaintFlags() & ~Paint.FAKE_BOLD_TEXT_FLAG;
				//
				//					setPaintFlags(flags | Paint.FAKE_BOLD_TEXT_FLAG);
				//				}
				break;
			case Typeface.ITALIC:
				setTypeface(FontManager.getInstance(getContext().getApplicationContext()).getNormalTypeface());
				//				setTypeface(FontManager.getInstance(getContext().getApplicationContext()).getIM());
				//
				//				if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1)
				//				{
				//					int flags = getPaintFlags() & ~Paint.FAKE_BOLD_TEXT_FLAG;
				//
				//					setPaintFlags(flags);
				//				}
				break;
			case Typeface.BOLD_ITALIC:
				setTypeface(FontManager.getInstance(getContext().getApplicationContext()).getBoldTypeface());
				//				setTypeface(FontManager.getInstance(getContext().getApplicationContext()).getBIM());
				//
				//				if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1)
				//				{
				//					int flags = getPaintFlags() & ~Paint.FAKE_BOLD_TEXT_FLAG;
				//
				//					setPaintFlags(flags | Paint.FAKE_BOLD_TEXT_FLAG);
				//				}
				break;
		}
	}
}
