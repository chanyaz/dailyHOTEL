package com.daily.dailyhotel.repository.remote;

import android.content.Context;
import android.text.TextUtils;

import com.daily.base.exception.BaseException;
import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ExLog;
import com.daily.dailyhotel.domain.RecentlyInterface;
import com.daily.dailyhotel.entity.StayOutbounds;
import com.daily.dailyhotel.repository.local.model.RecentlyPlace;
import com.daily.dailyhotel.repository.remote.model.GourmetListData;
import com.daily.dailyhotel.repository.remote.model.StayListData;
import com.daily.dailyhotel.repository.remote.model.StayOutboundsData;
import com.daily.dailyhotel.util.RecentlyPlaceUtil;
import com.twoheart.dailyhotel.model.Gourmet;
import com.twoheart.dailyhotel.model.RecentGourmetParams;
import com.twoheart.dailyhotel.model.RecentStayParams;
import com.twoheart.dailyhotel.model.Stay;
import com.twoheart.dailyhotel.model.time.GourmetBookingDay;
import com.twoheart.dailyhotel.model.time.StayBookingDay;
import com.twoheart.dailyhotel.network.DailyMobileAPI;
import com.twoheart.dailyhotel.network.dto.BaseDto;
import com.twoheart.dailyhotel.network.model.HomePlace;
import com.twoheart.dailyhotel.network.model.HomePlaces;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

/**
 * Created by android_sam on 2017. 6. 14..
 */

public class RecentlyRemoteImpl implements RecentlyInterface
{
    private Context mContext;

    public RecentlyRemoteImpl(@NonNull Context context)
    {
        mContext = context;
    }

    @Override
    public Observable<StayOutbounds> getStayOutboundRecentlyList(int numberOfResults, boolean useRealm)
    {
        String hotelIds = RecentlyPlaceUtil.getDbTargetIndices(mContext, RecentlyPlaceUtil.ServiceType.OB_STAY, RecentlyPlaceUtil.MAX_RECENT_PLACE_COUNT);

        if (useRealm == true)
        {
            String oldHotelIds = RecentlyPlaceUtil.getRealmTargetIndices(RecentlyPlaceUtil.ServiceType.OB_STAY, RecentlyPlaceUtil.MAX_RECENT_PLACE_COUNT);
            if (DailyTextUtils.isTextEmpty(oldHotelIds) == false)
            {
                if (DailyTextUtils.isTextEmpty(hotelIds) == false)
                {
                    hotelIds += ",";
                }

                hotelIds += oldHotelIds;
            }
        }

        return DailyMobileAPI.getInstance(mContext).getStayOutboundRecentlyList(hotelIds, numberOfResults).map(new Function<BaseDto<StayOutboundsData>, StayOutbounds>()
        {
            @Override
            public StayOutbounds apply(@NonNull BaseDto<StayOutboundsData> stayOutboundsDataBaseDto) throws Exception
            {
                StayOutbounds stayOutbounds = null;

                if (stayOutboundsDataBaseDto != null)
                {
                    if (stayOutboundsDataBaseDto.msgCode == 100 && stayOutboundsDataBaseDto.data != null)
                    {
                        stayOutbounds = stayOutboundsDataBaseDto.data.getStayOutboundList();

                        if (stayOutbounds == null)
                        {
                            stayOutbounds = new StayOutbounds();
                        }
                    } else
                    {
                        throw new BaseException(stayOutboundsDataBaseDto.msgCode, stayOutboundsDataBaseDto.msg);
                    }
                } else
                {
                    throw new BaseException(-1, null);
                }

                return stayOutbounds;
            }
        }).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<ArrayList<HomePlace>> getHomeRecentlyList(int maxSize)
    {
        JSONObject recentJsonObject = new JSONObject();

        try
        {
            ArrayList<RecentlyPlace> list = RecentlyPlaceUtil.getDbRecentlyTypeList(mContext //
                , RecentlyPlaceUtil.ServiceType.HOTEL, RecentlyPlaceUtil.ServiceType.GOURMET);
            JSONArray recentJsonArray = RecentlyPlaceUtil.getDbRecentlyJsonArray(list, maxSize);
            recentJsonObject.put("keys", recentJsonArray);

        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }

        return DailyMobileAPI.getInstance(mContext).getHomeRecentlyList(recentJsonObject).map(new Function<BaseDto<HomePlaces>, ArrayList<HomePlace>>()
        {
            @Override
            public ArrayList<HomePlace> apply(@NonNull BaseDto<HomePlaces> homePlacesBaseDto) throws Exception
            {
                ArrayList<HomePlace> homePlaceList = new ArrayList<>();

                if (homePlacesBaseDto != null)
                {
                    if (homePlacesBaseDto.msgCode == 100 && homePlacesBaseDto.data != null)
                    {
                        List<HomePlace> list = homePlacesBaseDto.data.getHomePlaceList();

                        if (list != null && list.size() > 0)
                        {
                            homePlaceList.addAll(list);
                        }
                    } else
                    {
                        throw new BaseException(homePlacesBaseDto.msgCode, homePlacesBaseDto.msg);
                    }
                } else
                {
                    throw new BaseException(-1, null);
                }

                return homePlaceList;
            }
        }).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<List<Stay>> getStayInboundRecentlyList(StayBookingDay stayBookingDay, boolean useRealm)
    {
        String targetIndices = RecentlyPlaceUtil.getDbTargetIndices(mContext, RecentlyPlaceUtil.ServiceType.HOTEL, RecentlyPlaceUtil.MAX_RECENT_PLACE_COUNT);

        if (useRealm == true)
        {
            String oldTargetIndices = RecentlyPlaceUtil.getRealmTargetIndices(RecentlyPlaceUtil.ServiceType.HOTEL, RecentlyPlaceUtil.MAX_RECENT_PLACE_COUNT);
            if (DailyTextUtils.isTextEmpty(oldTargetIndices) == false)
            {
                if (DailyTextUtils.isTextEmpty(targetIndices) == false)
                {
                    targetIndices += ",";
                }

                targetIndices += oldTargetIndices;
            }
        }

        if (TextUtils.isEmpty(targetIndices) == true)
        {
            targetIndices = "0";
        }

        RecentStayParams recentStayParams = new RecentStayParams();
        recentStayParams.setStayBookingDay(stayBookingDay);
        recentStayParams.setTargetIndices(targetIndices);

        return DailyMobileAPI.getInstance(mContext) //
            .getStayList(recentStayParams.toParamsMap(), recentStayParams.getBedTypeList(), recentStayParams.getLuxuryList(), null) //
            .map(new Function<BaseDto<StayListData>, List<Stay>>()
            {
                @Override
                public List<Stay> apply(@NonNull BaseDto<StayListData> stayListDataBaseDto) throws Exception
                {
                    List<Stay> stayList = null;

                    if (stayListDataBaseDto != null)
                    {
                        if (stayListDataBaseDto.msgCode == 100 && stayListDataBaseDto.data != null)
                        {
                            stayList = stayListDataBaseDto.data.getStayList();
                            if (stayList == null || stayList.size() == 0)
                            {
                                stayList = new ArrayList<>();
                            }
                        } else
                        {
                            throw new BaseException(stayListDataBaseDto.msgCode, stayListDataBaseDto.msg);
                        }
                    } else
                    {
                        throw new BaseException(-1, null);
                    }

                    return stayList;
                }
            }).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<List<Gourmet>> getGourmetRecentlyList(GourmetBookingDay gourmetBookingDay, boolean useRealm)
    {
        String targetIndices = RecentlyPlaceUtil.getDbTargetIndices(mContext, RecentlyPlaceUtil.ServiceType.GOURMET, RecentlyPlaceUtil.MAX_RECENT_PLACE_COUNT);

        if (useRealm == true)
        {
            String oldTargetIndices = RecentlyPlaceUtil.getRealmTargetIndices(RecentlyPlaceUtil.ServiceType.GOURMET, RecentlyPlaceUtil.MAX_RECENT_PLACE_COUNT);
            if (DailyTextUtils.isTextEmpty(oldTargetIndices) == false)
            {
                if (DailyTextUtils.isTextEmpty(targetIndices) == false)
                {
                    targetIndices += ",";
                }

                targetIndices += oldTargetIndices;
            }
        }

        if (TextUtils.isEmpty(targetIndices) == true)
        {
            targetIndices = "0";
        }

        RecentGourmetParams params = new RecentGourmetParams();
        params.setGourmetBookingDay(gourmetBookingDay);
        params.setTargetIndices(targetIndices);

        return DailyMobileAPI.getInstance(mContext) //
            .getGourmetList(params.toParamsMap(), params.getCategoryList(), params.getTimeList(), params.getLuxuryList()) //
            .map(new Function<BaseDto<GourmetListData>, List<Gourmet>>()
            {
                @Override
                public List<Gourmet> apply(@NonNull BaseDto<GourmetListData> gourmetListDataBaseDto) throws Exception
                {
                    List<Gourmet> gourmetList = null;

                    if (gourmetListDataBaseDto != null)
                    {
                        if (gourmetListDataBaseDto.msgCode == 100 && gourmetListDataBaseDto.data != null)
                        {
                            gourmetList = gourmetListDataBaseDto.data.getGourmetList(mContext);
                            if (gourmetList == null || gourmetList.size() == 0)
                            {
                                gourmetList = new ArrayList<>();
                            }
                        } else
                        {
                            throw new BaseException(gourmetListDataBaseDto.msgCode, gourmetListDataBaseDto.msg);
                        }
                    } else
                    {
                        throw new BaseException(-1, null);
                    }

                    return gourmetList;
                }
            }).observeOn(AndroidSchedulers.mainThread());
    }
}
