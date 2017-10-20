package com.clsroom.dialogs;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.clsroom.R;
import com.clsroom.adapters.AttendanceFbAdapter;
import com.clsroom.model.ClassAttendance;
import com.clsroom.model.Staff;
import com.clsroom.model.ToastMsg;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ViewStaffAttendanceDialogFragment extends CustomDialogFragment implements ValueEventListener
{
    public static final String TAG = "ViewStaffAttendance";
    int mYear;
    int mMonth;
    int mDay;
    private DatabaseReference mAttendanceRef;

    @Bind(R.id.recyclerView)
    RecyclerView mAttendanceListRecyclerView;

    @Bind(R.id.errorMsg)
    View mErrorMsg;

    @Bind(R.id.recyclerProgress)
    View mProgress;

    public static ViewStaffAttendanceDialogFragment getInstance(int year, int month, int day)
    {
        ViewStaffAttendanceDialogFragment fragment = new ViewStaffAttendanceDialogFragment();
        fragment.mDay = day;
        fragment.mMonth = month;
        fragment.mYear = year;
        return fragment;
    }

    public ViewStaffAttendanceDialogFragment()
    {

    }

    @Override
    public void onCreateView(View parentView)
    {
        ButterKnife.bind(this, parentView);
        mAttendanceRef = FirebaseDatabase.getInstance().getReference().child(ClassAttendance.ATTENDANCE)
                .child(Staff.STAFF).child("" + mYear + mMonth + mDay).child(ClassAttendance.ABSENTEES);
        mAttendanceRef.addValueEventListener(this);
    }

    @Override
    protected int getContentViewLayoutRes()
    {
        return R.layout.fragment_list;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        setSubTitle(getFormattedDate());
        setDialogTitle(R.string.absentees);
        mAttendanceListRecyclerView.setAdapter(AttendanceFbAdapter.getInstance(mAttendanceRef));
        mAttendanceListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private String getFormattedDate()
    {
        return mDay + "/" + (mMonth + 1) + "/" + mYear;
    }

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
        mProgress.setVisibility(View.GONE);
        ToastMsg.show(R.string.couldntLoadTheList);
    }
}