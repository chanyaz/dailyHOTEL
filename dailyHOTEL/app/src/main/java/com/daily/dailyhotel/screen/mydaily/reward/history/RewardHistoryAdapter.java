package com.daily.dailyhotel.screen.mydaily.reward.history;

import android.annotation.TargetApi;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daily.base.util.ScreenUtils;
import com.daily.dailyhotel.entity.ObjectItem;
import com.daily.dailyhotel.entity.RewardHistory;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.databinding.LayoutRewardHistoryDataBinding;
import com.twoheart.dailyhotel.databinding.LayoutRewardHistoryFooterDataBinding;
import com.twoheart.dailyhotel.databinding.LayoutRewardHistoryHeaderDataBinding;
import com.twoheart.dailyhotel.util.DailyCalendar;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RewardHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    Context mContext;
    private List<ObjectItem> mList;
    private OnEventListener mOnEventListener;

    public interface OnEventListener
    {
        void onClick(View view);

        void onHomeClick();
    }

    public RewardHistoryAdapter(Context context)
    {
        mContext = context;

        mList = new ArrayList<>();
    }

    public void setOnClickListener(OnEventListener listener)
    {
        mOnEventListener = listener;
    }

    public void clear()
    {
        mList.clear();
    }

    public void add(ObjectItem objectItem)
    {
        mList.add(objectItem);
    }

    public void add(int position, ObjectItem placeViewItem)
    {
        if (position >= 0 && position < mList.size())
        {
            mList.add(position, placeViewItem);
        }
    }

    public void addAll(Collection<? extends ObjectItem> collection)
    {
        if (collection == null)
        {
            return;
        }

        mList.addAll(collection);
    }

    public void setAll(Collection<? extends ObjectItem> collection)
    {
        clear();
        addAll(collection);
    }

    public void remove(int position)
    {
        if (mList == null || mList.size() <= position)
        {
            return;
        }

        mList.remove(position);
    }

    public ObjectItem getItem(int position)
    {
        if (position < 0 || mList.size() <= position)
        {
            return null;
        }

        return mList.get(position);
    }

    @Override
    public int getItemViewType(int position)
    {
        return mList.get(position).mType;
    }

    @Override
    public int getItemCount()
    {
        if (mList == null)
        {
            return 0;
        }

        return mList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        switch (viewType)
        {
            case ObjectItem.TYPE_HEADER_VIEW:
            {
                LayoutRewardHistoryHeaderDataBinding dataBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.layout_reward_history_header_data, parent, false);

                return new BaseDataBindingViewHolder(dataBinding);
            }

            case ObjectItem.TYPE_ENTRY:
            {
                LayoutRewardHistoryDataBinding viewDataBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.layout_reward_history_data, parent, false);

                return new HistoryViewHolder(viewDataBinding);
            }

            case ObjectItem.TYPE_FOOTER_VIEW:
            {
                LayoutRewardHistoryFooterDataBinding viewDataBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.layout_reward_history_footer_data, parent, false);

                return new FooterViewHolder(viewDataBinding);
            }
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        ObjectItem item = getItem(position);

        if (item == null)
        {
            return;
        }

        switch (item.mType)
        {
            case ObjectItem.TYPE_ENTRY:
                onBindViewHolder((HistoryViewHolder) holder, item);
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void onBindViewHolder(HistoryViewHolder holder, ObjectItem objectItem)
    {
        if (holder == null || objectItem == null)
        {
            return;
        }

        final String DATE_FORMAT = "yyyy.MM.dd(EEE)";

        RewardHistory rewardHistory = objectItem.getItem();

        switch (rewardHistory.type)
        {
            // 쿠폰 발행
            case A:
            {
                //
                final int DP_16 = ScreenUtils.dpToPx(mContext, 16);
                final int DP_18 = ScreenUtils.dpToPx(mContext, 18);
                holder.dataBinding.descriptionLayout.setPadding(0, DP_16, 0, DP_18);

                holder.dataBinding.rewardImageView.setVectorImageResource(R.drawable.vector_ic_reward_history_coupon);

                holder.dataBinding.titleTextView.setVisibility(View.VISIBLE);
                holder.dataBinding.titleTextView.setText("90,000원");

                final int DP_4 = ScreenUtils.dpToPx(mContext, 4);
                holder.dataBinding.descriptionTextView.setPadding(0, DP_4, 0, 0);
                holder.dataBinding.descriptionTextView.setText("91박 무료 리워드 쿠폰이 발행되었습니다. \n" + "쿠폰함에서 확인해보세요!");

                try
                {
                    holder.dataBinding.dateTextView.setText(mContext.getString(R.string.label_reward_coupon_issue_date, DailyCalendar.convertDateFormatString("2017-10-20T17:32:22+09:00", DailyCalendar.ISO_8601_FORMAT, DATE_FORMAT)));
                } catch (ParseException e)
                {

                }

                holder.dataBinding.reservationLinkTextView.setVisibility(View.GONE);
                break;
            }

            // 스티커 적립
            case B:
            {
                final int DP_16 = ScreenUtils.dpToPx(mContext, 16);
                final int DP_18 = ScreenUtils.dpToPx(mContext, 18);
                holder.dataBinding.descriptionLayout.setPadding(0, DP_16, 0, DP_18);

                holder.dataBinding.rewardImageView.setVectorImageResource(R.drawable.vector_ic_reward_history_coupon);

                holder.dataBinding.titleTextView.setVisibility(View.VISIBLE);
                holder.dataBinding.titleTextView.setText("홀리데이 인 익스프레스 싱가포르 오차드 호텔 리쿠프트");

                final int DP_4 = ScreenUtils.dpToPx(mContext, 4);
                holder.dataBinding.descriptionTextView.setPadding(0, DP_4, 0, 0);
                holder.dataBinding.descriptionTextView.setText(R.string.message_reward_issue_sticker);

                try
                {
                    holder.dataBinding.dateTextView.setText(mContext.getString(R.string.label_reward_payment_deposit, DailyCalendar.convertDateFormatString("2017-10-20T17:32:22+09:00", DailyCalendar.ISO_8601_FORMAT, DATE_FORMAT)));
                } catch (ParseException e)
                {

                }

                final String linkText = mContext.getString(R.string.label_reward_view_reservation);

                SpannableString spannableString = new SpannableString(linkText);
                spannableString.setSpan(new UnderlineSpan(), 0, linkText.length(), 0);
                holder.dataBinding.reservationLinkTextView.setVisibility(View.VISIBLE);
                holder.dataBinding.reservationLinkTextView.setText(spannableString);
                break;
            }

            // 스티커 만료
            case C:
            {
                holder.dataBinding.descriptionLayout.setPadding(0, 0, 0, 0);

                holder.dataBinding.rewardImageView.setVectorImageResource(R.drawable.vector_ic_reward_history_expired);
                holder.dataBinding.titleTextView.setVisibility(View.GONE);

                holder.dataBinding.descriptionTextView.setPadding(0, 0, 0, 0);
                holder.dataBinding.descriptionTextView.setText("보유하신 스티커 N개가 유효기간 만료로\n소멸되었습니다.");

                try
                {
                    holder.dataBinding.dateTextView.setText(mContext.getString(R.string.label_reward_sticker_expiration_date, DailyCalendar.convertDateFormatString("2017-10-20T17:32:22+09:00", DailyCalendar.ISO_8601_FORMAT, DATE_FORMAT)));
                } catch (ParseException e)
                {

                }

                holder.dataBinding.reservationLinkTextView.setVisibility(View.GONE);
                break;
            }

            // 스티커 선물
            case D:
            {
                holder.dataBinding.descriptionLayout.setPadding(0, 0, 0, 0);

                holder.dataBinding.rewardImageView.setVectorImageResource(R.drawable.vector_ic_reward_history_expired);
                holder.dataBinding.titleTextView.setVisibility(View.GONE);

                holder.dataBinding.descriptionTextView.setPadding(0, 0, 0, 0);
                holder.dataBinding.descriptionTextView.setText(R.string.message_reward_issue_sticker);

                try
                {
                    holder.dataBinding.dateTextView.setText(mContext.getString(R.string.label_reward_sticker_issue_date, DailyCalendar.convertDateFormatString("2017-10-20T17:32:22+09:00", DailyCalendar.ISO_8601_FORMAT, DATE_FORMAT)));
                } catch (ParseException e)
                {

                }

                holder.dataBinding.reservationLinkTextView.setVisibility(View.GONE);
                break;
            }
        }
    }

    private class HistoryViewHolder extends RecyclerView.ViewHolder
    {
        LayoutRewardHistoryDataBinding dataBinding;

        public HistoryViewHolder(LayoutRewardHistoryDataBinding dataBinding)
        {
            super(dataBinding.getRoot());

            this.dataBinding = dataBinding;

            dataBinding.getRoot().setOnClickListener(v ->
            {
                if (mOnEventListener != null)
                {
                    mOnEventListener.onClick(v);
                }
            });
        }
    }

    private class FooterViewHolder extends RecyclerView.ViewHolder
    {
        public FooterViewHolder(LayoutRewardHistoryFooterDataBinding dataBinding)
        {
            super(dataBinding.getRoot());

            dataBinding.homeImageView.setOnClickListener(v ->
            {
                if (mOnEventListener != null)
                {
                    mOnEventListener.onHomeClick();
                }
            });
        }
    }

    private class BaseDataBindingViewHolder extends RecyclerView.ViewHolder
    {
        public BaseDataBindingViewHolder(ViewDataBinding dataBinding)
        {
            super(dataBinding.getRoot());
        }
    }
}
