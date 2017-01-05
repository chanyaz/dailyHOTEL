package com.twoheart.dailyhotel;

import com.twoheart.dailyhotel.util.Util;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Created by android_sam on 2017. 1. 5..
 */

public class DailyMatcher
{
    public static Matcher<String> isEmpty()
    {
        return new TypeSafeMatcher<String>()
        {
            String stringValue;

            @Override
            protected boolean matchesSafely(String item)
            {
                stringValue = item;
                return Util.isTextEmpty(item);
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendValue(stringValue + " is empty");
            }
        };
    }

    public static Matcher<String> isNotEmpty()
    {
        return new TypeSafeMatcher<String>()
        {
            String stringValue;

            @Override
            protected boolean matchesSafely(String item)
            {
                stringValue = item;
                return Util.isTextEmpty(item) == false;
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendValue(stringValue + " is empty");
            }
        };
    }

    public static Matcher<Integer> moreThan(final int defaultValue)
    {
        return new TypeSafeMatcher<Integer>()
        {
            int intValue;

            @Override
            protected boolean matchesSafely(Integer item)
            {
                intValue = item;
                return item >= defaultValue;
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendValue(intValue + " is not over " + defaultValue);
            }
        };
    }
}