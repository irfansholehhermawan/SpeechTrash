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
        String prediksi = String.valueOf(current.getPrediksi());
        prediction.setText(prediksi);
        Log.i(TAG, "getView: "+prediksi);

        return view;
    }
}
