package com.daily.dailyhotel.screen.home.stay.inbound.detail;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.CompoundButton;

import com.daily.base.BaseActivity;
import com.daily.base.BaseAnalyticsInterface;
import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ExLog;
import com.daily.base.widget.DailyToast;
import com.daily.dailyhotel.base.BaseExceptionPresenter;
import com.daily.dailyhotel.entity.CommonDateTime;
import com.daily.dailyhotel.entity.DetailImageInformation;
import com.daily.dailyhotel.entity.ReviewScores;
import com.daily.dailyhotel.entity.StayBookDateTime;
import com.daily.dailyhotel.entity.StayDetail;
import com.daily.dailyhotel.entity.StayRoom;
import com.daily.dailyhotel.entity.TrueVR;
import com.daily.dailyhotel.entity.User;
import com.daily.dailyhotel.entity.WishResult;
import com.daily.dailyhotel.parcel.analytics.ImageListAnalyticsParam;
import com.daily.dailyhotel.parcel.analytics.NavigatorAnalyticsParam;
import com.daily.dailyhotel.parcel.analytics.StayDetailAnalyticsParam;
import com.daily.dailyhotel.parcel.analytics.StayPaymentAnalyticsParam;
import com.daily.dailyhotel.parcel.analytics.TrueReviewAnalyticsParam;
import com.daily.dailyhotel.repository.local.RecentlyLocalImpl;
import com.daily.dailyhotel.repository.remote.CalendarImpl;
import com.daily.dailyhotel.repository.remote.CommonRemoteImpl;
import com.daily.dailyhotel.repository.remote.ProfileRemoteImpl;
import com.daily.dailyhotel.repository.remote.StayRemoteImpl;
import com.daily.dailyhotel.screen.common.dialog.call.CallDialogActivity;
import com.daily.dailyhotel.screen.common.dialog.navigator.NavigatorDialogActivity;
import com.daily.dailyhotel.screen.common.images.ImageListActivity;
import com.daily.dailyhotel.screen.home.stay.inbound.detail.truereview.StayTrueReviewActivity;
import com.daily.dailyhotel.screen.home.stay.inbound.payment.StayPaymentActivity;
import com.daily.dailyhotel.screen.mydaily.reward.RewardActivity;
import com.daily.dailyhotel.storage.preference.DailyPreference;
import com.daily.dailyhotel.storage.preference.DailyRemoteConfigPreference;
import com.daily.dailyhotel.storage.preference.DailyUserPreference;
import com.twoheart.dailyhotel.DailyHotel;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Customer;
import com.twoheart.dailyhotel.screen.common.HappyTalkCategoryDialog;
import com.twoheart.dailyhotel.screen.common.TrueVRActivity;
import com.twoheart.dailyhotel.screen.common.ZoomMapActivity;
import com.twoheart.dailyhotel.screen.event.EventWebActivity;
import com.twoheart.dailyhotel.screen.hotel.filter.StayDetailCalendarActivity;
import com.twoheart.dailyhotel.screen.information.FAQActivity;
import com.twoheart.dailyhotel.screen.mydaily.coupon.SelectStayCouponDialogActivity;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function7;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by sheldon
 * Clean Architecture
 */
public class StayDetailPresenter extends BaseExceptionPresenter<StayDetailActivity, StayDetailViewInterface> implements StayDetailView.OnEventListener
{
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

    StayDetailAnalyticsInterface mAnalytics;

    private StayRemoteImpl mStayRemoteImpl;
    private CommonRemoteImpl mCommonRemoteImpl;
    private ProfileRemoteImpl mProfileRemoteImpl;
    private CalendarImpl mCalendarImpl;
    private RecentlyLocalImpl mRecentlyLocalImpl;

    int mStayIndex, mPriceFromList;
    private String mStayName;
    String mImageUrl;
    StayBookDateTime mStayBookDateTime;
    private CommonDateTime mCommonDateTime;
    StayDetail mStayDetail;
    StayRoom mSelectedRoom;
    private ReviewScores mReviewScores;
    private List<TrueVR> mTrueVRList;

    private int mStatus = STATUS_NONE;

    private boolean mIsUsedMultiTransition;
    private boolean mIsDeepLink;
    private boolean mCheckChangedPrice;
    private int mGradientType;
    private List<Integer> mSoldOutDateList;
    boolean mShowCalendar;
    boolean mShowTrueVR;

    DailyDeepLink mDailyDeepLink;
    private AppResearch mAppResearch;

    public interface StayDetailAnalyticsInterface extends BaseAnalyticsInterface
    {
        void setAnalyticsParam(StayDetailAnalyticsParam analyticsParam);

        StayPaymentAnalyticsParam getStayPaymentAnalyticsParam(StayDetail stayDetail, StayRoom stayRoom);

        void onScreen(Activity activity, StayBookDateTime stayBookDateTime, StayDetail stayDetail, int priceFromList);

        void onScreenRoomList(Activity activity, StayBookDateTime stayBookDateTime, StayDetail stayDetail, int priceFromList);

        void onRoomListClick(Activity activity, String stayName);

        void onEventShareKakaoClick(Activity activity, boolean login, String userType, boolean benefitAlarm//
            , int stayIndex, String stayName, boolean overseas);

        void onEventShareSmsClick(Activity activity, boolean login, String userType, boolean benefitAlarm//
            , int stayIndex, String stayName, boolean overseas);

        void onEventDownloadCoupon(Activity activity, String stayName);

        void onEventDownloadCouponByLogin(Activity activity, boolean login);

        void onEventShare(Activity activity);

        void onEventChangedPrice(Activity activity, boolean deepLink, String stayName, boolean soldOut);

        void onEventCalendarClick(Activity activity);

        void onEventBookingClick(Activity activity, StayBookDateTime stayBookDateTime//
            , int stayIndex, String stayName, String roomName, String category, int discountPrice);

        void onEventTrueReviewClick(Activity activity);

        void onEventTrueVRClick(Activity activity, int stayIndex);

        void onEventImageClick(Activity activity, String stayName);

        void onEventConciergeClick(Activity activity);

        void onEventMapClick(Activity activity, String stayName);

        void onEventClipAddressClick(Activity activity, String stayName);

        void onEventWishClick(Activity activity, StayBookDateTime stayBookDateTime, StayDetail stayDetail, int priceFromList, boolean myWish);

        void onEventCallClick(Activity activity);

        void onEventFaqClick(Activity activity);

        void onEventHappyTalkClick(Activity activity);

        void onEventStampClick(Activity activity);
    }

    public StayDetailPresenter(@NonNull StayDetailActivity activity)
    {
        super(activity);
    }

    @NonNull
    @Override
    protected StayDetailViewInterface createInstanceViewInterface()
    {
        return new StayDetailView(getActivity(), this);
    }

    @Override
    public void constructorInitialize(StayDetailActivity activity)
    {
        setContentView(R.layout.activity_stay_detail_data);

        mAppResearch = new AppResearch(activity);
        setAnalytics(new StayDetailAnalyticsImpl());

        mStayRemoteImpl = new StayRemoteImpl(activity);
        mCommonRemoteImpl = new CommonRemoteImpl(activity);
        mProfileRemoteImpl = new ProfileRemoteImpl(activity);
        mCalendarImpl = new CalendarImpl(activity);
        mRecentlyLocalImpl = new RecentlyLocalImpl(activity);

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
        mAnalytics = (StayDetailAnalyticsInterface) analytics;
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
            mAnalytics.setAnalyticsParam(new StayDetailAnalyticsParam());

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
            mIsUsedMultiTransition = intent.getBooleanExtra(StayDetailActivity.INTENT_EXTRA_DATA_MULTITRANSITION, false);
            mGradientType = intent.getIntExtra(StayDetailActivity.INTENT_EXTRA_DATA_CALL_GRADIENT_TYPE, StayDetailActivity.TRANS_GRADIENT_BOTTOM_TYPE_NONE);
            mIsDeepLink = false;

            mStayIndex = intent.getIntExtra(StayDetailActivity.INTENT_EXTRA_DATA_STAY_INDEX, -1);

            if (mStayIndex == -1)
            {
                return false;
            }

            mStayName = intent.getStringExtra(StayDetailActivity.INTENT_EXTRA_DATA_STAY_NAME);
            mImageUrl = intent.getStringExtra(StayDetailActivity.INTENT_EXTRA_DATA_IMAGE_URL);
            mPriceFromList = intent.getIntExtra(StayDetailActivity.INTENT_EXTRA_DATA_LIST_PRICE, StayDetailActivity.NONE_PRICE);

            String checkInDateTime = intent.getStringExtra(StayDetailActivity.INTENT_EXTRA_DATA_CHECK_IN);
            String checkOutDateTime = intent.getStringExtra(StayDetailActivity.INTENT_EXTRA_DATA_CHECK_OUT);

            setStayBookDateTime(checkInDateTime, checkOutDateTime);

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
            getViewInterface().setInitializedTransLayout(mStayName, mImageUrl);
        } else
        {
            getViewInterface().setSharedElementTransitionEnabled(false, StayDetailActivity.TRANS_GRADIENT_BOTTOM_TYPE_NONE);
            getViewInterface().setInitializedImage(mImageUrl);
        }

        addCompositeDisposable(mRecentlyLocalImpl.addRecentlyItem( //
            Constants.ServiceType.HOTEL, mStayIndex, mStayName, null, mImageUrl, true) //
            .observeOn(Schedulers.io()).subscribe());

        if (mIsUsedMultiTransition == true)
        {
            setRefresh(false);
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

        if (isRefresh() == true)
        {
            onRefresh(true);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        onHideRoomListClick(false);

        if (isRefresh() == true)
        {
            onRefresh(true);
        }

        mAppResearch.onResume("스테이", mStayIndex);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        mAppResearch.onPause("스테이", mStayIndex);
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
                    lock();

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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        unLockAll();

        switch (requestCode)
        {
            case StayDetailActivity.REQUEST_CODE_CALENDAR:
            {
                if (resultCode == Activity.RESULT_OK && data != null)
                {
                    if (data.hasExtra(Constants.NAME_INTENT_EXTRA_DATA_CHECK_IN_DATE) == true//
                        && data.hasExtra(Constants.NAME_INTENT_EXTRA_DATA_CHECK_OUT_DATE) == true)
                    {
                        String checkInDateTime = data.getStringExtra(Constants.NAME_INTENT_EXTRA_DATA_CHECK_IN_DATE);
                        String checkOutDateTime = data.getStringExtra(Constants.NAME_INTENT_EXTRA_DATA_CHECK_OUT_DATE);

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

            case StayDetailActivity.REQUEST_CODE_HAPPYTALK:
                break;

            case StayDetailActivity.REQUEST_CODE_CALL:
                break;

            case StayDetailActivity.REQUEST_CODE_PAYMENT:
                if (resultCode == BaseActivity.RESULT_CODE_REFRESH)
                {
                    setRefresh(true);
                }
                break;

            case StayDetailActivity.REQUEST_CODE_PROFILE_UPDATE:
            case StayDetailActivity.REQUEST_CODE_LOGIN:
            case StayDetailActivity.REQUEST_CODE_LOGIN_IN_BY_BOOKING:
                if (resultCode == Activity.RESULT_OK)
                {
                    onActionButtonClick();
                } else
                {
                    onHideRoomListClick(false);
                }

                setResult(BaseActivity.RESULT_CODE_REFRESH);
                break;

            case StayDetailActivity.REQUEST_CODE_DOWNLOAD_COUPON:
                break;

            case StayDetailActivity.REQUEST_CODE_LOGIN_IN_BY_WISH:
                if (resultCode == Activity.RESULT_OK)
                {
                    onWishClick();

                    setResult(BaseActivity.RESULT_CODE_REFRESH);
                }
                break;

            case StayDetailActivity.REQUEST_CODE_LOGIN_IN_BY_COUPON:
                if (resultCode == Activity.RESULT_OK)
                {
                    onDownloadCouponClick();

                    setResult(BaseActivity.RESULT_CODE_REFRESH);
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

        setRefresh(false);
        screenLock(showProgress);

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
    protected void setResult(int resultCode)
    {
        if (getResultCode() == BaseActivity.RESULT_CODE_REFRESH)
        {
            super.setResult(BaseActivity.RESULT_CODE_REFRESH);
        } else
        {
            super.setResult(resultCode);
        }
    }

    @Override
    protected void setResult(int resultCode, Intent resultData)
    {
        if (getResultCode() == BaseActivity.RESULT_CODE_REFRESH)
        {
            super.setResult(BaseActivity.RESULT_CODE_REFRESH);
        } else
        {
            super.setResult(resultCode, resultData);
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

        mAnalytics.onEventShare(getActivity());
    }

    @Override
    public void onWishClick()
    {
        if (mStayDetail == null || lock() == true)
        {
            return;
        }

        if (DailyHotel.isLogin() == false)
        {
            DailyToast.showToast(getActivity(), R.string.toast_msg_please_login, DailyToast.LENGTH_LONG);

            Intent intent = LoginActivity.newInstance(getActivity(), AnalyticsManager.Screen.DAILYHOTEL_DETAIL);
            startActivityForResult(intent, StayDetailActivity.REQUEST_CODE_LOGIN_IN_BY_WISH);
        } else
        {
            boolean wish = !mStayDetail.myWish;
            int wishCount = wish ? mStayDetail.wishCount + 1 : mStayDetail.wishCount - 1;

            notifyWishChanged(wishCount, wish);

            if (wish == true)
            {
                addCompositeDisposable(mStayRemoteImpl.addWish(mStayDetail.index)//
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<WishResult>()
                    {
                        @Override
                        public void accept(@io.reactivex.annotations.NonNull WishResult wishResult) throws Exception
                        {
                            Intent intent = new Intent();
                            intent.putExtra(StayDetailActivity.INTENT_EXTRA_DATA_WISH, true);
                            setResult(BaseActivity.RESULT_CODE_REFRESH, intent);

                            if (wishResult.success == true)
                            {
                                mStayDetail.myWish = true;
                                mStayDetail.wishCount++;

                                notifyWishChanged();

                                Observable<Boolean> observable = getViewInterface().showWishView(mStayDetail.myWish);

                                if (observable != null)
                                {
                                    addCompositeDisposable(observable.subscribeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
                                    {
                                        @Override
                                        public void accept(@io.reactivex.annotations.NonNull Boolean aBoolean) throws Exception
                                        {
                                            unLockAll();
                                        }
                                    }));
                                } else
                                {
                                    unLockAll();
                                }

                                mAnalytics.onEventWishClick(getActivity(), mStayBookDateTime, mStayDetail, mPriceFromList, true);
                            } else
                            {
                                notifyWishChanged(mStayDetail.wishCount, mStayDetail.myWish);

                                getViewInterface().showSimpleDialog(getString(R.string.dialog_notice2), wishResult.message//
                                    , getString(R.string.dialog_btn_text_confirm), null);

                                unLockAll();
                            }
                        }
                    }, new Consumer<Throwable>()
                    {
                        @Override
                        public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception
                        {
                            onHandleError(throwable);

                            notifyWishChanged(mStayDetail.wishCount, mStayDetail.myWish);
                        }
                    }));
            } else
            {
                addCompositeDisposable(mStayRemoteImpl.removeWish(mStayDetail.index)//
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<WishResult>()
                    {
                        @Override
                        public void accept(@io.reactivex.annotations.NonNull WishResult wishResult) throws Exception
                        {
                            Intent intent = new Intent();
                            intent.putExtra(StayDetailActivity.INTENT_EXTRA_DATA_WISH, false);
                            setResult(BaseActivity.RESULT_CODE_REFRESH, intent);

                            if (wishResult.success == true)
                            {
                                mStayDetail.myWish = false;
                                mStayDetail.wishCount--;

                                notifyWishChanged();

                                Observable<Boolean> observable = getViewInterface().showWishView(mStayDetail.myWish);

                                if (observable != null)
                                {
                                    addCompositeDisposable(observable.subscribeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
                                    {
                                        @Override
                                        public void accept(@io.reactivex.annotations.NonNull Boolean aBoolean) throws Exception
                                        {
                                            unLockAll();
                                        }
                                    }));
                                } else
                                {
                                    unLockAll();
                                }

                                mAnalytics.onEventWishClick(getActivity(), mStayBookDateTime, mStayDetail, mPriceFromList, false);
                            } else
                            {
                                notifyWishChanged(mStayDetail.wishCount, mStayDetail.myWish);

                                getViewInterface().showSimpleDialog(getString(R.string.dialog_notice2), wishResult.message//
                                    , getString(R.string.dialog_btn_text_confirm), null);

                                unLockAll();
                            }
                        }
                    }, new Consumer<Throwable>()
                    {
                        @Override
                        public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception
                        {
                            onHandleError(throwable);

                            notifyWishChanged(mStayDetail.wishCount, mStayDetail.myWish);
                        }
                    }));
            }
        }
    }

    @Override
    public void onShareKakaoClick()
    {
        if (mStayDetail == null || mStayBookDateTime == null)
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

            KakaoLinkManager.newInstance(getActivity()).shareStay(name//
                , mStayDetail.name//
                , mStayDetail.address//
                , mStayDetail.index//
                , mStayDetail.getImageInformationList().get(0).getImageMap().bigUrl //
                , mStayBookDateTime);

            mAnalytics.onEventShareKakaoClick(getActivity(), DailyHotel.isLogin()//
                , DailyUserPreference.getInstance(getActivity()).getType()//
                , DailyUserPreference.getInstance(getActivity()).isBenefitAlarm(), mStayDetail.index, mStayDetail.name, mStayDetail.overseas);
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

        unLockAll();
    }

    @Override
    public void onShareSmsClick()
    {
        if (mStayDetail == null || mStayBookDateTime == null)
        {
            return;
        }

        try
        {
            int nights = mStayBookDateTime.getNights();

            String longUrl = String.format(Locale.KOREA, "https://mobile.dailyhotel.co.kr/stay/%d?dateCheckIn=%s&stays=%d"//
                , mStayDetail.index, mStayBookDateTime.getCheckInDateTime("yyyy-MM-dd"), nights);

            String name = DailyUserPreference.getInstance(getActivity()).getName();

            if (DailyTextUtils.isTextEmpty(name) == true)
            {
                name = getString(R.string.label_friend) + "가";
            } else
            {
                name += "님이";
            }

            final String message = getString(R.string.message_detail_stay_share_sms//
                , name, mStayDetail.name//
                , mStayBookDateTime.getCheckInDateTime("yyyy.MM.dd(EEE)")//
                , mStayBookDateTime.getCheckOutDateTime("yyyy.MM.dd(EEE)")//
                , nights, nights + 1 //
                , mStayDetail.address);

            addCompositeDisposable(mCommonRemoteImpl.getShortUrl(longUrl).subscribe(new Consumer<String>()
            {
                @Override
                public void accept(@NonNull String shortUrl) throws Exception
                {
                    unLockAll();

                    Util.sendSms(getActivity(), message + shortUrl);
                }
            }, new Consumer<Throwable>()
            {
                @Override
                public void accept(@NonNull Throwable throwable) throws Exception
                {
                    unLockAll();

                    Util.sendSms(getActivity(), message + "https://mobile.dailyhotel.co.kr/stay/" + mStayDetail.index);
                }
            }));

            mAnalytics.onEventShareSmsClick(getActivity(), DailyHotel.isLogin()//
                , DailyUserPreference.getInstance(getActivity()).getType()//
                , DailyUserPreference.getInstance(getActivity()).isBenefitAlarm(), mStayDetail.index, mStayDetail.name, mStayDetail.overseas);
        } catch (Exception e)
        {
            unLockAll();

            ExLog.d(e.toString());
        }
    }

    @Override
    public void onImageClick(int position)
    {
        if (mStayDetail == null || mStayDetail.getImageInformationList() == null//
            || mStayDetail.getImageInformationList().size() == 0 || lock() == true)
        {
            return;
        }

        ImageListAnalyticsParam analyticsParam = new ImageListAnalyticsParam();
        analyticsParam.serviceType = Constants.ServiceType.HOTEL;

        startActivityForResult(ImageListActivity.newInstance(getActivity(), mStayDetail.name//
            , mStayDetail.getImageInformationList(), position, analyticsParam), StayDetailActivity.REQUEST_CODE_IMAGE_LIST);

        mAnalytics.onEventImageClick(getActivity(), mStayDetail.name);
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
            startCalendar(mCommonDateTime, mStayBookDateTime, mStayIndex, mSoldOutDateList, true);
        } catch (Exception e)
        {
            ExLog.e(e.toString());
            unLockAll();
        }
    }

    @Override
    public void onMapClick()
    {
        if (Util.isInstallGooglePlayService(getActivity()) == true)
        {
            if (mStayDetail == null || lock() == true)
            {
                return;
            }

            startActivityForResult(ZoomMapActivity.newInstance(getActivity()//
                , ZoomMapActivity.SourceType.HOTEL, mStayDetail.name, mStayDetail.address//
                , mStayDetail.latitude, mStayDetail.longitude, true), StayDetailActivity.REQUEST_CODE_MAP);
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

        if (mStayDetail != null)
        {
            mAnalytics.onEventMapClick(getActivity(), mStayDetail.name);
        }
    }

    @Override
    public void onClipAddressClick(String address)
    {
        DailyTextUtils.clipText(getActivity(), address);

        DailyToast.showToast(getActivity(), R.string.message_detail_copy_address, DailyToast.LENGTH_SHORT);

        if (mStayDetail != null)
        {
            mAnalytics.onEventClipAddressClick(getActivity(), mStayDetail.name);
        }
    }

    @Override
    public void onNavigatorClick()
    {
        if (mStayDetail == null || lock() == true)
        {
            return;
        }

        NavigatorAnalyticsParam analyticsParam = new NavigatorAnalyticsParam();
        analyticsParam.category = AnalyticsManager.Category.HOTEL_BOOKINGS;
        analyticsParam.action = AnalyticsManager.Action.HOTEL_DETAIL_NAVIGATION_APP_CLICKED;

        startActivityForResult(NavigatorDialogActivity.newInstance(getActivity(), mStayDetail.name//
            , mStayDetail.latitude, mStayDetail.longitude, false, analyticsParam), StayDetailActivity.REQUEST_CODE_NAVIGATOR);
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

        mAnalytics.onEventConciergeClick(getActivity());
    }

    @Override
    public void onHideRoomListClick(boolean animation)
    {
        if (mStatus != STATUS_BOOKING)
        {
            return;
        }

        Observable<Boolean> observable = getViewInterface().hideRoomList(animation);

        if (observable != null)
        {
            screenLock(false);

            addCompositeDisposable(observable.subscribe(new Consumer<Boolean>()
            {
                @Override
                public void accept(@io.reactivex.annotations.NonNull Boolean aBoolean) throws Exception
                {
                    unLockAll();

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

                onBookingRoom();
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

                mAnalytics.onScreenRoomList(getActivity(), mStayBookDateTime, mStayDetail, mPriceFromList);
                mAnalytics.onRoomListClick(getActivity(), mStayDetail.name);
                break;

            default:
                break;
        }
    }

    @Override
    public void onConciergeFaqClick()
    {
        startActivity(FAQActivity.newInstance(getActivity()));

        mAnalytics.onEventFaqClick(getActivity());
    }

    @Override
    public void onConciergeHappyTalkClick()
    {
        if (mStayDetail == null)
        {
            return;
        }

        try
        {
            // 카카오톡 패키지 설치 여부
            getActivity().getPackageManager().getPackageInfo("com.kakao.talk", PackageManager.GET_META_DATA);

            startActivityForResult(HappyTalkCategoryDialog.newInstance(getActivity(), HappyTalkCategoryDialog.CallScreen.SCREEN_STAY_DETAIL//
                , mStayDetail.index, 0, mStayDetail.name), StayDetailActivity.REQUEST_CODE_HAPPYTALK);
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
    public void onConciergeCallClick()
    {
        startActivityForResult(CallDialogActivity.newInstance(getActivity()), StayDetailActivity.REQUEST_CODE_CALL);

        mAnalytics.onEventCallClick(getActivity());
    }

    @Override
    public void onTrueReviewClick()
    {
        if (mStayDetail == null || mReviewScores == null || lock() == true)
        {
            return;
        }

        TrueReviewAnalyticsParam analyticsParam = new TrueReviewAnalyticsParam();
        analyticsParam.category = mStayDetail.category;

        startActivityForResult(StayTrueReviewActivity.newInstance(getActivity(), mStayDetail.index, mReviewScores, analyticsParam), StayDetailActivity.REQUEST_CODE_TRUE_VIEW);

        mAnalytics.onEventTrueReviewClick(getActivity());
    }

    @Override
    public void onTrueVRClick()
    {
        if (mStayDetail == null || mTrueVRList == null || mTrueVRList.size() == 0 || lock() == true)
        {
            return;
        }

        if (DailyPreference.getInstance(getActivity()).isTrueVRCheckDataGuide() == false)
        {
            getViewInterface().showTrueVRDialog(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean checked)
                {
                    DailyPreference.getInstance(getActivity()).setTrueVRCheckDataGuide(checked);
                }
            }, new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    startActivityForResult(TrueVRActivity.newInstance(getActivity(), mStayDetail.index, mTrueVRList//
                        , Constants.PlaceType.HOTEL, mStayDetail.category), StayDetailActivity.REQUEST_CODE_TRUE_VR);
                }
            }, new DialogInterface.OnDismissListener()
            {
                @Override
                public void onDismiss(DialogInterface dialog)
                {
                    unLockAll();
                }
            });
        } else
        {
            startActivityForResult(TrueVRActivity.newInstance(getActivity(), mStayDetail.index, (ArrayList) mTrueVRList//
                , Constants.PlaceType.HOTEL, mStayDetail.category), StayDetailActivity.REQUEST_CODE_TRUE_VR);
        }

        mAnalytics.onEventTrueVRClick(getActivity(), mStayDetail.index);
    }

    @Override
    public void onDownloadCouponClick()
    {
        if (mStayDetail == null || lock() == true)
        {
            return;
        }

        mAnalytics.onEventDownloadCoupon(getActivity(), mStayDetail.name);

        if (DailyHotel.isLogin() == false)
        {
            getViewInterface().showSimpleDialog(getString(R.string.dialog_notice2), getString(R.string.message_detail_please_login), //
                getString(R.string.dialog_btn_login_for_benefit), getString(R.string.dialog_btn_text_close), //
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = LoginActivity.newInstance(getActivity(), AnalyticsManager.Screen.DAILYGOURMET_DETAIL);
                        startActivityForResult(intent, StayDetailActivity.REQUEST_CODE_LOGIN_IN_BY_COUPON);

                        mAnalytics.onEventDownloadCouponByLogin(getActivity(), true);
                    }
                }, new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        mAnalytics.onEventDownloadCouponByLogin(getActivity(), false);
                    }
                }, new DialogInterface.OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                        mAnalytics.onEventDownloadCouponByLogin(getActivity(), false);
                    }
                }, new DialogInterface.OnDismissListener()
                {
                    @Override
                    public void onDismiss(DialogInterface dialog)
                    {
                        unLockAll();
                    }
                }, true);
        } else
        {
            Intent intent = SelectStayCouponDialogActivity.newInstance(getActivity(), mStayDetail.index //
                , mStayBookDateTime.getCheckInDateTime(DailyCalendar.ISO_8601_FORMAT)//
                , mStayBookDateTime.getCheckOutDateTime(DailyCalendar.ISO_8601_FORMAT), mStayDetail.category, mStayDetail.name);
            startActivityForResult(intent, StayDetailActivity.REQUEST_CODE_DOWNLOAD_COUPON);
        }
    }

    @Override
    public void onStampClick()
    {
        if (lock() == true)
        {
            return;
        }

        getViewInterface().showStampDialog(new DialogInterface.OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialog)
            {
                unLockAll();
            }
        });

        mAnalytics.onEventStampClick(getActivity());
    }

    @Override
    public void onHideWishTooltipClick()
    {
        DailyPreference.getInstance(getActivity()).setWishTooltip(false);
        getViewInterface().hideWishTooltip();
    }

    @Override
    public void onLoginClick()
    {
        if (lock() == true)
        {
            return;
        }

        Intent intent = LoginActivity.newInstance(getActivity(), AnalyticsManager.Screen.DAILYHOTEL_DETAIL);
        startActivityForResult(intent, StayDetailActivity.REQUEST_CODE_LOGIN);
    }

    @Override
    public void onRewardClick()
    {
        if (lock() == true)
        {
            return;
        }

        Intent intent = RewardActivity.newInstance(getActivity());
        startActivityForResult(intent, StayDetailActivity.REQUEST_CODE_REWARD);
    }

    @Override
    public void onRewardGuideClick()
    {

    }

    @Override
    public void onPriceTypeClick(PriceType priceType)
    {
        getViewInterface().setPriceType(priceType);
    }

    @Override
    public void onRoomClick(StayRoom stayRoom)
    {
        mSelectedRoom = stayRoom;
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

    void setReviewScores(ReviewScores reviewScores)
    {
        mReviewScores = reviewScores;
    }

    void setSoldOutDateList(List<String> soldOutList)
    {
        if (mSoldOutDateList == null)
        {
            mSoldOutDateList = new ArrayList<>();
        }

        mSoldOutDateList.clear();

        if (soldOutList != null && soldOutList.size() > 0)
        {
            for (String dayString : soldOutList)
            {
                int soldOutDay = Integer.parseInt(dayString.replaceAll("-", ""));
                mSoldOutDateList.add(soldOutDay);
            }
        }
    }

    void setGourmetDetail(StayDetail stayDetail)
    {
        mStayDetail = stayDetail;

        mAnalytics.onScreen(getActivity(), mStayBookDateTime, mStayDetail, mPriceFromList);
    }

    void setTrueVRList(List<TrueVR> trueVRList)
    {
        mTrueVRList = trueVRList;
    }

    void notifyDetailChanged()
    {
        if (mStayDetail == null)
        {
            return;
        }

        // 리스트에서 이미지가 큰사이즈가 없는 경우 상세에서도 해당 사이즈가 없기 때문에 고려해준다.
        try
        {
            if (mStayDetail.getImageInformationList() != null && mStayDetail.getImageInformationList().size() > 0)
            {
                mImageUrl = mStayDetail.getImageInformationList().get(0).getImageMap().bigUrl;
            }
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }

        addCompositeDisposable(mRecentlyLocalImpl.addRecentlyItem( //
            Constants.ServiceType.HOTEL, mStayDetail.index, mStayDetail.name, null, mImageUrl, false) //
            .observeOn(Schedulers.io()).subscribe());

        boolean showStamp = mStayDetail.overseas == false && DailyRemoteConfigPreference.getInstance(getActivity()).isRemoteConfigStampEnabled() == true;

        getViewInterface().setStayDetail(mStayBookDateTime, mStayDetail//
            , mReviewScores != null ? mReviewScores.reviewScoreTotalCount : 0, showStamp, DailyRemoteConfigPreference.getInstance(getActivity()).isKeyRemoteConfigRewardEnabled());

        // 리스트 가격 변동은 진입시 한번 만 한다.
        checkChangedPrice(mIsDeepLink, mStayDetail, mPriceFromList, mCheckChangedPrice == false);
        mCheckChangedPrice = true;

        // 선택된 방이 없으면 처음 방으로 한다.
        if (isSoldOut(mStayDetail) == true)
        {
            setStatus(STATUS_SOLD_OUT);
        } else
        {
            if (mSelectedRoom == null)
            {
                onRoomClick(mStayDetail.getRoomList().get(0));
            }

            setStatus(STATUS_ROOM_LIST);
        }

        if (DailyPreference.getInstance(getActivity()).getTrueVRSupport() > 0)
        {
            if (mTrueVRList != null && mTrueVRList.size() > 0)
            {
                getViewInterface().setTrueVRVisible(true);
            } else
            {
                getViewInterface().setTrueVRVisible(false);
            }
        } else
        {
            getViewInterface().setTrueVRVisible(false);
        }

        if (mShowCalendar == true)
        {
            mShowCalendar = false;

            if (mStayDetail.getRoomList() != null && mStayDetail.getRoomList().size() > 0)
            {
                onCalendarClick();
            }
        } else if (mShowTrueVR == true)
        {
            mShowTrueVR = false;

            if (DailyPreference.getInstance(getActivity()).getTrueVRSupport() > 0)
            {
                onTrueVRClick();
            } else
            {
                getViewInterface().showSimpleDialog(null, getString(R.string.message_truevr_not_support_hardware), getString(R.string.dialog_btn_text_confirm), null);
            }
        }

        mIsDeepLink = false;
    }

    private void notifyRewardChanged()
    {
        if (mStayDetail == null)
        {
            return;
        }

        if (DailyHotel.isLogin() == false)
        {
            boolean campaignEnabled = DailyRemoteConfigPreference.getInstance(getActivity()).isKeyRemoteConfigRewardStickerCampaignEnabled();

            getViewInterface().setRewardNonMember(DailyRemoteConfigPreference.getInstance(getActivity()).isKeyRemoteConfigRewardEnabled()//
                , DailyRemoteConfigPreference.getInstance(getActivity()).getKeyRemoteConfigRewardStickerTitleMessage()//
                , getString(R.string.label_reward_login)//
                , campaignEnabled ? DailyRemoteConfigPreference.getInstance(getActivity()).getKeyRemoteConfigRewardStickerNonMemberCampaignFreeNights() : 0//
                ,campaignEnabled ? DailyRemoteConfigPreference.getInstance(getActivity()).getKeyRemoteConfigRewardStickerNonMemberCampaignMessage()//
                    : DailyRemoteConfigPreference.getInstance(getActivity()).getKeyRemoteConfigRewardStickerNonMemberDefaultMessage());
        } else
        {

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
        if (mStayDetail == null)
        {
            return;
        }

        getViewInterface().setWishCount(mStayDetail.wishCount);
        getViewInterface().setWishSelected(mStayDetail.myWish);
    }

    void notifyWishChanged(int wishCount, boolean myWish)
    {
        if (mStayDetail == null)
        {
            return;
        }

        getViewInterface().setWishCount(wishCount);
        getViewInterface().setWishSelected(myWish);
    }

    private void showWishTooltip()
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

    private void startCalendar(CommonDateTime commonDateTime, StayBookDateTime stayBookDateTime//
        , int stayIndex, List<Integer> soldOutList, boolean animation) throws Exception
    {
        if (commonDateTime == null || stayBookDateTime == null)
        {
            return;
        }

        int dayCount = mStayDetail.overseas == false //
            ? StayDetailCalendarActivity.DEFAULT_DOMESTIC_CALENDAR_DAY_OF_MAX_COUNT //
            : StayDetailCalendarActivity.DEFAULT_OVERSEAS_CALENDAR_DAY_OF_MAX_COUNT;

        String callByScreen = equalsCallingActivity(EventWebActivity.class) ? AnalyticsManager.Label.EVENT : AnalyticsManager.ValueType.DETAIL;

        Intent intent = StayDetailCalendarActivity.newInstance(getActivity(), commonDateTime //
            , stayBookDateTime.getCheckInDateTime(DailyCalendar.ISO_8601_FORMAT)//
            , stayBookDateTime.getCheckOutDateTime(DailyCalendar.ISO_8601_FORMAT)//
            , dayCount, stayIndex, callByScreen //
            , (ArrayList) soldOutList, true, animation, mStayDetail.singleStay);

        startActivityForResult(intent, StayDetailActivity.REQUEST_CODE_CALENDAR);

        mAnalytics.onEventCalendarClick(getActivity());
    }

    private void checkChangedPrice(boolean isDeepLink, StayDetail stayDetail, int listViewPrice, boolean compareListPrice)
    {
        if (stayDetail == null)
        {
            return;
        }

        // 판매 완료 혹은 가격이 변동되었는지 조사한다
        List<StayRoom> roomList = stayDetail.getRoomList();

        if (roomList == null || roomList.size() == 0)
        {
            Intent intent = new Intent();
            intent.putExtra(StayDetailActivity.INTENT_EXTRA_DATA_SOLD_OUT, true);
            setResult(BaseActivity.RESULT_CODE_REFRESH, intent);

            getViewInterface().showSimpleDialog(getString(R.string.dialog_notice2), getString(R.string.message_stay_detail_sold_out)//
                , getString(R.string.label_changing_date), v -> onCalendarClick(), null, true);
        } else
        {
            if (isDeepLink == false && compareListPrice == true)
            {
                boolean hasPrice = false;

                if (listViewPrice == StayDetailActivity.NONE_PRICE)
                {
                    hasPrice = true;
                } else
                {
                    for (StayRoom room : roomList)
                    {
                        if (listViewPrice == room.discountAverage)
                        {
                            hasPrice = true;
                            break;
                        }
                    }
                }

                if (hasPrice == false)
                {
                    Intent intent = new Intent();
                    intent.putExtra(StayDetailActivity.INTENT_EXTRA_DATA_CHANGED_PRICE, true);
                    setResult(BaseActivity.RESULT_CODE_REFRESH, intent);

                    getViewInterface().showSimpleDialog(getString(R.string.dialog_notice2), getString(R.string.message_stay_detail_changed_price)//
                        , getString(R.string.dialog_btn_text_confirm), null, new DialogInterface.OnDismissListener()
                        {
                            @Override
                            public void onDismiss(DialogInterface dialog)
                            {
                                onActionButtonClick();
                            }
                        });

                    mAnalytics.onEventChangedPrice(getActivity(), isDeepLink, stayDetail.name, false);
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

        addCompositeDisposable(Observable.zip(observable//
            , mStayRemoteImpl.getDetail(mStayIndex, mStayBookDateTime)//
            , mCalendarImpl.getStayUnavailableCheckInDates(mStayIndex, StayDetailCalendarActivity.DEFAULT_OVERSEAS_CALENDAR_DAY_OF_MAX_COUNT, false)//
            , mStayRemoteImpl.getReviewScores(mStayIndex)//
            , mStayRemoteImpl.getHasCoupon(mStayIndex, mStayBookDateTime)//
            , mStayRemoteImpl.getTrueVR(mStayIndex)//
            , mCommonRemoteImpl.getCommonDateTime()//
            , new Function7<Boolean, StayDetail, List<String>, ReviewScores, Boolean, List<TrueVR>, CommonDateTime, StayDetail>()
            {
                @Override
                public StayDetail apply(@io.reactivex.annotations.NonNull Boolean aBoolean//
                    , @io.reactivex.annotations.NonNull StayDetail stayDetail//
                    , @io.reactivex.annotations.NonNull List<String> unavailableDates//
                    , @io.reactivex.annotations.NonNull ReviewScores reviewScores//
                    , @io.reactivex.annotations.NonNull Boolean hasCoupon//
                    , @io.reactivex.annotations.NonNull List<TrueVR> trueVRList//
                    , @io.reactivex.annotations.NonNull CommonDateTime commonDateTime) throws Exception
                {
                    setCommonDateTime(commonDateTime);
                    setReviewScores(reviewScores);
                    setSoldOutDateList(unavailableDates);

                    stayDetail.hasCoupon = hasCoupon;

                    setTrueVRList(trueVRList);
                    setGourmetDetail(stayDetail);

                    return stayDetail;
                }
            }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<StayDetail>()
        {
            @Override
            public void accept(@io.reactivex.annotations.NonNull StayDetail stayDetail) throws Exception
            {
                notifyDetailChanged();
                notifyWishChanged();
                notifyRewardChanged();

                if (disposable != null)
                {
                    disposable.dispose();
                }

                if (DailyPreference.getInstance(getActivity()).isWishTooltip() == true)
                {
                    showWishTooltip();
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

    private void onBookingRoom()
    {
        if (DailyHotel.isLogin() == false)
        {
            DailyToast.showToast(getActivity(), R.string.toast_msg_please_login, DailyToast.LENGTH_LONG);

            Intent intent = LoginActivity.newInstance(getActivity(), AnalyticsManager.Screen.DAILYHOTEL_DETAIL);
            startActivityForResult(intent, StayDetailActivity.REQUEST_CODE_LOGIN_IN_BY_BOOKING);
        } else
        {
            addCompositeDisposable(mProfileRemoteImpl.getProfile().subscribe(new Consumer<User>()
            {
                @Override
                public void accept(@io.reactivex.annotations.NonNull User user) throws Exception
                {
                    boolean isDailyUser = Constants.DAILY_USER.equalsIgnoreCase(user.userType);

                    if (isDailyUser == true)
                    {
                        // 인증이 되어있지 않던가 기존에 인증이 되었는데 인증이 해지되었다.
                        if (Util.isValidatePhoneNumber(user.phone) == false || (user.verified == true && user.phoneVerified == false))
                        {
                            startActivityForResult(EditProfilePhoneActivity.newInstance(getActivity()//
                                , EditProfilePhoneActivity.Type.NEED_VERIFICATION_PHONENUMBER, user.phone)//
                                , StayDetailActivity.REQUEST_CODE_PROFILE_UPDATE);
                        } else
                        {
                            startPayment(mStayBookDateTime, mStayDetail, mSelectedRoom);
                        }
                    } else
                    {
                        // 입력된 정보가 부족해.
                        if (DailyTextUtils.isTextEmpty(user.email, user.phone, user.name) == true)
                        {
                            Customer customer = new Customer();
                            customer.setEmail(user.email);
                            customer.setName(user.name);
                            customer.setPhone(user.phone);
                            customer.setUserIdx(Integer.toString(user.index));

                            startActivityForResult(AddProfileSocialActivity.newInstance(getActivity()//
                                , customer, user.birthday), StayDetailActivity.REQUEST_CODE_PROFILE_UPDATE);
                        } else if (Util.isValidatePhoneNumber(user.phone) == false)
                        {
                            startActivityForResult(EditProfilePhoneActivity.newInstance(getActivity()//
                                , EditProfilePhoneActivity.Type.WRONG_PHONENUMBER, user.phone)//
                                , StayDetailActivity.REQUEST_CODE_PROFILE_UPDATE);
                        } else
                        {
                            startPayment(mStayBookDateTime, mStayDetail, mSelectedRoom);
                        }
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
        }

        mAnalytics.onEventBookingClick(getActivity(), mStayBookDateTime, mStayDetail.index, mStayDetail.name//
            , mSelectedRoom.name, mStayDetail.category, mSelectedRoom.discountAverage);
    }

    private boolean isSoldOut(StayDetail stayDetail)
    {
        if (stayDetail == null)
        {
            return true;
        }

        return (mStayDetail.getRoomList() == null || mStayDetail.getRoomList().size() == 0);
    }

    private void startPayment(StayBookDateTime stayBookDateTime, StayDetail stayDetail, StayRoom stayRoom)
    {
        if (stayBookDateTime == null || stayDetail == null || stayRoom == null)
        {
            return;
        }

        List<DetailImageInformation> imageInformationList = stayDetail.getImageInformationList();
        String imageUrl = null;

        if (imageInformationList != null && imageInformationList.size() > 0)
        {
            imageUrl = imageInformationList.get(0).getImageMap().bigUrl;
        }

        Intent intent = StayPaymentActivity.newInstance(getActivity(), stayDetail.index//
            , stayDetail.name, imageUrl, stayRoom.index, stayRoom.discountTotal, stayRoom.name//
            , stayBookDateTime.getCheckInDateTime(DailyCalendar.ISO_8601_FORMAT)//
            , stayBookDateTime.getCheckOutDateTime(DailyCalendar.ISO_8601_FORMAT)//
            , stayDetail.overseas, stayDetail.category //
            , stayDetail.latitude, stayDetail.longitude //
            , mAnalytics.getStayPaymentAnalyticsParam(stayDetail, stayRoom));

        startActivityForResult(intent, StayDetailActivity.REQUEST_CODE_PAYMENT);
    }
}
