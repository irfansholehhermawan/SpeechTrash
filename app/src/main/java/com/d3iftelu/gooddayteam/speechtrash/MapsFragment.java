package com.d3iftelu.gooddayteam.speechtrash;


import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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


/**
 * A simple {@link Fragment} subclass.
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private View view;
    private static final String TAG = "MapsFragment";
    private GoogleMap mMap;
    private GPSTracker gps;
    private static final int LOCATION_PERMISSION_ID = 1001;
    private ProgressDialog progressDialog;
    private FirebaseUser curentUser;
    private ArrayList<Marker> markers;

    public MapsFragment() {
        // Required empty public constructor
    }

    /**
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_maps, container, false);

        MapView mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);


        markers = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_ID);
        }

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        curentUser = firebaseAuth.getCurrentUser();

        readDataInLocation();

        return view;

    }

    /**
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        gps = new GPSTracker(getContext());

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

            /**
             *
             * @param errorDrawable
             */
            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Log.d("picasso", "onBitmapFailed");
            }

            /**
             *
             * @param placeHolderDrawable
             */
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };

        Picasso.with(getActivity()).load(marker.getImageUrl())
                .resize(170, 170)
                .centerCrop()
                .transform(new BubbleTransformation(0))
                .into(mTarget);

        if (isMyPosition) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    // Sets the center of the map to Mountain View
                    .target(position)
                    // Sets the zoom
                    .zoom(15)
                    // Creates a CameraPosition from the builder
                    .build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        return driver_marker[0];
    }

    /**
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_ID && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setCurentUserMarker(true);
        }
    }

    /**
     * set user marker
     * @param isFirstTime detect is first time loading
     */
    private void setCurentUserMarker(boolean isFirstTime) {
        String name, url;
        String email = curentUser.getEmail();
        if (email.contains("@admin")){
            name = curentUser.getEmail();
            url = "https://firebasestorage.googleapis.com/v0/b/paspeechtrash.appspot.com/o/icon%2Fadmin.png?alt=media&token=e26d0f9c-10bc-4a33-913b-4761447e919a";
        } else {
            name = curentUser.getDisplayName();
            url = String.valueOf(curentUser.getPhotoUrl());
        }
        Marker marker;
        MarkerData markerData = new MarkerData(url, gps.getLatitude(), gps.getLongitude(), name);
        if (isFirstTime) {
            marker = setViewInMap(markerData, true);
        } else {
            marker = setViewInMap(markerData, false);
        }
        markers.add(marker);
    }

    /**
     * reading online user location
     */
    private void readDataInLocation() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("list_maps");

        myRef.addValueEventListener(new ValueEventListener() {
            /**
             *
             * @param dataSnapshot
             */
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mMap.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    MarkerData markerData = userSnapshot.getValue(MarkerData.class);
                    setViewInMap(markerData, false);
                }
                setCurentUserMarker(false);
            }

            /**
             *
             * @param databaseError
             */
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    }
}