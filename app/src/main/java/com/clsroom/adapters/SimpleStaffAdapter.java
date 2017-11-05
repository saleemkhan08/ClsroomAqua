package com.clsroom.adapters;

import android.view.View;

import com.clsroom.R;
import com.clsroom.model.Staff;
import com.clsroom.utils.Otto;
import com.clsroom.viewholders.StaffViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class SimpleStaffAdapter extends FirebaseRecyclerAdapter<Staff, StaffViewHolder>
{
    private static final String TAG = "SimpleStaffAdapter";

    public static SimpleStaffAdapter getInstance(DatabaseReference reference)
    {
        SimpleStaffAdapter adapter = new SimpleStaffAdapter(Staff.class,
                R.layout.staff_list_row, StaffViewHolder.class, reference.orderByChild(Staff.USER_ID));
        return adapter;
    }

    private SimpleStaffAdapter(Class<Staff> modelClass, int modelLayout, Class<StaffViewHolder> viewHolderClass,
                               Query ref)
    {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    @Override
    protected void populateViewHolder(final StaffViewHolder viewHolder, final Staff model, int position)
    {
        viewHolder.mImageView.setImageURI(model.getPhotoUrl());
        viewHolder.mFullName.setText(model.getFullName());
        viewHolder.mUserId.setText(model.getUserId());
        viewHolder.mDesignation.setText(model.getDesignation());
        viewHolder.optionsIconContainer.setVisibility(View.GONE);
        viewHolder.mAdminImageView.setVisibility(model.getIsAdmin() ? View.VISIBLE : View.GONE);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Otto.post(model);
            }
        });

    }
}
