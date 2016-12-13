package com.twoheart.dailyhotel;

import com.android.volley.VolleyError;
import com.twoheart.dailyhotel.model.Area;
import com.twoheart.dailyhotel.model.EventBanner;
import com.twoheart.dailyhotel.model.Province;
import com.twoheart.dailyhotel.model.Review;
import com.twoheart.dailyhotel.model.SaleTime;
import com.twoheart.dailyhotel.network.DailyNetworkAPI;
import com.twoheart.dailyhotel.network.request.DailyHotelJsonRequest;
import com.twoheart.dailyhotel.network.response.DailyHotelJsonResponseListener;
import com.twoheart.dailyhotel.screen.hotel.list.StayMainNetworkController;
import com.twoheart.dailyhotel.screen.main.MainNetworkController;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.DailyAssert;
import com.twoheart.dailyhotel.util.DailyPreference;
import com.twoheart.dailyhotel.util.ExLog;
import com.twoheart.dailyhotel.util.Util;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by android_sam on 2016. 12. 12..
 */

public class NetworkApiTest extends ApplicationTest
{
    protected static final String TAG = NetworkApiTest.class.getSimpleName();

    protected SaleTime mSaleTime;


    public NetworkApiTest()
    {
        super();
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    public void testLoginByDailyUser()
    {
        HashMap<String, String> params = new HashMap<>();
        params.put("email", DailyHotelJsonRequest.getUrlDecoderEx(Const.TEST_EMAIL));
        params.put("pw", DailyHotelJsonRequest.getUrlDecoderEx(Const.TEST_PASSWORD));
        params.put("social_id", "0");
        params.put("user_type", Constants.DAILY_USER);

        DailyNetworkAPI.getInstance(application).requestDailyUserSignin(TAG, params, new DailyHotelJsonResponseListener()
        {
            @Override
            public void onResponse(String url, Map<String, String> params, JSONObject response)
            {
                ExLog.d("Url : " + url);
                try
                {
                    int msgCode = response.getInt("msg_code");
                    DailyAssert.assertEquals(0, msgCode);

                    if (msgCode == 0)
                    {
                        JSONObject jsonObject = response.getJSONObject("data");

                        boolean isLogin = jsonObject.getBoolean("is_signin");
                        DailyAssert.assertTrue(isLogin);

                        if (isLogin == true)
                        {
                            DailyPreference.getInstance(application).setLastestCouponTime("");

                            JSONObject dataJSONObject = jsonObject.getJSONObject("data");
                            JSONObject tokenJSONObject = jsonObject.getJSONObject("token");
                            String accessToken = tokenJSONObject.getString("access_token");
                            String tokenType = tokenJSONObject.getString("token_type");

                            JSONObject userJSONObject = dataJSONObject.getJSONObject("user");
                            String userIndex = userJSONObject.getString("idx");
                            String email = userJSONObject.getString("email");
                            String name = userJSONObject.getString("name");
                            String recommender = userJSONObject.getString("rndnum");
                            String userType = userJSONObject.getString("userType");
                            //        String phoneNumber = userJSONObject.getString("phone");
                            String birthday = userJSONObject.getString("birthday");

                            DailyPreference.getInstance(application).setAuthorization(String.format("%s %s", tokenType, accessToken));
                            DailyPreference.getInstance(application).setUserInformation(userType, email, name, birthday, recommender);
                            return;
                        }
                    }

                    // 로그인이 실패한 경우
                    String msg = response.getString("msg");

                    if (Util.isTextEmpty(msg) == true)
                    {
                        msg = application.getResources().getString(R.string.toast_msg_failed_to_login);
                    }

                    DailyAssert.fail(msg);
                } catch (Exception e)
                {
                    DailyAssert.fail(e.getMessage());
                }
            }

            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                DailyAssert.fail(volleyError.getMessage());
            }
        });
    }

    public void testMainNetworkController()
    {
        MainNetworkController mainNetworkController = new MainNetworkController(this.application, TAG, new MainNetworkController.OnNetworkControllerListener()
        {
            @Override
            public void updateNewEvent(boolean isNewEvent, boolean isNewCoupon, boolean isNewNotices)
            {

            }

            @Override
            public void onReviewGourmet(Review review)
            {

            }

            @Override
            public void onReviewHotel(Review review)
            {

            }

            @Override
            public void onCheckServerResponse(String title, String message)
            {

            }

            @Override
            public void onAppVersionResponse(String currentVersion, String forceVersion)
            {

            }

            @Override
            public void onConfigurationResponse()
            {

            }

            @Override
            public void onNoticeAgreement(String message, boolean isFirstTimeBuyer)
            {

            }

            @Override
            public void onNoticeAgreementResult(String agreeMessage, String cancelMessage)
            {

            }

            @Override
            public void onCommonDateTime(long currentDateTime, long openDateTime, long closeDateTime)
            {
                mSaleTime = new SaleTime();
                mSaleTime.setCurrentTime(currentDateTime);
                //                mSaleTime.setDailyTime(dailyDateTime);
                mSaleTime.setOffsetDailyDay(0);
            }

            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                DailyAssert.fail(volleyError == null ? "" : volleyError.getMessage());
            }

            @Override
            public void onError(Exception e)
            {
                DailyAssert.fail(e == null ? "" : e.getMessage());
            }

            @Override
            public void onErrorPopupMessage(int msgCode, String message)
            {
                DailyAssert.fail(message);
            }

            @Override
            public void onErrorToastMessage(String message)
            {
                DailyAssert.fail(message);
            }
        });

        mainNetworkController.requestCheckServer();

        mainNetworkController.requestVersion();

        mainNetworkController.requestCommonDatetime();

        mainNetworkController.requestUserInformation();

        mainNetworkController.requestNoticeAgreement();

        mainNetworkController.requestNoticeAgreementResult(true);

        mainNetworkController.requestNoticeAgreementResult(false);

        mainNetworkController.requestReviewGourmet();
    }

    public void testStayNetworkController()
    {
        StayMainNetworkController stayMainNetworkController = new StayMainNetworkController(application, TAG, new StayMainNetworkController.OnNetworkControllerListener()
        {
            @Override
            public void onDateTime(long currentDateTime, long dailyDateTime)
            {
                mSaleTime = new SaleTime();
                mSaleTime.setCurrentTime(currentDateTime);
                mSaleTime.setDailyTime(dailyDateTime);
                mSaleTime.setOffsetDailyDay(0);
            }

            @Override
            public void onEventBanner(List<EventBanner> eventBannerList)
            {

            }

            @Override
            public void onRegionList(List<Province> provinceList, List<Area> areaList)
            {

            }

            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                DailyAssert.fail(volleyError == null ? "" : volleyError.getMessage());
            }

            @Override
            public void onError(Exception e)
            {
                DailyAssert.fail(e == null ? "" : e.getMessage());
            }

            @Override
            public void onErrorPopupMessage(int msgCode, String message)
            {
                DailyAssert.fail("error : msgCode=" + msgCode + ", message=" + message);
            }

            @Override
            public void onErrorToastMessage(String message)
            {
                DailyAssert.fail("error : " + message);
            }
        });

        stayMainNetworkController.requestDateTime();

        stayMainNetworkController.requestRegionList();

        stayMainNetworkController.requestEventBanner();
    }
}