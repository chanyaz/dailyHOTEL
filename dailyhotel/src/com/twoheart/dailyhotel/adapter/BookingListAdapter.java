package com.twoheart.dailyhotel.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.twoheart.dailyhotel.BookingListFragment;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Booking;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.VolleyImageLoader;
import com.twoheart.dailyhotel.widget.PinnedSectionListView.PinnedSectionListAdapter;

public class BookingListAdapter extends ArrayAdapter<Booking> implements PinnedSectionListAdapter
{
	private ArrayList<Booking> items;
	private Context context;
	private BookingListFragment.OnUserActionListener mOnUserActionListener;

	public BookingListAdapter(Context context, int resourceId, ArrayList<Booking> items)
	{
		super(context, resourceId, items);

		this.items = items;
		this.context = context;
	}

	public void setOnUserActionListener(BookingListFragment.OnUserActionListener listener)
	{
		mOnUserActionListener = listener;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		Booking booking = items.get(position);

		if (convertView != null)
		{
			Integer tag = (Integer) convertView.getTag();

			if (tag == null || tag != booking.type)
			{
				convertView = null;
			}
		}

		switch (booking.type)
		{
			case Booking.TYPE_ENTRY:
			{
				if (convertView == null)
				{
					LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = layoutInflater.inflate(R.layout.list_row_booking, parent, false);
					convertView.setTag(Booking.TYPE_ENTRY);
				}

				convertView = getEntryView(convertView, booking);
				break;
			}

			case Booking.TYPE_SECTION:
			{
				if (convertView == null)
				{
					LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = layoutInflater.inflate(R.layout.list_row_booking_section, parent, false);
					convertView.setTag(Booking.TYPE_SECTION);
				}

				convertView = getSectionView(convertView, booking);
				break;
			}
		}

		return convertView;
	}

	private View getEntryView(View view, final Booking booking)
	{
		if (view == null || booking == null)
		{
			return view;
		}

		// 호텔 이미지
		ImageView hotelImageView = (ImageView) view.findViewById(R.id.hotelImage);

		AQuery aquery = new AQuery(view);
		Bitmap cachedImg = VolleyImageLoader.getCache(booking.hotelImageUrl);

		if (cachedImg == null)
		{ // 힛인 밸류가 없다면 이미지를 불러온 후 캐시에 세이브
			BitmapAjaxCallback cb = new BitmapAjaxCallback()
			{
				@Override
				protected void callback(String url, ImageView iv, Bitmap bm, AjaxStatus status)
				{
					VolleyImageLoader.putCache(url, bm);
					super.callback(url, iv, bm, status);
				}
			};

			cb.url(booking.hotelImageUrl);
			aquery.id(hotelImageView).image(cb).animate(R.anim.fade_in);
		} else
		{
			aquery.id(hotelImageView).image(cachedImg).animate(R.anim.fade_in);
		}

		TextView waitAccountTextView = (TextView) view.findViewById(R.id.waitAccountTextView);
		ImageView bookingIconImageView = (ImageView) view.findViewById(R.id.bookingIconImageView);
		TextView name = (TextView) view.findViewById(R.id.tv_booking_row_name);
		TextView day = (TextView) view.findViewById(R.id.tv_booking_row_day);
		View dimView = view.findViewById(R.id.usedDimView);
		View deleteView = view.findViewById(R.id.deleteView);

		name.setText(booking.getHotel_name());

		Date checkinDate = new Date(booking.checkinTime);
		Date checkOutDate = new Date(booking.checkoutTime);

		SimpleDateFormat sFormat = new SimpleDateFormat("yyyy.MM.dd");
		sFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		String period = String.format("%s - %s", sFormat.format(checkinDate), sFormat.format(checkOutDate));
		day.setText(period);

		if (booking.isUsed == true)
		{
			waitAccountTextView.setVisibility(View.GONE);
			bookingIconImageView.setVisibility(View.GONE);

			name.setTextColor(context.getResources().getColor(R.color.bookinglist_used_text));
			day.setTextColor(context.getResources().getColor(R.color.bookinglist_used_text));

			dimView.setVisibility(View.VISIBLE);
			deleteView.setVisibility(View.VISIBLE);

			// 삭제 버튼을 누를 경우;
			deleteView.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (mOnUserActionListener != null)
					{
						mOnUserActionListener.delete(booking);
					}
				}
			});
		} else
		{
			bookingIconImageView.setVisibility(View.VISIBLE);

			if (booking.getPayType() == Constants.CODE_PAY_TYPE_ACCOUNT_WAIT)
			{
				waitAccountTextView.setVisibility(View.VISIBLE);
				waitAccountTextView.setText(booking.ment);
				bookingIconImageView.setImageResource(R.drawable.ic_wait);
			} else
			{
				waitAccountTextView.setVisibility(View.GONE);
				bookingIconImageView.setImageResource(R.drawable.ic_complete);
			}

			name.setTextColor(context.getResources().getColor(R.color.white));
			day.setTextColor(context.getResources().getColor(R.color.white));

			dimView.setVisibility(View.GONE);
			deleteView.setVisibility(View.GONE);
		}

		return view;
	}

	private View getSectionView(View view, Booking booking)
	{
		if (view == null || booking == null)
		{
			return view;
		}

		TextView sectionName = (TextView) view.findViewById(R.id.bookingSectionName);

		sectionName.setText(booking.getHotel_name());

		return view;
	}

	@Override
	public boolean isItemViewTypePinned(int viewType)
	{
		return viewType == Booking.TYPE_SECTION;
	}

	@Override
	public int getViewTypeCount()
	{
		return 2;
	}

	@Override
	public int getItemViewType(int position)
	{
		return getItem(position).type;
	}
}
