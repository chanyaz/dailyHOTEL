package com.daily.dailyhotel.entity;

import android.content.Context;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.daily.base.util.ScreenUtils;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.Map;

public abstract class BaseDetailImage
{
    public String caption;

    public abstract void setImage(Context context, SimpleDraweeView simpleDraweeView);

    public abstract Map getDataMap();

    public BaseDetailImage()
    {

    }

    public BaseDetailImage(Map<String, String> map)
    {

    }

    void setImageViewHeight(Context context, ImageView imageView, int width, int height)
    {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();

        float scale = (float) ScreenUtils.getScreenWidth(context) / width;
        int viewHeight = (int) (scale * height);

        if (layoutParams == null)
        {
            layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, viewHeight);
        } else
        {
            layoutParams.height = viewHeight;
        }

        imageView.setLayoutParams(layoutParams);
    }
}