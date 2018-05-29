package com.d3iftelu.gooddayteam.speechtrash;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DetailActivity extends AppCompatActivity {

    public static final String ARGS_DEVICE_NAME = "device name";
    public static final String ARGS_DEVICE_ID = "device id";
    private static final String TAG = DetailActivity.class.getSimpleName();
    private String mDeviceId, mDeviceName;
    private FirebaseUser currentUser;
    private DatabaseReference mDatabaseReference;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
//        String mDeviceName = intent.getStringExtra(DetailActivity.ARGS_DEVICE_NAME);
        mDeviceId = intent.getStringExtra(DetailActivity.ARGS_DEVICE_ID);
        mDeviceName = intent.getStringExtra(DetailActivity.ARGS_DEVICE_NAME);
        Log.i(TAG, "mDeviceId: " + mDeviceId);
        Log.i(TAG, "mDeviceName: " + mDeviceName);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.content_tabs);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        setUpLandingFragment();

        setTitle(mDeviceId);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        private SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new RealtimeChartFragment();
                case 1:
                    return new HistoryChartFragment();
                case 2:
                    return new PredictionFragment();
                default:
                    return DetailActivity.PlaceholderFragment.newInstance(position + 1);
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Monitoring";
                case 1:
                    return "History";
                case 2:
                    return "Prediction";
            }
            return null;
        }
    }

    /**
     * for seting landing page
     */
    private void setUpLandingFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack();
        fragmentManager.beginTransaction().add(R.id.content, new RealtimeChartFragment()).commit();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static DetailActivity.PlaceholderFragment newInstance(int sectionNumber) {
            DetailActivity.PlaceholderFragment fragment = new DetailActivity.PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_tabs, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail_device_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //to add option menu
        switch (item.getItemId()) {
            case R.id.change_petugas:
                showDialogPetugas();
                return true;
            case R.id.action_edit:
                editDevice(mDeviceId);
                return true;
            case R.id.action_delete:
                deleteDevice(mDeviceId);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDialogPetugas() {
        final String[] userId = new String[1];
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View viewDialog = inflater.inflate(R.layout.change_petugas_view_controller,null);

        final TextView textViewName = (TextView) viewDialog.findViewById(R.id.name_petugas);

        mDatabaseReference.child("device").child(mDeviceId).child("user_id").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot != null) {
                        userId[0] = dataSnapshot.getValue(String.class);

                        mDatabaseReference.child("list_petugas").child(userId[0]).child("name").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String namePetugas = dataSnapshot.getValue(String.class);
                                textViewName.setText(namePetugas);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    } else {
                        textViewName.setText("No Data Officers!");
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final View title = inflater.inflate(R.layout.text_view_customize,null);

        final ImageView icon = (ImageView) title.findViewById(R.id.custom_icon_of_title);
        final TextView text = (TextView) title.findViewById(R.id.custom_text_of_title);

        icon.setImageResource(R.drawable.ic_info_black_24dp);
        text.setText("Change Officer");
        builder.setCustomTitle(title);

        builder.setView(viewDialog)
                .setPositiveButton(R.string.dialog_change, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        gotoChangePetugas(userId[0]);
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

    private void editDevice(final String idDevice) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View viewDialog = inflater.inflate(R.layout.dialog_device_name,null);

        final EditText editTextName = (EditText) viewDialog.findViewById(R.id.edit_text_name);

        mDatabaseReference.child("list_device").child("admin").child(currentUser.getUid()).child(idDevice).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String nameDeviceText = dataSnapshot.getValue(String.class);
                editTextName.setText(nameDeviceText);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final View title = inflater.inflate(R.layout.text_view_customize,null);

        final ImageView icon = (ImageView) title.findViewById(R.id.custom_icon_of_title);
        final TextView text = (TextView) title.findViewById(R.id.custom_text_of_title);

        icon.setImageResource(R.drawable.ic_info_black_24dp);
        text.setText("Edit Device");
        builder.setCustomTitle(title);

        builder.setView(viewDialog)
                .setPositiveButton(R.string.dialog_submit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String deviceName = editTextName.getText().toString().trim();
                        saveToDatabase(idDevice, deviceName);
                        Toast.makeText(DetailActivity.this, "Data Successfully Updated!", Toast.LENGTH_SHORT).show();
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

    private void saveToDatabase(String idDevice, String deviceName) {
        mDatabaseReference.child("list_device").child("admin").child(currentUser.getUid()).child(idDevice).setValue(deviceName);
        mDatabaseReference.child("list_maps").child(idDevice).child("name").setValue(deviceName);
        mDatabaseReference.child("device").child(idDevice).child("deviceName").setValue(deviceName);
    }

    private void deleteDevice(final String idDevice) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_data_question)
                .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteData(idDevice);
                    }
                })
                .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }

    private void deleteData(String idDevice) {
        mDatabaseReference.child("device").child(idDevice).removeValue();
        mDatabaseReference.child("list_device").child("admin").child(currentUser.getUid()).child(idDevice).removeValue();
        mDatabaseReference.child("list_maps").child(idDevice).removeValue();
        Toast.makeText(this, "Data Successfully Deleted!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void gotoChangePetugas(String userID) {
        Intent gotoChangePetugas = new Intent(this, ListPetugasActivity.class);
        gotoChangePetugas.putExtra(DetailActivity.ARGS_DEVICE_ID, mDeviceId);
        gotoChangePetugas.putExtra(DetailActivity.ARGS_DEVICE_NAME, mDeviceName);
        gotoChangePetugas.putExtra("user_id", userID);
        startActivity(gotoChangePetugas);
    }
}
