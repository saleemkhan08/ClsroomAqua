package com.clsroom.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clsroom.R;
import com.clsroom.dialogs.NotificationDialogFragment;
import com.clsroom.listeners.EventsListener;
import com.clsroom.listeners.FragmentLauncher;
import com.clsroom.listeners.OnDismissListener;
import com.clsroom.model.Classes;
import com.clsroom.model.Notes;
import com.clsroom.model.NotesClassifier;
import com.clsroom.model.NotesImage;
import com.clsroom.model.Progress;
import com.clsroom.model.Subjects;
import com.clsroom.model.ToastMsg;
import com.clsroom.utils.ActionBarUtil;
import com.clsroom.utils.ImageUtil;
import com.clsroom.utils.NavigationUtil;
import com.clsroom.utils.Otto;
import com.clsroom.views.SquareImageView;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.clsroom.utils.NavigationUtil.PROFILE_FRAGMENT;

public class SingleNotesFragment extends Fragment implements EventsListener, ValueEventListener
{
    public static final String TAG = NavigationUtil.SINGLE_NOTES_FRAGMENT;
    public DatabaseReference mNotesDbRef;
    private FragmentLauncher launcher;

    @Bind(R.id.imagesContainer)
    LinearLayout imagesContainer;

    @Bind(R.id.rejectionText)
    public View rejectionText;

    @Bind(R.id.reviewComment)
    public TextView reviewComment;

    @Bind(R.id.createrImage)
    public ImageView createrImage;

    @Bind(R.id.errorMsg)
    TextView errorMsgTextView;

    @Bind(R.id.notesTitle)
    public TextView notesTitle;

    @Bind(R.id.createrName)
    public TextView createrName;

    @Bind(R.id.optionsIconContainer)
    public ImageView optionsIconContainer;

    @Bind(R.id.notesDescription)
    public TextView notesDescription;

    @Bind(R.id.dateTime)
    public TextView dateTime;

    @Bind(R.id.reviewButtonsContainer)
    public View reviewButtonsContainer;

    @Bind(R.id.approveBtn)
    public View approveBtn;

    @Bind(R.id.rejectButton)
    public View rejectButton;

    ArrayList<String> imageList;

    boolean isPending;
    private NotesClassifier mNotesClassifier;

    public static SingleNotesFragment getInstance(String notesId)
    {
        final SingleNotesFragment fragment = new SingleNotesFragment();
        try
        {
            String[] ids = notesId.split("_");
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference ref = rootRef.child(Notes.NOTES);
            fragment.isPending = ids[0].equals("P");
            if (fragment.isPending)//Reviewed or Pending
            {
                ref = ref.child(Notes.REVIEW);
            }
            fragment.mNotesDbRef = ref.child(ids[1]).child(ids[2]).child(ids[3]);//ClassId - SubjectId - NotesId
            fragment.mNotesClassifier = new NotesClassifier();
            fragment.mNotesClassifier.setClassId(ids[1]);
            fragment.mNotesClassifier.setSubjectId(ids[2]);
            fragment.setClassName(rootRef);
            fragment.setSubjectName(rootRef);
        }
        catch (Exception e)
        {
            Log.d(TAG, e.getMessage());
        }
        return fragment;
    }

    private void setClassName(DatabaseReference rootRef)
    {
        rootRef.child(Classes.CLASSES).child(mNotesClassifier.getClassId())
                .addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        Classes classes = dataSnapshot.getValue(Classes.class);
                        mNotesClassifier.setClassName(classes.getName());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });
    }

    private void setSubjectName(DatabaseReference rootRef)
    {
        rootRef.child(Subjects.SUBJECTS).child(mNotesClassifier.getClassId())
                .child(mNotesClassifier.getSubjectId()).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Subjects subjects = dataSnapshot.getValue(Subjects.class);
                mNotesClassifier.setSubjectName(subjects.getSubjectName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    public SingleNotesFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_single_notes, container, false);
        ButterKnife.bind(this, parentView);
        Otto.register(this);
        setLauncher();
        if (launcher != null)
        {
            launcher.setToolBarTitle(R.string.notes);
        }
        mNotesDbRef.addValueEventListener(this);
        return parentView;
    }

    @Override
    public boolean onBackPressed()
    {
        return true;
    }

    @Override
    public int getMenuItemId()
    {
        return R.id.nav_settings;
    }

    @Override
    public String getTagName()
    {
        return PROFILE_FRAGMENT;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot)
    {
        Notes model = dataSnapshot.getValue(Notes.class);
        if (model == null)
        {
            errorMsgTextView.setVisibility(View.VISIBLE);
        }
        else
        {
            errorMsgTextView.setVisibility(View.GONE);
            imageList = new ArrayList<>();
            imagesContainer.removeAllViews();
            int index = 0;
            for (NotesImage image : model.getNotesImages())
            {
                imageList.add(image.url);
                SquareImageView imageView = new SquareImageView(getActivity());
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                imageView.setPadding(0, 0, 0, 25);
                imageView.setLayoutParams(layoutParams);
                ImageUtil.loadImg(getActivity(), image.url, imageView);
                imageView.setId(index);
                imageView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        onImageClick(view.getId());
                    }
                });
                imagesContainer.addView(imageView);
                index++;
            }
            handleReviewOptions(model);
            dateTime.setText(model.displayDate());
            notesTitle.setText(model.getNotesTitle());
            notesDescription.setText(model.getNotesDescription());
            createrName.setText(model.getSubmitterName());

            ImageUtil.loadCircularImg(model.getSubmitterPhotoUrl(), createrImage);

            configureOptions(model);
        }

    }

    @Override
    public void onCancelled(DatabaseError databaseError)
    {

    }

    @Override
    public void onStart()
    {
        super.onStart();
        if (launcher != null)
        {
            launcher.updateEventsListener(this);
            Otto.post(ActionBarUtil.SHOW_SINGLE_NOTES_MENU);
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Otto.unregister(this);
    }

    private void setLauncher()
    {
        Activity activity = getActivity();
        if (activity instanceof FragmentLauncher)
        {
            launcher = (FragmentLauncher) activity;
        }
    }

    private void configureOptions(final Notes notes)
    {
        if (NavigationUtil.mCurrentUser.getUserId()
                .equals(notes.getSubmitterId()))
        {
            optionsIconContainer.setVisibility(View.VISIBLE);
        }
        else
        {
            optionsIconContainer.setVisibility(View.GONE);
        }
        optionsIconContainer.setOnClickListener(new View.OnClickListener()
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
        launcher.showFragment(AddOrEditNotesFragment.getInstance(notes, mNotesClassifier),
                true, AddOrEditNotesFragment.TAG);
    }

    private void handleReviewOptions(final Notes model)
    {
        reviewComment.setVisibility(View.GONE);
        if (isPending)
        {
            if (model.getNotesStatus() != null && model.getNotesStatus().equals(Notes.REJECTED))
            {
                if (NavigationUtil.isStudent)
                {
                    reviewComment.setText(model.getReviewComment());
                    reviewButtonsContainer.setVisibility(View.GONE);
                    reviewComment.setVisibility(View.VISIBLE);
                }
                else
                {
                    rejectionText.setVisibility(View.VISIBLE);
                    reviewButtonsContainer.setVisibility(View.GONE);
                }
            }
            else
            {
                rejectionText.setVisibility(View.GONE);
                if (NavigationUtil.isStudent)
                {
                    reviewButtonsContainer.setVisibility(View.GONE);
                }
                else
                {
                    reviewButtonsContainer.setVisibility(View.VISIBLE);

                    rejectButton.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            handleRejection(model);
                        }
                    });

                    approveBtn.setOnClickListener(new View.OnClickListener()
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
        else
        {
            reviewButtonsContainer.setVisibility(View.GONE);
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
                FirebaseDatabase.getInstance().getReference().child(Notes.NOTES)
                        .child(mNotesClassifier.getClassId()).child(mNotesClassifier.getSubjectId())
                        .child(model.dateTime()).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        mNotesDbRef.getRef().child(model.dateTime()).removeValue();
                        getActivity().onBackPressed();
                    }
                });
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

    public void onImageClick(int position)
    {
        GenericDraweeHierarchyBuilder hierarchyBuilder = GenericDraweeHierarchyBuilder
                .newInstance(launcher.getActivity().getResources())
                .setFailureImage(R.mipmap.broken_image);

        new ImageViewer.Builder(getActivity(), imageList)
                .setStartPosition(position)
                .setCustomDraweeHierarchyBuilder(hierarchyBuilder)
                .show();
    }
}
