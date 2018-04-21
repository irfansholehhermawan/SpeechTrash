package com.d3iftelu.gooddayteam.speechtrash.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.d3iftelu.gooddayteam.speechtrash.R;
import com.d3iftelu.gooddayteam.speechtrash.model.Device;

import java.util.ArrayList;

/**
 * Created by Sholeh Hermawan on 21/04/2018.
 */

public class DeviceListAdapter extends ArrayAdapter<Device> {
    public DeviceListAdapter(@NonNull Context context, @NonNull ArrayList<Device> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null)
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_device, parent, false);

        final Device current = getItem(position);

        TextView name = view.findViewById(R.id.text_view_device_name);
        name.setText(current.getDeviceName());

        TextView id = view.findViewById(R.id.text_view_device_id);
        id.setText(current.getDeviceId());

        ImageView status = view.findViewById(R.id.image_view_status);
        if(current.isStatus()){
            status.setVisibility(View.VISIBLE);
        } else {
            status.setVisibility(View.GONE);
        }

        return view;
    }
}
