/**
 * Copyright (c) 2014 Daily Co., Ltd. All rights reserved.
 * <p>
 * 호텔 리스트에서 호텔 선택 시 호텔의 정보들을 보여주는 화면이다.
 * 예약, 정보, 지도 프래그먼트를 담고 있는 액티비티이다.
 */
package com.twoheart.dailyhotel.place.activity;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Customer;
import com.twoheart.dailyhotel.model.GourmetDetail;
import com.twoheart.dailyhotel.model.PlaceDetail;
import com.twoheart.dailyhotel.model.SaleTime;
import com.twoheart.dailyhotel.model.TicketInformation;
import com.twoheart.dailyhotel.network.DailyNetworkAPI;
import com.twoheart.dailyhotel.network.response.DailyHotelJsonResponseListener;
import com.twoheart.dailyhotel.screen.common.BaseActivity;
import com.twoheart.dailyhotel.screen.common.ImageDetailListActivity;
import com.twoheart.dailyhotel.screen.common.ZoomMapActivity;
import com.twoheart.dailyhotel.screen.gourmet.detail.GourmetDetailLayout;
import com.twoheart.dailyhotel.screen.hotel.detail.HotelDetailLayout;
import com.twoheart.dailyhotel.screen.information.member.SignupActivity;
import com.twoheart.dailyhotel.util.DailyPreference;
import com.twoheart.dailyhotel.util.ExLog;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager.Action;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager.Screen;
import com.twoheart.dailyhotel.view.widget.DailyToast;
import com.twoheart.dailyhotel.view.widget.DailyToolbarLayout;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public abstract class PlaceDetailActivity extends BaseActivity
{
    private static final int DURATION_HOTEL_IMAGE_SHOW = 4000;

    protected GourmetDetailLayout mPlaceDetailLayout;
    protected PlaceDetail mPlaceDetail;
    protected int mCurrentImage;
    protected boolean mIsStartByShare;
    protected SaleTime mCheckInSaleTime;
    private TicketInformation mSelectedTicketInformation;
    private String mDefaultImageUrl;
    protected DailyToolbarLayout mDailyToolbarLayout;
    private View mToolbarUnderline;

    public interface OnUserActionListener
    {
        void showActionBar();

        void hideActionBar();

        void onClickImage(PlaceDetail placeDetail);

        void onSelectedImagePosition(int position);

        void doBooking(TicketInformation ticketInformation);

        void doKakaotalkConsult();

        void showTicketInformationLayout();

        void hideTicketInformationLayout();

        void showMap();

        void finish();
    }

    public interface OnImageActionListener
    {
        void nextSlide();

        void prevSlide();
    }

    private Handler mImageHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            if (isFinishing() == true || mPlaceDetailLayout == null)
            {
                return;
            }

            int direction = msg.arg1;

            mCurrentImage = mPlaceDetailLayout.getCurrentImage();

            if (direction > 0)
            {
                mCurrentImage++;
            } else if (direction < 0)
            {
                mCurrentImage--;
            }

            mPlaceDetailLayout.setCurrentImage(mCurrentImage);

            int autoSlide = msg.arg2;

            if (autoSlide == 1)
            {
                sendEmptyMessageDelayed(0, DURATION_HOTEL_IMAGE_SHOW);
            }
        }
    };

    protected abstract GourmetDetailLayout getLayout(BaseActivity activity, String imageUrl);

    protected abstract void requestPlaceDetailInformation(PlaceDetail placeDetail, SaleTime checkInSaleTime);

    protected abstract PlaceDetail createPlaceDetail(Intent intent);

    protected abstract void shareKakao(PlaceDetail placeDetail, String imageUrl, SaleTime checkInSaleTime, SaleTime checkOutSaleTime);

    protected abstract void processBooking(PlaceDetail placeDetail, TicketInformation ticketInformation, SaleTime checkInSaleTime, boolean isBenefit);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        if (intent == null)
        {
            finish();
            return;
        }

        if (intent.hasExtra(NAME_INTENT_EXTRA_DATA_TYPE) == true)
        {
            mIsStartByShare = true;

            long dailyTime = intent.getLongExtra(NAME_INTENT_EXTRA_DATA_DAILYTIME, 0);
            int dayOfDays = intent.getIntExtra(NAME_INTENT_EXTRA_DATA_DAYOFDAYS, -1);

            mCheckInSaleTime = new SaleTime();
            mCheckInSaleTime.setDailyTime(dailyTime);
            mCheckInSaleTime.setOffsetDailyDay(dayOfDays);

            mPlaceDetail = createPlaceDetail(intent);

            if (mPlaceDetail == null)
            {
                Util.restartApp(this);
                return;
            }

            initLayout(null, null);
        } else
        {
            mIsStartByShare = false;

            mCheckInSaleTime = intent.getParcelableExtra(NAME_INTENT_EXTRA_DATA_SALETIME);

            mPlaceDetail = createPlaceDetail(intent);

            String placeName = intent.getStringExtra(NAME_INTENT_EXTRA_DATA_PLACENAME);
            String imageUrl = intent.getStringExtra(NAME_INTENT_EXTRA_DATA_IMAGEURL);
            String category = intent.getStringExtra(NAME_INTENT_EXTRA_DATA_CATEGORY);
            mDefaultImageUrl = imageUrl;

            if (mPlaceDetail instanceof GourmetDetail && Util.isTextEmpty(category) == false)
            {
                ((GourmetDetail) mPlaceDetail).category = category;
            }

            if (mCheckInSaleTime == null || mPlaceDetail == null || placeName == null)
            {
                Util.restartApp(this);
                return;
            }

            initLayout(placeName, imageUrl);
        }
    }

    protected void initLayout(String placeName, String imageUrl)
    {
        setContentView(R.layout.activity_placedetail);

        if (mPlaceDetailLayout == null)
        {
            mPlaceDetailLayout = getLayout(this, imageUrl);
            mPlaceDetailLayout.setUserActionListener(mOnUserActionListener);
            mPlaceDetailLayout.setImageActionListener(mOnImageActionListener);
        }

        setLockUICancelable(true);
        initToolbar(placeName);

        mOnUserActionListener.hideActionBar();
    }

    private void initToolbar(String title)
    {
        mToolbarUnderline = findViewById(R.id.toolbarUnderline);
        mToolbarUnderline.setVisibility(View.INVISIBLE);

        View toolbar = findViewById(R.id.toolbar);
        mDailyToolbarLayout = new DailyToolbarLayout(this, toolbar);
        mDailyToolbarLayout.initToolbar(title, true);
        mDailyToolbarLayout.setToolbarMenu(R.drawable.navibar_ic_share, -1);
        mDailyToolbarLayout.setToolbarMenuClickListener(mToolbarOptionsItemSelected);
    }

    @Override
    protected void onStart()
    {
        AnalyticsManager.getInstance(PlaceDetailActivity.this).recordScreen(Screen.DAILYGOURMET_DETAIL, null);

        try
        {
            super.onStart();
        } catch (NullPointerException e)
        {
            ExLog.e(e.toString());

            Util.restartApp(this);
        }
    }

    @Override
    protected void onResume()
    {
        if (mPlaceDetailLayout != null)
        {
            mPlaceDetailLayout.hideTicketInformationLayout();
            mPlaceDetailLayout.setBookingStatus(GourmetDetailLayout.STATUS_SEARCH_TICKET);
        }

        lockUI();
        DailyNetworkAPI.getInstance().requestCommonDatetime(mNetworkTag, mDateTimeJsonResponseListener, this);

        super.onResume();
    }

    @Override
    public void onBackPressed()
    {
        if (mPlaceDetailLayout != null)
        {
            switch (mPlaceDetailLayout.getBookingStatus())
            {
                case HotelDetailLayout.STATUS_BOOKING:
                case HotelDetailLayout.STATUS_NONE:
                    mOnUserActionListener.hideTicketInformationLayout();
                    return;
            }
        }

        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        try
        {
            releaseUiComponent();

            switch (requestCode)
            {
                case CODE_REQUEST_ACTIVITY_BOOKING:
                {
                    setResult(resultCode);

                    if (resultCode == RESULT_OK || resultCode == CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_READY)
                    {
                        finish();
                    }
                    break;
                }

                case CODE_REQUEST_ACTIVITY_LOGIN:
                case CODE_REQUEST_ACTIVITY_USERINFO_UPDATE:
                {
                    if (resultCode == RESULT_OK)
                    {
                        mOnUserActionListener.doBooking(mSelectedTicketInformation);
                    }
                    break;
                }
            }

            super.onActivityResult(requestCode, resultCode, data);
        } catch (NullPointerException e)
        {
            ExLog.e(e.toString());

            Util.restartApp(this);
        }
    }

    @Override
    public void onError()
    {
        super.onError();

        finish();
    }

    private void moveToUserInfoUpdate(Customer user, int recommender, boolean isDailyUser)
    {
        Intent intent = SignupActivity.newInstance(this, user, recommender, isDailyUser);
        startActivityForResult(intent, CODE_REQUEST_ACTIVITY_USERINFO_UPDATE);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);
    }

    private boolean isEmptyTextField(String... fieldText)
    {

        for (int i = 0; i < fieldText.length; i++)
        {
            if (Util.isTextEmpty(fieldText[i]) == true)
            {
                return true;
            }
        }

        return false;
    }

    protected void recordAnalyticsGourmetDetail(String screen, PlaceDetail placeDetail)
    {
        if (placeDetail == null)
        {
            return;
        }

        try
        {
            Map<String, String> params = new HashMap<>();
            params.put(AnalyticsManager.KeyType.NAME, placeDetail.name);
            params.put(AnalyticsManager.KeyType.CATEGORY, ((GourmetDetail) placeDetail).category);
            params.put(AnalyticsManager.KeyType.DBENEFIT, Util.isTextEmpty(placeDetail.benefit) ? "no" : "yes");

            if (placeDetail.getTicketInformation() == null || placeDetail.getTicketInformation().size() == 0)
            {
                params.put(AnalyticsManager.KeyType.PRICE, "0");
            } else
            {
                params.put(AnalyticsManager.KeyType.PRICE, Integer.toString(placeDetail.getTicketInformation().get(0).discountPrice));
            }

            params.put(AnalyticsManager.KeyType.PLACE_INDEX, Integer.toString(placeDetail.index));
            params.put(AnalyticsManager.KeyType.DATE, mCheckInSaleTime.getDayOfDaysDateFormat("yyyy-MM-dd"));

            AnalyticsManager.getInstance(this).recordScreen(screen, params);
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UserActionListener
    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    private View.OnClickListener mToolbarOptionsItemSelected = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View dialogView = layoutInflater.inflate(R.layout.view_sharedialog_layout, null, false);

            final Dialog shareDialog = new Dialog(PlaceDetailActivity.this);
            shareDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            shareDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            shareDialog.setCanceledOnTouchOutside(true);

            // 버튼
            View kakaoShareLayout = dialogView.findViewById(R.id.kakaoShareLayout);

            kakaoShareLayout.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (shareDialog.isShowing() == true)
                    {
                        shareDialog.dismiss();
                    }

                    if (mDefaultImageUrl == null)
                    {
                        if (mPlaceDetail.getImageInformationList() != null && mPlaceDetail.getImageInformationList().size() > 0)
                        {
                            mDefaultImageUrl = mPlaceDetail.getImageInformationList().get(0).url;
                        }
                    }

                    shareKakao(mPlaceDetail, mDefaultImageUrl, mCheckInSaleTime, null);
                }
            });

            try
            {
                shareDialog.setContentView(dialogView);
                shareDialog.show();
            } catch (Exception e)
            {
                ExLog.d(e.toString());
            }
        }
    };

    private OnUserActionListener mOnUserActionListener = new OnUserActionListener()
    {
        @Override
        public void showActionBar()
        {
            mDailyToolbarLayout.setToolbarTransparent(false);
            mToolbarUnderline.setVisibility(View.VISIBLE);
        }

        @Override
        public void hideActionBar()
        {
            mDailyToolbarLayout.setToolbarTransparent(true);
            mToolbarUnderline.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onClickImage(PlaceDetail ticketDetailDto)
        {
            if (isLockUiComponent() == true)
            {
                return;
            }

            lockUiComponent();

            Intent intent = new Intent(PlaceDetailActivity.this, ImageDetailListActivity.class);
            intent.putExtra(NAME_INTENT_EXTRA_DATA_IMAGEURLLIST, ticketDetailDto.getImageInformationList());
            intent.putExtra(NAME_INTENT_EXTRA_DATA_SELECTED_POSOTION, mCurrentImage);
            startActivity(intent);
        }

        @Override
        public void onSelectedImagePosition(int position)
        {
            mCurrentImage = position;
        }

        @Override
        public void doBooking(TicketInformation ticketInformation)
        {
            if (lockUiComponentAndIsLockUiComponent() == true)
            {
                return;
            }

            mSelectedTicketInformation = ticketInformation;

            if (Util.isTextEmpty(DailyPreference.getInstance(PlaceDetailActivity.this).getAuthorization()) == true)
            {
                startLoginActivity();
            } else
            {
                lockUI();

                DailyNetworkAPI.getInstance().requestUserInformationEx(mNetworkTag, mUserSocialInformationJsonResponseListener, PlaceDetailActivity.this);
            }

            String label = String.format("%s-%s", mPlaceDetail.name, mSelectedTicketInformation.name);
            AnalyticsManager.getInstance(PlaceDetailActivity.this).recordEvent(AnalyticsManager.Category.GOURMETBOOKINGS//
                , Action.BOOKING_CLICKED, label, null);
        }

        @Override
        public void doKakaotalkConsult()
        {
            if (isLockUiComponent() == true || isFinishing() == true)
            {
                return;
            }

            lockUiComponent();

            try
            {
                startActivity(new Intent(Intent.ACTION_SEND, Uri.parse("kakaolink://friend/%40%EB%8D%B0%EC%9D%BC%EB%A6%AC%EA%B3%A0%EB%A9%94")));
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

            AnalyticsManager.getInstance(PlaceDetailActivity.this).recordEvent(AnalyticsManager.Category.GOURMETBOOKINGS//
                , Action.KAKAO_INQUIRY_CLICKED, mPlaceDetail.name, null);
        }

        @Override
        public void showTicketInformationLayout()
        {
            if (isLockUiComponent() == true || isFinishing() == true)
            {
                return;
            }

            lockUiComponent();

            if (mPlaceDetailLayout != null)
            {
                mPlaceDetailLayout.showAnimationTicketInformationLayout();
            }

            releaseUiComponent();

            recordAnalyticsGourmetDetail(Screen.DAILYGOURMET_DETAIL_TICKETTYPE, mPlaceDetail);
            AnalyticsManager.getInstance(PlaceDetailActivity.this).recordEvent(AnalyticsManager.Category.GOURMETBOOKINGS//
                , Action.TICKET_TYPE_CLICKED, mPlaceDetail.name, null);
        }

        @Override
        public void hideTicketInformationLayout()
        {
            if (isLockUiComponent() == true || isFinishing() == true)
            {
                return;
            }

            lockUiComponent();

            if (mPlaceDetailLayout != null)
            {
                mPlaceDetailLayout.hideAnimationTicketInformationLayout();
            }

            releaseUiComponent();
        }

        @Override
        public void showMap()
        {
            if (isLockUiComponent() == true || isFinishing() == true)
            {
                return;
            }

            lockUiComponent();

            Intent intent = ZoomMapActivity.newInstance(PlaceDetailActivity.this//
                , ZoomMapActivity.SourceType.GOURMET, mPlaceDetail.name//
                , mPlaceDetail.latitude, mPlaceDetail.longitude);

            startActivity(intent);

            //            SaleTime checkOutSaleTime = mCheckInSaleTime.getClone(mCheckInSaleTime.getOffsetDailyDay());
            //            String label = String.format("%s (%s-%s)", mPlaceDetail.name, mCheckInSaleTime.getDayOfDaysDateFormat("yyMMdd"), checkOutSaleTime.getDayOfDaysDateFormat("yyMMdd"));
            //
            //            AnalyticsManager.getInstance(getApplicationContext()).recordEvent(Screen.DAILYGOURMET_DETAIL, Action.CLICK, label, (long) mPlaceDetail.index);
        }

        @Override
        public void finish()
        {
            PlaceDetailActivity.super.finish();
        }
    };

    private OnImageActionListener mOnImageActionListener = new OnImageActionListener()
    {
        @Override
        public void nextSlide()
        {
            Message message = mImageHandler.obtainMessage();
            message.what = 0;
            message.arg1 = 1; // 오른쪽으로 이동.
            message.arg2 = 0; // 수동

            mImageHandler.removeMessages(0);
            mImageHandler.sendMessage(message);
        }

        @Override
        public void prevSlide()
        {
            Message message = mImageHandler.obtainMessage();
            message.what = 0;
            message.arg1 = -1; // 왼쪽으로 이동.
            message.arg2 = 0; // 수동

            mImageHandler.removeMessages(0);
            mImageHandler.sendMessage(message);
        }
    };

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Listener
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private DailyHotelJsonResponseListener mUserSocialInformationJsonResponseListener = new DailyHotelJsonResponseListener()
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

                    Customer user = new Customer();
                    user.setEmail(jsonObject.getString("email"));
                    user.setName(jsonObject.getString("name"));
                    user.setPhone(jsonObject.getString("phone"));
                    user.setUserIdx(jsonObject.getString("idx"));

                    // 추천인
                    int recommender = jsonObject.getInt("recommender_code");
                    boolean isDailyUser = jsonObject.getBoolean("is_daily_user");

                    if (isEmptyTextField(new String[]{user.getEmail(), user.getPhone(), user.getName()}) == false && Util.isValidatePhoneNumber(user.getPhone()) == true)
                    {
                        processBooking(mPlaceDetail, mSelectedTicketInformation, mCheckInSaleTime, false);
                    } else
                    {
                        // 정보 업데이트 화면으로 이동.
                        moveToUserInfoUpdate(user, recommender, isDailyUser);
                    }
                } else
                {
                    unLockUI();

                    String msg = response.getString("msg");

                    DailyToast.showToast(PlaceDetailActivity.this, msg, Toast.LENGTH_SHORT);
                }
            } catch (Exception e)
            {
                onError(e);
            }
        }
    };

    private DailyHotelJsonResponseListener mDateTimeJsonResponseListener = new DailyHotelJsonResponseListener()
    {
        @Override
        public void onResponse(String url, JSONObject response)
        {
            if (isFinishing() == true)
            {
                return;
            }

            try
            {
                if (response == null)
                {
                    throw new NullPointerException("response == null");
                }

                if (mIsStartByShare == true)
                {
                    mCheckInSaleTime.setCurrentTime(response.getLong("currentDateTime"));

                    long shareDailyTime = mCheckInSaleTime.getDayOfDaysDate().getTime();
                    long todayDailyTime = response.getLong("dailyDateTime");

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.KOREA);
                    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                    int shareDailyDay = Integer.parseInt(simpleDateFormat.format(new Date(shareDailyTime)));
                    int todayDailyDay = Integer.parseInt(simpleDateFormat.format(new Date(todayDailyTime)));

                    // 지난 날의 호텔인 경우.
                    if (shareDailyDay < todayDailyDay)
                    {
                        unLockUI();
                        DailyToast.showToast(PlaceDetailActivity.this, R.string.toast_msg_dont_past_hotelinfo, Toast.LENGTH_LONG);
                        finish();
                        return;
                    }

                    requestPlaceDetailInformation(mPlaceDetail, mCheckInSaleTime);
                } else
                {
                    SaleTime saleTime = new SaleTime();

                    saleTime.setCurrentTime(response.getLong("currentDateTime"));

                    long todayDailyTime = response.getLong("dailyDateTime");
                    saleTime.setDailyTime(todayDailyTime);

                    long shareDailyTime = mCheckInSaleTime.getDayOfDaysDate().getTime();

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.KOREA);
                    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                    int shareDailyDay = Integer.parseInt(simpleDateFormat.format(new Date(shareDailyTime)));
                    int todayDailyDay = Integer.parseInt(simpleDateFormat.format(new Date(todayDailyTime)));

                    // 지난 날의 호텔인 경우.
                    if (shareDailyDay < todayDailyDay)
                    {
                        unLockUI();

                        DailyToast.showToast(PlaceDetailActivity.this, R.string.toast_msg_dont_past_hotelinfo, Toast.LENGTH_LONG);
                        finish();
                        return;
                    }

                    requestPlaceDetailInformation(mPlaceDetail, mCheckInSaleTime);
                }
            } catch (Exception e)
            {
                onError(e);
                unLockUI();

                finish();
            }
        }
    };
}
