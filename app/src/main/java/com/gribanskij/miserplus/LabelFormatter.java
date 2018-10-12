package com.gribanskij.miserplus;

import android.app.Activity;

import com.jjoe64.graphview.DefaultLabelFormatter;

import java.util.Calendar;

/**
 * Created by santy on 08.10.2017.
 */

public class LabelFormatter extends DefaultLabelFormatter {


    private String[] month_names;
    private Calendar calendar = Calendar.getInstance();

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

        String month_name;
        int current_month = calendar.get(Calendar.MONTH);
        int valueX = (int) v;

        switch (valueX) {

            case 6: {

                month_name = month_names[current_month];
                break;
            }
            case 5: {

                month_name = getMonth(current_month - 1);
                break;

            }
            case 4: {
                month_name = getMonth(current_month - 2);
                break;

            }
            case 3: {
                month_name = getMonth(current_month - 3);
                break;

            }
            case 2: {
                month_name = getMonth(current_month - 4);
                break;

            }
            case 1: {
                month_name = getMonth(current_month - 5);
                break;

            }
            case 0: {
                month_name = getMonth(current_month - 6);
                break;

            }
            default:
                month_name = "";
        }

        return month_name;


    }

    private String getSum(double value) {


        return null;
    }

    private String getMonth(int monthIndex) {

        if (monthIndex < 0) {
            monthIndex = 12 + monthIndex;
            return month_names[monthIndex];
        } else {
            return month_names[monthIndex];
        }
    }
}
