package com.clsroom.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.clsroom.R;
import com.clsroom.views.SquareImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StaffViewHolder extends RecyclerView.ViewHolder
{
    @BindView(R.id.userImage)
    public SquareImageView mImageView;

    @BindView(R.id.admin)
    public View mAdminImageView;

    @BindView(R.id.fullName)
    public TextView mFullName;

    @BindView(R.id.userId)
    public TextView mUserId;

    @BindView(R.id.designation)
    public TextView mDesignation;

    @BindView(R.id.checkbox)
    public CheckBox mCheckBox;

    public View mItemView;

    @BindView(R.id.optionsIconContainer)
    public View optionsIconContainer;

    public StaffViewHolder(View itemView)
    {
        super(itemView);
        mItemView = itemView;
        ButterKnife.bind(this, itemView);
    }
}