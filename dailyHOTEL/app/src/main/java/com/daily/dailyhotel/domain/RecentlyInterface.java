package com.daily.dailyhotel.domain;

import com.daily.dailyhotel.entity.RecentlyPlace;
import com.daily.dailyhotel.entity.StayOutbounds;
import com.twoheart.dailyhotel.util.Constants;

import java.util.ArrayList;

import io.reactivex.Observable;

/**
 * Created by android_sam on 2017. 6. 14..
 */

public interface RecentlyInterface
{
    Observable<StayOutbounds> getStayOutboundRecentlyList(int numberOfResults, boolean useRealm);

    Observable<ArrayList<RecentlyPlace>> getInboundRecentlyList(int maxSize, boolean useRealm, Constants.ServiceType... serviceTypes);
}
