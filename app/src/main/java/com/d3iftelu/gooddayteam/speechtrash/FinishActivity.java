package com.d3iftelu.gooddayteam.speechtrash;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.d3iftelu.gooddayteam.speechtrash.chart.MyMarkerChartView;
import com.d3iftelu.gooddayteam.speechtrash.chart.MyMarkerView;
import com.d3iftelu.gooddayteam.speechtrash.interface_fragment.IOnFocusListenable;
import com.d3iftelu.gooddayteam.speechtrash.model.Message;
import com.d3iftelu.gooddayteam.speechtrash.model.Prediction;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
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

public class FinishActivity extends AppCompatActivity implements OnChartGestureListener, OnChartValueSelectedListener, IOnFocusListenable {

    private static String TAG = "FinishActivity";
    private String mDeviceId, mDeviceName, admin_id;
    private Switch mSwitchStatus;
    private TextView mTextViewDeviceName;
    private LineChart mChart;
    private ProcessingHelper processingHelper;
    private BarChart mBarChart;

    private final String[] MONTH_ARRAY = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};


    private float groupSpace = 0.08f;
    private float barSpace = 0.06f;
    private float barWidth = 0.4f;
    private int startMount = 1;
    private int groupCount = 0;
    private long time;


    private ArrayList<BarEntry> volumeValueBar = new ArrayList<>();
    private ArrayList<BarEntry> beratValueBar = new ArrayList<>();

    private FirebaseUser mCurrentUser;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;

    ArrayList<Entry> mValuesVolume;
    ArrayList<Entry> mValuesBerat;
    LineDataSet mSetVolume;
    LineDataSet mSetBerat;

    String[] curentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);

        Intent intent = getIntent();
        mDeviceId = intent.getStringExtra(DetailActivity.ARGS_DEVICE_ID);
        mDeviceName = intent.getStringExtra(DetailActivity.ARGS_DEVICE_NAME);
        setTitle(mDeviceId);
        processingHelper = new ProcessingHelper();
        time = processingHelper.getDateNow();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mSwitchStatus = findViewById(R.id.switch_status);
        mTextViewDeviceName = findViewById(R.id.text_view_device_name);
        mTextViewDeviceName.setText(mDeviceName);

        mValuesVolume = new ArrayList<>();
        mValuesBerat = new ArrayList<>();

        // ChartLine
        mChart = findViewById(R.id.line_chart);
        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);
        mChart.setDrawGridBackground(false);

        mChart.getDescription().setEnabled(false);

        mChart.setTouchEnabled(true);

        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setPinchZoom(true);

        MyMarkerView mv = new MyMarkerView(this, R.layout.marker_view);
        mv.setChartView(mChart); // For bounds control
        mChart.setMarker(mv); // Set the marker to the chart

        // x-axis limit line
        LimitLine llXAxis = new LimitLine(10f, "Index 10");
        llXAxis.setLineWidth(4f);
        llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);

        XAxis xAxis = mChart.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setAxisMaximum(120f);
        leftAxis.setAxisMinimum(-2f);

        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(false);

        leftAxis.setDrawLimitLinesBehindData(true);

        mChart.getAxisRight().setEnabled(false);
        readVolume();

        mChart.animateX(2500);
        Legend l = mChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);

        // barChart
        curentDate = getCurentDate();
        groupCount = Integer.parseInt(curentDate[2]);

        mBarChart = (BarChart) findViewById(R.id.bar_chart_group);
        mBarChart.setOnChartValueSelectedListener(this);
        mBarChart.getDescription().setEnabled(false);

        // scaling can now only be done on x- and y-axis separately
        mBarChart.setPinchZoom(false);

        mBarChart.setDrawBarShadow(false);

        mBarChart.setDrawGridBackground(false);

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        MyMarkerChartView marker = new MyMarkerChartView(this, R.layout.custom_marker_view);
        marker.setChartView(mBarChart); // For bounds control
        mBarChart.setMarker(marker); // Set the marker to the chart

        Legend legendBar = mBarChart.getLegend();
        legendBar.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legendBar.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legendBar.setOrientation(Legend.LegendOrientation.VERTICAL);
        legendBar.setDrawInside(true);
        legendBar.setYOffset(0f);
        legendBar.setXOffset(10f);
        legendBar.setYEntrySpace(0f);
        legendBar.setTextSize(8f);

        XAxis xAxisBar = mBarChart.getXAxis();
        xAxisBar.setGranularity(1f);
        xAxisBar.setCenterAxisLabels(true);
        xAxisBar.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return String.valueOf((int) value);
            }
        });

        YAxis leftAxisBar = mBarChart.getAxisLeft();
        leftAxisBar.setAxisMaximum(120f);
        leftAxisBar.setAxisMinimum(0f);

//        YAxis leftAxis = mBarChart.getAxisLeft();
//        leftAxis.setValueFormatter(new LargeValueFormatter());
//        leftAxis.setDrawGridLines(false);
//        leftAxis.setSpaceTop(35f);
//        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        mBarChart.getAxisRight().setEnabled(false);

        getBarData();
        readStatus();
        setViewOfChart();
    }

    private void readStatus() {
        mDatabaseReference.child("device").child(mDeviceId).child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean status = dataSnapshot.getValue(Boolean.class);
                mSwitchStatus.setChecked(status);
                if (!status) {
                    mSwitchStatus.setClickable(false);
                }
                mSwitchStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isStatus) {
                        saveStatusToDatabase(isStatus);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mDatabaseReference.child("device").child(mDeviceId).child("admin_id").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                admin_id = dataSnapshot.getValue(String.class);
                Log.i(TAG, "userID : " + admin_id);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void saveStatusToDatabase(boolean status) {
        if (!status){
            saveTimeFisnish();
            sendToMessage();
            saveHistoryFull();
        }
        mDatabaseReference.child("device").child(mDeviceId).child("status").setValue(status);
    }

    private void saveTimeFisnish() {
        mDatabaseReference.child("device").child(mDeviceId).child("history").child("lastKey").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String lastKey = dataSnapshot.getValue(String.class);
                    Log.i(TAG, "lastKey : " + lastKey);

                    mDatabaseReference.child("device").child(mDeviceId).child("history").child("full").child(lastKey).child("finishDate").setValue(String.valueOf(time));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void saveHistoryFull() {
        String date = String.valueOf(time);
        Prediction prediction = new Prediction(date,date);
        String mKey = mDatabaseReference.child("device").child(mDeviceId).child("history").child("full").push().getKey();
        mDatabaseReference.child("device").child(mDeviceId).child("history").child("lastKey").setValue(mKey);
        mDatabaseReference.child("device").child(mDeviceId).child("history").child("full").child(mKey).setValue(prediction);
    }

    private void sendToMessage() {
        String waktu = Long.toString(time);
        String pesan = "Tempat Sampah '"+mDeviceName+"' sudah diambil!";
        Message message = new Message(admin_id, pesan, mCurrentUser.getDisplayName(), waktu);
        mDatabaseReference.child("device").child(mDeviceId).child("messages").push().setValue(message);
    }

    private void getBarData(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("device");
        myRef.keepSynced(true);

        myRef.child(mDeviceId).child("history").child("volume").child(curentDate[0]).child(curentDate[1]).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (int i = 0; i < Integer.parseInt(curentDate[2]); i++) {
                    volumeValueBar.add(new BarEntry(i, 0));
                }
                for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                    long nilai = userSnapshot.getValue(Long.class);
                    int position = Integer.parseInt(userSnapshot.getKey());
                    volumeValueBar.set(position-1, new BarEntry(position-1, nilai));
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
                    beratValueBar.add(new BarEntry(i, 0));
                }
                for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                    long nilai = userSnapshot.getValue(Long.class);
                    int position = Integer.parseInt(userSnapshot.getKey());

                    Log.d(TAG, "onDataChange: " + (position - beratValueBar.size()));
                    if(beratValueBar.size()<position){
                        groupCount = position;
                        for (int i = beratValueBar.size(); i < position; i++) {
                            volumeValueBar.add(new BarEntry(i, 0));
                            beratValueBar.add(new BarEntry(i, 0));
                        }
                    }

                    beratValueBar.set(position-1, new BarEntry(position-1, nilai));
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

        if (mBarChart.getData() != null && mBarChart.getData().getDataSetCount() > 0) {

            volume = (BarDataSet) mBarChart.getData().getDataSetByIndex(0);
            berat = (BarDataSet) mBarChart.getData().getDataSetByIndex(1);
            volume.setValues(volumeValueBar);
            berat.setValues(beratValueBar);
            mBarChart.getData().notifyDataChanged();
            mBarChart.notifyDataSetChanged();

        } else {
            volume = new BarDataSet(volumeValueBar, "Volume");
            volume.setColor(Color.rgb(104, 241, 175));
            berat = new BarDataSet(beratValueBar, "Berat");
            berat.setColor(Color.rgb(164, 228, 251));

            BarData data = new BarData(volume, berat);
            data.setValueFormatter(new LargeValueFormatter());

            mBarChart.setData(data);
        }

        // specify the width each bar should have
        mBarChart.getBarData().setBarWidth(barWidth);

        // restrict the x-axis range
        mBarChart.getXAxis().setAxisMinimum(startMount);

        // barData.getGroupWith(...) is a helper that calculates the width each group needs based on the provided parameters
        mBarChart.getXAxis().setAxisMaximum(startMount + mBarChart.getBarData().getGroupWidth(groupSpace, barSpace) * groupCount);
        mBarChart.groupBars(startMount, groupSpace, barSpace);
        mBarChart.invalidate();

    }

    private String[] getCurentDate(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy MM dd");
        Date date = new Date();
        String[] curentDate = dateFormat.format(date).split(" ");
        curentDate[1] = MONTH_ARRAY[Integer.parseInt(curentDate[1])-1];
        return curentDate;
    }

    private void readVolume() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        final DatabaseReference myRef = database.getReference();
        myRef.keepSynced(true);
        myRef.child("device").child(mDeviceId).child("realtime").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mValuesVolume.clear();
                int i = 0;
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    int data = userSnapshot.child("volume").getValue(Integer.class);
                    Log.i(TAG, "onDataChange: " + data);
                    final Entry entry = new Entry(i, data);
                    mValuesVolume.add(entry);
                    i++;
                }
                readBerat();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
    }

    private void readBerat() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        final DatabaseReference myRef = database.getReference();
        myRef.keepSynced(true);

        myRef.child("device").child(mDeviceId).child("realtime").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mValuesBerat.clear();
                int i = 0;
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    try {
                        int data = userSnapshot.child("berat").getValue(Integer.class);
                        Log.i(TAG, "onDataChange: " + data);
                        final Entry entry = new Entry(i, data);
                        mValuesBerat.add(entry);
                        i++;
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }

                setData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
    }

    private void setData() {
        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            mSetVolume = (LineDataSet) mChart.getData().getDataSetByIndex(0);
            mSetVolume.setValues(mValuesVolume);
            mSetBerat = (LineDataSet) mChart.getData().getDataSetByIndex(0);
            mSetBerat.setValues(mValuesBerat);

            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            mSetVolume = new LineDataSet(mValuesVolume, "Volume");
            mSetBerat = new LineDataSet(mValuesBerat, "Berat");

            mSetVolume.setDrawIcons(false);
            mSetBerat.setDrawIcons(false);

            int color1 = ResourcesCompat.getColor(getResources(), R.color.colorChart1, null);
            mSetVolume.enableDashedHighlightLine(10f, 5f, 0f);
            mSetVolume.setColor(color1);
            mSetVolume.setCircleColor(color1);
            mSetVolume.setLineWidth(1f);
            mSetVolume.setCircleRadius(3f);
            mSetVolume.setDrawCircleHole(false);
            mSetVolume.setValueTextSize(9f);
            mSetVolume.setDrawValues(false);
//            mSetVolume.setDrawFilled(true);
            mSetVolume.setFormLineWidth(1f);
            mSetVolume.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            mSetVolume.setFormSize(15.f);
            mSetVolume.setFillColor(color1);

            int color2 = ResourcesCompat.getColor(getResources(), R.color.colorChart2, null);
            mSetBerat.enableDashedHighlightLine(10f, 5f, 0f);
            mSetBerat.setColor(color2);
            mSetBerat.setCircleColor(color2);
            mSetBerat.setLineWidth(1f);
            mSetBerat.setCircleRadius(3f);
            mSetBerat.setDrawCircleHole(false);
            mSetBerat.setValueTextSize(9f);
            mSetBerat.setDrawValues(false);
//            mSetPrediction.setDrawFilled(true);
            mSetBerat.setFormLineWidth(1f);
            mSetBerat.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            mSetBerat.setFormSize(15.f);
            mSetBerat.setFillColor(color2);

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(mSetVolume);
            dataSets.add(mSetBerat);

            LineData data = new LineData(dataSets);
            mChart.setData(data);
        }
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "START, x: " + me.getX() + ", y: " + me.getY());
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "END, lastGesture: " + lastPerformedGesture);

        // un-highlight values after the gesture is finished and no single-tap
        if (lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP)
            mChart.highlightValues(null); // or highlightTouch(null) for callback to onNothingSelected(...)
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
        Log.i("LongPress", "Chart longpressed.");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Log.i("DoubleTap", "Chart double-tapped.");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Log.i("SingleTap", "Chart single-tapped.");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        Log.i("Fling", "Chart flinged. VeloX: " + velocityX + ", VeloY: " + velocityY);
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        Log.i("Scale / Zoom", "ScaleX: " + scaleX + ", ScaleY: " + scaleY);
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        Log.i("Translate / Move", "dX: " + dX + ", dY: " + dY);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
        Log.i("LOWHIGH", "low: " + mChart.getLowestVisibleX() + ", high: " + mChart.getHighestVisibleX());
        Log.i("MIN MAX", "xmin: " + mChart.getXChartMin() + ", xmax: " + mChart.getXChartMax() + ", ymin: " + mChart.getYChartMin() + ", ymax: " + mChart.getYChartMax());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }
}
