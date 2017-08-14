package com.daily.dailyhotel.screen.home.campaigntag.gourmet;

import android.annotation.TargetApi;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Paint;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ScreenUtils;
import com.daily.base.util.VersionUtils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.imagepipeline.image.ImageInfo;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.databinding.ListRowGourmetDataBinding;
import com.twoheart.dailyhotel.databinding.ListRowStayDataBinding;
import com.twoheart.dailyhotel.model.Gourmet;
import com.twoheart.dailyhotel.model.PlaceViewItem;
import com.twoheart.dailyhotel.model.Stay;
import com.twoheart.dailyhotel.model.time.PlaceBookingDay;
import com.twoheart.dailyhotel.model.time.StayBookingDay;
import com.twoheart.dailyhotel.network.model.RecommendationGourmet;
import com.twoheart.dailyhotel.network.model.RecommendationStay;
import com.twoheart.dailyhotel.network.model.Sticker;
import com.twoheart.dailyhotel.place.adapter.PlaceListAdapter;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.Util;

import java.util.ArrayList;

/**
 * Created by iseung-won on 2017. 8. 8..
 */

public class GourmetCampaignListAdapter extends PlaceListAdapter
{
    private boolean mIsUsedMultiTransition;

    View.OnClickListener mOnClickListener;

    public GourmetCampaignListAdapter(Context context, ArrayList<PlaceViewItem> arrayList, View.OnClickListener listener)
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
    public void setPlaceBookingDay(PlaceBookingDay placeBookingDay)
    {
//        if (placeBookingDay == null)
//        {
//            return;
//        }
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
                ListRowGourmetDataBinding dataBinding = DataBindingUtil.inflate(mInflater, R.layout.list_row_gourmet_data, parent, false);

                return new GourmetCampaignListAdapter.GourmetViewHolder(dataBinding);
            }

            case PlaceViewItem.TYPE_HEADER_VIEW:
            {
                View view = mInflater.inflate(R.layout.list_row_collection_header, parent, false);

                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT//
                    , ScreenUtils.getRatioHeightType16x9(ScreenUtils.getScreenWidth(mContext)) //
                    + ScreenUtils.dpToPx(mContext, 81) - ScreenUtils.dpToPx(mContext, 97));
                view.setLayoutParams(layoutParams);

                return new GourmetCampaignListAdapter.HeaderViewHolder(view);
            }

            case PlaceViewItem.TYPE_FOOTER_VIEW:
            {
                View view = mInflater.inflate(R.layout.view_empty_gourmet_collection, parent, false);

                int height = ScreenUtils.getScreenHeight(mContext) - ScreenUtils.dpToPx(mContext, 97) //
                    - ScreenUtils.getRatioHeightType16x9(ScreenUtils.getScreenWidth(mContext)) //
                    + ScreenUtils.dpToPx(mContext, 81) - ScreenUtils.dpToPx(mContext, 97); //

                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT//
                    , height);
                view.setLayoutParams(layoutParams);

                return new FooterViewHolder(view);
            }

            case PlaceViewItem.TYPE_FOOTER_GUIDE_VIEW:
            {
                View view = mInflater.inflate(R.layout.list_row_users_place_footer, parent, false);
                return new GourmetCampaignListAdapter.FooterGuideViewHolder(view);
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
                onBindViewHolder((GourmetCampaignListAdapter.GourmetViewHolder) holder, item);
                break;

            case PlaceViewItem.TYPE_SECTION:
                onBindViewHolder((SectionViewHolder) holder, item);
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onBindViewHolder(GourmetCampaignListAdapter.GourmetViewHolder holder, PlaceViewItem placeViewItem)
    {
        final Gourmet gourmet = placeViewItem.getItem();

        String strPrice = DailyTextUtils.getPriceFormat(mContext, gourmet.price, false);
        String strDiscount = DailyTextUtils.getPriceFormat(mContext, gourmet.discountPrice, false);

        String address = gourmet.addressSummary;

        int barIndex = address.indexOf('|');
        if (barIndex >= 0)
        {
            address = address.replace(" | ", "ㅣ");
        } else if (address.indexOf('l') >= 0)
        {
            address = address.replace(" l ", "ㅣ");
        }

        holder.dataBinding.addressTextView.setText(address);
        holder.dataBinding.nameTextView.setText(gourmet.name);

        // 인원
        if (gourmet.persons > 1)
        {
            holder.dataBinding.personsTextView.setVisibility(View.VISIBLE);
            holder.dataBinding.personsTextView.setText(mContext.getString(R.string.label_persions, gourmet.persons));
        } else
        {
            holder.dataBinding.personsTextView.setVisibility(View.GONE);
        }

        if (gourmet.price <= 0 || gourmet.price <= gourmet.discountPrice)
        {
            holder.dataBinding.priceTextView.setVisibility(View.INVISIBLE);
            holder.dataBinding.priceTextView.setText(null);
        } else
        {
            holder.dataBinding.priceTextView.setVisibility(View.VISIBLE);

            holder.dataBinding.priceTextView.setText(strPrice);
            holder.dataBinding.priceTextView.setPaintFlags(holder.dataBinding.priceTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        // 만족도
        if (gourmet.satisfaction > 0)
        {
            holder.dataBinding.satisfactionView.setVisibility(View.VISIBLE);
            holder.dataBinding.satisfactionView.setText(//
                mContext.getResources().getString(R.string.label_list_satisfaction, gourmet.satisfaction));
        } else
        {
            holder.dataBinding.satisfactionView.setVisibility(View.GONE);
        }

        holder.dataBinding.discountPriceTextView.setText(strDiscount);
        holder.dataBinding.nameTextView.setSelected(true); // Android TextView marquee bug

        if (VersionUtils.isOverAPI16() == true)
        {
            holder.dataBinding.gradientView.setBackground(mPaintDrawable);
        } else
        {
            holder.dataBinding.gradientView.setBackgroundDrawable(mPaintDrawable);
        }

        String displayCategory;
        if (DailyTextUtils.isTextEmpty(gourmet.subCategory) == false)
        {
            displayCategory = gourmet.subCategory;
        } else
        {
            displayCategory = gourmet.category;
        }

        // grade
        if (DailyTextUtils.isTextEmpty(displayCategory) == true)
        {
            holder.dataBinding.gradeTextView.setVisibility(View.GONE);
        } else
        {
            holder.dataBinding.gradeTextView.setVisibility(View.VISIBLE);
            holder.dataBinding.gradeTextView.setText(displayCategory);
        }

        if (mIsUsedMultiTransition == true && VersionUtils.isOverAPI21() == true)
        {
            holder.dataBinding.imageView.setTransitionName(null);
        }

        // 스티커
        if (DailyTextUtils.isTextEmpty(gourmet.stickerUrl) == false)
        {
            holder.dataBinding.stickerSimpleDraweeView.setVisibility(View.VISIBLE);

            DraweeController controller = Fresco.newDraweeControllerBuilder().setControllerListener(new BaseControllerListener<ImageInfo>()
            {
                @Override
                public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable)
                {
                    ViewGroup.LayoutParams layoutParams = holder.dataBinding.stickerSimpleDraweeView.getLayoutParams();

                    int screenWidth = ScreenUtils.getScreenWidth(mContext);
                    if (screenWidth > Sticker.DEFAULT_SCREEN_WIDTH && screenWidth < Sticker.LARGE_SCREEN_WIDTH)
                    {
                        layoutParams.width = (int) (Sticker.MEDIUM_RATE * imageInfo.getWidth());
                        layoutParams.height = (int) (Sticker.MEDIUM_RATE * imageInfo.getHeight());
                    } else
                    {
                        layoutParams.width = imageInfo.getWidth();
                        layoutParams.height = imageInfo.getHeight();
                    }

                    holder.dataBinding.stickerSimpleDraweeView.setLayoutParams(layoutParams);
                }
            }).setUri(Uri.parse(gourmet.stickerUrl)).build();

            holder.dataBinding.stickerSimpleDraweeView.setController(controller);
        } else
        {
            holder.dataBinding.stickerSimpleDraweeView.setVisibility(View.GONE);
        }

        Util.requestImageResize(mContext, holder.dataBinding.imageView, gourmet.imageUrl);

        // SOLD OUT 표시
        if (gourmet.isSoldOut)
        {
            holder.dataBinding.soldoutView.setVisibility(View.VISIBLE);
        } else
        {
            holder.dataBinding.soldoutView.setVisibility(View.GONE);
        }

        if (DailyTextUtils.isTextEmpty(gourmet.dBenefitText) == false)
        {
            holder.dataBinding.dBenefitTextView.setVisibility(View.VISIBLE);
            holder.dataBinding.dBenefitTextView.setText(gourmet.dBenefitText);
        } else
        {
            holder.dataBinding.dBenefitTextView.setVisibility(View.GONE);
        }

        //        if (mShowDistanceIgnoreSort == true || getSortType() == Constants.SortType.DISTANCE)
        //        {
        //            holder.distanceTextView.setVisibility(View.VISIBLE);
        //            holder.distanceTextView.setText(mContext.getString(R.string.label_distance_km, new DecimalFormat("#.#").format(gourmet.distance)));
        //        } else
        //        {
        holder.dataBinding.dot1View.setVisibility(View.GONE);
        holder.dataBinding.distanceTextView.setVisibility(View.GONE);
        //        }

        // VR 여부, 추후 고메가 VR이 생기면 화면에 보여주도록 한다.
        //        if (recommendationGourmet.truevr == true && mTrueVREnabled == true)
        //        {
        //            if (holder.satisfactionView.getVisibility() == View.VISIBLE)
        //            {
        //                holder.dot2View.setVisibility(View.VISIBLE);
        //            } else
        //            {
        //                holder.dot2View.setVisibility(View.GONE);
        //            }
        //
        //            holder.trueVRView.setVisibility(View.VISIBLE);
        //        } else
        {
            holder.dataBinding.dot2View.setVisibility(View.GONE);
            holder.dataBinding.trueVRView.setVisibility(View.GONE);
        }

        if (holder.dataBinding.satisfactionView.getVisibility() == View.GONE//
            && holder.dataBinding.trueVRView.getVisibility() == View.GONE//
            && holder.dataBinding.distanceTextView.getVisibility() == View.GONE)
        {
            holder.dataBinding.informationLayout.setVisibility(View.GONE);
        } else
        {
            holder.dataBinding.informationLayout.setVisibility(View.VISIBLE);
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder
    {
        public HeaderViewHolder(View itemView)
        {
            super(itemView);
        }
    }

    private class FooterGuideViewHolder extends RecyclerView.ViewHolder
    {
        public FooterGuideViewHolder(View itemView)
        {
            super(itemView);
        }
    }

    private class GourmetViewHolder extends RecyclerView.ViewHolder
    {
        ListRowGourmetDataBinding dataBinding;

        public GourmetViewHolder(ListRowGourmetDataBinding dataBinding)
        {
            super(dataBinding.getRoot());

            this.dataBinding = dataBinding;

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
        }
    }
}