package com.twoheart.dailyhotel.adapter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.twoheart.dailyhotel.DailyHotel;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Hotel;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.GlobalFont;
import com.twoheart.dailyhotel.util.ui.HotelListViewItem;
import com.twoheart.dailyhotel.widget.HotelGradeView;
import com.twoheart.dailyhotel.widget.PinnedSectionListView.PinnedSectionListAdapter;

public class HotelListAdapter extends ArrayAdapter<HotelListViewItem> implements PinnedSectionListAdapter
{
	private Context context;
	private int resourceId;
	private LayoutInflater inflater;
	private LruCache<Integer, Bitmap> imgCache;

	public HotelListAdapter(Context context, int resourceId, ArrayList<HotelListViewItem> hotelList)
	{
		super(context, resourceId, hotelList);

		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		final int cacheSize = maxMemory / 8;
		this.imgCache = new LruCache<Integer, Bitmap>(cacheSize)
		{
			@Override
			protected int sizeOf(Integer key, Bitmap value)
			{
				return value.getRowBytes() * value.getHeight() / 1024;
			}
		}; // 최대 가용 메모리의 1/8 
		this.context = context;
		this.resourceId = resourceId;

		this.inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public void addAll(Collection<? extends HotelListViewItem> collection)
	{
		if (collection == null)
		{
			return;
		}

		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1)
		{
			super.addAll(collection);
		} else
		{
			for (HotelListViewItem item : collection)
			{
				add(item);
			}
		}
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{

		HotelListViewItem item = getItem(position);

		switch (item.getType())
		{
			case HotelListViewItem.TYPE_SECTION:
				HeaderListViewHolder headerViewHolder = null;

				if (convertView != null)
				{
					Object tag = convertView.getTag();

					if (tag != null && tag instanceof HeaderListViewHolder)
					{
						headerViewHolder = (HeaderListViewHolder) convertView.getTag();
					}
				}

				if (headerViewHolder == null)
				{
					convertView = inflater.inflate(R.layout.list_row_hotel_section, parent, false);
					headerViewHolder = new HeaderListViewHolder();
					headerViewHolder.regionDetailName = (TextView) convertView.findViewById(R.id.hotelListRegionName);

					convertView.setTag(headerViewHolder);
				}

				headerViewHolder.regionDetailName.setText(item.getCategory());

				GlobalFont.apply((ViewGroup) convertView);
				break;

			case HotelListViewItem.TYPE_ENTRY:

				Hotel element = item.getItem();
				HotelListViewHolder viewHolder = null;

				if (convertView != null)
				{
					Object tag = convertView.getTag();

					if (tag != null && tag instanceof HotelListViewHolder)
					{
						viewHolder = (HotelListViewHolder) convertView.getTag();
					}
				}

				if (viewHolder == null)
				{
					convertView = inflater.inflate(resourceId, parent, false);

					viewHolder = new HotelListViewHolder();
					viewHolder.llHotelRowContent = (RelativeLayout) convertView.findViewById(R.id.ll_hotel_row_content);
					viewHolder.img = (ImageView) convertView.findViewById(R.id.iv_hotel_row_img);
					viewHolder.name = (TextView) convertView.findViewById(R.id.tv_hotel_row_name);
					viewHolder.price = (TextView) convertView.findViewById(R.id.tv_hotel_row_price);
					viewHolder.discount = (TextView) convertView.findViewById(R.id.tv_hotel_row_discount);
					viewHolder.sold_out = (TextView) convertView.findViewById(R.id.tv_hotel_row_soldout);
					viewHolder.address = (TextView) convertView.findViewById(R.id.tv_hotel_row_address);
					viewHolder.grade = (HotelGradeView) convertView.findViewById(R.id.hv_hotel_grade);

					convertView.setTag(viewHolder);
				}

				DecimalFormat comma = new DecimalFormat("###,##0");
				String strPrice = comma.format(Integer.parseInt(element.getPrice()));
				String strDiscount = comma.format(Integer.parseInt(element.getDiscount()));

				viewHolder.address.setText(element.getAddress());
				viewHolder.name.setText(element.getName());

				Spanned currency = Html.fromHtml(getContext().getResources().getString(R.string.currency));

				SharedPreferences sharedPreference = convertView.getContext().getSharedPreferences(Constants.NAME_DAILYHOTEL_SHARED_PREFERENCE, Context.MODE_PRIVATE);
				String locale = sharedPreference.getString(Constants.KEY_PREFERENCE_LOCALE, null);

				if (locale.equals("한국어") == true)
				{
					viewHolder.price.setText(strPrice + currency);
					viewHolder.price.setPaintFlags(viewHolder.price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
					viewHolder.discount.setText(strDiscount + currency);
				} else
				{
					viewHolder.price.setText(currency + " " + strPrice);
					viewHolder.price.setPaintFlags(viewHolder.price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
					viewHolder.discount.setText(currency + " " + strDiscount);
				}

				viewHolder.name.setSelected(true); // Android TextView marquee bug

				final int colors[] = { Color.parseColor("#ED000000"), Color.parseColor("#E8000000"), Color.parseColor("#E2000000"), Color.parseColor("#66000000"), Color.parseColor("#00000000") };
				final float positions[] = { 0.0f, 0.01f, 0.02f, 0.17f, 0.38f };

				PaintDrawable p = new PaintDrawable();
				p.setShape(new RectShape());

				ShapeDrawable.ShaderFactory sf = new ShapeDrawable.ShaderFactory()
				{
					@Override
					public Shader resize(int width, int height)
					{
						return new LinearGradient(0, height, 0, 0, colors, positions, Shader.TileMode.CLAMP);
					}
				};

				p.setShaderFactory(sf);
				viewHolder.llHotelRowContent.setBackgroundDrawable(p);

				// grade
				viewHolder.grade.setHotelGradeCode(element.getCategory());

				GlobalFont.apply((ViewGroup) convertView);
				viewHolder.name.setTypeface(DailyHotel.getBoldTypeface());
				viewHolder.discount.setTypeface(DailyHotel.getBoldTypeface());

				AQuery aq = new AQuery(convertView);
				Bitmap cachedImg = getImgCache().get(position);

				if (cachedImg == null)
				{ // 힛인 밸류가 없다면 이미지를 불러온 후 캐시에 세이브
					BitmapAjaxCallback cb = new BitmapAjaxCallback()
					{
						@Override
						protected void callback(String url, ImageView iv, Bitmap bm, AjaxStatus status)
						{
							getImgCache().put(position, bm);
							super.callback(url, iv, bm, status);
						}
					};

					cb.url(element.getImage()).animation(AQuery.FADE_IN);
					aq.id(viewHolder.img).image(cb);

				} else
				{
					aq.id(viewHolder.img).image(cachedImg);
					//				cachedImg.recycle();
				}

				// 객실이 1~2 개일때 label 표시
				int avail_cnt = element.getAvailableRoom();
				// if(avail_cnt > 0 && avail_cnt < 3) {
				// label.setText(avail_cnt + " 객실 남음");
				// label.setVisibility(View.VISIBLE);
				// }
				// else
				// label.setVisibility(View.GONE);

				// SOLD OUT 표시
				if (avail_cnt == 0)
				{
					viewHolder.sold_out.setVisibility(View.VISIBLE);
				} else
				{
					viewHolder.sold_out.setVisibility(View.GONE);
				}

				// if(!items.get(position).isAvaliable()) {
				// sold_out.setBackgroundResource(R.drawable.dh_sold_out);
				// Drawable alpha1 = sold_out.getBackground();
				// alpha1.setAlpha(200);
				// }

				break;
		}

		return convertView;
	}

	private class HotelListViewHolder
	{
		RelativeLayout llHotelRowContent;
		ImageView img;
		TextView name;
		TextView price;
		TextView discount;
		TextView sold_out;
		TextView address;
		HotelGradeView grade;
	}

	private class HeaderListViewHolder
	{
		TextView regionDetailName;
	}

	@Override
	public boolean isItemViewTypePinned(int viewType)
	{
		return viewType == HotelListViewItem.TYPE_SECTION;
	}

	@Override
	public int getViewTypeCount()
	{
		return 2;
	}

	@Override
	public int getItemViewType(int position)
	{
		return getItem(position).getType();
	}

	public LruCache<Integer, Bitmap> getImgCache()
	{
		return imgCache;
	}

}
