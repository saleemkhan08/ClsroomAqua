package com.clsroom.adapters;

import android.util.Log;
import android.view.View;

import com.clsroom.R;
import com.clsroom.model.Students;
import com.clsroom.utils.ImageUtil;
import com.clsroom.viewholders.StudentViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class AttendanceFbAdapter extends FirebaseRecyclerAdapter<Students, StudentViewHolder>
{
    private static final String TAG = "AttendanceFbAdapter";

    public static AttendanceFbAdapter getInstance(DatabaseReference reference)
    {
        Log.d(TAG, "AttendanceFbAdapter getInstance: reference : " + reference);
        return new AttendanceFbAdapter(Students.class,
                R.layout.student_list_row, StudentViewHolder.class, reference);
    }

    private AttendanceFbAdapter(Class<Students> modelClass, int modelLayout, Class<StudentViewHolder> viewHolderClass,
                                Query ref)
    {
        super(modelClass, modelLayout, viewHolderClass, ref);
        Log.d(TAG, "StudentsAdapter Constructor");
    }

    @Override
    protected void populateViewHolder(final StudentViewHolder viewHolder, final Students model, int position)
    {
        Log.d(TAG, "populateViewHolder : " + position);
        String imageUrl = model.getPhotoUrl();
        ImageUtil.loadCircularImg(viewHolder.itemView.getContext(), imageUrl, viewHolder.mImageView);
        viewHolder.mFullName.setText(model.getFullName());
        viewHolder.mUserId.setText(model.getUserId());
        viewHolder.mCheckBox.setVisibility(View.GONE);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "onClick");
            }
        });
    }
}