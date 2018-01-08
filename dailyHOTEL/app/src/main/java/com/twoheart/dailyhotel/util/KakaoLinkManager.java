package com.twoheart.dailyhotel.util;

import android.content.Context;

import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ExLog;
import com.daily.dailyhotel.entity.GourmetBookDateTime;
import com.daily.dailyhotel.entity.StayBookDateTime;
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ButtonObject;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.FeedTemplate;
import com.kakao.message.template.LinkObject;
import com.kakao.message.template.LocationTemplate;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.time.GourmetBookingDay;
import com.twoheart.dailyhotel.model.time.StayBookingDay;

import java.net.URLEncoder;
import java.util.Locale;

public class KakaoLinkManager implements Constants
{
    private Context mContext;
    private KakaoLinkService mKakaoLinkService;

    private KakaoLinkManager(Context context)
    {
        mContext = context;
        mKakaoLinkService = KakaoLinkService.getInstance();
    }

    public static KakaoLinkManager newInstance(Context context)
    {
        return new KakaoLinkManager(context);
    }

    //    @Deprecated
    //    public void sendInviteKakaoLink(String text, String recommendCode)
    //    {
    //        final String URL = "https://app.adjust.com/lkhiuk?campaign=referral-in_app&adgroup=invite_friend&creative=app_download&deep_link=dailyhotel%3A%2F%2Fdailyhotel.co.kr%3Fvc%3D6%26v%3Dsu%26rc%3D" + recommendCode;
    //
    //        try
    //        {
    //            KakaoTalkLinkMessageBuilder messageBuilder = mKakaoLink.createKakaoTalkLinkMessageBuilder();
    //            messageBuilder.addImage("http://img.dailyhotel.me/app_static/kakao01.jpg", 300, 400);
    //            messageBuilder.addText(text);
    //            messageBuilder.addWebButton(mContext.getString(R.string.kakao_btn_invited_friend), URL);
    //            mKakaoLink.sendMessage(messageBuilder, mContext);
    //        } catch (KakaoParameterException e)
    //        {
    //            ExLog.e(e.toString());
    //        }
    //    }

    public void shareStay(String name, String hotelName, String address, int hotelIndex, String imageUrl, StayBookingDay stayBookingDay)
    {
        try
        {
            String checkInDay = stayBookingDay.getCheckInDay("yyyyMMdd");
            int nights = stayBookingDay.getNights();
            String schemeParams = String.format(Locale.KOREA, "vc=5&v=hd&i=%d&d=%s&n=%d", hotelIndex, checkInDay, nights);
            String text = mContext.getString(R.string.kakao_btn_share_hotel, name, hotelName//
                , stayBookingDay.getCheckInDay("yyyy.MM.dd(EEE)"), stayBookingDay.getCheckOutDay("yyyy.MM.dd(EEE)"), nights, nights + 1, address);

            String kakaoImageUrl = null;
            if (DailyTextUtils.isTextEmpty(imageUrl) == false)
            {
                int lastSlash = imageUrl.lastIndexOf('/');
                String fileName = imageUrl.substring(lastSlash + 1);
                kakaoImageUrl = imageUrl.substring(0, lastSlash + 1) + URLEncoder.encode(fileName);
            }

            LocationTemplate params = LocationTemplate.newBuilder(address, //
                ContentObject.newBuilder(hotelName, //
                    kakaoImageUrl, //
                    LinkObject.newBuilder() //
                        .setWebUrl("https://mobile.dailyhotel.co.kr/stay/" + hotelIndex) //
                        .setMobileWebUrl("https://mobile.dailyhotel.co.kr/stay/" + hotelIndex) //
                        .setAndroidExecutionParams(schemeParams) //
                        .setIosExecutionParams(schemeParams) //
                        .build()) //
                    .setDescrption(text) //
                    .build()) //
                .addButton(new ButtonObject(mContext.getString(R.string.label_kakao_mobile_app), LinkObject.newBuilder() //
                    .setWebUrl("https://mobile.dailyhotel.co.kr/stay/" + hotelIndex) //
                    .setMobileWebUrl("https://mobile.dailyhotel.co.kr/stay/" + hotelIndex) //
                    .setAndroidExecutionParams(schemeParams) //
                    .setIosExecutionParams(schemeParams) //
                    .build())) //
                .setAddressTitle(hotelName) //
                .build();

            mKakaoLinkService.sendDefault(mContext, params, new ResponseCallback<KakaoLinkResponse>()
            {
                @Override
                public void onFailure(ErrorResult errorResult)
                {
                    ExLog.e(errorResult.toString());
                }

                @Override
                public void onSuccess(KakaoLinkResponse result)
                {

                }
            });
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    public void shareStay(String name, String stayName, String address, int stayIndex, String imageUrl, StayBookDateTime stayBookDateTime)
    {
        try
        {
            String checkInDay = stayBookDateTime.getCheckInDateTime("yyyyMMdd");
            int nights = stayBookDateTime.getNights();
            String schemeParams = String.format(Locale.KOREA, "vc=5&v=hd&i=%d&d=%s&n=%d", stayIndex, checkInDay, nights);

            String text = mContext.getString(R.string.kakao_btn_share_hotel, name, stayName//
                , stayBookDateTime.getCheckInDateTime("yyyy.MM.dd(EEE)"), stayBookDateTime.getCheckOutDateTime("yyyy.MM.dd(EEE)"), nights, nights + 1, address);

            String kakaoImageUrl = null;
            if (DailyTextUtils.isTextEmpty(imageUrl) == false)
            {
                int lastSlash = imageUrl.lastIndexOf('/');
                String fileName = imageUrl.substring(lastSlash + 1);
                kakaoImageUrl = imageUrl.substring(0, lastSlash + 1) + URLEncoder.encode(fileName);
            }

            LocationTemplate params = LocationTemplate.newBuilder(address, //
                ContentObject.newBuilder(stayName, //
                    kakaoImageUrl, //
                    LinkObject.newBuilder() //
                        .setWebUrl("https://mobile.dailyhotel.co.kr/stay/" + stayIndex) //
                        .setMobileWebUrl("https://mobile.dailyhotel.co.kr/stay/" + stayIndex) //
                        .setAndroidExecutionParams(schemeParams) //
                        .setIosExecutionParams(schemeParams) //
                        .build()) //
                    .setDescrption(text) //
                    .build()) //
                .addButton(new ButtonObject(mContext.getString(R.string.label_kakao_mobile_app), LinkObject.newBuilder() //
                    .setWebUrl("https://mobile.dailyhotel.co.kr/stay/" + stayIndex) //
                    .setMobileWebUrl("https://mobile.dailyhotel.co.kr/stay/" + stayIndex) //
                    .setAndroidExecutionParams(schemeParams) //
                    .setIosExecutionParams(schemeParams) //
                    .build())) //
                .setAddressTitle(stayName) //
                .build();

            mKakaoLinkService.sendDefault(mContext, params, new ResponseCallback<KakaoLinkResponse>()
            {
                @Override
                public void onFailure(ErrorResult errorResult)
                {
                    ExLog.e(errorResult.toString());
                }

                @Override
                public void onSuccess(KakaoLinkResponse result)
                {

                }
            });
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    //    public void shareStayOutbound(String name, String hotelName, String englishName, String address, double latitude, double longitude
    //        , int hotelIndex, String imageUrl, StayBookDateTime stayBookDateTime)
    public void shareStayOutbound(String name, String stayName, String address, int stayIndex, String imageUrl, StayBookDateTime stayBookDateTime)
    {
        if (DailyTextUtils.isTextEmpty(name, stayName, address) == true || stayBookDateTime == null)
        {
            return;
        }

        try
        {
            String checkInDay = stayBookDateTime.getCheckInDateTime("yyyyMMdd");
            int nights = stayBookDateTime.getNights();
            String schemeParams = String.format(Locale.KOREA, "vc=20&v=pd&pt=stayOutbound&i=%d&d=%s&n=%d", stayIndex, checkInDay, nights);

            String text = mContext.getString(R.string.kakao_btn_share_stay_outbound, name, stayName//
                , stayBookDateTime.getCheckInDateTime("yyyy.MM.dd(EEE)"), stayBookDateTime.getCheckOutDateTime("yyyy.MM.dd(EEE)"), nights, nights + 1, address);

            String kakaoImageUrl = null;
            if (DailyTextUtils.isTextEmpty(imageUrl) == false)
            {
                int lastSlash = imageUrl.lastIndexOf('/');
                String fileName = imageUrl.substring(lastSlash + 1);
                kakaoImageUrl = imageUrl.substring(0, lastSlash + 1) + URLEncoder.encode(fileName);
            }

            // 명칭은 띄어쓰기는 '+' 로 연결 - 호텔 컬리넌 개포 ==> 호텔+컬리넌+개포  ,   lat, long , 지도레벨z 보통 17 구글에게 실망해서 안 넣기로 함
            // https://www.google.com/maps/search/데님호텔/@37.4761828,127.0466549,17z
            // String changePlaceName = englishName.replaceAll(" ", "\\+");
            // ExLog.d("sam : " + hotelName +" , " + changePlaceName);
            // String locationUrl = "https://www.google.com/maps/search/" + changePlaceName + "/@" + latitude + "," + longitude + ",17z";

            FeedTemplate params = FeedTemplate //
                .newBuilder(ContentObject.newBuilder(stayName, //
                    kakaoImageUrl, //
                    LinkObject.newBuilder() //
                        .setAndroidExecutionParams(schemeParams) //
                        .setIosExecutionParams(schemeParams) //
                        .build()) //
                    .setDescrption(text) //
                    .build()) //
                .addButton(new ButtonObject(mContext.getString(R.string.label_kakao_mobile_app), LinkObject.newBuilder() //
                    .setAndroidExecutionParams(schemeParams) //
                    .setIosExecutionParams(schemeParams) //
                    .build())) //
                .build();

            mKakaoLinkService.sendDefault(mContext, params, new ResponseCallback<KakaoLinkResponse>()
            {
                @Override
                public void onFailure(ErrorResult errorResult)
                {
                    ExLog.e(errorResult.toString());
                }

                @Override
                public void onSuccess(KakaoLinkResponse result)
                {
                }
            });
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    public void shareBookingStay(String message, String stayName, String address, int stayIndex, String imageUrl, String checkInDate, int nights)
    {
        try
        {
            String schemeParams = String.format(Locale.KOREA, "vc=5&v=hd&i=%d&d=%s&n=%d", stayIndex, checkInDate, nights);

            String kakaoImageUrl = null;
            if (DailyTextUtils.isTextEmpty(imageUrl) == false)
            {
                int lastSlash = imageUrl.lastIndexOf('/');
                String fileName = imageUrl.substring(lastSlash + 1);
                kakaoImageUrl = imageUrl.substring(0, lastSlash + 1) + URLEncoder.encode(fileName);
            }

            LocationTemplate params = LocationTemplate.newBuilder(address, //
                ContentObject.newBuilder(stayName, //
                    kakaoImageUrl, //
                    LinkObject.newBuilder() //
                        .setWebUrl("https://mobile.dailyhotel.co.kr/stay/" + stayIndex) //
                        .setMobileWebUrl("https://mobile.dailyhotel.co.kr/stay/" + stayIndex) //
                        .setAndroidExecutionParams(schemeParams) //
                        .setIosExecutionParams(schemeParams) //
                        .build()) //
                    .setDescrption(message) //
                    .build()) //
                .addButton(new ButtonObject(mContext.getString(R.string.label_kakao_mobile_app), LinkObject.newBuilder() //
                    .setWebUrl("https://mobile.dailyhotel.co.kr/stay/" + stayIndex) //
                    .setMobileWebUrl("https://mobile.dailyhotel.co.kr/stay/" + stayIndex) //
                    .setAndroidExecutionParams(schemeParams) //
                    .setIosExecutionParams(schemeParams) //
                    .build())) //
                .setAddressTitle(stayName) //
                .build();

            mKakaoLinkService.sendDefault(mContext, params, new ResponseCallback<KakaoLinkResponse>()
            {
                @Override
                public void onFailure(ErrorResult errorResult)
                {
                    ExLog.e(errorResult.toString());
                }

                @Override
                public void onSuccess(KakaoLinkResponse result)
                {

                }
            });
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    public void shareBookingStayOutbound(String message, String stayName, String address, int stayIndex, String imageUrl, String checkInDate, int nights)
    {
        try
        {
            String schemeParams = String.format(Locale.KOREA, "vc=20&v=pd&pt=stayOutbound&i=%d&d=%s&n=%d", stayIndex, checkInDate, nights);

            String kakaoImageUrl = null;
            if (DailyTextUtils.isTextEmpty(imageUrl) == false)
            {
                int lastSlash = imageUrl.lastIndexOf('/');
                String fileName = imageUrl.substring(lastSlash + 1);
                kakaoImageUrl = imageUrl.substring(0, lastSlash + 1) + URLEncoder.encode(fileName);
            }

            // 명칭은 띄어쓰기는 '+' 로 연결 - 호텔 컬리넌 개포 ==> 호텔+컬리넌+개포  ,   lat, long , 지도레벨z 보통 17 구글에게 실망해서 안 넣기로 함
            // https://www.google.com/maps/search/데님호텔/@37.4761828,127.0466549,17z
            // String changePlaceName = englishName.replaceAll(" ", "\\+");
            // ExLog.d("sam : " + hotelName +" , " + changePlaceName);
            // String locationUrl = "https://www.google.com/maps/search/" + changePlaceName + "/@" + latitude + "," + longitude + ",17z";

            FeedTemplate params = FeedTemplate //
                .newBuilder(ContentObject.newBuilder(stayName, //
                    kakaoImageUrl, //
                    LinkObject.newBuilder() //
                        .setAndroidExecutionParams(schemeParams) //
                        .setIosExecutionParams(schemeParams) //
                        .build()) //
                    .setDescrption(message) //
                    .build()) //
                .addButton(new ButtonObject(mContext.getString(R.string.label_kakao_mobile_app), LinkObject.newBuilder() //
                    .setAndroidExecutionParams(schemeParams) //
                    .setIosExecutionParams(schemeParams) //
                    .build())) //
                .build();

            mKakaoLinkService.sendDefault(mContext, params, new ResponseCallback<KakaoLinkResponse>()
            {
                @Override
                public void onFailure(ErrorResult errorResult)
                {
                    ExLog.e(errorResult.toString());
                }

                @Override
                public void onSuccess(KakaoLinkResponse result)
                {
                }
            });
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    public void shareBookingCancelStay(String message, String stayName, String address, String imageUrl)
    {
        try
        {
            String kakaoImageUrl = null;
            if (DailyTextUtils.isTextEmpty(imageUrl) == false)
            {
                int lastSlash = imageUrl.lastIndexOf('/');
                String fileName = imageUrl.substring(lastSlash + 1);
                kakaoImageUrl = imageUrl.substring(0, lastSlash + 1) + URLEncoder.encode(fileName);
            }

            LocationTemplate params = LocationTemplate.newBuilder(address, //
                ContentObject.newBuilder(stayName, //
                    kakaoImageUrl, //
                    LinkObject.newBuilder() //
                        .build()) //
                    .setDescrption(message) //
                    .build()) //
                .setAddressTitle(stayName) //
                .build();

            mKakaoLinkService.sendDefault(mContext, params, new ResponseCallback<KakaoLinkResponse>()
            {
                @Override
                public void onFailure(ErrorResult errorResult)
                {
                    ExLog.e(errorResult.toString());
                }

                @Override
                public void onSuccess(KakaoLinkResponse result)
                {

                }
            });
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    public void shareGourmet(String name, String placeName, String address, int index, String imageUrl, GourmetBookingDay gourmetBookingDay)
    {
        try
        {
            String date = gourmetBookingDay.getVisitDay("yyyyMMdd");
            String schemeParams = String.format(Locale.KOREA, "vc=5&v=gd&i=%d&d=%s", index, date);

            String text = mContext.getString(R.string.kakao_btn_share_fnb, name, placeName//
                , gourmetBookingDay.getVisitDay("yyyy.MM.dd(EEE)"), address);

            String kakaoImageUrl = null;
            if (DailyTextUtils.isTextEmpty(imageUrl) == false)
            {
                int lastSlash = imageUrl.lastIndexOf('/');
                String fileName = imageUrl.substring(lastSlash + 1);
                kakaoImageUrl = imageUrl.substring(0, lastSlash + 1) + URLEncoder.encode(fileName);
            }

            LocationTemplate params = LocationTemplate.newBuilder(address, //
                ContentObject.newBuilder(placeName, //
                    kakaoImageUrl, //
                    LinkObject.newBuilder() //
                        .setWebUrl("https://mobile.dailyhotel.co.kr/gourmet/" + index) //
                        .setMobileWebUrl("https://mobile.dailyhotel.co.kr/gourmet/" + index) //
                        .setAndroidExecutionParams(schemeParams) //
                        .setIosExecutionParams(schemeParams) //
                        .build()) //
                    .setDescrption(text) //
                    .build()) //
                .addButton(new ButtonObject(mContext.getString(R.string.label_kakao_mobile_app), LinkObject.newBuilder() //
                    .setWebUrl("https://mobile.dailyhotel.co.kr/gourmet/" + index) //
                    .setMobileWebUrl("https://mobile.dailyhotel.co.kr/gourmet/" + index) //
                    .setAndroidExecutionParams(schemeParams) //
                    .setIosExecutionParams(schemeParams) //
                    .build())) //
                .setAddressTitle(placeName) //
                .build();

            mKakaoLinkService.sendDefault(mContext, params, new ResponseCallback<KakaoLinkResponse>()
            {
                @Override
                public void onFailure(ErrorResult errorResult)
                {
                    ExLog.e(errorResult.toString());
                }

                @Override
                public void onSuccess(KakaoLinkResponse result)
                {

                }
            });
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    public void shareGourmet(String name, String placeName, String address, int index, String imageUrl, GourmetBookDateTime gourmetBookDateTime)
    {
        try
        {
            String date = gourmetBookDateTime.getVisitDateTime("yyyyMMdd");
            String schemeParams = String.format(Locale.KOREA, "vc=5&v=gd&i=%d&d=%s", index, date);

            String text = mContext.getString(R.string.kakao_btn_share_fnb, name, placeName//
                , gourmetBookDateTime.getVisitDateTime("yyyy.MM.dd(EEE)"), address);

            String kakaoImageUrl = null;
            if (DailyTextUtils.isTextEmpty(imageUrl) == false)
            {
                int lastSlash = imageUrl.lastIndexOf('/');
                String fileName = imageUrl.substring(lastSlash + 1);
                kakaoImageUrl = imageUrl.substring(0, lastSlash + 1) + URLEncoder.encode(fileName);
            }

            LocationTemplate params = LocationTemplate.newBuilder(address, //
                ContentObject.newBuilder(placeName, //
                    kakaoImageUrl, //
                    LinkObject.newBuilder() //
                        .setWebUrl("https://mobile.dailyhotel.co.kr/gourmet/" + index) //
                        .setMobileWebUrl("https://mobile.dailyhotel.co.kr/gourmet/" + index) //
                        .setAndroidExecutionParams(schemeParams) //
                        .setIosExecutionParams(schemeParams) //
                        .build()) //
                    .setDescrption(text) //
                    .build()) //
                .addButton(new ButtonObject(mContext.getString(R.string.label_kakao_mobile_app), LinkObject.newBuilder() //
                    .setWebUrl("https://mobile.dailyhotel.co.kr/gourmet/" + index) //
                    .setMobileWebUrl("https://mobile.dailyhotel.co.kr/gourmet/" + index) //
                    .setAndroidExecutionParams(schemeParams) //
                    .setIosExecutionParams(schemeParams) //
                    .build())) //
                .setAddressTitle(placeName) //
                .build();

            mKakaoLinkService.sendDefault(mContext, params, new ResponseCallback<KakaoLinkResponse>()
            {
                @Override
                public void onFailure(ErrorResult errorResult)
                {
                    ExLog.e(errorResult.toString());
                }

                @Override
                public void onSuccess(KakaoLinkResponse result)
                {

                }
            });
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    public void shareBookingGourmet(String message, String placeName, String address, int index, String imageUrl, String reservationDate)
    {
        try
        {
            String schemeParams = String.format(Locale.KOREA, "vc=5&v=gd&i=%d&d=%s", index, reservationDate);

            String kakaoImageUrl = null;
            if (DailyTextUtils.isTextEmpty(imageUrl) == false)
            {
                int lastSlash = imageUrl.lastIndexOf('/');
                String fileName = imageUrl.substring(lastSlash + 1);
                kakaoImageUrl = imageUrl.substring(0, lastSlash + 1) + URLEncoder.encode(fileName);
            }

            LocationTemplate params = LocationTemplate.newBuilder(address, //
                ContentObject.newBuilder(placeName, //
                    kakaoImageUrl, //
                    LinkObject.newBuilder() //
                        .setWebUrl("https://mobile.dailyhotel.co.kr/gourmet/" + index) //
                        .setMobileWebUrl("https://mobile.dailyhotel.co.kr/gourmet/" + index) //
                        .setAndroidExecutionParams(schemeParams) //
                        .setIosExecutionParams(schemeParams) //
                        .build()) //
                    .setDescrption(message) //
                    .build()) //
                .addButton(new ButtonObject(mContext.getString(R.string.label_kakao_mobile_app), LinkObject.newBuilder() //
                    .setWebUrl("https://mobile.dailyhotel.co.kr/gourmet/" + index) //
                    .setMobileWebUrl("https://mobile.dailyhotel.co.kr/gourmet/" + index) //
                    .setAndroidExecutionParams(schemeParams) //
                    .setIosExecutionParams(schemeParams) //
                    .build())) //
                .setAddressTitle(placeName) //
                .build();

            mKakaoLinkService.sendDefault(mContext, params, new ResponseCallback<KakaoLinkResponse>()
            {
                @Override
                public void onFailure(ErrorResult errorResult)
                {
                    ExLog.e(errorResult.toString());
                }

                @Override
                public void onSuccess(KakaoLinkResponse result)
                {

                }
            });
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    public void shareBookingCancelGourmet(String message, String placeName, String address, String imageUrl)
    {
        try
        {
            String kakaoImageUrl = null;
            if (DailyTextUtils.isTextEmpty(imageUrl) == false)
            {
                int lastSlash = imageUrl.lastIndexOf('/');
                String fileName = imageUrl.substring(lastSlash + 1);
                kakaoImageUrl = imageUrl.substring(0, lastSlash + 1) + URLEncoder.encode(fileName);
            }

            LocationTemplate params = LocationTemplate.newBuilder(address, //
                ContentObject.newBuilder(placeName, //
                    kakaoImageUrl, //
                    LinkObject.newBuilder() //
                        .build()) //
                    .setDescrption(message) //
                    .build()) //
                .setAddressTitle(placeName) //
                .build();

            mKakaoLinkService.sendDefault(mContext, params, new ResponseCallback<KakaoLinkResponse>()
            {
                @Override
                public void onFailure(ErrorResult errorResult)
                {
                    ExLog.e(errorResult.toString());
                }

                @Override
                public void onSuccess(KakaoLinkResponse result)
                {

                }
            });
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }
}
