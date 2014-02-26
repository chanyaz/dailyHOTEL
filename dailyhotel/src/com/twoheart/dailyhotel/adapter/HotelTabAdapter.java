package com.twoheart.dailyhotel.adapter;

import com.twoheart.dailyhotel.fragment.HotelTabBookingFragment;
import com.twoheart.dailyhotel.fragment.HotelTabInfoFragment;
import com.twoheart.dailyhotel.fragment.HotelTabMapFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class HotelTabAdapter extends FragmentPagerAdapter{
	public HotelTabAdapter(FragmentManager fm) {
		super(fm);
	}
	
	@Override
	public Fragment getItem(int position) {
		if (position == 0)	return HotelTabBookingFragment.newInstance();
		else if (position == 1)	return HotelTabInfoFragment.newInstance();
		else return HotelTabMapFragment.newInstance();
	}
	
	@Override
	public CharSequence getPageTitle(int position) {
		if (position == 0) return "����";
		else if (position == 1) return "����";
		else return "����";
	}
	
	@Override
	public int getCount() {
		return 3;
	}
	
}
