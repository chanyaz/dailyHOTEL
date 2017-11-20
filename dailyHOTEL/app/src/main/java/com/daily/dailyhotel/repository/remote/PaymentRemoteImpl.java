package com.daily.dailyhotel.repository.remote;

import android.content.Context;
import android.support.annotation.NonNull;

import com.daily.base.exception.BaseException;
import com.daily.base.util.ExLog;
import com.daily.dailyhotel.domain.PaymentInterface;
import com.daily.dailyhotel.entity.Card;
import com.daily.dailyhotel.entity.GourmetPayment;
import com.daily.dailyhotel.entity.PaymentResult;
import com.daily.dailyhotel.entity.People;
import com.daily.dailyhotel.entity.StayBookDateTime;
import com.daily.dailyhotel.entity.StayOutboundPayment;
import com.daily.dailyhotel.entity.StayPayment;
import com.daily.dailyhotel.entity.StayRefundPolicy;
import com.daily.dailyhotel.repository.remote.model.CardData;
import com.daily.dailyhotel.repository.remote.model.StayRefundPolicyData;
import com.daily.dailyhotel.storage.preference.DailyPreference;
import com.twoheart.dailyhotel.Setting;
import com.twoheart.dailyhotel.network.dto.BaseDto;
import com.twoheart.dailyhotel.network.dto.BaseListDto;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.Crypto;
import com.twoheart.dailyhotel.util.DailyCalendar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class PaymentRemoteImpl extends BaseRemoteImpl implements PaymentInterface
{
    public PaymentRemoteImpl(@NonNull Context context)
    {
        super(context);
    }

    @Override
    public Observable<StayOutboundPayment> getStayOutboundPayment(StayBookDateTime stayBookDateTime, int index//
        , String rateCode, String rateKey, String roomTypeCode, int roomBedTypeId, People people, String vendorType)
    {
        JSONObject jsonObject = new JSONObject();

        final int NUMBER_OF_ROOMS = 1;

        try
        {
            jsonObject.put("arrivalDate", stayBookDateTime.getCheckInDateTime("yyyy-MM-dd"));
            jsonObject.put("departureDate", stayBookDateTime.getCheckOutDateTime("yyyy-MM-dd"));
            jsonObject.put("numberOfRooms", NUMBER_OF_ROOMS);
            jsonObject.put("rooms", getRooms(new People[]{people}, new int[]{roomBedTypeId}));
            jsonObject.put("rateCode", rateCode);
            jsonObject.put("rateKey", rateKey);
            jsonObject.put("roomTypeCode", roomTypeCode);
            jsonObject.put("vendorType", vendorType);
        } catch (Exception e)
        {
            ExLog.e(e.toString());

            jsonObject = null;
        }

        final String URL = Constants.DEBUG ? DailyPreference.getInstance(mContext).getBaseOutBoundUrl() : Setting.getOutboundServerUrl();

        final String API = Constants.UNENCRYPTED_URL ? "api/v2/outbound/hotels/{stayIndex}/room-reservation-saleinfos"//
            : "MTcwJDI1JDk0JDEzNSQxNzMkMTYzJDcyJDEzOCQxNDgkNjYkMTA5JDEzNCQ3MiQxNjYkNjAkMTU2JA==$RjNCNjM2RjZFQjFBNUVEMkI1MFkVCRjA1NjZFNDBFOTI3MEM4RDUzN0NFQkZNDQTQ0RPkNDNTQRDBNUU3RDFCNzU1MUIyOUIxQYkQzQkY3ODRFMEjY4QkZGNTZBMDUxQkQzODg2NLUE5RTECRBN0YyM0EU2OHUVBMkE5QUJCVQjc4RREM2MTM1RUWPI=$";

        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("{stayIndex}", Integer.toString(index));

        return mDailyMobileService.getStayOutboundPayment(Crypto.getUrlDecoderEx(URL) + Crypto.getUrlDecoderEx(API, urlParams), jsonObject) //
            .subscribeOn(Schedulers.io()).map(stayOutboundPaymentDataBaseDto ->
            {
                StayOutboundPayment stayOutboundPayment = null;

                if (stayOutboundPaymentDataBaseDto != null)
                {
                    if (stayOutboundPaymentDataBaseDto.msgCode == 100 && stayOutboundPaymentDataBaseDto.data != null)
                    {
                        stayOutboundPayment = stayOutboundPaymentDataBaseDto.data.getStayOutboundPayment();
                    } else
                    {
                        throw new BaseException(stayOutboundPaymentDataBaseDto.msgCode, stayOutboundPaymentDataBaseDto.msg);
                    }
                } else
                {
                    throw new BaseException(-1, null);
                }

                return stayOutboundPayment;
            }).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<StayPayment> getStayPayment(StayBookDateTime stayBookDateTime, int index)
    {
        int nights;

        try
        {
            nights = stayBookDateTime.getNights();
        } catch (Exception e)
        {
            nights = 1;
        }

        final String API = Constants.UNENCRYPTED_URL ? "api/v3/hotel/payment/preview"//
            : "MTIkNDQkNTUkNTMkMzYkODQkODQkODQkMTQkNjQkOTUkNjckNTAkMTAwJDE2JDg3JA==$QTBENDRBMzY5VNUDLBEQUExNjlBMTc3ODE4QUQU0N0VENDZXBODNE5NTEM1ML0RGNTYREGRTQ0NzMwNkI5NEQwMPzQYLGwQjEzQKg=A=$";

        return mDailyMobileService.getStayPayment(Crypto.getUrlDecoderEx(API), index, stayBookDateTime.getCheckInDateTime("yyyy-MM-dd"), nights) //
            .subscribeOn(Schedulers.io()).map(stayPaymentDataBaseDto ->
            {
                StayPayment stayPayment = null;

                if (stayPaymentDataBaseDto != null)
                {
                    // 0	성공
                    // 4	데이터가 없을시
                    // 5	판매 마감시
                    // 6	현재 시간부터 날짜 바뀌기 전시간(새벽 3시
                    // 7    3시부터 9시까지
                    switch (stayPaymentDataBaseDto.msgCode)
                    {
                        case 100:
                            if (stayPaymentDataBaseDto.data != null)
                            {
                                stayPayment = stayPaymentDataBaseDto.data.getStayPayment();
                            } else
                            {
                                throw new BaseException(stayPaymentDataBaseDto.msgCode, stayPaymentDataBaseDto.msg);
                            }
                            break;

                        case 6:
                        case 7:
                            if (stayPaymentDataBaseDto.data != null)
                            {
                                stayPayment = stayPaymentDataBaseDto.data.getStayPayment();
                                stayPayment.mWarningMessage = stayPaymentDataBaseDto.msg;
                            } else
                            {
                                throw new BaseException(stayPaymentDataBaseDto.msgCode, stayPaymentDataBaseDto.msg);
                            }
                            break;

                        case 4:
                        case 5:
                        default:
                            throw new BaseException(stayPaymentDataBaseDto.msgCode, stayPaymentDataBaseDto.msg);
                    }
                } else
                {
                    throw new BaseException(-1, null);
                }

                return stayPayment;
            }).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<GourmetPayment> getGourmetPayment(int[] saleIndexes)
    {
        final String API = Constants.UNENCRYPTED_URL ? "api/fnb/sale/ticket/payment/info"//
            : "MzkkOTYkOTIkMTI5JDEwOSQzNyQxMjgkMTA3JDQ3JDY0JDMxJDEzMyQxNiQwJDk0JDEzOCQ=$KMzM4MzgyRkFFOTFBINUZCRkQxRjA5MjIEyRkFEOODYDwMkMwMKjk1RjkxRjNDMDM1MJTc1QjZCMjJCREFEQzk3NDdGMkUCzMDgVzNDZEGRkE1QThWDNkQM1MzNBRjEyMjQwQUUYH1DNkIQw$";

        return mDailyMobileService.getGourmetPayment(Crypto.getUrlDecoderEx(API), saleIndexes[0]) //
            .subscribeOn(Schedulers.io()).map(gourmetPaymentDataBaseDto ->
            {
                GourmetPayment gourmetPayment = null;

                if (gourmetPaymentDataBaseDto != null)
                {
                    if (gourmetPaymentDataBaseDto.msgCode == 0 && gourmetPaymentDataBaseDto.data != null)
                    {
                        gourmetPayment = gourmetPaymentDataBaseDto.data.getGourmetPayment();
                    } else
                    {
                        throw new BaseException(gourmetPaymentDataBaseDto.msgCode, gourmetPaymentDataBaseDto.msg);
                    }
                } else
                {
                    throw new BaseException(-1, null);
                }

                return gourmetPayment;
            }).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<List<Card>> getEasyCardList()
    {
        final String API = Constants.UNENCRYPTED_URL ? "api/user/session/billing/card/info"//
            : "NDIkOCQ1NSQ4NyQ4NyQ4MCQxMzIkOTIkMTMwJDU2JDE2JDQyJDY4JDU5JDEzMCQ3MyQ=$QzdFNkE5NNjgzM0JIFMjZFRjlCQjY4OEQ3NkI5NDdDKRjUMxNDkzNTk1MTBWjkzQkE5NELNDNQ0RFOENGRDAwMGEE5MTE3UARDYFGQjEzMzMxRjVDMDA2MjVEQzBGMTgxREYJGNDMK3MTGI2$";

        return mDailyMobileService.getEasyCardList(Crypto.getUrlDecoderEx(API)) //
            .subscribeOn(Schedulers.io()).map(new Function<BaseListDto<CardData>, List<Card>>()
            {
                @Override
                public List<Card> apply(@io.reactivex.annotations.NonNull BaseListDto<CardData> cardDataBaseListDto) throws Exception
                {
                    List<Card> cardList = new ArrayList<>();

                    if (cardDataBaseListDto != null)
                    {
                        if (cardDataBaseListDto.msgCode == 0 && cardDataBaseListDto.data != null)
                        {
                            for (CardData cardData : cardDataBaseListDto.data)
                            {
                                cardList.add(cardData.getCard());
                            }
                        } else
                        {
                            throw new BaseException(cardDataBaseListDto.msgCode, cardDataBaseListDto.msg);
                        }
                    } else
                    {
                        throw new BaseException(-1, null);
                    }

                    return cardList;
                }
            }).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<PaymentResult> getStayOutboundPaymentTypeEasy(int index, JSONObject jsonObject)
    {
        final String URL = Constants.DEBUG ? DailyPreference.getInstance(mContext).getBaseOutBoundUrl() : Setting.getOutboundServerUrl();

        final String API = Constants.UNENCRYPTED_URL ? "api/v1/outbound/hotels/{hotelId}/room-reservation-payments/oneclick"//
            : "MTU2JDEyNyQxNTAkMTYyJDQkMjIkMTc0JDE2NSQyNCQxNDMkMTkwJDU4JDQ2JDIyNCQyMyQxMDYk$OUM2KNjQyOEY1QzQxRTUzQQBzVczQTFEQjBGRjU1ODY2NDIQ0QjM0NTc4RjYR0NUYxNTU4MDIyMUMzRUQ2RDYyQzgyRkYzQTBDQ0Y3MzNGIRTA2RUNCRTlEQUNEODMxNzg2QkNSGMkFFMjIwRDMK1NUI1NTg5QOUI5RDExWODkHCzMTgwMDA2JRTY2QzQ5OUE3DOTY1MDE3RTkwREMzNDU2NDYwNTE3NTcXxRQ==$";

        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("{hotelId}", Integer.toString(index));

        return mDailyMobileService.getStayOutboundPaymentTypeEasy(Crypto.getUrlDecoderEx(URL) + Crypto.getUrlDecoderEx(API, urlParams), jsonObject) //
            .subscribeOn(Schedulers.io()).map(paymentResultDataBaseDto ->
            {
                PaymentResult paymentResult = null;

                if (paymentResultDataBaseDto != null)
                {
                    if (paymentResultDataBaseDto.msgCode == 100 && paymentResultDataBaseDto.data != null)
                    {
                        paymentResult = paymentResultDataBaseDto.data.getPaymentResult();
                    } else
                    {
                        throw new BaseException(paymentResultDataBaseDto.msgCode, paymentResultDataBaseDto.msg);
                    }
                } else
                {
                    throw new BaseException(-1, null);
                }

                return paymentResult;
            }).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<PaymentResult> getStayOutboundPaymentTypeBonus(int index, JSONObject jsonObject)
    {
        final String URL = Constants.DEBUG ? DailyPreference.getInstance(mContext).getBaseOutBoundUrl() : Setting.getOutboundServerUrl();

        final String API = Constants.UNENCRYPTED_URL ? "api/v1/outbound/hotels/{hotelId}/room-reservation-payments/bonus"//
            : "MTE5JDI0JDg1JDI1JDcyJDE5JDckNTUkNiQxOTckMTE3JDYyJDE0MiQ2MyQxNTIkODgk$OTdFMECFHDNDY2MkQ1NENQGQTNEYPQTg5RTczNjQwRjA4RjZCMjZBRkURzREI4DSOTREQ0M3Mjc4OEUYyNDFERjUX4ODg5OODI1NTlENUY0RDEwQTg2NzVENJzgyM0IwOTAC3NUUzNDM2NjVJGQzkxODkY5Q0E2NzEyOUQ0QzdBRTlEMDU2RUYzMDU0RUYyQTI5OTE2NDQ5FREVFOTk0QTM0QzQ2MjQxNTA0Ng==$";

        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("{hotelId}", Integer.toString(index));

        return mDailyMobileService.getStayOutboundPaymentTypeBonus(Crypto.getUrlDecoderEx(URL) + Crypto.getUrlDecoderEx(API, urlParams), jsonObject) //
            .subscribeOn(Schedulers.io()).map(paymentResultDataBaseDto ->
            {
                PaymentResult paymentResult = null;

                if (paymentResultDataBaseDto != null)
                {
                    if (paymentResultDataBaseDto.msgCode == 100 && paymentResultDataBaseDto.data != null)
                    {
                        paymentResult = paymentResultDataBaseDto.data.getPaymentResult();
                    } else
                    {
                        throw new BaseException(paymentResultDataBaseDto.msgCode, paymentResultDataBaseDto.msg);
                    }
                } else
                {
                    throw new BaseException(-1, null);
                }

                return paymentResult;
            }).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<String> getStayOutboundHasDuplicatePayment(int index, JSONObject jsonObject)
    {
        final String URL = Constants.DEBUG ? DailyPreference.getInstance(mContext).getBaseOutBoundUrl() : Setting.getOutboundServerUrl();

        final String API = Constants.UNENCRYPTED_URL ? "api/v2/outbound/hotels/{stayIndex}/valid-reservations"//
            : "ODckMTE0JDc0JDEyNCQ0NCQ5NSQxMjckMTEzJDExMCQ2NiQzNyQzMCQxMzIkNjckMyQ2MCQ=$MDcR4OTk3N0Q3ODg0N0YxNzdDMzIxMEUMyQzFGNIzQxQzk1VMkJDRDk4NTU5JNjE3MUMyXOBTQyMDg3RDLU3OEYzQ0RCODIM3QTZGWMUMxMENFOEY2RDJg2NK0VFQHjU5MzBBRRSZDhERDA4QTYyQUE1RkQzNDk5N0RDNDE4NTk4MDFCM0FDQjcxQTg=$";

        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("{stayIndex}", Integer.toString(index));

        return mDailyMobileService.getStayOutboundHasDuplicatePayment(Crypto.getUrlDecoderEx(URL) + Crypto.getUrlDecoderEx(API, urlParams), jsonObject) //
            .subscribeOn(Schedulers.io()).map(new Function<BaseDto<String>, String>()
            {
                @Override
                public String apply(@io.reactivex.annotations.NonNull BaseDto<String> baseDto) throws Exception
                {
                    String message;

                    if (baseDto != null)
                    {
                        if (baseDto.msgCode == 100)
                        {
                            message = "";
                        } else
                        {
                            message = baseDto.msg;
                        }
                    } else
                    {
                        throw new BaseException(-1, null);
                    }

                    return message;
                }
            }).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<PaymentResult> getStayPaymentTypeEasy(JSONObject jsonObject)
    {
        final String API = Constants.UNENCRYPTED_URL ? "api/v4/booking/hotel/oneclick"//
            : "NDQkNzMkNDUkNjAkMzEkMzkkNyQxNSQ0OSQ4OSQxJDM0JDIyJDM2JDc5JDgxJA==$OADFGREUJ1NkUwMjXExMTYZyQjk0MzMzQTgXPO1Q0ZDNTBBGM0VBZQSM0RDRUQwMDAwNDWk0QUEzNUIO2JRjQ1YMjM3M0VEXQ0I3RA==$";

        return mDailyMobileService.getPaymentTypeEasy(Crypto.getUrlDecoderEx(API), jsonObject) //
            .subscribeOn(Schedulers.io()).map(paymentResultDataBaseDto ->
            {
                PaymentResult paymentResult = null;

                if (paymentResultDataBaseDto != null)
                {
                    if (paymentResultDataBaseDto.msgCode == 100 && paymentResultDataBaseDto.data != null)
                    {
                        paymentResult = paymentResultDataBaseDto.data.getPaymentResult();
                    } else
                    {
                        throw new BaseException(paymentResultDataBaseDto.msgCode, paymentResultDataBaseDto.msg);
                    }
                } else
                {
                    throw new BaseException(-1, null);
                }

                return paymentResult;
            }).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<PaymentResult> getStayPaymentTypeBonus(JSONObject jsonObject)
    {
        final String API = Constants.UNENCRYPTED_URL ? "api/v4/booking/hotel/daily/only"//
            : "MzgkNDUkMyQyOSQ2MiQyNCQxNiQ0OSQ0OSQxNCQxMSQ3NyQ2MSQyMCQ0MSQxMyQ=$RTAU0NEFCRjFRQDQQzgAzIOTE0NjAJxMDAxSMEU3NTRU4RUIM0RkE0KXDNjRDMThEEOTQxNzPdBN0VGRkJVCNTgyMzMyN0ZGQzYzOA==$";

        return mDailyMobileService.getPaymentTypeBonus(Crypto.getUrlDecoderEx(API), jsonObject) //
            .subscribeOn(Schedulers.io()).map(paymentResultDataBaseDto ->
            {
                PaymentResult paymentResult = null;

                if (paymentResultDataBaseDto != null)
                {
                    if (paymentResultDataBaseDto.msgCode == 100 && paymentResultDataBaseDto.data != null)
                    {
                        paymentResult = paymentResultDataBaseDto.data.getPaymentResult();
                    } else
                    {
                        throw new BaseException(paymentResultDataBaseDto.msgCode, paymentResultDataBaseDto.msg);
                    }
                } else
                {
                    throw new BaseException(-1, null);
                }

                return paymentResult;
            }).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<StayRefundPolicy> getStayRefundPolicy(StayBookDateTime stayBookDateTime, int stayIndex, int roomIndex)
    {
        final String API = Constants.UNENCRYPTED_URL ? "api/v2/payment/policy_refund"//
            : "NjYkMzYkMzIkNTgkMjEkMjQkNDEkODgkNTQkNTgkNzckNDEkNTAkMzUkNzUkMzEk$RjFBOTM0MjJFODlCNkJFRTTlVCRTIxQBTE3RYMUZCCOGCUJDQTYwQNTdEMPkE4JRDUyRNkYyOTU1ZNUYYxNMjM2RTlBMDQxNOTI0Qg==$";

        return mDailyMobileService.getStayRefundPolicy(Crypto.getUrlDecoderEx(API), stayIndex//
            , roomIndex, stayBookDateTime.getCheckInDateTime(DailyCalendar.ISO_8601_FORMAT) //
            , stayBookDateTime.getCheckOutDateTime(DailyCalendar.ISO_8601_FORMAT)) //
            .subscribeOn(Schedulers.io()).map(new Function<BaseDto<StayRefundPolicyData>, StayRefundPolicy>()
            {
                @Override
                public StayRefundPolicy apply(@io.reactivex.annotations.NonNull BaseDto<StayRefundPolicyData> stayRefundPolicyDataBaseDto) throws Exception
                {
                    StayRefundPolicy stayRefundPolicy;

                    if (stayRefundPolicyDataBaseDto != null)
                    {
                        if (stayRefundPolicyDataBaseDto.msgCode == 100 && stayRefundPolicyDataBaseDto.data != null)
                        {
                            stayRefundPolicy = stayRefundPolicyDataBaseDto.data.getStayRefundPolicy();
                        } else
                        {
                            throw new BaseException(stayRefundPolicyDataBaseDto.msgCode, stayRefundPolicyDataBaseDto.msg);
                        }
                    } else
                    {
                        throw new BaseException(-1, null);
                    }

                    return stayRefundPolicy;
                }
            }).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<String> getStayHasDuplicatePayment(StayBookDateTime stayBookDateTime)
    {
        final String API = Constants.UNENCRYPTED_URL ? "api/v5/reservations/check/sameday"//
            : "NTUkOTQkMTckMzAkNzIkMTI3JDgkOTUkMjckMTA2JDEyMyQ5OSQ3JDEzOCQ5MSQxMTkk$MzRBOTgR0YMkMwMEZFMCDNCNjRGRGUJDRXjdERTMwMkM4Njc2Qzc2NUFBMDQK1RTcwMkI0QkI1MUjA2OEY0M0MyNzFGAN0FDNjJJBMOGERDNDUgwMzE0MUJQBOEM4RTPE4NEIxQ0MM1NETI3$";

        return mDailyMobileService.getStayHasDuplicatePayment(Crypto.getUrlDecoderEx(API) //
            , stayBookDateTime.getCheckInDateTime("yyyy-MM-dd"), stayBookDateTime.getNights())//
            .subscribeOn(Schedulers.io()).map(new Function<BaseDto<String>, String>()
            {
                @Override
                public String apply(@io.reactivex.annotations.NonNull BaseDto<String> baseDto) throws Exception
                {
                    String message;

                    if (baseDto != null)
                    {
                        if (baseDto.msgCode == 100)
                        {
                            message = "";
                        } else
                        {
                            message = baseDto.msg;
                        }
                    } else
                    {
                        throw new BaseException(-1, null);
                    }

                    return message;
                }
            }).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @param jsonObject arrivalDateTime ISO-8601  "yyyy-MM-dd'T'HH:mm:ssZZZZZ"
     *                   menuIndex
     *                   menuCount
     *                   usedBonus
     *                   bonus
     *                   usedCoupon
     *                   couponCode
     *                   guest
     *                   totalPrice
     *                   billingKey
     * @return
     */
    @Override
    public Observable<PaymentResult> getGourmetPaymentTypeEasy(JSONObject jsonObject)
    {
        final String API = Constants.UNENCRYPTED_URL ? "api/v4/booking/gourmet/oneclick"//
            : "MjMkODIkMzYkMTkkODkkODckMTMkNTQkMzYkMTckNTckNSQ3MiQ0OSQ3NyQzNSQ=$NzUxOUURCRjAzMIjdBKRDQQ0ODYB0NzZEODDhBMW0MX3MDYxMzCAzMEUwNDQQY5QzRBQTY0QTURyQzEVGQUMwM0NENzM2MY0IXzNMw==$";

        return mDailyMobileService.getPaymentTypeEasy(Crypto.getUrlDecoderEx(API), jsonObject) //
            .subscribeOn(Schedulers.io()).map(paymentResultDataBaseDto ->
            {
                PaymentResult paymentResult = null;

                if (paymentResultDataBaseDto != null)
                {
                    if (paymentResultDataBaseDto.msgCode == 100 && paymentResultDataBaseDto.data != null)
                    {
                        paymentResult = paymentResultDataBaseDto.data.getPaymentResult();
                    } else
                    {
                        throw new BaseException(paymentResultDataBaseDto.msgCode, paymentResultDataBaseDto.msg);
                    }
                } else
                {
                    throw new BaseException(-1, null);
                }

                return paymentResult;
            }).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @param jsonObject arrivalDateTime ISO-8601  "yyyy-MM-dd'T'HH:mm:ssZZZZZ"
     *                   menuIndex
     *                   menuCount
     *                   usedBonus
     *                   bonus
     *                   usedCoupon
     *                   couponCode
     *                   guest
     *                   totalPrice
     * @return
     */
    @Override
    public Observable<PaymentResult> getGourmetPaymentTypeBonus(JSONObject jsonObject)
    {
        final String API = Constants.UNENCRYPTED_URL ? "api/v4/booking/gourmet/daily/only"//
            : "MTI0JDMkNDMkODUkNDMkNjYkMzQkOTAkNjIkMTIkMTckOTgkOTckMTIzJDE0MSQzNyQ=$Q0IY4NjM0OEMBwMTITxRjlCMzM4NzA3QTJBONNUIxRUFEQTFFQzN0I1NUEwRDhGRTIM1ODET2NURENzIwQjhDMDdCM0UJ3ZQTMX1OMzNENUIxOEMxQTU3MUU4RERIBMkQyMDA3MkM4BNEQZ1$";

        return mDailyMobileService.getPaymentTypeBonus(Crypto.getUrlDecoderEx(API), jsonObject) //
            .subscribeOn(Schedulers.io()).map(paymentResultDataBaseDto ->
            {
                PaymentResult paymentResult = null;

                if (paymentResultDataBaseDto != null)
                {
                    if (paymentResultDataBaseDto.msgCode == 100 && paymentResultDataBaseDto.data != null)
                    {
                        paymentResult = paymentResultDataBaseDto.data.getPaymentResult();
                    } else
                    {
                        throw new BaseException(paymentResultDataBaseDto.msgCode, paymentResultDataBaseDto.msg);
                    }
                } else
                {
                    throw new BaseException(-1, null);
                }

                return paymentResult;
            }).observeOn(AndroidSchedulers.mainThread());
    }

    private JSONArray getRooms(People[] peoples, int[] roomBedTypeIds)
    {
        JSONArray roomJSONArray = new JSONArray();

        if (peoples == null || peoples.length == 0 || roomBedTypeIds == null || roomBedTypeIds.length == 0//
            || peoples.length != roomBedTypeIds.length)
        {
            return roomJSONArray;
        }

        try
        {
            int length = peoples.length;

            for (int i = 0; i < length; i++)
            {
                JSONObject roomJSONObject = new JSONObject();

                roomJSONObject.put("numberOfAdults", peoples[i].numberOfAdults);
                roomJSONObject.put("roomBedTypeId", roomBedTypeIds[i]);

                List<Integer> childAgeList = peoples[i].getChildAgeList();

                if (childAgeList != null && childAgeList.size() > 0)
                {
                    JSONArray childJSONArray = new JSONArray();

                    for (int age : childAgeList)
                    {
                        childJSONArray.put(Integer.toString(age));
                    }

                    roomJSONObject.put("numberOfChildren", childAgeList.size());
                    roomJSONObject.put("childAges", childJSONArray);
                } else
                {
                    roomJSONObject.put("numberOfChildren", 0);
                    roomJSONObject.put("childAges", null);
                }

                roomJSONArray.put(roomJSONObject);
            }
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }

        return roomJSONArray;
    }
}
