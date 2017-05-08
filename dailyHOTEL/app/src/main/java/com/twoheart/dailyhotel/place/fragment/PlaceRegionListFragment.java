package com.twoheart.dailyhotel.place.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.daily.base.util.ScreenUtils;
import com.daily.base.util.VersionUtils;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Area;
import com.twoheart.dailyhotel.model.Province;
import com.twoheart.dailyhotel.model.RegionViewItem;
import com.twoheart.dailyhotel.place.activity.PlaceRegionListActivity;
import com.twoheart.dailyhotel.place.adapter.PlaceRegionAnimatedExpandableListAdapter;
import com.twoheart.dailyhotel.place.base.BaseActivity;
import com.twoheart.dailyhotel.place.base.BaseFragment;
import com.twoheart.dailyhotel.util.DailyPreference;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.widget.DailyAnimatedExpandableListView;

import java.util.List;

public abstract class PlaceRegionListFragment extends BaseFragment
{
    OnPlaceRegionListFragment mOnPlaceRegionListFragment;

    DailyAnimatedExpandableListView mListView;
    PlaceRegionAnimatedExpandableListAdapter mAdapter;
    private Province mSelectedProvince;

    private PlaceRegionListActivity.Region mRegion;
    protected String mCategoryCode;

    private View mTermsOfLocationView;
    protected BaseActivity mBaseActivity;

    public interface OnPlaceRegionListFragment
    {
        void onActivityCreated(PlaceRegionListFragment placeRegionListFragment);

        void onRegionClick(Province province);

        void onAroundSearchClick();
    }

    protected abstract String getAroundPlaceText();

    protected abstract void recordAnalyticsScreen(PlaceRegionListActivity.Region region);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mBaseActivity = (BaseActivity) getActivity();

        mListView = (DailyAnimatedExpandableListView) inflater.inflate(R.layout.fragment_region_list, container, false);
        mListView.setOnGroupClickListener(mOnGroupClickListener);

        return mListView;
    }

    @Override
    public void onResume()
    {
        if (mAdapter != null)
        {
            recordAnalyticsScreen(mRegion);
        }

        super.onResume();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        if (mOnPlaceRegionListFragment != null)
        {
            mOnPlaceRegionListFragment.onActivityCreated(this);
        }
    }

    private View getHeaderLayout()
    {
        View headerView = LayoutInflater.from(mBaseActivity).inflate(R.layout.layout_region_around_search_header, null);

        View searchAroundLayout = headerView.findViewById(R.id.searchAroundLayout);
        searchAroundLayout.setOnClickListener(mOnHeaderClickListener);

        TextView text01View = (TextView) headerView.findViewById(R.id.text01View);
        text01View.setText(getAroundPlaceText());

        mTermsOfLocationView = headerView.findViewById(R.id.text02View);
        updateTermsOfLocationView();

        return headerView;
    }

    public void updateTermsOfLocationView()
    {
        if (DailyPreference.getInstance(mBaseActivity).isAgreeTermsOfLocation() == true)
        {
            mTermsOfLocationView.setVisibility(View.GONE);
        } else
        {
            mTermsOfLocationView.setVisibility(View.VISIBLE);
        }
    }

    public void setRegionViewList(BaseActivity baseActivity, List<RegionViewItem> arrayList)
    {
        if (mAdapter == null)
        {
            mAdapter = new PlaceRegionAnimatedExpandableListAdapter(baseActivity);
            mAdapter.setIsTablet(ScreenUtils.isTabletDevice(baseActivity));
            mAdapter.setOnChildClickListener(mOnChildClickListener);
        }

        mAdapter.setData(arrayList);

        if (mListView == null)
        {
            Util.restartApp(getContext());
            return;
        }

        if (mListView.getHeaderViewsCount() == 0)
        {
            mListView.addHeaderView(getHeaderLayout());
        }

        mListView.setAdapter(mAdapter);
        selectedPreviousArea(mSelectedProvince, arrayList);
    }

    public void setInformation(PlaceRegionListActivity.Region region, Province province, String categoryCode)
    {
        mRegion = region;
        mSelectedProvince = province;
        mCategoryCode = categoryCode;
    }

    public void setOnPlaceRegionListFragmentListener(OnPlaceRegionListFragment listener)
    {
        mOnPlaceRegionListFragment = listener;
    }

    public PlaceRegionListActivity.Region getRegion()
    {
        return mRegion;
    }

    View getGroupView(int groupPosition)
    {
        int count = mListView.getChildCount();

        for (int i = 0; i < count; i++)
        {
            View childView = mListView.getChildAt(i);

            if (childView != null)
            {
                Object tag = childView.getTag();

                if (tag != null && tag instanceof Integer == true)
                {
                    Integer childTag = (Integer) tag;

                    if (childTag == groupPosition)
                    {
                        return childView;
                    }
                }
            }
        }

        return null;
    }

    void expandGroupWidthAnimation(int groupPosition, final RegionViewItem regionViewItem)
    {
        mListView.expandGroupWithAnimation(groupPosition, new DailyAnimatedExpandableListView.OnAnimationListener()
        {
            @Override
            public void onAnimationEnd()
            {
                releaseUiComponent();

                regionViewItem.isExpandGroup = true;
            }
        });

        mListView.setTag(groupPosition);

        View groupView = getGroupView(groupPosition);

        if (groupView != null)
        {
            onGroupExpand(groupView, regionViewItem);
        }
    }

    void postExpandGroupWithAnimation(final int groupPosition)
    {
        mListView.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                if (mListView.isGroupExpanded(groupPosition))
                {
                    RegionViewItem regionViewItem = mAdapter.getAreaItem(groupPosition);

                    mListView.collapseGroupWithAnimation(groupPosition);

                    View groupView = getGroupView(groupPosition);

                    if (groupView != null)
                    {
                        onGroupCollapse(groupView, regionViewItem);
                    }
                } else
                {
                    final RegionViewItem regionViewItem = mAdapter.getAreaItem(groupPosition);

                    try
                    {
                        expandGroupWidthAnimation(groupPosition, regionViewItem);
                    } catch (Exception e)
                    {
                        mListView.setSelection(groupPosition);

                        postExpandGroupWithAnimation(groupPosition);
                    }
                }
            }
        }, 100);
    }

    private void selectedPreviousArea(Province province, List<RegionViewItem> arrayList)
    {
        if (province == null || arrayList == null)
        {
            return;
        }

        int size = arrayList.size();

        for (int i = 0; i < size; i++)
        {
            RegionViewItem regionViewItem = arrayList.get(i);

            if (province.getProvinceIndex() == regionViewItem.getProvince().getProvinceIndex())
            {
                if (regionViewItem.getAreaList().size() == 0)
                {
                    // 상세 지역이 없는 경우.
                    mListView.setSelection(i);
                    mListView.setSelectedGroup(i);

                    regionViewItem.isExpandGroup = false;
                } else
                {
                    mListView.setSelection(i);
                    mListView.expandGroup(i);
                    mListView.setTag(i);

                    regionViewItem.isExpandGroup = true;
                }
                break;
            }
        }
    }

    public void onGroupExpand(View view, final RegionViewItem regionViewItem)
    {
        if (view.getVisibility() != View.VISIBLE)
        {
            releaseUiComponent();

            regionViewItem.isExpandGroup = true;
            return;
        }

        if (VersionUtils.isOverAPI11() == true)
        {
            final ImageView imageView = (ImageView) view.findViewById(R.id.updownArrowImageView);

            RotateAnimation animation = new RotateAnimation(0.0f, -180.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setFillBefore(true);
            animation.setFillAfter(true);
            animation.setDuration(250);

            if (imageView != null)
            {
                animation.setAnimationListener(new Animation.AnimationListener()
                {
                    @Override
                    public void onAnimationStart(Animation animation)
                    {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation)
                    {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onAnimationEnd(Animation animation)
                    {
                        releaseUiComponent();
                        imageView.setAnimation(null);
                        imageView.setImageResource(R.drawable.ic_region_ic_sub_v_top);

                        regionViewItem.isExpandGroup = true;
                    }
                });

                imageView.startAnimation(animation);
            } else
            {
                releaseUiComponent();

                regionViewItem.isExpandGroup = true;
            }
        } else
        {
            releaseUiComponent();

            regionViewItem.isExpandGroup = true;
        }
    }

    public void onGroupCollapse(View view, final RegionViewItem regionViewItem)
    {
        if (view.getVisibility() != View.VISIBLE)
        {
            releaseUiComponent();

            regionViewItem.isExpandGroup = false;
            return;
        }

        if (VersionUtils.isOverAPI11() == true)
        {
            final ImageView imageView = (ImageView) view.findViewById(R.id.updownArrowImageView);

            RotateAnimation animation = new RotateAnimation(0.0f, 180.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setFillBefore(true);
            animation.setFillAfter(true);
            animation.setDuration(250);

            if (imageView != null)
            {
                animation.setAnimationListener(new Animation.AnimationListener()
                {
                    @Override
                    public void onAnimationStart(Animation animation)
                    {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation)
                    {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onAnimationEnd(Animation animation)
                    {
                        releaseUiComponent();

                        imageView.setAnimation(null);
                        imageView.setImageResource(R.drawable.ic_region_ic_sub_v);

                        regionViewItem.isExpandGroup = false;
                    }
                });

                imageView.startAnimation(animation);
            } else
            {
                releaseUiComponent();

                regionViewItem.isExpandGroup = false;
            }
        } else
        {
            releaseUiComponent();

            regionViewItem.isExpandGroup = false;
        }
    }

    private OnGroupClickListener mOnGroupClickListener = new OnGroupClickListener()
    {
        @Override
        public boolean onGroupClick(ExpandableListView parent, View v, final int groupPosition, long id)
        {
            if (isLockUiComponent() == true)
            {
                return true;
            }

            lockUiComponent();

            //
            if (mAdapter.getChildrenCount(groupPosition) == 0)
            {
                if (mOnPlaceRegionListFragment != null)
                {
                    mOnPlaceRegionListFragment.onRegionClick(mAdapter.getGroup(groupPosition));
                }
                return true;
            }

            Object tag = mListView.getTag();

            int previousGroupPosition = -1;

            if (tag != null && tag instanceof Integer == true)
            {
                previousGroupPosition = (Integer) tag;

                RegionViewItem regionViewItem = mAdapter.getAreaItem(previousGroupPosition);

                if (mListView.isGroupExpanded(previousGroupPosition))
                {
                    if (previousGroupPosition == groupPosition)
                    {
                        mListView.collapseGroupWithAnimation(previousGroupPosition);

                        View preGroupView = getGroupView(previousGroupPosition);

                        if (preGroupView == null)
                        {
                            regionViewItem.isExpandGroup = false;
                        } else
                        {
                            onGroupCollapse(preGroupView, regionViewItem);
                        }
                    } else
                    {
                        mListView.collapseGroup(previousGroupPosition);
                        regionViewItem.isExpandGroup = false;
                    }
                } else
                {
                    previousGroupPosition = -1;
                }
            }

            if (previousGroupPosition == groupPosition)
            {
                releaseUiComponent();
                return true;
            }

            postExpandGroupWithAnimation(groupPosition);

            return true;
        }
    };

    private View.OnClickListener mOnChildClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            Object tag = view.getTag();

            if (tag == null)
            {
                return;
            }

            if (tag instanceof Area == false || mOnPlaceRegionListFragment == null)
            {
                return;
            }

            Area area = (Area) tag;

            if (area.index == -1)
            {
                Integer groupPosition = (Integer) view.getTag(view.getId());

                if (groupPosition != null)
                {
                    mOnPlaceRegionListFragment.onRegionClick(mAdapter.getGroup(groupPosition));
                }
            } else
            {
                mOnPlaceRegionListFragment.onRegionClick(area);
            }
        }
    };

    private View.OnClickListener mOnHeaderClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            mOnPlaceRegionListFragment.onAroundSearchClick();
        }
    };
}