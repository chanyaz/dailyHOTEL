package com.daily.base;

import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;

public interface BaseViewInterface
{
    void setContentView(int layoutResID);

    void setContentView(int layoutResID, ViewGroup viewGroup);

    void setToolbarTitle(String title);
}
