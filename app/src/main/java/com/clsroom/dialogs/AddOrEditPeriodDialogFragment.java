package com.clsroom.dialogs;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.TextView;

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

import butterknife.Bind;
import butterknife.ButterKnife;

public class AddOrEditPeriodDialogFragment extends CustomDialogFragment implements AdapterView.OnItemSelectedListener,
        View.OnTouchListener
{
    public static final String TAG = "AddOrEditPeriodDialogFragment";

    @Bind(R.id.addAnother)
    Switch mAddAnother;

    @Bind(R.id.subjectName)
    EditText mSubjectName;

    @Bind(R.id.teacher)
    EditText mTeacherName;

    @Bind(R.id.teacherSpinner)
    Spinner mTeacherSpinner;

    @Bind(R.id.subjectSpinner)
    Spinner mSubjectSpinner;

    @Bind(R.id.startTime)
    EditText mStartTime;

    @Bind(R.id.endTime)
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
        if (TextUtils.isEmpty(mTimeTable.getStartTime()))
        {
            ToastMsg.show(R.string.please_enter_a_valid_start_time);
        }
        else if (TextUtils.isEmpty(mTimeTable.getEndTime()))
        {
            ToastMsg.show(R.string.please_enter_a_valid_end_time);
        }
        else
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
                }, 100);
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
                }, 100);
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
}