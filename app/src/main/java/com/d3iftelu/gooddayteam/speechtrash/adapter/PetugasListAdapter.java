package com.d3iftelu.gooddayteam.speechtrash.adapter;

import android.annotation.SuppressLint;
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

import com.d3iftelu.gooddayteam.speechtrash.GPSTracker;
import com.d3iftelu.gooddayteam.speechtrash.R;
import com.d3iftelu.gooddayteam.speechtrash.model.User;

import java.util.ArrayList;

import static android.support.constraint.Constraints.TAG;

/**
 * Created by Sholeh Hermawan on 21/04/2018.
 */

public class PetugasListAdapter  extends ArrayAdapter<User> {
    Context context;

    public PetugasListAdapter(@NonNull Context context, @NonNull ArrayList<User> objects) {
        super(context, 0, objects);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null)
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_petugas_view_controller, parent, false);

        final User current = getItem(position);

        TextView name = view.findViewById(R.id.name_petugas);
        name.setText(current.getName());

        TextView location = (TextView) view.findViewById(R.id.lokasi_petugas);
        GPSTracker gpsTracker = new GPSTracker(context);
        String locationName = gpsTracker.getLocationName(current.getLatitude(), current.getLongitude());
        location.setText(locationName);

        TextView status = (TextView) view.findViewById(R.id.status);
        boolean validasi = current.isValidasi();
        Log.i(TAG, "validasi: "+validasi);
        if (validasi) {
            status.setText("VERIFIED");
            status.setBackgroundColor(0xFF8CC63E);
        } else {
            status.setText("UNVERIFIED");
            status.setBackgroundColor(0xFF008DB3);
        }

        return view;
    }
}
