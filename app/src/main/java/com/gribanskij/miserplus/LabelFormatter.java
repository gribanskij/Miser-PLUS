package com.gribanskij.miserplus;

import android.app.Activity;

import com.jjoe64.graphview.DefaultLabelFormatter;

import java.util.Calendar;

/**
 * Created by santy on 08.10.2017.
 */

public class LabelFormatter extends DefaultLabelFormatter {


    private String[] month_names;

    public LabelFormatter(Activity activity) {
        super();
        this.month_names = new String[]{
                activity.getString(R.string.month_jan),
                activity.getString(R.string.month_feb),
                activity.getString(R.string.month_mar),
                activity.getString(R.string.month_apr),
                activity.getString(R.string.month_may),
                activity.getString(R.string.month_jun),
                activity.getString(R.string.month_jul),
                activity.getString(R.string.month_aug),
                activity.getString(R.string.month_sep),
                activity.getString(R.string.month_oct),
                activity.getString(R.string.month_nov),
                activity.getString(R.string.month_dec)
        };
    }

    @Override
    public String formatLabel(double value, boolean isValueX) {
        if (!isValueX) {
            return super.formatLabel(value, isValueX);
        } else {
            return getMonthName(value);
        }
    }

    private String getMonthName(double v) {

        String month_name = "";

        if ((v % 1) != 0) return month_name;
        int val = (int) v;

        Calendar calendar = Calendar.getInstance();
        int current_month = calendar.get(Calendar.MONTH);

        if (val + current_month < 12) {
            month_name = month_names[val + current_month];
        } else {
            month_name = month_names[(val + current_month) - 12];
        }
        return month_name;
    }

    private String getSum(double value) {


        return null;
    }
}
