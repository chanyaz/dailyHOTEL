package com.twoheart.dailyhotel.util.analytics;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ExLog;
import com.kakao.ad.common.json.CompleteRegistration;
import com.kakao.ad.common.json.Product;
import com.kakao.ad.common.json.Purchase;
import com.kakao.ad.common.json.ViewContent;
import com.kakao.ad.tracker.KakaoAdTracker;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.DailyDeepLink;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

public class KakaoManager extends BaseAnalyticsManager
{
    private static final boolean DEBUG = Constants.DEBUG;
    private static final String TAG = "[KakaoManager]";

    Context mContext;

    public KakaoManager(Context context)
    {
        KakaoAdTracker.getInstance().init(context, "306637043627835071");
    }

    @Override
    void recordScreen(Activity activity, String screenName, String screenClassOverride)
    {

    }

    @Override
    void recordScreen(Activity activity, String screenName, String screenClassOverride, Map<String, String> params)
    {
        if (activity == null || DailyTextUtils.isTextEmpty(screenName) == true || params == null)
        {
            return;
        }

        switch (screenName)
        {
            case AnalyticsManager.Screen.DAILYHOTEL_DETAIL:
            case AnalyticsManager.Screen.DAILYGOURMET_DETAIL:
            case AnalyticsManager.Screen.DAILYHOTEL_HOTELDETAILVIEW_OUTBOUND:
                ViewContent event = new ViewContent();
                event.content_id = params.get(AnalyticsManager.KeyType.PLACE_INDEX);

                KakaoAdTracker.getInstance().sendEvent(event);

                if (DEBUG == true)
                {
                    ExLog.d(TAG + " : " + event.getEventCode());
                }
                break;
        }
    }

    @Override
    void recordEvent(String category, String action, String label, Map<String, String> params)
    {

    }

    @Override
    void recordEvent(String category, String action, String label, long value, Map<String, String> params)
    {

    }

    @Override
    void recordDeepLink(DailyDeepLink dailyDeepLink)
    {

    }

    @Override
    void setUserInformation(String index, String userType)
    {

    }

    @Override
    void setUserBirthday(String birthday)
    {

    }

    @Override
    void setUserName(String name)
    {

    }

    @Override
    void setExceedBonus(boolean isExceedBonus)
    {

    }

    @Override
    void onActivityCreated(Activity activity, Bundle bundle)
    {

    }

    @Override
    void onActivityStarted(Activity activity)
    {

    }

    @Override
    void onActivityStopped(Activity activity)
    {

    }

    @Override
    void onActivityResumed(Activity activity)
    {

    }

    @Override
    void onActivityPaused(Activity activity)
    {

    }

    @Override
    void onActivitySaveInstanceState(Activity activity, Bundle bundle)
    {

    }

    @Override
    void onActivityDestroyed(Activity activity)
    {

    }

    @Override
    void currentAppVersion(String version)
    {

    }

    @Override
    void addCreditCard(String cardType)
    {

    }

    @Override
    void updateCreditCard(String cardTypes)
    {

    }

    @Override
    void signUpSocialUser(String userIndex, String email, String name, String gender, String phoneNumber, String userType, String callByScreen)
    {
        CompleteRegistration event = new CompleteRegistration();

        KakaoAdTracker.getInstance().sendEvent(event);

        if (DEBUG == true)
        {
            ExLog.d(TAG + " : " + event.getEventCode());
        }
    }

    @Override
    void signUpDailyUser(String userIndex, String email, String name, String phoneNumber, String birthday, String userType, String recommender, String callByScreen)
    {
        CompleteRegistration event = new CompleteRegistration();

        KakaoAdTracker.getInstance().sendEvent(event);

        if (DEBUG == true)
        {
            ExLog.d(TAG + " : " + event.getEventCode());
        }
    }

    @Override
    void purchaseCompleteHotel(String aggregationId, Map<String, String> params)
    {
        Purchase event = new Purchase();

        Product product = new Product();
        product.name = params.get(AnalyticsManager.KeyType.PLACE_INDEX);
        product.price = Double.parseDouble(params.get(AnalyticsManager.KeyType.PAYMENT_PRICE));
        product.quantity = 1;

        event.setProducts(new ArrayList(Arrays.asList(product)));

        KakaoAdTracker.getInstance().sendEvent(event);

        if (DEBUG == true)
        {
            ExLog.d(TAG + " : " + String.format(Locale.KOREA, "%s, %s, %s, %s", event.getEventCode(), product.name, product.price, product.quantity));
        }
    }

    @Override
    void purchaseCompleteStayOutbound(String aggregationId, Map<String, String> params)
    {
        Purchase event = new Purchase();

        Product product = new Product();
        product.name = params.get(AnalyticsManager.KeyType.PLACE_INDEX);
        product.price = Double.parseDouble(params.get(AnalyticsManager.KeyType.PAYMENT_PRICE));
        product.quantity = 1;

        event.setProducts(new ArrayList(Arrays.asList(product)));

        KakaoAdTracker.getInstance().sendEvent(event);

        if (DEBUG == true)
        {
            ExLog.d(TAG + " : " + String.format(Locale.KOREA, "%s, %s, %s, %s", event.getEventCode(), product.name, product.price, product.quantity));
        }
    }

    @Override
    void purchaseCompleteGourmet(String aggregationId, Map<String, String> params)
    {
        Purchase event = new Purchase();

        Product product = new Product();
        product.name = params.get(AnalyticsManager.KeyType.PLACE_INDEX);
        product.price = Double.parseDouble(params.get(AnalyticsManager.KeyType.PAYMENT_PRICE));
        product.quantity = 1;

        event.setProducts(new ArrayList(Arrays.asList(product)));

        KakaoAdTracker.getInstance().sendEvent(event);

        if (DEBUG == true)
        {
            ExLog.d(TAG + " : " + String.format(Locale.KOREA, "%s, %s, %s, %s", event.getEventCode(), product.name, product.price, product.quantity));
        }
    }

    @Override
    void startDeepLink(Uri deepLinkUri)
    {

    }

    @Override
    void startApplication()
    {

    }

    @Override
    void onRegionChanged(String country, String provinceName)
    {

    }

    @Override
    void setPushEnabled(boolean onOff, String pushSettingType)
    {

    }

    @Override
    void purchaseWithCoupon(Map<String, String> param)
    {

    }

    @Override
    void onSearch(String keyword, String autoKeyword, String category, int resultCount)
    {

    }
}
