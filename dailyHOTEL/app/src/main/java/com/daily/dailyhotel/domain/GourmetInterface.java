package com.daily.dailyhotel.domain;

import com.daily.dailyhotel.entity.GourmetBookDateTime;
import com.daily.dailyhotel.entity.GourmetDetail;
import com.daily.dailyhotel.entity.ReviewScores;
import com.daily.dailyhotel.entity.TrueReviews;
import com.daily.dailyhotel.entity.TrueVR;
import com.daily.dailyhotel.entity.WishResult;
import com.twoheart.dailyhotel.model.Gourmet;
import com.twoheart.dailyhotel.model.GourmetParams;

import java.util.List;

import io.reactivex.Observable;

public interface GourmetInterface
{
    Observable<List<Gourmet>> getList(GourmetParams gourmetParams);

    Observable<GourmetDetail> getDetail(int gourmetIndex, GourmetBookDateTime gourmetBookDateTime);

    Observable<Boolean> getHasCoupon(int gourmetIndex, GourmetBookDateTime gourmetBookDateTime);

    Observable<WishResult> addWish(int gourmetIndex);

    Observable<WishResult> removeWish(int gourmetIndex);

    Observable<ReviewScores> getReviewScores(int gourmetIndex);

    Observable<TrueReviews> getTrueReviews(int gourmetIndex, int page, int limit);

    Observable<List<TrueVR>> getTrueVR(int gourmetIndex);
}
