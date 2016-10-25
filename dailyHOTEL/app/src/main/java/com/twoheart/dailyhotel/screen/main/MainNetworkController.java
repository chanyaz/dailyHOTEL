package com.twoheart.dailyhotel.screen.main;

import android.content.Context;

import com.android.volley.VolleyError;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.network.DailyNetworkAPI;
import com.twoheart.dailyhotel.network.response.DailyHotelJsonResponseListener;
import com.twoheart.dailyhotel.place.base.BaseNetworkController;
import com.twoheart.dailyhotel.place.base.OnBaseNetworkControllerListener;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.DailyCalendar;
import com.twoheart.dailyhotel.util.DailyPreference;
import com.twoheart.dailyhotel.util.ExLog;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;

import org.json.JSONObject;

import java.util.Map;

public class MainNetworkController extends BaseNetworkController
{
    public interface OnNetworkControllerListener extends OnBaseNetworkControllerListener
    {
        void updateNewEvent(boolean isNewEvent, boolean isNewCoupon, boolean isNewNotices);

        void onSatisfactionGourmet(String ticketName, int reservationIndex, long checkInTime);

        void onSatisfactionHotel(String hotelName, int reservationIndex, long checkInTime, long checkOutTime);

        void onCheckServerResponse(String title, String message);

        void onAppVersionResponse(String currentVersion, String forceVersion);

        void onConfigurationResponse();

        void onNoticeAgreement(String message, boolean isFirstTimeBuyer);

        void onNoticeAgreementResult(String agreeMessage, String cancelMessage);

        void onCommonDateTime(String currentDateTime, String openDateTime, String closeDateTime);
    }

    public MainNetworkController(Context context, String networkTag, OnNetworkControllerListener listener)
    {
        super(context, networkTag, listener);
    }

    protected void requestCheckServer()
    {
        // 서버 상태 체크
        DailyNetworkAPI.getInstance(mContext).requestCheckServer(mNetworkTag, mStatusHealthCheckJsonResponseListener);
    }

    //    public void requestConfiguration()
    //    {
    //        DailyNetworkAPI.getInstance(mContext).requestCompanyInformation(mNetworkTag, mCompanyInformationJsonResponseListener, mCompanyInformationJsonResponseListener);
    //    }

    public void requestUserInformation()
    {
        DailyNetworkAPI.getInstance(mContext).requestUserProfile(mNetworkTag, mUserProfileJsonResponseListener);
    }

    /**
     * 이벤트가 있는지를 요청한다
     */
    protected void requestCommonDatetime()
    {
        DailyNetworkAPI.getInstance(mContext).requestCommonDatetime(mNetworkTag, new DailyHotelJsonResponseListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
            }

            @Override
            public void onResponse(String url, Map<String, String> params, JSONObject response)
            {
                try
                {
                    int msgCode = response.getInt("msgCode");

                    if (msgCode == 100)
                    {
                        JSONObject dataJSONObject = response.getJSONObject("data");

                        String currentDateTime = dataJSONObject.getString("currentDateTime");
                        String openDateTime = dataJSONObject.getString("openDateTime");
                        String closeDateTime = dataJSONObject.getString("closeDateTime");

                        ((OnNetworkControllerListener) mOnNetworkControllerListener).onCommonDateTime(currentDateTime, openDateTime, closeDateTime);
                    } else
                    {
                        String message = response.getString("msg");
                    }
                } catch (Exception e)
                {
                    ExLog.d(e.toString());
                }
            }
        });
    }

    protected void requestEventNCouponNNoticeNewCount(String lastEventTime, String lastCouponTime, String lastNoticeTime)
    {
        if (Util.isTextEmpty(lastEventTime, lastCouponTime, lastNoticeTime) == true)
        {
            return;
        }

        DailyNetworkAPI.getInstance(mContext).requestEventNCouponNNoticeNewCount(mNetworkTag, lastEventTime, lastCouponTime, lastNoticeTime, mDailyEventCountJsonResponseListener);
    }

    protected void requestVersion()
    {
        DailyNetworkAPI.getInstance(mContext).requestCommonVer(mNetworkTag, mAppVersionJsonResponseListener);
    }

    protected void requestGourmetIsExistRating()
    {
        DailyNetworkAPI.getInstance(mContext).requestGourmetIsExistRating(mNetworkTag, mGourmetSatisfactionRatingExistJsonResponseListener);
    }

    public void registerNotificationId(final String registrationId, String userIndex)
    {
        DailyHotelJsonResponseListener dailyHotelJsonResponseListener = new DailyHotelJsonResponseListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {

            }

            @Override
            public void onResponse(String url, Map<String, String> params, JSONObject response)
            {
                try
                {
                    int msgCode = response.getInt("msgCode");

                    if (msgCode == 100 && response.has("data") == true)
                    {
                        JSONObject jsonObject = response.getJSONObject("data");

                        int uid = jsonObject.getInt("uid");
                        DailyPreference.getInstance(mContext).setNotificationUid(uid);
                        DailyPreference.getInstance(mContext).setGCMRegistrationId(registrationId);
                    }
                } catch (Exception e)
                {
                    ExLog.d(e.toString());
                }
            }
        };

        int uid = DailyPreference.getInstance(mContext).getNotificationUid();
        if (uid < 0)
        {
            DailyNetworkAPI.getInstance(mContext).requestUserRegisterNotification(mNetworkTag, registrationId, dailyHotelJsonResponseListener);
        } else
        {
            if (registrationId.equalsIgnoreCase(DailyPreference.getInstance(mContext).getGCMRegistrationId()) == false)
            {
                DailyNetworkAPI.getInstance(mContext).requestUserUpdateNotification(mNetworkTag, userIndex, registrationId, Integer.toString(uid), dailyHotelJsonResponseListener);
            }
        }
    }

    public void requestNoticeAgreement()
    {
        DailyNetworkAPI.getInstance(mContext).requestNoticeAgreement(mNetworkTag, mNoticeAgreementJsonResponseListener);
    }

    public void requestNoticeAgreementResult(boolean isAgree)
    {
        DailyNetworkAPI.getInstance(mContext).requestNoticeAgreementResult(mNetworkTag, isAgree, mNoticeAgreementResultJsonResponseListener);
    }

    private DailyHotelJsonResponseListener mStatusHealthCheckJsonResponseListener = new DailyHotelJsonResponseListener()
    {
        @Override
        public void onResponse(String url, Map<String, String> params, JSONObject response)
        {
            try
            {
                int msgCode = response.getInt("msg_code");

                if (msgCode == 200)
                {
                    JSONObject jsonObject = response.getJSONObject("data");

                    boolean isSuspend = jsonObject.getBoolean("isSuspend");

                    if (isSuspend == true)
                    {
                        String title = jsonObject.getString("messageTitle");
                        String message = jsonObject.getString("messageBody");

                        ((OnNetworkControllerListener) mOnNetworkControllerListener).onCheckServerResponse(title, message);
                    } else
                    {
                        ((OnNetworkControllerListener) mOnNetworkControllerListener).onCheckServerResponse(null, null);
                    }
                }
            } catch (Exception e)
            {
                ExLog.d(e.toString());
            }
        }

        @Override
        public void onErrorResponse(VolleyError volleyError)
        {
            ((OnNetworkControllerListener) mOnNetworkControllerListener).onCheckServerResponse(null, null);
        }
    };

    private DailyHotelJsonResponseListener mAppVersionJsonResponseListener = new DailyHotelJsonResponseListener()
    {
        @Override
        public void onResponse(String url, Map<String, String> params, JSONObject response)
        {
            try
            {
                int msgCode = response.getInt("msgCode");

                if (msgCode == 100)
                {
                    JSONObject dataJSONObject = response.getJSONObject("data");

                    String maxVersionName;
                    String minVersionName;

                    switch (Constants.RELEASE_STORE)
                    {
                        case T_STORE:
                            maxVersionName = dataJSONObject.getString("tstoreMax");
                            minVersionName = dataJSONObject.getString("tstoreMin");
                            break;

                        case PLAY_STORE:
                        default:
                            maxVersionName = dataJSONObject.getString("playMax");
                            minVersionName = dataJSONObject.getString("playMin");
                            break;
                    }

                    ((OnNetworkControllerListener) mOnNetworkControllerListener).onAppVersionResponse(maxVersionName, minVersionName);
                } else
                {
                    String message = response.getString("msg");

                    mOnNetworkControllerListener.onErrorPopupMessage(msgCode, message);
                }
            } catch (Exception e)
            {
                mOnNetworkControllerListener.onError(e);
            }
        }

        @Override
        public void onErrorResponse(VolleyError volleyError)
        {
            mOnNetworkControllerListener.onErrorPopupMessage(-1, mContext.getString(R.string.act_base_network_connect));
        }
    };

    //    private DailyHotelJsonResponseListener mCompanyInformationJsonResponseListener = new DailyHotelJsonResponseListener()
    //    {
    //        @Override
    //        public void onResponse(String url, Map<String, String> params, JSONObject response)
    //        {
    //            try
    //            {
    //                int msgCode = response.getInt("msg_code");
    //
    //                if (msgCode == 0)
    //                {
    //                    JSONObject jsonObject = response.getJSONObject("data");
    //                    JSONObject companyJSONObject = jsonObject.getJSONObject("companyInfo");
    //
    //                    String companyName = companyJSONObject.getString("name");
    //                    String companyCEO = companyJSONObject.getString("ceo");
    //                    String companyBizRegNumber = companyJSONObject.getString("bizRegNumber");
    //                    String companyItcRegNumber = companyJSONObject.getString("itcRegNumber");
    //                    String address = companyJSONObject.getString("address1");
    //                    String phoneNumber = companyJSONObject.getString("phoneNumber1");
    //                    String fax = companyJSONObject.getString("fax1");
    //                    String privacyEmail = companyJSONObject.getString("privacyManager");
    //
    //                    DailyPreference.getInstance(mContext).setCompanyInformation(companyName//
    //                        , companyCEO, companyBizRegNumber, companyItcRegNumber, address, phoneNumber, fax, privacyEmail);
    //                }
    //
    //                ((OnNetworkControllerListener) mOnNetworkControllerListener).onConfigurationResponse();
    //            } catch (Exception e)
    //            {
    //                mOnNetworkControllerListener.onError(e);
    //            }
    //        }
    //
    //        @Override
    //        public void onErrorResponse(VolleyError volleyError)
    //        {
    //            mOnNetworkControllerListener.onErrorPopupMessage(-1, mContext.getString(R.string.act_base_network_connect));
    //        }
    //    };

    private DailyHotelJsonResponseListener mDailyEventCountJsonResponseListener = new DailyHotelJsonResponseListener()
    {
        @Override
        public void onErrorResponse(VolleyError volleyError)
        {
        }

        @Override
        public void onResponse(String url, Map<String, String> params, JSONObject response)
        {
            try
            {
                boolean isExistNewEvent = false;
                boolean isExistNewCoupon = false;
                boolean isExistNewNotices = false;

                int msgCode = response.getInt("msgCode");

                if (msgCode == 100)
                {
                    JSONObject dataJSONObject = response.getJSONObject("data");

                    isExistNewEvent = dataJSONObject.getBoolean("isExistNewEvent");
                    isExistNewCoupon = dataJSONObject.getBoolean("isExistNewCoupon");
                    isExistNewNotices = dataJSONObject.getBoolean("isExistNewNotices");
                }

                ((OnNetworkControllerListener) mOnNetworkControllerListener).updateNewEvent(isExistNewEvent, isExistNewCoupon, isExistNewNotices);
            } catch (Exception e)
            {
                ExLog.d(e.toString());
            }
        }
    };

    private DailyHotelJsonResponseListener mGourmetSatisfactionRatingExistJsonResponseListener = new DailyHotelJsonResponseListener()
    {
        @Override
        public void onResponse(String url, Map<String, String> params, JSONObject response)
        {
            try
            {
                int msg_code = response.getInt("msg_code");

                if (msg_code == 0 && response.has("data") == true)
                {
                    JSONObject jsonObject = response.getJSONObject("data");

                    long checkInTime = jsonObject.getLong("sday");
                    String ticketName = jsonObject.getString("ticket_name");
                    int reservationIndex = jsonObject.getInt("reservation_rec_idx");

                    ((OnNetworkControllerListener) mOnNetworkControllerListener).onSatisfactionGourmet(ticketName, reservationIndex, checkInTime);
                } else
                {
                    // 고메 이벤트까지 없으면 첫구매 이벤트 확인한다.
                    requestNoticeAgreement();
                }
            } catch (Exception e)
            {
                ExLog.d(e.toString());
            }
        }

        @Override
        public void onErrorResponse(VolleyError volleyError)
        {

        }
    };

    private DailyHotelJsonResponseListener mHotelSatisfactionRatingExistJsonResponseListener = new DailyHotelJsonResponseListener()
    {
        @Override
        public void onResponse(String url, Map<String, String> params, JSONObject response)
        {
            try
            {
                int msgCode = response.getInt("msg_code");

                if (msgCode == 0 && response.has("data") == true)
                {
                    JSONObject jsonObject = response.getJSONObject("data");

                    //					String guestName = jsonObject.getString("guest_name");
                    //					String roomName = jsonObject.getString("room_name");
                    long checkInDate = jsonObject.getLong("checkin_date");
                    long checkOutDate = jsonObject.getLong("checkout_date");
                    String hotelName = jsonObject.getString("hotel_name");
                    int reservationIndex = jsonObject.getInt("reserv_idx");

                    ((OnNetworkControllerListener) mOnNetworkControllerListener).onSatisfactionHotel(hotelName, reservationIndex, checkInDate, checkOutDate);
                } else
                {
                    requestGourmetIsExistRating();
                }
            } catch (Exception e)
            {
                ExLog.d(e.toString());
            }
        }

        @Override
        public void onErrorResponse(VolleyError volleyError)
        {

        }
    };

    private DailyHotelJsonResponseListener mUserProfileJsonResponseListener = new DailyHotelJsonResponseListener()
    {
        @Override
        public void onResponse(String url, Map<String, String> params, JSONObject response)
        {
            try
            {
                int msgCode = response.getInt("msgCode");

                if (msgCode == 100)
                {
                    JSONObject jsonObject = response.getJSONObject("data");

                    final String userIndex = jsonObject.getString("userIdx");
                    final String userType = jsonObject.has("userType") == true ? jsonObject.getString("userType") : AnalyticsManager.ValueType.EMPTY;
                    AnalyticsManager.getInstance(mContext).setUserInformation(userIndex, userType);

                    AnalyticsManager.getInstance(mContext).startApplication();

                    Util.requestGoogleCloudMessaging(mContext, new Util.OnGoogleCloudMessagingListener()
                    {
                        @Override
                        public void onResult(String registrationId)
                        {
                            if (Util.isTextEmpty(registrationId) == true)
                            {
                                return;
                            }

                            registerNotificationId(registrationId, userIndex);
                        }
                    });

                    // 누적 적립금 판단.
                    DailyNetworkAPI.getInstance(mContext).requestUserProfileBenefit(mNetworkTag, mUserProfileBenefitJsonResponseListener);

                    // 호텔 평가요청
                    DailyNetworkAPI.getInstance(mContext).requestHotelIsExistRating(mNetworkTag, mHotelSatisfactionRatingExistJsonResponseListener);
                } else
                {
                    mOnNetworkControllerListener.onError(null);
                }
            } catch (Exception e)
            {
                mOnNetworkControllerListener.onError(e);
            }
        }

        @Override
        public void onErrorResponse(VolleyError volleyError)
        {
            if (volleyError.networkResponse != null && volleyError.networkResponse.statusCode == 401)
            {
                mOnNetworkControllerListener.onErrorResponse(volleyError);
            } else
            {
                mOnNetworkControllerListener.onErrorPopupMessage(-1, mContext.getString(R.string.act_base_network_connect));
            }
        }
    };

    private DailyHotelJsonResponseListener mUserProfileBenefitJsonResponseListener = new DailyHotelJsonResponseListener()
    {
        @Override
        public void onResponse(String url, Map<String, String> params, JSONObject response)
        {
            try
            {
                int msgCode = response.getInt("msgCode");

                if (msgCode == 100)
                {
                    JSONObject jsonObject = response.getJSONObject("data");

                    boolean isExceedBonus = jsonObject.getBoolean("exceedLimitedBonus");

                    DailyPreference.getInstance(mContext).setUserExceedBonus(isExceedBonus);
                    AnalyticsManager.getInstance(mContext).setExceedBonus(isExceedBonus);
                } else
                {
                    // 에러가 나도 특별히 해야할일은 없다.
                }
            } catch (Exception e)
            {
                mOnNetworkControllerListener.onError(e);
            }
        }

        @Override
        public void onErrorResponse(VolleyError volleyError)
        {
            mOnNetworkControllerListener.onErrorResponse(volleyError);
        }
    };

    private DailyHotelJsonResponseListener mNoticeAgreementJsonResponseListener = new DailyHotelJsonResponseListener()
    {
        @Override
        public void onResponse(String url, Map<String, String> params, JSONObject response)
        {
            try
            {
                int msgCode = response.getInt("msgCode");

                if (msgCode == 100)
                {
                    JSONObject dataJSONObject = response.getJSONObject("data");

                    String message01 = dataJSONObject.getString("description1");
                    String message02 = dataJSONObject.getString("description2");
                    boolean isFirstTimeBuyer = dataJSONObject.getBoolean("isFirstTimeBuyer");

                    String message = message01 + "\n\n" + message02;

                    ((OnNetworkControllerListener) mOnNetworkControllerListener).onNoticeAgreement(message, isFirstTimeBuyer);
                }
            } catch (Exception e)
            {
            }
        }

        @Override
        public void onErrorResponse(VolleyError volleyError)
        {
            mOnNetworkControllerListener.onErrorResponse(volleyError);
        }
    };

    private DailyHotelJsonResponseListener mNoticeAgreementResultJsonResponseListener = new DailyHotelJsonResponseListener()
    {
        @Override
        public void onResponse(String url, Map<String, String> params, JSONObject response)
        {
            try
            {
                int msgCode = response.getInt("msgCode");

                if (msgCode == 100)
                {
                    JSONObject dataJSONObject = response.getJSONObject("data");

                    String agreeAt = dataJSONObject.getString("agreedAt");
                    String description1InAgree = dataJSONObject.getString("description1InAgree");
                    String description2InAgree = dataJSONObject.getString("description2InAgree");
                    String description1InReject = dataJSONObject.getString("description1InReject");
                    String description2InReject = dataJSONObject.getString("description2InReject");

                    //                    agreeAt = Util.simpleDateFormatISO8601toFormat(agreeAt, "yyyy년 MM월 dd일");
                    agreeAt = DailyCalendar.convertDateFormatString(agreeAt, DailyCalendar.ISO_8601_FORMAT, "yyyy년 MM월 dd일");

                    String agreeMessage = description1InAgree.replace("{{DATE}}", "\n" + agreeAt) + "\n\n" + description2InAgree;
                    String cancelMessage = description1InReject.replace("{{DATE}}", "\n" + agreeAt) + "\n\n" + description2InReject;

                    ((OnNetworkControllerListener) mOnNetworkControllerListener).onNoticeAgreementResult(agreeMessage, cancelMessage);
                }
                //            } catch (ParseException e)
                //            {
                //                if (Constants.DEBUG == false)
                //                {
                //                    Crashlytics.log("Url: " + url);
                //                }
                //
                //                mOnNetworkControllerListener.onError(e);
            } catch (Exception e)
            {
                mOnNetworkControllerListener.onError(e);
            }
        }

        @Override
        public void onErrorResponse(VolleyError volleyError)
        {
            mOnNetworkControllerListener.onErrorResponse(volleyError);
        }
    };
}
