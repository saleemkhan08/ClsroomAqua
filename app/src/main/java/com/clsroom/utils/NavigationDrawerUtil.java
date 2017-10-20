package com.clsroom.utils;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.clsroom.LoginActivity;
import com.clsroom.R;
import com.clsroom.fragments.ClassesListFragment;
import com.clsroom.fragments.LeavesFragment;
import com.clsroom.fragments.NotesFragment;
import com.clsroom.fragments.NotificationListFragment;
import com.clsroom.fragments.ProfileFragment;
import com.clsroom.fragments.StaffListFragment;
import com.clsroom.fragments.StudentsListFragment;
import com.clsroom.fragments.SubjectsListFragment;
import com.clsroom.fragments.TimeTableFragment;
import com.clsroom.listeners.EventsListener;
import com.clsroom.model.Staff;
import com.clsroom.model.Students;
import com.clsroom.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.ContentValues.TAG;

public class NavigationDrawerUtil implements NavigationView.OnNavigationItemSelectedListener, ValueEventListener
{
    public static final String STUDENTS_LIST_FRAGMENT = "studentsListFragment";
    public static final String CLASSES_LIST_FRAGMENT = "classesListFragment";
    public static final String STAFF_LIST_FRAGMENT = "staffListFragment";
    public static final String LEAVES_LIST_FRAGMENT = "leavesListFragment";
    public static final String NOTES_FRAGMENT = "notesFragment";
    public static final String SUBJECTS_FRAGMENT = "subjectsFragment";
    public static final String ATTENDANCE_FRAGMENT = "attendanceFragment";
    public static final String TIME_TABLE_FRAGMENT = "timeTableFragment";
    public static final String PROFILE_FRAGMENT = "profileFragment";
    public static final String NOTIFICATIONS_FRAGMENT = "notificationsFragment";
    public static boolean isStudent;
    public static boolean isAdmin;
    private final View headerView;
    private int mCurrentMenu;

    private AppCompatActivity mActivity;
    private DrawerLayout mDrawer;
    private SharedPreferences mSharedPrefs;
    private ProgressDialog mProgressDialog;
    private FragmentManager mFragmentManager;
    public String mCurrentFragment = "";
    public NavigationView mNavigationView;
    public EventsListener mListener;
    private Menu mMenu;
    private TextView mUserFullName;
    private TextView mUserDesignation;
    private ImageView mProfileImgView;
    public static User mCurrentUser;
    private boolean isMenuLoaded;

    public NavigationDrawerUtil(AppCompatActivity activity)
    {
        mActivity = activity;
        mFragmentManager = mActivity.getSupportFragmentManager();
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
        mDrawer = (DrawerLayout) mActivity.findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) mActivity.findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        headerView = mNavigationView.getHeaderView(0);
        headerView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                loadFragment(PROFILE_FRAGMENT, true);
                mDrawer.closeDrawers();
            }
        });

        mProfileImgView = (ImageView) headerView.findViewById(R.id.profileImageView);
        mUserFullName = (TextView) headerView.findViewById(R.id.userFullName);
        mUserDesignation = (TextView) headerView.findViewById(R.id.userId);

        String userId = mSharedPrefs.getString(LoginActivity.LOGIN_USER_ID, "");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        if (userId.charAt(0) == 'a' || userId.charAt(0) == 's')
        {
            ref.child(User.STAFF).child(userId).addValueEventListener(this);
        }
        else
        {
            ref.child(User.STUDENTS).child(userId.substring(0, 3)).child(userId).addValueEventListener(this);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.admin_students:
                loadFragment(STUDENTS_LIST_FRAGMENT, true);
                break;
            case R.id.admin_classes:
                loadFragment(CLASSES_LIST_FRAGMENT, true);
                break;
            case R.id.admin_staff:
                loadFragment(STAFF_LIST_FRAGMENT, true);
                break;
            case R.id.launch_leaves_fragment:
                loadFragment(LEAVES_LIST_FRAGMENT, true);
                break;
            case R.id.launch_notes_fragment:
                loadFragment(NOTES_FRAGMENT, true);
                break;
            case R.id.launch_subjects_fragment:
                loadFragment(SUBJECTS_FRAGMENT, true);
                break;
            case R.id.launch_time_table_fragment:
                loadFragment(TIME_TABLE_FRAGMENT, true);
                break;
            case R.id.nav_notifications:
                loadFragment(NOTIFICATIONS_FRAGMENT, true);
                break;
            case R.id.nav_settings:
                loadFragment(PROFILE_FRAGMENT, true);
                break;
            case R.id.nav_logout:
                logout();
                break;
        }
        closeDrawer();
        return true;
    }

    private Fragment getFragment(String tag)
    {
        Fragment fragment = mFragmentManager.findFragmentByTag(tag);
        if (fragment == null)
        {
            switch (tag)
            {
                case CLASSES_LIST_FRAGMENT:
                    return new ClassesListFragment();
                case STAFF_LIST_FRAGMENT:
                    return new StaffListFragment();
                case LEAVES_LIST_FRAGMENT:
                    return new LeavesFragment();
                case NOTES_FRAGMENT:
                    return new NotesFragment();
                case SUBJECTS_FRAGMENT:
                    return new SubjectsListFragment();
                case TIME_TABLE_FRAGMENT:
                    return new TimeTableFragment();
                case PROFILE_FRAGMENT:
                    return ProfileFragment.getInstance(mCurrentUser);
                case NOTIFICATIONS_FRAGMENT:
                    return new NotificationListFragment();
            }
        }

        if (tag.contains(STUDENTS_LIST_FRAGMENT))
        {
            return new StudentsListFragment();
        }

        return fragment;
    }

    private void loadFragment(String tag, boolean addToBackStack)
    {
        Log.d(TAG, "loadFragment : " + tag);
        if (!mCurrentFragment.equals(tag))
        {
            boolean isPopped = mFragmentManager.popBackStackImmediate(tag, 0);
            Log.d(TAG, "isPopped : " + isPopped);
            if (!isPopped)
            {
                loadFragment(getFragment(tag), addToBackStack, tag);
            }
            mCurrentFragment = tag;
        }
    }

    public void loadFragment(Fragment fragment, boolean addToBackStack, String tag)
    {
        mListener = (EventsListener) fragment;
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.content_main, (Fragment) mListener, tag);
        if (addToBackStack)
        {
            transaction.addToBackStack(tag);
        }
        transaction.commit();
    }

    public boolean isDrawerOpen()
    {
        return mDrawer.isDrawerOpen(GravityCompat.START);
    }

    public void closeDrawer()
    {
        mDrawer.closeDrawer(GravityCompat.START);
    }

    private void showProgressDialog(String msg)
    {
        Log.d(TAG, "showProgressDialog");

        if (mProgressDialog == null)
        {
            mProgressDialog = new ProgressDialog(mActivity);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setMessage(msg);
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

    private void logout()
    {
        showProgressDialog("Logging out ...");
        mSharedPrefs.edit().putBoolean(LoginActivity.LOGIN_STATUS, false).apply();

        Log.d(TAG, "Launching MainActivity : through Handler");
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                hideProgressDialog();
                mActivity.startActivity(new Intent(mActivity, LoginActivity.class));
                mActivity.finish();
            }
        }, 1000);
    }

    public boolean onBackPressed()
    {
        if (isDrawerOpen())
        {
            closeDrawer();
            return false;
        }
        else
        {
            return mListener.onBackPressed();
        }
    }

    private void loadCurrentMenu()
    {
        mMenu = mNavigationView.getMenu();
        mMenu.clear();
        mNavigationView.inflateMenu(mCurrentMenu);
    }

    public void updateCurrentFragment(EventsListener listener)
    {
        mListener = listener;
        String tag = listener.getTagName();
        mNavigationView.setCheckedItem(listener.getMenuItemId());
        mCurrentFragment = tag;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot)
    {
        Log.d("ProfileUpdateIssue", "dataSnapshot : " + dataSnapshot);
        if (dataSnapshot.hasChild(Staff.IS_ADMIN))
        {
            mCurrentUser = dataSnapshot.getValue(Staff.class);
            mUserDesignation.setText(((Staff) mCurrentUser).getDesignation());
        }
        else
        {
            mCurrentUser = dataSnapshot.getValue(Students.class);
            mUserDesignation.setText(((Students) mCurrentUser).getClassName());
        }

        mUserFullName.setText(mCurrentUser.getFullName());
        ImageUtil.loadCircularImg(mActivity, mCurrentUser.getPhotoUrl(), mProfileImgView);

        if (!isMenuLoaded)
        {
            switch (mCurrentUser.userType())
            {
                case User.ADMIN:
                    mCurrentMenu = R.menu.admin_drawer;
                    loadFragment(CLASSES_LIST_FRAGMENT, false);
                    isStudent = false;
                    isAdmin = true;
                    break;
                case User.STAFF:
                    mCurrentMenu = R.menu.staff_drawer;
                    loadFragment(NOTES_FRAGMENT, false);
                    isStudent = false;
                    isAdmin = false;
                    break;
                default:
                    mCurrentMenu = R.menu.student_drawer;
                    isStudent = true;
                    isAdmin = false;
                    loadFragment(NOTES_FRAGMENT, false);
                    break;
            }
            isMenuLoaded = true;
            loadCurrentMenu();
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError)
    {

    }

    public static String getClassId()
    {
        return mCurrentUser.getUserId().substring(0, 3);
    }
}
