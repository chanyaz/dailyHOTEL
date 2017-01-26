package com.twoheart.dailyhotel.screen.search.collection;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Place;
import com.twoheart.dailyhotel.model.PlaceViewItem;
import com.twoheart.dailyhotel.model.SaleTime;
import com.twoheart.dailyhotel.network.DailyMobileAPI;
import com.twoheart.dailyhotel.place.adapter.PlaceListAdapter;
import com.twoheart.dailyhotel.place.base.BaseActivity;
import com.twoheart.dailyhotel.util.DailyCalendar;
import com.twoheart.dailyhotel.util.EdgeEffectColor;
import com.twoheart.dailyhotel.util.ExLog;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;
import com.twoheart.dailyhotel.widget.DailyToolbarLayout;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class CollectionBaseActivity extends BaseActivity
{
    private static final String QUERY_TYPE_INDEX = "index";
    private static final String QUERY_TYPE_SEARCH = "search";

    protected static final String INTENT_EXTRA_DATA_INDEX = "index";
    protected static final String INTENT_EXTRA_DATA_IMAGE_URL = "imageUrl";
    protected static final String INTENT_EXTRA_DATA_TITLE = "title";
    protected static final String INTENT_EXTRA_DATA_SUBTITLE = "subTitle";

    DailyToolbarLayout mDailyToolbarLayout;
    RecyclerView mRecyclerView;
    PlaceListAdapter mPlaceListAdapter;
    protected SaleTime mStartSaleTime, mEndSaleTime;
    int mFeaturedIndex;

    protected abstract void initIntentTime(Intent intent);

    protected abstract void requestFeaturedPlaceList();

    protected abstract PlaceListAdapter getPlaceListAdapter(View.OnClickListener listener);

    protected abstract void onPlaceClick(View view, PlaceViewItem placeViewItem, int count);

    protected abstract String getCalendarDate();

    protected abstract void onCalendarActivityResult(int resultCode, Intent data);

    protected abstract String getSectionTitle(int count);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_collection_search);

        Intent intent = getIntent();

        if (intent == null)
        {
            finish();
            return;
        }

        initIntentTime(intent);

        mFeaturedIndex = intent.getIntExtra(INTENT_EXTRA_DATA_INDEX, -1);
        String title = intent.getStringExtra(INTENT_EXTRA_DATA_TITLE);
        String subTitle = intent.getStringExtra(INTENT_EXTRA_DATA_SUBTITLE);
        String imageUrl = intent.getStringExtra(INTENT_EXTRA_DATA_IMAGE_URL);

        if(mFeaturedIndex <= 0)
        {
            finish();
            return;
        }

        if(Util.isUsedMultiTransition() == true)
        {

        } else
        {
            initLayout(title, imageUrl);

            lockUI();

            requestCommonDateTime();
        }
    }

    private void requestCommonDateTime()
    {
        DailyMobileAPI.getInstance(this).requestCommonDateTime(mNetworkTag, new Callback<JSONObject>()
        {
            @Override
            public void onResponse(Call<JSONObject> call, Response<JSONObject> response)
            {
                if (response != null && response.isSuccessful() && response.body() != null)
                {
                    try
                    {
                        JSONObject responseJSONObject = response.body();

                        int msgCode = responseJSONObject.getInt("msgCode");

                        if (msgCode == 100)
                        {
                            JSONObject dataJSONObject = responseJSONObject.getJSONObject("data");

                            long currentDateTime = DailyCalendar.getTimeGMT9(dataJSONObject.getString("currentDateTime"), DailyCalendar.ISO_8601_FORMAT);
                            long dailyDateTime = DailyCalendar.getTimeGMT9(dataJSONObject.getString("dailyDateTime"), DailyCalendar.ISO_8601_FORMAT);

                            mStartSaleTime = new SaleTime();
                            mStartSaleTime.setCurrentTime(currentDateTime);
                            mStartSaleTime.setDailyTime(dailyDateTime);

                            mEndSaleTime = mStartSaleTime.getClone(1);

                            requestFeaturedPlaceList();
                        } else
                        {
                            String message = responseJSONObject.getString("msg");
                            onErrorPopupMessage(msgCode, message);
                        }
                    } catch (Exception e)
                    {
                        onError(e);
                    }
                } else
                {
                    onErrorResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call<JSONObject> call, Throwable t)
            {
                onError(t);
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        AnalyticsManager.getInstance(this).recordScreen(this, AnalyticsManager.Screen.RECOMMEND_LIST, null);
    }

    private void initLayout(String title, String titleImageUrl)
    {
        initToolbar(title);

        // 백이미지
        final View backImageView = findViewById(R.id.backImageView);
        backImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });


        // 21:9 높이
        int height21x9 = Util.getRatioHeightType21x9(Util.getLCDWidth(this));

        // 이미지
        SimpleDraweeView simpleDraweeView = (SimpleDraweeView) findViewById(R.id.titleImageView);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height21x9);
        simpleDraweeView.setLayoutParams(layoutParams);

        Util.requestImageResize(this, simpleDraweeView, titleImageUrl);

        TextView titleTextView = (TextView) findViewById(R.id.titleTextView);
        titleTextView.setPadding(0, height21x9 * 32 / 100, 0, 0);

        // 리스트
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        EdgeEffectColor.setEdgeGlowColor(mRecyclerView, getResources().getColor(R.color.default_over_scroll_edge));

        if (mPlaceListAdapter == null)
        {
            mPlaceListAdapter = getPlaceListAdapter(mOnItemClickListener);
        }

        mRecyclerView.setAdapter(mPlaceListAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                int position = linearLayoutManager.findFirstVisibleItemPosition();

                if (position == 0 && linearLayoutManager.findViewByPosition(position).getBottom() > backImageView.getBottom())
                {
                    mDailyToolbarLayout.setToolbarVisibility(false, true);
                } else
                {
                    mDailyToolbarLayout.setToolbarVisibility(true, true);
                }
            }
        });

        mDailyToolbarLayout.setToolbarVisibility(false, false);
    }

    private void initToolbar(String title)
    {
        View toolbar = findViewById(R.id.toolbar);
        mDailyToolbarLayout = new DailyToolbarLayout(this, toolbar);
        mDailyToolbarLayout.initToolbar(title, new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        unLockUI();

        switch (requestCode)
        {
            case CODE_REQUEST_ACTIVITY_STAY_DETAIL:
            case CODE_REQUEST_ACTIVITY_GOURMET_DETAIL:
            {
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                    case CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_READY:
                        setResult(resultCode);
                        finish();
                        break;

                    case CODE_RESULT_ACTIVITY_REFRESH:
                        lockUI();

                        requestFeaturedPlaceList();
                        break;
                }
                break;
            }

            case CODE_REQUEST_ACTIVITY_CALENDAR:
                onCalendarActivityResult(resultCode, data);

                lockUI();

                requestFeaturedPlaceList();
                break;
        }
    }

    private ArrayList<PlaceViewItem> makePlaceList(List<? extends Place> placeList, String[] placeIndexs)
    {
        ArrayList<PlaceViewItem> placeViewItemList = new ArrayList<>();

        // 빈공간
        placeViewItemList.add(new PlaceViewItem(PlaceViewItem.TYPE_HEADER_VIEW, null));

        // 달력 넣기
        placeViewItemList.add(new PlaceViewItem(PlaceViewItem.TYPE_CALENDAR_VIEW, getCalendarDate()));

        if (placeList == null || placeList.size() == 0)
        {
            placeViewItemList.add(new PlaceViewItem(PlaceViewItem.TYPE_FOOTER_VIEW, null));
        } else
        {
            // 개수 넣기
            placeViewItemList.add(new PlaceViewItem(PlaceViewItem.TYPE_SECTION, getSectionTitle(placeList.size())));

            if (placeIndexs != null && placeIndexs.length > 0)
            {
                for (String hotelIndex : placeIndexs)
                {
                    try
                    {
                        int index = Integer.parseInt(hotelIndex);

                        for (Place place : placeList)
                        {
                            if (index == place.index)
                            {
                                placeViewItemList.add(new PlaceViewItem(PlaceViewItem.TYPE_ENTRY, place));
                            }
                        }
                    } catch (Exception e)
                    {
                        ExLog.d(e.toString());
                    }
                }
            } else
            {
                for (Place place : placeList)
                {
                    placeViewItemList.add(new PlaceViewItem(PlaceViewItem.TYPE_ENTRY, place));
                }
            }
        }

        return placeViewItemList;
    }

    protected void onPlaceList(ArrayList<Place> list)
    {
//        if (isFinishing() == true)
//        {
//            unLockUI();
//            return;
//        }
//
//        ArrayList<PlaceViewItem> placeViewItems = makePlaceList(list, mFeaturedIndex);
//
//        mPlaceListAdapter.setAll(placeViewItems);
//        mPlaceListAdapter.notifyDataSetChanged();
//
//        unLockUI();
    }

    protected View.OnClickListener mOnItemClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            int position = mRecyclerView.getChildAdapterPosition(view);
            if (position < 0)
            {
                return;
            }

            PlaceViewItem placeViewItem = mPlaceListAdapter.getItem(position);

            if (placeViewItem.mType == PlaceViewItem.TYPE_ENTRY)
            {
                onPlaceClick(view, placeViewItem, mPlaceListAdapter.getItemCount());
            }
        }
    };
}