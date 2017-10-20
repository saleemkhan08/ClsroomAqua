package com.clsroom.adapters;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import com.clsroom.R;
import com.clsroom.model.Students;
import com.clsroom.utils.ActionBarUtil;
import com.clsroom.utils.ImageUtil;
import com.clsroom.utils.Otto;
import com.clsroom.viewholders.StudentViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.clsroom.utils.ActionBarUtil.SHOW_INDEPENDENT_STUDENTS_MENU;

public class StudentsResultsAdapter extends FirebaseRecyclerAdapter<Students, StudentViewHolder>
{
    private static final String TAG = "StudentsResultsAdapter";
    private Activity mActivity;
    public static boolean isSelectionEnabled;
    public ArrayList<String> mSelectedStudents;
    public Map<String, Students> mUnSelectedStudents;

    private boolean isSelectAll;

    public static StudentsResultsAdapter getInstance(DatabaseReference reference, Activity activity)
    {
        Log.d(TAG, "StudentsAdapter getInstance: reference : " + reference);
        return new StudentsResultsAdapter(Students.class,
                R.layout.student_list_row, StudentViewHolder.class, reference, activity);
    }

    private StudentsResultsAdapter(Class<Students> modelClass, int modelLayout, Class<StudentViewHolder> viewHolderClass,
                                   Query ref, Activity activity)
    {
        super(modelClass, modelLayout, viewHolderClass, ref);
        Log.d(TAG, "StudentsAdapter Constructor");
        mActivity = activity;
    }

    @Override
    protected void populateViewHolder(final StudentViewHolder viewHolder, final Students model, int position)
    {
        Log.d(TAG, "populateViewHolder : " + position);
        String imageUrl = model.getPhotoUrl();
        ImageUtil.loadCircularImg(viewHolder.itemView.getContext(), imageUrl, viewHolder.mImageView);

        viewHolder.mFullName.setText(model.getFullName());
        viewHolder.mUserId.setText(model.getUserId());
        Log.d(TAG, this + " : isSelectionEnabled : " + isSelectionEnabled);
        viewHolder.mCheckBox.setVisibility(isSelectionEnabled ? View.VISIBLE : View.GONE);
        viewHolder.mCheckBox.setChecked(isSelectAll);
        viewHolder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked)
            {
                Log.d(TAG, "onCheckedChanged : " + isChecked);
                if (isChecked)
                {
                    mSelectedStudents.add(model.getUserId());
                    mUnSelectedStudents.remove(model.getUserId());
                }
                else
                {
                    mSelectedStudents.remove(model.getUserId());
                    mUnSelectedStudents.put(model.getUserId(), model);
                }
            }
        });
        viewHolder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "onClick");
            }
        });

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

    private void enableSelection()
    {
        isSelectionEnabled = true;
        mSelectedStudents = new ArrayList<>();
        mUnSelectedStudents = new HashMap<>();
        Otto.register(StudentsResultsAdapter.this);
        notifyDataSetChanged();
    }

    @Subscribe
    public void reload(String str)
    {
        Log.d(TAG, "reload : " + str);
        if (str.equals(SHOW_INDEPENDENT_STUDENTS_MENU))
        {
            notifyDataSetChanged();
            Otto.unregister(this);
        }
    }

    public void enableAttendance()
    {
        isSelectAll = true;
        Otto.post(ActionBarUtil.SHOW_ATTENDANCE_MENU);
        enableSelection();

    }

    public void setSelectAll()
    {
        isSelectAll = !isSelectAll;
        notifyDataSetChanged();
    }
}