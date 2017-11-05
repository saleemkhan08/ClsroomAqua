package com.clsroom.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.clsroom.R;
import com.clsroom.views.SquareImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RequestedLeavesViewHolder extends RecyclerView.ViewHolder
{
    @BindView(R.id.userImage)
    public SquareImageView mImageView;

    @BindView(R.id.fullName)
    public TextView mFullName;

    @BindView(R.id.userId)
    public TextView requestMessage;

    @BindView(R.id.designation)
    public TextView mDesignation;

    public View mItemView;

    @BindView(R.id.optionsIconContainer)
    public View optionsIconContainer;

    @BindView(R.id.statusIcon)
    public ImageView statusIcon;

    public RequestedLeavesViewHolder(View itemView)
    {
        super(itemView);
        mItemView = itemView;
        ButterKnife.bind(this, itemView);
    }
}