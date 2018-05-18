package com.d3iftelu.gooddayteam.speechtrash;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.d3iftelu.gooddayteam.speechtrash.adapter.MessageAdapter;
import com.d3iftelu.gooddayteam.speechtrash.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class DetailChatActivity extends AppCompatActivity {
    private FirebaseDatabase mDatabase;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mMessagesReference;
    private ChildEventListener mChildEventListener;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    private ListView mMessageListView;
    private MessageAdapter mMessageAdapter;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;
    private ImageView mSendButton;
    private String mDeviceId, mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_chat);

        Intent intent = getIntent();
        mDeviceId = getIntent().getStringExtra(DetailActivity.ARGS_DEVICE_ID);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        mMessagesReference = mDatabase.getReference().child("device").child(mDeviceId).child("messages");

        mUsername = mCurrentUser.getUid();
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageListView = (ListView) findViewById(R.id.messageListView);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendButton = (ImageView) findViewById(R.id.sendButton);

        List<Message> messages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(this, R.layout.view_message, messages);
        mMessageListView.setAdapter(mMessageAdapter);

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
                Message message = new Message(mMessageEditText.getText().toString(), mCurrentUser.getDisplayName(), time);
                mMessagesReference.push().setValue(message);

                mMessageEditText.setText("");
            }
        });

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    onSignedInInitialize(user.getDisplayName());
                }
            }
        };
    }

    private void onSignedInInitialize(String username) {
        mUsername = username;
        attachDatabaseReadListener();
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Message message = dataSnapshot.getValue(Message.class);
                    mMessageAdapter.add(message);
                }
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }
                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }
                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mMessagesReference.addChildEventListener(mChildEventListener);
        }
    }


}
