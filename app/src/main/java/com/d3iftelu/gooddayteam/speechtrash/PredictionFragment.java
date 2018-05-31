package com.d3iftelu.gooddayteam.speechtrash;


import android.content.Intent;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.d3iftelu.gooddayteam.speechtrash.adapter.PredictionListAdapter;
import com.d3iftelu.gooddayteam.speechtrash.chart.MyMarkerView;
import com.d3iftelu.gooddayteam.speechtrash.interface_fragment.IOnFocusListenable;
import com.d3iftelu.gooddayteam.speechtrash.model.History;
import com.d3iftelu.gooddayteam.speechtrash.model.Prediction;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class PredictionFragment extends Fragment  implements OnChartGestureListener, OnChartValueSelectedListener, IOnFocusListenable {
    private static final String TAG = PredictionFragment.class.getSimpleName();
    private LineChart mChart;
    private String prediction, mDeviceId;
    private TextView mTextViewDay;
    private TextView mTextViewWaktuStart;
    private TextView mTextViewWaktuFinish;
    private TextView mTextViewWaktuNow;
    private TextView mTextViewWaktuPrediction;
    private ImageButton button;
    public ArrayList<History> historyFull = new ArrayList<>();

    DatabaseReference mDatabaseReference;

    ArrayList<Entry> mValuesVolume;
    ArrayList<Entry> mValuesPrediction;
    LineDataSet mSetVolume;
    LineDataSet mSetPrediction;


    public PredictionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_prediction, container, false);

        Intent intent = getActivity().getIntent();
        mDeviceId = intent.getStringExtra(DetailActivity.ARGS_DEVICE_ID);
        mTextViewDay = (TextView) rootView.findViewById(R.id.text_day);
        mTextViewWaktuStart = (TextView) rootView.findViewById(R.id.time_start);
        mTextViewWaktuFinish = (TextView) rootView.findViewById(R.id.time_finish);
        mTextViewWaktuNow = (TextView) rootView.findViewById(R.id.time_now);
        mTextViewWaktuPrediction = (TextView) rootView.findViewById(R.id.time_prediction);
        button = (ImageButton) rootView.findViewById(R.id.button_history_prediction);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoHistoryPrediction();
            }
        });

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mValuesVolume = new ArrayList<>();
        mValuesPrediction = new ArrayList<>();

        // ChartLine
        mChart = rootView.findViewById(R.id.line_chart);
        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);
        mChart.setDrawGridBackground(false);

        mChart.getDescription().setEnabled(false);

        mChart.setTouchEnabled(true);

        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setPinchZoom(true);

        MyMarkerView mv = new MyMarkerView(getContext(), R.layout.marker_view);
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
        dataPrediction();

        mChart.animateX(2500);
        Legend l = mChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);

        readRealtimeData();

        return rootView;
    }

    private void gotoHistoryPrediction(){
        Intent button = new Intent(getContext(), HistoryPrediksiActivity.class);
        button.putExtra(DetailActivity.ARGS_DEVICE_ID, mDeviceId);
        startActivity(button);
    }

    private void dataPrediction() {
        final int[] averagePrediction = new int[1];
        mDatabaseReference.keepSynced(true);
        mDatabaseReference.child("device").child(mDeviceId).child("history").child("full").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                historyFull.clear();
                if (dataSnapshot != null) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        Log.i(TAG, "DataPred " + " : " + userSnapshot.getKey());
                            History history = userSnapshot.getValue(History.class);
                            historyFull.add(history);

                    }
                    averagePrediction[0] = calculateAverage(historyFull);
                    prediction = String.valueOf(calculateAverage(historyFull));
                    mTextViewWaktuPrediction.setText(prediction);
                    Log.i(TAG, "DataPrediksi " + i + " : " + calculateAverage(historyFull));
                } else {
                    Log.i(TAG, "ZONK!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
        mDatabaseReference.child("device").child(mDeviceId).child("history").child("lastKey").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    final String lastKey = dataSnapshot.getValue(String.class);

                    mDatabaseReference.child("device").child(mDeviceId).child("history").child("full").child(lastKey).child("startDate").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String time = dataSnapshot.getValue(String.class);
                                long waktu = Long.parseLong(time);
                                ProcessingHelper processingHelper = new ProcessingHelper();
                                String date = processingHelper.changeToDate(waktu);
                                String dateStart = processingHelper.changeToChild(waktu);
                                mTextViewWaktuStart.setText(date);

                                String[] arrayFinish = date.split(" ");
                                String finishDate = arrayFinish[0];
                                int dateFinish = Integer.parseInt(finishDate);
                                dateFinish = dateFinish + averagePrediction[0];
                                String finishText = dateFinish + " " + arrayFinish[1] + " " + arrayFinish[2];
                                mTextViewWaktuFinish.setText(finishText);

                                if (averagePrediction[0] != 0) {
                                    String[] arrayStart = dateStart.split("-");
                                    String startDate = arrayStart[2];
                                    int tgl = Integer.parseInt(startDate);
                                    int valuePrediction = 100 / averagePrediction[0];
                                    int x = 0;
                                    for (int i = 0; i <= averagePrediction[0]; i++) {
                                        if (i == 0) {
                                            x = 0;
                                        } else {
                                            x = x + valuePrediction;
                                        }
                                        final String childDate = arrayStart[0] + "-" + arrayStart[1] + "-" + tgl;
                                        Log.i(TAG, "childDate: " + childDate);
                                        tgl++;

                                        final int finalVolume = 0;
                                        final String finalX = String.valueOf(x);
                                        mDatabaseReference.child("device").child(mDeviceId).child("prediksi").child(lastKey).child(childDate).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                String cek = dataSnapshot.child("prediksi").getValue(String.class);
                                                if (cek != null) {
                                                    mDatabaseReference.child("device").child(mDeviceId).child("prediksi").child(lastKey).child(childDate).child("prediksi").setValue(finalX);
                                                } else {
                                                    mDatabaseReference.child("device").child(mDeviceId).child("prediksi").child(lastKey).child(childDate).child("prediksi").setValue(finalX);
                                                    mDatabaseReference.child("device").child(mDeviceId).child("prediksi").child(lastKey).child(childDate).child("volume").setValue(finalVolume);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                } else {
                                    mTextViewWaktuPrediction.setText("No Data Prediction!");
                                    mTextViewDay.setVisibility(View.GONE);
                                }
                            }
                            readDataPrediction(lastKey);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    mDatabaseReference.child("device").child(mDeviceId).child("history").child("full").child(lastKey).child("prediksi").setValue(averagePrediction[0]);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private int calculateAverage(ArrayList<History> marks) {
        int average = 0;
        int div = 0;
        if(marks != null){
            for(int i=0; i <= (marks.size()-1); i++){
                average += marks.get(i).ConvertToHasil();
            }
        }

        assert marks != null;
        if (marks.size() == 0){
            div = 1;
        } else {
            div = marks.size();
        }
        return average / div ;
    }

    private void readRealtimeData() {
        mDatabaseReference.child("device").child(mDeviceId).child("monitoring").child("time").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String time = dataSnapshot.getValue(String.class);
                    long waktu = Long.parseLong(time);
                    ProcessingHelper processingHelper = new ProcessingHelper();
                    mTextViewWaktuNow.setText(processingHelper.changeToDate(waktu));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void readDataVolume(String lastKey) {
        mDatabaseReference.keepSynced(true);
        mDatabaseReference.child("device").child(mDeviceId).child("prediksi").child(lastKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mValuesVolume.clear();
                int i = 0;
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    if (userSnapshot.exists()) {
                        int data = userSnapshot.child("volume").getValue(Integer.class);
                        Log.i(TAG, "onDataChange: " + data);
                        final Entry entry = new Entry(i, data);
                        mValuesVolume.add(entry);
                        i++;
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

    private void readDataPrediction(final String lastKey) {
        mDatabaseReference.keepSynced(true);
        mDatabaseReference.child("device").child(mDeviceId).child("prediksi").child(lastKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mValuesPrediction.clear();
                int i = 0;
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    if (userSnapshot.exists()) {
                        String data = userSnapshot.child("prediksi").getValue(String.class);
                        Log.i(TAG, "onDataChange: " + data);
                        final Entry entry = new Entry(i, Integer.parseInt(data));
                        mValuesPrediction.add(entry);
                        i++;
                    }
                }
                readDataVolume(lastKey);
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
            mSetPrediction = (LineDataSet) mChart.getData().getDataSetByIndex(0);
            mSetPrediction.setValues(mValuesPrediction);
            mSetVolume = (LineDataSet) mChart.getData().getDataSetByIndex(0);
            mSetVolume.setValues(mValuesVolume);

            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            mSetVolume = new LineDataSet(mValuesVolume, "Volume");
            mSetPrediction = new LineDataSet(mValuesPrediction, "Prediksi");

            mSetPrediction.setDrawIcons(false);
            mSetVolume.setDrawIcons(false);

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
            mSetPrediction.enableDashedHighlightLine(10f, 5f, 0f);
            mSetPrediction.setColor(color2);
            mSetPrediction.setCircleColor(color2);
            mSetPrediction.setLineWidth(1f);
            mSetPrediction.setCircleRadius(3f);
            mSetPrediction.setDrawCircleHole(false);
            mSetPrediction.setValueTextSize(9f);
            mSetPrediction.setDrawValues(false);
//            mSetPrediction.setDrawFilled(true);
            mSetPrediction.setFormLineWidth(1f);
            mSetPrediction.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            mSetPrediction.setFormSize(15.f);
            mSetPrediction.setFillColor(color2);

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(mSetPrediction);
            dataSets.add(mSetVolume);

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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

    }
}
