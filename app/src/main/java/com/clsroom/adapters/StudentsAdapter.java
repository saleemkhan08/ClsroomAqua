package com.clsroom.adapters;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import com.clsroom.R;
import com.clsroom.dialogs.NotificationDialogFragment;
import com.clsroom.dialogs.ResetPasswordDialogFragment;
import com.clsroom.fragments.ProfileFragment;
import com.clsroom.listeners.FragmentLauncher;
import com.clsroom.listeners.OnDismissListener;
import com.clsroom.model.Progress;
import com.clsroom.model.Students;
import com.clsroom.model.ToastMsg;
import com.clsroom.utils.ActionBarUtil;
import com.clsroom.utils.NavigationUtil;
import com.clsroom.utils.Otto;
import com.clsroom.viewholders.StudentViewHolder;
import com.clsroom.views.DetailsTransition;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.squareup.otto.Subscribe;

import java.util.LinkedHashSet;

import static com.clsroom.utils.ActionBarUtil.SHOW_STUDENTS_MENU_FOR_ADMIN;

public class StudentsAdapter extends FirebaseRecyclerAdapter<Students, StudentViewHolder>
{
    private static final String TAG = "StudentsAdapter";
    public static boolean isSelectionEnabled;
    public LinkedHashSet<Students> mSelectedStudents;
    public LinkedHashSet<Students> mUnSelectedStudents;
    private FragmentLauncher launcher;
    private DatabaseReference mStudentDbReference;
    private boolean isSelectAll;

    public static StudentsAdapter getInstance(DatabaseReference reference, FragmentLauncher launcher)
    {
        Log.d(TAG, "StudentsAdapter getInstance: reference : " + reference);
        StudentsAdapter adapter = new StudentsAdapter(Students.class,
                R.layout.student_list_row, StudentViewHolder.class, reference);
        adapter.launcher = launcher;
        adapter.mStudentDbReference = reference;
        return adapter;
    }

    private StudentsAdapter(Class<Students> modelClass, int modelLayout, Class<StudentViewHolder> viewHolderClass,
                            Query ref)
    {
        super(modelClass, modelLayout, viewHolderClass, ref);
        Log.d(TAG, "StudentsAdapter Constructor");
    }

    @Override
    protected void populateViewHolder(final StudentViewHolder viewHolder, final Students model, int position)
    {
        viewHolder.mImageView.setImageURI(model.getPhotoUrl());
        viewHolder.mFullName.setText(model.getFullName());
        viewHolder.mUserId.setText(model.getUserId());

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

        if (mSelectedStudents != null)
        {
            viewHolder.mCheckBox.setChecked(mSelectedStudents.contains(model));
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
                Log.d("relaunchIssue", "studentAdapter : onClick");
                ProfileFragment fragment2 = ProfileFragment.getInstance(model);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                    fragment2.setSharedElementEnterTransition(new DetailsTransition());
                    fragment2.setSharedElementReturnTransition(new DetailsTransition());

                    viewHolder.mImageView.setTransitionName(model.getUserId());
                    launcher.addFragment(fragment2, true, ProfileFragment.TAG, viewHolder.mImageView, ProfileFragment.PROFILE_IMAGE);
                }
                else
                {
                    launcher.addFragment(fragment2, true, ProfileFragment.TAG);
                }
            }
        });
        if (NavigationUtil.isAdmin)
        {
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View view)
                {
                    Log.d(TAG, this + ", onLongClick, isSelectionEnabled : " + isSelectionEnabled);
                    Otto.post(ActionBarUtil.SHOW_MULTIPLE_STUDENT_MENU);
                    enableSelection();
                    return true;
                }
            });
        }
        configureOptions(viewHolder, model);
    }

    private void configureOptions(final StudentViewHolder holder, final Students students)
    {
        holder.optionsIconContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                PopupMenu popup = new PopupMenu(launcher.getActivity(), v);
                popup.getMenuInflater()
                        .inflate(R.menu.student_options, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    public boolean onMenuItemClick(MenuItem item)
                    {
                        switch (item.getItemId())
                        {
                            case R.id.action_delete:
                                confirmDelete(students);
                                break;
                            case R.id.reset_password:
                                resetPassword(students);
                                break;
                            case R.id.send_notification:
                                sendNotification(students);
                                break;
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });
    }

    private void confirmDelete(Students students)
    {
        Progress.show(R.string.deleting);
        mStudentDbReference.child(students.getUserId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
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

    private void sendNotification(Students students)
    {
        NotificationDialogFragment.getInstance(students, new OnDismissListener()
        {
            @Override
            public void onDismiss(String msg)
            {
                if (!TextUtils.isEmpty(msg))
                {
                    ToastMsg.show(R.string.sent);
                }
            }
        }).show(launcher.getSupportFragmentManager(), NotificationDialogFragment.TAG);
    }

    private void resetPassword(Students students)
    {
        ResetPasswordDialogFragment.getInstance(mStudentDbReference.child(students.getUserId()))
                .show(launcher.getSupportFragmentManager(), ResetPasswordDialogFragment.TAG);
    }

    private void updateSelection(boolean isChecked, Students model)
    {
        if (isChecked)
        {
            mSelectedStudents.add(model);
            mUnSelectedStudents.remove(model);
        }
        else
        {
            mSelectedStudents.remove(model);
            mUnSelectedStudents.add(model);
        }
    }

    private void enableSelection()
    {
        isSelectionEnabled = true;
        clearSet();
        Otto.register(StudentsAdapter.this);
        notifyDataSetChanged();
    }

    @Subscribe
    public void reload(String str)
    {
        Log.d(TAG, "reload : " + str);
        if (str.equals(SHOW_STUDENTS_MENU_FOR_ADMIN))
        {
            notifyDataSetChanged();
            Otto.unregister(this);
        }
    }

    public void enableAttendance(LinkedHashSet<Students> studentsSet)
    {
        Otto.post(ActionBarUtil.SHOW_ATTENDANCE_MENU);
        isSelectionEnabled = true;
        clearSet();
        mSelectedStudents.addAll(studentsSet);
        Otto.register(StudentsAdapter.this);
        notifyDataSetChanged();

    }

    public void setSelectAll(LinkedHashSet<Students> studentsSet)
    {
        isSelectAll = !isSelectAll;
        clearSet();
        if (isSelectAll)
        {
            mSelectedStudents.addAll(studentsSet);
        }
        else
        {
            mUnSelectedStudents.addAll(studentsSet);
        }
        notifyDataSetChanged();
    }

    private void clearSet()
    {
        if (mSelectedStudents == null)
        {
            mSelectedStudents = new LinkedHashSet<>();
        }
        else
        {
            mSelectedStudents.clear();
        }

        if (mUnSelectedStudents == null)
        {
            mUnSelectedStudents = new LinkedHashSet<>();
        }
        else
        {
            mUnSelectedStudents.clear();
        }
    }
}