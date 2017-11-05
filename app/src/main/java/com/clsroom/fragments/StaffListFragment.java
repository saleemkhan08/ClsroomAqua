package com.clsroom.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.android.volley.VolleyError;
import com.clsroom.R;
import com.clsroom.adapters.StaffAdapter;
import com.clsroom.dialogs.AddOrEditStaffDialogFragment;
import com.clsroom.dialogs.ViewStaffAttendanceDialogFragment;
import com.clsroom.listeners.EventsListener;
import com.clsroom.listeners.FragmentLauncher;
import com.clsroom.listeners.ResultListener;
import com.clsroom.model.Progress;
import com.clsroom.model.Snack;
import com.clsroom.model.Staff;
import com.clsroom.model.ToastMsg;
import com.clsroom.model.User;
import com.clsroom.utils.ActionBarUtil;
import com.clsroom.utils.ConnectivityUtil;
import com.clsroom.utils.NavigationUtil;
import com.clsroom.utils.Otto;
import com.clsroom.utils.TransitionUtil;
import com.clsroom.utils.VolleyUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.otto.Subscribe;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.clsroom.model.User.EMAIL_NOT_SENT;
import static com.clsroom.model.User.EMAIL_SENT;
import static com.clsroom.model.User.GENERATE_USER_LIST_URL;
import static com.clsroom.model.User.INVALID_EMAIL;
import static com.clsroom.model.User.INVALID_TOKEN;


public class StaffListFragment extends Fragment implements EventsListener, DatePickerDialog.OnDateSetListener
{
    private static final String TAG = NavigationUtil.STAFF_LIST_FRAGMENT;

    @BindView(R.id.staffListRecyclerView)
    RecyclerView mStaffListRecyclerView;

    @BindView(R.id.recyclerProgress)
    View mProgress;

    @BindView(R.id.errorMsg)
    View mErrorMsg;

    @BindView(R.id.fabContainer)
    ViewGroup mFabContainer;

    @BindView(R.id.attendanceFab)
    View mTakeAttendanceFab;

    @BindView(R.id.savefab)
    View mSaveAttendanceFab;

    private DatabaseReference mRootRef;
    private StaffAdapter mAdapter;
    private DatePickerDialog mDatePickerDialog;
    private LinkedHashSet<Staff> staffSet;
    private FragmentLauncher launcher;

    public StaffListFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_staff_list, container, false);
        setLauncher();
        Otto.register(this);
        ButterKnife.bind(this, parentView);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        setUpRecyclerView();
        mDatePickerDialog = new DatePickerDialog(getActivity(), this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

        refreshActionBar();
        return parentView;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Otto.unregister(this);
    }

    private void setUpRecyclerView()
    {
        mProgress.setVisibility(View.VISIBLE);
        mErrorMsg.setVisibility(View.GONE);
        DatabaseReference staffDbRef = mRootRef.child(Staff.STAFF);
        mAdapter = StaffAdapter.getInstance(staffDbRef, launcher);
        mStaffListRecyclerView.setAdapter(mAdapter);
        mStaffListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mStaffListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if (dy > 50 && mFabContainer.isShown())
                {
                    TransitionUtil.slideTransition(mFabContainer);
                    mFabContainer.setVisibility(View.GONE);
                }
                else if (dy < 0 && !mFabContainer.isShown() && NavigationUtil.isAdmin)
                {
                    TransitionUtil.slideTransition(mFabContainer);
                    mFabContainer.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState)
            {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        staffDbRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                mProgress.setVisibility(View.GONE);
                if (dataSnapshot.getChildrenCount() <= 0)
                {
                    mErrorMsg.setVisibility(View.VISIBLE);
                    if (staffSet == null)
                    {
                        staffSet = new LinkedHashSet<>();
                    }
                    else
                    {
                        staffSet.clear();
                    }
                }
                else
                {
                    mErrorMsg.setVisibility(View.GONE);
                    if (staffSet == null)
                    {
                        staffSet = new LinkedHashSet<>();
                    }
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        staffSet.add(snapshot.getValue(Staff.class));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG, "databaseError : " + databaseError);
                mProgress.setVisibility(View.GONE);
                mErrorMsg.setVisibility(View.VISIBLE);
            }
        });
    }

    @OnClick(R.id.attendanceFab)
    public void takeAttendance(View view)
    {
        if (ConnectivityUtil.isConnected(getActivity()))
        {
            if (staffSet.size() > 0)
            {
                mAdapter.enableAttendance(staffSet);
                mSaveAttendanceFab.setVisibility(View.VISIBLE);
                mTakeAttendanceFab.setVisibility(View.GONE);
            }
            else
            {
                ToastMsg.show(R.string.staff_list_is_empty);
            }
        }
        else
        {
            Snack.show(R.string.noInternet);
        }
    }

    @OnClick(R.id.savefab)
    public void saveAttendance(View view)
    {
        if (ConnectivityUtil.isConnected(getActivity()))
        {
            onBackPressed();
            StaffAttendanceListFragment fragment = StaffAttendanceListFragment.getInstance(mAdapter.mUnSelectedStaff);
            launcher.addFragment(fragment, true, StaffAttendanceListFragment.TAG);
        }
        else
        {
            Snack.show(R.string.noInternet);
        }
    }

    @Override
    public boolean onBackPressed()
    {
        if (StaffAdapter.isSelectionEnabled)
        {
            StaffAdapter.isSelectionEnabled = false;
            mAdapter.notifyDataSetChanged();
            Otto.post(ActionBarUtil.SHOW_INDEPENDENT_STAFF_MENU);
            mSaveAttendanceFab.setVisibility(View.GONE);
            mTakeAttendanceFab.setVisibility(View.VISIBLE);
            return false;
        }
        return true;
    }

    @Override
    public int getMenuItemId()
    {
        return R.id.admin_staff;
    }

    @Override
    public String getTagName()
    {
        return NavigationUtil.STAFF_LIST_FRAGMENT;
    }

    @Subscribe
    public void onOptionItemClicked(Integer itemId)
    {
        switch (itemId)
        {
            case R.id.addNewStaff:
                showDialogFragment();
                break;
            case R.id.viewStaffAttendance:
                mDatePickerDialog.show();
                break;
            case R.id.selectAll:
                mAdapter.setSelectAll(staffSet);
                break;
            case R.id.generate_credential:
                generateCredentialList();
                break;
        }
    }

    private void generateCredentialList()
    {
        if (NavigationUtil.isAdmin)
        {
            Progress.show(R.string.generating);
            Map<String, String> data = new HashMap<>();
            data.put(User.UID, NavigationUtil.mCurrentUser.getUserId());
            data.put(User.CLASS_ID, "s");
            data.put(User.CLASS_NAME, "");
            data.put(User.TOKEN, NavigationUtil.mCurrentUser.getToken());

            try
            {
                VolleyUtil.sendGetData(getActivity(), GENERATE_USER_LIST_URL, data, new ResultListener<String>()
                {
                    @Override
                    public void onSuccess(String result)
                    {
                        Progress.hide();
                        switch (result.trim())
                        {
                            case EMAIL_NOT_SENT:
                                ToastMsg.show(R.string.there_was_some_issue_in_generating_user_credentials);
                                break;
                            case EMAIL_SENT:
                                ToastMsg.show(R.string.list_has_been_mailed_to_your_registered_mail_id);
                                break;
                            case INVALID_EMAIL:
                                ToastMsg.show(R.string.your_registered_mail_id_is_invalid);
                                break;
                            case INVALID_TOKEN:
                                ToastMsg.show(R.string.you_are_not_authorized_to_generate_the_list);
                                break;
                        }
                    }

                    @Override
                    public void onError(VolleyError error)
                    {
                        Progress.hide();
                        ToastMsg.show(R.string.you_are_not_authorized_to_generate_the_list);
                    }
                });
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            ToastMsg.show(R.string.you_are_not_authorized_to_generate_the_list);
        }
    }

    public void showDialogFragment()
    {
        if (ConnectivityUtil.isConnected(getActivity()))
        {
            FragmentManager manager = getActivity().getSupportFragmentManager();
            AddOrEditStaffDialogFragment fragment = AddOrEditStaffDialogFragment.getInstance(null);
            fragment.show(manager, AddOrEditStaffDialogFragment.TAG);
        }
        else
        {
            Snack.show(R.string.noInternet);
        }
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day)
    {
        FragmentManager manager = getActivity().getSupportFragmentManager();
        ViewStaffAttendanceDialogFragment fragment = ViewStaffAttendanceDialogFragment.getInstance(year, month, day);
        fragment.show(manager, ViewStaffAttendanceDialogFragment.TAG);
    }

    private void setLauncher()
    {
        Activity activity = getActivity();
        if (activity instanceof FragmentLauncher)
        {
            launcher = (FragmentLauncher) activity;
            launcher.setFragment(this);
        }
    }

    @Override
    public void refreshActionBar()
    {
        if (launcher != null)
        {
            launcher.setToolBarTitle(R.string.staff);
            launcher.updateEventsListener(this);
            Otto.post(ActionBarUtil.SHOW_INDEPENDENT_STAFF_MENU);
        }
    }
}
