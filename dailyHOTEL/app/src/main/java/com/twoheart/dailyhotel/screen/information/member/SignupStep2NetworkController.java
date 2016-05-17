package com.twoheart.dailyhotel.screen.information.member;

import android.content.Context;

import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.network.DailyNetworkAPI;
import com.twoheart.dailyhotel.network.response.DailyHotelJsonResponseListener;
import com.twoheart.dailyhotel.place.base.BaseNetworkController;
import com.twoheart.dailyhotel.place.base.OnBaseNetworkControllerListener;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.ExLog;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignupStep2NetworkController extends BaseNetworkController
{
    protected interface OnNetworkControllerListener extends OnBaseNetworkControllerListener
    {
        void onVerification(String time);

        void onSignUp(int notificationUid, String gcmRegisterId);

        void onLogin(String authorization);

        void onUserInformation(String userIndex, String email, String name, String phoneNumber);

        void onAlreadyVerification(String phoneNumber);
    }

    public SignupStep2NetworkController(Context context, String networkTag, OnBaseNetworkControllerListener listener)
    {
        super(context, networkTag, listener);
    }

    @Override
    public void onErrorResponse(VolleyError volleyError)
    {
        mOnNetworkControllerListener.onErrorResponse(volleyError);
    }

    public void requestVerfication(String signupKey, String phoneNumber, boolean force)
    {
        DailyNetworkAPI.getInstance(mContext).requestDailyUserSignupVerfication(mNetworkTag, signupKey, phoneNumber, force, mVerificationJsonResponseListener);
    }

    public void requestSingUp(String signupKey, String code)
    {
        DailyNetworkAPI.getInstance(mContext).requestDailyUserSignup(mNetworkTag, signupKey, code, mDailyUserSignupJsonResponseListener);
    }

    public void requestLogin(String email, String password)
    {
        Map<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("pw", password);
        params.put("social_id", "0");
        params.put("user_type", Constants.DAILY_USER);

        DailyNetworkAPI.getInstance(mContext).requestDailyUserSignin(mNetworkTag, params, mDailyUserLoginJsonResponseListener, this);
    }

    public void requestUserInformation()
    {
        DailyNetworkAPI.getInstance(mContext).requestUserInformation(mNetworkTag, mUserInformationJsonResponseListener, mUserInformationJsonResponseListener);
    }

    public void requestGoogleCloudMessagingId()
    {
        Util.requestGoogleCloudMessaging(mContext, new Util.OnGoogleCloudMessagingListener()
        {
            @Override
            public void onResult(final String registrationId)
            {
                if (Util.isTextEmpty(registrationId) == false)
                {
                    DailyNetworkAPI.getInstance(mContext).requestUserRegisterNotification(mNetworkTag, registrationId, new DailyHotelJsonResponseListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError volleyError)
                        {

                        }

                        @Override
                        public void onResponse(String url, JSONObject response)
                        {
                            int uid = -1;

                            try
                            {
                                int msg_code = response.getInt("msgCode");

                                if (msg_code == 0 && response.has("data") == true)
                                {
                                    JSONObject jsonObject = response.getJSONObject("data");

                                    uid = jsonObject.getInt("uid");
                                }
                            } catch (Exception e)
                            {
                                ExLog.d(e.toString());
                            }

                            ((OnNetworkControllerListener) mOnNetworkControllerListener).onSignUp(uid, registrationId);
                        }
                    }, new ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError arg0)
                        {
                            ((OnNetworkControllerListener) mOnNetworkControllerListener).onSignUp(-1, null);
                        }
                    });
                } else
                {
                    ((OnNetworkControllerListener) mOnNetworkControllerListener).onSignUp(-1, null);
                }
            }
        });
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Listener
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private DailyHotelJsonResponseListener mVerificationJsonResponseListener = new DailyHotelJsonResponseListener()
    {
        @Override
        public void onResponse(String url, JSONObject response)
        {
            try
            {
                int msgCode = response.getInt("msgCode");

                switch (msgCode)
                {
                    case 100:
                    {
                        JSONObject dataJONObject = response.getJSONObject("data");
                        String message = dataJONObject.getString("msg");

                        ((OnNetworkControllerListener) mOnNetworkControllerListener).onVerification(message);
                        break;
                    }

                    // 회원 가입 중 세션이 만료되었습니다
                    case 2000:
                    {
                        JSONObject dataJONObject = response.getJSONObject("data");
                        String message = dataJONObject.getString("msg");

                        mOnNetworkControllerListener.onErrorPopupMessage(msgCode, message);
                        break;
                    }

                    // 다른 번호로 이미 인증된 경우
                    case 2001:
                    {
                        JSONObject dataJONObject = response.getJSONObject("data");
                        String phoneNumber = dataJONObject.getString("phone");

                        ((OnNetworkControllerListener) mOnNetworkControllerListener).onAlreadyVerification(phoneNumber);
                        break;
                    }

                    default:
                        mOnNetworkControllerListener.onErrorPopupMessage(msgCode, response.getString("msg"));
                        break;
                }
            } catch (Exception e)
            {
                mOnNetworkControllerListener.onError(e);
            }
        }

        @Override
        public void onErrorResponse(VolleyError volleyError)
        {
            try
            {
                JSONObject jsonObject = new JSONObject(new String(volleyError.networkResponse.data));
                mOnNetworkControllerListener.onErrorPopupMessage(jsonObject.getInt("msgCode"), jsonObject.getString("msg"));
            } catch (Exception e)
            {
                mOnNetworkControllerListener.onErrorResponse(volleyError);
            }
        }
    };

    private DailyHotelJsonResponseListener mDailyUserSignupJsonResponseListener = new DailyHotelJsonResponseListener()
    {
        @Override
        public void onResponse(String url, JSONObject response)
        {
            try
            {
                int msgCode = response.getInt("msgCode");

                if (msgCode == 0)
                {
                    JSONObject jsonObject = response.getJSONObject("data");

                    boolean isSignup = jsonObject.getBoolean("is_signup");

                    if (isSignup == true)
                    {
                        requestGoogleCloudMessagingId();
                        return;
                    }
                }

                String msg = response.getString("msg");

                if (Util.isTextEmpty(msg) == true)
                {
                    msg = mContext.getString(R.string.toast_msg_failed_to_signup);
                }

                mOnNetworkControllerListener.onErrorPopupMessage(msgCode, msg);
            } catch (Exception e)
            {
                mOnNetworkControllerListener.onError(e);
            }
        }

        @Override
        public void onErrorResponse(VolleyError volleyError)
        {
            try
            {
                JSONObject jsonObject = new JSONObject(new String(volleyError.networkResponse.data));
                mOnNetworkControllerListener.onErrorPopupMessage(jsonObject.getInt("msgCode"), jsonObject.getString("msg"));
            } catch (Exception e)
            {
                mOnNetworkControllerListener.onErrorResponse(volleyError);
            }
        }
    };

    private DailyHotelJsonResponseListener mDailyUserLoginJsonResponseListener = new DailyHotelJsonResponseListener()
    {
        @Override
        public void onErrorResponse(VolleyError volleyError)
        {

        }

        @Override
        public void onResponse(String url, JSONObject response)
        {
            try
            {
                int msgCode = response.getInt("msg_code");

                if (msgCode == 0)
                {
                    JSONObject jsonObject = response.getJSONObject("data");

                    boolean isSignin = jsonObject.getBoolean("is_signin");

                    if (isSignin == true)
                    {
                        JSONObject tokenJSONObject = response.getJSONObject("token");
                        String accessToken = tokenJSONObject.getString("access_token");
                        String tokenType = tokenJSONObject.getString("token_type");

                        ((OnNetworkControllerListener) mOnNetworkControllerListener).onLogin(String.format("%s %s", tokenType, accessToken));
                        return;
                    }
                }

                // 로그인이 실패한 경우
                String msg = response.getString("msg");

                if (Util.isTextEmpty(msg) == true)
                {
                    msg = mContext.getString(R.string.toast_msg_failed_to_login);
                }

                mOnNetworkControllerListener.onErrorPopupMessage(msgCode, msg);
            } catch (Exception e)
            {
                mOnNetworkControllerListener.onError(e);
            }
        }
    };

    private DailyHotelJsonResponseListener mUserInformationJsonResponseListener = new DailyHotelJsonResponseListener()
    {
        @Override
        public void onResponse(String url, JSONObject response)
        {
            try
            {
                String userIndex = String.valueOf(response.getInt("idx"));

                AnalyticsManager.getInstance(mContext).setUserIndex(userIndex);

                String name = response.getString("name");
                String email = response.getString("email");
                String phone = response.getString("phone");

                ((OnNetworkControllerListener) mOnNetworkControllerListener).onUserInformation(userIndex, email, name, phone);
            } catch (Exception e)
            {
                mOnNetworkControllerListener.onError(e);
            }
        }

        @Override
        public void onErrorResponse(VolleyError volleyError)
        {
            try
            {
                JSONObject jsonObject = new JSONObject(new String(volleyError.networkResponse.data));
                mOnNetworkControllerListener.onErrorPopupMessage(jsonObject.getInt("msgCode"), jsonObject.getString("msg"));
            } catch (Exception e)
            {
                mOnNetworkControllerListener.onErrorResponse(volleyError);
            }
        }
    };
}
