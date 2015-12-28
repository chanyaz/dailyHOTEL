package com.twoheart.dailyhotel.view.widget;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.twoheart.dailyhotel.R;

public class DailyTextView extends AppCompatTextView
{
    private int mCurMaxLine = 0;

    public DailyTextView(Context context)
    {
        super(context);

        setFontStyle(context, null);
    }

    public DailyTextView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        setFontStyle(context, attrs);
    }

    public DailyTextView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        setFontStyle(context, attrs);
    }

    private void setFontStyle(Context context, AttributeSet attrs)
    {
        int fontStyle = 3;

        boolean isBold = getTypeface().isBold();

        if (isBold == true)
        {
            fontStyle = 0;
        } else
        {
            if (attrs != null)
            {
                fontStyle = context.obtainStyledAttributes(attrs, R.styleable.dailyFont).getInt(R.styleable.dailyFont_style, 3);
            }
        }

        //		 <attr name="fontStyle" >
        //	        <enum name="Bold" value="0" />
        //	        <enum name="DemiLight" value="1" />
        //	        <enum name="Medium" value="2 />
        //	        <enum name="Regular" value="3" />
        //	    </attr>

        switch (fontStyle)
        {
            // Bold
            case 0:
                setTypeface(FontManager.getInstance(context).getBoldTypeface());
                break;

            // DemiLight
            case 1:
                setTypeface(FontManager.getInstance(context).getDemiLightTypeface());
                break;

            // Medium
            case 2:
                setTypeface(FontManager.getInstance(context).getMediumTypeface());
                break;

            // Regular
            case 3:
                setTypeface(FontManager.getInstance(context).getRegularTypeface());
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
