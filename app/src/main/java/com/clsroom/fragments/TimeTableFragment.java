package com.clsroom.fragments;

import android.app.Activity;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.otto.Subscribe;
import com.clsroom.MainActivity;
import com.clsroom.R;
import com.clsroom.adapters.TimeTableAdapter;
import com.clsroom.dialogs.AddOrEditPeriodDialogFragment;
import com.clsroom.listeners.EventsListener;
import com.clsroom.model.Classes;
import com.clsroom.model.Progress;
import com.clsroom.model.Subjects;
import com.clsroom.model.TimeTable;
import com.clsroom.model.ToastMsg;
import com.clsroom.utils.ActionBarUtil;
import com.clsroom.utils.NavigationDrawerUtil;
import com.clsroom.utils.Otto;
import com.clsroom.utils.TransitionUtil;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class TimeTableFragment extends ClassTabFragment implements EventsListener
{
    private static final String TAG = "TimeTableFragment";
    private String[] weekDays;
    private String[] weekDaysKey;

    @Bind(R.id.timeTableRecyclerView)
    RecyclerView mTimeTableRecyclerView;

    @Bind(R.id.recyclerProgress)
    View mProgress;

    @Bind(R.id.errorMsg)
    View mErrorMsg;

    @Bind(R.id.fabContainer)
    ViewGroup mFabContainer;

    @Bind(R.id.weekdaysTab)
    TabLayout mWeekDaysTab;


    private DatabaseReference mRootRef;
    private Classes mCurrentClass;
    private TimeTable mCurrentTimeTable;
    private TimeTableAdapter mAdapter;
    private DatabaseReference mTimeTableDbRef;
    private Handler mHandler;
    private String mCurrentWeekDayCode;
    private DatabaseReference mSubjectDbRef;
    private boolean areSubjectsAvailable;

    public TimeTableFragment()
    {
        Log.d(TAG, "TimeTableFragment");
    }

    @Override
    public void onCreateView(View parentView)
    {
        Log.d(TAG, "onCreateView2");
        ButterKnife.bind(this, parentView);
        mHandler = new Handler();
        weekDays = getResources().getStringArray(R.array.weekdays);
        weekDaysKey = getResources().getStringArray(R.array.weekdays_key);
        mCurrentWeekDayCode = weekDaysKey[0];
        mCurrentTimeTable = new TimeTable();
        addWeekDays();
        ((MainActivity) getActivity()).setToolBarTitle(getString(R.string.timeTable));
        mRootRef = FirebaseDatabase.getInstance().getReference();
    }

    private void addWeekDays()
    {
        for (int i = 0; i < weekDays.length; i++)
        {
            TabLayout.Tab tab = mWeekDaysTab.newTab();
            tab.setText(weekDays[i]);
            tab.setTag(weekDaysKey[i]);
            mWeekDaysTab.addTab(tab);
        }
        mWeekDaysTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                mCurrentWeekDayCode = (String) tab.getTag();
                mCurrentTimeTable.setWeekdayCode(mCurrentWeekDayCode);
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
    public void onStart()
    {
        super.onStart();
        Log.d(TAG, "onStart");
        Otto.register(this);
    }

    @Override
    protected int getContentViewLayoutRes()
    {
        Log.d(TAG, "getContentViewLayoutRes");
        return R.layout.fragment_time_table;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.d(TAG, "onResume");
        Activity activity = getActivity();
        if (activity instanceof MainActivity)
        {
            ((MainActivity) activity).updateEventsListener(this);
            Otto.post(ActionBarUtil.SHOW_INDEPENDENT_TIME_TABLE_MENU);
        }
    }


    @Override
    public void onStop()
    {
        super.onStop();
        Log.d(TAG, "onStop");
        Otto.unregister(this);
    }

    private void setUpRecyclerView()
    {
        Log.d(TAG, "setUpRecyclerView");
        mTimeTableDbRef = mRootRef.child(TimeTable.TIME_TABLE).child(mCurrentClass.getCode()).child(mCurrentWeekDayCode);
        mAdapter = TimeTableAdapter.getInstance(mTimeTableDbRef, (AppCompatActivity) getActivity());
        mTimeTableRecyclerView.setAdapter(mAdapter);
        mTimeTableRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mTimeTableRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
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
        mTimeTableDbRef.addValueEventListener(new ValueEventListener()
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
                Log.d(TAG, "databaseError : " + databaseError);
            }
        });
    }

    @OnClick(R.id.addPeriod)
    public void addPeriod(View view)
    {
        if (!areSubjectsAvailable)
        {
            ToastMsg.show(R.string.time_table_cannot_be_set_before_adding_subjects);
        }
        else
        {
            mCurrentTimeTable.setStartTime(null);
            mCurrentTimeTable.setEndTime(null);
            if (mCurrentTimeTable.getWeekdayCode() == null)
            {
                mCurrentTimeTable.setWeekdayCode(weekDaysKey[0]);
            }
            AddOrEditPeriodDialogFragment.getInstance(mCurrentTimeTable)
                    .show(getFragmentManager(), AddOrEditPeriodDialogFragment.TAG);
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
        return R.id.launch_time_table_fragment;
    }

    @Override
    public String getTagName()
    {
        return NavigationDrawerUtil.TIME_TABLE_FRAGMENT;
    }

    @Subscribe
    public void onOptionItemClicked(Integer itemId)
    {
        switch (itemId)
        {
            case R.id.selectAllPeriods:
                mAdapter.setSelectAll();
                break;
            case R.id.deletePeriods:
                Progress.show(R.string.deleting);
                for (String code : mAdapter.mSelectedPeriods)
                {
                    mTimeTableDbRef.getRef().child(code).removeValue();
                }
                mHandler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Progress.hide();
                        ToastMsg.show(R.string.deleted);
                    }
                }, 500);
                break;
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab)
    {
        Log.d(TAG, "onTabSelected");
        mCurrentClass = (Classes) tab.getTag();
        mCurrentTimeTable.setClassCode(mCurrentClass.getCode());
        updateSubjectsAvailability();
        setUpRecyclerView();
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
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
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
}
