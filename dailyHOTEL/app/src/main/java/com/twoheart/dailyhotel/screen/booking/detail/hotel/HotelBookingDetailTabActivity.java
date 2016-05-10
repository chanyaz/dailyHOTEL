/**
 * Copyright (c) 2014 Daily Co., Ltd. All rights reserved.
 * <p>
 * HotelBookingDetailTabActivity (예약한 호텔의 예약, 정보, 지도탭을 보여주는 화면)
 * <p>
 * 예약한 호텔리스트에서 호텔 클릭 시 호텔의 정보들을 보여주는 화면이다.
 * 예약, 정보, 지도 프래그먼트를 담고 있는 액티비티이다.
 */
package com.twoheart.dailyhotel.screen.booking.detail.hotel;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.HotelBookingDetail;
import com.twoheart.dailyhotel.model.PlaceBookingDetail;
import com.twoheart.dailyhotel.network.DailyNetworkAPI;
import com.twoheart.dailyhotel.network.response.DailyHotelJsonResponseListener;
import com.twoheart.dailyhotel.place.activity.PlaceBookingDetailTabActivity;
import com.twoheart.dailyhotel.place.base.BaseFragment;
import com.twoheart.dailyhotel.screen.booking.detail.BookingDetailFragmentPagerAdapter;
import com.twoheart.dailyhotel.util.DailyPreference;
import com.twoheart.dailyhotel.util.ExLog;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;
import com.twoheart.dailyhotel.widget.DailyToast;
import com.twoheart.dailyhotel.widget.FontManager;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class HotelBookingDetailTabActivity extends PlaceBookingDetailTabActivity
{
    public HotelBookingDetail mHotelBookingDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mHotelBookingDetail = new HotelBookingDetail();
    }

    @Override
    protected void loadFragments(ViewPager viewPager, PlaceBookingDetail placeBookingDetail)
    {
        String tag = (String) viewPager.getTag();

        if (tag != null)
        {
            return;
        }

        viewPager.setTag("HotelBookingDetailTabActivity");

        ArrayList<BaseFragment> fragmentList = new ArrayList<>();

        BaseFragment baseFragment01 = HotelBookingDetailTabBookingFragment.newInstance(placeBookingDetail, mBooking.reservationIndex, mBooking.isUsed);
        fragmentList.add(baseFragment01);

        BaseFragment baseFragment02 = HotelBookingDetailTabInfomationFragment.newInstance(placeBookingDetail);
        fragmentList.add(baseFragment02);

        BaseFragment baseFragment03 = HotelBookingDetailTabMapFragment.newInstance(placeBookingDetail, mBooking.isUsed);
        fragmentList.add(baseFragment03);

        BookingDetailFragmentPagerAdapter fragmentPagerAdapter = new BookingDetailFragmentPagerAdapter(getSupportFragmentManager(), fragmentList);
        viewPager.setAdapter(fragmentPagerAdapter);
    }

    @Override
    protected void onOptionsItemSelected(View view)
    {
        PopupMenu popupMenu = new PopupMenu(this, view);

        if (Util.isTextEmpty(mHotelBookingDetail.hotelPhone) == false)
        {
            popupMenu.getMenuInflater().inflate(R.menu.actionbar_hotel_booking_call, popupMenu.getMenu());
        } else
        {
            popupMenu.getMenuInflater().inflate(R.menu.actionbar_hotel_booking_call2, popupMenu.getMenu());
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                onOptionsItemSelected(item);
                return false;
            }
        });

        try
        {
            popupMenu.show();
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }

        view.post(new Runnable()
        {
            @Override
            public void run()
            {
                FontManager.apply(((ViewGroup) getWindow().getDecorView())//
                    , FontManager.getInstance(HotelBookingDetailTabActivity.this).getRegularTypeface());
            }
        });
    }

    @Override
    protected void onTabSelected(int position)
    {
        switch (position)
        {
            case 0:
                break;

            case 1:
                break;

            case 2:
            {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd", Locale.KOREA);
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                // Check In
                String checkInDay = simpleDateFormat.format(new Date(mHotelBookingDetail.checkInDate));

                // Check Out
                String checkOutDay = simpleDateFormat.format(new Date(mHotelBookingDetail.checkOutDate));

                String label = String.format("Hotel-%s-%s-%s", mHotelBookingDetail.placeName, checkInDay, checkOutDay);

                AnalyticsManager.getInstance(this).recordEvent(AnalyticsManager.Category.BOOKING_STATUS//
                    , mBooking.isUsed ? AnalyticsManager.Action.PAST_BOOKING_MAP_VIEW_CLICKED : AnalyticsManager.Action.UPCOMING_BOOKING_MAP_VIEW_CLICKED//
                    , label, null);
                break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_daily_call:
                if (Util.isTelephonyEnabled(HotelBookingDetailTabActivity.this) == true)
                {
                    try
                    {
                        String phone = DailyPreference.getInstance(HotelBookingDetailTabActivity.this).getCompanyPhoneNumber();

                        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)));
                    } catch (ActivityNotFoundException e)
                    {
                        DailyToast.showToast(HotelBookingDetailTabActivity.this, R.string.toast_msg_no_call, Toast.LENGTH_LONG);
                    }
                } else
                {
                    DailyToast.showToast(HotelBookingDetailTabActivity.this, R.string.toast_msg_no_call, Toast.LENGTH_LONG);
                }
                break;

            case R.id.action_kakaotalk:
                try
                {
                    startActivity(new Intent(Intent.ACTION_SEND, Uri.parse("kakaolink://friend/@%EB%8D%B0%EC%9D%BC%EB%A6%AC%ED%98%B8%ED%85%94")));
                } catch (ActivityNotFoundException e)
                {
                    try
                    {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL_STORE_GOOGLE_KAKAOTALK)));
                    } catch (ActivityNotFoundException e1)
                    {
                        Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
                        marketLaunch.setData(Uri.parse(URL_STORE_GOOGLE_KAKAOTALK_WEB));
                        startActivity(marketLaunch);
                    }
                }
                break;

            case R.id.action_direct_call:
                if (Util.isTelephonyEnabled(HotelBookingDetailTabActivity.this) == true)
                {
                    String phone = mHotelBookingDetail.hotelPhone;

                    if (Util.isTextEmpty(mHotelBookingDetail.hotelPhone) == true)
                    {
                        phone = DailyPreference.getInstance(HotelBookingDetailTabActivity.this).getCompanyPhoneNumber();
                    }

                    try
                    {
                        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)));
                    } catch (ActivityNotFoundException e)
                    {
                        String message = getString(R.string.toast_msg_no_hotel_call, mHotelBookingDetail.hotelPhone);
                        DailyToast.showToast(HotelBookingDetailTabActivity.this, message, Toast.LENGTH_LONG);
                    }
                } else
                {
                    String message = getString(R.string.toast_msg_no_hotel_call, mHotelBookingDetail.hotelPhone);
                    DailyToast.showToast(HotelBookingDetailTabActivity.this, message, Toast.LENGTH_LONG);
                }
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    protected void requestPlaceBookingDetail(int reservationIndex)
    {
        lockUI();

        // 호텔 정보를 가져온다.
        DailyNetworkAPI.getInstance(this).requestHotelBookingDetailInformation(mNetworkTag, reservationIndex, mReservationBookingDetailJsonResponseListener, this);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Listener
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private DailyHotelJsonResponseListener mReservationBookingDetailJsonResponseListener = new DailyHotelJsonResponseListener()
    {
        @Override
        public void onErrorResponse(VolleyError volleyError)
        {

        }

        @Override
        public void onResponse(String url, JSONObject response)
        {
            try
            {
                int msgCode = response.getInt("msg_code");

                if (msgCode == 0)
                {
                    JSONObject jsonObject = response.getJSONObject("data");

                    mHotelBookingDetail.setData(jsonObject);

                    loadFragments(getViewPager(), mHotelBookingDetail);
                } else
                {
                    onErrorPopupMessage(msgCode, response.getString("msg"));
                }
            } catch (Exception e)
            {
                onError(e);
            } finally
            {
                unLockUI();
            }
        }
    };
}
