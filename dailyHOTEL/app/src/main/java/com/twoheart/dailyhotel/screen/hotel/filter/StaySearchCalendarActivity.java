package com.twoheart.dailyhotel.screen.hotel.filter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ExLog;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Keyword;
import com.twoheart.dailyhotel.model.time.PlaceBookingDay;
import com.twoheart.dailyhotel.model.time.StayBookingDay;
import com.twoheart.dailyhotel.network.model.TodayDateTime;
import com.twoheart.dailyhotel.util.Constants;

public class StaySearchCalendarActivity extends StayCalendarActivity
{
    public static final String INTENT_EXTRA_DATA_SEARCH_TYPE = "searchType";
    public static final String INTENT_EXTRA_DATA_SEARCH_INPUT_TEXT = "inputText";
    public static final String INTENT_EXTRA_DATA_SEARCH_KEYWORD = "searchKeyword";

    private Constants.SearchType mSearchType;
    private String mInputText;
    private Keyword mSearchKeyword;

    /**
     * @param context
     * @param todayDateTime
     * @param stayBookingDay
     * @param screen
     * @param isSelected
     * @param isAnimation
     * @return
     */
    public static Intent newInstance(Context context, TodayDateTime todayDateTime //
        , StayBookingDay stayBookingDay, int dayOfMaxCount, String screen, boolean isSelected //
        , boolean isAnimation, Constants.SearchType searchType, String inputText, Keyword keyword)
    {
        Intent intent = new Intent(context, StaySearchCalendarActivity.class);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_TODAYDATETIME, todayDateTime);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_PLACEBOOKINGDAY, stayBookingDay);
        intent.putExtra(INTENT_EXTRA_DATA_SCREEN, screen);
        intent.putExtra(INTENT_EXTRA_DATA_ISSELECTED, isSelected);
        intent.putExtra(INTENT_EXTRA_DATA_ANIMATION, isAnimation);
        intent.putExtra(INTENT_EXTRA_DATA_DAY_OF_MAXCOUNT, dayOfMaxCount);
        intent.putExtra(INTENT_EXTRA_DATA_SEARCH_TYPE, searchType == null ? null : searchType.name());
        intent.putExtra(INTENT_EXTRA_DATA_SEARCH_INPUT_TEXT, inputText);
        intent.putExtra(INTENT_EXTRA_DATA_SEARCH_KEYWORD, keyword);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Intent intent = getIntent();
        try {
            String searchType = intent.getStringExtra(INTENT_EXTRA_DATA_SEARCH_TYPE);
            if (DailyTextUtils.isTextEmpty(searchType) == false)
            {
                mSearchType = SearchType.valueOf(searchType);
            }
        } catch (Exception e)
        {
            ExLog.d(e.toString());
            mSearchType = null;
        }

        mInputText = intent.getStringExtra(INTENT_EXTRA_DATA_SEARCH_INPUT_TEXT);
        mSearchKeyword = intent.getParcelableExtra(INTENT_EXTRA_DATA_SEARCH_KEYWORD);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setResult(int resultCode, PlaceBookingDay placeBookingDay)
    {
        Intent intent = new Intent();
        intent.putExtra(NAME_INTENT_EXTRA_DATA_PLACEBOOKINGDAY, placeBookingDay);
        intent.putExtra(INTENT_EXTRA_DATA_SEARCH_TYPE, mSearchType.name());
        intent.putExtra(INTENT_EXTRA_DATA_SEARCH_INPUT_TEXT, mInputText);
        intent.putExtra(INTENT_EXTRA_DATA_SEARCH_KEYWORD, mSearchKeyword);

        setResult(resultCode, intent);
    }

    @Override
    protected String getConfirmText(int nights)
    {
        return getString(mSearchType == null //
            ? R.string.label_calendar_stay_search_selected_date //
            : R.string.label_calendar_stay_search_selected_date_after_search //
            , nights);
    }
}