package com.clsroom.dialogs;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.clsroom.R;
import com.clsroom.adapters.StaffFirebaseListAdapter;
import com.clsroom.adapters.SubjectFirebaseListAdapter;
import com.clsroom.model.Staff;
import com.clsroom.model.Subjects;
import com.clsroom.model.TimeTable;
import com.clsroom.model.ToastMsg;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTouch;

import static com.clsroom.utils.DateTimeUtil.get2DigitNum;

public class AddOrEditPeriodDialogFragment extends CustomDialogFragment implements AdapterView.OnItemSelectedListener,
        View.OnTouchListener, CompoundButton.OnCheckedChangeListener
{
    public static final String TAG = "AddOrEditPeriodDialogFragment";

    @BindView(R.id.addAnother)
    Switch mAddAnother;

    @BindView(R.id.breakTime)
    Switch breakTime;

    @BindView(R.id.subjectName)
    EditText mSubjectName;

    @BindView(R.id.teacher)
    EditText mTeacherName;

    @BindView(R.id.subjectContainer)
    View subjectContainer;

    @BindView(R.id.teacherContainer)
    View teacherContainer;

    @BindView(R.id.teacherSpinner)
    Spinner mTeacherSpinner;

    @BindView(R.id.subjectSpinner)
    Spinner mSubjectSpinner;

    @BindView(R.id.startTime)
    EditText mStartTime;

    @BindView(R.id.endTime)
    EditText mEndTime;

    TimeTable mTimeTable;
    private SpinnerAdapter mStaffAdapter;
    private DatabaseReference mSubjectDbRef;
    private DatabaseReference mStaffDbRef;
    private DatabaseReference mRootRef;

    public static AddOrEditPeriodDialogFragment getInstance(TimeTable timeTable)
    {
        AddOrEditPeriodDialogFragment fragment = new AddOrEditPeriodDialogFragment();
        fragment.mTimeTable = timeTable;
        fragment.mRootRef = FirebaseDatabase.getInstance().getReference();
        fragment.mSubjectDbRef = fragment.mRootRef.child(Subjects.SUBJECTS).child(timeTable.getClassCode());
        fragment.mStaffDbRef = fragment.mRootRef.child(Staff.STAFF);
        return fragment;
    }

    @Override
    public void onCreateView(View parentView)
    {
        ButterKnife.bind(this, parentView);
        setSubmitBtnImg(R.mipmap.plus);
        setDialogTitle(R.string.addPeriod);
        setSubmitBtnTxt(R.string.add);
        breakTime.setOnCheckedChangeListener(this);
    }

    @Override
    protected int getContentViewLayoutRes()
    {
        return R.layout.fragment_add_time_table;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        mStaffAdapter = new StaffFirebaseListAdapter(getActivity(),
                Staff.class, android.R.layout.simple_list_item_1, mStaffDbRef);
        mTeacherName.setOnTouchListener(this);

        SubjectFirebaseListAdapter subjectAdapter = new SubjectFirebaseListAdapter(getActivity(),
                Subjects.class, android.R.layout.simple_list_item_1, mSubjectDbRef);

        mSubjectSpinner.setAdapter(subjectAdapter);
        mSubjectSpinner.setOnItemSelectedListener(this);
        mSubjectName.setOnTouchListener(this);

        if (mTimeTable.getStartTime() != null)
        {
            mStartTime.setText(mTimeTable.getStartTime());
            mEndTime.setText(mTimeTable.getEndTime());

            mStartTime.setEnabled(false);
            mEndTime.setEnabled(false);

            Staff staff = new Staff();
            staff.setFullName(mTimeTable.getTeacherName());
            staff.setPhotoUrl(mTimeTable.getTeacherPhotoUrl());
            staff.setUserId(mTimeTable.getTeacherCode());

            mTeacherName.setText(staff.getFullName());
            mTeacherName.setTag(staff);

            Subjects subject = new Subjects();
            subject.setSubjectCode(mTimeTable.getSubjectCode());
            subject.setSubjectName(mTimeTable.getSubjectName());

            subject.setClassCode(mTimeTable.getClassCode());

            subject.setTeacherName(mTimeTable.getTeacherName());
            subject.setTeacherImgUrl(mTimeTable.getTeacherPhotoUrl());
            subject.setTeacherCode(mTimeTable.getTeacherCode());

            mSubjectName.setText(subject.getSubjectName());
            mSubjectName.setTag(subject);

            mAddAnother.setVisibility(View.GONE);
            setDialogTitle(R.string.editPeriod);
            setSubmitBtnTxt(R.string.save);
            setSubmitBtnImg(R.mipmap.save_button);
        }
    }

    @Override
    public void submit(View view)
    {
        super.submit(view);
        mTimeTable.setStartTime(mStartTime.getText().toString());
        mTimeTable.setEndTime(mEndTime.getText().toString());
        if (mTimeTable.isBreak())
        {
            mTimeTable.setSubjectCode("");
            mTimeTable.setSubjectName("");
            mTimeTable.setTeacherCode("");
            mTimeTable.setTeacherName("");
            mTimeTable.setTeacherPhotoUrl("");
        }
        if (validateTimeTable())
        {
            Log.d("NullTest", mTimeTable.getClassCode() + ", " + mTimeTable.getStartTimeKey() + ", " + mTimeTable.getWeekdayCode());
            mRootRef.child(TimeTable.TIME_TABLE)
                    .child(mTimeTable.getClassCode()).child(mTimeTable.getWeekdayCode())
                    .child(mTimeTable.getStartTimeKey()).setValue(mTimeTable).addOnCompleteListener(new OnCompleteListener<Void>()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    ToastMsg.show(R.string.saved);
                    if (mAddAnother.isChecked())
                    {
                        mStartTime.setText("");
                        mEndTime.setText("");
                    }
                    else
                    {
                        dismiss();
                    }
                }
            });
        }
    }

    private boolean validateTimeTable()
    {
        if (TextUtils.isEmpty(mTimeTable.getStartTime()))
        {
            ToastMsg.show(R.string.please_enter_a_valid_start_time);
            return false;
        }
        else if (TextUtils.isEmpty(mTimeTable.getEndTime()))
        {
            ToastMsg.show(R.string.please_enter_a_valid_end_time);
            return false;
        }
        else if (isStartTimeGreaterThanEndTime())
        {
            ToastMsg.show(R.string.start_time_is_greater_than_end_time);
            return false;
        }
        else if (isDifferenceTooHigh())
        {
            ToastMsg.show(R.string.difference_between_start_and_end_time_is_high);
            return false;
        }
        return true;
    }

    private boolean isDifferenceTooHigh()
    {
        StartEndTime startTime = (StartEndTime) mStartTime.getTag();
        StartEndTime endTime = (StartEndTime) mEndTime.getTag();
        if (startTime != null && endTime != null)
        {
            return (endTime.hour - startTime.hour) >= 8;
        }
        else
        {
            return false;
        }
    }

    private boolean isStartTimeGreaterThanEndTime()
    {
        StartEndTime startTime = (StartEndTime) mStartTime.getTag();
        StartEndTime endTime = (StartEndTime) mEndTime.getTag();

        if (startTime != null && endTime != null)
        {
            if (startTime.hour > endTime.hour)
            {
                return true;
            }
            else if (startTime.hour == endTime.hour)
            {
                if (startTime.min > endTime.min || startTime.min == endTime.min)
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
    {
        Subjects subjects = (Subjects) view.getTag();
        mTimeTable.setSubjectName(subjects.getSubjectName());
        mTimeTable.setSubjectCode(subjects.getSubjectCode());

        mSubjectName.setText(mTimeTable.getSubjectName());
        mSubjectName.setTag(subjects);

        Staff staff = new Staff();
        staff.setUserId(subjects.getTeacherCode());
        mTimeTable.setTeacherCode(subjects.getTeacherCode());
        staff.setPhotoUrl(subjects.getTeacherImgUrl());
        mTimeTable.setTeacherPhotoUrl(subjects.getTeacherImgUrl());
        staff.setFullName(subjects.getTeacherName());
        mTimeTable.setTeacherName(subjects.getTeacherName());

        mTeacherName.setText(staff.getFullName());
        mTeacherName.setTag(staff);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView)
    {
        ToastMsg.show(R.string.pleaseSelectClassTeacher);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent)
    {
        closeTheKeyBoard();
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
        {
            if (view.getId() == R.id.subjectName)
            {
                mSubjectName.requestFocus();
                mSubjectName.setCursorVisible(false);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mSubjectSpinner.performClick();
                    }
                }, 300);
            }
            else
            {
                mTeacherSpinner.setAdapter(mStaffAdapter);
                mTeacherSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
                    {
                        mTeacherName.setText(((TextView) view).getText().toString());
                        Staff staff = (Staff) view.getTag();
                        mTeacherName.setTag(staff.getFullName());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView)
                    {

                    }
                });

                mTeacherSpinner.setSelection(findPosition());
                mTeacherName.requestFocus();
                mTeacherName.setCursorVisible(false);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mTeacherSpinner.performClick();
                    }
                }, 300);
            }
        }
        return true;
    }

    private int findPosition()
    {
        for (int i = 0; i < mStaffAdapter.getCount(); i++)
        {
            if (((Staff) mStaffAdapter.getItem(i)).getUserId().equals(mTimeTable.getTeacherCode()))
            {
                return i;
            }
        }
        return 0;
    }

    private void closeTheKeyBoard()
    {
        View view = getView();
        if (view != null)
        {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @OnTouch(R.id.startTime)
    public boolean selectStartTime(MotionEvent motionEvent)
    {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
        {
            closeTheKeyBoard();
            Calendar currentTime = Calendar.getInstance();
            int hour = currentTime.get(Calendar.HOUR_OF_DAY);
            int minute = currentTime.get(Calendar.MINUTE);
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener()
            {
                @Override
                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute)
                {
                    mStartTime.setText(getTime(selectedHour, selectedMinute));
                    StartEndTime time = new StartEndTime();
                    time.min = selectedMinute;
                    time.hour = selectedHour;
                    mStartTime.setTag(time);
                }
            }, hour, minute, false);
            mTimePicker.setTitle("Select Start Time");
            mTimePicker.show();
        }
        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked)
    {
        mTimeTable.setBreak(isChecked);
        if (isChecked)
        {
            teacherContainer.setVisibility(View.GONE);
            subjectContainer.setVisibility(View.GONE);

        }
        else
        {
            teacherContainer.setVisibility(View.VISIBLE);
            subjectContainer.setVisibility(View.VISIBLE);
        }

    }

    private class StartEndTime
    {
        int hour;
        int min;
    }

    @OnTouch(R.id.endTime)
    public boolean selectEndTime(MotionEvent motionEvent)
    {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
        {
            closeTheKeyBoard();
            Calendar currentTime = Calendar.getInstance();
            int hour = currentTime.get(Calendar.HOUR_OF_DAY);
            int minute = currentTime.get(Calendar.MINUTE);
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener()
            {
                @Override
                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute)
                {
                    mEndTime.setText(getTime(selectedHour, selectedMinute));
                    StartEndTime time = new StartEndTime();
                    time.min = selectedMinute;
                    time.hour = selectedHour;
                    mEndTime.setTag(time);
                }
            }, hour, minute, false);
            mTimePicker.setTitle("Select Start Time");
            mTimePicker.show();
        }
        return true;
    }

    private String getTime(int selectedHour, int selectedMinute)
    {
        if (selectedHour > 12)
        {
            return get2DigitNum(selectedHour - 12) + ":" + get2DigitNum(selectedMinute) + " PM";
        }
        else
        {
            return get2DigitNum(selectedHour) + ":" + get2DigitNum(selectedMinute) + " AM";
        }
    }


}