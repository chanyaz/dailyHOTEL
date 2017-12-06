package com.daily.dailyhotel.screen.booking.detail.stay;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.SharedElementCallback;
import android.view.View;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.daily.base.BaseAnalyticsInterface;
import com.daily.base.exception.DuplicateRunException;
import com.daily.base.exception.PermissionException;
import com.daily.base.exception.ProviderException;
import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ExLog;
import com.daily.base.widget.DailyToast;
import com.daily.dailyhotel.base.BaseExceptionPresenter;
import com.daily.dailyhotel.entity.CarouselListItem;
import com.daily.dailyhotel.entity.CommonDateTime;
import com.daily.dailyhotel.entity.RefundPolicy;
import com.daily.dailyhotel.entity.Review;
import com.daily.dailyhotel.entity.ReviewAnswerValue;
import com.daily.dailyhotel.entity.ReviewItem;
import com.daily.dailyhotel.entity.ReviewQuestionItem;
import com.daily.dailyhotel.entity.StayBookingDetail;
import com.daily.dailyhotel.parcel.analytics.GourmetDetailAnalyticsParam;
import com.daily.dailyhotel.parcel.analytics.NavigatorAnalyticsParam;
import com.daily.dailyhotel.parcel.analytics.StayDetailAnalyticsParam;
import com.daily.dailyhotel.repository.remote.BookingRemoteImpl;
import com.daily.dailyhotel.repository.remote.CommonRemoteImpl;
import com.daily.dailyhotel.repository.remote.GourmetRemoteImpl;
import com.daily.dailyhotel.repository.remote.RefundRemoteImpl;
import com.daily.dailyhotel.screen.booking.detail.map.GourmetBookingDetailMapActivity;
import com.daily.dailyhotel.screen.common.dialog.call.CallDialogActivity;
import com.daily.dailyhotel.screen.common.dialog.call.front.FrontCallDialogActivity;
import com.daily.dailyhotel.screen.common.dialog.navigator.NavigatorDialogActivity;
import com.daily.dailyhotel.screen.home.gourmet.detail.GourmetDetailActivity;
import com.daily.dailyhotel.screen.home.stay.inbound.detail.StayDetailActivity;
import com.daily.dailyhotel.storage.preference.DailyRemoteConfigPreference;
import com.daily.dailyhotel.storage.preference.DailyUserPreference;
import com.daily.dailyhotel.util.DailyLocationExFactory;
import com.facebook.drawee.view.SimpleDraweeView;
import com.twoheart.dailyhotel.DailyHotel;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Gourmet;
import com.twoheart.dailyhotel.model.GourmetCurationOption;
import com.twoheart.dailyhotel.model.GourmetSearchCuration;
import com.twoheart.dailyhotel.model.GourmetSearchParams;
import com.twoheart.dailyhotel.model.PlaceBookingDetail;
import com.twoheart.dailyhotel.model.ReviewPickQuestion;
import com.twoheart.dailyhotel.model.ReviewScoreQuestion;
import com.twoheart.dailyhotel.model.time.GourmetBookingDay;
import com.twoheart.dailyhotel.model.time.StayBookingDay;
import com.twoheart.dailyhotel.screen.booking.detail.hotel.IssuingReceiptActivity;
import com.twoheart.dailyhotel.screen.booking.detail.hotel.StayAutoRefundActivity;
import com.twoheart.dailyhotel.screen.common.HappyTalkCategoryDialog;
import com.twoheart.dailyhotel.screen.common.PermissionManagerActivity;
import com.twoheart.dailyhotel.screen.common.ZoomMapActivity;
import com.twoheart.dailyhotel.screen.gourmet.preview.GourmetPreviewActivity;
import com.twoheart.dailyhotel.screen.information.FAQActivity;
import com.twoheart.dailyhotel.screen.mydaily.member.LoginActivity;
import com.twoheart.dailyhotel.screen.review.ReviewActivity;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.DailyCalendar;
import com.twoheart.dailyhotel.util.KakaoLinkManager;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by sheldon
 * Clean Architecture
 */
public class StayBookingDetailPresenter extends BaseExceptionPresenter<StayBookingDetailActivity, StayBookingDetailInterface> implements StayBookingDetailView.OnEventListener
{
    private StayBookingDetailAnalyticsInterface mAnalytics;

    private CommonRemoteImpl mCommonRemoteImpl;
    private BookingRemoteImpl mBookingRemoteImpl;
    private GourmetRemoteImpl mGourmetRemoteImpl;
    private RefundRemoteImpl mRefundRemoteImpl;

    private int mReservationIndex;
    private String mAggregationId;
    private String mImageUrl;
    private boolean mIsDeepLink; // 딱히 쓰지는 않음
    private int mBookingState;
    private CommonDateTime mCommonDateTime;
    private StayBookingDetail mStayBookingDetail;
    private RefundPolicy mRefundPolicy;

    private DailyLocationExFactory mDailyLocationExFactory;

    private View mViewByLongPress;
    private Gourmet mGourmetByLongPress;
    private List<Gourmet> mRecommendGourmetList;

    public interface StayBookingDetailAnalyticsInterface extends BaseAnalyticsInterface
    {
        void onScreen(Activity activity, StayBookingDetail stayBookingDetail, String refundPolicy, int bookingState);

        void onEventShareClick(Activity activity);

        void onEventShareKakaoClick(Activity activity);

        void onEventMoreShareClick(Activity activity);

        void onEventConciergeClick(Activity activity);

        void onEventConciergeFaqClick(Activity activity);

        void onEventFrontCallClick(Activity activity);

        void onEventFrontReservationCallClick(Activity activity);

        void onEventHappyTalkClick(Activity activity);

        void onEventHappyTalkClick2(Activity activity, boolean isRefund);

        void onEventConciergeCallClick(Activity activity, boolean isRefund);

        void onEventStartConciergeCall(Activity activity);

        void onEventMapClick(Activity activity);

        void onEventViewDetailClick(Activity activity);

        void onEventReviewClick(Activity activity);

        void onEventHideBookingClick(Activity activity, int stayIndex);

        void onEventHideBookingSuccess(Activity activity, int stayIndex);

        void onEventRefundClick(Activity activity, boolean isFreeRefund);

        void onEventRecommendGourmetList(Activity activity, boolean hasData);

        void onEventRecommendGourmetViewAllClick(Activity activity);

        void onEventRecommendGourmetItemClick(Activity activity, int stayIndex, double distance);
    }

    public StayBookingDetailPresenter(@NonNull StayBookingDetailActivity activity)
    {
        super(activity);
    }

    @NonNull
    @Override
    protected StayBookingDetailInterface createInstanceViewInterface()
    {
        return new StayBookingDetailView(getActivity(), this);
    }

    @Override
    public void constructorInitialize(StayBookingDetailActivity activity)
    {
        setContentView(R.layout.activity_stay_booking_detail_data);

        setAnalytics(new StayBookingDetailAnalyticsImpl());

        mCommonRemoteImpl = new CommonRemoteImpl(activity);
        mBookingRemoteImpl = new BookingRemoteImpl(activity);
        mGourmetRemoteImpl = new GourmetRemoteImpl(activity);
        mRefundRemoteImpl = new RefundRemoteImpl(activity);

        setRefresh(true);
    }

    @Override
    public void setAnalytics(BaseAnalyticsInterface analytics)
    {
        mAnalytics = (StayBookingDetailAnalyticsInterface) analytics;
    }

    @Override
    public boolean onIntent(Intent intent)
    {
        if (intent == null)
        {
            return true;
        }

        Bundle bundle = intent.getExtras();

        if (bundle != null)
        {
            mReservationIndex = bundle.getInt(StayBookingDetailActivity.NAME_INTENT_EXTRA_DATA_BOOKINGIDX);
            mAggregationId = bundle.getString(StayBookingDetailActivity.NAME_INTENT_EXTRA_DATA_AGGREGATION_ID);
            mImageUrl = bundle.getString(StayBookingDetailActivity.NAME_INTENT_EXTRA_DATA_URL);
            mIsDeepLink = bundle.getBoolean(StayBookingDetailActivity.NAME_INTENT_EXTRA_DATA_DEEPLINK, false);
            mBookingState = bundle.getInt(StayBookingDetailActivity.NAME_INTENT_EXTRA_DATA_BOOKING_STATE);
        }

        if (mReservationIndex <= 0)
        {
            Util.restartApp(getActivity());
            return true;
        }

        return true;
    }

    @Override
    public void onPostCreate()
    {
    }

    @Override
    public void onStart()
    {
        super.onStart();

        if (mReservationIndex <= 0)
        {
            Util.restartApp(getActivity());
            return;
        }

        if (DailyHotel.isLogin() == false)
        {
            startLogin();
            return;
        }

        if (isRefresh() == true)
        {
            onRefresh(true);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (isRefresh() == true)
        {
            onRefresh(true);
        }

        if (Util.supportPreview(getActivity()) == true)
        {
            if (getViewInterface().isBlurVisible() == true)
            {
                getViewInterface().setBlurVisible(getActivity(), false);
            }
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onDestroy()
    {
        // 꼭 호출해 주세요.
        super.onDestroy();

        if (mDailyLocationExFactory != null)
        {
            mDailyLocationExFactory.stopLocationMeasure();
        }
    }

    @Override
    public boolean onBackPressed()
    {
        if (getViewInterface() != null)
        {
            boolean isShow = mRecommendGourmetList != null && mRecommendGourmetList.size() > 0;
            getViewInterface().setRecommendGourmetButtonAnimation(isShow);
        }

        if (mStayBookingDetail != null && getViewInterface() != null && getViewInterface().isExpandedMap() == true)
        {
            onCollapseMapClick();
            return true;
        }

        return super.onBackPressed();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        Util.restartApp(getActivity());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        unLockAll();

        switch (requestCode)
        {
            case StayBookingDetailActivity.REQUEST_CODE_DETAIL:
            {
                setResult(resultCode);

                if (resultCode == Activity.RESULT_OK //
                    || resultCode == Constants.CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_READY //
                    || resultCode == Constants.CODE_RESULT_ACTIVITY_PAYMENT_TIMEOVER)
                {
                    finish();
                }
                break;
            }

            case StayBookingDetailActivity.REQUEST_CODE_LOGIN:
            {
                if (resultCode == Activity.RESULT_OK)
                {
                    setRefresh(true);
                } else
                {
                    finish();
                }
                break;
            }

            case StayBookingDetailActivity.REQUEST_CODE_SETTING_LOCATION:
            {
                onMyLocationClick();
                break;
            }

            case StayBookingDetailActivity.REQUEST_CODE_PERMISSION_MANAGER:
            {
                if (resultCode == Activity.RESULT_OK)
                {
                    onMyLocationClick();
                } else if (resultCode == Constants.CODE_RESULT_ACTIVITY_GO_HOME)
                {
                    setResult(resultCode);
                    finish();
                }
                break;
            }

            case StayBookingDetailActivity.REQUEST_CODE_ZOOMMAP:
                if (resultCode == Constants.CODE_RESULT_ACTIVITY_GO_HOME)
                {
                    setResult(resultCode);
                    finish();
                }
                break;

            case StayBookingDetailActivity.REQUEST_CODE_REVIEW:
            {
                if (resultCode == Activity.RESULT_OK)
                {
                    if (mStayBookingDetail == null)
                    {
                        return;
                    }

                    mStayBookingDetail.reviewStatusType = PlaceBookingDetail.ReviewStatusType.COMPLETE;
                    getViewInterface().setReviewButtonLayout(mStayBookingDetail.reviewStatusType);
                }
                break;
            }

            case StayBookingDetailActivity.REQUEST_CODE_FAQ:
                if (resultCode == Constants.CODE_RESULT_ACTIVITY_GO_HOME)
                {
                    setResult(Constants.CODE_RESULT_ACTIVITY_GO_HOME);
                    finish();
                }
                break;

            case StayBookingDetailActivity.REQUEST_CODE_REFUND:
            {
                switch (resultCode)
                {
                    case Constants.CODE_RESULT_ACTIVITY_REFRESH:
                    {
                        setRefresh(true);

                        setResult(resultCode);
                        break;
                    }

                    case Activity.RESULT_OK:
                    {
                        setResult(Constants.CODE_RESULT_ACTIVITY_REFRESH);
                        finish();
                        break;
                    }
                }
                break;
            }

            case StayBookingDetailActivity.REQUEST_CODE_FRONT_CALL:
                if (resultCode == Activity.RESULT_OK)
                {
                    mAnalytics.onEventFrontCallClick(getActivity());
                }
                break;

            case StayBookingDetailActivity.REQUEST_CODE_FRONT_RESERVATION_CALL:
                if (resultCode == Activity.RESULT_OK)
                {
                    mAnalytics.onEventFrontReservationCallClick(getActivity());
                }
                break;

            case StayBookingDetailActivity.REQUEST_CODE_CALL:
                if (resultCode == Activity.RESULT_OK)
                {
                    mAnalytics.onEventStartConciergeCall(getActivity());
                }
                break;

            case StayBookingDetailActivity.REQUEST_CODE_PREVIEW:
            {
                if (resultCode == Activity.RESULT_OK)
                {
                    Observable.create(new ObservableOnSubscribe<Object>()
                    {
                        @Override
                        public void subscribe(ObservableEmitter<Object> e) throws Exception
                        {
                            startGourmetDetail(mViewByLongPress, mGourmetByLongPress, mCommonDateTime, mStayBookingDetail);
                        }
                    }).subscribeOn(AndroidSchedulers.mainThread()).subscribe();
                }
                break;
            }
        }
    }

    @Override
    protected synchronized void onRefresh(boolean showProgress)
    {
        if (getActivity().isFinishing() == true || isRefresh() == false)
        {
            return;
        }

        setRefresh(false);
        screenLock(showProgress);

        Observable<StayBookingDetail> bookingDetailObservable = Observable.defer(new Callable<ObservableSource<? extends StayBookingDetail>>()
        {
            @Override
            public ObservableSource<? extends StayBookingDetail> call() throws Exception
            {
                if (DailyTextUtils.isTextEmpty(mAggregationId) == true)
                {
                    return mBookingRemoteImpl.getStayBookingDetail(mReservationIndex);
                }

                return mBookingRemoteImpl.getStayBookingDetail(mAggregationId);
            }
        });

        addCompositeDisposable(Observable.zip(mCommonRemoteImpl.getCommonDateTime(), bookingDetailObservable, new BiFunction<CommonDateTime, StayBookingDetail, StayBookingDetail>()
        {
            @Override
            public StayBookingDetail apply(CommonDateTime commonDateTime, StayBookingDetail stayBookingDetail) throws Exception
            {
                setCommonDateTime(commonDateTime);
                setStayBookingDetail(stayBookingDetail);
                return stayBookingDetail;
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<StayBookingDetail>()
        {
            @Override
            public void accept(StayBookingDetail stayBookingDetail) throws Exception
            {
                notifyStayBookingDetail();
                unLockAll();
            }
        }, new Consumer<Throwable>()
        {
            @Override
            public void accept(Throwable throwable) throws Exception
            {
                onHandleErrorAndFinish(throwable);
            }
        }));
    }

    private void setCommonDateTime(CommonDateTime commonDateTime)
    {
        if (commonDateTime == null)
        {
            return;
        }

        mCommonDateTime = commonDateTime;
    }

    private void setStayBookingDetail(StayBookingDetail stayBookingDetail)
    {
        mStayBookingDetail = stayBookingDetail;
    }

    private void notifyStayBookingDetail()
    {
        if (mStayBookingDetail == null || mCommonDateTime == null)
        {
            //            finish(); // ????  이거 할 필요가????
            return;
        }

        try
        {
            getViewInterface().setBookingDetail(mStayBookingDetail);
            getViewInterface().setRemindDate(mCommonDateTime.currentDateTime, mStayBookingDetail);

            // Reward
            getViewInterface().setDepositStickerCardVisible(mStayBookingDetail.activeReward);

            if (mStayBookingDetail.activeReward == true)
            {
                getViewInterface().setDepositStickerCard(DailyRemoteConfigPreference.getInstance(getActivity()).getKeyRemoteConfigRewardStickerCardTitleMessage(), mStayBookingDetail.rewardStickerCount);
            }

            if (mStayBookingDetail.readyForRefund == true)
            {
                // 환불 대기 인 상태에서는 문구가 고정이다.
                getViewInterface().setRefundPolicyInformation(true, mStayBookingDetail.readyForRefund, null);

                mAnalytics.onScreen(getActivity(), mStayBookingDetail, null, mBookingState);
            } else
            {
                long checkOutDateTime = DailyCalendar.convertStringToDate(mStayBookingDetail.checkOutDateTime).getTime();
                long currentDateTime = DailyCalendar.convertStringToDate(mCommonDateTime.currentDateTime).getTime();

                if (currentDateTime < checkOutDateTime)
                {
                    addCompositeDisposable(mRefundRemoteImpl.getStayRefundPolicy(mStayBookingDetail.reservationIndex, mStayBookingDetail.transactionType) //
                        .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<RefundPolicy>()
                        {
                            @Override
                            public void accept(RefundPolicy refundPolicy) throws Exception
                            {
                                onRefundPolicy(refundPolicy);
                                mAnalytics.onScreen(getActivity(), mStayBookingDetail, refundPolicy.refundPolicy, mBookingState);
                                unLockAll();
                            }
                        }, new Consumer<Throwable>()
                        {
                            @Override
                            public void accept(Throwable throwable) throws Exception
                            {
                                onRefundPolicy(null);
                                mAnalytics.onScreen(getActivity(), mStayBookingDetail, null, mBookingState);
                                unLockAll();
                            }
                        }));

                } else
                {
                    getViewInterface().setRefundPolicyInformation(false, mStayBookingDetail.readyForRefund, null);

                    mAnalytics.onScreen(getActivity(), mStayBookingDetail, null, mBookingState);
                }
            }

            getViewInterface().setHiddenBookingVisible(mBookingState);

            long currentDateTime = DailyCalendar.convertStringToDate(mCommonDateTime.currentDateTime).getTime();
            long checkInDateTime = DailyCalendar.convertStringToDate(mStayBookingDetail.checkInDateTime).getTime();

            if (currentDateTime > checkInDateTime || (mStayBookingDetail.latitude == 0.0d && mStayBookingDetail.longitude == 0.0d))
            {
                // 고메 추천 Hidden - 현재 시간이 체크인 시간보다 큰 경우
                getViewInterface().setRecommendGourmetLayoutVisible(false);
                unLockAll();
            } else
            {
                // 고메 추천 Show
                GourmetBookingDay gourmetBookingDay = new GourmetBookingDay();

                Date checkInDate = DailyCalendar.convertStringToDate(mStayBookingDetail.checkInDateTime);
                gourmetBookingDay.setVisitDay(DailyCalendar.format(checkInDate, DailyCalendar.ISO_8601_FORMAT));

                Location location = new Location((String) null);
                location.setLatitude(mStayBookingDetail.latitude);
                location.setLongitude(mStayBookingDetail.longitude);

                GourmetSearchCuration gourmetCuration = new GourmetSearchCuration();
                GourmetCurationOption gourmetCurationOption = (GourmetCurationOption) gourmetCuration.getCurationOption();
                gourmetCurationOption.setSortType(Constants.SortType.DISTANCE);

                gourmetCuration.setGourmetBookingDay(gourmetBookingDay);
                gourmetCuration.setLocation(location);
                gourmetCuration.setCurationOption(gourmetCurationOption);
                gourmetCuration.setRadius(10d);

                GourmetSearchParams gourmetParams = (GourmetSearchParams) gourmetCuration.toPlaceParams(1, 10, true);

                addCompositeDisposable(mGourmetRemoteImpl.getList(gourmetParams) //
                    .observeOn(Schedulers.io()).map(new Function<List<Gourmet>, ArrayList<CarouselListItem>>()
                    {
                        @Override
                        public ArrayList<CarouselListItem> apply(@io.reactivex.annotations.NonNull List<Gourmet> gourmets) throws Exception
                        {
                            //                            mRecommendGourmetList = gourmets;
                            return convertCarouselListItemList(gourmets);
                        }
                    }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<ArrayList<CarouselListItem>>()
                    {
                        @Override
                        public void accept(@io.reactivex.annotations.NonNull ArrayList<CarouselListItem> carouselListItemList) throws Exception
                        {
                            unLockAll();

                            getViewInterface().setRecommendGourmetData(carouselListItemList);

                            boolean hasData = !(carouselListItemList == null || carouselListItemList.size() == 0);

                            mAnalytics.onEventRecommendGourmetList(getActivity(), hasData);
                        }
                    }, new Consumer<Throwable>()
                    {
                        @Override
                        public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception
                        {
                            unLockAll();

                            getViewInterface().setRecommendGourmetData(null);

                            mAnalytics.onEventRecommendGourmetList(getActivity(), false);
                        }
                    }));
            }

        } catch (Exception e)
        {
            Crashlytics.logException(e);
            finish();
            return;
        }
    }

    private void onRefundPolicy(RefundPolicy refundPolicy)
    {
        if (mStayBookingDetail == null || mCommonDateTime == null)
        {
            return;
        }

        mRefundPolicy = refundPolicy;

        boolean isVisibleRefundPolicy = true;

        if (refundPolicy != null)
        {
            ExLog.d("test, comment : " + refundPolicy.comment + " , message : " + refundPolicy.message + " , refundPolicy : " + refundPolicy.refundPolicy + " , refundManual : " + refundPolicy.refundManual);

            // 환불 킬스위치 ON
            if (refundPolicy.refundManual == true)
            {
                if (RefundPolicy.STATUS_NRD.equalsIgnoreCase(refundPolicy.refundPolicy) == false)
                {
                    refundPolicy.refundPolicy = RefundPolicy.STATUS_SURCHARGE_REFUND;
                    refundPolicy.comment = refundPolicy.message;

                    ExLog.d("test, refundPolicy change from message to comment");
                }
            } else
            {
                if (RefundPolicy.STATUS_NONE.equalsIgnoreCase(refundPolicy.refundPolicy) == true)
                {
                    isVisibleRefundPolicy = false;
                }
            }
        } else
        {
            ExLog.d("test, refundPolicy is null");

            isVisibleRefundPolicy = false;
        }

        getViewInterface().setRefundPolicyInformation(isVisibleRefundPolicy, mStayBookingDetail.readyForRefund, refundPolicy);
    }

    @Override
    public void onBackClick()
    {
        getActivity().onBackPressed();
    }

    private void startLogin()
    {
        getViewInterface().showSimpleDialog(null, getString(R.string.message_booking_detail_do_login) //
            , getString(R.string.dialog_btn_text_yes), getString(R.string.dialog_btn_text_no), new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = LoginActivity.newInstance(getActivity());
                    startActivityForResult(intent, StayBookingDetailActivity.REQUEST_CODE_LOGIN);
                }
            }, new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    finish();
                }
            });
    }

    private String getRefundPolicyStatus(boolean readyForRefund, RefundPolicy refundPolicy)
    {
        // 환불 대기 상태
        if (readyForRefund == true)
        {
            return RefundPolicy.STATUS_WAIT_REFUND;
        } else
        {
            if (refundPolicy != null && DailyTextUtils.isTextEmpty(refundPolicy.refundPolicy) == false)
            {
                return refundPolicy.refundPolicy;
            } else
            {
                return RefundPolicy.STATUS_SURCHARGE_REFUND;
            }
        }
    }

    private ArrayList<CarouselListItem> convertCarouselListItemList(List<Gourmet> list)
    {
        ArrayList<Gourmet> gourmetList = new ArrayList<>();
        ArrayList<CarouselListItem> carouselListItemList = new ArrayList<>();

        if (list == null || list.size() == 0)
        {
            mRecommendGourmetList = gourmetList;
            return carouselListItemList;
        }

        for (Gourmet gourmet : list)
        {
            try
            {
                if (gourmet.isSoldOut == true)
                {
                    // sold out 업장 제외하기로 함
                    // ExLog.d(gourmet.name + " , " + gourmet.isSoldOut + " : " + gourmet.availableTicketNumbers);
                    continue;
                }

                gourmetList.add(gourmet);

                CarouselListItem item = new CarouselListItem(CarouselListItem.TYPE_GOURMET, gourmet);
                carouselListItemList.add(item);
            } catch (Exception e)
            {
                if (gourmet != null)
                {
                    ExLog.w(gourmet.index + " | " + gourmet.name + " :: " + e.getMessage());
                }
            }
        }

        mRecommendGourmetList = gourmetList;

        return carouselListItemList;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    void startGourmetDetail(View view, Gourmet gourmet, CommonDateTime commonDateTime, StayBookingDetail stayBookingDetail)
    {
        if (view == null || gourmet == null || commonDateTime == null || stayBookingDetail == null)
        {
            return;
        }

        try
        {
            long currentDateTime = DailyCalendar.convertStringToDate(commonDateTime.currentDateTime).getTime();
            long checkInDateTime = DailyCalendar.convertStringToDate(stayBookingDetail.checkInDateTime).getTime();

            String visitDateTime = stayBookingDetail.checkInDateTime;
            if (currentDateTime > checkInDateTime)
            {
                visitDateTime = commonDateTime.dailyDateTime;
            }

            // --> 추후에 정리되면 메소드로 수정
            GourmetDetailAnalyticsParam analyticsParam = new GourmetDetailAnalyticsParam();
            analyticsParam.price = gourmet.price;
            analyticsParam.discountPrice = gourmet.discountPrice;
            analyticsParam.setShowOriginalPriceYn(analyticsParam.price, analyticsParam.discountPrice);
            analyticsParam.setProvince(null);
            analyticsParam.entryPosition = gourmet.entryPosition;
            analyticsParam.totalListCount = -1;
            analyticsParam.isDailyChoice = gourmet.isDailyChoice;
            analyticsParam.setAddressAreaName(gourmet.addressSummary);

            // <-- 추후에 정리되면 메소드로 수정

            if (Util.isUsedMultiTransition() == true)
            {
                getActivity().setExitSharedElementCallback(new SharedElementCallback()
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

                //                Intent intent = GourmetDetailActivity.newInstance(StayReservationDetailActivity.this //
                //                    , gourmetBookingDay, gourmet.index, gourmet.name //
                //                    , gourmet.imageUrl, gourmet.category, gourmet.isSoldOut, analyticsParam, true //
                //                    , PlaceDetailLayout.TRANS_GRADIENT_BOTTOM_TYPE_NONE);


                Intent intent = GourmetDetailActivity.newInstance(getActivity() //
                    , gourmet.index, gourmet.name, gourmet.imageUrl, gourmet.discountPrice//
                    , visitDateTime, gourmet.category, gourmet.isSoldOut, false, false, true//
                    , GourmetDetailActivity.TRANS_GRADIENT_BOTTOM_TYPE_NONE//
                    , analyticsParam);

                if (intent == null)
                {
                    Util.restartApp(getActivity());
                    return;
                }

                View simpleDraweeView = view.findViewById(R.id.contentImageView);
                View gradientTopView = view.findViewById(R.id.gradientTopView);
                View gradientBottomView = view.findViewById(R.id.gradientBottomView);

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity()//
                    , android.support.v4.util.Pair.create(simpleDraweeView, getString(R.string.transition_place_image)) //
                    , android.support.v4.util.Pair.create(gradientTopView, getString(R.string.transition_gradient_top_view)) //
                    , android.support.v4.util.Pair.create(gradientBottomView, getString(R.string.transition_gradient_bottom_view)));

                startActivityForResult(intent, StayBookingDetailActivity.REQUEST_CODE_DETAIL, options.toBundle());
            } else
            {
                //                Intent intent = GourmetDetailActivity.newInstance(getActivity() //
                //                    , gourmetBookingDay, gourmet.index, gourmet.name //
                //                    , gourmet.imageUrl, gourmet.category, gourmet.isSoldOut, analyticsParam, false //
                //                    , PlaceDetailLayout.TRANS_GRADIENT_BOTTOM_TYPE_NONE);

                Intent intent = GourmetDetailActivity.newInstance(getActivity() //
                    , gourmet.index, gourmet.name, gourmet.imageUrl, gourmet.discountPrice//
                    , visitDateTime, gourmet.category, gourmet.isSoldOut, false, false, false//
                    , GourmetDetailActivity.TRANS_GRADIENT_BOTTOM_TYPE_NONE//
                    , analyticsParam);

                if (intent == null)
                {
                    Util.restartApp(getActivity());
                    return;
                }

                startActivityForResult(intent, StayBookingDetailActivity.REQUEST_CODE_DETAIL);

                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
            }
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    private Observable<Location> searchMyLocation(Observable locationAnimationObservable)
    {
        if (mDailyLocationExFactory == null)
        {
            mDailyLocationExFactory = new DailyLocationExFactory(getActivity());
        }

        if (mDailyLocationExFactory.measuringLocation() == true)
        {
            return null;
        }

        Disposable locationAnimationDisposable;

        if (locationAnimationObservable != null)
        {
            locationAnimationDisposable = locationAnimationObservable.subscribe();
        } else
        {
            locationAnimationDisposable = null;
        }

        return new Observable<Location>()
        {
            @Override
            protected void subscribeActual(Observer<? super Location> observer)
            {
                mDailyLocationExFactory.checkLocationMeasure(new DailyLocationExFactory.OnCheckLocationListener()
                {
                    @Override
                    public void onRequirePermission()
                    {
                        if (locationAnimationDisposable != null)
                        {
                            locationAnimationDisposable.dispose();
                        }

                        observer.onError(new PermissionException());
                    }

                    @Override
                    public void onFailed()
                    {
                        if (locationAnimationDisposable != null)
                        {
                            locationAnimationDisposable.dispose();
                        }

                        observer.onError(new Exception());
                    }

                    @Override
                    public void onProviderEnabled()
                    {
                        mDailyLocationExFactory.startLocationMeasure(new DailyLocationExFactory.OnLocationListener()
                        {
                            @Override
                            public void onFailed()
                            {
                                if (locationAnimationDisposable != null)
                                {
                                    locationAnimationDisposable.dispose();
                                }

                                observer.onError(new Exception());
                            }

                            @Override
                            public void onAlreadyRun()
                            {
                                if (locationAnimationDisposable != null)
                                {
                                    locationAnimationDisposable.dispose();
                                }

                                observer.onError(new DuplicateRunException());
                            }

                            @Override
                            public void onLocationChanged(Location location)
                            {
                                if (locationAnimationDisposable != null)
                                {
                                    locationAnimationDisposable.dispose();
                                }

                                unLockAll();

                                mDailyLocationExFactory.stopLocationMeasure();

                                if (location == null)
                                {
                                    observer.onError(new NullPointerException());
                                } else
                                {
                                    observer.onNext(location);
                                    observer.onComplete();
                                }
                            }
                        });
                    }

                    @Override
                    public void onProviderDisabled()
                    {
                        if (locationAnimationDisposable != null)
                        {
                            locationAnimationDisposable.dispose();
                        }

                        observer.onError(new ProviderException());
                    }
                });
            }
        }.doOnComplete(() ->
        {
            if (locationAnimationDisposable != null)
            {
                locationAnimationDisposable.dispose();
            }
        }).doOnDispose(() ->
        {
            if (locationAnimationDisposable != null)
            {
                locationAnimationDisposable.dispose();
            }
        }).doOnError(throwable ->
        {
            unLockAll();

            if (throwable instanceof PermissionException)
            {
                Intent intent = PermissionManagerActivity.newInstance(getActivity(), PermissionManagerActivity.PermissionType.ACCESS_FINE_LOCATION);
                startActivityForResult(intent, StayBookingDetailActivity.REQUEST_CODE_PERMISSION_MANAGER);
            } else if (throwable instanceof ProviderException)
            {
                // 현재 GPS 설정이 꺼져있습니다 설정에서 바꾸어 주세요.
                View.OnClickListener positiveListener = new View.OnClickListener()//
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, StayBookingDetailActivity.REQUEST_CODE_SETTING_LOCATION);
                    }
                };

                View.OnClickListener negativeListener = new View.OnClickListener()//
                {
                    @Override
                    public void onClick(View v)
                    {
                        DailyToast.showToast(getActivity(), R.string.message_failed_mylocation, DailyToast.LENGTH_SHORT);
                    }
                };

                DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                        DailyToast.showToast(getActivity(), R.string.message_failed_mylocation, DailyToast.LENGTH_SHORT);
                    }
                };

                getViewInterface().showSimpleDialog(//
                    getString(R.string.dialog_title_used_gps), getString(R.string.dialog_msg_used_gps), //
                    getString(R.string.dialog_btn_text_dosetting), //
                    getString(R.string.dialog_btn_text_cancel), //
                    positiveListener, negativeListener, cancelListener, null, true);
            } else if (throwable instanceof DuplicateRunException)
            {

            } else
            {
                DailyToast.showToast(getActivity(), R.string.message_failed_mylocation, DailyToast.LENGTH_SHORT);
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onIssuingReceiptClick()
    {
        if (getActivity() == null || lock() == true)
        {
            return;
        }

        startActivityForResult( //
            IssuingReceiptActivity.newInstance(getActivity(), mReservationIndex, mBookingState) //
            , StayBookingDetailActivity.REQUEST_CODE_ISSUING_RECEIPT);
    }

    @Override
    public void onShareClick()
    {
        if (lock() == true)
        {
            return;
        }

        getViewInterface().showShareDialog(new DialogInterface.OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialog)
            {
                unLockAll();
            }
        });

        mAnalytics.onEventShareClick(getActivity());
    }

    @Override
    public void onMapLoading()
    {
        if (getActivity() == null)
        {
            return;
        }

        DailyToast.showToast(getActivity(), R.string.message_loading_map, Toast.LENGTH_SHORT);
    }

    @Override
    public void onMapClick()
    {
        if (getActivity() == null || mStayBookingDetail == null || lock() == true)
        {
            return;
        }

        Intent intent = ZoomMapActivity.newInstance(getActivity()//
            , ZoomMapActivity.SourceType.HOTEL_BOOKING, mStayBookingDetail.stayName, mStayBookingDetail.stayAddress//
            , mStayBookingDetail.latitude, mStayBookingDetail.longitude, mStayBookingDetail.overseas);

        startActivityForResult(intent, StayBookingDetailActivity.REQUEST_CODE_ZOOMMAP);

        mAnalytics.onEventMapClick(getActivity());
    }

    @Override
    public void onExpandMapClick()
    {
        if (getActivity() == null || mStayBookingDetail == null || lock() == true)
        {
            return;
        }

        getViewInterface().setBookingDetailMapToolbar();

        addCompositeDisposable(getViewInterface().expandMap(mStayBookingDetail.latitude, mStayBookingDetail.longitude)//
            .subscribeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
            {
                @Override
                public void accept(@io.reactivex.annotations.NonNull Boolean aBoolean) throws Exception
                {
                    getViewInterface().setRecommendGourmetButtonAnimation(false);
                    unLockAll();
                }
            }, new Consumer<Throwable>()
            {
                @Override
                public void accept(Throwable throwable) throws Exception
                {
                    unLockAll();
                }
            }));

        mAnalytics.onEventMapClick(getActivity());
    }

    @Override
    public void onCollapseMapClick()
    {
        if (lock() == true)
        {
            return;
        }

        clearCompositeDisposable();

        getViewInterface().setBookingDetailToolbar();

        addCompositeDisposable(getViewInterface().collapseMap()//
            .subscribeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
            {
                @Override
                public void accept(@io.reactivex.annotations.NonNull Boolean aBoolean) throws Exception
                {
                    unLockAll();
                }
            }));
    }

    @Override
    public void onViewDetailClick()
    {
        if (getActivity() == null || mStayBookingDetail == null || mCommonDateTime == null || lock() == true)
        {
            return;
        }

        try
        {
            StayBookingDay stayBookingDay = new StayBookingDay();
            stayBookingDay.setCheckInDay(mCommonDateTime.dailyDateTime);
            stayBookingDay.setCheckOutDay(mCommonDateTime.dailyDateTime, 1);

            //                Intent intent = StayDetailActivity.newInstance(StayReservationDetailActivity.this, stayBookingDay//
            //                    , mPlaceBookingDetail.isOverseas, mPlaceBookingDetail.placeIndex, 0, false, false, false);

            Intent intent = StayDetailActivity.newInstance(getActivity() //
                , mStayBookingDetail.stayIndex, mStayBookingDetail.stayName, null, StayDetailActivity.NONE_PRICE//
                , stayBookingDay.getCheckInDay(DailyCalendar.ISO_8601_FORMAT)//
                , stayBookingDay.getCheckOutDay(DailyCalendar.ISO_8601_FORMAT)//
                , false, StayDetailActivity.TRANS_GRADIENT_BOTTOM_TYPE_NONE, new StayDetailAnalyticsParam());

            startActivityForResult(intent, StayBookingDetailActivity.REQUEST_CODE_DETAIL);

            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.hold);

            mAnalytics.onEventViewDetailClick(getActivity());
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    @Override
    public void onNavigatorClick()
    {
        if (getActivity() == null || mStayBookingDetail == null || lock() == true)
        {
            return;
        }

        NavigatorAnalyticsParam analyticsParam = new NavigatorAnalyticsParam();
        analyticsParam.category = AnalyticsManager.Category.HOTEL_BOOKINGS;
        analyticsParam.action = AnalyticsManager.Action.HOTEL_DETAIL_NAVIGATION_APP_CLICKED;

        startActivityForResult(NavigatorDialogActivity.newInstance(getActivity(), mStayBookingDetail.stayName//
            , mStayBookingDetail.latitude, mStayBookingDetail.longitude, mStayBookingDetail.overseas, analyticsParam) //
            , StayBookingDetailActivity.REQUEST_CODE_NAVIGATOR);
    }

    @Override
    public void onClipAddressClick()
    {
        if (getActivity() == null || mStayBookingDetail == null)
        {
            return;
        }

        DailyTextUtils.clipText(getActivity(), mStayBookingDetail.stayAddress);

        DailyToast.showToast(getActivity(), R.string.message_detail_copy_address, Toast.LENGTH_SHORT);
    }

    @Override
    public void onMyLocationClick()
    {
        if (lock() == true)
        {
            return;
        }

        Observable<Long> locationAnimationObservable = getViewInterface().getLocationAnimation();
        Observable observable = searchMyLocation(locationAnimationObservable);

        if (observable != null)
        {
            addCompositeDisposable(observable.subscribe(new Consumer<Location>()
            {
                @Override
                public void accept(@io.reactivex.annotations.NonNull Location location) throws Exception
                {
                    unLockAll();
                    getViewInterface().setMyLocation(location);
                }
            }, new Consumer<Throwable>()
            {
                @Override
                public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception
                {
                    unLockAll();
                }
            }));
        } else
        {
            unLockAll();

        }
    }

    @Override
    public void onConciergeClick()
    {
        if (getActivity() == null || mStayBookingDetail == null || mCommonDateTime == null || lock() == true)
        {
            return;
        }

        String frontPhone;
        String frontReservationPhone;
        if (mStayBookingDetail == null)
        {
            frontPhone = null;
            frontReservationPhone = null;
        } else
        {
            frontPhone = mStayBookingDetail.phone1;
            frontReservationPhone = mStayBookingDetail.phone2;
        }

        getViewInterface().showConciergeDialog(frontPhone, frontReservationPhone, mCommonDateTime.currentDateTime, new DialogInterface.OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialogInterface)
            {
                unLockAll();
                ;
            }
        });

        mAnalytics.onEventConciergeClick(getActivity());
    }

    @Override
    public void onConciergeFaqClick()
    {
        startActivityForResult(FAQActivity.newInstance(getActivity()), StayBookingDetailActivity.REQUEST_CODE_FAQ);

        mAnalytics.onEventConciergeFaqClick(getActivity());
    }

    @Override
    public void onFrontCallClick(String frontPhone)
    {
        if (DailyTextUtils.isTextEmpty(frontPhone) == true)
        {
            return;
        }

        startActivityForResult(FrontCallDialogActivity.newInstance( //
            getActivity(), frontPhone, getString(R.string.dialog_msg_front_call_stay)) //
            , StayBookingDetailActivity.REQUEST_CODE_FRONT_CALL);
    }

    @Override
    public void onFrontReservationCallClick(String frontPhone)
    {
        if (DailyTextUtils.isTextEmpty(frontPhone) == true)
        {
            return;
        }

        startActivityForResult(FrontCallDialogActivity.newInstance( //
            getActivity(), frontPhone, getString(R.string.dialog_msg_reservation_call_stay)) //
            , StayBookingDetailActivity.REQUEST_CODE_FRONT_RESERVATION_CALL);
    }

    @Override
    public void onConciergeHappyTalkClick(boolean isRefund)
    {
        if (getActivity() == null || mStayBookingDetail == null)
        {
            return;
        }

        try
        {
            // 카카오톡 패키지 설치 여부
            getActivity().getPackageManager().getPackageInfo("com.kakao.talk", PackageManager.GET_META_DATA);

            if (isRefund == true)
            {
                startActivityForResult(HappyTalkCategoryDialog.newInstance(getActivity()//
                    , HappyTalkCategoryDialog.CallScreen.SCREEN_STAY_REFUND//
                    , mStayBookingDetail.stayIndex, mReservationIndex, mStayBookingDetail.stayName), StayBookingDetailActivity.REQUEST_CODE_HAPPYTALK);
            } else
            {
                startActivityForResult(HappyTalkCategoryDialog.newInstance(getActivity()//
                    , HappyTalkCategoryDialog.CallScreen.SCREEN_STAY_BOOKING//
                    , mStayBookingDetail.stayIndex, mReservationIndex, mStayBookingDetail.stayName), StayBookingDetailActivity.REQUEST_CODE_HAPPYTALK);
            }

            mAnalytics.onEventHappyTalkClick2(getActivity(), isRefund);
        } catch (Exception e)
        {
            getViewInterface().showSimpleDialog(null, getString(R.string.dialog_msg_not_installed_kakaotalk)//
                , getString(R.string.dialog_btn_text_yes), getString(R.string.dialog_btn_text_no)//
                , new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Util.installPackage(getActivity(), "com.kakao.talk");
                    }
                }, null);
        }

        mAnalytics.onEventHappyTalkClick(getActivity());
    }

    @Override
    public void onConciergeCallClick(boolean isRefund)
    {
        startActivityForResult(CallDialogActivity.newInstance(getActivity()), isRefund == true //
            ? StayBookingDetailActivity.REQUEST_CODE_REFUND_CALL : StayBookingDetailActivity.REQUEST_CODE_CALL);

        mAnalytics.onEventConciergeCallClick(getActivity(), isRefund);
    }

    @Override
    public void onShareKakaoClick()
    {
        if (getActivity() == null || mStayBookingDetail == null)
        {
            return;
        }

        try
        {
            // 카카오톡 패키지 설치 여부
            getActivity().getPackageManager().getPackageInfo("com.kakao.talk", PackageManager.GET_META_DATA);

            String userName = DailyUserPreference.getInstance(getActivity()).getName();

            String message = getString(R.string.message_booking_stay_share_kakao, //
                userName, mStayBookingDetail.stayName, mStayBookingDetail.guestName,//
                DailyTextUtils.getPriceFormat(getActivity(), mStayBookingDetail.priceTotal, false), //
                mStayBookingDetail.roomName, DailyCalendar.convertDateFormatString(mStayBookingDetail.checkInDateTime, DailyCalendar.ISO_8601_FORMAT, "yyyy.MM.dd(EEE) HH시"),//
                DailyCalendar.convertDateFormatString(mStayBookingDetail.checkOutDateTime, DailyCalendar.ISO_8601_FORMAT, "yyyy.MM.dd(EEE) HH시"), //
                mStayBookingDetail.stayAddress);

            String[] checkInDates = mStayBookingDetail.checkInDateTime.split("T");
            String[] checkOutDates = mStayBookingDetail.checkOutDateTime.split("T");

            Date checkInDate = DailyCalendar.convertDate(checkInDates[0] + "T00:00:00+09:00", DailyCalendar.ISO_8601_FORMAT);
            Date checkOutDate = DailyCalendar.convertDate(checkOutDates[0] + "T00:00:00+09:00", DailyCalendar.ISO_8601_FORMAT);

            int nights = (int) ((DailyCalendar.clearTField(checkOutDate.getTime()) - DailyCalendar.clearTField(checkInDate.getTime())) / DailyCalendar.DAY_MILLISECOND);

            KakaoLinkManager.newInstance(getActivity()).shareBookingStay(message, mStayBookingDetail.stayIndex,//
                mImageUrl, DailyCalendar.convertDateFormatString(mStayBookingDetail.checkInDateTime, DailyCalendar.ISO_8601_FORMAT, "yyyyMMdd"), nights);

            mAnalytics.onEventShareKakaoClick(getActivity());
        } catch (Exception e)
        {
            ExLog.d(e.toString());

            getViewInterface().showSimpleDialog(null, getString(R.string.dialog_msg_not_installed_kakaotalk)//
                , getString(R.string.dialog_btn_text_yes), getString(R.string.dialog_btn_text_no)//
                , new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Util.installPackage(getActivity(), "com.kakao.talk");
                    }
                }, null);
        }
    }

    @Override
    public void onMoreShareClick()
    {
        if (getActivity() == null || mStayBookingDetail == null)
        {
            return;
        }

        try
        {
            String userName = DailyUserPreference.getInstance(getActivity()).getName();

            String[] checkInDates = mStayBookingDetail.checkInDateTime.split("T");
            String[] checkOutDates = mStayBookingDetail.checkOutDateTime.split("T");

            Date checkInDate = DailyCalendar.convertDate(checkInDates[0] + "T00:00:00+09:00", DailyCalendar.ISO_8601_FORMAT);
            Date checkOutDate = DailyCalendar.convertDate(checkOutDates[0] + "T00:00:00+09:00", DailyCalendar.ISO_8601_FORMAT);

            int nights = (int) ((DailyCalendar.clearTField(checkOutDate.getTime()) - DailyCalendar.clearTField(checkInDate.getTime())) / DailyCalendar.DAY_MILLISECOND);

            String longUrl = String.format(Locale.KOREA, "https://mobile.dailyhotel.co.kr/stay/%d?dateCheckIn=%s&stays=%d"//
                , mStayBookingDetail.stayIndex, DailyCalendar.convertDateFormatString(mStayBookingDetail.checkInDateTime, DailyCalendar.ISO_8601_FORMAT, "yyyy-MM-dd")//
                , nights);

            final String message = getString(R.string.message_booking_stay_share_sms, //
                userName, mStayBookingDetail.stayName, mStayBookingDetail.guestName,//
                DailyTextUtils.getPriceFormat(getActivity(), mStayBookingDetail.priceTotal, false), //
                mStayBookingDetail.roomName, DailyCalendar.convertDateFormatString(mStayBookingDetail.checkInDateTime, DailyCalendar.ISO_8601_FORMAT, "yyyy.MM.dd(EEE) HH시"),//
                DailyCalendar.convertDateFormatString(mStayBookingDetail.checkOutDateTime, DailyCalendar.ISO_8601_FORMAT, "yyyy.MM.dd(EEE) HH시"), //
                mStayBookingDetail.stayAddress);

            CommonRemoteImpl commonRemote = new CommonRemoteImpl(getActivity());

            addCompositeDisposable(commonRemote.getShortUrl(longUrl).subscribe(new Consumer<String>()
            {
                @Override
                public void accept(@io.reactivex.annotations.NonNull String shortUrl) throws Exception
                {
                    unLockAll();

                    Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                    intent.setType("text/plain");

                    intent.putExtra(Intent.EXTRA_SUBJECT, "");
                    intent.putExtra(Intent.EXTRA_TEXT, message + shortUrl);
                    Intent chooser = Intent.createChooser(intent, getString(R.string.label_doshare));
                    startActivity(chooser);
                }
            }, new Consumer<Throwable>()
            {
                @Override
                public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception
                {
                    unLockAll();

                    Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "");
                    intent.putExtra(Intent.EXTRA_TEXT, message + "https://mobile.dailyhotel.co.kr/stay/" + mStayBookingDetail.stayIndex);
                    Intent chooser = Intent.createChooser(intent, getString(R.string.label_doshare));
                    startActivity(chooser);
                }
            }));

            mAnalytics.onEventMoreShareClick(getActivity());
        } catch (Exception e)
        {
            unLockAll();

            ExLog.d(e.toString());
        }
    }

    @Override
    public void onHiddenReservationClick()
    {
        if (mStayBookingDetail == null || lock() == true)
        {
            return;
        }

        getViewInterface().showSimpleDialog(getString(R.string.dialog_notice2), getString(R.string.dialog_msg_delete_booking)//
            , getString(R.string.dialog_btn_text_yes), getString(R.string.dialog_btn_text_no), new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    screenLock(true);

                    addCompositeDisposable(mBookingRemoteImpl.getStayHiddenBooking(mReservationIndex) //
                        .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
                        {
                            @Override
                            public void accept(@NonNull Boolean result) throws Exception
                            {
                                unLockAll();

                                if (result == true)
                                {
                                    getViewInterface().showSimpleDialog(getString(R.string.dialog_notice2)//
                                        , getString(R.string.message_booking_delete_booking)//
                                        , getString(R.string.dialog_btn_text_confirm), null, new DialogInterface.OnDismissListener()
                                        {
                                            @Override
                                            public void onDismiss(DialogInterface dialog)
                                            {
                                                setResult(Constants.CODE_RESULT_ACTIVITY_REFRESH);
                                                finish();
                                            }
                                        });

                                    mAnalytics.onEventHideBookingSuccess(getActivity(), mStayBookingDetail.stayIndex);
                                } else
                                {
                                    getViewInterface().showSimpleDialog(getString(R.string.dialog_notice2)//
                                        , getString(R.string.message_booking_failed_delete_booking)//
                                        , getString(R.string.dialog_btn_text_confirm), null, new DialogInterface.OnDismissListener()
                                        {
                                            @Override
                                            public void onDismiss(DialogInterface dialog)
                                            {
                                            }
                                        });
                                }
                            }
                        }, new Consumer<Throwable>()
                        {
                            @Override
                            public void accept(@NonNull Throwable throwable) throws Exception
                            {
                                unLockAll();

                                getViewInterface().showSimpleDialog(getString(R.string.dialog_notice2)//
                                    , getString(R.string.message_booking_failed_delete_booking)//
                                    , getString(R.string.dialog_btn_text_confirm), null, new DialogInterface.OnDismissListener()
                                    {
                                        @Override
                                        public void onDismiss(DialogInterface dialog)
                                        {
                                        }
                                    });
                            }
                        }));
                }
            }, null, null, new DialogInterface.OnDismissListener()
            {
                @Override
                public void onDismiss(DialogInterface dialog)
                {
                    unLock();
                }
            }, true);

        mAnalytics.onEventHideBookingClick(getActivity(), mStayBookingDetail.stayIndex);
    }

    @Override
    public void onReviewClick(String reviewStatus)
    {
        if (getActivity() == null || lock() == true)
        {
            return;
        }

        if (PlaceBookingDetail.ReviewStatusType.ADDABLE.equalsIgnoreCase(reviewStatus) == true)
        {
            addCompositeDisposable(mCommonRemoteImpl.getReview("hotel", mReservationIndex) //
                .subscribeOn(Schedulers.io()).map(new Function<Review, com.twoheart.dailyhotel.model.Review>()
                {
                    @Override
                    public com.twoheart.dailyhotel.model.Review apply(@io.reactivex.annotations.NonNull Review review) throws Exception
                    {
                        return reviewToReviewParcelable(review);
                    }
                }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<com.twoheart.dailyhotel.model.Review>()
                {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull com.twoheart.dailyhotel.model.Review review) throws Exception
                    {
                        Intent intent = ReviewActivity.newInstance(getActivity(), review);
                        startActivityForResult(intent, StayBookingDetailActivity.REQUEST_CODE_REVIEW);
                    }
                }, new Consumer<Throwable>()
                {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception
                    {
                        onHandleError(throwable);
                    }
                }));

            mAnalytics.onEventReviewClick(getActivity());
        }
    }

    private com.twoheart.dailyhotel.model.Review reviewToReviewParcelable(Review review)
    {
        com.twoheart.dailyhotel.model.Review reviewParcelable = new com.twoheart.dailyhotel.model.Review();

        if (review == null)
        {
            return reviewParcelable;
        }

        reviewParcelable.requiredCommentReview = review.requiredCommentReview;
        reviewParcelable.reserveIdx = review.reserveIdx;

        com.twoheart.dailyhotel.model.ReviewItem reviewItemParcelable = new com.twoheart.dailyhotel.model.ReviewItem();

        ReviewItem reviewItem = review.getReviewItem();

        if (reviewItem != null)
        {
            reviewItemParcelable.itemIdx = reviewItem.itemIdx;
            reviewItemParcelable.itemName = reviewItem.itemName;
            reviewItemParcelable.imageUrl = reviewItem.imageUrl;

            if ("HOTEL".equalsIgnoreCase(reviewItem.serviceType) == true)
            {
                reviewItemParcelable.placeType = Constants.PlaceType.HOTEL;
            } else if ("GOURMET".equalsIgnoreCase(reviewItem.serviceType) == true)
            {
                reviewItemParcelable.placeType = Constants.PlaceType.FNB;
            } else
            {
                ExLog.d("unKnown service type");
            }

            reviewItemParcelable.useEndDate = reviewItem.useEndDate;
            reviewItemParcelable.useStartDate = reviewItem.useStartDate;
        }

        reviewParcelable.setReviewItem(reviewItemParcelable);

        //
        ArrayList<ReviewPickQuestion> reviewPickQuestionListParcelable = new ArrayList<>();

        List<ReviewQuestionItem> reviewPickQuestionList = review.getReviewPickQuestionList();

        if (reviewPickQuestionList != null && reviewPickQuestionList.size() > 0)
        {
            for (ReviewQuestionItem reviewQuestionItem : reviewPickQuestionList)
            {
                ReviewPickQuestion reviewPickQuestion = new ReviewPickQuestion();
                reviewPickQuestion.title = reviewQuestionItem.title;
                reviewPickQuestion.description = reviewQuestionItem.description;
                reviewPickQuestion.answerCode = reviewQuestionItem.answerCode;

                //
                ArrayList<com.twoheart.dailyhotel.model.ReviewAnswerValue> reviewAnswerValueListParcelable = new ArrayList<>();

                List<ReviewAnswerValue> reviewAnswerValueList = reviewQuestionItem.getAnswerValueList();

                if (reviewAnswerValueList != null && reviewAnswerValueList.size() > 0)
                {
                    for (ReviewAnswerValue reviewAnswerValue : reviewAnswerValueList)
                    {
                        com.twoheart.dailyhotel.model.ReviewAnswerValue reviewAnswerValueParcelable = new com.twoheart.dailyhotel.model.ReviewAnswerValue();

                        reviewAnswerValueParcelable.code = reviewAnswerValue.code;
                        reviewAnswerValueParcelable.description = reviewAnswerValue.description;

                        reviewAnswerValueListParcelable.add(reviewAnswerValueParcelable);
                    }

                    // 짝수개로 맞춘다.
                    if (reviewAnswerValueListParcelable.size() % 2 == 1)
                    {
                        reviewAnswerValueListParcelable.add(new com.twoheart.dailyhotel.model.ReviewAnswerValue());
                    }
                }

                reviewPickQuestion.setAnswerValueList(reviewAnswerValueListParcelable);
                reviewPickQuestionListParcelable.add(reviewPickQuestion);
            }
        }

        reviewParcelable.setReviewPickQuestionList(reviewPickQuestionListParcelable);

        //
        ArrayList<ReviewScoreQuestion> reviewScoreQuestionListParcelable = new ArrayList<ReviewScoreQuestion>();

        List<ReviewQuestionItem> reviewScoreQuestionList = review.getReviewScoreQuestionList();

        if (reviewScoreQuestionList != null && reviewScoreQuestionList.size() > 0)
        {
            for (ReviewQuestionItem reviewQuestionItem : reviewScoreQuestionList)
            {
                ReviewScoreQuestion reviewScoreQuestion = new ReviewScoreQuestion();
                reviewScoreQuestion.title = reviewQuestionItem.title;
                reviewScoreQuestion.description = reviewQuestionItem.description;
                reviewScoreQuestion.answerCode = reviewQuestionItem.answerCode;

                reviewScoreQuestionListParcelable.add(reviewScoreQuestion);
            }
        }

        reviewParcelable.setReviewScoreQuestionList(reviewScoreQuestionListParcelable);

        return reviewParcelable;
    }

    @Override
    public void onRefundClick()
    {
        if (mStayBookingDetail == null || lock() == true)
        {
            return;
        }

        switch (getRefundPolicyStatus(mStayBookingDetail.readyForRefund, mRefundPolicy))
        {
            case RefundPolicy.STATUS_NO_CHARGE_REFUND:
            {
                Intent intent = StayAutoRefundActivity.newInstance(getActivity(), getStayBookingDetail(mStayBookingDetail, mRefundPolicy), mAggregationId);
                startActivityForResult(intent, StayBookingDetailActivity.REQUEST_CODE_REFUND);

                mAnalytics.onEventRefundClick(getActivity(), true);

                break;
            }

            default:
                getViewInterface().showRefundCallDialog(new DialogInterface.OnDismissListener()
                {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface)
                    {
                        unLockAll();
                    }
                });

                mAnalytics.onEventRefundClick(getActivity(), false);
                break;
        }
    }

    private com.twoheart.dailyhotel.model.StayBookingDetail getStayBookingDetail(StayBookingDetail stayBookingDetail, RefundPolicy refundPolicy)
    {
        if (stayBookingDetail == null)
        {
            return null;
        }

        com.twoheart.dailyhotel.model.StayBookingDetail oldBookingDetail = new com.twoheart.dailyhotel.model.StayBookingDetail();
        oldBookingDetail.setData(stayBookingDetail);

        if (refundPolicy != null)
        {
            if (refundPolicy.refundManual == true)
            {
                if (com.twoheart.dailyhotel.model.StayBookingDetail.STATUS_NRD.equalsIgnoreCase(refundPolicy.refundPolicy) == true)
                {
                    oldBookingDetail.refundPolicy = refundPolicy.refundPolicy;
                    oldBookingDetail.mRefundComment = refundPolicy.comment;
                } else
                {
                    oldBookingDetail.refundPolicy = com.twoheart.dailyhotel.model.StayBookingDetail.STATUS_SURCHARGE_REFUND;
                    oldBookingDetail.mRefundComment = refundPolicy.message;
                }
            } else
            {
                if (com.twoheart.dailyhotel.model.StayBookingDetail.STATUS_NONE.equalsIgnoreCase(refundPolicy.refundPolicy) == true)
                {
                    oldBookingDetail.isVisibleRefundPolicy = false;
                } else
                {
                    oldBookingDetail.mRefundComment = refundPolicy.comment;
                }

                oldBookingDetail.refundPolicy = refundPolicy.refundPolicy;
            }
        }
        return oldBookingDetail;
    }

    @Override
    public void onRecommendListItemViewAllClick()
    {
        if (getActivity() == null || mStayBookingDetail == null || lock() == true)
        {
            return;
        }

        try
        {
            String title = getString(R.string.label_home_view_all);

            long currentDateTime = DailyCalendar.convertStringToDate(mCommonDateTime.currentDateTime).getTime();
            long checkInDateTime = DailyCalendar.convertStringToDate(mStayBookingDetail.checkInDateTime).getTime();

            String visitDay = mStayBookingDetail.checkInDateTime;
            if (currentDateTime > checkInDateTime)
            {
                visitDay = mCommonDateTime.dailyDateTime;
            }

            GourmetBookingDay gourmetBookingDay = new GourmetBookingDay();
            gourmetBookingDay.setVisitDay(visitDay);

            ArrayList<Gourmet> gourmetList = new ArrayList<>();
            if (mRecommendGourmetList != null && mRecommendGourmetList.size() > 0)
            {
                gourmetList.addAll(mRecommendGourmetList);
            }

            Location location = new Location((String) null);
            location.setLatitude(mStayBookingDetail.latitude);
            location.setLongitude(mStayBookingDetail.longitude);

            Intent intent = GourmetBookingDetailMapActivity.newInstance( //
                getActivity(), title, gourmetBookingDay, gourmetList //
                , location, mStayBookingDetail.stayName, false);

            getActivity().startActivityForResult(intent, StayBookingDetailActivity.REQUEST_CODE_RECOMMEND_GOURMET_VIEW_ALL);

            mAnalytics.onEventRecommendGourmetViewAllClick(getActivity());

        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }
    }

    @Override
    public void onRecommendListItemClick(View view)
    {
        if (getActivity() == null || mStayBookingDetail == null || mCommonDateTime == null || lock() == true)
        {
            return;
        }

        CarouselListItem item = (CarouselListItem) view.getTag();
        if (item == null)
        {
            return;
        }

        Gourmet gourmet = item.getItem();
        if (gourmet == null)
        {
            return;
        }

        startGourmetDetail(view, gourmet, mCommonDateTime, mStayBookingDetail);

        mAnalytics.onEventRecommendGourmetItemClick(getActivity(), gourmet.index, gourmet.distance);
    }

    @Override
    public void onRecommendListItemLongClick(View view)
    {
        if (getActivity() == null || mStayBookingDetail == null || mCommonDateTime == null || lock() == true)
        {
            return;
        }

        CarouselListItem item = (CarouselListItem) view.getTag();
        if (item == null)
        {
            return;
        }

        Gourmet gourmet = item.getItem();
        if (gourmet == null)
        {
            return;
        }

        try
        {
            mViewByLongPress = view;
            mGourmetByLongPress = gourmet;

            getViewInterface().setBlurVisible(getActivity(), true);

            GourmetBookingDay gourmetBookingDay = new GourmetBookingDay();
            gourmetBookingDay.setVisitDay(mCommonDateTime.dailyDateTime);

            Intent intent = GourmetPreviewActivity.newInstance(getActivity(), gourmetBookingDay, gourmet);

            startActivityForResult(intent, StayBookingDetailActivity.REQUEST_CODE_PREVIEW);
        } catch (Exception e)
        {
            unLockAll();
        }
    }
}
