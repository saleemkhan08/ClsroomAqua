package com.clsroom.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.clsroom.R;
import com.clsroom.adapters.ClassesAdapter;
import com.clsroom.dialogs.AddOrEditClassDialogFragment;
import com.clsroom.listeners.EventsListener;
import com.clsroom.listeners.FragmentLauncher;
import com.clsroom.model.Classes;
import com.clsroom.model.Snack;
import com.clsroom.utils.ActionBarUtil;
import com.clsroom.utils.ConnectivityUtil;
import com.clsroom.utils.NavigationUtil;
import com.clsroom.utils.Otto;
import com.clsroom.utils.TransitionUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ClassesListFragment extends Fragment implements View.OnClickListener, EventsListener
{
    private static final String TAG = NavigationUtil.CLASSES_LIST_FRAGMENT;
    private FragmentLauncher launcher;

    @BindView(R.id.classesListRecyclerView)
    RecyclerView mClassesListRecyclerView;

    @BindView(R.id.recyclerProgress)
    View mProgress;

    @BindView(R.id.errorMsg)
    View mErrorMsg;

    @BindView(R.id.fabContainer)
    RelativeLayout mFabContainer;

    private DatabaseReference mRootRef;

    public ClassesListFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_classes_list, container, false);
        ButterKnife.bind(this, parentView);
        setLauncher();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mFabContainer.setOnClickListener(this);
        setUpRecyclerView();
        refreshActionBar();
        return parentView;
    }

    private void setUpRecyclerView()
    {
        DatabaseReference classesDbRef = mRootRef.child(Classes.CLASSES);
        ClassesAdapter adapter = ClassesAdapter.getInstance(classesDbRef, launcher);
        Log.d(TAG, "mAdapter : " + adapter);
        mProgress.setVisibility(View.VISIBLE);
        mErrorMsg.setVisibility(View.GONE);
        mClassesListRecyclerView.setAdapter(adapter);
        mClassesListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mClassesListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if (dy > 50 && mFabContainer.isShown())
                {
                    TransitionUtil.slideTransition(mFabContainer);
                    mFabContainer.setVisibility(View.GONE);
                }
                else if (dy < 0 && !mFabContainer.isShown() && NavigationUtil.isAdmin)
                {
                    TransitionUtil.slideTransition(mFabContainer);
                    mFabContainer.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState)
            {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        classesDbRef.addValueEventListener(new ValueEventListener()
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
                mProgress.setVisibility(View.GONE);
                mErrorMsg.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onClick(View view)
    {
        if (ConnectivityUtil.isConnected(getActivity()))
        {
            FragmentManager manager = getActivity().getSupportFragmentManager();
            AddOrEditClassDialogFragment fragment = (AddOrEditClassDialogFragment) manager
                    .findFragmentByTag(AddOrEditClassDialogFragment.TAG);

            if (fragment == null)
            {
                fragment = new AddOrEditClassDialogFragment();
            }
            fragment.show(manager, AddOrEditClassDialogFragment.TAG);
        }
        else
        {
            Snack.show(R.string.noInternet);
        }
    }

    @Override
    public boolean onBackPressed()
    {
        return true;
    }

    @Override
    public int getMenuItemId()
    {
        return R.id.admin_classes;
    }

    @Override
    public String getTagName()
    {
        return NavigationUtil.CLASSES_LIST_FRAGMENT;
    }

    private void setLauncher()
    {
        Activity activity = getActivity();
        if (activity instanceof FragmentLauncher)
        {
            launcher = (FragmentLauncher) activity;
        }
        launcher.hideInitialProgressBar();
    }
    @Override
    public void refreshActionBar()
    {
        if (launcher != null)
        {
            launcher.updateEventsListener(this);
            Otto.post(ActionBarUtil.NO_MENU);
            launcher.setToolBarTitle(R.string.classes);
        }
    }
}
