package com.daily.dailyhotel.repository.remote.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.daily.dailyhotel.entity.StayCampaignTags;
import com.twoheart.dailyhotel.model.Stay;

import java.util.ArrayList;

/**
 * Created by android_sam on 2017. 8. 8..
 */
@JsonObject
public class StayCampaignTagsData
{
    @JsonField(name = "responseHashtagCampaign")
    public CampaignTagData responseHashtagCampaign;

    @JsonField(name = "imgUrl")
    public String imageUrl;

    @JsonField(name = "saleRecords")
    public ArrayList<StaySalesData> saleRecords;

    @JsonField(name = "configurations")
    public ConfigurationsData configurations;

    public StayCampaignTags getStayCampaigns()
    {
        StayCampaignTags stayCampaignTags = new StayCampaignTags();

        stayCampaignTags.imageUrl = imageUrl;
        stayCampaignTags.setCampaignTag(responseHashtagCampaign.getCampaignTag());
        stayCampaignTags.setStayList(getStayList());

        if (configurations != null)
        {
            stayCampaignTags.activeReward = configurations.activeReward;
        }

        return stayCampaignTags;
    }

    private ArrayList<Stay> getStayList()
    {
        ArrayList<Stay> stayList = new ArrayList<>();

        for (StaySalesData staySalesData : saleRecords)
        {
            Stay stay = staySalesData.getStay();

            stay.imageUrl = imageUrl + stay.imageUrl;

            stayList.add(stay);
        }

        return stayList;
    }
}
