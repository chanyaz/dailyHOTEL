package com.twoheart.dailyhotel.screen.booking.detail.gourmet;

import android.content.Context;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ExLog;
import com.daily.dailyhotel.view.DailyBookingProductView;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.GourmetBookingDetail;
import com.twoheart.dailyhotel.model.PlaceBookingDetail;
import com.twoheart.dailyhotel.network.model.TodayDateTime;
import com.twoheart.dailyhotel.place.base.OnBaseEventListener;
import com.twoheart.dailyhotel.place.layout.PlaceReservationDetailLayout;
import com.twoheart.dailyhotel.util.DailyCalendar;
import com.twoheart.dailyhotel.util.Util;

import java.util.Date;
import java.util.Random;

public class GourmetReservationDetailLayout extends PlaceReservationDetailLayout
{
    public GourmetReservationDetailLayout(Context context, OnBaseEventListener listener)
    {
        super(context, listener);
    }

    @Override
    protected void initPlaceInformationLayout(Context context, View view, TodayDateTime todayDateTime, PlaceBookingDetail placeBookingDetail)
    {
        if (context == null || view == null || todayDateTime == null || placeBookingDetail == null)
        {
            return;
        }

        GourmetBookingDetail gourmetBookingDetail = (GourmetBookingDetail) placeBookingDetail;

        // 3일전 부터 몇일 남음 필요.
        View remainedDayLayout = view.findViewById(R.id.remainedDayLayout);
        TextView remainedDayTextView = (TextView) view.findViewById(R.id.remainedDayTextView);
        String remainedDayText;

        try
        {
            Date checkInDate = DailyCalendar.convertStringToDate(gourmetBookingDetail.reservationTime);
            Date currentDate = DailyCalendar.convertStringToDate(todayDateTime.currentDateTime);

            int dayOfDays = (int) ((DailyCalendar.clearTField(checkInDate.getTime()) - DailyCalendar.clearTField(currentDate.getTime())) / DailyCalendar.DAY_MILLISECOND);
            if (dayOfDays < 0 || dayOfDays > 3)
            {
                remainedDayText = null;
            } else if (dayOfDays > 0)
            {
                // 하루이상 남음
                remainedDayText = context.getString(R.string.frag_booking_duedate_formet_gourmet, dayOfDays);
            } else
            {
                // 당일
                remainedDayText = context.getString(R.string.frag_booking_today_type_gourmet);
            }

            if (DailyTextUtils.isTextEmpty(remainedDayText) == true)
            {
                remainedDayLayout.setVisibility(View.GONE);
            } else
            {
                remainedDayLayout.setVisibility(View.VISIBLE);
                remainedDayTextView.setText(remainedDayText);
            }
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }

        TextView gourmetNameTextView = (TextView) view.findViewById(R.id.gourmetNameTextView);
        TextView addressTextView = (TextView) view.findViewById(R.id.addressTextView);

        gourmetNameTextView.setText(gourmetBookingDetail.placeName);
        addressTextView.setText(gourmetBookingDetail.address);

        DailyBookingProductView productView = (DailyBookingProductView) view.findViewById(R.id.productInformationView);

        // TODO : Test Code 서버 연결 작업 후 재 작업 필요.
        int randPersons = new Random(5).nextInt() - 1;
        int tempPrice = gourmetBookingDetail.price;

        productView.addInformation(gourmetBookingDetail.ticketName, gourmetBookingDetail.ticketCount, randPersons, tempPrice);
        // TODO : 임시 두줄
        //        productView.addInformation(gourmetBookingDetail.ticketName +"\n" + gourmetBookingDetail.ticketName, gourmetBookingDetail.ticketCount, randPersons, tempPrice);
    }

    @Override
    protected void initTimeInformationLayout(Context context, View view, PlaceBookingDetail placeBookingDetail)
    {
        if (context == null || view == null || placeBookingDetail == null)
        {
            return;
        }

        GourmetBookingDetail gourmetBookingDetail = (GourmetBookingDetail) placeBookingDetail;

        TextView ticketDateTextView = (TextView) view.findViewById(R.id.ticketDateTextView);
        TextView visitPersonTextView = (TextView) view.findViewById(R.id.visitPersonTextView);

        try
        {
            String ticketDateFormat = DailyCalendar.convertDateFormatString(gourmetBookingDetail.reservationTime, DailyCalendar.ISO_8601_FORMAT, "yyyy.M.d(EEE) HH:mm");
            ticketDateTextView.setText(ticketDateFormat);
        } catch (Exception e)
        {
            ticketDateTextView.setText(null);
        }

        try
        {
            // TODO : 임시 방문인원 - 서버 연결 시 추가 작업 예정
            int persons = new Random(10).nextInt();
            visitPersonTextView.setText(mContext.getString(R.string.label_booking_visit_persons_format, persons));
        } catch (Exception e)
        {
            visitPersonTextView.setText(null);
        }
    }

    @Override
    protected void initGuestInformationLayout(Context context, View view, PlaceBookingDetail placeBookingDetail)
    {
        if (context == null || view == null || placeBookingDetail == null)
        {
            return;
        }

        GourmetBookingDetail gourmetBookingDetail = (GourmetBookingDetail) placeBookingDetail;

        TextView guestNameTextView = (TextView) view.findViewById(R.id.guestNameTextView);
        TextView guestPhoneTextView = (TextView) view.findViewById(R.id.guestPhoneTextView);
        TextView guestEmailTextView = (TextView) view.findViewById(R.id.guestEmailTextView);

        guestNameTextView.setText(gourmetBookingDetail.guestName);
        guestPhoneTextView.setText(Util.addHyphenMobileNumber(mContext, gourmetBookingDetail.guestPhone));
        guestEmailTextView.setText(gourmetBookingDetail.guestEmail);
    }

    @Override
    protected void initPaymentInformationLayout(Context context, View view, PlaceBookingDetail placeBookingDetail)
    {
        if (context == null || view == null || placeBookingDetail == null)
        {
            return;
        }

        GourmetBookingDetail gourmetBookingDetail = (GourmetBookingDetail) placeBookingDetail;

        TextView paymentDateTextView = (TextView) view.findViewById(R.id.paymentDateTextView);
        TextView priceTextView = (TextView) view.findViewById(R.id.priceTextView);

        View bonusLayout = view.findViewById(R.id.bonusLayout);
        View couponLayout = view.findViewById(R.id.couponLayout);
        bonusLayout.setVisibility(View.GONE);

        TextView couponTextView = (TextView) view.findViewById(R.id.couponTextView);
        TextView totalPriceTextView = (TextView) view.findViewById(R.id.totalPriceTextView);

        try
        {
            paymentDateTextView.setText(DailyCalendar.convertDateFormatString(gourmetBookingDetail.paymentDate, DailyCalendar.ISO_8601_FORMAT, "yyyy.MM.dd"));
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }

        priceTextView.setText(DailyTextUtils.getPriceFormat(mContext, gourmetBookingDetail.price, false));

        if (gourmetBookingDetail.coupon > 0)
        {
            couponLayout.setVisibility(View.VISIBLE);
            couponTextView.setText("- " + DailyTextUtils.getPriceFormat(mContext, gourmetBookingDetail.coupon, false));
        } else
        {
            couponLayout.setVisibility(View.GONE);
        }

        totalPriceTextView.setText(DailyTextUtils.getPriceFormat(mContext, gourmetBookingDetail.paymentPrice, false));

        // 영수증 발급
        View confirmView = view.findViewById(R.id.buttonLayout);
        confirmView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((OnEventListener) mOnEventListener).onIssuingReceiptClick();
            }
        });
    }

    @Override
    protected void initRefundPolicyLayout(Context context, View view, PlaceBookingDetail placeBookingDetail)
    {

    }

    @Override
    protected void onScrollChanged(ScrollView scrollView, int l, int t, int oldl, int oldt)
    {

    }
}
