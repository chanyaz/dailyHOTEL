package com.twoheart.dailyhotel.screen.search.stay.result;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.View;

import com.twoheart.dailyhotel.model.PlaceViewItem;
import com.twoheart.dailyhotel.model.Stay;
import com.twoheart.dailyhotel.screen.hotel.list.StayListAdapter;
import com.twoheart.dailyhotel.util.Constants;

import java.util.ArrayList;

public class StaySearchResultListAdapter extends StayListAdapter
{
    public StaySearchResultListAdapter(Context context, ArrayList<PlaceViewItem> arrayList, View.OnClickListener listener, View.OnClickListener eventBannerListener)
    {
        super(context, arrayList, listener, eventBannerListener);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected void onBindViewHolder(StayViewHolder holder, PlaceViewItem placeViewItem, int position)
    {
        final Stay stay = placeViewItem.getItem();

        holder.stayCardView.setStickerVisible(mRewardEnabled && stay.dailyReward);
        holder.stayCardView.setDeleteVisible(false);
        holder.stayCardView.setWishVisible(true);
        holder.stayCardView.setWish(stay.myWish);

        holder.stayCardView.setImage(stay.imageUrl);

        holder.stayCardView.setGradeText(stay.getGrade().getName(mContext));
        holder.stayCardView.setVRVisible(stay.truevr && mTrueVREnabled);
        holder.stayCardView.setReviewText(stay.satisfaction, stay.reviewCount);

        holder.stayCardView.setNewVisible(stay.newItem);

        holder.stayCardView.setStayNameText(stay.name);

        if (mShowDistanceIgnoreSort == true || getSortType() == Constants.SortType.DISTANCE)
        {
            holder.stayCardView.setDistanceVisible(true);
            holder.stayCardView.setDistanceText(stay.distance);
        } else
        {
            holder.stayCardView.setDistanceVisible(false);
        }

        holder.stayCardView.setAddressText(stay.addressSummary);

        if (stay.availableRooms > 0)
        {
            holder.stayCardView.setPriceText(stay.discountRate, stay.discountPrice, stay.price, stay.couponDiscountText, mNights > 1);
        } else
        {
            holder.stayCardView.setPriceText(0, 0, 0, null, false);
        }

        holder.stayCardView.setBenefitText(stay.dBenefitText);

        if (position == 0)
        {
            holder.stayCardView.setDividerVisible(false);
        } else
        {
            holder.stayCardView.setDividerVisible(true);
        }


        //
        //
        //
        //        String strPrice = DailyTextUtils.getPriceFormat(mContext, stay.price, false);
        //        String strDiscount = DailyTextUtils.getPriceFormat(mContext, stay.discountPrice, false);
        //
        //        String address = stay.addressSummary;
        //
        //        int barIndex = address.indexOf('|');
        //        if (barIndex >= 0)
        //        {
        //            address = address.replace(" | ", "ㅣ");
        //        } else if (address.indexOf('l') >= 0)
        //        {
        //            address = address.replace(" l ", "ㅣ");
        //        }
        //
        //        holder.dataBinding.addressTextView.setText(address);
        //        holder.dataBinding.nameTextView.setText(stay.name);
        //
        //        boolean isVisiblePrice = false;
        //        boolean isVisibleSatisfaction = false;
        //
        //        if (stay.price <= 0 || stay.price <= stay.discountPrice)
        //        {
        //            holder.dataBinding.priceTextView.setVisibility(View.INVISIBLE);
        //            holder.dataBinding.priceTextView.setText(null);
        //        } else
        //        {
        //            holder.dataBinding.priceTextView.setVisibility(View.VISIBLE);
        //            holder.dataBinding.priceTextView.setText(strPrice);
        //            holder.dataBinding.priceTextView.setPaintFlags(holder.dataBinding.priceTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        //
        //            isVisiblePrice = true;
        //        }
        //
        //        // 만족도
        //        if (stay.satisfaction > 0)
        //        {
        //            holder.dataBinding.satisfactionView.setVisibility(View.VISIBLE);
        //            holder.dataBinding.satisfactionView.setText(//
        //                mContext.getResources().getString(R.string.label_list_satisfaction, stay.satisfaction));
        //
        //            isVisibleSatisfaction = true;
        //        } else
        //        {
        //            holder.dataBinding.satisfactionView.setVisibility(View.GONE);
        //        }
        //
        //        // 판매 완료인 경우에는 보여주지 않는다.
        //        if (mNights > 1 && stay.availableRooms > 0)
        //        {
        //            holder.dataBinding.averageTextView.setVisibility(View.VISIBLE);
        //        } else
        //        {
        //            holder.dataBinding.averageTextView.setVisibility(View.GONE);
        //        }
        //
        //        holder.dataBinding.discountPriceTextView.setText(strDiscount);
        //        holder.dataBinding.nameTextView.setSelected(true); // Android TextView marquee bug
        //
        //        if (VersionUtils.isOverAPI16() == true)
        //        {
        //            holder.dataBinding.gradientView.setBackground(mPaintDrawable);
        //        } else
        //        {
        //            holder.dataBinding.gradientView.setBackgroundDrawable(mPaintDrawable);
        //        }
        //
        //        // grade
        //        holder.dataBinding.gradeTextView.setText(stay.getGrade().getName(mContext));
        //        holder.dataBinding.gradeTextView.setBackgroundResource(stay.getGrade().getColorResId());
        //
        //        Util.requestImageResize(mContext, holder.dataBinding.imageView, stay.imageUrl);
        //
        //        // SOLD OUT 표시
        //        holder.dataBinding.soldoutView.setVisibility(View.GONE);
        //
        //        if (stay.availableRooms == 0)
        //        {
        //            holder.dataBinding.priceTextView.setVisibility(View.INVISIBLE);
        //            holder.dataBinding.priceTextView.setText(null);
        //            holder.dataBinding.discountPriceTextView.setText(mContext.getString(R.string.act_hotel_soldout));
        //        }
        //
        //        if (DailyTextUtils.isTextEmpty(stay.dBenefitText) == false)
        //        {
        //            holder.dataBinding.dBenefitTextView.setVisibility(View.VISIBLE);
        //            holder.dataBinding.dBenefitTextView.setText(stay.dBenefitText);
        //        } else
        //        {
        //            holder.dataBinding.dBenefitTextView.setVisibility(View.GONE);
        //        }
        //
        //        if (mShowDistanceIgnoreSort == true || getSortType() == Constants.SortType.DISTANCE)
        //        {
        //            if (holder.dataBinding.satisfactionView.getVisibility() == View.VISIBLE || holder.dataBinding.trueVRView.getVisibility() == View.VISIBLE)
        //            {
        //                holder.dataBinding.dot1View.setVisibility(View.VISIBLE);
        //            } else
        //            {
        //                holder.dataBinding.dot1View.setVisibility(View.GONE);
        //            }
        //
        //            holder.dataBinding.distanceTextView.setVisibility(View.VISIBLE);
        //            holder.dataBinding.distanceTextView.setText(mContext.getString(R.string.label_distance_km, new DecimalFormat("#.#").format(stay.distance)));
        //        } else
        //        {
        //            holder.dataBinding.dot1View.setVisibility(View.GONE);
        //            holder.dataBinding.distanceTextView.setVisibility(View.GONE);
        //        }
        //
        //        // VR 여부
        //        if (stay.truevr == true && mTrueVREnabled == true)
        //        {
        //            if (holder.dataBinding.satisfactionView.getVisibility() == View.VISIBLE)
        //            {
        //                holder.dataBinding.dot2View.setVisibility(View.VISIBLE);
        //            } else
        //            {
        //                holder.dataBinding.dot2View.setVisibility(View.GONE);
        //            }
        //
        //            holder.dataBinding.trueVRView.setVisibility(View.VISIBLE);
        //        } else
        //        {
        //            holder.dataBinding.dot2View.setVisibility(View.GONE);
        //            holder.dataBinding.trueVRView.setVisibility(View.GONE);
        //        }
        //
        //        if (holder.dataBinding.satisfactionView.getVisibility() == View.GONE//
        //            && holder.dataBinding.trueVRView.getVisibility() == View.GONE//
        //            && holder.dataBinding.distanceTextView.getVisibility() == View.GONE)
        //        {
        //            holder.dataBinding.informationLayout.setVisibility(View.GONE);
        //        } else
        //        {
        //            holder.dataBinding.informationLayout.setVisibility(View.VISIBLE);
        //        }
    }
}
