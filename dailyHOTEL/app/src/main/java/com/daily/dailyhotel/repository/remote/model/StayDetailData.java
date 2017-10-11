package com.daily.dailyhotel.repository.remote.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.daily.base.util.DailyTextUtils;
import com.daily.dailyhotel.entity.DetailImageInformation;
import com.daily.dailyhotel.entity.ImageMap;
import com.daily.dailyhotel.entity.StayDetail;
import com.daily.dailyhotel.entity.StayRoom;
import com.twoheart.dailyhotel.model.Stay;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

@JsonObject
public class StayDetailData
{
    @JsonField(name = "idx")
    public int index;

    @JsonField(name = "name")
    public String name;

    @JsonField(name = "latitude")
    public double latitude;

    @JsonField(name = "longitude")
    public double longitude;

    @JsonField(name = "address")
    public String address;

    @JsonField(name = "category")
    public String category;

    @JsonField(name = "imgPath")
    public LinkedHashMap<String, List<ImageInformationData>> imgPath;

    @JsonField(name = "grade")
    public String grade;

    @JsonField(name = "price")
    public int price;

    @JsonField(name = "discount")
    public int discount;

    @JsonField(name = "ratingPersons")
    public int ratingPersons;

    @JsonField(name = "ratingValue")
    public int ratingValue;

    @JsonField(name = "ratingShow")
    public boolean ratingShow;

    @JsonField(name = "parking")
    public boolean parking;

    @JsonField(name = "noParking")
    public boolean noParking;

    @JsonField(name = "pool")
    public boolean pool;

    @JsonField(name = "fitness")
    public boolean fitness;

    @JsonField(name = "pet")
    public boolean pet;

    @JsonField(name = "sharedBbq")
    public boolean sharedBBQ;

    @JsonField(name = "businessCenter")
    public boolean businessCenter;

    @JsonField(name = "sauna")
    public boolean sauna;

    @JsonField(name = "kidsPlayroom")
    public boolean kidsPlayRoom;

    @JsonField(name = "benefitWarning")
    public String benefitWarning;

    @JsonField(name = "rooms")
    public List<RoomData> rooms;

    @JsonField(name = "singleStay")
    public boolean singleStay; // 연박 불가 여부

    @JsonField(name = "overseas")
    public boolean overseas; // 0 : 국내 , 1 : 해외

    @JsonField(name = "waitingForBooking")
    public boolean waitingForBooking; // 예약 대기

    @JsonField(name = "details")
    public List<LinkedHashMap<String, List<String>>> details;

    @JsonField(name = "imgUrl")
    public String imgUrl;

    @JsonField(name = "benefit")
    public String benefit;

    @JsonField(name = "benefitContents")
    public List<String> benefitContents;

    @JsonField(name = "wishCount")
    public int wishCount;

    @JsonField(name = "myWish")
    public boolean myWish;

    public StayDetailData()
    {

    }

    public StayDetail getStayDetail()
    {
        StayDetail stayDetail = new StayDetail();

        stayDetail.index = index;
        stayDetail.name = name;
        stayDetail.latitude = latitude;
        stayDetail.longitude = longitude;
        stayDetail.address = address;
        stayDetail.category = category;

        try
        {
            stayDetail.grade = Stay.Grade.valueOf(grade);
        } catch (Exception e)
        {
            stayDetail.grade = Stay.Grade.etc;
        }

        stayDetail.price = price;
        stayDetail.discount = discount;
        stayDetail.ratingPersons = ratingPersons;
        stayDetail.ratingValue = ratingValue;
        stayDetail.ratingShow = ratingShow;
        stayDetail.benefit = benefit;
        stayDetail.wishCount = wishCount;
        stayDetail.myWish = myWish;
        stayDetail.singleStay = singleStay;
        stayDetail.overseas = overseas;
        stayDetail.waitingForBooking = waitingForBooking;

        if (DailyTextUtils.isTextEmpty(benefitWarning) == false)
        {
            benefitContents.add(benefitWarning);
        }

        stayDetail.setBenefitContentList(benefitContents);

        // Pictogram
        List<StayDetail.Pictogram> pictogramList = new ArrayList<>();

        // 주차
        if (parking == true)
        {
            pictogramList.add(StayDetail.Pictogram.PARKING);
        }

        // 주차금지
        if (noParking == true)
        {
            pictogramList.add(StayDetail.Pictogram.NO_PARKING);
        }

        // 수영장
        if (pool == true)
        {
            pictogramList.add(StayDetail.Pictogram.POOL);
        }

        // 피트니스
        if (fitness == true)
        {
            pictogramList.add(StayDetail.Pictogram.FITNESS);
        }

        // 사우나
        if (sauna == true)
        {
            pictogramList.add(StayDetail.Pictogram.SAUNA);
        }

        // 비지니스 센터
        if (businessCenter == true)
        {
            pictogramList.add(StayDetail.Pictogram.BUSINESS_CENTER);
        }

        // 키즈 플레이 룸
        if (kidsPlayRoom == true)
        {
            pictogramList.add(StayDetail.Pictogram.KIDS_PLAY_ROOM);
        }

        // 바베큐
        if (sharedBBQ == true)
        {
            pictogramList.add(StayDetail.Pictogram.SHARED_BBQ);
        }

        // 애완동물
        if (pet == true)
        {
            pictogramList.add(StayDetail.Pictogram.PET);
        }

        stayDetail.setPictogramList(pictogramList);

        // 이미지
        List<DetailImageInformation> detailImageInformationList = new ArrayList<>();

        if (imgPath != null && imgPath.size() > 0)
        {
            Iterator<String> keyList = imgPath.keySet().iterator();

            while (keyList.hasNext())
            {
                String key = keyList.next();

                for (ImageInformationData imageInformationData : imgPath.get(key))
                {
                    DetailImageInformation detailImageInformation = new DetailImageInformation();

                    ImageMap imageMap = new ImageMap();
                    imageMap.smallUrl = null;
                    imageMap.mediumUrl = imgUrl + key + imageInformationData.name;
                    imageMap.bigUrl = imgUrl + key + imageInformationData.name;

                    detailImageInformation.caption = imageInformationData.description;
                    detailImageInformation.setImageMap(imageMap);

                    detailImageInformationList.add(detailImageInformation);
                }
            }
        }

        stayDetail.setImageInformationList(detailImageInformationList);

        // Room
        List<StayRoom> stayRoomList = new ArrayList<>();

        if (rooms != null && rooms.size() > 0)
        {
            for (RoomData roomData : rooms)
            {
                stayRoomList.add(roomData.getRoom());
            }
        }

        stayDetail.setRoomList(stayRoomList);

        // Detail
        stayDetail.setDescriptionList(details);


        return stayDetail;
    }

    @JsonObject
    static class ImageInformationData
    {
        @JsonField(name = "description")
        public String description;

        @JsonField(name = "name")
        public String name;

        public ImageInformationData()
        {

        }
    }

    @JsonObject
    static class RoomData
    {
        @JsonField(name = "roomIdx")
        public int roomIndex;

        @JsonField(name = "roomName")
        public String roomName;

        @JsonField(name = "price")
        public int price;

        @JsonField(name = "roomBenefit")
        public String roomBenefit;

        @JsonField(name = "tv")
        public boolean hasTV;

        @JsonField(name = "pc")
        public boolean hasPC;

        @JsonField(name = "spaWallpool")
        public boolean hasSpaWhirlpool;

        @JsonField(name = "karaoke")
        public boolean hasKaraoke;

        @JsonField(name = "partyRoom")
        public boolean hasPartyRoom;

        @JsonField(name = "privateBbq")
        public boolean hasPrivateBBQ;

        @JsonField(name = "discountAverage")
        public int discountAverage;

        @JsonField(name = "discountTotal")
        public int discountTotal;

        @JsonField(name = "description1")
        public String description1;

        @JsonField(name = "description2")
        public String description2;

        @JsonField(name = "refundType")
        public String refundType;

        public RoomData()
        {

        }

        public StayRoom getRoom()
        {
            final String NRD = "nrd";

            StayRoom stayRoom = new StayRoom();

            stayRoom.index = roomIndex;
            stayRoom.name = roomName;
            stayRoom.price = price;
            stayRoom.benefit = roomBenefit;
            stayRoom.hasTV = hasTV;
            stayRoom.hasPC = hasPC;
            stayRoom.hasSpaWhirlpool = hasSpaWhirlpool;
            stayRoom.hasKaraoke = hasKaraoke;
            stayRoom.hasPartyRoom = hasPartyRoom;
            stayRoom.hasPrivateBBQ = hasPrivateBBQ;
            stayRoom.discountAverage = discountAverage;
            stayRoom.discountTotal = discountTotal;
            stayRoom.description1 = description1;
            stayRoom.description2 = description2;
            stayRoom.refundType = refundType;

            if (DailyTextUtils.isTextEmpty(refundType) == false && NRD.equalsIgnoreCase(refundType) == true)
            {
                stayRoom.nrd = true;
            } else
            {
                stayRoom.nrd = false;
            }

            return stayRoom;
        }
    }
}
