package com.d3iftelu.gooddayteam.speechtrash.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.d3iftelu.gooddayteam.speechtrash.ProcessingHelper;
import com.d3iftelu.gooddayteam.speechtrash.R;
import com.d3iftelu.gooddayteam.speechtrash.model.Message;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends ArrayAdapter<Message> {

    public MessageAdapter(@NonNull Context context, @NonNull ArrayList<Message> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.view_message, parent, false);
        }


        TextView messageTextView = (TextView) convertView.findViewById(R.id.messageTextView);
        TextView authorTextView = (TextView) convertView.findViewById(R.id.nameTextView);
        TextView timestampDate = (TextView) convertView.findViewById(R.id.time_stamp);
        TextView timestampTime = (TextView) convertView.findViewById(R.id.time_stamp2);

        Message message = getItem(position);

        ProcessingHelper processingHelper = new ProcessingHelper();
        String time = message.getTimestamp();
        long times = Long.parseLong(time);
        String date = processingHelper.changeToDate(times);
        String waktu = processingHelper.changeToTime(times);

        authorTextView.setText(message.getName());
        messageTextView.setText(message.getMessage());
        timestampDate.setText(date);
        timestampTime.setText(waktu);

        return convertView;
    }
}
