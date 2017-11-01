package com.clsroom.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.clsroom.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class StudentViewHolder extends RecyclerView.ViewHolder
{
    @Bind(R.id.userImage)
    public ImageView mImageView;

    @Bind(R.id.fullName)
    public TextView mFullName;

    @Bind(R.id.userId)
    public TextView mUserId;

    @Bind(R.id.checkbox)
    public CheckBox mCheckBox;

    public View mItemView;

    @Bind(R.id.optionsIconContainer)
    public View optionsIconContainer;

    public StudentViewHolder(View itemView)
    {
        super(itemView);
        mItemView = itemView;
        ButterKnife.bind(this, itemView);
    }
}