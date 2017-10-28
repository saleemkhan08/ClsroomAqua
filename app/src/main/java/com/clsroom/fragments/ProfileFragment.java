package com.clsroom.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.clsroom.R;
import com.clsroom.dialogs.ChangePasswordDialogFragment;
import com.clsroom.dialogs.EditNameDialogFragment;
import com.clsroom.dialogs.EditUserDetailsDialogFragment;
import com.clsroom.listeners.EventsListener;
import com.clsroom.listeners.FragmentLauncher;
import com.clsroom.model.Progress;
import com.clsroom.model.Snack;
import com.clsroom.model.Staff;
import com.clsroom.model.Students;
import com.clsroom.model.ToastMsg;
import com.clsroom.model.User;
import com.clsroom.utils.ActionBarUtil;
import com.clsroom.utils.ConnectivityUtil;
import com.clsroom.utils.ImageUtil;
import com.clsroom.utils.NavigationUtil;
import com.clsroom.utils.Otto;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.otto.Subscribe;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;
import static com.clsroom.utils.NavigationUtil.PROFILE_FRAGMENT;

public class ProfileFragment extends Fragment implements EventsListener, ValueEventListener
{
    private static final int PICK_PROFILE_IMAGE = 55;
    public static final String TAG = NavigationUtil.PROFILE_FRAGMENT;

    @Bind(R.id.editName)
    View editName;

    @Bind(R.id.uploadProfileImg)
    View editImage;

    @Bind(R.id.fabContainer)
    View editDetails;

    @Bind(R.id.profilePicture)
    ImageView mProfileImgView;

    @Bind(R.id.fullName)
    TextView mUserFullName;

    @Bind(R.id.designation)
    TextView mDesignation;

    @Bind(R.id.dobValue)
    TextView dobValue;

    @Bind(R.id.emailVlaue)
    TextView emailValue;

    @Bind(R.id.phoneNoVlaue)
    TextView phoneNoValue;

    @Bind(R.id.addressValue)
    TextView addressValue;

    private User mCurrentUser;
    private DatabaseReference mUserDbRef;
    private FragmentLauncher launcher;


    public static ProfileFragment getInstance(User user)
    {
        ProfileFragment fragment = new ProfileFragment();
        fragment.mCurrentUser = user;
        return fragment;
    }

    public ProfileFragment()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, parentView);
        Otto.register(this);
        Log.d("ProfileRelaunchIssue", "ProfileFragment : onCreate");
        setLauncher();
        if (mCurrentUser instanceof Students)

        {
            Students student = (Students) mCurrentUser;
            mUserDbRef = FirebaseDatabase.getInstance().getReference()
                    .child(User.STUDENTS).child(student.classId())
                    .child(student.getUserId());
        }
        else

        {
            mUserDbRef = FirebaseDatabase.getInstance().getReference()
                    .child(User.STAFF).child(mCurrentUser.getUserId());
        }

        mUserDbRef.addValueEventListener(this);

        if (launcher != null)
        {
            launcher.setToolBarTitle(R.string.profile);
        }

        if (!mCurrentUser.equals(NavigationUtil.mCurrentUser))
        {
            editDetails.setVisibility(View.GONE);
            editImage.setVisibility(View.GONE);
            editName.setVisibility(View.GONE);
        }
        return parentView;
    }

    private void updateProfileInfo(Students student)
    {
        mCurrentUser = student;
        mDesignation.setText(student.getClassName());
        updateCommonInfo(student);
    }

    private void updateCommonInfo(User user)
    {
        mUserFullName.setText(user.getFullName());
        ImageUtil.loadCircularImg(getActivity(), user.getPhotoUrl(), mProfileImgView);
        phoneNoValue.setText(user.getPhone());
        addressValue.setText(user.getAddress());
        emailValue.setText(user.getEmail());
        dobValue.setText(user.getDob());
    }

    private void updateProfileInfo(Staff staff)
    {
        mCurrentUser = staff;
        mDesignation.setText(staff.getDesignation());
        updateCommonInfo(staff);
    }

    @Override
    public boolean onBackPressed()
    {
        Log.d("ProfileRelaunchIssue", "ProfileFragment : onBackPressed");
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

    @OnClick(R.id.editName)
    public void editName()
    {
        if (ConnectivityUtil.isConnected(getActivity()))
        {
            EditNameDialogFragment.getInstance(mUserDbRef, mCurrentUser.getFullName())
                    .show(getFragmentManager(), EditNameDialogFragment.TAG);
        }
        else
        {
            Snack.show(R.string.noInternet);
        }
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot)
    {
        if (dataSnapshot.hasChild(Staff.IS_ADMIN))
        {
            updateProfileInfo(dataSnapshot.getValue(Staff.class));
        }
        else
        {
            updateProfileInfo(dataSnapshot.getValue(Students.class));
        }

    }

    @Override
    public void onCancelled(DatabaseError databaseError)
    {

    }

    @OnClick(R.id.uploadProfileImg)
    public void addImages(View view)
    {
        if (ConnectivityUtil.isConnected(getActivity()))
        {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_PROFILE_IMAGE);
        }
        else
        {
            Snack.show(R.string.noInternet);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        try
        {
            if (requestCode == PICK_PROFILE_IMAGE && resultCode == RESULT_OK && null != data)
            {
                if (data.getData() != null)
                {
                    Uri mImageUri = data.getData();
                    StorageReference rootRef = FirebaseStorage.getInstance().getReference();
                    StorageReference userImageRef;
                    if (mCurrentUser instanceof Staff)
                    {
                        Log.d("UploadIssue", "Staff");
                        Progress.show(R.string.uploading);
                        userImageRef = rootRef.child(User.STAFF).child(mCurrentUser.getUserId() + ".jpg");
                    }
                    else if (mCurrentUser instanceof Students)
                    {
                        Log.d("UploadIssue", "Students");
                        Progress.show(R.string.uploading);
                        Students student = (Students) mCurrentUser;
                        userImageRef = rootRef.child(User.STUDENTS).child(student.classId())
                                .child(student.getUserId() + ".jpg");
                    }
                    else
                    {
                        ToastMsg.show(R.string.something_went_wrong);
                        return;
                    }
                    Log.d("UploadIssue", "userImageRef : " + userImageRef);
                    Log.d("UploadIssue", "mImageUri : " + mImageUri);

                    Bitmap bmp = null;
                    try
                    {
                        bmp = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), mImageUri);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                        byte[] imageBytes = baos.toByteArray();
                        userImageRef.putBytes(imageBytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                        {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                            {
                                Log.d("UploadIssue", "onSuccess : " + taskSnapshot.getDownloadUrl());

                                mCurrentUser.setPhotoUrl(taskSnapshot.getDownloadUrl().toString());
                                mUserDbRef.setValue(mCurrentUser);
                                Progress.hide();
                                ToastMsg.show(R.string.uploaded);
                            }
                        }).addOnFailureListener(new OnFailureListener()
                        {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                Log.d("UploadIssue", "onFailure");
                                Progress.hide();
                                ToastMsg.show(R.string.please_try_again);
                            }
                        });
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        ToastMsg.show(R.string.please_try_again);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.fabContainer)
    public void editProfileInfo()
    {
        if (ConnectivityUtil.isConnected(getActivity()))
        {
            EditUserDetailsDialogFragment.getInstance(mUserDbRef, mCurrentUser)
                    .show(getFragmentManager(), EditUserDetailsDialogFragment.TAG);
        }
        else
        {
            Snack.show(R.string.noInternet);
        }
    }

    @Subscribe
    public void onOptionItemClicked(Integer itemId)
    {
        switch (itemId)
        {
            case R.id.changePassword:
                showPasswordChangeDialog();
                break;
        }
    }

    private void showPasswordChangeDialog()
    {
        if (ConnectivityUtil.isConnected(getActivity()))
        {
            ChangePasswordDialogFragment.getInstance(mUserDbRef, mCurrentUser.getPassword())
                    .show(getFragmentManager(), ChangePasswordDialogFragment.TAG);
        }
        else
        {
            Snack.show(R.string.noInternet);
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Log.d("ProfileRelaunchIssue", "ProfileFragment : onStart");
        if (launcher != null)
        {
            launcher.updateEventsListener(this);
            Otto.post(ActionBarUtil.SHOW_PROFILE_MENU);
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.d("ProfileRelaunchIssue", "ProfileFragment : onDestroy");
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
}

