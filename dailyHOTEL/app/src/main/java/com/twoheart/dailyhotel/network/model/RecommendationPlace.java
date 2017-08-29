package com.twoheart.dailyhotel.network.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonIgnore;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.Map;

@JsonObject
public abstract class RecommendationPlace
{
    @JsonField(name = "name")
    public String name;

    @JsonField(name = "regionName")
    public String regionName;

    @JsonField(name = "addrSummary")
    public String addrSummary;

    @JsonField(name = "category")
    public String category;

    @JsonField(name = "discount")
    public int discount;

    @JsonField(name = "price")
    public int price;

    @JsonField(name = "imgPathMain")
    public Map<String, Object> imgPathMain;

    @JsonField(name = "latitude")
    public double latitude;

    @JsonField(name = "longitude")
    public double longitude;

    @JsonField(name = "districtName")
    public String districtName;

    @JsonField(name = "rating")
    public int rating;

    @JsonField(name = "benefit")
    public String benefit;

    @JsonField(name = "isSoldOut")
    public boolean isSoldOut;

    @JsonField(name = "truevr")
    public boolean truevr;

    @JsonField(name = "stickerIdx")
    public Integer stickerIdx;

    @JsonField(name = "distance")
    public int distance;

    @JsonIgnore
    public String imageUrl;

    @JsonIgnore
    public String stickerUrl;

    @JsonIgnore
    public int entryPosition;
}
