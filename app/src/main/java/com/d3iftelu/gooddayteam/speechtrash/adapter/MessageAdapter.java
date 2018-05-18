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

    public MessageAdapter(Context context, int resource, List<Message> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.view_message, parent, false);
        }


        TextView messageTextView = (TextView) convertView.findViewById(R.id.messageTextView);
        TextView authorTextView = (TextView) convertView.findViewById(R.id.nameTextView);
        TextView timestampTextView = (TextView) convertView.findViewById(R.id.nameTextView);

        Message message = getItem(position);

        ProcessingHelper processingHelper = new ProcessingHelper();
        long time = message.getTimestamp();
        String waktu = processingHelper.changeUnixTimeStampToStringDate(time);

        authorTextView.setText(message.getName());
        messageTextView.setText(message.getMessage());
        timestampTextView.setText(waktu);

        return convertView;
    }
}
