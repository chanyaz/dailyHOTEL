package com.twoheart.dailyhotel.screen.search.stay.result;

import android.location.Location;

import com.twoheart.dailyhotel.model.Area;
import com.twoheart.dailyhotel.model.Category;
import com.twoheart.dailyhotel.model.Province;
import com.twoheart.dailyhotel.model.SaleTime;
import com.twoheart.dailyhotel.model.StayCurationOption;
import com.twoheart.dailyhotel.model.StayParams;
import com.twoheart.dailyhotel.util.Constants;

public class StaySearchResultCurationManager
{
    private static StaySearchResultCurationManager mInstance;

    private Province mProvince;
    private Location mLocation; // Not Parcelable

    private SaleTime mCheckInSaleTime;
    private SaleTime mCheckOutSaleTime;
    private Category mCategory;
    private StayCurationOption mStayCurationOption;

    public static synchronized StaySearchResultCurationManager getInstance()
    {
        if (mInstance == null)
        {
            mInstance = new StaySearchResultCurationManager();
        }

        return mInstance;
    }

    private StaySearchResultCurationManager()
    {
        mStayCurationOption = new StayCurationOption();

        clear();
    }

    public void setCheckInSaleTime(long currentDateTime, long dailyDateTime)
    {
        if (mCheckInSaleTime == null)
        {
            mCheckInSaleTime = new SaleTime();
        }

        mCheckInSaleTime.setCurrentTime(currentDateTime);
        mCheckInSaleTime.setDailyTime(dailyDateTime);
    }

    public void destroy()
    {
        clear();
        mInstance = null;
    }

    public void setCheckInSaleTime(SaleTime saleTime)
    {
        mCheckInSaleTime = saleTime;
    }

    public void setCheckOutSaleTime(SaleTime saleTime)
    {
        mCheckOutSaleTime = saleTime;
    }

    public SaleTime getCheckInSaleTime()
    {
        return mCheckInSaleTime;
    }

    public SaleTime getCheckOutSaleTime()
    {
        return mCheckOutSaleTime;
    }

    public int getNights()
    {
        if (mCheckInSaleTime == null || mCheckOutSaleTime == null)
        {
            return 1;
        }

        return mCheckOutSaleTime.getOffsetDailyDay() - mCheckInSaleTime.getOffsetDailyDay();
    }

    public StayCurationOption getStayCurationOption()
    {
        return mStayCurationOption;
    }

    public Province getProvince()
    {
        return mProvince;
    }

    public void setProvince(Province province)
    {
        mProvince = province;
    }

    public Location getLocation()
    {
        return mLocation;
    }

    public void setLocation(Location location)
    {
        mLocation = location;
    }

    public Category getCategory()
    {
        return mCategory;
    }

    public void setCategory(Category category)
    {
        mCategory = category;
    }

    public StayParams getStayParams(int page, int limit, boolean isDetails)
    {
        StayParams params = new StayParams();

        params.dateCheckIn = mCheckInSaleTime.getDayOfDaysDateFormat("yyyy-MM-dd");
        params.stays = getNights();
        params.provinceIdx = mProvince.getProvinceIndex();

        if (mProvince instanceof Area)
        {
            Area area = (Area) mProvince;
            if (area != null)
            {
                params.areaIdx = area.index;
            }
        }

        params.persons = mStayCurationOption.person;
        params.category = mCategory;
        params.bedType = mStayCurationOption.getParamStringByBedTypes(); // curationOption에서 가져온 스트링
        params.luxury = mStayCurationOption.getParamStingByAmenities(); // curationOption에서 가져온 스트링

        Constants.SortType sortType = mStayCurationOption.getSortType();
        if (Constants.SortType.DISTANCE == sortType)
        {
            if (mLocation != null)
            {
                params.latitude = mLocation.getLatitude();
                params.longitude = mLocation.getLongitude();
            }
        }

        params.page = page;
        params.limit = limit;
        params.setSortType(sortType);
        params.details = isDetails;

        return params;
    }

    public void clear()
    {
        mCategory = Category.ALL;

        mStayCurationOption.clear();

        mCheckInSaleTime = null;
        mCheckOutSaleTime = null;

        mProvince = null;
        mLocation = null;
    }
}
