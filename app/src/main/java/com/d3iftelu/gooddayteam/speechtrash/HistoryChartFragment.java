package com.d3iftelu.gooddayteam.speechtrash;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.d3iftelu.gooddayteam.speechtrash.chart.MonthAxisValueFormatter;
import com.d3iftelu.gooddayteam.speechtrash.chart.MyMarkerChartView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryChartFragment extends Fragment implements OnChartValueSelectedListener {

    private static String TAG = "HistoryChartFragment";
    private String mDeviceId;

    private BarChart mChartMonthly;
    private BarChart mChartAnnualy;

    private final String[] MONTH_ARRAY = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

    private float groupSpace = 0.08f;
    private float barSpace = 0.06f;
    private float barWidth = 0.4f;
    private int startMount = 1;
    private int groupCountMounthly = 0;
    private int groupCountAnnualy = 12;


    private ArrayList<BarEntry> volumeValue = new ArrayList<>();
    private ArrayList<BarEntry> beratValue = new ArrayList<>();
    private ArrayList<BarEntry> value = new ArrayList<>();

    String[] curentDate;

    public HistoryChartFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history_chart, container, false);

        Intent intent = getActivity().getIntent();
        mDeviceId = intent.getStringExtra(DetailActivity.ARGS_DEVICE_ID);

        curentDate = getCurentDate();
        groupCountMounthly = Integer.parseInt(curentDate[2]);

        mChartMonthly = (BarChart) view.findViewById(R.id.bar_chart_group);
        mChartMonthly.setOnChartValueSelectedListener(this);
        mChartMonthly.getDescription().setEnabled(false);

        // scaling can now only be done on x- and y-axis separately
        mChartMonthly.setPinchZoom(false);

        mChartMonthly.setDrawBarShadow(false);

        mChartMonthly.setDrawGridBackground(false);

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        MyMarkerChartView markerChartMounth = new MyMarkerChartView(getContext(), R.layout.custom_marker_view);
        markerChartMounth.setChartView(mChartMonthly); // For bounds control
        mChartMonthly.setMarker(markerChartMounth); // Set the marker to the chart

        Legend mounthlyLegend = mChartMonthly.getLegend();
        mounthlyLegend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        mounthlyLegend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        mounthlyLegend.setOrientation(Legend.LegendOrientation.VERTICAL);
        mounthlyLegend.setDrawInside(true);
        mounthlyLegend.setYOffset(0f);
        mounthlyLegend.setXOffset(10f);
        mounthlyLegend.setYEntrySpace(0f);
        mounthlyLegend.setTextSize(8f);

        XAxis xAxisMonth = mChartMonthly.getXAxis();
        xAxisMonth.setGranularity(1f);
        xAxisMonth.setCenterAxisLabels(true);
        xAxisMonth.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return String.valueOf((int) value);
            }
        });

        YAxis leftAxisMonth = mChartMonthly.getAxisLeft();
        leftAxisMonth.setAxisMaximum(120f);
        leftAxisMonth.setAxisMinimum(0f);

//        YAxis leftAxis = mChartMonthly.getAxisLeft();
//        leftAxis.setValueFormatter(new LargeValueFormatter());
//        leftAxis.setDrawGridLines(false);
//        leftAxis.setSpaceTop(35f);
//        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        mChartMonthly.getAxisRight().setEnabled(false);
        getBarDataMonth();
        setViewOfChartMonth();

        mChartAnnualy = (BarChart) view.findViewById(R.id.bar_chart);
        mChartAnnualy.setOnChartValueSelectedListener(this);
        mChartAnnualy.getDescription().setEnabled(false);
        mChartAnnualy.setDrawGridBackground(false);
        mChartAnnualy.setPinchZoom(false);
        mChartAnnualy.setDrawBarShadow(false);

        Legend annualyLegend = mChartAnnualy.getLegend();
        annualyLegend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        annualyLegend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        annualyLegend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        annualyLegend.setDrawInside(false);
        annualyLegend.setForm(Legend.LegendForm.SQUARE);
        annualyLegend.setFormSize(9f);
        annualyLegend.setTextSize(9f);
        annualyLegend.setXEntrySpace(8f);

        MyMarkerChartView markerChartAnnualy = new MyMarkerChartView(getContext(), R.layout.custom_marker_view);
        markerChartAnnualy.setChartView(mChartAnnualy); // For bounds control
        mChartAnnualy.setMarker(markerChartAnnualy); // Set the marker to the chart

        YAxis leftAxisAnnualy = mChartAnnualy.getAxisLeft();
        leftAxisAnnualy.setValueFormatter(new LargeValueFormatter());
        leftAxisAnnualy.setDrawGridLines(false);
        leftAxisAnnualy.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        XAxis xAxisAnnualy = mChartAnnualy.getXAxis();
        xAxisAnnualy.setGranularity(1f);
        xAxisAnnualy.setCenterAxisLabels(true);
        xAxisAnnualy.setValueFormatter(new MonthAxisValueFormatter(mChartAnnualy));

        mChartAnnualy.getAxisRight().setEnabled(false);

        setData();

        getBarData();

        return view;
    }

    private void getBarData(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("device");
        myRef.keepSynced(true);

        myRef.child(mDeviceId).child("history").child("annualValue").child(getCurentYear()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    long value = 0;
                    int monthPosition = 0;
                    for (int i = 1; i < 13; i++) {
                        HistoryChartFragment.this.value.add(new BarEntry(i, 0));
                    }
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        long nilai = userSnapshot.getValue(Long.class);
                        for (int i = 0; i < MONTH_ARRAY.length; i++) {
                            if (userSnapshot.getKey().equals(MONTH_ARRAY[i])) {
                                monthPosition = i + 1;
                                break;
                            }
                        }
                        HistoryChartFragment.this.value.set(monthPosition - 1, new BarEntry(monthPosition, nilai));
                        value += nilai;
                    }
                    setData();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
    }

    /**
     * for set dummy data
     */
    private void setData() {
        BarDataSet value;

        if (mChartAnnualy.getData() != null && mChartAnnualy.getData().getDataSetCount() > 0) {
            value = (BarDataSet) mChartAnnualy.getData().getDataSetByIndex(0);
            value.setValues(this.value);
            mChartAnnualy.getData().notifyDataChanged();
            mChartAnnualy.notifyDataSetChanged();
        } else {
            value = new BarDataSet(this.value, "Full in Year " + getCurentYear());
            value.setColor(Color.rgb(104, 241, 175));

            BarData data = new BarData(value);
            data.setValueFormatter(new LargeValueFormatter());
            mChartAnnualy.setData(data);
        }

        // specify the width each bar should have
        mChartAnnualy.getBarData().setBarWidth(barWidth);

        // restrict the x-axis range
        mChartAnnualy.getXAxis().setAxisMinimum(startMount);

        // barData.getGroupWith(...) is a helper that calculates the width each group needs based on the provided parameters
//        mChartAnnualy.getXAxis().setAxisMaximum(startMount + mChartAnnualy.getBarData().getGroupWidth(groupSpace, barSpace) * groupCountAnnualy);
//        mChartAnnualy.groupBars(startMount, groupSpace, barSpace);
        mChartAnnualy.invalidate();
    }

    private String getCurentYear(){
        DateFormat yearFormat = new SimpleDateFormat("yyyy");
        Date date = new Date();
        return yearFormat.format(date);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    private void getBarDataMonth(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("device");
        myRef.keepSynced(true);

        myRef.child(mDeviceId).child("history").child("volume").child(curentDate[0]).child(curentDate[1]).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (int i = 0; i < Integer.parseInt(curentDate[2]); i++) {
                    volumeValue.add(new BarEntry(i, 0));
                }
                for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                    long nilai = userSnapshot.getValue(Long.class);
                    int position = Integer.parseInt(userSnapshot.getKey());
                    volumeValue.set(position-1, new BarEntry(position-1, nilai));
                }
                setViewOfChartMonth();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
        myRef.child(mDeviceId).child("history").child("berat").child(curentDate[0]).child(curentDate[1]).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (int i = 0; i < Integer.parseInt(curentDate[2]); i++) {
                    beratValue.add(new BarEntry(i, 0));
                }
                for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                    long nilai = userSnapshot.getValue(Long.class);
                    int position = Integer.parseInt(userSnapshot.getKey());

                    Log.d(TAG, "onDataChange: " + (position - beratValue.size()));
                    if(beratValue.size()<position){
                        groupCountMounthly = position;
                        for (int i = beratValue.size(); i < position; i++) {
                            volumeValue.add(new BarEntry(i, 0));
                            beratValue.add(new BarEntry(i, 0));
                        }
                    }

                    beratValue.set(position-1, new BarEntry(position-1, nilai));
                }
                setViewOfChartMonth();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
    }

    private void setViewOfChartMonth(){
        BarDataSet volume, berat;

        if (mChartMonthly.getData() != null && mChartMonthly.getData().getDataSetCount() > 0) {

            volume = (BarDataSet) mChartMonthly.getData().getDataSetByIndex(0);
            berat = (BarDataSet) mChartMonthly.getData().getDataSetByIndex(1);
            volume.setValues(volumeValue);
            berat.setValues(beratValue);
            mChartMonthly.getData().notifyDataChanged();
            mChartMonthly.notifyDataSetChanged();

        } else {
            volume = new BarDataSet(volumeValue, "Volume");
            volume.setColor(Color.rgb(249, 156, 147));
            berat = new BarDataSet(beratValue, "Berat");
            berat.setColor(Color.rgb(112, 234, 255));

            BarData data = new BarData(volume, berat);
            data.setValueFormatter(new LargeValueFormatter());

            mChartMonthly.setData(data);
        }

        // specify the width each bar should have
        mChartMonthly.getBarData().setBarWidth(barWidth);

        // restrict the x-axis range
        mChartMonthly.getXAxis().setAxisMinimum(startMount);

        // barData.getGroupWith(...) is a helper that calculates the width each group needs based on the provided parameters
        mChartMonthly.getXAxis().setAxisMaximum(startMount + mChartMonthly.getBarData().getGroupWidth(groupSpace, barSpace) * groupCountMounthly);
        mChartMonthly.groupBars(startMount, groupSpace, barSpace);
        mChartMonthly.invalidate();

    }

    private String[] getCurentDate(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy MM dd");
        Date date = new Date();
        String[] curentDate = dateFormat.format(date).split(" ");
        curentDate[1] = MONTH_ARRAY[Integer.parseInt(curentDate[1])-1];
        return curentDate;
    }
}