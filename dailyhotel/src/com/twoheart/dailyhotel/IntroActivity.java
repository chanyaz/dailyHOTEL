package com.twoheart.dailyhotel;

import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.twoheart.dailyhotel.fragment.IntroGuideFragment;
import com.twoheart.dailyhotel.util.ui.BaseActivity;
import com.viewpagerindicator.CirclePageIndicator;

public class IntroActivity extends BaseActivity implements OnClickListener, OnPageChangeListener {
	
	private FragmentPagerAdapter mAdapter;
	private ViewPager mPager;
	private CirclePageIndicator mIndicator;
	private LinearLayout llIntroStart;
	private TextView tvSkip;
	private TextView tvStart;
	
	private List<Integer> mGuideBackgrounds;
	private List<String> mGuideTitles;
	private List<String> mGuideDesces;
	private List<Integer> mGuideImages;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setActionBarHide();
		setContentView(R.layout.activity_intro);
		
		mPager = (ViewPager) findViewById(R.id.intro_pager);
		mIndicator = (CirclePageIndicator) findViewById(R.id.intro_indicator);
		llIntroStart = (LinearLayout) findViewById(R.id.ll_intro_start);
		tvSkip = (TextView) findViewById(R.id.tv_skip);
		tvStart = (TextView) findViewById(R.id.tv_start);
		
		tvStart.setTypeface(DailyHotel.getBoldTypeface());
		
		initializeGuideContents();
		
		mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
			
			@Override
			public Fragment getItem(int position) {
				return IntroGuideFragment.newInstance(mGuideBackgrounds.get(position), mGuideTitles.get(position), 
						mGuideImages.get(position), mGuideDesces.get(position));
			}
			
			@Override
			public int getCount() {
				return mGuideTitles.size();
			}
			
		}; 
		
		mPager.setAdapter(mAdapter);
		mIndicator.setViewPager(mPager);
		mIndicator.setSnap(true);
		mIndicator.setOnPageChangeListener(this);
		
		llIntroStart.setOnClickListener(this);
		tvSkip.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View view) {
		if (view.getId() == llIntroStart.getId() || (view.getId() == tvSkip.getId())) {
			disableShowGuide();
			finish();
		}
	}
	
	@Override
	public void onBackPressed() {
		disableShowGuide();
		super.onBackPressed();
		
	}
	
	private void initializeGuideContents() {
		mGuideBackgrounds = new ArrayList<Integer>();
		mGuideTitles = new ArrayList<String>();
		mGuideDesces = new ArrayList<String>();
		mGuideImages = new ArrayList<Integer>();
		
		mGuideBackgrounds.add(R.drawable.img_bg_guide1);
		mGuideBackgrounds.add(R.drawable.img_bg_guide2);
		mGuideBackgrounds.add(R.drawable.img_bg_guide3);
		mGuideBackgrounds.add(R.drawable.img_bg_guide4);
		
		mGuideTitles.add("���� ȣ�� ����");
		mGuideTitles.add("������ ȣ��");
		mGuideTitles.add("���� ���� ����");
		mGuideTitles.add("���� ����");
		
		mGuideDesces.add("���� ���ຸ�� ���ϰ� �����ϰ�\n������ ȣ���� �����ϼ���.");
		mGuideDesces.add("�� �������� �������� ��������,\n���� �ŷ����� ȣ�ڸ��� �����մϴ�.");
		mGuideDesces.add("���� 9�ÿ��� �� 12�ñ���\n30�ʸ��� �����ϰ� �ٷ� üũ���ϼ���.");
		mGuideDesces.add("���� ���� ���Ǹ��� �Ǹ��ϱ⿡\n���� ������ �� �ۿ� �����ϴ�.\n\n�׷� ������ ȣ���� ���������?");
		
		mGuideImages.add(R.drawable.img_ic_guide_logo);
		mGuideImages.add(R.drawable.img_ic_guide_curation);
		mGuideImages.add(R.drawable.img_ic_guide_clock);
		mGuideImages.add(R.drawable.img_ic_guide_surprise);
		

	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		
	}

	@Override
	public void onPageSelected(int position) {
		if (position == (mGuideTitles.size() - 1)) {
			llIntroStart.setVisibility(View.VISIBLE);
			llIntroStart.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
			tvSkip.setVisibility(View.GONE);
			tvSkip.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out));
		} else {
			llIntroStart.setVisibility(View.GONE);
			tvSkip.setVisibility(View.VISIBLE);
		}
	}
	
	private void disableShowGuide() {
		Editor edit = sharedPreference.edit();
		edit.putBoolean(KEY_PREFERENCE_SHOW_GUIDE, false);
		edit.commit();
	}
	
}
