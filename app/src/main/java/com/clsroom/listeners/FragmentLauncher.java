package com.clsroom.listeners;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

public interface FragmentLauncher
{
    void setToolBarTitle(int resId);
    void updateEventsListener(EventsListener listener);
    FragmentManager getSupportFragmentManager();
    AppCompatActivity getActivity();
    void showFragment(Fragment instance, boolean addToBackStack, String tag);
    String getString(int resId);
    Fragment getFragment();
    void setFragment(Fragment fragment);
    void showFragment(Fragment fragment, boolean addToBackStack, String tag, ImageView sharedImageView, String transitionName);
}
