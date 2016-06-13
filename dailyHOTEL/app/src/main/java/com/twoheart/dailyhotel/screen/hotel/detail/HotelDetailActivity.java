/**
 * Copyright (c) 2014 Daily Co., Ltd. All rights reserved.
 * <p>
 * 호텔 리스트에서 호텔 선택 시 호텔의 정보들을 보여주는 화면이다.
 * 예약, 정보, 지도 프래그먼트를 담고 있는 액티비티이다.
 */
package com.twoheart.dailyhotel.screen.hotel.detail;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Area;
import com.twoheart.dailyhotel.model.Customer;
import com.twoheart.dailyhotel.model.HotelDetail;
import com.twoheart.dailyhotel.model.ImageInformation;
import com.twoheart.dailyhotel.model.Province;
import com.twoheart.dailyhotel.model.SaleRoomInformation;
import com.twoheart.dailyhotel.model.SaleTime;
import com.twoheart.dailyhotel.network.DailyNetworkAPI;
import com.twoheart.dailyhotel.network.response.DailyHotelJsonResponseListener;
import com.twoheart.dailyhotel.place.base.BaseActivity;
import com.twoheart.dailyhotel.screen.common.ImageDetailListActivity;
import com.twoheart.dailyhotel.screen.common.ZoomMapActivity;
import com.twoheart.dailyhotel.screen.hotel.payment.HotelPaymentActivity;
import com.twoheart.dailyhotel.screen.information.member.AddProfileSocialActivity;
import com.twoheart.dailyhotel.screen.information.member.EditProfilePhoneActivity;
import com.twoheart.dailyhotel.util.DailyPreference;
import com.twoheart.dailyhotel.util.ExLog;
import com.twoheart.dailyhotel.util.KakaoLinkManager;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager.Action;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager.Screen;
import com.twoheart.dailyhotel.widget.DailyToast;
import com.twoheart.dailyhotel.widget.DailyToolbarLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class HotelDetailActivity extends BaseActivity
{
    private static final int DURATION_HOTEL_IMAGE_SHOW = 4000;

    private HotelDetail mHotelDetail;
    private SaleTime mCheckInSaleTime;

    private Province mProvince;
    private String mArea; // Analytics용 소지역
    private int mViewPrice; // Analytics용 리스트 가격

    private int mCurrentImage;
    private SaleRoomInformation mSelectedSaleRoomInformation;
    private boolean mIsStartByShare;
    private String mDefaultImageUrl;

    private HotelDetailLayout mHotelDetailLayout;
    private DailyToolbarLayout mDailyToolbarLayout;
    private View mToolbarUnderline;

    private Handler mImageHandler;
    private boolean mDontReloadAtOnResume;

    public interface OnUserActionListener
    {
        void showActionBar();

        void hideActionBar();

        void onClickImage(HotelDetail hotelDetail);

        void nextSlide();

        void prevSlide();

        void onSelectedImagePosition(int position);

        void doBooking(SaleRoomInformation saleRoomInformation);

        void doKakaotalkConsult();

        void showRoomType();

        void hideRoomType();

        void showMap();

        void clipAddress(String address);
    }

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

        mImageHandler = new ImageHandler(this);

        if (intent.hasExtra(NAME_INTENT_EXTRA_DATA_TYPE) == true)
        {
            mIsStartByShare = true;

            long dailyTime = intent.getLongExtra(NAME_INTENT_EXTRA_DATA_DAILYTIME, 0);
            int dayOfDays = intent.getIntExtra(NAME_INTENT_EXTRA_DATA_DAYOFDAYS, -1);

            mCheckInSaleTime = new SaleTime();
            mCheckInSaleTime.setDailyTime(dailyTime);
            mCheckInSaleTime.setOffsetDailyDay(dayOfDays);

            int hotelIndex = intent.getIntExtra(NAME_INTENT_EXTRA_DATA_HOTELIDX, -1);
            int nights = intent.getIntExtra(NAME_INTENT_EXTRA_DATA_NIGHTS, 0);

            mHotelDetail = new HotelDetail(hotelIndex, nights);

            initLayout(null, null);
        } else
        {
            mIsStartByShare = false;

            mCheckInSaleTime = intent.getParcelableExtra(NAME_INTENT_EXTRA_DATA_SALETIME);

            int hotelIndex = intent.getIntExtra(NAME_INTENT_EXTRA_DATA_HOTELIDX, -1);
            int nights = intent.getIntExtra(NAME_INTENT_EXTRA_DATA_NIGHTS, 0);

            mHotelDetail = new HotelDetail(hotelIndex, nights);

            String hotelName = intent.getStringExtra(NAME_INTENT_EXTRA_DATA_HOTELNAME);
            String hotelImageUrl = intent.getStringExtra(NAME_INTENT_EXTRA_DATA_IMAGEURL);
            mDefaultImageUrl = hotelImageUrl;

            mHotelDetail.categoryCode = intent.getStringExtra(NAME_INTENT_EXTRA_DATA_CATEGORY);

            if (mCheckInSaleTime == null || hotelIndex == -1 || hotelName == null || nights <= 0)
            {
                Util.restartApp(this);
                return;
            }

            mProvince = intent.getParcelableExtra(NAME_INTENT_EXTRA_DATA_PROVINCE);
            mArea = intent.getStringExtra(NAME_INTENT_EXTRA_DATA_AREA);
            mViewPrice = intent.getIntExtra(NAME_INTENT_EXTRA_DATA_PRICE, 0);

            initLayout(hotelName, hotelImageUrl);
        }
    }

    private void initLayout(String hotelName, String imageUrl)
    {
        setContentView(R.layout.activity_hoteldetail);

        if (mHotelDetailLayout == null)
        {
            mHotelDetailLayout = new HotelDetailLayout(this, imageUrl);
            mHotelDetailLayout.setUserActionListener(mOnUserActionListener);
        }

        setLockUICancelable(true);
        initToolbar(hotelName);

        mOnUserActionListener.hideActionBar();
    }

    private void initToolbar(String title)
    {
        mToolbarUnderline = findViewById(R.id.toolbarUnderline);
        mToolbarUnderline.setVisibility(View.INVISIBLE);

        View toolbar = findViewById(R.id.toolbar);
        mDailyToolbarLayout = new DailyToolbarLayout(this, toolbar);
        mDailyToolbarLayout.initToolbar(title, new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        }, true);

        mDailyToolbarLayout.setToolbarMenu(R.drawable.navibar_ic_share, -1);
        mDailyToolbarLayout.setToolbarMenuClickListener(mToolbarOptionsItemSelected);
    }

    @Override
    protected void onStart()
    {
        AnalyticsManager.getInstance(HotelDetailActivity.this).recordScreen(Screen.DAILYHOTEL_DETAIL);

        super.onStart();
    }

    @Override
    protected void onResume()
    {
        if (mHotelDetailLayout != null)
        {
            mHotelDetailLayout.hideRoomType();

            if (mHotelDetailLayout.getBookingStatus() != HotelDetailLayout.STATUS_SOLD_OUT)
            {
                mHotelDetailLayout.setBookingStatus(HotelDetailLayout.STATUS_SEARCH_ROOM);
            }
        }

        if (mDontReloadAtOnResume == true)
        {
            mDontReloadAtOnResume = false;
        } else
        {
            lockUI();
            DailyNetworkAPI.getInstance(this).requestCommonDatetime(mNetworkTag, mDateTimeJsonResponseListener, this);
        }

        super.onResume();
    }

    @Override
    public void onBackPressed()
    {
        if (mHotelDetailLayout != null)
        {
            switch (mHotelDetailLayout.getBookingStatus())
            {
                case HotelDetailLayout.STATUS_BOOKING:
                case HotelDetailLayout.STATUS_NONE:
                    mOnUserActionListener.hideRoomType();
                    return;
            }
        }

        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        releaseUiComponent();

        switch (requestCode)
        {
            case CODE_REQUEST_ACTIVITY_BOOKING:
            {
                mDontReloadAtOnResume = true;

                setResult(resultCode);

                if (resultCode == RESULT_OK || resultCode == CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_READY || resultCode == CODE_RESULT_ACTIVITY_PAYMENT_TIMEOVER)
                {
                    finish();
                }
                break;
            }

            case CODE_REQUEST_ACTIVITY_LOGIN:
            case CODE_REQUEST_ACTIVITY_USERINFO_UPDATE:
            {
                mDontReloadAtOnResume = true;

                if (resultCode == RESULT_OK)
                {
                    mOnUserActionListener.doBooking(mSelectedSaleRoomInformation);
                }
                break;
            }

            case CODE_REQUEST_ACTIVITY_IMAGELIST:
            case CODE_REQUEST_ACTIVITY_ZOOMMAP:
            case CODE_REQUEST_ACTIVITY_SHAREKAKAO:
                mDontReloadAtOnResume = true;
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onError()
    {
        super.onError();

        finish();
    }

    private void moveToBooking(HotelDetail hotelDetail, SaleRoomInformation saleRoomInformation, SaleTime checkInSaleTime)
    {
        if (hotelDetail == null || saleRoomInformation == null || checkInSaleTime == null)
        {
            return;
        }

        String imageUrl = null;
        ArrayList<ImageInformation> mImageInformationList = hotelDetail.getImageInformationList();

        if (mImageInformationList != null && mImageInformationList.size() > 0)
        {
            imageUrl = mImageInformationList.get(0).url;
        }

        saleRoomInformation.categoryCode = hotelDetail.categoryCode;

        Intent intent = HotelPaymentActivity.newInstance(HotelDetailActivity.this, saleRoomInformation//
            , checkInSaleTime, imageUrl, hotelDetail.hotelIndex, !Util.isTextEmpty(hotelDetail.hotelBenefit), mProvince, mArea);

        startActivityForResult(intent, CODE_REQUEST_ACTIVITY_BOOKING);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);
    }

    private void moveToAddSocialUserInformation(Customer user)
    {
        Intent intent = AddProfileSocialActivity.newInstance(this, user);
        startActivityForResult(intent, CODE_REQUEST_ACTIVITY_USERINFO_UPDATE);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);
    }

    private void moveToUpdateUserPhoneNumber(Customer user, EditProfilePhoneActivity.Type type)
    {
        Intent intent = EditProfilePhoneActivity.newInstance(this, user.getUserIdx(), type);
        startActivityForResult(intent, CODE_REQUEST_ACTIVITY_USERINFO_UPDATE);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);
    }

    private void recordAnalyticsHotelDetail(String screen, HotelDetail hotelDetail)
    {
        if (hotelDetail == null)
        {
            return;
        }

        try
        {
            Map<String, String> params = new HashMap<>();
            params.put(AnalyticsManager.KeyType.NAME, mHotelDetail.hotelName);
            params.put(AnalyticsManager.KeyType.GRADE, mHotelDetail.grade.getName(HotelDetailActivity.this));
            params.put(AnalyticsManager.KeyType.DBENEFIT, Util.isTextEmpty(mHotelDetail.hotelBenefit) ? "no" : "yes");

            if (mHotelDetail.getSaleRoomList() == null || mHotelDetail.getSaleRoomList().size() == 0)
            {
                params.put(AnalyticsManager.KeyType.PRICE, "0");
            } else
            {
                params.put(AnalyticsManager.KeyType.PRICE, Integer.toString(mHotelDetail.getSaleRoomList().get(0).averageDiscount));
            }

            params.put(AnalyticsManager.KeyType.QUANTITY, Integer.toString(mHotelDetail.nights));
            params.put(AnalyticsManager.KeyType.PLACE_INDEX, Integer.toString(mHotelDetail.hotelIndex));

            SaleTime checkOutSaleTime = mCheckInSaleTime.getClone(mCheckInSaleTime.getOffsetDailyDay() + hotelDetail.nights);

            params.put(AnalyticsManager.KeyType.CHECK_IN, mCheckInSaleTime.getDayOfDaysDateFormat("yyyy-MM-dd"));
            params.put(AnalyticsManager.KeyType.CHECK_OUT, checkOutSaleTime.getDayOfDaysDateFormat("yyyy-MM-dd"));

            params.put(AnalyticsManager.KeyType.ADDRESS, mHotelDetail.address);
            params.put(AnalyticsManager.KeyType.HOTEL_CATEGORY, mHotelDetail.categoryCode);

            if (mProvince == null)
            {
                params.put(AnalyticsManager.KeyType.PROVINCE, AnalyticsManager.ValueType.EMPTY);
                params.put(AnalyticsManager.KeyType.DISTRICT, AnalyticsManager.ValueType.EMPTY);
                params.put(AnalyticsManager.KeyType.AREA, AnalyticsManager.ValueType.EMPTY);
            } else
            {
                if (mProvince instanceof Area)
                {
                    Area area = (Area) mProvince;
                    params.put(AnalyticsManager.KeyType.PROVINCE, area.getProvince().name);
                    params.put(AnalyticsManager.KeyType.DISTRICT, area.name);
                } else
                {
                    params.put(AnalyticsManager.KeyType.PROVINCE, mProvince.name);
                    params.put(AnalyticsManager.KeyType.DISTRICT, AnalyticsManager.ValueType.EMPTY);
                }

                params.put(AnalyticsManager.KeyType.AREA, Util.isTextEmpty(mArea) ? AnalyticsManager.ValueType.EMPTY : mArea);
            }

            params.put(AnalyticsManager.KeyType.UNIT_PRICE, Integer.toString(mViewPrice));
            params.put(AnalyticsManager.KeyType.CHECK_IN_DATE, Long.toString(mCheckInSaleTime.getDayOfDaysDate().getTime()));

            AnalyticsManager.getInstance(HotelDetailActivity.this).recordScreen(screen, params);
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }
    }

    private Map<String, String> recordAnalyticsBooking(HotelDetail hotelDetail, SaleRoomInformation saleRoomInformation)
    {
        if (hotelDetail == null || saleRoomInformation == null)
        {
            return null;
        }

        try
        {
            Map<String, String> params = new HashMap<>();
            params.put(AnalyticsManager.KeyType.NAME, mHotelDetail.hotelName);
            params.put(AnalyticsManager.KeyType.QUANTITY, Integer.toString(mHotelDetail.nights));
            params.put(AnalyticsManager.KeyType.PLACE_INDEX, Integer.toString(mHotelDetail.hotelIndex));
            params.put(AnalyticsManager.KeyType.HOTEL_CATEGORY, mHotelDetail.categoryCode);

            if (mProvince == null)
            {
                params.put(AnalyticsManager.KeyType.PROVINCE, AnalyticsManager.ValueType.EMPTY);
                params.put(AnalyticsManager.KeyType.DISTRICT, AnalyticsManager.ValueType.EMPTY);
                params.put(AnalyticsManager.KeyType.AREA, AnalyticsManager.ValueType.EMPTY);
            } else
            {
                if (mProvince instanceof Area)
                {
                    Area area = (Area) mProvince;
                    params.put(AnalyticsManager.KeyType.PROVINCE, area.getProvince().name);
                    params.put(AnalyticsManager.KeyType.DISTRICT, area.name);
                } else
                {
                    params.put(AnalyticsManager.KeyType.PROVINCE, mProvince.name);
                    params.put(AnalyticsManager.KeyType.DISTRICT, AnalyticsManager.ValueType.EMPTY);
                }

                params.put(AnalyticsManager.KeyType.AREA, Util.isTextEmpty(mArea) ? AnalyticsManager.ValueType.EMPTY : mArea);
            }

            params.put(AnalyticsManager.KeyType.PRICE_OF_SELECTED_ROOM, Integer.toString(saleRoomInformation.averageDiscount));
            params.put(AnalyticsManager.KeyType.CHECK_IN_DATE, Long.toString(mCheckInSaleTime.getDayOfDaysDate().getTime()));

            return params;
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }

        return null;
    }

    private void shareKakao()
    {
        if (mDefaultImageUrl == null && mHotelDetail.getImageInformationList() != null && mHotelDetail.getImageInformationList().size() > 0)
        {
            mDefaultImageUrl = mHotelDetail.getImageInformationList().get(0).url;
        }

        String name = DailyPreference.getInstance(HotelDetailActivity.this).getUserName();

        if (Util.isTextEmpty(name) == true)
        {
            name = getString(R.string.label_friend) + "가";
        } else
        {
            name += "님이";
        }

        KakaoLinkManager.newInstance(HotelDetailActivity.this).shareHotel(name, mHotelDetail.hotelName, mHotelDetail.address//
            , mHotelDetail.hotelIndex//
            , mDefaultImageUrl//
            , mCheckInSaleTime, mHotelDetail.nights);

        SaleTime checkOutSaleTime = mCheckInSaleTime.getClone(mCheckInSaleTime.getOffsetDailyDay() + mHotelDetail.nights);

        HashMap<String, String> params = new HashMap<>();
        params.put(AnalyticsManager.KeyType.NAME, mHotelDetail.hotelName);
        params.put(AnalyticsManager.KeyType.CHECK_IN, mCheckInSaleTime.getDayOfDaysDateFormat("yyyy-MM-dd"));
        params.put(AnalyticsManager.KeyType.CHECK_OUT, checkOutSaleTime.getDayOfDaysDateFormat("yyyy-MM-dd"));

        SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREA);
        params.put(AnalyticsManager.KeyType.CURRENT_TIME, dateFormat2.format(new Date()));

        AnalyticsManager.getInstance(getApplicationContext()).recordEvent(AnalyticsManager.Category.HOTEL_BOOKINGS//
            , Action.SOCIAL_SHARE_CLICKED, mHotelDetail.hotelName, params);
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

            final Dialog shareDialog = new Dialog(HotelDetailActivity.this);
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

                    shareKakao();
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
        public void onClickImage(HotelDetail hotelDetail)
        {
            if (isLockUiComponent() == true)
            {
                return;
            }

            lockUiComponent();

            Intent intent = new Intent(HotelDetailActivity.this, ImageDetailListActivity.class);
            intent.putExtra(NAME_INTENT_EXTRA_DATA_IMAGEURLLIST, hotelDetail.getImageInformationList());
            intent.putExtra(NAME_INTENT_EXTRA_DATA_SELECTED_POSOTION, mCurrentImage);
            startActivityForResult(intent, CODE_REQUEST_ACTIVITY_IMAGELIST);
        }

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

        @Override
        public void onSelectedImagePosition(int position)
        {
            mCurrentImage = position;
        }

        @Override
        public void doBooking(SaleRoomInformation saleRoomInformation)
        {
            if (saleRoomInformation == null)
            {
                finish();
                return;
            }

            if (lockUiComponentAndIsLockUiComponent() == true)
            {
                return;
            }

            mSelectedSaleRoomInformation = saleRoomInformation;

            if (Util.isTextEmpty(DailyPreference.getInstance(HotelDetailActivity.this).getAuthorization()) == true)
            {
                startLoginActivity();
            } else
            {
                lockUI();

                DailyNetworkAPI.getInstance(HotelDetailActivity.this).requestUserInformationEx(mNetworkTag, mUserInformationExJsonResponseListener, HotelDetailActivity.this);
            }

            String label = String.format("%s-%s", mHotelDetail.hotelName, mSelectedSaleRoomInformation.roomName);
            AnalyticsManager.getInstance(HotelDetailActivity.this).recordEvent(AnalyticsManager.Category.HOTEL_BOOKINGS//
                , Action.BOOKING_CLICKED, label, recordAnalyticsBooking(mHotelDetail, saleRoomInformation));
        }

        @Override
        public void doKakaotalkConsult()
        {
            if (isLockUiComponent() == true || isFinishing() == true)
            {
                return;
            }

            lockUiComponent();

            Intent intent = new Intent(Intent.ACTION_SEND, Uri.parse("kakaolink://friend/@%EB%8D%B0%EC%9D%BC%EB%A6%AC%ED%98%B8%ED%85%94"));
            if (intent.resolveActivity(getPackageManager()) == null)
            {
                Util.installPackage(HotelDetailActivity.this, "com.kakao.talk");
            } else
            {
                startActivityForResult(intent, CODE_REQUEST_ACTIVITY_SHAREKAKAO);
            }

            AnalyticsManager.getInstance(HotelDetailActivity.this).recordEvent(AnalyticsManager.Category.HOTEL_BOOKINGS//
                , Action.KAKAO_INQUIRY_CLICKED, mHotelDetail.hotelName, null);
        }

        @Override
        public void showRoomType()
        {
            if (isLockUiComponent() == true || isFinishing() == true)
            {
                return;
            }

            lockUiComponent();

            if (mHotelDetailLayout != null)
            {
                mHotelDetailLayout.showAnimationRoomType();
            }

            releaseUiComponent();

            recordAnalyticsHotelDetail(Screen.DAILYHOTEL_DETAIL_ROOMTYPE, mHotelDetail);
            AnalyticsManager.getInstance(getApplicationContext()).recordEvent(AnalyticsManager.Category.HOTEL_BOOKINGS//
                , Action.ROOM_TYPE_CLICKED, mHotelDetail.hotelName, null);
        }

        @Override
        public void hideRoomType()
        {
            if (isLockUiComponent() == true || isFinishing() == true)
            {
                return;
            }

            lockUiComponent();

            if (mHotelDetailLayout != null)
            {
                mHotelDetailLayout.hideAnimationRoomType();
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

            Intent intent = ZoomMapActivity.newInstance(HotelDetailActivity.this//
                , ZoomMapActivity.SourceType.HOTEL, mHotelDetail.hotelName//
                , mHotelDetail.latitude, mHotelDetail.longitude, mHotelDetail.isOverseas);

            startActivityForResult(intent, CODE_REQUEST_ACTIVITY_ZOOMMAP);

            AnalyticsManager.getInstance(getApplicationContext()).recordEvent(AnalyticsManager.Category.HOTEL_BOOKINGS, Action.HOTEL_DETAIL_MAP_CLICKED, mHotelDetail.hotelName, null);
        }

        @Override
        public void clipAddress(String address)
        {
            Util.clipText(HotelDetailActivity.this, address);

            DailyToast.showToast(HotelDetailActivity.this, R.string.message_detail_copy_address, Toast.LENGTH_SHORT);

            AnalyticsManager.getInstance(getApplicationContext()).recordEvent(AnalyticsManager.Category.HOTEL_BOOKINGS, Action.HOTEL_DETAIL_ADDRESS_COPY_CLICKED, mHotelDetail.hotelName, null);
        }
    };

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Listener
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private DailyHotelJsonResponseListener mHotelDetailInformationJsonResponseListener = new DailyHotelJsonResponseListener()
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
                int msgCode = response.getInt("msgCode");

                // 0	성공
                // 4	데이터가 없을시
                // 5	판매 마감시
                switch (msgCode)
                {
                    case 100:
                    {
                        JSONObject dataJSONObject = response.getJSONObject("data");

                        mHotelDetail.setData(dataJSONObject);

                        if (mIsStartByShare == true)
                        {
                            mHotelDetail.categoryCode = mHotelDetail.grade.getName(HotelDetailActivity.this);

                            mIsStartByShare = false;
                            mDailyToolbarLayout.setToolbarText(mHotelDetail.hotelName);
                        }

                        if (mHotelDetailLayout != null)
                        {
                            mHotelDetailLayout.setHotelDetail(mHotelDetail, mCheckInSaleTime, mCurrentImage);
                        }

                        recordAnalyticsHotelDetail(Screen.DAILYHOTEL_DETAIL, mHotelDetail);
                        break;
                    }

                    case 5:
                    {
                        if (response.has("msg") == true)
                        {
                            String msg = response.getString("msg");

                            showSimpleDialog(getString(R.string.dialog_notice2), msg, getString(R.string.dialog_btn_text_confirm), null, new DialogInterface.OnDismissListener()
                            {
                                @Override
                                public void onDismiss(DialogInterface dialog)
                                {
                                    finish();
                                }
                            });
                        } else
                        {
                            throw new NullPointerException("response == null");
                        }
                        break;
                    }

                    case 4:
                    default:
                    {
                        if (response.has("msg") == true)
                        {
                            String msg = response.getString("msg");

                            DailyToast.showToast(HotelDetailActivity.this, msg, Toast.LENGTH_SHORT);
                            finish();
                        } else
                        {
                            throw new NullPointerException("response == null");
                        }
                        break;
                    }
                }
            } catch (JSONException e)
            {
                if (DEBUG == false)
                {
                    String message = url + " : " + response.toString();
                    Crashlytics.logException(new JSONException(message));
                }

                onError(e);
                finish();
            } catch (Exception e)
            {
                onError();
                finish();
            } finally
            {
                unLockUI();
            }
        }
    };

    private DailyHotelJsonResponseListener mUserInformationExJsonResponseListener = new DailyHotelJsonResponseListener()
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

                    if (isDailyUser == true)
                    {
                        DailyNetworkAPI.getInstance(HotelDetailActivity.this).requestUserInformation(mNetworkTag, mUserInformationJsonResponseListener, this);
                    } else
                    {
                        // 입력된 정보가 부족해.
                        if (Util.isTextEmpty(user.getEmail(), user.getPhone(), user.getName()) == true)
                        {
                            moveToAddSocialUserInformation(user);
                        } else if (Util.isValidatePhoneNumber(user.getPhone()) == false)
                        {
                            moveToUpdateUserPhoneNumber(user, EditProfilePhoneActivity.Type.WRONG_PHONENUMBER);
                        } else
                        {
                            moveToBooking(mHotelDetail, mSelectedSaleRoomInformation, mCheckInSaleTime);
                        }
                    }
                } else
                {
                    unLockUI();

                    String msg = response.getString("msg");

                    DailyToast.showToast(HotelDetailActivity.this, msg, Toast.LENGTH_SHORT);
                }
            } catch (Exception e)
            {
                onError(e);
            }
        }
    };

    private DailyHotelJsonResponseListener mUserInformationJsonResponseListener = new DailyHotelJsonResponseListener()
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
                Customer user = new Customer();
                user.setEmail(response.getString("email"));
                user.setName(response.getString("name"));
                user.setPhone(response.getString("phone"));
                user.setUserIdx(response.getString("idx"));

                boolean isVerified = response.getBoolean("is_verified");
                boolean isPhoneVerified = response.getBoolean("is_phone_verified");

                if (Util.isValidatePhoneNumber(user.getPhone()) == false)
                {
                    moveToUpdateUserPhoneNumber(user, EditProfilePhoneActivity.Type.NEED_VERIFICATION_PHONENUMBER);
                } else
                {
                    // 기존에 인증이 되었는데 인증이 해지되었다.
                    if (isVerified == true && isPhoneVerified == false)
                    {
                        moveToUpdateUserPhoneNumber(user, EditProfilePhoneActivity.Type.NEED_VERIFICATION_PHONENUMBER);
                    } else
                    {
                        moveToBooking(mHotelDetail, mSelectedSaleRoomInformation, mCheckInSaleTime);
                    }
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
        public void onErrorResponse(VolleyError volleyError)
        {

        }

        @Override
        public void onResponse(String url, JSONObject response)
        {
            if (isFinishing() == true)
            {
                return;
            }

            try
            {
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
                        DailyToast.showToast(HotelDetailActivity.this, R.string.toast_msg_dont_past_hotelinfo, Toast.LENGTH_LONG);
                        finish();
                        return;
                    }

                    // 호텔 정보를 가져온다.
                    DailyNetworkAPI.getInstance(HotelDetailActivity.this).requestHotelDetailInformation(mNetworkTag, mHotelDetail.hotelIndex, mCheckInSaleTime.getDayOfDaysDateFormat("yyyyMMdd"), mHotelDetail.nights, mHotelDetailInformationJsonResponseListener, HotelDetailActivity.this);
                } else
                {
                    DailyNetworkAPI.getInstance(HotelDetailActivity.this).requestHotelDetailInformation(mNetworkTag, mHotelDetail.hotelIndex, mCheckInSaleTime.getDayOfDaysDateFormat("yyyyMMdd"), mHotelDetail.nights, mHotelDetailInformationJsonResponseListener, HotelDetailActivity.this);
                }
            } catch (Exception e)
            {
                onError(e);
                unLockUI();

                finish();
            }
        }
    };

    private static class ImageHandler extends Handler
    {
        private final WeakReference<HotelDetailActivity> mWeakReference;

        public ImageHandler(HotelDetailActivity activity)
        {
            mWeakReference = new WeakReference<>(activity);
        }

        public void handleMessage(Message msg)
        {
            HotelDetailActivity hotelDetailActivity = mWeakReference.get();

            if (hotelDetailActivity == null)
            {
                return;
            }

            if (hotelDetailActivity.isFinishing() == true || hotelDetailActivity.mHotelDetailLayout == null)
            {
                return;
            }

            int direction = msg.arg1;

            hotelDetailActivity.mCurrentImage = hotelDetailActivity.mHotelDetailLayout.getCurrentImage();

            if (direction > 0)
            {
                hotelDetailActivity.mCurrentImage++;
            } else if (direction < 0)
            {
                hotelDetailActivity.mCurrentImage--;
            }

            hotelDetailActivity.mHotelDetailLayout.setCurrentImage(hotelDetailActivity.mCurrentImage);

            int autoSlide = msg.arg2;

            if (autoSlide == 1)
            {
                sendEmptyMessageDelayed(0, DURATION_HOTEL_IMAGE_SHOW);
            }
        }
    }
}
