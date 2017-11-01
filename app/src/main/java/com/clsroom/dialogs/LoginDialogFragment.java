package com.clsroom.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.clsroom.R;
import com.clsroom.listeners.OnDismissListener;
import com.clsroom.listeners.ResultListener;
import com.clsroom.model.Progress;
import com.clsroom.model.Snack;
import com.clsroom.model.ToastMsg;
import com.clsroom.utils.ConnectivityUtil;
import com.clsroom.utils.Otto;
import com.clsroom.utils.TransitionUtil;
import com.clsroom.utils.VolleyUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginDialogFragment extends DialogFragment
{
    public static final String TAG = "LoginDialogFragment";
    public static final String FIREBASE_USER_ID = "firebaseUserId";
    private static final int MIN_LENGTH_OF_USER_ID = 3;
    private static final int MIN_LENGTH_OF_PASSWORD = 6;
    private static final String PASSWORD_RESET_URL = "https://us-central1-clsroom-aqua.cloudfunctions.net/passwordResetHttp";

    public static final String EMAIL_SUFFIX = "@clsroom.com";
    private static final String EMAIL_SENT = "Email Sent";
    private static final String INVALID_EMAIL = "Invalid Email";
    private static final String PASSWORD_NOT_RESET = "Password not reset";
    private static final String EMAIL_NOT_SENT = "Email Not Sent";
    private static final String UID = "uid";

    @Bind(R.id.userId)
    EditText mUserIdEditText;

    @Bind(R.id.password)
    EditText mPasswordEditText;

    @Bind(R.id.passwordResetErrorMessage)
    TextView passwordResetErrorMessage;

    @Bind(R.id.loginCredentials)
    ViewGroup loginCredentials;

    private FirebaseAuth mAuth;
    public String mUserId;
    public String mPassword;
    private OnDismissListener mOnDismissListener;

    public static LoginDialogFragment getInstance()
    {
        return new LoginDialogFragment();
    }

    public LoginDialogFragment()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Window window = getDialog().getWindow();
        if (window != null)
        {
            window.requestFeature(Window.FEATURE_NO_TITLE);
        }
        mAuth = FirebaseAuth.getInstance();
        View parentView = inflater.inflate(R.layout.fragment_loing_credentials, container, false);
        ButterKnife.bind(this, parentView);
        Otto.register(this);
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.login_bg);
        return parentView;
    }

    @OnClick(R.id.loginButton)
    public void onClick(View button)
    {
        closeTheKeyBoard();
        if (ConnectivityUtil.isConnected(getActivity()))
        {
            login();
        }
        else
        {
            Snack.show(R.string.noInternet);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);
        Otto.unregister(this);
        if (mOnDismissListener != null)
        {
            mOnDismissListener.onDismiss(null);
        }
    }

    private void login()
    {
        mUserId = mUserIdEditText.getText().toString().trim();
        mPassword = mPasswordEditText.getText().toString().trim();
        if (mUserId.length() < MIN_LENGTH_OF_USER_ID)
        {
            ToastMsg.show(R.string.validUserIdErrMsg);
        }
        else if (mPassword.length() < MIN_LENGTH_OF_PASSWORD)
        {
            ToastMsg.show(R.string.validPasswordErrMsg);
        }
        else
        {
            Progress.show(R.string.signing_in);
            mAuth.signInWithEmailAndPassword(mUserId + EMAIL_SUFFIX, mPassword)
                    .addOnCompleteListener((OnCompleteListener<AuthResult>) getActivity());
            dismiss();
        }
    }

    private void closeTheKeyBoard()
    {
        View view = getActivity().getCurrentFocus();
        if (view != null)
        {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void setOnDismissListener(OnDismissListener onDismissListener)
    {
        mOnDismissListener = onDismissListener;
    }

    @OnClick(R.id.forgotPassword)
    public void forgotPassword(View forgotPassword)
    {
        if (ConnectivityUtil.isConnected(getActivity()))
        {
            Log.d("UnknownLogin", "forgotPassword");
            closeTheKeyBoard();
            checkUserId();
        }
        else
        {
            Snack.show(R.string.noInternet);
        }
    }

    private void checkUserId()
    {
        final String userId = mUserIdEditText.getText().toString();
        if (TextUtils.isEmpty(userId))
        {
            ToastMsg.show(R.string.validUserIdErrMsg);
        }
        else
        {
            Progress.show(R.string.please_wait);
            Map<String, String> data = new HashMap<>();
            data.put(UID, userId);
            VolleyUtil.sendGetData(getActivity(), PASSWORD_RESET_URL, data, new ResultListener<String>()
            {
                @Override
                public void onSuccess(String result)
                {
                    Log.d("UnknownLogin", "result : " + result);
                    switch (result)
                    {
                        case EMAIL_SENT:
                            showPasswordResetResult(R.string.password_has_been_reset_and_sent_to_your_registered_mail_id);
                            break;
                        case EMAIL_NOT_SENT:
                        case PASSWORD_NOT_RESET:
                            showPasswordResetResult(R.string.please_contact_your_admin_to_reset_the_password);
                            break;
                        case INVALID_EMAIL:
                            showPasswordResetResult(R.string.you_have_not_updated_your_mail_id_or_your_mail_id_is_invalid);
                            break;
                    }
                }

                @Override
                public void onError(VolleyError error)
                {
                    Log.d("UnknownLogin", "error : " + error.getMessage());
                    showPasswordResetResult(R.string.please_contact_your_admin_to_reset_the_password);
                }
            });
        }
    }

    private void showPasswordResetResult(int msg)
    {
        Progress.hide();
        loginCredentials.setVisibility(View.GONE);
        TransitionUtil.defaultTransition(loginCredentials);
        passwordResetErrorMessage.setText(msg);
    }
}