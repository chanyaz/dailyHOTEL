package com.twoheart.dailyhotel.screen.gourmet.filter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.SaleTime;
import com.twoheart.dailyhotel.place.activity.PlaceCalendarActivity;
import com.twoheart.dailyhotel.util.DailyCalendar;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GourmetCalendarActivity extends PlaceCalendarActivity
{
    private static final int DAYCOUNT_OF_MAX = 30;
    private static final int ENABLE_DAYCOUNT_OF_MAX = 14;

    private View mDayView;
    private TextView mConfirmTextView;
    protected String mCallByScreen;

    private boolean mIsAnimation;
    protected boolean mIsChanged;

    public static Intent newInstance(Context context, SaleTime saleTime, String screen, boolean isSelected, boolean isAnimation)
    {
        Intent intent = new Intent(context, GourmetCalendarActivity.class);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_SALETIME, saleTime);
        intent.putExtra(INTENT_EXTRA_DATA_SCREEN, screen);
        intent.putExtra(INTENT_EXTRA_DATA_ISSELECTED, isSelected);
        intent.putExtra(INTENT_EXTRA_DATA_ANIMATION, isAnimation);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        SaleTime saleTime = intent.getParcelableExtra(NAME_INTENT_EXTRA_DATA_SALETIME);
        mCallByScreen = intent.getStringExtra(INTENT_EXTRA_DATA_SCREEN);
        boolean isSelected = intent.getBooleanExtra(INTENT_EXTRA_DATA_ISSELECTED, true);
        mIsAnimation = intent.getBooleanExtra(INTENT_EXTRA_DATA_ANIMATION, false);

        if (saleTime == null)
        {
            Util.restartApp(this);
            return;
        }

        initLayout(R.layout.activity_calendar, saleTime.getClone(0), ENABLE_DAYCOUNT_OF_MAX, DAYCOUNT_OF_MAX);
        initToolbar(getString(R.string.label_calendar_gourmet_select));

        reset();

        if (isSelected == true)
        {
            setSelectedDay(saleTime);
        }

        if (mIsAnimation == true)
        {
            mAnimationLayout.setVisibility(View.INVISIBLE);
            mAnimationLayout.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    showAnimation();
                }
            }, 50);
        } else
        {
            setTouchEnabled(true);
        }
    }

    @Override
    protected void initLayout(int layoutResID, SaleTime dailyTime, int enableDayCountOfMax, int dayCountOfMax)
    {
        super.initLayout(layoutResID, dailyTime, enableDayCountOfMax, dayCountOfMax);

        mConfirmTextView = (TextView) findViewById(R.id.confirmView);
        mConfirmTextView.setVisibility(View.VISIBLE);
        mConfirmTextView.setOnClickListener(this);
        mConfirmTextView.setEnabled(false);
        mConfirmTextView.setText(R.string.label_calendar_gourmet_search_selected_date);

        if (AnalyticsManager.ValueType.SEARCH.equalsIgnoreCase(mCallByScreen) == true)
        {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, Util.dpToPx(this, 133));
            mExitView.setLayoutParams(layoutParams);
        }
    }

    @Override
    protected void onStart()
    {
        AnalyticsManager.getInstance(this).recordScreen(AnalyticsManager.Screen.DAILYGOURMET_LIST_CALENDAR);

        super.onStart();
    }

    @Override
    public void finish()
    {
        super.finish();

        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed()
    {
        // 일단은 애니메이션으로 검색 선택시에 Analytics를 구분하도록 한다.
        AnalyticsManager.getInstance(this).recordEvent(AnalyticsManager.Category.NAVIGATION//
            , AnalyticsManager.Action.GOURMET_BOOKING_CALENDAR_CLOSED, mCallByScreen, null);

        hideAnimation();
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.exitView:
            case R.id.closeView:

                AnalyticsManager.getInstance(this).recordEvent(AnalyticsManager.Category.NAVIGATION//
                    , AnalyticsManager.Action.GOURMET_BOOKING_CALENDAR_CLOSED, mCallByScreen, null);

                hideAnimation();
                break;

            case R.id.confirmView:
            {
                Day day = (Day) mDayView.getTag();

                onConfirm(day.dayTime);
                break;
            }

            default:
            {
                Day day = (Day) view.getTag();

                if (day == null)
                {
                    return;
                }

                if (lockUiComponentAndIsLockUiComponent() == true)
                {
                    return;
                }

                if (mDayView != null)
                {
                    reset();
                }

                mDayView = view;
                setSelectedDay(mDayView);

                view.setSelected(true);
                setToolbarText(day.dayTime.getDayOfDaysDateFormat("yyyy.MM.dd(EEE)"));

                mConfirmTextView.setEnabled(true);

                releaseUiComponent();
                break;
            }
        }
    }

    protected void onConfirm(SaleTime saleTime)
    {
        if (saleTime == null)
        {
            return;
        }

        if (lockUiComponentAndIsLockUiComponent() == true)
        {
            return;
        }

        String date = saleTime.getDayOfDaysDateFormat("yyyy.MM.dd(EEE)");

        Map<String, String> params = new HashMap<>();
        params.put(AnalyticsManager.KeyType.VISIT_DATE, Long.toString(saleTime.getDayOfDaysDate().getTime()));
        params.put(AnalyticsManager.KeyType.SCREEN, mCallByScreen);

        //        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd(EEE) HH시 mm분");
        //        String phoneDate = simpleDateFormat.format(new Date());

        AnalyticsManager.getInstance(this).recordEvent(AnalyticsManager.Category.NAVIGATION, AnalyticsManager.Action.GOURMET_BOOKING_DATE_CLICKED//
            , (mIsChanged ? AnalyticsManager.ValueType.CHANGED : //
                AnalyticsManager.ValueType.NONE) + "-" + date + "-" + DailyCalendar.format(new Date(), "yyyy.MM.dd(EEE) HH시 mm분"), params);

        Intent intent = new Intent();
        intent.putExtra(NAME_INTENT_EXTRA_DATA_SALETIME, saleTime);

        setResult(RESULT_OK, intent);
        hideAnimation();
    }

    private void setSelectedDay(View view)
    {
        if (view == null)
        {
            return;
        }

        TextView textView = (TextView) view.findViewById(R.id.textView);
        textView.setText(R.string.label_visit_day);
        textView.setVisibility(View.VISIBLE);

        view.setBackgroundResource(R.drawable.select_date_gourmet);
    }

    private void resetSelectedDay(View view)
    {
        if (view == null)
        {
            return;
        }

        TextView textView = (TextView) view.findViewById(R.id.textView);
        textView.setText(null);
        textView.setVisibility(View.INVISIBLE);

        view.setBackgroundDrawable(getResources().getDrawable(R.drawable.selector_calendar_day_background));
    }

    private void setSelectedDay(SaleTime saleTime)
    {
        if (saleTime == null)
        {
            return;
        }

        for (View dayView : mDailyViews)
        {
            Day day = (Day) dayView.getTag();

            if (saleTime.isDayOfDaysDateEquals(day.dayTime) == true)
            {
                dayView.performClick();
                break;
            }
        }
    }

    private void reset()
    {
        mIsChanged = true;

        if (mDayView != null)
        {
            resetSelectedDay(mDayView);
            mDayView = null;
        }

        int length = mDailyViews.length;

        for (int i = 0; i < length; i++)
        {
            if (i < ENABLE_DAYCOUNT_OF_MAX)
            {
                mDailyViews[i].setEnabled(true);
            } else
            {
                mDailyViews[i].setEnabled(false);
            }

            mDailyViews[i].setSelected(false);
        }

        setToolbarText(getString(R.string.label_calendar_gourmet_select));
        mConfirmTextView.setEnabled(false);
    }
}
