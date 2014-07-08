package com.twoheart.dailyhotel.fragment;

import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.twoheart.dailyhotel.DailyHotel;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.adapter.HotelImageFragmentPagerAdapter;
import com.twoheart.dailyhotel.model.HotelDetail;
import com.twoheart.dailyhotel.util.ui.BaseFragment;
import com.twoheart.dailyhotel.widget.HotelViewPager;
import com.viewpagerindicator.LoopCirclePageIndicator;

public class HotelTabBookingFragment extends BaseFragment implements
OnTouchListener {

	private static final String TAG = "HotelTabBookingFragment";
	private static final int DURATION_HOTEL_IMAGE_SHOW = 2000;
	private static final String KEY_BUNDLE_ARGUMENTS_HOTEL_DETAIL = "hotel_detail";

	private HotelDetail mHotelDetail;
	private HotelImageFragmentPagerAdapter mAdapter;
	private HotelViewPager mViewPager;
	private LoopCirclePageIndicator mIndicator;
	private TextView tvBedType, tvAddress, tvPrice, tvDiscount;

	private Handler mHandler;
	private int mCurrentPage = 0;

	public static HotelTabBookingFragment newInstance(HotelDetail hotelDetail) {

		HotelTabBookingFragment newFragment = new HotelTabBookingFragment();
		Bundle arguments = new Bundle();

		arguments.putParcelable(KEY_BUNDLE_ARGUMENTS_HOTEL_DETAIL, hotelDetail);
		newFragment.setArguments(arguments);
		newFragment.setTitle("����");

		return newFragment;

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHotelDetail = (HotelDetail) getArguments().getParcelable(KEY_BUNDLE_ARGUMENTS_HOTEL_DETAIL);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_hotel_tab_booking, container,
				false);

		tvBedType = (TextView) view
				.findViewById(R.id.tv_hotel_tab_booking_bed_type);
		tvAddress = (TextView) view
				.findViewById(R.id.tv_hotel_tab_booking_address);
		tvPrice = (TextView) view.findViewById(R.id.tv_hotel_tab_booking_price);
		tvDiscount = (TextView) view
				.findViewById(R.id.tv_hotel_tab_booking_discount);
		mViewPager = (HotelViewPager) view
				.findViewById(R.id.vp_hotel_tab_booking_img);
		mIndicator = (LoopCirclePageIndicator) view
				.findViewById(R.id.cp_hotel_tab_booking_indicator);

		tvBedType.setText(mHotelDetail.getHotel().getBedType());
		tvAddress.setText(mHotelDetail.getHotel().getAddress());
		tvAddress.setSelected(true);
		tvDiscount.setText(mHotelDetail.getHotel().getDiscount() + "��");
		tvPrice.setText(mHotelDetail.getHotel().getPrice() + "��");
		tvPrice.setPaintFlags(tvPrice.getPaintFlags()
				| Paint.STRIKE_THRU_TEXT_FLAG);

		if (mAdapter == null) {
			mAdapter = new HotelImageFragmentPagerAdapter(getChildFragmentManager(), mHotelDetail);
			mViewPager.setAdapter(mAdapter);
		} else {
			mAdapter.notifyDataSetChanged();
		}

		mViewPager.setOnTouchListener(this);
		mViewPager.setCurrentItem((mHotelDetail.getImageUrl().size() * 10000)); // �������� ū ���� ����� �����Ͽ� ������ �ϰ� �� 
		mIndicator.setViewPager(mViewPager);
		mIndicator.setSnap(true);

		mCurrentPage = mHotelDetail.getImageUrl().size();
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				mCurrentPage = mViewPager.getCurrentItem();
				mCurrentPage++;
				mViewPager.setCurrentItem(mCurrentPage, true);
				this.sendEmptyMessageDelayed(0, DURATION_HOTEL_IMAGE_SHOW);
			}
		};

//		mHandler.sendEmptyMessageDelayed(0, DURATION_HOTEL_IMAGE_SHOW);

		return view;
	}

	@Override
	public void onResume(){
		super.onResume();
		tvDiscount.setTypeface(DailyHotel.getBoldTypeface());
		if (mHandler != null) { 
			mHandler.sendEmptyMessageDelayed(0, DURATION_HOTEL_IMAGE_SHOW);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mHandler != null) { 
			mHandler.removeMessages(0);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (mHandler != null)
			mHandler.removeMessages(0);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		if (v.getId() == mViewPager.getId()) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mHandler.removeMessages(0);
				break;
			case MotionEvent.ACTION_MOVE:
				mHandler.removeMessages(0);
				break;

			case MotionEvent.ACTION_UP:
				mHandler.sendEmptyMessageDelayed(0,
						DURATION_HOTEL_IMAGE_SHOW * 2);
			default:
				break;
			}
		}

		return false;
	}

}
