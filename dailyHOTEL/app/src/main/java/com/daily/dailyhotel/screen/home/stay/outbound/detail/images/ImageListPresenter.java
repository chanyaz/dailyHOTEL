package com.daily.dailyhotel.screen.home.stay.outbound.detail.images;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.daily.base.BaseAnalyticsInterface;
import com.daily.dailyhotel.base.BaseExceptionPresenter;
import com.daily.dailyhotel.entity.StayOutboundDetailImage;
import com.daily.dailyhotel.parcel.StayOutboundDetailImageParcel;
import com.twoheart.dailyhotel.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sheldon
 * Clean Architecture
 */
public class ImageListPresenter extends BaseExceptionPresenter<ImageListActivity, ImageListInterface> implements ImageListView.OnEventListener
{
    private ImageListAnalyticsInterface mAnalytics;

    private String mTitle;
    private List<StayOutboundDetailImage> mImageList;
    private int mIndex;
    private boolean mTouchMoving;

    public interface ImageListAnalyticsInterface extends BaseAnalyticsInterface
    {
    }

    public ImageListPresenter(@NonNull ImageListActivity activity)
    {
        super(activity);
    }

    @NonNull
    @Override
    protected ImageListInterface createInstanceViewInterface()
    {
        return new ImageListView(getActivity(), this);
    }

    @Override
    public void constructorInitialize(ImageListActivity activity)
    {
        setContentView(R.layout.activity_stay_outbound_image_list_data);

        setAnalytics(new ImageListAnalyticsImpl());

        setRefresh(true);
    }

    @Override
    public void setAnalytics(BaseAnalyticsInterface analytics)
    {
        mAnalytics = (ImageListAnalyticsInterface) analytics;
    }

    @Override
    public boolean onIntent(Intent intent)
    {
        if (intent == null)
        {
            return true;
        }

        mTitle = intent.getStringExtra(ImageListActivity.INTENT_EXTRA_DATA_TITLE);
        ArrayList<StayOutboundDetailImageParcel> imageList = intent.getParcelableArrayListExtra(ImageListActivity.INTENT_EXTRA_DATA_IMAGE_LIST);

        if (imageList == null || imageList.size() == 0)
        {
            return false;
        }

        mImageList = new ArrayList<>();

        for (StayOutboundDetailImageParcel stayOutboundDetailImageParcel : imageList)
        {
            mImageList.add(stayOutboundDetailImageParcel.getStayOutboundDetailImage());
        }

        mIndex = intent.getIntExtra(ImageListActivity.INTENT_EXTRA_DATA_INDEX, 0);

        return true;
    }

    @Override
    public void onPostCreate()
    {
        getViewInterface().setToolbarTitle(mTitle);

        getViewInterface().setImageList(mImageList, mIndex);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        if (isRefresh() == true)
        {
            onRefresh(true);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onDestroy()
    {
        // 꼭 호출해 주세요.
        super.onDestroy();
    }

    @Override
    public void onFinish()
    {
        super.onFinish();

        if (mTouchMoving == true)
        {
            getActivity().overridePendingTransition(R.anim.hold, R.anim.fade_out);
        } else
        {
            getActivity().overridePendingTransition(R.anim.hold, R.anim.slide_out_bottom);
        }
    }

    @Override
    public boolean onBackPressed()
    {
        return super.onBackPressed();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        unLockAll();
    }

    @Override
    protected void onRefresh(boolean showProgress)
    {
        if (getActivity().isFinishing() == true)
        {
            return;
        }

    }

    @Override
    public void onBackClick()
    {
        getActivity().onBackPressed();
    }

    @Override
    public void onTouchMoving(boolean moving)
    {
        mTouchMoving = moving;
    }
}