package com.twoheart.dailyhotel.util;

import android.content.Context;

import com.kakao.kakaolink.AppActionBuilder;
import com.kakao.kakaolink.AppActionInfoBuilder;
import com.kakao.kakaolink.KakaoLink;
import com.kakao.kakaolink.KakaoTalkLinkMessageBuilder;
import com.kakao.util.KakaoParameterException;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.SaleTime;

import java.net.URLEncoder;
import java.util.Date;
import java.util.TimeZone;

public class KakaoLinkManager implements Constants
{
    private KakaoLink mKakaoLink;
    private Context mContext;

    private KakaoLinkManager(Context context)
    {
        try
        {
            mContext = context;
            mKakaoLink = KakaoLink.getKakaoLink(mContext);
        } catch (KakaoParameterException e)
        {
            ExLog.e(e.toString());
        }
    }

    public static KakaoLinkManager newInstance(Context context)
    {
        return new KakaoLinkManager(context);
    }

    public void sendInviteKakaoLink(String text, String recommendCode)
    {
        final String URL = "https://app.adjust.com/lkhiuk?campaign=referral-in_app&adgroup=invite_friend&creative=app_download&deep_link=dailyhotel%3A%2F%2Fdailyhotel.co.kr%3Fvc%3D6%26v%3Dsu%26rc%3D" + recommendCode;

        try
        {
            KakaoTalkLinkMessageBuilder messageBuilder = mKakaoLink.createKakaoTalkLinkMessageBuilder();
            messageBuilder.addImage("http://img.dailyhotel.me/app_static/kakao01.jpg", 300, 400);
            messageBuilder.addText(text);
            messageBuilder.addWebButton(mContext.getString(R.string.kakao_btn_invited_friend), URL);
            mKakaoLink.sendMessage(messageBuilder, mContext);
        } catch (KakaoParameterException e)
        {
            ExLog.e(e.toString());
        }
    }

    public void shareHotel(String name, String hotelName, String address, int hotelIndex, String imageUrl, SaleTime checkInSaleTime, int nights)
    {
        try
        {
            KakaoTalkLinkMessageBuilder messageBuilder = mKakaoLink.createKakaoTalkLinkMessageBuilder();

            String date = checkInSaleTime.getDayOfDaysDateFormat("yyyyMMdd");
            String schemeParams = String.format("vc=5&v=hd&i=%d&d=%s&n=%d", hotelIndex, date, nights);

            messageBuilder.addAppButton(mContext.getString(R.string.kakao_btn_go_hotel), //
                new AppActionBuilder().addActionInfo(AppActionInfoBuilder.createAndroidActionInfoBuilder().setExecuteParam(schemeParams).build())//
                    .addActionInfo(AppActionInfoBuilder.createiOSActionInfoBuilder().setExecuteParam(schemeParams).build()).build());

            Date checkInDate = checkInSaleTime.getDayOfDaysDate();
            Date checkOutDate = new Date(checkInSaleTime.getDayOfDaysDate().getTime() + SaleTime.MILLISECOND_IN_A_DAY * nights);

            String text = mContext.getString(R.string.kakao_btn_share_hotel, name, hotelName//
                , DailyCalendar.format(checkInDate.getTime(), "yyyy.MM.dd(EEE)", TimeZone.getTimeZone("GMT"))//
                , DailyCalendar.format(checkOutDate.getTime(), "yyyy.MM.dd(EEE)", TimeZone.getTimeZone("GMT"))//
                , nights, nights + 1, address);

            if (Util.isTextEmpty(imageUrl) == false)
            {
                int lastSlash = imageUrl.lastIndexOf('/');
                String fileName = imageUrl.substring(lastSlash + 1);
                messageBuilder.addImage(imageUrl.substring(0, lastSlash + 1) + URLEncoder.encode(fileName), 300, 200);
            }

            messageBuilder.addText(text);

            mKakaoLink.sendMessage(messageBuilder, mContext);
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    public void shareBookingStay(String message, int stayIndex, String imageUrl, String checkInDate, int nights)
    {
        try
        {
            KakaoTalkLinkMessageBuilder messageBuilder = mKakaoLink.createKakaoTalkLinkMessageBuilder();

            String schemeParams = String.format("vc=5&v=hd&i=%d&d=%s&n=%d", stayIndex, checkInDate, nights);

            messageBuilder.addAppButton(mContext.getString(R.string.label_kakao_reservation_stay), //
                new AppActionBuilder().addActionInfo(AppActionInfoBuilder.createAndroidActionInfoBuilder().setExecuteParam(schemeParams).build())//
                    .addActionInfo(AppActionInfoBuilder.createiOSActionInfoBuilder().setExecuteParam(schemeParams).build()).build());

            if (Util.isTextEmpty(imageUrl) == false)
            {
                int lastSlash = imageUrl.lastIndexOf('/');
                String fileName = imageUrl.substring(lastSlash + 1);
                messageBuilder.addImage(imageUrl.substring(0, lastSlash + 1) + URLEncoder.encode(fileName), 300, 200);
            }

            messageBuilder.addText(message);

            mKakaoLink.sendMessage(messageBuilder, mContext);
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    public void shareGourmet(String name, String placeName, String address, int index, String imageUrl, SaleTime saleTime)
    {
        try
        {
            KakaoTalkLinkMessageBuilder messageBuilder = mKakaoLink.createKakaoTalkLinkMessageBuilder();

            String date = saleTime.getDayOfDaysDateFormat("yyyyMMdd");
            String schemeParams = String.format("vc=5&v=gd&i=%d&d=%s", index, date);

            messageBuilder.addAppButton(mContext.getString(R.string.kakao_btn_go_fnb)//
                , new AppActionBuilder().addActionInfo(AppActionInfoBuilder.createAndroidActionInfoBuilder()//
                    .setExecuteParam(schemeParams).build())//
                    .addActionInfo(AppActionInfoBuilder.createiOSActionInfoBuilder().setExecuteParam(schemeParams).build()).build());

            Date checkInDate = saleTime.getDayOfDaysDate();
            String text = mContext.getString(R.string.kakao_btn_share_fnb, name, placeName//
                , DailyCalendar.format(checkInDate.getTime(), "yyyy.MM.dd(EEE)", TimeZone.getTimeZone("GMT")), address);

            if (Util.isTextEmpty(imageUrl) == false)
            {
                int lastSlash = imageUrl.lastIndexOf('/');
                String fileName = imageUrl.substring(lastSlash + 1);
                messageBuilder.addImage(imageUrl.substring(0, lastSlash + 1) + URLEncoder.encode(fileName), 300, 200);
            }

            messageBuilder.addText(text);

            mKakaoLink.sendMessage(messageBuilder, mContext);
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    public void shareBookingGourmet(String message, int index, String imageUrl, String reservationDate)
    {
        try
        {
            KakaoTalkLinkMessageBuilder messageBuilder = mKakaoLink.createKakaoTalkLinkMessageBuilder();

            String schemeParams = String.format("vc=5&v=gd&i=%d&d=%s", index, reservationDate);

            messageBuilder.addAppButton(mContext.getString(R.string.label_kakao_reservation_gourmet)//
                , new AppActionBuilder().addActionInfo(AppActionInfoBuilder.createAndroidActionInfoBuilder()//
                    .setExecuteParam(schemeParams).build())//
                    .addActionInfo(AppActionInfoBuilder.createiOSActionInfoBuilder().setExecuteParam(schemeParams).build()).build());

            if (Util.isTextEmpty(imageUrl) == false)
            {
                int lastSlash = imageUrl.lastIndexOf('/');
                String fileName = imageUrl.substring(lastSlash + 1);
                messageBuilder.addImage(imageUrl.substring(0, lastSlash + 1) + URLEncoder.encode(fileName), 300, 200);
            }

            messageBuilder.addText(message);

            mKakaoLink.sendMessage(messageBuilder, mContext);
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }
}
