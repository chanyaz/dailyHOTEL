package com.twoheart.dailyhotel.place.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.twoheart.dailyhotel.model.EventBanner;
import com.twoheart.dailyhotel.model.PlaceCuration;
import com.twoheart.dailyhotel.place.base.BaseActivity;
import com.twoheart.dailyhotel.place.base.BaseFragment;
import com.twoheart.dailyhotel.place.base.BaseNetworkController;
import com.twoheart.dailyhotel.place.layout.PlaceListLayout;
import com.twoheart.dailyhotel.util.Constants;

public abstract class PlaceListFragment extends BaseFragment implements Constants
{
    protected int mPlaceCount;
    protected int mLoadMorePageIndex;

    protected ViewType mViewType;

    protected View mBottomOptionLayout; // 애니매이션 때문에 어쩔수 없음.

    protected BaseActivity mBaseActivity;

    protected PlaceListLayout mPlaceListLayout;

    protected BaseNetworkController mNetworkController;

    protected OnPlaceListFragmentListener mOnPlaceListFragmentListener;

    // onPlaceClick 부분이 있는데 이부분은 고메와 호텔은 서로 상속받아서 사용한다.
    public interface OnPlaceListFragmentListener
    {
        void onEventBannerClick(EventBanner eventBanner);

        void onActivityCreated(PlaceListFragment placeListFragment);

        void onScrolled(RecyclerView recyclerView, int dx, int dy);

        void onScrollStateChanged(RecyclerView recyclerView, int newState);

        void onShowMenuBar();

        void onFilterClick();

        void onShowActivityEmptyView(boolean isShow);

        void onRecordAnalytics(Constants.ViewType viewType);
    }

    protected abstract int getLayoutResourceId();

    protected abstract PlaceListLayout getPlaceListLayout();

    protected abstract BaseNetworkController getNetworkController();

    protected abstract void refreshList(boolean isShowProgress, int page);

    public abstract void setPlaceCuration(PlaceCuration curation);

    public void setPlaceOnListFragmentListener(OnPlaceListFragmentListener listener)
    {
        mOnPlaceListFragmentListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mBaseActivity = (BaseActivity) getActivity();
        mViewType = ViewType.LIST;
        mLoadMorePageIndex = 1;

        mPlaceListLayout = getPlaceListLayout();
        mPlaceListLayout.setBottomOptionLayout(mBottomOptionLayout);

        mNetworkController = getNetworkController();

        return mPlaceListLayout.onCreateView(getLayoutResourceId(), container);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        if (mOnPlaceListFragmentListener != null)
        {
            mOnPlaceListFragmentListener.onActivityCreated(this);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (mViewType == ViewType.MAP)
        {
            PlaceListMapFragment placeListMapFragment = mPlaceListLayout.getListMapFragment();

            if (placeListMapFragment != null)
            {
                placeListMapFragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    public void clearList()
    {
        mPlaceCount = 0;
        mPlaceListLayout.clearList();
    }

    public void refreshList(boolean isShowProgress)
    {
        if (mViewType == null)
        {
            return;
        }

        switch (mViewType)
        {
            case LIST:
                int size = mPlaceListLayout.getItemCount();
                if (size == 0)
                {
                    refreshList(isShowProgress, 1);
                }
                break;

            case MAP:
                refreshList(isShowProgress, 0);
                break;

            default:
                break;
        }
    }

    public void addList(boolean isShowProgress)
    {
        refreshList(isShowProgress, mLoadMorePageIndex + 1);
    }

    public void setVisibility(ViewType viewType, boolean isCurrentPage)
    {
        mViewType = viewType;
        mPlaceListLayout.setVisibility(getChildFragmentManager(), viewType, isCurrentPage);

        mOnPlaceListFragmentListener.onShowMenuBar();
    }

    public void setScrollListTop()
    {
        if (mPlaceListLayout == null)
        {
            return;
        }

        mPlaceListLayout.setScrollListTop();
    }

    public boolean hasSalesPlace()
    {
        return mPlaceListLayout.hasSalesPlace();
    }

    public void setBottomOptionLayout(View view)
    {
        mBottomOptionLayout = view;
    }

    public void setViewType(ViewType viewType)
    {
        this.mViewType = viewType;
    }

    public int getPlaceCount()
    {
        return mPlaceCount;
    }
}
