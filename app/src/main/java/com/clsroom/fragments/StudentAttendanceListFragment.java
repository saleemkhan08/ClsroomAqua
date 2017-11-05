package com.clsroom.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

import com.clsroom.R;
import com.clsroom.adapters.StudentAttendanceAdapter;
import com.clsroom.listeners.EventsListener;
import com.clsroom.listeners.FragmentLauncher;
import com.clsroom.model.ClassAttendance;
import com.clsroom.model.Progress;
import com.clsroom.model.Snack;
import com.clsroom.model.Students;
import com.clsroom.model.ToastMsg;
import com.clsroom.utils.ActionBarUtil;
import com.clsroom.utils.ConnectivityUtil;
import com.clsroom.utils.NavigationUtil;
import com.clsroom.utils.Otto;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.clsroom.LoginActivity.LOGIN_USER_ID;


public class StudentAttendanceListFragment extends Fragment implements EventsListener, DatePickerDialog.OnDateSetListener
{
    public static final String TAG = NavigationUtil.STUDENTS_ATTENDANCE_LIST_FRAGMENT;

    @BindView(R.id.attendanceListRecyclerView)
    RecyclerView mAttendanceListRecyclerView;

    @BindView(R.id.errorMsg)
    View mErrorMsg;

    @BindView(R.id.dateTextView)
    TextView mDateTextView;

    private ArrayList<Students> mStudentsList;
    private DatePickerDialog mDatePickerDialog;
    private DatabaseReference mAttendanceRef;
    private String mClassCode;
    private String mAttendanceDate;
    private String mTakenDate;
    private FragmentLauncher launcher;

    public StudentAttendanceListFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_attendance_list, container, false);
        ButterKnife.bind(this, parentView);
        Otto.register(this);
        setLauncher();

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        String currentDate = getFormattedDate(currentYear, currentMonth, currentDay);
        mTakenDate = currentDate + " " + calendar.get(Calendar.HOUR_OF_DAY) + ":" +
                calendar.get(Calendar.MINUTE);
        mDatePickerDialog = new DatePickerDialog(getActivity(), this, currentYear, currentMonth, currentDay);
        mAttendanceDate = "" + currentYear + currentMonth + currentDay;

        mDateTextView.setText(currentDate);
        mAttendanceRef = FirebaseDatabase.getInstance().getReference()
                .child(ClassAttendance.ATTENDANCE)
                .child(mClassCode);
        setUpRecyclerView();
        refreshActionBar();
        return parentView;
    }

    private String getFormattedDate(int currentYear, int currentMonth, int currentDay)
    {

        return currentDay + "/" + (currentMonth + 1) + "/" + currentYear;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Otto.unregister(this);
    }

    public static StudentAttendanceListFragment getInstance(LinkedHashSet<Students> studentsSet, String classCode)
    {
        StudentAttendanceListFragment fragment = new StudentAttendanceListFragment();
        Log.d(TAG, "getInstance : " + studentsSet);
        fragment.mStudentsList = new ArrayList<>();
        fragment.mStudentsList.addAll(studentsSet);
        fragment.mClassCode = classCode;
        return fragment;
    }

    private void setUpRecyclerView()
    {
        Log.d(TAG, "setUpRecyclerView : " + mStudentsList);
        StudentAttendanceAdapter adapter = new StudentAttendanceAdapter(mStudentsList);
        mAttendanceListRecyclerView.setAdapter(adapter);
        mAttendanceListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (mStudentsList.size() <= 0)
        {
            mErrorMsg.setVisibility(View.VISIBLE);
        }
        else
        {
            mErrorMsg.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.editDate)
    public void editDate(View view)
    {
        if (ConnectivityUtil.isConnected(getActivity()))
        {
            mDatePickerDialog.show();
        }
        else
        {
            Snack.show(R.string.noInternet);
        }

    }

    @OnClick(R.id.saveAttendance)
    public void saveAttendance(View view)
    {
        if (ConnectivityUtil.isConnected(getActivity()))
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            ClassAttendance attendance = new ClassAttendance();
            attendance.setListOfAbsentees(mStudentsList);
            attendance.setStaffId(preferences.getString(LOGIN_USER_ID, ""));
            attendance.setTakenDate(mTakenDate);
            Progress.show(R.string.saving);
            mAttendanceRef.child(mAttendanceDate).setValue(attendance).addOnCompleteListener(new OnCompleteListener<Void>()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    Progress.hide();
                    if (task.isSuccessful())
                    {
                        ToastMsg.show(R.string.saved);
                        getActivity().onBackPressed();
                    }
                }
            });
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
        return R.id.admin_students;
    }

    @Override
    public String getTagName()
    {
        return NavigationUtil.ATTENDANCE_FRAGMENT;
    }

    @Subscribe
    public void onOptionItemClicked(Integer itemId)
    {
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day)
    {
        mDateTextView.setText(getFormattedDate(year, month, day));
        mAttendanceDate = "" + year + month + day;
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
            launcher.setToolBarTitle(R.string.attendance);
            Otto.post(ActionBarUtil.NO_MENU);
        }
    }
}
