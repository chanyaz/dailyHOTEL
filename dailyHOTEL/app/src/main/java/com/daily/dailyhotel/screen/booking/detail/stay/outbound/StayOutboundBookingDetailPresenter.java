package com.daily.dailyhotel.screen.booking.detail.stay.outbound;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;

import com.daily.base.BaseAnalyticsInterface;
import com.daily.base.exception.DuplicateRunException;
import com.daily.base.exception.PermissionException;
import com.daily.base.exception.ProviderException;
import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ExLog;
import com.daily.base.util.FontManager;
import com.daily.base.widget.DailyToast;
import com.daily.dailyhotel.base.BaseExceptionPresenter;
import com.daily.dailyhotel.entity.Booking;
import com.daily.dailyhotel.entity.CommonDateTime;
import com.daily.dailyhotel.entity.StayBookDateTime;
import com.daily.dailyhotel.entity.StayOutboundBookingDetail;
import com.daily.dailyhotel.parcel.analytics.NavigatorAnalyticsParam;
import com.daily.dailyhotel.parcel.analytics.StayOutboundDetailAnalyticsParam;
import com.daily.dailyhotel.repository.remote.BookingRemoteImpl;
import com.daily.dailyhotel.repository.remote.CommonRemoteImpl;
import com.daily.dailyhotel.screen.booking.detail.stay.outbound.receipt.StayOutboundReceiptActivity;
import com.daily.dailyhotel.screen.booking.detail.stay.outbound.refund.StayOutboundRefundActivity;
import com.daily.dailyhotel.screen.common.dialog.call.CallDialogActivity;
import com.daily.dailyhotel.screen.common.dialog.navigator.NavigatorDialogActivity;
import com.daily.dailyhotel.screen.home.stay.outbound.detail.StayOutboundDetailActivity;
import com.daily.dailyhotel.storage.preference.DailyUserPreference;
import com.daily.dailyhotel.util.DailyLocationExFactory;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.screen.common.HappyTalkCategoryDialog;
import com.twoheart.dailyhotel.screen.common.PermissionManagerActivity;
import com.twoheart.dailyhotel.screen.common.ZoomMapActivity;
import com.twoheart.dailyhotel.screen.information.FAQActivity;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.DailyCalendar;
import com.twoheart.dailyhotel.util.KakaoLinkManager;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;
import com.twoheart.dailyhotel.widget.CustomFontTypefaceSpan;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;

/**
 * Created by sheldon
 * Clean Architecture
 */
public class StayOutboundBookingDetailPresenter extends BaseExceptionPresenter<StayOutboundBookingDetailActivity, StayOutboundBookingDetailInterface> implements StayOutboundBookingDetailView.OnEventListener
{
    StayOutboundBookingAnalyticsInterface mAnalytics;

    private CommonRemoteImpl mCommonRemoteImpl;
    BookingRemoteImpl mBookingRemoteImpl;

    int mBookingIndex;
    private String mImageUrl;
    int mBookingState;

    private CommonDateTime mCommonDateTime;
    StayOutboundBookingDetail mStayOutboundBookingDetail;

    DailyLocationExFactory mDailyLocationExFactory;

    public interface StayOutboundBookingAnalyticsInterface extends BaseAnalyticsInterface
    {
        void onScreen(Activity activity, int bookingState, int placeIndex, StayOutboundBookingDetail.RefundType refundType);

        StayOutboundDetailAnalyticsParam getDetailAnalyticsParam(StayOutboundBookingDetail stayOutboundBookingDetail);
    }

    public StayOutboundBookingDetailPresenter(@NonNull StayOutboundBookingDetailActivity activity)
    {
        super(activity);
    }

    @NonNull
    @Override
    protected StayOutboundBookingDetailInterface createInstanceViewInterface()
    {
        return new StayOutboundBookingDetailView(getActivity(), this);
    }

    @Override
    public void constructorInitialize(StayOutboundBookingDetailActivity activity)
    {
        setContentView(R.layout.activity_stay_outbound_booking_detail_data);

        setAnalytics(new StayOutboundBookingDetailAnalyticsImpl());

        mCommonRemoteImpl = new CommonRemoteImpl(activity);
        mBookingRemoteImpl = new BookingRemoteImpl(activity);

        setRefresh(true);
    }

    @Override
    public void setAnalytics(BaseAnalyticsInterface analytics)
    {
        mAnalytics = (StayOutboundBookingAnalyticsInterface) analytics;
    }

    @Override
    public boolean onIntent(Intent intent)
    {
        if (intent == null)
        {
            return true;
        }

        mBookingIndex = intent.getIntExtra(StayOutboundBookingDetailActivity.INTENT_EXTRA_DATA_BOOKING_INDEX, -1);
        mImageUrl = intent.getStringExtra(StayOutboundBookingDetailActivity.INTENT_EXTRA_DATA_IMAGE_URL);
        mBookingState = intent.getIntExtra(StayOutboundBookingDetailActivity.INTENT_EXTRA_DATA_BOOKING_STATE, Booking.BOOKING_STATE_NONE);

        return true;
    }

    @Override
    public void onPostCreate()
    {
        getViewInterface().setToolbarTitle(getString(R.string.actionbar_title_booking_list_frag));
        getViewInterface().setBookingDetailToolbar();

        getViewInterface().setDeleteBookingVisible(mBookingState == Booking.BOOKING_STATE_AFTER_USE);
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
        if (mStayOutboundBookingDetail != null && getViewInterface().isExpandedMap() == true)
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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        unLockAll();

        switch (requestCode)
        {
            case StayOutboundBookingDetailActivity.REQUEST_CODE_PERMISSION_MANAGER:
            {
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                        onMyLocationClick();
                        break;

                    default:
                        break;
                }
                break;
            }

            case StayOutboundBookingDetailActivity.REQUEST_CODE_SETTING_LOCATION:
                onMyLocationClick();
                break;

            case StayOutboundBookingDetailActivity.REQUEST_CODE_REFUND:
                if (resultCode == Activity.RESULT_OK)
                {
                    setResult(Constants.CODE_RESULT_ACTIVITY_REFRESH);
                    onBackClick();
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

        addCompositeDisposable(Observable.zip(mCommonRemoteImpl.getCommonDateTime(), mBookingRemoteImpl.getStayOutboundBookingDetail(mBookingIndex)//
            , new BiFunction<CommonDateTime, StayOutboundBookingDetail, StayOutboundBookingDetail>()
            {
                @Override
                public StayOutboundBookingDetail apply(@io.reactivex.annotations.NonNull CommonDateTime commonDateTime, @io.reactivex.annotations.NonNull StayOutboundBookingDetail stayOutboundBookingDetail) throws Exception
                {
                    setCommonDateTime(commonDateTime);
                    setStayOutboundBookingDetail(stayOutboundBookingDetail);
                    return stayOutboundBookingDetail;
                }
            }).subscribe(new Consumer<StayOutboundBookingDetail>()
        {
            @Override
            public void accept(@io.reactivex.annotations.NonNull StayOutboundBookingDetail stayOutboundBookingDetail) throws Exception
            {
                notifyStayOutboundBookingDetailChanged();

                unLockAll();

                mAnalytics.onScreen(getActivity(), mBookingState, mStayOutboundBookingDetail.stayIndex, mStayOutboundBookingDetail.refundStatus);
            }
        }, new Consumer<Throwable>()
        {
            @Override
            public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception
            {
                onHandleErrorAndFinish(throwable);
            }
        }));
    }

    @Override
    public void onBackClick()
    {
        getActivity().onBackPressed();
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
    public void onMapLoading()
    {
        DailyToast.showToast(getActivity(), R.string.message_loading_map, DailyToast.LENGTH_SHORT);
    }

    @Override
    public void onMapClick()
    {
        if (mStayOutboundBookingDetail == null || lock() == true)
        {
            return;
        }

        Intent intent = ZoomMapActivity.newInstance(getActivity()//
            , ZoomMapActivity.SourceType.HOTEL_BOOKING, mStayOutboundBookingDetail.name, mStayOutboundBookingDetail.address//
            , mStayOutboundBookingDetail.latitude, mStayOutboundBookingDetail.longitude, true);

        startActivityForResult(intent, StayOutboundBookingDetailActivity.REQUEST_CODE_ZOOMMAP);

        AnalyticsManager.getInstance(getActivity()).recordEvent(AnalyticsManager.Category.BOOKING_STATUS//
            , AnalyticsManager.Action.MAP_CLICK, AnalyticsManager.ValueType.EMPTY, null);
    }

    @Override
    public void onExpandMapClick()
    {
        if (mStayOutboundBookingDetail == null || lock() == true)
        {
            return;
        }

        getViewInterface().setBookingDetailMapToolbar();

        addCompositeDisposable(getViewInterface().expandMap(mStayOutboundBookingDetail.latitude, mStayOutboundBookingDetail.longitude)//
            .subscribeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
            {
                @Override
                public void accept(@io.reactivex.annotations.NonNull Boolean aBoolean) throws Exception
                {
                    unLockAll();
                }
            }));

        AnalyticsManager.getInstance(getActivity()).recordEvent(AnalyticsManager.Category.BOOKING_STATUS//
            , AnalyticsManager.Action.MAP_CLICK, AnalyticsManager.ValueType.EMPTY, null);
    }

    @Override
    public void onCollapseMapClick()
    {
        if (mStayOutboundBookingDetail == null || lock() == true)
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
        if (mStayOutboundBookingDetail == null || lock() == true)
        {
            return;
        }

        try
        {
            StayBookDateTime stayBookDateTime = new StayBookDateTime();
            stayBookDateTime.setCheckInDateTime(mCommonDateTime.currentDateTime, 7);
            stayBookDateTime.setCheckOutDateTime(stayBookDateTime.getCheckInDateTime(DailyCalendar.ISO_8601_FORMAT), 1);

            StayOutboundDetailAnalyticsParam analyticsParam = mAnalytics.getDetailAnalyticsParam(mStayOutboundBookingDetail);

            startActivityForResult(StayOutboundDetailActivity.newInstance(getActivity(), mStayOutboundBookingDetail.stayIndex//
                , mStayOutboundBookingDetail.name, null, StayOutboundDetailActivity.NONE_PRICE//
                , stayBookDateTime.getCheckInDateTime(DailyCalendar.ISO_8601_FORMAT)//
                , stayBookDateTime.getCheckOutDateTime(DailyCalendar.ISO_8601_FORMAT)//
                , 2, null, false, StayOutboundDetailActivity.TRANS_GRADIENT_BOTTOM_TYPE_NONE, analyticsParam), StayOutboundBookingDetailActivity.REQUEST_CODE_DETAIL);

            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.hold);

            AnalyticsManager.getInstance(getActivity()).recordEvent(AnalyticsManager.Category.BOOKING_STATUS//
                , AnalyticsManager.Action.BOOKING_ITEM_DETAIL_CLICK, AnalyticsManager.ValueType.EMPTY, null);
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    @Override
    public void onNavigatorClick()
    {
        if (mStayOutboundBookingDetail == null || lock() == true)
        {
            return;
        }

        NavigatorAnalyticsParam analyticsParam = new NavigatorAnalyticsParam();

        startActivityForResult(NavigatorDialogActivity.newInstance(getActivity(), mStayOutboundBookingDetail.name//
            , mStayOutboundBookingDetail.latitude, mStayOutboundBookingDetail.longitude, true, analyticsParam), StayOutboundBookingDetailActivity.REQUEST_CODE_NAVIGATOR);
    }

    @Override
    public void onRefundClick()
    {
        if (mStayOutboundBookingDetail == null || lock() == true)
        {
            return;
        }

        switch (mStayOutboundBookingDetail.refundStatus)
        {
            case FULL:
                startActivityForResult(StayOutboundRefundActivity.newInstance(getActivity()//
                    , mBookingIndex, getString(R.string.label_request_free_refund))//
                    , StayOutboundBookingDetailActivity.REQUEST_CODE_REFUND);
                break;
            case PARTIAL:
                startActivityForResult(StayOutboundRefundActivity.newInstance(getActivity()//
                    , mBookingIndex, getString(R.string.label_contact_request_refund))//
                    , StayOutboundBookingDetailActivity.REQUEST_CODE_REFUND);
                break;

            case NRD:
            case TIMEOVER:
            default:
                getViewInterface().showRefundCallDialog(new DialogInterface.OnDismissListener()
                {
                    @Override
                    public void onDismiss(DialogInterface dialog)
                    {
                        unLockAll();
                    }
                });
                break;
        }

        unLockAll();
    }

    @Override
    public void onClipAddressClick()
    {
        if (mStayOutboundBookingDetail == null)
        {
            return;
        }

        DailyTextUtils.clipText(getActivity(), mStayOutboundBookingDetail.address);

        DailyToast.showToast(getActivity(), R.string.message_detail_copy_address, DailyToast.LENGTH_SHORT);
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
    public void onIssuingReceiptClick()
    {
        if (lock() == true)
        {
            return;
        }

        startActivityForResult(StayOutboundReceiptActivity.newInstance(getActivity(), mBookingIndex), StayOutboundBookingDetailActivity.REQUEST_CODE_ISSUING_RECEIPT);
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
    public void onConciergeFaqClick()
    {
        startActivity(FAQActivity.newInstance(getActivity()));
    }

    @Override
    public void onConciergeHappyTalkClick(boolean refund)
    {
        if (mStayOutboundBookingDetail == null)
        {
            return;
        }

        try
        {
            // 카카오톡 패키지 설치 여부
            getActivity().getPackageManager().getPackageInfo("com.kakao.talk", PackageManager.GET_META_DATA);

            if (refund == true)
            {
                startActivityForResult(HappyTalkCategoryDialog.newInstance(getActivity(), HappyTalkCategoryDialog.CallScreen.SCREEN_STAY_OUTBOUND_REFUND//
                    , mStayOutboundBookingDetail.stayIndex, 0, mStayOutboundBookingDetail.name), StayOutboundBookingDetailActivity.REQUEST_CODE_HAPPYTALK);
            } else
            {
                startActivityForResult(HappyTalkCategoryDialog.newInstance(getActivity(), HappyTalkCategoryDialog.CallScreen.SCREEN_STAY_OUTBOUND_BOOKING//
                    , mStayOutboundBookingDetail.stayIndex, 0, mStayOutboundBookingDetail.name), StayOutboundBookingDetailActivity.REQUEST_CODE_HAPPYTALK);
            }
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
        startActivityForResult(CallDialogActivity.newInstance(getActivity()), StayOutboundBookingDetailActivity.REQUEST_CODE_CALL);
    }

    @Override
    public void onShareKakaoClick()
    {
        if (mStayOutboundBookingDetail == null)
        {
            return;
        }

        try
        {
            // 카카오톡 패키지 설치 여부
            getActivity().getPackageManager().getPackageInfo("com.kakao.talk", PackageManager.GET_META_DATA);

            String userName = DailyUserPreference.getInstance(getActivity()).getName();

            String checkInTime = getString(R.string.label_stay_outbound_payment_hour, mStayOutboundBookingDetail.checkInTime.split(":")[0]);
            String checkInDate = DailyCalendar.convertDateFormatString(mStayOutboundBookingDetail.checkInDate, "yyyy-MM-dd", "yyyy-MM-dd(EEE)");

            String checkOutTime = getString(R.string.label_stay_outbound_payment_hour, mStayOutboundBookingDetail.checkOutTime.split(":")[0]);
            String checkOutDate = DailyCalendar.convertDateFormatString(mStayOutboundBookingDetail.checkOutDate, "yyyy-MM-dd", "yyyy-MM-dd(EEE)");

            String message = getString(R.string.message_booking_stay_outbound_share_kakao, //
                userName, mStayOutboundBookingDetail.name, mStayOutboundBookingDetail.guestLastName + " " + mStayOutboundBookingDetail.guestFirstName,//
                DailyTextUtils.getPriceFormat(getActivity(), mStayOutboundBookingDetail.paymentPrice, false), //
                mStayOutboundBookingDetail.roomName, checkInDate + " " + checkInTime,//
                checkOutDate + " " + checkOutTime, //
                mStayOutboundBookingDetail.address);

            int nights = DailyCalendar.compareDateDay(DailyCalendar.convertDateFormatString(mStayOutboundBookingDetail.checkOutDate, "yyyy-MM-dd", DailyCalendar.ISO_8601_FORMAT)//
                , DailyCalendar.convertDateFormatString(mStayOutboundBookingDetail.checkInDate, "yyyy-MM-dd", DailyCalendar.ISO_8601_FORMAT));

            KakaoLinkManager.newInstance(getActivity()).shareBookingStayOutbound(message, mStayOutboundBookingDetail.stayIndex,//
                mImageUrl, DailyCalendar.convertDateFormatString(mStayOutboundBookingDetail.checkInDate, "yyyy-MM-dd", "yyyyMMdd"), nights);
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
    public void onShareSmsClick()
    {
        if (mStayOutboundBookingDetail == null)
        {
            return;
        }

        try
        {
            String userName = DailyUserPreference.getInstance(getActivity()).getName();

            String checkInTime = getString(R.string.label_stay_outbound_payment_hour, mStayOutboundBookingDetail.checkInTime.split(":")[0]);
            String checkInDate = DailyCalendar.convertDateFormatString(mStayOutboundBookingDetail.checkInDate, "yyyy-MM-dd", "yyyy-MM-dd(EEE)");

            String checkOutTime = getString(R.string.label_stay_outbound_payment_hour, mStayOutboundBookingDetail.checkOutTime.split(":")[0]);
            String checkOutDate = DailyCalendar.convertDateFormatString(mStayOutboundBookingDetail.checkOutDate, "yyyy-MM-dd", "yyyy-MM-dd(EEE)");

            //            int nights = DailyCalendar.compareDateDay(DailyCalendar.convertDateFormatString(mStayOutboundBookingDetail.checkOutDate, "yyyy-MM-dd", DailyCalendar.ISO_8601_FORMAT)//
            //                , DailyCalendar.convertDateFormatString(mStayOutboundBookingDetail.checkInDate, "yyyy-MM-dd", DailyCalendar.ISO_8601_FORMAT));

            String message = getString(R.string.message_booking_stay_share_sms, //
                userName, mStayOutboundBookingDetail.name, mStayOutboundBookingDetail.guestLastName + " " + mStayOutboundBookingDetail.guestFirstName,//
                DailyTextUtils.getPriceFormat(getActivity(), mStayOutboundBookingDetail.paymentPrice, false), //
                mStayOutboundBookingDetail.roomName, checkInDate + " " + checkInTime,//
                checkOutDate + " " + checkOutTime, //
                mStayOutboundBookingDetail.address);

            Util.sendSms(getActivity(), message);
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }
    }

    @Override
    public void onHiddenReservationClick()
    {
        if (mStayOutboundBookingDetail == null || lock() == true)
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

                    addCompositeDisposable(mBookingRemoteImpl.getStayOutboundHideBooking(mBookingIndex).subscribe(new Consumer<Boolean>()
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
                                            finish();
                                        }
                                    });

                                AnalyticsManager.getInstance(getActivity()).recordEvent(AnalyticsManager.Category.BOOKING_STATUS//
                                    , AnalyticsManager.Action.BOOKING_HISTORY_DELETE, "ob_" + mStayOutboundBookingDetail.stayIndex, null);
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


        AnalyticsManager.getInstance(getActivity()).recordEvent(AnalyticsManager.Category.BOOKING_STATUS//
            , AnalyticsManager.Action.BOOKING_HISTORY_DELETE_TRY, "ob_" + mStayOutboundBookingDetail.stayIndex, null);
    }

    void setCommonDateTime(@NonNull CommonDateTime commonDateTime)
    {
        if (commonDateTime == null)
        {
            return;
        }

        mCommonDateTime = commonDateTime;
    }

    void setStayOutboundBookingDetail(StayOutboundBookingDetail stayOutboundBookingDetail)
    {
        mStayOutboundBookingDetail = stayOutboundBookingDetail;
    }

    void notifyStayOutboundBookingDetailChanged()
    {
        if (mStayOutboundBookingDetail == null)
        {
            return;
        }

        final String DATE_FORMAT = "yyyy.MM.dd(EEE)";

        try
        {
            String checkInTime = getString(R.string.label_stay_outbound_payment_hour, mStayOutboundBookingDetail.checkInTime.split(":")[0]);
            String checkInDate = DailyCalendar.convertDateFormatString(mStayOutboundBookingDetail.checkInDate, "yyyy-MM-dd", DATE_FORMAT);

            SpannableString checkInDateSpannableString = new SpannableString(checkInDate + " " + checkInTime);
            checkInDateSpannableString.setSpan( //
                new CustomFontTypefaceSpan(FontManager.getInstance(getActivity()).getMediumTypeface()),//
                checkInDate.length(), checkInDate.length() + checkInTime.length() + 1,//
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            String checkOutTime = getString(R.string.label_stay_outbound_payment_hour, mStayOutboundBookingDetail.checkOutTime.split(":")[0]);
            String checkOutDate = DailyCalendar.convertDateFormatString(mStayOutboundBookingDetail.checkOutDate, "yyyy-MM-dd", DATE_FORMAT);

            SpannableString checkOutDateSpannableString = new SpannableString(checkOutDate + " " + checkOutTime);
            checkOutDateSpannableString.setSpan( //
                new CustomFontTypefaceSpan(FontManager.getInstance(getActivity()).getMediumTypeface()),//
                checkOutDate.length(), checkOutDate.length() + checkOutTime.length() + 1,//
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            int nights = DailyCalendar.compareDateDay(DailyCalendar.convertDateFormatString(mStayOutboundBookingDetail.checkOutDate, "yyyy-MM-dd", DailyCalendar.ISO_8601_FORMAT)//
                , DailyCalendar.convertDateFormatString(mStayOutboundBookingDetail.checkInDate, "yyyy-MM-dd", DailyCalendar.ISO_8601_FORMAT));
            getViewInterface().setBookingDate(checkInDateSpannableString, checkOutDateSpannableString, nights);
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }

        getViewInterface().setBookingDetail(mStayOutboundBookingDetail);

        getViewInterface().setRefundPolicy(mStayOutboundBookingDetail);
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
                startActivityForResult(intent, StayOutboundBookingDetailActivity.REQUEST_CODE_PERMISSION_MANAGER);
            } else if (throwable instanceof ProviderException)
            {
                // 현재 GPS 설정이 꺼져있습니다 설정에서 바꾸어 주세요.
                View.OnClickListener positiveListener = new View.OnClickListener()//
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, StayOutboundBookingDetailActivity.REQUEST_CODE_SETTING_LOCATION);
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
}
