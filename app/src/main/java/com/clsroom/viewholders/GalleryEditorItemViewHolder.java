package com.clsroom.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.clsroom.R;
import com.clsroom.views.SquareImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GalleryEditorItemViewHolder extends RecyclerView.ViewHolder
{
    @BindView(R.id.image)
    public SquareImageView mImageView;

    @BindView(R.id.imageNumber)
    public TextView mImageNumber;

    @BindView(R.id.deleteImage)
    public ImageView mDeleteImage;

    public GalleryEditorItemViewHolder(View itemView)
    {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
