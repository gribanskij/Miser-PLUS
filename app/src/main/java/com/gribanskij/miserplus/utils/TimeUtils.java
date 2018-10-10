package com.gribanskij.miserplus.utils;


import java.util.Calendar;

public class TimeUtils {

    private final static long DURATION_DAY_MS = 86400000; //(24 * 60 * 60 * 1000);
    private final static long DURATION_WEEK_MS = 604800000;// 7 * DURATION_DAY_MS;
    private final static int[] CALENDAR_TIME_FIELDS = {Calendar.HOUR_OF_DAY, Calendar.HOUR, Calendar.AM_PM, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND};


    private TimeUtils() {
    }

    public static long getBegin_month() {
        Calendar mCalendar = Calendar.getInstance();
        setZeroTime(mCalendar);
        mCalendar.set(Calendar.DAY_OF_MONTH, 1);
        return mCalendar.getTimeInMillis();
    }

    public static long getEnd_month() {
        Calendar mCalendar = Calendar.getInstance();
        return getBegin_month() + (DURATION_DAY_MS * mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));

    }

    public static long getBegin_week(int first_day_of_week) {
        Calendar mCalendar = Calendar.getInstance();
        setZeroTime(mCalendar);
        if (mCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            mCalendar.add(Calendar.DAY_OF_MONTH, -6);
        } else {
            mCalendar.add(Calendar.DAY_OF_MONTH, -(mCalendar.get(Calendar.DAY_OF_WEEK) - 2));
        }

        if (first_day_of_week == Calendar.MONDAY) {
            return mCalendar.getTimeInMillis();
        } else if (first_day_of_week == Calendar.SUNDAY) {
            return mCalendar.getTimeInMillis() - DURATION_DAY_MS;
        }
        return mCalendar.getTimeInMillis();

    }

    public static long getBegin_day() {
        Calendar mCalendar = Calendar.getInstance();
        setZeroTime(mCalendar);
        return mCalendar.getTimeInMillis();
    }

    public static long getEnd_week(int first_day_of_week) {
        return getBegin_week(first_day_of_week) + DURATION_WEEK_MS;
    }

    public static long getEnd_day() {
        return getBegin_day() + DURATION_DAY_MS;
    }

    private static void setZeroTime(Calendar c) {
        for (int i : CALENDAR_TIME_FIELDS) {
            c.clear(i);
        }
    }

}
