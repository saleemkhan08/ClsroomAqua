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

public class ChangePasswordDialogFragment extends CustomDialogFragment
{
    public static final String TAG = "EditNameDialogFragment";

    @Bind(R.id.currentPassword)
    EditText currentPassword;

    @Bind(R.id.newPassword)
    EditText newPassword;

    @Bind(R.id.confirmPassword)
    EditText confirmPassword;

    DatabaseReference mUserPasswordDbRef;
    String currentPasswordTxt;

    public static ChangePasswordDialogFragment getInstance(DatabaseReference userDbRef, String currentPassword)
    {
        ChangePasswordDialogFragment fragment = new ChangePasswordDialogFragment();
        fragment.mUserPasswordDbRef = userDbRef.child(User.PASSWORD);
        fragment.currentPasswordTxt = currentPassword;
        return fragment;
    }

    public ChangePasswordDialogFragment()
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
        return R.layout.fragment_change_password;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        setDialogTitle(R.string.changePassword);
        setSubmitBtnTxt(R.string.save);
        setSubmitBtnImg(R.mipmap.save_button);
    }

    @Override
    public void submit(View view)
    {
        super.submit(view);
        String currentPassword = this.currentPassword.getText().toString();
        String newPassword = this.newPassword.getText().toString();
        String confirmPassword = this.confirmPassword.getText().toString();
        ToastMsg.show(currentPasswordTxt);
        if (!currentPassword.equals(currentPasswordTxt))
        {
            ToastMsg.show(R.string.wrong_current_password);
        }
        else if (TextUtils.isEmpty(newPassword))
        {
            ToastMsg.show(R.string.password_must_contain_at_least_6_char);
        }
        else if (!newPassword.equals(confirmPassword))
        {
            ToastMsg.show(R.string.new_password_did_not_match_with_confirm_password);
        }
        else
        {
            Progress.show(R.string.saving);
            mUserPasswordDbRef.setValue(newPassword).addOnCompleteListener(new OnCompleteListener<Void>()
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