package com.clsroom.adapters;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import com.clsroom.R;
import com.clsroom.dialogs.AddOrEditStaffDialogFragment;
import com.clsroom.fragments.ProfileFragment;
import com.clsroom.fragments.StaffListFragment;
import com.clsroom.listeners.FragmentLauncher;
import com.clsroom.model.Progress;
import com.clsroom.model.Snack;
import com.clsroom.model.Staff;
import com.clsroom.model.ToastMsg;
import com.clsroom.utils.ActionBarUtil;
import com.clsroom.utils.ConnectivityUtil;
import com.clsroom.utils.ImageUtil;
import com.clsroom.utils.Otto;
import com.clsroom.viewholders.StaffViewHolder;
import com.clsroom.views.DetailsTransition;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.squareup.otto.Subscribe;

import java.util.LinkedHashSet;

import static com.clsroom.utils.ActionBarUtil.SHOW_INDEPENDENT_STAFF_MENU;

public class StaffAdapter extends FirebaseRecyclerAdapter<Staff, StaffViewHolder>
{
    private static final String TAG = "StaffAdapter";
    private FragmentLauncher launcher;
    private DatabaseReference mStaffDbReference;
    public static boolean isSelectionEnabled;
    private LinkedHashSet<Staff> mSelectedStaff;
    public LinkedHashSet<Staff> mUnSelectedStaff;
    private boolean isSelectAll;

    public static StaffAdapter getInstance(DatabaseReference reference, FragmentLauncher launcher)
    {
        StaffAdapter adapter = new StaffAdapter(Staff.class,
                R.layout.staff_list_row, StaffViewHolder.class, reference.orderByChild(Staff.USER_ID), launcher);
        adapter.mStaffDbReference = reference;
        return adapter;
    }

    private StaffAdapter(Class<Staff> modelClass, int modelLayout, Class<StaffViewHolder> viewHolderClass,
                         Query ref, FragmentLauncher launcher)
    {
        super(modelClass, modelLayout, viewHolderClass, ref);
        this.launcher = launcher;
    }

    @Override
    protected void populateViewHolder(final StaffViewHolder viewHolder, final Staff model, int position)
    {
        String imageUrl = model.getPhotoUrl();
        ImageUtil.loadCircularImg(viewHolder.itemView.getContext(), imageUrl, viewHolder.mImageView);

        viewHolder.mFullName.setText(model.getFullName());
        viewHolder.mUserId.setText(model.getUserId());
        viewHolder.mDesignation.setText(model.getDesignation());
        viewHolder.mAdminImageView.setVisibility(model.getIsAdmin() ? View.VISIBLE : View.GONE);
        if (isSelectionEnabled)
        {
            viewHolder.mCheckBox.setVisibility(View.VISIBLE);
            viewHolder.optionsIconContainer.setVisibility(View.GONE);
        }
        else
        {
            viewHolder.mCheckBox.setVisibility(View.GONE);
            viewHolder.optionsIconContainer.setVisibility(View.VISIBLE);
        }
        if (mSelectedStaff != null)
        {
            viewHolder.mCheckBox.setChecked(mSelectedStaff.contains(model));
        }
        viewHolder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked)
            {
                if (viewHolder.mCheckBox.isShown())
                {
                    updateSelection(isChecked, model);
                }
            }
        });
        viewHolder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ProfileFragment fragment2 = ProfileFragment.getInstance(model);
                StaffListFragment fragment1 = (StaffListFragment) launcher.getFragment();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                    fragment2.setSharedElementEnterTransition(new DetailsTransition());
                    fragment2.setSharedElementReturnTransition(new DetailsTransition());

                    viewHolder.mImageView.setTransitionName(model.getUserId());
                    launcher.showFragment(fragment2, true, ProfileFragment.TAG, viewHolder.mImageView, "profileImage");
                }
                else
                {
                    launcher.showFragment(fragment2, true, ProfileFragment.TAG);
                }
            }
        });

        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                Log.d(TAG, this + ", onLongClick, isSelectionEnabled : " + isSelectionEnabled);
                Otto.post(ActionBarUtil.SHOW_MULTIPLE_STAFF_MENU);
                enableSelection();
                return true;
            }
        });
        configureOptions(viewHolder, model);
    }

    private void configureOptions(final StaffViewHolder holder, final Staff staff)
    {
        holder.optionsIconContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                PopupMenu popup = new PopupMenu(launcher.getActivity(), v);
                popup.getMenuInflater()
                        .inflate(R.menu.classes_options, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    public boolean onMenuItemClick(MenuItem item)
                    {
                        switch (item.getItemId())
                        {
                            case R.id.action_edit:
                                editClasses(staff);
                                break;
                            case R.id.action_delete:
                                confirmDelete(staff);
                                break;
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });
    }

    private void confirmDelete(Staff staff)
    {
        Progress.show(R.string.deleting);
        mStaffDbReference.child(staff.getUserId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                Progress.hide();
                if (task.isSuccessful())
                {
                    ToastMsg.show(R.string.deleted);
                }
                else
                {
                    ToastMsg.show(R.string.please_try_again);
                }
            }
        });
    }

    private void editClasses(Staff staff)
    {
        if (ConnectivityUtil.isConnected(launcher.getActivity()))
        {
            FragmentManager manager = launcher.getSupportFragmentManager();
            AddOrEditStaffDialogFragment fragment = AddOrEditStaffDialogFragment.getInstance(staff);
            fragment.show(manager, AddOrEditStaffDialogFragment.TAG);
        }
        else
        {
            Snack.show(R.string.noInternet);
        }
    }

    private void updateSelection(boolean isChecked, Staff model)
    {
        if (isChecked)
        {
            mSelectedStaff.add(model);
            mUnSelectedStaff.remove(model);
        }
        else
        {
            mSelectedStaff.remove(model);
            mUnSelectedStaff.add(model);
        }
    }

    private void enableSelection()
    {
        isSelectionEnabled = true;
        clearSet();
        Otto.register(this);
        notifyDataSetChanged();
    }

    @Subscribe
    public void reload(String str)
    {
        Log.d(TAG, "reload : " + str);
        if (str.equals(SHOW_INDEPENDENT_STAFF_MENU))
        {
            notifyDataSetChanged();
            Otto.unregister(this);
        }
    }

    public void enableAttendance(LinkedHashSet<Staff> staffSet)
    {
        Otto.post(ActionBarUtil.SHOW_ATTENDANCE_MENU);
        isSelectionEnabled = true;
        clearSet();
        mSelectedStaff.addAll(staffSet);
        Otto.register(this);
        notifyDataSetChanged();
    }

    public void setSelectAll(LinkedHashSet<Staff> staffSet)
    {
        isSelectAll = !isSelectAll;
        clearSet();
        if (isSelectAll)
        {
            mSelectedStaff.addAll(staffSet);
        }
        else
        {
            mUnSelectedStaff.addAll(staffSet);
        }
        notifyDataSetChanged();
    }

    private void clearSet()
    {
        if (mSelectedStaff == null)
        {
            mSelectedStaff = new LinkedHashSet<>();
        }
        else
        {
            mSelectedStaff.clear();
        }

        if (mUnSelectedStaff == null)
        {
            mUnSelectedStaff = new LinkedHashSet<>();
        }
        else
        {
            mUnSelectedStaff.clear();
        }
    }
}
