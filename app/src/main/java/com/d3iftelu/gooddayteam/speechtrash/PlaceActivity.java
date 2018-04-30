package com.d3iftelu.gooddayteam.speechtrash;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

public class PlaceActivity extends Activity implements OnMapReadyCallback {

    private static final String TAG = "PlaceActivity";
    private String idDevice;
    private GoogleMap mMap;
    private GPSTracker gps;
    private static final int LOCATION_PERMISSION_ID = 1001;
    private ProgressDialog progressDialog;

    AutoCompleteTextView autoCompleteTextView;
    String [] Sports;

    private FirebaseUser curentUser;
    private ArrayList<Marker> markers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);

        Intent intent = getIntent();
        idDevice = intent.getStringExtra("idDevice");

        setProgressDialog();
//        showProgressDialog(true);

        MapView mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);


        markers = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(PlaceActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PlaceActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_ID);
        }

        FloatingActionButton floatingActionButtonGoToStopwatch = (FloatingActionButton) findViewById(R.id.my_location);
        floatingActionButtonGoToStopwatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogInputName(idDevice);
                Toast.makeText(PlaceActivity.this, "Under Maintance!!!",Toast.LENGTH_LONG).show();

            }
        });

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        curentUser = firebaseAuth.getCurrentUser();

//        readOnlineUserLocation();
    }

    private void setProgressDialog() {
        progressDialog = new ProgressDialog(PlaceActivity.this);
        progressDialog.setCancelable(true);
        progressDialog.setMessage("Pleace Wait");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    private void showProgressDialog(boolean isShow) {
        if (isShow) {
            progressDialog.show();
        } else {
            progressDialog.hide();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        gps = new GPSTracker(PlaceActivity.this);

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
//                showProgressDialog(false);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Log.d("picasso", "onBitmapFailed");
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };

        Picasso.with(PlaceActivity.this).load(marker.getImageUrl())
                .resize(200, 200)
                .centerCrop()
                .transform(new BubbleTransformation(20))
                .into(mTarget);

        if (isMyPosition) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(position)          // Sets the center of the map to Mountain View
                    .zoom(17)                    // Sets the zoom
                    .build();                    // Creates a CameraPosition from the builder
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        return driver_marker[0];
    }

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

    private void showDialogInputName(final String idDevice){
        AlertDialog.Builder builder = new AlertDialog.Builder(PlaceActivity.this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();

        View viewDialog = inflater.inflate(R.layout.dialog, null);
        final EditText editTextName = viewDialog.findViewById(R.id.edit_text_name);

        final View title = inflater.inflate(R.layout.text_view_customize,null);

        final ImageView icon = (ImageView) title.findViewById(R.id.custom_icon_of_title);
        final TextView text = (TextView) title.findViewById(R.id.custom_text_of_title);

        icon.setImageResource(R.drawable.ic_info_black_24dp);
        text.setText(R.string.title_add_device);
        builder.setCustomTitle(title);

        builder.setView(viewDialog)
                .setPositiveButton(R.string.dialog_submit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String deviceName = editTextName.getText().toString().trim();
                        saveToDatabase(idDevice, deviceName);
                        goToMainActivity();

                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveToDatabase(String idDevice, String deviceName){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        FirebaseDatabase.getInstance().getReference("user_profile").child(firebaseUser.getUid()).child(idDevice).setValue(deviceName);
        FirebaseDatabase.getInstance().getReference("user").child(firebaseUser.getUid()).child(idDevice).setValue(deviceName);
//        FirebaseDatabase.getInstance().getReference("device").child(idDevice).child("deviceName").setValue(deviceName);
        finish();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(PlaceActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
