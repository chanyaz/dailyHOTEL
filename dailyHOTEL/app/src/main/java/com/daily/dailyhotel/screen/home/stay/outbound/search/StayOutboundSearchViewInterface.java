package com.daily.dailyhotel.screen.home.stay.outbound.search;

import com.daily.base.BaseDialogViewInterface;
import com.daily.dailyhotel.entity.StayOutboundSuggest;

import java.util.List;

@Deprecated
public interface StayOutboundSearchViewInterface extends BaseDialogViewInterface
{
    void setCalendarText(String calendarText);

    void setSuggest(String suggest);

    void setSearchEnable(boolean enable);

    void setPeopleText(String peopleText);

    void setPopularAreaList(List<StayOutboundSuggest> stayOutboundSuggestList);

    void setPopularAreaVisible(boolean visible);
}
