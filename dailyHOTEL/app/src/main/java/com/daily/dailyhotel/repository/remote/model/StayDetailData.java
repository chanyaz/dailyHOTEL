package com.daily.dailyhotel.repository.remote.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.daily.dailyhotel.entity.DetailImageInformation;
import com.daily.dailyhotel.entity.ImageMap;
import com.daily.dailyhotel.entity.Room;
import com.daily.dailyhotel.entity.Stay;
import com.daily.dailyhotel.entity.StayDetailk;
import com.daily.dailyhotel.entity.TrueAwards;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

@JsonObject
public class StayDetailData
{
    @JsonField(name = "category")
    public String category;

    @JsonField(name = "idx")
    public int index;

    @JsonField(name = "name")
    public String name;

    @JsonField(name = "grade")
    public String grade;

    @JsonField(name = "discount")
    public int discount;

    @JsonField(name = "singleStay")
    public boolean singleStay;

    @JsonField(name = "provideRewardSticker")
    public boolean provideRewardSticker;

    @JsonField(name = "coupon")
    public CouponData coupon;

    @JsonField(name = "rating")
    public RatingData rating;

    @JsonField(name = "awards")
    public AwardsData awards;

    @JsonField(name = "configurations")
    public ConfigurationsData configurations;

    @JsonField(name = "checkTime")
    public CheckTimeData checkTime;

    @JsonField(name = "address")
    public String address;

    @JsonField(name = "roomCount")
    public int roomCount;

    @JsonField(name = "facilities")
    public List<String> facilities;

    @JsonField(name = "benefit")
    public BenefitData benefit;

    @JsonField(name = "details")
    public List<DetailData> details;

    @JsonField(name = "refundPolicy")
    public RefundPolicyData refundPolicy;

    @JsonField(name = "wishCount")
    public int wishCount;

    @JsonField(name = "myWish")
    public boolean myWish;

    @JsonField(name = "dailyComments")
    public List<String> dailyComments;

    @JsonField(name = "waitingForBooking")
    public boolean waitingForBooking;

    @JsonField(name = "breakfast")
    public BreakfastData breakfast;

    @JsonField(name = "location")
    public LocationData location;

    @JsonField(name = "images")
    public List<ImageData> images;

    @JsonField(name = "province")
    public ProvinceData province;

    @JsonField(name = "rooms")
    public List<RoomData> rooms;

    @JsonField(name = "vr")
    public List<VRData> vr;

    @JsonField(name = "statistic")
    public ReviewStatisticData statistic;

    public StayDetailData()
    {

    }

    public StayDetailk getStayDetail()
    {
        StayDetailk stayDetail = new StayDetailk();

        stayDetail.setIndex(index);
        stayDetail.setWishCount(wishCount);
        stayDetail.setWish(myWish);
        stayDetail.setSingleStay(singleStay);

        if (images != null && images.size() > 0)
        {
            List<DetailImageInformation> detailImageInformationList = new ArrayList<>();

            for (ImageData imageData : images)
            {
                if (imageData.primary)
                {
                    detailImageInformationList.add(0, imageData.getDetailImageInformation());
                } else
                {
                    detailImageInformationList.add(imageData.getDetailImageInformation());
                }
            }

            stayDetail.setImageList(detailImageInformationList);
        }

        if (vr != null && vr.size() > 0)
        {
            List<StayDetailk.VRInformation> vrInformationList = new ArrayList<>();

            for (VRData vrData : vr)
            {
                vrInformationList.add(vrData.getVRInformation());
            }

            stayDetail.setVrInformation(vrInformationList);
        }

        StayDetailk.BaseInformation baseInformation = new StayDetailk.BaseInformation();
        baseInformation.setCategory(category);

        try
        {
            baseInformation.setGrade(Stay.Grade.valueOf(grade));
        } catch (Exception e)
        {
            baseInformation.setGrade(Stay.Grade.etc);
        }

        baseInformation.setProvideRewardSticker(provideRewardSticker);
        baseInformation.setName(name);
        baseInformation.setDiscount(discount);

        if (awards != null)
        {
            baseInformation.setAwards(awards.getTrueAwards());
        }

        stayDetail.setBaseInformation(baseInformation);


        if (rating != null)
        {
            StayDetailk.TrueReviewInformation trueReviewInformation = new StayDetailk.TrueReviewInformation();

            trueReviewInformation.setRatingCount(rating.persons);
            trueReviewInformation.setRatingPercent(rating.values);
            trueReviewInformation.setShowRating(rating.show);

            if (rating.primary != null)
            {
                StayDetailk.TrueReviewInformation.PrimaryReview primaryReview = new StayDetailk.TrueReviewInformation.PrimaryReview();
                primaryReview.setScore(rating.primary.avgScore);
                primaryReview.setComment(rating.primary.comment);
                primaryReview.setUserId(rating.primary.userId);

                trueReviewInformation.setReview(primaryReview);
            }

            stayDetail.setTrueReviewInformation(trueReviewInformation);
        }

        StayDetailk.BenefitInformation benefitInformation = new StayDetailk.BenefitInformation();

        if (benefit != null)
        {
            benefitInformation.setTitle(benefit.title);
            benefitInformation.setContentList(benefit.getContents());
        }

        if (coupon != null)
        {
            benefitInformation.setCoupon(coupon.getCoupon());
        }

        StayDetailk.RoomInformation roomInformation = new StayDetailk.RoomInformation();

        if (rooms != null && rooms.size() > 0)
        {
            List<Room> roomList = new ArrayList<>();
            List<String> bedTypeList = new ArrayList<>();
            List<String> facilityList = new ArrayList<>();

            for (RoomData roomData : rooms)
            {
                roomList.add(roomData.getRoom());


            }

            roomInformation.setBedTypeList(bedTypeList);
            roomInformation.setFacilityList(facilityList);
            roomInformation.setRoomList(roomList);

            stayDetail.setRoomInformation(roomInformation);
        }

        if (dailyComments != null && dailyComments.size() > 0)
        {
            stayDetail.setDailyCommentList(dailyComments);
        }

        stayDetail.setTotalRoomCount(roomCount);
        stayDetail.setFacilityList(facilities);

        StayDetailk.AddressInformation addressInformation = new StayDetailk.AddressInformation();

        addressInformation.setAddress(address);

        if (location != null)
        {
            addressInformation.setLatitude(location.latitude);
            addressInformation.setLongitude(location.longitude);
        }

        stayDetail.setAddressInformation(addressInformation);

        if (checkTime != null)
        {
            StayDetailk.CheckTimeInformation checkTimeInformation = new StayDetailk.CheckTimeInformation();

            checkTimeInformation.setCheckIn(checkTime.checkIn);
            checkTimeInformation.setCheckOut(checkTime.checkOut);
            checkTimeInformation.setDescription(checkTime.description);

            stayDetail.setCheckTimeInformation(checkTimeInformation);
        }

        StayDetailk.DetailInformation detailInformation = new StayDetailk.DetailInformation();

        if (details != null && details.size() > 0)
        {
            List<StayDetailk.DetailInformation.Item> itemList = new ArrayList<>();

            for (DetailData detailData : details)
            {
                itemList.add(detailData.getItem());
            }

            detailInformation.setItemList(itemList);
        }

        if (breakfast != null)
        {
            detailInformation.setBreakfastInformation(breakfast.getBreakfastInformation());
        }

        if (refundPolicy != null)
        {
            StayDetailk.RefundInformation refundInformation = new StayDetailk.RefundInformation();

            refundInformation.setTitle(refundPolicy.title);
            refundInformation.setContentList(refundPolicy.contents);
            refundInformation.setNrdWarningMessage(refundPolicy.nrdWarning);

            stayDetail.setRefundInformation(refundInformation);
        }

        StayDetailk.CheckInformation checkInformation = new StayDetailk.CheckInformation();

        checkInformation.setWaitingForBooking(waitingForBooking);

        stayDetail.setCheckInformation(checkInformation);

        return stayDetail;
    }

    @JsonObject
    static class CouponData
    {
        @JsonField(name = "couponDiscount")
        public int couponDiscount;

        @JsonField(name = "isDownloaded")
        public boolean isDownloaded;

        public StayDetailk.BenefitInformation.Coupon getCoupon()
        {
            StayDetailk.BenefitInformation.Coupon coupon = new StayDetailk.BenefitInformation.Coupon();
            coupon.setCouponDiscount(couponDiscount);
            coupon.setDownloaded(isDownloaded);

            return coupon;
        }
    }

    @JsonObject
    static class RatingData
    {
        @JsonField(name = "persons")
        public int persons;

        @JsonField(name = "values")
        public int values;

        @JsonField(name = "show")
        public boolean show;

        @JsonField(name = "primary")
        public ReviewData primary;

        static class ReviewData
        {
            @JsonField(name = "avgScore")
            public int avgScore;

            @JsonField(name = "comment")
            public String comment;

            @JsonField(name = "createdAt")
            public String createdAt;

            @JsonField(name = "userId")
            public String userId;
        }
    }

    @JsonObject
    static class AwardsData
    {
        @JsonField(name = "idx")
        public int index;

        @JsonField(name = "serviceType")
        public String serviceType;

        @JsonField(name = "title")
        public String title;

        @JsonField(name = "description")
        public String description;

        @JsonField(name = "imgUrl")
        public String imgUrl;

        TrueAwards getTrueAwards()
        {
            TrueAwards trueAwards = new TrueAwards();
            trueAwards.index = Integer.toString(index);
            trueAwards.description = description;
            trueAwards.serviceType = serviceType;
            trueAwards.title = title;
            trueAwards.imageUrl = imgUrl;

            return trueAwards;
        }
    }

    @JsonObject
    static class CheckTimeData
    {
        @JsonField(name = "checkIn")
        public String checkIn;

        @JsonField(name = "checkOut")
        public String checkOut;

        @JsonField(name = "description")
        public List<String> description;
    }

    @JsonObject
    static class RefundPolicyData
    {
        @JsonField(name = "title")
        public String title;

        @JsonField(name = "contents")
        public List<String> contents;

        @JsonField(name = "nrdWarning")
        public String nrdWarning;
    }

    @JsonObject
    static class BenefitData
    {
        @JsonField(name = "title")
        public String title;

        @JsonField(name = "contents")
        public String contents;

        List<String> getContents()
        {
            List<String> contentList = new ArrayList<>();

            try
            {
                JSONArray jsonArray = new JSONArray(contents);

                int length = jsonArray.length();

                for (int i = 0; i < length; i++)
                {
                    contentList.add(jsonArray.getString(i));
                }
            } catch (Exception e)
            {

            }

            return contentList;
        }
    }

    @JsonObject
    static class DetailData
    {
        @JsonField(name = "type")
        public String type;

        @JsonField(name = "title")
        public String title;

        @JsonField(name = "contents")
        public List<String> contents;

        public StayDetailk.DetailInformation.Item getItem()
        {
            StayDetailk.DetailInformation.Item item = new StayDetailk.DetailInformation.Item();

            item.setTitle(title);
            item.setContentList(contents);

            return item;
        }
    }

    @JsonObject
    static class BreakfastData
    {
        @JsonField(name = "description")
        public String description;

        @JsonField(name = "items")
        public List<ItemData> items;

        @JsonObject
        static class ItemData
        {
            @JsonField(name = "amount")
            public int amount;

            @JsonField(name = "maxAge")
            public int maxAge;

            @JsonField(name = "minAge")
            public int minAge;

            @JsonField(name = "title")
            public String title;

            StayDetailk.DetailInformation.BreakfastInformation.Item getItem()
            {
                StayDetailk.DetailInformation.BreakfastInformation.Item item = new StayDetailk.DetailInformation.BreakfastInformation.Item();

                item.setAmount(amount);
                item.setMaxAge(maxAge);
                item.setMinAge(minAge);
                item.setTitle(title);

                return item;
            }
        }

        public StayDetailk.DetailInformation.BreakfastInformation getBreakfastInformation()
        {
            StayDetailk.DetailInformation.BreakfastInformation breakfastInformation = new StayDetailk.DetailInformation.BreakfastInformation();

            breakfastInformation.setDescription(description);

            if (items != null && items.size() > 0)
            {
                List<StayDetailk.DetailInformation.BreakfastInformation.Item> itemList = new ArrayList<>();

                for (ItemData itemData : items)
                {
                    itemList.add(itemData.getItem());
                }

                breakfastInformation.setItems(itemList);
            }

            return breakfastInformation;
        }
    }

    @JsonObject
    static class LocationData
    {
        @JsonField(name = "latitude")
        public double latitude;

        @JsonField(name = "longitude")
        public double longitude;
    }

    @JsonObject
    static class ImageData
    {
        @JsonField(name = "url")
        public String url;

        @JsonField(name = "description")
        public String description;

        @JsonField(name = "primary")
        public boolean primary;

        public DetailImageInformation getDetailImageInformation()
        {
            DetailImageInformation detailImageInformation = new DetailImageInformation();
            detailImageInformation.caption = description;

            ImageMap imageMap = new ImageMap();
            imageMap.smallUrl = imageMap.mediumUrl = imageMap.bigUrl = url;

            detailImageInformation.setImageMap(imageMap);

            return detailImageInformation;
        }
    }

    @JsonObject
    static class ProvinceData
    {
        @JsonField(name = "idx")
        public int index;

        @JsonField(name = "name")
        public String name;
    }

    @JsonObject
    static class RoomData
    {
        @JsonField(name = "roomIdx")
        public int roomIndex;

        @JsonField(name = "roomName")
        public String roomName;

        @JsonField(name = "roomType")
        public String roomType;

        @JsonField(name = "image")
        public ImageData image;

        @JsonField(name = "amount")
        public AmountData amount;

        @JsonField(name = "persons")
        public List<PersonData> persons;

        @JsonField(name = "benefit")
        public String benefit;

        @JsonField(name = "provideRewardSticker")
        public boolean provideRewardSticker;

        @JsonField(name = "amenities")
        public List<String> amenities;

        @JsonField(name = "checkTime")
        public CheckTimeData checkTime;

        @JsonField(name = "descriptions")
        public List<String> descriptions;

        @JsonField(name = "squareMeter")
        public String squareMeter;

        @JsonField(name = "needToKnows")
        public List<String> needToKnows;

        @JsonField(name = "consecutive")
        public ConsecutiveData consecutive;

        @JsonField(name = "roomCharge")
        public RoomChargeData roomCharge;

        @JsonField(name = "refundType")
        public String refundType;

        public RoomData()
        {

        }

        public Room getRoom()
        {
            Room room = new Room();


            return room;
        }

        @JsonObject
        static class AmountData
        {
            @JsonField(name = "discountAverage")
            public int discountAverage;

            @JsonField(name = "discountRate")
            public int discountRate;

            @JsonField(name = "discountTotal")
            public int discountTotal;

            @JsonField(name = "priceAverage")
            public int priceAverage;
        }

        @JsonObject
        static class PersonData
        {
            @JsonField(name = "fixed")
            public int fixed;

            @JsonField(name = "extra")
            public int extra;

            @JsonField(name = "extraCharge")
            public boolean extraCharge;

            @JsonField(name = "breakfast")
            public int breakfast;
        }

        @JsonObject
        static class ConsecutiveData
        {
            @JsonField(name = "charge")
            public int charge;

            @JsonField(name = "enable")
            public boolean enable;
        }

        @JsonObject
        static class RoomChargeData
        {
            @JsonField(name = "descriptions")
            public String descriptions;

            @JsonField(name = "extraBed")
            public int extraBed;

            @JsonField(name = "extraBedEnable")
            public boolean extraBedEnable;

            @JsonField(name = "extraBedding")
            public int extraBedding;

            @JsonField(name = "extraBeddingEnable")
            public boolean extraBeddingEnable;
        }

        @JsonObject
        static class BedTypeData
        {
            @JsonField(name = "bedType")
            public String bedType;

            @JsonField(name = "count")
            public int count;
        }
    }

    @JsonObject
    static class VRData
    {
        @JsonField(name = "name")
        public String name;

        @JsonField(name = "type")
        public String type;

        @JsonField(name = "typeIdx")
        public int typeIdx;

        @JsonField(name = "url")
        public String url;

        StayDetailk.VRInformation getVRInformation()
        {
            StayDetailk.VRInformation vrInformation = new StayDetailk.VRInformation();

            vrInformation.setName(name);
            vrInformation.setType(type);
            vrInformation.setTypeIdx(typeIdx);
            vrInformation.setUrl(url);

            return vrInformation;
        }
    }

    @JsonObject
    static class ReviewStatisticData
    {
        @JsonField(name = "reviewScoreAvgs")
        public List<ReviewScoreAvgData> reviewScoreAvgs;

        @JsonField(name = "reviewScoreTotalCount")
        public int reviewScoreTotalCount;

        @JsonObject
        static class ReviewScoreAvgData
        {
            @JsonField(name = "type")
            public String type;

            @JsonField(name = "scoreAvg")
            public float scoreAvg;
        }
    }
}