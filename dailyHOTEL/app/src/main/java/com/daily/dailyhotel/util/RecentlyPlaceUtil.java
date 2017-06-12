package com.daily.dailyhotel.util;

import android.content.Context;

import com.daily.base.util.ExLog;
import com.daily.dailyhotel.repository.local.model.RecentlyRealmObject;
import com.twoheart.dailyhotel.model.HomeRecentParam;
import com.twoheart.dailyhotel.model.RecentPlaces;
import com.twoheart.dailyhotel.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * Created by android_sam on 2017. 6. 8..
 */

public class RecentlyPlaceUtil
{
    public static void migrateRecentlyPlaces(Context context)
    {
        RecentPlaces recentPlaces = new RecentPlaces(context);
        ArrayList<HomeRecentParam> recentlyParamList = recentPlaces.getParamList(RecentPlaces.MAX_RECENT_PLACE_COUNT);

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

        setRecentlyRealmList(recentPlaces, realmObjectRealmList);
    }

    private static void setRecentlyRealmList(RecentPlaces recentPlaces, RealmList<RecentlyRealmObject> list)
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
            recentlyRealmObject.serviceType = param.serviceType;

            long savingTime = param.savingTime >= 0 ? param.savingTime : new Date().getTime();
            recentlyRealmObject.date = new Date(savingTime);
        } catch (Exception e)
        {
            if (Constants.DEBUG == true)
            {
                ExLog.w(e.getMessage());
            }
        }

        return recentlyRealmObject;
    }

    public static JSONArray getRecentlyJsonArray(RealmResults<RecentlyRealmObject> list, int maxSize)
    {
        if (list == null || list.size() == 0 || maxSize == 0)
        {
            return null;
        }

        JSONArray jsonArray = new JSONArray();

        if (maxSize > list.size())
        {
            maxSize = list.size();
        }

        for (int i = 0 ; i < maxSize ; i++)
        {
            RecentlyRealmObject realmObject = list.get(i);

            JSONObject jsonObject = new JSONObject();

            try
            {
                jsonObject.put("serviceType", realmObject.serviceType);
                jsonObject.put("idx", realmObject.index);

                jsonArray.put(jsonObject);
            } catch (JSONException e)
            {
                ExLog.d(e.getMessage());
            }
        }

        return jsonArray;
    }
}
