package com.d3iftelu.gooddayteam.speechtrash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.d3iftelu.gooddayteam.speechtrash.adapter.PetugasListAdapter;
import com.d3iftelu.gooddayteam.speechtrash.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ListPetugasActivity extends AppCompatActivity {
    private static final String TAG = "ListPetugasActivity";
    private ListView petugasListView;
    private PetugasListAdapter petugasAdapter;
    private TextView emptyNotification;
    private ProgressBar loadingData;
    private String mDeviceId, mDeviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_petugas);

        mDeviceId = getIntent().getStringExtra(DetailActivity.ARGS_DEVICE_ID);
        mDeviceName = getIntent().getStringExtra(DetailActivity.ARGS_DEVICE_NAME);

        petugasListView = (ListView) findViewById(R.id.list_view_petugas);
        loadingData = (ProgressBar) findViewById(R.id.item_progres_bar);
        emptyNotification = (TextView) findViewById(R.id.infoTextView);

        final ArrayList<User> realData = getData();
        petugasAdapter = new PetugasListAdapter(this,realData);
        petugasListView.setAdapter(petugasAdapter);
        petugasListView.setEmptyView(emptyNotification);

        petugasListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String uid = realData.get(i).getUser_id();
                Log.i(TAG, "data UID : "+uid);
                saveToDatabase(uid);
                Toast.makeText(ListPetugasActivity.this, "Petugas : "+ realData.get(i).getName() + " berhasil ditambahkan pada Tempat Sampah ID : " + mDeviceId + "(" + mDeviceName +")", Toast.LENGTH_LONG).show();
                Intent gotoListDevice = new Intent(ListPetugasActivity.this, AdminActivity.class);
                startActivity(gotoListDevice);
            }
        });
    }

    private void saveToDatabase(String uid) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("list_device").child(uid).child(mDeviceId).setValue(mDeviceName);
        databaseReference.child("device").child(mDeviceId).child("user_id").setValue(uid);
    }

    public ArrayList<User> getData() {
        final ArrayList<User> currentPetugas = new ArrayList<>();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        loadingData.setVisibility(View.VISIBLE);
        DatabaseReference scheduleRef = database.getReference("list_petugas");
        scheduleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentPetugas.clear();
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    final User data = postSnapshot.getValue(User.class);
                    final User dataId = new User(data, postSnapshot.getKey());
                    currentPetugas.add(dataId);
                }
                petugasAdapter.notifyDataSetChanged();
                loadingData.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Error", "loadPost:onCancelled", databaseError.toException());
            }
        });
        return currentPetugas;
    }
}
