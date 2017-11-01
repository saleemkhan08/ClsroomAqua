package com.clsroom.listeners;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public interface FragmentLauncher
{
    void setToolBarTitle(int resId);
    void updateEventsListener(EventsListener listener);
    FragmentManager getSupportFragmentManager();
    AppCompatActivity getActivity();
    void replaceFragment(Fragment instance, boolean addToBackStack, String tag);
    void addFragment(Fragment instance, boolean addToBackStack, String tag);
    String getString(int resId);
    Fragment getFragment();
    void setFragment(Fragment fragment);
    void replaceFragment(Fragment fragment, boolean addToBackStack, String tag, View sharedImageView, String transitionName);
    void addFragment(Fragment fragment, boolean addToBackStack, String tag, View sharedImageView, String transitionName);
}
