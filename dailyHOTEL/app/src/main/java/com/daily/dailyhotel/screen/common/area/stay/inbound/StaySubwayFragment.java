package com.daily.dailyhotel.screen.common.area.stay.inbound;


import android.support.annotation.NonNull;

import com.daily.base.OnBaseFragmentEventListener;
import com.daily.dailyhotel.base.BasePagerFragment;
import com.daily.dailyhotel.entity.StayArea;

/**
 * Created by sheldon
 * Clean Architecture
 */
public class StaySubwayFragment extends BasePagerFragment<StaySubwayFragmentPresenter, StaySubwayFragment.OnEventListener>
{
    public interface OnEventListener extends OnBaseFragmentEventListener
    {
        void onAroundSearchClick();

        void onAreaClick(StayArea areaGroup, StayArea area);
    }

    @NonNull
    @Override
    protected StaySubwayFragmentPresenter createInstancePresenter()
    {
        return new StaySubwayFragmentPresenter(this);
    }

    @Override
    protected OnEventListener getFragmentEventListener()
    {
        return super.getFragmentEventListener();
    }
}
