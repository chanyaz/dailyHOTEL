package com.daily.dailyhotel.screen.booking.detail.gourmet;

import android.location.Location;

import com.daily.dailyhotel.base.BaseBlurViewInterface;
import com.daily.dailyhotel.entity.GourmetBookingDetail;

import io.reactivex.Observable;

public interface GourmetBookingDetailInterface extends BaseBlurViewInterface
{
    void setBookingDetailToolbar();

    void setBookingDetailMapToolbar();

    void setBookingDetail(GourmetBookingDetail gourmetBookingDetail);

    void setRemindDate(String currentDateTime, String bookingDateTime);

    void setBookingDateAndPersons(String ticketDate, int persons);

    void setHiddenBookingVisible(int bookingState);

    void setReviewButtonLayout(String reviewStatus);

    Observable<Boolean> expandMap(double latitude, double longitude);

    Observable<Boolean> collapseMap();

    void setMyLocation(Location location);
}
