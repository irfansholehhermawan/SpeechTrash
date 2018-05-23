package com.d3iftelu.gooddayteam.speechtrash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.d3iftelu.gooddayteam.speechtrash.adapter.DeviceListAdapter;
import com.d3iftelu.gooddayteam.speechtrash.model.Device;
import com.d3iftelu.gooddayteam.speechtrash.model.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

        if (mUID != null) {
            readData(mUID);
        }

        mListViewDevice = findViewById(R.id.list_view_device);
        mTextViewDataIsEmpty = findViewById(R.id.text_view_empty_view);
        loadingData = (ProgressBar) findViewById(R.id.item_progres_bar);

        final ArrayList<Device> devices = readDeviceData();

        mAdapter = new DeviceListAdapter(this, devices);
        mListViewDevice.setAdapter(mAdapter);
        mListViewDevice.setEmptyView(mTextViewDataIsEmpty);
        setTitle(mName);

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

    private ArrayList<Device> readDeviceData() {
        final ArrayList<Device> devicesData = new ArrayList<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        loadingData.setVisibility(View.VISIBLE);

        final DatabaseReference myRef = database.getReference();
        myRef.keepSynced(true);
        myRef.child("list_device").child(mUID).addValueEventListener(new ValueEventListener() {
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
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("list_petugas").child(mUID);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User history = dataSnapshot.getValue(User.class);
                latLng = new LatLng(history.getLatitude(), history.getLongitude());
                if(mMap != null)
                    setMarker(latLng);
            }

            /**
             *
             * @param databaseError
             */
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
