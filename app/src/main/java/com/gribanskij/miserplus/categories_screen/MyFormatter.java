package com.gribanskij.miserplus.categories_screen;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;


public class MyFormatter implements IValueFormatter, IAxisValueFormatter {

    private PercentFormatter percentFormatter = new PercentFormatter();

    public MyFormatter() {
    }


    // IValueFormatter

    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        if (value == 0) return "";
        return percentFormatter.getFormattedValue(value, entry, dataSetIndex, viewPortHandler);
    }

    // IAxisValueFormatter

    public String getFormattedValue(float value, AxisBase axis) {
        if (value == 0) return "";
        return percentFormatter.getFormattedValue(value, axis);
    }

    public int getDecimalDigits() {
        return 1;
    }


}
