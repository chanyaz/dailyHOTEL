package com.daily.dailyhotel.screen.home.campaigntag.stay;

import com.daily.base.BaseDialogViewInterface;
import com.twoheart.dailyhotel.model.PlaceViewItem;
import com.twoheart.dailyhotel.model.time.StayBookingDay;

import java.util.ArrayList;

/**
 * Created by iseung-won on 2017. 8. 4..
 */

public interface StayCampaignTagListInterface extends BaseDialogViewInterface
{
//    void setResultCount(int resultCount);

    void setData(ArrayList<PlaceViewItem> placeViewItemList, StayBookingDay stayBookingDay);

    void setCalendarText(String text);
}