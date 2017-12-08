package com.daily.dailyhotel.repository.local.model;

import android.net.Uri;

import com.daily.dailyhotel.domain.StayObRecentlySuggestColumns;

/**
 * Created by android_sam on 2017. 9. 11..
 */

public class StayObRecentlySuggestList implements StayObRecentlySuggestColumns
{
    private static final String AUTHORITY = "com.twoheart.dailyhotel.DailyContentProvider";

    public static final Uri NOTIFICATION_URI = Uri.parse("content://" + AUTHORITY + "/suggest");
}
