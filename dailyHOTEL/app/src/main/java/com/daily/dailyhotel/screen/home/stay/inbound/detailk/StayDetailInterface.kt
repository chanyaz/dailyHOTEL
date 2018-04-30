package com.daily.dailyhotel.screen.home.stay.inbound.detailk

import android.app.Activity
import android.content.DialogInterface
import android.view.View
import android.widget.CompoundButton
import com.daily.base.BaseAnalyticsInterface
import com.daily.base.BaseDialogViewInterface
import com.daily.base.OnBaseEventListener
import com.daily.dailyhotel.entity.*
import com.daily.dailyhotel.parcel.analytics.StayDetailAnalyticsParam
import com.daily.dailyhotel.parcel.analytics.StayPaymentAnalyticsParam
import io.reactivex.Observable

interface StayDetailInterface {
    interface ViewInterface : BaseDialogViewInterface {


        fun setInitializedLayout(name: String?, url: String?)

        fun setTransitionVisible(visible: Boolean)

        fun getSharedElementTransition(gradientType: StayDetailActivity.TransGradientType): Observable<Boolean>

        fun setSharedElementTransitionEnabled(enabled: Boolean, gradientType: StayDetailActivity.TransGradientType)


        fun showWishTooltip()

        fun hideWishTooltip()


        fun setWishCount(count: Int)

        fun setWishSelected(selected: Boolean)

        fun setVRVisible(visible: Boolean)

        fun setMoreImageVisible(visible: Boolean)

        fun setImageList(imageList: List<DetailImageInformation>)

        fun setBaseInformation()

        fun setTrueAwardsVisible(visible: Boolean)

        fun setTrueReview()

        fun setBenefit()

        fun setRoomFilter()

        fun setRoomList()

        fun setDailyComment()

        fun setAmenities()

        fun setAddress()

        fun setCheckDateInformation()

        fun setDetailInformation()

        fun setBreakfastInformation()

        fun setCancellationAndRefundPolicy()

        fun setWaitingBookingVisible()

        fun setRewardVisible(visible: Boolean)

        fun setRewardMemberInformation(titleText: String, optionText: String, nights: Int, descriptionText: String)

        fun setRewardNonMemberInformation(titleText: String, optionText: String, campaignFreeNights: Int, descriptionText: String)

        fun startRewardStickerAnimation()

        fun stopRewardStickerAnimation()

        fun setConciergeInformation()


        fun scrollTop()

        fun showShareDialog(listener: DialogInterface.OnDismissListener)

        fun showWishPopup(myWish: Boolean): Observable<Boolean>

        fun showConciergeDialog(listener: DialogInterface.OnDismissListener)

        fun showTrueVRDialog(checkedChangeListener: CompoundButton.OnCheckedChangeListener,
                             positiveListener: View.OnClickListener,
                             onDismissListener: DialogInterface.OnDismissListener)

        fun showTrueAwardsDialog(trueAwards: TrueAwards?, onDismissListener: DialogInterface.OnDismissListener)


        fun setActionButtonText(text: String)

        fun setActionButtonEnabled(enabled: Boolean)
    }

    interface OnEventListener : OnBaseEventListener {
        fun onShareClick()

        fun onWishClick()

        fun onShareKakaoClick()

        fun onCopyLinkClick()

        fun onMoreShareClick()

        fun onImageClick(position: Int)

        fun onCalendarClick()

        fun onMapClick()

        fun onClipAddressClick(address: String)

        fun onNavigatorClick()

        fun onConciergeClick()

        fun onMoreRoomListClick()

        fun onPriceTypeClick(priceType: StayDetailPresenter.PriceType)

        fun onConciergeFaqClick()

        fun onConciergeHappyTalkClick()

        fun onConciergeCallClick()

        fun onRoomClick(stayRoom: StayRoom)

        fun onTrueReviewClick()

        fun onTrueVRClick()

        fun onDownloadCouponClick()

        fun onHideWishTooltipClick()

        fun onLoginClick()

        fun onRewardClick()

        fun onRewardGuideClick()

        fun onTrueAwardsClick()
    }

    interface AnalyticsInterface : BaseAnalyticsInterface {
        fun setAnalyticsParam(analyticsParam: StayDetailAnalyticsParam)

        fun getStayPaymentAnalyticsParam(stayDetail: StayDetailk, stayRoom: StayRoom): StayPaymentAnalyticsParam

        fun onScreen(activity: Activity, stayBookDateTime: StayBookDateTime, stayDetail: StayDetailk?, priceFromList: Int)

        fun onScreenRoomList(activity: Activity, stayBookDateTime: StayBookDateTime, stayDetail: StayDetailk, priceFromList: Int)

        fun onEventRoomListOpenClick(activity: Activity, stayName: String)

        fun onEventRoomListCloseClick(activity: Activity, stayName: String)

        fun onEventRoomClick(activity: Activity, roomName: String)

        fun onEventShareKakaoClick(activity: Activity, login: Boolean, userType: String, benefitAlarm: Boolean//
                                   , stayIndex: Int, stayName: String?)

        fun onEventLinkCopyClick(activity: Activity)

        fun onEventMoreShareClick(activity: Activity)

        fun onEventDownloadCoupon(activity: Activity, stayName: String?)

        fun onEventDownloadCouponByLogin(activity: Activity, login: Boolean)

        fun onEventShare(activity: Activity)

        fun onEventChangedPrice(activity: Activity, deepLink: Boolean, stayName: String, soldOut: Boolean)

        fun onEventCalendarClick(activity: Activity)

        fun onEventBookingClick(activity: Activity, stayBookDateTime: StayBookDateTime//
                                , stayIndex: Int, stayName: String, roomName: String, discountPrice: Int, category: String//
                                , provideRewardSticker: Boolean, isOverseas: Boolean)

        fun onEventTrueReviewClick(activity: Activity)

        fun onEventTrueVRClick(activity: Activity, stayIndex: Int)

        fun onEventImageClick(activity: Activity, stayName: String?)

        fun onEventConciergeClick(activity: Activity)

        fun onEventMapClick(activity: Activity, stayName: String?)

        fun onEventClipAddressClick(activity: Activity, stayName: String?)

        fun onEventWishClick(activity: Activity, stayBookDateTime: StayBookDateTime, stayDetail: StayDetailk, priceFromList: Int, myWish: Boolean)

        fun onEventCallClick(activity: Activity)

        fun onEventFaqClick(activity: Activity)

        fun onEventHappyTalkClick(activity: Activity)

        fun onEventShowTrueReview(activity: Activity, stayIndex: Int)

        fun onEventShowCoupon(activity: Activity, stayIndex: Int)

        fun onEventTrueAwards(activity: Activity, stayIndex: Int)

        fun onEventTrueAwardsClick(activity: Activity, stayIndex: Int)
    }
}