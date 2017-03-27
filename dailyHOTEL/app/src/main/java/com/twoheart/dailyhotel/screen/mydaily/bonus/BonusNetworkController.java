package com.twoheart.dailyhotel.screen.mydaily.bonus;

import android.content.Context;

import com.twoheart.dailyhotel.model.Bonus;
import com.twoheart.dailyhotel.network.DailyMobileAPI;
import com.twoheart.dailyhotel.place.base.BaseNetworkController;
import com.twoheart.dailyhotel.place.base.OnBaseNetworkControllerListener;
import com.twoheart.dailyhotel.util.DailyCalendar;
import com.twoheart.dailyhotel.util.DailyUserPreference;
import com.twoheart.dailyhotel.util.ExLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class BonusNetworkController extends BaseNetworkController
{
    protected interface OnNetworkControllerListener extends OnBaseNetworkControllerListener
    {
        void onUserInformation(String name, String recommendCode, boolean isExceedBonus);

        void onBonusHistoryList(List<Bonus> list);

        void onBonus(int bonus);
    }

    public BonusNetworkController(Context context, String networkTag, OnBaseNetworkControllerListener listener)
    {
        super(context, networkTag, listener);
    }

    public void requestProfileBenefit()
    {
        DailyMobileAPI.getInstance(mContext).requestUserProfileBenefit(mNetworkTag, mUserProfileBenefitCallback);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Listener
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    retrofit2.Callback mUserBonusListCallback = new retrofit2.Callback<JSONObject>()
    {
        @Override
        public void onResponse(Call<JSONObject> call, Response<JSONObject> response)
        {
            if (response != null && response.isSuccessful() && response.body() != null)
            {
                //적립금 내역리스트
                try
                {
                    JSONObject responseJSONObject = response.body();

                    List<Bonus> list = new ArrayList<>();

                    if (responseJSONObject.has("history") == true && responseJSONObject.isNull("history") == false)
                    {
                        JSONArray jsonArray = responseJSONObject.getJSONArray("history");
                        int length = jsonArray.length();

                        for (int i = 0; i < length; i++)
                        {
                            JSONObject historyObj = jsonArray.getJSONObject(i);

                            String content = historyObj.getString("content");
                            String expires = historyObj.getString("expires");
                            int bonus = historyObj.getInt("bonus");

                            String[] dates = expires.split("\\.");

                            try
                            {
                                int year = Integer.parseInt(dates[0]);
                                int month = Integer.parseInt(dates[1]) - 1;
                                int day = Integer.parseInt(dates[2]);

                                Calendar calendar = DailyCalendar.getInstance();
                                calendar.set(year, month, day);

                                expires = DailyCalendar.format(calendar.getTime(), "yyyy.MM.dd(EEE)");
                            } catch (Exception e)
                            {
                                ExLog.d(e.toString());
                            }

                            list.add(new Bonus(content, bonus, expires));
                        }
                    }

                    ((OnNetworkControllerListener) mOnNetworkControllerListener).onBonusHistoryList(list);
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
    };

    retrofit2.Callback mUserProfileCallback = new retrofit2.Callback<JSONObject>()
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
                        JSONObject jsonObject = responseJSONObject.getJSONObject("data");

                        String recommendCode = jsonObject.getString("referralCode");
                        String name = jsonObject.getString("name");
                        boolean isExceedBonus = DailyUserPreference.getInstance(mContext).isExceedBonus();

                        ((OnNetworkControllerListener) mOnNetworkControllerListener).onUserInformation(recommendCode, name, isExceedBonus);

                        // 적립금 목록 요청.
                        DailyMobileAPI.getInstance(mContext).requestUserBonus(mNetworkTag, mUserBonusListCallback);
                    } else
                    {
                        String msg = responseJSONObject.getString("msg");
                        mOnNetworkControllerListener.onErrorToastMessage(msg);
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
    };

    private retrofit2.Callback mUserProfileBenefitCallback = new retrofit2.Callback<JSONObject>()
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

                        int bonus = dataJSONObject.getInt("bonusAmount");
                        boolean isExceedBonus = dataJSONObject.getBoolean("exceedLimitedBonus");

                        ((OnNetworkControllerListener) mOnNetworkControllerListener).onBonus(bonus);

                        DailyUserPreference.getInstance(mContext).setExceedBonus(isExceedBonus);
                        DailyMobileAPI.getInstance(mContext).requestUserProfile(mNetworkTag, mUserProfileCallback);
                    } else
                    {
                        String msg = responseJSONObject.getString("msg");
                        mOnNetworkControllerListener.onErrorToastMessage(msg);
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
    };
}