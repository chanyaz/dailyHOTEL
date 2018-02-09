package com.daily.dailyhotel.screen.home.search.stay.inbound.suggest;

import com.daily.base.BaseDialogViewInterface;
import com.daily.dailyhotel.entity.GourmetSuggest;
import com.daily.dailyhotel.entity.StayOutboundSuggest;
import com.daily.dailyhotel.entity.StaySuggest;

import java.util.List;

public interface SearchStaySuggestInterface extends BaseDialogViewInterface
{
    void setStaySuggests(List<StaySuggest> staySuggestList);

    void setGourmetSuggests(List<GourmetSuggest> gourmetSuggestList);

    void setStayOutboundSuggests(List<StayOutboundSuggest> stayOutboundSuggestList);

    void setSuggest(String suggest);

    void showKeyboard();

    void hideKeyboard();

    void setProgressBarVisible(boolean visible);

    void setRecentlySuggests(StaySuggest locationSuggest, List<StaySuggest> staySuggestList);

    void setPopularAreaSuggests(StaySuggest locationSuggest, List<StaySuggest> staySuggestList);

    int getRecentlySuggestAllEntryCount();

    int getRecentlySuggestEntryCount(int menuType);

    void setKeywordEditHint(String hint);

    void setKeywordEditText(String text);

    void setVoiceSearchEnabled(boolean enabled);

    void removeRecentlyItem(int position);

    void removeRecentlySection(int menuType);

    void setNearbyStaySuggest(StaySuggest nearbyStaySuggest);
}
