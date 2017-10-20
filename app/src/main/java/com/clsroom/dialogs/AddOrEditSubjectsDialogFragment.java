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
import android.widget.Switch;
import android.widget.TextView;

import com.clsroom.R;
import com.clsroom.adapters.StaffFirebaseListAdapter;
import com.clsroom.model.Progress;
import com.clsroom.model.Staff;
import com.clsroom.model.Subjects;
import com.clsroom.model.ToastMsg;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AddOrEditSubjectsDialogFragment extends CustomDialogFragment implements AdapterView.OnItemSelectedListener, View.OnTouchListener
{
    public static final String TAG = "AddSubjectsDialogFragment";

    @Bind(R.id.addAnother)
    Switch mAddAnother;

    @Bind(R.id.subjectName)
    EditText mSubjectName;

    @Bind(R.id.subjectCode)
    EditText mSubjectCode;

    @Bind(R.id.teacher)
    EditText mTeacherName;

    @Bind(R.id.teacherSpinner)
    Spinner mTeacherSpinner;

    Subjects mCurrentSubject;
    String mCurrentClassCode;

    public static AddOrEditSubjectsDialogFragment getInstance(String code)
    {
        AddOrEditSubjectsDialogFragment fragment = new AddOrEditSubjectsDialogFragment();
        fragment.mCurrentClassCode = code;
        return fragment;

    }

    public static AddOrEditSubjectsDialogFragment getInstance(Subjects subject)
    {
        AddOrEditSubjectsDialogFragment fragment = new AddOrEditSubjectsDialogFragment();
        fragment.mCurrentSubject = subject;
        fragment.mCurrentClassCode = subject.getClassCode();
        return fragment;
    }

    @Override
    public void onCreateView(View parentView)
    {
        ButterKnife.bind(this, parentView);
        setSubmitBtnImg(R.mipmap.save_button);
        setDialogTitle(R.string.addSubject);
        setSubmitBtnTxt(R.string.add);
    }

    @Override
    protected int getContentViewLayoutRes()
    {
        return R.layout.fragment_add_subjects;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child(Staff.STAFF);
        StaffFirebaseListAdapter adapter = new StaffFirebaseListAdapter(getActivity(),
                Staff.class, android.R.layout.simple_list_item_1, dbRef);
        mTeacherSpinner.setAdapter(adapter);
        mTeacherSpinner.setOnItemSelectedListener(this);
        mTeacherName.setOnTouchListener(this);
        if (mCurrentSubject != null)
        {
            Staff staff = new Staff();
            staff.setFullName(mCurrentSubject.getTeacherName());
            staff.setPhotoUrl(mCurrentSubject.getTeacherImgUrl());
            staff.setUserId(mCurrentSubject.getTeacherCode());

            mTeacherName.setText(staff.getFullName());
            mTeacherName.setTag(staff);

            mSubjectCode.setText(mCurrentSubject.getSubjectCode());
            mSubjectCode.setEnabled(false);

            mSubjectName.setText(mCurrentSubject.getSubjectName());
            mAddAnother.setVisibility(View.GONE);
            setDialogTitle(R.string.editSubject);
        }
        else
        {
            mCurrentSubject = new Subjects();
        }
    }

    @Override
    public void submit(View view)
    {
        super.submit(view);
        mCurrentSubject.setSubjectName(mSubjectName.getText().toString());
        mCurrentSubject.setSubjectCode(mSubjectCode.getText().toString());
        mCurrentSubject.setClassCode(mCurrentClassCode);

        Staff staff = (Staff) mTeacherName.getTag();
        mCurrentSubject.setTeacherName(staff.getFullName());
        mCurrentSubject.setTeacherCode(staff.getUserId());
        mCurrentSubject.setTeacherImgUrl(staff.getPhotoUrl());


        if (TextUtils.isEmpty(mCurrentSubject.getSubjectName()))
        {
            ToastMsg.show(R.string.please_enter_a_valid_subject_name);
        }
        else
        {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                    .child(Subjects.SUBJECTS).child(mCurrentClassCode);
            Progress.show(R.string.saving);
            Log.d("SavingP", "" + this);
            reference.child(mCurrentSubject.getSubjectCode()).setValue(mCurrentSubject)
                    .addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            Progress.hide();
                            if (task.isSuccessful())
                            {
                                ToastMsg.show(R.string.saved);
                                if (mAddAnother.isChecked())
                                {
                                    mSubjectName.setText("");
                                    mSubjectCode.setText("");
                                }
                                else
                                {
                                    dismiss();
                                }
                            }
                            else
                            {
                                ToastMsg.show(R.string.please_try_again);
                            }
                        }
                    });
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
    {
        mTeacherName.setText(((TextView) view).getText().toString());
        mTeacherName.setTag(view.getTag());
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView)
    {
        ToastMsg.show(R.string.pleaseSelectClassTeacher);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent)
    {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
        {
            mTeacherName.requestFocus();
            mTeacherName.setCursorVisible(false);
            closeTheKeyBoard();
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
        return true;
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