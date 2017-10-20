package com.clsroom;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

import static com.clsroom.LoginActivity.LOGIN_STATUS;

public class TestAuthActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_auth);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Log.d("UnknownLogin", "mAuth.getCurrentUser : " + mAuth.getCurrentUser()
                + ", onCreate : " + mSharedPreferences.getBoolean(LOGIN_STATUS, false));

        mSharedPreferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener()
        {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s)
            {
                Log.d("UnknownLogin", "String : " + s);
            }
        });

        mAuth.addAuthStateListener(this);
        /*mAuth.signOut();
        mAuth.getCurrentUser().delete();

        Log.d("UnknownLogin", "mAuth.getCurrentUser : " + mAuth.getCurrentUser()
                + ", onCreate : " + mSharedPreferences.getBoolean(LOGIN_STATUS, false));*/
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
    {
        Log.d("UnknownLogin", "onAuthStateChanged : " + firebaseAuth);
    }
}
