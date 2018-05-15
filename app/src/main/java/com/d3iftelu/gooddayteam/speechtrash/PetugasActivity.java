package com.d3iftelu.gooddayteam.speechtrash;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PetugasActivity extends AppCompatActivity {
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_petugas);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        checkUser(mCurrentUser);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //to add option menu
        switch (item.getItemId()) {
            case R.id.list_maps:
                Intent maps = new Intent(PetugasActivity.this, MapsActivity.class);
                startActivity(maps);
                return true;
            case R.id.logout:
                signOutCheck();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void signOutCheck(){
        AlertDialog.Builder builder = new AlertDialog.Builder(PetugasActivity.this);
        builder.setMessage("Do you want to Logout ?")
                .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();
                        Intent intentLogin = new Intent(PetugasActivity.this, LoginActivity.class);
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
        Intent intentLogin = new Intent(PetugasActivity.this, LoginActivity.class);
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
