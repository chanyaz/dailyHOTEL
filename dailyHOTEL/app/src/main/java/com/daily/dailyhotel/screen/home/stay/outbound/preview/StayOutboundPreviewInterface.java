package com.daily.dailyhotel.screen.home.stay.outbound.preview;

import com.daily.base.BaseDialogViewInterface;
import com.daily.dailyhotel.entity.DetailImageInformation;

import java.util.List;

public interface StayOutboundPreviewInterface extends BaseDialogViewInterface
{
    void setStayRating(float rating);

    void setStayName(String stayName);

    void setImages(List<DetailImageInformation> imageList);

    void setRoomInformation(boolean soldOut, int roomCount, int night, int minPrice, int maxPrice);

    void showAnimation();

    void hideAnimation();
}
