package com.clsroom.dialogs;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.clsroom.R;
import com.clsroom.model.Classes;
import com.clsroom.model.Progress;
import com.clsroom.model.Students;
import com.clsroom.model.ToastMsg;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AddStudentsDialogFragment extends CustomDialogFragment
{
    public static final String TAG = "AddStudentsDF";

    @Bind(R.id.studentCount)
    EditText mStudentCount;

    DatabaseReference mClassesDbRef;
    private DatabaseReference mRootRef;
    Classes mCurrentClass;
    private DatabaseReference mStudentDbRef;

    public static AddStudentsDialogFragment getInstance(Classes currentClass)
    {
        AddStudentsDialogFragment fragment = new AddStudentsDialogFragment();
        fragment.mCurrentClass = currentClass;
        return fragment;
    }

    public AddStudentsDialogFragment()
    {

    }

    @Override
    public void onCreateView(View parentView)
    {
        ButterKnife.bind(this, parentView);
    }

    @Override
    protected int getContentViewLayoutRes()
    {
        return R.layout.fragment_add_student;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mClassesDbRef = mRootRef.child(Classes.CLASSES);
        setDialogTitle(R.string.addStudents);
        setSubmitBtnTxt(R.string.add);
        setSubmitBtnImg(R.mipmap.plus);
    }

    @Override
    public void submit(View view)
    {
        super.submit(view);
        String studentCountText = mStudentCount.getText().toString();
        if (TextUtils.isEmpty(studentCountText))
        {
            ToastMsg.show(R.string.please_enter_no_of_students);
        }
        else
        {
            final int studentCount = Integer.parseInt(studentCountText);
            mStudentDbRef = mRootRef.child(Students.STUDENTS).child(mCurrentClass.getCode());
            Progress.show(R.string.saving);
            Log.d("SavingP", "" + this);
            mStudentDbRef.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    Progress.hide();
                    ArrayList<String> keyList = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        keyList.add(snapshot.getKey());
                    }
                    addStudent(keyList, studentCount);
                    mStudentDbRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {
                    Progress.hide();
                }
            });
        }
    }

    private void addStudent(ArrayList<String> codes, int studentCount)
    {
        Progress.show(R.string.adding);
        for (int i = 1, j = 1; i <= studentCount; i++, j++)
        {
            String studentCode = mCurrentClass.getCode() + getStudentCountStr(j);
            while (codes.contains(studentCode))
            {
                j++;
                studentCode = mCurrentClass.getCode() + getStudentCountStr(j);
            }

            Students student = new Students();
            student.setUserId(studentCode);
            student.setClassName(mCurrentClass.getName());
            mStudentDbRef.child(studentCode).setValue(student);
        }

        mStudentDbRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Progress.hide();
                int childrenCount = (int) dataSnapshot.getChildrenCount();
                mCurrentClass.setStudentCount(childrenCount);
                mClassesDbRef.child(mCurrentClass.getCode()).setValue(mCurrentClass);
                mStudentDbRef.removeEventListener(this);
                ToastMsg.show(R.string.added);
                dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Progress.hide();
                ToastMsg.show(R.string.couldntAddStudents);
            }
        });
    }

    private String getStudentCountStr(int currentStudentCount)
    {
        if (currentStudentCount < 10)
        {
            return "00" + currentStudentCount;
        }
        else if (currentStudentCount < 100)
        {
            return "0" + currentStudentCount;
        }
        else
        {
            return "" + currentStudentCount;
        }
    }
}