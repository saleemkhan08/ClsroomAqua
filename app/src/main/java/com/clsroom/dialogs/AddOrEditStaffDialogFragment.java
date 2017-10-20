package com.clsroom.dialogs;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;

import com.clsroom.R;
import com.clsroom.model.Progress;
import com.clsroom.model.Staff;
import com.clsroom.model.ToastMsg;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AddOrEditStaffDialogFragment extends CustomDialogFragment
{
    public static final String TAG = "AddOrEditStaffDialogFragment";

    @Bind(R.id.staffName)
    EditText mStaffName;

    @Bind(R.id.staffCode)
    EditText mStaffUserId;

    @Bind(R.id.designation)
    EditText mDesignation;

    @Bind(R.id.qualification)
    EditText mQualification;

    @Bind(R.id.isAdmin)
    Switch mIsAdmin;

    Staff mCurrentStaff;

    public static AddOrEditStaffDialogFragment getInstance(Staff classes)
    {
        AddOrEditStaffDialogFragment fragment = new AddOrEditStaffDialogFragment();
        fragment.mCurrentStaff = classes;
        return fragment;
    }

    public AddOrEditStaffDialogFragment()
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
        return R.layout.fragment_add_staffs;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        setDialogTitle(R.string.addNewStaff);
        if (mCurrentStaff != null)
        {
            mDesignation.setText(mCurrentStaff.getDesignation());
            mIsAdmin.setChecked(mCurrentStaff.getIsAdmin());

            mStaffUserId.setText(mCurrentStaff.getUserId());
            mStaffUserId.setEnabled(false);

            mStaffName.setText(mCurrentStaff.getFullName());
            mQualification.setText(mCurrentStaff.getQualification());
            setDialogTitle(R.string.editStaff);
        }
        setSubmitBtnTxt(R.string.save);
        setSubmitBtnImg(R.mipmap.save_button);
    }

    @Override
    public void submit(View view)
    {
        super.submit(view);
        String staffName = mStaffName.getText().toString();
        String staffUserId = mStaffUserId.getText().toString();
        String qualification = mQualification.getText().toString();
        String designation = mDesignation.getText().toString();
        boolean isAdmin = mIsAdmin.isChecked();

        if (TextUtils.isEmpty(staffName))
        {
            ToastMsg.show(R.string.please_enter_staff_name);
        }
        else if (TextUtils.isEmpty(staffUserId))
        {
            ToastMsg.show(R.string.please_enter_staff_id);
        }
        else if (TextUtils.isEmpty(qualification))
        {
            ToastMsg.show(R.string.please_enter_staff_qualification);
        }
        else if (TextUtils.isEmpty(designation))
        {
            ToastMsg.show(R.string.please_enter_staff_designation);
        }
        else
        {
            if (mCurrentStaff == null)
            {
                mCurrentStaff = new Staff();
            }
            mCurrentStaff.setAdmin(isAdmin);
            mCurrentStaff.setDesignation(designation);
            mCurrentStaff.setFullName(staffName);
            mCurrentStaff.setUserId(staffUserId);
            mCurrentStaff.setQualification(qualification);

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Staff.STAFF);
            Progress.show(R.string.saving);
            Log.d("SavingP", "" + this);
            reference.child(staffUserId).setValue(mCurrentStaff).addOnCompleteListener(new OnCompleteListener<Void>()
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
}