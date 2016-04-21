package com.twoheart.dailyhotel.screen.information.member;

import android.content.Context;

import com.android.volley.VolleyError;
import com.twoheart.dailyhotel.network.DailyNetworkAPI;
import com.twoheart.dailyhotel.network.response.DailyHotelJsonResponseListener;
import com.twoheart.dailyhotel.place.base.BaseNetworkController;
import com.twoheart.dailyhotel.place.base.OnBaseNetworkControllerListener;

import org.json.JSONObject;

public class ProfileNetworkController extends BaseNetworkController
{
    protected interface OnNetworkControllerListener extends OnBaseNetworkControllerListener
    {
        void onUserInformation(String email, String name, String phone);
    }

    public ProfileNetworkController(Context context, String networkTag, OnBaseNetworkControllerListener listener)
    {
        super(context, networkTag, listener);
    }

    @Override
    public void onErrorResponse(VolleyError volleyError)
    {
        mOnNetworkControllerListener.onErrorResponse(volleyError);
    }

    public void requestUserInformation()
    {
        DailyNetworkAPI.getInstance().requestUserInformation(mNetworkTag, mUserLogInfoJsonResponseListener, this);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Listener
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private DailyHotelJsonResponseListener mUserLogInfoJsonResponseListener = new DailyHotelJsonResponseListener()
    {
        @Override
        public void onResponse(String url, JSONObject response)
        {
            try
            {
                String email = response.getString("email");
                String name = response.getString("name");
                String phone = response.getString("phone");

                ((OnNetworkControllerListener) mOnNetworkControllerListener).onUserInformation(email, name, phone);
            } catch (Exception e)
            {
                mOnNetworkControllerListener.onError(e);
            }
        }
    };

    //    private DailyHotelJsonResponseListener mUserUpdateJsonResponseListener = new DailyHotelJsonResponseListener()
    //    {
    //        @Override
    //        public void onResponse(String url, JSONObject response)
    //        {
    //            try
    //            {
    //                String result = response.getString("success");
    //                String msg = null;
    //
    //                if (response.length() > 1)
    //                {
    //                    msg = response.getString("msg");
    //                }
    //
    //                if (result.equals("true") == true)
    //                {
    //                    unLockUI();
    //                    DailyToast.showToast(ProfileNetworkController.this, R.string.toast_msg_profile_success_to_change, Toast.LENGTH_SHORT);
    //                    updateTextField();
    //                } else
    //                {
    //                    unLockUI();
    //                    DailyToast.showToast(ProfileNetworkController.this, msg, Toast.LENGTH_LONG);
    //                }
    //            } catch (Exception e)
    //            {
    //                onError(e);
    //            }
    //        }
    //    };
}
