package com.clsroom.dialogs;

import android.content.DialogInterface;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.clsroom.R;
import com.clsroom.adapters.RequestedLeavesAdapter;
import com.clsroom.model.Leaves;
import com.clsroom.model.ToastMsg;
import com.clsroom.utils.NavigationUtil;
import com.clsroom.utils.Otto;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RequestedLeavesDialogFragment extends CustomDialogFragment implements ValueEventListener
{
    public static final String TAG = "RequestedLeavesDialogFragment";

    @BindView(R.id.recyclerView)
    RecyclerView mRequestedLeavesRecyclerView;

    @BindView(R.id.errorMsg)
    TextView mErrorMsg;

    @BindView(R.id.recyclerProgress)
    View mProgress;

    private DatabaseReference mRequestedLeavesDbRef;
    private Leaves mLeave;
    private RequestedLeavesAdapter adapter;

    public RequestedLeavesDialogFragment()
    {

    }

    public static RequestedLeavesDialogFragment getInstance()
    {
        return new RequestedLeavesDialogFragment();
    }

    @Override
    public void onCreateView(View parentView)
    {
        ButterKnife.bind(this, parentView);
        mRequestedLeavesDbRef = FirebaseDatabase.getInstance().getReference()
                .child(Leaves.LEAVES).child(NavigationUtil.mCurrentUser.getUserId())
                .child(Leaves.REQUESTED_LEAVES);
        mRequestedLeavesDbRef.addValueEventListener(this);
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
        setDialogTitle(R.string.requested_leaves);
        mErrorMsg.setText(R.string.noLeavesFound);
        adapter = RequestedLeavesAdapter.getInstance(mRequestedLeavesDbRef);
        mRequestedLeavesRecyclerView.setAdapter(adapter);
        mRequestedLeavesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
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
            if (mLeave != null)
            {
                int index = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    if (snapshot.getValue(Leaves.class).equals(mLeave))
                    {
                        mRequestedLeavesRecyclerView.scrollToPosition(index);
                        break;
                    }
                    index++;
                }
            }
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError)
    {
        mProgress.setVisibility(View.GONE);
        ToastMsg.show(R.string.couldntLoadTheList);
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);
        Otto.unregister(this);
    }

    public static RequestedLeavesDialogFragment getInstance(Leaves mLeave)
    {
        RequestedLeavesDialogFragment fragment = getInstance();
        fragment.mLeave = mLeave;
        return fragment;
    }
}