package com.daily.dailyhotel.screen.home.stay.outbound.detail;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.SharedElementCallback;
import android.util.SparseArray;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.daily.base.BaseActivity;
import com.daily.base.BaseAnalyticsInterface;
import com.daily.base.exception.BaseException;
import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ExLog;
import com.daily.base.util.ScreenUtils;
import com.daily.base.widget.DailyToast;
import com.daily.dailyhotel.base.BaseExceptionPresenter;
import com.daily.dailyhotel.entity.CarouselListItem;
import com.daily.dailyhotel.entity.CommonDateTime;
import com.daily.dailyhotel.entity.DetailImageInformation;
import com.daily.dailyhotel.entity.ImageMap;
import com.daily.dailyhotel.entity.People;
import com.daily.dailyhotel.entity.StayBookDateTime;
import com.daily.dailyhotel.entity.StayOutbound;
import com.daily.dailyhotel.entity.StayOutboundDetail;
import com.daily.dailyhotel.entity.StayOutboundRoom;
import com.daily.dailyhotel.entity.StayOutbounds;
import com.daily.dailyhotel.entity.User;
import com.daily.dailyhotel.parcel.analytics.ImageListAnalyticsParam;
import com.daily.dailyhotel.parcel.analytics.NavigatorAnalyticsParam;
import com.daily.dailyhotel.parcel.analytics.StayOutboundDetailAnalyticsParam;
import com.daily.dailyhotel.parcel.analytics.StayOutboundPaymentAnalyticsParam;
import com.daily.dailyhotel.repository.local.RecentlyLocalImpl;
import com.daily.dailyhotel.repository.remote.CommonRemoteImpl;
import com.daily.dailyhotel.repository.remote.ProfileRemoteImpl;
import com.daily.dailyhotel.repository.remote.StayOutboundRemoteImpl;
import com.daily.dailyhotel.screen.common.dialog.call.CallDialogActivity;
import com.daily.dailyhotel.screen.common.dialog.navigator.NavigatorDialogActivity;
import com.daily.dailyhotel.screen.common.dialog.wish.WishDialogActivity;
import com.daily.dailyhotel.screen.common.images.ImageListActivity;
import com.daily.dailyhotel.screen.common.web.DailyWebActivity;
import com.daily.dailyhotel.screen.home.stay.outbound.calendar.StayOutboundCalendarActivity;
import com.daily.dailyhotel.screen.home.stay.outbound.detail.amenities.AmenityListActivity;
import com.daily.dailyhotel.screen.home.stay.outbound.payment.StayOutboundPaymentActivity;
import com.daily.dailyhotel.screen.home.stay.outbound.people.SelectPeopleActivity;
import com.daily.dailyhotel.screen.home.stay.outbound.preview.StayOutboundPreviewActivity;
import com.daily.dailyhotel.screen.mydaily.reward.RewardActivity;
import com.daily.dailyhotel.storage.preference.DailyPreference;
import com.daily.dailyhotel.storage.preference.DailyRemoteConfigPreference;
import com.daily.dailyhotel.storage.preference.DailyUserPreference;
import com.facebook.drawee.view.SimpleDraweeView;
import com.twoheart.dailyhotel.DailyHotel;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Customer;
import com.twoheart.dailyhotel.screen.common.HappyTalkCategoryDialog;
import com.twoheart.dailyhotel.screen.common.ZoomMapActivity;
import com.twoheart.dailyhotel.screen.information.FAQActivity;
import com.twoheart.dailyhotel.screen.mydaily.member.AddProfileSocialActivity;
import com.twoheart.dailyhotel.screen.mydaily.member.EditProfilePhoneActivity;
import com.twoheart.dailyhotel.screen.mydaily.member.LoginActivity;
import com.twoheart.dailyhotel.util.AppResearch;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.DailyCalendar;
import com.twoheart.dailyhotel.util.DailyDeepLink;
import com.twoheart.dailyhotel.util.DailyExternalDeepLink;
import com.twoheart.dailyhotel.util.KakaoLinkManager;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function4;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

/**
 * Created by sheldon
 * Clean Architecture
 */
public class StayOutboundDetailPresenter extends BaseExceptionPresenter<StayOutboundDetailActivity, StayOutboundDetailViewInterface> implements StayOutboundDetailView.OnEventListener
{
    private static final int DAYS_OF_MAXCOUNT = 365;
    private static final int NIGHTS_OF_MAXCOUNT = 28;

    public static final int STATUS_NONE = 0;
    public static final int STATUS_ROOM_LIST = 1;
    public static final int STATUS_BOOKING = 2;
    public static final int STATUS_SOLD_OUT = 3;
    public static final int STATUS_FINISH = 4;

    public enum PriceType
    {
        AVERAGE,
        TOTAL
    }

    StayOutboundDetailAnalyticsInterface mAnalytics;

    private StayOutboundRemoteImpl mStayOutboundRemoteImpl;
    private CommonRemoteImpl mCommonRemoteImpl;
    private ProfileRemoteImpl mProfileRemoteImpl;
    private RecentlyLocalImpl mRecentlyLocalImpl;

    int mStayIndex, mListTotalPrice;
    private String mStayName, mStayEnglishName;
    String mImageUrl;
    StayBookDateTime mStayBookDateTime;
    private CommonDateTime mCommonDateTime;
    StayOutboundDetail mStayOutboundDetail;
    People mPeople;
    StayOutboundRoom mSelectedRoom;
    ArrayList<CarouselListItem> mRecommendAroundList;
    View mViewByLongPress;
    android.support.v4.util.Pair[] mPairsByLongPress;
    StayOutbound mStayOutboundByLongPress;

    private int mStatus = STATUS_NONE;

    private boolean mIsUsedMultiTransition;
    private boolean mIsDeepLink;
    private boolean mCheckChangedPrice;
    private int mGradientType;
    boolean mShowCalendar;
    boolean mShowTrueVR;

    DailyDeepLink mDailyDeepLink;
    private AppResearch mAppResearch;

    public interface StayOutboundDetailAnalyticsInterface extends BaseAnalyticsInterface
    {
        void setAnalyticsParam(StayOutboundDetailAnalyticsParam analyticsParam);

        StayOutboundDetailAnalyticsParam getAnalyticsParam();

        StayOutboundDetailAnalyticsParam getAnalyticsParam(StayOutbound stayOutbound, String grade);

        void onScreen(Activity activity, StayBookDateTime stayBookDateTime, StayOutboundDetail stayOutboundDetail, int priceFromList);

        void onScreenRoomList(Activity activity, int stayIndex, boolean provideRewardSticker);

        void onEventHasRecommendList(Activity activity, boolean hasData);

        void onEventRecommendItemClick(Activity activity, int stayIndex, int clickStayIndex);

        void onEventRecommendItemList(Activity activity, int stayIndex, List<Integer> stayIndexList);

        void onEventBookingClick(Activity activity, int stayIndex, String stayName, String roomName //
            , int discountPrice, boolean provideRewardSticker, String checkInDate, int nights);

        void onEventWishClick(Activity activity);

        void onEventShareKakaoClick(Activity activity);

        void onEventMoreShareClick(Activity activity);

        StayOutboundPaymentAnalyticsParam getPaymentAnalyticsParam(String grade, boolean nrd, boolean showOriginalPrice);
    }

    public StayOutboundDetailPresenter(@NonNull StayOutboundDetailActivity activity)
    {
        super(activity);
    }

    @NonNull
    @Override
    protected StayOutboundDetailViewInterface createInstanceViewInterface()
    {
        return new StayOutboundDetailView(getActivity(), this);
    }

    @Override
    public void constructorInitialize(StayOutboundDetailActivity activity)
    {
        setContentView(R.layout.activity_stay_outbound_detail_data);

        mAppResearch = new AppResearch(activity);
        setAnalytics(new StayOutboundDetailAnalyticsImpl());

        mStayOutboundRemoteImpl = new StayOutboundRemoteImpl(activity);
        mCommonRemoteImpl = new CommonRemoteImpl(activity);
        mProfileRemoteImpl = new ProfileRemoteImpl(activity);
        mRecentlyLocalImpl = new RecentlyLocalImpl(activity);

        setPeople(People.DEFAULT_ADULTS, null);

        setStatus(STATUS_NONE);

        setRefresh(false);

        Observable<Boolean> observable = getViewInterface().hideRoomList(false);

        if (observable != null)
        {
            addCompositeDisposable(observable.subscribe());
        }
    }

    @Override
    public void setAnalytics(BaseAnalyticsInterface analytics)
    {
        mAnalytics = (StayOutboundDetailAnalyticsInterface) analytics;
    }

    @Override
    public boolean onIntent(Intent intent)
    {
        if (intent == null)
        {
            return true;
        }

        if (intent.hasExtra(BaseActivity.INTENT_EXTRA_DATA_DEEPLINK) == true)
        {
            mAnalytics.setAnalyticsParam(new StayOutboundDetailAnalyticsParam());

            try
            {
                mDailyDeepLink = DailyDeepLink.getNewInstance(Uri.parse(intent.getStringExtra(BaseActivity.INTENT_EXTRA_DATA_DEEPLINK)));
            } catch (Exception e)
            {
                mDailyDeepLink = null;

                return false;
            }

            mIsUsedMultiTransition = false;
            mIsDeepLink = true;

            if (mDailyDeepLink != null && mDailyDeepLink.isExternalDeepLink() == true)
            {
                addCompositeDisposable(mCommonRemoteImpl.getCommonDateTime().subscribe(new Consumer<CommonDateTime>()
                {
                    @Override
                    public void accept(CommonDateTime commonDateTime) throws Exception
                    {
                        setCommonDateTime(commonDateTime);

                        DailyExternalDeepLink externalDeepLink = (DailyExternalDeepLink) mDailyDeepLink;

                        mStayIndex = Integer.parseInt(externalDeepLink.getIndex());

                        int nights = 1;

                        try
                        {
                            nights = Integer.parseInt(externalDeepLink.getNights());
                        } catch (Exception e)
                        {
                            ExLog.d(e.toString());
                        } finally
                        {
                            if (nights <= 0)
                            {
                                nights = 1;
                            }
                        }

                        String date = externalDeepLink.getDate();
                        int datePlus = externalDeepLink.getDatePlus();
                        mShowCalendar = externalDeepLink.isShowCalendar();
                        mShowTrueVR = externalDeepLink.isShowVR();

                        if (DailyTextUtils.isTextEmpty(date) == false)
                        {
                            if (Integer.parseInt(date) > Integer.parseInt(DailyCalendar.convertDateFormatString(commonDateTime.currentDateTime, DailyCalendar.ISO_8601_FORMAT, "yyyyMMdd")))
                            {
                                Date checkInDate = DailyCalendar.convertDate(date, "yyyyMMdd", TimeZone.getTimeZone("GMT+09:00"));

                                setStayBookDateTime(DailyCalendar.format(checkInDate, DailyCalendar.ISO_8601_FORMAT), 0, nights);
                            } else
                            {
                                setStayBookDateTime(commonDateTime.currentDateTime, 0, 1);
                            }
                        } else if (datePlus >= 0)
                        {
                            setStayBookDateTime(commonDateTime.currentDateTime, datePlus, nights);
                        } else
                        {
                            setStayBookDateTime(commonDateTime.currentDateTime, 0, 1);
                        }

                        mDailyDeepLink.clear();
                        mDailyDeepLink = null;

                        setRefresh(true);
                        onRefresh(true);
                    }
                }, new Consumer<Throwable>()
                {
                    @Override
                    public void accept(Throwable throwable) throws Exception
                    {
                        onHandleError(throwable);
                    }
                }));
            }
        } else
        {
            mIsUsedMultiTransition = intent.getBooleanExtra(StayOutboundDetailActivity.INTENT_EXTRA_DATA_MULTITRANSITION, false);
            mGradientType = intent.getIntExtra(StayOutboundDetailActivity.INTENT_EXTRA_DATA_CALL_GRADIENT_TYPE, StayOutboundDetailActivity.TRANS_GRADIENT_BOTTOM_TYPE_NONE);
            mIsDeepLink = false;

            mStayIndex = intent.getIntExtra(StayOutboundDetailActivity.INTENT_EXTRA_DATA_STAY_INDEX, -1);

            if (mStayIndex == -1)
            {
                return false;
            }

            mStayName = intent.getStringExtra(StayOutboundDetailActivity.INTENT_EXTRA_DATA_STAY_NAME);
            mStayEnglishName = intent.getStringExtra(StayOutboundDetailActivity.INTENT_EXTRA_DATA_STAY_ENGLISH_NAME);
            mImageUrl = intent.getStringExtra(StayOutboundDetailActivity.INTENT_EXTRA_DATA_IMAGE_URL);
            mListTotalPrice = intent.getIntExtra(StayOutboundDetailActivity.INTENT_EXTRA_DATA_LIST_PRICE, StayOutboundDetailActivity.NONE_PRICE);

            String checkInDateTime = intent.getStringExtra(StayOutboundDetailActivity.INTENT_EXTRA_DATA_CHECK_IN_DATE_TIME);
            String checkOutDateTime = intent.getStringExtra(StayOutboundDetailActivity.INTENT_EXTRA_DATA_CHECK_OUT_DATE_TIME);

            setStayBookDateTime(checkInDateTime, checkOutDateTime);

            int numberOfAdults = intent.getIntExtra(StayOutboundDetailActivity.INTENT_EXTRA_DATA_NUMBER_OF_ADULTS, 2);
            ArrayList<Integer> childAgeList = intent.getIntegerArrayListExtra(StayOutboundDetailActivity.INTENT_EXTRA_DATA_CHILD_LIST);

            setPeople(numberOfAdults, childAgeList);

            mAnalytics.setAnalyticsParam(intent.getParcelableExtra(BaseActivity.INTENT_EXTRA_DATA_ANALYTICS));
        }

        return true;
    }

    @Override
    public void onPostCreate()
    {
        if (mIsDeepLink == false && mIsUsedMultiTransition == true)
        {
            getViewInterface().setSharedElementTransitionEnabled(true, mGradientType);
            getViewInterface().setInitializedTransLayout(mStayName, mStayEnglishName, mImageUrl);
        } else
        {
            getViewInterface().setSharedElementTransitionEnabled(false, StayOutboundDetailActivity.TRANS_GRADIENT_BOTTOM_TYPE_NONE);
            getViewInterface().setInitializedImage(mImageUrl);
        }

        addCompositeDisposable(mRecentlyLocalImpl.addRecentlyItem( //
            Constants.ServiceType.OB_STAY, mStayIndex, mStayName, null, mImageUrl, true) //
            .observeOn(Schedulers.io()).subscribe());

        if (mIsUsedMultiTransition == true)
        {
            screenLock(false);

            Disposable disposable = Observable.timer(2, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())//
                .observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> screenLock(true));

            addCompositeDisposable(disposable);

            onRefresh(getViewInterface().getSharedElementTransition(mGradientType), disposable);
        } else
        {
            if (mIsDeepLink == false)
            {
                setRefresh(true);
            }
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();

        //        String checkInDate = mStayBookDateTime == null ? "" : mStayBookDateTime.getCheckInDateTime("yyyyMMdd");
        //        int nights = mStayBookDateTime == null ? 1 : mStayBookDateTime.getNights();
        //
        //        mAnalytics.onScreen(getActivity(), checkInDate, nights);

        if (isRefresh() == true)
        {
            onRefresh(true);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (Util.supportPreview(getActivity()) == true && getViewInterface().isBlurVisible() == true)
        {
            getViewInterface().setBlurVisible(getActivity(), false);
        }

        if (mStatus == STATUS_BOOKING)
        {
            onHideRoomListClick(false);
        }

        if (isRefresh() == true)
        {
            onRefresh(true);
        }

        if (DailyHotel.isLogin() == false && DailyRemoteConfigPreference.getInstance(getActivity()).isKeyRemoteConfigRewardStickerCampaignEnabled() == true)
        {
            getViewInterface().startCampaignStickerAnimation();
        }

        mAppResearch.onResume("outbound_스테이", mStayIndex);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        getViewInterface().stopCampaignStickerAnimation();

        mAppResearch.onPause("outbound_스테이", mStayIndex);
    }

    @Override
    public void onDestroy()
    {
        // 꼭 호출해 주세요.
        super.onDestroy();
    }

    @Override
    public void onFinish()
    {
        super.onFinish();

        if (mIsUsedMultiTransition == false)
        {
            getActivity().overridePendingTransition(R.anim.hold, R.anim.slide_out_right);
        }
    }

    @Override
    public synchronized boolean onBackPressed()
    {
        switch (mStatus)
        {
            case STATUS_BOOKING:
                onHideRoomListClick(true);
                return true;

            case STATUS_FINISH:
                break;

            default:
                setStatus(STATUS_FINISH);

                if (mIsUsedMultiTransition == true)
                {
                    if (lock() == true)
                    {
                        return true;
                    }

                    getViewInterface().setTransitionVisible(true);
                    getViewInterface().scrollTop();

                    Single.just(mIsUsedMultiTransition).delaySubscription(300, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
                    {
                        @Override
                        public void accept(@io.reactivex.annotations.NonNull Boolean aBoolean) throws Exception
                        {
                            getActivity().onBackPressed();
                        }
                    });

                    return true;
                }
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
            case StayOutboundDetailActivity.REQUEST_CODE_CALENDAR:
            {
                if (resultCode == Activity.RESULT_OK && data != null)
                {
                    if (data.hasExtra(StayOutboundCalendarActivity.INTENT_EXTRA_DATA_CHECKIN_DATETIME) == true//
                        && data.hasExtra(StayOutboundCalendarActivity.INTENT_EXTRA_DATA_CHECKOUT_DATETIME) == true)
                    {
                        String checkInDateTime = data.getStringExtra(StayOutboundCalendarActivity.INTENT_EXTRA_DATA_CHECKIN_DATETIME);
                        String checkOutDateTime = data.getStringExtra(StayOutboundCalendarActivity.INTENT_EXTRA_DATA_CHECKOUT_DATETIME);

                        if (DailyTextUtils.isTextEmpty(checkInDateTime, checkOutDateTime) == true)
                        {
                            return;
                        }

                        setStayBookDateTime(checkInDateTime, checkOutDateTime);
                        setRefresh(true);
                    }
                }
                break;
            }

            case StayOutboundDetailActivity.REQUEST_CODE_PEOPLE:
            {
                if (resultCode == Activity.RESULT_OK && data != null)
                {
                    if (data.hasExtra(SelectPeopleActivity.INTENT_EXTRA_DATA_NUMBER_OF_ADULTS) == true && data.hasExtra(SelectPeopleActivity.INTENT_EXTRA_DATA_CHILD_LIST) == true)
                    {
                        int numberOfAdults = data.getIntExtra(SelectPeopleActivity.INTENT_EXTRA_DATA_NUMBER_OF_ADULTS, People.DEFAULT_ADULTS);
                        ArrayList<Integer> childAgeList = data.getIntegerArrayListExtra(SelectPeopleActivity.INTENT_EXTRA_DATA_CHILD_LIST);

                        setPeople(numberOfAdults, childAgeList);
                        setRefresh(true);
                    }
                }
                break;
            }

            case StayOutboundDetailActivity.REQUEST_CODE_HAPPYTALK:
                break;

            case StayOutboundDetailActivity.REQUEST_CODE_CALL:
                break;

            case StayOutboundDetailActivity.REQUEST_CODE_PAYMENT:
                if (resultCode == BaseActivity.RESULT_CODE_REFRESH)
                {
                    setResult(BaseActivity.RESULT_CODE_REFRESH);
                    setRefresh(true);
                }
                break;

            case StayOutboundDetailActivity.REQUEST_CODE_PROFILE_UPDATE:
            case StayOutboundDetailActivity.REQUEST_CODE_LOGIN_IN_BY_BOOKING:
                if (resultCode == Activity.RESULT_OK)
                {
                    onActionButtonClick();

                    // 결제 후에 돌아올때 리플래쉬 한다.
                    //                    setRefresh(true);

                    setResult(BaseActivity.RESULT_CODE_REFRESH);
                } else
                {
                    onHideRoomListClick(false);
                }
                break;

            case StayOutboundDetailActivity.REQUEST_CODE_LOGIN:
                if (resultCode == Activity.RESULT_OK)
                {
                    setRefresh(true);

                    setResult(BaseActivity.RESULT_CODE_REFRESH);
                }
                break;

            case StayOutboundDetailActivity.REQUEST_CODE_LOGIN_IN_BY_WISH:
                if (resultCode == Activity.RESULT_OK)
                {
                    onWishClick();

                    setRefresh(true);

                    setResult(BaseActivity.RESULT_CODE_DATA_CHANGED);
                }
                break;

            case StayOutboundDetailActivity.REQUEST_CODE_PREVIEW:
                if (resultCode == Activity.RESULT_OK)
                {
                    Observable.create(new ObservableOnSubscribe<Object>()
                    {
                        @Override
                        public void subscribe(ObservableEmitter<Object> e) throws Exception
                        {
                            startStayOutboundDetail(mViewByLongPress, mStayOutboundByLongPress, mPairsByLongPress);
                        }
                    }).subscribeOn(AndroidSchedulers.mainThread()).subscribe();
                }
                break;

            case StayOutboundDetailActivity.REQUEST_CODE_WISH_DIALOG:
                if (data != null)
                {
                    boolean wish = data.getBooleanExtra(WishDialogActivity.INTENT_EXTRA_DATA_WISH, false);

                    mStayOutboundDetail.myWish = wish;
                    mStayOutboundDetail.wishCount = wish ? mStayOutboundDetail.wishCount + 1 : mStayOutboundDetail.wishCount - 1;

                    notifyWishChanged();

                    setResult(BaseActivity.RESULT_CODE_DATA_CHANGED);
                }
                break;
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

        mSelectedRoom = null;

        onRefresh(new Observable<Boolean>()
        {
            @Override
            protected void subscribeActual(Observer<? super Boolean> observer)
            {
                observer.onNext(true);
                observer.onComplete();
            }
        }, null);
    }

    @Override
    public void onBackClick()
    {
        getActivity().onBackPressed();
    }

    @Override
    protected void onHandleError(Throwable throwable)
    {
        unLockAll();

        // 에러가 나는 경우 리스트로 복귀
        if (throwable instanceof BaseException)
        {
            // 팝업 에러 보여주기
            BaseException baseException = (BaseException) throwable;

            getViewInterface().showSimpleDialog(null, baseException.getMessage()//
                , getString(R.string.frag_error_btn), new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        setRefresh(true);
                        onRefresh(true);
                    }
                }, new DialogInterface.OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                        if (getActivity().isFinishing() == true)
                        {
                            return;
                        }

                        getActivity().onBackPressed();
                    }
                });
        } else if (throwable instanceof HttpException && ((HttpException) throwable).code() != BaseException.CODE_UNAUTHORIZED)
        {
            retrofit2.HttpException httpException = (HttpException) throwable;

            getViewInterface().showSimpleDialog(null, getString(R.string.act_base_network_connect)//
                , getString(R.string.frag_error_btn), new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        onRefresh(true);
                    }
                }, new DialogInterface.OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                        if (getActivity().isFinishing() == true)
                        {
                            return;
                        }

                        getActivity().onBackPressed();
                    }
                });

            DailyToast.showToast(getActivity(), getString(R.string.act_base_network_connect), DailyToast.LENGTH_LONG);

            Crashlytics.log(httpException.response().raw().request().url().toString());
            Crashlytics.logException(throwable);
        } else
        {
            super.onHandleError(throwable);
        }
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
    }

    @Override
    public void onWishClick()
    {
        if (mStayOutboundDetail == null || lock() == true)
        {
            return;
        }

        if (DailyHotel.isLogin() == false)
        {
            DailyToast.showToast(getActivity(), R.string.toast_msg_please_login, DailyToast.LENGTH_LONG);

            Intent intent = LoginActivity.newInstance(getActivity(), AnalyticsManager.Screen.DAILYHOTEL_DETAIL);
            startActivityForResult(intent, StayOutboundDetailActivity.REQUEST_CODE_LOGIN_IN_BY_WISH);
        } else
        {
            boolean wish = !mStayOutboundDetail.myWish;
            int wishCount = wish ? mStayOutboundDetail.wishCount + 1 : mStayOutboundDetail.wishCount - 1;

            notifyWishChanged(wishCount, wish);

            startActivityForResult(WishDialogActivity.newInstance(getActivity(), Constants.ServiceType.OB_STAY//
                , mStayOutboundDetail.index, wish, -1, AnalyticsManager.Screen.DAILYHOTEL_LIST), StayOutboundDetailActivity.REQUEST_CODE_WISH_DIALOG);

            mAnalytics.onEventWishClick(getActivity());
        }
    }

    @Override
    public void onShareKakaoClick()
    {
        if (mStayOutboundDetail == null || mStayBookDateTime == null)
        {
            return;
        }

        try
        {
            // 카카오톡 패키지 설치 여부
            getActivity().getPackageManager().getPackageInfo("com.kakao.talk", PackageManager.GET_META_DATA);

            String name = DailyUserPreference.getInstance(getActivity()).getName();

            if (DailyTextUtils.isTextEmpty(name) == true)
            {
                name = getString(R.string.label_friend) + "가";
            } else
            {
                name += "님이";
            }

            String imageUrl;

            ImageMap imageMap = mStayOutboundDetail.getImageList().get(0).getImageMap();

            if (ScreenUtils.getScreenWidth(getActivity()) >= ScreenUtils.DEFAULT_STAYOUTBOUND_XXHDPI_WIDTH)
            {
                if (DailyTextUtils.isTextEmpty(imageMap.bigUrl) == true)
                {
                    imageUrl = imageMap.smallUrl;
                } else
                {
                    imageUrl = imageMap.bigUrl;
                }
            } else
            {
                if (DailyTextUtils.isTextEmpty(imageMap.mediumUrl) == true)
                {
                    imageUrl = imageMap.smallUrl;
                } else
                {
                    imageUrl = imageMap.mediumUrl;
                }
            }

            KakaoLinkManager.newInstance(getActivity()).shareStayOutbound(name//
                , mStayOutboundDetail.name//
                , mStayOutboundDetail.address//
                , mStayOutboundDetail.index//
                , imageUrl//
                , mStayBookDateTime);

            mAnalytics.onEventShareKakaoClick(getActivity());
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
    }

    @Override
    public void onMoreShareClick()
    {
        if (mStayOutboundDetail == null || mStayBookDateTime == null)
        {
            return;
        }

        try
        {
            String name = DailyUserPreference.getInstance(getActivity()).getName();

            if (DailyTextUtils.isTextEmpty(name) == true)
            {
                name = getString(R.string.label_friend) + "가";
            } else
            {
                name += "님이";
            }

            int nights = mStayBookDateTime.getNights();

            String message = getString(R.string.message_detail_stay_outbound_share_sms//
                , name, mStayOutboundDetail.name//
                , mStayBookDateTime.getCheckInDateTime("yyyy.MM.dd(EEE)")//
                , mStayBookDateTime.getCheckOutDateTime("yyyy.MM.dd(EEE)")//
                , nights, nights + 1 //
                , mStayOutboundDetail.address);

            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("text/plain");

            intent.putExtra(Intent.EXTRA_SUBJECT, "");
            intent.putExtra(Intent.EXTRA_TEXT, message);
            Intent chooser = Intent.createChooser(intent, getString(R.string.label_doshare));
            startActivity(chooser);

            mAnalytics.onEventMoreShareClick(getActivity());
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }
    }

    @Override
    public void onImageClick(int position)
    {
        if (mStayOutboundDetail == null || mStayOutboundDetail.getImageList() == null//
            || mStayOutboundDetail.getImageList().size() == 0 || lock() == true)
        {
            return;
        }

        ImageListAnalyticsParam analyticsParam = new ImageListAnalyticsParam();
        analyticsParam.serviceType = Constants.ServiceType.OB_STAY;

        startActivityForResult(ImageListActivity.newInstance(getActivity(), mStayOutboundDetail.name//
            , mStayOutboundDetail.getImageList(), position, analyticsParam), StayOutboundDetailActivity.REQUEST_CODE_IMAGE_LIST);
    }

    @Override
    public void onCalendarClick()
    {
        if (mStayBookDateTime == null || lock() == true)
        {
            return;
        }

        try
        {
            Calendar startCalendar = DailyCalendar.getInstance();
            startCalendar.setTime(DailyCalendar.convertDate(mCommonDateTime.currentDateTime, DailyCalendar.ISO_8601_FORMAT));
            startCalendar.add(Calendar.DAY_OF_MONTH, -1);

            String startDateTime = DailyCalendar.format(startCalendar.getTime(), DailyCalendar.ISO_8601_FORMAT);

            startCalendar.add(Calendar.DAY_OF_MONTH, DAYS_OF_MAXCOUNT);

            String endDateTime = DailyCalendar.format(startCalendar.getTime(), DailyCalendar.ISO_8601_FORMAT);

            Intent intent = StayOutboundCalendarActivity.newInstance(getActivity()//
                , mStayBookDateTime.getCheckInDateTime(DailyCalendar.ISO_8601_FORMAT)//
                , mStayBookDateTime.getCheckOutDateTime(DailyCalendar.ISO_8601_FORMAT)//
                , startDateTime, endDateTime, NIGHTS_OF_MAXCOUNT, AnalyticsManager.ValueType.STAY, true, 0, true);

            startActivityForResult(intent, StayOutboundDetailActivity.REQUEST_CODE_CALENDAR);
        } catch (Exception e)
        {
            ExLog.e(e.toString());

            unLock();
        }
    }

    @Override
    public void onPeopleClick()
    {
        if (lock() == true)
        {
            return;
        }

        Intent intent;

        if (mPeople == null)
        {
            intent = SelectPeopleActivity.newInstance(getActivity(), People.DEFAULT_ADULTS, null);
        } else
        {
            intent = SelectPeopleActivity.newInstance(getActivity(), mPeople.numberOfAdults, mPeople.getChildAgeList());
        }

        startActivityForResult(intent, StayOutboundDetailActivity.REQUEST_CODE_PEOPLE);
    }

    @Override
    public void onMapClick()
    {
        if (Util.isInstallGooglePlayService(getActivity()) == true)
        {
            if (getActivity().isFinishing() == true || lock() == true)
            {
                return;
            }

            startActivityForResult(ZoomMapActivity.newInstance(getActivity()//
                , ZoomMapActivity.SourceType.HOTEL, mStayOutboundDetail.name, mStayOutboundDetail.address//
                , mStayOutboundDetail.latitude, mStayOutboundDetail.longitude, true), StayOutboundDetailActivity.REQUEST_CODE_MAP);
        } else
        {
            getViewInterface().showSimpleDialog(getString(R.string.dialog_title_googleplayservice)//
                , getString(R.string.dialog_msg_install_update_googleplayservice)//
                , getString(R.string.dialog_btn_text_install), getString(R.string.dialog_btn_text_cancel), //
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        try
                        {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.google.android.gms"));
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                            intent.setPackage("com.android.vending");
                            startActivity(intent);
                        } catch (ActivityNotFoundException e)
                        {
                            try
                            {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.gms"));
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                                intent.setPackage("com.android.vending");
                                startActivity(intent);
                            } catch (ActivityNotFoundException f)
                            {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.google.android.gms"));
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                                startActivity(intent);
                            }
                        }
                    }
                }, null, true);

        }
    }

    @Override
    public void onClipAddressClick(String address)
    {
        DailyTextUtils.clipText(getActivity(), address);

        DailyToast.showToast(getActivity(), R.string.message_detail_copy_address, DailyToast.LENGTH_SHORT);
    }

    @Override
    public void onNavigatorClick()
    {
        if (mStayOutboundDetail == null || lock() == true)
        {
            return;
        }

        NavigatorAnalyticsParam analyticsParam = new NavigatorAnalyticsParam();

        startActivityForResult(NavigatorDialogActivity.newInstance(getActivity(), mStayOutboundDetail.name//
            , mStayOutboundDetail.latitude, mStayOutboundDetail.longitude, true, analyticsParam), StayOutboundDetailActivity.REQUEST_CODE_NAVIGATOR);
    }

    @Override
    public void onConciergeClick()
    {
        getViewInterface().showConciergeDialog(new DialogInterface.OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialog)
            {
                unLockAll();
            }
        });
    }

    @Override
    public void onHideRoomListClick(boolean animation)
    {
        Observable<Boolean> observable = getViewInterface().hideRoomList(animation);

        if (observable != null)
        {
            if (animation == true)
            {
                screenLock(false);
            }

            addCompositeDisposable(observable.subscribe(new Consumer<Boolean>()
            {
                @Override
                public void accept(@io.reactivex.annotations.NonNull Boolean aBoolean) throws Exception
                {
                    if (animation == true)
                    {
                        unLockAll();
                    }

                    setStatus(STATUS_ROOM_LIST);
                }
            }));
        }
    }

    @Override
    public void onActionButtonClick()
    {
        switch (mStatus)
        {
            case STATUS_BOOKING:
                if (mSelectedRoom == null || lock() == true)
                {
                    return;
                }

                if (DailyHotel.isLogin() == false)
                {
                    DailyToast.showToast(getActivity(), R.string.toast_msg_please_login, DailyToast.LENGTH_LONG);

                    startActivityForResult(LoginActivity.newInstance(getActivity(), AnalyticsManager.Screen.DAILYHOTEL_HOTELDETAILVIEW_OUTBOUND)//
                        , StayOutboundDetailActivity.REQUEST_CODE_LOGIN_IN_BY_BOOKING);
                } else
                {
                    addCompositeDisposable(mProfileRemoteImpl.getProfile().subscribe(new Consumer<User>()
                    {
                        @Override
                        public void accept(@io.reactivex.annotations.NonNull User user) throws Exception
                        {
                            switch (Util.verifyUserInformation(user))
                            {
                                case Util.VERIFY_USER:
                                    StayOutboundPaymentAnalyticsParam analyticsParam = mAnalytics.getPaymentAnalyticsParam(getString(R.string.label_stay_outbound_detail_grade, (int) mStayOutboundDetail.rating)//
                                        , mSelectedRoom.nonRefundable, mSelectedRoom.promotion);

                                    startActivityForResult(StayOutboundPaymentActivity.newInstance(getActivity(), mStayOutboundDetail.index//
                                        , mStayOutboundDetail.name, mImageUrl, mSelectedRoom.total//
                                        , mStayBookDateTime.getCheckInDateTime(DailyCalendar.ISO_8601_FORMAT)//
                                        , mStayBookDateTime.getCheckOutDateTime(DailyCalendar.ISO_8601_FORMAT)//
                                        , mPeople.numberOfAdults, mPeople.getChildAgeList()//
                                        , mSelectedRoom.roomName, mSelectedRoom.rateCode, mSelectedRoom.rateKey//
                                        , mSelectedRoom.roomTypeCode, mSelectedRoom.roomBedTypeId, mSelectedRoom.vendorType, analyticsParam)//
                                        , StayOutboundDetailActivity.REQUEST_CODE_PAYMENT);
                                    break;

                                case Util.VERIFY_DAILY_USER_NOT_VERIFY_PHONE:
                                    startActivityForResult(EditProfilePhoneActivity.newInstance(getActivity()//
                                        , EditProfilePhoneActivity.Type.NEED_VERIFICATION_PHONENUMBER, user.phone)//
                                        , StayOutboundDetailActivity.REQUEST_CODE_PROFILE_UPDATE);
                                    break;

                                case Util.VERIFY_SOCIAL_USER_NOT_VERIFY:
                                case Util.VERIFY_SOCIAL_USER_NOT_VERIFY_EMAIL:
                                    startActivityForResult(AddProfileSocialActivity.newInstance(getActivity()//
                                        , new Customer(user), user.birthday), StayOutboundDetailActivity.REQUEST_CODE_PROFILE_UPDATE);
                                    break;

                                case Util.VERIFY_SOCIAL_USER_NOT_VERIFY_PHONE:
                                    startActivityForResult(EditProfilePhoneActivity.newInstance(getActivity()//
                                        , EditProfilePhoneActivity.Type.WRONG_PHONENUMBER, user.phone)//
                                        , StayOutboundDetailActivity.REQUEST_CODE_PROFILE_UPDATE);
                                    break;

                                default:
                                    break;
                            }
                        }
                    }, new Consumer<Throwable>()
                    {
                        @Override
                        public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception
                        {
                            onHandleError(throwable);
                        }
                    }));

                    String checkInDate = mStayBookDateTime.getCheckInDateTime("yyyyMMdd");
                    int nights = mStayBookDateTime.getNights();

                    mAnalytics.onEventBookingClick(getActivity(), mStayOutboundDetail.index //
                        , mStayOutboundDetail.name, mSelectedRoom.roomName, mSelectedRoom.nightly //
                        , mStayOutboundDetail.activeReward && mSelectedRoom.provideRewardSticker //
                        , checkInDate, nights);
                }
                break;

            case STATUS_ROOM_LIST:
                screenLock(false);

                Observable<Boolean> observable = getViewInterface().showRoomList(true);

                if (observable != null)
                {
                    addCompositeDisposable(observable.subscribe(new Consumer<Boolean>()
                    {
                        @Override
                        public void accept(@io.reactivex.annotations.NonNull Boolean aBoolean) throws Exception
                        {
                            unLockAll();

                            setStatus(STATUS_BOOKING);
                        }
                    }));
                }

                mAnalytics.onScreenRoomList(getActivity(), mStayOutboundDetail.index, mStayOutboundDetail.provideRewardSticker);
                break;

            default:
                break;
        }
    }

    @Override
    public void onAmenityMoreClick()
    {
        if (mStayOutboundDetail == null || mStayOutboundDetail.getAmenityList() == null//
            || mStayOutboundDetail.getAmenityList().size() == 0 || lock() == true)
        {
            return;
        }

        SparseArray<String> amenitySparseArray = mStayOutboundDetail.getAmenityList();
        int size = amenitySparseArray.size();
        ArrayList<String> amenityList = new ArrayList<>(size);

        for (int i = 0; i < size; i++)
        {
            String amenity = amenitySparseArray.get(amenitySparseArray.keyAt(i));

            if (DailyTextUtils.isTextEmpty(amenity) == false)
            {
                amenityList.add(amenitySparseArray.get(amenitySparseArray.keyAt(i)));
            }
        }

        startActivityForResult(AmenityListActivity.newInstance(getActivity(), amenityList), StayOutboundDetailActivity.REQUEST_CODE_AMENITY);
    }

    @Override
    public void onPriceTypeClick(PriceType priceType)
    {
        getViewInterface().setPriceType(priceType);
    }

    @Override
    public void onConciergeFaqClick()
    {
        startActivity(FAQActivity.newInstance(getActivity()));
    }

    @Override
    public void onConciergeHappyTalkClick()
    {
        if (mStayOutboundDetail == null)
        {
            return;
        }

        try
        {
            // 카카오톡 패키지 설치 여부
            getActivity().getPackageManager().getPackageInfo("com.kakao.talk", PackageManager.GET_META_DATA);

            startActivityForResult(HappyTalkCategoryDialog.newInstance(getActivity(), HappyTalkCategoryDialog.CallScreen.SCREEN_STAY_OUTBOUND_DETAIL//
                , mStayOutboundDetail.index, 0, mStayOutboundDetail.name), StayOutboundDetailActivity.REQUEST_CODE_HAPPYTALK);
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
    }

    @Override
    public void onConciergeCallClick()
    {
        startActivityForResult(CallDialogActivity.newInstance(getActivity()), StayOutboundDetailActivity.REQUEST_CODE_CALL);
    }

    @Override
    public void onRoomClick(StayOutboundRoom stayOutboundRoom)
    {
        mSelectedRoom = stayOutboundRoom;
    }

    @Override
    public void onRecommendAroundItemClick(View view, android.support.v4.util.Pair[] pairs)
    {
        if (lock() == true)
        {
            return;
        }

        if (view == null)
        {
            return;
        }

        CarouselListItem item = (CarouselListItem) view.getTag();
        if (item == null)
        {
            return;
        }

        StayOutbound stayOutbound = item.getItem();
        if (stayOutbound == null)
        {
            return;
        }

        startStayOutboundDetail(view, stayOutbound, pairs);

        mAnalytics.onEventRecommendItemClick(getActivity(), mStayIndex, stayOutbound.index);
    }

    @Override
    public void onRecommendAroundItemLongClick(View view, android.support.v4.util.Pair[] pairs)
    {
        if (lock() == true)
        {
            return;
        }

        if (view == null || getViewInterface() == null || mCommonDateTime == null)
        {
            return;
        }

        CarouselListItem item = (CarouselListItem) view.getTag();
        if (item == null)
        {
            return;
        }

        StayOutbound stayOutbound = item.getItem();
        if (stayOutbound == null)
        {
            return;
        }

        try
        {
            mViewByLongPress = view;
            mStayOutboundByLongPress = stayOutbound;
            mPairsByLongPress = pairs;

            getViewInterface().setBlurVisible(getActivity(), true);

            startActivityForResult(StayOutboundPreviewActivity.newInstance(getActivity(), stayOutbound.index, -1//
                , stayOutbound.name//
                , mStayBookDateTime.getCheckInDateTime(DailyCalendar.ISO_8601_FORMAT)//
                , mStayBookDateTime.getCheckOutDateTime(DailyCalendar.ISO_8601_FORMAT)//
                , mPeople.numberOfAdults, mPeople.getChildAgeList())//
                , StayOutboundDetailActivity.REQUEST_CODE_PREVIEW);
        } catch (Exception e)
        {
            unLockAll();
        }
    }

    @Override
    public void onLoginClick()
    {
        if (lock() == true)
        {
            return;
        }

        Intent intent = LoginActivity.newInstance(getActivity(), AnalyticsManager.Screen.DAILYHOTEL_DETAIL);
        startActivityForResult(intent, StayOutboundDetailActivity.REQUEST_CODE_LOGIN);
    }

    @Override
    public void onRewardClick()
    {
        if (lock() == true)
        {
            return;
        }

        Intent intent = RewardActivity.newInstance(getActivity());
        startActivityForResult(intent, StayOutboundDetailActivity.REQUEST_CODE_REWARD);
    }

    @Override
    public void onRewardGuideClick()
    {
        if (lock() == true)
        {
            return;
        }

        startActivityForResult(DailyWebActivity.newInstance(getActivity(), getString(R.string.label_daily_reward)//
            , DailyRemoteConfigPreference.getInstance(getActivity()).getKeyRemoteConfigStaticUrlDailyReward()), StayOutboundDetailActivity.REQUEST_CODE_WEB);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    void startStayOutboundDetail(View view, StayOutbound stayOutbound, android.support.v4.util.Pair[] pairs)
    {
        String imageUrl;
        if (ScreenUtils.getScreenWidth(getActivity()) >= ScreenUtils.DEFAULT_STAYOUTBOUND_XXHDPI_WIDTH)
        {
            if (DailyTextUtils.isTextEmpty(stayOutbound.getImageMap().bigUrl) == false)
            {
                imageUrl = stayOutbound.getImageMap().bigUrl;
            } else
            {
                imageUrl = stayOutbound.getImageMap().smallUrl;
            }
        } else
        {
            if (DailyTextUtils.isTextEmpty(stayOutbound.getImageMap().mediumUrl) == false)
            {
                imageUrl = stayOutbound.getImageMap().mediumUrl;
            } else
            {
                imageUrl = stayOutbound.getImageMap().smallUrl;
            }
        }

        try
        {
            StayOutboundDetailAnalyticsParam analyticsParam = mAnalytics.getAnalyticsParam( //
                stayOutbound, getString(R.string.label_stay_outbound_filter_x_star_rate, (int) stayOutbound.rating));

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

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), pairs);
                //
                //                View simpleDraweeView = view.findViewById(R.id.contentImageView);
                //                View gradientTopView = view.findViewById(R.id.gradientTopView);
                //                View gradientBottomView = view.findViewById(R.id.gradientBottomView);
                //
                //                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity()//
                //                    , android.support.v4.util.Pair.create(simpleDraweeView, getString(R.string.transition_place_image)) //
                //                    , android.support.v4.util.Pair.create(gradientTopView, getString(R.string.transition_gradient_top_view)) //
                //                    , android.support.v4.util.Pair.create(gradientBottomView, getString(R.string.transition_gradient_bottom_view)));

                startActivityForResult(StayOutboundDetailActivity.newInstance(getActivity(), stayOutbound.index//
                    , stayOutbound.name, stayOutbound.nameEng, imageUrl, stayOutbound.total//
                    , mStayBookDateTime.getCheckInDateTime(DailyCalendar.ISO_8601_FORMAT)//
                    , mStayBookDateTime.getCheckOutDateTime(DailyCalendar.ISO_8601_FORMAT)//
                    , mPeople.numberOfAdults, mPeople.getChildAgeList(), false, StayOutboundDetailActivity.TRANS_GRADIENT_BOTTOM_TYPE_NONE, analyticsParam)//
                    , StayOutboundDetailActivity.REQUEST_CODE_DETAIL, options.toBundle());
            } else
            {
                startActivityForResult(StayOutboundDetailActivity.newInstance(getActivity(), stayOutbound.index//
                    , stayOutbound.name, stayOutbound.nameEng, imageUrl, stayOutbound.total//
                    , mStayBookDateTime.getCheckInDateTime(DailyCalendar.ISO_8601_FORMAT)//
                    , mStayBookDateTime.getCheckOutDateTime(DailyCalendar.ISO_8601_FORMAT)//
                    , mPeople.numberOfAdults, mPeople.getChildAgeList(), false, StayOutboundDetailActivity.TRANS_GRADIENT_BOTTOM_TYPE_NONE, analyticsParam)//
                    , StayOutboundDetailActivity.REQUEST_CODE_DETAIL);

                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
            }
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    void setStatus(int status)
    {
        mStatus = status;

        getViewInterface().setBottomButtonLayout(status);
    }

    void setCommonDateTime(@NonNull CommonDateTime commonDateTime)
    {
        if (commonDateTime == null)
        {
            return;
        }

        mCommonDateTime = commonDateTime;
    }

    private void setPeople(int numberOfAdults, ArrayList<Integer> childAgeList)
    {
        if (mPeople == null)
        {
            mPeople = new People(People.DEFAULT_ADULTS, null);
        }

        mPeople.numberOfAdults = numberOfAdults;
        mPeople.setChildAgeList(childAgeList);
    }

    void onStayOutboundDetail(StayOutboundDetail stayOutboundDetail)
    {
        if (stayOutboundDetail == null)
        {
            return;
        }

        mStayOutboundDetail = stayOutboundDetail;

        // 리스트에서 이미지가 큰사이즈가 없는 경우 상세에서도 해당 사이즈가 없기 때문에 고려해준다.
        try
        {
            DetailImageInformation detailImageInformation = stayOutboundDetail.getImageList().get(0);
            ImageMap imageMap = detailImageInformation.getImageMap();

            if (mImageUrl.equalsIgnoreCase(imageMap.smallUrl) == true)
            {
                imageMap.bigUrl = null;
                imageMap.mediumUrl = null;
            }

            // 땡큐 페이지에서 이미지를 못읽는 경우가 생겨서 작은 이미지로 수정
            mImageUrl = imageMap.smallUrl;
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }

        if (mIsDeepLink == true)
        {
            getViewInterface().setToolbarTitle(stayOutboundDetail.name);
        }

        getViewInterface().setStayDetail(mStayBookDateTime, mPeople, stayOutboundDetail);

        // 리스트 가격 변동은 진입시 한번 만 한다.
        checkChangedPrice(mIsDeepLink, stayOutboundDetail, mListTotalPrice, mCheckChangedPrice == false);
        mCheckChangedPrice = true;

        // 선택된 방이 없으면 처음 방으로 한다.
        if (mStayOutboundDetail.getRoomList() == null || mStayOutboundDetail.getRoomList().size() == 0)
        {
            setStatus(STATUS_SOLD_OUT);
        } else
        {
            if (mSelectedRoom == null)
            {
                onRoomClick(stayOutboundDetail.getRoomList().get(0));
            }

            setStatus(STATUS_ROOM_LIST);
        }

        mIsDeepLink = false;

        try
        {
            addCompositeDisposable(mRecentlyLocalImpl.addRecentlyItem( //
                Constants.ServiceType.OB_STAY, stayOutboundDetail.index, stayOutboundDetail.name, null, mImageUrl, false) //
                .observeOn(Schedulers.io()).subscribe());
        } catch (Exception e)
        {
            ExLog.d(e.getMessage());
        }
    }

    /**
     * @param checkInDateTime  ISO-8601
     * @param checkOutDateTime ISO-8601
     */
    private void setStayBookDateTime(String checkInDateTime, String checkOutDateTime)
    {
        if (DailyTextUtils.isTextEmpty(checkInDateTime, checkOutDateTime) == true)
        {
            return;
        }

        if (mStayBookDateTime == null)
        {
            mStayBookDateTime = new StayBookDateTime();
        }

        try
        {
            mStayBookDateTime.setCheckInDateTime(checkInDateTime);
            mStayBookDateTime.setCheckOutDateTime(checkOutDateTime);
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    void setStayBookDateTime(String checkInDateTime, int checkInPlusDay, int nights)
    {
        if (DailyTextUtils.isTextEmpty(checkInDateTime) == true)
        {
            return;
        }

        if (mStayBookDateTime == null)
        {
            mStayBookDateTime = new StayBookDateTime();
        }

        try
        {
            mStayBookDateTime.setCheckInDateTime(checkInDateTime, checkInPlusDay);
            mStayBookDateTime.setCheckOutDateTime(mStayBookDateTime.getCheckInDateTime(DailyCalendar.ISO_8601_FORMAT), nights);
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    void notifyWishChanged()
    {
        if (mStayOutboundDetail == null)
        {
            return;
        }

        getViewInterface().setWishCount(mStayOutboundDetail.wishCount);
        getViewInterface().setWishSelected(mStayOutboundDetail.myWish);
    }

    void notifyWishChanged(int wishCount, boolean myWish)
    {
        if (mStayOutboundDetail == null)
        {
            return;
        }

        getViewInterface().setWishCount(wishCount);
        getViewInterface().setWishSelected(myWish);
    }

    void showWishTooltip()
    {
        getViewInterface().showWishTooltip();

        addCompositeDisposable(Observable.timer(3, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())//
            .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Long>()
            {
                @Override
                public void accept(Long aLong) throws Exception
                {
                    DailyPreference.getInstance(getActivity()).setWishTooltip(false);
                    getViewInterface().hideWishTooltip();
                }
            }));
    }

    void setRecommendAroundList(StayOutbounds stayOutbounds)
    {
        if (stayOutbounds == null)
        {
            return;
        }

        List<StayOutbound> stayOutboundList = stayOutbounds.getStayOutbound();
        if (stayOutboundList == null || stayOutboundList.size() == 0)
        {
            mRecommendAroundList = null;
            return;
        }

        if (mRecommendAroundList == null)
        {
            mRecommendAroundList = new ArrayList<>();
        }

        mRecommendAroundList.clear();

        for (StayOutbound stayOutbound : stayOutboundList)
        {
            CarouselListItem item = new CarouselListItem(CarouselListItem.TYPE_OB_STAY, stayOutbound);
            mRecommendAroundList.add(item);
        }
    }

    void notifyRecommendAroundList()
    {
        if (getViewInterface() == null)
        {
            return;
        }

        boolean hasRecommendAroundList = mRecommendAroundList == null || mRecommendAroundList.size() == 0 ? false : true;

        getViewInterface().setRecommendAroundVisible(hasRecommendAroundList);

        if (hasRecommendAroundList == true)
        {
            getViewInterface().setRecommendAroundList(mRecommendAroundList, mStayBookDateTime);
        }
    }

    void notifyRewardChanged()
    {
        if (mStayOutboundDetail == null)
        {
            return;
        }

        boolean hasRecommendAroundList = mRecommendAroundList == null || mRecommendAroundList.size() == 0 ? false : true;

        if (mStayOutboundDetail.activeReward == true && mStayOutboundDetail.provideRewardSticker == true)
        {
            getViewInterface().setRewardVisible(true, hasRecommendAroundList);

            if (DailyHotel.isLogin() == false)
            {
                boolean campaignEnabled = DailyRemoteConfigPreference.getInstance(getActivity()).isKeyRemoteConfigRewardStickerCampaignEnabled();

                getViewInterface().setRewardNonMember(DailyRemoteConfigPreference.getInstance(getActivity()).getKeyRemoteConfigRewardStickerCardTitleMessage()//
                    , getString(R.string.label_reward_login)//
                    , campaignEnabled ? DailyRemoteConfigPreference.getInstance(getActivity()).getKeyRemoteConfigRewardStickerNonMemberCampaignFreeNights() : 0//
                    , campaignEnabled ? DailyRemoteConfigPreference.getInstance(getActivity()).getKeyRemoteConfigRewardStickerNonMemberCampaignMessage()//
                        : DailyRemoteConfigPreference.getInstance(getActivity()).getKeyRemoteConfigRewardStickerNonMemberDefaultMessage());

                if (campaignEnabled == true)
                {
                    getViewInterface().startCampaignStickerAnimation();
                }
            } else
            {
                getViewInterface().setRewardMember(DailyRemoteConfigPreference.getInstance(getActivity()).getKeyRemoteConfigRewardStickerCardTitleMessage()//
                    , getString(R.string.label_reward_go_reward), mStayOutboundDetail.rewardStickerCount//
                    , DailyRemoteConfigPreference.getInstance(getActivity()).getKeyRemoteConfigRewardStickerMemberMessage(mStayOutboundDetail.rewardStickerCount));

                getViewInterface().stopCampaignStickerAnimation();
            }
        } else
        {
            getViewInterface().setRewardVisible(false, hasRecommendAroundList);
        }
    }

    private void checkChangedPrice(boolean isDeepLink, StayOutboundDetail stayOutboundDetail, int listViewPrice, boolean compareListPrice)
    {
        if (stayOutboundDetail == null)
        {
            return;
        }

        // 판매 완료 혹은 가격이 변동되었는지 조사한다
        List<StayOutboundRoom> roomList = stayOutboundDetail.getRoomList();

        if (roomList == null || roomList.size() == 0)
        {
            setResult(BaseActivity.RESULT_CODE_REFRESH);

            getViewInterface().showSimpleDialog(getString(R.string.dialog_notice2), getString(R.string.message_stay_outbound_detail_sold_out)//
                , getString(R.string.dialog_btn_text_confirm), null);
        } else
        {
            if (isDeepLink == false && compareListPrice == true)
            {
                boolean hasPrice = false;

                if (listViewPrice == StayOutboundDetailActivity.NONE_PRICE)
                {
                    hasPrice = true;
                } else
                {
                    for (StayOutboundRoom room : roomList)
                    {
                        if (listViewPrice == room.total)
                        {
                            hasPrice = true;
                            break;
                        }
                    }
                }

                if (hasPrice == false)
                {
                    setResult(BaseActivity.RESULT_CODE_REFRESH);

                    getViewInterface().showSimpleDialog(getString(R.string.dialog_notice2), getString(R.string.message_stay_outbound_detail_changed_price)//
                        , getString(R.string.dialog_btn_text_confirm), null);
                }
            }
        }
    }

    private void onRefresh(Observable<Boolean> observable, Disposable disposable)
    {
        if (observable == null)
        {
            return;
        }

        addCompositeDisposable(Observable.zip(observable, mCommonRemoteImpl.getCommonDateTime() //
            , mStayOutboundRemoteImpl.getDetail(mStayIndex, mStayBookDateTime, mPeople) //
            , mStayOutboundRemoteImpl.getRecommendAroundList(mStayIndex, mStayBookDateTime, mPeople) //
            , new Function4<Boolean, CommonDateTime, StayOutboundDetail, StayOutbounds, StayOutboundDetail>()
            {
                @Override
                public StayOutboundDetail apply(@io.reactivex.annotations.NonNull Boolean aBoolean//
                    , @io.reactivex.annotations.NonNull CommonDateTime commonDateTime //
                    , @io.reactivex.annotations.NonNull StayOutboundDetail stayOutboundDetail //
                    , @io.reactivex.annotations.NonNull StayOutbounds stayOutbounds) throws Exception
                {
                    setCommonDateTime(commonDateTime);
                    setRecommendAroundList(stayOutbounds);
                    return stayOutboundDetail;
                }
            }).subscribe(new Consumer<StayOutboundDetail>()
        {
            @Override
            public void accept(@io.reactivex.annotations.NonNull StayOutboundDetail stayOutboundDetail) throws Exception
            {
                // 순서 변경 금지... 추천 영역과 상세 데이터 넣는 부분 변경시 Oreo에서 스크롤이 하단가는 이슈 발생
                notifyRecommendAroundList();
                onStayOutboundDetail(stayOutboundDetail);
                notifyWishChanged();
                notifyRewardChanged();

                try
                {
                    mAnalytics.onScreen(getActivity(), mStayBookDateTime, mStayOutboundDetail, mListTotalPrice);

                    boolean hasRecommendList = mRecommendAroundList == null || mRecommendAroundList.size() == 0 ? false : true;

                    mAnalytics.onEventHasRecommendList(getActivity(), hasRecommendList);

                    if (DailyPreference.getInstance(getActivity()).isWishTooltip() == true)
                    {
                        showWishTooltip();
                    }

                    if (hasRecommendList == true)
                    {
                        List<Integer> recommendIndexList = new ArrayList<>();
                        for (CarouselListItem carouselListItem : mRecommendAroundList)
                        {
                            StayOutbound stayOutbound = carouselListItem.getItem();
                            if (stayOutbound != null)
                            {
                                recommendIndexList.add(stayOutbound.index);
                            }
                        }

                        mAnalytics.onEventRecommendItemList(getActivity(), stayOutboundDetail.index, recommendIndexList);
                    }
                } catch (Exception e)
                {
                    ExLog.d(e.getMessage());
                }

                if (disposable != null)
                {
                    disposable.dispose();
                }

                unLockAll();
            }
        }, new Consumer<Throwable>()
        {
            @Override
            public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception
            {
                if (disposable != null)
                {
                    disposable.dispose();
                }

                onHandleError(throwable);
            }
        }));
    }
}
