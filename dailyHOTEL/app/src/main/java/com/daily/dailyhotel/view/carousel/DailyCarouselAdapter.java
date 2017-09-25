package com.daily.dailyhotel.view.carousel;

import android.annotation.TargetApi;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Paint;
import android.os.Build;
import android.os.Vibrator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ScreenUtils;
import com.daily.dailyhotel.entity.CarouselListItem;
import com.daily.dailyhotel.entity.ImageMap;
import com.daily.dailyhotel.entity.RecentlyPlace;
import com.daily.dailyhotel.entity.StayOutbound;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.imagepipeline.image.ImageInfo;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.databinding.ListRowCarouselItemDataBinding;
import com.twoheart.dailyhotel.model.Gourmet;
import com.twoheart.dailyhotel.model.Stay;
import com.twoheart.dailyhotel.network.model.Prices;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.Util;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by iseung-won on 2017. 8. 24..
 */

public class DailyCarouselAdapter extends RecyclerView.Adapter<DailyCarouselAdapter.PlaceViewHolder>
{
    Context mContext;
    private boolean mIsUsePriceLayout;
    private ArrayList<CarouselListItem> mList;
    //    protected PaintDrawable mPaintDrawable;
    protected ItemClickListener mItemClickListener;

    public interface ItemClickListener
    {
        void onItemClick(View view);

        void onItemLongClick(View view);
    }

    public DailyCarouselAdapter(Context context, ArrayList<CarouselListItem> list, ItemClickListener listener)
    {
        mContext = context;
        mList = list;
        mItemClickListener = listener;

        //        makeShaderFactory();
    }

    @Override
    public PlaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        ListRowCarouselItemDataBinding dataBinding = DataBindingUtil.inflate( //
            LayoutInflater.from(mContext), R.layout.list_row_carousel_item_data, parent, false);
        return new PlaceViewHolder(dataBinding);
    }

    @Override
    public void onBindViewHolder(PlaceViewHolder holder, int position)
    {
        CarouselListItem item = getItem(position);
        if (item == null)
        {
            return;
        }

        setLayoutMargin(holder, position);

        holder.itemView.setTag(item);

        switch (item.mType)
        {
            case CarouselListItem.TYPE_RECENTLY_PLACE:
            {
                onBindViewHolderByRecentlyPlace(holder, item);
                break;
            }

            case CarouselListItem.TYPE_IN_STAY:
            {
                onBindViewHolderByStay(holder, item);
                break;
            }

            case CarouselListItem.TYPE_OB_STAY:
            {
                onBindViewHolderByStayOutbound(holder, item);
                break;
            }

            case CarouselListItem.TYPE_GOURMET:
            {
                onBindViewHolderByGourmet(holder, item);
                break;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void onBindViewHolderByRecentlyPlace(PlaceViewHolder holder, CarouselListItem item)
    {
        final RecentlyPlace place = item.getItem();

        holder.dataBinding.contentImageView.setTag(holder.dataBinding.contentImageView.getId(), item);
        Util.requestImageResize(mContext, holder.dataBinding.contentImageView, place.imageUrl);

        //        if (VersionUtils.isOverAPI16() == true)
        //        {
        //            holder.dataBinding.gradientBottomView.setBackground(mPaintDrawable);
        //        } else
        //        {
        //            holder.dataBinding.gradientBottomView.setBackgroundDrawable(mPaintDrawable);
        //        }

        //        // SOLD OUT 표시
        //        if (place.isSoldOut == true)
        //        {
        //            holder.dataBinding.soldOutView.setVisibility(View.VISIBLE);
        //        } else
        //        {
        //            holder.dataBinding.soldOutView.setVisibility(View.GONE);
        //        }

        holder.dataBinding.contentTextView.setText(place.title);

        Prices prices = place.prices;

        if (prices == null || prices.discountPrice == 0 || mIsUsePriceLayout == false)
        {
            holder.dataBinding.priceLayout.setVisibility(mIsUsePriceLayout == false ? View.GONE : View.INVISIBLE);
            holder.dataBinding.contentOriginPriceView.setText("");
            holder.dataBinding.contentDiscountPriceView.setText("");
            holder.dataBinding.contentPersonView.setText("");
        } else
        {
            holder.dataBinding.priceLayout.setVisibility(View.VISIBLE);

            String strPrice = DailyTextUtils.getPriceFormat(mContext, prices.normalPrice, false);
            String strDiscount = DailyTextUtils.getPriceFormat(mContext, prices.discountPrice, false);

            holder.dataBinding.contentDiscountPriceView.setText(strDiscount);

            if (prices.normalPrice <= 0 || prices.normalPrice <= prices.discountPrice)
            {
                holder.dataBinding.contentOriginPriceView.setText("");
            } else
            {
                holder.dataBinding.contentOriginPriceView.setText(strPrice);
                holder.dataBinding.contentOriginPriceView.setPaintFlags(holder.dataBinding.contentOriginPriceView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
        }

        holder.dataBinding.contentProvinceView.setText(place.regionName);

        if (Constants.ServiceType.HOTEL.name().equalsIgnoreCase(place.serviceType) == true)
        {
            Stay.Grade grade;
            try
            {
                grade = Stay.Grade.valueOf(place.details.grade);
            } catch (Exception e)
            {
                grade = Stay.Grade.etc;
            }

            holder.dataBinding.contentGradeView.setText(grade.getName(mContext));
            holder.dataBinding.contentDotImageView.setVisibility(View.VISIBLE);

            holder.dataBinding.contentPersonView.setText("");
            holder.dataBinding.contentPersonView.setVisibility(View.GONE);
        } else if (Constants.ServiceType.GOURMET.name().equalsIgnoreCase(place.serviceType) == true)
        {
            // grade
            if (DailyTextUtils.isTextEmpty(place.details.category) == true)
            {
                holder.dataBinding.contentGradeView.setVisibility(View.GONE);
                holder.dataBinding.contentDotImageView.setVisibility(View.GONE);
                holder.dataBinding.contentGradeView.setText("");
            } else
            {
                holder.dataBinding.contentGradeView.setVisibility(View.VISIBLE);
                holder.dataBinding.contentDotImageView.setVisibility(View.VISIBLE);
                holder.dataBinding.contentGradeView.setText(place.details.category);
            }

            if (prices != null && place.details.persons > 1)
            {
                holder.dataBinding.contentPersonView.setText(//
                    mContext.getString(R.string.label_home_person_format, place.details.persons));
                holder.dataBinding.contentPersonView.setVisibility(View.VISIBLE);
            } else
            {
                holder.dataBinding.contentPersonView.setText("");
                holder.dataBinding.contentPersonView.setVisibility(View.GONE);
            }
        } else
        {
            // Stay Outbound 의 경우 PlaceType 이 없음
            holder.dataBinding.contentGradeView.setText("");
            holder.dataBinding.contentDotImageView.setVisibility(View.GONE);

            holder.dataBinding.contentPersonView.setText("");
            holder.dataBinding.contentPersonView.setVisibility(View.GONE);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void onBindViewHolderByStay(PlaceViewHolder holder, CarouselListItem item)
    {
        final Stay stay = item.getItem();

        holder.dataBinding.contentImageView.setTag(holder.dataBinding.contentImageView.getId(), item);
        Util.requestImageResize(mContext, holder.dataBinding.contentImageView, stay.imageUrl);

        //        if (VersionUtils.isOverAPI16() == true)
        //        {
        //            holder.dataBinding.gradientBottomView.setBackground(mPaintDrawable);
        //        } else
        //        {
        //            holder.dataBinding.gradientBottomView.setBackgroundDrawable(mPaintDrawable);
        //        }

        //        // SOLD OUT 표시
        //        if (stay.isSoldOut == true)
        //        {
        //            holder.dataBinding.soldOutView.setVisibility(View.VISIBLE);
        //        } else
        //        {
        //            holder.dataBinding.soldOutView.setVisibility(View.GONE);
        //        }

        holder.dataBinding.contentTextView.setText(stay.name);

        int originPrice = stay.price;
        int discountPrice = stay.discountPrice;

        if (originPrice == 0 || discountPrice == 0 || mIsUsePriceLayout == false)
        {
            holder.dataBinding.priceLayout.setVisibility(mIsUsePriceLayout == false ? View.GONE : View.INVISIBLE);
            holder.dataBinding.contentOriginPriceView.setText("");
            holder.dataBinding.contentDiscountPriceView.setText("");
            holder.dataBinding.contentPersonView.setText("");
        } else
        {
            holder.dataBinding.priceLayout.setVisibility(View.VISIBLE);

            String strPrice = DailyTextUtils.getPriceFormat(mContext, originPrice, false);
            String strDiscount = DailyTextUtils.getPriceFormat(mContext, discountPrice, false);

            holder.dataBinding.contentDiscountPriceView.setText(strDiscount);

            if (originPrice <= 0 || originPrice <= discountPrice)
            {
                holder.dataBinding.contentOriginPriceView.setText("");
            } else
            {
                holder.dataBinding.contentOriginPriceView.setText(strPrice);
                holder.dataBinding.contentOriginPriceView.setPaintFlags(holder.dataBinding.contentOriginPriceView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
        }

        holder.dataBinding.contentProvinceView.setText(stay.regionName);

        holder.dataBinding.contentGradeView.setText(stay.getGrade().getName(mContext));
        holder.dataBinding.contentDotImageView.setVisibility(View.VISIBLE);

        holder.dataBinding.contentPersonView.setText("");
        holder.dataBinding.contentPersonView.setVisibility(View.GONE);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void onBindViewHolderByStayOutbound(PlaceViewHolder holder, CarouselListItem item)
    {
        final StayOutbound stayOutbound = item.getItem();

        holder.dataBinding.contentImageView.setTag(holder.dataBinding.contentImageView.getId(), item);
        ImageMap imageMap = stayOutbound.getImageMap();
        String url;

        if (ScreenUtils.getScreenWidth(mContext) >= ScreenUtils.DEFAULT_STAYOUTBOUND_XXHDPI_WIDTH)
        {
            if (DailyTextUtils.isTextEmpty(imageMap.bigUrl) == true)
            {
                url = imageMap.smallUrl;
            } else
            {
                url = imageMap.bigUrl;
            }
        } else
        {
            if (DailyTextUtils.isTextEmpty(imageMap.mediumUrl) == true)
            {
                url = imageMap.smallUrl;
            } else
            {
                url = imageMap.mediumUrl;
            }
        }

        ControllerListener controllerListener = new BaseControllerListener<ImageInfo>()
        {
            @Override
            public void onFailure(String id, Throwable throwable)
            {
                if (throwable instanceof IOException == true)
                {
                    if (url.equalsIgnoreCase(imageMap.bigUrl) == true)
                    {
                        imageMap.bigUrl = null;
                    } else if (url.equalsIgnoreCase(imageMap.mediumUrl) == true)
                    {
                        imageMap.mediumUrl = null;
                    } else
                    {
                        // 작은 이미지를 로딩했지만 실패하는 경우.
                        return;
                    }

                    holder.dataBinding.contentImageView.setImageURI(imageMap.smallUrl);
                }
            }
        };

        DraweeController draweeController = Fresco.newDraweeControllerBuilder()//
            .setControllerListener(controllerListener).setUri(url).build();

        holder.dataBinding.contentImageView.setController(draweeController);

        //        if (VersionUtils.isOverAPI16() == true)
        //        {
        //            holder.dataBinding.gradientBottomView.setBackground(mPaintDrawable);
        //        } else
        //        {
        //            holder.dataBinding.gradientBottomView.setBackgroundDrawable(mPaintDrawable);
        //        }

        //        // SOLD OUT 표시
        //        if (place.isSoldOut == true)
        //        {
        //            holder.dataBinding.soldOutView.setVisibility(View.VISIBLE);
        //        } else
        //        {
        //            holder.dataBinding.soldOutView.setVisibility(View.GONE);
        //        }

        holder.dataBinding.priceLayout.setVisibility(mIsUsePriceLayout == false ? View.GONE : View.INVISIBLE);
        holder.dataBinding.contentOriginPriceView.setText("");
        holder.dataBinding.contentDiscountPriceView.setText("");
        holder.dataBinding.contentPersonView.setText("");

        holder.dataBinding.contentTextView.setText(stayOutbound.name);
        //        holder.dataBinding.nameEngTextView.setText("(" + stayOutbound.nameEng + ")");

        holder.dataBinding.contentProvinceView.setText(stayOutbound.city);

        // Stay Outbound 의 경우 PlaceType 이 없음
        holder.dataBinding.contentGradeView.setText("");
        holder.dataBinding.contentDotImageView.setVisibility(View.GONE);

        holder.dataBinding.contentPersonView.setText("");
        holder.dataBinding.contentPersonView.setVisibility(View.GONE);

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void onBindViewHolderByGourmet(PlaceViewHolder holder, CarouselListItem item)
    {
        final Gourmet gourmet = item.getItem();

        holder.dataBinding.contentImageView.setTag(holder.dataBinding.contentImageView.getId(), item);
        Util.requestImageResize(mContext, holder.dataBinding.contentImageView, gourmet.imageUrl);

        //        if (VersionUtils.isOverAPI16() == true)
        //        {
        //            holder.dataBinding.gradientBottomView.setBackground(mPaintDrawable);
        //        } else
        //        {
        //            holder.dataBinding.gradientBottomView.setBackgroundDrawable(mPaintDrawable);
        //        }

        //        // SOLD OUT 표시
        //        if (place.isSoldOut == true)
        //        {
        //            holder.dataBinding.soldOutView.setVisibility(View.VISIBLE);
        //        } else
        //        {
        //            holder.dataBinding.soldOutView.setVisibility(View.GONE);
        //        }

        holder.dataBinding.contentTextView.setText(gourmet.name);

        int originPrice = gourmet.price;
        int discountPrice = gourmet.discountPrice;

        if (originPrice == 0 || discountPrice == 0 || mIsUsePriceLayout == false)
        {
            holder.dataBinding.priceLayout.setVisibility(mIsUsePriceLayout == false ? View.GONE : View.INVISIBLE);
            holder.dataBinding.contentOriginPriceView.setText("");
            holder.dataBinding.contentDiscountPriceView.setText("");
            holder.dataBinding.contentPersonView.setText("");
        } else
        {
            holder.dataBinding.priceLayout.setVisibility(View.VISIBLE);

            String strPrice = DailyTextUtils.getPriceFormat(mContext, originPrice, false);
            String strDiscount = DailyTextUtils.getPriceFormat(mContext, discountPrice, false);

            holder.dataBinding.contentDiscountPriceView.setText(strDiscount);

            if (originPrice <= 0 || originPrice <= discountPrice)
            {
                holder.dataBinding.contentOriginPriceView.setText("");
            } else
            {
                holder.dataBinding.contentOriginPriceView.setText(strPrice);
                holder.dataBinding.contentOriginPriceView.setPaintFlags(holder.dataBinding.contentOriginPriceView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
        }

        holder.dataBinding.contentProvinceView.setText(gourmet.regionName);

        // grade
        if (DailyTextUtils.isTextEmpty(gourmet.category) == true)
        {
            holder.dataBinding.contentGradeView.setVisibility(View.GONE);
            holder.dataBinding.contentDotImageView.setVisibility(View.GONE);
            holder.dataBinding.contentGradeView.setText("");
        } else
        {
            holder.dataBinding.contentGradeView.setVisibility(View.VISIBLE);
            holder.dataBinding.contentDotImageView.setVisibility(View.VISIBLE);
            holder.dataBinding.contentGradeView.setText(gourmet.category);
        }

        if (gourmet.persons > 1)
        {
            holder.dataBinding.contentPersonView.setText(//
                mContext.getString(R.string.label_home_person_format, gourmet.persons));
            holder.dataBinding.contentPersonView.setVisibility(View.VISIBLE);
        } else
        {
            holder.dataBinding.contentPersonView.setText("");
            holder.dataBinding.contentPersonView.setVisibility(View.GONE);
        }
    }

    public CarouselListItem getItem(int position)
    {
        if (position < 0 || mList.size() <= position)
        {
            return null;
        }

        return mList.get(position);
    }

    public ArrayList<CarouselListItem> getData()
    {
        return mList;
    }

    public void setData(ArrayList<CarouselListItem> list)
    {
        mList = list;
    }

    @Override
    public int getItemCount()
    {
        return mList == null ? 0 : mList.size();
    }

    public void setUsePriceLayout(boolean isUse)
    {
        mIsUsePriceLayout = isUse;
    }

    private void setLayoutMargin(PlaceViewHolder holder, int position)
    {
        if (holder == null)
        {
            return;
        }

        int outSide = ScreenUtils.dpToPx(mContext, 15d);
        int inSide = ScreenUtils.dpToPx(mContext, 12d) / 2;

        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
        params.leftMargin = position == 0 ? outSide : inSide;
        params.rightMargin = position == getItemCount() - 1 ? outSide : inSide;
        holder.itemView.setLayoutParams(params);
    }

    //    private void makeShaderFactory()
    //    {
    //        // 그라디에이션 만들기.
    //        final int colors[] = {Color.parseColor("#ED000000"), Color.parseColor("#E8000000"), Color.parseColor("#E2000000"), Color.parseColor("#66000000"), Color.parseColor("#00000000")};
    //        final float positions[] = {0.0f, 0.01f, 0.02f, 0.17f, 0.38f};
    //
    //        mPaintDrawable = new PaintDrawable();
    //        mPaintDrawable.setShape(new RectShape());
    //
    //        ShapeDrawable.ShaderFactory sf = new ShapeDrawable.ShaderFactory()
    //        {
    //            @Override
    //            public Shader resize(int width, int height)
    //            {
    //                return new LinearGradient(0, height, 0, 0, colors, positions, Shader.TileMode.CLAMP);
    //            }
    //        };
    //
    //        mPaintDrawable.setShaderFactory(sf);
    //    }

    protected class PlaceViewHolder extends RecyclerView.ViewHolder
    {
        ListRowCarouselItemDataBinding dataBinding;

        public PlaceViewHolder(ListRowCarouselItemDataBinding dataBinding)
        {
            super(dataBinding.getRoot());

            this.dataBinding = dataBinding;

            dataBinding.contentImageView.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
            dataBinding.contentImageView.getHierarchy().setPlaceholderImage(R.drawable.layerlist_placeholder);

            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (mItemClickListener == null)
                    {
                        return;
                    }

                    mItemClickListener.onItemClick(v);
                }
            });

            if (Util.supportPreview(mContext) == true)
            {
                itemView.setOnLongClickListener(new View.OnLongClickListener()
                {
                    @Override
                    public boolean onLongClick(View v)
                    {
                        if (mItemClickListener == null)
                        {
                            return false;
                        } else
                        {
                            Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(70);

                            mItemClickListener.onItemLongClick(v);
                            return true;
                        }
                    }
                });
            }
        }
    }
}