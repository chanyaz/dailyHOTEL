package com.daily.dailyhotel.screen.home.stay.inbound.detail.rooms

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.support.v4.view.MotionEventCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.LinearInterpolator
import android.widget.CompoundButton
import com.daily.base.BaseDialogView
import com.daily.base.util.*
import com.daily.dailyhotel.entity.Room
import com.daily.dailyhotel.entity.StayDetailk
import com.daily.dailyhotel.util.isNotNullAndNotEmpty
import com.daily.dailyhotel.util.isTextEmpty
import com.daily.dailyhotel.util.letNotEmpty
import com.daily.dailyhotel.util.runTrue
import com.daily.dailyhotel.view.DailyRoomInfoGridView
import com.twoheart.dailyhotel.R
import com.twoheart.dailyhotel.databinding.ActivityStayRoomsDataBinding
import com.twoheart.dailyhotel.databinding.ListRowStayRoomInvisibleLayoutDataBinding
import com.twoheart.dailyhotel.util.DailyCalendar
import com.twoheart.dailyhotel.util.EdgeEffectColor
import com.twoheart.dailyhotel.util.Util
import com.twoheart.dailyhotel.widget.CustomFontTypefaceSpan
import io.reactivex.Observable
import io.reactivex.Observer

class StayRoomsView(activity: StayRoomsActivity, listener: StayRoomsInterface.OnEventListener)//
    : BaseDialogView<StayRoomsInterface.OnEventListener, ActivityStayRoomsDataBinding>(activity, listener)
        , StayRoomsInterface.ViewInterface, View.OnClickListener {
    private lateinit var listAdapter: StayRoomAdapter
    private var mTouchVerticalMargin: Int = 0
    private var mTouchHorizontalMargin: Int = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun setContentView(viewDataBinding: ActivityStayRoomsDataBinding) {
        viewDataBinding.run {
            closeImageView.setOnClickListener({
                eventListener.onBackClick()
            })

            recyclerView.layoutManager = ZoomCenterLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            EdgeEffectColor.setEdgeGlowColor(viewDataBinding.recyclerView, getColor(R.color.default_over_scroll_edge))

            val pagerSnapHelper = PagerSnapHelper()
            pagerSnapHelper.attachToRecyclerView(recyclerView)

            viewDataBinding.recyclerView.setOnTouchListener(object : View.OnTouchListener {
                private val MOVE_STATE_NONE = 0
                private val MOVE_STATE_SCROLL = 10
                private val MOVE_STATE_VIEWPAGER = 100
                private val MOVE_CALIBRATE_VALUE = 1.25f

                private var mMoveState: Int = 0
                private var mPrevX: Float = 0.toFloat()
                private var mPrevY: Float = 0.toFloat()

                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    if (listAdapter == null) return false
                    if (listAdapter.itemCount == 0) return false
                    if (event == null) return false

                    setRecyclerScrollEnabled()

                    when (event.action and MotionEventCompat.ACTION_MASK) {
                        MotionEvent.ACTION_DOWN -> {
                            mPrevX = event.x
                            mPrevY = event.y

                            mMoveState = MOVE_STATE_NONE
                        }

                        MotionEvent.ACTION_UP -> run {
                            setInvisibleLayoutAnimation()

                            val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

                            val x = (mPrevX - event.x).toInt()
                            val y = (mPrevY - event.y).toInt()

                            val distance = Math.sqrt((x * x + y * y).toDouble()).toInt()
                            if (distance < touchSlop) {
                                mMoveState = MOVE_STATE_NONE
                                return@run
                            }
                        }

                        MotionEvent.ACTION_CANCEL -> {
                            mMoveState = MOVE_STATE_NONE
                        }

                        MotionEvent.ACTION_MOVE -> {
                            val x = event.x
                            val y = event.y

                            when (mMoveState) {
                                MOVE_STATE_NONE -> {
                                    if (Math.abs(x - mPrevX) == Math.abs(y - mPrevY)) {
                                        if (viewDataBinding.invisibleLayout!!.nestedScrollView.visibility != View.GONE) {
                                            viewDataBinding.invisibleLayout.nestedScrollView.visibility = View.GONE
                                        }
                                    } else if (Math.abs(x - mPrevX) * MOVE_CALIBRATE_VALUE > Math.abs(y - mPrevY)) {
                                        // x 축으로 이동한 경우.
                                        mMoveState = MOVE_STATE_VIEWPAGER
                                        if (viewDataBinding.invisibleLayout!!.nestedScrollView.visibility != View.GONE) {
                                            viewDataBinding.invisibleLayout.nestedScrollView.visibility = View.GONE
                                        }
                                    } else {
                                        // y축으로 이동한 경우.
                                        mMoveState = MOVE_STATE_SCROLL

                                        if (viewDataBinding.invisibleLayout!!.nestedScrollView.visibility != View.VISIBLE) {
                                            viewDataBinding.invisibleLayout.nestedScrollView.visibility = View.VISIBLE
                                        }

                                        setInvisibleLayout(true, mPrevY, y)
                                    }
                                }

                                MOVE_STATE_SCROLL -> {
                                    if (viewDataBinding.invisibleLayout!!.nestedScrollView.visibility != View.VISIBLE) {
                                        viewDataBinding.invisibleLayout.nestedScrollView.visibility = View.VISIBLE
                                    }

                                    setInvisibleLayout(true, mPrevY, y)
                                }

                                MOVE_STATE_VIEWPAGER -> {

                                }
                            }
                        }
                    }

                    return false
                }
            })

            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)

                    val view = pagerSnapHelper.findSnapView(viewDataBinding.recyclerView.layoutManager)
                    val position = viewDataBinding.recyclerView.getChildAdapterPosition(view)

                    eventListener.onScrolled(position, true)
                }
            })

            if (!::listAdapter.isInitialized) {
                listAdapter = StayRoomAdapter(context, mutableListOf())
            }

            listAdapter.setEventListener(object : StayRoomAdapter.OnEventListener {
                override fun finish() {
                }

                override fun onMoreImageClick(position: Int) {
                    eventListener.onMoreImageClick(position)
                }

                override fun onVrImageClick(position: Int) {
                    eventListener.onVrImageClick(position)
                }
            })

            recyclerView.adapter = listAdapter

            guideLayout.setOnClickListener(this@StayRoomsView)
            guideLayout.visibility = View.GONE
        }
    }

    override fun setToolbarTitle(title: String?) {
        viewDataBinding.titleTextView.text = title
    }

    override fun setIndicatorText(position: Int) {
        val count = if (listAdapter.itemCount == 0) 1 else listAdapter.itemCount

        viewDataBinding.indicatorTextView.text = "$position / ${if (count == 0) 1 else count}"
    }

    override fun onClick(v: View?) {
        if (v == null) return

        when (v.id) {
            R.id.closeImageView -> eventListener.onCloseClick()

            R.id.guideLayout -> eventListener.onGuideClick()

            R.id.bookingTextView -> eventListener.onBookingClick()

            else -> {
            }
        }
    }

    override fun setBookingButtonText(position: Int) {
        val price = listAdapter.getItem(position)?.let {
            it.amountInformation.discountTotal
        } ?: 0

        val text = context.resources.getString(R.string.label_stay_room_booking_button_text
                , DailyTextUtils.getPriceFormat(context, price, false))

        viewDataBinding.bookingTextView.text = text
    }

    override fun setNights(nights: Int) {
        listAdapter.setNights(nights)

        viewDataBinding.nightsTextView.text = context.resources.getString(R.string.label_nights, nights)
        viewDataBinding.nightsTextView.visibility = if (nights > 1) View.VISIBLE else View.GONE
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setRoomList(roomList: MutableList<Room>, position: Int) {
        if (roomList.size == 0) {
            return
        }

        listAdapter.setData(roomList)

        viewDataBinding.recyclerView.post {
            (viewDataBinding.recyclerView.layoutManager as LinearLayoutManager)
                    .scrollToPositionWithOffset(position, listAdapter.getLayoutMargin().toInt())

            val roomViewHolder: StayRoomAdapter.RoomViewHolder = viewDataBinding.recyclerView.findViewHolderForAdapterPosition(position) as StayRoomAdapter.RoomViewHolder

            val top = viewDataBinding.recyclerView.top
            val paddingTop = roomViewHolder.dataBinding.root.paddingTop
            val width = roomViewHolder.dataBinding.root.measuredWidth
            val paddingLeft = roomViewHolder.dataBinding.root.paddingLeft
            val paddingRight = roomViewHolder.dataBinding.root.paddingRight

            mTouchVerticalMargin = top + paddingTop
            mTouchHorizontalMargin = (ScreenUtils.getScreenWidth(context) - (width - paddingLeft - paddingRight)) / 2

            ExLog.d("sam - top : " + top + " , mTouchVerticalMargin : " + mTouchVerticalMargin
                    + " , width : " + width
                    + " , paddingLeft : " + paddingLeft
                    + " , paddingRight : " + paddingRight
                    + " , mTouchHorizontalMargin : " + mTouchHorizontalMargin)

            viewDataBinding.invisibleLayout!!.nestedScrollView.setPadding(mTouchHorizontalMargin, mTouchVerticalMargin, mTouchHorizontalMargin, 0)

            setInvisibleData(position)

            viewDataBinding.invisibleLayout!!.nestedScrollView.setOnTouchListener(object : View.OnTouchListener {
                private var mPrevY: Float = 0.toFloat()

                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    if (viewDataBinding == null) return false
                    if (listAdapter.itemCount == 0) return false

                    setRecyclerScrollEnabled()

                    when (event.action and MotionEventCompat.ACTION_MASK) {
                        MotionEvent.ACTION_DOWN -> {
                            mPrevY = event.y
                        }

                        MotionEvent.ACTION_UP -> {
                            setInvisibleLayoutAnimation()
                        }

                        MotionEvent.ACTION_CANCEL -> {
                        }

                        MotionEvent.ACTION_MOVE -> {
                            val y = event.y

                            val verticalPadding = viewDataBinding.invisibleLayout!!.nestedScrollView.paddingTop
                            val scrollY = viewDataBinding.invisibleLayout!!.nestedScrollView.scrollY

                            ExLog.d("sam - verticalPadding : $verticalPadding , scrollY : $scrollY")

                            if (scrollY != 0) {
                                mPrevY = event.y
                                return false
                            }

                            setInvisibleLayout(false, mPrevY, y)

                            if (verticalPadding != 0) return true
                        }
                    }

                    return false
                }
            })
        }
    }

    override fun notifyDataSetChanged() {
        listAdapter.notifyDataSetChanged()
    }

    private fun setRecyclerScrollEnabled() {
        val layoutManager = viewDataBinding.recyclerView.layoutManager as ZoomCenterLayoutManager
        if (viewDataBinding.invisibleLayout?.nestedScrollView?.visibility == View.VISIBLE) {
            layoutManager.setScrollEnabled(false)
        } else {
            layoutManager.setScrollEnabled(true)
        }
    }

    private fun setInvisibleData(position: Int) {
        val room = listAdapter.getItem(position) ?: return

        // TODO : StayRoomAdapter 의 onBindViewHolder 기능 다 추가 필요

        val dataBinding: ListRowStayRoomInvisibleLayoutDataBinding = viewDataBinding.invisibleLayout!!

        if (room.imageInformation == null) {
            dataBinding.defaultImageLayout.visibility = View.GONE
            dataBinding.defaultImageLayout.setOnClickListener(null)
        } else {
            dataBinding.defaultImageLayout.visibility = View.VISIBLE
            dataBinding.defaultImageLayout.setOnClickListener {
                //                onEventListener?.let {
//                    it.onMoreImageClick(position)
//                }
            }

            dataBinding.simpleDraweeView.hierarchy.setPlaceholderImage(R.drawable.layerlist_placeholder)
            Util.requestImageResize(context, dataBinding.simpleDraweeView, room.imageInformation.imageMap.bigUrl)

            dataBinding.moreIconView.visibility = if (room.imageCount > 0) View.VISIBLE else View.GONE
            dataBinding.vrIconView.visibility = if (room.vrInformationList.isNotNullAndNotEmpty()) View.VISIBLE else View.GONE
            dataBinding.vrIconView.setOnClickListener {
                //                onEventListener?.let {
//                    it.onVrImageClick(position)
//                }
            }
        }

        dataBinding.roomNameTextView.text = room.name

        setAmountInformationView(dataBinding, room.amountInformation)

        setRefundInformationView(dataBinding, room.refundInformation)

        setBaseInformationGridView(dataBinding, room)

        setAttributeInformationView(dataBinding, room.attributeInformation)

        var benefitList = mutableListOf<String>()

        val breakfast = room.personsInformation?.breakfast ?: 0
        if (breakfast > 0) {
            benefitList.add(context.resources.getString(R.string.label_stay_room_breakfast_person, breakfast))
        }

        if (!room.benefit.isTextEmpty()) {
            benefitList.add(room.benefit)
        }
        setRoomBenefitInformationView(dataBinding, benefitList)

        setRewardAndCouponInformationView(dataBinding, room.provideRewardSticker, room.hasUsableCoupon)

        setCheckTimeInformationView(dataBinding, room.checkTimeInformation)

        setRoomDescriptionInformationView(dataBinding, room.descriptionList)

        setRoomAmenityInformationView(dataBinding, room.amenityList)

        setRoomChargeInformatinoView(dataBinding, room.roomChargeInformation)

        setNeedToKnowInformationView(dataBinding, room.needToKnowList)
    }

    private fun setAmountInformationView(dataBinding: ListRowStayRoomInvisibleLayoutDataBinding, amountInformation: Room.AmountInformation) {
        dataBinding.discountPercentTextView.visibility = View.VISIBLE
        dataBinding.priceTextView.visibility = View.VISIBLE

        val discountRateSpan = SpannableString("${amountInformation.discountRate}%")
        discountRateSpan.setSpan(CustomFontTypefaceSpan(FontManager.getInstance(context).regularTypeface), discountRateSpan.length - 1, discountRateSpan.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        discountRateSpan.setSpan(AbsoluteSizeSpan(ScreenUtils.dpToPx(context, 12.0)), discountRateSpan.length - 1, discountRateSpan.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        dataBinding.discountPercentTextView.text = discountRateSpan

        val nightsString = if (listAdapter.getNights() > 1) context.resources.getString(R.string.label_stay_detail_slash_one_nights) else ""
        val discountPriceString = DailyTextUtils.getPriceFormat(context, amountInformation.discountAverage, false)

        val discountPriceSpan = SpannableString("$discountPriceString$nightsString")
        discountPriceSpan.setSpan(CustomFontTypefaceSpan(FontManager.getInstance(context).regularTypeface), discountPriceString.length - 1, discountPriceSpan.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        discountPriceSpan.setSpan(AbsoluteSizeSpan(ScreenUtils.dpToPx(context, 12.0)), discountPriceString.length - 1, discountPriceSpan.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        dataBinding.discountPriceTextView.text = discountPriceSpan

        dataBinding.priceTextView.text = SpannableString(DailyTextUtils.getPriceFormat(context, amountInformation.priceAverage, false))
    }

    private fun setRefundInformationView(dataBinding: ListRowStayRoomInvisibleLayoutDataBinding, refundInformation: StayDetailk.RefundInformation?) {
        if (refundInformation == null) {
            dataBinding.refundPolicyTextView.visibility = View.GONE
            return
        }

        val isNrd = !refundInformation.type.isTextEmpty() && refundInformation.type?.toLowerCase().equals("nrd", true)
        if (!isNrd) {
            dataBinding.refundPolicyTextView.visibility = View.GONE
            return
        }

        var text = refundInformation.warningMessage
        if (text.isTextEmpty()) {
            text = context.resources.getString(R.string.label_stay_room_default_nrd_text)
        }

        dataBinding.refundPolicyTextView.visibility = View.VISIBLE
        dataBinding.refundPolicyTextView.text = text
    }

    private fun setBaseInformationGridView(dataBinding: ListRowStayRoomInvisibleLayoutDataBinding, room: Room) {
        val personsInformation: Room.PersonsInformation? = room.personsInformation
        val bedTypeList: List<Room.BedInformation.BedTypeInformation>? = room.bedInformation?.bedTypeList

        if (personsInformation == null && !bedTypeList.isNotNullAndNotEmpty() && room.squareMeter == 0f) {
            dataBinding.baseInfoGroup.visibility = View.GONE
            return
        }

        dataBinding.baseInfoGroup.visibility = View.VISIBLE

        setPersonInformationView(dataBinding, room)
        setBedInformationView(dataBinding, room)
        setSquareInformationView(dataBinding, room)
    }

    private fun setPersonInformationView(dataBinding: ListRowStayRoomInvisibleLayoutDataBinding, room: Room) {
        val personsInformation: Room.PersonsInformation? = room.personsInformation

        var personVectorIconResId: Int = 0
        var personTitle: String = ""
        var personDescription: String = ""

        personsInformation?.let {
            personTitle = context.resources.getString(R.string.label_standard_persons, it.fixed)

            personVectorIconResId = when (it.fixed) {
                0, 1 -> R.drawable.vector_ic_detail_item_people_1

                2 -> R.drawable.vector_ic_detail_item_people_2

                else -> R.drawable.vector_ic_detail_item_people_3
            }

            val subDescription = if (it.extra == 0) "" else " " + context.resources.getString(if (it.extraCharge) R.string.label_bracket_pay else R.string.label_bracket_free)
            personDescription = context.resources.getString(R.string.label_stay_outbound_room_max_person_free, it.fixed + it.extra) + subDescription
        }

        dataBinding.personIconImageView.setVectorImageResource(personVectorIconResId)
        dataBinding.personTitleTextView.text = personTitle
        dataBinding.personDescriptionTextView.text = personDescription
    }

    private fun setBedInformationView(dataBinding: ListRowStayRoomInvisibleLayoutDataBinding, room: Room) {
        val bedTypeList: List<Room.BedInformation.BedTypeInformation>? = room.bedInformation?.bedTypeList

        var bedVectorIconResId: Int = 0

        val typeStringList = mutableListOf<String>()

        bedTypeList?.forEach { bedTypeInformation ->
            val bedType: StayRoomAdapter.BedType = try {
                StayRoomAdapter.BedType.valueOf(bedTypeInformation.bedType.toUpperCase())
            } catch (e: Exception) {
                StayRoomAdapter.BedType.UNKNOWN
            }

            bedVectorIconResId = if (bedVectorIconResId == 0) {
                bedType.vectorIconResId
            } else {
                R.drawable.vector_ic_detail_item_bed_double
            }

            typeStringList += "${bedType.getName(context)} ${bedTypeInformation.count}"
            typeStringList += "${bedType.getName(context)} ${bedTypeInformation.count}"
        }

        bedVectorIconResId.takeIf { bedVectorIconResId == 0 }.let {
            StayRoomAdapter.BedType.UNKNOWN.vectorIconResId
        }

        dataBinding.bedIconImageView.setVectorImageResource(bedVectorIconResId)
        dataBinding.bedDescriptionLayout.setData(typeStringList)
    }

    private fun setSquareInformationView(dataBinding: ListRowStayRoomInvisibleLayoutDataBinding, room: Room) {
        dataBinding.squareTitleTextView.text = "${room.squareMeter}m"

        val pyoung = Math.round(room.squareMeter / 400 * 121)
        dataBinding.squareDescriptionTextView.text = context.resources.getString(R.string.label_pyoung_format, pyoung)
    }

    private fun setAttributeInformationView(dataBinding: ListRowStayRoomInvisibleLayoutDataBinding, attribute: Room.AttributeInformation?) {
        if (attribute == null) {
            dataBinding.subInfoGroup.visibility = View.GONE
            return
        }

        dataBinding.subInfoGroup.visibility = View.VISIBLE

        val roomType: StayRoomAdapter.RoomType = try {
            StayRoomAdapter.RoomType.valueOf(attribute.roomStructure)
        } catch (e: Exception) {
            StayRoomAdapter.RoomType.ONE_ROOM
        }

        var titleText = roomType.getName(context)

        attribute.isEntireHouse.runTrue { titleText += "/" + context.resources.getString(R.string.label_room_type_entire_house) }
        attribute.isDuplex.run { titleText += "/" + context.resources.getString(R.string.label_room_type_duplex_room) }

        dataBinding.subInfoGridView.setTitleText(titleText)
        dataBinding.subInfoGridView.setTitleVisible(true)
        dataBinding.subInfoGridView.setColumnCount(2)

        val stringList = mutableListOf<String>()
        var roomString = ""

        attribute.structureInformationList?.forEach {
            when (it.type) {
                "BED_ROOM" -> {
                    if (!roomString.isTextEmpty()) {
                        roomString += ", "
                    }

                    roomString += context.resources.getString(R.string.label_bed_room_format, it.count)
                }

                "IN_FLOOR_HEATING_ROOM" -> {
                    if (!roomString.isTextEmpty()) {
                        roomString += ", "
                    }

                    roomString += context.resources.getString(R.string.label_in_floor_heating_room_format, it.count)
                }

                "LIVING_ROOM" -> {
                    stringList += context.resources.getString(R.string.label_living_room_format, it.count)
                }

                "KITCHEN" -> {
                    stringList += context.resources.getString(R.string.label_kitchen_format, it.count)
                }

                "REST_ROOM" -> {
                    stringList += context.resources.getString(R.string.label_rest_room_format, it.count)
                }

                else -> {
                    // do nothing
                }
            }
        }

        if (!roomString.isTextEmpty()) {
            stringList.add(0, roomString)
        }

        dataBinding.subInfoGridView.setData(DailyRoomInfoGridView.ItemType.NONE, stringList)
    }

    private fun setRoomBenefitInformationView(dataBinding: ListRowStayRoomInvisibleLayoutDataBinding, benefitList: MutableList<String>) {
        if (benefitList.isEmpty()) {
            dataBinding.roomBenefitGroup.visibility = View.GONE
            return
        }

        dataBinding.roomBenefitGroup.visibility = View.VISIBLE

        dataBinding.roomAmenityGridView.setTitleText(R.string.label_stay_room_benefit_title)
        dataBinding.roomBenefitGridView.setColumnCount(1)
        dataBinding.roomBenefitGridView.setData(DailyRoomInfoGridView.ItemType.DOWN_CARET, benefitList)
    }

    private fun setRewardAndCouponInformationView(dataBinding: ListRowStayRoomInvisibleLayoutDataBinding, rewardable: Boolean, useCoupon: Boolean) {
        if (rewardable || useCoupon) {
            dataBinding.discountInfoGroup.visibility = View.VISIBLE
        } else {
            dataBinding.discountInfoGroup.visibility = View.GONE
            return
        }


        var text = ""
        val rewardString = context.resources.getString(R.string.label_stay_room_rewardable)
        val couponString = context.resources.getString(R.string.label_stay_room_coupon_useable)

        if (rewardable) {
            text = "  $rewardString"
        }

        if (useCoupon) {
            if (!text.isTextEmpty()) {
                text += context.resources.getString(R.string.label_stay_room_reward_coupon_or)
            }

            text += couponString
        }

        if (!text.isTextEmpty()) {
            text += context.resources.getString(R.string.label_stay_room_end_description)
        }

        val spannableString = SpannableString(text)

        val rewardStart = text.indexOf(rewardString)

        if (rewardStart != -1) {
            spannableString.setSpan(DailyImageSpan(context, R.drawable.vector_ic_r_ic_xs_14, DailyImageSpan.ALIGN_VERTICAL_CENTER), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableString.setSpan(ForegroundColorSpan(context.resources.getColor(R.color.default_line_cfaae37)), rewardStart, rewardStart + rewardString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        val couponStart = text.indexOf(couponString)
        if (couponStart != -1) {
            spannableString.setSpan(ForegroundColorSpan(context.resources.getColor(R.color.default_text_cf27c7a)), couponStart, rewardStart + couponString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        dataBinding.discountInfoTextView.text = spannableString
    }

    private fun setCheckTimeInformationView(dataBinding: ListRowStayRoomInvisibleLayoutDataBinding, checkTimeInformation: StayDetailk.CheckTimeInformation?) {
        if (checkTimeInformation == null) return

        val checkInTime = DailyCalendar.convertDateFormatString(checkTimeInformation.checkIn, "HH:mm:ss", "HH:mm")
        val checkOutTime = DailyCalendar.convertDateFormatString(checkTimeInformation.checkOut, "HH:mm:ss", "HH:mm")

        if (isTextEmpty(checkInTime, checkOutTime)) {
            dataBinding.checkTimeInfoLayout.visibility = View.GONE
            return
        }

        dataBinding.checkTimeInfoLayout.visibility = View.VISIBLE

        dataBinding.checkInTimeTextView.text = checkInTime
        dataBinding.checkOutTimeTextView.text = checkOutTime
    }

    private fun setRoomDescriptionInformationView(dataBinding: ListRowStayRoomInvisibleLayoutDataBinding, descriptionList: MutableList<String>?) {
        if (descriptionList == null || descriptionList.size == 0) {
            dataBinding.roomDescriptionGroup.visibility = View.GONE
            return
        }

        dataBinding.roomDescriptionGroup.visibility = View.VISIBLE

        dataBinding.roomDescriptionGridView.setTitleText(R.string.label_stay_room_description_title)
        dataBinding.roomDescriptionGridView.setColumnCount(1)
        dataBinding.roomDescriptionGridView.setData(DailyRoomInfoGridView.ItemType.DOT, descriptionList)
    }

    private fun setRoomAmenityInformationView(dataBinding: ListRowStayRoomInvisibleLayoutDataBinding, amenityList: MutableList<String>) {
        if (amenityList.size == 0) {
            dataBinding.roomAmenityGroup.visibility = View.GONE
            return
        }

        val list = mutableListOf<String>()
        amenityList.forEach {
            val amenityType: StayRoomAdapter.RoomAmenityType? = try {
                StayRoomAdapter.RoomAmenityType.valueOf(it)
            } catch (e: Exception) {
                null
            }

            amenityType?.run { list += amenityType.getName(context) }
        }

        if (list.isEmpty()) {
            dataBinding.roomAmenityGroup.visibility = View.GONE
            return
        }

        dataBinding.roomAmenityGroup.visibility = View.VISIBLE
        dataBinding.roomAmenityGridView.setTitleText(R.string.label_stay_room_amenity_title)
        dataBinding.roomAmenityGridView.setColumnCount(1)
        dataBinding.roomAmenityGridView.setData(DailyRoomInfoGridView.ItemType.DOT, list)
    }

    private fun setRoomChargeInformatinoView(dataBinding: ListRowStayRoomInvisibleLayoutDataBinding, info: Room.ChargeInformation?) {
        if (info == null) {
            dataBinding.extraChargeLayout.visibility = View.GONE
            return
        }

        if (!info.extraPersonInformationList.isNotNullAndNotEmpty() && info.extraInformation == null && info.consecutiveInformation == null) {
            dataBinding.extraChargeLayout.visibility = View.GONE
            return
        }

        if (info.extraPersonInformationList.isNotNullAndNotEmpty()) {
            dataBinding.extraChargePersonTableLayout.visibility = View.GONE
        } else {
            dataBinding.extraChargePersonTableLayout.visibility = View.VISIBLE

            dataBinding.extraChargePersonTableLayout.setTitleText(R.string.label_stay_room_extra_charge_person_title)
            dataBinding.extraChargePersonTableLayout.setTitleVisible(true)
            dataBinding.extraChargePersonTableLayout.clearTableLayout()

            info.extraPersonInformationList.forEach {
                var title = it.title

                listAdapter.getPersonRangeText(it.minAge, it.maxAge).letNotEmpty { title += " ($it)" }

                val subDescription = if (it.maxPersons > 0) context.resources.getString(R.string.label_room_max_person_range_format, it.maxPersons) else ""

                dataBinding.extraChargePersonTableLayout.addTableRow(title, listAdapter.getExtraChargePrice(it.amount), subDescription)
            }
        }

        if (info.extraInformation == null) {
            dataBinding.extraChargeBedTableLayout.visibility = View.GONE
            dataBinding.extraChargeDescriptionGridView.visibility = View.GONE
        } else {
            dataBinding.extraChargeBedTableLayout.visibility = View.VISIBLE

            dataBinding.extraChargeBedTableLayout.setTitleVisible(true)
            dataBinding.extraChargeBedTableLayout.setTitleText(R.string.label_stay_room_extra_charge_bed_title)
            dataBinding.extraChargeBedTableLayout.clearTableLayout()

            (info.extraInformation.extraBeddingEnable).runTrue {
                dataBinding.extraChargeBedTableLayout.addTableRow(context.resources.getString(R.string.label_bedding), listAdapter.getExtraChargePrice(info.extraInformation.extraBedding))
            }

            (info.extraInformation.extraBedEnable).runTrue {
                dataBinding.extraChargeBedTableLayout.addTableRow(context.resources.getString(R.string.label_extra_bed), listAdapter.getExtraChargePrice(info.extraInformation.extraBed))
            }

            dataBinding.extraChargeBedTableLayout.visibility = if (listAdapter.itemCount == 0) View.GONE else View.VISIBLE

            dataBinding.extraChargeDescriptionGridView.setColumnCount(1)
            dataBinding.extraChargeDescriptionGridView.setTitleVisible(false)
            dataBinding.extraChargeDescriptionGridView.setData(DailyRoomInfoGridView.ItemType.DOT, info.extraInformation.descriptionList)
        }

        if (info.consecutiveInformation == null || !info.consecutiveInformation.enable) {
            dataBinding.extraChargeNightsTableLayout.visibility = View.GONE
        } else {
            dataBinding.extraChargeNightsTableLayout.visibility = View.VISIBLE

            dataBinding.extraChargeNightsTableLayout.setTitleVisible(true)
            dataBinding.extraChargeNightsTableLayout.setTitleText(R.string.label_stay_room_extra_charge_bed_title)
            dataBinding.extraChargeNightsTableLayout.clearTableLayout()

            dataBinding.extraChargeNightsTableLayout.addTableRow(context.resources.getString(R.string.label_stay_room_extra_charge_consecutive_item_title), listAdapter.getExtraChargePrice(info.consecutiveInformation.charge))
        }
    }

//    private fun getPersonRangeText(minAge: Int, maxAge: Int): String {
//        return if (minAge == -1 && maxAge == -1) {
//            ""
//        } else if (minAge != -1 && maxAge != -1) {
//            context.resources.getString(R.string.label_person_age_range_format, minAge, maxAge)
//        } else if (minAge != -1) {
//            context.resources.getString(R.string.label_person_age_and_over_format, minAge)
//        } else {
//            context.resources.getString(R.string.label_person_age_under_format, maxAge)
//        }
//    }
//
//    private fun getExtraChargePrice(price: Int): String {
//        if (price <= 0) {
//            return context.resources.getString(R.string.label_free)
//        }
//
//        return DailyTextUtils.getPriceFormat(context, price, false)
//    }

    private fun setNeedToKnowInformationView(dataBinding: ListRowStayRoomInvisibleLayoutDataBinding, needToKnowList: MutableList<String>?) {
        if (needToKnowList == null || needToKnowList.size == 0) {
            dataBinding.roomCheckInfoGroup.visibility = View.GONE
            return
        }

        dataBinding.roomCheckInfoGroup.visibility = View.VISIBLE

        dataBinding.roomCheckInfoGridView.setTitleText(R.string.label_stay_room_need_to_know_title)
        dataBinding.roomCheckInfoGridView.setColumnCount(1)
        dataBinding.roomCheckInfoGridView.setData(DailyRoomInfoGridView.ItemType.DOT, needToKnowList)
    }

    private fun setInvisibleLayout(increasing: Boolean, preY: Float, y: Float) {
        if (preY == y) {
            return
        }

        val gap = y - preY // plus 값이면 상단으로 올림, minus 값이면 하단으로 내림

        val horizontalRatio = mTouchHorizontalMargin.toFloat() / mTouchVerticalMargin.toFloat()
        val horizontalGap = (if (increasing) mTouchHorizontalMargin else 0) + gap * horizontalRatio
        val horizontal: Int
        if (horizontalGap > 0) {
            horizontal = if (horizontalGap > mTouchHorizontalMargin) mTouchHorizontalMargin else Math.round(horizontalGap)
        } else {
            horizontal = 0
        }

        val vertical: Float
        val verticalGap = (if (increasing) mTouchVerticalMargin else 0) + gap
        vertical = if (verticalGap > 0) {
            if (verticalGap > mTouchVerticalMargin) mTouchVerticalMargin.toFloat() else verticalGap
        } else {
            0f
        }

        viewDataBinding.invisibleLayout!!.nestedScrollView.setPadding(horizontal, vertical.toInt(), horizontal, 0)

        if (increasing && verticalGap <= 0) {
            viewDataBinding.invisibleLayout!!.nestedScrollView.scrollY = (-verticalGap).toInt()
        }
    }

    private fun setInvisibleLayoutAnimation() {

        val view = viewDataBinding.invisibleLayout!!.nestedScrollView

        val scaleUp = view.paddingTop < mTouchVerticalMargin / 2
        val end = if (scaleUp) 0 else mTouchVerticalMargin
        val horizontalRatio = mTouchHorizontalMargin.toFloat() / mTouchVerticalMargin.toFloat()

        val duration = 200

        ExLog.d("sam - params.topMargin : " + view.paddingTop + " , end : " + end)
        val animator = ValueAnimator.ofInt(view.paddingTop, end)
        animator.duration = duration.toLong()
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            val horizontal = Math.round(value * horizontalRatio)

            ExLog.d("sam - value : $value , horizontal : $horizontal")

            viewDataBinding.invisibleLayout!!.nestedScrollView.setPadding(horizontal, value, horizontal, 0)
        }

        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {
                if (scaleUp) {
                    viewDataBinding.invisibleLayout!!.nestedScrollView.visibility = View.VISIBLE
                } else {
                    viewDataBinding.invisibleLayout!!.nestedScrollView.visibility = View.GONE
                }

                setRecyclerScrollEnabled()
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {

            }
        })

        animator.start()
    }

    override fun setGuideVisible(visible: Boolean) {
        viewDataBinding.guideLayout.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun hideGuideAnimation(): Observable<Boolean> {
        val objectAnimator = ObjectAnimator.ofFloat(viewDataBinding.guideLayout, "alpha", 1.0f, 0.0f)

        objectAnimator.interpolator = LinearInterpolator()
        objectAnimator.duration = 300

        return object : Observable<Boolean>() {
            override fun subscribeActual(observer: Observer<in Boolean>) {
                objectAnimator.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animator: Animator) {}

                    override fun onAnimationEnd(animator: Animator) {
                        objectAnimator.removeAllListeners()

                        viewDataBinding.guideLayout.visibility = View.GONE

                        observer.onNext(true)
                        observer.onComplete()
                    }

                    override fun onAnimationCancel(animator: Animator) {}

                    override fun onAnimationRepeat(animator: Animator) {}
                })

                objectAnimator.start()
            }
        }
    }

    override fun showVrDialog(checkedChangeListener: CompoundButton.OnCheckedChangeListener
                              , positiveListener: View.OnClickListener
                              , onDismissListener: DialogInterface.OnDismissListener) {
        showSimpleDialog(null
                , getString(R.string.message_stay_used_data_guide)
                , getString(R.string.label_dont_again)
                , getString(R.string.dialog_btn_do_continue)
                , getString(R.string.dialog_btn_text_close)
                , checkedChangeListener, positiveListener
                , null, null, onDismissListener, true)
    }
}