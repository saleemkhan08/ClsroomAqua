package com.clsroom;

import android.app.ProgressDialog;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.clsroom.listeners.EventsListener;
import com.clsroom.listeners.FragmentLauncher;
import com.clsroom.model.Progress;
import com.clsroom.model.Snack;
import com.clsroom.model.ToastMsg;
import com.clsroom.utils.ActionBarUtil;
import com.clsroom.utils.ConnectivityUtil;
import com.clsroom.utils.NavigationUtil;
import com.clsroom.utils.Otto;
import com.squareup.otto.Subscribe;

public class MainActivity extends AppCompatActivity implements FragmentLauncher
{
    private static final String TAG = "MainActivity";
    public static final String NOTIFICATION_OBJECT = "notificationAction";

    private ProgressDialog mProgressDialog;
    private NavigationUtil mNavigationUtil;
    private Toolbar mToolbar;
    private ActionBarUtil mActionBarUtil;
    private Fragment mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        setSupportActionBar(mToolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            View view = getWindow().getDecorView();
            int flags = view.getSystemUiVisibility();
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        Log.d("DualMainActivity", "OnCreate");
        mNavigationUtil = new NavigationUtil(this);
        Otto.register(this);
    }

    @Override
    public void onBackPressed()
    {
        Log.d("ProfileRelaunchIssue", "MainActivity : onBackPressed");
        if (mNavigationUtil.onBackPressed())
        {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mActionBarUtil.unRegisterOtto();
        Otto.unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        mActionBarUtil = new ActionBarUtil(this, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (ConnectivityUtil.isConnected(this))
        {
            Otto.post(item.getItemId());
        }
        else
        {
            Snack.show(R.string.noInternet);
        }
        return true;
    }

    @Override
    public void updateEventsListener(EventsListener listener)
    {
        mNavigationUtil.updateCurrentFragment(listener);
    }

    @Override
    public AppCompatActivity getActivity()
    {
        return this;
    }

    @Override
    public void showFragment(Fragment fragment, boolean addToBackStack, String tag)
    {
        Log.d("relaunchIssue", "MainAct : showFragment1");
        if (ConnectivityUtil.isConnected(this))
        {
            mNavigationUtil.loadFragment(fragment, addToBackStack, tag);
        }
        else
        {
            Snack.show(R.string.noInternet);
        }
    }

    @Override
    public Fragment getFragment()
    {
        return mCurrentFragment;
    }

    @Override
    public void setFragment(Fragment fragment)
    {
        mCurrentFragment = fragment;
    }

    @Override
    public void showFragment(Fragment fragment, boolean addToBackStack, String tag, ImageView sharedImageView, String transitionName)
    {
        Log.d("relaunchIssue", "MainAct : showFragment2");
        if (ConnectivityUtil.isConnected(this))
        {
            mNavigationUtil.loadFragment(fragment, addToBackStack, tag, sharedImageView, transitionName);
        }
        else
        {
            Snack.show(R.string.noInternet);
        }
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
        if (toast.getTxtMsg() == null)
        {
            Toast.makeText(this, toast.getMsg(), Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this, toast.getTxtMsg(), Toast.LENGTH_SHORT).show();
        }

    }

    @Subscribe
    public void progressDialog(Progress progress)
    {
        Log.d("UploadIssue", "Otto Subscribe progressDialog " + progress.getMsg()
                + ", progress.toBeShown() : " + progress.toBeShown());
        if (progress.getMsg() != 0)
        {
            Log.d("progressDialog", "" + getString(progress.getMsg()));
        }
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
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.setMessage(getString(msg));
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
    public void setToolBarTitle(int resId)
    {
        mToolbar.setTitle(resId);
    }
}
