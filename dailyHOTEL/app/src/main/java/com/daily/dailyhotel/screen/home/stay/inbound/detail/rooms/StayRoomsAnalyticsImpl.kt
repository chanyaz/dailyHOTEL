package com.daily.dailyhotel.screen.home.stay.inbound.detail.rooms

import com.twoheart.dailyhotel.util.analytics.AnalyticsManager

class StayRoomsAnalyticsImpl : StayRoomsInterface.AnalyticsInterface {
    override fun onScreen(activity: StayRoomsActivity) {
        AnalyticsManager.getInstance(activity).recordScreen(activity, AnalyticsManager.Screen.DAILYHOTEL_ROOM_DETAIL_VIEW, null)
    }

    override fun onScrolled(activity: StayRoomsActivity, stayIndex: Int, roomIndex: Int) {
        AnalyticsManager.getInstance(activity).recordEvent(AnalyticsManager.Category.DETAIL_VIEW_ROOM, "room_type_swipe", stayIndex.toString(), null)
    }

    override fun onBookingClick(activity: StayRoomsActivity, stayIndex: Int, roomIndex: Int) {
        AnalyticsManager.getInstance(activity).recordEvent(AnalyticsManager.Category.DETAIL_VIEW_ROOM, "booking_clicked", stayIndex.toString(), null)
    }
}