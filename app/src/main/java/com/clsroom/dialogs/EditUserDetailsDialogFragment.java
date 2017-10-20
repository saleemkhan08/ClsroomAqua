package com.clsroom.dialogs;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.clsroom.R;
import com.clsroom.model.Progress;
import com.clsroom.model.ToastMsg;
import com.clsroom.model.User;

import butterknife.Bind;
import butterknife.ButterKnife;

public class EditUserDetailsDialogFragment extends CustomDialogFragment
{
    public static final String TAG = "EditUserDetailsDialogFragment";

    @Bind(R.id.dob)
    EditText dob;

    @Bind(R.id.phoneNo)
    EditText phoneNo;

    @Bind(R.id.email)
    EditText email;

    @Bind(R.id.address)
    EditText address;

    DatabaseReference mUserDbRef;
    User currentUser;

    public static EditUserDetailsDialogFragment getInstance(DatabaseReference userDbRef, User user)
    {
        EditUserDetailsDialogFragment fragment = new EditUserDetailsDialogFragment();
        fragment.mUserDbRef = userDbRef;
        fragment.currentUser = user;
        return fragment;
    }

    public EditUserDetailsDialogFragment()
    {

    }

    @Override
    public void onCreateView(View parentView)
    {
        ButterKnife.bind(this, parentView);
        dob.setText(currentUser.getDob());
        email.setText(currentUser.getEmail());
        phoneNo.setText(currentUser.getPhone());
        address.setText(currentUser.getAddress());
    }

    @Override
    protected int getContentViewLayoutRes()
    {
        return R.layout.fragment_edit_user_details;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        setDialogTitle(R.string.editName);
        setSubmitBtnTxt(R.string.save);
        setSubmitBtnImg(R.mipmap.save_button);
    }

    @Override
    public void submit(View view)
    {
        super.submit(view);
        currentUser.setDob(dob.getText().toString());
        currentUser.setEmail(email.getText().toString());
        currentUser.setPhone(phoneNo.getText().toString());
        currentUser.setAddress(address.getText().toString());

        if (TextUtils.isEmpty(currentUser.getDob()))
        {
            ToastMsg.show(R.string.please_enter_a_valid_date_of_birth);
        }
        else if (TextUtils.isEmpty(currentUser.getEmail()))
        {
            ToastMsg.show(R.string.please_enter_a_valid_email);
        }
        else if (TextUtils.isEmpty(currentUser.getPhone()))
        {
            ToastMsg.show(R.string.please_enter_a_valid_phone_no);
        }
        else if (TextUtils.isEmpty(currentUser.getAddress()))
        {
            ToastMsg.show(R.string.please_enter_a_valid_address);
        }
        else
        {
            Progress.show(R.string.saving);
            mUserDbRef.setValue(currentUser).addOnCompleteListener(new OnCompleteListener<Void>()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    dismiss();
                    Progress.hide();
                    if (task.isSuccessful())
                    {
                        ToastMsg.show(R.string.saved);
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