package com.twoheart.dailyhotel.screen.hotel.list;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.os.Vibrator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ScreenUtils;
import com.daily.base.util.VersionUtils;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.PlaceViewItem;
import com.twoheart.dailyhotel.model.Stay;
import com.twoheart.dailyhotel.model.time.PlaceBookingDay;
import com.twoheart.dailyhotel.model.time.StayBookingDay;
import com.twoheart.dailyhotel.place.adapter.PlaceListAdapter;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.Util;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class StayListAdapter extends PlaceListAdapter
{
    private int mNights;
    View.OnClickListener mOnClickListener;

    public StayListAdapter(Context context, ArrayList<PlaceViewItem> arrayList, View.OnClickListener listener, View.OnClickListener eventBannerListener)
    {
        super(context, arrayList);

        mOnClickListener = listener;
        mOnEventBannerClickListener = eventBannerListener;

        setSortType(Constants.SortType.DEFAULT);
    }

    @Override
    public void setPlaceBookingDay(PlaceBookingDay placeBookingDay)
    {
        if (placeBookingDay == null)
        {
            return;
        }

        try
        {
            mNights = ((StayBookingDay) placeBookingDay).getNights();
        } catch (Exception e)
        {
            mNights = 1;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        switch (viewType)
        {
            case PlaceViewItem.TYPE_SECTION:
            {
                View view = mInflater.inflate(R.layout.list_row_default_section, parent, false);

                return new SectionViewHolder(view);
            }

            case PlaceViewItem.TYPE_ENTRY:
            {
                View view = mInflater.inflate(R.layout.list_row_hotel, parent, false);

                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT//
                    , ScreenUtils.getRatioHeightType16x9(ScreenUtils.getScreenWidth(mContext)));
                view.setLayoutParams(layoutParams);

                return new HotelViewHolder(view);
            }

            case PlaceViewItem.TYPE_EVENT_BANNER:
            {
                View view = mInflater.inflate(R.layout.list_row_eventbanner, parent, false);

                return new EventBannerViewHolder(view);
            }

            case PlaceViewItem.TYPE_FOOTER_VIEW:
            {
                View view = mInflater.inflate(R.layout.list_row_footer, parent, false);

                return new FooterViewHolder(view);
            }

            case PlaceViewItem.TYPE_LOADING_VIEW:
            {
                View view = mInflater.inflate(R.layout.list_row_loading, parent, false);

                return new FooterViewHolder(view);
            }
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        PlaceViewItem item = getItem(position);

        if (item == null)
        {
            return;
        }

        switch (item.mType)
        {
            case PlaceViewItem.TYPE_ENTRY:
                onBindViewHolder((HotelViewHolder) holder, item);
                break;

            case PlaceViewItem.TYPE_SECTION:
                onBindViewHolder((SectionViewHolder) holder, item);
                break;

            case PlaceViewItem.TYPE_EVENT_BANNER:
                onBindViewHolder((EventBannerViewHolder) holder, item);
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void onBindViewHolder(HotelViewHolder holder, PlaceViewItem placeViewItem)
    {
        final Stay stay = placeViewItem.getItem();

        String strPrice = DailyTextUtils.getPriceFormat(mContext, stay.price, false);
        String strDiscount = DailyTextUtils.getPriceFormat(mContext, stay.discountPrice, false);

        String address = stay.addressSummary;

        int barIndex = address.indexOf('|');
        if (barIndex >= 0)
        {
            address = address.replace(" | ", "ㅣ");
        } else if (address.indexOf('l') >= 0)
        {
            address = address.replace(" l ", "ㅣ");
        }

        holder.hotelAddressView.setText(address);
        holder.hotelNameView.setText(stay.name);

        if (stay.price <= 0 || stay.price <= stay.discountPrice)
        {
            holder.hotelPriceView.setVisibility(View.INVISIBLE);
            holder.hotelPriceView.setText(null);
        } else
        {
            holder.hotelPriceView.setVisibility(View.VISIBLE);
            holder.hotelPriceView.setText(strPrice);
            holder.hotelPriceView.setPaintFlags(holder.hotelPriceView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        // 만족도
        if (stay.satisfaction > 0)
        {
            holder.satisfactionView.setVisibility(View.VISIBLE);
            holder.satisfactionView.setText(//
                mContext.getResources().getString(R.string.label_list_satisfaction, stay.satisfaction));
        } else
        {
            holder.satisfactionView.setVisibility(View.GONE);
        }

        if (mNights > 1)
        {
            holder.averageView.setVisibility(View.VISIBLE);
        } else
        {
            holder.averageView.setVisibility(View.GONE);
        }

        holder.hotelDiscountView.setText(strDiscount);
        holder.hotelNameView.setSelected(true); // Android TextView marquee bug

        if (VersionUtils.isOverAPI16() == true)
        {
            holder.gradientView.setBackground(mPaintDrawable);
        } else
        {
            holder.gradientView.setBackgroundDrawable(mPaintDrawable);
        }

        // grade
        holder.hotelGradeView.setText(stay.getGrade().getName(mContext));
        holder.hotelGradeView.setBackgroundResource(stay.getGrade().getColorResId());

        Util.requestImageResize(mContext, holder.hotelImageView, stay.imageUrl);

        // SOLD OUT 표시
        if (stay.isSoldOut == true)
        {
            holder.hotelSoldOutView.setVisibility(View.VISIBLE);
        } else
        {
            holder.hotelSoldOutView.setVisibility(View.GONE);
        }

        if (DailyTextUtils.isTextEmpty(stay.dBenefitText) == false)
        {
            holder.dBenefitLayout.setVisibility(View.VISIBLE);
            holder.dBenefitTextView.setText(stay.dBenefitText);
        } else
        {
            holder.dBenefitLayout.setVisibility(View.GONE);
        }

        if (mShowDistanceIgnoreSort == true || getSortType() == Constants.SortType.DISTANCE)
        {
            if (holder.satisfactionView.getVisibility() == View.VISIBLE || holder.trueVRView.getVisibility() == View.VISIBLE)
            {
                holder.dot1View.setVisibility(View.VISIBLE);
            } else
            {
                holder.dot1View.setVisibility(View.GONE);
            }

            holder.distanceTextView.setVisibility(View.VISIBLE);
            holder.distanceTextView.setText("(거리:" + new DecimalFormat("#.#").format(stay.distance) + "km)");
        } else
        {
            holder.dot1View.setVisibility(View.GONE);
            holder.distanceTextView.setVisibility(View.GONE);
        }

        // VR 여부
        if (stay.truevr == true && Util.supportPeekNPop(mContext) == true)
        {
            if (holder.satisfactionView.getVisibility() == View.VISIBLE)
            {
                holder.dot2View.setVisibility(View.VISIBLE);
            } else
            {
                holder.dot2View.setVisibility(View.GONE);
            }

            holder.trueVRView.setVisibility(View.VISIBLE);
        } else
        {
            holder.dot2View.setVisibility(View.GONE);
            holder.trueVRView.setVisibility(View.GONE);
        }

        if (holder.satisfactionView.getVisibility() == View.GONE//
            && holder.trueVRView.getVisibility() == View.GONE//
            && holder.distanceTextView.getVisibility() == View.GONE)
        {
            holder.informationLayout.setVisibility(View.GONE);
        } else
        {
            holder.informationLayout.setVisibility(View.VISIBLE);
        }
    }

    private class HotelViewHolder extends RecyclerView.ViewHolder
    {
        View gradientView;
        com.facebook.drawee.view.SimpleDraweeView hotelImageView;
        TextView hotelNameView;
        TextView hotelPriceView;
        TextView hotelDiscountView;
        View hotelSoldOutView;
        TextView hotelAddressView;
        TextView hotelGradeView;
        TextView satisfactionView;
        View averageView;
        TextView distanceTextView;
        View dBenefitLayout;
        TextView dBenefitTextView;
        View informationLayout;
        View trueVRView;
        View dot1View;
        View dot2View;

        public HotelViewHolder(View itemView)
        {
            super(itemView);

            dBenefitLayout = itemView.findViewById(R.id.dBenefitLayout);
            dBenefitTextView = (TextView) dBenefitLayout.findViewById(R.id.dBenefitTextView);
            gradientView = itemView.findViewById(R.id.gradientView);
            hotelImageView = (com.facebook.drawee.view.SimpleDraweeView) itemView.findViewById(R.id.imageView);
            hotelNameView = (TextView) itemView.findViewById(R.id.nameTextView);
            hotelPriceView = (TextView) itemView.findViewById(R.id.priceTextView);
            satisfactionView = (TextView) itemView.findViewById(R.id.satisfactionView);
            hotelDiscountView = (TextView) itemView.findViewById(R.id.discountPriceTextView);
            hotelSoldOutView = itemView.findViewById(R.id.soldoutView);
            hotelAddressView = (TextView) itemView.findViewById(R.id.addressTextView);
            hotelGradeView = (TextView) itemView.findViewById(R.id.gradeTextView);
            averageView = itemView.findViewById(R.id.averageTextView);
            distanceTextView = (TextView) itemView.findViewById(R.id.distanceTextView);
            informationLayout = itemView.findViewById(R.id.informationLayout);
            trueVRView = itemView.findViewById(R.id.trueVRView);
            dot1View = itemView.findViewById(R.id.dot1View);
            dot2View = itemView.findViewById(R.id.dot2View);

            itemView.setOnClickListener(mOnClickListener);

            if (Util.supportPeekNPop(mContext) == true)
            {
                itemView.setOnLongClickListener(new View.OnLongClickListener()
                {
                    @Override
                    public boolean onLongClick(View v)
                    {
                        if (mOnLongClickListener == null)
                        {
                            return false;
                        } else
                        {
                            Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(70);

                            return mOnLongClickListener.onLongClick(v);
                        }
                    }
                });
            }
        }
    }
}
