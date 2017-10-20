package com.clsroom.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.clsroom.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NotificationViewHolder extends RecyclerView.ViewHolder
{
    @Bind(R.id.senderImage)
    public ImageView senderImage;

    @Bind(R.id.senderName)
    public TextView senderName;

    @Bind(R.id.message)
    public TextView message;

    @Bind(R.id.dateTime)
    public TextView dateTime;

    @Bind(R.id.deleteNotification)
    public View deleteNotification;

    public View mItemView;

    public NotificationViewHolder(View itemView)
    {
        super(itemView);
        mItemView = itemView;
        ButterKnife.bind(this, itemView);
    }
}