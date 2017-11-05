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

public class SubjectViewHolder extends RecyclerView.ViewHolder
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

    public View mItemView;

    public SubjectViewHolder(View itemView)
    {
        super(itemView);
        mItemView = itemView;
        ButterKnife.bind(this, itemView);
    }
}