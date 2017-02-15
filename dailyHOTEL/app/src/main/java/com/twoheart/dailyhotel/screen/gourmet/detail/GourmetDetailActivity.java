package com.twoheart.dailyhotel.screen.gourmet.detail;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.DraweeTransition;
import com.twoheart.dailyhotel.DailyHotel;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Area;
import com.twoheart.dailyhotel.model.Customer;
import com.twoheart.dailyhotel.model.Gourmet;
import com.twoheart.dailyhotel.model.GourmetDetail;
import com.twoheart.dailyhotel.model.ImageInformation;
import com.twoheart.dailyhotel.model.PlaceDetail;
import com.twoheart.dailyhotel.model.Province;
import com.twoheart.dailyhotel.model.RecentPlaces;
import com.twoheart.dailyhotel.model.SaleTime;
import com.twoheart.dailyhotel.model.TicketInformation;
import com.twoheart.dailyhotel.network.model.HomePlace;
import com.twoheart.dailyhotel.network.model.RecommendationGourmet;
import com.twoheart.dailyhotel.place.activity.PlaceDetailActivity;
import com.twoheart.dailyhotel.place.layout.PlaceDetailLayout;
import com.twoheart.dailyhotel.place.networkcontroller.PlaceDetailNetworkController;
import com.twoheart.dailyhotel.screen.common.ImageDetailListActivity;
import com.twoheart.dailyhotel.screen.common.ZoomMapActivity;
import com.twoheart.dailyhotel.screen.gourmet.filter.GourmetDetailCalendarActivity;
import com.twoheart.dailyhotel.screen.gourmet.payment.GourmetPaymentActivity;
import com.twoheart.dailyhotel.screen.mydaily.coupon.SelectGourmetCouponDialogActivity;
import com.twoheart.dailyhotel.screen.mydaily.member.EditProfilePhoneActivity;
import com.twoheart.dailyhotel.screen.mydaily.member.LoginActivity;
import com.twoheart.dailyhotel.screen.mydaily.wishlist.WishListTabActivity;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.DailyCalendar;
import com.twoheart.dailyhotel.util.DailyPreference;
import com.twoheart.dailyhotel.util.ExLog;
import com.twoheart.dailyhotel.util.KakaoLinkManager;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;
import com.twoheart.dailyhotel.widget.AlphaTransition;
import com.twoheart.dailyhotel.widget.DailyToast;
import com.twoheart.dailyhotel.widget.TextTransition;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Response;

public class GourmetDetailActivity extends PlaceDetailActivity
{
    TicketInformation mSelectedTicketInformation;
    private boolean mCheckPrice;

    /**
     * 리스트에서 호출
     *
     * @param context
     * @param saleTime
     * @param province
     * @param gourmet
     * @param listCount
     * @return
     */
    public static Intent newInstance(Context context, SaleTime saleTime, Province province, Gourmet gourmet//
        , int listCount, boolean isUsedMultiTransition)
    {
        Intent intent = new Intent(context, GourmetDetailActivity.class);

        intent.putExtra(NAME_INTENT_EXTRA_DATA_SALETIME, saleTime);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_PROVINCE, province);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_PLACEIDX, gourmet.index);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_PLACENAME, gourmet.name);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_IMAGEURL, gourmet.imageUrl);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_CATEGORY, gourmet.category);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_DISCOUNTPRICE, gourmet.discountPrice);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_PRICE, gourmet.price);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_CALENDAR_FLAG, false);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_ENTRY_INDEX, gourmet.entryPosition);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_LIST_COUNT, listCount);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_IS_DAILYCHOICE, gourmet.isDailyChoice);

        String[] area = gourmet.addressSummary.split("\\||l|ㅣ|I");
        intent.putExtra(NAME_INTENT_EXTRA_DATA_AREA, area[0].trim());

        String isShowOriginalPrice;
        if (gourmet.price <= 0 || gourmet.price <= gourmet.discountPrice)
        {
            isShowOriginalPrice = "N";
        } else
        {
            isShowOriginalPrice = "Y";
        }

        intent.putExtra(NAME_INTENT_EXTRA_DATA_IS_SHOW_ORIGINALPRICE, isShowOriginalPrice);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_IS_USED_MULTITRANSITIOIN, isUsedMultiTransition);

        return intent;
    }

    /**
     * 딥링크로 호출
     *
     * @param context
     * @param saleTime
     * @param gourmetIndex
     * @param isShowCalendar
     * @return
     */
    public static Intent newInstance(Context context, SaleTime saleTime, int gourmetIndex, int ticketIndex//
        , boolean isShowCalendar, boolean isUsedMultiTransition)
    {
        Intent intent = new Intent(context, GourmetDetailActivity.class);

        intent.putExtra(NAME_INTENT_EXTRA_DATA_TYPE, "share");
        intent.putExtra(NAME_INTENT_EXTRA_DATA_SALETIME, saleTime);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_PLACEIDX, gourmetIndex);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_TICKETINDEX, ticketIndex);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_CALENDAR_FLAG, isShowCalendar);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_ENTRY_INDEX, -1);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_LIST_COUNT, -1);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_IS_DAILYCHOICE, false);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_IS_USED_MULTITRANSITIOIN, isUsedMultiTransition);

        return intent;
    }

    /**
     * 딥링크로 호출
     */
    public static Intent newInstance(Context context, SaleTime startSaleTime, SaleTime endSaleTime//
        , int gourmetIndex, int ticketIndex, boolean isShowCalendar, boolean isUsedMultiTransition)
    {
        Intent intent = new Intent(context, GourmetDetailActivity.class);

        intent.putExtra(NAME_INTENT_EXTRA_DATA_TYPE, "share");
        intent.putExtra(INTENT_EXTRA_DATA_START_SALETIME, startSaleTime);
        intent.putExtra(INTENT_EXTRA_DATA_END_SALETIME, endSaleTime);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_PLACEIDX, gourmetIndex);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_TICKETINDEX, ticketIndex);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_CALENDAR_FLAG, isShowCalendar);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_ENTRY_INDEX, -1);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_LIST_COUNT, -1);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_IS_DAILYCHOICE, false);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_IS_USED_MULTITRANSITIOIN, isUsedMultiTransition);

        return intent;
    }

    /**
     * 검색 결과에서 호출
     *
     * @param context
     * @param saleTime
     * @param gourmet
     * @param listCount
     * @return
     */
    public static Intent newInstance(Context context, SaleTime saleTime, Gourmet gourmet, int listCount, boolean isUsedMultiTransition)
    {
        SaleTime startSaleTime = saleTime.getClone(0);

        return newInstance(context, saleTime, gourmet, startSaleTime, null, listCount, isUsedMultiTransition);
    }

    public static Intent newInstance(Context context, SaleTime saleTime, Gourmet gourmet, SaleTime startSaleTime//
        , SaleTime endSaleTime, int listCount, boolean isUsedMultiTransition)
    {
        Intent intent = new Intent(context, GourmetDetailActivity.class);

        intent.putExtra(NAME_INTENT_EXTRA_DATA_SALETIME, saleTime);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_PLACEIDX, gourmet.index);

        intent.putExtra(INTENT_EXTRA_DATA_START_SALETIME, startSaleTime);
        intent.putExtra(INTENT_EXTRA_DATA_END_SALETIME, endSaleTime);

        intent.putExtra(NAME_INTENT_EXTRA_DATA_PLACENAME, gourmet.name);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_IMAGEURL, gourmet.imageUrl);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_CATEGORY, gourmet.category);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_DISCOUNTPRICE, gourmet.discountPrice);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_PRICE, gourmet.price);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_CALENDAR_FLAG, false);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_ENTRY_INDEX, gourmet.entryPosition);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_LIST_COUNT, listCount);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_IS_DAILYCHOICE, gourmet.isDailyChoice);

        String isShowOriginalPrice;
        if (gourmet.price <= 0 || gourmet.price <= gourmet.discountPrice)
        {
            isShowOriginalPrice = "N";
        } else
        {
            isShowOriginalPrice = "Y";
        }

        intent.putExtra(NAME_INTENT_EXTRA_DATA_IS_SHOW_ORIGINALPRICE, isShowOriginalPrice);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_IS_USED_MULTITRANSITIOIN, isUsedMultiTransition);

        return intent;
    }

    public static Intent newInstance(Context context, SaleTime saleTime, HomePlace homePlace, boolean isUsedMultiTransition)
    {
        SaleTime startSaleTime = saleTime.getClone(0);

        Intent intent = new Intent(context, GourmetDetailActivity.class);

        intent.putExtra(NAME_INTENT_EXTRA_DATA_SALETIME, saleTime);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_PLACEIDX, homePlace.index);

        intent.putExtra(INTENT_EXTRA_DATA_START_SALETIME, startSaleTime);
        //        intent.putExtra(INTENT_EXTRA_DATA_END_SALETIME, endSaleTime);

        intent.putExtra(NAME_INTENT_EXTRA_DATA_PLACENAME, homePlace.title);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_IMAGEURL, homePlace.imageUrl);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_CATEGORY, homePlace.details.category);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_DISCOUNTPRICE, SKIP_CHECK_DISCOUNT_PRICE_VALUE);
//        intent.putExtra(NAME_INTENT_EXTRA_DATA_PRICE, SKIP_CHECK_DISCOUNT_PRICE_VALUE);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_CALENDAR_FLAG, false);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_ENTRY_INDEX, -1);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_LIST_COUNT, -1);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_IS_DAILYCHOICE, false);

        String isShowOriginalPrice = "N";

        intent.putExtra(NAME_INTENT_EXTRA_DATA_IS_SHOW_ORIGINALPRICE, isShowOriginalPrice);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_IS_USED_MULTITRANSITIOIN, isUsedMultiTransition);

        return intent;
    }

    public static Intent newInstance(Context context, SaleTime saleTime, RecommendationGourmet recommendationGourmet//
        , SaleTime startSaleTime, SaleTime endSaleTime, int listCount, boolean isUsedMultiTransition)
    {
        Intent intent = new Intent(context, GourmetDetailActivity.class);

        intent.putExtra(NAME_INTENT_EXTRA_DATA_SALETIME, saleTime);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_PLACEIDX, recommendationGourmet.index);

        intent.putExtra(INTENT_EXTRA_DATA_START_SALETIME, startSaleTime);
        intent.putExtra(INTENT_EXTRA_DATA_END_SALETIME, endSaleTime);

        intent.putExtra(NAME_INTENT_EXTRA_DATA_PLACENAME, recommendationGourmet.name);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_IMAGEURL, recommendationGourmet.imageUrl);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_CATEGORY, recommendationGourmet.category);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_DISCOUNTPRICE, recommendationGourmet.discount);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_PRICE, recommendationGourmet.price);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_CALENDAR_FLAG, false);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_ENTRY_INDEX, recommendationGourmet.entryPosition);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_LIST_COUNT, listCount);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_IS_DAILYCHOICE, recommendationGourmet.isDailyChoice);

        String isShowOriginalPrice;
        if (recommendationGourmet.price <= 0 || recommendationGourmet.price <= recommendationGourmet.discount)
        {
            isShowOriginalPrice = "N";
        } else
        {
            isShowOriginalPrice = "Y";
        }

        intent.putExtra(NAME_INTENT_EXTRA_DATA_IS_SHOW_ORIGINALPRICE, isShowOriginalPrice);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_IS_USED_MULTITRANSITIOIN, isUsedMultiTransition);

        return intent;
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

        mStartSaleTime = intent.getParcelableExtra(INTENT_EXTRA_DATA_START_SALETIME);
        mEndSaleTime = intent.getParcelableExtra(INTENT_EXTRA_DATA_END_SALETIME);

        mSaleTime = intent.getParcelableExtra(NAME_INTENT_EXTRA_DATA_SALETIME);

        boolean isShowCalendar = intent.getBooleanExtra(NAME_INTENT_EXTRA_DATA_CALENDAR_FLAG, false);

        if (mStartSaleTime != null && mEndSaleTime != null)
        {
            // 범위 지정인데 이미 날짜가 지난 경우
            if (mStartSaleTime.getOffsetDailyDay() == 0 && mEndSaleTime.getOffsetDailyDay() == 0)
            {
                showSimpleDialog(null, getString(R.string.message_end_event), getString(R.string.dialog_btn_text_yes), getString(R.string.dialog_btn_text_no), new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent eventIntent = new Intent();
                        eventIntent.setData(Uri.parse("dailyhotel://dailyhotel.co.kr?vc=10&v=el"));
                        startActivity(eventIntent);
                    }
                }, null);

                mEndSaleTime = null;

                // 이벤트 기간이 종료된 경우 달력을 띄우지 않는다.
                isShowCalendar = false;
            }

            if (mSaleTime == null)
            {
                mSaleTime = mStartSaleTime.getClone();
            }
        } else
        {
            if (mSaleTime == null)
            {
                Util.restartApp(this);
                return;
            }

            mStartSaleTime = mSaleTime.getClone(0);
            mEndSaleTime = null;
        }

        mPlaceDetail = createPlaceDetail(intent);

        // 최근 본 업장 저장
        RecentPlaces recentPlaces = new RecentPlaces(this);
        recentPlaces.add(PlaceType.FNB, mPlaceDetail.index);
        recentPlaces.savePreference();

        if (mSaleTime == null || mPlaceDetail == null)
        {
            Util.restartApp(this);
            return;
        }

        if (intent.hasExtra(NAME_INTENT_EXTRA_DATA_TYPE) == true)
        {
            mIsDeepLink = true;
            mDontReloadAtOnResume = false;
            mIsTransitionEnd = true;

            mOpenTicketIndex = intent.getIntExtra(NAME_INTENT_EXTRA_DATA_TICKETINDEX, 0);

            initLayout(null, null, false);

            if (isShowCalendar == true)
            {
                startCalendar(mSaleTime, mStartSaleTime, mEndSaleTime, mPlaceDetail.index, false);
            }
        } else
        {
            mIsDeepLink = false;

            String placeName = intent.getStringExtra(NAME_INTENT_EXTRA_DATA_PLACENAME);
            mDefaultImageUrl = intent.getStringExtra(NAME_INTENT_EXTRA_DATA_IMAGEURL);
            ((GourmetDetail) mPlaceDetail).category = intent.getStringExtra(NAME_INTENT_EXTRA_DATA_CATEGORY);

            if (placeName == null)
            {
                Util.restartApp(this);
                return;
            }

            mProvince = intent.getParcelableExtra(NAME_INTENT_EXTRA_DATA_PROVINCE);
            mArea = intent.getStringExtra(NAME_INTENT_EXTRA_DATA_AREA);
            mViewPrice = intent.getIntExtra(NAME_INTENT_EXTRA_DATA_DISCOUNTPRICE, 0);

            boolean isFromMap = intent.hasExtra(NAME_INTENT_EXTRA_DATA_FROM_MAP) == true;

            mIsUsedMultiTransition = intent.getBooleanExtra(NAME_INTENT_EXTRA_DATA_IS_USED_MULTITRANSITIOIN, false);

            if (mIsUsedMultiTransition == true)
            {
                initTransition();
            } else
            {
                mIsTransitionEnd = true;
            }

            initLayout(placeName, mDefaultImageUrl, isFromMap);

            if (isShowCalendar == true)
            {
                startCalendar(mSaleTime, mStartSaleTime, mEndSaleTime, mPlaceDetail.index, false);
            }
        }
    }

    private void initTransition()
    {
        if (mIsUsedMultiTransition == true)
        {
            TransitionSet inTransitionSet = DraweeTransition.createTransitionSet(ScalingUtils.ScaleType.CENTER_CROP, ScalingUtils.ScaleType.CENTER_CROP);
            Transition inNameTextTransition = new TextTransition(getResources().getColor(R.color.white), getResources().getColor(R.color.default_text_c323232)//
                , 17, 18, new LinearInterpolator());
            inNameTextTransition.addTarget(getString(R.string.transition_place_name));
            inTransitionSet.addTransition(inNameTextTransition);

            Transition inBottomAlphaTransition = new AlphaTransition(1.0f, 0.0f, new LinearInterpolator());
            inBottomAlphaTransition.addTarget(getString(R.string.transition_gradient_bottom_view));
            inTransitionSet.addTransition(inBottomAlphaTransition);

            Transition inTopAlphaTransition = new AlphaTransition(0.0f, 1.0f, new LinearInterpolator());
            inTopAlphaTransition.addTarget(getString(R.string.transition_gradient_top_view));
            inTransitionSet.addTransition(inTopAlphaTransition);

            getWindow().setSharedElementEnterTransition(inTransitionSet);

            TransitionSet outTransitionSet = DraweeTransition.createTransitionSet(ScalingUtils.ScaleType.CENTER_CROP, ScalingUtils.ScaleType.CENTER_CROP);
            Transition outNameTextTransition = new TextTransition(getResources().getColor(R.color.default_text_c323232), getResources().getColor(R.color.white)//
                , 18, 17, new LinearInterpolator());
            outNameTextTransition.addTarget(getString(R.string.transition_place_name));
            outTransitionSet.addTransition(outNameTextTransition);

            Transition outBottomAlphaTransition = new AlphaTransition(0.0f, 1.0f, new LinearInterpolator());
            outBottomAlphaTransition.addTarget(getString(R.string.transition_gradient_bottom_view));
            outTransitionSet.addTransition(outBottomAlphaTransition);

            Transition outTopAlphaTransition = new AlphaTransition(1.0f, 0.0f, new LinearInterpolator());
            outTopAlphaTransition.addTarget(getString(R.string.transition_gradient_top_view));
            outTransitionSet.addTransition(outTopAlphaTransition);

            outTransitionSet.setDuration(200);

            getWindow().setSharedElementReturnTransition(outTransitionSet);
            getWindow().getSharedElementEnterTransition().addListener(new Transition.TransitionListener()
            {
                @Override
                public void onTransitionStart(Transition transition)
                {

                }

                @Override
                public void onTransitionEnd(Transition transition)
                {
                    mPlaceDetailLayout.setTransImageVisibility(false);
                    mPlaceDetailLayout.setDefaultImage(mDefaultImageUrl);

                    // 딥링크가 아닌 경우에는 시간을 요청할 필요는 없다. 어떻게 할지 고민중
                    mIsTransitionEnd = true;

                    if (mInitializeStatus == STATUS_INITIALIZE_DATA)
                    {
                        mHandler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                updateDetailInformationLayout((GourmetDetail) mPlaceDetail);
                            }
                        });
                    } else
                    {
                        // 애니메이션이 끝났으나 아직 데이터가 로드 되지 않은 경우에는 프로그래스 바를 그리도록 한다.
                        lockUI();
                    }
                }

                @Override
                public void onTransitionCancel(Transition transition)
                {

                }

                @Override
                public void onTransitionPause(Transition transition)
                {

                }

                @Override
                public void onTransitionResume(Transition transition)
                {

                }
            });
        } else
        {
            mIsTransitionEnd = true;
        }
    }

    private void initLayout(String placeName, String imageUrl, boolean isFromMap)
    {
        setContentView(mPlaceDetailLayout.onCreateView(R.layout.activity_placedetail));

        if (mIsDeepLink == false && mIsUsedMultiTransition == true)
        {
            initTransLayout(placeName, imageUrl, isFromMap);
        } else
        {
            mPlaceDetailLayout.setDefaultImage(imageUrl);
        }

        mPlaceDetailLayout.setStatusBarHeight(this);
        mPlaceDetailLayout.setIsUsedMultiTransitions(mIsUsedMultiTransition);

        setLockUICancelable(true);
        initToolbar(placeName);

        mOnEventListener.hideActionBar(false);
    }

    private void initTransLayout(String placeName, String imageUrl, boolean isFromMap)
    {
        if (Util.isTextEmpty(imageUrl) == true)
        {
            return;
        }

        mPlaceDetailLayout.setTransImageView(imageUrl);
        ((GourmetDetailLayout) mPlaceDetailLayout).setTitleText(placeName);

        if (isFromMap == true)
        {
            mPlaceDetailLayout.setTransBottomGradientBackground(R.color.black_a28);
        }
    }

    @Override
    protected PlaceDetailLayout getDetailLayout(Context context)
    {
        return new GourmetDetailLayout(context, mOnEventListener);
    }

    @Override
    protected PlaceDetailNetworkController getNetworkController(Context context)
    {
        return new GourmetDetailNetworkController(context, mNetworkTag, mOnNetworkControllerListener);
    }

    @Override
    protected PlaceDetail createPlaceDetail(Intent intent)
    {
        if (intent == null)
        {
            return null;
        }

        int index = intent.getIntExtra(NAME_INTENT_EXTRA_DATA_PLACEIDX, -1);
        int entryIndex = intent.getIntExtra(NAME_INTENT_EXTRA_DATA_ENTRY_INDEX, -1);
        String isShowOriginalPrice = intent.getStringExtra(NAME_INTENT_EXTRA_DATA_IS_SHOW_ORIGINALPRICE);
        int listCount = intent.getIntExtra(NAME_INTENT_EXTRA_DATA_LIST_COUNT, -1);
        boolean isDailyChoice = intent.getBooleanExtra(NAME_INTENT_EXTRA_DATA_IS_DAILYCHOICE, false);

        return new GourmetDetail(index, entryIndex, isShowOriginalPrice, listCount, isDailyChoice);
    }

    @Override
    protected void shareKakao(PlaceDetail placeDetail, String imageUrl)
    {
        String name = DailyPreference.getInstance(GourmetDetailActivity.this).getUserName();

        if (Util.isTextEmpty(name) == true)
        {
            name = getString(R.string.label_friend) + "가";
        } else
        {
            name += "님이";
        }

        KakaoLinkManager.newInstance(this).shareGourmet(name, placeDetail.name, placeDetail.address//
            , placeDetail.index //
            , imageUrl //
            , mSaleTime);

        recordAnalyticsShared(placeDetail, AnalyticsManager.ValueType.KAKAO);
    }

    @Override
    protected void shareSMS(PlaceDetail placeDetail)
    {
        if (placeDetail == null)
        {
            return;
        }

        GourmetDetail gourmetDetail = (GourmetDetail) placeDetail;

        try
        {
            String name = DailyPreference.getInstance(GourmetDetailActivity.this).getUserName();

            if (Util.isTextEmpty(name) == true)
            {
                name = getString(R.string.label_friend) + "가";
            } else
            {
                name += "님이";
            }

            String message = getString(R.string.message_detail_gourmet_share_sms, //
                name, gourmetDetail.name, mSaleTime.getDayOfDaysDateFormat("yyyy.MM.dd (EEE)"),//
                gourmetDetail.address);

            Util.sendSms(this, message);
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }

        recordAnalyticsShared(placeDetail, AnalyticsManager.ValueType.MESSAGE);
    }

    private void recordAnalyticsShared(PlaceDetail placeDetail, String label)
    {
        try
        {
            HashMap<String, String> params = new HashMap<>();
            params.put(AnalyticsManager.KeyType.SERVICE, AnalyticsManager.ValueType.GOURMET);
            params.put(AnalyticsManager.KeyType.COUNTRY, placeDetail.isOverseas ? AnalyticsManager.ValueType.OVERSEAS : AnalyticsManager.ValueType.DOMESTIC);

            if (mProvince instanceof Area)
            {
                Area area = (Area) mProvince;
                params.put(AnalyticsManager.KeyType.PROVINCE, area.getProvince().name);
            } else
            {
                params.put(AnalyticsManager.KeyType.PROVINCE, mProvince.name);
            }

            if (DailyHotel.isLogin() == true)
            {
                params.put(AnalyticsManager.KeyType.USER_TYPE, AnalyticsManager.ValueType.MEMBER);

                switch (DailyPreference.getInstance(this).getUserType())
                {
                    case Constants.DAILY_USER:
                        params.put(AnalyticsManager.KeyType.MEMBER_TYPE, AnalyticsManager.UserType.EMAIL);
                        break;

                    case Constants.KAKAO_USER:
                        params.put(AnalyticsManager.KeyType.MEMBER_TYPE, AnalyticsManager.UserType.KAKAO);
                        break;

                    case Constants.FACEBOOK_USER:
                        params.put(AnalyticsManager.KeyType.MEMBER_TYPE, AnalyticsManager.UserType.FACEBOOK);
                        break;

                    default:
                        params.put(AnalyticsManager.KeyType.MEMBER_TYPE, AnalyticsManager.ValueType.EMPTY);
                        break;
                }
            } else
            {
                params.put(AnalyticsManager.KeyType.USER_TYPE, AnalyticsManager.ValueType.GUEST);
                params.put(AnalyticsManager.KeyType.MEMBER_TYPE, AnalyticsManager.ValueType.EMPTY);
            }

            params.put(AnalyticsManager.KeyType.PUSH_NOTIFICATION, DailyPreference.getInstance(this).isUserBenefitAlarm() ? "on" : "off");
            params.put(AnalyticsManager.KeyType.SHARE_METHOD, label);
            params.put(AnalyticsManager.KeyType.VENDOR_ID, Integer.toString(placeDetail.index));
            params.put(AnalyticsManager.KeyType.VENDOR_NAME, placeDetail.name);

            AnalyticsManager.getInstance(GourmetDetailActivity.this).recordEvent(AnalyticsManager.Category.SHARE//
                , AnalyticsManager.Action.GOURMET_ITEM_SHARE, label, params);
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }
    }

    @Override
    protected void startKakao()
    {
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
    }

    protected void processBooking(SaleTime saleTime, GourmetDetail gourmetDetail, TicketInformation ticketInformation)
    {
        if (saleTime == null || gourmetDetail == null || ticketInformation == null)
        {
            return;
        }

        String imageUrl = null;
        ArrayList<ImageInformation> mImageInformationList = gourmetDetail.getImageInformationList();

        if (mImageInformationList != null && mImageInformationList.size() > 0)
        {
            imageUrl = mImageInformationList.get(0).url;
        }

        boolean isBenefit = Util.isTextEmpty(gourmetDetail.benefit) == false;

        Intent intent = GourmetPaymentActivity.newInstance(GourmetDetailActivity.this, ticketInformation//
            , saleTime, imageUrl, gourmetDetail.category, gourmetDetail.index, isBenefit //
            , mProvince, mArea, gourmetDetail.isShowOriginalPrice, gourmetDetail.entryPosition //
            , gourmetDetail.isDailyChoice, gourmetDetail.ratingValue);

        startActivityForResult(intent, CODE_REQUEST_ACTIVITY_BOOKING);
    }

    @Override
    protected void onCalendarActivityResult(int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK)
        {
            hideSimpleDialog();

            SaleTime checkInSaleTime = data.getParcelableExtra(NAME_INTENT_EXTRA_DATA_SALETIME);

            if (checkInSaleTime == null)
            {
                return;
            }

            mSaleTime = checkInSaleTime;
            mPlaceDetail = new GourmetDetail(mPlaceDetail.index, mPlaceDetail.entryPosition, //
                mPlaceDetail.isShowOriginalPrice, mPlaceDetail.listCount, mPlaceDetail.isDailyChoice);

            ((GourmetDetailNetworkController) mPlaceDetailNetworkController).requestHasCoupon(mPlaceDetail.index,//
                mSaleTime.getDayOfDaysDateFormat("yyyy-MM-dd"));
        }
    }

    @Override
    protected void hideProductInformationLayout(boolean isAnimation)
    {
        mOnEventListener.hideProductInformationLayout(isAnimation);
    }

    @Override
    protected void doBooking()
    {
        mOnEventListener.doBooking();
    }

    @Override
    protected void downloadCoupon()
    {
        if (lockUiComponentAndIsLockUiComponent() == true)
        {
            return;
        }

        AnalyticsManager.getInstance(this).recordEvent(AnalyticsManager.Category.GOURMET_BOOKINGS, AnalyticsManager.Action.GOURMET_COUPON_DOWNLOAD, mPlaceDetail.name, null);

        if (DailyHotel.isLogin() == false)
        {
            showSimpleDialog(getString(R.string.dialog_notice2), getString(R.string.message_detail_please_login), //
                getString(R.string.dialog_btn_login_for_benefit), getString(R.string.dialog_btn_text_close), //
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        AnalyticsManager.getInstance(GourmetDetailActivity.this).recordEvent(AnalyticsManager.Category.POPUP_BOXES, AnalyticsManager.Action.COUPON_LOGIN, AnalyticsManager.Label.LOGIN, null);

                        Intent intent = LoginActivity.newInstance(GourmetDetailActivity.this, AnalyticsManager.Screen.DAILYHOTEL_DETAIL);
                        startActivityForResult(intent, CODE_REQUEST_ACTIVITY_LOGIN_BY_COUPON);
                    }
                }, new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        AnalyticsManager.getInstance(GourmetDetailActivity.this).recordEvent(AnalyticsManager.Category.POPUP_BOXES, AnalyticsManager.Action.COUPON_LOGIN, AnalyticsManager.Label.CLOSED, null);
                    }
                }, new DialogInterface.OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                        AnalyticsManager.getInstance(GourmetDetailActivity.this).recordEvent(AnalyticsManager.Category.POPUP_BOXES, AnalyticsManager.Action.COUPON_LOGIN, AnalyticsManager.Label.CLOSED, null);
                    }
                }, new DialogInterface.OnDismissListener()
                {
                    @Override
                    public void onDismiss(DialogInterface dialog)
                    {
                        releaseUiComponent();
                    }
                }, true);
        } else
        {
            Intent intent = SelectGourmetCouponDialogActivity.newInstance(this, mPlaceDetail.index, //
                mSaleTime.getDayOfDaysDateFormat("yyyy-MM-dd"), mPlaceDetail.name);
            startActivityForResult(intent, CODE_REQUEST_ACTIVITY_DOWNLOAD_COUPON);
        }
    }

    @Override
    protected void recordAnalyticsShareClicked()
    {
        AnalyticsManager.getInstance(this).recordEvent(AnalyticsManager.Category.SHARE,//
            AnalyticsManager.Action.ITEM_SHARE, AnalyticsManager.Label.GOURMET, null);
    }

    void updateDetailInformationLayout(GourmetDetail gourmetDetail)
    {
        switch (mInitializeStatus)
        {
            case STATUS_INITIALIZE_DATA:
                mInitializeStatus = STATUS_INITIALIZE_LAYOUT;
                break;

            case STATUS_INITIALIZE_COMPLETE:
                break;

            default:
                return;
        }

        if (mIsDeepLink == true)
        {
            mDailyToolbarLayout.setToolbarText(gourmetDetail.name);
        }

        if (mPlaceDetailLayout != null)
        {
            ((GourmetDetailLayout) mPlaceDetailLayout).setDetail(mSaleTime, gourmetDetail, mCurrentImage);
        }

        if (mCheckPrice == false)
        {
            mCheckPrice = true;
            checkGourmetTicket(mIsDeepLink, gourmetDetail, mViewPrice);
        }

        // 딥링크로 메뉴 오픈 요청
        if (mIsDeepLink == true && mOpenTicketIndex > 0 && gourmetDetail.getTicketInformation().size() > 0)
        {
            if (mPlaceDetailLayout != null)
            {
                mPlaceDetailLayout.showProductInformationLayout(mOpenTicketIndex);
                mPlaceDetailLayout.hideWishButton();
            }
        }

        mOpenTicketIndex = 0;
        mIsDeepLink = false;
        mInitializeStatus = STATUS_INITIALIZE_COMPLETE;
    }

    private void checkGourmetTicket(boolean isDeepLink, GourmetDetail gourmetDetail, int listViewPrice)
    {
        // 판매 완료 혹은 가격이 변동되었는지 조사한다
        ArrayList<TicketInformation> ticketInformationList = gourmetDetail.getTicketInformation();

        if (ticketInformationList == null || ticketInformationList.size() == 0)
        {
            showSimpleDialog(getString(R.string.dialog_notice2), getString(R.string.message_gourmet_detail_sold_out)//
                , getString(R.string.dialog_btn_text_confirm), null, new DialogInterface.OnDismissListener()
                {
                    @Override
                    public void onDismiss(DialogInterface dialog)
                    {
                        setResultCode(CODE_RESULT_ACTIVITY_REFRESH);
                    }
                });

            if (isDeepLink == true)
            {
                AnalyticsManager.getInstance(GourmetDetailActivity.this).recordEvent(AnalyticsManager.Category.POPUP_BOXES,//
                    AnalyticsManager.Action.SOLDOUT_DEEPLINK, gourmetDetail.name, null);
            } else
            {
                AnalyticsManager.getInstance(GourmetDetailActivity.this).recordEvent(AnalyticsManager.Category.POPUP_BOXES,//
                    AnalyticsManager.Action.SOLDOUT, gourmetDetail.name, null);
            }
        } else
        {
            if (isDeepLink == false)
            {
                boolean hasPrice = false;

                if (listViewPrice == SKIP_CHECK_DISCOUNT_PRICE_VALUE) {
                    hasPrice = true;
                } else
                {
                    for (TicketInformation ticketInformation : ticketInformationList)
                    {
                        if (listViewPrice == ticketInformation.discountPrice)
                        {
                            hasPrice = true;
                            break;
                        }
                    }
                }

                if (hasPrice == false)
                {
                    setResultCode(CODE_RESULT_ACTIVITY_REFRESH);

                    showSimpleDialog(getString(R.string.dialog_notice2), getString(R.string.message_gourmet_detail_changed_price)//
                        , getString(R.string.dialog_btn_text_confirm), null, new DialogInterface.OnDismissListener()
                        {
                            @Override
                            public void onDismiss(DialogInterface dialog)
                            {
                                mOnEventListener.showProductInformationLayout();
                            }
                        });

                    AnalyticsManager.getInstance(GourmetDetailActivity.this).recordEvent(AnalyticsManager.Category.POPUP_BOXES,//
                        AnalyticsManager.Action.SOLDOUT_CHANGEPRICE, gourmetDetail.name, null);
                }
            }
        }
    }

    void startCalendar(SaleTime saleTime, SaleTime startSaleTime, SaleTime endSaleTime, int placeIndex, boolean isAnimation)
    {
        if (isFinishing() == true || lockUiComponentAndIsLockUiComponent() == true)
        {
            return;
        }

        String callByScreen;
        if (mIsDeepLink == true)
        {
            callByScreen = AnalyticsManager.Label.EVENT;
        } else
        {
            callByScreen = AnalyticsManager.ValueType.DETAIL;
        }

        Intent intent = GourmetDetailCalendarActivity.newInstance(GourmetDetailActivity.this, //
            saleTime, startSaleTime, endSaleTime, placeIndex, callByScreen, true, isAnimation);
        startActivityForResult(intent, Constants.CODE_REQUEST_ACTIVITY_CALENDAR);

        AnalyticsManager.getInstance(GourmetDetailActivity.this).recordEvent(AnalyticsManager.Category.NAVIGATION_//
            , AnalyticsManager.Action.GOURMET_BOOKING_CALENDAR_CLICKED, AnalyticsManager.ValueType.DETAIL, null);
    }

    protected void recordAnalyticsGourmetDetail(String screen, SaleTime saleTime, GourmetDetail gourmetDetail)
    {
        if (saleTime == null || gourmetDetail == null)
        {
            return;
        }

        try
        {
            Map<String, String> params = new HashMap<>();
            params.put(AnalyticsManager.KeyType.NAME, gourmetDetail.name);
            params.put(AnalyticsManager.KeyType.GRADE, gourmetDetail.grade.name()); // 14
            params.put(AnalyticsManager.KeyType.CATEGORY, gourmetDetail.category);
            params.put(AnalyticsManager.KeyType.DBENEFIT, Util.isTextEmpty(gourmetDetail.benefit) ? "no" : "yes");

            if (gourmetDetail.getTicketInformation() == null || gourmetDetail.getTicketInformation().size() == 0)
            {
                params.put(AnalyticsManager.KeyType.PRICE, "0");
            } else
            {
                params.put(AnalyticsManager.KeyType.PRICE, Integer.toString(gourmetDetail.getTicketInformation().get(0).discountPrice));
            }

            params.put(AnalyticsManager.KeyType.PLACE_INDEX, Integer.toString(gourmetDetail.index));
            params.put(AnalyticsManager.KeyType.DATE, saleTime.getDayOfDaysDateFormat("yyyy-MM-dd"));

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
                    params.put(AnalyticsManager.KeyType.DISTRICT, AnalyticsManager.ValueType.ALL_LOCALE_KR);
                }

                params.put(AnalyticsManager.KeyType.AREA, Util.isTextEmpty(mArea) ? AnalyticsManager.ValueType.EMPTY : mArea);
            }

            params.put(AnalyticsManager.KeyType.UNIT_PRICE, Integer.toString(mViewPrice));
            params.put(AnalyticsManager.KeyType.VISIT_DATE, Long.toString(saleTime.getDayOfDaysDate().getTime()));

            String listIndex = gourmetDetail.entryPosition == -1 //
                ? AnalyticsManager.ValueType.EMPTY : Integer.toString(gourmetDetail.entryPosition);

            params.put(AnalyticsManager.KeyType.LIST_INDEX, listIndex);

            String placeCount = gourmetDetail.listCount == -1 //
                ? AnalyticsManager.ValueType.EMPTY : Integer.toString(gourmetDetail.listCount);

            params.put(AnalyticsManager.KeyType.PLACE_COUNT, placeCount);

            params.put(AnalyticsManager.KeyType.RATING, Integer.toString(gourmetDetail.ratingValue));
            params.put(AnalyticsManager.KeyType.IS_SHOW_ORIGINAL_PRICE, gourmetDetail.isShowOriginalPrice);
            params.put(AnalyticsManager.KeyType.DAILYCHOICE, gourmetDetail.isDailyChoice ? "y" : "n");
            params.put(AnalyticsManager.KeyType.LENGTH_OF_STAY, "1");

            AnalyticsManager.getInstance(this).recordScreen(this, screen, null, params);
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }
    }

    protected Map<String, String> recordAnalyticsBooking(SaleTime saleTime, GourmetDetail gourmetDetail, TicketInformation ticketInformation)
    {
        if (saleTime == null || gourmetDetail == null || ticketInformation == null)
        {
            return null;
        }

        try
        {
            Map<String, String> params = new HashMap<>();
            params.put(AnalyticsManager.KeyType.NAME, gourmetDetail.name);
            params.put(AnalyticsManager.KeyType.CATEGORY, gourmetDetail.category);

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
                    params.put(AnalyticsManager.KeyType.DISTRICT, AnalyticsManager.ValueType.ALL_LOCALE_KR);
                }

                params.put(AnalyticsManager.KeyType.AREA, Util.isTextEmpty(mArea) ? AnalyticsManager.ValueType.EMPTY : mArea);
            }

            params.put(AnalyticsManager.KeyType.PRICE_OF_SELECTED_TICKET, Integer.toString(ticketInformation.discountPrice));
            params.put(AnalyticsManager.KeyType.VISIT_DATE, Long.toString(saleTime.getDayOfDaysDate().getTime()));

            return params;
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }

        return null;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Listener
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    GourmetDetailLayout.OnEventListener mOnEventListener = new GourmetDetailLayout.OnEventListener()
    {
        @Override
        public void doBooking(TicketInformation ticketInformation)
        {
            if (ticketInformation == null)
            {
                finish();
                return;
            }

            if (lockUiComponentAndIsLockUiComponent() == true)
            {
                return;
            }

            mSelectedTicketInformation = ticketInformation;

            if (DailyHotel.isLogin() == false)
            {
                startLoginActivity(AnalyticsManager.Screen.DAILYGOURMET_DETAIL);
            } else
            {
                lockUI();
                mPlaceDetailNetworkController.requestProfile();
            }

            String label = String.format("%s-%s", mPlaceDetail.name, mSelectedTicketInformation.name);
            AnalyticsManager.getInstance(GourmetDetailActivity.this).recordEvent(AnalyticsManager.Category.GOURMET_BOOKINGS//
                , AnalyticsManager.Action.BOOKING_CLICKED, label, recordAnalyticsBooking(mSaleTime, (GourmetDetail) mPlaceDetail, mSelectedTicketInformation));
        }

        @Override
        public void doBooking()
        {
            doBooking(mSelectedTicketInformation);
        }

        @Override
        public void downloadCoupon()
        {
            GourmetDetailActivity.this.downloadCoupon();
        }

        @Override
        public void showActionBar(boolean isAnimation)
        {
            mDailyToolbarLayout.setToolbarVisibility(true, isAnimation);
        }

        @Override
        public void hideActionBar(boolean isAnimation)
        {
            mDailyToolbarLayout.setToolbarVisibility(false, isAnimation);
        }

        @Override
        public void onClickImage(PlaceDetail placeDetail)
        {
            if (isLockUiComponent() == true)
            {
                return;
            }

            ArrayList<ImageInformation> imageInformationArrayList = placeDetail.getImageInformationList();
            if (imageInformationArrayList.size() == 0)
            {
                return;
            }

            lockUiComponent();

            Intent intent = ImageDetailListActivity.newInstance(GourmetDetailActivity.this, PlaceType.FNB, placeDetail.name, imageInformationArrayList, mCurrentImage);
            startActivityForResult(intent, CODE_REQUEST_ACTIVITY_IMAGELIST);

            AnalyticsManager.getInstance(GourmetDetailActivity.this).recordEvent(AnalyticsManager.Category.GOURMET_BOOKINGS,//
                AnalyticsManager.Action.GOURMET_IMAGE_CLICKED, placeDetail.name, null);
        }

        @Override
        public void onSelectedImagePosition(int position)
        {
            mCurrentImage = position;
        }

        @Override
        public void onConciergeClick()
        {
            if (isLockUiComponent() == true || isFinishing() == true)
            {
                return;
            }

            lockUiComponent();

            showCallDialog();
        }

        @Override
        public void showProductInformationLayout()
        {
            if (isLockUiComponent() == true || isFinishing() == true)
            {
                return;
            }

            lockUiComponent();

            if (mPlaceDetailLayout != null)
            {
                mPlaceDetailLayout.showAnimationProductInformationLayout();
                mPlaceDetailLayout.hideWishButtonAnimation();
            }

            if (Util.isOverAPI21() == true)
            {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(R.color.textView_textColor_shadow_soldout));
            }

            releaseUiComponent();

            recordAnalyticsGourmetDetail(AnalyticsManager.Screen.DAILYGOURMET_DETAIL_TICKETTYPE, mSaleTime, (GourmetDetail) mPlaceDetail);
            AnalyticsManager.getInstance(GourmetDetailActivity.this).recordEvent(AnalyticsManager.Category.GOURMET_BOOKINGS//
                , AnalyticsManager.Action.TICKET_TYPE_CLICKED, mPlaceDetail.name, null);
        }

        @Override
        public void hideProductInformationLayout(boolean isAnimation)
        {
            if (isLockUiComponent() == true || isFinishing() == true)
            {
                return;
            }

            lockUiComponent();

            if (mPlaceDetailLayout != null)
            {
                if (isAnimation == true)
                {
                    mPlaceDetailLayout.hideAnimationProductInformationLayout();
                    mPlaceDetailLayout.showWishButtonAnimation();
                } else
                {
                    mPlaceDetailLayout.hideProductInformationLayout();
                    mPlaceDetailLayout.showWishButton();
                }
            }

            if (Util.isOverAPI21() == true)
            {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(R.color.white));
            }

            releaseUiComponent();
        }

        @Override
        public void showMap()
        {
            if (Util.isInstallGooglePlayService(GourmetDetailActivity.this) == true)
            {
                if (lockUiComponentAndIsLockUiComponent() == true || isFinishing() == true)
                {
                    return;
                }

                Intent intent = ZoomMapActivity.newInstance(GourmetDetailActivity.this//
                    , ZoomMapActivity.SourceType.GOURMET, mPlaceDetail.name, mPlaceDetail.address//
                    , mPlaceDetail.latitude, mPlaceDetail.longitude, false);

                startActivityForResult(intent, CODE_REQUEST_ACTIVITY_ZOOMMAP);

                AnalyticsManager.getInstance(getApplicationContext()).recordEvent(AnalyticsManager.Category.GOURMET_BOOKINGS,//
                    AnalyticsManager.Action.GOURMET_DETAIL_MAP_CLICKED, mPlaceDetail.name, null);
            } else
            {
                Util.installGooglePlayService(GourmetDetailActivity.this);
            }
        }

        @Override
        public void finish()
        {
            GourmetDetailActivity.this.finish();
        }

        @Override
        public void clipAddress(String address)
        {
            Util.clipText(GourmetDetailActivity.this, address);

            DailyToast.showToast(GourmetDetailActivity.this, R.string.message_detail_copy_address, Toast.LENGTH_SHORT);

            AnalyticsManager.getInstance(getApplicationContext()).recordEvent(AnalyticsManager.Category.GOURMET_BOOKINGS,//
                AnalyticsManager.Action.GOURMET_DETAIL_ADDRESS_COPY_CLICKED, mPlaceDetail.name, null);
        }

        @Override
        public void showNavigatorDialog()
        {
            if (lockUiComponentAndIsLockUiComponent() == true)
            {
                return;
            }

            Util.showShareMapDialog(GourmetDetailActivity.this, mPlaceDetail.name//
                , mPlaceDetail.latitude, mPlaceDetail.longitude, false//
                , AnalyticsManager.Category.GOURMET_BOOKINGS//
                , AnalyticsManager.Action.GOURMET_DETAIL_NAVIGATION_APP_CLICKED//
                , null);
        }

        @Override
        public void onCalendarClick()
        {
            startCalendar(mSaleTime, mStartSaleTime, mEndSaleTime, mPlaceDetail.index, true);
        }

        @Override
        public void onWishButtonClick()
        {
            if (DailyHotel.isLogin() == false)
            {
                DailyToast.showToast(GourmetDetailActivity.this, R.string.toast_msg_please_login, Toast.LENGTH_LONG);

                Intent intent = LoginActivity.newInstance(GourmetDetailActivity.this, AnalyticsManager.Screen.DAILYGOURMET_DETAIL);
                startActivityForResult(intent, CODE_REQUEST_ACTIVITY_LOGIN_BY_DETAIL_WISHLIST);
            } else
            {
                GourmetDetailActivity.this.onWishButtonClick(PlaceType.FNB, mPlaceDetail);
            }
        }

        @Override
        public void releaseUiComponent()
        {
            GourmetDetailActivity.this.releaseUiComponent();
        }
    };

    private GourmetDetailNetworkController.OnNetworkControllerListener mOnNetworkControllerListener = new GourmetDetailNetworkController.OnNetworkControllerListener()
    {
        @Override
        public void onCommonDateTime(long currentDateTime, long dailyDateTime)
        {
            if (mIsDeepLink == true)
            {
                mSaleTime.setCurrentTime(currentDateTime);
                long shareDailyTime = mSaleTime.getDayOfDaysDate().getTime();

                int shareDailyDay = Integer.parseInt(DailyCalendar.format(shareDailyTime, "yyyyMMdd", TimeZone.getTimeZone("GMT")));
                int todayDailyDay = Integer.parseInt(DailyCalendar.format(dailyDateTime, "yyyyMMdd", TimeZone.getTimeZone("GMT")));

                // 지난 날의 호텔인 경우.
                if (shareDailyDay < todayDailyDay)
                {
                    unLockUI();
                    DailyToast.showToast(GourmetDetailActivity.this, R.string.toast_msg_dont_past_hotelinfo, Toast.LENGTH_LONG);
                    finish();
                    return;
                }
            }

            ((GourmetDetailNetworkController) mPlaceDetailNetworkController).requestHasCoupon(mPlaceDetail.index,//
                mSaleTime.getDayOfDaysDateFormat("yyyy-MM-dd"));
        }

        @Override
        public void onUserProfile(Customer user, String birthday, boolean isDailyUser, boolean isVerified, boolean isPhoneVerified)
        {
            if (isDailyUser == true)
            {
                if (Util.isValidatePhoneNumber(user.getPhone()) == false)
                {
                    moveToUpdateUserPhoneNumber(user, EditProfilePhoneActivity.Type.NEED_VERIFICATION_PHONENUMBER, user.getPhone());
                } else
                {
                    // 기존에 인증이 되었는데 인증이 해지되었다.
                    if (isVerified == true && isPhoneVerified == false)
                    {
                        moveToUpdateUserPhoneNumber(user, EditProfilePhoneActivity.Type.NEED_VERIFICATION_PHONENUMBER, user.getPhone());
                    } else
                    {
                        processBooking(mSaleTime, (GourmetDetail) mPlaceDetail, mSelectedTicketInformation);
                    }
                }
            } else
            {
                // 입력된 정보가 부족해.
                if (Util.isTextEmpty(user.getEmail(), user.getPhone(), user.getName()) == true)
                {
                    moveToAddSocialUserInformation(user, birthday);
                } else if (Util.isValidatePhoneNumber(user.getPhone()) == false)
                {
                    moveToUpdateUserPhoneNumber(user, EditProfilePhoneActivity.Type.WRONG_PHONENUMBER, user.getPhone());
                } else
                {
                    processBooking(mSaleTime, (GourmetDetail) mPlaceDetail, mSelectedTicketInformation);
                }
            }
        }

        @Override
        public void onGourmetDetailInformation(JSONObject dataJSONObject)
        {
            try
            {
                mPlaceDetail.setData(dataJSONObject);

                if (mInitializeStatus == STATUS_INITIALIZE_NONE)
                {
                    mInitializeStatus = STATUS_INITIALIZE_DATA;
                }

                if (mIsTransitionEnd == true)
                {
                    updateDetailInformationLayout((GourmetDetail) mPlaceDetail);
                }

                recordAnalyticsGourmetDetail(AnalyticsManager.Screen.DAILYGOURMET_DETAIL, mSaleTime, (GourmetDetail) mPlaceDetail);
            } catch (Exception e)
            {
                DailyToast.showToast(GourmetDetailActivity.this, R.string.act_base_network_connect, Toast.LENGTH_LONG);
                finish();
            } finally
            {
                unLockUI();
            }
        }

        @Override
        public void onHasCoupon(boolean hasCoupon)
        {
            mPlaceDetail.hasCoupon = hasCoupon;

            ((GourmetDetailNetworkController) mPlaceDetailNetworkController).requestGourmetDetailInformation(mSaleTime.getDayOfDaysDateFormat("yyyy-MM-dd"), mPlaceDetail.index);
        }

        public void onAddWishList(boolean isSuccess, String message)
        {
            if (isSameCallingActivity(WishListTabActivity.class.getName()) == true)
            {
                if (mResultIntent == null)
                {
                    mResultIntent = new Intent();
                    mResultIntent.putExtra(NAME_INTENT_EXTRA_DATA_IS_CHANGE_WISHLIST, true);
                }
                setResultCode(CODE_RESULT_ACTIVITY_REFRESH);
            }

            if (isSuccess == true)
            {
                mPlaceDetail.myWish = true;
                int wishCount = ++mPlaceDetail.wishCount;
                mPlaceDetailLayout.setWishButtonCount(wishCount);
                mPlaceDetailLayout.setWishButtonSelected(true);
                mPlaceDetailLayout.setUpdateWishPopup(PlaceDetailLayout.WishPopupState.ADD);

                try
                {
                    Map<String, String> params = new HashMap<>();
                    params.put(AnalyticsManager.KeyType.PLACE_TYPE, AnalyticsManager.ValueType.GOURMET);
                    params.put(AnalyticsManager.KeyType.NAME, mPlaceDetail.name);
                    params.put(AnalyticsManager.KeyType.VALUE, Integer.toString(mViewPrice));
                    params.put(AnalyticsManager.KeyType.COUNTRY, mPlaceDetail.isOverseas ? AnalyticsManager.ValueType.OVERSEAS : AnalyticsManager.ValueType.DOMESTIC);
                    params.put(AnalyticsManager.KeyType.CATEGORY, ((GourmetDetail) mPlaceDetail).category);

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
                            params.put(AnalyticsManager.KeyType.DISTRICT, AnalyticsManager.ValueType.ALL_LOCALE_KR);
                        }

                        params.put(AnalyticsManager.KeyType.AREA, Util.isTextEmpty(mArea) ? AnalyticsManager.ValueType.EMPTY : mArea);
                    }

                    params.put(AnalyticsManager.KeyType.GRADE, ((GourmetDetail) mPlaceDetail).grade.name());
                    params.put(AnalyticsManager.KeyType.PLACE_INDEX, Integer.toString(mPlaceDetail.index));
                    params.put(AnalyticsManager.KeyType.RATING, Integer.toString(mPlaceDetail.ratingValue));

                    String listIndex = mPlaceDetail.entryPosition == -1 //
                        ? AnalyticsManager.ValueType.EMPTY : Integer.toString(mPlaceDetail.entryPosition);

                    params.put(AnalyticsManager.KeyType.LIST_INDEX, listIndex);
                    params.put(AnalyticsManager.KeyType.DAILYCHOICE, mPlaceDetail.isDailyChoice ? "y" : "n");
                    params.put(AnalyticsManager.KeyType.DBENEFIT, Util.isTextEmpty(mPlaceDetail.benefit) ? "no" : "yes");

                    params.put(AnalyticsManager.KeyType.CHECK_IN, mSaleTime.getDayOfDaysDateFormat("yyyy-MM-dd"));
                    params.put(AnalyticsManager.KeyType.LENGTH_OF_STAY, "1");
                    params.put(AnalyticsManager.KeyType.IS_SHOW_ORIGINAL_PRICE, mPlaceDetail.isShowOriginalPrice);


                    AnalyticsManager.getInstance(GourmetDetailActivity.this).recordEvent(//
                        AnalyticsManager.Category.NAVIGATION_,//
                        AnalyticsManager.Action.WISHLIST_ON, mPlaceDetail.name, params);
                } catch (Exception e)
                {
                    ExLog.d(e.toString());
                }
            } else
            {
                mPlaceDetailLayout.setWishButtonCount(mPlaceDetail.wishCount);
                mPlaceDetailLayout.setWishButtonSelected(mPlaceDetail.myWish);

                if (Util.isTextEmpty(message) == true)
                {
                    message = "";
                }

                releaseUiComponent();

                showSimpleDialog(getResources().getString(R.string.dialog_notice2), message//
                    , getResources().getString(R.string.dialog_btn_text_confirm), null);
            }
        }

        @Override
        public void onRemoveWishList(boolean isSuccess, String message)
        {
            if (isSameCallingActivity(WishListTabActivity.class.getName()) == true)
            {
                if (mResultIntent == null)
                {
                    mResultIntent = new Intent();
                    mResultIntent.putExtra(NAME_INTENT_EXTRA_DATA_IS_CHANGE_WISHLIST, true);
                }
                setResultCode(CODE_RESULT_ACTIVITY_REFRESH);
            }

            if (isSuccess == true)
            {
                mPlaceDetail.myWish = false;
                int wishCount = --mPlaceDetail.wishCount;
                mPlaceDetailLayout.setWishButtonCount(wishCount);
                mPlaceDetailLayout.setWishButtonSelected(false);
                mPlaceDetailLayout.setUpdateWishPopup(PlaceDetailLayout.WishPopupState.DELETE);

                Map<String, String> params = new HashMap<>();
                params.put(AnalyticsManager.KeyType.PLACE_TYPE, AnalyticsManager.ValueType.GOURMET);
                params.put(AnalyticsManager.KeyType.NAME, mPlaceDetail.name);
                params.put(AnalyticsManager.KeyType.VALUE, Integer.toString(mViewPrice));
                params.put(AnalyticsManager.KeyType.COUNTRY, mPlaceDetail.isOverseas ? AnalyticsManager.ValueType.OVERSEAS : AnalyticsManager.ValueType.DOMESTIC);
                params.put(AnalyticsManager.KeyType.CATEGORY, ((GourmetDetail) mPlaceDetail).category);

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
                        params.put(AnalyticsManager.KeyType.DISTRICT, AnalyticsManager.ValueType.ALL_LOCALE_KR);
                    }

                    params.put(AnalyticsManager.KeyType.AREA, Util.isTextEmpty(mArea) ? AnalyticsManager.ValueType.EMPTY : mArea);
                }

                params.put(AnalyticsManager.KeyType.GRADE, ((GourmetDetail) mPlaceDetail).grade.name());
                params.put(AnalyticsManager.KeyType.PLACE_INDEX, Integer.toString(mPlaceDetail.index));
                params.put(AnalyticsManager.KeyType.RATING, Integer.toString(mPlaceDetail.ratingValue));

                String listIndex = mPlaceDetail.entryPosition == -1 //
                    ? AnalyticsManager.ValueType.EMPTY : Integer.toString(mPlaceDetail.entryPosition);

                params.put(AnalyticsManager.KeyType.LIST_INDEX, listIndex);
                params.put(AnalyticsManager.KeyType.DAILYCHOICE, mPlaceDetail.isDailyChoice ? "y" : "n");
                params.put(AnalyticsManager.KeyType.DBENEFIT, Util.isTextEmpty(mPlaceDetail.benefit) ? "no" : "yes");

                params.put(AnalyticsManager.KeyType.CHECK_IN, mSaleTime.getDayOfDaysDateFormat("yyyy-MM-dd"));
                params.put(AnalyticsManager.KeyType.LENGTH_OF_STAY, "1");
                params.put(AnalyticsManager.KeyType.IS_SHOW_ORIGINAL_PRICE, mPlaceDetail.isShowOriginalPrice);


                AnalyticsManager.getInstance(GourmetDetailActivity.this).recordEvent(//
                    AnalyticsManager.Category.NAVIGATION_,//
                    AnalyticsManager.Action.WISHLIST_OFF, mPlaceDetail.name, params);
            } else
            {
                mPlaceDetailLayout.setWishButtonCount(mPlaceDetail.wishCount);
                mPlaceDetailLayout.setWishButtonSelected(mPlaceDetail.myWish);

                if (Util.isTextEmpty(message) == true)
                {
                    message = "";
                }

                releaseUiComponent();

                showSimpleDialog(getResources().getString(R.string.dialog_notice2), message//
                    , getResources().getString(R.string.dialog_btn_text_confirm), null);
            }
        }

        @Override
        public void onError(Throwable e)
        {
            setResultCode(CODE_RESULT_ACTIVITY_REFRESH);
            GourmetDetailActivity.this.onError(e);
        }

        @Override
        public void onErrorPopupMessage(int msgCode, String message)
        {
            setResultCode(CODE_RESULT_ACTIVITY_REFRESH);

            // 판매 마감시
            if (msgCode == 5)
            {
                GourmetDetailActivity.this.onErrorPopupMessage(msgCode, message, null);
            } else
            {
                GourmetDetailActivity.this.onErrorPopupMessage(msgCode, message);
            }

        }

        @Override
        public void onErrorToastMessage(String message)
        {
            setResultCode(CODE_RESULT_ACTIVITY_REFRESH);
            GourmetDetailActivity.this.onErrorToastMessage(message);
            finish();
        }

        @Override
        public void onErrorResponse(Call call, Response response)
        {
            setResultCode(CODE_RESULT_ACTIVITY_REFRESH);
            GourmetDetailActivity.this.onErrorResponse(call, response);
            finish();
        }
    };
}
