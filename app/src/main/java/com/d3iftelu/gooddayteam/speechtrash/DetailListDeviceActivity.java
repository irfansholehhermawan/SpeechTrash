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
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.d3iftelu.gooddayteam.speechtrash.adapter.DeviceListAdapter;
import com.d3iftelu.gooddayteam.speechtrash.model.Device;
import com.d3iftelu.gooddayteam.speechtrash.model.MarkerData;
import com.d3iftelu.gooddayteam.speechtrash.model.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DetailListDeviceActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "DetailPetugasActivity";
    public static final String ARGS_UID = "petugas_uid";
    public static final String ARGS_NAME = "petugas_name";
    private DatabaseReference myRef;
    private Switch mSwitchStatus;
    private TextView mTextViewOfficerName;
    private ListView mListViewDevice;
    private TextView mTextViewDataIsEmpty;
    private ProgressBar loadingData;
    private DeviceListAdapter mAdapter;
    private String mUID, mName;
    private GoogleMap mMap;
    private LatLng latLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_list_device);

        Intent intent = getIntent();
        mUID = intent.getStringExtra(DetailListDeviceActivity.ARGS_UID);
        mName = intent.getStringExtra(DetailListDeviceActivity.ARGS_NAME);
        Log.i(TAG, "CEK: "+ mUID + " : " + mName);
        myRef = FirebaseDatabase.getInstance().getReference();

        if (mUID != null) {
            readData(mUID);
        }
        mSwitchStatus = findViewById(R.id.switch_status);
        mTextViewOfficerName = findViewById(R.id.text_name_officer);
        mListViewDevice = findViewById(R.id.list_view_device);
        mTextViewDataIsEmpty = findViewById(R.id.text_view_empty_view);
        loadingData = (ProgressBar) findViewById(R.id.item_progres_bar);

        final ArrayList<Device> devices = readDeviceData();

        mAdapter = new DeviceListAdapter(this, devices);
        mListViewDevice.setAdapter(mAdapter);
        mListViewDevice.setEmptyView(mTextViewDataIsEmpty);
        setTitle(mName);
        readValidasi();

        mListViewDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(DetailListDeviceActivity.this, DetailChatActivity.class);
                intent.putExtra(DetailActivity.ARGS_DEVICE_NAME, devices.get(i).getDeviceName());
                intent.putExtra(DetailActivity.ARGS_DEVICE_ID, devices.get(i).getDeviceId());
                Log.i(TAG, "Uji idDevice : "+devices.get(i).getDeviceId());
                startActivity(intent);
            }
        });

        MapView mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
    }

    private void readValidasi() {
        myRef.child("admin").child("petugas").child(mUID).child("validasi").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    boolean status = dataSnapshot.getValue(Boolean.class);
                    mSwitchStatus.setChecked(status);
                    mSwitchStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean isStatus) {
                            saveStatusToDatabase(isStatus);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mTextViewOfficerName.setText(mName);
    }

    private void saveStatusToDatabase(boolean status) {
        if (status){
            myRef.child("admin").child("petugas").child(mUID).child("validasi").setValue(status);
            myRef.child("list_petugas").child(mUID).child("validasi").setValue(status);
            myRef.child("list_petugas").child(mUID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final User data = dataSnapshot.getValue(User.class);
                    MarkerData marker = new MarkerData(String.valueOf(data.getPhotoUrl()), data.getLatitude(), data.getLongitude(), data.getName());
                    myRef.child("list_maps").child(mUID).setValue(marker);
                    Log.i(TAG, "MarkerData: "+data);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            myRef.child("admin").child("petugas").child(mUID).child("validasi").setValue(status);
            myRef.child("list_petugas").child(mUID).child("validasi").setValue(status);
            myRef.child("list_maps").child(mUID).removeValue();
        }
    }

    private ArrayList<Device> readDeviceData() {
        final ArrayList<Device> devicesData = new ArrayList<>();
        loadingData.setVisibility(View.VISIBLE);
        myRef.keepSynced(true);
        myRef.child("list_device").child("petugas").child(mUID).addValueEventListener(new ValueEventListener() {
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
                                boolean status = dataSnapshot.child("status").getValue(boolean.class);
                                device.setStatus(status);

                            } catch (NullPointerException e){
                                myRef.child("device").child(deviceId).child("status").setValue(false);
                                device.setStatus(false);
                            }
                            loadingData.setVisibility(View.GONE);
                            mAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                loadingData.setVisibility(View.GONE);
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

    /**
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(latLng != null)
            setMarker(latLng);
    }

    private void setMarker(LatLng latLng){
        mMap.addMarker(new MarkerOptions()
                .position(latLng));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(17)
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void readData(String mUID){
        myRef.child("list_petugas").child(mUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User history = dataSnapshot.getValue(User.class);
                    latLng = new LatLng(history.getLatitude(), history.getLongitude());
                    if (mMap != null)
                        setMarker(latLng);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.listpetugas_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //to add option menu
        switch (item.getItemId()) {
            case R.id.action_delete:
                deleteDevice(mUID);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteDevice(final String uid) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_data_question)
                .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteData(uid);
                    }
                })
                .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }

    private void deleteData(String uid) {
        myRef.child("list_petugas").child(uid).removeValue();
        myRef.child("list_maps").child(uid).removeValue();
        Toast.makeText(this, "Data Officer Successfully Deleted!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
