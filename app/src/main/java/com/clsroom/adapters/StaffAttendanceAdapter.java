package com.clsroom.adapters;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.clsroom.R;
import com.clsroom.model.Staff;
import com.clsroom.utils.ImageUtil;
import com.clsroom.viewholders.StudentViewHolder;

import java.util.ArrayList;

public class StaffAttendanceAdapter extends RecyclerView.Adapter<StudentViewHolder>
{
    private static final String TAG = "StaffAttendanceAdapter";
    private final AppCompatActivity mActivity;
    private ArrayList<Staff> mAbsentees;

    public StaffAttendanceAdapter(ArrayList<Staff> list, AppCompatActivity activity)
    {
        mActivity = activity;
        mAbsentees = new ArrayList<>();
        mAbsentees.addAll(list);
        Log.d(TAG, "AttendanceAdapter : " + mAbsentees);
    }

    @Override
    public StudentViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new StudentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.student_list_row, parent, false));
    }

    @Override
    public void onBindViewHolder(StudentViewHolder viewHolder, int position)
    {
        Staff model = mAbsentees.get(position);
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

    @Override
    public int getItemCount()
    {
        return mAbsentees.size();
    }
}
