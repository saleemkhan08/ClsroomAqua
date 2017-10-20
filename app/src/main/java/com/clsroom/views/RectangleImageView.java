package com.clsroom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RectangleImageView extends ImageView
{

    public RectangleImageView(Context context)
    {
        super(context);
    }

    public RectangleImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public RectangleImageView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth() / 2);
    }
}
