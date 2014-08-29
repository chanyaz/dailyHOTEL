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

import com.google.analytics.tracking.android.Logger.LogLevel;
import com.twoheart.dailyhotel.BuildConfig;

public interface Constants {

	// ����� ���� ����
	public static final boolean DEBUG = BuildConfig.DEBUG;

	// ����� ����.
	enum Stores { PLAY_STORE, T_STORE, N_STORE };
	public static final Stores RELEASE_STORE = Stores.N_STORE; 

	// �׻� ������ ���� ����
	public static final boolean ALWAYS_OPEN = false;

    public static final String GCM_PROJECT_NUMBER = "1025681158000";
    public static final String GCM_DEVICE_TYPE_ANDROID = "0"; // GCM ����� �� �� API�� deviceType, 0�� �ȵ���̵� 1�� ������.

	// ������ ȣ��Ʈ 
		public static final String URL_DAILYHOTEL_SERVER = "http://restful.dailyhotel.kr/goodnight/"; //  ���� ����
//		public static final String URL_DAILYHOTEL_SERVER = "http://dev.dailyhotel.kr/goodnight/";				// ���� ���� ���꼭��
		
		
//		public static final String URL_DAILYHOTEL_SERVER = "http://dailyhotel.kr/goodnight/";				// ���� ����
    	
//		public static final String URL_DAILYHOTEL_SERVER = "http://1.234.22.96/goodnight/"; // new ���� 7.6 �׶󼭹�
    	
//    	public static final String URL_DAILYHOTEL_SERVER = "http://newinsik.cafe24.com/goodnight/"; // new ����
//		public static final String URL_DAILYHOTEL_SERVER = "http://dailyhotel.cafe24.com/goodnight/";		// ���� ����
    	
//		public static final String URL_DAILYHOTEL_SERVER = "http://was1.dailyhotel.kr/goodnight/";					// new ���� ����
//		public static final String URL_DAILYHOTEL_SERVER = "http://1.234.83.117/goodnight/";							// new ���� ����
 
		
	// ȸ�� ��ǥ��ȣ
	public static final String PHONE_NUMBER_DAILYHOTEL = "1800-9120";
	
	// uiLock�� ���� API�� ���Ͽ��µ� ���� �ð� �ȿ� ������ ���� ���Ѱ��. error �߻�.
	public static final int REQUEST_EXPIRE_JUDGE = 5000; 

	// �����÷��� ���� ���� Ȯ�� Ÿ�Ӿƿ�
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 5000;
    
	// Volley�� �ִ� retry Ƚ��,  ���⼭ 0�� ������Ʈ�� ��Ʈ���� ���� ������ ����.
	public static final int REQUEST_MAX_RETRY = 0; 

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
	public static final String URL_WEBAPI_USER_UPDATE_FACEBOOK = "user/session/facebook/update";
	public static final String URL_WEBAPI_USER_SIGNUP = "user/join";
	public static final String URL_WEBAPI_USER_ALIVE = "user/alive";
	public static final String URL_WEBAPI_USER_FORGOTPWD = "user/sendpw/";
	public static final String URL_WEBAPI_USER_FINDRND = "user/findrnd/";
	public static final String URL_WEBAPI_USER_UPDATE = "user/update";

	// DailyHOTEL Reservation Controller WebAPI URL
	public static final String URL_WEBAPI_RESERVE_PAYMENT = "reserv/session/req/";
	public static final String URL_WEBAPI_RESERVE_PAYMENT_DISCOUNT = "reserv/session/bonus/";
	public static final String URL_WEBAPI_RESERVE_MINE = "reserv/mine";
	public static final String URL_WEBAPI_RESERVE_MINE_DETAIL = "reserv/mine/detail";
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
	public static final String URL_STORE_N_DAILYHOTEL = "market://details?id=com.twoheart.dailyhotel";

	public static final String URL_WEB_PRIVACY = "http://policies.dailyhotel.co.kr/privacy/";
	public static final String URL_WEB_TERMS = "http://policies.dailyhotel.co.kr/terms/";
	public static final String URL_WEB_ABOUT = "http://policies.dailyhotel.co.kr/about/";

	// Payment App GoogleStore URL
	public static final String URL_STORE_PAYMENT_ISP = "market://details?id=kvp.jjy.MispAndroid320";
	public static final String URL_STORE_PAYMENT_KFTC = "market://details?id=com.kftc.bankpay.android&hl=ko";
	public static final String URL_STORE_PAYMENT_MPOCKET = "market://details?id=kr.co.samsungcard.mpocket";
	
	// Gcm Server URL
	public static final String URL_GCM_REGISTER = "user/notification/register";

	// Payment App PackageName
	public static final String PACKAGE_NAME_ISP = "kvp.jjy.MispAndroid";
	public static final String PACKAGE_NAME_KFTC = "com.kftc.bankpay.android"; 
	public static final String PACKAGE_NAME_MPOCKET = "kr.co.samsungcard.mpocket"; 

	// Preference
	public static final String NAME_DAILYHOTEL_SHARED_PREFERENCE = "GOOD_NIGHT";

	public static final String KEY_PREFERENCE_RESENT_CNT = "RESENT_CNT";

	// user info
	public static final String KEY_PREFERENCE_AUTO_LOGIN = "AUTO_LOGIN";
	public static final String KEY_PREFERENCE_USER_ID = "USER_ID";
	public static final String KEY_PREFERENCE_USER_PWD = "USER_PWD";
	public static final String KEY_PREFERENCE_USER_ACCESS_TOKEN = "USER_ACCESSTOKEN";
    public static final String KEY_PREFERENCE_GCM_ID = "PUSH_ID";

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
	public static final String KEY_PREFERENCE_USER_IDX = "USER_IDX"; // ���� ���������� ���� �����, �̸��� �뵵�� ���� ���� -> ���� �ڵ�
	
	public static final String KEY_PREFERENCE_ACCOUNT_READY_FLAG = "ACCOUNT_READY_FLAG"; //

	// Android ������Ʈ ���� �����͸� �ְ���� �� ���Ǵ� ����Ʈ �̸�(Ű)�� ������ ����̴�.
	public static final String NAME_INTENT_EXTRA_DATA_HOTEL = "hotel";
	public static final String NAME_INTENT_EXTRA_DATA_HOTELDETAIL = "hoteldetail";
	public static final String NAME_INTENT_EXTRA_DATA_SALETIME = "saletime";
	public static final String NAME_INTENT_EXTRA_DATA_REGION = "region";
	public static final String NAME_INTENT_EXTRA_DATA_BOOKING = "booking";
	public static final String NAME_INTENT_EXTRA_DATA_PAY = "pay";
	public static final String NAME_INTENT_EXTRA_DATA_SELECTED_IMAGE_URL = "sel_image_url";
	public static final String NAME_INTENT_EXTRA_DATA_IS_INTENT_FROM_PUSH = "is_intent_from_push";
	public static final String NAME_INTENT_EXTRA_DATA_PUSH_MSG = "push_msg";

	// Android Activity�� Request Code���̴�.
	public static final int CODE_REQUEST_ACTIVITY_HOTELTAB = 1;
	public static final int CODE_REQUEST_FRAGMENT_BOOKINGLIST = 2;
	public static final int CODE_REQUEST_ACTIVITY_LOGIN = 3;
	public static final int CODE_REQUEST_ACTIVITY_PAYMENT = 4;
	public static final int CODE_REQUEST_ACTIVITY_SPLASH = 5;
	public static final int CODE_REQEUST_ACTIVITY_SIGNUP = 6;
	public static final int CODE_REQUEST_ACTIVITY_BOOKING = 7;
	public static final int CODE_REQUEST_ACTIVITY_INTRO = 8;
	public static final int CODE_REQUEST_ISPMOBILE = 9;
	public static final int CODE_REQUEST_KFTC_BANKPAY = 10;
	public static final int CODE_REQUEST_ACTIVITY_BOOKING_DETAIL = 11;

	// Android Activity�� Result Code���̴�.
	public static final int CODE_RESULT_ACTIVITY_PAYMENT_FAIL = 100;
	public static final int CODE_RESULT_ACTIVITY_PAYMENT_SUCCESS = 101;
	public static final int CODE_RESULT_ACTIVITY_PAYMENT_INVALID_SESSION = 102; // 
	public static final int CODE_RESULT_ACTIVITY_PAYMENT_SOLD_OUT = 103; // ���ǵǾ�����.
	public static final int CODE_RESULT_ACTIVITY_PAYMENT_COMPLETE = 104;
	public static final int CODE_RESULT_ACTIVITY_PAYMENT_INVALID_DATE = 105; 
	public static final int CODE_RESULT_ACTIVITY_PAYMENT_NOT_AVAILABLE = 106;
	public static final int CODE_RESULT_ACTIVITY_PAYMENT_NETWORK_ERROR = 107;
	public static final int CODE_RESULT_ACTIVITY_SPLASH_NEW_EVENT = 108;
	public static final int CODE_RESULT_ACTIVITY_PAYMENT_CANCELED = 109;
	public static final int CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_READY = 110;
	public static final int CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_TIME_ERROR = 111;
	public static final int CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_DUPLICATE = 112;
	public static final int CODE_RESULT_ACTIVITY_PAYMENT_TIMEOVER = 113;
	public static final int CODE_RESULT_ACTIVITY_PAYMENT_SALES_CLOSED = 114; // ������ �Ϸ� ��ư�� �����µ� �ֹ� �ð��� �������.
	public static final int CODE_RESULT_ACTIVITY_EXPIRED_PAYMENT_WAIT = 201;
	
	// ���� ����Ʈ���� 
	public static final int CODE_PAY_TYPE_CARD_COMPLETE = 10;
	public static final int CODE_PAY_TYPE_ACCOUNT_WAIT = 20;
	public static final int CODE_PAY_TYPE_ACCOUNT_COMPLETE = 21;

	// Android Google Analytics ������.
	public static final String GA_PROPERTY_ID = "UA-43721645-1";

	// Dispatch period in seconds.
	public static final int GA_DISPATCH_PERIOD = 30;

	// Prevent hits from being sent to reports, i.e. during testing.
	public static final boolean GA_IS_DRY_RUN = false;

	// GA Logger verbosity.
	public static final LogLevel GA_LOG_VERBOSITY = LogLevel.INFO;

	// Key used to store a user's tracking preferences in SharedPreferences.
	public static final String TRACKING_PREF_KEY = "trackingPreference";
	
	// GA E-Commerce Constants
	public static final String GA_COMMERCE_DEFAULT_AFFILIATION = "DailyHOTEL";
	public static final Double GA_COMMERCE_DEFAULT_TAX = 0d;
	public static final Double GA_COMMERCE_DEFAULT_SHIPPING = 0d;
	public static final String GA_COMMERCE_DEFAULT_CURRENCY_CODE = "KRW";
	public static final String GA_COMMERCE_DEFAULT_SKU = "1";
	public static final Long GA_COMMERCE_DEFAULT_QUANTITY = 1L;

	// GA Event Constants
	public static final String GA_SIGNUP_EVENT_CATEGORY = "Signup";
	public static final String GA_SIGNUP_EVENT_ACTION = "SignupComplete";
	public static final String GA_SIGNUP_EVENT_LABEL = "SignupComplete";
	public static final Long GA_SIGNUP_EVENT_VALUE = 1L;
	
	public static final String GA_PURCHASE_EVENT_CATEGORY = "Purchase";
	public static final String GA_PURCHASE_EVENT_ACTION = "PurchaseComplete";
	public static final String GA_PURCHASE_EVENT_LABEL = "PurchaseComplete";
	public static final Long GA_PURCHASE_EVENT_VALUE = 1L;

}
