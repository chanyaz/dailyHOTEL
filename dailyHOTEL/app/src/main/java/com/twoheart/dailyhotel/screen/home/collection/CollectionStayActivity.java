package com.twoheart.dailyhotel.screen.home.collection;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.SharedElementCallback;
import android.view.View;

import com.daily.base.BaseActivity;
import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ExLog;
import com.daily.base.util.ScreenUtils;
import com.daily.dailyhotel.entity.CommonDateTime;
import com.daily.dailyhotel.parcel.analytics.StayDetailAnalyticsParam;
import com.daily.dailyhotel.screen.common.calendar.stay.StayCalendarActivity;
import com.daily.dailyhotel.screen.common.dialog.wish.WishDialogActivity;
import com.daily.dailyhotel.screen.home.stay.inbound.detail.StayDetailActivity;
import com.daily.dailyhotel.screen.home.stay.inbound.preview.StayPreviewActivity;
import com.daily.dailyhotel.storage.preference.DailyRemoteConfigPreference;
import com.daily.dailyhotel.view.DailyStayCardView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.twoheart.dailyhotel.DailyHotel;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.PlaceViewItem;
import com.twoheart.dailyhotel.model.time.PlaceBookingDay;
import com.twoheart.dailyhotel.model.time.StayBookingDay;
import com.twoheart.dailyhotel.network.model.Recommendation;
import com.twoheart.dailyhotel.network.model.RecommendationPlace;
import com.twoheart.dailyhotel.network.model.RecommendationPlaceList;
import com.twoheart.dailyhotel.network.model.RecommendationStay;
import com.twoheart.dailyhotel.network.model.Sticker;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.DailyCalendar;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;

public class CollectionStayActivity extends CollectionBaseActivity
{
    protected static final String INTENT_EXTRA_DATA_TYPE = "type";
    protected static final String INTENT_EXTRA_DATA_INDEX = "index";
    protected static final String INTENT_EXTRA_DATA_IMAGE_URL = "imageUrl";
    protected static final String INTENT_EXTRA_DATA_TITLE = "title";
    protected static final String INTENT_EXTRA_DATA_SUBTITLE = "subTitle";
    protected static final String INTENT_EXTRA_DATA_CHECK_IN_DATE = "checkInDate";
    protected static final String INTENT_EXTRA_DATA_CHECK_OUT_DATE = "checkOutDate";
    protected static final String INTENT_EXTRA_DATA_AFTER_DAY = "afterDay";
    protected static final String INTENT_EXTRA_DATA_NIGHTS = "nights";

    private static final int TYPE_DEFAULT = 0;
    private static final int TYPE_DATE = 1;
    private static final int TYPE_AFTER_DAY = 2;

    private StayBookingDay mStartStayBookingDay;
    private int mType;
    private int mAfterDay, mNights;
    boolean mActiveReward;

    /**
     * @param context
     * @param index
     * @param checkInDateTime  ISO-8601
     * @param checkOutDateTime ISO-8601
     * @return
     */
    public static Intent newInstance(Context context, int index, String checkInDateTime, String checkOutDateTime)
    {
        Intent intent = new Intent(context, CollectionStayActivity.class);

        intent.putExtra(INTENT_EXTRA_DATA_TYPE, TYPE_DATE);
        intent.putExtra(INTENT_EXTRA_DATA_INDEX, index);
        intent.putExtra(INTENT_EXTRA_DATA_CHECK_IN_DATE, checkInDateTime);
        intent.putExtra(INTENT_EXTRA_DATA_CHECK_OUT_DATE, checkOutDateTime);

        return intent;
    }

    public static Intent newInstance(Context context, int index, int afterDay, int nights)
    {
        Intent intent = new Intent(context, CollectionStayActivity.class);

        intent.putExtra(INTENT_EXTRA_DATA_TYPE, TYPE_AFTER_DAY);
        intent.putExtra(INTENT_EXTRA_DATA_INDEX, index);
        intent.putExtra(INTENT_EXTRA_DATA_AFTER_DAY, afterDay);
        intent.putExtra(INTENT_EXTRA_DATA_NIGHTS, nights);

        return intent;
    }

    public static Intent newInstance(Context context, int index, String imageUrl, String title, String subTitle //
        , String checkInDateTime, String checkOutDateTime, boolean isUsedMultiTransition)
    {
        Intent intent = new Intent(context, CollectionStayActivity.class);

        intent.putExtra(INTENT_EXTRA_DATA_TYPE, TYPE_DEFAULT);
        intent.putExtra(INTENT_EXTRA_DATA_INDEX, index);
        intent.putExtra(INTENT_EXTRA_DATA_IMAGE_URL, imageUrl);
        intent.putExtra(INTENT_EXTRA_DATA_TITLE, title);
        intent.putExtra(INTENT_EXTRA_DATA_SUBTITLE, subTitle);
        intent.putExtra(NAME_INTENT_EXTRA_DATA_IS_USED_MULTITRANSITIOIN, isUsedMultiTransition);

        if (DailyTextUtils.isTextEmpty(checkInDateTime) == false)
        {
            intent.putExtra(INTENT_EXTRA_DATA_CHECK_IN_DATE, checkInDateTime);
        }

        if (DailyTextUtils.isTextEmpty(checkOutDateTime) == false)
        {
            intent.putExtra(INTENT_EXTRA_DATA_CHECK_OUT_DATE, checkOutDateTime);
        }

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

        mRecommendationIndex = intent.getIntExtra(INTENT_EXTRA_DATA_INDEX, -1);
        mIsUsedMultiTransition = intent.getBooleanExtra(NAME_INTENT_EXTRA_DATA_IS_USED_MULTITRANSITIOIN, false);

        if (mRecommendationIndex <= 0)
        {
            finish();
            return;
        }

        mType = intent.getIntExtra(INTENT_EXTRA_DATA_TYPE, TYPE_DEFAULT);

        String title = null;
        String subTitle = null;
        String imageUrl = null;

        switch (mType)
        {
            case TYPE_DEFAULT:
            {
                title = intent.getStringExtra(INTENT_EXTRA_DATA_TITLE);
                subTitle = intent.getStringExtra(INTENT_EXTRA_DATA_SUBTITLE);
                imageUrl = intent.getStringExtra(INTENT_EXTRA_DATA_IMAGE_URL);

                String checkInDateTime = intent.getStringExtra(INTENT_EXTRA_DATA_CHECK_IN_DATE);
                String checkOutDateTime = intent.getStringExtra(INTENT_EXTRA_DATA_CHECK_OUT_DATE);

                if (DailyTextUtils.isTextEmpty(checkInDateTime) == false)
                {
                    try
                    {
                        mStartStayBookingDay = new StayBookingDay();
                        mStartStayBookingDay.setCheckInDay(checkInDateTime);

                        if (DailyTextUtils.isTextEmpty(checkOutDateTime) == false)
                        {
                            mStartStayBookingDay.setCheckOutDay(checkOutDateTime);
                        } else
                        {
                            mStartStayBookingDay.setCheckOutDay(checkInDateTime, 1);
                        }
                    } catch (Exception e)
                    {
                        mStartStayBookingDay = null;
                    }
                }

                break;
            }

            case TYPE_DATE:
            {
                String checkInDateTime = intent.getStringExtra(INTENT_EXTRA_DATA_CHECK_IN_DATE);
                String checkOutDateTime = intent.getStringExtra(INTENT_EXTRA_DATA_CHECK_OUT_DATE);

                if (DailyTextUtils.isTextEmpty(checkInDateTime, checkOutDateTime) == false)
                {
                    try
                    {
                        mStartStayBookingDay = new StayBookingDay();
                        mStartStayBookingDay.setCheckInDay(checkInDateTime);
                        mStartStayBookingDay.setCheckOutDay(checkOutDateTime);
                    } catch (Exception e)
                    {
                        mStartStayBookingDay = null;
                    }
                }
                break;
            }

            case TYPE_AFTER_DAY:
            {
                mAfterDay = intent.getIntExtra(INTENT_EXTRA_DATA_AFTER_DAY, 0);
                mNights = intent.getIntExtra(INTENT_EXTRA_DATA_NIGHTS, 1);
                break;
            }
        }

        mCollectionBaseLayout = new CollectionStayLayout(this, mOnEventListener);

        setContentView(mCollectionBaseLayout.onCreateView(R.layout.activity_collection_search));

        mCollectionBaseLayout.setUsedMultiTransition(mIsUsedMultiTransition);

        if (mType == TYPE_DEFAULT && mIsUsedMultiTransition == true)
        {
            mCollectionBaseLayout.setTitleLayoutData(title, subTitle, imageUrl);
            mCollectionBaseLayout.notifyChangedTitleLayout();

            initTransition();
        } else
        {
            mCollectionBaseLayout.setTitleLayoutData(title, subTitle, imageUrl);
            mCollectionBaseLayout.notifyChangedTitleLayout();

            lockUI();

            requestCommonDateTime();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        unLockUI();

        switch (requestCode)
        {
            case CODE_REQUEST_ACTIVITY_STAY_DETAIL:
            {
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                    case CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_READY:
                    case CODE_RESULT_ACTIVITY_GO_HOME:
                        setResult(resultCode);
                        finish();
                        break;

                    case CODE_RESULT_ACTIVITY_REFRESH:
                        lockUI();

                        requestCommonDateTime();
                        break;

                    case com.daily.base.BaseActivity.RESULT_CODE_REFRESH:
                        if (data == null)
                        {
                            lockUI();

                            requestCommonDateTime();
                        } else
                        {
                            if (data.hasExtra(StayDetailActivity.INTENT_EXTRA_DATA_CHANGED_PRICE) == true//
                                || data.hasExtra(StayDetailActivity.INTENT_EXTRA_DATA_SOLD_OUT) == true)
                            {
                                lockUI();

                                requestCommonDateTime();
                            } else
                            {
                                onChangedWish(mWishPosition, data.getBooleanExtra(StayDetailActivity.INTENT_EXTRA_DATA_WISH, false));
                            }
                        }
                        break;
                }
                break;
            }

            case CODE_REQUEST_ACTIVITY_CALENDAR:
                onCalendarActivityResult(resultCode, data);
                break;

            case CODE_REQUEST_ACTIVITY_PREVIEW:
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                        Observable.create(new ObservableOnSubscribe<Object>()
                        {
                            @Override
                            public void subscribe(ObservableEmitter<Object> e) throws Exception
                            {
                                onPlaceDetailClickByLongPress(mViewByLongPress, mPlaceViewItemByLongPress, mListCountByLongPress);
                            }
                        }).subscribeOn(AndroidSchedulers.mainThread()).subscribe();
                        break;

                    case BaseActivity.RESULT_CODE_REFRESH:
                        if (data != null && data.hasExtra(StayDetailActivity.INTENT_EXTRA_DATA_WISH) == true)
                        {
                            onChangedWish(mWishPosition, data.getBooleanExtra(StayDetailActivity.INTENT_EXTRA_DATA_WISH, false));
                        } else
                        {
                            lockUI();

                            requestCommonDateTime();
                        }
                        break;
                }
                break;

            case Constants.CODE_REQUEST_ACTIVITY_WISH_DIALOG:
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                    case BaseActivity.RESULT_CODE_ERROR:
                        if (data != null)
                        {
                            onChangedWish(mWishPosition, data.getBooleanExtra(WishDialogActivity.INTENT_EXTRA_DATA_WISH, false));
                        }
                        break;

                    case com.daily.base.BaseActivity.RESULT_CODE_REFRESH:
                        lockUI();

                        requestCommonDateTime();
                        break;
                }
                break;
        }
    }

    @Override
    protected void requestRecommendationPlaceList(PlaceBookingDay placeBookingDay)
    {
        if (placeBookingDay == null)
        {
            return;
        }

        mCollectionBaseLayout.setListScrollTop();

        try
        {
            StayBookingDay stayBookingDay = (StayBookingDay) placeBookingDay;

            String salesDate = stayBookingDay.getCheckInDay("yyyy-MM-dd");
            int period = stayBookingDay.getNights();

            addCompositeDisposable(Observable.zip(mRecommendationRemoteImpl.getRecommendationList() //
                , mRecommendationRemoteImpl.getRecommendationStayList(mRecommendationIndex, salesDate, period) //
                , new BiFunction<List<Recommendation>, RecommendationPlaceList<RecommendationStay>, ArrayList<PlaceViewItem>>()
                {
                    @Override
                    public ArrayList<PlaceViewItem> apply(List<Recommendation> recommendationList, RecommendationPlaceList<RecommendationStay> recommendationPlaceList) throws Exception
                    {
                        Recommendation recommendation = recommendationPlaceList.recommendation;
                        mCollectionBaseLayout.setTitleLayoutData(recommendation.title, recommendation.subtitle //
                            , ScreenUtils.getResolutionImageUrl(CollectionStayActivity.this //
                                , recommendation.defaultImageUrl, recommendation.lowResolutionImageUrl));

                        long currentTime, endTime;
                        try
                        {
                            currentTime = DailyCalendar.convertDate(mCommonDateTime.currentDateTime, DailyCalendar.ISO_8601_FORMAT).getTime();
                            endTime = DailyCalendar.convertDate(recommendation.endedAt, DailyCalendar.ISO_8601_FORMAT).getTime();
                        } catch (Exception e)
                        {
                            ExLog.d(e.toString());

                            currentTime = 0;
                            endTime = -1;
                        }

                        mIsOverShowDate = endTime < currentTime;

                        mActiveReward = recommendationPlaceList.configurations.activeReward;

                        ArrayList<RecommendationStay> stayList = new ArrayList<>(recommendationPlaceList.items);
                        ArrayList<PlaceViewItem> placeViewItems = makePlaceList( //
                            recommendationPlaceList.imageBaseUrl, (mIsOverShowDate ? null : stayList), recommendationPlaceList.stickers);

                        for (Recommendation item : recommendationList)
                        {
                            if (item.idx == recommendation.idx)
                            {
                                recommendationList.remove(item);
                                break;
                            }
                        }

                        placeViewItems.add(new PlaceViewItem(PlaceViewItem.TYPE_RECOMMEND_VIEW, recommendationList));

                        return placeViewItems;
                    }
                }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<ArrayList<PlaceViewItem>>()
            {
                @Override
                public void accept(ArrayList<PlaceViewItem> placeViewItems) throws Exception
                {
                    mCollectionBaseLayout.notifyChangedTitleLayout();
                    onPlaceList(mIsOverShowDate, placeViewItems, mActiveReward);
                    unLockUI();
                }
            }, new Consumer<Throwable>()
            {
                @Override
                public void accept(Throwable throwable) throws Exception
                {
                    onHandleError(throwable);
                }
            }));
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    @Override
    protected String getCalendarDate(PlaceBookingDay placeBookingDay)
    {
        if (placeBookingDay == null)
        {
            return null;
        }

        StayBookingDay stayBookingDay = (StayBookingDay) placeBookingDay;

        try
        {
            String checkInDate = stayBookingDay.getCheckInDay("yyyy.MM.dd(EEE)");
            String checkOutDate = stayBookingDay.getCheckOutDay("yyyy.MM.dd(EEE)");

            int nights = stayBookingDay.getNights();

            return String.format(Locale.KOREA, "%s - %s, %d박", checkInDate, checkOutDate, nights);
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }

        return null;
    }

    @Override
    protected void setPlaceBookingDay(CommonDateTime commonDateTime)
    {
        if (commonDateTime == null)
        {
            return;
        }

        try
        {
            StayBookingDay stayBookingDay = new StayBookingDay();
            stayBookingDay.setCheckInDay(commonDateTime.dailyDateTime);
            stayBookingDay.setCheckOutDay(commonDateTime.dailyDateTime, 1);

            switch (mType)
            {
                case TYPE_DEFAULT:
                    if (mStartStayBookingDay != null)
                    {
                        try
                        {
                            int startCheckInDay = Integer.parseInt(mStartStayBookingDay.getCheckInDay("yyyyMMdd"));
                            int dailyCheckInDay = Integer.parseInt(stayBookingDay.getCheckInDay("yyyyMMdd"));

                            // 데일리타임 이후 날짜인 경우에는
                            if (startCheckInDay >= dailyCheckInDay)
                            {
                                stayBookingDay.setCheckInDay(mStartStayBookingDay.getCheckInDay(DailyCalendar.ISO_8601_FORMAT));
                                stayBookingDay.setCheckOutDay(mStartStayBookingDay.getCheckOutDay(DailyCalendar.ISO_8601_FORMAT));
                            }
                        } catch (Exception e)
                        {
                            ExLog.e(e.toString());
                        }

                        mStartStayBookingDay = null;
                    }
                    break;

                case TYPE_DATE:
                    if (mStartStayBookingDay != null)
                    {
                        try
                        {
                            int startCheckInDay = Integer.parseInt(mStartStayBookingDay.getCheckInDay("yyyyMMdd"));
                            int dailyCheckInDay = Integer.parseInt(stayBookingDay.getCheckInDay("yyyyMMdd"));

                            // 데일리타임 이후 날짜인 경우에는
                            if (startCheckInDay >= dailyCheckInDay)
                            {
                                stayBookingDay.setCheckInDay(mStartStayBookingDay.getCheckInDay(DailyCalendar.ISO_8601_FORMAT));
                                stayBookingDay.setCheckOutDay(mStartStayBookingDay.getCheckOutDay(DailyCalendar.ISO_8601_FORMAT));
                            }
                        } catch (Exception e)
                        {
                            ExLog.e(e.toString());
                        }

                        mStartStayBookingDay = null;
                    }

                    mType = TYPE_DEFAULT;
                    break;

                case TYPE_AFTER_DAY:
                    if (mAfterDay >= 0 && mNights > 0)
                    {
                        try
                        {
                            stayBookingDay.setCheckInDay(commonDateTime.dailyDateTime, mAfterDay);
                            stayBookingDay.setCheckOutDay(commonDateTime.dailyDateTime, mAfterDay + mNights);
                        } catch (Exception e)
                        {
                            ExLog.e(e.toString());
                        }

                        mAfterDay = -1;
                        mNights = -1;
                    }

                    mType = TYPE_DEFAULT;
                    break;
            }

            mPlaceBookingDay = stayBookingDay;
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    @Override
    protected void onCalendarActivityResult(int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK)
        {
            try
            {
                String checkInDateTime = data.getStringExtra(StayCalendarActivity.INTENT_EXTRA_DATA_CHECK_IN_DATETIME);
                String checkOutDateTime = data.getStringExtra(StayCalendarActivity.INTENT_EXTRA_DATA_CHECK_OUT_DATETIME);

                StayBookingDay stayBookingDay = new StayBookingDay();
                stayBookingDay.setCheckInDay(checkInDateTime);
                stayBookingDay.setCheckOutDay(checkOutDateTime);

                mPlaceBookingDay = stayBookingDay;

                mCollectionBaseLayout.setCalendarText(getCalendarDate(stayBookingDay));

                lockUI();

                requestRecommendationPlaceList(stayBookingDay);
            } catch (Exception e)
            {
                ExLog.e(e.toString());
            }
        }
    }

    @Override
    protected void startCalendarActivity(CommonDateTime commonDateTime, PlaceBookingDay placeBookingDay)
    {
        if (commonDateTime == null || placeBookingDay == null)
        {
            return;
        }

        final int DAYS_OF_MAX_COUNT = 60;

        try
        {
            Calendar calendar = DailyCalendar.getInstance();
            calendar.setTime(DailyCalendar.convertDate(commonDateTime.dailyDateTime, DailyCalendar.ISO_8601_FORMAT));

            String startDateTime = DailyCalendar.format(calendar.getTime(), DailyCalendar.ISO_8601_FORMAT);

            calendar.add(Calendar.DAY_OF_MONTH, DAYS_OF_MAX_COUNT - 1);

            String endDateTime = DailyCalendar.format(calendar.getTime(), DailyCalendar.ISO_8601_FORMAT);

            StayBookingDay stayBookingDay = (StayBookingDay) placeBookingDay;

            Intent intent = StayCalendarActivity.newInstance(CollectionStayActivity.this//
                , startDateTime, endDateTime, DAYS_OF_MAX_COUNT - 1//
                , stayBookingDay.getCheckInDay(DailyCalendar.ISO_8601_FORMAT)//
                , stayBookingDay.getCheckOutDay(DailyCalendar.ISO_8601_FORMAT)//
                , AnalyticsManager.ValueType.SEARCH, true//
                , 0, true);

            startActivityForResult(intent, CODE_REQUEST_ACTIVITY_CALENDAR);
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    @Override
    protected String getSectionTitle(int count)
    {
        return getString(R.string.label_count_stay, count);
    }

    @Override
    protected void onCommonDateTime(CommonDateTime commonDateTime)
    {
        mCommonDateTime = commonDateTime;

        setPlaceBookingDay(commonDateTime);

        mCollectionBaseLayout.setCalendarText(getCalendarDate(mPlaceBookingDay));

        requestRecommendationPlaceList(mPlaceBookingDay);
    }

    @Override
    protected ArrayList<PlaceViewItem> makePlaceList(String imageBaseUrl, List<? extends RecommendationPlace> placeList, List<Sticker> stickerList)
    {
        ArrayList<PlaceViewItem> placeViewItemList = new ArrayList<>();

        // 빈공간
        placeViewItemList.add(new PlaceViewItem(PlaceViewItem.TYPE_HEADER_VIEW, null));

        if (placeList == null || placeList.size() == 0)
        {
            placeViewItemList.add(new PlaceViewItem(PlaceViewItem.TYPE_EMPTY_VIEW, null));
        } else
        {
            // 개수 넣기
            //            placeViewItemList.add(new PlaceViewItem(PlaceViewItem.TYPE_SECTION, getSectionTitle(placeList.size())));

            int entryPosition = 0;

            for (RecommendationPlace place : placeList)
            {
                place.imageUrl = imageBaseUrl + place.imageUrl;
                place.entryPosition = entryPosition++;

                placeViewItemList.add(new PlaceViewItem(PlaceViewItem.TYPE_ENTRY, place));
            }

            placeViewItemList.add(new PlaceViewItem(PlaceViewItem.TYPE_FOOTER_VIEW, null));
        }

        return placeViewItemList;
    }

    @Override
    protected void onPlaceDetailClickByLongPress(View view, PlaceViewItem placeViewItem, int listCount)
    {
        if (view == null || placeViewItem == null)
        {
            return;
        }

        mOnEventListener.onPlaceClick(mWishPosition, view, placeViewItem, listCount);
    }

    @Override
    protected void onChangedWish(int position, boolean wish)
    {
        if (position < 0)
        {
            return;
        }

        if (mCollectionBaseLayout == null)
        {
            Util.restartApp(this);
            return;
        }

        PlaceViewItem placeViewItem = mCollectionBaseLayout.getItem(position);

        if (placeViewItem == null)
        {
            return;
        }

        RecommendationStay recommendationStay = placeViewItem.getItem();
        recommendationStay.myWish = wish;
        mCollectionBaseLayout.notifyWishChanged(position, wish);
    }

    private CollectionStayLayout.OnEventListener mOnEventListener = new CollectionBaseLayout.OnEventListener()
    {
        @Override
        public void onCalendarClick()
        {
            startCalendarActivity(mCommonDateTime, mPlaceBookingDay);
        }

        @SuppressLint("RestrictedApi")
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onPlaceClick(int position, View view, PlaceViewItem placeViewItem, int count)
        {
            if (placeViewItem == null || placeViewItem.mType != PlaceViewItem.TYPE_ENTRY)
            {
                return;
            }

            RecommendationStay recommendationStay = placeViewItem.getItem();

            mWishPosition = position;

            StayDetailAnalyticsParam analyticsParam = new StayDetailAnalyticsParam();
            analyticsParam.setAddressAreaName(recommendationStay.addrSummary);
            analyticsParam.discountPrice = recommendationStay.discount;
            analyticsParam.price = recommendationStay.price;
            analyticsParam.setShowOriginalPriceYn(analyticsParam.price, analyticsParam.discountPrice);
            analyticsParam.setRegion(null);
            analyticsParam.entryPosition = recommendationStay.entryPosition;
            analyticsParam.totalListCount = count;
            analyticsParam.isDailyChoice = recommendationStay.isDailyChoice;
            analyticsParam.gradeName = recommendationStay.grade;

            StayBookingDay stayBookingDay = (StayBookingDay) mPlaceBookingDay;

            if (mIsUsedMultiTransition == true)
            {
                setExitSharedElementCallback(new SharedElementCallback()
                {
                    @Override
                    public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots)
                    {
                        super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);

                        for (View view : sharedElements)
                        {
                            if (view instanceof SimpleDraweeView)
                            {
                                view.setVisibility(View.VISIBLE);
                                break;
                            }
                        }
                    }
                });

                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(CollectionStayActivity.this, ((DailyStayCardView) view).getOptionsCompat());

                Intent intent = StayDetailActivity.newInstance(CollectionStayActivity.this //
                    , recommendationStay.index, recommendationStay.name, recommendationStay.imageUrl, recommendationStay.discount//
                    , stayBookingDay.getCheckInDay(DailyCalendar.ISO_8601_FORMAT)//
                    , stayBookingDay.getCheckOutDay(DailyCalendar.ISO_8601_FORMAT)//
                    , null, null//
                    , true, StayDetailActivity.TransGradientType.LIST, analyticsParam);

                startActivityForResult(intent, CODE_REQUEST_ACTIVITY_STAY_DETAIL, optionsCompat.toBundle());
            } else
            {
                Intent intent = StayDetailActivity.newInstance(CollectionStayActivity.this //
                    , recommendationStay.index, recommendationStay.name, recommendationStay.imageUrl, recommendationStay.discount//
                    , stayBookingDay.getCheckInDay(DailyCalendar.ISO_8601_FORMAT)//
                    , stayBookingDay.getCheckOutDay(DailyCalendar.ISO_8601_FORMAT)//
                    , null, null//
                    , false, StayDetailActivity.TransGradientType.NONE, analyticsParam);

                startActivityForResult(intent, CODE_REQUEST_ACTIVITY_STAY_DETAIL);

                overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
            }

            AnalyticsManager.getInstance(CollectionStayActivity.this).recordEvent(//
                AnalyticsManager.Category.HOME_RECOMMEND, Integer.toString(mRecommendationIndex),//
                Integer.toString(recommendationStay.index), null);

            // 할인 쿠폰이 보이는 경우
            if (DailyTextUtils.isTextEmpty(recommendationStay.couponDiscountText) == false)
            {
                AnalyticsManager.getInstance(CollectionStayActivity.this).recordEvent(AnalyticsManager.Category.PRODUCT_LIST//
                    , AnalyticsManager.Action.COUPON_STAY, Integer.toString(recommendationStay.index), null);
            }

            if (recommendationStay.reviewCount > 0)
            {
                AnalyticsManager.getInstance(CollectionStayActivity.this).recordEvent(AnalyticsManager.Category.PRODUCT_LIST//
                    , AnalyticsManager.Action.TRUE_REVIEW_STAY, Integer.toString(recommendationStay.index), null);
            }

            if (recommendationStay.truevr == true)
            {
                AnalyticsManager.getInstance(CollectionStayActivity.this).recordEvent(AnalyticsManager.Category.NAVIGATION//
                    , AnalyticsManager.Action.STAY_ITEM_CLICK_TRUE_VR, Integer.toString(recommendationStay.index), null);
            }

            if (DailyRemoteConfigPreference.getInstance(CollectionStayActivity.this).isKeyRemoteConfigRewardStickerEnabled()//
                && recommendationStay.provideRewardSticker == true)
            {
                AnalyticsManager.getInstance(CollectionStayActivity.this).recordEvent(AnalyticsManager.Category.REWARD//
                    , AnalyticsManager.Action.THUMBNAIL_CLICK, Integer.toString(recommendationStay.index), null);
            }

            if (recommendationStay.discountRate > 0)
            {
                AnalyticsManager.getInstance(CollectionStayActivity.this).recordEvent(AnalyticsManager.Category.PRODUCT_LIST//
                    , AnalyticsManager.Action.DISCOUNT_STAY, Integer.toString(recommendationStay.index), null);
            }
        }

        @Override
        public void onPlaceLongClick(int position, View view, PlaceViewItem placeViewItem, int count)
        {
            if (placeViewItem == null || placeViewItem.mType != PlaceViewItem.TYPE_ENTRY)
            {
                return;
            }

            mCollectionBaseLayout.setBlurVisibility(CollectionStayActivity.this, true);

            RecommendationStay recommendationStay = placeViewItem.getItem();

            mWishPosition = position;

            // 기존 데이터를 백업한다.
            mViewByLongPress = view;
            mPlaceViewItemByLongPress = placeViewItem;
            mListCountByLongPress = count;

            StayBookingDay stayBookingDay = (StayBookingDay) mPlaceBookingDay;

            Intent intent = StayPreviewActivity.newInstance(CollectionStayActivity.this//
                , stayBookingDay.getCheckInDay(DailyCalendar.ISO_8601_FORMAT)//
                , stayBookingDay.getCheckOutDay(DailyCalendar.ISO_8601_FORMAT)//
                , recommendationStay.index, recommendationStay.name, recommendationStay.grade, recommendationStay.discount);

            startActivityForResult(intent, CODE_REQUEST_ACTIVITY_PREVIEW);
        }

        @Override
        public void onWishClick(int position, PlaceViewItem placeViewItem)
        {
            if (lockUiComponentAndIsLockUiComponent() == true)
            {
                return;
            }

            RecommendationStay recommendationStay = placeViewItem.getItem();

            mWishPosition = position;

            boolean currentWish = recommendationStay.myWish;

            if (DailyHotel.isLogin() == true)
            {
                onChangedWish(position, !currentWish);
            }

            startActivityForResult(WishDialogActivity.newInstance(CollectionStayActivity.this, ServiceType.HOTEL//
                , recommendationStay.index, !currentWish, AnalyticsManager.Screen.DAILYHOTEL_LIST), Constants.CODE_REQUEST_ACTIVITY_WISH_DIALOG);

            AnalyticsManager.getInstance(CollectionStayActivity.this).recordEvent(AnalyticsManager.Category.PRODUCT_LIST//
                , AnalyticsManager.Action.WISH_STAY, !currentWish ? AnalyticsManager.Label.ON.toLowerCase() : AnalyticsManager.Label.OFF.toLowerCase(), null);
        }

        @Override
        public void onRecommendationClick(Recommendation recommendation)
        {
            if (recommendation == null)
            {
                return;
            }

            startCollectionPlace(recommendation);
        }

        @Override
        public void finish()
        {
            CollectionStayActivity.this.onBackPressed();
        }
    };
}