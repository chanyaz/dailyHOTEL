package com.twoheart.dailyhotel.screen.information.coupon;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Coupon;
import com.twoheart.dailyhotel.util.CouponUtil;
import com.twoheart.dailyhotel.util.ExLog;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.widget.DailyTextView;
import com.twoheart.dailyhotel.widget.FontManager;

import java.util.List;

/**
 * Created by Sam Lee on 2016. 5. 20..
 */
public class CouponListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_ITEM = 2;

    private static final int HEADER_COUNT = 1;

    private List<Coupon> mList;
    private Context mContext;
    private OnCouponItemListener mListener;

    public interface OnCouponItemListener
    {
        void startNotice();

        void showNotice(View view, int position);

        void onDownloadClick(View view, int position);
    }

    public CouponListAdapter(Context context, List<Coupon> list, OnCouponItemListener listener)
    {
        mContext = context;

        if (list == null)
        {
            throw new IllegalArgumentException("couponList must not be null");
        }

        mList = list;
        mListener = listener;
    }

    /**
     * 쿠폰아이템
     *
     * @param position 실제 포지션에서 -1 된 값(헤더 사이즈 뺀값)
     * @return
     */
    public Coupon getItem(int position)
    {
        if (position > 0)
        {
            position = position - HEADER_COUNT;
        }
        return mList.get(position);
    }

    @Override
    public int getItemCount()
    {
        return mList == null ? 0 : mList.size() + HEADER_COUNT;
    }

    @Override
    public int getItemViewType(int position)
    {
        return (position == 0) ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
    }

    public void setData(List<Coupon> list)
    {
        mList = list;
    }

    public Coupon getCoupon(String userCouponCode)
    {
        if (mList == null)
        {
            return null;
        }

        for (Coupon coupon : mList)
        {
            if (coupon.userCouponCode.equalsIgnoreCase(userCouponCode) == true)
            {
                return coupon;
            }
        }

        return null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        if (viewType == VIEW_TYPE_HEADER)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_couponlist_header, parent, false);
            return new HeaderViewHolder(view);
        } else
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_coupon, parent, false);
            return new ItemViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position)
    {
        int viewType = getItemViewType(position);
        if (viewType == VIEW_TYPE_HEADER)
        {
            // 헤더
            ((HeaderViewHolder) holder).onBindViewHolder();
        } else
        {
            // 리스트 아이템
            ((ItemViewHolder) holder).onBindViewHolder(position);
        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder
    {
        View rootView;
        View listItemLayout;
        TextView couponPriceTextView;
        TextView descriptionTextView;
        TextView expireTextView;
        DailyTextView dueDateTextView;
        TextView minPriceTextView;
        TextView useIconView;
        TextView downloadIconView;
        TextView noticeTextView;

        public ItemViewHolder(View itemView)
        {
            super(itemView);

            rootView = itemView;
            listItemLayout = itemView.findViewById(R.id.listItemLayout);
            couponPriceTextView = (TextView) itemView.findViewById(R.id.couponPriceTextView);
            descriptionTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);
            expireTextView = (TextView) itemView.findViewById(R.id.expireTextView);
            dueDateTextView = (DailyTextView) itemView.findViewById(R.id.dueDateTextView);
            minPriceTextView = (TextView) itemView.findViewById(R.id.minPriceTextView);
            useIconView = (TextView) itemView.findViewById(R.id.useIconView);
            downloadIconView = (TextView) itemView.findViewById(R.id.downloadIconView);
            noticeTextView = (TextView) itemView.findViewById(R.id.noticeTextView);
        }

        public void onBindViewHolder(final int position)
        {
            Coupon coupon = getItem(position);

            String strAmount = Util.getPriceFormat(mContext, coupon.amount, false);
            couponPriceTextView.setText(strAmount);

            descriptionTextView.setText(coupon.title);
            expireTextView.setText(CouponUtil.getAvailableDatesString(coupon.validFrom, coupon.validTo));

            int dueDate = CouponUtil.getDueDateCount(coupon.serverDate, coupon.validTo);
            String strDueDate;

            if (dueDate > 1)
            {
                // 2일 남음 이상
                strDueDate = mContext.getResources().getString(R.string.coupon_duedate_text, dueDate);
            } else
            {
                // 오늘까지
                strDueDate = mContext.getResources().getString(R.string.coupon_today_text);
            }

            if (dueDate > 7)
            {
                // 8일 남음 이상
                dueDateTextView.setTypeface(FontManager.getInstance(mContext).getRegularTypeface());
                dueDateTextView.setTextColor(mContext.getResources().getColor(R.color.coupon_expire_text));

            } else
            {
                // 7일 남음 부터 오늘까지
                dueDateTextView.setTypeface(FontManager.getInstance(mContext).getMediumTypeface());
                dueDateTextView.setTextColor(mContext.getResources().getColor(R.color.coupon_red_wine_text));
            }

            dueDateTextView.setText(strDueDate);

            String lastLineText = "";
            boolean isEmptyStayFromTo = Util.isTextEmpty(coupon.stayFrom, coupon.stayTo);
            boolean isEmptyAmountMinimum = coupon.amountMinimum == 0;

            if (isEmptyAmountMinimum == false)
            {
                lastLineText += mContext.getResources().getString( //
                    isEmptyStayFromTo == false ? R.string.coupon_min_price_short_text : R.string.coupon_min_price_text, //
                    Util.getPriceFormat(mContext, coupon.amountMinimum, false));
            }

            if (isEmptyStayFromTo == false)
            {
                if (isEmptyAmountMinimum == false)
                {
                    lastLineText += ", ";
                }

                lastLineText += CouponUtil.getDateOfStayAvailableString( //
                    mContext, coupon.stayFrom, coupon.stayTo);
            }

            final Typeface typeface = FontManager.getInstance(mContext).getRegularTypeface();
            final float textWidth = Util.getTextWidth(mContext, lastLineText, 11d, typeface);

            if (minPriceTextView.getWidth() <= textWidth) {
                lastLineText = lastLineText.replace(", ", ",\n");
            }

            minPriceTextView.setText(lastLineText);

            if (coupon.isDownloaded == true)
            {
                //useable
                downloadIconView.setVisibility(View.GONE);
                useIconView.setVisibility(View.VISIBLE);
                listItemLayout.setBackgroundResource(R.drawable.more_coupon_bg);
                noticeTextView.setTextColor(mContext.getResources().getColor(R.color.coupon_description_text));
            } else
            {
                //download
                downloadIconView.setVisibility(View.VISIBLE);
                useIconView.setVisibility(View.GONE);
                listItemLayout.setBackgroundResource(R.drawable.more_coupon_download_bg);
                noticeTextView.setTextColor(mContext.getResources().getColor(R.color.white_a80));
            }

            CharSequence charSequence = Util.isTextEmpty(noticeTextView.getText().toString()) ? "" : noticeTextView.getText().toString();
            SpannableString spannableString = new SpannableString(charSequence);
            spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            noticeTextView.setText(spannableString);
            noticeTextView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    mListener.showNotice(v, position);
                }
            });

            downloadIconView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    mListener.onDownloadClick(v, position);
                }
            });
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder
    {
        View rootView;
        DailyTextView noticeView;

        public HeaderViewHolder(View itemView)
        {
            super(itemView);

            rootView = itemView;
            noticeView = (DailyTextView) itemView.findViewById(R.id.couponUseNoticeTextView);
        }

        public void onBindViewHolder()
        {
            SpannableString spannableString = new SpannableString(noticeView.getText());
            spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            noticeView.setText(spannableString);

            noticeView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    mListener.startNotice();
                }
            });
        }
    }

}