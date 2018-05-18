package com.d3iftelu.gooddayteam.speechtrash;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

/**
 * Created by Sholeh Hermawan on 21/04/2018.
 */

public class ReaderActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    public static final int REQUEST_CAMERA = 1;
    public ZXingScannerView scannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (!checkPermission()) {
                requestPermission();
            }
        }

    }

    /**
     * checking for camera permission
     * @return is camera granted
     */
    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(ReaderActivity.this, CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * request permission
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    /**
     * result of request permission
     * @param requestCode request code
     * @param permission permission
     * @param grantResults grant result
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, @NonNull String permission[], @NonNull int grantResults[]) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (!cameraAccepted) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            if (shouldShowRequestPermissionRationale(CAMERA)) {
                                displayAlertMessage(
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                                    requestPermission(new String[]{CAMERA}, REQUEST_CAMERA);}
                                            }

                                            private void requestPermission(String[] strings, int requestCamera) {
                                            }
                                        });
                                return;
                            }
                        }
                    }

                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (checkPermission()) {
                scannerView = new ZXingScannerView(this);
                setContentView(scannerView);
            }
            scannerView.setResultHandler(this);
            scannerView.startCamera();
        } else {
            requestPermission();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scannerView.stopCamera();
    }

    /**
     * showing alert message
     * @param listener listen for action click
     */
    public void displayAlertMessage(DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(ReaderActivity.this)
                .setMessage("you need to allow access for both permission")
                .setPositiveButton("OK", listener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
    @Override
    public void handleResult(Result result) {
        final String idDevice = result.getText();
        goToPlaceActivity(idDevice);
    }

    private void goToPlaceActivity(String idDevice) {
        Intent intent = new Intent(ReaderActivity.this, PlaceActivity.class);
        intent.putExtra("idDevice", idDevice);
        startActivity(intent);
    }

}
