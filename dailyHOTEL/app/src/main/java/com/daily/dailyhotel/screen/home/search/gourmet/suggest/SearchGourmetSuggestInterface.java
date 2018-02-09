package com.daily.dailyhotel.screen.home.search.gourmet.suggest;

import com.daily.base.BaseDialogViewInterface;
import com.daily.dailyhotel.entity.GourmetSuggest;

import java.util.List;

public interface SearchGourmetSuggestInterface extends BaseDialogViewInterface
{
    void setSuggests(List<GourmetSuggest> gourmetSuggestList);

    void setSuggest(String suggest);

    void showKeyboard();

    void hideKeyboard();

    void setProgressBarVisible(boolean visible);

    void setRecentlySuggests(GourmetSuggest locationSuggest, List<GourmetSuggest> gourmetSuggestList);

    void setPopularAreaSuggests(GourmetSuggest locationSuggest, List<GourmetSuggest> gourmetSuggestList);

    int getRecentlySuggestEntryCount();

    void setKeywordEditHint(String hint);

    void setKeywordEditText(String text);

    void setVoiceSearchEnabled(boolean enabled);

    void removeRecentlyItem(int position);

    void removeRecentlySection(int menuType);

    void setNearbyGourmetSuggest(GourmetSuggest nearbyGourmetSuggest);
}
