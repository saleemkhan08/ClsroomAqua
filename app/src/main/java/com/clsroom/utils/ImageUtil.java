package com.clsroom.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.clsroom.R;

public class ImageUtil
{
    private static final String TAG = "ImageUtil";

    public static void loadCircularImg(final Context context, String url, final ImageView imageView)
    {
        try
        {
            Glide.with(context).load(url).asBitmap().placeholder(R.mipmap.user_icon_accent)
                    .centerCrop().into(new BitmapImageViewTarget(imageView)
            {
                @Override
                protected void setResource(Bitmap resource)
                {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    imageView.setImageDrawable(circularBitmapDrawable);
                }
            });
        }
        catch (Exception e)
        {
            Log.d(TAG, e.getMessage());
        }
    }

    public static void loadCircularImg(String url, final ImageView imageView)
    {
        try
        {
            Glide.with(imageView.getContext()).load(url).asBitmap().placeholder(R.mipmap.user_icon_accent)
                    .centerCrop().into(new BitmapImageViewTarget(imageView)
            {
                @Override
                protected void setResource(Bitmap resource)
                {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(imageView.getContext().getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    imageView.setImageDrawable(circularBitmapDrawable);
                }
            });
        }
        catch (Exception e)
        {
            Log.d(TAG, e.getMessage());
        }
    }

    public static void loadImg(final Context context, Uri url, final ImageView imageView)
    {
        try
        {
            Glide.with(context).load(url)
                    .asBitmap().placeholder(R.drawable.placeholder)
                    .centerCrop().into(imageView);
        }
        catch (Exception e)
        {
            Log.d(TAG, e.getMessage());
        }
    }

    public static void loadImg(final Context context, String url, final ImageView imageView)
    {
        try
        {
            Glide.with(context).load(url)
                    .asBitmap().placeholder(R.drawable.placeholder)
                    .centerCrop().into(imageView);
        }
        catch (Exception e)
        {
            Log.d(TAG, e.getMessage());
        }
    }

    public static void loadSquareImg(String url, final ImageView imageView)
    {
        try
        {
            Glide.with(imageView.getContext()).load(url)
                    .asBitmap().placeholder(R.drawable.placeholder)
                    .centerCrop().into(imageView);
        }
        catch (Exception e)
        {
            Log.d(TAG, e.getMessage());
        }
    }


}
