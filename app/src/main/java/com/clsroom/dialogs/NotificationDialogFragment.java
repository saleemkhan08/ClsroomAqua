package com.clsroom.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.clsroom.R;
import com.clsroom.listeners.OnDismissListener;
import com.clsroom.model.Leaves;
import com.clsroom.model.Notes;
import com.clsroom.model.Notifications;
import com.clsroom.model.Progress;
import com.clsroom.model.Snack;
import com.clsroom.model.ToastMsg;
import com.clsroom.model.User;
import com.clsroom.utils.ConnectivityUtil;
import com.clsroom.utils.DateTimeUtil;
import com.clsroom.utils.NavigationUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationDialogFragment extends CustomDialogFragment
{
    public static final String TAG = "NotificationDialogFragment";

    @BindView(R.id.message)
    EditText mMessage;

    DatabaseReference mNotificationDbRef;
    Notes notes;
    Notifications mCurrentNotification;
    OnDismissListener listener;
    Leaves leaves;
    private DatabaseReference notificationsRootRef;
    private Context mContext;
    private String notification;
    private User user;

    public static NotificationDialogFragment getInstance(Notes notes, OnDismissListener listener)
    {
        NotificationDialogFragment fragment = getInstance(listener);
        fragment.notes = notes;
        return fragment;
    }

    public static NotificationDialogFragment getInstance(User user, OnDismissListener listener)
    {
        NotificationDialogFragment fragment = getInstance(listener);
        fragment.user = user;
        return fragment;
    }

    public static NotificationDialogFragment getInstance(Leaves leaves, OnDismissListener listener)
    {
        NotificationDialogFragment fragment = getInstance(listener);
        fragment.leaves = leaves;
        return fragment;
    }

    public static NotificationDialogFragment getInstance(Leaves leaves)
    {
        NotificationDialogFragment fragment = getInstance();
        fragment.leaves = leaves;
        return fragment;
    }

    private static NotificationDialogFragment getInstance(OnDismissListener listener)
    {
        NotificationDialogFragment fragment = NotificationDialogFragment.getInstance();
        fragment.listener = listener;
        return fragment;
    }

    private static NotificationDialogFragment getInstance()
    {
        NotificationDialogFragment fragment = new NotificationDialogFragment();
        fragment.notificationsRootRef = FirebaseDatabase.getInstance().getReference()
                .child(Notifications.NOTIFICATIONS);
        fragment.mCurrentNotification = new Notifications();
        fragment.mCurrentNotification.setSenderId(NavigationUtil.mCurrentUser.getUserId());
        fragment.mCurrentNotification.setSenderName(NavigationUtil.mCurrentUser.getFullName());
        fragment.mCurrentNotification.setSenderPhotoUrl(NavigationUtil.mCurrentUser.getPhotoUrl());
        return fragment;
    }

    public NotificationDialogFragment()
    {

    }

    @Override
    public void onCreateView(View parentView)
    {
        ButterKnife.bind(this, parentView);

        mContext = getActivity();
        if (notes != null)
        {
            sendNotesRelatedNotifications();
        }
        else
        {
            sendPersonalNotification();
        }
    }

    private void sendPersonalNotification()
    {
        mCurrentNotification.setLeaveId(null);
        mCurrentNotification.setNotesId(null);
        mNotificationDbRef = notificationsRootRef.child(user.getUserId());
    }

    @Override
    protected int getContentViewLayoutRes()
    {
        return R.layout.fragment_notification;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        setSubmitBtnTxt(R.string.send);
        setSubmitBtnImg(R.mipmap.submit);
    }

    @Override
    public void submit(View view)
    {
        notification = mMessage.getText().toString().trim();
        if (TextUtils.isEmpty(notification))
        {
            ToastMsg.show(R.string.please_enter_the_notification_message);
        }
        else
        {
            if (!(notes != null && notes.getNotesStatus().equals(Notes.REJECTED)))
            {
                mCurrentNotification.setMessage(notification);
            }
            else
            {
                notification = mCurrentNotification.getMessage();
            }
            sendNotification();
        }
    }

    public void sendLeavesRelatedNotification(Context context)
    {
        if (ConnectivityUtil.isConnected(context))
        {
            mContext = context;
            mCurrentNotification.setNotesId(null);
            switch (leaves.getStatus())
            {
                case Leaves.STATUS_REJECTED:
                    mCurrentNotification.setMessage(getLeavesRejectionMsg());
                    mNotificationDbRef = notificationsRootRef.child(leaves.getRequesterId());
                    mCurrentNotification.setLeaveId(leaves.dbKeyDate());
                    mCurrentNotification.setLeaveRefType(Leaves.MY_LEAVES);
                    break;

                case Leaves.STATUS_APPROVED:
                    mCurrentNotification.setMessage(getLeavesApprovalMsg());
                    mNotificationDbRef = notificationsRootRef.child(leaves.getRequesterId());
                    mCurrentNotification.setLeaveId(leaves.dbKeyDate());
                    mCurrentNotification.setLeaveRefType(Leaves.MY_LEAVES);
                    break;

                case Leaves.STATUS_APPLIED:
                    mCurrentNotification.setMessage(getLeavesApplicationMsg());
                    mNotificationDbRef = notificationsRootRef.child(leaves.getApproverId());
                    mCurrentNotification.setLeaveId(leaves.dbKeyDate());
                    mCurrentNotification.setLeaveRefType(Leaves.REQUESTED_LEAVES);
                    break;

                case Leaves.STATUS_CANCELLED:
                    mCurrentNotification.setMessage(getLeavesCancellationMsg());
                    mNotificationDbRef = notificationsRootRef.child(leaves.getApproverId());
                    break;
            }
            sendNotification();
        }
        else
        {
            Snack.show(R.string.noInternet);
        }
    }

    private String getLeavesCancellationMsg()
    {
        return mCurrentNotification.getSenderName() + " "
                + mContext.getString(R.string.has_cancelled_the_following_leave)
                + " : " + leaves.getFromDate() + " to " + leaves.getToDate();
    }

    private String getLeavesApplicationMsg()
    {
        return mCurrentNotification.getSenderName() + " "
                + mContext.getString(R.string.has_applied_for_leave_on_following_days)
                + " : " + leaves.getFromDate() + " to " + leaves.getToDate();
    }

    private String getLeavesApprovalMsg()
    {
        return mCurrentNotification.getSenderName() + " "
                + mContext.getString(R.string.has_approved_following_leaves)
                + " : " + leaves.getFromDate() + " to " + leaves.getToDate();
    }

    private String getLeavesRejectionMsg()
    {
        return mCurrentNotification.getSenderName() + " "
                + mContext.getString(R.string.has_rejected_follwoing_leaves)
                + " : " + leaves.getFromDate() + " to " + leaves.getToDate();
    }

    private void sendNotesRelatedNotifications()
    {
        mCurrentNotification.setLeaveId(null);
        String notesId = "";
        switch (notes.getNotesStatus())
        {
            case Notes.REJECTED:
                notesId = "P";
                setDialogTitle(R.string.reviewerComment);
                mCurrentNotification.setMessage(getNotesRejectionMsg(notes));
                mNotificationDbRef = notificationsRootRef.child(notes.getSubmitterId());
                break;

            case Notes.RE_SUBMITTED:
                notesId = "P";
                setDialogTitle(R.string.notificationMessage);
                mCurrentNotification.setMessage(getNotesReSubmitMsg(notes));
                mNotificationDbRef = notificationsRootRef.child(notes.getReviewerId());
                mMessage.setText(mCurrentNotification.getMessage());
                break;

            case Notes.APPROVED:
                notesId = Notes.REVIEWED;
                setDialogTitle(R.string.notificationMessage);
                mCurrentNotification.setMessage(getNotesApprovalMsg(notes));
                mNotificationDbRef = notificationsRootRef.child(notes.getSubmitterId());
                mMessage.setText(mCurrentNotification.getMessage());
                break;

            case Notes.REVIEW:
                notesId = Notes.PENDING;
                setDialogTitle(R.string.notificationMessage);
                mCurrentNotification.setMessage(getNotesReviewMsg(notes));
                mNotificationDbRef = notificationsRootRef.child(notes.getReviewerId());
                mMessage.setText(mCurrentNotification.getMessage());
                break;
        }
        notesId += "_" + notes.getClassSubId() + "_" + notes.dateTime();
        mCurrentNotification.setNotesId(notesId);
    }

    private String getNotesReSubmitMsg(Notes notes)
    {
        return notes.getSubmitterName() + " " + mContext.getString(R.string.has_re_submitted_following_notes_for_review)
                + "\n\"" + notes.getNotesTitle() + "\"\n";
    }

    private String getNotesReviewMsg(Notes notes)
    {
        return notes.getSubmitterName() + " " + mContext.getString(R.string.has_submitted_following_notes_for_review)
                + "\n\"" + notes.getNotesTitle() + "\"\n";
    }

    private String getNotesApprovalMsg(Notes notes)
    {
        return mContext.getString(R.string.following_notes_has_been_approved)
                + "\n\"" + notes.getNotesTitle() + "\"\n";
    }

    String getNotesRejectionMsg(Notes notes)
    {
        return mContext.getString(R.string.following_notes_has_been_rejected)
                + "\n\"" + notes.getNotesTitle() + "\"\n";
    }

    public void sendNotification()
    {
        Progress.show(R.string.sending_notification);
        String key = DateTimeUtil.getKey();
        mCurrentNotification.dateTime(key);

        mNotificationDbRef.child(key).setValue(mCurrentNotification).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                Progress.hide();
                if (task.isSuccessful())
                {
                    ToastMsg.show(R.string.sent);
                }
                else
                {
                    ToastMsg.show(R.string.notification_couldnt_be_sent);
                }

                if (NotificationDialogFragment.this.isVisible())
                {
                    dismiss();
                }
            }
        });
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);
        listener.onDismiss(notification);
    }
}