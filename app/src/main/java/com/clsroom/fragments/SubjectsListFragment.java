package com.clsroom.fragments;

import android.app.Activity;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.clsroom.R;
import com.clsroom.adapters.SubjectsAdapter;
import com.clsroom.dialogs.AddOrEditSubjectsDialogFragment;
import com.clsroom.listeners.EventsListener;
import com.clsroom.listeners.FragmentLauncher;
import com.clsroom.model.Classes;
import com.clsroom.model.Progress;
import com.clsroom.model.Snack;
import com.clsroom.model.Subjects;
import com.clsroom.model.ToastMsg;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class SubjectsListFragment extends ClassTabFragment implements EventsListener
{
    private static final String TAG = NavigationUtil.SUBJECTS_FRAGMENT;

    @BindView(R.id.subjetcsListRecyclerView)
    RecyclerView mSubjectsListRecyclerView;

    @BindView(R.id.recyclerProgress)
    View mProgress;

    @BindView(R.id.errorMsg)
    View mErrorMsg;

    @BindView(R.id.fabContainer)
    ViewGroup mFabContainer;

    @BindView(R.id.addSubjects)
    View mAddSubjectsFab;

    private DatabaseReference mRootRef;
    private SubjectsAdapter mAdapter;
    private DatabaseReference mSubjectsDbRef;
    private Handler mHandler;
    private FragmentLauncher launcher;

    public SubjectsListFragment()
    {
        Log.d(TAG, "SubjectsListFragment");
    }

    @Override
    public void onCreateView(View parentView)
    {
        Log.d(TAG, "onCreateView2");
        ButterKnife.bind(this, parentView);
        Otto.register(this);
        setLauncher();
        mHandler = new Handler();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        refreshActionBar();
    }


    @Override
    protected int getContentViewLayoutRes()
    {
        Log.d(TAG, "getContentViewLayoutRes");
        return R.layout.fragment_subjects_list;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        Otto.unregister(this);
    }

    private void setUpRecyclerView()
    {
        Log.d(TAG, "setUpRecyclerView");
        mProgress.setVisibility(View.VISIBLE);
        mErrorMsg.setVisibility(View.GONE);

        mSubjectsDbRef = mRootRef.child(Subjects.SUBJECTS).child(mCurrentClass.getCode());
        mAdapter = SubjectsAdapter.getInstance(mSubjectsDbRef, launcher);
        mSubjectsListRecyclerView.setAdapter(mAdapter);
        mSubjectsListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSubjectsListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
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
        mSubjectsDbRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Log.d(TAG, "Data : " + dataSnapshot);
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
                mProgress.setVisibility(View.GONE);
                mErrorMsg.setVisibility(View.VISIBLE);
                Log.d(TAG, "databaseError : " + databaseError);
            }
        });
    }

    @OnClick(R.id.addSubjects)
    public void addSubjects(View view)
    {
        if (ConnectivityUtil.isConnected(getActivity()))
        {
            AddOrEditSubjectsDialogFragment fragment = AddOrEditSubjectsDialogFragment.getInstance(mCurrentClass.getCode());
            fragment.show(getFragmentManager(), AddOrEditSubjectsDialogFragment.TAG);
        }
        else
        {
            Snack.show(R.string.noInternet);
        }
    }

    @Override
    public boolean onBackPressed()
    {
        if (SubjectsAdapter.isSelectionEnabled)
        {
            SubjectsAdapter.isSelectionEnabled = false;
            mAdapter.notifyDataSetChanged();
            Otto.post(ActionBarUtil.SHOW_INDEPENDENT_SUBJECT_MENU);
            return false;
        }
        return true;
    }

    @Override
    public int getMenuItemId()
    {
        return R.id.launch_subjects_fragment;
    }

    @Override
    public String getTagName()
    {
        return NavigationUtil.SUBJECTS_FRAGMENT;
    }

    @Subscribe
    public void onOptionItemClicked(Integer itemId)
    {
        switch (itemId)
        {
            case R.id.selectAllSubjects:
                mAdapter.setSelectAll();
                break;
            case R.id.deleteSubjects:
                Progress.show(R.string.deleting);
                for (String code : mAdapter.mSelectedSubjects)
                {
                    mSubjectsDbRef.getRef().child(code).removeValue();
                }
                mHandler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Progress.hide();
                        ToastMsg.show(R.string.deleted);
                    }
                }, 300);
                break;
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab)
    {
        if (mTabSelected)
        {
            mCurrentClass = (Classes) tab.getTag();
        }
        Log.d("TabSelectionIssue", "SubjectListFragment > onTabSelected > mCurrentClass : " + mCurrentClass);
        setUpRecyclerView();
    }

    @Override
    public void handleStudent()
    {
        super.handleStudent();
        mFabContainer.setVisibility(View.GONE);
    }

    @Override
    public void handleStaff()
    {
        super.handleStaff();
        mFabContainer.setVisibility(View.GONE);
    }

    private void setLauncher()
    {
        Activity activity = getActivity();
        if (activity instanceof FragmentLauncher)
        {
            launcher = (FragmentLauncher) activity;
        }
    }
    @Override
    public void refreshActionBar()
    {
        if (launcher != null)
        {
            launcher.updateEventsListener(this);
            launcher.setToolBarTitle(R.string.subjects);
            Otto.post(ActionBarUtil.SHOW_INDEPENDENT_SUBJECT_MENU);
        }
    }
}
