package com.clsroom.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.otto.Subscribe;
import com.clsroom.MainActivity;
import com.clsroom.R;
import com.clsroom.adapters.StudentsAdapter;
import com.clsroom.dialogs.AddStudentsDialogFragment;
import com.clsroom.dialogs.ViewStudentAttendanceDialogFragment;
import com.clsroom.listeners.EventsListener;
import com.clsroom.model.Classes;
import com.clsroom.model.Progress;
import com.clsroom.model.Students;
import com.clsroom.model.ToastMsg;
import com.clsroom.utils.ActionBarUtil;
import com.clsroom.utils.NavigationDrawerUtil;
import com.clsroom.utils.Otto;
import com.clsroom.utils.TransitionUtil;

import java.util.Calendar;
import java.util.LinkedHashSet;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class StudentsListFragment extends ClassTabFragment implements EventsListener, DatePickerDialog.OnDateSetListener
{
    private static final String TAG = "StudentsListFragment";

    @Bind(R.id.studentsListRecyclerView)
    RecyclerView mStudentsListRecyclerView;

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
    private Classes mCurrentClass;
    private StudentsAdapter mAdapter;
    private DatePickerDialog mDatePickerDialog;
    private DatabaseReference mStudentsDbRef;
    private DatabaseReference mClassesDbRef;
    private LinkedHashSet<Students> studentsSet;

    public StudentsListFragment()
    {
        Log.d(TAG, "StudentsListFragment");
    }

    @Override
    public void onCreateView(View parentView)
    {
        Log.d(TAG, "onCreateView2");
        ButterKnife.bind(this, parentView);
        mRootRef = FirebaseDatabase.getInstance().getReference();
        Activity activity = getActivity();
        if (activity instanceof MainActivity)
        {
            ((MainActivity) activity).updateEventsListener(this);
            ((MainActivity) getActivity()).setToolBarTitle(getString(R.string.students));
            Otto.post(ActionBarUtil.SHOW_INDEPENDENT_STUDENTS_MENU);
        }
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
        return R.layout.fragment_students_list;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.d(TAG, "onResume");
        mDatePickerDialog = new DatePickerDialog(getActivity(), this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
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
        mStudentsDbRef = mRootRef.child(Students.STUDENTS).child(mCurrentClass.getCode());
        mClassesDbRef = mRootRef.child(Classes.CLASSES).child(mCurrentClass.getCode());
        mAdapter = StudentsAdapter.getInstance(mStudentsDbRef);
        mStudentsListRecyclerView.setAdapter(mAdapter);
        mStudentsListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mStudentsListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if (dy > 50 && mFabContainer.isShown())
                {
                    TransitionUtil.slideTransition(mFabContainer);
                    mFabContainer.setVisibility(View.GONE);
                }
                else if (dy < 0 && !mFabContainer.isShown() && !NavigationDrawerUtil.isStudent)
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
        mStudentsDbRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Log.d(TAG, "Data : " + dataSnapshot);
                mProgress.setVisibility(View.GONE);
                if (dataSnapshot.getChildrenCount() <= 0)
                {
                    mErrorMsg.setVisibility(View.VISIBLE);
                    if (studentsSet == null)
                    {
                        studentsSet = new LinkedHashSet<>();
                    }
                    else
                    {
                        studentsSet.clear();
                    }
                }
                else
                {
                    mErrorMsg.setVisibility(View.GONE);
                    if (studentsSet == null)
                    {
                        studentsSet = new LinkedHashSet<>();
                    }
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        studentsSet.add(snapshot.getValue(Students.class));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG, "databaseError : " + databaseError);
            }
        });
    }

    @OnClick(R.id.attendanceFab)
    public void takeAttendance(View view)
    {
        if (studentsSet.size() > 0)
        {
            mAdapter.enableAttendance(studentsSet);
            mSaveAttendanceFab.setVisibility(View.VISIBLE);
            mTakeAttendanceFab.setVisibility(View.GONE);
        }
        else
        {
            ToastMsg.show(R.string.student_list_is_empty);
        }
    }

    @OnClick(R.id.savefab)
    public void saveAttendance(View view)
    {
        onBackPressed();
        StudentAttendanceListFragment fragment = StudentAttendanceListFragment
                .getInstance(mAdapter.mUnSelectedStudents, mCurrentClass.getCode());
        ((MainActivity) getActivity()).showFragment(fragment, true, StudentAttendanceListFragment.TAG);
    }

    @Override
    public boolean onBackPressed()
    {
        if (StudentsAdapter.isSelectionEnabled)
        {
            StudentsAdapter.isSelectionEnabled = false;
            mAdapter.notifyDataSetChanged();
            Otto.post(ActionBarUtil.SHOW_INDEPENDENT_STUDENTS_MENU);
            mSaveAttendanceFab.setVisibility(View.GONE);
            mTakeAttendanceFab.setVisibility(View.VISIBLE);
            return false;
        }
        return true;
    }

    @Override
    public int getMenuItemId()
    {
        return R.id.admin_students;
    }

    @Override
    public String getTagName()
    {
        return NavigationDrawerUtil.STUDENTS_LIST_FRAGMENT;
    }

    @Subscribe
    public void onOptionItemClicked(Integer itemId)
    {
        switch (itemId)
        {
            case R.id.addNewStudent:
                showDialogFragment();
                break;
            case R.id.selectAll:
                mAdapter.setSelectAll(studentsSet);
                break;
            case R.id.viewStudentAttendance:
                mDatePickerDialog.show();
                break;
            case R.id.deleteStudents:
                Progress.show(R.string.deleting);
                for (Students student : mAdapter.mSelectedStudents)
                {
                    mStudentsDbRef.getRef().child(student.getUserId()).removeValue();
                }
                mStudentsDbRef.addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        mCurrentClass.setStudentCount((int) dataSnapshot.getChildrenCount());
                        mClassesDbRef.setValue(mCurrentClass)
                                .addOnCompleteListener(new OnCompleteListener<Void>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        Progress.hide();
                                        if (task.isSuccessful())
                                        {
                                            onBackPressed();
                                            ToastMsg.show(R.string.deleted);
                                        }
                                        else
                                        {
                                            ToastMsg.show(R.string.couldntUpdateStudentCount);
                                        }
                                    }
                                });
                        mStudentsDbRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {
                        Progress.hide();
                        ToastMsg.show(R.string.please_try_again);
                    }
                });
                break;
        }
    }

    public void showDialogFragment()
    {
        FragmentManager manager = getActivity().getSupportFragmentManager();
        AddStudentsDialogFragment fragment = AddStudentsDialogFragment.getInstance(mCurrentClass);
        fragment.show(manager, AddStudentsDialogFragment.TAG);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day)
    {
        FragmentManager manager = getActivity().getSupportFragmentManager();
        ViewStudentAttendanceDialogFragment fragment = ViewStudentAttendanceDialogFragment.getInstance(mCurrentClass.getCode(), year, month, day);
        fragment.show(manager, ViewStudentAttendanceDialogFragment.TAG);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab)
    {
        Log.d(TAG, "onTabSelected");
        mCurrentClass = (Classes) tab.getTag();
        mProgress.setVisibility(View.VISIBLE);
        setUpRecyclerView();
    }
}
