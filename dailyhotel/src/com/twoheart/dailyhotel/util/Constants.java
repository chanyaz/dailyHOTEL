/**
 * Copyright (c) 2014 Daily Co., Ltd. All rights reserved.
 *
 * Constants (���ø����̼� ���� ���)
 * 
 * ���ø����̼ǿ��� ���Ǵ� ���� ������� �����س��� �������̽��̴�. ���ø�
 * ���̼ǿ��� ���Ǵ� ���� ������� ���� ������ �����̸� ���� �κп��� ��
 * �������� ���ǹǷ� ����μ� ����ƴ�. �� �������̽��� �� Ŭ�������� ���
 * �޾Ƽ� �ٷ� ���� �� �ִ�.
 *
 * @since 2014-02-24
 * @version 1
 * @author Mike Han(mike@dailyhotel.co.kr)
 */
package com.twoheart.dailyhotel.util;

import com.twoheart.dailyhotel.BuildConfig;

public interface Constants {

	// ����� ���� ����
	public static final boolean DEBUG = BuildConfig.DEBUG;
	
	// �÷��� ������ ������ ����
	public static final boolean IS_GOOGLE_RELEASE = true;
	
	// ������ ȣ��Ʈ 
	public static final String URL_DAILYHOTEL_SERVER = "http://dailyhotel.kr/goodnight/";				// ���� ����
//	public static final String URL_DAILYHOTEL_SERVER = "http://dailyhotel.cafe24.com/goodnight/";		// ���� ����
	 
	// ȸ�� ��ǥ��ȣ
	public static final String PHONE_NUMBER_DAILYHOTEL = "1800-9120";
	
	// ȣ�� �򰡸� ǥ���� �ִ� ��¥
	public static final int DAYS_DISPLAY_RATING_HOTEL_DIALOG = 7;

	// DailyHOTEL User Controller WebAPI URL
	public static final String URL_WEBAPI_USER = "user/";
	public static final String URL_WEBAPI_USER_LOGIN = "user/login/mobile";
	public static final String URL_WEBAPI_USER_LOGOUT = "user/logout/mobile";
	public static final String URL_WEBAPI_USER_INFO = "user/session/myinfo";
	public static final String URL_WEBAPI_USER_BONUS_ALL = "user/session/bonus/all";
	public static final String URL_WEBAPI_USER_BONUS_VAILD = "user/session/bonus/vaild";
	public static final String URL_WEBAPI_USER_LOGIN_FACEBOOK = "user/login/sns/facebook";
	public static final String URL_WEBAPI_USER_SIGNUP = "user/join";
	public static final String URL_WEBAPI_USER_ALIVE = "user/alive";
	public static final String URL_WEBAPI_USER_FORGOTPWD = "user/sendpw/";
	public static final String URL_WEBAPI_USER_FINDRND = "user/findrnd/";
	public static final String URL_WEBAPI_USER_UPDATE = "user/update";
	
	// DailyHOTEL Reservation Controller WebAPI URL
	public static final String URL_WEBAPI_RESERVE_PAYMENT = "reserv/session/req/";
	public static final String URL_WEBAPI_RESERVE_PAYMENT_DISCOUNT = "reserv/session/bonus/";
	public static final String URL_WEBAPI_RESERVE_MINE = "reserv/mine";
	public static final String URL_WEBAPI_RESERVE_SAVED_MONEY = "reserv/bonus";
	public static final String URL_WEBAPI_RESERVE_CHECKIN = "reserv/checkinout/";
	public static final String URL_WEBAPI_RESERVE_REVIEW = "reserv/review/";
	
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
	
	public static final String URL_STORE_GOOGLE_DAILYHOTEL = "market://details?id=com.twoheart.dailyhotel";
	public static final String URL_STORE_T_DAILYHOTEL = "http://tsto.re/0000412421";
	
	public static final String URL_WEB_PRIVACY = "http://policies.dailyhotel.co.kr/privacy/";
	public static final String URL_WEB_TERMS = "http://policies.dailyhotel.co.kr/terms/";
	public static final String URL_WEB_ABOUT = "http://policies.dailyhotel.co.kr/about/";
	
	// Preference
	public static final String NAME_DAILYHOTEL_SHARED_PREFERENCE = "GOOD_NIGHT";

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
	public static final String KEY_PREFERENCE_SKIP_MAX_VERSION = "SKIP_MAX_VERSION";
	
	// region
	public static final String KEY_PREFERENCE_REGION_SELECT = "REGION_SELECT";
	public static final String KEY_PREFERENCE_REGION_INDEX = "REGION_INDEX";
	
	public static final String KEY_PREFERENCE_SHOW_GUIDE = "SHOW_GUIDE";
	
	public static final String KEY_PREFERENCE_HOTEL_NAME = "HOTEL_NAME";
	public static final String KEY_PREFERENCE_HOTEL_SALE_IDX = "HOTEL_SALE_IDX";
	public static final String KEY_PREFERENCE_HOTEL_CHECKOUT = "HOTEL_CHECKOUT";
	public static final String VALUE_PREFERENCE_HOTEL_NAME_DEFAULT = "none";
	public static final int VALUE_PREFERENCE_HOTEL_SALE_IDX_DEFAULT = 1;
	public static final String VALUE_PREFERENCE_HOTEL_CHECKOUT_DEFAULT = "14-04-30-20";
	public static final String KEY_PREFERENCE_USER_IDX = "USER_IDX";

	// Android ������Ʈ ���� �����͸� �ְ���� �� ���Ǵ� ����Ʈ �̸�(Ű)�� ������ ����̴�.
	public static final String NAME_INTENT_EXTRA_DATA_HOTEL = "hotel";
	public static final String NAME_INTENT_EXTRA_DATA_HOTELDETAIL = "hoteldetail";
	public static final String NAME_INTENT_EXTRA_DATA_SALETIME = "saletime";
	public static final String NAME_INTENT_EXTRA_DATA_BOOKING = "booking";
	public static final String NAME_INTENT_EXTRA_DATA_PAY = "pay";
	public static final String NAME_INTENT_EXTRA_DATA_SELECTED_IMAGE_URL = "sel_image_url";
	
	// Android Activity�� Request Code���̴�.
	public static final int CODE_REQUEST_ACTIVITY_HOTELTAB = 1;
	public static final int CODE_REQUEST_FRAGMENT_BOOKINGLIST = 2;
	public static final int CODE_REQUEST_ACTIVITY_LOGIN = 3;
	public static final int CODE_REQUEST_ACTIVITY_PAYMENT = 4;
	public static final int CODE_REQUEST_ACTIVITY_SPLASH = 5;
	public static final int CODE_REQEUST_ACTIVITY_SIGNUP = 6;
	public static final int CODE_REQUEST_ACTIVITY_BOOKING = 7;
	
	// Android Activity�� Result Code���̴�.
	public static final int CODE_RESULT_ACTIVITY_PAYMENT_FAIL = 100;
	public static final int CODE_RESULT_ACTIVITY_PAYMENT_SUCCESS = 101;
	public static final int CODE_RESULT_ACTIVITY_PAYMENT_INVALID_SESSION = 102;
	public static final int CODE_RESULT_ACTIVITY_PAYMENT_SOLD_OUT = 103;
	public static final int CODE_RESULT_ACTIVITY_PAYMENT_COMPLETE = 104;
	public static final int CODE_RESULT_ACTIVITY_PAYMENT_INVALID_DATE = 105;
	public static final int CODE_RESULT_ACTIVITY_PAYMENT_NOT_AVAILABLE = 106;
	public static final int CODE_RESULT_ACTIVITY_PAYMENT_NETWORK_ERROR = 107;
	public static final int CODE_RESULT_ACTIVITY_SPLASH_NEW_EVENT = 108;
	
}
