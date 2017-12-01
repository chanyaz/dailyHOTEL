package com.daily.dailyhotel.domain;

import com.daily.dailyhotel.entity.Booking;
import com.daily.dailyhotel.entity.BookingCancel;
import com.daily.dailyhotel.entity.GourmetBookingDetail;
import com.daily.dailyhotel.entity.GourmetMultiBookingDetail;
import com.daily.dailyhotel.entity.StayBookingDetail;
import com.daily.dailyhotel.entity.StayOutboundBookingDetail;
import com.daily.dailyhotel.entity.WaitingDeposit;

import java.util.List;

import io.reactivex.Observable;

public interface BookingInterface
{
    // Stay Outbound 예약 리스트
    Observable<List<Booking>> getStayOutboundBookingList();

    // 예약 리스트
    Observable<List<Booking>> getBookingList();

    // 예약 리스트
    Observable<Boolean> getStayOutboundHideBooking(int reservationIndex);

    // 예약 상세
    Observable<StayOutboundBookingDetail> getStayOutboundBookingDetail(int reservationIndex);

    // 스테이 예약 상세
    Observable<StayBookingDetail> getStayBookingDetail(String aggregationId);

    // 스테이 예약 상세
    Observable<StayBookingDetail> getStayBookingDetail(int reservationIndex);

    // 스테이 숨기기
    Observable<Boolean> getStayHiddenBooking(int reservationIndex);

    // 고메 예약 상세
    Observable<GourmetBookingDetail> getGourmetBookingDetail(String aggregationId);

    // 고메 예약 상세
    Observable<GourmetBookingDetail> getGourmetBookingDetail(int reservationIndex);

    // 고메 예약 상세 - 신규 멀티 구매 용
    Observable<GourmetMultiBookingDetail> getGourmetMultiBookingDetail(String aggregationId);

    // 고메 예약 상세 - 신규 멀티 구매 용
    Observable<GourmetMultiBookingDetail> getGourmetMultiBookingDetail(int reservationIndex);

    // 고메 숨기기
    Observable<Boolean> getGourmetHiddenBooking(int reservationIndex);

    // 임금 대기
    Observable<WaitingDeposit> getWaitingDeposit(String aggregationId);

    // 국내 취소내역 리스트
    Observable<List<BookingCancel>> getBookingCancelList();

    // Stay Outbound 취소내역 리스트
    Observable<List<BookingCancel>> getStayOutboundBookingCancelList();
}
