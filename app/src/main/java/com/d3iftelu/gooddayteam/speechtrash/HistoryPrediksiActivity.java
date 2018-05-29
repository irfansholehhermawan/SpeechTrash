package com.d3iftelu.gooddayteam.speechtrash;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.d3iftelu.gooddayteam.speechtrash.adapter.PredictionListAdapter;
import com.d3iftelu.gooddayteam.speechtrash.model.Prediction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HistoryPrediksiActivity extends AppCompatActivity {
    private static final String TAG = "HistoryPrediksiActivity";
    private DatabaseReference mDatabaseReference;
    private ListView mListViewDevice;
    private TextView mTextViewDataIsEmpty;
    private ProgressBar loadingData;
    private PredictionListAdapter mAdapter;
    private String mIdDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_prediksi);

        mIdDevice = getIntent().getStringExtra(DetailActivity.ARGS_DEVICE_ID);
        setTitle(mIdDevice);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mListViewDevice = findViewById(R.id.list_history_prediction);
        mTextViewDataIsEmpty = findViewById(R.id.text_view_empty_view);
        loadingData = (ProgressBar) findViewById(R.id.item_progres_bar);

        final ArrayList<Prediction> predictions = getData();

        mAdapter = new PredictionListAdapter(this, predictions);
        mListViewDevice.setAdapter(mAdapter);
        mListViewDevice.setEmptyView(mTextViewDataIsEmpty);
    }

    private ArrayList<Prediction> getData() {
        final ArrayList<Prediction> predictionData = new ArrayList<>();
        loadingData.setVisibility(View.VISIBLE);
        mDatabaseReference.keepSynced(true);
        mDatabaseReference.child("device").child(mIdDevice).child("history").child("full").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                predictionData.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    if (userSnapshot.exists()) {
                        final Prediction data = userSnapshot.getValue(Prediction.class);
                        final Prediction dataId = new Prediction(data, userSnapshot.getKey());
                        predictionData.add(dataId);
                        Log.i(TAG, "DataPrediction : "+ data);
                    }
                }
                loadingData.setVisibility(View.GONE);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
        return predictionData;
    }
}
