package com.clsroom.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.clsroom.R;
import com.clsroom.adapters.StaffAdapter;
import com.clsroom.dialogs.AddOrEditStaffDialogFragment;
import com.clsroom.dialogs.ViewStaffAttendanceDialogFragment;
import com.clsroom.listeners.EventsListener;
import com.clsroom.listeners.FragmentLauncher;
import com.clsroom.model.Staff;
import com.clsroom.model.ToastMsg;
import com.clsroom.utils.ActionBarUtil;
import com.clsroom.utils.NavigationDrawerUtil;
import com.clsroom.utils.Otto;
import com.clsroom.utils.TransitionUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.otto.Subscribe;

import java.util.Calendar;
import java.util.LinkedHashSet;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class StaffListFragment extends Fragment implements EventsListener, DatePickerDialog.OnDateSetListener
{
    private static final String TAG = "StaffListFragment";

    @Bind(R.id.staffListRecyclerView)
    RecyclerView mStaffListRecyclerView;

    @Bind(R.id.recyclerProgress)
    View mProgress;

    @Bind(R.id.errorMsg)
    View mErrorMsg;

    @Bind(R.id.fabContainer)
    ViewGroup mFabContainer;

    @Bind(R.id.attendanceFab)
    View mTakeAttendanceFab;

    @Bind(R.id.savefab)
    View mSaveAttendanceFab;

    private DatabaseReference mRootRef;
    private StaffAdapter mAdapter;
    private DatePickerDialog mDatePickerDialog;
    private LinkedHashSet<Staff> staffSet;
    private FragmentLauncher launcher;

    public StaffListFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_staff_list, container, false);
        setLauncher();
        Otto.register(this);
        ButterKnife.bind(this, parentView);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        if (launcher != null)
        {
            launcher.setToolBarTitle(R.string.staff);
        }
        setUpRecyclerView();
        mDatePickerDialog = new DatePickerDialog(getActivity(), this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));


        return parentView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        if (launcher != null)
        {
            launcher.updateEventsListener(this);
            Otto.post(ActionBarUtil.SHOW_INDEPENDENT_STAFF_MENU);
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Otto.unregister(this);
    }

    private void setUpRecyclerView()
    {
        DatabaseReference staffDbRef = mRootRef.child(Staff.STAFF);
        mAdapter = StaffAdapter.getInstance(staffDbRef, launcher);
        mStaffListRecyclerView.setAdapter(mAdapter);
        mStaffListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mStaffListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
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
        staffDbRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                mProgress.setVisibility(View.GONE);
                if (dataSnapshot.getChildrenCount() <= 0)
                {
                    mErrorMsg.setVisibility(View.VISIBLE);
                    if (staffSet == null)
                    {
                        staffSet = new LinkedHashSet<>();
                    }
                    else
                    {
                        staffSet.clear();
                    }
                }
                else
                {
                    mErrorMsg.setVisibility(View.GONE);
                    if (staffSet == null)
                    {
                        staffSet = new LinkedHashSet<>();
                    }
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        staffSet.add(snapshot.getValue(Staff.class));
                    }
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

    @OnClick(R.id.attendanceFab)
    public void takeAttendance(View view)
    {
        if (staffSet.size() > 0)
        {
            mAdapter.enableAttendance(staffSet);
            mSaveAttendanceFab.setVisibility(View.VISIBLE);
            mTakeAttendanceFab.setVisibility(View.GONE);
        }
        else
        {
            ToastMsg.show(R.string.staff_list_is_empty);
        }
    }

    @OnClick(R.id.savefab)
    public void saveAttendance(View view)
    {
        onBackPressed();
        StaffAttendanceListFragment fragment = StaffAttendanceListFragment.getInstance(mAdapter.mUnSelectedStaff);
        launcher.showFragment(fragment, true, StaffAttendanceListFragment.TAG);
    }

    @Override
    public boolean onBackPressed()
    {
        if (StaffAdapter.isSelectionEnabled)
        {
            StaffAdapter.isSelectionEnabled = false;
            mAdapter.notifyDataSetChanged();
            Otto.post(ActionBarUtil.SHOW_INDEPENDENT_STAFF_MENU);
            mSaveAttendanceFab.setVisibility(View.GONE);
            mTakeAttendanceFab.setVisibility(View.VISIBLE);
            return false;
        }
        return true;
    }

    @Override
    public int getMenuItemId()
    {
        return R.id.admin_staff;
    }

    @Override
    public String getTagName()
    {
        return NavigationDrawerUtil.STAFF_LIST_FRAGMENT;
    }

    @Subscribe
    public void onOptionItemClicked(Integer itemId)
    {
        switch (itemId)
        {
            case R.id.addNewStaff:
                showDialogFragment();
                break;
            case R.id.viewStaffAttendance:
                mDatePickerDialog.show();
                break;
            case R.id.selectAll:
                mAdapter.setSelectAll(staffSet);
                break;
        }
    }

    public void showDialogFragment()
    {
        FragmentManager manager = getActivity().getSupportFragmentManager();
        AddOrEditStaffDialogFragment fragment = AddOrEditStaffDialogFragment.getInstance(null);
        fragment.show(manager, AddOrEditStaffDialogFragment.TAG);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day)
    {
        FragmentManager manager = getActivity().getSupportFragmentManager();
        ViewStaffAttendanceDialogFragment fragment = ViewStaffAttendanceDialogFragment.getInstance(year, month, day);
        fragment.show(manager, ViewStaffAttendanceDialogFragment.TAG);
    }

    private void setLauncher()
    {
        Activity activity = getActivity();
        if (activity instanceof FragmentLauncher)
        {
            launcher = (FragmentLauncher) activity;
            launcher.setFragment(this);
        }
    }
}
