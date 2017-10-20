package com.clsroom.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.clsroom.fragments.LoginPlaceholderFragment;

public class LoginSectionsPagerAdapter extends FragmentPagerAdapter
{
    public LoginSectionsPagerAdapter(FragmentManager fragmentManager)
    {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position)
    {
        return LoginPlaceholderFragment.newInstance(position + 1);
    }

    @Override
    public int getCount()
    {
        return 3;
    }

}
