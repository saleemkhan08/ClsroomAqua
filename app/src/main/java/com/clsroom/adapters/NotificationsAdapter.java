package com.clsroom.adapters;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.clsroom.R;
import com.clsroom.fragments.LeavesFragment;
import com.clsroom.listeners.FragmentLauncher;
import com.clsroom.model.Notifications;
import com.clsroom.model.Progress;
import com.clsroom.model.ToastMsg;
import com.clsroom.utils.ImageUtil;
import com.clsroom.utils.NavigationDrawerUtil;
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
        Log.d(TAG, "SubjectsAdapter getInstance: reference : " + reference);

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
        Log.d(TAG, "SubjectsAdapter Constructor");
    }

    @Override
    protected void populateViewHolder(final NotificationViewHolder viewHolder, final Notifications model, int position)
    {
        Log.d(TAG, "populateViewHolder : " + position);
        String imageUrl = model.getSenderPhotoUrl();
        ImageUtil.loadCircularImg(viewHolder.itemView.getContext(), imageUrl, viewHolder.senderImage);

        viewHolder.senderName.setText(model.getSenderName());
        viewHolder.message.setText(model.getMessage());
        viewHolder.dateTime.setText(model.displayDate());

        viewHolder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String leaveId = model.getLeaveId();
                String leaveRefType = model.getLeaveRefType();
                String notesId = model.getNotesId();
                String userId = model.getLeaveRefType().equals(REQUESTED_LEAVES) ?
                        model.getSenderId() : NavigationDrawerUtil.mCurrentUser.getUserId();
                if (leaveId != null && leaveRefType != null)
                {
                    launcher.showFragment(LeavesFragment
                                    .getInstance(leaveId, userId, leaveRefType),
                            true, LeavesFragment.TAG);
                }
                else if (notesId != null)
                {
                    //Notes link
                }
            }
        });

        viewHolder.deleteNotification.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Progress.show(R.string.deleting);
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