package com.daily.dailyhotel.screen.home.stay.inbound.filter;

import com.daily.base.BaseDialogViewInterface;
import com.daily.dailyhotel.entity.StayFilter;

public interface StayFilterInterface extends BaseDialogViewInterface
{
    void setFirstSortText(String text);

    void setSortLayoutEnabled(boolean enabled);

    void setSortLayout(StayFilter.SortType sortType);

    void setPerson(int person, int personCountOfMax, int personCountOfMin);

    void setBedTypeCheck(int flagBedTypeFilters);

    void setAmenitiesCheck(int flagAmenitiesFilters);

    void setRoomAmenitiesCheck(int flagRoomAmenitiesFilters);

    void setConfirmText(String text);

    void setConfirmEnabled(boolean enabled);
}
