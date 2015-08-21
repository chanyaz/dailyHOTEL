package com.twoheart.dailyhotel.activity;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.android.volley.Request.Method;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.adapter.BoardListAdapter;
import com.twoheart.dailyhotel.model.Board;
import com.twoheart.dailyhotel.util.AnalyticsManager;
import com.twoheart.dailyhotel.util.AnalyticsManager.Action;
import com.twoheart.dailyhotel.util.AnalyticsManager.Screen;
import com.twoheart.dailyhotel.util.network.request.DailyHotelJsonRequest;
import com.twoheart.dailyhotel.util.network.response.DailyHotelJsonResponseListener;
import com.twoheart.dailyhotel.util.ui.BaseActivity;

import android.os.Bundle;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;

public class NoticeActivity extends BaseActivity
{

	private ArrayList<Board> mList;
	private ExpandableListView mListView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_board);
		setActionBar(R.string.actionbar_title_notice_activity);

		mListView = (ExpandableListView) findViewById(R.id.expandable_list_board);
		mListView.setOnGroupExpandListener(new OnGroupExpandListener()
		{
			private int mExpandedChildPos = -1;

			@Override
			public void onGroupExpand(int groupPosition)
			{
				if (mExpandedChildPos != -1 && groupPosition != mExpandedChildPos)
				{
					mListView.collapseGroup(mExpandedChildPos);
				}
				mExpandedChildPos = groupPosition;
				mListView.setSelectionFromTop(mExpandedChildPos, 0);

				AnalyticsManager.getInstance(NoticeActivity.this).recordEvent(Screen.NOTICE, Action.CLICK, mList.get(groupPosition).getSubject(), (long) (groupPosition + 1));
			}
		});
	}

	@Override
	protected void onStart()
	{
		AnalyticsManager.getInstance(NoticeActivity.this).recordScreen(Screen.NOTICE);
		super.onStart();
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		lockUI();
		mQueue.add(new DailyHotelJsonRequest(Method.GET, new StringBuilder(URL_DAILYHOTEL_SERVER).append(URL_WEBAPI_BOARD_NOTICE).toString(), null, mBoardNoticeResponseListener, this));
	}

	@Override
	public void finish()
	{
		super.finish();
		overridePendingTransition(R.anim.slide_out_left, R.anim.slide_out_right);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Listener
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private DailyHotelJsonResponseListener mBoardNoticeResponseListener = new DailyHotelJsonResponseListener()
	{

		@Override
		public void onResponse(String url, JSONObject response)
		{

			mList = new ArrayList<Board>();

			try
			{
				if (response == null)
				{
					throw new NullPointerException("response == null");
				}

				JSONArray json = response.getJSONArray("articles");

				int length = json.length();
				for (int i = 0; i < length; i++)
				{

					JSONObject obj = json.getJSONObject(i);
					String subject = obj.getString("subject");
					String content = obj.getString("content");
					String regdate = obj.getString("regdate");

					mList.add(new Board(subject, content, regdate));
				}

				mListView.setAdapter(new BoardListAdapter(NoticeActivity.this, mList));
			} catch (Exception e)
			{
				onError(e);
			} finally
			{
				unLockUI();
			}
		}
	};

	//	@Override
	//	public void onResponse(String url, JSONObject response) {
	//		if (url.contains(URL_WEBAPI_BOARD_NOTICE)) {
	//			mList = new ArrayList<Board>();
	//
	//			try {
	//				JSONObject jsonObj = response;
	//				JSONArray json = jsonObj.getJSONArray("articles");
	//
	//				for (int i = 0; i < json.length(); i++) {
	//
	//					JSONObject obj = json.getJSONObject(i);
	//					String subject = obj.getString("subject");
	//					String content = obj.getString("content");
	//					String regdate = obj.getString("regdate");
	//
	//					mList.add(new Board(subject, content, regdate));
	//				}
	//				
	//				mListView.setAdapter(new BoardListAdapter(this, mList));
	//			} catch (Exception e) {
	//				onError(e);
	//			} finally {
	//				unLockUI();
	//			}
	//		}
	//	}
}
