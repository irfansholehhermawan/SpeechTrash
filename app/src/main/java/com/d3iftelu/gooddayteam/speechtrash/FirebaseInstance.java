package com.d3iftelu.gooddayteam.speechtrash;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Sholeh Hermawan on 22/04/2018.
 */

public class FirebaseInstance extends FirebaseInstanceIdService {

    private static final String REG_TOKEN =  "REG_TOKEN";

    @Override

    public void onTokenRefresh() {
        String recent_token = FirebaseInstanceId.getInstance().getToken();
        PrefManager prefManager = new PrefManager(FirebaseInstance.this);
        prefManager.setToken(recent_token);
    }
}
