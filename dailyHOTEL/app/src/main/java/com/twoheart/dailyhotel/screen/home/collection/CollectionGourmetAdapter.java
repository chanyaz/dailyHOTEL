package com.twoheart.dailyhotel.screen.home.collection;

import android.annotation.TargetApi;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Vibrator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ScreenUtils;
import com.daily.dailyhotel.view.DailyGourmetCardView;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.databinding.LayoutSectionDataBinding;
import com.twoheart.dailyhotel.model.PlaceViewItem;
import com.twoheart.dailyhotel.model.time.PlaceBookingDay;
import com.twoheart.dailyhotel.network.model.RecommendationGourmet;
import com.twoheart.dailyhotel.place.adapter.PlaceListAdapter;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.Util;

import java.util.ArrayList;

public class CollectionGourmetAdapter extends PlaceListAdapter
{
    private boolean mIsUsedMultiTransition;
    View.OnClickListener mOnClickListener;

    public CollectionGourmetAdapter(Context context, ArrayList<PlaceViewItem> arrayList, View.OnClickListener listener)
    {
        super(context, arrayList);

        mOnClickListener = listener;

        setSortType(Constants.SortType.DEFAULT);
    }

    public void setUsedMultiTransition(boolean isUsedMultiTransition)
    {
        mIsUsedMultiTransition = isUsedMultiTransition;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        switch (viewType)
        {
            case PlaceViewItem.TYPE_SECTION:
            {
                LayoutSectionDataBinding viewDataBinding = DataBindingUtil.inflate(mInflater, R.layout.layout_section_data, parent, false);

                return new SectionViewHolder(viewDataBinding);
            }

            case PlaceViewItem.TYPE_ENTRY:
            {
                DailyGourmetCardView gourmetCardView = new DailyGourmetCardView(mContext);
                gourmetCardView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                return new GourmetViewHolder(gourmetCardView);
            }

            case PlaceViewItem.TYPE_HEADER_VIEW:
            {
                View view = mInflater.inflate(R.layout.list_row_collection_header, parent, false);

                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT//
                    , ScreenUtils.getRatioHeightType16x9(ScreenUtils.getScreenWidth(mContext)) + ScreenUtils.dpToPx(mContext, 81) - ScreenUtils.dpToPx(mContext, 97));
                view.setLayoutParams(layoutParams);

                return new BaseViewHolder(view);
            }

            case PlaceViewItem.TYPE_EMPTY_VIEW:
            {
                View view = mInflater.inflate(R.layout.view_empty_gourmet_collection, parent, false);

                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT//
                    , ScreenUtils.getScreenHeight(mContext) - ScreenUtils.dpToPx(mContext, 97) - ScreenUtils.getRatioHeightType16x9(ScreenUtils.getScreenWidth(mContext)) + ScreenUtils.dpToPx(mContext, 81) - ScreenUtils.dpToPx(mContext, 97));
                view.setLayoutParams(layoutParams);

                return new BaseViewHolder(view);
            }

            case PlaceViewItem.TYPE_FOOTER_VIEW:
            {
                View view = mInflater.inflate(R.layout.list_row_users_place_footer, parent, false);

                return new BaseViewHolder(view);
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
                onBindViewHolder((GourmetViewHolder) holder, item, position);
                break;

            case PlaceViewItem.TYPE_SECTION:
                onBindViewHolder((SectionViewHolder) holder, item);
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onBindViewHolder(GourmetViewHolder holder, PlaceViewItem placeViewItem, int position)
    {
        final RecommendationGourmet recommendationGourmet = placeViewItem.getItem();

        holder.gourmetCardView.setStickerVisible(false);
        holder.gourmetCardView.setDeleteVisible(false);
        holder.gourmetCardView.setWishVisible(true);
        holder.gourmetCardView.setWish(recommendationGourmet.myWish);

        holder.gourmetCardView.setTagStickerImage(recommendationGourmet.stickerUrl);
        holder.gourmetCardView.setImage(recommendationGourmet.imageUrl);

        holder.gourmetCardView.setGradeText(DailyTextUtils.isTextEmpty(recommendationGourmet.categorySub) == false ? recommendationGourmet.categorySub : recommendationGourmet.category);
        holder.gourmetCardView.setVRVisible(recommendationGourmet.truevr && mTrueVREnabled);
        holder.gourmetCardView.setReviewText(recommendationGourmet.rating, recommendationGourmet.reviewCount);
        holder.gourmetCardView.setNewVisible(recommendationGourmet.newItem);
        holder.gourmetCardView.setGourmetNameText(recommendationGourmet.name);
        holder.gourmetCardView.setDistanceVisible(false);
        holder.gourmetCardView.setAddressText(recommendationGourmet.addrSummary);

        if (recommendationGourmet.availableTicketNumbers == 0 //
            || recommendationGourmet.availableTicketNumbers < recommendationGourmet.minimumOrderQuantity //
            || recommendationGourmet.isExpired == true)
        {
            holder.gourmetCardView.setPriceText(0, 0, 0, null, 0);
        } else
        {
            holder.gourmetCardView.setPriceText(recommendationGourmet.discountRate, recommendationGourmet.discount, recommendationGourmet.price, recommendationGourmet.couponDiscountText, recommendationGourmet.persons);
        }

        holder.gourmetCardView.setBenefitText(recommendationGourmet.benefit);

        // 최상위에는 빈뷰이가 1번째가 첫번째다.
        if (position == 1)
        {
            holder.gourmetCardView.setDividerVisible(false);
        } else
        {
            holder.gourmetCardView.setDividerVisible(true);
        }
    }

    @Override
    public void setPlaceBookingDay(PlaceBookingDay placeBookingDay)
    {

    }

    class GourmetViewHolder extends RecyclerView.ViewHolder
    {
        DailyGourmetCardView gourmetCardView;

        public GourmetViewHolder(DailyGourmetCardView gourmetCardView)
        {
            super(gourmetCardView);

            this.gourmetCardView = gourmetCardView;

            itemView.setOnClickListener(mOnClickListener);

            if (Util.supportPreview(mContext) == true)
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

            gourmetCardView.setOnWishClickListener(v ->
            {
                if (mOnWishClickListener != null)
                {
                    mOnWishClickListener.onClick(gourmetCardView);
                }
            });
        }
    }
}
