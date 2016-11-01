package com.twoheart.dailyhotel.screen.information.wishlist;

import android.content.Context;

import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;
import com.twoheart.dailyhotel.model.Stay;
import com.twoheart.dailyhotel.network.response.DailyHotelJsonResponseListener;
import com.twoheart.dailyhotel.place.base.BaseNetworkController;
import com.twoheart.dailyhotel.place.base.OnBaseNetworkControllerListener;
import com.twoheart.dailyhotel.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by android_sam on 2016. 11. 1..
 */

public class StayWishListNetworkController extends BaseNetworkController
{
    public StayWishListNetworkController(Context context, String networkTag, OnBaseNetworkControllerListener listener)
    {
        super(context, networkTag, listener);
    }

    public interface OnNetworkControllerListener extends OnBaseNetworkControllerListener
    {
        void onStayWishList(ArrayList<Stay> list);

        void onDeleteStayWishListItem(int position);
    }

    public void requestStayWishList()
    {
        ((StayWishListNetworkController.OnNetworkControllerListener) mOnNetworkControllerListener).onStayWishList(null);
    }

    public void requestDeleteStayWishListItem() {
        ((StayWishListNetworkController.OnNetworkControllerListener) mOnNetworkControllerListener).onDeleteStayWishListItem(-1);
    }


    private DailyHotelJsonResponseListener mListJsonResponseListener = new DailyHotelJsonResponseListener()
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
                    JSONArray hotelJSONArray = null;

                    if (dataJSONObject.has("hotelSales") == true)
                    {
                        hotelJSONArray = dataJSONObject.getJSONArray("hotelSales");
                    }

                    //                    int page;
                    String imageUrl;

                    ArrayList<Stay> stayList;

                    if (hotelJSONArray != null)
                    {
                        imageUrl = dataJSONObject.getString("imgUrl");
                        int nights = dataJSONObject.getInt("stays");
                        stayList = makeStayList(hotelJSONArray, imageUrl, nights);
                    } else
                    {
                        stayList = new ArrayList<>();
                    }

                    //                    try
                    //                    {
                    //                        Uri uri = Uri.parse(url);
                    //                        String pageString = uri.getQueryParameter("page");
                    //                        page = Integer.parseInt(pageString);
                    //
                    //                    } catch (Exception e)
                    //                    {
                    //                        page = 0;
                    //                    }

                    ((StayWishListNetworkController.OnNetworkControllerListener) mOnNetworkControllerListener).onStayWishList(stayList);
                } else
                {
                    String message = response.getString("msg");

                    if (Constants.DEBUG == false)
                    {
                        Crashlytics.log(url);
                    }

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
            mOnNetworkControllerListener.onErrorResponse(volleyError);
        }

        private ArrayList<Stay> makeStayList(JSONArray jsonArray, String imageUrl, int nights) throws JSONException
        {
            if (jsonArray == null)
            {
                return new ArrayList<>();
            }

            int length = jsonArray.length();
            ArrayList<Stay> stayList = new ArrayList<>(length);
            JSONObject jsonObject;
            Stay stay;

            for (int i = 0; i < length; i++)
            {
                jsonObject = jsonArray.getJSONObject(i);

                stay = new Stay();

                if (stay.setStay(jsonObject, imageUrl, nights) == true)
                {
                    stayList.add(stay); // 추가.
                }
            }

            return stayList;
        }
    };

    DailyHotelJsonResponseListener mDeleteItemJsonResponseListener = new DailyHotelJsonResponseListener()
    {
        @Override
        public void onResponse(String url, Map<String, String> params, JSONObject response)
        {
            try
            {
                int msgCode = response.getInt("msgCode");
                if (msgCode == 100)
                {
//                    JSONObject dataJSONObject = response.getJSONObject("data");

                    ((StayWishListNetworkController.OnNetworkControllerListener) mOnNetworkControllerListener).onDeleteStayWishListItem(-1);
                } else
                {
                    String message = response.getString("msg");

                    if (Constants.DEBUG == false)
                    {
                        Crashlytics.log(url);
                    }

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
            mOnNetworkControllerListener.onErrorResponse(volleyError);
        }
    };
}
