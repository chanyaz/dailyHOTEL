package com.twoheart.dailyhotel.setting;

import static com.twoheart.dailyhotel.AppConstants.PREFERENCE_AUTO_LOGIN;
import static com.twoheart.dailyhotel.AppConstants.PREFERENCE_IS_LOGIN;
import static com.twoheart.dailyhotel.AppConstants.PREFERENCE_USER_ID;
import static com.twoheart.dailyhotel.AppConstants.PREFERENCE_USER_PWD;
import static com.twoheart.dailyhotel.AppConstants.SHARED_PREFERENCES_NAME;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.twoheart.dailyhotel.MainActivity;
import com.twoheart.dailyhotel.R;

public class SettingFragment extends Fragment implements OnClickListener{

	private View view;
	
	private TextView notice;
	private TextView help;
	private TextView mail;
	private TextView login;
	private TextView call;
	private TextView introduction;
	private TextView cur_version;
	private LinearLayout version;
	
	private boolean isLogin = false;	 // login ����
	
	private SharedPreferences prefs;
	
	@Override
	public void onResume() {
		super.onResume();
		checkLogin();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		view = inflater.inflate(R.layout.fragment_setting, null);
		
		// ActionBar Setting
		MainActivity activity = (MainActivity)view.getContext();
		activity.changeTitle("����");
		activity.hideMenuItem();
		activity.addMenuItem("dummy");
		
		// sliding setting
//		activity.getSlidingMenu().setMode(SlidingMenu.LEFT);
		
		
		loadResource();
		checkLogin();
		
		return view;
	}
	
	public void loadResource() {
		notice = (TextView) view.findViewById(R.id.tv_setting_notice);
		cur_version = (TextView) view.findViewById(R.id.tv_setting_version);
		version = (LinearLayout) view.findViewById(R.id.ll_setting_version);
		help = (TextView) view.findViewById(R.id.tv_setting_help);
		mail = (TextView) view.findViewById(R.id.tv_setting_mail);
		login = (TextView) view.findViewById(R.id.tv_setting_login);
		call = (TextView) view.findViewById(R.id.tv_setting_call);
		introduction = (TextView) view.findViewById(R.id.tv_setting_introduction);
		
		notice.setOnClickListener(this);
		version.setOnClickListener(this);
		help.setOnClickListener(this);
		mail.setOnClickListener(this);
		login.setOnClickListener(this);
		call.setOnClickListener(this);
		introduction.setOnClickListener(this);
		
		try {
			cur_version.setText(getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName);
		} catch(Exception e) {
			e.toString();
		}
	}
	
	public void checkLogin() {
		prefs = view.getContext().getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
		isLogin =  prefs.getBoolean(PREFERENCE_IS_LOGIN, false);
		
		if(isLogin)
			login.setText("�α׾ƿ�");
		else
			login.setText("�α���");
	}
	
	@Override
	public void onClick(View v) {
		
		if(v.getId() == notice.getId()) {
			
			Intent i = new Intent(v.getContext(), NoticeActivity.class);
			MainActivity activity = (MainActivity) view.getContext();
			activity.startActivity(i);
			activity.overridePendingTransition(R.anim.slide_in_right,R.anim.hold);
			
		}	else if(v.getId() == version.getId()) {
			
			Intent i = new Intent(v.getContext(), VersionActivity.class);
			MainActivity activity = (MainActivity) view.getContext();
			activity.startActivity(i);
			activity.overridePendingTransition(R.anim.slide_in_right,R.anim.hold);
			
		}	else if(v.getId() == help.getId()) {
			
			Intent i = new Intent(v.getContext(), HelpActivity.class);
			MainActivity activity = (MainActivity) view.getContext();
			activity.startActivity(i);
			activity.overridePendingTransition(R.anim.slide_in_right,R.anim.hold);
			
		}	else if(v.getId() == mail.getId()) {
			
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("message/rfc822");
			intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"help@dailyhotel.co.kr"});
			intent.putExtra(Intent.EXTRA_SUBJECT, "Subject of email");
			intent.putExtra(Intent.EXTRA_TEXT, "Body of email");
			startActivity(intent.createChooser(intent, "�۾��� ������ �� ������ ���ø����̼�"));
			
		}	else if(v.getId() == login.getId()) {
			
			if(isLogin) {		// �α��� �Ǿ� �ִ� ����
				AlertDialog.Builder alert_confirm = new AlertDialog.Builder(view.getContext());
				alert_confirm.setMessage("�α׾ƿ� �Ͻðڽ��ϱ�?").setCancelable(false).setPositiveButton("�α׾ƿ�",
				new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        // 'YES'
				    	prefs = getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
						SharedPreferences.Editor ed = prefs.edit();
						ed.putBoolean(PREFERENCE_IS_LOGIN, false);
						ed.putBoolean(PREFERENCE_AUTO_LOGIN, false);
						ed.putString(PREFERENCE_USER_ID, null);
						ed.putString(PREFERENCE_USER_PWD, null);
						ed.commit();
						Toast.makeText(getActivity(), "�α׾ƿ� �Ǿ����ϴ�", Toast.LENGTH_SHORT).show();
						login.setText("�α���");
						isLogin = false;
				    }
				}).setNegativeButton("���",
				new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				    return;
				    }
				});
				AlertDialog alert = alert_confirm.create();
				alert.show();
				
			} else {	// �α׾ƿ� ����
				Intent i = new Intent(v.getContext(), LoginActivity.class);
				MainActivity activity = (MainActivity) view.getContext();
				activity.startActivity(i);
				activity.overridePendingTransition(R.anim.slide_in_right,R.anim.hold);
			}
			
		}	else if(v.getId() == call.getId()) {
			Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:070-4028-9331"));
	    	startActivity(i);
		}	else if(v.getId() == introduction.getId())	{
			Intent i = new Intent(view.getContext(), IntroductionActivity.class);
			MainActivity activity = (MainActivity) view.getContext();
			activity.startActivity(i);
			activity.overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
		}
	}
}
