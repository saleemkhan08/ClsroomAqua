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

import com.clsroom.R;
import com.clsroom.adapters.NotificationsAdapter;
import com.clsroom.listeners.EventsListener;
import com.clsroom.listeners.FragmentLauncher;
import com.clsroom.model.Notifications;
import com.clsroom.utils.ActionBarUtil;
import com.clsroom.utils.NavigationUtil;
import com.clsroom.utils.Otto;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.Bind;
import butterknife.ButterKnife;


public class NotificationListFragment extends Fragment implements EventsListener
{
    private static final String TAG = NavigationUtil.NOTIFICATIONS_FRAGMENT;

    @Bind(R.id.notificationListRecyclerView)
    RecyclerView notificationListRecyclerView;

    @Bind(R.id.recyclerProgress)
    View mProgress;

    @Bind(R.id.errorMsg)
    View mErrorMsg;

    private DatabaseReference mRootRef;
    private FragmentLauncher launcher;

    public NotificationListFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_notifications_list, container, false);
        ButterKnife.bind(this, parentView);
        setLauncher();
        Log.d("NotificationListIssue", "onCreateView");
        mRootRef = FirebaseDatabase.getInstance().getReference();
        if (launcher != null)
        {
            launcher.setToolBarTitle(R.string.notifications);
        }
        setUpRecyclerView();
        return parentView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        if (launcher != null)
        {
            launcher.updateEventsListener(this);
            Otto.post(ActionBarUtil.NO_MENU);
        }
    }

    private void setUpRecyclerView()
    {
        DatabaseReference notificationsDbRef = mRootRef.child(Notifications.NOTIFICATIONS)
                .child(NavigationUtil.mCurrentUser.getUserId());
        Log.d("NotificationListIssue", "notificationsDbRef : " + notificationsDbRef);
        NotificationsAdapter adapter = NotificationsAdapter.getInstance(notificationsDbRef, launcher);
        notificationListRecyclerView.setAdapter(adapter);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        notificationListRecyclerView.setLayoutManager(manager);

        notificationsDbRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Log.d("NotificationListIssue", "dataSnapshot : " + dataSnapshot);
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
                Log.d("NotificationListIssue", "databaseError : " + databaseError);
                mProgress.setVisibility(View.GONE);
                mErrorMsg.setVisibility(View.VISIBLE);
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
        return NavigationUtil.NOTIFICATIONS_FRAGMENT;
    }

    private void setLauncher()
    {
        Activity activity = getActivity();
        if (activity instanceof FragmentLauncher)
        {
            launcher = (FragmentLauncher) activity;
        }
    }
}
