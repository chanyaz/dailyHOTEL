package com.twoheart.dailyhotel;

import static com.twoheart.dailyhotel.util.AppConstants.PREFERENCE_SELECTED_MENU;
import static com.twoheart.dailyhotel.util.AppConstants.SHARED_PREFERENCES_NAME;

import java.security.MessageDigest;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.twoheart.dailyhotel.fragment.HotelListFragment;
import com.twoheart.dailyhotel.util.AppConstants;
import com.twoheart.dailyhotel.util.ui.BaseActivity;
import com.twoheart.dailyhotel.util.ui.CloseOnBackPressed;

public class MainActivity extends BaseActivity {

	private static final String TAG = "MainActivity";
	
	private Fragment content;
	
	private boolean isMenuItem = false;
	private String itemStr = null;
	
	private TextView	 title;
	
	private SharedPreferences prefs;
	private CloseOnBackPressed backButtonHandler;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
		//actionbar setting
//		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
//		View customView = LayoutInflater.from(this).inflate(
//	            R.layout.actionbar_background, null);
//		ActionBar.LayoutParams params = new ActionBar.LayoutParams(
//	            ActionBar.LayoutParams.MATCH_PARENT,
//	            ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
//		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//		getSupportActionBar().setDisplayShowTitleEnabled(false);
//		getSupportActionBar().setDisplayShowHomeEnabled(true);
//		getSupportActionBar().setIcon(R.drawable.dh_ic_menu);
//		getSupportActionBar().setCustomView(R.drawable.dh_actionbar_background);
		
		
		// actionbar background color setting
		Drawable myDrawable;
		Resources res = getResources();
		try {
		   myDrawable = Drawable.createFromXml(res, res.getXml(R.drawable.dh_actionbar_background));
		   getSupportActionBar().setBackgroundDrawable(myDrawable);
		} catch (Exception ex) {
		   Log.e(TAG, "Exception loading drawable : " + ex.toString()); 
		}
		
		loadResource();
		
		// 맨 처음은 호텔리스트
		mDrawerList.setItemChecked(mMenuImages.indexOf(menuHotel), true);
		
		prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
		SharedPreferences.Editor ed = prefs.edit();
		ed.putString(PREFERENCE_SELECTED_MENU, "hotel");
		ed.commit();
		
		HotelListFragment hotelListFrag = new HotelListFragment();
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, hotelListFrag)
		.commit();
		
		if (AppConstants.DEBUG) {
			
			try {
				PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
				for (Signature signature : info.signatures) {
					MessageDigest md = MessageDigest.getInstance("SHA");
					md.update(signature.toByteArray());
					Log.d("KeyHash: getPackageName()" + getPackageName(), Base64.encodeToString(md.digest(), Base64.DEFAULT));
				} 
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		backButtonHandler = new CloseOnBackPressed(this);
		
	}
	
	public void loadResource() {
		title = (TextView) findViewById(R.id.tv_actionbar_title);
	}
	
	public void changeTitle(String str) {
		getSupportActionBar().setTitle(str);
	}
	
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	
    	if(isMenuItem) {
    		if(itemStr.equals("dummy")) {
    			menu.add(0,2,0,"").
	    		setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    		} else {
	    		menu.add(0,1,0,itemStr).
	    		setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    		}
    	}
    		return true;
    }
    
    public void addMenuItem(String str) {
	    	isMenuItem = true;
	    	itemStr = str;
	    	supportInvalidateOptionsMenu();
    }
    
    public void hideMenuItem() {
	    	isMenuItem = false;
	    	supportInvalidateOptionsMenu();
    }
    
    @Override
    public void finish() {
	    	if (backButtonHandler.onBackPressed())
				super.finish();
    	
    }
 	
}
