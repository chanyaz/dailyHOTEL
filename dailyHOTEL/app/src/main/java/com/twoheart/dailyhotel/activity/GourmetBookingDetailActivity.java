/**
 * Copyright (c) 2014 Daily Co., Ltd. All rights reserved.
 * <p/>
 * BookingTabActivity (예약한 호텔의 예약, 정보, 지도탭을 보여주는 화면)
 * <p/>
 * 예약한 호텔리스트에서 호텔 클릭 시 호텔의 정보들을 보여주는 화면이다.
 * 예약, 정보, 지도 프래그먼트를 담고 있는 액티비티이다.
 */
package com.twoheart.dailyhotel.activity;

import android.view.View;

import com.android.volley.Request.Method;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.fragment.BaseFragment;
import com.twoheart.dailyhotel.fragment.GourmetTabBookingFragment;
import com.twoheart.dailyhotel.fragment.PlaceTabInfoFragment;
import com.twoheart.dailyhotel.fragment.PlaceTabMapFragment;
import com.twoheart.dailyhotel.model.GourmetBookingDetail;
import com.twoheart.dailyhotel.network.request.DailyHotelJsonRequest;
import com.twoheart.dailyhotel.network.response.DailyHotelJsonResponseListener;
import com.twoheart.dailyhotel.view.widget.FragmentViewPager;

import org.json.JSONObject;

import java.util.ArrayList;

public class GourmetBookingDetailActivity extends PlaceBookingDetailActivity
{
    private DailyHotelJsonResponseListener mReservationBookingDetailJsonResponseListener = new DailyHotelJsonResponseListener()
    {
        @Override
        public void onResponse(String url, JSONObject response)
        {
            try
            {
                if (response == null)
                {
                    throw new NullPointerException("response == null");
                }

                int msg_code = response.getInt("msg_code");

                if (msg_code == 0)
                {
                    JSONObject jsonObject = response.getJSONObject("data");

                    if (mPlaceBookingDetail == null)
                    {
                        mPlaceBookingDetail = new GourmetBookingDetail();
                    }

                    mPlaceBookingDetail.setData(jsonObject);

                    loadFragments();
                } else
                {
                    if (response.has("msg") == true)
                    {
                        String msg = response.getString("msg");

                        showSimpleDialog(null, msg, getString(R.string.dialog_btn_text_confirm), null, new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                finish();
                            }
                        }, null, false);
                        return;
                    } else
                    {
                        onInternalError();
                    }
                }
            } catch (Exception e)
            {
                onInternalError();
            } finally
            {
                unLockUI();
            }
        }
    };

    @Override
    protected void loadFragments()
    {
        if (mFragmentViewPager == null)
        {
            ArrayList<String> titleList = new ArrayList<String>();
            titleList.add(getString(R.string.frag_booking_tab_title));
            titleList.add(getString(R.string.frag_tab_info_title));
            titleList.add(getString(R.string.frag_tab_map_title));

            mFragmentViewPager = (FragmentViewPager) findViewById(R.id.fragmentViewPager);

            ArrayList<BaseFragment> mFragmentList = new ArrayList<BaseFragment>();

            BaseFragment baseFragment01 = GourmetTabBookingFragment.newInstance(mPlaceBookingDetail, booking, getString(R.string.drawer_menu_pin_title_resrvation));
            mFragmentList.add(baseFragment01);

            BaseFragment baseFragment02 = PlaceTabInfoFragment.newInstance(mPlaceBookingDetail, titleList.get(1));
            mFragmentList.add(baseFragment02);

            BaseFragment baseFragment03 = PlaceTabMapFragment.newInstance(mPlaceBookingDetail, titleList.get(2));
            mFragmentList.add(baseFragment03);

            mFragmentViewPager.setData(mFragmentList);
            mFragmentViewPager.setAdapter(getSupportFragmentManager());

            mTabIndicator.setViewPager(mFragmentViewPager.getViewPager());
            mTabIndicator.setOnPageChangeListener(mOnPageChangeListener);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Listener
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void requestPlaceBookingDetail()
    {
        lockUI();

        String params = String.format("?reservation_rec_idx=%d", booking.reservationIndex);

        mQueue.add(new DailyHotelJsonRequest(Method.GET, new StringBuilder(URL_DAILYHOTEL_SERVER).append(URL_WEBAPI_FNB_RESERVATION_BOOKING_DETAIL).append(params).toString(), null, mReservationBookingDetailJsonResponseListener, this));
    }

}
