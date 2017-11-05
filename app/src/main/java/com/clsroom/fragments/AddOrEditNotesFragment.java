package com.clsroom.fragments;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
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

import com.clsroom.R;
import com.clsroom.adapters.EditGalleryAdapter;
import com.clsroom.adapters.StaffFirebaseListAdapter;
import com.clsroom.dialogs.NotificationDialogFragment;
import com.clsroom.listeners.EventsListener;
import com.clsroom.listeners.FragmentLauncher;
import com.clsroom.listeners.OnDismissListener;
import com.clsroom.model.Notes;
import com.clsroom.model.NotesClassifier;
import com.clsroom.model.NotesImage;
import com.clsroom.model.Progress;
import com.clsroom.model.Snack;
import com.clsroom.model.Staff;
import com.clsroom.model.ToastMsg;
import com.clsroom.utils.ActionBarUtil;
import com.clsroom.utils.ConnectivityUtil;
import com.clsroom.utils.DateTimeUtil;
import com.clsroom.utils.ItemMovementCallbackHelper;
import com.clsroom.utils.NavigationUtil;
import com.clsroom.utils.Otto;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;

public class AddOrEditNotesFragment extends Fragment implements EventsListener, AdapterView.OnItemSelectedListener, View.OnTouchListener
{
    public static final String TAG = NavigationUtil.ADD_OR_EDIT_NOTES_FRAGMENT;
    public static final int PICK_IMAGE_MULTIPLE_GALLERY = 125;
    private FragmentLauncher launcher;

    @BindView(R.id.notesApproverSpinner)
    Spinner mNotesApproverSpinner;

    @BindView(R.id.notesReviewer)
    EditText mNotesApprover;

    @BindView(R.id.imagesRecyclerView)
    RecyclerView mImagesRecyclerView;

    @BindView(R.id.notesTitle)
    EditText mNotesTitle;

    @BindView(R.id.notesDescription)
    EditText mNotesDescription;

    @BindView(R.id.subjectName)
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
        Log.d("ClosingIssue", "onCreateView");
        View parentView = inflater.inflate(R.layout.fragment_add_notes, container, false);
        ButterKnife.bind(this, parentView);
        setLauncher();
        Otto.register(this);

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

        mNotesDbRef = (NavigationUtil.isStudent)

                ? mRootRef.child(Notes.NOTES).child(Notes.REVIEW).child(mCurrentNotesClassifier.getClassId())
                .child(mCurrentNotesClassifier.getSubjectId())

                : mRootRef.child(Notes.NOTES).child(mCurrentNotesClassifier.getClassId())
                .child(mCurrentNotesClassifier.getSubjectId());

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        setUpStaffSpinner();
        mSubjectName.setText(mCurrentNotesClassifier.getClassName() + " - " + mCurrentNotesClassifier.getSubjectName());
        refreshActionBar();
        return parentView;
    }

    private void setLauncher()
    {
        Activity activity = getActivity();
        if (activity instanceof FragmentLauncher)
        {
            launcher = (FragmentLauncher) activity;
        }
        Log.d("ClosingIssue", "setLauncher : " + launcher);
    }

    private void setUpStaffSpinner()
    {
        if (NavigationUtil.isStudent)
        {
            mStaffDbRef = mRootRef.child(Staff.STAFF);
            if (mCurrentNotesClassifier.isEdit())
            {
                updateStaffSpinnerSelection();
            }
            else
            {
                mStaffAdapter = new StaffFirebaseListAdapter(getActivity(),
                        Staff.class, android.R.layout.simple_list_item_1, mStaffDbRef);
                mNotesApproverSpinner.setAdapter(mStaffAdapter);
                mNotesApproverSpinner.setOnItemSelectedListener(this);
                mNotesApprover.setOnTouchListener(this);
            }
        }
        else
        {
            mNotesApprover.setEnabled(false);
            Staff staff = (Staff) NavigationUtil.mCurrentUser;
            mNotesApprover.setText(staff.getFullName());
            mNotesApprover.setTag(staff);
        }
    }

    private void updateStaffSpinnerSelection()
    {
        mStaffDbRef.child(notes.getReviewerId()).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Staff staff = dataSnapshot.getValue(Staff.class);
                mNotesApprover.setText(staff.getFullName());
                mNotesApprover.setTag(staff);
                mNotesApprover.setEnabled(false);
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
        Log.d("ClosingIssue", "addImages");
        if (ConnectivityUtil.isConnected(getActivity()))
        {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_MULTIPLE_GALLERY);
        }
        else
        {
            Snack.show(R.string.noInternet);
        }
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
        return NavigationUtil.NOTES_FRAGMENT;
    }

    @Override
    public void refreshActionBar()
    {
        if (launcher != null)
        {
            if (mCurrentNotesClassifier.isEdit())
            {
                launcher.setToolBarTitle(R.string.editNotes);
                mNotesTitle.setText(notes.getNotesTitle());
                mNotesDescription.setText(notes.getNotesDescription());
            }
            else
            {
                launcher.setToolBarTitle(R.string.addNotes);
            }
            launcher.updateEventsListener(this);
            Otto.post(ActionBarUtil.NO_MENU);
        }
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
            }, 300);
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
    public void onDestroy()
    {
        super.onDestroy();
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
                updateImageList(currentList);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.saveNotes)
    public void submit()
    {
        Log.d("ClosingIssue", "save");
        if (ConnectivityUtil.isConnected(getActivity()))
        {
            Staff staff = (Staff) mNotesApprover.getTag();
            notes.setReviewerId(staff.getUserId());
            notes.setClassSubId(mCurrentNotesClassifier.getClassId() + "_" + mCurrentNotesClassifier.getSubjectId());
            if (notes.dateTime().equals("0"))
            {
                notes.dateTime(DateTimeUtil.getKey());
            }

            notes.setNotesDescription(mNotesDescription.getText().toString().trim());
            notes.setNotesTitle(mNotesTitle.getText().toString().trim());

            notes.setSubmitterId(NavigationUtil.mCurrentUser.getUserId());
            notes.setSubmitterName(NavigationUtil.mCurrentUser.getFullName());
            notes.setSubmitterPhotoUrl(NavigationUtil.mCurrentUser.getPhotoUrl());

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
                uploadImages();
            }
        }
        else
        {
            Snack.show(R.string.noInternet);
        }
    }

    private void uploadImages()
    {
        Progress.show(R.string.uploading);
        StorageReference ref = mNotesStorageRef.child(notes.dateTime());
        final int noOfUploadingPhoto = mImageList.size();

        Log.d("ClosingIssue", "mImageList : " + mImageList);
        if (noOfUploadingPhoto > 0)
        {
            for (Object image : mImageList)
            {
                final String photoName = System.currentTimeMillis() + ".jpg";
                if (image instanceof Uri)
                {
                    Bitmap bmp = null;
                    try
                    {
                        bmp = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), (Uri) image);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                        byte[] data = baos.toByteArray();

                        ref.child(photoName).putBytes(data).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                            {
                                if (task.isSuccessful())
                                {
                                    Log.d("ClosingIssue", "onSuccess");
                                    saveNotesDetailsForCurrentlyUploadedImages(notes, task.getResult(), photoName, noOfUploadingPhoto);
                                }
                                else
                                {
                                    Progress.hide();
                                    ToastMsg.show(R.string.please_try_again);
                                }
                            }
                        });
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        ToastMsg.show(R.string.please_try_again);
                    }
                }
                else if (image instanceof NotesImage)
                {
                    Log.d("ClosingIssue", "already uploaded image");
                    saveNotesDetailsForAlreadyUploadedImages(notes, (NotesImage) image, noOfUploadingPhoto);
                }
            }
        }
        else
        {
            Log.d("ClosingIssue", "No images");
            saveNotesDetails();
        }
    }

    private void saveNotesDetailsForCurrentlyUploadedImages(Notes notes, UploadTask.TaskSnapshot taskSnapshot,
                                                            String photoName, int noOfPhotos)
    {
        Log.d("ClosingIssue", "updateProgressDialog");
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
                    saveNotesDetails();
                }
            }
        }
    }

    private void saveNotesDetailsForAlreadyUploadedImages(Notes notes, NotesImage notesImage, int noOfPhotos)
    {
        if (notesImage != null)
        {
            mGalleryImagesList.add(notesImage);
            if (noOfPhotos == mGalleryImagesList.size())
            {
                notes.setNotesImages(mGalleryImagesList);
                Log.d("ClosingIssue", "saveNotesDetailsForAlreadyUploadedImages");
                saveNotesDetails();
            }
        }
    }

    private void sendNotificationToReviewer()
    {
        Log.d("ClosingIssue", "onComplete");
        if (NavigationUtil.isStudent)
        {
            Log.d("ClosingIssue", "sending notification getNotesStatus : " + notes.getNotesStatus());
            NotificationDialogFragment.getInstance(notes, new OnDismissListener()
            {
                @Override
                public void onDismiss(String msg)
                {
                    if (!TextUtils.isEmpty(msg))
                    {
                        showSuccessMessage();
                    }
                }
            }).show(getActivity().getSupportFragmentManager(),
                    NotificationDialogFragment.TAG);
        }
    }

    private void saveNotesDetails()
    {
        Log.d("ClosingIssue", "saveNotesDetails");
        String status = notes.getNotesStatus();
        if (mCurrentNotesClassifier.isEdit() && status != null && status.equals(Notes.REJECTED))
        {
            notes.setNotesStatus(Notes.RE_SUBMITTED);
        }
        else
        {
            notes.setNotesStatus(Notes.REVIEW);
        }

        if (NavigationUtil.isStudent)
        {
            FirebaseDatabase.getInstance().getReference().child(Notes.NOTES)
                    .child(mCurrentNotesClassifier.getClassId()).child(mCurrentNotesClassifier.getSubjectId())
                    .child(notes.dateTime()).removeValue();
        }

        mNotesDbRef.child(notes.dateTime()).setValue(notes).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    sendNotificationToReviewer();
                }
                else
                {
                    Progress.hide();
                    ToastMsg.show(R.string.please_try_again);
                }
            }
        });
    }

    private void showSuccessMessage()
    {
        Log.d("ClosingIssue", "notesUploadSuccess");
        Progress.hide();
        if (NavigationUtil.isStudent)
        {
            ToastMsg.show(R.string.notes_will_be_displayed_after_review);
        }
        else
        {
            ToastMsg.show(R.string.notes_uploaded);
        }
        getActivity().onBackPressed();
    }
}
