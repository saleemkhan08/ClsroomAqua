package com.clsroom.fcm;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.clsroom.model.User;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class ClsroomInstanceIdService extends FirebaseInstanceIdService
{
    private static final String TAG = "ClsroomInstanceIdService";

    @Override
    public void onTokenRefresh()
    {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String refreshedToken)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit().putString(User.TOKEN, refreshedToken).apply();
    }
}
