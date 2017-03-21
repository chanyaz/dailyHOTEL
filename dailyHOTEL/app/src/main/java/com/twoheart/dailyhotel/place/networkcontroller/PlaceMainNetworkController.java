package com.twoheart.dailyhotel.place.networkcontroller;

import android.content.Context;

import com.twoheart.dailyhotel.model.Area;
import com.twoheart.dailyhotel.model.EventBanner;
import com.twoheart.dailyhotel.model.Province;
import com.twoheart.dailyhotel.network.DailyMobileAPI;
import com.twoheart.dailyhotel.network.dto.BaseDto;
import com.twoheart.dailyhotel.network.model.TodayDateTime;
import com.twoheart.dailyhotel.place.base.BaseNetworkController;
import com.twoheart.dailyhotel.place.base.OnBaseNetworkControllerListener;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public abstract class PlaceMainNetworkController extends BaseNetworkController
{
    public interface OnNetworkControllerListener extends OnBaseNetworkControllerListener
    {
        void onDateTime(TodayDateTime todayDateTime);

        void onEventBanner(List<EventBanner> eventBannerList);

        void onRegionList(List<Province> provinceList, List<Area> areaList);
    }

    public abstract void requestEventBanner();

    public abstract void requestRegionList();

    public PlaceMainNetworkController(Context context, String networkTag, OnBaseNetworkControllerListener listener)
    {
        super(context, networkTag, listener);
    }

    public void requestDateTime()
    {
        DailyMobileAPI.getInstance(mContext).requestCommonDateTimeRefactoring(mNetworkTag, mDateTimeCallback);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    // NetworkActionListener
    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    private retrofit2.Callback mDateTimeCallback = new retrofit2.Callback<BaseDto<TodayDateTime>>()
    {
        @Override
        public void onResponse(Call<BaseDto<TodayDateTime>> call, Response<BaseDto<TodayDateTime>> response)
        {
            if (response != null && response.isSuccessful() && response.body() != null)
            {
                try
                {
                    BaseDto<TodayDateTime> baseDto = response.body();

                    if (baseDto.msgCode == 100)
                    {
                        ((OnNetworkControllerListener) mOnNetworkControllerListener).onDateTime(baseDto.data);
                    } else
                    {
                        mOnNetworkControllerListener.onErrorPopupMessage(baseDto.msgCode, baseDto.msg);
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
        public void onFailure(Call<BaseDto<TodayDateTime>> call, Throwable t)
        {
            mOnNetworkControllerListener.onError(t);
        }
    };
}
