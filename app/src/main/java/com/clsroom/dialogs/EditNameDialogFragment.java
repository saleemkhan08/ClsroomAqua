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

public class EditNameDialogFragment extends CustomDialogFragment
{
    public static final String TAG = "EditNameDialogFragment";

    @Bind(R.id.name)
    EditText name;

    DatabaseReference mUserNameDbRef;
    String currentName;

    public static EditNameDialogFragment getInstance(DatabaseReference userDbRef, String name)
    {
        EditNameDialogFragment fragment = new EditNameDialogFragment();
        fragment.mUserNameDbRef = userDbRef.child(User.FULL_NAME);
        fragment.currentName = name;
        return fragment;
    }

    public EditNameDialogFragment()
    {

    }

    @Override
    public void onCreateView(View parentView)
    {
        ButterKnife.bind(this, parentView);
        name.setText(currentName);
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
        setDialogTitle(R.string.editName);
        setSubmitBtnTxt(R.string.save);
        setSubmitBtnImg(R.mipmap.save_button);
    }

    @Override
    public void submit(View view)
    {
        super.submit(view);
        String fullName = name.getText().toString();
        if (TextUtils.isEmpty(fullName))
        {
            ToastMsg.show(R.string.please_enter_a_valid_name);
        }
        else
        {
            Progress.show(R.string.saving);
            mUserNameDbRef.setValue(fullName).addOnCompleteListener(new OnCompleteListener<Void>()
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