package com.twoheart.dailyhotel.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

public class LineBreakByWordTextView extends TextView {
	private int mAvailableWidth = 0;
	private Paint mPaint;
	private List<String> mCutStr = new ArrayList<String>();

	public LineBreakByWordTextView(Context context) {
		super(context);
	}

	public LineBreakByWordTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private int setTextInfo(String text, int textWidth, int textHeight) {
		// �׸� ����Ʈ ����
		mPaint = getPaint();
		mPaint.setColor(getTextColors().getDefaultColor());
		mPaint.setTextSize(getTextSize());

		int mTextHeight = textHeight;

		if (textWidth > 0) {
			// �� ����
			mAvailableWidth = textWidth - this.getPaddingLeft() - this.getPaddingRight();

			mCutStr.clear();
			int end = 0;
			do {
				// ���ڰ� width ���� �Ѿ���� üũ
				end = mPaint.breakText(text, true, mAvailableWidth, null);
				if (end > 0) {
					// �ڸ� ���ڿ��� ���ڿ� �迭�� ��� ���´�.
					mCutStr.add(text.substring(0, end));
					// �Ѿ ���� ��� �߶� ������ ����ϵ��� ����
					text = text.substring(end);
					// �������� ���� ����
					if (textHeight == 0) mTextHeight += getLineHeight();
				}
			} while (end > 0);
		}
		mTextHeight += getPaddingTop() + getPaddingBottom();
		return mTextHeight;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// ���� ���� ����
		float height = getPaddingTop() + getLineHeight();
		for (String text : mCutStr) {
			// ĵ������ ���� ���� ��ū ���� �׸���
			canvas.drawText(text, getPaddingLeft(), height, mPaint);
			height += getLineHeight();
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
		int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
		int height = setTextInfo(this.getText().toString(), parentWidth, parentHeight);
		// �θ� ���̰� 0�ΰ�� ���� �׷��� ���̸�ŭ ����� �����...
		if (parentHeight == 0) parentHeight = height;
		this.setMeasuredDimension(parentWidth, parentHeight);
	}

	@Override
	protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
		// ���ڰ� ����Ǿ����� �ٽ� ����
		setTextInfo(text.toString(), this.getWidth(), this.getHeight());
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// ����� ����Ǿ����� �ٽ� ����(���� �����...)
		if (w != oldw) setTextInfo(this.getText().toString(), w, h);
	}
}