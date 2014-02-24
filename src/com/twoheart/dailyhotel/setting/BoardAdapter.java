package com.twoheart.dailyhotel.setting;

import java.util.ArrayList;

import com.twoheart.dailyhotel.R;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BoardAdapter extends BaseExpandableListAdapter{
	private ArrayList<BoardElement> list = null;
	private LayoutInflater inflater = null;
	private Boolean groupClickState[]; 
	private int position;
	
	
	public BoardAdapter(Context context, ArrayList<BoardElement> list) {
		super();
		this.inflater = LayoutInflater.from(context);
		this.list = list;
		groupClickState = new Boolean[list.size()] ;
		for(int i=0; i<groupClickState.length; i++) {
			groupClickState[i] = false;
		}
	}


	@Override
	public String getChild(int groupPosition, int childPosition) {
		return list.get(groupPosition).content;
	}


	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}


	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

		View v = convertView;
		
		if(v == null) {
			v = inflater.inflate(R.layout.list_row_board_child, null);
		}
		
		TextView tv_content = (TextView) v.findViewById(R.id.tv_board_content);
		tv_content.setText(getChild(groupPosition, 0));
		
		return v;
	}


	@Override
	public int getChildrenCount(int groupPosition) {
		return 1;
	}


	@Override
	public Object getGroup(int groupPosition) {
		return list.get(groupPosition).getSubject();
	}


	@Override
	public int getGroupCount() {
		return list.size();
	}


	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}


	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		View v = convertView;
		if ( v == null) {
			v = inflater.inflate(R.layout.list_row_board_group, parent, false);
		}
		
		TextView tv_subject = (TextView)v.findViewById(R.id.tv_board_subject);
		ImageView iv_arrow = (ImageView) v.findViewById(R.id.iv_board_arrow);
		
		tv_subject.setText(Html.fromHtml(getGroup(groupPosition) + " <font color='#FF7A7A'>" + list.get(groupPosition).regdate + "</font>"));
		if(groupClickState[groupPosition]) {
			iv_arrow.setImageResource(R.drawable.dh_arrow_open);
		} else {
			iv_arrow.setImageResource(R.drawable.dh_arrow_close);
		}
			
		return v;
	}


	@Override
	public boolean hasStableIds() {
		return true;
	}


	@Override
	public boolean isChildSelectable(int arg0, int arg1) {
		return true;
	}
	
	@Override
	public void onGroupExpanded(int groupPosition) {
		groupClickState[groupPosition] = true;
		super.onGroupExpanded(groupPosition);
	}
	
	@Override
	public void onGroupCollapsed(int groupPosition) {
		groupClickState[groupPosition] = false;
		super.onGroupCollapsed(groupPosition);
	}
	
	
	
}
