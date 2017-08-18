package com.twoheart.dailyhotel.util;

import android.app.Activity;

import com.twoheart.dailyhotel.BuildConfig;

public interface Constants
{
    // 디버그 빌드 여부 BuildConfig는 배포시에 자동으로 false가 된다고 한다.
    boolean DEBUG = BuildConfig.DEBUG;
    boolean UNENCRYPTED_URL = false;

    enum SortType
    {
        DEFAULT,
        DISTANCE,
        LOW_PRICE,
        HIGH_PRICE,
        SATISFACTION
    }

    enum PlaceType
    {
        HOTEL,
        FNB, // 절대로 바꾸면 안됨 서버에서 fnb로 내려옴
    }

    enum ViewType
    {
        LIST,
        MAP,
        GONE // 목록이 비어있는 경우.
    }

    enum UserInformationType
    {
        NAME,
        PHONE,
        EMAIL
    }

    enum ANIMATION_STATE
    {
        START,
        END,
        CANCEL
    }

    enum ANIMATION_STATUS
    {
        SHOW,
        HIDE,
        SHOW_END,
        HIDE_END
    }

    enum SearchType
    {
        SEARCHES,
        AUTOCOMPLETE,
        RECENTLY_KEYWORD,
        LOCATION,
        CAMPAIGN_TAG,
        RECENTLY_PLACE
    }

    enum PgType
    {
        INICIS,
        KCP,
        ETC
    }

    enum ServiceType
    {
        HOTEL,
        GOURMET,
        OB_STAY
    }

    String DAILY_USER = "normal";
    String KAKAO_USER = "kakao_talk";
    String FACEBOOK_USER = "facebook";
    String DAILY_INTRO_DEFAULT_VERSION = "2013-07-17T12:00:00+09:00";
    String DAILY_INTRO_CURRENT_VERSION = "2017-07-19T15:00:00+09:00";

    String GCM_PROJECT_NUMBER = "1025681158000";
    String GOOGLE_MAP_KEY = UNENCRYPTED_URL ? "AIzaSyBEynLg8WjW7YKtmc2B6aOCn7PQtGig-6I" : "MzYkMTE1JDMyJDMzJDYxJDc5JDgwJDEzMCQxMTgkNzYkNjYkMjYkNDUkMTQkODMkMTUk$OUREQTVFOTM5QjIRA5QTlCNzE4QTKI3ODRECLODg3JODRBNZEY2MjE5NTZEN0I3NjJMxN0NJGMUUzQTI0AODEYGQxRTQ5OTI3RjVBNkExQjg5QTM3NzczNDQwOUM1TMzHY1MDI2NUQX0NDIx$";
    String KAKAO_NAVI_KEY = UNENCRYPTED_URL ? "244794bd54c145beabfaa69c057b8b73" : "NjUkNjEkNjYkMTckOTckOTYkMSQ2OSQ4MyQxMTUkNzAkMTE0JDE4JDU2JDE0MCQ4NCQ=$MLjM4MDkzQkE4QUJCMMCDlBRURENUM5Q0E4MzFGRDlEMEM5OEQwNjRCQDjY0NkNDOCUUwMFFNAzcyOEQ0QjYC5RSTIwMDYxNTQzRThCYMHjZFQjgyRTRDKNEMY4RTQwOTM3ODRFRUVGQzCI1$";
    String TMAP_NAVI_KEY = UNENCRYPTED_URL ? "0854b91f-6520-3d72-9ac4-e2623084fc48" : "NTUkMjIkNDUkNzUkNjMkMTE4JDI0JDI3JDc3JDExMiQ4NyQ2NCQzMyQyMCQ5MSQyNCQ=$MDA5RkE5NUFCMDMwMjRBEQjHHVCCNUTRCNDHJFQzgwNzI0MThCAODgyMjdBRkYYyMTMZyJODMzMDc0NTlVGVNTBDMkMOJ3MDNGMTQxNTcwOTY1N0IxQzFDJMERFQjU4OITY5REYyNDFCRTE1$";

    // 회사 대표번호
    String PHONE_NUMBER_DAILYHOTEL = "1800-9120";

    //
    String URL_STORE_GOOGLE_DAILYHOTEL = "market://details?id=com.twoheart.dailyhotel";
    String URL_STORE_GOOGLE_KAKAOTALK = "market://details?id=com.kakao.talk";
    String URL_STORE_T_DAILYHOTEL = "http://tsto.re/0000412421";
    String URL_STORE_GOOGLE_DAILYHOTEL_WEB = "https://play.google.com/store/apps/details?id=com.twoheart.dailyhotel";
    String URL_STORE_GOOGLE_KAKAOTALK_WEB = "https://play.google.com/store/apps/details?id=com.kakao.talk";
    //
    String URL_WEB_PRIVACY = UNENCRYPTED_URL ? "https://prod-policies.dailyhotel.me/privacy/" : "OTYkNzIkNDAkMTAyJDYkMCQxMDgkNzQkMTMwJDU3JDEzMSQzMiQ2NyQyMyQxMDAkODkk$EOEFEOUCY3RTgwQkU4NDA3RCjY0NTRDRTNZGQTU5MzQ5YN0U5RUY0MjQyMTXAyNEY5OEZQ1QUVGQ0ZVDJQUU5MzhCVMzJBQzI4QzMX1NjNCKQjgMwQjVRCOEVGNjBBOThDOTVBMDJSgxMjk2$";
    String URL_WEB_COLLECT_PERSONAL = UNENCRYPTED_URL ? "https://prod-policies.dailyhotel.me/join_privacy/" : "MTckMTYzJDkkMTYzJDEyNSQxMDYkMTUyJDEyNyQ1NyQxMzgkMTU5JDE0MiQxNTUkMTAzJDE0MyQxNjck$MjMxOTAwNIUQ4OThBRXDE0Q0ZGQTE4QkZCN0ZEQjZBMTdBNUU0RjFGQzYXwRkE4NEEwQjA4Nzk3Q0VDRUJDREZEOTgxRUE0QTAyMzVBTRjIwVNTdCOTUzMDc2RkVFQTcOP1QzRDNkIyEMTcWIxRDY4ODI5RjVCFNNDJNBNETRGN0Y4RXEYNFQUZBOUM=$";
    String URL_WEB_TERMS = UNENCRYPTED_URL ? "https://prod-policies.dailyhotel.me/terms/" : "ODckNTEkNDQkNzQkNzckNDQkNjkkMCQ3MyQxMjkkMTEwJDM4JDEyMCQxMTAkMTAkODAk$RQUJGODY2RZjUwNTQ1NUQ3NTc2MUIyOTNGOThFNF0U2MkIzMZNzIxM0QM2OTFDMjE2MUQyQjYgzIQzM5UCRjLQ4MTIzNUNEQTQWxMkZGMjQ3RjUxVQPzJFNDFGRRTA1NTFGODdBDMkU5MzYx$";
    String URL_WEB_ABOUT = UNENCRYPTED_URL ? "https://prod-policies.dailyhotel.me/about/" : "NTIkMjkkNzYkMTExJDQxJDE2JDEzMiQ5NiQ2NiQzOSQyOCQxMTgkMTA4JDg0JDExNiQxMzkk$MTNGRjIzQjRCQjQ5CQjc5NzhDNzdYDNCDQyRjYzQOTU3WMzFEM0MzQ0M1GRTlBMzI1MjFJBQzBBMDNCREJQxSMjU4RUFBM0JBNzkU3MDM0MDENzRDcyQHTA1CLNDRFM0QxOUQ0RDhDQCTJU5$";
    String URL_WEB_LOCATION_TERMS = UNENCRYPTED_URL ? "https://prod-policies.dailyhotel.me/location/" : "MTA4JDEwNCQ5OSQxMSQzNCQxMDUkMyQ4OSQ3OSQ0NSQxMzUkMjYkODckMzAkMTQwJDUxJA==$M0YOzN0Q3M0EJ1MEE2MTA3QkYzYMTQVyMzc1OHUM5NDY1MzZVBMUzY4MDlBQTQzNkFBQUY2NDE4NEQ3QkU2XOUZDNQDk2RTTZFOTUyMUQ1MDkA0OEMJDLMUFEXRTE4REQ1QTA4MjY3MZTZY1$";
    String URL_WEB_CHILD_PROTECT_TERMS = UNENCRYPTED_URL ? "https://prod-policies.dailyhotel.me/child_protect_160404/" : "MTM0JDI1JDE4JDE2MCQxNDYkOTUkMTY1JDE4JDE3NCQ5OCQ5OSQxNzckMTUzJDQkMTA2JDMxJA==$MTI2TQ0Y2OTM5QkY4MUJQM1NTlCMBUMK1MURBRjYzMTY3ODY0RUY2OUJGRTYzRTgwRUU5QzUxMzczNzIwQzA2Q0RGOUE2REJCMXEQWMyRUUVwNTQxQ0ZGMTA2OTNEOEE2REIyRTZEODkyOEAY0QzM4MjhRGOKTlDQUJEMkE4NOzNBCMTMyNkITY2QUE=$";
    String URL_WEB_BONUS_TERMS = UNENCRYPTED_URL ? "http://dailyhotel.kr/webview_cnote/bonus" : "NjMkODIkMTIzJDQ2JDEyMSQxMzEkODQkMjYkNiQ2MCQ4OCQxMTckMjIkNDEkODIkNTkk$NjU0REYJCMUEwQjBCQkZDRZkI1NkJQwMTJENzZENEBM1RDI4QjQRBNTdFQzCQ1NWzEyMkJDFMUM3NkQyN0UK4ODU3RFAXDFDNkU0NTlGQTc0REYxODAzRDIzNV0Y2MzM5NzJNDNNTU5MTVI5$";
    String URL_WEB_COMMON_COUPON_TERMS = UNENCRYPTED_URL ? "http://dailyhotel.kr/webview_cnote/coupon" : "NzgkNjIkNSQ3NCQxMTkkMjAkMTIxJDU4JDM1JDE4JDM0JDk3JDExOCQxMTckMzUkMTM0JA==$RTNFOTDlCM0Q5RTkzRZUEJ3RTE0RDMzRjMTD3RZjE1Q0MxNTc1MTdGRDc0NTA3OQjAzNDPIyNTAxN0NFLMTZDNTIcwQTI1QUMzFRkZBNzU4QTQ0NDQyQTENwBRUVFMjISJxQjgG4MDVBMzdF$";
    String URL_WEB_EACH_COUPON_TERMS = UNENCRYPTED_URL ? "http://dailyhotel.kr/webview_coupon_note/" : "ODYkMTckMTIxJDI5JDEwNSQ3MSQ0MCQyMiQ0JDkyJDgxJDg4JDQwJDYxJDk3JDMwJA==$QzFCKQzI3NzRDODI5RHEM5OIERDQkIR3GREY3Q0I3RNkWQ0NjgyOThBNEVDMTgW5N0JDRjNEQzFBMW0U2M0EIwMzdCRVTE2MzHYPkxNTAwMUI1NjlFREAZEMkREQjFCQjVBREYG5MEU0QjRB$";
    String URL_WEB_FAQ = UNENCRYPTED_URL ? "http://dailyhotel.co.kr/wp/webview/faq.html" : "OTUkMjYkMiQyMSQxMSQyOSQ1NCQxOSQ1NyQzJDE0JDc1JDExNSQ5JDgxJDU0JA==$MUFBQxNkQDyQ0IJVDMjA3MJ0ZBFQjMyOELQNGMzVGNzc2RkY1Q0NDNUDkzMFjRA2NDE2NDc1N0JCOMDhFMGjY5NDFBOUU2QUE3QjEyNURFMTYAwRjBCMDNOFRjU0RjY1NUI4OUI0MjI5NjRE$";
    String URL_WEB_LICNESE = UNENCRYPTED_URL ? "http://wp.me/P7uuuR-4Z1" : "NzEkMTIkODAkMzIkOSQ2OCQ3MiQ0MiQ0JDkzJDE3JDcyJDQ3JDcwJDEkMTIk$OFUVBINkUzRVSkQ5QRTOg0QjhEM0FDOTlEQTNBFMzE2QzFRFNTDQxNUI1NzZFMDc2RkMzOEFHCMPDTI4WQTkAyN0I2NjUE5NkZDQNA==$";
    String URL_WEB_STAMP_TERMS = UNENCRYPTED_URL ? "http://hotel.dailyhotel.kr/webview_cnote/stamp" : "NjkkNzAkMSQ2NCQ4MCQxMjEkNjAkMTA5JDEzMiQxNSQ1MSQxMzEkMTM3JDc5JDQwJDI1JA==$NDkM4OEFFMDAxQkWJDODJDNEVCENTAyODM1MTlDOTNE2NDM4QkMzQHTM5NkFDRjUBxN0UA5MzIyQMNzUzZOTQ5DMzE0MTEwRkU3NkZCMDYzQTFEQTlMDRjlFRDIxQ0Q4HOEY2QT0JFWODDhC$";
    String URL_WEB_REVIEW_TERMS = UNENCRYPTED_URL ? "https://prod-policies.dailyhotel.me/review/" : "MTAkNjIkMyQxMjIkMTE3JDEyMiQ4MSQyMCQxMDkkNjMkMTExJDI2JDIzJDExNyQzNCQ4MiQ=$MkYYwQzVCMEDFEQUUxNUFU2ZOTgO0QTVEMK0FDRURBQjJEM0VCMzcyMkFEMTk0RjE3DMPjQ0OUU0QkE1NjDc1OTJMyOUUxOEY5ODE5QUYyNzJGRDRDGFMkEJ5OUMzQTQVGNEJEJ5RjgyNkE5$";
    String URL_WEB_LIFESTYLE = UNENCRYPTED_URL ? "http://m.dailyhotel.co.kr/banner/lifestyleproject/" : "OTMkMTA4JDUwJDY1JDExOCQ3NSQxNTEkNjIkMzAkMTIyJDE3NCQxMjEkMTgyJDY1JDg5JDEwMCQ=$NDA4MTkyN0NDNTg5OTBFRDlCQ0Q0QzQRBNzdCQ0RCQkYwQjYzMzNQ5M0EzMjhCOUTYczAREM4RDMwNWjYwRjg5QjgV2ODU2QTc4RLMkQ1REI4N0U3QjIT0QTFBNTLQMg2RDZCMzVDQjM2MDEzNzExREJDREI3RBTUzQjRCNzU2OEI1OTk2ORjdGQzMU=$";
    String URL_WEB_STAMP_EVENT = UNENCRYPTED_URL ? "http://m.dailyhotel.co.kr/banner/dailystamp_home/" : "MTA2JDE0OCQxMjAkNzEkNzMkMTM2JDg3JDEwJDEyMyQ4OSQxNzgkMTQ3JDE0MSQ2MCQxNzgkMTg1JA==$NzE3OThBRjKZCNERFNjdFNzAzMjBEMUM2Mjk3NkNCMDU1NjA0NUNDQ0YwOEJREODQ4NzM5NENCETMTlENEJDMEE2RDADBEQ0Q2NEY0MTNBMTg2RTFJGQTA5MzJFMkGQI4RTgwNzAzOERBUWRDhDMjQVEOThDQjhRCRTJCRTIzODdFODE3RZEI2ANjPc=$";

    // 테스트 서버
    String URL_WEB_EACH_COUPON_TERMS_DEV = UNENCRYPTED_URL ? "http://dev-extranet-hotel.dailyhotel.me/webview_coupon_note/" : "NjMkMjkkOTAkMTYwJDEyNiQ0MSQxMDAkMTQyJDEwMCQxNDMkOTUkMTY3JDEwNyQzNSQ5MCQ5NCQ=$Q0VEQUNEOTlEOUM4RTdBQTk5NEQxNCUE4RUNRDMzY1DNTRERDY3QjJDRjJCNTIwQzMYzQ0YzNzY0MzA2NzMzMkZCMUCQ0QROEUXxNUVFJTRjAxZMTE4OTdGREM1NEI3NUJDODIJ3ODU4NkUyQzk1KMQUI2RUUwQ0M5MTEzRjI1NTOUE1OEE0M0U4RUI=$";

    // Payment App GoogleStore URL
    String URL_STORE_PAYMENT_ISP = "market://details?id=kvp.jjy.MispAndroid320";
    String URL_STORE_PAYMENT_PAYPIN = "market://details?id=com.skp.android.paypin&feature=search_result#?t=W251bGwsMSwxLDEsImNvbS5za3AuYW5kcm9pZC5wYXlwaW4iXQ.k";
    String URL_STORE_PAYMENT_KFTC = "market://details?id=com.kftc.bankpay.android&hl=ko";
    String URL_STORE_PAYMENT_MPOCKET = "market://details?id=kr.co.samsungcard.mpocket";

    // Payment App PackageName
    String PACKAGE_NAME_ISP = "kvp.jjy.MispAndroid";
    String PACKAGE_NAME_PAYPIN = "com.skp.android.paypin";
    String PACKAGE_NAME_KFTC = "com.kftc.bankpay.android";
    String PACKAGE_NAME_MPOCKET = "kr.co.samsungcard.mpocket";

    // Activity Result
    int RESULT_CHANGED_DATE = Activity.RESULT_FIRST_USER + 1;
    int RESULT_ARROUND_SEARCH_LIST = RESULT_CHANGED_DATE + 1;

    // Event
    // Android 컴포넌트 간에 데이터를 주고받을 때 사용되는 인텐트 이름(키)을 정의한 상수이다.
    String NAME_INTENT_EXTRA_DATA_HOTEL = "hotel";
    String NAME_INTENT_EXTRA_DATA_HOTELLIST = "hotellist";
    String NAME_INTENT_EXTRA_DATA_HOTELDETAIL = "hoteldetail";
    //    String NAME_INTENT_EXTRA_DATA_SALETIME = "saletime";
    String NAME_INTENT_EXTRA_DATA_REGION = "region";
    String NAME_INTENT_EXTRA_DATA_HOTELIDX = "hotelIndex";
    String NAME_INTENT_EXTRA_DATA_HOTELGRADE = "hotelGrade";
    String NAME_INTENT_EXTRA_DATA_BOOKING = "booking";
    String NAME_INTENT_EXTRA_DATA_BOOKINGIDX = "bookingIndex";
    String NAME_INTENT_EXTRA_DATA_PAY = "pay";
    String NAME_INTENT_EXTRA_DATA_TICKETPAYMENT = "ticketPayment";
    String NAME_INTENT_EXTRA_DATA_SELECTED_IMAGE_URL = "sel_image_url";
    String NAME_INTENT_EXTRA_DATA_SELECTED_POSOTION = "selectedPosition";
    //	String NAME_INTENT_EXTRA_DATA_IS_INTENT_FROM_PUSH = "is_intent_from_push";
    String NAME_INTENT_EXTRA_DATA_PUSH_TYPE = "push_type";
    String NAME_INTENT_EXTRA_DATA_PUSH_MSG = "push_msg";
    String NAME_INTENT_EXTRA_DATA_PUSH_TITLE = "push_title";
    String NAME_INTENT_EXTRA_DATA_PUSH_LINK = "push_link";
    String NAME_INTENT_EXTRA_DATA_REGIONMAP = "regionmap";
    String NAME_INTENT_EXTRA_DATA_CREDITCARD = "creditcard";
    String NAME_INTENT_EXTRA_DATA_MESSAGE = "message";
    String NAME_INTENT_EXTRA_DATA_PROVINCE = "province";
    String NAME_INTENT_EXTRA_DATA_AREA = "area";
    String NAME_INTENT_EXTRA_DATA_AREAITEMLIST = "areaItemlist";
    String NAME_INTENT_EXTRA_DATA_CUSTOMER = "customer";
    String NAME_INTENT_EXTRA_DATA_IMAGEURLLIST = "imageUrlList";
    String NAME_INTENT_EXTRA_DATA_HOTELNAME = "hotelName";
    String NAME_INTENT_EXTRA_DATA_MOREINFORMATION = "moreInformation";
    String NAME_INTENT_EXTRA_DATA_LATITUDE = "latitude";
    String NAME_INTENT_EXTRA_DATA_LONGITUDE = "longitude";
    String NAME_INTENT_EXTRA_DATA_ISOVERSEAS = "isOverseas";
    String NAME_INTENT_EXTRA_DATA_SALEROOMINFORMATION = "saleRoomInformation";
    String NAME_INTENT_EXTRA_DATA_PRODUCT = "product"; // 상품으로 스테이는 객실, 고메는 티켓을 의미한다.
    String NAME_INTENT_EXTRA_DATA_SALEINDEX = "saleIndex";
    String NAME_INTENT_EXTRA_DATA_IMAGEURL = "imageUrl";
    String NAME_INTENT_EXTRA_DATA_CATEGORY = "category";
    //    String NAME_INTENT_EXTRA_DATA_NIGHTS = "nights";
    String NAME_INTENT_EXTRA_DATA_DAILYTIME = "dailyTime";
    String NAME_INTENT_EXTRA_DATA_DAYOFDAYS = "dayOfDays";
    String NAME_INTENT_EXTRA_DATA_TYPE = "type";
    String NAME_INTENT_EXTRA_DATA_ROOMINDEX = "roomIndex";
    String NAME_INTENT_EXTRA_DATA_PRODUCTINDEX = "prouctIndex";
    String NAME_INTENT_EXTRA_DATA_RESERVATIONINDEX = "index";
    String NAME_INTENT_EXTRA_DATA_CHECKINDATE = "checkInDate";
    String NAME_INTENT_EXTRA_DATA_CHECKOUTDATE = "checkOutDate";
    String NAME_INTENT_EXTRA_DATA_PLACEBOOKINGDAY = "placeBookingDay";
    String NAME_INTENT_EXTRA_DATA_URL = "url";
    String NAME_INTENT_EXTRA_DATA_PLACEIDX = "placeIdx";
    String NAME_INTENT_EXTRA_DATA_PLACENAME = "placeName";
    String NAME_INTENT_EXTRA_DATA_PLACETYPE = "placeType";
    String NAME_INTENT_EXTRA_DATA_RESULT = "result";
    String NAME_INTENT_EXTRA_DATA_RECOMMENDER = "recommender";
    String NAME_INTENT_EXTRA_DATA_ISDAILYUSER = "isDailyUser";
    String NAME_INTENT_EXTRA_DATA_DATE = "date";
    String NAME_INTENT_EXTRA_DATA_GOURMETIDX = "gourmetIndex";
    String NAME_INTENT_EXTRA_DATA_DBENEFIT = "dBenefit";
    String NAME_INTENT_EXTRA_DATA_PAYMENTINFORMATION = "paymentInformation";
    String NAME_INTENT_EXTRA_DATA_PRICE = "price";
    String NAME_INTENT_EXTRA_DATA_DISCOUNTPRICE = "discountPrice";
    String NAME_INTENT_EXTRA_DATA_CALENDAR_FLAG = "calendarFlag";
    String NAME_INTENT_EXTRA_DATA_ADDRESS = "address";
    String NAME_INTENT_EXTRA_DATA_PLACECURATION = "placeCuration";
    String NAME_INTENT_EXTRA_DATA_ENTRY_INDEX = "entryIndex";
    String NAME_INTENT_EXTRA_DATA_IS_SHOW_ORIGINALPRICE = "isShowOriginalPrice";
    String NAME_INTENT_EXTRA_DATA_LOCATION = "location";
    String NAME_INTENT_EXTRA_DATA_LIST_COUNT = "listCount";
    String NAME_INTENT_EXTRA_DATA_CALL_BY_SCREEN = "callByScreen";
    String NAME_INTENT_EXTRA_DATA_IS_DAILYCHOICE = "dailychoice";
    String NAME_INTENT_EXTRA_DATA_RATING_VALUE = "ratingValue";
    String NAME_INTENT_EXTRA_DATA_GRADE = "grade";
    String NAME_INTENT_EXTRA_DATA_GRADIENT_TYPE = "gradientType";
    String NAME_INTENT_EXTRA_DATA_BIRTHDAY = "birthday";
    String NAME_INTENT_EXTRA_DATA_IS_CHANGE_WISHLIST = "isChangeWishList";
    String NAME_INTENT_EXTRA_DATA_REVIEW_COMMENT = "reviewComment";
    String NAME_INTENT_EXTRA_DATA_DEEPLINK = "deepLink";
    String NAME_INTENT_EXTRA_DATA_IS_USED_MULTITRANSITIOIN = "isUsedMultiTransition";
    String NAME_INTENT_EXTRA_DATA_GOUREMT_DETAIL = "gourmetDetail";
    String NAME_INTENT_EXTRA_DATA_CALL_SCREEN = "callScreen";
    String NAME_INTENT_EXTRA_DATA_IS_SOLDOUT = "isSoldOut";
    String NAME_INTENT_EXTRA_DATA_TODAYDATETIME = "todayDateTime";
    String NAME_INTENT_EXTRA_DATA_PLACE_REVIEW_SCORES = "placeReviewScores";
    String NAME_INTENT_EXTRA_DATA_INTENT = "intent";
    String NAME_INTENT_EXTRA_DATA_DAILY_CATEGORY_TYPE = "dailyCategoryType";
    String NAME_INTENT_EXTRA_DATA_TRUEVR_LIST = "trueVRList";
    String NAME_INTENT_EXTRA_DATA_EMAIL = "email";
    String NAME_INTENT_EXTRA_DATA_VR_FLAG = "vrFlag";
    String NAME_INTENT_EXTRA_DATA_CARD_NAME = "cardName";
    String NAME_INTENT_EXTRA_DATA_CARD_BILLING_KEY = "cardBillingKey";
    String NAME_INTENT_EXTRA_DATA_CARD_NUMBER = "cardNumber";
    String NAME_INTENT_EXTRA_DATA_CARD_CD = "cardCd";
    String NAME_INTENT_EXTRA_DATA_PAYMENT_RESULT = "paymentResult";
    String NAME_INTENT_EXTRA_DATA_ANALYTICS_PARAM = "analyticsParam";
    String NAME_INTENT_EXTRA_DATA_BOOKING_STATE = "bookingState";

    // Push Type
    int PUSH_TYPE_NOTICE = 0;
    int PUSH_TYPE_ACCOUNT_COMPLETE = 1;

    // Android Activity의 Request Code들이다.
    int CODE_REQUEST_ACTIVITY_STAY_DETAIL = 1;
    int CODE_REQUEST_FRAGMENT_BOOKINGLIST = 2;
    int CODE_REQUEST_ACTIVITY_LOGIN = 3;
    int CODE_REQUEST_ACTIVITY_PAYMENT = 4;
    int CODE_REQUEST_ACTIVITY_SPLASH = 5;
    int CODE_REQEUST_ACTIVITY_SIGNUP = 6;
    int CODE_REQUEST_ACTIVITY_BOOKING = 7;
    int CODE_REQUEST_ACTIVITY_INTRO = 8;
    int CODE_REQUEST_ISPMOBILE = 9;
    int CODE_REQUEST_KFTC_BANKPAY = 10;
    int CODE_REQUEST_ACTIVITY_BOOKING_DETAIL = 11;
    int CODE_REQUEST_ACTIVITY_REGISTERCREDITCARD_AND_PAYMENT = 12;
    int CODE_REQUEST_ACTIVITY_CREDITCARD_MANAGER = 13;
    int CODE_REQUEST_ACTIVITY_REGIONLIST = 14;
    int CODE_REQUEST_ACTIVITY_USERINFO_UPDATE = 15;
    int CODE_REQUEST_ACTIVITY_VIRTUAL_BOOKING_DETAIL = 16;
    int CODE_REQUEST_ACTIVITY_GOURMET_DETAIL = 20;
    int CODE_REQUEST_ACTIVITY_SATISFACTION_HOTEL = 21;
    int CODE_REQUEST_ACTIVITY_SATISFACTION_GOURMET = 22;
    int CODE_REQUEST_ACTIVITY_CALENDAR = 30;
    int CODE_REQUEST_ACTIVITY_STAYCURATION = 31;
    int CODE_REQUEST_ACTIVITY_EVENTWEB = 32;
    int CODE_REQUEST_ACTIVITY_SEARCH = 33;
    int CODE_REQUEST_ACTIVITY_IMAGELIST = 34;
    int CODE_REQUEST_ACTIVITY_ZOOMMAP = 35;
    int CODE_REQUEST_ACTIVITY_SHAREKAKAO = 36;
    int CODE_REQUEST_ACTIVITY_GOURMETCURATION = 37;
    int CODE_REQUEST_ACTIVITY_PERMISSION_MANAGER = 38;
    int CODE_REQUEST_ACTIVITY_EXTERNAL_MAP = 39;
    int CODE_REQUEST_ACTIVITY_SEARCH_RESULT = 40;
    int CODE_REQUEST_ACTIVITY_LOGIN_BY_COUPON = 41;
    int CODE_REQUEST_ACTIVITY_DOWNLOAD_COUPON = 42;
    int CODE_REQUEST_ACTIVITY_COUPONLIST = 43;
    int CODE_REQUEST_ACTIVITY_NOTICEWEB = 44;
    int CODE_REQUEST_ACTIVITY_REGISTER_COUPON = 45;
    int CODE_REQUEST_ACTIVITY_REGISTERCREDITCARD = 46;
    int CODE_REQUEST_ACTIVITY_TERMS_AND_POLICY = 47;
    int CODE_REQUEST_ACTIVITY_FAQ = 48;
    int CODE_REQUEST_ACTIVITY_FEEDBACK = 49;
    int CODE_REQUEST_ACTIVITY_RECENTPLACE = 50;
    int CODE_REQUEST_ACTIVITY_CONTACT_US = 51;
    int CODE_REQUEST_ACTIVITY_LOGIN_BY_DETAIL_WISHLIST = 52;
    int CODE_REQUEST_ACTIVITY_COLLECTION = 53;

    int CODE_REQUEST_ACTIVITY_EVENT_LIST = 54;
    int CODE_REQUEST_ACTIVITY_NOTICE_LIST = 55;
    int CODE_REQUEST_ACTIVITY_CONTACTUS = 56;
    int CODE_REQUEST_ACTIVITY_ABOUT = 57;

    int CODE_REQUEST_ACTIVITY_STAY = 58;
    int CODE_REQUEST_ACTIVITY_GOURMET = 59;
    int CODE_REQUEST_ACTIVITY_GOURMET_PRODUCT_LIST = 60;
    int CODE_REQUEST_ACTIVITY_GOURMET_PRODUCT_DETAIL = 61;
    int CODE_REQUEST_ACTIVITY_HAPPY_TALK = 62;
    int CODE_REQUEST_ACTIVITY_STAMP = 63;
    int CODE_REQUEST_ACTIVITY_STAMP_HISTORY = 64;
    int CODE_REQUEST_ACTIVITY_STAMP_TERMS = 65;
    int CODE_REQUEST_ACTIVITY_COUPON_TERMS = 66;
    int CODE_REQUEST_ACTIVITY_BONUS = 67;
    int CODE_REQUEST_ACTIVITY_COUPON_HISTORY = 68;
    int CODE_REQUEST_ACTIVITY_REVIEW_TERMS = 69;
    int CODE_REQUEST_ACTIVITY_PLACE_REVIEW = 70;
    int CODE_REQUEST_ACTIVITY_GUIDE = 71;
    int CODE_REQUEST_ACTIVITY_SNS = 72;
    int CODE_REQUEST_ACTIVITY_LIFESTYLE = 73;
    int CODE_REQUEST_ACTIVITY_TRUEVIEW = 74;
    int CODE_REQUEST_ACTIVITY_PREVIEW = 75;
    int CODE_REQUEST_ACTIVITY_STAY_OB_DETAIL = 76;
    int CODE_REQUEST_ACTIVITY_RECEIPT = 77;

    // Android Activity의 Result Code들이다.
    int CODE_RESULT_ACTIVITY_PAYMENT_FAIL = 100;
    int CODE_RESULT_ACTIVITY_PAYMENT_SUCCESS = 101;
    int CODE_RESULT_ACTIVITY_PAYMENT_INVALID_SESSION = 102; //
    int CODE_RESULT_ACTIVITY_PAYMENT_SOLD_OUT = 103; // 완판되었을때.
    int CODE_RESULT_ACTIVITY_PAYMENT_COMPLETE = 104;
    int CODE_RESULT_ACTIVITY_PAYMENT_INVALID_DATE = 105;
    int CODE_RESULT_ACTIVITY_PAYMENT_NOT_AVAILABLE = 106;
    int CODE_RESULT_ACTIVITY_PAYMENT_NETWORK_ERROR = 107;
    int CODE_RESULT_ACTIVITY_SPLASH_NEW_EVENT = 108;
    int CODE_RESULT_ACTIVITY_PAYMENT_CANCELED = 109;
    int CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_READY = 110;
    int CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_TIME_ERROR = 111;
    int CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_DUPLICATE = 112;
    int CODE_RESULT_ACTIVITY_PAYMENT_TIMEOVER = 113;
    int CODE_RESULT_ACTIVITY_PAYMENT_SALES_CLOSED = 114; // 예약을 하려 버튼을 눌렀는데 주문 시간이 지난경우.
    int CODE_RESULT_ACTIVITY_PAYMENT_UNKNOW_ERROR = 115; // 알수 없는 에러.
    int CODE_RESULT_ACTIVITY_PAYMENT_NOT_ONSALE = 116;
    int CODE_RESULT_ACTIVITY_PAYMENT_PRECHECK = 117;
    int CODE_RESULT_ACTIVITY_EXPIRED_PAYMENT_WAIT = 201;
    int CODE_RESULT_ACTIVITY_PAYMENT_CANCEL = 202;
    int CODE_RESULT_ACTIVITY_SETTING_LOCATION = 210;
    int CODE_RESULT_PAYMENT_BILLING_SUCCSESS = 300;
    int CODE_RESULT_PAYMENT_BILLING_FAIL = 301;
    int CODE_RESULT_PAYMENT_BILLING_DUPLICATE = 302;
    int CODE_RESULT_ACTIVITY_HOME = 303;
    int CODE_RESULT_ACTIVITY_SEARCHRESULT_KEYWORD = 304;
    int CODE_RESULT_ACTIVITY_REFRESH = 305;
    int CODE_RESULT_ACTIVITY_STAY_LIST = 306;
    int CODE_RESULT_ACTIVITY_GOURMET_LIST = 307;
    int CODE_RESULT_ACTIVITY_STAY_AUTOREFUND = 308;
    int CODE_RESULT_ACTIVITY_GO_HOME = 309;
    int CODE_RESULT_ACTIVITY_EVENT = 310;
    int CODE_RESULT_ACTIVITY_STAY_OUTBOUND_SEARCH = 311;
    int CODE_RESULT_ACTIVITY_GO_SEARCH = 312;
    int CODE_RESULT_ACTIVITY_GO_REGION_LIST = 313;

    // 예약 리스트에서
    int CODE_PAY_TYPE_CARD_COMPLETE = 10;
    int CODE_PAY_TYPE_ACCOUNT_WAIT = 20;
    int CODE_PAY_TYPE_ACCOUNT_COMPLETE = 21;

    // 퍼미션 관련
    int REQUEST_CODE_PERMISSIONS_ACCESS_FINE_LOCATION = 10;
    int REQUEST_CODE_PERMISSIONS_READ_PHONE_STATE = 11;

    int REQUEST_CODE_APPLICATION_DETAILS_SETTINGS = 1000;


    // 리스트 페이지 사이즈
    int PAGENATION_LIST_SIZE = 200;

    // Setting region JSONObject Key - not preference
    String JSON_KEY_PROVINCE_NAME = "region";
    String JSON_KEY_AREA_NAME = "area";
    String JSON_KEY_IS_OVER_SEAS = "isOverSeas";
}
