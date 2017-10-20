package com.clsroom.fragments;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.clsroom.MainActivity;
import com.clsroom.R;
import com.clsroom.adapters.EditGalleryAdapter;
import com.clsroom.adapters.StaffFirebaseListAdapter;
import com.clsroom.dialogs.NotificationDialogFragment;
import com.clsroom.listeners.EventsListener;
import com.clsroom.listeners.OnDismissListener;
import com.clsroom.model.Notes;
import com.clsroom.model.NotesClassifier;
import com.clsroom.model.NotesImage;
import com.clsroom.model.Progress;
import com.clsroom.model.Staff;
import com.clsroom.model.ToastMsg;
import com.clsroom.utils.ActionBarUtil;
import com.clsroom.utils.DateTimeUtil;
import com.clsroom.utils.ItemMovementCallbackHelper;
import com.clsroom.utils.NavigationDrawerUtil;
import com.clsroom.utils.Otto;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;

public class AddOrEditNotesFragment extends Fragment implements EventsListener, AdapterView.OnItemSelectedListener, View.OnTouchListener
{
    public static final String TAG = "AddOrEditNotesFragment";
    public static final int PICK_IMAGE_MULTIPLE_GALLERY = 125;

    @Bind(R.id.notesApproverSpinner)
    Spinner mNotesApproverSpinner;

    @Bind(R.id.notesReviewer)
    EditText mNotesApprover;

    @Bind(R.id.imagesRecyclerView)
    RecyclerView mImagesRecyclerView;

    @Bind(R.id.notesTitle)
    EditText mNotesTitle;

    @Bind(R.id.notesDescription)
    EditText mNotesDescription;

    @Bind(R.id.subjectName)
    TextView mSubjectName;

    private DatabaseReference mRootRef;
    NotesClassifier mCurrentNotesClassifier;
    private StaffFirebaseListAdapter mStaffAdapter;
    private Handler mHandler;
    private DatabaseReference mStaffDbRef;
    private ArrayList mImageList;
    private EditGalleryAdapter mAdapter;
    private StorageReference mNotesStorageRef;
    private ArrayList<NotesImage> mGalleryImagesList;
    private DatabaseReference mNotesDbRef;
    private Notes notes;
    private SharedPreferences sharedPref;

    public AddOrEditNotesFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_add_notes, container, false);
        ButterKnife.bind(this, parentView);
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mImageList = new ArrayList<>();
        mAdapter = new EditGalleryAdapter(mImageList, notes);
        mImagesRecyclerView.setAdapter(mAdapter);
        mImagesRecyclerView.setNestedScrollingEnabled(false);
        mImagesRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        ItemMovementCallbackHelper callbackHelper = new ItemMovementCallbackHelper(mAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callbackHelper);
        itemTouchHelper.attachToRecyclerView(mImagesRecyclerView);
        mGalleryImagesList = new ArrayList<>();
        mNotesStorageRef = FirebaseStorage.getInstance().getReference()
                .child(Notes.NOTES).child(mCurrentNotesClassifier.getClassId())
                .child(mCurrentNotesClassifier.getSubjectId());

        mNotesDbRef = mRootRef.child(Notes.NOTES).child(Notes.REVIEW).child(mCurrentNotesClassifier.getClassId())
                .child(mCurrentNotesClassifier.getSubjectId());

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        Activity activity = getActivity();

        if (activity instanceof MainActivity)
        {
            if (mCurrentNotesClassifier.isEdit())
            {
                ((MainActivity) activity).setToolBarTitle(getString(R.string.editNotes));
                mNotesTitle.setText(notes.getNotesTitle());
                mNotesDescription.setText(notes.getNotesDescription());
            }
            else
            {
                ((MainActivity) activity).setToolBarTitle(getString(R.string.addNotes));
            }
            ((MainActivity) activity).updateEventsListener(this);
            Otto.post(ActionBarUtil.NO_MENU);
        }

        mSubjectName.setText(mCurrentNotesClassifier.getClassName() + " - " + mCurrentNotesClassifier.getSubjectName());
        setUpStaffSpinner();

        return parentView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Otto.register(this);
    }

    private void setUpStaffSpinner()
    {
        mStaffDbRef = mRootRef.child(Staff.STAFF);
        mStaffAdapter = new StaffFirebaseListAdapter(getActivity(),
                Staff.class, android.R.layout.simple_list_item_1, mStaffDbRef);
        mNotesApproverSpinner.setAdapter(mStaffAdapter);
        mNotesApproverSpinner.setOnItemSelectedListener(this);
        mNotesApprover.setOnTouchListener(this);
        updateStaffSpinnerSelection();
    }

    private void updateStaffSpinnerSelection()
    {
        mStaffDbRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                mHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            mNotesApproverSpinner.setSelection(findPosition());
                        }
                        catch (Exception e)
                        {
                            mNotesApproverSpinner.setSelection(0);
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    @OnClick(R.id.addImagesFabContainer)
    public void addImages(View view)
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        if (Build.VERSION.SDK_INT >= 18)
        {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_MULTIPLE_GALLERY);
    }

    private void updateImageList(ArrayList<Uri> currentList)
    {
        mImageList.addAll(currentList);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onBackPressed()
    {
        return true;
    }

    @Override
    public int getMenuItemId()
    {
        return R.id.launch_notes_fragment;
    }

    @Override
    public String getTagName()
    {
        return NavigationDrawerUtil.NOTES_FRAGMENT;
    }

    public static AddOrEditNotesFragment getInstance(NotesClassifier resultClassifier)
    {
        AddOrEditNotesFragment fragment = new AddOrEditNotesFragment();
        fragment.mCurrentNotesClassifier = resultClassifier;
        fragment.mHandler = new Handler();
        fragment.notes = new Notes();
        return fragment;
    }

    public static AddOrEditNotesFragment getInstance(Notes notes, NotesClassifier notesClassifier)
    {
        AddOrEditNotesFragment fragment = new AddOrEditNotesFragment();
        notesClassifier.setEdit(true);
        fragment.mCurrentNotesClassifier = notesClassifier;
        fragment.notes = notes;
        fragment.mHandler = new Handler();
        return fragment;
    }

    private int findPosition()
    {
        for (int i = 0; i < mStaffAdapter.getCount(); i++)
        {
            if ((mStaffAdapter.getItem(i).getUserId().equals(mCurrentNotesClassifier.getTeacherId())))
            {
                return i;
            }
        }
        return 0;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
    {
        mNotesApprover.setText(((TextView) view).getText().toString());
        mNotesApprover.setTag(view.getTag());
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView)
    {
        ToastMsg.show(R.string.pleaseSelectClassTeacher);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent)
    {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
        {
            mNotesApprover.requestFocus();
            mNotesApprover.setCursorVisible(false);
            closeTheKeyBoard();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    mNotesApproverSpinner.performClick();
                }
            }, 100);
        }
        return true;
    }

    private void closeTheKeyBoard()
    {
        View view = getView();
        if (view != null)
        {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
        Otto.unregister(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        try
        {
            if (requestCode == PICK_IMAGE_MULTIPLE_GALLERY && resultCode == RESULT_OK && null != data)
            {
                ArrayList<Uri> currentList = new ArrayList<>();
                if (data.getData() != null)
                {
                    Uri mImageUri = data.getData();
                    currentList.add(mImageUri);
                }
                else
                {
                    if (data.getClipData() != null)
                    {
                        ClipData mClipData = data.getClipData();
                        for (int i = 0; i < mClipData.getItemCount(); i++)
                        {
                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri mImageUri = item.getUri();
                            currentList.add(mImageUri);
                        }
                    }

                }
                Toast.makeText(getActivity(), "currentList count : " + currentList.size(), Toast.LENGTH_SHORT).show();
                updateImageList(currentList);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.saveNotes)
    public void save()
    {
        Staff staff = (Staff) mNotesApprover.getTag();
        notes.setReviewerId(staff.getUserId());

        if (notes.getDate() == null)
        {
            notes.setDate(DateTimeUtil.getKey());
        }

        notes.setNotesDescription(mNotesDescription.getText().toString().trim());
        notes.setNotesTitle(mNotesTitle.getText().toString().trim());

        notes.setSubmitterId(NavigationDrawerUtil.mCurrentUser.getUserId());
        notes.setSubmitterName(NavigationDrawerUtil.mCurrentUser.getFullName());
        notes.setSubmitterPhotoUrl(NavigationDrawerUtil.mCurrentUser.getPhotoUrl());

        if (TextUtils.isEmpty(notes.getNotesTitle()))
        {
            ToastMsg.show(R.string.please_enter_notes_title);
        }
        else if (TextUtils.isEmpty(notes.getNotesDescription()))
        {
            ToastMsg.show(R.string.please_enter_description);
        }
        else
        {
            Progress.show(R.string.uploading);
            StorageReference ref = mNotesStorageRef.child(notes.getDate());
            final int noOfUploadingPhoto = mImageList.size();
            Log.d("fixImageOrderIssue", "mImageList : " + mImageList);
            if (noOfUploadingPhoto > 0)
            {
                for (Object image : mImageList)
                {
                    final String photoName = System.currentTimeMillis() + ".jpg";
                    if (image instanceof Uri)
                    {
                        ref.child(photoName).putFile((Uri) image)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                                {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                                    {
                                        try
                                        {
                                            Log.d("PhotoUploadFlow", "onSuccess");
                                            updateProgressDialog(notes, taskSnapshot, photoName, noOfUploadingPhoto);
                                        }
                                        catch (Exception e)
                                        {
                                            Log.d("PhotoUploadFlow", "Exception " + e.getMessage());
                                            Progress.hide();
                                            ToastMsg.show(R.string.please_try_again);
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener()
                                {
                                    @Override
                                    public void onFailure(@NonNull Exception e)
                                    {
                                        Progress.hide();
                                        ToastMsg.show(R.string.please_try_again);
                                    }
                                });
                    }
                    else if (image instanceof NotesImage)
                    {
                        updateProgressDialog(notes, (NotesImage) image, noOfUploadingPhoto);
                    }
                }
            }
            else
            {
                saveNotes(notes);
            }
        }
    }

    private void updateProgressDialog(Notes notes, UploadTask.TaskSnapshot taskSnapshot,
                                      String photoName, int noOfPhotos)
    {
        Log.d("PhotoUploadFlow", "updateProgressDialog");
        Uri downloadUri = taskSnapshot.getDownloadUrl();
        if (downloadUri != null)
        {
            String url = downloadUri.toString();
            if (!url.trim().isEmpty())
            {
                NotesImage image = new NotesImage();
                image.url = url;
                image.name = photoName;
                mGalleryImagesList.add(image);
                if (noOfPhotos == mGalleryImagesList.size())
                {
                    notes.setNotesImages(mGalleryImagesList);
                    saveNotes(notes);
                }
            }
        }
    }

    private void updateProgressDialog(Notes notes, NotesImage notesImage, int noOfPhotos)
    {
        if (notesImage != null)
        {
            mGalleryImagesList.add(notesImage);
            if (noOfPhotos == mGalleryImagesList.size())
            {
                notes.setNotesImages(mGalleryImagesList);
                saveNotes(notes);
            }
        }

    }

    private void saveNotes(final Notes notes)
    {
        Log.d("fixImageOrderIssue", "notes.getNotesImages() : " + notes.getNotesImages());
        mNotesDbRef.child(notes.getDate()).setValue(notes).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                notes.setNotesStatus(Notes.REVIEW);
                NotificationDialogFragment.getInstance(notes, new OnDismissListener()
                {
                    @Override
                    public void onDismiss()
                    {
                        Progress.hide();
                        ToastMsg.show(R.string.notes_will_be_displayed_after_review);
                        getActivity().onBackPressed();
                    }
                }).show(getActivity().getSupportFragmentManager(),
                        NotificationDialogFragment.TAG);


            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Progress.hide();
                ToastMsg.show(R.string.please_try_again);
            }
        });
    }

}
