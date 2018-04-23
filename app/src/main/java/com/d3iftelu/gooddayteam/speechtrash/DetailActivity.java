package com.d3iftelu.gooddayteam.speechtrash;

import android.content.Intent;
import android.graphics.DashPathEffect;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.d3iftelu.gooddayteam.speechtrash.chart.MyMarkerView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity implements OnChartGestureListener, OnChartValueSelectedListener {

    public static final String ARGS_DEVICE_NAME = "device name";
    public static final String ARGS_DEVICE_ID = "device id";
    private static final String TAG = DetailActivity.class.getSimpleName();

    private Switch mSwitchStatus;
    private TextView mTextViewDeviceName;
    private LineChart mChart;

    private String mDeviceName;
    private String mDeviceId;

    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;

    ArrayList<Entry> mValuesVolume;
    ArrayList<Entry> mValuesBerat;
    LineDataSet mSetVolume;
    LineDataSet mSetBerat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(ARGS_DEVICE_NAME);
        mDeviceId = intent.getStringExtra(ARGS_DEVICE_ID);

        mSwitchStatus = findViewById(R.id.switch_status);
        mTextViewDeviceName = findViewById(R.id.text_view_device_name);
        mTextViewDeviceName.setText(mDeviceName);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mValuesVolume = new ArrayList<>();
        mValuesBerat = new ArrayList<>();

        readStatus();
        setStatus();
        devineDevice();
    }

    private void readStatus() {
        mDatabaseReference.child("device").child(mDeviceId).child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean status = dataSnapshot.getValue(Boolean.class);
                mSwitchStatus.setChecked(status);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setStatus() {
        mSwitchStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean status) {
                saveStatusToDatabase(status);
            }
        });
    }

    private void saveStatusToDatabase(boolean status) {
        mDatabaseReference.child("device").child(mDeviceId).child("status").setValue(status);
    }

    private void devineDevice() {
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
        leftAxis.setAxisMaximum(20f);
        leftAxis.setAxisMinimum(-5f);

        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(false);

        leftAxis.setDrawLimitLinesBehindData(true);

        mChart.getAxisRight().setEnabled(false);
        readAmpere();

        mChart.animateX(2500);
        Legend l = mChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    private void readAmpere() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        final DatabaseReference myRef = database.getReference();
        myRef.keepSynced(true);
        myRef.child("device").child(mDeviceId).child("value").addValueEventListener(new ValueEventListener() {
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
                readVoltage();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
    }

    private void readVoltage() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        final DatabaseReference myRef = database.getReference();
        myRef.keepSynced(true);

        myRef.child("device").child(mDeviceId).child("value").addValueEventListener(new ValueEventListener() {
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
//            mSetBerat.setDrawFilled(true);
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
