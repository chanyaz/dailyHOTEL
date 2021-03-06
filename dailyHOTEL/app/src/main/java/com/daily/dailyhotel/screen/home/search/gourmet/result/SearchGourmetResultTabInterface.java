package com.daily.dailyhotel.screen.home.search.gourmet.result;

import android.app.Activity;

import com.daily.base.BaseAnalyticsInterface;
import com.daily.base.BaseDialogViewInterface;
import com.daily.base.OnBaseEventListener;
import com.daily.dailyhotel.base.BasePagerFragment;
import com.daily.dailyhotel.entity.CampaignTag;
import com.daily.dailyhotel.entity.GourmetSuggest;

import java.util.List;

import io.reactivex.Observable;

public interface SearchGourmetResultTabInterface
{
    interface ViewInterface extends BaseDialogViewInterface
    {
        void setViewType(SearchGourmetResultTabPresenter.ViewType viewType);

        void setToolbarTitleImageResource(int resId);

        void setToolbarDateText(String text);

        void setToolbarRadiusSpinnerVisible(boolean visible);

        void setRadiusSpinnerSelection(float radius);

        void setFloatingActionViewVisible(boolean visible);

        void resetFloatingActionViewTranslation();

        void setOptionFilterSelected(boolean selected);

        Observable<BasePagerFragment> setCampaignTagFragment();

        Observable<BasePagerFragment> setSearchResultFragment();

        void setEmptyViewVisible(boolean visible);

        void setEmptyViewCampaignTagVisible(boolean visible);

        void setEmptyViewCampaignTag(String title, List<CampaignTag> campaignTagList);

        boolean onFragmentBackPressed();

        void refreshCurrentFragment();

        void removeAllFragment();
    }

    interface OnEventListener extends OnBaseEventListener
    {
        void onToolbarTitleClick();

        void onResearchClick();

        void onEmptyStayResearchClick();

        void onFinishAndRefresh();

        void onViewTypeClick();

        void onFilterClick();

        void onCalendarClick();

        void onChangedRadius(float radius);

        void setEmptyViewVisible(boolean visible);

        void onStayClick();

        void onStayOutboundClick();

        void onCampaignTagClick(CampaignTag campaignTag);
    }

    interface AnalyticsInterface extends BaseAnalyticsInterface
    {
        void onEventChangedViewType(Activity activity, SearchGourmetResultTabPresenter.ViewType viewType);

        void onEventCalendarClick(Activity activity);

        void onEventFilterClick(Activity activity);

        void onEventBackClick(Activity activity);

        void onEventCancelClick(Activity activity);

        void onEventResearchClick(Activity activity, GourmetSuggest suggest);

        void onEventChangedRadius(Activity activity, GourmetSuggest suggest, float radius);

        void onEventStayClick(Activity activity);

        void onEventStayOutboundClick(Activity activity);

        void onEventCampaignTagClick(Activity activity, int index);
    }
}
