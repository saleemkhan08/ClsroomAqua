package com.clsroom.adapters;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.clsroom.R;
import com.clsroom.dialogs.NotificationDialogFragment;
import com.clsroom.fragments.AddOrEditNotesFragment;
import com.clsroom.fragments.ProfileFragment;
import com.clsroom.fragments.SingleNotesFragment;
import com.clsroom.listeners.FragmentLauncher;
import com.clsroom.listeners.ImageClickListener;
import com.clsroom.listeners.OnDismissListener;
import com.clsroom.model.Notes;
import com.clsroom.model.NotesClassifier;
import com.clsroom.model.NotesImage;
import com.clsroom.model.Progress;
import com.clsroom.model.Staff;
import com.clsroom.model.Students;
import com.clsroom.model.ToastMsg;
import com.clsroom.model.User;
import com.clsroom.utils.ImageUtil;
import com.clsroom.utils.NavigationUtil;
import com.clsroom.utils.Otto;
import com.clsroom.viewholders.NotesViewHolder;
import com.clsroom.views.DetailsTransition;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.otto.Subscribe;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;
import java.util.List;

import static com.clsroom.utils.ActionBarUtil.SHOW_INDEPENDENT_NOTES_MENU;

public class NotesAdapter extends FirebaseRecyclerAdapter<Notes, NotesViewHolder> implements View.OnClickListener, ImageClickListener
{
    private static final String TAG = "NotesAdapter";
    private FragmentLauncher launcher;
    private Query mNotesDbRef;
    private NotesClassifier mNotesClassifier;
    private String notesStatus;

    public static NotesAdapter getInstance(NotesClassifier classifier, DatabaseReference reference, FragmentLauncher launcher)
    {
        Query ref = reference.orderByKey();
        String status;
        if (reference.toString().contains(Notes.REVIEW))
        {
            status = Notes.PENDING;
            if (NavigationUtil.isStudent)
            {
                ref = reference.orderByChild(Notes.SUBMITTER_ID).startAt(NavigationUtil.userId).endAt(NavigationUtil.userId);
            }
        }
        else
        {
            ref = reference.getRef().orderByChild(Notes.DATE_TIME);
            status = Notes.REVIEWED;
        }
        NotesAdapter fragment = new NotesAdapter(Notes.class,
                R.layout.notes_row, NotesViewHolder.class, ref, launcher);
        fragment.mNotesDbRef = ref;
        fragment.notesStatus = status;
        fragment.mNotesClassifier = classifier;
        return fragment;
    }

    private NotesAdapter(Class<Notes> modelClass, int modelLayout, Class<NotesViewHolder> viewHolderClass,
                         Query ref, FragmentLauncher launcher)
    {
        super(modelClass, modelLayout, viewHolderClass, ref);
        Log.d(TAG, "TimeTableAdapter Constructor");
        this.launcher = launcher;
    }

    @Override
    protected void populateViewHolder(final NotesViewHolder viewHolder, final Notes model, int position)
    {
        final ArrayList<String> list = new ArrayList<>();
        for (NotesImage image : model.getNotesImages())
        {
            list.add(image.url);
        }
        handleReviewOptions(viewHolder, model);
        viewHolder.setClickListener(this);

        viewHolder.itemView.setOnClickListener(this);
        viewHolder.itemView.setClickable(true);
        viewHolder.itemView.setTag(model);


        viewHolder.dateTime.setText(model.displayDate());
        viewHolder.notesTitle.setText(model.getNotesTitle());
        viewHolder.notesDescription.setText(model.getNotesDescription());
        configureOptions(viewHolder, model);
        User.getRef(model.getSubmitterId()).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                User user;
                if (dataSnapshot.hasChild(Staff.IS_ADMIN))
                {
                    user = dataSnapshot.getValue(Staff.class);
                }
                else
                {
                    user = dataSnapshot.getValue(Students.class);
                }
                if (user != null)
                {
                    viewHolder.createrName.setText(user.getFullName());
                    ImageUtil.loadCircularImg(user.getPhotoUrl(), viewHolder.createrImage);

                    viewHolder.createrImage.setOnClickListener(NotesAdapter.this);
                    viewHolder.createrImage.setTag(user);

                }
                else
                {
                    viewHolder.createrName.setText(R.string.unknown_user);
                    ImageUtil.loadCircularImg("", viewHolder.createrImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                viewHolder.createrName.setText(R.string.unknown_user);
                ImageUtil.loadCircularImg("", viewHolder.createrImage);
            }
        });

        ArrayList<NotesImage> images = model.getNotesImages();
        switch (images.size())
        {
            case 0:
                break;
            case 1:
                viewHolder.singleImage.setVisibility(View.VISIBLE);
                ImageUtil.loadSquareImg(images.get(0).url, viewHolder.singleImage);
                viewHolder.singleImage.setTag(list);
                break;
            case 2:
                viewHolder.dualImageContainer.setVisibility(View.VISIBLE);
                ImageUtil.loadSquareImg(images.get(0).url, viewHolder.dualImage1);
                ImageUtil.loadSquareImg(images.get(1).url, viewHolder.dualImage2);
                viewHolder.dualImage1.setTag(list);
                viewHolder.dualImage2.setTag(list);
                break;
            case 3:
                viewHolder.tripleImageContainer.setVisibility(View.VISIBLE);
                ImageUtil.loadSquareImg(images.get(0).url, viewHolder.tripleImage1);
                ImageUtil.loadSquareImg(images.get(1).url, viewHolder.tripleImage2);
                ImageUtil.loadSquareImg(images.get(2).url, viewHolder.tripleImage3);
                viewHolder.tripleImage1.setTag(list);
                viewHolder.tripleImage2.setTag(list);
                viewHolder.tripleImage3.setTag(list);
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
                viewHolder.quadImage1.setTag(list);
                viewHolder.quadImage2.setTag(list);
                viewHolder.quadImage3.setTag(list);
                viewHolder.quadImage4.setTag(list);
                break;
        }
    }

    private void handleReviewOptions(final NotesViewHolder viewHolder, final Notes model)
    {
        viewHolder.reviewComment.setVisibility(View.GONE);
        if (mNotesClassifier.isReviewedNotesShown())
        {
            viewHolder.reviewButtonsContainer.setVisibility(View.GONE);
        }
        else
        {
            if (model.getNotesStatus() != null && model.getNotesStatus().equals(Notes.REJECTED))
            {
                if (NavigationUtil.isStudent)
                {
                    viewHolder.reviewComment.setText(model.getReviewComment());
                    viewHolder.reviewButtonsContainer.setVisibility(View.GONE);
                    viewHolder.reviewComment.setVisibility(View.VISIBLE);
                }
                else
                {
                    viewHolder.rejectionText.setVisibility(View.VISIBLE);
                    viewHolder.reviewButtonsContainer.setVisibility(View.GONE);
                }
            }
            else
            {
                viewHolder.rejectionText.setVisibility(View.GONE);
                if (NavigationUtil.isStudent)
                {
                    viewHolder.reviewButtonsContainer.setVisibility(View.GONE);
                }
                else
                {
                    viewHolder.reviewButtonsContainer.setVisibility(View.VISIBLE);

                    viewHolder.rejectButton.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            handleRejection(model);
                        }
                    });

                    viewHolder.approveBtn.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            handleAcceptance(model);
                        }
                    });
                }
            }
        }
    }

    private void handleAcceptance(final Notes model)
    {
        model.setNotesStatus(Notes.APPROVED);
        showNotificationDialog(model, new OnDismissListener()
        {
            @Override
            public void onDismiss(String msg)
            {
                if (!TextUtils.isEmpty(msg))
                {
                    FirebaseDatabase.getInstance().getReference().child(Notes.NOTES)
                            .child(mNotesClassifier.getClassId()).child(mNotesClassifier.getSubjectId())
                            .child(model.dateTime()).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            mNotesDbRef.getRef().child(model.dateTime()).removeValue();
                        }
                    });
                }
            }
        });
    }

    private void handleRejection(final Notes model)
    {
        model.setNotesStatus(Notes.REJECTED);
        showNotificationDialog(model, new OnDismissListener()
        {
            @Override
            public void onDismiss(String msg)
            {
                if (!TextUtils.isEmpty(msg))
                {
                    model.setReviewComment(msg);
                    updateRejectionStatus(model);
                }
            }
        });
    }

    private void updateRejectionStatus(Notes notes)
    {
        mNotesDbRef.getRef().child(notes.dateTime()).setValue(notes);
    }

    private void showNotificationDialog(Notes model, OnDismissListener listener)
    {
        NotificationDialogFragment.getInstance(model, listener)
                .show(launcher.getSupportFragmentManager(),
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
        if (NavigationUtil.mCurrentUser.getUserId()
                .equals(notes.getSubmitterId()))
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
                PopupMenu popup = new PopupMenu(launcher.getActivity(), v);
                popup.getMenuInflater()
                        .inflate(R.menu.classes_options, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    public boolean onMenuItemClick(MenuItem item)
                    {
                        switch (item.getItemId())
                        {
                            case R.id.action_edit:
                                editNotes(notes);
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
        notesStorageRef.child(notes.dateTime()).delete().addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                mNotesDbRef.getRef().child(notes.dateTime()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
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

    private void editNotes(Notes notes)
    {
        launcher.replaceFragment(AddOrEditNotesFragment.getInstance(notes, mNotesClassifier),
                true, AddOrEditNotesFragment.TAG);
    }

    private void showProfile(User model, View view)
    {
        Log.d("relaunchIssue", "studentAdapter : onClick");
        ProfileFragment fragment = ProfileFragment.getInstance(model);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            fragment.setSharedElementEnterTransition(new DetailsTransition());
            fragment.setSharedElementReturnTransition(new DetailsTransition());

            view.setTransitionName(model.getUserId());
            launcher.addFragment(fragment, true, ProfileFragment.TAG, view,
                    ProfileFragment.PROFILE_IMAGE);
        }
        else
        {
            launcher.addFragment(fragment, true, ProfileFragment.TAG);
        }
    }

    @Override
    public void onClick(View view)
    {
        Object tag = view.getTag();
        if (view.getId() == R.id.createrImage)
        {
            if (tag instanceof User)
            {
                showProfile((User) tag, view);
            }
        }
        else
        {
            if (tag instanceof Notes)
            {
                SingleNotesFragment fragment = SingleNotesFragment.getInstance((Notes) tag, notesStatus);
                launcher.addFragment(fragment, true, NavigationUtil.SINGLE_NOTES_FRAGMENT);
            }
        }
    }

    @Override
    public void onImageClick(int position, View view)
    {
        Object tag = view.getTag();
        if (tag instanceof List)
        {
            GenericDraweeHierarchyBuilder hierarchyBuilder = GenericDraweeHierarchyBuilder
                    .newInstance(launcher.getActivity().getResources())
                    .setPlaceholderImage(R.mipmap.notes);


            new ImageViewer.Builder(launcher.getActivity(), (List) tag)
                    .setStartPosition(position)
                    .setCustomDraweeHierarchyBuilder(hierarchyBuilder)
                    .show();
        }
    }
}