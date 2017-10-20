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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.clsroom.MainActivity;
import com.clsroom.R;
import com.clsroom.adapters.ClassesAdapter;
import com.clsroom.dialogs.AddOrEditClassDialogFragment;
import com.clsroom.listeners.EventsListener;
import com.clsroom.model.Classes;
import com.clsroom.utils.ActionBarUtil;
import com.clsroom.utils.NavigationDrawerUtil;
import com.clsroom.utils.Otto;
import com.clsroom.utils.TransitionUtil;

import butterknife.Bind;
import butterknife.ButterKnife;


public class ClassesListFragment extends Fragment implements View.OnClickListener, EventsListener
{
    private static final String TAG = "ClassesListFragment";

    @Bind(R.id.classesListRecyclerView)
    RecyclerView mClassesListRecyclerView;

    @Bind(R.id.recyclerProgress)
    View mProgress;

    @Bind(R.id.errorMsg)
    View mErrorMsg;

    @Bind(R.id.fabContainer)
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
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mFabContainer.setOnClickListener(this);
        return parentView;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Activity activity = getActivity();
        if (activity instanceof MainActivity)
        {
            ((MainActivity) activity).setToolBarTitle(getString(R.string.classes));
            ((MainActivity) activity).updateEventsListener(this);
            Otto.post(ActionBarUtil.NO_MENU);
        }
        setUpRecyclerView();
    }

    private void setUpRecyclerView()
    {
        DatabaseReference classesDbRef = mRootRef.child(Classes.CLASSES);
        ClassesAdapter adapter = ClassesAdapter.getInstance(classesDbRef, getActivity());
        Log.d(TAG, "mAdapter : " + adapter);
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
                else if (dy < 0 && !mFabContainer.isShown() && NavigationDrawerUtil.isAdmin)
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
            }
        });
    }

    @Override
    public void onClick(View view)
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
        return NavigationDrawerUtil.CLASSES_LIST_FRAGMENT;
    }
}
