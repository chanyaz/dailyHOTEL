package com.twoheart.dailyhotel.util;

public interface Constants {

	public static final boolean DEBUG = true;

	public static final String DAILYHOTEL_PHONE_NUMBER = "1800-1225";
	
//	 public static String URL_DAILYHOTEL_SERVER = "http://dailyhotel.kr/goodnight/";
	public static final String URL_DAILYHOTEL_SERVER = "http://dailyhotel.cafe24.com/goodnight/";
	
	// DailyHOTEL User Controller WebAPI URL
	public static final String URL_WEBAPI_USER = "user/";
	public static final String URL_WEBAPI_USER_LOGIN = "user/login/mobile";
	public static final String URL_WEBAPI_USER_LOGOUT = "user/logout";
	public static final String URL_WEBAPI_USER_INFO = "user/session/myinfo";
	public static final String URL_WEBAPI_USER_BONUS_ALL = "user/session/bonus/all";
	public static final String URL_WEBAPI_USER_BONUS_VAILD = "user/session/bonus/vaild";
	public static final String URL_WEBAPI_USER_LOGIN_FACEBOOK = "user/login/sns/facebook";
	public static final String URL_WEBAPI_USER_DEVICE = "user/device";
	public static final String URL_WEBAPI_USER_SIGNUP = "user/join";
	public static final String URL_WEBAPI_USER_ALIVE = "user/alive";
	public static final String URL_WEBAPI_USER_FORGOTPWD = "user/sendpw/";
	
	// DailyHOTEL Reservation Controller WebAPI URL
	public static final String URL_WEBAPI_RESERVE_PAYMENT = "reserv/session/req/";
	public static final String URL_WEBAPI_RESERVE_PAYMENT_DISCOUNT = "reserv/session/bonus/";
	public static final String URL_WEBAPI_RESERVE_MINE = "reserv/mine";
	public static final String URL_WEBAPI_RESERVE_SAVED_MONEY = "reserv/bonus";
	public static final String URL_WEBAPI_RESERVE_CHECKIN = "reserv/checkinout/";
	
	// DailyHOTEL App Management Controller WebAPI URL
	public static final String URL_WEBAPI_APP_VERSION = "common/ver_dual";
	public static final String URL_WEBAPI_APP_LEGAL = "common/regal";
	public static final String URL_WEBAPI_APP_TIME = "time";
	public static final String URL_WEBAPI_APP_SALE_TIME = "common/sale_time";
	
	// DailyHOTEL Hotel Controller WebAPI URL
	public static final String URL_WEBAPI_HOTEL = "hotel/";
	public static final String URL_WEBAPI_HOTEL_DETAIL = "hotel/detail/";
	public static final String URL_WEBAPI_HOTEL_MAP = "hotel/all";
	
	// DailyHOTEL Board Controller WebAPI URL
	public static final String URL_WEBAPI_BOARD_FAQ = "board/json/faq";
	public static final String URL_WEBAPI_BOARD_NOTICE = "board/json/notice";
	
	// DailyHOTEL Site Controller WebAPI URL
	public static final String URL_WEBAPI_SITE_LOCATION_LIST = "site/get";
	
	// Preference
	public static final String NAME_DAILYHOTEL_SHARED_PREFERENCE = "GOOD_NIGHT";

	public static final String KEY_PREFERENCE_GCM = "GCM";
	public static final String KEY_PREFERENCE_RESENT_CNT = "RESENT_CNT";

	// user info
	public static final String KEY_PREFERENCE_AUTO_LOGIN = "AUTO_LOGIN";
	public static final String KEY_PREFERENCE_USER_ID = "USER_ID";
	public static final String KEY_PREFERENCE_USER_PWD = "USER_PWD";
	public static final String KEY_PREFERENCE_USER_ACCESS_TOKEN = "USER_ACCESSTOKEN";

	// version
	public static final String KEY_PREFERENCE_CURRENT_VERSION_NAME = "CURRENT_VERSION_NAME";
	public static final String KEY_PREFERENCE_MIN_VERSION_NAME = "MIN_VERSION_NAME";
	public static final String KEY_PREFERENCE_MAX_VERSION_NAME = "MAX_VERSION_NAME";
	
	// region
	public static final String KEY_PREFERENCE_REGION_SELECT = "REGION_SELECT";
	public static final String KEY_PREFERENCE_REGION_DEFALUT = "REGION_DEFALUT";
	public static final String KEY_PREFERENCE_REGION_INDEX = "REGION_INDEX";

	public static final String NAME_INTENT_EXTRA_DATA_HOTEL = "hotel";
	public static final String NAME_INTENT_EXTRA_DATA_HOTELDETAIL = "hoteldetail";
	public static final String NAME_INTENT_EXTRA_DATA_SALETIME = "saletime";
	public static final String NAME_INTENT_EXTRA_DATA_BOOKING = "booking";
	public static final String NAME_INTENT_EXTRA_DATA_PAY = "pay";
	
	public static final int CODE_REQUEST_ACTIVITY_HOTELTAB = 1;
	public static final int CODE_REQUEST_FRAGMENT_BOOKINGLIST = 3;
	public static final int CODE_REQUEST_ACTIVITY_LOGIN = 4;
	public static final int CODE_REQUEST_ACTIVITY_PAYMENT = 5;
	public static final int CODE_REQUEST_ACTIVITY_SPLASH = 6;
	public static final int CODE_REQEUST_ACTIVITY_SIGNUP = 7;
	
	public static final int CODE_RESULT_ACTIVITY_PAYMENT_FAIL = 7;
	public static final int CODE_RESULT_ACTIVITY_PAYMENT_SUCCESS = 8;
	public static final int CODE_RESULT_ACTIVITY_PAYMENT_INVALID_SESSION = 9;
	public static final int CODE_RESULT_ACTIVITY_PAYMENT_SOLD_OUT = 10;
	public static final int CODE_RESULT_ACTIVITY_PAYMENT_COMPLETE = 11;
	public static final int CODE_RESULT_ACTIVITY_PAYMENT_INVALID_DATE = 12;
	public static final int CODE_RESULT_ACTIVITY_PAYMENT_NOT_AVAILABLE = 13;
	public static final int CODE_RESULT_ACTIVITY_PAYMENT_NETWORK_ERROR = 14;
	public static final int CODE_RESULT_ACTIVITY_SPLASH_NEW_EVENT = 15;
	
	public static final String URL_STORE_GOOGLE_DAILYHOTEL = "market://details?id=com.twoheart.dailyhotel";
	public static final String URL_STORE_T_DAILYHOTEL = "http://tsto.re/0000412421";
	
	public static final String URL_WEB_PRIVACY = "http://policies.dailyhotel.co.kr/privacy/";
	public static final String URL_WEB_TERMS = "http://policies.dailyhotel.co.kr/terms/";
	
}
