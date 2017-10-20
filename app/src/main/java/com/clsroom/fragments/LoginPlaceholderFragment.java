package com.clsroom.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.clsroom.R;

import static com.clsroom.LoginActivity.LOGIN_STATUS;

public class LoginPlaceholderFragment extends Fragment
{
    private static final String ARG_SECTION_NUMBER = "section_number";

    public LoginPlaceholderFragment()
    {
    }

    public static LoginPlaceholderFragment newInstance(int sectionNumber)
    {
        LoginPlaceholderFragment fragment = new LoginPlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        final TextView textView = rootView.findViewById(R.id.section_label);
        textView.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "Gabriola.ttf"));
        int pageNum = getArguments().getInt(ARG_SECTION_NUMBER);
        ImageView bgImg = (ImageView) rootView.findViewById(R.id.background_image);
        switch (pageNum)
        {
            case 1:
                bgImg.setImageResource(R.mipmap.notes);
                textView.setText(R.string.notes_management);
                break;
            case 2:
                bgImg.setImageResource(R.mipmap.reading);
                textView.setText(R.string.attendance_management);
                break;
            case 3:
                bgImg.setImageResource(R.mipmap.teacher);
                textView.setText(R.string.time_table_management);
                break;
        }

        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(LOGIN_STATUS, false))
        {
            Log.d("TextVisibilityIssue", "Hiding");
            textView.setVisibility(View.GONE);
        }
        return rootView;
    }
}
