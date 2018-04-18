package com.daily.dailyhotel.screen.home.stay.inbound.detailk;

import android.app.Activity
import android.content.DialogInterface
import android.view.View
import android.widget.CompoundButton
import com.daily.base.BaseAnalyticsInterface
import com.daily.base.BaseDialogViewInterface
import com.daily.base.OnBaseEventListener
import com.daily.dailyhotel.entity.StayBookDateTime
import com.daily.dailyhotel.entity.StayDetail
import com.daily.dailyhotel.entity.StayRoom
import com.daily.dailyhotel.entity.TrueAwards
import com.daily.dailyhotel.parcel.analytics.StayDetailAnalyticsParam
import com.daily.dailyhotel.parcel.analytics.StayPaymentAnalyticsParam
import com.daily.dailyhotel.screen.home.stay.inbound.detail.StayDetailPresenter
import io.reactivex.Observable

interface StayDetailInterface {
    interface ViewInterface : BaseDialogViewInterface {
        fun getSharedElementTransition(gradientType: StayDetailActivity.TransGradientType): Observable<Boolean>

        fun setInitializedLayout(name: String?, url: String?)

        fun setTransitionVisible(visible: Boolean)

        fun setSharedElementTransitionEnabled(enabled: Boolean, gradientType: StayDetailActivity.TransGradientType)
    }

    interface OnEventListener : OnBaseEventListener {
    }

    interface AnalyticsInterface : BaseAnalyticsInterface {
        fun setAnalyticsParam(analyticsParam: StayDetailAnalyticsParam)

        fun getStayPaymentAnalyticsParam(stayDetail: StayDetail, stayRoom: StayRoom): StayPaymentAnalyticsParam

        fun onScreen(activity: Activity, stayBookDateTime: StayBookDateTime, stayDetail: StayDetail, priceFromList: Int)

        fun onScreenRoomList(activity: Activity, stayBookDateTime: StayBookDateTime, stayDetail: StayDetail, priceFromList: Int)

        fun onEventRoomListOpenClick(activity: Activity, stayName: String)

        fun onEventRoomListCloseClick(activity: Activity, stayName: String)

        fun onEventRoomClick(activity: Activity, roomName: String)

        fun onEventShareKakaoClick(activity: Activity, login: Boolean, userType: String, benefitAlarm: Boolean//
                                   , stayIndex: Int, stayName: String, overseas: Boolean)

        fun onEventLinkCopyClick(activity: Activity)

        fun onEventMoreShareClick(activity: Activity)

        fun onEventDownloadCoupon(activity: Activity, stayName: String)

        fun onEventDownloadCouponByLogin(activity: Activity, login: Boolean)

        fun onEventShare(activity: Activity)

        fun onEventChangedPrice(activity: Activity, deepLink: Boolean, stayName: String, soldOut: Boolean)

        fun onEventCalendarClick(activity: Activity)

        fun onEventBookingClick(activity: Activity, stayBookDateTime: StayBookDateTime//
                                , stayIndex: Int, stayName: String, roomName: String, discountPrice: Int, category: String//
                                , provideRewardSticker: Boolean, isOverseas: Boolean)

        fun onEventTrueReviewClick(activity: Activity)

        fun onEventTrueVRClick(activity: Activity, stayIndex: Int)

        fun onEventImageClick(activity: Activity, stayName: String)

        fun onEventConciergeClick(activity: Activity)

        fun onEventMapClick(activity: Activity, stayName: String)

        fun onEventClipAddressClick(activity: Activity, stayName: String)

        fun onEventWishClick(activity: Activity, stayBookDateTime: StayBookDateTime, stayDetail: StayDetail, priceFromList: Int, myWish: Boolean)

        fun onEventCallClick(activity: Activity)

        fun onEventFaqClick(activity: Activity)

        fun onEventHappyTalkClick(activity: Activity)

        fun onEventShowTrueReview(activity: Activity, stayIndex: Int)

        fun onEventShowCoupon(activity: Activity, stayIndex: Int)

        fun onEventTrueAwards(activity: Activity, stayIndex: Int)

        fun onEventTrueAwardsClick(activity: Activity, stayIndex: Int)
    }
}
