package com.twoheart.dailyhotel.view.widget;

import com.twoheart.dailyhotel.R;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

public class DailyCustomFontTextView extends AppCompatTextView
{
	private int mCurMaxLine = 0;

	public DailyCustomFontTextView(Context context)
	{
		super(context);
	}

	public DailyCustomFontTextView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		setFontStyle(context, attrs);
	}

	public DailyCustomFontTextView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);

		setFontStyle(context, attrs);
	}

	private void setFontStyle(Context context, AttributeSet attrs)
	{
		int fontStyle = context.obtainStyledAttributes(attrs, R.styleable.dailyFont).getInt(R.styleable.dailyFont_style, -1);

		//		 <attr name="fontStyle" >
		//	        <enum name="Black" value="0" />
		//	        <enum name="Bold" value="1" />
		//	        <enum name="DemiLight" value="2" />
		//	        <enum name="Light" value="3" />
		//	        <enum name="Medium" value="4" />
		//	        <enum name="Regular" value="5" />
		//	        <enum name="Thin" value="6" />
		//	    </attr>

		switch (fontStyle)
		{
			// Black
			case 0:
				setTypeface(FontManager.getInstance(context).getBlackTypeface());
				break;

			// Bold
			case 1:
				setTypeface(FontManager.getInstance(context).getBoldTypeface());
				break;

			// DemiLight
			case 2:
				setTypeface(FontManager.getInstance(context).getDemiLightTypeface());
				break;

			// Light
			case 3:
				setTypeface(FontManager.getInstance(context).getLightTypeface());
				break;

			// Medium
			case 4:
				setTypeface(FontManager.getInstance(context).getMediumTypeface());
				break;

			// Regular
			case 5:
				setTypeface(FontManager.getInstance(context).getRegularTypeface());
				break;

			// Thin
			case 6:
				setTypeface(FontManager.getInstance(context).getThinTypeface());
				break;
		}
	}

	public int getCurrentMaxLines()
	{
		return mCurMaxLine;
	}

	@Override
	public void setMaxLines(int maxlines)
	{
		mCurMaxLine = maxlines;
		super.setMaxLines(maxlines);
	}

	@Override
	public void setTypeface(Typeface typeface, int style)
	{
		switch (style)
		{
			case Typeface.NORMAL:
				setTypeface(FontManager.getInstance(getContext()).getRegularTypeface());
				break;
			case Typeface.BOLD:
				setTypeface(FontManager.getInstance(getContext()).getBoldTypeface());
				break;
			case Typeface.ITALIC:
				setTypeface(FontManager.getInstance(getContext()).getRegularTypeface());
				break;
			case Typeface.BOLD_ITALIC:
				setTypeface(FontManager.getInstance(getContext()).getBoldTypeface());
				break;
		}
	}

	@Override
	public void setTypeface(Typeface typeface)
	{
		setPaintFlags(getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
		super.setTypeface(typeface);
	}
}
