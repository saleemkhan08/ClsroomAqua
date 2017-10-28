package com.clsroom.adapters;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.clsroom.R;
import com.clsroom.fragments.LeavesFragment;
import com.clsroom.fragments.SingleNotesFragment;
import com.clsroom.listeners.FragmentLauncher;
import com.clsroom.model.Notifications;
import com.clsroom.model.Progress;
import com.clsroom.model.ToastMsg;
import com.clsroom.utils.ImageUtil;
import com.clsroom.utils.NavigationUtil;
import com.clsroom.viewholders.NotificationViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import static com.clsroom.model.Leaves.REQUESTED_LEAVES;


public class NotificationsAdapter extends FirebaseRecyclerAdapter<Notifications, NotificationViewHolder>
{
    private static final String TAG = "NotificationsAdapter";
    private DatabaseReference mNotificationRef;
    private FragmentLauncher launcher;

    public static NotificationsAdapter getInstance(DatabaseReference reference, FragmentLauncher launcher)
    {
        Log.d("NotificationListIssue", "NotificationsAdapter getInstance: reference : " + reference);
        NotificationsAdapter fragment = new NotificationsAdapter(Notifications.class,
                R.layout.notification_list_row, NotificationViewHolder.class, reference.orderByChild("dateTime"));
        fragment.mNotificationRef = reference;
        fragment.launcher = launcher;
        return fragment;
    }

    private NotificationsAdapter(Class<Notifications> modelClass, int modelLayout,
                                 Class<NotificationViewHolder> viewHolderClass, Query ref)
    {
        super(modelClass, modelLayout, viewHolderClass, ref);
        Log.d("NotificationListIssue", "NotificationsAdapter Constructor");
    }

    @Override
    protected void populateViewHolder(final NotificationViewHolder viewHolder, final Notifications model, int position)
    {
        Log.d("NotificationListIssue", "NotificationsAdapter populateViewHolder : " + position);
        String imageUrl = model.getSenderPhotoUrl();
        ImageUtil.loadCircularImg(viewHolder.itemView.getContext(), imageUrl, viewHolder.senderImage);

        viewHolder.senderName.setText(model.getSenderName());
        viewHolder.message.setText(model.getMessage());
        viewHolder.dateTime.setText(model.displayDate());

        Log.d("NotificationListIssue", "NotificationsAdapter getSenderName : " + model.getSenderName());
        Log.d("NotificationListIssue", "NotificationsAdapter getMessage : " + model.getMessage());
        Log.d("NotificationListIssue", "NotificationsAdapter displayDate : " + model.displayDate());
        viewHolder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.d("NotificationListIssue", "NotificationsAdapter onClick : getLeaveId :" + model.getLeaveId());
                Log.d("NotificationListIssue", "NotificationsAdapter onClick : getLeaveRefType : " + model.getLeaveRefType());
                Log.d("NotificationListIssue", "NotificationsAdapter onClick : getNotesId : " + model.getNotesId());

                String leaveId = model.getLeaveId();
                String leaveRefType = model.getLeaveRefType();
                String notesId = model.getNotesId();
                if (leaveId != null && leaveRefType != null)
                {
                    String userId = model.getLeaveRefType().equals(REQUESTED_LEAVES) ?
                            model.getSenderId() : NavigationUtil.mCurrentUser.getUserId();

                    launcher.showFragment(LeavesFragment
                                    .getInstance(leaveId, userId, leaveRefType),
                            true, NavigationUtil.LEAVES_LIST_FRAGMENT);
                }
                else if (notesId != null)
                {
                    SingleNotesFragment fragment = SingleNotesFragment.getInstance(notesId);
                    if (fragment.mNotesDbRef != null)
                    {
                        launcher.showFragment(fragment, true, NavigationUtil.SINGLE_NOTES_FRAGMENT);
                    }
                    else
                    {
                        ToastMsg.show(R.string.notes_doesnt_exists);
                    }
                }
            }
        });

        viewHolder.deleteNotification.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Progress.show(R.string.deleting);
                Log.d("NotificationListIssue", "NotificationsAdapter onClick : deleteNotification");
                mNotificationRef.child(model.dateTime()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        Progress.hide();
                        if (task.isSuccessful())
                        {
                            ToastMsg.show(R.string.deleted);
                        }
                        else
                        {
                            ToastMsg.show(R.string.please_try_again);
                        }
                    }
                });
            }
        });

    }
}