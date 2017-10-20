package com.clsroom.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.clsroom.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RequestedLeavesViewHolder extends RecyclerView.ViewHolder
{
    @Bind(R.id.userImage)
    public ImageView mImageView;

    @Bind(R.id.fullName)
    public TextView mFullName;

    @Bind(R.id.userId)
    public TextView requestMessage;

    @Bind(R.id.designation)
    public TextView mDesignation;

    public View mItemView;

    @Bind(R.id.optionsIconContainer)
    public View optionsIconContainer;

    @Bind(R.id.statusIcon)
    public ImageView statusIcon;

    public RequestedLeavesViewHolder(View itemView)
    {
        super(itemView);
        mItemView = itemView;
        ButterKnife.bind(this, itemView);
    }
}