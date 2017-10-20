package com.clsroom.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.clsroom.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ClassesViewHolder extends RecyclerView.ViewHolder
{
    @Bind(R.id.className)
    public TextView mClassName;

    @Bind(R.id.classTeacher)
    public TextView mClassTeacher;

    @Bind(R.id.studentCount)
    public TextView mStudentCount;

    @Bind(R.id.optionsIconContainer)
    public ImageView optionsIconContainer;

    public View mItemView;

    public ClassesViewHolder(View itemView)
    {
        super(itemView);
        mItemView = itemView;
        ButterKnife.bind(this, itemView);
    }
}