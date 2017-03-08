package com.twoheart.dailyhotel.screen.mydaily.stamp;

import android.content.Context;
import android.view.View;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.place.base.BaseLayout;
import com.twoheart.dailyhotel.place.base.OnBaseEventListener;
import com.twoheart.dailyhotel.widget.DailyToolbarLayout;

public class StampLayout extends BaseLayout implements View.OnClickListener
{
    public interface OnEventListener extends OnBaseEventListener
    {
        void onStampHistoryClick();

        void onStampTermsClick();
    }

    public StampLayout(Context context, OnEventListener mOnEventListener)
    {
        super(context, mOnEventListener);
    }

    @Override
    protected void initLayout(View view)
    {
        initToolbar(view);

        View stampHistoryTextView = view.findViewById(R.id.stampHistoryTextView);
        View stampTermsTextView = view.findViewById(R.id.stampTermsTextView);

        stampHistoryTextView.setOnClickListener(this);
        stampTermsTextView.setOnClickListener(this);
    }

    private void initToolbar(View view)
    {
        View toolbar = view.findViewById(R.id.toolbar);

        DailyToolbarLayout dailyToolbarLayout = new DailyToolbarLayout(mContext, toolbar);
        dailyToolbarLayout.initToolbar(mContext.getString(R.string.actionbar_title_stamp), new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mOnEventListener.finish();
            }
        });
    }

    public void setStampDate(String date1, String date2, String date3)
    {

    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.stampHistoryTextView:
                ((OnEventListener)mOnEventListener).onStampHistoryClick();
                break;

            case R.id.stampTermsTextView:
                ((OnEventListener)mOnEventListener).onStampTermsClick();
                break;
        }
    }
}