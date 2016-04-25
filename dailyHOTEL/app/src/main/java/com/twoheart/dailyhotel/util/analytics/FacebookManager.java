package com.twoheart.dailyhotel.util.analytics;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.applinks.AppLinkData;
import com.twoheart.dailyhotel.LauncherActivity;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.ExLog;
import com.twoheart.dailyhotel.util.Util;

import java.util.Map;

public class FacebookManager implements IBaseAnalyticsManager
{
    private static final boolean DEBUG = Constants.DEBUG;
    private static final String TAG = "[FacebookManager]";
    private static final boolean ENABLED = true;

    private Context mContext;

    public FacebookManager(Context context)
    {
        mContext = context;

        setDeferredDeepLink();
    }

    private void setDeferredDeepLink()
    {
        AppLinkData.fetchDeferredAppLinkData(mContext, new AppLinkData.CompletionHandler()
        {
            @Override
            public void onDeferredAppLinkDataFetched(AppLinkData appLinkData)
            {
                if (ENABLED == false)
                {
                    return;
                }

                if (appLinkData == null)
                {
                    return;
                }

                Intent intent = new Intent(mContext, LauncherActivity.class);
                intent.setData(appLinkData.getTargetUri());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public void recordScreen(String screen, Map<String, String> params)
    {
        if (ENABLED == false)
        {
            return;
        }

        if (params == null)
        {
            if (AnalyticsManager.Screen.DAILYHOTEL_LIST.equalsIgnoreCase(screen) == true)
            {
                AppEventsLogger appEventsLogger = AppEventsLogger.newLogger(mContext);

                Bundle parameters = new Bundle();
                parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, EventParam.HOTEL_LIST);

                appEventsLogger.logEvent(AppEventsConstants.EVENT_NAME_SEARCHED, parameters);

            } else if (AnalyticsManager.Screen.DAILYGOURMET_LIST.equalsIgnoreCase(screen) == true)
            {
                AppEventsLogger appEventsLogger = AppEventsLogger.newLogger(mContext);

                Bundle parameters = new Bundle();
                parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, EventParam.GOURMET_LIST);

                appEventsLogger.logEvent(AppEventsConstants.EVENT_NAME_SEARCHED, parameters);
            }
        } else
        {
            if (AnalyticsManager.Screen.DAILYHOTEL_DETAIL.equalsIgnoreCase(screen) == true)
            {
                AppEventsLogger appEventsLogger = AppEventsLogger.newLogger(mContext);

                Bundle parameters = new Bundle();
                parameters.putString(AppEventsConstants.EVENT_PARAM_DESCRIPTION, params.get(AnalyticsManager.KeyType.NAME));
                parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, params.get(AnalyticsManager.KeyType.PLACE_INDEX));
                parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, AnalyticsManager.Label.HOTEL);
                parameters.putString(EventParam.CHECK_IN_DATE, params.get(AnalyticsManager.KeyType.CHECK_IN));
                parameters.putString(EventParam.CHECK_OUT_DATE, params.get(AnalyticsManager.KeyType.CHECK_OUT));
                parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, "KRW");
                parameters.putString(EventParam.HOTEL_VALUE_TO_SUM, params.get(AnalyticsManager.KeyType.PRICE));
                parameters.putString(EventParam.NUMBER_OF_NIGHTS, params.get(AnalyticsManager.KeyType.QUANTITY));

                appEventsLogger.logEvent(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, parameters);

                if (DEBUG == true)
                {
                    ExLog.d(TAG + "Screen : " + screen + parameters.toString());
                }
            } else if (AnalyticsManager.Screen.DAILYGOURMET_DETAIL.equalsIgnoreCase(screen) == true)
            {
                AppEventsLogger appEventsLogger = AppEventsLogger.newLogger(mContext);

                Bundle parameters = new Bundle();
                parameters.putString(AppEventsConstants.EVENT_PARAM_DESCRIPTION, params.get(AnalyticsManager.KeyType.NAME));
                parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, params.get(AnalyticsManager.KeyType.PLACE_INDEX));
                parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, AnalyticsManager.Label.GOURMET);
                parameters.putString(EventParam.GOURMET_RESERVATION_DATE, params.get(AnalyticsManager.KeyType.DATE));
                parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, "KRW");
                parameters.putString(EventParam.GOURMET_VALUE_TO_SUM, params.get(AnalyticsManager.KeyType.PRICE));

                appEventsLogger.logEvent(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, parameters);

                if (DEBUG == true)
                {
                    ExLog.d(TAG + "Screen : " + screen + parameters.toString());
                }
            } else if (AnalyticsManager.Screen.DAILYHOTEL_PAYMENT.equalsIgnoreCase(screen) == true)
            {
                AppEventsLogger appEventsLogger = AppEventsLogger.newLogger(mContext);

                Bundle parameters = new Bundle();
                parameters.putString(AppEventsConstants.EVENT_PARAM_DESCRIPTION, params.get(AnalyticsManager.KeyType.NAME));
                parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, params.get(AnalyticsManager.KeyType.PLACE_INDEX));
                parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, AnalyticsManager.Label.HOTEL);
                parameters.putString(EventParam.CHECK_IN_DATE, params.get(AnalyticsManager.KeyType.CHECK_IN));
                parameters.putString(EventParam.CHECK_OUT_DATE, params.get(AnalyticsManager.KeyType.CHECK_OUT));
                parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, "KRW");
                parameters.putString(EventParam.HOTEL_VALUE_TO_SUM, params.get(AnalyticsManager.KeyType.TOTAL_PRICE));
                parameters.putString(EventParam.NUMBER_OF_NIGHTS, params.get(AnalyticsManager.KeyType.QUANTITY));

                appEventsLogger.logEvent(AppEventsConstants.EVENT_NAME_INITIATED_CHECKOUT, parameters);

                if (DEBUG == true)
                {
                    ExLog.d(TAG + "Screen : " + screen + parameters.toString());
                }
            } else if (AnalyticsManager.Screen.DAILYGOURMET_PAYMENT.equalsIgnoreCase(screen) == true)
            {
                AppEventsLogger appEventsLogger = AppEventsLogger.newLogger(mContext);

                Bundle parameters = new Bundle();
                parameters.putString(AppEventsConstants.EVENT_PARAM_DESCRIPTION, params.get(AnalyticsManager.KeyType.NAME));
                parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, params.get(AnalyticsManager.KeyType.PLACE_INDEX));
                parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, AnalyticsManager.Label.GOURMET);
                parameters.putString(EventParam.GOURMET_RESERVATION_DATE, params.get(AnalyticsManager.KeyType.DATE));
                parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, "KRW");
                parameters.putString(EventParam.GOURMET_VALUE_TO_SUM, params.get(AnalyticsManager.KeyType.PRICE));

                appEventsLogger.logEvent(AppEventsConstants.EVENT_NAME_INITIATED_CHECKOUT, parameters);

                if (DEBUG == true)
                {
                    ExLog.d(TAG + "Screen : " + screen + parameters.toString());
                }
            }
        }
    }

    @Override
    public void recordEvent(String category, String action, String label, Map<String, String> params)
    {
        if (ENABLED == false)
        {
            return;
        }

        if (AnalyticsManager.Category.NAVIGATION.equalsIgnoreCase(category) == true)
        {
            if (AnalyticsManager.Action.HOTEL_LOCATIONS_CLICKED.equalsIgnoreCase(action) == true)
            {
                AppEventsLogger appEventsLogger = AppEventsLogger.newLogger(mContext);

                Bundle parameters = new Bundle();

                String value = String.format("%s_%s", mContext.getString(R.string.label_hotel), label.replaceAll("-", "_"));
                parameters.putString(AppEventsConstants.EVENT_PARAM_DESCRIPTION, value);

                appEventsLogger.logEvent(AppEventsConstants.EVENT_NAME_SEARCHED, parameters);

                if (DEBUG == true)
                {
                    ExLog.d(TAG + "Event : " + category + " | " + action + " | " + label + " | " + parameters.toString());
                }
            } else if (AnalyticsManager.Action.GOURMET_LOCATIONS_CLICKED.equalsIgnoreCase(action) == true)
            {
                AppEventsLogger appEventsLogger = AppEventsLogger.newLogger(mContext);

                Bundle parameters = new Bundle();

                String value = String.format("%s_%s", mContext.getString(R.string.label_fnb), label.replaceAll("-", "_"));
                parameters.putString(AppEventsConstants.EVENT_PARAM_DESCRIPTION, value);

                appEventsLogger.logEvent(AppEventsConstants.EVENT_NAME_SEARCHED, parameters);

                if (DEBUG == true)
                {
                    ExLog.d(TAG + "Event : " + category + " | " + action + " | " + label + " | " + parameters.toString());
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Special Event
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void setUserIndex(String index)
    {
        if (ENABLED == false)
        {
            return;
        }

        AppEventsLogger appEventsLogger = AppEventsLogger.newLogger(mContext);

        if (Util.isTextEmpty(index) == true)
        {
            appEventsLogger.logEvent(EventName.LOGIN, 0);
        } else
        {
            appEventsLogger.logEvent(EventName.LOGIN, 1);
        }
    }

    @Override
    public void onResume(Activity activity)
    {
        if (ENABLED == false)
        {
            return;
        }

        AppEventsLogger.activateApp(activity);
    }

    @Override
    public void onPause(Activity activity)
    {
        if (ENABLED == false)
        {
            return;
        }

        AppEventsLogger.deactivateApp(activity);
    }

    @Override
    public void addCreditCard(String cardType)
    {
        if (ENABLED == false)
        {
            return;
        }

        AppEventsLogger appEventsLogger = AppEventsLogger.newLogger(mContext);

        Bundle parameters = new Bundle();
        parameters.putString(EventParam.CARD_TYPE, cardType);

        appEventsLogger.logEvent(AppEventsConstants.EVENT_NAME_ADDED_PAYMENT_INFO, parameters);

        if (DEBUG == true)
        {
            ExLog.d(TAG + "addCreditCard : " + parameters.toString());
        }
    }

    @Override
    public void signUpSocialUser(String userIndex, String email, String name, String gender, String phoneNumber, String userType)
    {
        if (ENABLED == false)
        {
            return;
        }

        AppEventsLogger appEventsLogger = AppEventsLogger.newLogger(mContext);

        Bundle parameters = new Bundle();
        parameters.putString(AppEventsConstants.EVENT_PARAM_REGISTRATION_METHOD, userType);

        appEventsLogger.logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION, parameters);

        if (DEBUG == true)
        {
            ExLog.d(TAG + "signUpSocialUser : " + parameters.toString());
        }
    }

    @Override
    public void signUpDailyUser(String userIndex, String email, String name, String phoneNumber, String userType)
    {
        if (ENABLED == false)
        {
            return;
        }

        AppEventsLogger appEventsLogger = AppEventsLogger.newLogger(mContext);

        Bundle parameters = new Bundle();
        parameters.putString(AppEventsConstants.EVENT_PARAM_REGISTRATION_METHOD, userType);

        appEventsLogger.logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION, parameters);

        if (DEBUG == true)
        {
            ExLog.d(TAG + "signUpDailyUser : " + parameters.toString());
        }
    }

    @Override
    public void purchaseCompleteHotel(String transId, Map<String, String> params)
    {
        if (ENABLED == false)
        {
            return;
        }

        AppEventsLogger appEventsLogger = AppEventsLogger.newLogger(mContext);

        Bundle parameters = new Bundle();
        parameters.putString(AppEventsConstants.EVENT_PARAM_DESCRIPTION, params.get(AnalyticsManager.KeyType.NAME));
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, params.get(AnalyticsManager.KeyType.PLACE_INDEX));
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, AnalyticsManager.Label.HOTEL);
        parameters.putString(EventParam.CHECK_IN_DATE, params.get(AnalyticsManager.KeyType.CHECK_IN));
        parameters.putString(EventParam.CHECK_OUT_DATE, params.get(AnalyticsManager.KeyType.CHECK_OUT));
        parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, "KRW");
        parameters.putString(EventParam.HOTEL_VALUE_TO_SUM, params.get(AnalyticsManager.KeyType.PAYMENT_PRICE));
        parameters.putString(EventParam.NUMBER_OF_NIGHTS, params.get(AnalyticsManager.KeyType.QUANTITY));

        appEventsLogger.logEvent(AppEventsConstants.EVENT_NAME_PURCHASED, parameters);

        if (DEBUG == true)
        {
            ExLog.d(TAG + "purchaseCompleteHotel : " + parameters.toString());
        }
    }

    @Override
    public void purchaseCompleteGourmet(String transId, Map<String, String> params)
    {
        if (ENABLED == false)
        {
            return;
        }

        AppEventsLogger appEventsLogger = AppEventsLogger.newLogger(mContext);

        Bundle parameters = new Bundle();
        parameters.putString(AppEventsConstants.EVENT_PARAM_DESCRIPTION, params.get(AnalyticsManager.KeyType.NAME));
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, params.get(AnalyticsManager.KeyType.PLACE_INDEX));
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, AnalyticsManager.Label.GOURMET);
        parameters.putString(EventParam.GOURMET_RESERVATION_DATE, params.get(AnalyticsManager.KeyType.DATE));
        parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, "KRW");
        parameters.putString(EventParam.GOURMET_VALUE_TO_SUM, params.get(AnalyticsManager.KeyType.PAYMENT_PRICE));
        parameters.putString(AppEventsConstants.EVENT_PARAM_NUM_ITEMS, params.get(AnalyticsManager.KeyType.QUANTITY));

        appEventsLogger.logEvent(AppEventsConstants.EVENT_NAME_PURCHASED, parameters);

        if (DEBUG == true)
        {
            ExLog.d(TAG + "purchaseCompleteGourmet : " + parameters.toString());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class EventName
    {
        public static final String LOGIN = "login";
    }

    private static final class EventParam
    {
        public static final String HOTEL_LIST = "HotelList";
        public static final String GOURMET_LIST = "GourmetList";

        public static final String CHECK_IN_DATE = "Check in Date";
        public static final String CHECK_OUT_DATE = "Check out Date";
        public static final String NUMBER_OF_NIGHTS = "Number of Nights";
        public static final String GOURMET_RESERVATION_DATE = "Gourmet Reservation Date";
        public static final String HOTEL_VALUE_TO_SUM = "Hotel valueToSum";
        public static final String GOURMET_VALUE_TO_SUM = "Gourmet valueToSum";
        public static final String CARD_TYPE = "Card Type";
    }
}
