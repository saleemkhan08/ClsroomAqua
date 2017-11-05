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
import android.widget.TextView;

import com.clsroom.R;
import com.clsroom.adapters.StaffFirebaseListAdapter;
import com.clsroom.model.Classes;
import com.clsroom.model.Progress;
import com.clsroom.model.Staff;
import com.clsroom.model.ToastMsg;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddOrEditClassDialogFragment extends CustomDialogFragment implements AdapterView.OnItemSelectedListener, View.OnTouchListener
{
    public static final String TAG = "AddOrEditClassDialogFragment";

    @BindView(R.id.classCode)
    EditText mClassCode;

    @BindView(R.id.className)
    EditText mClassName;

    @BindView(R.id.classTeacherSpinner)
    Spinner mClassTeacherSpinner;

    @BindView(R.id.classTeacher)
    EditText mClassTeacher;

    Classes mCurrentClass;

    public static AddOrEditClassDialogFragment getInstance(Classes classes)
    {
        AddOrEditClassDialogFragment fragment = new AddOrEditClassDialogFragment();
        fragment.mCurrentClass = classes;
        return fragment;
    }

    public AddOrEditClassDialogFragment()
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
        return R.layout.fragment_add_class;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child(Staff.STAFF);
        StaffFirebaseListAdapter adapter = new StaffFirebaseListAdapter(getActivity(),
                Staff.class, android.R.layout.simple_list_item_1, dbRef);
        mClassTeacherSpinner.setAdapter(adapter);
        mClassTeacherSpinner.setOnItemSelectedListener(this);
        mClassTeacher.setOnTouchListener(this);
        setDialogTitle(R.string.addClass);
        if (mCurrentClass != null)
        {
            mClassTeacher.setText(mCurrentClass.getClassTeacherName());
            mClassTeacher.setTag(mCurrentClass.getClassTeacherId());
            mClassCode.setText(mCurrentClass.getCode());
            mClassCode.setEnabled(false);
            mClassName.setText(mCurrentClass.getName());
            setDialogTitle(R.string.editClass);
        }
        setSubmitBtnTxt(R.string.save);
        setSubmitBtnImg(R.mipmap.save_button);
    }

    @Override
    public void submit(View view)
    {
        super.submit(view);
        String className = mClassName.getText().toString();
        String classCode = mClassCode.getText().toString();
        String classTeacherId = ((Staff) mClassTeacher.getTag()).getUserId();
        if (TextUtils.isEmpty(className))
        {
            ToastMsg.show(R.string.please_enter_class_name);
        }
        else if (TextUtils.isEmpty(classCode))
        {
            ToastMsg.show(R.string.please_enter_class_code);
        }
        else
        {
            if (mCurrentClass == null)
            {
                mCurrentClass = new Classes();
            }
            mCurrentClass.setCode(classCode);
            mCurrentClass.setName(className);
            mCurrentClass.setClassTeacherId(classTeacherId);
            mCurrentClass.setClassTeacherName(mClassTeacher.getText().toString());
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Classes.CLASSES);
            Progress.show(R.string.saving);
            Log.d("SavingP", "" + this);
            reference.child(classCode).setValue(mCurrentClass).addOnCompleteListener(new OnCompleteListener<Void>()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    Progress.hide();
                    if (task.isSuccessful())
                    {
                        ToastMsg.show(R.string.saved);
                        dismiss();
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
        mClassTeacher.setText(((TextView) view).getText().toString());
        mClassTeacher.setTag(view.getTag());
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
            mClassTeacher.requestFocus();
            mClassTeacher.setCursorVisible(false);
            closeTheKeyBoard();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    mClassTeacherSpinner.performClick();
                }
            }, 300);
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