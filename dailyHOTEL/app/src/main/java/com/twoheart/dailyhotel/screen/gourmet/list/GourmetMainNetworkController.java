package com.twoheart.dailyhotel.screen.gourmet.list;

import android.content.Context;

import com.twoheart.dailyhotel.model.Area;
import com.twoheart.dailyhotel.model.EventBanner;
import com.twoheart.dailyhotel.model.Province;
import com.twoheart.dailyhotel.network.DailyMobileAPI;
import com.twoheart.dailyhotel.place.base.OnBaseNetworkControllerListener;
import com.twoheart.dailyhotel.place.manager.PlaceEventBannerManager;
import com.twoheart.dailyhotel.place.networkcontroller.PlaceMainNetworkController;
import com.twoheart.dailyhotel.util.ExLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class GourmetMainNetworkController extends PlaceMainNetworkController
{
    public GourmetMainNetworkController(Context context, String networkTag, OnBaseNetworkControllerListener listener)
    {
        super(context, networkTag, listener);
    }

    @Override
    public void requestEventBanner()
    {
        DailyMobileAPI.getInstance(mContext).requestEventBannerList(mNetworkTag, "gourmet", mEventBannerListCallback);
    }

    @Override
    public void requestRegionList()
    {
        DailyMobileAPI.getInstance(mContext).requestGourmetRegionList(mNetworkTag, mRegionListCallback);
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

                        JSONArray provinceArray = dataJSONObject.getJSONArray("province");
                        ArrayList<Province> provinceList = makeProvinceList(provinceArray);

                        JSONArray areaJSONArray = dataJSONObject.getJSONArray("area");
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
