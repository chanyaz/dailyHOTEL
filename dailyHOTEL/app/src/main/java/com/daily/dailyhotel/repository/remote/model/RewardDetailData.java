package com.daily.dailyhotel.repository.remote.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.daily.dailyhotel.entity.RewardDetail;
import com.daily.dailyhotel.entity.RewardSticker;

import java.util.ArrayList;
import java.util.List;

@JsonObject
public class RewardDetailData
{
    @JsonField(name = "availableRewardCouponCount")
    public int availableRewardCouponCount;

    @JsonField(name = "hasRewardHistory")
    public boolean hasRewardHistory;

    @JsonField(name = "hasRewardCardHistory")
    public boolean hasRewardCardHistory;

    @JsonField(name = "rewardCard")
    public RewardCardData rewardCard;

    @JsonField(name = "stickers")
    public List<StickerData> stickers;

    @JsonField(name = "configurations")
    public ConfigurationsData configurations;

    public RewardDetailData()
    {

    }

    public RewardDetail getRewardDetail()
    {
        RewardDetail rewardDetail = new RewardDetail();

        rewardDetail.availableRewardCouponCount = availableRewardCouponCount;
        rewardDetail.hasRewardHistory = hasRewardHistory;
        rewardDetail.hasRewardCardHistory = hasRewardCardHistory;

        if (rewardCard != null)
        {
            rewardDetail.expiredAt = rewardCard.expiredAt;
            rewardDetail.rewardStickerCount = rewardCard.rewardStickerCount;
        }

        if (stickers != null && stickers.size() > 0)
        {
            List<RewardSticker> rewardStickerList = new ArrayList<>();

            for (StickerData stickerData : stickers)
            {
                rewardStickerList.add(stickerData.getRewardSticker());
            }

            rewardDetail.setRewardStickerList(rewardStickerList);
        }

        if (configurations != null)
        {
            rewardDetail.activeReward = configurations.activeReward;
        }

        return rewardDetail;
    }

    @JsonObject
    static class RewardCardData
    {
        @JsonField(name = "expiredAt")
        public String expiredAt;

        @JsonField(name = "rewardStickerCount")
        public int rewardStickerCount;
    }

    @JsonObject
    static class StickerData
    {
        @JsonField(name = "cardIdx")
        public int cardIdx;

        @JsonField(name = "rewardStickerType")
        public String rewardStickerType;

        public RewardSticker getRewardSticker()
        {
            RewardSticker rewardSticker = new RewardSticker();

            rewardSticker.cardIndex = cardIdx;
            rewardSticker.rewardStickerType = rewardStickerType;

            return rewardSticker;
        }
    }
}
