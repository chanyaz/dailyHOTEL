package com.daily.dailyhotel.domain;

import com.daily.dailyhotel.entity.People;
import com.daily.dailyhotel.entity.StayBookDateTime;
import com.daily.dailyhotel.entity.StayOutboundDetail;
import com.daily.dailyhotel.entity.StayOutboundFilters;
import com.daily.dailyhotel.entity.StayOutbounds;

import io.reactivex.Observable;

public interface StayOutboundInterface
{
    Observable<StayOutbounds> getStayOutboundList(StayBookDateTime stayBookDateTime, long geographyId//
        , String geographyType, People people, StayOutboundFilters stayOutboundFilters, String cacheKey, String cacheLocation);

    Observable<StayOutboundDetail> getStayOutboundDetail(int index, StayBookDateTime stayBookDateTime, People people);
}