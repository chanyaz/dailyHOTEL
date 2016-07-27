package com.twoheart.dailyhotel.screen.gourmet.detail;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.GourmetDetail;
import com.twoheart.dailyhotel.model.ImageInformation;
import com.twoheart.dailyhotel.model.SaleTime;
import com.twoheart.dailyhotel.model.TicketInformation;
import com.twoheart.dailyhotel.place.adapter.PlaceDetailImageViewPagerAdapter;
import com.twoheart.dailyhotel.place.base.OnBaseEventListener;
import com.twoheart.dailyhotel.place.layout.PlaceDetailLayout;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;

import java.util.ArrayList;

public class GourmetDetailLayout extends PlaceDetailLayout
{
    private GourmetDetailListAdapter mListAdapter;
    private TicketInformation mSelectedTicketInformation;

    private GourmetDetailTicketTypeListAdapter mTicketTypeListAdapter;

    public interface OnEventListener extends PlaceDetailLayout.OnEventListener
    {
        void doBooking(TicketInformation ticketInformation);
    }

    public GourmetDetailLayout(Context context, OnBaseEventListener listener)
    {
        super(context, listener);
    }

    @Override
    protected String getProductTypeTitle()
    {
        return mContext.getString(R.string.act_hotel_search_ticket);
    }

    @Override
    protected View getTitleLayout()
    {
        if (mListAdapter == null)
        {
            return null;
        }

        return mListAdapter.getTitleLayout();
    }

    @Override
    protected View getGradeTextView()
    {
        if (mListAdapter == null)
        {
            return null;
        }

        return mListAdapter.getGradeTextView();
    }

    @Override
    protected View getMagicToolbarView()
    {
        if (mListAdapter == null)
        {
            return null;
        }

        return mListAdapter.getMagicToolbarView();
    }

    public void setDetail(SaleTime saleTime, GourmetDetail gourmetDetail, int imagePosition)
    {
        if (gourmetDetail == null)
        {
            return;
        }

        mPlaceDetail = gourmetDetail;

        if (mImageAdapter == null)
        {
            mImageAdapter = new PlaceDetailImageViewPagerAdapter(mContext);
        }

        ArrayList<ImageInformation> imageInformationList = gourmetDetail.getImageInformationList();
        mImageAdapter.setData(imageInformationList);
        mViewPager.setAdapter(mImageAdapter);
        mDailyViewPagerIndicator.setTotalCount(imageInformationList.size());

        if (imageInformationList.size() > 0)
        {
            mDailyViewPagerIndicator.setImageInformation(imageInformationList.get(0).description, 0);
        } else
        {
            mDailyViewPagerIndicator.setImageInformation(null, 0);
        }

        if (mListAdapter == null)
        {
            mListAdapter = new GourmetDetailListAdapter(mContext, saleTime, (GourmetDetail) mPlaceDetail,//
                (GourmetDetailLayout.OnEventListener) mOnEventListener, mEmptyViewOnTouchListener);
            mListView.setAdapter(mListAdapter);
        } else
        {
            mListAdapter.setData((GourmetDetail) mPlaceDetail, saleTime);
        }

        setCurrentImage(imagePosition);

        hideProductInformationLayout();

        // SOLD OUT 판단 조건.
        ArrayList<TicketInformation> ticketInformationList = gourmetDetail.getTicketInformation();

        if (ticketInformationList == null || ticketInformationList.size() == 0)
        {
            mBookingTextView.setVisibility(View.GONE);
            mSoldoutTextView.setVisibility(View.VISIBLE);

            setBookingStatus(STATUS_SOLD_OUT);
        } else
        {
            mBookingTextView.setVisibility(View.VISIBLE);
            mBookingTextView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    switch (mBookingStatus)
                    {
                        case STATUS_BOOKING:
                            ((GourmetDetailLayout.OnEventListener) mOnEventListener).doBooking(mSelectedTicketInformation);
                            break;

                        case STATUS_SELECT_PRODUCT:
                            ((OnEventListener) mOnEventListener).showProductInformationLayout();
                            break;
                    }
                }
            });

            mSoldoutTextView.setVisibility(View.GONE);

            setBookingStatus(STATUS_SELECT_PRODUCT);

            updateTicketInformationLayout(ticketInformationList);
        }

        mListAdapter.notifyDataSetChanged();
    }

    protected void updateTicketInformationLayout(ArrayList<TicketInformation> ticketInformationList)
    {
        if (ticketInformationList == null || ticketInformationList.size() == 0)
        {
            return;
        }

        // 처음 세팅하는 경우 객실 타입 세팅
        if (mTicketTypeListAdapter == null)
        {
            mSelectedTicketInformation = ticketInformationList.get(0);

            mTicketTypeListAdapter = new GourmetDetailTicketTypeListAdapter(mContext, ticketInformationList, new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int position = mProductTypeRecyclerView.getChildAdapterPosition(v);

                    if (position < 0)
                    {
                        return;
                    }

                    mSelectedTicketInformation = mTicketTypeListAdapter.getItem(position);
                    mTicketTypeListAdapter.setSelected(position);
                    mTicketTypeListAdapter.notifyDataSetChanged();

                    AnalyticsManager.getInstance(mContext).recordEvent(AnalyticsManager.Category.GOURMET_BOOKINGS//
                        , AnalyticsManager.Action.TICKET_TYPE_ITEM_CLICKED, mSelectedTicketInformation.name, null);
                }
            });
        } else
        {
            // 재세팅 하는 경우
            mSelectedTicketInformation = ticketInformationList.get(0);

            mTicketTypeListAdapter.addAll(ticketInformationList);
            mTicketTypeListAdapter.setSelected(0);
            mTicketTypeListAdapter.notifyDataSetChanged();
        }

        // 객실 개수로 높이를 재지정해준다.
        int size = ticketInformationList.size();
        int height = Util.dpToPx(mContext, 100) * size;
        final int maxHeight = Util.dpToPx(mContext, 350);
        ViewGroup.LayoutParams layoutParams = mProductTypeRecyclerView.getLayoutParams();

        if (height > maxHeight)
        {
            layoutParams.height = maxHeight;
        } else
        {
            layoutParams.height = height;
        }

        mProductTypeRecyclerView.setLayoutParams(layoutParams);
        mProductTypeRecyclerView.setAdapter(mTicketTypeListAdapter);
    }

    @Override
    public void setBookingStatus(int status)
    {
        mBookingStatus = status;

        switch (status)
        {
            case STATUS_NONE:
            {
                mBookingTextView.setVisibility(View.VISIBLE);
                mSoldoutTextView.setVisibility(View.GONE);
                break;
            }

            case STATUS_SELECT_PRODUCT:
            {
                mBookingTextView.setVisibility(View.VISIBLE);
                mSoldoutTextView.setVisibility(View.GONE);

                mBookingTextView.setText(R.string.act_hotel_search_ticket);
                break;
            }

            case STATUS_BOOKING:
            {
                mBookingTextView.setVisibility(View.VISIBLE);
                mSoldoutTextView.setVisibility(View.GONE);

                mBookingTextView.setText(R.string.act_hotel_booking);
                break;
            }

            case STATUS_SOLD_OUT:
            {
                mBookingTextView.setVisibility(View.GONE);
                mSoldoutTextView.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    @Override
    public void hideAnimationProductInformationLayout()
    {
        super.hideAnimationProductInformationLayout();

        AnalyticsManager.getInstance(mContext).recordEvent(AnalyticsManager.Category.GOURMET_BOOKINGS//
            , AnalyticsManager.Action.TICKET_TYPE_CANCEL_CLICKED, mPlaceDetail.name, null);
    }
}