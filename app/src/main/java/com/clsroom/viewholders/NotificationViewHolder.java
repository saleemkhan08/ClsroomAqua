package com.clsroom.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.clsroom.R;
import com.clsroom.views.SquareImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationViewHolder extends RecyclerView.ViewHolder
{
    @BindView(R.id.senderImage)
    public SquareImageView senderImage;

    @BindView(R.id.senderName)
    public TextView senderName;

    @BindView(R.id.message)
    public TextView message;

    @BindView(R.id.dateTime)
    public TextView dateTime;

    @BindView(R.id.deleteNotification)
    public View deleteNotification;

    public View mItemView;

    public NotificationViewHolder(View itemView)
    {
        super(itemView);
        mItemView = itemView;
        ButterKnife.bind(this, itemView);
    }
}