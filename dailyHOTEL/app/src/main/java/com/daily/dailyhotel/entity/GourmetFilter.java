package com.daily.dailyhotel.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class GourmetFilter
{
    public static final int FLAG_TIME_NONE = 0x00;
    public static final int FLAG_TIME_06_11 = 0x01;
    public static final int FLAG_TIME_11_15 = FLAG_TIME_06_11 << 1;
    public static final int FLAG_TIME_15_17 = FLAG_TIME_11_15 << 1;
    public static final int FLAG_TIME_17_21 = FLAG_TIME_15_17 << 1;
    public static final int FLAG_TIME_21_06 = FLAG_TIME_17_21 << 1;

    public static final int FLAG_AMENITIES_NONE = 0x00;
    public static final int FLAG_AMENITIES_PARKING = 0x01;
    public static final int FLAG_AMENITIES_VALET = FLAG_AMENITIES_PARKING << 1;
    public static final int FLAG_AMENITIES_BABYSEAT = FLAG_AMENITIES_VALET << 1;
    public static final int FLAG_AMENITIES_PRIVATEROOM = FLAG_AMENITIES_BABYSEAT << 1;
    public static final int FLAG_AMENITIES_GROUPBOOKING = FLAG_AMENITIES_PRIVATEROOM << 1;
    public static final int FLAG_AMENITIES_CORKAGE = FLAG_AMENITIES_GROUPBOOKING << 1;

    private HashMap<String, Integer> mFlagCategoryFilterMap; // 선택된 음식 종류
    private LinkedHashMap<String, Category> mCategoryMap;

    public int flagTimeFilter;
    public int flagAmenitiesFilters;

    public SortType defaultSortType = SortType.DEFAULT;
    public SortType sortType = defaultSortType;

    public enum SortType
    {
        DEFAULT,
        DISTANCE,
        LOW_PRICE,
        HIGH_PRICE,
        SATISFACTION
    }

    public static class Category
    {
        public String name;
        public int code;
        public int sequence;
    }

    public GourmetFilter()
    {
        mFlagCategoryFilterMap = new HashMap<>();
        mCategoryMap = new LinkedHashMap<>();
    }

    public boolean isDistanceSort()
    {
        return sortType == SortType.DISTANCE;
    }

    public void setCategoryMap(LinkedHashMap<String, Category> categoryMap)
    {
        if (categoryMap == null)
        {
            return;
        }

        mCategoryMap.putAll(categoryMap);
    }

    public boolean hasCategory(Category category)
    {
        if (category == null)
        {
            return false;
        }

        return mFlagCategoryFilterMap.containsKey(category.name);
    }

    public void addCategory(Category category)
    {
        if (category == null)
        {
            return;
        }

        mFlagCategoryFilterMap.put(category.name, category.code);
    }

    public void removeCategory(Category category)
    {
        if (category == null)
        {
            return;
        }

        mFlagCategoryFilterMap.remove(category.name);
    }

    public HashMap<String, Integer> getCategoryFilterMap()
    {
        return mFlagCategoryFilterMap;
    }

    public LinkedHashMap<String, Category> getCategoryMap()
    {
        return mCategoryMap;
    }

    public boolean isDefault()
    {
        return (sortType == defaultSortType//
            && mFlagCategoryFilterMap.size() == 0//
            && flagTimeFilter == GourmetFilter.FLAG_TIME_NONE//
            && flagAmenitiesFilters == GourmetFilter.FLAG_AMENITIES_NONE);
    }

    public GourmetFilter reset()
    {
        sortType = defaultSortType;
        mFlagCategoryFilterMap.clear();
        flagTimeFilter = FLAG_TIME_NONE;
        flagAmenitiesFilters = FLAG_AMENITIES_NONE;

        return this;
    }

    public List<Integer> getCategoryFilter()
    {
        if (mFlagCategoryFilterMap.size() == 0)
        {
            return null;
        }

        return new ArrayList(mFlagCategoryFilterMap.values());
    }

    public List<String> getTimeFilter()
    {
        if (flagTimeFilter == GourmetFilter.FLAG_TIME_NONE)
        {
            return null;
        }

        List<String> arrayList = new ArrayList();

        if ((flagTimeFilter & GourmetFilter.FLAG_TIME_06_11) == GourmetFilter.FLAG_TIME_06_11)
        {
            arrayList.add("06_11");
        }

        if ((flagTimeFilter & GourmetFilter.FLAG_TIME_11_15) == GourmetFilter.FLAG_TIME_11_15)
        {
            arrayList.add("11_15");
        }

        if ((flagTimeFilter & GourmetFilter.FLAG_TIME_15_17) == GourmetFilter.FLAG_TIME_15_17)
        {
            arrayList.add("15_17");
        }

        if ((flagTimeFilter & GourmetFilter.FLAG_TIME_17_21) == GourmetFilter.FLAG_TIME_17_21)
        {
            arrayList.add("17_21");
        }

        if ((flagTimeFilter & GourmetFilter.FLAG_TIME_21_06) == GourmetFilter.FLAG_TIME_21_06)
        {
            arrayList.add("21_06");
        }

        return arrayList;
    }

    public List<String> getAmenitiesFilter()
    {
        if (flagAmenitiesFilters == GourmetFilter.FLAG_AMENITIES_NONE)
        {
            return null;
        }

        List<String> arrayList = new ArrayList();

        if ((flagAmenitiesFilters & GourmetFilter.FLAG_AMENITIES_PARKING) == GourmetFilter.FLAG_AMENITIES_PARKING)
        {
            arrayList.add("Parking");
        }

        if ((flagAmenitiesFilters & GourmetFilter.FLAG_AMENITIES_VALET) == GourmetFilter.FLAG_AMENITIES_VALET)
        {
            arrayList.add("Valet");
        }

        if ((flagAmenitiesFilters & GourmetFilter.FLAG_AMENITIES_BABYSEAT) == GourmetFilter.FLAG_AMENITIES_BABYSEAT)
        {
            arrayList.add("BabySeat");
        }

        if ((flagAmenitiesFilters & GourmetFilter.FLAG_AMENITIES_PRIVATEROOM) == GourmetFilter.FLAG_AMENITIES_PRIVATEROOM)
        {
            arrayList.add("PrivateRoom");
        }

        if ((flagAmenitiesFilters & GourmetFilter.FLAG_AMENITIES_GROUPBOOKING) == GourmetFilter.FLAG_AMENITIES_GROUPBOOKING)
        {
            arrayList.add("GroupBooking");
        }

        if ((flagAmenitiesFilters & GourmetFilter.FLAG_AMENITIES_CORKAGE) == GourmetFilter.FLAG_AMENITIES_CORKAGE)
        {
            arrayList.add("Corkage");
        }

        return arrayList;
    }
}
