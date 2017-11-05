package com.clsroom.adapters;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;

import com.clsroom.R;
import com.clsroom.dialogs.NotificationDialogFragment;
import com.clsroom.model.Leaves;
import com.clsroom.model.Staff;
import com.clsroom.model.Students;
import com.clsroom.model.User;
import com.clsroom.viewholders.RequestedLeavesViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class RequestedLeavesAdapter extends FirebaseRecyclerAdapter<Leaves, RequestedLeavesViewHolder>
{
    private static final String TAG = "RequestedLeavesAdapter";
    private DatabaseReference leavesDbRef;

    public static RequestedLeavesAdapter getInstance(DatabaseReference reference)
    {
        return new RequestedLeavesAdapter(Leaves.class,
                R.layout.requested_leave_row, RequestedLeavesViewHolder.class, reference.orderByChild(Leaves.REQUESTED_LEAVES_KEY));
    }

    private RequestedLeavesAdapter(Class<Leaves> modelClass, int modelLayout,
                                   Class<RequestedLeavesViewHolder> viewHolderClass,
                                   Query ref)
    {
        super(modelClass, modelLayout, viewHolderClass, ref);
        leavesDbRef = FirebaseDatabase.getInstance().getReference().child(Leaves.LEAVES);
    }

    @Override
    protected void populateViewHolder(final RequestedLeavesViewHolder viewHolder, final Leaves model, int position)
    {
        loadUserDetails(viewHolder, model);
        Context context = viewHolder.itemView.getContext();
        String status = context.getString(R.string.pending);
        switch (model.getStatus())
        {
            case Leaves.STATUS_APPLIED:
                configureOptions(viewHolder, model);
                viewHolder.statusIcon.setVisibility(View.GONE);
                viewHolder.optionsIconContainer.setVisibility(View.VISIBLE);
                status = context.getString(R.string.pending);
                break;
            case Leaves.STATUS_APPROVED:
                viewHolder.optionsIconContainer.setVisibility(View.GONE);
                viewHolder.statusIcon.setVisibility(View.VISIBLE);
                viewHolder.statusIcon.setImageResource(R.drawable.ic_check_black_48dp);
                status = context.getString(R.string.approved);
                break;
            case Leaves.STATUS_REJECTED:
                viewHolder.optionsIconContainer.setVisibility(View.GONE);
                viewHolder.statusIcon.setVisibility(View.VISIBLE);
                viewHolder.statusIcon.setImageResource(R.drawable.ic_clear_black_48dp);
                status = context.getString(R.string.rejected);
                break;
        }

        viewHolder.requestMessage.setText(
                context.getString(R.string.date)
                        + " : " + model.getFromDate()
                        + " " + context.getString(R.string.to)
                        + " " + model.getToDate()
                        + "\n" + context.getString(R.string.reason)
                        + " : " + model.getReason()
                        + "\n" + context.getString(R.string.status)
                        + " : " + status);
    }

    private void loadUserDetails(final RequestedLeavesViewHolder viewHolder, Leaves model)
    {
        DatabaseReference ref = User.getRef(model.getRequesterId());
        ref.addListenerForSingleValueEvent(new ValueEventListener()
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
                viewHolder.mImageView.setImageURI(user.getPhotoUrl());
                viewHolder.mFullName.setText(user.getFullName());

                Context context = viewHolder.itemView.getContext();

                viewHolder.mDesignation.setText(user instanceof Students ?
                        context.getString(R.string.classTxt) + " : " + ((Students) user).getClassName()
                        : context.getString(R.string.designation) + " : " + ((Staff) user).getDesignation());

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void configureOptions(final RequestedLeavesViewHolder holder, final Leaves leaves)
    {
        holder.optionsIconContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                PopupMenu popup = new PopupMenu(holder.itemView.getContext(), v);
                popup.getMenuInflater()
                        .inflate(R.menu.leaves_options, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    public boolean onMenuItemClick(MenuItem item)
                    {
                        switch (item.getItemId())
                        {
                            case R.id.action_approve:
                                leaves.setStatus(Leaves.STATUS_APPROVED);
                                break;
                            case R.id.action_reject:
                                leaves.setStatus(Leaves.STATUS_REJECTED);
                                break;
                        }
                        updateStatus(leaves, holder.itemView.getContext());
                        return true;
                    }
                });
                popup.show();
            }
        });
    }

    private void updateStatus(Leaves leave, Context context)
    {
        leavesDbRef.child(leave.getRequesterId())
                .child(Leaves.MY_LEAVES)
                .child(Leaves.getDbKeyDate(leave.getFromDate()))
                .child(Leaves.STATUS).setValue(leave.getStatus());

        leavesDbRef.child(leave.getApproverId())
                .child(Leaves.REQUESTED_LEAVES)
                .child(leave.requestedLeaveKey())
                .child(Leaves.STATUS).setValue(leave.getStatus());

        NotificationDialogFragment.getInstance(leave).sendLeavesRelatedNotification(context);
    }
}
