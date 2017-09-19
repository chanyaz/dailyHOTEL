package com.daily.dailyhotel.entity;

public class ObjectItem
{
    public static final int TYPE_ENTRY = 0;
    public static final int TYPE_SECTION = 1;
    public static final int TYPE_EVENT_BANNER = 2;
    public static final int TYPE_FOOTER_VIEW = 3;
    public static final int TYPE_LOADING_VIEW = 4;
    public static final int TYPE_FOOTER_GUIDE_VIEW = 5;
    public static final int TYPE_HEADER_VIEW = 6;

    public int mType;
    private Object mItem;

    public ObjectItem(int type, Object item)
    {
        mType = type;
        mItem = item;
    }

    public <T> T getItem()
    {
        return (T) mItem;
    }
}
