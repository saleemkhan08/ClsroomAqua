package com.clsroom.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.clsroom.R;
import com.clsroom.listeners.ImageClickListener;
import com.clsroom.model.Snack;
import com.clsroom.utils.ConnectivityUtil;
import com.clsroom.views.RectangleImageView;
import com.clsroom.views.SquareImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NotesViewHolder extends RecyclerView.ViewHolder
{
    private ImageClickListener mListener;

    @BindView(R.id.singleImage)
    public SquareImageView singleImage;

    @BindView(R.id.dualImageContainer)
    public View dualImageContainer;

    @BindView(R.id.dualImage1)
    public RectangleImageView dualImage1;

    @BindView(R.id.dualImage2)
    public RectangleImageView dualImage2;

    @BindView(R.id.tripleImageContainer)
    public View tripleImageContainer;

    @BindView(R.id.tripleImage1)
    public SquareImageView tripleImage1;

    @BindView(R.id.tripleImage2)
    public SquareImageView tripleImage2;

    @BindView(R.id.tripleImage3)
    public RectangleImageView tripleImage3;

    @BindView(R.id.quadImageContainer)
    public View quadImageContainer;

    @BindView(R.id.quadImage1)
    public SquareImageView quadImage1;

    @BindView(R.id.quadImage2)
    public SquareImageView quadImage2;

    @BindView(R.id.quadImage3)
    public SquareImageView quadImage3;

    @BindView(R.id.quadImage4)
    public SquareImageView quadImage4;

    @BindView(R.id.createrImage)
    public SquareImageView createrImage;

    @BindView(R.id.notesTitle)
    public TextView notesTitle;

    @BindView(R.id.createrName)
    public TextView createrName;

    @BindView(R.id.optionsIconContainer)
    public ImageView optionsIconContainer;

    @BindView(R.id.additionalImageCount)
    public TextView additionalImageCount;

    @BindView(R.id.notesDescription)
    public TextView notesDescription;

    @BindView(R.id.dateTime)
    public TextView dateTime;

    @BindView(R.id.reviewButtonsContainer)
    public View reviewButtonsContainer;

    @BindView(R.id.approveBtn)
    public View approveBtn;

    @BindView(R.id.rejectButton)
    public View rejectButton;

    public View mItemView;

    @BindView(R.id.rejectionText)
    public View rejectionText;

    @BindView(R.id.reviewComment)
    public TextView reviewComment;

    public NotesViewHolder(View itemView)
    {
        super(itemView);
        mItemView = itemView;
        ButterKnife.bind(this, itemView);
    }

    public void setClickListener(ImageClickListener listener)
    {
        mListener = listener;
    }

    @OnClick({R.id.singleImage, R.id.dualImage1, R.id.tripleImage1, R.id.quadImage1})
    void onFirstImageClick(View view)
    {
        if (ConnectivityUtil.isConnected(mItemView.getContext()))
        {
            mListener.onImageClick(0, view);
        }
        else
        {
            Snack.show(R.string.noInternet);
        }

    }

    @OnClick({R.id.dualImage2, R.id.tripleImage2, R.id.quadImage2})
    void onSecondImageClick(View view)
    {
        if (ConnectivityUtil.isConnected(mItemView.getContext()))
        {
            mListener.onImageClick(1, view);
        }
        else
        {
            Snack.show(R.string.noInternet);
        }
    }

    @OnClick({R.id.tripleImage3, R.id.quadImage3})
    void onThirdImageClick(View view)
    {
        if (ConnectivityUtil.isConnected(mItemView.getContext()))
        {
            mListener.onImageClick(2, view);
        }
        else
        {
            Snack.show(R.string.noInternet);
        }
    }

    @OnClick(R.id.quadImage4)
    void onFourthImageClick(View view)
    {
        if (ConnectivityUtil.isConnected(mItemView.getContext()))
        {
            mListener.onImageClick(3, view);
        }
        else
        {
            Snack.show(R.string.noInternet);
        }
    }

}