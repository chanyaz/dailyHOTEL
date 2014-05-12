/**
 * Copyright (c) 2014 Daily Co., Ltd. All rights reserved.
 *
 * MainActivity (����ȭ��)
 * 
 * ���ø����̼��� �� ȭ�����μ� ���� ���� �� �������� ȭ���̴�. �� ȭ���� ���ø����̼�
 * ���� ���� �� SplashActivity�� ���� ����, ��κ��� ���ø����̼� �ʱ�ȭ �۾��� 
 * SplashActivity���� �ѱ��. �׷���, �Ϻ� �ʱ�ȭ �۾��� �����ϸ�, �α��� ���ǰ�����
 * �׺���̼� �޴��� ǥ���ϴ� ���� �ϴ� ȭ���̴�.
 *
 * @since 2014-02-24
 * @version 1
 * @author Mike Han(mike@dailyhotel.co.kr)
 */
package com.twoheart.dailyhotel;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.androidquery.util.AQUtility;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.readystatesoftware.systembartint.SystemBarTintManager.SystemBarConfig;
import com.twoheart.dailyhotel.activity.EventWebActivity;
import com.twoheart.dailyhotel.activity.SplashActivity;
import com.twoheart.dailyhotel.fragment.RatingHotelFragment;
import com.twoheart.dailyhotel.model.Hotel;
import com.twoheart.dailyhotel.model.HotelDetail;
import com.twoheart.dailyhotel.model.SaleTime;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.network.VolleyHttpClient;
import com.twoheart.dailyhotel.util.network.request.DailyHotelJsonRequest;
import com.twoheart.dailyhotel.util.ui.BaseActivity;
import com.twoheart.dailyhotel.util.ui.CloseOnBackPressed;

public class MainActivity extends BaseActivity implements OnItemClickListener,
		Constants {

	private static final String TAG = "MainActivity";

	private static final String DRAWER_MENU_SECTION_RESERVATION = "����";
	private static final String DRAWER_MENU_ENTRY_HOTEL = "������ ȣ��";
	private static final String DRAWER_MENU_ENTRY_BOOKING = "����Ȯ��";
	private static final String DRAWER_MENU_SECTION_ACCOUNT = "����";
	private static final String DRAWER_MENU_ENTRY_CREDIT = "������";
	private static final String DRAWER_MENU_ENTRY_SETTING = "����";

	public static final int INDEX_HOTEL_LIST_FRAGMENT = 0;
	public static final int INDEX_BOOKING_LIST_FRAGMENT = 1;
	public static final int INDEX_CREDIT_FRAGMENT = 2;
	public static final int INDEX_SETTING_FRAGMENT = 3;

	public static final String KEY_HOTEL_LIST_FRAGMENT = "hotel_list";
	public static final String KEY_BOOKING_LIST_FRAGMENT = "booking_list";
	public static final String KEY_CREDIT_FRAGMENT = "credit";
	public static final String KEY_SEETING_FRAGMENT = "setting";

	private static final String TAG_FRAGMENT_RATING_HOTEL = "rating_hotel";

	public ListView drawerList;
	public DrawerLayout drawerLayout;
	public RelativeLayout leftDrawer;
	private FrameLayout mContentFrame;
	private LinearLayout btnEvent;
	
	public ActionBarDrawerToggle drawerToggle;
	protected FragmentManager fragmentManager;
	protected List<DrawerMenu> mMenuImages;
	protected List<Fragment> mFragments;
	private DrawerMenuListAdapter mDrawerMenuListAdapter;
	
	// ���������� �ӹ����� Fragment�� index
	public int indexLastFragment;	// Error Fragment���� �ٽ� ���ƿ� �� �ʿ�.
	
	// SystemBarTintManager
	private SystemBarTintManager tintManager;
	public SystemBarConfig config;
	
	// DrawerMenu ��ü��
	public DrawerMenu menuHotelListFragment;
	public DrawerMenu menuBookingListFragment;
	public DrawerMenu menuCreditFragment;
	public DrawerMenu menuSettingFragment;

	// Back ��ư�� �� �� ���� �ڵ鷯 ��� ����
	private CloseOnBackPressed backButtonHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ��Ű ����ȭ�� �ʱ�ȭ�Ѵ�. �α���, �α׾ƿ� ���� ��Ű�� MainActivity�� �����ֱ�� ����ȭ�Ѵ�.
		CookieSyncManager.createInstance(getApplicationContext());

		// ������ ������ ���ῡ ���� ����� ��Ű���� ���� �� �����Ƿ�, SplashActivity���� �ڵ� �α�����
		// ó���ϱ� ������ �̸� �̹� ����Ǿ� �ִ� ��Ű���� �����Ѵ�.
		if (CookieManager.getInstance().getCookie(URL_DAILYHOTEL_SERVER) != null)
			VolleyHttpClient.destroyCookie();

		// ���÷��� ȭ���� ����
		startActivityForResult(new Intent(this, SplashActivity.class), CODE_REQUEST_ACTIVITY_SPLASH);

		// Anroid 4.4 �̻󿡼� Android StatusBar�� Android NavigationBar�� Translucent�ϰ� ���ִ� API�� ����ϵ��� �Ѵ�.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			setTheme(R.style.AppTheme_Translucent);

			// SystemBarTintManager�� 3rd Party ���̺귯���� StatusBar�� NavigationBar�� ���õ� API�� ���� ������ �� �ֵ��� ���ش�.
			tintManager = new SystemBarTintManager(this);
			config = tintManager.getConfig();

			tintManager.setStatusBarTintEnabled(true);
			int actionBarColor = getResources().getColor(android.R.color.white);
			tintManager.setStatusBarTintColor(actionBarColor);

		} else {
			setTheme(R.style.AppTheme);
		}

		setContentView(R.layout.activity_main);
		setNavigationDrawer();

		mContentFrame = (FrameLayout) findViewById(R.id.content_frame);

		// Android 4.4 �̻󿡼� Android StatusBar�� Android NavigationBar�� Translucent�ϰ� 
		// �� ��� ���� ����� �ʿ��� ���̽��� �߻��ϹǷ� �ش� ���̽��� ���� ���� ó���Ѵ�.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			mContentFrame.setPadding(mContentFrame.getPaddingLeft(),
					config.getStatusBarHeight() + config.getActionBarHeight(),
					mContentFrame.getPaddingRight(),
					mContentFrame.getPaddingBottom());

			drawerList
					.setPadding(
							drawerList.getPaddingLeft(),
							config.getStatusBarHeight()
									+ config.getActionBarHeight(),
							drawerList.getPaddingRight(),
							drawerList.getPaddingBottom());

		}

		fragmentManager = getSupportFragmentManager();
		backButtonHandler = new CloseOnBackPressed(this);

		// �� ó���� ȣ�ڸ���Ʈ
		selectMenuDrawer(menuHotelListFragment);

		// Facebook SDK�� �����ϱ� ���� ��Ű�� Hash �� ǥ��
		if (DEBUG) {
			printPackageHashKey();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == CODE_REQUEST_ACTIVITY_SPLASH) {
			switch (resultCode) {
			case RESULT_OK :		// ���÷��� ȭ���� ���������� ����Ǿ��� ���
				break;
			case CODE_RESULT_ACTIVITY_SPLASH_NEW_EVENT :		// ���÷��ð� ���������� ����Ǿ��µ� ���ο� �̺�Ʈ �˸��� �ִ� ���
				ImageView ivNewEvent = (ImageView) findViewById(R.id.iv_new_event);
				ivNewEvent.setVisibility(View.VISIBLE);
				break;
			default :		// ���÷��ð� ������������ ����Ǿ��� ���
				super.finish();		// ���ø����̼�(���� ȭ��)�� �����ع�����
				return;				// �޼��带 ���������� - ȣ�� �򰡸� �������� ����.
			}
			
			boolean showGuide = sharedPreference.getBoolean(KEY_PREFERENCE_SHOW_GUIDE, true);
			if (showGuide) {
				startActivity(new Intent(this, IntroActivity.class));
			}
			
			// ȣ����
			try {
				String purchasedHotelName = sharedPreference.getString(
						KEY_PREFERENCE_HOTEL_NAME,
						VALUE_PREFERENCE_HOTEL_NAME_DEFAULT);
				int purchasedHotelSaleIdx = sharedPreference.getInt(
						KEY_PREFERENCE_HOTEL_SALE_IDX,
						VALUE_PREFERENCE_HOTEL_SALE_IDX_DEFAULT);
				String purchasedHotelCheckOut = sharedPreference.getString(
						KEY_PREFERENCE_HOTEL_CHECKOUT,
						VALUE_PREFERENCE_HOTEL_CHECKOUT_DEFAULT);

				Date today = new Date();
				Date checkOut = SaleTime.stringToDate(Util
						.dailyHotelTimeConvert(purchasedHotelCheckOut));

				if (!purchasedHotelName.equals(VALUE_PREFERENCE_HOTEL_NAME_DEFAULT)) {
					if (today.compareTo(checkOut) >= 0) {
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(checkOut);
						calendar.add(Calendar.DATE, DAYS_DISPLAY_RATING_HOTEL_DIALOG);
						Date deadLineDay = calendar.getTime();
						
						if (today.compareTo(deadLineDay) < 0) {
							Hotel purchasedHotel = new Hotel();
							purchasedHotel.setName(purchasedHotelName);
		
							HotelDetail purchasedHotelInformation = new HotelDetail();
							purchasedHotelInformation.setHotel(purchasedHotel);
							purchasedHotelInformation.setSaleIdx(purchasedHotelSaleIdx);
		
							RatingHotelFragment dialog = RatingHotelFragment
									.newInstance(purchasedHotelInformation);
							dialog.show(fragmentManager, TAG_FRAGMENT_RATING_HOTEL);
						} else {
							RatingHotelFragment dialog = RatingHotelFragment
									.newInstance(null);
							dialog.destroyRatingHotelFlag();
						}
					}
				}
			} catch (Exception e) {
				onError(e);
			}
		}
	}

	/**
	 * �׺���̼� ��ο����� �޴��� �����ϴ� ȿ���� ���ִ� �޼���
	 * @param selectedMenu DrawerMenu ��ü�� �޴´�.
	 */
	public void selectMenuDrawer(DrawerMenu selectedMenu) {
		drawerList.performItemClick(
				drawerList.getAdapter().getView(
						mMenuImages.indexOf(selectedMenu), null, null),
				mMenuImages.indexOf(selectedMenu), mDrawerMenuListAdapter
						.getItemId(mMenuImages.indexOf(selectedMenu)));
	}

	private void initializeFragments() {
		if (mFragments != null)
			mFragments.clear();
		else
			mFragments = new LinkedList<Fragment>();

		mFragments.add(new HotelListFragment());
		mFragments.add(new BookingListFragment());
		mFragments.add(new CreditFragment());
		mFragments.add(new SettingFragment());

	}

	/**
	 * �׺���̼� ��ο� �޴����� ������ �� �ִ� Fragment�� ��ȯ�ϴ� �޼����̴�.
	 * @param index Fragment ����Ʈ�� �ش��ϴ� index�� �޴´�.
	 * @return ��û�� index�� �ش��ϴ� Fragment�� ��ȯ�Ѵ�.
	 */
	public Fragment getFragment(int index) {
		Fragment newFragment = null;

		try {
			newFragment = mFragments.get(index);

		} catch (Exception e) {
			// onError(e);

			initializeFragments();
			newFragment = getFragment(index);

		}

		return newFragment;

	}

	/**
	 * Fragment �����̳ʿ��� �ش� Fragment�� �����Ͽ� ǥ���Ѵ�.
	 * @param fragment Fragment ����Ʈ�� ������ Fragement���� �޴� ���� ����.
	 */
	public void replaceFragment(Fragment fragment) {
		try {
			clearFragmentBackStack();

			fragmentManager.beginTransaction()
					.replace(mContentFrame.getId(), fragment)
					.commitAllowingStateLoss();

			// Android 4.4 �̻��� ��� Android StatusBar�� Android NavigationBar�� ��� Translucent�ϴµ�
			// �츮 ���ø����̼ǿ����� HotelListFragment������ Android NavigationBar�� Translucent�ϰ� �Ͽ���.
			// �׷��� �ٸ� Fragment�鿡���� �׺���̼� ��ο��� �����ϴ� ������ �־ ���̰� �߻��ϰ� �Ǵµ� �ش� �̽���
			// �ذ��ϱ� ���� �κ��� �� �κ��̴�.
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				if (fragment instanceof HotelListFragment) {
					mContentFrame.setPadding(mContentFrame.getPaddingLeft(),
							mContentFrame.getPaddingTop(),
							mContentFrame.getPaddingRight(), 0);

					Window w = getWindow();
					w.setFlags(
							WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
							WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
					
					RelativeLayout.LayoutParams btnEventLayoutParams = (RelativeLayout.LayoutParams) btnEvent.getLayoutParams();
					btnEventLayoutParams.bottomMargin = config.getNavigationBarHeight();
					btnEvent.setLayoutParams(btnEventLayoutParams);


				} else {
					WindowManager.LayoutParams attrs = getWindow()
							.getAttributes();
					attrs.flags &= (~WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
					getWindow().setAttributes(attrs);
					
					RelativeLayout.LayoutParams btnEventLayoutParams = (RelativeLayout.LayoutParams) btnEvent.getLayoutParams();
					btnEventLayoutParams.bottomMargin = 0;
					btnEvent.setLayoutParams(btnEventLayoutParams);

				}
			}
		} catch (IllegalStateException e) {
			onError(e);

		}

	}

	/**
	 * Fragment �����̳ʿ��� �ش� Fragement�� �׾ƿø���.
	 * @param fragment Fragment ����Ʈ�� ������ Fragment���� �޴� ���� ����.
	 */
	public void addFragment(Fragment fragment) {
		fragmentManager
				.beginTransaction()
				.setCustomAnimations(R.anim.slide_in_right,
						R.anim.slide_out_right, R.anim.slide_in_right,
						R.anim.slide_out_right)
				.add(R.id.content_frame, fragment).addToBackStack(null)
				.commit();

	}

	/**
	 * Fragment �����̳��� ǥ�õǴ� Fragment�� ������ �� Fragment �����̳ʿ� ����� Fragment���� �����Ѵ�.
	 */
	private void clearFragmentBackStack() {
		for (int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i)
			fragmentManager.popBackStack();

	}

	@Deprecated
	public void removeFragment(Fragment fragment) {
		fragmentManager.beginTransaction().remove(fragment)
				.commitAllowingStateLoss();
	}

	public void printPackageHashKey() {
		try {
			PackageInfo info = getPackageManager().getPackageInfo(
					getPackageName(), PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				Log.e("KeyHash: getPackageName()" + getPackageName(),
						Base64.encodeToString(md.digest(), Base64.DEFAULT));
			}
		} catch (Exception e) {
			onError(e);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view,
			int position, long id) {

		int selectedMenuIconId = ((DrawerMenu) (adapterView.getAdapter()
				.getItem(position))).getIcon();

		switch (selectedMenuIconId) {
		case R.drawable.selector_drawermenu_todayshotel:
			indexLastFragment = INDEX_HOTEL_LIST_FRAGMENT;
			break;

		case R.drawable.selector_drawermenu_reservation:
			indexLastFragment = INDEX_BOOKING_LIST_FRAGMENT;
			break;

		case R.drawable.selector_drawermenu_saving:
			indexLastFragment = INDEX_CREDIT_FRAGMENT;
			break;

		case R.drawable.selector_drawermenu_setting:
			indexLastFragment = INDEX_SETTING_FRAGMENT;
			break;
		}

		replaceFragment(getFragment(indexLastFragment));
		drawerLayout.closeDrawer(leftDrawer);

	}

	/**
	 * �׺���̼� ��ο��� �����ϴ� �޼���
	 */
	public void setNavigationDrawer() {
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		leftDrawer = (RelativeLayout) findViewById(R.id.drawer);
		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
				R.drawable.ic_drawer, 0, 0) {

			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				supportInvalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				supportInvalidateOptionsMenu();
			}
		};

		drawerLayout.setDrawerListener(drawerToggle);
		drawerList = (ListView) findViewById(R.id.drawer_list);

		menuHotelListFragment = new DrawerMenu(DRAWER_MENU_ENTRY_HOTEL,
				R.drawable.selector_drawermenu_todayshotel,
				DrawerMenu.DRAWER_MENU_LIST_TYPE_ENTRY);
		menuBookingListFragment = new DrawerMenu(DRAWER_MENU_ENTRY_BOOKING,
				R.drawable.selector_drawermenu_reservation,
				DrawerMenu.DRAWER_MENU_LIST_TYPE_ENTRY);
		menuCreditFragment = new DrawerMenu(DRAWER_MENU_ENTRY_CREDIT,
				R.drawable.selector_drawermenu_saving,
				DrawerMenu.DRAWER_MENU_LIST_TYPE_ENTRY);
		menuSettingFragment = new DrawerMenu(DRAWER_MENU_ENTRY_SETTING,
				R.drawable.selector_drawermenu_setting,
				DrawerMenu.DRAWER_MENU_LIST_TYPE_ENTRY);

		mMenuImages = new ArrayList<DrawerMenu>();
		mMenuImages.add(new DrawerMenu(DrawerMenu.DRAWER_MENU_LIST_TYPE_LOGO));
		mMenuImages.add(new DrawerMenu(DRAWER_MENU_SECTION_RESERVATION,
				DrawerMenu.DRAWER_MENU_LIST_TYPE_SECTION));
		mMenuImages.add(menuHotelListFragment);
		mMenuImages.add(menuBookingListFragment);
		mMenuImages.add(new DrawerMenu(DRAWER_MENU_SECTION_ACCOUNT,
				DrawerMenu.DRAWER_MENU_LIST_TYPE_SECTION));
		mMenuImages.add(menuCreditFragment);
		mMenuImages.add(menuSettingFragment);
		
		btnEvent = (LinearLayout) findViewById(R.id.btn_footer);
		TextView tvParticipateInEvent = (TextView) findViewById(R.id.tv_participate_event);
		tvParticipateInEvent.setTypeface(DailyHotel.getBoldTypeface());
		btnEvent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				drawerLayout.closeDrawer(leftDrawer);
				Intent i = new Intent(MainActivity.this, EventWebActivity.class);
				startActivity(i);
				overridePendingTransition(R.anim.slide_in_bottom,
						R.anim.hold);
			}
		});
		

		mDrawerMenuListAdapter = new DrawerMenuListAdapter(this,
				R.layout.list_row_drawer_entry, mMenuImages);

		drawerList.setAdapter(mDrawerMenuListAdapter);
		drawerList.setOnItemClickListener(this);
		
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		if (drawerToggle != null)
			drawerToggle.syncState();

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if (drawerToggle != null)
			drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			toggleDrawer();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public void toggleDrawer() {
		if (!drawerLayout.isDrawerOpen(leftDrawer)) {
			drawerLayout.openDrawer(leftDrawer);
		} else {
			drawerLayout.closeDrawer(leftDrawer);
		}
	}

	@Override
	public void finish() {
		if (backButtonHandler.onBackPressed())
			super.finish();

	}

	private class DrawerMenu {

		public static final int DRAWER_MENU_LIST_TYPE_LOGO = 0;
		public static final int DRAWER_MENU_LIST_TYPE_SECTION = 1;
		public static final int DRAWER_MENU_LIST_TYPE_ENTRY = 2;

		private String title;
		private int icon;
		private int type;

		public DrawerMenu(int type) {
			super();
			this.type = type;
		}

		public DrawerMenu(String title, int type) {
			super();
			this.title = title;
			this.type = type;
		}

		public DrawerMenu(String title, int icon, int type) {
			super();
			this.title = title;
			this.icon = icon;
			this.type = type;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public int getIcon() {
			return icon;
		}

		public void setIcon(int icon) {
			this.icon = icon;
		}

	}

	private class DrawerMenuListAdapter extends BaseAdapter {

		private List<DrawerMenu> list;
		private LayoutInflater inflater;
		private Context context;
		private int layout;

		public DrawerMenuListAdapter(Context context, int layout,
				List<DrawerMenu> list) {
			this.context = context;
			this.layout = layout;
			this.inflater = (LayoutInflater) this.context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.list = list;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public boolean isEnabled(int position) {
			return (list.get(position).getType() == DrawerMenu.DRAWER_MENU_LIST_TYPE_ENTRY) ? true
					: false;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			DrawerMenu item = list.get(position);

			switch (item.getType()) {
			case DrawerMenu.DRAWER_MENU_LIST_TYPE_LOGO:
				convertView = inflater.inflate(R.layout.list_row_drawer_logo,
						null);
				break;

			case DrawerMenu.DRAWER_MENU_LIST_TYPE_SECTION:
				convertView = inflater.inflate(
						R.layout.list_row_drawer_section, null);

				TextView drawerMenuItemTitle = (TextView) convertView
						.findViewById(R.id.drawerMenuItemTitle);

				drawerMenuItemTitle.setText(item.getTitle());
				break;

			case DrawerMenu.DRAWER_MENU_LIST_TYPE_ENTRY:
				convertView = inflater.inflate(R.layout.list_row_drawer_entry,
						null);

				ImageView drawerMenuItemIcon = (ImageView) convertView
						.findViewById(R.id.drawerMenuItemIcon);
				TextView drawerMenuItemText = (TextView) convertView
						.findViewById(R.id.drawerMenuItemTitle);

				drawerMenuItemIcon.setImageResource(item.getIcon());
				drawerMenuItemText.setText(item.getTitle());

				break;
			}

			return convertView;
		}
	}

	@Override
	protected void onDestroy() {

		// ��Ű ���Ḧ ���� ������ �α׾ƿ� ������Ʈ
		mQueue.add(new DailyHotelJsonRequest(Method.GET, new StringBuilder(
				URL_DAILYHOTEL_SERVER).append(URL_WEBAPI_USER_LOGOUT)
				.toString(), null, null, null));

		VolleyHttpClient.destroyCookie();

		// AQuery�� ĳ�õ��� �����Ѵ�.
		AQUtility.cleanCacheAsync(getApplicationContext());

		super.onDestroy();
	}

	@Override
	public void onError() {
		super.onError();
		
		// Error Fragment�� ǥ���Ѵ�.
		replaceFragment(new ErrorFragment());
	}

}
