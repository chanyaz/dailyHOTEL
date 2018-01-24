package com.daily.dailyhotel.screen.home.search.stay.inbound;

import android.support.annotation.NonNull;

import com.daily.base.OnBaseFragmentEventListener;
import com.daily.dailyhotel.base.BasePagerFragment;
import com.daily.dailyhotel.screen.home.search.SearchPresenter;
import com.twoheart.dailyhotel.databinding.FragmentSearchStayDataBinding;

public class SearchStayFragment extends BasePagerFragment<SearchStayFragmentPresenter, SearchStayFragment.OnEventListener>
{
    public interface OnEventListener extends OnBaseFragmentEventListener
    {
    }

    @NonNull
    @Override
    protected SearchStayFragmentPresenter createInstancePresenter()
    {
        return new SearchStayFragmentPresenter(this);
    }

    @Override
    protected OnEventListener getFragmentEventListener()
    {
        return super.getFragmentEventListener();
    }
}
