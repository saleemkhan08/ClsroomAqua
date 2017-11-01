package com.clsroom.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.clsroom.R;
import com.clsroom.adapters.NotesAdapter;
import com.clsroom.adapters.TimeTableAdapter;
import com.clsroom.listeners.EventsListener;
import com.clsroom.listeners.FragmentLauncher;
import com.clsroom.model.Classes;
import com.clsroom.model.Notes;
import com.clsroom.model.NotesClassifier;
import com.clsroom.model.Snack;
import com.clsroom.model.Subjects;
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
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class NotesFragment extends ClassTabFragment implements EventsListener
{
    private static final String TAG = NavigationUtil.NOTES_FRAGMENT;

    @Bind(R.id.notesRecyclerView)
    RecyclerView mNotesRecyclerView;

    @Bind(R.id.recyclerProgress)
    View mProgress;

    @Bind(R.id.errorMsg)
    View mErrorMsg;

    @Bind(R.id.fabContainer)
    ViewGroup mFabContainer;

    @Bind(R.id.subjectsTab)
    TabLayout mSubjectsTab;


    private DatabaseReference mRootRef;
    private DatabaseReference mNotesDbRef;
    private String mCurrentSubjectCode;
    private DatabaseReference mSubjectDbRef;
    private NotesClassifier mCurrentNotesClassifier;
    private boolean areSubjectsAvailable;
    private NotesAdapter mAdapter;
    private FragmentLauncher launcher;
    private String notesId;

    public NotesFragment()
    {
        Log.d("LifeCycleCheck", "Constructor");
        mCurrentNotesClassifier = new NotesClassifier();
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        Log.d("LifeCycleCheck", "onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d("LifeCycleCheck", "onCreate : savedInstanceState : " + savedInstanceState);
    }

    @Override
    public void onCreateView(View parentView)
    {
        Log.d("LifeCycleCheck", "onCreateView");
        ButterKnife.bind(this, parentView);
        Otto.register(this);
        setLauncher();
        if (launcher != null)
        {
            launcher.setToolBarTitle(R.string.notes);
        }
        mRootRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        Log.d("LifeCycleCheck", "onActivityCreated : savedInstanceState : " + savedInstanceState);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Log.d("LifeCycleCheck", "onStart");
        if (launcher != null)
        {
            launcher.updateEventsListener(this);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.d("LifeCycleCheck", "onResume");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        Log.d("LifeCycleCheck", "onPause");
    }

    @Override
    public void onStop()
    {
        super.onStop();
        Log.d("LifeCycleCheck", "onStop");
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        Log.d("LifeCycleCheck", "onSaveInstanceState");
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        Log.d("LifeCycleCheck", "onDestroyView");
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.d("LifeCycleCheck", "onDestroy");
        Otto.unregister(this);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        Log.d("LifeCycleCheck", "onDetach");
    }

    private void setUpSubjectsTabsListener()
    {
        mSubjectsTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                Subjects subject = (Subjects) tab.getTag();
                mCurrentSubjectCode = subject.getSubjectCode();
                mCurrentNotesClassifier.setSubjectId(mCurrentSubjectCode);
                mCurrentNotesClassifier.setSubjectName(subject.getSubjectName());
                if (mCurrentClass != null)
                {
                    setUpRecyclerView();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {

            }
        });
    }


    @Override
    protected int getContentViewLayoutRes()
    {
        Log.d(TAG, "getContentViewLayoutRes");
        return R.layout.fragment_notes;
    }

    private void setUpRecyclerView()
    {
        Log.d(TAG, "setUpRecyclerView");
        if (mCurrentNotesClassifier.isReviewedNotesShown())
        {
            Otto.post(ActionBarUtil.SHOW_PENDING_NOTES_FRAGMENT_MENU);
            mNotesDbRef = mRootRef.child(Notes.NOTES).child(mCurrentClass.getCode()).child(mCurrentSubjectCode);
        }
        else
        {
            Otto.post(ActionBarUtil.SHOW_NOTES_FRAGMENT_MENU);
            mNotesDbRef = mRootRef.child(Notes.NOTES).child(Notes.REVIEW).child(mCurrentClass.getCode()).child(mCurrentSubjectCode);
        }

        mAdapter = NotesAdapter.getInstance(mCurrentNotesClassifier, mNotesDbRef, launcher);
        mNotesRecyclerView.setAdapter(mAdapter);
        mNotesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mNotesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if (dy > 50 && mFabContainer.isShown())
                {
                    TransitionUtil.slideTransition(mFabContainer);
                    mFabContainer.setVisibility(View.GONE);
                }
                else if (dy < 0 && !mFabContainer.isShown())
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
        mNotesDbRef.addValueEventListener(new ValueEventListener()
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

    @OnClick(R.id.addNotes)
    public void addNotes(View view)
    {
        if (ConnectivityUtil.isConnected(getActivity()))
        {
            if (launcher != null)
            {
                launcher.replaceFragment(AddOrEditNotesFragment.getInstance(mCurrentNotesClassifier),
                        true, AddOrEditNotesFragment.TAG);
            }
        }
        else
        {
            Snack.show(R.string.noInternet);
        }
    }

    @Override
    public boolean onBackPressed()
    {
        if (TimeTableAdapter.isSelectionEnabled)
        {
            TimeTableAdapter.isSelectionEnabled = false;
            mAdapter.notifyDataSetChanged();
            Otto.post(ActionBarUtil.SHOW_INDEPENDENT_TIME_TABLE_MENU);
            return false;
        }
        return true;
    }

    @Override
    public int getMenuItemId()
    {
        return R.id.launch_notes_fragment;
    }

    @Override
    public String getTagName()
    {
        return NavigationUtil.NOTES_FRAGMENT;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab)
    {
        if (mTabSelected)
        {
            mCurrentClass = (Classes) tab.getTag();
        }
        mCurrentNotesClassifier.setClassId(mCurrentClass.getCode());
        mCurrentNotesClassifier.setClassName(mCurrentClass.getName());
        Log.d("TabSelectionIssue", "NotesFragment > onTabSelected > mCurrentClass : " + mCurrentClass);
        updateSubjectsAvailability();
        setUpSubjectsTabsListener();
    }


    private void updateSubjectsAvailability()
    {
        mSubjectDbRef = FirebaseDatabase.getInstance().getReference()
                .child(Subjects.SUBJECTS).child(mCurrentClass.getCode());
        mSubjectDbRef.addListenerForSingleValueEvent(new ValueEventListener()
                                                     {
                                                         @Override
                                                         public void onDataChange(DataSnapshot dataSnapshot)
                                                         {
                                                             areSubjectsAvailable = (dataSnapshot.getChildrenCount() > 0);
                                                             mSubjectsTab.removeAllTabs();
                                                             for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                                             {
                                                                 Subjects subject = snapshot.getValue(Subjects.class);
                                                                 TabLayout.Tab tab = mSubjectsTab.newTab();
                                                                 tab.setText(subject.getSubjectName());
                                                                 tab.setTag(subject);
                                                                 mSubjectsTab.addTab(tab);
                                                             }
                                                             if (mSubjectsTab.getTabAt(0) != null)
                                                             {
                                                                 mSubjectsTab.getTabAt(0).select();
                                                             }
                                                         }

                                                         @Override
                                                         public void onCancelled(DatabaseError databaseError)
                                                         {

                                                         }
                                                     }

                                                    );
    }

    @Subscribe
    public void onOptionItemClicked(Integer itemId)
    {
        switch (itemId)
        {
            case R.id.reviewed:
                mCurrentNotesClassifier.setReviewedNotesShown(true);
                setUpRecyclerView();
                break;
            case R.id.pending:
                mCurrentNotesClassifier.setReviewedNotesShown(false);
                setUpRecyclerView();
                break;
        }
    }

    private void setLauncher()
    {
        Activity activity = getActivity();
        if (activity instanceof FragmentLauncher)
        {
            launcher = (FragmentLauncher) activity;
        }
    }

    public static NotesFragment getInstance(String notesId)
    {
        NotesFragment fragment = new NotesFragment();
        fragment.notesId = notesId;
        return fragment;
    }
}
