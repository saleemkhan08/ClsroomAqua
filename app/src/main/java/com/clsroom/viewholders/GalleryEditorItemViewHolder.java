package com.clsroom.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.clsroom.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class GalleryEditorItemViewHolder extends RecyclerView.ViewHolder
{
    @Bind(R.id.image)
    public ImageView mImageView;

    @Bind(R.id.imageNumber)
    public TextView mImageNumber;

    @Bind(R.id.deleteImage)
    public ImageView mDeleteImage;

    public GalleryEditorItemViewHolder(View itemView)
    {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
