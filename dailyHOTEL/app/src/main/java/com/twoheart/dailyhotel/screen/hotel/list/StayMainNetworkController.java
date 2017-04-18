package com.twoheart.dailyhotel.screen.hotel.list;

import android.content.Context;

import com.daily.base.util.ExLog;
import com.twoheart.dailyhotel.model.Area;
import com.twoheart.dailyhotel.model.EventBanner;
import com.twoheart.dailyhotel.model.Province;
import com.twoheart.dailyhotel.network.DailyMobileAPI;
import com.twoheart.dailyhotel.place.base.OnBaseNetworkControllerListener;
import com.twoheart.dailyhotel.place.manager.PlaceEventBannerManager;
import com.twoheart.dailyhotel.place.networkcontroller.PlaceMainNetworkController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class StayMainNetworkController extends PlaceMainNetworkController
{
    public StayMainNetworkController(Context context, String networkTag, OnBaseNetworkControllerListener listener)
    {
        super(context, networkTag, listener);
    }

    public void requestEventBanner()
    {
        DailyMobileAPI.getInstance(mContext).requestEventBannerList(mNetworkTag, "hotel", mEventBannerListCallback);
    }

    public void requestRegionList()
    {
        DailyMobileAPI.getInstance(mContext).requestStayRegionList(mNetworkTag, mRegionListCallback);
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    // NetworkActionListener
    //////////////////////////////////////////////////////////////////////////////////////////////////////////


    private retrofit2.Callback mRegionListCallback = new retrofit2.Callback<JSONObject>()
    {
        @Override
        public void onResponse(Call<JSONObject> call, Response<JSONObject> response)
        {
            if (response != null && response.isSuccessful() && response.body() != null)
            {
                try
                {
                    JSONObject responseJSONObject = response.body();

                    int msgCode = responseJSONObject.getInt("msgCode");
                    if (msgCode == 100)
                    {
                        JSONObject dataJSONObject = responseJSONObject.getJSONObject("data");

                        JSONArray provinceArray = dataJSONObject.getJSONArray("regionProvince");
                        ArrayList<Province> provinceList = makeProvinceList(provinceArray);

                        JSONArray areaJSONArray = dataJSONObject.getJSONArray("regionArea");
                        ArrayList<Area> areaList = makeAreaList(areaJSONArray);

                        ((OnNetworkControllerListener) mOnNetworkControllerListener).onRegionList(provinceList, areaList);
                    } else
                    {
                        String message = responseJSONObject.getString("msg");

                        mOnNetworkControllerListener.onErrorPopupMessage(msgCode, message);
                    }
                } catch (Exception e)
                {
                    mOnNetworkControllerListener.onError(e);
                }
            } else
            {
                mOnNetworkControllerListener.onErrorResponse(call, response);
            }
        }

        @Override
        public void onFailure(Call<JSONObject> call, Throwable t)
        {
            mOnNetworkControllerListener.onError(t);
        }

        private ArrayList<Province> makeProvinceList(JSONArray jsonArray)
        {
            ArrayList<Province> provinceList = new ArrayList<>();

            try
            {
                int length = jsonArray.length();
                for (int i = 0; i < length; i++)
                {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    try
                    {
                        Province province = new Province(jsonObject, null);

                        provinceList.add(province);
                    } catch (JSONException e)
                    {
                        ExLog.d(e.toString());
                    }
                }
            } catch (Exception e)
            {
                ExLog.d(e.toString());
            }

            return provinceList;
        }

        private ArrayList<Area> makeAreaList(JSONArray jsonArray) throws JSONException
        {
            ArrayList<Area> areaList = new ArrayList<>();

            int length = jsonArray.length();
            for (int i = 0; i < length; i++)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                try
                {
                    Area area = new Area(jsonObject);

                    areaList.add(area);
                } catch (JSONException e)
                {
                    ExLog.d(e.toString());
                }
            }

            return areaList;
        }
    };

    private retrofit2.Callback mEventBannerListCallback = new retrofit2.Callback<JSONObject>()
    {
        @Override
        public void onResponse(Call<JSONObject> call, Response<JSONObject> response)
        {
            if (response != null && response.isSuccessful() && response.body() != null)
            {
                JSONObject responseJSONObject = response.body();

                List<EventBanner> eventBannerList = PlaceEventBannerManager.makeEventBannerList(responseJSONObject);

                ((OnNetworkControllerListener) mOnNetworkControllerListener).onEventBanner(eventBannerList);
            } else
            {
                ((OnNetworkControllerListener) mOnNetworkControllerListener).onEventBanner(null);
            }
        }

        @Override
        public void onFailure(Call<JSONObject> call, Throwable t)
        {
            ((OnNetworkControllerListener) mOnNetworkControllerListener).onEventBanner(null);
        }
    };
}
