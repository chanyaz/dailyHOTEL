package com.twoheart.dailyhotel.screen.gourmet.preview;

import android.content.Context;
import android.view.View;

import com.daily.base.util.DailyTextUtils;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.GourmetDetail;
import com.twoheart.dailyhotel.model.time.GourmetBookingDay;
import com.twoheart.dailyhotel.network.model.GourmetDetailParams;
import com.twoheart.dailyhotel.network.model.GourmetProduct;
import com.twoheart.dailyhotel.place.base.OnBaseEventListener;
import com.twoheart.dailyhotel.place.layout.PlacePreviewLayout;

/**
 * 호텔 상세 정보 화면
 *
 * @author sheldon
 */
public class GourmetPreviewLayout extends PlacePreviewLayout implements View.OnClickListener
{
    public GourmetPreviewLayout(Context context, OnBaseEventListener listener)
    {
        super(context, listener);
    }

    public void setGrade(String grade)
    {
        if (DailyTextUtils.isTextEmpty(grade) == true)
        {
            mPlaceGradeTextView.setVisibility(View.INVISIBLE);
            return;
        }

        // 등급
        mPlaceGradeTextView.setVisibility(View.VISIBLE);
        mPlaceGradeTextView.setText(grade);
    }

    public void setSubGrade(String subGrade)
    {
        if (DailyTextUtils.isTextEmpty(subGrade) == true)
        {
            mPlaceSubGradeTextView.setVisibility(View.INVISIBLE);
            return;
        }

        // 등급
        mPlaceSubGradeTextView.setVisibility(View.VISIBLE);
        mPlaceSubGradeTextView.setText(subGrade);
    }

    protected void updateLayout(GourmetBookingDay gourmetBookingDay, GourmetDetail gourmetDetail, int reviewCount, boolean changedPrice, boolean soldOut)
    {
        if (gourmetBookingDay == null || gourmetDetail == null)
        {
            return;
        }

        GourmetDetailParams gourmetDetailParams = gourmetDetail.getGourmetDetailParmas();

        if (gourmetDetailParams == null)
        {
            return;
        }

        updateImageLayout(gourmetDetailParams.getImageList());

        if (soldOut == true)
        {
            mBookingTextView.setText(R.string.label_booking_view_detail);
        } else
        {
            mBookingTextView.setText(R.string.label_preview_booking);
        }

        // 가격
        if (soldOut == true || changedPrice == true)
        {
            mProductCountTextView.setText(R.string.message_preview_changed_price);

            mPriceTextView.setVisibility(View.GONE);
            mStayAverageView.setVisibility(View.GONE);
        } else
        {
            // N개의 메뉴타입
            mProductCountTextView.setText(mContext.getString(R.string.label_detail_gourmet_product_count, gourmetDetailParams.getProductList().size()));
            mStayAverageView.setVisibility(View.GONE);

            int minPrice = Integer.MAX_VALUE;
            int maxPrice = Integer.MIN_VALUE;

            for (GourmetProduct gourmetProduct : gourmetDetailParams.getProductList())
            {
                if (minPrice > gourmetProduct.discountPrice)
                {
                    minPrice = gourmetProduct.discountPrice;
                }

                if (maxPrice < gourmetProduct.discountPrice)
                {
                    maxPrice = gourmetProduct.discountPrice;
                }
            }

            String priceFormat;

            if (minPrice == maxPrice)
            {
                priceFormat = DailyTextUtils.getPriceFormat(mContext, maxPrice, false);
            } else
            {
                priceFormat = DailyTextUtils.getPriceFormat(mContext, minPrice, false) + " ~ " + DailyTextUtils.getPriceFormat(mContext, maxPrice, false);
            }

            mPriceTextView.setText(priceFormat);
        }

        updateMoreInformation(reviewCount, gourmetDetailParams.wishCount);
        updateBottomLayout(gourmetDetailParams.myWish);
    }
}