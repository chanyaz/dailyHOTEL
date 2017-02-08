package com.twoheart.dailyhotel.screen.event;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.network.model.Event;
import com.twoheart.dailyhotel.place.base.BaseActivity;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.DailyDeepLink;
import com.twoheart.dailyhotel.util.DailyPreference;
import com.twoheart.dailyhotel.util.EdgeEffectColor;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager.Screen;
import com.twoheart.dailyhotel.widget.DailyToolbarLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class EventListActivity extends BaseActivity implements AdapterView.OnItemClickListener
{
    View mEmptyView;
    ListView mListView;
    EventListAdapter mEventListAdapter;
    private EventListNetworkController mEventListNetworkController;
    private boolean mDontReload;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        overridePendingTransition(R.anim.slide_in_right, R.anim.hold);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_eventlist);

        mEventListNetworkController = new EventListNetworkController(this, mNetworkTag, mOnNetworkControllerListener);

        DailyPreference.getInstance(this).setNewEvent(false);
        DailyPreference.getInstance(this).setViewedEventTime(DailyPreference.getInstance(this).getLastestEventTime());

        initToolbar();
        initLayout();
    }

    private void initToolbar()
    {
        View toolbar = findViewById(R.id.toolbar);

        DailyToolbarLayout dailyToolbarLayout = new DailyToolbarLayout(this, toolbar);
        dailyToolbarLayout.initToolbar(getString(R.string.actionbar_title_event_list_frag), new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }

    private void initLayout()
    {
        mEmptyView = findViewById(R.id.emptyLayout);

        mListView = (ListView) findViewById(R.id.listView);
        EdgeEffectColor.setEdgeGlowColor(mListView, getResources().getColor(R.color.default_over_scroll_edge));
        mListView.setOnItemClickListener(this);

        View homeButtonView = findViewById(R.id.homeButtonView);
        homeButtonView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setResult(Constants.CODE_RESULT_ACTIVITY_GO_HOME);
                finish();
            }
        });
    }

    @Override
    public void onStart()
    {
        AnalyticsManager.getInstance(this).recordScreen(this, Screen.EVENT_LIST, null);

        super.onStart();

        if (DailyDeepLink.getInstance().isValidateLink() == true)
        {
            if (DailyDeepLink.getInstance().isEventDetailView() == true)
            {
                startEventWeb(DailyDeepLink.getInstance().getUrl(), DailyDeepLink.getInstance().getTitle());
            }

            DailyDeepLink.getInstance().clear();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (mDontReload == true)
        {
            mDontReload = false;
        } else
        {
            lockUI();
            mEventListNetworkController.requestEventList();
        }
    }

    @Override
    public void finish()
    {
        super.finish();

        overridePendingTransition(R.anim.hold, R.anim.slide_out_right);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        if (isLockUiComponent() == true)
        {
            return;
        }

        lockUI();

        Event event = mEventListAdapter.getItem(position);

        startEventWeb(event.linkUrl, event.title);

        AnalyticsManager.getInstance(EventListActivity.this).recordEvent(AnalyticsManager.Category.NAVIGATION_//
            , AnalyticsManager.Action.EVENT_CLICKED, event.title, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        unLockUI();

        switch (requestCode)
        {
            case CODE_REQUEST_ACTIVITY_EVENTWEB:
                mDontReload = true;

                if (resultCode == Constants.CODE_RESULT_ACTIVITY_GO_HOME)
                {
                    setResult(Constants.CODE_RESULT_ACTIVITY_GO_HOME);
                    finish();
                }
                break;
        }
    }

    void startEventWeb(String url, String eventName)
    {
        if (Util.isTextEmpty(url) == true)
        {
            return;
        }

        Intent intent = EventWebActivity.newInstance(EventListActivity.this, EventWebActivity.SourceType.EVENT, url, eventName);
        startActivityForResult(intent, CODE_REQUEST_ACTIVITY_EVENTWEB);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // User Action Listener
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private EventListNetworkController.OnNetworkControllerListener mOnNetworkControllerListener = new EventListNetworkController.OnNetworkControllerListener()
    {
        @Override
        public void onEventListResponse(List<Event> eventList)
        {
            if (mEventListAdapter == null)
            {
                mEventListAdapter = new EventListAdapter(EventListActivity.this, 0, new ArrayList<Event>());
            } else
            {
                mEventListAdapter.clear();
            }

            if (eventList == null)
            {
                mListView.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
            } else
            {
                mListView.setVisibility(View.VISIBLE);
                mEmptyView.setVisibility(View.GONE);

                mEventListAdapter.addAll(eventList);
                mListView.setAdapter(mEventListAdapter);
                mEventListAdapter.notifyDataSetChanged();
            }

            unLockUI();
        }

        @Override
        public void onError(Throwable e)
        {
            unLockUI();
            EventListActivity.this.onError(e);
        }

        @Override
        public void onErrorPopupMessage(int msgCode, String message)
        {
            unLockUI();
            EventListActivity.this.onErrorPopupMessage(msgCode, message);
        }

        @Override
        public void onErrorToastMessage(String message)
        {
            unLockUI();
            EventListActivity.this.onErrorToastMessage(message);
        }

        @Override
        public void onErrorResponse(Call call, Response response)
        {
            unLockUI();
            EventListActivity.this.onErrorResponse(call, response);
        }
    };
}