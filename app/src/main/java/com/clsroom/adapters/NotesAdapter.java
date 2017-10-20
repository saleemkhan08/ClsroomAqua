package com.clsroom.adapters;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.clsroom.MainActivity;
import com.clsroom.R;
import com.clsroom.dialogs.NotificationDialogFragment;
import com.clsroom.fragments.AddOrEditNotesFragment;
import com.clsroom.listeners.ImageClickListener;
import com.clsroom.listeners.OnDismissListener;
import com.clsroom.model.Notes;
import com.clsroom.model.NotesClassifier;
import com.clsroom.model.NotesImage;
import com.clsroom.model.Progress;
import com.clsroom.model.ToastMsg;
import com.clsroom.utils.ImageUtil;
import com.clsroom.utils.NavigationDrawerUtil;
import com.clsroom.utils.Otto;
import com.clsroom.viewholders.NotesViewHolder;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.otto.Subscribe;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;

import static com.clsroom.utils.ActionBarUtil.SHOW_INDEPENDENT_NOTES_MENU;


public class NotesAdapter extends FirebaseRecyclerAdapter<Notes, NotesViewHolder>
{
    private static final String TAG = "NotesAdapter";
    private AppCompatActivity mActivity;
    private Query mNotesDbRef;
    private NotesClassifier mNotesClassifier;

    public static NotesAdapter getInstance(NotesClassifier classifier, DatabaseReference reference, AppCompatActivity activity)
    {
        Query ref = reference.orderByChild(Notes.DATE);
        NotesAdapter fragment = new NotesAdapter(Notes.class,
                R.layout.notes_row, NotesViewHolder.class, ref, activity);
        fragment.mNotesDbRef = ref;
        fragment.mNotesClassifier = classifier;
        return fragment;
    }

    private NotesAdapter(Class<Notes> modelClass, int modelLayout, Class<NotesViewHolder> viewHolderClass,
                         Query ref, AppCompatActivity activity)
    {
        super(modelClass, modelLayout, viewHolderClass, ref);
        Log.d(TAG, "TimeTableAdapter Constructor");
        mActivity = activity;
    }

    @Override
    protected void populateViewHolder(final NotesViewHolder viewHolder, final Notes model, int position)
    {
        final ArrayList<String> list = new ArrayList<>();
        for (NotesImage image : model.getNotesImages())
        {
            list.add(image.url);
        }
        if (mNotesClassifier.isReviewedNotesShown())
        {
            viewHolder.reviewButtonsContainer.setVisibility(View.GONE);
        }
        else
        {
            if (model.getNotesStatus() != null && model.getNotesStatus().equals(Notes.REJECTED))
            {
                viewHolder.rejectionText.setVisibility(View.VISIBLE);
                viewHolder.reviewButtonsContainer.setVisibility(View.GONE);
            }
            else
            {
                viewHolder.rejectionText.setVisibility(View.GONE);
                viewHolder.reviewButtonsContainer.setVisibility(View.VISIBLE);

                viewHolder.rejectButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        model.setNotesStatus(Notes.REJECTED);
                        showNotificationDialog(model, new OnDismissListener()
                        {
                            @Override
                            public void onDismiss()
                            {
                                updateRejectionStatus(model);
                            }
                        });

                    }
                });

                viewHolder.approveBtn.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        model.setNotesStatus(Notes.APPROVED);
                        showNotificationDialog(model, new OnDismissListener()
                        {
                            @Override
                            public void onDismiss()
                            {
                                FirebaseDatabase.getInstance().getReference().child(Notes.NOTES)
                                        .child(mNotesClassifier.getClassId()).child(mNotesClassifier.getSubjectId())
                                        .child(model.getDate()).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        mNotesDbRef.getRef().child(model.getDate()).removeValue();
                                    }
                                });
                            }
                        });
                    }
                });
            }
        }
        viewHolder.setClickListener(new ImageClickListener()
        {
            @Override
            public void onImageClick(int position)
            {
                GenericDraweeHierarchyBuilder hierarchyBuilder = GenericDraweeHierarchyBuilder
                        .newInstance(mActivity.getResources())
                        .setFailureImage(R.mipmap.broken_image)
                        .setProgressBarImage(new ProgressBarDrawable())
                        .setPlaceholderImage(R.mipmap.notebook_placeholder);

                new ImageViewer.Builder(mActivity, list)
                        .setStartPosition(position)
                        .setCustomDraweeHierarchyBuilder(hierarchyBuilder)
                        .show();
            }
        });
        viewHolder.dateTime.setText(model.displayDate());
        viewHolder.notesTitle.setText(model.getNotesTitle());
        viewHolder.notesDescription.setText(model.getNotesDescription());
        viewHolder.createrName.setText(model.getSubmitterName());
        ImageUtil.loadCircularImg(model.getSubmitterPhotoUrl(), viewHolder.createrImage);

        configureOptions(viewHolder, model);

        ArrayList<NotesImage> images = model.getNotesImages();
        switch (images.size())
        {
            case 0:
                break;
            case 1:
                viewHolder.singleImage.setVisibility(View.VISIBLE);
                ImageUtil.loadSquareImg(images.get(0).url, viewHolder.singleImage);
                break;
            case 2:
                viewHolder.dualImageContainer.setVisibility(View.VISIBLE);
                ImageUtil.loadSquareImg(images.get(0).url, viewHolder.dualImage1);
                ImageUtil.loadSquareImg(images.get(1).url, viewHolder.dualImage2);
                break;
            case 3:
                viewHolder.tripleImageContainer.setVisibility(View.VISIBLE);
                ImageUtil.loadSquareImg(images.get(0).url, viewHolder.tripleImage1);
                ImageUtil.loadSquareImg(images.get(1).url, viewHolder.tripleImage2);
                ImageUtil.loadSquareImg(images.get(2).url, viewHolder.tripleImage3);
                break;
            default:
                viewHolder.additionalImageCount.setVisibility(View.VISIBLE);
                viewHolder.additionalImageCount.setText("+" + (images.size() - 4));
            case 4:
                viewHolder.quadImageContainer.setVisibility(View.VISIBLE);
                ImageUtil.loadSquareImg(images.get(0).url, viewHolder.quadImage1);
                ImageUtil.loadSquareImg(images.get(1).url, viewHolder.quadImage2);
                ImageUtil.loadSquareImg(images.get(2).url, viewHolder.quadImage3);
                ImageUtil.loadSquareImg(images.get(3).url, viewHolder.quadImage4);
                break;
        }
    }

    private void updateRejectionStatus(Notes notes)
    {
        mNotesDbRef.getRef().child(notes.getDate()).setValue(notes);
    }

    private void showNotificationDialog(Notes model, OnDismissListener listener)
    {
        NotificationDialogFragment.getInstance(model, listener)
                .show(mActivity.getSupportFragmentManager(),
                        NotificationDialogFragment.TAG);
    }

    @Subscribe
    public void reload(String str)
    {
        Log.d(TAG, "reload : " + str);
        if (str.equals(SHOW_INDEPENDENT_NOTES_MENU))
        {
            notifyDataSetChanged();
            Otto.unregister(this);
        }
    }

    private void configureOptions(final NotesViewHolder holder, final Notes notes)
    {
        if (NavigationDrawerUtil.mCurrentUser.getUserId()
                .equals(notes.getSubmitterId()) || !NavigationDrawerUtil.isStudent)
        {
            holder.optionsIconContainer.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.optionsIconContainer.setVisibility(View.GONE);
        }
        holder.optionsIconContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                PopupMenu popup = new PopupMenu(mActivity, v);
                popup.getMenuInflater()
                        .inflate(R.menu.classes_options, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    public boolean onMenuItemClick(MenuItem item)
                    {
                        switch (item.getItemId())
                        {
                            case R.id.action_edit:
                                editClasses(notes);
                                break;
                            case R.id.action_delete:
                                confirmDelete(notes);
                                break;
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });
    }

    private void confirmDelete(final Notes notes)
    {
        Progress.show(R.string.deleting);
        StorageReference notesStorageRef = FirebaseStorage.getInstance().getReference()
                .child(Notes.NOTES).child(mNotesClassifier.getClassId())
                .child(mNotesClassifier.getSubjectId());
        notesStorageRef.child(notes.getDate()).delete().addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                mNotesDbRef.getRef().child(notes.getDate()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        Progress.hide();
                        ToastMsg.show(R.string.deleted);
                    }
                });
            }
        });
    }

    private void editClasses(Notes notes)
    {
        ((MainActivity) mActivity).showFragment(AddOrEditNotesFragment.getInstance(notes, mNotesClassifier),
                true, AddOrEditNotesFragment.TAG);
    }
}