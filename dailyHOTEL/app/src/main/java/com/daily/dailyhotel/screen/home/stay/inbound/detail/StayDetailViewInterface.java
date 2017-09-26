package com.daily.dailyhotel.screen.home.stay.inbound.detail;

import android.app.Dialog;

import com.daily.base.BaseDialogViewInterface;
import com.daily.dailyhotel.entity.StayBookDateTime;
import com.daily.dailyhotel.entity.StayDetail;

import io.reactivex.Observable;

public interface StayDetailViewInterface extends BaseDialogViewInterface
{
    Observable<Boolean> showRoomList(boolean animation);

    Observable<Boolean> hideRoomList(boolean animation);

    void setStayDetail(StayBookDateTime stayBookDateTime, StayDetail stayOutboundDetail);

    Observable<Boolean> getSharedElementTransition(int gradientType);

    void setInitializedImage(String url);

    void setInitializedTransLayout(String name, String url);

    void setTransitionVisible(boolean visible);

    void setSharedElementTransitionEnabled(boolean enabled, int gradientType);

    void setBottomButtonLayout(int status);

    void setDetailImageCaption(String caption);

    void setPriceType(StayDetailPresenter.PriceType priceType);

    void showConciergeDialog(Dialog.OnDismissListener listener);

    void showShareDialog(Dialog.OnDismissListener listener);

    void scrollTop();
}
