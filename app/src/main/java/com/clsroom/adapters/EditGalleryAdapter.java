package com.clsroom.adapters;

import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.clsroom.R;
import com.clsroom.listeners.IOnItemMovedListener;
import com.clsroom.model.Notes;
import com.clsroom.model.NotesImage;
import com.clsroom.viewholders.GalleryEditorItemViewHolder;

import java.util.ArrayList;
import java.util.Collections;

public class EditGalleryAdapter extends RecyclerView.Adapter<GalleryEditorItemViewHolder> implements IOnItemMovedListener
{
    private static final String TAG = "EditGalleryAdapter";
    private ArrayList mGalleryImagesList;

    public EditGalleryAdapter(ArrayList<Uri> galleryImagesList, Notes mNotes)
    {
        mGalleryImagesList = galleryImagesList;
        if (mNotes != null && mGalleryImagesList != null)
        {
            try
            {
                mGalleryImagesList.addAll(mNotes.getNotesImages());
            }
            catch (NullPointerException e)
            {
                Log.d(TAG, e.getMessage());
            }
        }
    }

    @Override
    public boolean onItemMoved(int fromPosition, int toPosition)
    {
        if (fromPosition < toPosition)
        {
            for (int i = fromPosition; i < toPosition; i++)
            {
                Collections.swap(mGalleryImagesList, i, i + 1);
            }
        }
        else
        {
            for (int i = fromPosition; i > toPosition; i--)
            {
                Collections.swap(mGalleryImagesList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        updateItemNumber();
        return true;
    }

    private void updateItemNumber()
    {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                notifyDataSetChanged();
            }
        }, 300);
    }

    @Override
    public GalleryEditorItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.edit_gallery_images_row, parent, false);
        return new GalleryEditorItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(GalleryEditorItemViewHolder viewHolder, final int position)
    {
        Object url = mGalleryImagesList.get(position);
        if (url instanceof NotesImage)
        {
            String urlTxt = ((NotesImage) url).url;
            viewHolder.mImageView.setImageURI(urlTxt);
        }
        else
        {
            viewHolder.mImageView.setImageURI((Uri) url);
        }

        viewHolder.mImageNumber.setText("" + (position + 1));

        viewHolder.mDeleteImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mGalleryImagesList.remove(position);
                notifyItemRemoved(position);
                updateItemNumber();
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return mGalleryImagesList.size();
    }

}
