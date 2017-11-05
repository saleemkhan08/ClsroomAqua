package com.clsroom.dialogs;

import android.content.DialogInterface;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.clsroom.R;
import com.clsroom.adapters.SimpleStaffAdapter;
import com.clsroom.model.Staff;
import com.clsroom.model.ToastMsg;
import com.clsroom.utils.Otto;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.otto.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SelectStaffDialogFragment extends CustomDialogFragment implements ValueEventListener
{
    public static final String TAG = "SelectStaffDialogFragment";

    @BindView(R.id.recyclerView)
    RecyclerView mStaffListRecyclerView;

    @BindView(R.id.errorMsg)
    TextView mErrorMsg;

    @BindView(R.id.recyclerProgress)
    View mProgress;

    private DatabaseReference mStaffDbRef;

    public SelectStaffDialogFragment()
    {

    }

    public static SelectStaffDialogFragment getInstance()
    {
        return new SelectStaffDialogFragment();
    }

    @Override
    public void onCreateView(View parentView)
    {
        ButterKnife.bind(this, parentView);
        mStaffDbRef = FirebaseDatabase.getInstance().getReference().child(Staff.STAFF);
        mStaffDbRef.addValueEventListener(this);
        Otto.register(this);
    }

    @Override
    protected int getContentViewLayoutRes()
    {
        return R.layout.fragment_list;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        setDialogTitle(R.string.selectStaff);
        mErrorMsg.setText(R.string.noStaffFound);
        mStaffListRecyclerView.setAdapter(SimpleStaffAdapter.getInstance(mStaffDbRef));
        mStaffListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot)
    {
        mProgress.setVisibility(View.GONE);
        if (dataSnapshot.getChildrenCount() <= 0)
        {
            mErrorMsg.setVisibility(View.VISIBLE);
        }
        else
        {
            mErrorMsg.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError)
    {
        mProgress.setVisibility(View.GONE);
        ToastMsg.show(R.string.couldntLoadTheList);
    }

    @Subscribe
    public void close(Staff staff)
    {
        dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);
        Otto.unregister(this);
    }
}