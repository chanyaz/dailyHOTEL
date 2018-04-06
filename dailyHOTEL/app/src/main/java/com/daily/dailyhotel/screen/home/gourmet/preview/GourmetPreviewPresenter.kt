package com.daily.dailyhotel.screen.home.gourmet.preview

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.view.View
import com.daily.base.BaseActivity
import com.daily.base.util.DailyTextUtils
import com.daily.base.util.FontManager
import com.daily.dailyhotel.base.BaseExceptionPresenter
import com.daily.dailyhotel.entity.GourmetBookDateTime
import com.daily.dailyhotel.entity.GourmetDetail
import com.daily.dailyhotel.entity.GourmetMenu
import com.daily.dailyhotel.entity.ReviewScores
import com.daily.dailyhotel.repository.remote.CommonRemoteImpl
import com.daily.dailyhotel.repository.remote.GourmetRemoteImpl
import com.daily.dailyhotel.screen.common.dialog.wish.WishDialogActivity
import com.daily.dailyhotel.storage.preference.DailyUserPreference
import com.twoheart.dailyhotel.R
import com.twoheart.dailyhotel.util.Constants
import com.twoheart.dailyhotel.util.KakaoLinkManager
import com.twoheart.dailyhotel.util.Util
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager
import com.twoheart.dailyhotel.widget.CustomFontTypefaceSpan
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import java.util.*

class GourmetPreviewPresenter(activity: GourmetPreviewActivity)
    : BaseExceptionPresenter<GourmetPreviewActivity, GourmetPreviewInterface.ViewInterface>(activity), GourmetPreviewInterface.OnEventListener {

    private lateinit var gourmetRemoteImpl: GourmetRemoteImpl
    private lateinit var commonRemoteImpl: CommonRemoteImpl

    private lateinit var bookDateTime: GourmetBookDateTime
    private var gourmetIndex: Int = 0
    private lateinit var gourmetName: String
    private lateinit var gourmetCategory: String
    private var viewPrice: Int = GourmetPreviewActivity.SKIP_CHECK_PRICE_VALUE

    private lateinit var detail: GourmetDetail
    private var trueReviewCount: Int = 0

    private val analytics: GourmetPreviewInterface.AnalyticsInterface by lazy {
        GourmetPreviewAnalyticsImpl()
    }

    override fun createInstanceViewInterface(): GourmetPreviewInterface.ViewInterface {
        return GourmetPreviewView(activity, this)
    }

    override fun constructorInitialize(activity: GourmetPreviewActivity) {
        setContentView(R.layout.activity_stay_preview_data)

        gourmetRemoteImpl = GourmetRemoteImpl(activity)
        commonRemoteImpl = CommonRemoteImpl(activity)

        isRefresh = true
    }

    override fun onIntent(intent: Intent?): Boolean {
        if (intent == null) {
            return true
        }

        val visitDateTime = intent.getStringExtra(GourmetPreviewActivity.INTENT_EXTRA_DATA_VISIT_DATE_TIME)

        try {
            bookDateTime = GourmetBookDateTime(visitDateTime)
        } catch (e: Exception) {
            return false
        }

        gourmetIndex = intent.getIntExtra(GourmetPreviewActivity.INTENT_EXTRA_DATA_GOURMET_INDEX, 0)

        if (gourmetIndex <= 0) {
            return false
        }

        gourmetName = intent.getStringExtra(GourmetPreviewActivity.INTENT_EXTRA_DATA_GOURMET_NAME)
        gourmetCategory = intent.getStringExtra(GourmetPreviewActivity.INTENT_EXTRA_DATA_GOURMET_CATEGORY)
        viewPrice = intent.getIntExtra(GourmetPreviewActivity.INTENT_EXTRA_DATA_GOURMET_VIEW_PRICE, GourmetPreviewActivity.SKIP_CHECK_PRICE_VALUE)

        return true
    }

    override fun onNewIntent(intent: Intent?) {
    }

    override fun onPostCreate() {
        notifyDataSetChanged()

        addCompositeDisposable(viewInterface.showAnimation().subscribe())
    }

    override fun onStart() {
        super.onStart()

        if (isRefresh) {
            onRefresh(true)
        }
    }

    override fun onResume() {
        super.onResume()

        if (isRefresh) {
            onRefresh(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBackPressed(): Boolean {
        if (lock()) {
            return true
        }

        hideAnimationAfterFinish()

        analytics.onEventBackClick(activity)

        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        unLockAll()

        when (requestCode) {
            GourmetPreviewActivity.REQUEST_CODE_WISH_DIALOG -> onWishDialogActivityResult(resultCode, intent)
        }
    }

    private fun onWishDialogActivityResult(resultCode: Int, intent: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK -> {

                intent?.let {
                    val wish = it.getBooleanExtra(WishDialogActivity.INTENT_EXTRA_DATA_WISH, false)

                    setResult(BaseActivity.RESULT_CODE_REFRESH, Intent().putExtra(GourmetPreviewActivity.INTENT_EXTRA_DATA_WISH, wish))
                }

                hideAnimationAfterFinish()
            }

            else -> viewInterface.setWish(detail.myWish)
        }
    }

    private fun hideAnimationAfterFinish(subFunction: (() -> Unit)? = null) {
        addCompositeDisposable(viewInterface.hideAnimation().observeOn(AndroidSchedulers.mainThread()).subscribe {
            subFunction?.invoke()
            finish()
        })
    }

    @Synchronized
    override fun onRefresh(showProgress: Boolean) {
        if (isFinish || !isRefresh) {
            return
        }

        isRefresh = false
        screenLock(showProgress)

        addCompositeDisposable(Observable.zip(gourmetRemoteImpl.getDetail(gourmetIndex, bookDateTime), gourmetRemoteImpl.getReviewScores(gourmetIndex)
                , BiFunction<GourmetDetail, ReviewScores, Int> { gourmetDetail, reviewScores ->
            this@GourmetPreviewPresenter.detail = gourmetDetail

            analytics.onScreen(activity, gourmetDetail.category)

            reviewScores.reviewScoreTotalCount
        }).observeOn(AndroidSchedulers.mainThread()).subscribe({ trueReviewCount ->
            this@GourmetPreviewPresenter.trueReviewCount = trueReviewCount

            notifyDataSetChanged()

            unLockAll()
        }, { throwable ->
            onHandleError(throwable)
            hideAnimationAfterFinish()
        }))
    }

    override fun onBackClick() {
        activity.onBackPressed()
    }

    override fun onDetailClick() {
        setResult(Activity.RESULT_OK)
        onBackClick()

        analytics.onEventDetailClick(activity)
    }

    override fun onWishClick() {
        if (!::detail.isInitialized || lock()) {
            return
        }

        val changeWish = !detail.myWish

        viewInterface.setWish(changeWish)

        analytics.onEventWishClick(activity, changeWish)

        startActivityForResult(WishDialogActivity.newInstance(activity, Constants.ServiceType.GOURMET
                , gourmetIndex, changeWish, AnalyticsManager.Screen.PEEK_POP), GourmetPreviewActivity.REQUEST_CODE_WISH_DIALOG)
    }

    override fun onKakaoClick() {
        if (!::detail.isInitialized || lock()) {
            return
        }

        analytics.onEventKakaoClick(activity)

        try {
            activity.packageManager.getPackageInfo("com.kakao.talk", PackageManager.GET_META_DATA)
        } catch (e: PackageManager.NameNotFoundException) {
            viewInterface.showSimpleDialog(null, getString(R.string.dialog_msg_not_installed_kakaotalk)
                    , getString(R.string.dialog_btn_text_yes), getString(R.string.dialog_btn_text_no)
                    , View.OnClickListener { Util.installPackage(activity, "com.kakao.talk") }
                    , null, null, DialogInterface.OnDismissListener { onBackClick() }, true)

            unLockAll()
            return
        }

        screenLock(true)

        val name = DailyUserPreference.getInstance(activity).name
        val urlFormat = "https://mobile.dailyhotel.co.kr/gourmet/%d?reserveDate=%s&utm_source=share&utm_medium=gourmet_detail_kakaotalk"
        val longUrl = String.format(Locale.KOREA, urlFormat, gourmetIndex
                , bookDateTime.getVisitDateTime("yyyy-MM-dd"))

        // flatMapCompletable 을 사용하고 싶었는데 shortUrl 을 넘겨주어야 하는게 쉽지 않다.
        addCompositeDisposable(commonRemoteImpl.getShortUrl(longUrl).observeOn(AndroidSchedulers.mainThread()).subscribe({ shortUrl ->
            hideAnimationAfterFinish { startKakaoLinkApplication(name, detail, bookDateTime, shortUrl) }
        }, {
            val mobileWebUrl = "https://mobile.dailyhotel.co.kr/gourmet/"

            hideAnimationAfterFinish { startKakaoLinkApplication(name, detail, bookDateTime, mobileWebUrl + detail.index) }
        }))
    }

    private fun startKakaoLinkApplication(userName: String, detail: GourmetDetail, bookDateTime: GourmetBookDateTime, url: String) {
        KakaoLinkManager.newInstance(activity).shareGourmet(userName
                , detail.name, detail.address, detail.index
                , if (detail.hasImageInformations()) detail.imageInformationList[0].imageMap.mediumUrl else null
                , url, bookDateTime)
    }

    override fun onMapClick() {
        if (!::detail.isInitialized || lock()) {
            return
        }

        hideAnimationAfterFinish { Util.shareNaverMap(activity, detail.name, detail.latitude.toString(), detail.longitude.toString()) }

        analytics.onEventMapClick(activity)
    }

    override fun onCloseClick() {
        if (lock()) {
            return
        }

        hideAnimationAfterFinish()

        analytics.onEventCloseClick(activity)
    }


    internal fun notifyDataSetChanged() {
        viewInterface.setName(gourmetName)

        if (::detail.isInitialized) {
            val soldOut = !detail.hasMenus()

            viewInterface.setCategory(detail.category, detail.categorySub)
            viewInterface.setImages(detail.imageInformationList.map { it.imageMap.smallUrl }.toTypedArray())

            notifyMenuInformationDataSetChanged(soldOut, detail.menuList)
            notifyReviewInformationDataSetChanged(trueReviewCount, detail.wishCount)

            viewInterface.setWish(detail.myWish)
            viewInterface.setBookingButtonText(if (soldOut) getString(R.string.label_booking_view_detail) else getString(R.string.label_preview_booking))
        } else {
            viewInterface.setCategory(detail.category, null)
        }
    }

    private fun notifyMenuInformationDataSetChanged(soldOut: Boolean, menuList: List<GourmetMenu>?) {
        val roomTypeCountText: String = if (soldOut) getString(R.string.message_preview_changed_price) else getMenuTypeCountText(menuList)
        val rangePriceText: String? = getRangePriceText(menuList)

        viewInterface.setMenuInformation(roomTypeCountText, !soldOut, rangePriceText)
    }

    private fun notifyReviewInformationDataSetChanged(trueReviewCount: Int, wishCount: Int) {
        when {
            trueReviewCount > 0 && wishCount > 0 -> {
                viewInterface.setReviewInformationVisible(true)

                val trueReviewCountText = getTrueReviewCountText(trueReviewCount)
                val wishCountText = getWishCountText(wishCount)

                viewInterface.setReviewInformation(true, trueReviewCountText, true, wishCountText)
            }

            trueReviewCount > 0 -> {
                viewInterface.setReviewInformationVisible(true)

                val trueReviewCountText = getTrueReviewCountText(trueReviewCount)

                viewInterface.setReviewInformation(true, trueReviewCountText, false, null)
            }

            wishCount > 0 -> {
                viewInterface.setReviewInformationVisible(true)

                val wishCountText = getWishCountText(wishCount)

                viewInterface.setReviewInformation(false, null, true, wishCountText)
            }

            else -> viewInterface.setReviewInformationVisible(false)
        }
    }

    private fun getMenuTypeCountText(menuList: List<GourmetMenu>?): String {
        return if (menuList == null || menuList.isEmpty()) {
            getString(R.string.message_preview_changed_price)
        } else {
            getString(R.string.label_detail_gourmet_product_count, menuList.size)
        }
    }

    private fun getRangePriceText(menuList: List<GourmetMenu>?): String? {
        return menuList?.let {
            var minPrice = Int.MAX_VALUE
            var maxPrice = Int.MIN_VALUE

            it.forEach {
                minPrice = Math.min(minPrice, it.discountPrice)
                maxPrice = Math.max(maxPrice, it.discountPrice)
            }

            if (minPrice == Int.MAX_VALUE || minPrice <= 0 || maxPrice == Int.MIN_VALUE || maxPrice == 0) {
                return null
            }

            if (minPrice == maxPrice) {
                DailyTextUtils.getPriceFormat(activity, maxPrice, false)
            } else {
                DailyTextUtils.getPriceFormat(activity, minPrice, false) + " ~ " + DailyTextUtils.getPriceFormat(activity, maxPrice, false)
            }
        }
    }

    private fun getTrueReviewCountText(count: Int): SpannableStringBuilder {
        val trueReviewCountText = getString(R.string.label_detail_truereview_count, DailyTextUtils.formatIntegerToString(count))

        return getCountTextSpannableStringBuilder(trueReviewCountText)
    }

    private fun getWishCountText(count: Int): SpannableStringBuilder {
        val wishCountText = getString(R.string.label_detail_wish_count, DailyTextUtils.formatIntegerToString(count))

        return getCountTextSpannableStringBuilder(wishCountText)
    }

    private fun getCountTextSpannableStringBuilder(countText: String): SpannableStringBuilder {
        val spannableStringBuilder = SpannableStringBuilder(countText)

        spannableStringBuilder.setSpan(CustomFontTypefaceSpan(FontManager.getInstance(activity).demiLightTypeface),
                countText.indexOf(" "), countText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        return spannableStringBuilder
    }
}