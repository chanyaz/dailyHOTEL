package com.twoheart.dailyhotel.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.crashlytics.android.Crashlytics;
import com.twoheart.dailyhotel.DailyHotel;
import com.twoheart.dailyhotel.Setting;
import com.twoheart.dailyhotel.model.PlacePaymentInformation;

import org.json.JSONObject;

/**
 */
public class DailyPreference
{
    public static final String DAILYHOTEL_SHARED_PREFERENCE_V1 = "dailyHOTEL_v1"; // 새로 만든

    public static final String PREFERENCE_REMOTE_CONFIG = "DH_RemoteConfig";

    /////////////////////////////////////////////////////////////////////////////////////////
    // "dailyHOTEL_v1" Preference
    /////////////////////////////////////////////////////////////////////////////////////////

    private static final String KEY_OPENING_ALARM = "1"; // 알람
    //    private static final String KEY_LAST_MENU = "3"; // 마지막 메뉴 리스트가 무엇인지
    private static final String KEY_SHOW_GUIDE = "4"; // 가이드를 봤는지 여부
    //    private static final String KEY_ALLOW_PUSH = "5";
    //    private static final String KEY_ALLOW_BENEFIT_ALARM = "6";

    //    private static final String KEY_COLLAPSEKEY = "10"; // 푸시 중복 되지 않도록
    //    private static final String KEY_SOCIAL_SIGNUP = "11"; // 회원가입시 소셜 가입자인 경우

    private static final String KEY_HOTEL_REGION_ISOVERSEA = "12"; // 현재 선택된 지역이 국내/해외
    private static final String KEY_GOURMET_REGION_ISOVERSEA = "13"; // 현재 선택된 지역이 국내/해외

    private static final String KEY_NEW_EVENT = "14"; // 현재 이벤트 유무
    private static final String KEY_NEW_COUPON = "15"; // 현재 새로운 쿠폰 유무(로그인 사용자만 보임)
    private static final String KEY_NEW_NOTICE = "16"; // 현재 새로운 쿠폰 유무(로그인 사용자만 보임)

    private static final String KEY_AGREE_TERMS_OF_LOCATION = "21"; // 위치 약관 동의 여부
    //    private static final String KEY_INFORMATION_CS_OPERATION_TIMEMESSAGE = "22"; // 운영시간 문구
    private static final String KEY_APP_VERSION = "23";

    private static final String KEY_SHOW_BENEFIT_ALARM = "24";
    private static final String KEY_BENEFIT_ALARM_MESSAGE = "25";
    private static final String KEY_FIRST_BUYER = "26";
    private static final String KEY_FIRST_APP_VERSION = "27";

    //    private static final String KEY_IS_VIEW_RECENT_PLACE_TOOLTIP = "28"; // 삭제! - 30 으로 대체 됨
    private static final String KEY_INFORMATION_CS_OPERATION_TIME = "29"; // 운영시간 H,H (앞은 시작 뒤는 끝나는 시간)
    //    private static final String KEY_IS_VIEW_WISHLIST_TOOLTIP = "30";
    private static final String KEY_IS_VIEW_SEARCH_TOOLTIP = "31";

    private static final String KEY_IS_REQUEST_REVIEW_ = "32";

    //    private static final String KEY_STAY_LAST_VIEW_DATE = "108";
    //    private static final String KEY_GOURMET_LAST_VIEW_DATE = "109";

    private static final String KEY_HOTEL_SEARCH_RECENTLY = "200";
    private static final String KEY_GOURMET_SEARCH_RECENTLY = "201";

    private static final String KEY_NOTICE_NEW_LIST = "202";
    private static final String KEY_NOTICE_NEW_REMOVE_LIST = "203";

    private static final String KEY_SELECTED_SIMPLE_CARD = "204"; // 마지막으로 간편결제된 카드

    private static final String KEY_STAY_RECENT_PLACES = "210";
    private static final String KEY_GOURMET_RECENT_PLACES = "211";
    private static final String KEY_ALL_RECENT_PLACES = "212";

    // ----> DailyPreference 로 이동
    private static final String KEY_AUTHORIZATION = "1000";
    // <-----

    private static final String KEY_VERIFICATION = "1001";
    private static final String KEY_BASE_URL = "1005"; // 앱의 기본 URL

    private static final String KEY_SETTING_MIGRATION_FLAG = "1003"; // 2.0.0 이후 사용안함
    private static final String KEY_STAY_CATEGORY_CODE = "1010";
    private static final String KEY_STAY_CATEGORY_NAME = "1011";

    private static final String KEY_CALENDAR_HOLIDAYS = "1012";
    private static final String KEY_CHECK_CALENDAR_HOLIDAYS_STARTDAY = "1013";
    private static final String KEY_HAPPY_TALK_CATEGORY = "1014"; // 해피톡 상담유형 저장하기

    private static final String KEY_BACKGROUND_APP_TIME = "2000";

    /////////////////////////////////////////////////////////////////////////////////////////
    // Remote Config Preference
    /////////////////////////////////////////////////////////////////////////////////////////

    // remote config text
    private static final String KEY_REMOTE_CONFIG_INTRO_VERSION = "1";
    private static final String KEY_REMOTE_CONFIG_INTRO_NEW_VERSION = "2";
    private static final String KEY_REMOTE_CONFIG_INTRO_NEW_URL = "3";

    private static final String KEY_REMOTE_CONFIG_TEXT_VERSION = "100";
    private static final String KEY_REMOTE_CONFIG_TEXT_LOGINTEXT01 = "101";
    private static final String KEY_REMOTE_CONFIG_TEXT_SIGNUPTEXT01 = "102";
    private static final String KEY_REMOTE_CONFIG_TEXT_SIGNUPTEXT02 = "103";
    private static final String KEY_REMOTE_CONFIG_TEXT = "110";

    private static final String KEY_REMOTE_CONFIG_COMPANY_NAME = "200";
    private static final String KEY_REMOTE_CONFIG_COMPANY_CEO = "201";
    private static final String KEY_REMOTE_CONFIG_COMPANY_BIZREGNUMBER = "202";
    private static final String KEY_REMOTE_CONFIG_COMPANY_ITCREGNUMBER = "203";
    private static final String KEY_REMOTE_CONFIG_COMPANY_ADDRESS = "204";
    private static final String KEY_REMOTE_CONFIG_COMPANY_PHONENUMBER = "205";
    private static final String KEY_REMOTE_CONFIG_COMPANY_FAX = "206";
    private static final String KEY_REMOTE_CONFIG_COMPANY_PRIVACY_EMAIL = "207";

    private static final String KEY_REMOTE_CONFIG_STAY_PAYMENT_IS_SIMPLECARD_ENABLED = "300";
    private static final String KEY_REMOTE_CONFIG_STAY_PAYMENT_IS_CARD_ENABLED = "301";
    private static final String KEY_REMOTE_CONFIG_STAY_PAYMENT_IS_PHONE_ENABLED = "302";
    private static final String KEY_REMOTE_CONFIG_STAY_PAYMENT_IS_VIRTUAL_ENABLED = "303";

    private static final String KEY_REMOTE_CONFIG_GOURMET_PAYMENT_IS_SIMPLECARD_ENABLED = "304";
    private static final String KEY_REMOTE_CONFIG_GOURMET_PAYMENT_IS_CARD_ENABLED = "305";
    private static final String KEY_REMOTE_CONFIG_GOURMET_PAYMENT_IS_PHONE_ENABLED = "306";
    private static final String KEY_REMOTE_CONFIG_GOURMET_PAYMENT_IS_VIRTUAL_ENABLED = "307";

    private static final String KEY_REMOTE_CONFIG_HOME_MESSAGE_AREA_LOGIN_ENABLED = "310";
    private static final String KEY_REMOTE_CONFIG_HOME_MESSAGE_AREA_LOGOUT_ENABLED = "311";
    private static final String KEY_REMOTE_CONFIG_HOME_MESSAGE_AREA_LOGOUT_TITLE = "312";
    private static final String KEY_REMOTE_CONFIG_HOME_MESSAGE_AREA_LOGOUT_CTA = "313";

    private static final String KEY_REMOTE_CONFIG_HOME_EVENT_CURRENT_VERSION = "314";
    private static final String KEY_REMOTE_CONFIG_HOME_EVENT_TITLE = "315";
    private static final String KEY_REMOTE_CONFIG_HOME_EVENT_URL = "316";
    private static final String KEY_REMOTE_CONFIG_HOME_EVENT_INDEX = "317";

    // Stamp
    private static final String KEY_REMOTE_CONFIG_STAMP_ENABLED = "318";
    private static final String KEY_REMOTE_CONFIG_STAMP_STAY_DETAIL_MESSAGE1 = "319";
    private static final String KEY_REMOTE_CONFIG_STAMP_STAY_DETAIL_MESSAGE2 = "320";
    private static final String KEY_REMOTE_CONFIG_STAMP_STAY_DETAIL_MESSAGE3 = "321";
    private static final String KEY_REMOTE_CONFIG_STAMP_STAY_DETAIL_MESSAGE3_ENABLED = "322";
    private static final String KEY_REMOTE_CONFIG_STAMP_STAY_DETAIL_POPUP_TITLE = "323";
    private static final String KEY_REMOTE_CONFIG_STAMP_STAY_DETAIL_POPUP_MESSAGE = "324";
    private static final String KEY_REMOTE_CONFIG_STAMP_STAY_THANKYOU_MESSAGE1 = "325";
    private static final String KEY_REMOTE_CONFIG_STAMP_STAY_THANKYOU_MESSAGE2 = "326";
    private static final String KEY_REMOTE_CONFIG_STAMP_STAY_THANKYOU_MESSAGE3 = "327";
    private static final String KEY_REMOTE_CONFIG_STAMP_STAMP_DATE1 = "328";
    private static final String KEY_REMOTE_CONFIG_STAMP_STAMP_DATE2 = "329";
    private static final String KEY_REMOTE_CONFIG_STAMP_STAMP_DATE3 = "330";
    private static final String KEY_REMOTE_CONFIG_STAMP_END_EVENT_POPUP_ENABLED = "331";

    private static final String KEY_REMOTE_CONFIG_ABTEST_GOURMET_PRODUCT_LIST = "340";
    private static final String KEY_REMOTE_CONFIG_ABTEST_HOME_BUTTON = "341";

    /////////////////////////////////////////////////////////////////////////////////////////
    // New Key old --> v1
    /////////////////////////////////////////////////////////////////////////////////////////

    // Setting
    private static final String KEY_SETTING_GCM_ID = "1002";
    private static final String KEY_SETTING_VERSION_SKIP_MAX_VERSION = "1004";

    // Setting - Region - Old 2017.04.07
        private static final String KEY_SETTING_REGION_STAY_SELECT = "1110";
//        private static final String KEY_SETTING_REGION_STAY_SETTING = "1111"; // home 이후 사용안하는 부분
        private static final String KEY_SETTING_REGION_PROVINCE_STAY_SELECT = "1112"; // adjust
        private static final String KEY_SETTING_REGION_FNB_SELECT = "1120";
//        private static final String KEY_SETTING_REGION_FNB_SETTING = "1121"; // home 이후 사용안하는 부분
        private static final String KEY_SETTING_REGION_PROVINCE_FNB_SELECT = "1122"; // adjust
    // Setting - Region New 2017.04.07 - 스테이 호텔 구분 안하고 전체를 다 개별 카테고리로 봄
    private static final String KEY_SETTING_REGION_STAY_ALL = "1130";
    private static final String KEY_SETTING_REGION_GOURMET_ALL = "1131";
    private static final String KEY_SETTING_REGION_STAY_HOTEL = "1132";
    private static final String KEY_SETTING_REGION_STAY_BOUTIQUE = "1133";
    private static final String KEY_SETTING_REGION_STAY_PENSION = "1134";
    private static final String KEY_SETTING_REGION_STAY_RESORT = "1135";

    // Setting - Home
    private static final String KEY_SETTING_HOME_MESSAGE_AREA_ENABLED = "1201";

    // User
    // -----> DailyUserPreference 로 이동
    private static final String KEY_USER_EMAIL = "2001";
    private static final String KEY_USER_TYPE = "2002";
    private static final String KEY_USER_NAME = "2003";
    private static final String KEY_USER_RECOMMENDER = "2004";
    private static final String KEY_USER_BENEFIT_ALARM = "2005";
    private static final String KEY_USER_IS_EXCEED_BONUS = "2006";
    private static final String KEY_USER_BIRTHDAY = "2007";


    private static final String KEY_PAYMENT_OVERSEAS_NAME = "4000";
    private static final String KEY_PAYMENT_OVERSEAS_PHONE = "4001";
    private static final String KEY_PAYMENT_OVERSEAS_EMAIL = "4002";
    // <------

    // Payment
    private static final String KEY_PAYMENT_INFORMATION = "4003";


    // payment - Virtual Account
    private static final String KEY_PAYMENT_ACCOUNT_READY_FLAG = "4100";

    // Event
    private static final String KEY_EVENT_LASTEST_EVENT_TIME = "6100";
    private static final String KEY_EVENT_LASTEST_COUPON_TIME = "6101";
    private static final String KEY_EVENT_LASTEST_NOTICE_TIME = "6102";
    private static final String KEY_EVENT_VIEWED_EVENT_TIME = "6200";
    private static final String KEY_EVENT_VIEWED_COUPON_TIME = "6201";
    private static final String KEY_EVENT_VIEWED_NOTICE_TIME = "6202";
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    private static DailyPreference mInstance;
    private SharedPreferences mPreferences;
    private SharedPreferences mRemoteConfigPreferences;
    private Editor mEditor;
    private Editor mRemoteConfigEditor;

    private DailyPreference(Context context)
    {
        mPreferences = context.getSharedPreferences(DAILYHOTEL_SHARED_PREFERENCE_V1, Context.MODE_PRIVATE);
        mEditor = mPreferences.edit();

        mRemoteConfigPreferences = context.getSharedPreferences(PREFERENCE_REMOTE_CONFIG, Context.MODE_PRIVATE);
        mRemoteConfigEditor = mRemoteConfigPreferences.edit();
    }

    public static synchronized DailyPreference getInstance(Context context)
    {
        if (mInstance == null)
        {
            mInstance = new DailyPreference(context);
        }

        return mInstance;
    }

    /**
     * 앱 삭제시에도 해당 데이터는 남기도록 한다.
     */
    public void clear()
    {
        // 해택 알림 내용은 유지 하도록 한다. 단 로그인시에는 서버에서 다시 가져와서 세팅한다.
        boolean isShowBenefitAlarm = isShowBenefitAlarm();
        boolean isShowSearchToolTip = isViewSearchTooltip();

        String allRecentPlaces = getAllRecentPlaces();

        String baseUrl = getBaseUrl();

        boolean isHomeTextMessageAreaEnable = isHomeTextMessageAreaEnabled();

        if (mEditor != null)
        {
            mEditor.clear();
            mEditor.apply();
        }

        setShowBenefitAlarm(isShowBenefitAlarm);
        setIsViewSearchTooltip(isShowSearchToolTip);

        setAllRecentPlaces(allRecentPlaces);

        setBaseUrl(baseUrl);

        setHomeTextMessageAreaEnabled(isHomeTextMessageAreaEnable);

        DailyHotel.AUTHORIZATION = null;
    }

    private String getValue(SharedPreferences sharedPreferences, String key, String defaultValue)
    {
        String result = defaultValue;

        try
        {
            if (sharedPreferences != null)
            {
                result = sharedPreferences.getString(key, defaultValue);
            }

        } catch (ClassCastException e)
        {
            if (Constants.DEBUG == false)
            {
                try
                {
                    Object object = sharedPreferences.getAll().get(key);
                    String msg = "key : " + key + ", value : " + sharedPreferences.getAll().get(key) + ", Type : " + object.toString();

                    Crashlytics.log(msg);
                    Crashlytics.logException(e);
                } catch (Exception e1)
                {

                }
            }

            if (sharedPreferences != null)
            {
                sharedPreferences.edit().remove(key);
                sharedPreferences.edit().putString(key, defaultValue);
                sharedPreferences.edit().apply();
            }
        }

        return result;
    }

    private void setValue(Editor editor, String key, String value)
    {
        if (editor != null)
        {
            if (Util.isTextEmpty(value) == true)
            {
                editor.remove(key);
            } else
            {
                editor.putString(key, value);
            }

            editor.apply();
        }
    }

    private boolean getValue(SharedPreferences sharedPreferences, String key, boolean defaultValue)
    {
        boolean result = defaultValue;

        try
        {
            if (sharedPreferences != null)
            {
                result = sharedPreferences.getBoolean(key, defaultValue);
            }

        } catch (ClassCastException e)
        {
            if (Constants.DEBUG == false)
            {
                try
                {
                    Object object = sharedPreferences.getAll().get(key);
                    String msg = "key : " + key + ", value : " + sharedPreferences.getAll().get(key) + ", Type : " + object.toString();

                    Crashlytics.log(msg);
                    Crashlytics.logException(e);
                } catch (Exception e1)
                {

                }
            }

            if (sharedPreferences != null)
            {
                sharedPreferences.edit().remove(key);
                sharedPreferences.edit().putBoolean(key, defaultValue);
                sharedPreferences.edit().apply();
            }
        }

        return result;
    }

    private void setValue(Editor editor, String key, boolean value)
    {
        if (editor != null)
        {
            editor.putBoolean(key, value);
            editor.apply();
        }
    }

    private long getValue(SharedPreferences sharedPreferences, String key, long defaultValue)
    {
        long result = defaultValue;

        try
        {
            if (sharedPreferences != null)
            {
                result = sharedPreferences.getLong(key, defaultValue);
            }

        } catch (ClassCastException e)
        {
            if (Constants.DEBUG == false)
            {
                try
                {
                    Object object = sharedPreferences.getAll().get(key);
                    String msg = "key : " + key + ", value : " + sharedPreferences.getAll().get(key) + ", Type : " + object.toString();

                    Crashlytics.log(msg);
                    Crashlytics.logException(e);
                } catch (Exception e1)
                {

                }
            }

            if (sharedPreferences != null)
            {
                sharedPreferences.edit().remove(key);
                sharedPreferences.edit().putLong(key, defaultValue);
                sharedPreferences.edit().apply();
            }
        }

        return result;
    }

    private void setValue(Editor editor, String key, int value)
    {
        if (editor != null)
        {
            editor.putInt(key, value);
            editor.apply();
        }
    }

    private int getValue(SharedPreferences sharedPreferences, String key, int defaultValue)
    {
        int result = defaultValue;

        try
        {
            if (sharedPreferences != null)
            {
                result = sharedPreferences.getInt(key, defaultValue);
            }

        } catch (ClassCastException e)
        {
            if (Constants.DEBUG == false)
            {
                try
                {
                    Object object = sharedPreferences.getAll().get(key);
                    String msg = "key : " + key + ", value : " + sharedPreferences.getAll().get(key) + ", Type : " + object.toString();

                    Crashlytics.log(msg);
                    Crashlytics.logException(e);
                } catch (Exception e1)
                {

                }
            }

            if (sharedPreferences != null)
            {
                sharedPreferences.edit().remove(key);
                sharedPreferences.edit().putInt(key, defaultValue);
                sharedPreferences.edit().apply();
            }
        }

        return result;
    }

    private void setValue(Editor editor, String key, long value)
    {
        if (editor != null)
        {
            editor.putLong(key, value);
            editor.apply();
        }
    }

    private void removeValue(Editor editor, String key)
    {
        if (editor != null)
        {
            editor.remove(key);
            editor.apply();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    // "dailyHOTEL_v1" Preference
    /////////////////////////////////////////////////////////////////////////////////////////

    public boolean getEnabledOpeningAlarm()
    {
        return getValue(mPreferences, KEY_OPENING_ALARM, false);
    }

    public void setEnabledOpeningAlarm(boolean value)
    {
        setValue(mEditor, KEY_OPENING_ALARM, value);
    }

    public boolean hasNewEvent()
    {
        return getValue(mPreferences, KEY_NEW_EVENT, false);
    }

    public void setNewEvent(boolean value)
    {
        setValue(mEditor, KEY_NEW_EVENT, value);
    }

    public boolean hasNewCoupon()
    {
        return getValue(mPreferences, KEY_NEW_COUPON, false);
    }

    public void setNewCoupon(boolean value)
    {
        setValue(mEditor, KEY_NEW_COUPON, value);
    }

    public boolean hasNewNotice()
    {
        return getValue(mPreferences, KEY_NEW_NOTICE, false);
    }

    public void setNewNotice(boolean value)
    {
        setValue(mEditor, KEY_NEW_NOTICE, value);
    }

    //    public String getLastMenu()
    //    {
    //        return getValue(mPreferences, KEY_LAST_MENU, null);
    //    }

    //    public void setLastMenu(String value)
    //    {
    //        setValue(mEditor, KEY_LAST_MENU, value);
    //    }

    public boolean isShowGuide()
    {
        return getValue(mPreferences, KEY_SHOW_GUIDE, false);
    }

    public void setShowGuide(boolean value)
    {
        setValue(mEditor, KEY_SHOW_GUIDE, value);
    }

    //    public String getStayLastViewDate()
    //    {
    //        return getValue(mPreferences, KEY_STAY_LAST_VIEW_DATE, null);
    //    }
    //
    //    public void setStayLastViewDate(String value)
    //    {
    //        setValue(mEditor, KEY_STAY_LAST_VIEW_DATE, value);
    //    }
    //
    //    public String getGourmetLastViewDate()
    //    {
    //        return getValue(mPreferences, KEY_GOURMET_LAST_VIEW_DATE, null);
    //    }
    //
    //    public void setGourmetLastViewDate(String value)
    //    {
    //        setValue(mEditor, KEY_GOURMET_LAST_VIEW_DATE, value);
    //    }

    public boolean isSelectedOverseaRegion(Constants.PlaceType placeType)
    {
        switch (placeType)
        {
            case FNB:
                return getValue(mPreferences, KEY_GOURMET_REGION_ISOVERSEA, false);

            case HOTEL:
            default:
                return getValue(mPreferences, KEY_HOTEL_REGION_ISOVERSEA, false);
        }
    }

    public void setSelectedOverseaRegion(Constants.PlaceType placeType, boolean value)
    {
        switch (placeType)
        {
            case HOTEL:
                setValue(mEditor, KEY_HOTEL_REGION_ISOVERSEA, value);
                break;

            case FNB:
                setValue(mEditor, KEY_GOURMET_REGION_ISOVERSEA, value);
                break;
        }
    }

    public boolean isVerification()
    {
        return getValue(mPreferences, KEY_VERIFICATION, false);
    }

    public void setVerification(boolean value)
    {
        setValue(mEditor, KEY_VERIFICATION, value);
    }

    @Deprecated
    public boolean isMigrationFlag()
    {
        return getValue(mPreferences, KEY_SETTING_MIGRATION_FLAG, false);
    }

    @Deprecated
    public void setMigrationFlag(boolean value)
    {
        setValue(mEditor, KEY_SETTING_MIGRATION_FLAG, value);
    }

    public void setHotelRecentSearches(String text)
    {
        setValue(mEditor, KEY_HOTEL_SEARCH_RECENTLY, text);
    }

    public String getHotelRecentSearches()
    {
        return getValue(mPreferences, KEY_HOTEL_SEARCH_RECENTLY, null);
    }

    public void setGourmetRecentSearches(String text)
    {
        setValue(mEditor, KEY_GOURMET_SEARCH_RECENTLY, text);
    }

    public String getGourmetRecentSearches()
    {
        return getValue(mPreferences, KEY_GOURMET_SEARCH_RECENTLY, null);
    }

    public void setAllRecentPlaces(String recentPlaces)
    {
        setValue(mEditor, KEY_ALL_RECENT_PLACES, recentPlaces);
    }

    public String getAllRecentPlaces()
    {
        return getValue(mPreferences, KEY_ALL_RECENT_PLACES, null);
    }

    public void setTermsOfLocation(boolean value)
    {
        setValue(mEditor, KEY_AGREE_TERMS_OF_LOCATION, value);
    }

    public boolean isAgreeTermsOfLocation()
    {
        return getValue(mPreferences, KEY_AGREE_TERMS_OF_LOCATION, false);
    }

    public String getOperationTime()
    {
        return getValue(mPreferences, KEY_INFORMATION_CS_OPERATION_TIME, "9,3");
    }

    public void setOperationTime(String text)
    {
        setValue(mEditor, KEY_INFORMATION_CS_OPERATION_TIME, text);
    }

    public void setAppVersion(String value)
    {
        setValue(mEditor, KEY_APP_VERSION, value);
    }

    public String getAppVersion()
    {
        return getValue(mPreferences, KEY_APP_VERSION, null);
    }

    public void setShowBenefitAlarm(boolean value)
    {
        setValue(mEditor, KEY_SHOW_BENEFIT_ALARM, value);
    }

    public boolean isShowBenefitAlarm()
    {
        return getValue(mPreferences, KEY_SHOW_BENEFIT_ALARM, false);
    }

    public void setBenefitAlarmMessage(String value)
    {
        setValue(mEditor, KEY_BENEFIT_ALARM_MESSAGE, value);
    }

    public String getBenefitAlarmMessage()
    {
        return getValue(mPreferences, KEY_BENEFIT_ALARM_MESSAGE, null);
    }

    public void setShowBenefitAlarmFirstBuyer(boolean value)
    {
        setValue(mEditor, KEY_FIRST_BUYER, value);
    }

    public boolean isShowBenefitAlarmFirstBuyer()
    {
        return getValue(mPreferences, KEY_FIRST_BUYER, false);
    }

    public void setFirstAppVersion(String value)
    {
        setValue(mEditor, KEY_FIRST_APP_VERSION, value);
    }

    public String getFirstAppVersion()
    {
        return getValue(mPreferences, KEY_FIRST_APP_VERSION, null);
    }

    public void setIsViewSearchTooltip(boolean value)
    {
        setValue(mEditor, KEY_IS_VIEW_SEARCH_TOOLTIP, value);
    }

    public boolean isViewSearchTooltip()
    {
        return getValue(mPreferences, KEY_IS_VIEW_SEARCH_TOOLTIP, false);
    }

    public void setIsRequestReview(boolean value)
    {
        setValue(mEditor, KEY_IS_REQUEST_REVIEW_, value);
    }

    public boolean isRequestReview()
    {
        return getValue(mPreferences, KEY_IS_REQUEST_REVIEW_, false);
    }

    public void setStayCategory(String name, String code)
    {
        setValue(mEditor, KEY_STAY_CATEGORY_NAME, name);
        setValue(mEditor, KEY_STAY_CATEGORY_CODE, code);
    }

    public String getStayCategoryCode()
    {
        return getValue(mPreferences, KEY_STAY_CATEGORY_CODE, null);
    }

    public String getStayCategoryName()
    {
        return getValue(mPreferences, KEY_STAY_CATEGORY_NAME, null);
    }

    public long getBackgroundAppTime()
    {
        return getValue(mPreferences, KEY_BACKGROUND_APP_TIME, 0L);
    }

    public void setBackgroundAppTime(long value)
    {
        setValue(mEditor, KEY_BACKGROUND_APP_TIME, value);
    }

    public String getBaseUrl()
    {
        return getValue(mPreferences, KEY_BASE_URL, Crypto.getUrlDecoderEx(Setting.URL_DAILYHOTEL_SERVER_DEFAULT));
    }

    public void setBaseUrl(String value)
    {
        setValue(mEditor, KEY_BASE_URL, value);

        // 반영이 안되는 경우가 있어서 특별히 추가 하였습니다.
        mEditor.commit();
    }

    public void setCalendarHolidays(String value)
    {
        setValue(mEditor, KEY_CALENDAR_HOLIDAYS, value);
    }

    public String getCalendarHolidays()
    {
        return getValue(mPreferences, KEY_CALENDAR_HOLIDAYS, null);
    }

    public void setCheckCalendarHolidays(String value)
    {
        setValue(mEditor, KEY_CHECK_CALENDAR_HOLIDAYS_STARTDAY, value);
    }

    public String getCheckCalendarHolidays()
    {
        return getValue(mPreferences, KEY_CHECK_CALENDAR_HOLIDAYS_STARTDAY, null);
    }

    public void setHappyTalkCategory(String value)
    {
        setValue(mEditor, KEY_HAPPY_TALK_CATEGORY, value);
    }

    public String getHappyTalkCategory()
    {
        return getValue(mPreferences, KEY_HAPPY_TALK_CATEGORY, null);
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    // Remote Config Text
    /////////////////////////////////////////////////////////////////////////////////////////

    public String getRemoteConfigIntroImageVersion()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_INTRO_VERSION, Constants.DAILY_INTRO_CURRENT_VERSION);
    }

    public void setRemoteConfigIntroImageVersion(String value)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_INTRO_VERSION, value);
    }

    public String getRemoteConfigIntroImageNewVersion()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_INTRO_NEW_VERSION, null);
    }

    public void setRemoteConfigIntroImageNewVersion(String value)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_INTRO_NEW_VERSION, value);
    }

    public String getRemoteConfigIntroImageNewUrl()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_INTRO_NEW_URL, null);
    }

    public void setRemoteConfigIntroImageNewUrl(String value)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_INTRO_NEW_URL, value);
    }

    public void setRemoteConfigText(String value)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_TEXT, value);
    }

    public String getRemoteConfigText()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_TEXT, null);
    }

    public void setRemoteConfigTextVersion(String value)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_TEXT_VERSION, value);
    }

    public String getRemoteConfigTextVersion()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_TEXT_VERSION, null);
    }

    public void setRemoteConfigTextLoginText01(String value)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_TEXT_LOGINTEXT01, value);
    }

    public String getRemoteConfigTextLoginText01()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_TEXT_LOGINTEXT01, null);
    }

    public void setRemoteConfigTextSignUpText01(String value)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_TEXT_SIGNUPTEXT01, value);
    }

    public String getRemoteConfigTextSignUpText01()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_TEXT_SIGNUPTEXT01, null);
    }

    public void setRemoteConfigTextSignUpText02(String value)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_TEXT_SIGNUPTEXT02, value);
    }

    public String getRemoteConfigTextSignUpText02()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_TEXT_SIGNUPTEXT02, null);
    }

    public void setRemoteConfigCompanyInformation(String name, String ceo, String bizRegNumber//
        , String itcRegNumber, String address, String phoneNumber, String fax, String email)
    {
        if (mRemoteConfigEditor != null)
        {
            mRemoteConfigEditor.putString(KEY_REMOTE_CONFIG_COMPANY_NAME, name);
            mRemoteConfigEditor.putString(KEY_REMOTE_CONFIG_COMPANY_CEO, ceo);
            mRemoteConfigEditor.putString(KEY_REMOTE_CONFIG_COMPANY_BIZREGNUMBER, bizRegNumber);
            mRemoteConfigEditor.putString(KEY_REMOTE_CONFIG_COMPANY_ITCREGNUMBER, itcRegNumber);
            mRemoteConfigEditor.putString(KEY_REMOTE_CONFIG_COMPANY_ADDRESS, address);
            mRemoteConfigEditor.putString(KEY_REMOTE_CONFIG_COMPANY_PHONENUMBER, phoneNumber);
            mRemoteConfigEditor.putString(KEY_REMOTE_CONFIG_COMPANY_FAX, fax);

            if (Util.isTextEmpty(email) == false)
            {
                mRemoteConfigEditor.putString(KEY_REMOTE_CONFIG_COMPANY_PRIVACY_EMAIL, email);
            }

            mRemoteConfigEditor.apply();
        }
    }

    public String getRemoteConfigCompanyName()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_COMPANY_NAME, null);
    }

    public String getRemoteConfigCompanyCEO()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_COMPANY_CEO, null);
    }

    public String getRemoteConfigCompanyBizRegNumber()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_COMPANY_BIZREGNUMBER, null);
    }

    public String getRemoteConfigCompanyItcRegNumber()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_COMPANY_ITCREGNUMBER, null);
    }

    public String getRemoteConfigCompanyAddress()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_COMPANY_ADDRESS, null);
    }

    public String getRemoteConfigCompanyPhoneNumber()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_COMPANY_PHONENUMBER, null);
    }

    public String getRemoteConfigCompanyFax()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_COMPANY_FAX, null);
    }

    public String getRemoteConfigCompanyPrivacyEmail()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_COMPANY_PRIVACY_EMAIL, "privacy.korea@dailyhotel.com");
    }

    public void setRemoteConfigStaySimpleCardPaymentEnabled(boolean value)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_STAY_PAYMENT_IS_SIMPLECARD_ENABLED, value);
    }

    public boolean isRemoteConfigStaySimpleCardPaymentEnabled()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_STAY_PAYMENT_IS_SIMPLECARD_ENABLED, true);
    }

    public void setRemoteConfigStayCardPaymentEnabled(boolean value)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_STAY_PAYMENT_IS_CARD_ENABLED, value);
    }

    public boolean isRemoteConfigStayCardPaymentEnabled()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_STAY_PAYMENT_IS_CARD_ENABLED, true);
    }

    public void setRemoteConfigStayPhonePaymentEnabled(boolean value)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_STAY_PAYMENT_IS_PHONE_ENABLED, value);
    }

    public boolean isRemoteConfigStayPhonePaymentEnabled()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_STAY_PAYMENT_IS_PHONE_ENABLED, true);
    }

    public void setRemoteConfigStayVirtualPaymentEnabled(boolean value)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_STAY_PAYMENT_IS_VIRTUAL_ENABLED, value);
    }

    public boolean isRemoteConfigStayVirtualPaymentEnabled()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_STAY_PAYMENT_IS_VIRTUAL_ENABLED, true);
    }

    public void setRemoteConfigGourmetSimpleCardPaymentEnabled(boolean value)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_GOURMET_PAYMENT_IS_SIMPLECARD_ENABLED, value);
    }

    public boolean isRemoteConfigGourmetSimpleCardPaymentEnabled()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_GOURMET_PAYMENT_IS_SIMPLECARD_ENABLED, true);
    }

    public void setRemoteConfigGourmetCardPaymentEnabled(boolean value)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_GOURMET_PAYMENT_IS_CARD_ENABLED, value);
    }

    public boolean isRemoteConfigGourmetCardPaymentEnabled()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_GOURMET_PAYMENT_IS_CARD_ENABLED, true);
    }

    public void setRemoteConfigGourmetPhonePaymentEnabled(boolean value)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_GOURMET_PAYMENT_IS_PHONE_ENABLED, value);
    }

    public boolean isRemoteConfigGourmetPhonePaymentEnabled()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_GOURMET_PAYMENT_IS_PHONE_ENABLED, true);
    }

    public void setRemoteConfigGourmetVirtualPaymentEnabled(boolean value)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_GOURMET_PAYMENT_IS_VIRTUAL_ENABLED, value);
    }

    public boolean isRemoteConfigGourmetVirtualPaymentEnabled()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_GOURMET_PAYMENT_IS_VIRTUAL_ENABLED, true);
    }

    public void setRemoteConfigHomeMessageAreaLoginEnabled(boolean value)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_HOME_MESSAGE_AREA_LOGIN_ENABLED, value);
    }

    public boolean isRemoteConfigHomeMessageAreaLoginEnabled()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_HOME_MESSAGE_AREA_LOGIN_ENABLED, true);
    }

    public void setRemoteConfigHomeMessageAreaLogoutEnabled(boolean value)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_HOME_MESSAGE_AREA_LOGOUT_ENABLED, value);
    }

    public boolean isRemoteConfigHomeMessageAreaLogoutEnabled()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_HOME_MESSAGE_AREA_LOGOUT_ENABLED, true);
    }

    public void setRemoteConfigHomeMessageAreaLogoutTitle(String value)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_HOME_MESSAGE_AREA_LOGOUT_TITLE, value);
    }

    public String getRemoteConfigHomeMessageAreaLogoutTitle()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_HOME_MESSAGE_AREA_LOGOUT_TITLE, null);
    }

    public void setRemoteConfigHomeMessageAreaLogoutCallToAction(String value)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_HOME_MESSAGE_AREA_LOGOUT_CTA, value);
    }

    public String getRemoteConfigHomeMessageAreaLogoutCallToAction()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_HOME_MESSAGE_AREA_LOGOUT_CTA, null);
    }

    public void setRemoteConfigHomeEventCurrentVersion(String value)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_HOME_EVENT_CURRENT_VERSION, value);
    }

    public String getRemoteConfigHomeEventCurrentVersion()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_HOME_EVENT_CURRENT_VERSION, null);
    }

    public void setRemoteConfigHomeEventUrl(String value)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_HOME_EVENT_URL, value);
    }

    public String getRemoteConfigHomeEventUrl()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_HOME_EVENT_URL, null);
    }

    public void setRemoteConfigHomeEventTitle(String value)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_HOME_EVENT_TITLE, value);
    }

    public String getRemoteConfigHomeEventTitle()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_HOME_EVENT_TITLE, null);
    }

    public void setRemoteConfigHomeEventIndex(int index)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_HOME_EVENT_INDEX, index);
    }

    public int getRemoteConfigHomeEventIndex()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_HOME_EVENT_INDEX, -1);
    }

    public void setRemoteConfigStampEnabled(boolean enabled)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_STAMP_ENABLED, enabled);
    }

    public boolean isRemoteConfigStampEnabled()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_STAMP_ENABLED, false);
    }

    public void setRemoteConfigStampStayDetailMessage(String message1, String message2, String message3, boolean message3Enabled)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_STAMP_STAY_DETAIL_MESSAGE1, message1);
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_STAMP_STAY_DETAIL_MESSAGE2, message2);
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_STAMP_STAY_DETAIL_MESSAGE3, message3);
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_STAMP_STAY_DETAIL_MESSAGE3_ENABLED, message3Enabled);
    }

    public String getRemoteConfigStampStayDetailMessage1()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_STAMP_STAY_DETAIL_MESSAGE1, null);
    }

    public String getRemoteConfigStampStayDetailMessage2()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_STAMP_STAY_DETAIL_MESSAGE2, null);
    }

    public String getRemoteConfigStampStayDetailMessage3()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_STAMP_STAY_DETAIL_MESSAGE3, null);
    }

    public boolean isRemoteConfigStampStayDetailMessage3Enabled()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_STAMP_STAY_DETAIL_MESSAGE3_ENABLED, false);
    }

    public void setRemoteConfigStampStayDetailPopup(String title, String message)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_STAMP_STAY_DETAIL_POPUP_TITLE, title);
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_STAMP_STAY_DETAIL_POPUP_MESSAGE, message);
    }

    public String getRemoteConfigStampStayDetailPopupTitle()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_STAMP_STAY_DETAIL_POPUP_TITLE, null);
    }

    public String getRemoteConfigStampStayDetailPopupMessage()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_STAMP_STAY_DETAIL_POPUP_MESSAGE, null);
    }

    public void setRemoteConfigStampStayThankYouMessage(String message1, String message2, String message3)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_STAMP_STAY_THANKYOU_MESSAGE1, message1);
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_STAMP_STAY_THANKYOU_MESSAGE2, message2);
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_STAMP_STAY_THANKYOU_MESSAGE3, message3);
    }

    public String getRemoteConfigStampStayThankYouMessage1()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_STAMP_STAY_THANKYOU_MESSAGE1, null);
    }

    public String getRemoteConfigStampStayThankYouMessage2()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_STAMP_STAY_THANKYOU_MESSAGE2, null);
    }

    public String getRemoteConfigStampStayThankYouMessage3()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_STAMP_STAY_THANKYOU_MESSAGE3, null);
    }

    public void setRemoteConfigStampStayEndEventPopupEnabled(boolean value)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_STAMP_END_EVENT_POPUP_ENABLED, value);
    }

    public boolean isRemoteConfigStampStayEndEventPopupEnabled()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_STAMP_END_EVENT_POPUP_ENABLED, true);
    }

    public void setRemoteConfigStampDate(String date1, String date2, String date3)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_STAMP_STAMP_DATE1, date1);
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_STAMP_STAMP_DATE2, date2);
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_STAMP_STAMP_DATE3, date3);
    }

    public String getRemoteConfigStampDate1()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_STAMP_STAMP_DATE1, null);
    }

    public String getRemoteConfigStampDate2()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_STAMP_STAMP_DATE2, null);
    }

    public String getRemoteConfigStampDate3()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_STAMP_STAMP_DATE3, null);
    }

    public void setRemoteConfigABTestGourmetProductList(String value)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_ABTEST_GOURMET_PRODUCT_LIST, value);
    }

    public String getRemoteConfigABTestGourmetProductList()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_ABTEST_GOURMET_PRODUCT_LIST, null);
    }

    public void setRemoteConfigABTestHomeButton(String value)
    {
        setValue(mRemoteConfigEditor, KEY_REMOTE_CONFIG_ABTEST_HOME_BUTTON, value);
    }

    public String getRemoteConfigABTestHomeButton()
    {
        return getValue(mRemoteConfigPreferences, KEY_REMOTE_CONFIG_ABTEST_HOME_BUTTON, null);
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    // new
    /////////////////////////////////////////////////////////////////////////////////////////

    public String getGCMRegistrationId()
    {
        return getValue(mPreferences, KEY_SETTING_GCM_ID, null);
    }

    public String getLastestEventTime()
    {
        return getValue(mPreferences, KEY_EVENT_LASTEST_EVENT_TIME, null);
    }

    public void setLastestEventTime(String value)
    {
        setValue(mEditor, KEY_EVENT_LASTEST_EVENT_TIME, value);
    }

    public String getLastestCouponTime()
    {
        return getValue(mPreferences, KEY_EVENT_LASTEST_COUPON_TIME, null);
    }

    public void setLastestCouponTime(String value)
    {
        setValue(mEditor, KEY_EVENT_LASTEST_COUPON_TIME, value);
    }

    public String getLastestNoticeTime()
    {
        return getValue(mPreferences, KEY_EVENT_LASTEST_NOTICE_TIME, null);
    }

    public void setLastestNoticeTime(String value)
    {
        setValue(mEditor, KEY_EVENT_LASTEST_NOTICE_TIME, value);
    }

    public String getViewedEventTime()
    {
        return getValue(mPreferences, KEY_EVENT_VIEWED_EVENT_TIME, null);
    }

    public void setViewedEventTime(String value)
    {
        setValue(mEditor, KEY_EVENT_VIEWED_EVENT_TIME, value);
    }

    public String getViewedCouponTime()
    {
        return getValue(mPreferences, KEY_EVENT_VIEWED_COUPON_TIME, null);
    }

    public void setViewedCouponTime(String value)
    {
        setValue(mEditor, KEY_EVENT_VIEWED_COUPON_TIME, value);
    }

    public String getViewedNoticeTime()
    {
        return getValue(mPreferences, KEY_EVENT_VIEWED_NOTICE_TIME, null);
    }

    public void setViewedNoticeTime(String value)
    {
        setValue(mEditor, KEY_EVENT_VIEWED_NOTICE_TIME, value);
    }

    public String getNoticeNewList()
    {
        return getValue(mPreferences, KEY_NOTICE_NEW_LIST, null);
    }

    public void setNoticeNewList(String value)
    {
        setValue(mEditor, KEY_NOTICE_NEW_LIST, value);
    }

    public String getNoticeNewRemoveList()
    {
        return getValue(mPreferences, KEY_NOTICE_NEW_REMOVE_LIST, null);
    }

    public void setNoticeNewRemoveList(String value)
    {
        setValue(mEditor, KEY_NOTICE_NEW_REMOVE_LIST, value);
    }

    public String getSelectedSimpleCard()
    {
        return getValue(mPreferences, KEY_SELECTED_SIMPLE_CARD, null);
    }

    public void setSelectedSimpleCard(String value)
    {
        setValue(mEditor, KEY_SELECTED_SIMPLE_CARD, value);
    }

    // version - 2.0.4 로 강업 이후 삭제 필요 부분
    @Deprecated
    public String getSelectedRegion(Constants.PlaceType placeType)
    {
        switch (placeType)
        {
            case FNB:
                return getValue(mPreferences, KEY_SETTING_REGION_FNB_SELECT, null);

            case HOTEL:
            default:
                return getValue(mPreferences, KEY_SETTING_REGION_STAY_SELECT, null);
        }
    }

    // version - 2.0.4 로 강업 이후 삭제 필요 부분
    @Deprecated
    public void setSelectedRegion(Constants.PlaceType placeType, String value)
    {
        switch (placeType)
        {
            case HOTEL:
                setValue(mEditor, KEY_SETTING_REGION_STAY_SELECT, value);
                break;

            case FNB:
                setValue(mEditor, KEY_SETTING_REGION_FNB_SELECT, value);
                break;
        }
    }

    /**
     * 선택된 대지역 저장값 - Adjust 용
     * // version - 2.0.4 로 강업 이후 삭제 필요 부분
     * @return
     */
    @Deprecated
    public String getSelectedRegionTypeProvince(Constants.PlaceType placeType)
    {
        switch (placeType)
        {
            case FNB:
                return getValue(mPreferences, KEY_SETTING_REGION_PROVINCE_FNB_SELECT, null);

            case HOTEL:
            default:
                return getValue(mPreferences, KEY_SETTING_REGION_PROVINCE_STAY_SELECT, null);
        }
    }

    // version - 2.0.4 로 강업 이후 삭제 필요 부분
    @Deprecated
    public void setSelectedRegionTypeProvince(Constants.PlaceType placeType, String value)
    {
        switch (placeType)
        {
            case FNB:
                setValue(mEditor, KEY_SETTING_REGION_PROVINCE_FNB_SELECT, value);
                break;

            case HOTEL:
            default:
                setValue(mEditor, KEY_SETTING_REGION_PROVINCE_STAY_SELECT, value);
                break;
        }
    }

    public JSONObject getDailyRegion(Constants.DailyCategoryType type)
    {
        String value = getValue(mPreferences, getDailyRegionKey(type), null);
        if (Util.isTextEmpty(value) == true)
        {
            return null;
        }

        JSONObject jsonObject = null;
        try
        {
            jsonObject = new JSONObject(value);
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }

        return jsonObject;
    }

    public void setDailyRegion(Constants.DailyCategoryType type, JSONObject jsonObject)
    {
        String value;

        if (jsonObject == null)
        {
            value = "";
        } else {
            value = jsonObject.toString();
        }

        setValue(mEditor, getDailyRegionKey(type), value);
    }

    public void setDailyRegion(Constants.DailyCategoryType type //
        , String provinceName, String areaName, boolean isOverSeas)
    {
        JSONObject jsonObject = Util.getDailyRegionJSONObject(provinceName, areaName, isOverSeas);

        setDailyRegion(type, jsonObject);
    }

    private String getDailyRegionKey(Constants.DailyCategoryType type)
    {
        switch (type)
        {
            case STAY_ALL:
                return KEY_SETTING_REGION_STAY_ALL;

            case GOURMET_ALL:
                return KEY_SETTING_REGION_GOURMET_ALL;

            case STAY_HOTEL:
                return KEY_SETTING_REGION_STAY_HOTEL;

            case STAY_BOUTIQUE:
                return KEY_SETTING_REGION_STAY_BOUTIQUE;

            case STAY_PENSION:
                return KEY_SETTING_REGION_STAY_PENSION;

            case STAY_RESORT:
                return KEY_SETTING_REGION_STAY_RESORT;

            default:
                return null;
        }
    }

    public String getOverseasName()
    {
        return getValue(mPreferences, KEY_PAYMENT_OVERSEAS_NAME, null);
    }

    public String getOverseasPhone()
    {
        return getValue(mPreferences, KEY_PAYMENT_OVERSEAS_PHONE, null);
    }

    public String getOverseasEmail()
    {
        return getValue(mPreferences, KEY_PAYMENT_OVERSEAS_EMAIL, null);
    }

    public String getUserType()
    {
        return getValue(mPreferences, KEY_USER_TYPE, null);
    }

    public String getUserName()
    {
        return getValue(mPreferences, KEY_USER_NAME, null);
    }

    public String getUserEmail()
    {
        return getValue(mPreferences, KEY_USER_EMAIL, null);
    }

    public String getUserBirthday()
    {
        return getValue(mPreferences, KEY_USER_BIRTHDAY, null);
    }

    public String getUserRecommender()
    {
        return getValue(mPreferences, KEY_USER_RECOMMENDER, null);
    }

    public boolean isUserBenefitAlarm()
    {
        return getValue(mPreferences, KEY_USER_BENEFIT_ALARM, false);
    }

    public boolean isUserExceedBonus()
    {
        return getValue(mPreferences, KEY_USER_IS_EXCEED_BONUS, false);
    }

    public String getAuthorization()
    {
        try
        {
            return Crypto.urlDecrypt(getValue(mPreferences, KEY_AUTHORIZATION, null));
        } catch (Exception e)
        {
            return null;
        }
    }

    public void clearUserInformation()
    {
        if (mEditor != null)
        {
            mEditor.remove(KEY_USER_EMAIL);
            mEditor.remove(KEY_USER_TYPE);
            mEditor.remove(KEY_USER_NAME);
            mEditor.remove(KEY_USER_RECOMMENDER);
            mEditor.remove(KEY_USER_BENEFIT_ALARM);
            mEditor.remove(KEY_USER_IS_EXCEED_BONUS);
            mEditor.remove(KEY_USER_BIRTHDAY);
            mEditor.remove(KEY_PAYMENT_OVERSEAS_NAME);
            mEditor.remove(KEY_PAYMENT_OVERSEAS_PHONE);
            mEditor.remove(KEY_PAYMENT_OVERSEAS_EMAIL);
            mEditor.remove(KEY_AUTHORIZATION);

            mEditor.apply();
        }
    }

    public String getSkipVersion()
    {
        return getValue(mPreferences, KEY_SETTING_VERSION_SKIP_MAX_VERSION, "0");
    }

    public void setSkipVersion(String value)
    {
        setValue(mEditor, KEY_SETTING_VERSION_SKIP_MAX_VERSION, value);
    }

    public int getVirtualAccountReadyFlag()
    {
        return getValue(mPreferences, KEY_PAYMENT_ACCOUNT_READY_FLAG, -1);
    }

    public void setVirtualAccountReadyFlag(int value)
    {
        setValue(mEditor, KEY_PAYMENT_ACCOUNT_READY_FLAG, value);
    }

    private static final String PAYMENT_SEPARATOR = "±";

    public String[] getPaymentInformation()
    {
        String value = getValue(mPreferences, KEY_PAYMENT_INFORMATION, null);

        if (Util.isTextEmpty(value) == true)
        {
            return null;
        } else
        {
            return value.split(PAYMENT_SEPARATOR);
        }
    }

    public void setPaymentInformation(Constants.PlaceType placeType, String placeName, PlacePaymentInformation.PaymentType paymentType, String dateTime)
    {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(placeType.name());
        stringBuilder.append(PAYMENT_SEPARATOR);
        stringBuilder.append(placeName);
        stringBuilder.append(PAYMENT_SEPARATOR);
        stringBuilder.append(paymentType.name());
        stringBuilder.append(PAYMENT_SEPARATOR);
        stringBuilder.append(dateTime);

        setValue(mEditor, KEY_PAYMENT_INFORMATION, stringBuilder.toString());
    }

    public void setPaymentInformation(Constants.PlaceType placeType, String placeName, PlacePaymentInformation.PaymentType paymentType, String checkInDate, String checkOutDate)
    {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(placeType.name());
        stringBuilder.append(PAYMENT_SEPARATOR);
        stringBuilder.append(placeName);
        stringBuilder.append(PAYMENT_SEPARATOR);
        stringBuilder.append(paymentType.name());
        stringBuilder.append(PAYMENT_SEPARATOR);
        stringBuilder.append(checkInDate);
        stringBuilder.append(PAYMENT_SEPARATOR);
        stringBuilder.append(checkOutDate);

        setValue(mEditor, KEY_PAYMENT_INFORMATION, stringBuilder.toString());
    }

    public void clearPaymentInformation()
    {
        removeValue(mEditor, KEY_PAYMENT_INFORMATION);
    }

    public boolean isHomeTextMessageAreaEnabled()
    {
        return getValue(mPreferences, KEY_SETTING_HOME_MESSAGE_AREA_ENABLED, true);
    }

    public void setHomeTextMessageAreaEnabled(boolean isEnabled)
    {
        setValue(mEditor, KEY_SETTING_HOME_MESSAGE_AREA_ENABLED, isEnabled);
    }
}
