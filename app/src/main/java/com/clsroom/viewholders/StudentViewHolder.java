package com.clsroom.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.clsroom.R;
import com.clsroom.views.SquareImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StudentViewHolder extends RecyclerView.ViewHolder
{
    @BindView(R.id.userImage)
    public SquareImageView mImageView;

    @BindView(R.id.fullName)
    public TextView mFullName;

    @BindView(R.id.userId)
    public TextView mUserId;

    @BindView(R.id.checkbox)
    public CheckBox mCheckBox;

    public View mItemView;

    @BindView(R.id.optionsIconContainer)
    public View optionsIconContainer;

    public StudentViewHolder(View itemView)
    {
        super(itemView);
        mItemView = itemView;
        ButterKnife.bind(this, itemView);
    }
}