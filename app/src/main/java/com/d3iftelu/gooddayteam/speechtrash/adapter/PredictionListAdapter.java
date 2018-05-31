package com.d3iftelu.gooddayteam.speechtrash.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.d3iftelu.gooddayteam.speechtrash.ProcessingHelper;
import com.d3iftelu.gooddayteam.speechtrash.R;
import com.d3iftelu.gooddayteam.speechtrash.model.Prediction;

import java.util.ArrayList;

/**
 * Created by Sholeh Hermawan on 21/04/2018.
 */

public class PredictionListAdapter extends ArrayAdapter<Prediction> {
    private static final String TAG = "PredictionListAdapter";

    public PredictionListAdapter(@NonNull Context context, @NonNull ArrayList<Prediction> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null)
            view = LayoutInflater.from(getContext()).inflate(R.layout.prediction_view_controller, parent, false);

        final Prediction current = getItem(position);
        ProcessingHelper convert = new ProcessingHelper();

        TextView startDate = view.findViewById(R.id.prediction_date_start);
        String start = convert.changeUnixTimeStampToStringDate(Long.parseLong(current.getStartDate()));
        startDate.setText(start);
        Log.i(TAG, "getView: "+start);


        TextView finishDate = view.findViewById(R.id.prediction_date_finish);
        String finish = convert.changeUnixTimeStampToStringDate(Long.parseLong(current.getFinishDate()));
        finishDate.setText(finish);
        Log.i(TAG, "getView: "+finish);

        TextView prediction = view.findViewById(R.id.prediction_day);
        TextView marker = view.findViewById(R.id.marker_prediction);
        int x = current.getPrediksi();
        String prediksi = String.valueOf(x);
        prediction.setText(prediksi);
        Log.i(TAG, "getView: "+prediksi);
        if (x == 0){
            prediction.setBackgroundColor(0xFFD9D8D8);
            marker.setBackgroundColor(0xFFD9D8D8);
        } else if (x == 1 || x == 2){
            prediction.setBackgroundColor(0xFFD10202);
            marker.setBackgroundColor(0xFFD10202);
        } else if (x == 3 || x == 4){
            prediction.setBackgroundColor(0xFF3498DB);
            marker.setBackgroundColor(0xFF3498DB);
        } else if (x == 5 || x == 6){
            prediction.setBackgroundColor(0xFFF1C40F);
            marker.setBackgroundColor(0xFFF1C40F);
        } else {
            prediction.setBackgroundColor(0xFF2ECC71);
            marker.setBackgroundColor(0xFF2ECC71);
        }

        return view;
    }
}
