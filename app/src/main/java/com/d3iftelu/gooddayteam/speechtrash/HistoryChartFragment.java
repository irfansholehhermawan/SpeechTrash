package com.d3iftelu.gooddayteam.speechtrash;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

    private BarChart mChart;

    private final String[] MONTH_ARRAY = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};


    private float groupSpace = 0.08f;
    private float barSpace = 0.06f;
    private float barWidth = 0.4f;
    private int startMount = 1;
    private int groupCount = 0;


    private ArrayList<BarEntry> volumeValue = new ArrayList<>();
    private ArrayList<BarEntry> beratValue = new ArrayList<>();

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
        groupCount = Integer.parseInt(curentDate[2]);

        mChart = (BarChart) view.findViewById(R.id.bar_chart_group);
        mChart.setOnChartValueSelectedListener(this);
        mChart.getDescription().setEnabled(false);

        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        mChart.setDrawBarShadow(false);

        mChart.setDrawGridBackground(false);

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        MyMarkerChartView mv = new MyMarkerChartView(getContext(), R.layout.custom_marker_view);
        mv.setChartView(mChart); // For bounds control
        mChart.setMarker(mv); // Set the marker to the chart

        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(true);
        l.setYOffset(0f);
        l.setXOffset(10f);
        l.setYEntrySpace(0f);
        l.setTextSize(8f);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return String.valueOf((int) value);
            }
        });

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setAxisMaximum(120f);
        leftAxis.setAxisMinimum(0f);

//        YAxis leftAxis = mChart.getAxisLeft();
//        leftAxis.setValueFormatter(new LargeValueFormatter());
//        leftAxis.setDrawGridLines(false);
//        leftAxis.setSpaceTop(35f);
//        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        mChart.getAxisRight().setEnabled(false);

        getBarData();

        setViewOfChart();

        return view;
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    private void getBarData(){
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
                setViewOfChart();
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
                        groupCount = position;
                        for (int i = beratValue.size(); i < position; i++) {
                            volumeValue.add(new BarEntry(i, 0));
                            beratValue.add(new BarEntry(i, 0));
                        }
                    }

                    beratValue.set(position-1, new BarEntry(position-1, nilai));
                }
                setViewOfChart();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
    }

    private void setViewOfChart(){
        BarDataSet volume, berat;

        if (mChart.getData() != null && mChart.getData().getDataSetCount() > 0) {

            volume = (BarDataSet) mChart.getData().getDataSetByIndex(0);
            berat = (BarDataSet) mChart.getData().getDataSetByIndex(1);
            volume.setValues(volumeValue);
            berat.setValues(beratValue);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();

        } else {
            volume = new BarDataSet(volumeValue, "Volume");
            volume.setColor(Color.rgb(249, 156, 147));
            berat = new BarDataSet(beratValue, "Berat");
            berat.setColor(Color.rgb(112, 234, 255));

            BarData data = new BarData(volume, berat);
            data.setValueFormatter(new LargeValueFormatter());

            mChart.setData(data);
        }

        // specify the width each bar should have
        mChart.getBarData().setBarWidth(barWidth);

        // restrict the x-axis range
        mChart.getXAxis().setAxisMinimum(startMount);

        // barData.getGroupWith(...) is a helper that calculates the width each group needs based on the provided parameters
        mChart.getXAxis().setAxisMaximum(startMount + mChart.getBarData().getGroupWidth(groupSpace, barSpace) * groupCount);
        mChart.groupBars(startMount, groupSpace, barSpace);
        mChart.invalidate();

    }

    private String[] getCurentDate(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy MM dd");
        Date date = new Date();
        String[] curentDate = dateFormat.format(date).split(" ");
        curentDate[1] = MONTH_ARRAY[Integer.parseInt(curentDate[1])-1];
        return curentDate;
    }
}