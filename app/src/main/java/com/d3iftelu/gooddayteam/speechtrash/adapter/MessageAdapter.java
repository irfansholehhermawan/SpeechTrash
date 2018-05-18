package com.d3iftelu.gooddayteam.speechtrash.adapter;

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

public class MessageAdapter extends ArrayAdapter<Message> {

    public MessageAdapter(@NonNull Context context, @NonNull ArrayList<Message> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null)
            view = LayoutInflater.from(getContext()).inflate(R.layout.view_message, parent, false);


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

        return view;
    }
}
