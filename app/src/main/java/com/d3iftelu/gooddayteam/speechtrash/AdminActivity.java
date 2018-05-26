package com.d3iftelu.gooddayteam.speechtrash;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdminActivity extends AppCompatActivity {
    private static final String TAG = "AdminActivity";
    private FirebaseUser mCurrentUser;
    private boolean cek;
    private String cekMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        Log.i(TAG, "MENU: " + cekMenu);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        checkUser(mCurrentUser);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        setUpLandingFragment();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_maps:
                    changeFragment(new MapsFragment());
                    cekMenu = "maps";
                    Log.i(TAG, "MENU: " + cekMenu);
                    return true;
                case R.id.navigation_list_device:
                    changeFragment(new ListDeviceFragment());
                    cekMenu = "device";
                    Log.i(TAG, "MENU: " + cekMenu);
                    return true;
                case R.id.navigation_list_petugas:
                    changeFragment(new ListPetugasFragment());
                    cekMenu = "officers";
                    Log.i(TAG, "MENU: " + cekMenu);
                    return true;
            }
            return false;
        }
    };

    private void changeFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();
    }

    private void setUpLandingFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack();
        fragmentManager.beginTransaction().add(R.id.content, new MapsFragment()).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_admin, menu);
        return true;
    }

//    /**
//     * This method is called after invalidateOptionsMenu(), so that the
//     * menu can be updated (some menu items can be hidden or made visible).
//     */
//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        super.onPrepareOptionsMenu(menu);
//        if (cekMenu == null) {
//            MenuItem menuItem = menu.findItem(R.id.add_device_with_text);
//            menuItem.setVisible(false);
//        }
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //to add option menu
        switch (item.getItemId()) {
            case R.id.logout:
                signOutCheck();
                return true;
            case R.id.add_device_with_text:
                addDeviceWithText();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addDeviceWithText() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AdminActivity.this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();

        View viewDialog = inflater.inflate(R.layout.dialog_add_device_with_text, null);
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
                        String deviceID = editTextName.getText().toString().trim();
                        readDeviceData(deviceID);
                        if (!cek){
                            goToPlaceActivity(deviceID);
                        }
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

    private void readDeviceData(final String deviceID) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference();
        myRef.keepSynced(true);
        myRef.child("list_device").child(mCurrentUser.getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getKey().equals(deviceID)){
                    cekId(deviceID);
                } else {
                    cek = false;
                }
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
        });
    }

    private void cekId(String deviceID){
        Intent intent = new Intent(AdminActivity.this, DetailActivity.class);
        intent.putExtra(DetailActivity.ARGS_DEVICE_ID, deviceID);
        startActivity(intent);
    }

    private void goToPlaceActivity(String deviceID) {
        Intent intent = new Intent(AdminActivity.this, PlaceActivity.class);
        intent.putExtra("idDevice", deviceID);
        startActivity(intent);
    }

    private void signOutCheck(){
        AlertDialog.Builder builder = new AlertDialog.Builder(AdminActivity.this);
        builder.setMessage("Do you want to Logout ?")
                .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();
                        Intent intentLogin = new Intent(AdminActivity.this, LoginActivity.class);
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
        Intent intentLogin = new Intent(AdminActivity.this, LoginActivity.class);
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
