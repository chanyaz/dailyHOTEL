/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twoheart.dailyhotel.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.twoheart.dailyhotel.R;

public class HotelIconGenerator
{
	private static final float TEXT_SIZE_DP = 14.0f;
	private static final float SELECTED_TEXT_SIZE_DP = 16.0f;

	private ViewGroup mContainer;
	private TextView mTextView;

	/**
	 * Creates a new IconGenerator with the default style.
	 */
	public HotelIconGenerator(Context context)
	{
		mContainer = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.marker_hotel, null);
		mTextView = (TextView) mContainer.findViewById(R.id.text);
	}

	/**
	 * Sets the text content, then creates an icon with the current style.
	 *
	 * @param text
	 *            the text content to display inside the icon.
	 */
	public Bitmap makeIcon(String text, int drawableResId)
	{
		if (mTextView != null)
		{
			mTextView.setText(text);
			mTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DP);
			mTextView.setTypeface(mTextView.getTypeface(), Typeface.NORMAL);
			mTextView.setBackgroundResource(drawableResId);
		}

		return makeIcon(drawableResId);
	}

	public Bitmap makeSelectedIcon(String text, int drawableResId)
	{
		if (mTextView != null)
		{
			mTextView.setText(text);
			mTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, SELECTED_TEXT_SIZE_DP);
			mTextView.setTypeface(mTextView.getTypeface(), Typeface.BOLD);
			mTextView.setBackgroundResource(drawableResId);
		}

		return makeIcon(drawableResId);
	}

	/**
	 * Creates an icon with the current content and style.
	 * <p/>
	 * This method is useful if a custom view has previously been set, or if
	 * text content is not applicable.
	 */
	public Bitmap makeIcon(int drawableResId)
	{
		int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		mContainer.measure(measureSpec, measureSpec);

		int measuredWidth = mContainer.getMeasuredWidth();
		int measuredHeight = mContainer.getMeasuredHeight();

		mContainer.layout(0, 0, measuredWidth, measuredHeight);

		Bitmap bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);

		mContainer.draw(canvas);
		return bitmap;
	}

	/**
	 * Sets the text color, size, style, hint color, and highlight color from
	 * the specified <code>TextAppearance</code> resource.
	 *
	 * @param resid
	 *            the identifier of the resource.
	 */
	public void setTextAppearance(Context context, int resid)
	{
		if (mTextView != null)
		{
			mTextView.setTextAppearance(context, resid);
		}
	}

	public void setTextColor(int resid)
	{
		if (mTextView != null)
		{
			mTextView.setTextColor(resid);
		}
	}
}
