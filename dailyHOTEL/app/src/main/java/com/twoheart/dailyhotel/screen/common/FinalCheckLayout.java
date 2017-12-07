package com.twoheart.dailyhotel.screen.common;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daily.base.util.ScreenUtils;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.widget.DailySignatureView;

/**
 * 신용카드 Final Check
 *
 * @author sheldon
 */
public class FinalCheckLayout extends FrameLayout
{
    private DailySignatureView mDailySignatureView;
    private ViewGroup mMessageLayout;

    public FinalCheckLayout(Context context)
    {
        super(context);

        initLayout(context);
    }

    public FinalCheckLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        initLayout(context);
    }

    public FinalCheckLayout(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        initLayout(context);
    }

    public FinalCheckLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);

        initLayout(context);
    }

    private void initLayout(Context context)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_finalcheck, this, true);
        mMessageLayout = view.findViewById(R.id.messageLayout);
        mDailySignatureView = view.findViewById(R.id.signatureView);
    }

    public void setMessages(int[] textResIds)
    {
        if (textResIds == null)
        {
            return;
        }

        Context context = getContext();

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int length = textResIds.length;

        for (int i = 0; i < length; i++)
        {
            View messageRow = inflater.inflate(R.layout.row_payment_agreedialog, mMessageLayout, false);

            TextView messageTextView = messageRow.findViewById(R.id.messageTextView);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            if (i == length - 1)
            {
                layoutParams.setMargins(ScreenUtils.dpToPx(context, 5), 0, 0, 0);
            } else
            {
                layoutParams.setMargins(ScreenUtils.dpToPx(context, 5), 0, 0, ScreenUtils.dpToPx(context, 10));
            }

            messageTextView.setLayoutParams(layoutParams);

            String message = context.getString(textResIds[i]);

            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(message);

            for (int startIndex = spannableStringBuilder.toString().indexOf("<b>"); startIndex >= 0; startIndex = spannableStringBuilder.toString().indexOf("<b>"))
            {
                spannableStringBuilder.delete(startIndex, startIndex + "<b>".length());

                int endIndex = spannableStringBuilder.toString().indexOf("</b>");

                spannableStringBuilder.delete(endIndex, endIndex + "</b>".length());

                spannableStringBuilder.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.dh_theme_color)), //
                    startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableStringBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), //
                    startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            messageTextView.setText(spannableStringBuilder);

            mMessageLayout.addView(messageRow);
        }
    }

    public DailySignatureView getDailySignatureView()
    {
        return mDailySignatureView;
    }

    public void setOnUserActionListener(DailySignatureView.OnUserActionListener listener)
    {
        if (mDailySignatureView != null)
        {
            mDailySignatureView.setOnUserActionListener(listener);
        }
    }
}