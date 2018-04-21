package com.d3iftelu.gooddayteam.speechtrash;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.d3iftelu.gooddayteam.speechtrash.adapter.DeviceListAdapter;
import com.d3iftelu.gooddayteam.speechtrash.model.Device;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private ListView mListViewDevice;
    private TextView mTextViewDataIsEmpty;
    private ProgressBar loadingData;

    private FirebaseUser mCurrentUser;
    private DeviceListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        checkUser(mCurrentUser);
        mListViewDevice = findViewById(R.id.list_view_device);
        mTextViewDataIsEmpty = findViewById(R.id.text_view_empty_view);
        loadingData = (ProgressBar) findViewById(R.id.item_progres_bar);

        mListViewDevice.setEmptyView(mTextViewDataIsEmpty);

        final ArrayList<Device> devices = readDeviceData();

        mAdapter = new DeviceListAdapter(this, devices);
        mListViewDevice.setAdapter(mAdapter);

        mListViewDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
//                intent.putExtra(DetailActivity.ARGS_DEVICE_NAME, devices.get(i).getDeviceName());
//                intent.putExtra(DetailActivity.ARGS_DEVICE_ID, devices.get(i).getDeviceId());
//                startActivity(intent);
                Toast.makeText(MainActivity.this, "Under Maintance!!!",Toast.LENGTH_LONG).show();
            }
        });
    }

    public void goToReader(View view) {
        Intent intent = new Intent(MainActivity.this, ReaderActivity.class);
        startActivity(intent);
    }

    private ArrayList<Device> readDeviceData() {
        final ArrayList<Device> devicesData = new ArrayList<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        loadingData.setVisibility(View.VISIBLE);

        final DatabaseReference myRef = database.getReference();
        myRef.keepSynced(true);
        myRef.child("user").child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                devicesData.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String deviceName = userSnapshot.getValue(String.class);
                    final String deviceId = userSnapshot.getKey();

                    final Device device = new Device(deviceName, deviceId);
                    devicesData.add(device);

                    myRef.child("device").child(deviceId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            try {
                                boolean status =
                                        dataSnapshot.child("status").getValue(boolean.class);
                                device.setStatus(status);
                            } catch (NullPointerException e){
                                myRef.child("device").child(deviceId).child("status").setValue(false);
                                device.setStatus(false);
                            }
                            mAdapter.notifyDataSetChanged();
                            loadingData.setVisibility(View.GONE);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
        return devicesData;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //to add option menu
        switch (item.getItemId()) {
            case R.id.logout:
                signOutCheck();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void signOutCheck(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Do you want to logout ?")
                .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();
                        Intent intentLogin = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intentLogin);
                    }
                }).setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    private void checkUser(FirebaseUser firebaseUser){
        if(firebaseUser == null){
            goToLoginActivity();
        }
    }

    private void goToLoginActivity(){
        Intent intentLogin = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intentLogin);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}