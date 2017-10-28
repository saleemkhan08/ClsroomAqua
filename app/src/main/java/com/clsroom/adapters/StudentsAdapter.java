package com.clsroom.adapters;

import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import com.clsroom.R;
import com.clsroom.fragments.ProfileFragment;
import com.clsroom.fragments.StudentsListFragment;
import com.clsroom.listeners.FragmentLauncher;
import com.clsroom.model.Students;
import com.clsroom.utils.ActionBarUtil;
import com.clsroom.utils.ImageUtil;
import com.clsroom.utils.NavigationUtil;
import com.clsroom.utils.Otto;
import com.clsroom.viewholders.StudentViewHolder;
import com.clsroom.views.DetailsTransition;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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
    private boolean isSelectAll;

    public static StudentsAdapter getInstance(DatabaseReference reference, FragmentLauncher launcher)
    {
        Log.d(TAG, "StudentsAdapter getInstance: reference : " + reference);
        StudentsAdapter adapter = new StudentsAdapter(Students.class,
                R.layout.student_list_row, StudentViewHolder.class, reference);
        adapter.launcher = launcher;
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
        String imageUrl = model.getPhotoUrl();
        ImageUtil.loadCircularImg(viewHolder.itemView.getContext(), imageUrl, viewHolder.mImageView);

        viewHolder.mFullName.setText(model.getFullName());
        viewHolder.mUserId.setText(model.getUserId());

        viewHolder.mCheckBox.setVisibility(isSelectionEnabled ? View.VISIBLE : View.GONE);
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
                StudentsListFragment fragment1 = (StudentsListFragment) launcher.getFragment();
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