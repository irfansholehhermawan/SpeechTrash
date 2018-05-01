package com.d3iftelu.gooddayteam.speechtrash;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import com.d3iftelu.gooddayteam.speechtrash.model.MarkerData;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

public class MapsActivity extends Activity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private GPSTracker gps;
    private static final int LOCATION_PERMISSION_ID = 1001;
    private ProgressDialog progressDialog;
    private MapView mapView;

    private FirebaseUser curentUser;
    private DatabaseReference databaseReference;
    private ArrayList<Marker> markers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        setProgressDialog();

        mapView = (MapView) findViewById(R.id.main_maps);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);


        markers = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_ID);
        }

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        curentUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        readListDevice();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private LatLng getPosition() {
        gps = new GPSTracker(this);
        LatLng myPosition = new LatLng(gps.getLatitude(), gps.getLongitude());
        return  myPosition;
    }

    private void setProgressDialog() {
        progressDialog = new ProgressDialog(MapsActivity.this);
        progressDialog.setCancelable(true);
        progressDialog.setMessage("Pleace Wait");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        gps = new GPSTracker(MapsActivity.this);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms
                setCurentUserMarker(true);
                gps.writeLatLngToDatabase(new LatLng(gps.getLatitude(), gps.getLongitude()));
            }
        }, 100);
    }

    private Marker setViewInMap(final MarkerData marker, boolean isMyPosition) {
        final LatLng position = new LatLng(marker.getLat(), marker.getLng());
        final Marker[] driver_marker = new Marker[1];
        Target mTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Log.i(TAG, "onBitmapLoaded: Finish Load");
                driver_marker[0] = mMap.addMarker(new MarkerOptions()
                        .position(position)
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .title(marker.getName())
                );
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Log.d("picasso", "onBitmapFailed");
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };

        Picasso.with(MapsActivity.this).load(marker.getImageUrl())
                .resize(210, 210)
                .centerCrop()
                .transform(new BubbleTransformation(10))
                .into(mTarget);

        if (isMyPosition) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(position)          // Sets the center of the map to Mountain View
                    .zoom(15)                    // Sets the zoom
                    .build();                    // Creates a CameraPosition from the builder
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        return driver_marker[0];
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_ID && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setCurentUserMarker(true);
        }
    }

    private void setCurentUserMarker(boolean isFirstTime) {
        Marker marker;
        MarkerData markerData = new MarkerData(String.valueOf(curentUser.getPhotoUrl()), gps.getLatitude(), gps.getLongitude(), curentUser.getDisplayName());
        if (isFirstTime) {
            marker = setViewInMap(markerData, true);
        } else {
            marker = setViewInMap(markerData, false);
        }
        markers.add(marker);
    }


    private void readListDevice() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("list_device");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mMap.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    MarkerData markerData = userSnapshot.getValue(MarkerData.class);
                    setViewInMap(markerData, false);
                    Log.i(TAG, "ListMaps : " + markerData.getName());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
    }
}
