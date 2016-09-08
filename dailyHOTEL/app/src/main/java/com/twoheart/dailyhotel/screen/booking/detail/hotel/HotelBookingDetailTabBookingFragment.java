package com.twoheart.dailyhotel.screen.booking.detail.hotel;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.HotelBookingDetail;
import com.twoheart.dailyhotel.model.PlaceBookingDetail;
import com.twoheart.dailyhotel.place.base.BaseActivity;
import com.twoheart.dailyhotel.place.base.BaseFragment;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.DailyCalendar;
import com.twoheart.dailyhotel.util.EdgeEffectColor;
import com.twoheart.dailyhotel.util.ExLog;
import com.twoheart.dailyhotel.util.Util;

public class HotelBookingDetailTabBookingFragment extends BaseFragment implements Constants
{
    private static final String KEY_BUNDLE_ARGUMENTS_BOOKING_DETAIL = "bookingDetail";
    private static final String KEY_BUNDLE_ARGUMENTS_RESERVATION_INDEX = "reservationIndex";

    private HotelBookingDetail mBookingDetail;
    private int mReservationIndex;

    public static HotelBookingDetailTabBookingFragment newInstance(PlaceBookingDetail bookingDetail, int reservationIndex)
    {
        HotelBookingDetailTabBookingFragment newFragment = new HotelBookingDetailTabBookingFragment();

        //관련 정보는 BookingTabActivity에서 넘겨받음.
        Bundle arguments = new Bundle();
        arguments.putParcelable(KEY_BUNDLE_ARGUMENTS_BOOKING_DETAIL, bookingDetail);
        arguments.putInt(KEY_BUNDLE_ARGUMENTS_RESERVATION_INDEX, reservationIndex);

        newFragment.setArguments(arguments);

        return newFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();

        if (bundle != null)
        {
            mBookingDetail = bundle.getParcelable(KEY_BUNDLE_ARGUMENTS_BOOKING_DETAIL);
            mReservationIndex = bundle.getInt(KEY_BUNDLE_ARGUMENTS_RESERVATION_INDEX);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        BaseActivity baseActivity = (BaseActivity) getActivity();

        if (baseActivity == null)
        {
            return null;
        }

        View view = inflater.inflate(R.layout.fragment_booking_tab_booking, container, false);

        ScrollView scrollLayout = (ScrollView) view.findViewById(R.id.scrollLayout);
        EdgeEffectColor.setEdgeGlowColor(scrollLayout, getResources().getColor(R.color.default_over_scroll_edge));

        initHotelInformationLayout(view, mBookingDetail);
        initCheckInOutInformationLayout(view, mBookingDetail);
        initGuestInformationLayout(view, mBookingDetail);
        initPaymentInformationLayout(view, mBookingDetail);
        initRefundPolicyLayout(view, mBookingDetail);

        // 영수증 발급
        View confirmView = view.findViewById(R.id.buttonLayout);
        confirmView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                BaseActivity baseActivity = (BaseActivity) getActivity();

                if (baseActivity == null)
                {
                    return;
                }

                Intent intent = new Intent(baseActivity, IssuingReceiptActivity.class);
                intent.putExtra(NAME_INTENT_EXTRA_DATA_BOOKINGIDX, mReservationIndex);
                startActivity(intent);
            }
        });

        return view;
    }

    private void initHotelInformationLayout(View view, HotelBookingDetail bookingDetail)
    {
        if (view == null || bookingDetail == null)
        {
            return;
        }

        TextView tvHotelName = (TextView) view.findViewById(R.id.tv_booking_tab_hotel_name);
        TextView tvAddress = (TextView) view.findViewById(R.id.tv_booking_tab_address);
        TextView tvBedtype = (TextView) view.findViewById(R.id.tv_booking_tab_bedtype);

        tvHotelName.setText(bookingDetail.placeName);
        tvAddress.setText(bookingDetail.address);
        tvBedtype.setText(bookingDetail.roomName);
    }

    private void initCheckInOutInformationLayout(View view, HotelBookingDetail bookingDetail)
    {
        if (view == null || bookingDetail == null)
        {
            return;
        }

        TextView tvCheckIn = (TextView) view.findViewById(R.id.tv_booking_tab_checkin);
        TextView tvCheckOut = (TextView) view.findViewById(R.id.tv_booking_tab_checkout);

        try
        {
            // Check In
            //            String checkInDay = Util.simpleDateFormatISO8601toFormat(bookingDetail.checkInDate, "yyyy.MM.dd(EEE) HH:mm");
            String checkInDay = DailyCalendar.convertDateFormatString(bookingDetail.checkInDate, DailyCalendar.ISO_8601_FORMAT, "yyyy.MM.dd(EEE) HH:mm");

            // Check Out
            //            String checkOutDay = Util.simpleDateFormatISO8601toFormat(bookingDetail.checkOutDate, "yyyy.MM.dd(EEE) HH:mm");
            String checkOutDay = DailyCalendar.convertDateFormatString(bookingDetail.checkOutDate, DailyCalendar.ISO_8601_FORMAT, "yyyy.MM.dd(EEE) HH:mm");

            tvCheckIn.setText(checkInDay);
            tvCheckOut.setText(checkOutDay);
        } catch (Exception e)
        {
            tvCheckIn.setText(null);
            tvCheckOut.setText(null);
        }
    }

    private void initGuestInformationLayout(View view, HotelBookingDetail bookingDetail)
    {
        TextView tvCustomerName = (TextView) view.findViewById(R.id.tv_booking_tab_user_name);
        TextView tvCustomerPhone = (TextView) view.findViewById(R.id.tv_booking_tab_user_phone);

        tvCustomerName.setText(bookingDetail.guestName);
        tvCustomerPhone.setText(Util.addHippenMobileNumber(getContext(), bookingDetail.guestPhone));
    }

    private void initPaymentInformationLayout(View view, HotelBookingDetail bookingDetail)
    {
        TextView paymentDateTextView = (TextView) view.findViewById(R.id.paymentDateTextView);
        TextView priceTextView = (TextView) view.findViewById(R.id.priceTextView);

        View bonusLayout = view.findViewById(R.id.bonusLayout);
        View couponLayout = view.findViewById(R.id.couponLayout);
        TextView bonusTextView = (TextView) view.findViewById(R.id.bonusTextView);
        TextView couponTextView = (TextView) view.findViewById(R.id.couponTextView);
        TextView totalPriceTextView = (TextView) view.findViewById(R.id.totalPriceTextView);

        try
        {
            //            paymentDateTextView.setText(Util.simpleDateFormatISO8601toFormat(bookingDetail.paymentDate, "yyyy.MM.dd"));
            paymentDateTextView.setText(DailyCalendar.convertDateFormatString(bookingDetail.paymentDate, DailyCalendar.ISO_8601_FORMAT, "yyyy.MM.dd"));
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }

        priceTextView.setText(Util.getPriceFormat(getContext(), bookingDetail.price, false));


        if (bookingDetail.bonus > 0)
        {
            bonusLayout.setVisibility(View.VISIBLE);
            bonusTextView.setText("- " + Util.getPriceFormat(getContext(), bookingDetail.bonus, false));
        } else
        {
            bonusLayout.setVisibility(View.GONE);
        }

        if (bookingDetail.coupon > 0)
        {
            couponLayout.setVisibility(View.VISIBLE);
            couponTextView.setText("- " + Util.getPriceFormat(getContext(), bookingDetail.coupon, false));
        } else
        {
            couponLayout.setVisibility(View.GONE);
        }

        totalPriceTextView.setText(Util.getPriceFormat(getContext(), bookingDetail.paymentPrice, false));
    }

    private void initRefundPolicyLayout(View view, HotelBookingDetail bookingDetail)
    {
        View refundPolicyLayout = view.findViewById(R.id.refundPolicyLayout);

        if (bookingDetail.isNRD == true)
        {
            refundPolicyLayout.setVisibility(View.VISIBLE);

            TextView refundPolicyTextView = (TextView) refundPolicyLayout.findViewById(R.id.refundPolicyTextView);

            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(getString(R.string.message_booking_refund_product));
            spannableStringBuilder.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.dh_theme_color)), //
                0, 14, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            refundPolicyTextView.setText(spannableStringBuilder);
        } else
        {
            refundPolicyLayout.setVisibility(View.GONE);
        }
    }
}
