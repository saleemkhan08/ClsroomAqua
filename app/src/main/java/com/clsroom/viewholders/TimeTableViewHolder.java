package com.clsroom.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.clsroom.R;
import com.clsroom.views.SquareImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TimeTableViewHolder extends RecyclerView.ViewHolder
{
    @BindView(R.id.teacherImage)
    public SquareImageView mTeacherImage;

    @BindView(R.id.subjectName)
    public TextView mSubjectName;

    @BindView(R.id.teacherName)
    public TextView mClassTeacherName;

    @BindView(R.id.checkbox)
    public CheckBox mCheckBox;

    @BindView(R.id.optionsIconContainer)
    public ImageView mOptionsIconContainer;

    @BindView(R.id.periodTime)
    public TextView mPeriodTime;

    @BindView(R.id.innerContainer)
    public View mItemView;

    public TimeTableViewHolder(View itemView)
    {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}