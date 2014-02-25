package com.twoheart.dailyhotel;

import static com.twoheart.dailyhotel.AppConstants.PREFERENCE_IS_LOGIN;
import static com.twoheart.dailyhotel.AppConstants.PREFERENCE_SELECTED_MENU;
import static com.twoheart.dailyhotel.AppConstants.SHARED_PREFERENCES_NAME;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.twoheart.dailyhotel.booking.BookingListFragment;
import com.twoheart.dailyhotel.credit.CreditFragment;
import com.twoheart.dailyhotel.credit.NoLoginFragment;
import com.twoheart.dailyhotel.hotel.HotelListFragment;
import com.twoheart.dailyhotel.utils.CNetStatus;

public class ErrorFragment extends SherlockFragment implements OnClickListener{
	
	private View view;
	
	private Button btn_error;
	private SharedPreferences prefs;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		view = inflater.inflate(R.layout.fragment_error, null);
		
		// ActionBar Setting
		MainActivity activity = (MainActivity)view.getContext();
		activity.changeTitle("");
		activity.hideMenuItem();
		activity.addMenuItem("dummy");
		
		// sliding setting
//		activity.getSlidingMenu().setMode(SlidingMenu.LEFT);
		
		loadResource();
		
		return view;
	}
	
	public void loadResource() {
		btn_error = (Button) view.findViewById(R.id.btn_error);
		btn_error.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		if(v.getId() == btn_error.getId()) {
			
			
			// network ������ �ȵ�������
			if(!checkNetwork()) {
				Toast.makeText(view.getContext(), "��Ʈ��ũ ���¸� Ȯ���� �ּ���", Toast.LENGTH_SHORT).show();
				return;
			}

			prefs = view.getContext().getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
			String str = prefs.getString(PREFERENCE_SELECTED_MENU, null);
			if(str.equals("hotel")) {
				MainActivity activity = (MainActivity) view.getContext();
				activity.switchContent(new HotelListFragment());
			} else if(str.equals("booking")) {
				MainActivity activity = (MainActivity) view.getContext();
				activity.switchContent(new BookingListFragment());
			} else if(str.equals("credit")) {
				MainActivity activity = (MainActivity) view.getContext();
				if(checkLogin())	//�α��λ���
					activity.switchContent(new CreditFragment());
				else		// �α׾ƿ� ����
					activity.switchContent(new NoLoginFragment());
			}
			
		}
	}
	
	private boolean checkLogin() {
		prefs = getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
		return prefs.getBoolean(PREFERENCE_IS_LOGIN, false);
	}
	
	public Boolean checkNetwork() {
		boolean result = false;
		
	    CNetStatus net_status = CNetStatus.getInstance();  
        
	    switch (net_status.getNetType(view.getContext())) {
		    case CNetStatus.NET_TYPE_WIFI:  
		    	//WIFI �������
		    	result = true;
		        break;  
		    case CNetStatus.NET_TYPE_3G:  
		    	// 3G Ȥ�� LTE���� ����
		    	result = true;
		        break;  
		    case CNetStatus.NET_TYPE_NONE: 
		    	result = false;
		    	break;
	    }
	    return result;
	}

}
