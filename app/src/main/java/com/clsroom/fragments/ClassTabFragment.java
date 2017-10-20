package com.clsroom.fragments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.clsroom.R;
import com.clsroom.model.Classes;
import com.clsroom.model.User;
import com.clsroom.utils.NavigationDrawerUtil;

public abstract class ClassTabFragment extends Fragment implements ValueEventListener, TabLayout.OnTabSelectedListener
{
    RelativeLayout mContent;
    TabLayout mTabLayout;

    private DatabaseReference mClassesRef;
    private String mClassId;
    public static Classes sCurrentClass;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_classes_tab, container, false);
        mTabLayout = (TabLayout) parentView.findViewById(R.id.classesTab);

        mContent = (RelativeLayout) parentView.findViewById(R.id.fragmentContent);
        mContent.removeAllViews();
        mContent.addView(inflater.inflate(getContentViewLayoutRes(), null));
        onCreateView(mContent);
        mClassesRef = FirebaseDatabase.getInstance().getReference().child(Classes.CLASSES);
        handleUser();
        return parentView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        mClassesRef.addListenerForSingleValueEvent(this);
        mTabLayout.removeAllTabs();
        mTabLayout.addOnTabSelectedListener(this);
    }

    protected abstract int getContentViewLayoutRes();

    @Override
    public void onDataChange(DataSnapshot dataSnapshot)
    {
        if (mClassId == null)
        {
            sCurrentClass = null;
            for (DataSnapshot snapshot : dataSnapshot.getChildren())
            {
                Classes classes = snapshot.getValue(Classes.class);
                TabLayout.Tab tab = mTabLayout.newTab();
                tab.setText(classes.getName());
                tab.setTag(classes);
                mTabLayout.addTab(tab);
            }
        }
        else
        {
            for (DataSnapshot snapshot : dataSnapshot.getChildren())
            {
                sCurrentClass = snapshot.getValue(Classes.class);

                if (mClassId.equals(sCurrentClass.getCode()))
                {
                    TabLayout.Tab tab = mTabLayout.newTab();
                    tab.setText(sCurrentClass.getName());
                    tab.setTag(sCurrentClass);
                    mTabLayout.addTab(tab);
                    mTabLayout.setVisibility(View.GONE);
                    return;
                }
            }
        }
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
        switch (NavigationDrawerUtil.mCurrentUser.userType())
        {
            case User.STUDENTS:
                mClassId = NavigationDrawerUtil.getClassId();
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
