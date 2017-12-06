package com.daily.dailyhotel.screen.home.stay.outbound.calendar;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;

import com.daily.base.BaseActivity;
import com.daily.base.util.ScreenUtils;
import com.daily.dailyhotel.entity.ObjectItem;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.databinding.ActivityCalendarDataBinding;

import java.util.List;

import io.reactivex.Observable;

public class StayOutboundCalendarView extends PlaceCalendarView<StayOutboundCalendarView.OnEventListener, ActivityCalendarDataBinding> implements StayOutboundCalendarViewInterface
{
    private StayOutboundCalendarAdapter mCalendarAdapter;

    public interface OnEventListener extends PlaceCalendarView.OnEventListener
    {
        void onDayClick(PlaceCalendarPresenter.Day day);

        void onConfirmClick();
    }

    public StayOutboundCalendarView(BaseActivity baseActivity, StayOutboundCalendarView.OnEventListener listener)
    {
        super(baseActivity, listener);
    }

    @Override
    protected void setContentView(final ActivityCalendarDataBinding viewDataBinding)
    {
        if (viewDataBinding == null)
        {
            return;
        }

        super.setContentView(viewDataBinding);


    }

    @Override
    public void setToolbarTitle(String title)
    {
        super.setToolbarTitle(title);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.closeView:
            case R.id.exitView:
                getEventListener().onBackClick();
                break;

            case R.id.confirmView:
                getEventListener().onConfirmClick();
                break;

            default:
                getEventListener().onDayClick((PlaceCalendarPresenter.Day) v.getTag());
                break;
        }
    }

    @Override
    public void setCalendarList(List<ObjectItem> calendarList)
    {
        if (getViewDataBinding() == null || calendarList == null)
        {
            return;
        }

        if (mCalendarAdapter == null)
        {
            mCalendarAdapter = new StayOutboundCalendarAdapter(getContext(), calendarList);
            mCalendarAdapter.setOnClickListener(view -> getEventListener().onDayClick((PlaceCalendarPresenter.Day) view.getTag()));


            getViewDataBinding().calendarRecyclerView.setAdapter(mCalendarAdapter);
        }

        mCalendarAdapter.setAll(calendarList);
    }

    @Override
    public void scrollMonthPosition(int year, int month)
    {
        if (getViewDataBinding() == null || mCalendarAdapter == null)
        {
            return;
        }

        int position = mCalendarAdapter.getMonthPosition(year, month);
        ((LinearLayoutManager)getViewDataBinding().calendarRecyclerView.getLayoutManager()).scrollToPositionWithOffset(position, 0);
    }

    @Override
    public void notifyCalendarDataSetChanged()
    {
        if (getViewDataBinding() == null || mCalendarAdapter == null)
        {
            return;
        }

        mCalendarAdapter.notifyDataSetChanged();
    }

    @Override
    public Observable<Boolean> showAnimation()
    {
        return super.showAnimation();
    }

    @Override
    public Observable<Boolean> hideAnimation()
    {
        return super.hideAnimation();
    }

    @Override
    public void setVisibility(boolean visibility)
    {
        super.setVisibility(visibility);
    }

    @Override
    public void setCheckInDay(int checkInDay)
    {
        if (mCalendarAdapter == null)
        {
            return;
        }

        mCalendarAdapter.setCheckInDay(checkInDay);
    }

    @Override
    public void setCheckOutDay(int checkOutDay)
    {
        if (mCalendarAdapter == null)
        {
            return;
        }

        mCalendarAdapter.setCheckOutDay(checkOutDay);
    }

    @Override
    public void setLastDayEnabled(boolean enabled)
    {
        if (getViewDataBinding() == null || mCalendarAdapter == null)
        {
            return;
        }

        mCalendarAdapter.setLastDayEnabled(enabled);
    }

    @Override
    public void setConfirmEnabled(boolean enabled)
    {
        super.setConfirmEnabled(enabled);
    }

    @Override
    public void setConfirmText(String text)
    {
        super.setConfirmText(text);
    }

    @Override
    public void setMarginTop(int marginTop)
    {
        if (getViewDataBinding() == null)
        {
            return;
        }

        final int DEFAULT_HEIGHT = ScreenUtils.dpToPx(getContext(), 92);

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) getViewDataBinding().exitView.getLayoutParams();

        if (layoutParams == null)
        {
            layoutParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DEFAULT_HEIGHT);
        }

        layoutParams.height = DEFAULT_HEIGHT + marginTop;

        getViewDataBinding().exitView.setLayoutParams(layoutParams);
    }
}
