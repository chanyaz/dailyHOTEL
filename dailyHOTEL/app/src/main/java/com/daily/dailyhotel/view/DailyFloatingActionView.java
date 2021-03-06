package com.daily.dailyhotel.view;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.daily.base.util.ScreenUtils;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.databinding.DailyViewFloatingActionDataBinding;

public class DailyFloatingActionView extends ConstraintLayout
{
    private DailyViewFloatingActionDataBinding mViewDataBinding;
    private boolean mViewOptionMapEnabled;
    private boolean mViewOptionListEnabled;
    private ViewOption mViewOption;

    public enum ViewOption
    {
        LIST,
        MAP,
    }

    public DailyFloatingActionView(Context context)
    {
        super(context);

        initLayout(context);
    }

    public DailyFloatingActionView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        initLayout(context);
    }

    public DailyFloatingActionView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        initLayout(context);
    }

    private void initLayout(Context context)
    {
        mViewDataBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.daily_view_floating_action_data, this, true);

        setBackgroundResource(R.drawable.fab);

        setViewOptionEnabled(true);

        setViewOption(ViewOption.LIST);
    }

    public void setOnViewOptionClickListener(OnClickListener listener)
    {
        if (mViewDataBinding == null)
        {
            return;
        }

        mViewDataBinding.viewActionTextView.setOnClickListener(listener);
    }

    public void setOnFilterOptionClickListener(OnClickListener listener)
    {
        if (mViewDataBinding == null)
        {
            return;
        }

        mViewDataBinding.filterActionTextView.setOnClickListener(listener);
    }

    public void setViewOptionEnabled(boolean enabled)
    {
        if (mViewDataBinding == null)
        {
            return;
        }

        mViewOptionListEnabled = enabled;
        mViewOptionMapEnabled = enabled;

        mViewDataBinding.viewActionTextView.setEnabled(enabled);
        mViewDataBinding.viewActionTextView.setAlpha(enabled ? 1.0f : 0.2f);
    }

    public void setViewOptionMapEnabled(boolean enabled)
    {
        if (mViewDataBinding == null)
        {
            return;
        }

        mViewOptionMapEnabled = enabled;

        if (mViewOption == ViewOption.MAP)
        {
            mViewDataBinding.viewActionTextView.setEnabled(enabled);
            mViewDataBinding.viewActionTextView.setAlpha(enabled ? 1.0f : 0.2f);
        }
    }

    public void setViewOptionVisible(boolean visible)
    {
        if (mViewDataBinding == null)
        {
            return;
        }

        final int DP_10 = ScreenUtils.dpToPx(getContext(), 10);

        if (visible == true)
        {
            mViewDataBinding.viewActionTextView.setVisibility(View.VISIBLE);
            mViewDataBinding.verticalLine.setVisibility(View.VISIBLE);

            mViewDataBinding.filterActionTextView.setPadding(ScreenUtils.dpToPx(getContext(), 12), DP_10, ScreenUtils.dpToPx(getContext(), 18), DP_10);
        } else
        {
            mViewDataBinding.viewActionTextView.setVisibility(View.GONE);
            mViewDataBinding.verticalLine.setVisibility(View.GONE);

            final int DP_18 = ScreenUtils.dpToPx(getContext(), 18);

            mViewDataBinding.filterActionTextView.setPadding(DP_18, DP_10, DP_18, DP_10);
        }
    }

    public void setFilterOptionEnable(boolean enable)
    {
        if (mViewDataBinding == null)
        {
            return;
        }

        mViewDataBinding.filterActionTextView.setEnabled(enable);

        float alpha = enable ? 1.0f : 0.2f;

        mViewDataBinding.filterActionTextView.setAlpha(alpha);
        mViewDataBinding.filterOnView.setAlpha(alpha);
    }

    //    public void setViewOptionListSelected()
    //    {
    //        if (mViewDataBinding == null)
    //        {
    //            return;
    //        }
    //
    //        mViewType = StayTabPresenter.ViewType.LIST;
    //
    //        mViewDataBinding.viewActionTextView.setText(R.string.label_list);
    //        mViewDataBinding.viewActionTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.vector_ic_fab_02_list, 0, 0, 0);
    //
    //        mViewDataBinding.viewActionTextView.setEnabled(mViewOptionListEnabled);
    //        mViewDataBinding.viewActionTextView.setAlpha(mViewOptionListEnabled ? 1.0f : 0.2f);
    //    }
    //
    //    public void setViewOptionMapSelected()
    //    {
    //        if (mViewDataBinding == null)
    //        {
    //            return;
    //        }
    //
    //        mViewType = StayTabPresenter.ViewType.MAP;
    //
    //        mViewDataBinding.viewActionTextView.setText(R.string.label_map);
    //        mViewDataBinding.viewActionTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.vector_ic_fab_01_map, 0, 0, 0);
    //
    //        mViewDataBinding.viewActionTextView.setEnabled(mViewOptionMapEnabled);
    //        mViewDataBinding.viewActionTextView.setAlpha(mViewOptionMapEnabled ? 1.0f : 0.2f);
    //    }

    public void setViewOption(ViewOption viewOption)
    {
        if (viewOption == null)
        {
            return;
        }

        mViewOption = viewOption;

        switch (viewOption)
        {
            case LIST:
                mViewDataBinding.viewActionTextView.setText(R.string.label_list);
                mViewDataBinding.viewActionTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.vector_ic_fab_02_list, 0, 0, 0);

                mViewDataBinding.viewActionTextView.setEnabled(mViewOptionListEnabled);
                mViewDataBinding.viewActionTextView.setAlpha(mViewOptionListEnabled ? 1.0f : 0.2f);
                break;

            case MAP:
                mViewDataBinding.viewActionTextView.setText(R.string.label_map);
                mViewDataBinding.viewActionTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.vector_ic_fab_01_map, 0, 0, 0);

                mViewDataBinding.viewActionTextView.setEnabled(mViewOptionMapEnabled);
                mViewDataBinding.viewActionTextView.setAlpha(mViewOptionMapEnabled ? 1.0f : 0.2f);
                break;
        }
    }

    public void setFilterOptionSelected(boolean selected)
    {
        if (mViewDataBinding == null)
        {
            return;
        }

        mViewDataBinding.filterActionTextView.setSelected(selected);
        mViewDataBinding.filterOnView.setVisibility(selected ? VISIBLE : INVISIBLE);
    }
}
