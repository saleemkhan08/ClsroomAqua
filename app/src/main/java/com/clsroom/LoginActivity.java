package com.clsroom;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.otto.Subscribe;
import com.clsroom.adapters.LoginSectionsPagerAdapter;
import com.clsroom.dialogs.LoginDialogFragment;
import com.clsroom.listeners.OnDismissListener;
import com.clsroom.model.Progress;
import com.clsroom.model.Snack;
import com.clsroom.model.ToastMsg;
import com.clsroom.utils.Otto;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.clsroom.dialogs.LoginDialogFragment.FIREBASE_USER_ID;

public class LoginActivity extends AppCompatActivity implements OnDismissListener, OnCompleteListener<AuthResult>
{
    public static final String LOGIN_STATUS = "loginStatus";
    public static final String LOGIN_USER_ID = "loginUserId";
    private static final String TAG = "LoginActivity";

    private ProgressDialog mProgressDialog;
    public boolean mIsRunning;

    @Bind(R.id.loginContainer)
    View mLoginButtonContainer;

    @Bind(R.id.pageIndicatorView)
    View mPageIndicatorView;

    private SharedPreferences mSharedPreferences;
    private FirebaseAuth mAuth;
    private LoginDialogFragment mFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        LoginSectionsPagerAdapter sectionsPagerAdapter = new LoginSectionsPagerAdapter(getSupportFragmentManager());

        TextView appName = (TextView) findViewById(R.id.title);
        appName.setTypeface(Typeface.createFromAsset(getAssets(), "Gabriola.ttf"));

        ViewPager viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(sectionsPagerAdapter);

        Log.d("UnknownLogin", "LoginActivity onCreate : currentUser : " + mAuth.getCurrentUser()
                + "LOGIN_STATUS : " + mSharedPreferences.getBoolean(LOGIN_STATUS, false));
        checkLogin();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Otto.register(this);
        mIsRunning = true;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        hideProgressDialog();
        Otto.unregister(this);
        mIsRunning = false;
    }

    @OnClick(R.id.loginDialog)
    public void launchLoginDialog()
    {
        FragmentManager manager = getSupportFragmentManager();
        mFragment = LoginDialogFragment.getInstance();
        mFragment.setOnDismissListener(this);
        mFragment.show(manager, LoginDialogFragment.TAG);
        mLoginButtonContainer.setVisibility(View.GONE);
    }

    @Subscribe
    public void snackBar(Snack snack)
    {
        Snackbar snackbar = Snackbar
                .make(findViewById(android.R.id.content), snack.getMsg(), Snackbar.LENGTH_SHORT);
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();

        TextView textView = (TextView) layout.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
        {
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
        layout.setBackgroundResource(R.color.colorPrimary);
        snackbar.show();
    }

    @Subscribe
    public void toastMsg(ToastMsg toast)
    {
        Toast.makeText(this, toast.getMsg(), Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void progressDialog(Progress progress)
    {
        if (progress.toBeShown())
        {
            showProgressDialog(progress.getMsg());
        }
        else
        {
            hideProgressDialog();
        }
    }

    private void showProgressDialog(int msg)
    {
        Log.d(TAG, "showProgressDialog");
        if (mProgressDialog == null)
        {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setMessage(getString(msg));
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    private void hideProgressDialog()
    {
        Log.d(TAG, "hideProgressDialog");
        if (mProgressDialog != null && mProgressDialog.isShowing())
        {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onDismiss()
    {
        mLoginButtonContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onComplete(@NonNull Task<AuthResult> task)
    {
        if (task.isSuccessful())
        {
            if (mAuth.getCurrentUser() != null)
            {
                Log.d("UnknownLogin", "onComplete : " + mAuth.getCurrentUser().getDisplayName());
                mSharedPreferences.edit()
                        .putBoolean(LOGIN_STATUS, true)
                        .putString(FIREBASE_USER_ID, mAuth.getCurrentUser().getUid())
                        .putString(LOGIN_USER_ID, mFragment.mUserId)
                        .apply();
                launchMainActivity();
                return;
            }
        }
        loginFailed();
    }

    private void launchMainActivity()
    {
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                if (mIsRunning)
                {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }
            }
        }, 1000);
    }

    private void loginFailed()
    {
        mSharedPreferences.edit()
                .putBoolean(LOGIN_STATUS, false)
                .apply();
        Progress.hide();
        ToastMsg.show(R.string.loginFailed);
    }

    public void checkLogin()
    {
        Log.d("UnknownLogin", "mAuth.getCurrentUser");
        if (mSharedPreferences.getBoolean(LOGIN_STATUS, false))
        {
            mLoginButtonContainer.setVisibility(View.GONE);
            mPageIndicatorView.setVisibility(View.GONE);
            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    if (mIsRunning)
                    {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                }
            }, 1000);
        }
        else
        {
            mLoginButtonContainer.setVisibility(View.VISIBLE);
        }
    }
}
