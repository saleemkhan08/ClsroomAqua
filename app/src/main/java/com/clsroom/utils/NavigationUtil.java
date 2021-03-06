package com.clsroom.utils;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.clsroom.listeners.FragmentLauncher;
import com.clsroom.model.Snack;
import com.clsroom.model.Staff;
import com.clsroom.model.Students;
import com.clsroom.model.ToastMsg;
import com.clsroom.model.User;
import com.clsroom.views.SquareImageView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import static android.content.ContentValues.TAG;
import static com.clsroom.LoginActivity.LOGIN_USER_ID;

public class NavigationUtil implements NavigationView.OnNavigationItemSelectedListener, ValueEventListener, DrawerLayout.DrawerListener
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
    public static final String SINGLE_NOTES_FRAGMENT = "singleNotesFragment";
    public static final String ADD_OR_EDIT_NOTES_FRAGMENT = "addOrEditNotesFragment";
    public static final String STAFF_ATTENDANCE_LIST_FRAGMENT = "StaffAttendanceList";
    public static final String STUDENTS_ATTENDANCE_LIST_FRAGMENT = "StudentAttendanceList";
    public static boolean isStudent;
    public static boolean isAdmin;
    public static String userId;
    private int mCurrentMenu;

    private FragmentLauncher launcher;
    private DrawerLayout mDrawer;
    private SharedPreferences mSharedPrefs;
    private ProgressDialog mProgressDialog;
    private FragmentManager mFragmentManager;
    private String mCurrentFragment = "";
    private NavigationView mNavigationView;
    private EventsListener mListener;
    private TextView mUserFullName;
    private TextView mUserDesignation;
    private SquareImageView mProfileImgView;
    public static User mCurrentUser;
    private boolean isMenuLoaded;
    private int itemId;
    private String mMainPageFragmentTag;
    private EventsListener mTempListener;

    public NavigationUtil(final FragmentLauncher launcher)
    {
        this.launcher = launcher;
        mFragmentManager = this.launcher.getSupportFragmentManager();
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(launcher.getActivity());
        mDrawer = launcher.getActivity().findViewById(R.id.drawer_layout);
        mNavigationView = launcher.getActivity().findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        View headerView = mNavigationView.getHeaderView(0);
        headerView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                itemId = view.getId();
                if (ConnectivityUtil.isConnected(launcher.getActivity()))
                {
                    addFragment(PROFILE_FRAGMENT);
                }
                else
                {
                    Snack.show(R.string.noInternet);
                }
                mDrawer.closeDrawers();
            }
        });

        mProfileImgView = headerView.findViewById(R.id.profileImageView);
        mUserFullName = headerView.findViewById(R.id.userFullName);
        mUserDesignation = headerView.findViewById(R.id.userId);

        String userId = mSharedPrefs.getString(LOGIN_USER_ID, "");
        String refreshedToken = mSharedPrefs.getString(User.TOKEN, "");

        DatabaseReference ref = User.getRef(userId);
        ref.addValueEventListener(this);
        ref.child(User.TOKEN).setValue(refreshedToken).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    Log.d(TAG, "Saved Refreshed token");
                }
                else
                {
                    Log.d(TAG, "Could not save Refreshed token");
                }
            }
        });

        mDrawer.addDrawerListener(this);
        mFragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener()
        {
            @Override
            public void onBackStackChanged()
            {
                int currentEntryCount = (mFragmentManager.getBackStackEntryCount());
                if (currentEntryCount > 0)
                {
                    mCurrentFragment = mFragmentManager.getBackStackEntryAt(currentEntryCount - 1).getName();
                }
                else
                {
                    mCurrentFragment = "";
                }
                Log.d("relaunchIssue", "mCurrentFragmentName : " + mCurrentFragment);
            }
        });
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                if (firebaseAuth.getCurrentUser() == null)
                {
                    ToastMsg.show(R.string.please_login);
                    logout();

                }
            }
        });
    }

    private boolean isNewObjectRequired(String tag)
    {
        if (!mCurrentFragment.equals(tag))
        {
            if (!tag.equals(mMainPageFragmentTag))
            {
                Log.d("LifeCycleCheck", "isNewObjectRequired : " + tag);
                boolean popped = mFragmentManager.popBackStackImmediate(tag, 0);
                if (!popped)
                {
                    return true;
                }
            }
            else
            {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item)
    {
        itemId = item.getItemId();
        closeDrawer();
        return true;
    }

    private void handleNavigationItemClick()
    {
        Log.d("relaunchIssue", "NavUtil : handleNavigationItemClick : itemId : " + itemId);
        switch (itemId)
        {
            case R.id.admin_students:
                replaceFragment(STUDENTS_LIST_FRAGMENT, true);
                break;
            case R.id.admin_classes:
                replaceFragment(CLASSES_LIST_FRAGMENT, true);
                break;
            case R.id.admin_staff:
                replaceFragment(STAFF_LIST_FRAGMENT, true);
                break;
            case R.id.launch_leaves_fragment:
                replaceFragment(LEAVES_LIST_FRAGMENT, true);
                break;
            case R.id.launch_notes_fragment:
                replaceFragment(NOTES_FRAGMENT, true);
                break;
            case R.id.launch_subjects_fragment:
                replaceFragment(SUBJECTS_FRAGMENT, true);
                break;
            case R.id.launch_time_table_fragment:
                replaceFragment(TIME_TABLE_FRAGMENT, true);
                break;
            case R.id.nav_notifications:
                replaceFragment(NOTIFICATIONS_FRAGMENT, true);
                break;
            case R.id.nav_settings:
                replaceFragment(PROFILE_FRAGMENT, true);
                break;
            case R.id.nav_logout:
                logout();
                break;
        }
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
                case STUDENTS_LIST_FRAGMENT:
                    return new StudentsListFragment();
            }
        }
        return fragment;
    }

    private void replaceFragment(String tag, boolean addToBackStack)
    {
        Log.d("relaunchIssue", "NavUtil : replaceFragment3");
        if (isNewObjectRequired(tag))
        {
            replaceFragment(getFragment(tag), addToBackStack, tag);
        }
    }

    private void addFragment(String tag)
    {
        Log.d("relaunchIssue", "NavUtil : addFragment3");
        if (isNewObjectRequired(tag))
        {
            addFragment(getFragment(tag), true, tag);
        }
    }

    public void replaceFragment(Fragment fragment, boolean addToBackStack, String tag)
    {
        Log.d("relaunchIssue", "NavUtil : replaceFragment2");
        replaceFragment(fragment, addToBackStack, tag, null, null);
    }

    public void addFragment(Fragment fragment, boolean addToBackStack, String tag)
    {
        Log.d("relaunchIssue", "NavUtil : addFragment2");
        addFragment(fragment, addToBackStack, tag, null, null);
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
            mProgressDialog = new ProgressDialog(launcher.getActivity());
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
                launcher.getActivity().startActivity(new Intent(launcher.getActivity(), LoginActivity.class));
                launcher.getActivity().finish();
            }
        }, 1000);
    }

    public boolean onBackPressed()
    {
        Log.d("relaunchIssue", "NavigationUtil : onBackPressed");
        if (mTempListener != null)
        {
            mListener = mTempListener;
            mListener.refreshActionBar();
            mTempListener = null;
        }
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
        Menu mMenu = mNavigationView.getMenu();
        mMenu.clear();
        mNavigationView.inflateMenu(mCurrentMenu);
    }

    public void updateCurrentFragment(EventsListener listener)
    {
        mListener = listener;
        String tag = listener.getTagName();
        mNavigationView.setCheckedItem(listener.getMenuItemId());
        mCurrentFragment = tag;
        Log.d("relaunchIssue", "NavUtil : updateCurrentFragment : " + tag);
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
        mProfileImgView.setImageURI(mCurrentUser.getPhotoUrl());
        userId = mCurrentUser.getUserId();
        if (!isMenuLoaded)
        {
            switch (mCurrentUser.userType())
            {
                case User.ADMIN:
                    mCurrentMenu = R.menu.admin_drawer;
                    mMainPageFragmentTag = CLASSES_LIST_FRAGMENT;
                    isStudent = false;
                    isAdmin = true;
                    break;
                case User.STAFF:
                    mMainPageFragmentTag = NOTES_FRAGMENT;
                    mCurrentMenu = R.menu.staff_drawer;
                    isStudent = false;
                    isAdmin = false;
                    break;
                default:
                    mCurrentMenu = R.menu.student_drawer;
                    isStudent = true;
                    isAdmin = false;
                    mMainPageFragmentTag = NOTES_FRAGMENT;
                    break;
            }
            Log.d("relaunchIssue", "NavUtil : mMainPageFragmentTag");
            replaceFragment(mMainPageFragmentTag, false);
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

    public void replaceFragment(Fragment fragment, boolean addToBackStack, String tag, View sharedImageView, String transitionName)
    {
        Log.d("relaunchIssue", "NavUtil : replaceFragment1");
        mTempListener = null;
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.content_main, fragment, tag);
        //transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        if (addToBackStack)
        {
            transaction.addToBackStack(tag);
        }
        if (transitionName != null && !TextUtils.isEmpty(transitionName) && sharedImageView != null
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            transaction.addSharedElement(sharedImageView, transitionName);
        }
        transaction.commit();
    }

    public void addFragment(Fragment fragment, boolean addToBackStack, String tag, View sharedImageView, String transitionName)
    {
        Log.d("relaunchIssue", "NavUtil : addFragment1");
        mTempListener = mListener;
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.add(R.id.content_main, fragment, tag);
        if (addToBackStack)
        {
            transaction.addToBackStack(tag);
        }
        if (transitionName != null && !TextUtils.isEmpty(transitionName) && sharedImageView != null
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            transaction.addSharedElement(sharedImageView, transitionName);
        }
        transaction.commit();
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset)
    {

    }

    @Override
    public void onDrawerOpened(View drawerView)
    {

    }

    @Override
    public void onDrawerClosed(View drawerView)
    {
        Log.d("relaunchIssue", "NavUtil : onDrawerClosed");
        if (ConnectivityUtil.isConnected(launcher.getActivity()))
        {
            handleNavigationItemClick();
        }
        else
        {
            Snack.show(R.string.noInternet);
        }
    }

    @Override
    public void onDrawerStateChanged(int newState)
    {

    }
}
