package com.clsroom.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.clsroom.MainActivity;
import com.clsroom.R;
import com.clsroom.adapters.NotificationsAdapter;
import com.clsroom.listeners.EventsListener;
import com.clsroom.model.Notifications;
import com.clsroom.utils.ActionBarUtil;
import com.clsroom.utils.NavigationDrawerUtil;
import com.clsroom.utils.Otto;

import butterknife.Bind;
import butterknife.ButterKnife;


public class NotificationListFragment extends Fragment implements EventsListener
{
    private static final String TAG = "NotificationFragment";

    @Bind(R.id.notificationListRecyclerView)
    RecyclerView notificationListRecyclerView;

    @Bind(R.id.recyclerProgress)
    View mProgress;

    @Bind(R.id.errorMsg)
    View mErrorMsg;

    private DatabaseReference mRootRef;

    public NotificationListFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_notifications_list, container, false);
        ButterKnife.bind(this, parentView);
        mRootRef = FirebaseDatabase.getInstance().getReference();
        return parentView;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Activity activity = getActivity();
        if (activity instanceof MainActivity)
        {
            ((MainActivity) activity).setToolBarTitle(getString(R.string.notifications));
            ((MainActivity) activity).updateEventsListener(this);
            Otto.post(ActionBarUtil.NO_MENU);
        }
        setUpRecyclerView();
    }

    private void setUpRecyclerView()
    {
        DatabaseReference notificationsDbRef = mRootRef.child(Notifications.NOTIFICATIONS)
                .child(NavigationDrawerUtil.mCurrentUser.getUserId());
        NotificationsAdapter adapter = NotificationsAdapter.getInstance(notificationsDbRef, getActivity());
        notificationListRecyclerView.setAdapter(adapter);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        notificationListRecyclerView.setLayoutManager(manager);

        notificationsDbRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                mProgress.setVisibility(View.GONE);
                if (dataSnapshot.getChildrenCount() <= 0)
                {
                    mErrorMsg.setVisibility(View.VISIBLE);
                }
                else
                {
                    mErrorMsg.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG, "databaseError : " + databaseError);
            }
        });
    }

    @Override
    public boolean onBackPressed()
    {
        return true;
    }

    @Override
    public int getMenuItemId()
    {
        return R.id.nav_notifications;
    }

    @Override
    public String getTagName()
    {
        return NavigationDrawerUtil.NOTIFICATIONS_FRAGMENT;
    }
}
