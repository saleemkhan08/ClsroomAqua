package com.clsroom;

import android.app.ProgressDialog;
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
import android.widget.TextView;
import android.widget.Toast;

import com.clsroom.listeners.EventsListener;
import com.clsroom.model.Progress;
import com.clsroom.model.Snack;
import com.clsroom.model.ToastMsg;
import com.clsroom.utils.ActionBarUtil;
import com.clsroom.utils.NavigationDrawerUtil;
import com.clsroom.utils.Otto;
import com.squareup.otto.Subscribe;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";

    private ProgressDialog mProgressDialog;
    private NavigationDrawerUtil mNavigationDrawerUtil;
    private Toolbar mToolbar;
    private ActionBarUtil mActionBarUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

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
        mNavigationDrawerUtil = new NavigationDrawerUtil(this);
        Otto.register(this);
    }

    @Override
    public void onBackPressed()
    {
        if (mNavigationDrawerUtil.onBackPressed())
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
        Otto.post(item.getItemId());
        return true;
    }

    public void updateEventsListener(EventsListener listener)
    {
        mNavigationDrawerUtil.updateCurrentFragment(listener);
    }

    public void setToolBarTitle(String title)
    {
        mToolbar.setTitle(title);
    }

    public void showFragment(Fragment fragment, boolean addToBackStack, String tag)
    {
        mNavigationDrawerUtil.loadFragment(fragment, addToBackStack, tag);
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
}
