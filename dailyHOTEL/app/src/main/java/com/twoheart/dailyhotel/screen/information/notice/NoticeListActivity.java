package com.twoheart.dailyhotel.screen.information.notice;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Notice;
import com.twoheart.dailyhotel.network.DailyMobileAPI;
import com.twoheart.dailyhotel.place.base.BaseActivity;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.DailyDeepLink;
import com.twoheart.dailyhotel.util.DailyPreference;
import com.twoheart.dailyhotel.util.EdgeEffectColor;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;
import com.twoheart.dailyhotel.widget.DailyToolbarLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;

public class NoticeListActivity extends BaseActivity implements AdapterView.OnItemClickListener
{
    View mEmptyView, mListLayout;
    ListView mListView;
    NoticeListAdapter mNoticeListAdapter;

    private boolean mDontReload;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        overridePendingTransition(R.anim.slide_in_right, R.anim.hold);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_notice);

        DailyPreference.getInstance(this).setViewedNoticeTime(DailyPreference.getInstance(this).getLastestNoticeTime());

        initToolbar();
        initLayout();
    }

    private void initToolbar()
    {
        View toolbar = findViewById(R.id.toolbar);

        DailyToolbarLayout dailyToolbarLayout = new DailyToolbarLayout(this, toolbar);
        dailyToolbarLayout.initToolbar(getString(R.string.actionbar_title_notice_activity), new View.OnClickListener()
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
        mListLayout = findViewById(R.id.listLayout);
        mEmptyView = findViewById(R.id.emptyLayout);
        mListView = (ListView) findViewById(R.id.listView);
        mListView.setOnItemClickListener(this);

        EdgeEffectColor.setEdgeGlowColor(mListView, getResources().getColor(R.color.default_over_scroll_edge));

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
        super.onStart();

        if (DailyDeepLink.getInstance().isValidateLink() == true)
        {
            if (DailyDeepLink.getInstance().isNoticeDetailView() == true)
            {
                int index = DailyDeepLink.getInstance().getNoticeIndex();

                if (index > 0)
                {
                    Util.removeNoticeNewList(this, index);
                }

                String title = DailyDeepLink.getInstance().getTitle();
                String url = DailyDeepLink.getInstance().getUrl();

                Intent intent = NoticeWebActivity.newInstance(this, title, url);
                startActivityForResult(intent, CODE_REQUEST_ACTIVITY_NOTICEWEB);
            }

            DailyDeepLink.getInstance().clear();
        }

        AnalyticsManager.getInstance(this).recordScreen(this, AnalyticsManager.Screen.MENU_NOTICELIST, null);
    }


    @Override
    protected void onResume()
    {
        super.onResume();

        if (mDontReload == true && mNoticeListAdapter != null)
        {
            mDontReload = false;
            mNoticeListAdapter.notifyDataSetChanged();
        } else
        {
            lockUI();

            DailyMobileAPI.getInstance(this).requestNoticeList(mNetworkTag, mNoticeListCallback);
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
        if (lockUiComponentAndIsLockUiComponent() == true)
        {
            return;
        }

        Notice notice = mNoticeListAdapter.getItem(position);
        notice.isNew = false;

        Util.removeNoticeNewList(this, notice.index);

        Intent intent = NoticeWebActivity.newInstance(this, notice.title, notice.linkUrl);
        startActivityForResult(intent, CODE_REQUEST_ACTIVITY_NOTICEWEB);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        unLockUI();

        mDontReload = true;

        switch (requestCode)
        {
            case CODE_REQUEST_ACTIVITY_NOTICEWEB:
                if (resultCode == Constants.CODE_RESULT_ACTIVITY_GO_HOME)
                {
                    setResult(Constants.CODE_RESULT_ACTIVITY_GO_HOME);
                    finish();
                }
                break;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Listener
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private retrofit2.Callback mNoticeListCallback = new retrofit2.Callback<JSONObject>()
    {
        @Override
        public void onResponse(Call<JSONObject> call, Response<JSONObject> response)
        {
            if (response != null && response.isSuccessful() && response.body() != null)
            {
                try
                {
                    JSONObject responseJSONObject = response.body();

                    int msgCode = responseJSONObject.getInt("msgCode");

                    if (msgCode == 100)
                    {
                        JSONObject dataJSONObject = responseJSONObject.getJSONObject("data");
                        JSONArray jsonArray = dataJSONObject.getJSONArray("notices");

                        int length = jsonArray.length();

                        if (length > 0)
                        {
                            ArrayList<Notice> noticeList = new ArrayList<>(length);

                            if (mNoticeListAdapter == null)
                            {
                                mNoticeListAdapter = new NoticeListAdapter(NoticeListActivity.this, 0, new ArrayList<Notice>());
                            } else
                            {
                                mNoticeListAdapter.clear();
                            }

                            mListLayout.setVisibility(View.VISIBLE);
                            mEmptyView.setVisibility(View.GONE);

                            Notice notice;

                            for (int i = 0; i < length; i++)
                            {
                                notice = new Notice(jsonArray.getJSONObject(i));
                                noticeList.add(notice);
                            }

                            noticeList = Util.chekckNoticeNewList(NoticeListActivity.this, noticeList);

                            mNoticeListAdapter.addAll(noticeList);
                            mListView.setAdapter(mNoticeListAdapter);
                        } else
                        {
                            mListLayout.setVisibility(View.GONE);
                            mEmptyView.setVisibility(View.VISIBLE);
                        }
                    } else
                    {
                        String msg = responseJSONObject.getString("msg");
                        onErrorPopupMessage(msgCode, msg);
                    }
                } catch (Exception e)
                {
                    onError(e);
                } finally
                {
                    unLockUI();
                }
            } else
            {
                NoticeListActivity.this.onErrorResponse(call, response);
            }
        }

        @Override
        public void onFailure(Call<JSONObject> call, Throwable t)
        {
            NoticeListActivity.this.onError(t);
        }
    };
}
