package com.daily.dailyhotel.entity;

import android.content.Context;

import com.twoheart.dailyhotel.R;

import java.io.Serializable;

/**
 * Created by android_sam on 2018. 2. 1..
 */

public class GourmetSuggestV2
{
    //    public static final String CATEGORY_REGION = "region"; // default - 지역
    //    public static final String CATEGORY_GOURMET = "gourmet"; // 레스토랑
    //    public static final String CATEGORY_LOCATION = "location"; // 위치
    //    public static final String CATEGORY_DIRECT = "direct"; // 검색어 - 직접 입력

    public static final int MENU_TYPE_DIRECT = 1;
    public static final int MENU_TYPE_LOCATION = 2;
    public static final int MENU_TYPE_RECENTLY_SEARCH = 3;
    public static final int MENU_TYPE_RECENTLY_GOURMET = 4;
    public static final int MENU_TYPE_SUGGEST = 5;
    public static final int MENU_TYPE_CAMPAIGN_TAG = 6;

    public int menuType; // 검색어 입력창에서 선택 된 메뉴 - 주로 Analytics 에서 사용,  선택된 메뉴가 필요할때 사용
    public SuggestItem suggestItem;

    public GourmetSuggestV2()
    {
    }

    public GourmetSuggestV2(int menuType, SuggestItem suggestItem)
    {
        this.menuType = menuType;
        this.suggestItem = suggestItem;
    }

    public String getDisplayNameSearchHomeType(Context context)
    {
        if (suggestItem == null || context == null)
        {
            return null;
        }

        if (suggestItem instanceof GourmetSuggestV2.Gourmet)
        {
            return suggestItem.name;
        } else if (suggestItem instanceof GourmetSuggestV2.Province)
        {
            return ((GourmetSuggestV2.Province) suggestItem).getProvinceName();
        } else if (suggestItem instanceof GourmetSuggestV2.Direct)
        {
            return ((GourmetSuggestV2.Direct) suggestItem).name;
        } else if (suggestItem instanceof GourmetSuggestV2.Location)
        {
            return context.getString(R.string.label_search_suggest_type_location_item_format, ((GourmetSuggestV2.Location) suggestItem).name);
        } else if (suggestItem instanceof GourmetSuggestV2.CampaignTag)
        {
            return "#" + ((GourmetSuggestV2.CampaignTag) suggestItem).name;
        }

        return null;
    }

    public boolean isLocationSuggestItem()
    {
        return suggestItem == null ? false : suggestItem instanceof GourmetSuggestV2.Location;
    }

    public boolean isGourmetSuggestItem()
    {
        return suggestItem == null ? false : suggestItem instanceof GourmetSuggestV2.Gourmet;
    }

    public boolean isCampaignTagSuggestItem()
    {
        return suggestItem == null ? false : suggestItem instanceof GourmetSuggestV2.CampaignTag;
    }

    @SuppressWarnings("serial")
    public static class SuggestItem implements Serializable
    {
        public String name;

        public SuggestItem()
        {

        }

        public SuggestItem(String name)
        {
            this.name = name;
        }
    }

    public static class Gourmet extends SuggestItem
    {
        public int index;
        //        public String name;
        public int discount;
        public boolean available;
        public Province province;

        public String getProvinceName()
        {
            return province == null ? null : province.getProvinceName();
        }
    }

    public static class Province extends SuggestItem
    {
        public int index;
        //        public String name;
        public Area area;

        public String getProvinceName()
        {
            return area == null ? name : area.name;
        }
    }

    public static class Area extends SuggestItem
    {
        public int index;
        //        public int name;
    }

    public static class Direct extends SuggestItem
    {
        //        public String name;

        public Direct(String name)
        {
            super(name);
        }
    }

    public static class Location extends SuggestItem
    {
        public double latitude;
        public double longitude;
        public String address;
        //        public String name;
    }

    public static class CampaignTag extends SuggestItem
    {
        public int index;
        public String startDate; // ISO-8601
        public String endDate; // ISO-8601
        //        public String campaignTag; // 이 항목은 name 으로 대체
        public String serviceType;

        public static CampaignTag getSuggestItem(com.daily.dailyhotel.entity.CampaignTag campaignTag)
        {
            GourmetSuggestV2.CampaignTag suggestItem = new GourmetSuggestV2.CampaignTag();
            suggestItem.index = campaignTag.index;
            suggestItem.name = campaignTag.campaignTag;
            suggestItem.startDate = campaignTag.startDate;
            suggestItem.endDate = campaignTag.endDate;
            suggestItem.serviceType = campaignTag.serviceType;

            return suggestItem;
        }
    }

    // 서버에서 받은 타입이 아님, 리스트 노출용 섹션
    public static class Section extends SuggestItem
    {
        //        public String name;
        public Section(String title)
        {
            super(title);
        }
    }
}
