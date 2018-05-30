package com.d3iftelu.gooddayteam.speechtrash;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.d3iftelu.gooddayteam.speechtrash.model.MarkerData;
import com.d3iftelu.gooddayteam.speechtrash.model.User;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Sholeh Hermawan on 21/04/2018.
 */

public class GPSTracker extends Service implements LocationListener {

    private static final String TAG = "GPSTracker";
    private final Context mContext;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    private FirebaseUser curentUser;

    // Declaring a Location Manager
    protected LocationManager locationManager;

    public GPSTracker(Context context) {
        this.mContext = context;
        getLocation();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        curentUser = firebaseAuth.getCurrentUser();

    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if ( Build.VERSION.SDK_INT >= 23 &&
                    ContextCompat.checkSelfPermission( mContext, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission( mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            }

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     * */
    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    /**
     * Function to get latitude
     * */
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     * Function to get longitude
     * */
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    /**
     * Function to check GPS/wifi enabled
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }


    @Override
    public void onLocationChanged(Location location) {
        writeLatLngToDatabase(new LatLng(location.getLatitude(), location.getLongitude()));
        this.location = location;
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void writeLatLngToDatabase(final LatLng myPosition){
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        final PrefManager prefManager = new PrefManager(mContext);
        String email = curentUser.getEmail();
        if (email.contains("@admin")){
            String name = curentUser.getEmail();
            String url = "https://firebasestorage.googleapis.com/v0/b/paspeechtrash.appspot.com/o/icon%2Fadmin.png?alt=media&token=c0be8582-2a21-4425-ac28-88809f866379";
            User user = new User(curentUser.getUid(), name , url, myPosition.latitude, myPosition.longitude, prefManager.getToken(), true);
            databaseReference.child("admin").child(curentUser.getUid()).setValue(user);
        } else {
            databaseReference.child("admin").child("petugas").child(curentUser.getUid()).child("validasi").setValue(false);
            databaseReference.child("admin").child("petugas").child(curentUser.getUid()).child("validasi").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        boolean validasi = dataSnapshot.getValue(Boolean.class);
                        if (!validasi){
                            User user = new User(curentUser.getUid(), curentUser.getDisplayName() , String.valueOf(curentUser.getPhotoUrl()), myPosition.latitude, myPosition.longitude, prefManager.getToken(), false);
                            databaseReference.child("list_petugas").child(curentUser.getUid()).setValue(user);
                        } else {
                            MarkerData marker = new MarkerData(String.valueOf(curentUser.getPhotoUrl()), myPosition.latitude, myPosition.longitude, curentUser.getDisplayName());
                            databaseReference.child("list_maps").child(curentUser.getUid()).setValue(marker);
                            User user = new User(curentUser.getUid(), curentUser.getDisplayName() , String.valueOf(curentUser.getPhotoUrl()), myPosition.latitude, myPosition.longitude, prefManager.getToken(), true);
                            databaseReference.child("list_petugas").child(curentUser.getUid()).setValue(user);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public String getLocationName(double latitude, double longitude){
        String locationName = null;
        try {
            Geocoder gcd = new Geocoder(mContext, Locale.getDefault());
            List<Address> addresses = null;
            addresses = gcd.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                locationName = addresses.get(0).getThoroughfare();
                Log.i(TAG, "getLocationName: " + addresses);
            }
            else {
                locationName = "not detected locaton name";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return locationName;
    }
}