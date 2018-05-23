package com.d3iftelu.gooddayteam.speechtrash;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.d3iftelu.gooddayteam.speechtrash.adapter.MessageAdapter;
import com.d3iftelu.gooddayteam.speechtrash.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DetailChatActivity extends AppCompatActivity {
    private static final String TAG = "DetailChatActivity";
    private FirebaseDatabase mDatabase;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mMessagesReference;
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    private ListView mMessageListView;
    private MessageAdapter mMessageAdapter;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;
    private ImageView mSendButton;
    private TextView mTextViewDataIsEmpty;
    private String mDeviceId, admin_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_chat);

        mDeviceId = getIntent().getStringExtra(DetailActivity.ARGS_DEVICE_ID);
        setTitle(mDeviceId);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        mMessagesReference = mDatabase.getReference().child("device").child(mDeviceId).child("messages");

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mTextViewDataIsEmpty = (TextView) findViewById(R.id.text_view_empty_view);
        mMessageListView = (ListView) findViewById(R.id.messageListView);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendButton = (ImageView) findViewById(R.id.sendButton);

        final ArrayList<Message> messages = readMessageData();

        mMessageAdapter = new MessageAdapter(this, messages);
        mMessageListView.setAdapter(mMessageAdapter);
        mMessageListView.setEmptyView(mTextViewDataIsEmpty);

        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});



        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProcessingHelper processingHelper = new ProcessingHelper();
                long time = processingHelper.getDateNow();
                String pengirim;
                String name = mCurrentUser.getDisplayName();
                if (name.equals("")){
                    pengirim = mDeviceId;
                } else {
                    pengirim = name;
                }
                Log.i(TAG, "Uji id device : "+mDeviceId);
                Log.i(TAG, "Uji name : "+name);
                Log.i(TAG, "Uji pengirim : "+pengirim);
                Message message = new Message(admin_id, mMessageEditText.getText().toString(), pengirim, time);
                mMessagesReference.push().setValue(message);

                mMessageEditText.setText("");
            }
        });

        mDatabase.getReference().child("device").child(mDeviceId).child("admin_id").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                admin_id = dataSnapshot.getValue(String.class);
                Log.i(TAG, "adminID : " +admin_id);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private ArrayList<Message> readMessageData() {
        final ArrayList<Message> messageData = new ArrayList<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mProgressBar.setVisibility(View.VISIBLE);

        final DatabaseReference myRef = database.getReference();
        myRef.keepSynced(true);
        myRef.child("device").child(mDeviceId).child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                messageData.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    final Message data = userSnapshot.getValue(Message.class);
                    final Message dataId = new Message(data, userSnapshot.getKey());
                    messageData.add(dataId);
                }
                mProgressBar.setVisibility(View.GONE);
                mMessageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
        return messageData;
    }
}
