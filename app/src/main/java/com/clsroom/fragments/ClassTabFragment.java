package com.clsroom.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.clsroom.R;
import com.clsroom.model.Classes;
import com.clsroom.model.User;
import com.clsroom.utils.NavigationUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public abstract class ClassTabFragment extends Fragment implements ValueEventListener, TabLayout.OnTabSelectedListener
{
    RelativeLayout mContent;
    TabLayout mTabLayout;
    private static final String TAG = "ClassTabFragment";
    private String mClassId;
    public Classes mCurrentClass;
    public boolean mTabSelected;
    private int mCurrentClassIndex;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_classes_tab, container, false);
        mTabLayout = parentView.findViewById(R.id.classesTab);
        mContent = parentView.findViewById(R.id.fragmentContent);
        mContent.removeAllViews();
        mContent.addView(inflater.inflate(getContentViewLayoutRes(), null));
        DatabaseReference classesRef = FirebaseDatabase.getInstance().getReference().child(Classes.CLASSES);
        handleUser();
        Log.d("TabSelectionIssue", "ClassTabFragment > onCreateView > mCurrentClass : " + mCurrentClass);
        classesRef.addListenerForSingleValueEvent(this);
        mTabSelected = mCurrentClass == null;
        mTabLayout.addOnTabSelectedListener(this);
        onCreateView(mContent);
        return parentView;
    }

    protected abstract int getContentViewLayoutRes();

    @Override
    public void onDataChange(DataSnapshot dataSnapshot)
    {
        mTabLayout.removeAllTabs();
        if (mClassId == null)
        {
            int index = 0;
            Log.d("TabSelectionIssue", "ClassTabFragment > onDataChange > mCurrentClass : " + mCurrentClass);
            for (DataSnapshot snapshot : dataSnapshot.getChildren())
            {
                Classes classes = snapshot.getValue(Classes.class);
                TabLayout.Tab tab = mTabLayout.newTab();
                tab.setText(classes.getName());
                tab.setTag(classes);
                mTabLayout.addTab(tab);
                Log.d("TabSelectionIssue", "ClassTabFragment > mCurrentClass : " + mCurrentClass);
                if (mCurrentClass != null)
                {
                    if (mCurrentClass.getCode().equals(classes.getCode()))
                    {
                        mCurrentClassIndex = index;
                        selectThisTab();
                    }
                }
                index++;
            }
        }
        else
        {
            for (DataSnapshot snapshot : dataSnapshot.getChildren())
            {
                Classes classes = snapshot.getValue(Classes.class);

                if (mClassId.equals(classes.getCode()))
                {
                    TabLayout.Tab tab = mTabLayout.newTab();
                    tab.setText(classes.getName());
                    tab.setTag(classes);
                    mTabLayout.addTab(tab);
                    mTabLayout.setVisibility(View.GONE);
                    return;
                }
            }
        }
    }

    private void selectThisTab()
    {
        new Handler().post(new Runnable()
        {
            @Override
            public void run()
            {
                TabLayout.Tab tab = mTabLayout.getTabAt(mCurrentClassIndex);
                if (tab != null)
                {
                    tab.select();
                }
                mTabSelected = true;
            }
        });
    }

    @Override
    public void onCancelled(DatabaseError databaseError)
    {

    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab)
    {

    }

    public abstract void onCreateView(View parentView);

    @Override
    public void onTabReselected(TabLayout.Tab tab)
    {

    }

    private void handleUser()
    {
        switch (NavigationUtil.mCurrentUser.userType())
        {
            case User.STUDENTS:
                mClassId = NavigationUtil.getClassId();
                handleStudent();
                break;
            case User.STAFF:
                handleStaff();
                break;
        }
    }

    public void handleStaff()
    {

    }

    public void handleStudent()
    {

    }
}
