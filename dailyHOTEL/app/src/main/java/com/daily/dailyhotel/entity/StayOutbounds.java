package com.daily.dailyhotel.entity;

import java.util.List;

public class StayOutbounds extends Configurations
{
    private List<StayOutbound> mStayOutboundList;

    public String cacheKey;
    public String cacheLocation;
    public String customerSessionId;
    public boolean moreResultsAvailable;

    public StayOutbounds()
    {

    }

    public void setStayOutbound(List<StayOutbound> stayOutbound)
    {
        mStayOutboundList = stayOutbound;
    }

    public List<StayOutbound> getStayOutbound()
    {
        return mStayOutboundList;
    }
}
