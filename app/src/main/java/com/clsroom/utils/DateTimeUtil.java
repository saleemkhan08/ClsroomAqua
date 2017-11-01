package com.clsroom.utils;

import java.util.Calendar;

public class DateTimeUtil
{
    public static String getKey()
    {
        Calendar calendar = Calendar.getInstance();

        return calendar.get(Calendar.YEAR)
                + get2DigitNum(calendar.get(Calendar.MONTH) + 1)
                + get2DigitNum(calendar.get(Calendar.DAY_OF_MONTH))
                + get2DigitNum(calendar.get(Calendar.HOUR_OF_DAY))
                + get2DigitNum(calendar.get(Calendar.MINUTE))
                + get2DigitNum(calendar.get(Calendar.SECOND));
    }

    public static String get2DigitNum(int num)
    {
        return (num < 10) ? "0" + num : "" + num;
    }
}
