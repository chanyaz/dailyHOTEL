package com.daily.dailyhotel.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.support.annotation.Nullable;

import com.crashlytics.android.Crashlytics;
import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ExLog;
import com.daily.dailyhotel.entity.CarouselListItem;
import com.daily.dailyhotel.entity.RecentlyPlace;
import com.daily.dailyhotel.entity.StayOutbound;
import com.daily.dailyhotel.entity.StayOutbounds;
import com.daily.dailyhotel.storage.database.DailyDb;
import com.daily.dailyhotel.storage.database.DailyDbHelper;
import com.daily.dailyhotel.repository.local.model.RecentlyDbPlace;
import com.daily.dailyhotel.repository.local.model.RecentlyList;
import com.daily.dailyhotel.repository.local.model.RecentlyRealmObject;
import com.twoheart.dailyhotel.model.Gourmet;
import com.twoheart.dailyhotel.model.HomeRecentParam;
import com.twoheart.dailyhotel.model.RecentPlaces;
import com.twoheart.dailyhotel.model.Stay;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.DailyCalendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by android_sam on 2017. 6. 8..
 */

public class RecentlyPlaceUtil
{
    public static final String RECENT_PLACE_DELIMITER = ",";
    public static final int MAX_RECENT_PLACE_COUNT = 30;
    public static final String SERVICE_TYPE_IB_STAY_NAME = "HOTEL";
    public static final String SERVICE_TYPE_OB_STAY_NAME = "OB_STAY";
    public static final String SERVICE_TYPE_GOURMET_NAME = "GOURMET";

    public static void migrateRecentlyPlaces(Context context)
    {
        RecentPlaces recentPlaces = new RecentPlaces(context);
        ArrayList<HomeRecentParam> recentlyParamList = recentPlaces.getParamList(RecentPlaces.MAX_RECENT_PLACE_COUNT);

        changeServiceType();

        if (recentlyParamList == null || recentlyParamList.size() == 0)
        {
            return;
        }

        RealmList<RecentlyRealmObject> realmObjectRealmList = new RealmList<>();

        int size = recentlyParamList.size();
        // 오래된 리스트부터 저장하기 위한 역순 계산 - 날짜 저장을 위하여...
        for (int i = size - 1; i >= 0; i--)
        {
            HomeRecentParam param = recentlyParamList.get(i);

            RecentlyRealmObject recentlyRealmObject = convertRecentlyRealmObject(param);
            if (recentlyRealmObject != null)
            {
                realmObjectRealmList.add(recentlyRealmObject);
            }
        }

        setRecentlyRealmListAsync(recentPlaces, realmObjectRealmList);
    }

    private static void changeServiceType()
    {

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransactionAsync(new Realm.Transaction()
        {
            @Override
            public void execute(Realm realm)
            {
                RealmResults<RecentlyRealmObject> results = realm.where(RecentlyRealmObject.class).equalTo("serviceType", "IB_STAY").findAll();
                if (results == null || results.size() == 0)
                {
                    return;
                }

                RealmList<RecentlyRealmObject> list = new RealmList<RecentlyRealmObject>();

                for (RecentlyRealmObject realmObject : results)
                {
                    realmObject.serviceType = Constants.ServiceType.HOTEL.name();
                    list.add(realmObject);
                }

                realm.copyToRealmOrUpdate(list);
            }
        });
    }

    private static void setRecentlyRealmListAsync(RecentPlaces recentPlaces, RealmList<RecentlyRealmObject> list)
    {
        if (recentPlaces == null)
        {
            return;
        }

        if (list == null || list.size() == 0)
        {
            recentPlaces.clear();
            recentPlaces.savePreference();
            return;
        }

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransactionAsync(new Realm.Transaction()
        {
            @Override
            public void execute(Realm realm)
            {
                realm.copyToRealmOrUpdate(list);
            }
        }, new Realm.Transaction.OnSuccess()
        {
            @Override
            public void onSuccess()
            {
                if (Constants.DEBUG == true)
                {
                    ExLog.w("realm Success");
                }

                recentPlaces.clear();
                recentPlaces.savePreference();

            }
        }, new Realm.Transaction.OnError()
        {
            @Override
            public void onError(Throwable error)
            {
                if (Constants.DEBUG == true)
                {
                    ExLog.w("realm Error , error : " + error.getMessage());
                }

                recentPlaces.clear();
            }
        });
    }

    private static RecentlyRealmObject convertRecentlyRealmObject(HomeRecentParam param)
    {
        if (param == null)
        {
            return null;
        }

        RecentlyRealmObject recentlyRealmObject = null;

        try
        {
            recentlyRealmObject = new RecentlyRealmObject();
            recentlyRealmObject.index = param.index;

            if (SERVICE_TYPE_IB_STAY_NAME.equalsIgnoreCase(param.serviceType) == true)
            {
                recentlyRealmObject.serviceType = Constants.ServiceType.HOTEL.name();
            } else if (SERVICE_TYPE_GOURMET_NAME.equalsIgnoreCase(param.serviceType) == true)
            {
                recentlyRealmObject.serviceType = Constants.ServiceType.GOURMET.name();
            } else if (SERVICE_TYPE_OB_STAY_NAME.equalsIgnoreCase(param.serviceType) == true)
            {
                recentlyRealmObject.serviceType = Constants.ServiceType.OB_STAY.name();
            } else
            {
                // 지정 되지 않은 타입
                return null;
            }

            // 기존 단말 저장 시간은 그냥 순서이므로 무시하고 다시 시간을 설정 함
            Calendar calendar = DailyCalendar.getInstance();
            recentlyRealmObject.savingTime = calendar.getTimeInMillis();
        } catch (Exception e)
        {
            if (Constants.DEBUG == true)
            {
                ExLog.w(e.getMessage());
            }
        }

        return recentlyRealmObject;
    }

    public static JSONArray getDbRecentlyJsonArray(ArrayList<RecentlyDbPlace> list, int maxSize)
    {
        JSONArray jsonArray = new JSONArray();

        if (list == null || list.size() == 0 || maxSize == 0)
        {
            // dummy Data 생성
            JSONObject jsonObject = new JSONObject();
            try
            {
                jsonObject.put("serviceType", "HOTEL");
                jsonObject.put("idx", 0);

                jsonArray.put(jsonObject);
            } catch (JSONException e)
            {
                ExLog.d(e.getMessage());
            }

            return jsonArray;
        }

        int size = list.size();

        if (maxSize > size)
        {
            maxSize = size;
        }

        for (int i = 0; i < maxSize; i++)
        {
            JSONObject jsonObject = new JSONObject();

            RecentlyDbPlace recentlyDbPlace = list.get(i);

            try
            {
                String serviceTypeString = recentlyDbPlace.serviceType.name();

                if (Constants.ServiceType.HOTEL.name().equalsIgnoreCase(serviceTypeString) == true //
                    || Constants.ServiceType.GOURMET.name().equalsIgnoreCase(serviceTypeString) == true)
                {
                    int index = recentlyDbPlace.index;

                    jsonObject.put("serviceType", serviceTypeString);
                    jsonObject.put("idx", index);

                    jsonArray.put(jsonObject);
                }
            } catch (Exception e)
            {
                ExLog.d(e.getMessage());
            }
        }

        return jsonArray;
    }

    public static ArrayList<RecentlyDbPlace> getDbRecentlyTypeList(Context context, Constants.ServiceType... serviceTypes)
    {
        if (context == null)
        {
            return null;
        }

        DailyDb dailyDb = DailyDbHelper.getInstance().open(context);

        Cursor cursor = null;

        ArrayList<RecentlyDbPlace> recentlyList = new ArrayList<>();

        try
        {
            cursor = dailyDb.getRecentlyPlaces(-1, serviceTypes);

            if (cursor == null || cursor.getCount() == 0)
            {
                return null;
            }

            for (int i = 0; i < cursor.getCount(); i++)
            {
                cursor.moveToPosition(i);

                try
                {
                    RecentlyDbPlace recentlyDbPlace = new RecentlyDbPlace();
                    recentlyDbPlace.index = cursor.getInt(cursor.getColumnIndex(RecentlyList.PLACE_INDEX));
                    recentlyDbPlace.name = cursor.getString(cursor.getColumnIndex(RecentlyList.NAME));
                    recentlyDbPlace.englishName = cursor.getString(cursor.getColumnIndex(RecentlyList.ENGLISH_NAME));
                    recentlyDbPlace.savingTime = cursor.getLong(cursor.getColumnIndex(RecentlyList.SAVING_TIME));

                    Constants.ServiceType serviceType;

                    try
                    {
                        serviceType = Constants.ServiceType.valueOf(cursor.getString(cursor.getColumnIndex(RecentlyList.SERVICE_TYPE)));
                    } catch (Exception e)
                    {
                        serviceType = null;
                    }

                    recentlyDbPlace.serviceType = serviceType;
                    recentlyDbPlace.imageUrl = cursor.getString(cursor.getColumnIndex(RecentlyList.IMAGE_URL));

                    recentlyList.add(recentlyDbPlace);
                } catch (Exception e)
                {
                    ExLog.w("index : " + i + " , e : " + e.toString());
                }
            }

        } catch (Exception e)
        {
            ExLog.e(e.toString());
        } finally
        {
            try
            {
                if (cursor != null)
                {
                    cursor.close();
                }
            } catch (Exception e)
            {
            }
        }

        DailyDbHelper.getInstance().close();

        return recentlyList;
    }

    @Nullable
    public static ArrayList<RecentlyDbPlace> getRealmRecentlyTypeList(Constants.ServiceType... serviceTypes)
    {
        Realm realm = Realm.getDefaultInstance();

        ArrayList<RecentlyDbPlace> resultList = new ArrayList<>();

        try
        {
            RealmQuery query = realm.where(RecentlyRealmObject.class);

            if (serviceTypes != null)
            {
                if (serviceTypes.length > 1)
                {
                    query.beginGroup();

                    for (int i = 0; i < serviceTypes.length; i++)
                    {
                        if (i > 0)
                        {
                            query.or();
                        }

                        query.equalTo("serviceType", serviceTypes[i].name());
                    }

                    query.endGroup();
                } else
                {
                    query.equalTo("serviceType", serviceTypes[0].name());
                }
            }

            RealmResults<RecentlyRealmObject> realmResults = query.findAllSorted("savingTime", Sort.DESCENDING);

            if (realmResults != null && realmResults.size() > 0)
            {
                for (RecentlyRealmObject realmObject : realmResults)
                {
                    Constants.ServiceType serviceType;
                    try
                    {
                        serviceType = Constants.ServiceType.valueOf(realmObject.serviceType);
                    } catch (Exception e)
                    {
                        continue;
                    }

                    RecentlyDbPlace dbPlace = new RecentlyDbPlace();
                    dbPlace.index = realmObject.index;
                    dbPlace.serviceType = serviceType;
                    dbPlace.savingTime = realmObject.savingTime;
                    dbPlace.name = realmObject.name;
                    dbPlace.englishName = realmObject.englishName;
                    dbPlace.imageUrl = realmObject.imageUrl;

                    resultList.add(dbPlace);
                }
            }

        } catch (Exception e)
        {
            ExLog.w(e.toString());
            Crashlytics.logException(new Exception("realm DB parsing fail : change data - list", e));
        } finally
        {
            ExLog.d("realm close");
            try
            {
                realm.close();
            } catch (Exception e)
            {
                ExLog.w(e.toString());
                Crashlytics.logException(new Exception("realm DB closing fail - list", e));
            }
        }

        return resultList;
    }

    public static long getOldestSavingTime(Constants.ServiceType... serviceTypes)
    {
        Realm realm = Realm.getDefaultInstance();
        long savingTime = -1;

        try
        {
            RealmQuery query = realm.where(RecentlyRealmObject.class);

            if (serviceTypes != null)
            {
                if (serviceTypes.length > 1)
                {
                    query.beginGroup();

                    for (int i = 0; i < serviceTypes.length; i++)
                    {
                        if (i > 0)
                        {
                            query.or();
                        }

                        query.equalTo("serviceType", serviceTypes[i].name());
                    }

                    query.endGroup();
                } else
                {
                    query.equalTo("serviceType", serviceTypes[0].name());
                }
            }

            RealmResults<RecentlyRealmObject> realmResults = query.findAllSorted("savingTime", Sort.ASCENDING);

            if (realmResults != null && realmResults.size() > 0)
            {
                savingTime = realmResults.get(0).savingTime;
            }
        } catch (Exception e)
        {
            ExLog.w(e.toString());
            Crashlytics.logException(new Exception("realm DB parsing fail : change data - time", e));
        } finally
        {
            ExLog.d("realm close");

            try
            {
                realm.close();
            } catch (Exception e)
            {
                ExLog.w(e.toString());
                Crashlytics.logException(new Exception("realm DB closing fail - time", e));
            }
        }

        return savingTime;
    }

    public static RecentlyDbPlace getRecentlyPlace(Constants.ServiceType serviceType, int index)
    {
        Realm realm = Realm.getDefaultInstance();
        RecentlyDbPlace dbPlace = null;

        try
        {
            RealmQuery query = realm.where(RecentlyRealmObject.class);

            query.equalTo("serviceType", serviceType.name());
            query.equalTo("index", index);

            RealmResults<RecentlyRealmObject> realmResults = query.findAllSorted("savingTime", Sort.DESCENDING);

            if (realmResults == null || realmResults.size() == 0)
            {
                return null;
            }

            RecentlyRealmObject realmObject = realmResults.get(0);
            if (realmObject == null)
            {
                return null;
            }

            dbPlace = new RecentlyDbPlace();
            dbPlace.index = realmObject.index;
            dbPlace.serviceType = serviceType;
            dbPlace.savingTime = realmObject.savingTime;
            dbPlace.name = realmObject.name;
            dbPlace.englishName = realmObject.englishName;
            dbPlace.imageUrl = realmObject.imageUrl;

        } catch (Exception e)
        {
            ExLog.w(e.toString());
            Crashlytics.logException(new Exception("realm DB parsing fail : change data - place", e));
        } finally
        {
            ExLog.d("realm close");

            try
            {
                realm.close();
            } catch (Exception e)
            {
                ExLog.w(e.toString());
                Crashlytics.logException(new Exception("realm DB closing fail - place", e));
            }
        }

        return dbPlace;
    }

    public static String getDbTargetIndices(Context context, Constants.ServiceType serviceType, int maxSize)
    {
        if (context == null || serviceType == null || maxSize <= 0)
        {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        DailyDb dailyDb = DailyDbHelper.getInstance().open(context);

        Cursor cursor = null;

        try
        {
            cursor = dailyDb.getRecentlyPlaces(-1, serviceType);

            if (cursor == null || cursor.getCount() == 0 || maxSize <= 0)
            {
                return "";
            }

            int size = cursor.getCount();
            if (maxSize > size)
            {
                maxSize = size;
            }

            for (int i = 0; i < maxSize; i++)
            {
                cursor.moveToPosition(i);

                int index = cursor.getInt(cursor.getColumnIndex(RecentlyList.PLACE_INDEX));

                if (i != 0)
                {
                    builder.append(RECENT_PLACE_DELIMITER);
                }

                builder.append(index);
            }
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        } finally
        {
            try
            {
                if (cursor != null)
                {
                    cursor.close();
                }
            } catch (Exception e)
            {
            }
        }

        DailyDbHelper.getInstance().close();

        return builder.toString();
    }

    public static String getRealmTargetIndices(Constants.ServiceType serviceType, int maxSize)
    {
        ArrayList<RecentlyDbPlace> recentlyList = RecentlyPlaceUtil.getRealmRecentlyTypeList(serviceType);

        if (recentlyList == null || recentlyList.size() == 0 || maxSize <= 0)
        {
            return "";
        }

        if (maxSize > recentlyList.size())
        {
            maxSize = recentlyList.size();
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < maxSize; i++)
        {
            RecentlyDbPlace place = recentlyList.get(i);

            if (i != 0)
            {
                builder.append(RECENT_PLACE_DELIMITER);
            }

            builder.append(place.index);
        }

        return builder.toString();
    }

    public static ArrayList<Integer> getDbRecentlyIndexList(Context context, Constants.ServiceType... serviceTypes)
    {
        if (context == null || serviceTypes == null || serviceTypes.length == 0)
        {
            return null;
        }

        ArrayList<Integer> indexList = new ArrayList<>();

        DailyDb dailyDb = DailyDbHelper.getInstance().open(context);

        Cursor cursor = null;

        try
        {
            cursor = dailyDb.getRecentlyPlaces(-1, serviceTypes);

            if (cursor == null || cursor.getCount() == 0)
            {
                return null;
            }

            int size = cursor.getCount();
            if (MAX_RECENT_PLACE_COUNT < size)
            {
                size = MAX_RECENT_PLACE_COUNT;
            }

            for (int i = 0; i < size; i++)
            {
                cursor.moveToPosition(i);

                int index = cursor.getInt(cursor.getColumnIndex(RecentlyList.PLACE_INDEX));

                indexList.add(index);
            }
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        } finally
        {
            try
            {
                if (cursor != null)
                {
                    cursor.close();
                }
            } catch (Exception e)
            {
            }
        }

        DailyDbHelper.getInstance().close();

        return indexList;
    }

    public static ArrayList<Integer> getRealmRecentlyIndexList(Constants.ServiceType... serviceTypes)
    {
        ArrayList<RecentlyDbPlace> recentlyList = RecentlyPlaceUtil.getRealmRecentlyTypeList(serviceTypes);

        if (recentlyList == null || recentlyList.size() == 0)
        {
            return null;
        }

        ArrayList<Integer> indexList = new ArrayList<>();
        for (RecentlyDbPlace place : recentlyList)
        {
            indexList.add(place.index);
        }

        return indexList;
    }

    public static void addRecentlyItem(Context context, final Constants.ServiceType serviceType, int index, String name //
        , String englishName, String imageUrl, boolean isUpdateDate)
    {
        if (serviceType == null || index <= 0 || context == null)
        {
            return;
        }

        DailyDb dailyDb = DailyDbHelper.getInstance().open(context);
        dailyDb.addRecentlyPlace(serviceType, index, name, englishName, imageUrl, isUpdateDate);

        DailyDbHelper.getInstance().close();
    }

    public static void deleteRecentlyItem(Context context, Constants.ServiceType serviceType, int index)
    {
        if (serviceType == null || index <= 0 || context == null)
        {
            return;
        }

        DailyDb dailyDb = DailyDbHelper.getInstance().open(context);
        dailyDb.deleteRecentlyItem(serviceType, index);

        DailyDbHelper.getInstance().close();
    }

    public static ArrayList<CarouselListItem> mergeCarouselListItemList(Context context, ArrayList<RecentlyPlace> recentlyPlaceList, StayOutbounds stayOutbounds, boolean useRealm)
    {
        ArrayList<CarouselListItem> carouselListItemList = new ArrayList<>();
        if (recentlyPlaceList != null)
        {
            for (RecentlyPlace recentlyPlace : recentlyPlaceList)
            {
                CarouselListItem item = new CarouselListItem(CarouselListItem.TYPE_RECENTLY_PLACE, recentlyPlace);
                carouselListItemList.add(item);
            }
        }

        List<StayOutbound> stayOutboundList = stayOutbounds.getStayOutbound();
        if (stayOutboundList != null)
        {
            for (StayOutbound stayOutbound : stayOutboundList)
            {
                CarouselListItem item = new CarouselListItem(CarouselListItem.TYPE_OB_STAY, stayOutbound);
                carouselListItemList.add(item);
            }
        }

        // sort list
        sortCarouselListItemList(context, carouselListItemList, useRealm, (Constants.ServiceType[]) null);

        return carouselListItemList;
    }

    public static void sortCarouselListItemList(Context context, ArrayList<CarouselListItem> actualList, boolean useRealm, Constants.ServiceType... serviceTypes)
    {
        if (context == null || actualList == null || actualList.size() == 0)
        {
            return;
        }

        ArrayList<RecentlyDbPlace> recentlyTypeList = RecentlyPlaceUtil.getDbRecentlyTypeList(context, serviceTypes);

        if (useRealm == true)
        {
            ArrayList<RecentlyDbPlace> results = RecentlyPlaceUtil.getRealmRecentlyTypeList(serviceTypes);
            if (results != null && results.size() > 0)
            {
                if (recentlyTypeList == null)
                {
                    recentlyTypeList = new ArrayList<>();
                }

                for (RecentlyDbPlace place : results)
                {
                    recentlyTypeList.add(place);
                }
            }
        }

        Collections.sort(recentlyTypeList, new Comparator<RecentlyDbPlace>()
        {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public int compare(RecentlyDbPlace o1, RecentlyDbPlace o2)
            {
                return Long.compare(o1.savingTime, o2.savingTime);
            }
        });

        Collections.reverse(recentlyTypeList);

        if (recentlyTypeList == null || recentlyTypeList.size() == 0)
        {
            return;
        }

        ArrayList<Integer> expectedList = new ArrayList<>();
        for (RecentlyDbPlace recentlyDbPlace : recentlyTypeList)
        {
            expectedList.add(recentlyDbPlace.index);
        }

        Collections.sort(actualList, new Comparator<CarouselListItem>()
        {
            @Override
            public int compare(CarouselListItem item1, CarouselListItem item2)
            {
                Integer position1 = expectedList.indexOf(getCarouselListItemIndex(item1));
                Integer position2 = expectedList.indexOf(getCarouselListItemIndex(item2));
                return position1.compareTo(position2);
            }
        });
    }

    public static Constants.ServiceType getServiceType(String serviceTypeString)
    {
        if (DailyTextUtils.isTextEmpty(serviceTypeString) == true)
        {
            return null;
        }

        Constants.ServiceType serviceType = null;

        if (SERVICE_TYPE_OB_STAY_NAME.equalsIgnoreCase(serviceTypeString) //
            || Constants.ServiceType.OB_STAY.name().equalsIgnoreCase(serviceTypeString))
        {
            serviceType = Constants.ServiceType.OB_STAY;
        } else if (SERVICE_TYPE_IB_STAY_NAME.equalsIgnoreCase(serviceTypeString) //
            || Constants.ServiceType.HOTEL.name().equalsIgnoreCase(serviceTypeString))
        {
            serviceType = Constants.ServiceType.HOTEL;
        } else if (SERVICE_TYPE_GOURMET_NAME.equalsIgnoreCase(serviceTypeString) //
            || Constants.ServiceType.GOURMET.name().equalsIgnoreCase(serviceTypeString))
        {
            serviceType = Constants.ServiceType.GOURMET;
        }

        return serviceType;
    }

    public static Constants.ServiceType getServiceType(CarouselListItem carouselListItem)
    {
        Constants.ServiceType serviceType = null;
        switch (carouselListItem.mType)
        {
            case CarouselListItem.TYPE_RECENTLY_PLACE:
            {
                RecentlyPlace place = carouselListItem.getItem();
                serviceType = RecentlyPlaceUtil.getServiceType(place.serviceType);
                break;
            }

            case CarouselListItem.TYPE_IN_STAY:
            {
                serviceType = Constants.ServiceType.HOTEL;
                break;
            }

            case CarouselListItem.TYPE_OB_STAY:
            {
                serviceType = Constants.ServiceType.OB_STAY;
                break;
            }

            case CarouselListItem.TYPE_GOURMET:
            {
                serviceType = Constants.ServiceType.GOURMET;
                break;
            }
        }

        return serviceType;
    }

    public static int getCarouselListItemIndex(CarouselListItem carouselListItem)
    {
        int index = -1;
        switch (carouselListItem.mType)
        {
            case CarouselListItem.TYPE_RECENTLY_PLACE:
            {
                RecentlyPlace item = carouselListItem.getItem();
                index = item.index;
                break;
            }

            case CarouselListItem.TYPE_IN_STAY:
            {
                Stay item = carouselListItem.getItem();
                index = item.index;
                break;
            }

            case CarouselListItem.TYPE_OB_STAY:
            {
                StayOutbound item = carouselListItem.getItem();
                index = item.index;
                break;
            }

            case CarouselListItem.TYPE_GOURMET:
            {
                Gourmet item = carouselListItem.getItem();
                index = item.index;
                break;
            }
        }

        return index;
    }
}
