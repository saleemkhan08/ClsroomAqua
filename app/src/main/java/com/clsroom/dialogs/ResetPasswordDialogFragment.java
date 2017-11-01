package com.clsroom.dialogs;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.clsroom.R;
import com.clsroom.model.Progress;
import com.clsroom.model.ToastMsg;
import com.clsroom.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ResetPasswordDialogFragment extends CustomDialogFragment
{
    public static final String TAG = "ResetPasswordDialogFragment";

    @Bind(R.id.name)
    EditText name;

    @Bind(R.id.textInputLayout)
    TextInputLayout textInputLayout;

    DatabaseReference mUserNameDbRef;

    public static ResetPasswordDialogFragment getInstance(DatabaseReference userDbRef)
    {
        ResetPasswordDialogFragment fragment = new ResetPasswordDialogFragment();
        fragment.mUserNameDbRef = userDbRef.child(User.PASSWORD);
        return fragment;
    }

    public ResetPasswordDialogFragment()
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
        return R.layout.fragment_edit_name;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        setDialogTitle(R.string.reset_password);
        setSubmitBtnTxt(R.string.save);
        setSubmitBtnImg(R.mipmap.save_button);
        textInputLayout.setHint(getString(R.string.newPassword));
    }

    @Override
    public void submit(View view)
    {
        super.submit(view);
        String newPassword = name.getText().toString().trim();
        if (TextUtils.isEmpty(newPassword) || newPassword.length() < 6)
        {
            ToastMsg.show(R.string.please_enter_a_valid_password);
        }
        else
        {
            Progress.show(R.string.saving);
            mUserNameDbRef.setValue(newPassword).addOnCompleteListener(new OnCompleteListener<Void>()
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