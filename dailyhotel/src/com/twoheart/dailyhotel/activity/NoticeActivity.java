package com.twoheart.dailyhotel.activity;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ExpandableListView.OnGroupExpandListener;

import com.android.volley.Request.Method;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.twoheart.dailyhotel.DailyHotel;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.adapter.BoardListAdapter;
import com.twoheart.dailyhotel.model.Board;
import com.twoheart.dailyhotel.util.RenewalGaManager;
import com.twoheart.dailyhotel.util.network.request.DailyHotelJsonRequest;
import com.twoheart.dailyhotel.util.network.response.DailyHotelJsonResponseListener;
import com.twoheart.dailyhotel.util.ui.BaseActivity;

public class NoticeActivity extends BaseActivity implements
		DailyHotelJsonResponseListener {

	private static final String TAG = "NoticeActivity";

	private ArrayList<Board> mList;
	private ExpandableListView mListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBar(R.string.actionbar_title_notice_activity);
		setContentView(R.layout.activity_board);
		DailyHotel.getGaTracker().set(Fields.SCREEN_NAME, TAG);

		mListView = (ExpandableListView) findViewById(R.id.expandable_list_board);
		mListView.setOnGroupExpandListener(new OnGroupExpandListener() {
			private int mExpandedChildPos = -1;
			@Override
			public void onGroupExpand(int groupPosition) {
				if(mExpandedChildPos != -1 && groupPosition != mExpandedChildPos) {
					mListView.collapseGroup(mExpandedChildPos);
				}
				mExpandedChildPos = groupPosition;
				mListView.setSelectionFromTop(mExpandedChildPos, 0); // 클릭한 그룹뷰를 탑으로 두기 위하여 이동.
				RenewalGaManager.getInstance(getApplicationContext()).recordEvent("click", "selectNotice", mList.get(groupPosition).getSubject(), (long) (groupPosition+1));
			}
		});
		mListView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch(scrollState) {
				case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
					RenewalGaManager.getInstance(getApplicationContext()).recordEvent("scroll", "articles", "공지사항", null);
					break;
				}
				
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		lockUI();
		mQueue.add(new DailyHotelJsonRequest(Method.GET, new StringBuilder(
				URL_DAILYHOTEL_SERVER).append(URL_WEBAPI_BOARD_NOTICE)
				.toString(), null, this, this));
		
		RenewalGaManager.getInstance(getApplicationContext()).recordScreen("noticeList", "/settings/notice");
		RenewalGaManager.getInstance(getApplicationContext()).recordEvent("visit", "noticeList", null, null);
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		DailyHotel.getGaTracker().send(MapBuilder.createAppView().build());
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.slide_out_left, R.anim.slide_out_right);
	}

	@Override
	public void onResponse(String url, JSONObject response) {
		if (url.contains(URL_WEBAPI_BOARD_NOTICE)) {
			mList = new ArrayList<Board>();

			try {
				JSONObject jsonObj = response;
				JSONArray json = jsonObj.getJSONArray("articles");

				for (int i = 0; i < json.length(); i++) {

					JSONObject obj = json.getJSONObject(i);
					String subject = obj.getString("subject");
					String content = obj.getString("content");
					String regdate = obj.getString("regdate");

					mList.add(new Board(subject, content, regdate));
				}
				
				mListView.setAdapter(new BoardListAdapter(this, mList));
			} catch (Exception e) {
				onError(e);
			} finally {
				unLockUI();
			}
		}
	}
}
