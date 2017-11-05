package com.clsroom.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.clsroom.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StudentResultViewHolder extends RecyclerView.ViewHolder
{
    @BindView(R.id.userImage)
    public ImageView mImageView;

    @BindView(R.id.fullName)
    public TextView mFullName;


    public View mItemView;

    public StudentResultViewHolder(View itemView)
    {
        super(itemView);
        mItemView = itemView;
        ButterKnife.bind(this, itemView);
    }
}