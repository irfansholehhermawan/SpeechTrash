package com.d3iftelu.gooddayteam.speechtrash;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.d3iftelu.gooddayteam.speechtrash.adapter.PetugasListAdapter;
import com.d3iftelu.gooddayteam.speechtrash.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListPetugasFragment extends Fragment {
    private static final String TAG = "ListPetugasFragment";
    private ListView petugasListView;
    private PetugasListAdapter petugasAdapter;
    private TextView emptyNotification;
    private ProgressBar loadingData;
    private ArrayList<User> dataPetugas = new ArrayList<>();

    public ListPetugasFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_list_petugas, container, false);


        petugasListView = (ListView) rootView.findViewById(R.id.list_view_petugas);
        loadingData = (ProgressBar) rootView.findViewById(R.id.item_progres_bar);
        emptyNotification = (TextView) rootView.findViewById(R.id.infoTextView);

        ArrayList<User> realData = getData();
        petugasAdapter = new PetugasListAdapter(getContext(),realData);
        petugasListView.setAdapter(petugasAdapter);
        petugasListView.setEmptyView(emptyNotification);
//        getDataPetugas();
        return rootView;
    }

    private void getDataPetugas() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference scheduleRef = database.getReference("list_petugas");
        scheduleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //for (DataSnapshot realDataSnapshot : dataSnapshot.getChildren()) {

                for (DataSnapshot snap : dataSnapshot.getChildren()){
                    User schedule = snap.getValue(User.class);
                    if (schedule != null) {
                        dataPetugas.add(schedule);
                        Log.d(TAG,"Data"+schedule.getName());
                    }else{
                        Log.d(TAG,"ZONK");
                    }
                }

                //}
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public ArrayList<User> getData() {
        final ArrayList<User> currentPetugas = new ArrayList<>();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        loadingData.setVisibility(View.VISIBLE);
        DatabaseReference scheduleRef = database.getReference("list_petugas");
        scheduleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentPetugas.clear();
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    final User data = postSnapshot.getValue(User.class);
                    final User dataId = new User(data, postSnapshot.getKey());
                    currentPetugas.add(dataId);
                }
                petugasAdapter.notifyDataSetChanged();
                loadingData.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Error", "loadPost:onCancelled", databaseError.toException());
            }
        });
        return currentPetugas;
    }
}
