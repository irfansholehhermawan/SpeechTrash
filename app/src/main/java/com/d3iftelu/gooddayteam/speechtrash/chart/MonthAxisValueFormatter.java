package com.d3iftelu.gooddayteam.speechtrash.chart;

import android.util.Log;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

public class MonthAxisValueFormatter implements IAxisValueFormatter {
    private String[] mMonths = new String[]{
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    private BarLineChartBase<?> chart;

    /**
     * set mounth in diagram
     * @param chart chart wil add x axist
     */
    public MonthAxisValueFormatter(BarLineChartBase<?> chart) {
        this.chart = chart;
    }

    /**
     * set the mount in the chatr
     * @param value number of axist
     * @param axis position axist
     * @return mounth
     */
    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        int index = (int) value-1;
        Log.i("Coba", "getFormattedValue: " + index);
        if(index>=0 && index < 12)
            return mMonths[index];
//            return String.valueOf(index);
        else
            return "";
    }
}
