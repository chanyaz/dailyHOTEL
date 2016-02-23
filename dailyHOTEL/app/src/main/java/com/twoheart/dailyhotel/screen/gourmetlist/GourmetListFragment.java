package com.twoheart.dailyhotel.screen.gourmetlist;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.activity.BaseActivity;
import com.twoheart.dailyhotel.fragment.BaseFragment;
import com.twoheart.dailyhotel.fragment.PlaceMapFragment;
import com.twoheart.dailyhotel.model.Area;
import com.twoheart.dailyhotel.model.EventBanner;
import com.twoheart.dailyhotel.model.Gourmet;
import com.twoheart.dailyhotel.model.GourmetCurationOption;
import com.twoheart.dailyhotel.model.Place;
import com.twoheart.dailyhotel.model.PlaceViewItem;
import com.twoheart.dailyhotel.model.Province;
import com.twoheart.dailyhotel.model.SaleTime;
import com.twoheart.dailyhotel.network.DailyNetworkAPI;
import com.twoheart.dailyhotel.network.response.DailyHotelJsonResponseListener;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.ExLog;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager.Screen;
import com.twoheart.dailyhotel.view.widget.DailyToast;
import com.twoheart.dailyhotel.view.widget.PinnedSectionRecycleView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GourmetListFragment extends BaseFragment implements Constants
{
    private static final int APPBARLAYOUT_DRAG_DISTANCE = 200;

    protected PinnedSectionRecycleView mGourmetRecycleView;
    protected GourmetListAdapter mGourmetAdapter;
    protected SaleTime mSaleTime;

    private View mEmptyView;
    private FrameLayout mMapLayout;
    private PlaceMapFragment mPlaceMapFragment;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private List<EventBanner> mEventBannerList;

    private GourmetMainFragment.VIEW_TYPE mViewType;
    protected boolean mScrollListTop;
    protected GourmetMainFragment.OnCommunicateListener mOnCommunicateListener;

    private int mDownDistance;
    private int mUpDistance;

    private boolean mIsAttach;

    protected List<Gourmet> mGourmetList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_gourmet_list, container, false);

        mGourmetRecycleView = (PinnedSectionRecycleView) view.findViewById(R.id.recycleView);
        mGourmetRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
        mGourmetRecycleView.setTag("GourmetListFragment");

        BaseActivity baseActivity = (BaseActivity) getActivity();

        mGourmetAdapter = new GourmetListAdapter(baseActivity, new ArrayList<PlaceViewItem>(), mOnItemClickListener, mOnEventBannerItemClickListener);
        mGourmetRecycleView.setAdapter(mGourmetAdapter);
        mGourmetRecycleView.setOnScrollListener(mOnScrollListener);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.dh_theme_color);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                mOnCommunicateListener.refreshAll(false);
            }
        });

        mEmptyView = view.findViewById(R.id.emptyView);

        mMapLayout = (FrameLayout) view.findViewById(R.id.mapLayout);

        mViewType = GourmetMainFragment.VIEW_TYPE.LIST;

        setVisibility(mViewType);

        mGourmetRecycleView.setShadowVisible(false);

        return view;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        mIsAttach = true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (mViewType == GourmetMainFragment.VIEW_TYPE.MAP)
        {
            if (mPlaceMapFragment != null)
            {
                mPlaceMapFragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if (mViewType == GourmetMainFragment.VIEW_TYPE.MAP)
        {
            if (mPlaceMapFragment != null)
            {
                mPlaceMapFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    public void onPageSelected(boolean isRequestHotelList)
    {
        BaseActivity baseActivity = (BaseActivity) getActivity();

        if (baseActivity == null)
        {
            return;
        }

        baseActivity.invalidateOptionsMenu();
    }

    public void onPageUnSelected()
    {
    }

    public void onRefreshComplete()
    {
        mSwipeRefreshLayout.setRefreshing(false);

        if (mViewType == GourmetMainFragment.VIEW_TYPE.MAP)
        {
            return;
        }

        Object objectTag = mSwipeRefreshLayout.getTag();

        if (objectTag == null)
        {
            mSwipeRefreshLayout.setTag(mSwipeRefreshLayout.getId());

            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
            animation.setDuration(300);
            animation.setAnimationListener(new Animation.AnimationListener()
            {
                @Override
                public void onAnimationStart(Animation animation)
                {
                    mSwipeRefreshLayout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation)
                {
                    mSwipeRefreshLayout.setAnimation(null);
                }

                @Override
                public void onAnimationRepeat(Animation animation)
                {

                }
            });

            mSwipeRefreshLayout.startAnimation(animation);
        }
    }

    /**
     * 새로 고침을 하지 않고 기존의 있는 데이터를 보여준다.
     *
     * @param type
     * @param isCurrentPage
     */
    public void setViewType(GourmetMainFragment.VIEW_TYPE type, boolean isCurrentPage)
    {
        mViewType = type;

        if (mEmptyView.getVisibility() == View.VISIBLE)
        {
            setVisibility(GourmetMainFragment.VIEW_TYPE.GONE);
        } else
        {
            switch (type)
            {
                case LIST:
                    setVisibility(GourmetMainFragment.VIEW_TYPE.LIST, isCurrentPage);
                    break;

                case MAP:
                    setVisibility(GourmetMainFragment.VIEW_TYPE.MAP, isCurrentPage);

                    if (mPlaceMapFragment != null)
                    {
                        mPlaceMapFragment.setOnCommunicateListener(mOnCommunicateListener);

                        if (isCurrentPage == true)
                        {
                            List<PlaceViewItem> arrayList = mGourmetAdapter.getAll();

                            if (arrayList != null)
                            {
                                mPlaceMapFragment.setPlaceViewItemList(arrayList, getSelectedSaleTime(), false);
                            }
                        }
                    }
                    break;

                case GONE:
                    break;
            }
        }
    }

    protected void setVisibility(GourmetMainFragment.VIEW_TYPE type, boolean isCurrentPage)
    {
        switch (type)
        {
            case LIST:
                mEmptyView.setVisibility(View.GONE);
                mMapLayout.setVisibility(View.GONE);

                // 맵과 리스트에서 당일상품 탭 안보이도록 수정

                if (mPlaceMapFragment != null)
                {
                    getChildFragmentManager().beginTransaction().remove(mPlaceMapFragment).commitAllowingStateLoss();
                    mMapLayout.removeAllViews();
                    mPlaceMapFragment = null;
                }

                mSwipeRefreshLayout.setVisibility(View.VISIBLE);
                break;

            case MAP:
                mEmptyView.setVisibility(View.GONE);
                mMapLayout.setVisibility(View.VISIBLE);

                if (isCurrentPage == true && mPlaceMapFragment == null)
                {
                    mPlaceMapFragment = new GourmetMapFragment();
                    getChildFragmentManager().beginTransaction().add(mMapLayout.getId(), mPlaceMapFragment).commitAllowingStateLoss();
                }

                mSwipeRefreshLayout.setVisibility(View.INVISIBLE);
                break;

            case GONE:
                AnalyticsManager.getInstance(getActivity()).recordScreen(Screen.DAILYGOURMET_LIST_EMPTY, null);

                mEmptyView.setVisibility(View.VISIBLE);
                mMapLayout.setVisibility(View.GONE);

                mSwipeRefreshLayout.setVisibility(View.INVISIBLE);
                break;
        }
    }

    protected void setVisibility(GourmetMainFragment.VIEW_TYPE type)
    {
        setVisibility(type, true);
    }

    protected SaleTime getSelectedSaleTime()
    {
        return mSaleTime;
    }

    protected SaleTime getSaleTime()
    {
        return mSaleTime;
    }

    public void setSaleTime(SaleTime saleTime)
    {
        mSaleTime = saleTime;
    }

    public void setOnCommunicateListener(GourmetMainFragment.OnCommunicateListener listener)
    {
        mOnCommunicateListener = listener;
    }

    public void refreshList()
    {
        Map<String, String> params = new HashMap<>();
        params.put("type", "gourmet");

        DailyNetworkAPI.getInstance().requestEventBannerList(mNetworkTag, params, mEventBannerListJsonResponseListener, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError volleyError)
            {
                fetchList();
            }
        });
    }

    public void fetchList()
    {
        GourmetCurationOption gourmetCurationOption = mOnCommunicateListener.getCurationOption();
        fetchList(gourmetCurationOption.getProvince(), getSelectedSaleTime(), null);
    }

    public void fetchList(Province province, SaleTime checkInSaleTime, SaleTime checkOutSaleTime)
    {
        BaseActivity baseActivity = (BaseActivity) getActivity();

        if (province == null || checkInSaleTime == null)
        {
            Util.restartApp(baseActivity);
            return;
        }

        lockUI();

        int stayDays = 0;

        if (checkOutSaleTime == null)
        {
            // 오늘, 내일인 경우
            stayDays = 1;
        } else
        {
            // 연박인 경우
            stayDays = checkOutSaleTime.getOffsetDailyDay() - checkInSaleTime.getOffsetDailyDay();
        }

        if (stayDays <= 0)
        {
            unLockUI();
            return;
        }

        String params = null;

        if (province instanceof Area)
        {
            Area area = (Area) province;

            params = String.format("?province_idx=%d&area_idx=%d&sday=%s", area.getProvinceIndex(), area.index, checkInSaleTime.getDayOfDaysDateFormat("yyMMdd"));
        } else
        {
            params = String.format("?province_idx=%d&sday=%s", province.getProvinceIndex(), checkInSaleTime.getDayOfDaysDateFormat("yyMMdd"));
        }

        if (DEBUG == true && this instanceof GourmetDaysListFragment)
        {
            baseActivity.showSimpleDialog(null, mSaleTime.toString() + "\n" + params, getString(R.string.dialog_btn_text_confirm), null);
        }

        DailyNetworkAPI.getInstance().requestGourmetList(mNetworkTag, params, mGourmetListJsonResponseListener, baseActivity);
    }

    public void setScrollListTop(boolean scrollListTop)
    {
        mScrollListTop = scrollListTop;
    }

    private void requestSortList(GourmetListFragment.SortType type, final Location location)
    {
        if (SortType.DEFAULT == type)
        {
            ExLog.d("Not supported type");
            return;
        }

        List<PlaceViewItem> arrayList = mGourmetAdapter.getAll();

        int size = arrayList.size();

        if (size == 0)
        {
            unLockUI();
            return;
        }

        for (int i = size - 1; i >= 0; i--)
        {
            PlaceViewItem placeViewItem = arrayList.get(i);

            if (placeViewItem.getType() != PlaceViewItem.TYPE_ENTRY)
            {
                arrayList.remove(i);
            }
        }

        switch (type)
        {
            case DISTANCE:
            {
                // 중복된 위치에 있는 호텔들은 위해서 소팅한다.
                Comparator<PlaceViewItem> comparator = new Comparator<PlaceViewItem>()
                {
                    public int compare(PlaceViewItem placeViewItem1, PlaceViewItem placeViewItem2)
                    {
                        Place place1 = placeViewItem1.<Gourmet>getItem();
                        Place place2 = placeViewItem2.<Gourmet>getItem();

                        float[] results1 = new float[3];
                        Location.distanceBetween(location.getLatitude(), location.getLongitude(), place1.latitude, place1.longitude, results1);
                        ((Gourmet) place1).distance = results1[0];

                        float[] results2 = new float[3];
                        Location.distanceBetween(location.getLatitude(), location.getLongitude(), place2.latitude, place2.longitude, results2);
                        ((Gourmet) place2).distance = results2[0];

                        return Float.compare(results1[0], results2[0]);
                    }
                };

                if (arrayList.size() == 1)
                {
                    PlaceViewItem placeViewItem = arrayList.get(0);
                    Place place1 = placeViewItem.<Gourmet>getItem();

                    float[] results1 = new float[3];
                    Location.distanceBetween(location.getLatitude(), location.getLongitude(), place1.latitude, place1.longitude, results1);
                    ((Gourmet) place1).distance = results1[0];
                } else
                {
                    Collections.sort(arrayList, comparator);
                }
                break;
            }

            case LOW_PRICE:
            {
                // 중복된 위치에 있는 호텔들은 위해서 소팅한다.
                Comparator<PlaceViewItem> comparator = new Comparator<PlaceViewItem>()
                {
                    public int compare(PlaceViewItem placeViewItem1, PlaceViewItem placeViewItem2)
                    {
                        Place place1 = placeViewItem1.<Gourmet>getItem();
                        Place place2 = placeViewItem2.<Gourmet>getItem();

                        return place1.discountPrice - place2.discountPrice;
                    }
                };

                Collections.sort(arrayList, comparator);
                break;
            }

            case HIGH_PRICE:
            {
                // 중복된 위치에 있는 호텔들은 위해서 소팅한다.
                Comparator<PlaceViewItem> comparator = new Comparator<PlaceViewItem>()
                {
                    public int compare(PlaceViewItem placeViewItem1, PlaceViewItem placeViewItem2)
                    {
                        Place place1 = placeViewItem1.<Gourmet>getItem();
                        Place place2 = placeViewItem2.<Gourmet>getItem();

                        return place2.discountPrice - place1.discountPrice;
                    }
                };

                Collections.sort(arrayList, comparator);
                break;
            }
        }

        if (mOnCommunicateListener != null)
        {
            mOnCommunicateListener.expandedAppBar(true, true);
        }

        mGourmetAdapter.setSortType(type);
        mGourmetRecycleView.scrollToPosition(0);
        mGourmetAdapter.notifyDataSetChanged();
        unLockUI();
    }

    private ArrayList<PlaceViewItem> curationSorting(List<Gourmet> gourmetList, GourmetCurationOption gourmetCurationOption)
    {
        ArrayList<PlaceViewItem> gourmetViewItemList = new ArrayList<>();

        if (gourmetList == null || gourmetList.size() == 0)
        {
            return gourmetViewItemList;
        }

        final Location location = gourmetCurationOption.getLocation();

        switch (gourmetCurationOption.getSortType())
        {
            case DEFAULT:
                return makeSectionList(gourmetList);

            case DISTANCE:
            {
                // 중복된 위치에 있는 호텔들은 위해서 소팅한다.
                Comparator<Gourmet> comparator = new Comparator<Gourmet>()
                {
                    public int compare(Gourmet gourmet1, Gourmet gourmet2)
                    {
                        float[] results1 = new float[3];
                        Location.distanceBetween(location.getLatitude(), location.getLongitude(), gourmet1.latitude, gourmet1.longitude, results1);
                        gourmet1.distance = results1[0];

                        float[] results2 = new float[3];
                        Location.distanceBetween(location.getLatitude(), location.getLongitude(), gourmet2.latitude, gourmet2.longitude, results2);
                        gourmet2.distance = results2[0];

                        return Float.compare(results1[0], results2[0]);
                    }
                };

                if (gourmetList.size() == 1)
                {
                    Gourmet gourmet = gourmetList.get(0);

                    float[] results1 = new float[3];
                    Location.distanceBetween(location.getLatitude(), location.getLongitude(), gourmet.latitude, gourmet.longitude, results1);
                    gourmet.distance = results1[0];
                } else
                {
                    Collections.sort(gourmetList, comparator);
                }
                break;
            }

            case LOW_PRICE:
            {
                // 중복된 위치에 있는 호텔들은 위해서 소팅한다.
                Comparator<Gourmet> comparator = new Comparator<Gourmet>()
                {
                    public int compare(Gourmet gourmet1, Gourmet gourmet2)
                    {
                        return gourmet1.discountPrice - gourmet2.discountPrice;
                    }
                };

                Collections.sort(gourmetList, comparator);
                break;
            }

            case HIGH_PRICE:
            {
                // 중복된 위치에 있는 호텔들은 위해서 소팅한다.
                Comparator<Gourmet> comparator = new Comparator<Gourmet>()
                {
                    public int compare(Gourmet gourmet1, Gourmet gourmet2)
                    {
                        return gourmet2.discountPrice - gourmet1.discountPrice;
                    }
                };

                Collections.sort(gourmetList, comparator);
                break;
            }
        }

        for (Gourmet gourmet : gourmetList)
        {
            gourmetViewItemList.add(new PlaceViewItem(PlaceViewItem.TYPE_ENTRY, gourmet));
        }

        return gourmetViewItemList;
    }

    private ArrayList<PlaceViewItem> makeSectionList(List<Gourmet> gourmetList)
    {
        ArrayList<PlaceViewItem> placeViewItemList = new ArrayList<PlaceViewItem>();

        if (gourmetList == null || gourmetList.size() == 0)
        {
            return placeViewItemList;
        }

        String area = null;
        boolean hasDailyChoice = false;

        for (Gourmet gourmet : gourmetList)
        {
            String region = gourmet.districtName;

            if (Util.isTextEmpty(region) == true)
            {
                continue;
            }

            if (gourmet.isDailyChoice == true)
            {
                if (hasDailyChoice == false)
                {
                    hasDailyChoice = true;

                    PlaceViewItem section = new PlaceViewItem(PlaceViewItem.TYPE_SECTION, getString(R.string.label_dailychoice));
                    placeViewItemList.add(section);
                }
            } else
            {
                if (Util.isTextEmpty(area) == true || region.equalsIgnoreCase(area) == false)
                {
                    area = region;

                    PlaceViewItem section = new PlaceViewItem(PlaceViewItem.TYPE_SECTION, region);
                    placeViewItemList.add(section);
                }
            }

            placeViewItemList.add(new PlaceViewItem(PlaceViewItem.TYPE_ENTRY, gourmet));
        }

        return placeViewItemList;
    }

    public void curationList(GourmetCurationOption curationOption)
    {
        mScrollListTop = true;

        ArrayList<PlaceViewItem> placeViewItemList = curationList(mGourmetList, curationOption);
        setGourmetListViewItemList(placeViewItemList, curationOption.getSortType());
    }

    private ArrayList<PlaceViewItem> curationList(List<Gourmet> list, GourmetCurationOption curationOption)
    {
        List<Gourmet> gourmetList = curationCategory(list, curationOption.getFilterMap());

        return curationSorting(gourmetList, curationOption);
    }

    private List<Gourmet> curationCategory(List<Gourmet> list, Map<String, Integer> categoryMap)
    {
        if (categoryMap == null || categoryMap.size() == 0)
        {
            return list;
        }

        List<Gourmet> filteredCategoryList = new ArrayList<>(list);

        for (Gourmet gourmet : list)
        {
            if (categoryMap.containsKey(gourmet.category) == true)
            {
                filteredCategoryList.add(gourmet);
            }
        }

        return filteredCategoryList;
    }

    private void setGourmetListViewItemList(ArrayList<PlaceViewItem> gourmetListViewItemList, SortType sortType)
    {
        setVisibility(mViewType);

        if (mViewType == GourmetMainFragment.VIEW_TYPE.MAP)
        {
            mPlaceMapFragment.setOnCommunicateListener(mOnCommunicateListener);
            mPlaceMapFragment.setPlaceViewItemList(gourmetListViewItemList, getSelectedSaleTime(), mScrollListTop);

            AnalyticsManager.getInstance(getContext()).recordScreen(Screen.DAILYGOURMET_LIST_MAP, null);
        } else
        {
            AnalyticsManager.getInstance(getContext()).recordScreen(Screen.DAILYGOURMET_LIST, null);
        }

        mGourmetAdapter.clear();

        if (gourmetListViewItemList.size() == 0)
        {
            mGourmetAdapter.notifyDataSetChanged();

            setVisibility(GourmetMainFragment.VIEW_TYPE.GONE);

            mOnCommunicateListener.expandedAppBar(true, true);
            mOnCommunicateListener.setMapViewVisible(false);
        } else
        {
            if (sortType == SortType.DEFAULT)
            {
                if (mEventBannerList != null && mEventBannerList.size() > 0)
                {
                    PlaceViewItem placeViewItem = new PlaceViewItem(PlaceViewItem.TYPE_EVENT_BANNER, mEventBannerList);
                    gourmetListViewItemList.add(0, placeViewItem);
                }
            }

            mGourmetAdapter.addAll(gourmetListViewItemList, sortType);
            mGourmetAdapter.notifyDataSetChanged();

            if (mScrollListTop == true)
            {
                mScrollListTop = false;
                mGourmetRecycleView.scrollToPosition(0);
            }

            if (mOnCommunicateListener != null)
            {
                mOnCommunicateListener.setMapViewVisible(true);
            }
        }
    }

    public boolean hasSalesPlace()
    {
        boolean hasPlace = false;

        List<PlaceViewItem> arrayList = mGourmetAdapter.getAll();

        if (arrayList != null)
        {
            for (PlaceViewItem placeViewItem : arrayList)
            {
                if (placeViewItem.getType() == PlaceViewItem.TYPE_ENTRY//
                    && placeViewItem.<Gourmet>getItem().isSoldOut == false)
                {
                    hasPlace = true;
                    break;
                }
            }
        }

        return hasPlace;
    }

    private void recordAnalyticsSortTypeEvent(Context context, SortType sortType)
    {
        if (context == null || sortType == null)
        {
            return;
        }

        String label;

        switch (sortType)
        {
            case DISTANCE:
                label = context.getString(R.string.label_sort_by_distance);
                break;

            case LOW_PRICE:
                label = context.getString(R.string.label_sort_by_low_price);
                break;

            case HIGH_PRICE:
                label = context.getString(R.string.label_sort_by_high_price);
                break;

            default:
                label = context.getString(R.string.label_sort_by_area);
                break;
        }

        AnalyticsManager.getInstance(getContext()).recordEvent(AnalyticsManager.Category.NAVIGATION//
            , AnalyticsManager.Action.GOURMET_SORTING_CLICKED, label, null);
    }

    public void resetScrollDistance(boolean isUpDistance)
    {
        if (isUpDistance == true)
        {
            mDownDistance = 1;
            mUpDistance = 0;
        } else
        {
            mUpDistance = -1;
            mDownDistance = 0;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener()
    {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy)
        {
            super.onScrolled(recyclerView, dx, dy);

            if (dy < 0)
            {
                if (mDownDistance == 1)
                {
                    return;
                }

                mDownDistance += dy;

                BaseActivity baseActivity = (BaseActivity) getActivity();

                if (-mDownDistance >= Util.dpToPx(baseActivity, APPBARLAYOUT_DRAG_DISTANCE))
                {
                    if (mOnCommunicateListener != null)
                    {
                        mUpDistance = 0;
                        mDownDistance = 1;
                        mOnCommunicateListener.showAppBarLayout();
                    }
                }
            } else if (dy > 0)
            {
                if (mUpDistance == -1)
                {
                    return;
                }

                mUpDistance += dy;

                BaseActivity baseActivity = (BaseActivity) getActivity();

                if (mUpDistance >= Util.dpToPx(baseActivity, APPBARLAYOUT_DRAG_DISTANCE))
                {
                    if (mOnCommunicateListener != null)
                    {
                        mDownDistance = 0;
                        mUpDistance = -1;
                        mOnCommunicateListener.showAppBarLayout();
                        mOnCommunicateListener.expandedAppBar(false, true);
                    }
                }
            }
        }
    };

    private View.OnClickListener mOnItemClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            BaseActivity baseActivity = (BaseActivity) getActivity();

            if (baseActivity == null)
            {
                return;
            }

            int position = mGourmetRecycleView.getChildAdapterPosition(view);

            if (position < 0)
            {
                refreshList();
                return;
            }

            PlaceViewItem gourmetViewItem = mGourmetAdapter.getItem(position);

            if (gourmetViewItem.getType() == PlaceViewItem.TYPE_ENTRY)
            {
                mOnCommunicateListener.selectPlace(gourmetViewItem, getSelectedSaleTime());
            }
        }
    };

    private View.OnClickListener mOnEventBannerItemClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            BaseActivity baseActivity = (BaseActivity) getActivity();

            if (baseActivity == null)
            {
                return;
            }

            Integer index = (Integer) view.getTag(view.getId());

            if (index != null)
            {
                EventBanner eventBanner = mEventBannerList.get(index.intValue());

                mOnCommunicateListener.selectEventBanner(eventBanner);
            }
        }
    };


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Listener
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private DailyHotelJsonResponseListener mEventBannerListJsonResponseListener = new DailyHotelJsonResponseListener()
    {
        @Override
        public void onResponse(String url, JSONObject response)
        {
            try
            {
                int msgCode = response.getInt("msgCode");

                if (msgCode == 100)
                {
                    JSONObject dataJSONObject = response.getJSONObject("data");

                    String baseUrl = dataJSONObject.getString("imgUrl");

                    JSONArray jsonArray = dataJSONObject.getJSONArray("eventBanner");

                    if (mEventBannerList == null)
                    {
                        mEventBannerList = new ArrayList<>();
                    }

                    mEventBannerList.clear();

                    int length = jsonArray.length();
                    for (int i = 0; i < length; i++)
                    {
                        try
                        {
                            EventBanner eventBanner = new EventBanner(jsonArray.getJSONObject(i), baseUrl);
                            mEventBannerList.add(eventBanner);
                        } catch (Exception e)
                        {
                            ExLog.d(e.toString());
                        }
                    }
                }
            } catch (Exception e)
            {
                ExLog.d(e.toString());
            } finally
            {
                fetchList();
            }
        }
    };

    private DailyHotelJsonResponseListener mGourmetListJsonResponseListener = new DailyHotelJsonResponseListener()
    {
        @Override
        public void onResponse(String url, JSONObject response)
        {
            BaseActivity baseActivity = (BaseActivity) getActivity();

            if (baseActivity == null)
            {
                return;
            }

            try
            {
                int msg_code = response.getInt("msg_code");

                if (msg_code != 0)
                {
                    if (response.has("msg") == true)
                    {
                        String msg = response.getString("msg");
                        DailyToast.showToast(baseActivity, msg, Toast.LENGTH_SHORT);
                    }

                    throw new NullPointerException("response == null");
                }

                JSONObject dataJSONObject = response.getJSONObject("data");

                String imageUrl = dataJSONObject.getString("imgUrl");
                JSONArray gourmetJSONArray = dataJSONObject.getJSONArray("saleList");

                int length;

                if (gourmetJSONArray == null)
                {
                    length = 0;
                } else
                {
                    length = gourmetJSONArray.length();
                }

                mGourmetList.clear();

                if (length == 0)
                {
                    mGourmetAdapter.clear();
                    mGourmetAdapter.notifyDataSetChanged();

                    setVisibility(GourmetMainFragment.VIEW_TYPE.GONE);

                    mOnCommunicateListener.expandedAppBar(true, true);
                    mOnCommunicateListener.setMapViewVisible(false);
                } else
                {
                    ArrayList<Gourmet> gourmetList = makeGourmetList(gourmetJSONArray, imageUrl);

                    // 필터 정보 넣기

                    GourmetCurationOption gourmetCurationOption = mOnCommunicateListener.getCurationOption();

                    // 기본적으로 보관한다.
                    mGourmetList.addAll(gourmetList);

                    ArrayList<PlaceViewItem> placeViewItemList = curationList(gourmetList, gourmetCurationOption);

                    setGourmetListViewItemList(placeViewItemList, gourmetCurationOption.getSortType());
                }

                // 리스트 요청 완료후에 날짜 탭은 애니매이션을 진행하도록 한다.
                onRefreshComplete();
            } catch (Exception e)
            {
                onError(e);
            } finally
            {
                unLockUI();
            }
        }

        private ArrayList<Gourmet> makeGourmetList(JSONArray jsonArray, String imageUrl) throws JSONException
        {
            if (jsonArray == null)
            {
                return new ArrayList<Gourmet>();
            }

            int length = jsonArray.length();
            ArrayList<Gourmet> gourmetList = new ArrayList<Gourmet>(length);
            JSONObject jsonObject;
            Gourmet gouremt;

            for (int i = 0; i < length; i++)
            {
                jsonObject = jsonArray.getJSONObject(i);

                gouremt = new Gourmet();

                if (gouremt.setData(jsonObject, imageUrl) == true)
                {
                    gourmetList.add(gouremt); // 추가.
                }
            }

            return gourmetList;
        }


    };

}
