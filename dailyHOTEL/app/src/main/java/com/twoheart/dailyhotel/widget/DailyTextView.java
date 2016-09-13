package com.twoheart.dailyhotel.widget;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.util.Util;

public class DailyTextView extends AppCompatTextView
{
    private int mCurMaxLine = 0;

    public DailyTextView(Context context)
    {
        super(context);

        setDrawableCompat(context, null);
        setFontStyle(context, null);
    }

    public DailyTextView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        setDrawableCompat(context, attrs);
        setFontStyle(context, attrs);
    }

    public DailyTextView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        setDrawableCompat(context, attrs);
        setFontStyle(context, attrs);
    }

    private void setDrawableCompat(Context context, AttributeSet attrs)
    {
        if (context == null || attrs == null)
        {
            return;
        }

        int drawableCompatLeftResId = context.obtainStyledAttributes(attrs, R.styleable.app).getResourceId(R.styleable.app_drawableCompatLeft, 0);
        int drawableCompatTopResId = context.obtainStyledAttributes(attrs, R.styleable.app).getResourceId(R.styleable.app_drawableCompatTop, 0);
        int drawableCompatRightResId = context.obtainStyledAttributes(attrs, R.styleable.app).getResourceId(R.styleable.app_drawableCompatRight, 0);
        int drawableCompatBottomResId = context.obtainStyledAttributes(attrs, R.styleable.app).getResourceId(R.styleable.app_drawableCompatBottom, 0);

        if (drawableCompatLeftResId == 0 && drawableCompatTopResId == 0 && drawableCompatRightResId == 0 && drawableCompatBottomResId == 0)
        {
            return;
        }

        setCompoundDrawablesWithIntrinsicBounds(drawableCompatLeftResId, drawableCompatTopResId, drawableCompatRightResId, drawableCompatBottomResId);
    }

    private void setFontStyle(Context context, AttributeSet attrs)
    {
        int fontStyle = 3;

        Typeface typeface = getTypeface();

        if (typeface != null)
        {
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

    @Override
    public void setCompoundDrawablesWithIntrinsicBounds(int left, int top, int right, int bottom)
    {
        if (Util.isOverAPI21() == true)
        {
            super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
        } else
        {
            Context context = getContext();
            Drawable leftDrawable = null;
            Drawable topDrawable = null;
            Drawable rightDrawable = null;
            Drawable bottomDrawable = null;

            if (left > 0)
            {
                leftDrawable = AppCompatDrawableManager.get().getDrawable(context, left);
            }

            if (top > 0)
            {
                topDrawable = AppCompatDrawableManager.get().getDrawable(context, top);
            }

            if (right > 0)
            {
                rightDrawable = AppCompatDrawableManager.get().getDrawable(context, right);
            }

            if (bottom > 0)
            {
                bottomDrawable = AppCompatDrawableManager.get().getDrawable(context, bottom);
            }

            super.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, topDrawable, rightDrawable, bottomDrawable);
        }
    }

    public void setDrawableVectorTintList(int id)
    {
        Drawable[] drawables = getCompoundDrawables();

        if (drawables == null)
        {
            return;
        }

        for (Drawable drawable : drawables)
        {
            if (drawable == null)
            {
                continue;
            }

            if (drawable instanceof VectorDrawableCompat)
            {
                ((VectorDrawableCompat) drawable).setTintList(getResources().getColorStateList(id));
            } else
            {
                if (Util.isOverAPI21() == true)
                {
                    drawable.setTintList(getResources().getColorStateList(id));
                }
            }
        }
    }
}
