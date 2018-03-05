package com.daily.dailyhotel.entity;

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

    public static class SuggestItem
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
        public String name;
        public int discount;
        public int availableTickets;
        public boolean isExpired;
        public int minimumOrderQuantity;
        public Province province;
    }

    public static class Province extends SuggestItem
    {
        public int index;
        //        public String name;
        public Area area;
    }

    public static class Area extends SuggestItem
    {
        public int index;
        //        public int name;
    }

    public static class Direct extends SuggestItem
    {
        //        public String name;
    }

    public static class Location extends SuggestItem
    {
        public double latitude;
        public double longitude;
        public String addess;
        //        public String name;
    }
}
