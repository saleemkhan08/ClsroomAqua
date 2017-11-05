package com.clsroom.dialogs;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.clsroom.R;
import com.clsroom.model.Leaves;
import com.clsroom.model.Progress;
import com.clsroom.utils.NavigationUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.clsroom.model.Leaves.MY_LEAVES;
import static com.clsroom.model.Leaves.REQUESTED_LEAVES;

public class LeavesDetailDialogFragment extends CustomDialogFragment implements ValueEventListener
{
    public static final String TAG = "LeavesDetailDialogFragment";


    @BindView(R.id.reason)
    TextView mReason;

    @BindView(R.id.fromDate)
    TextView mFromDate;

    @BindView(R.id.toDate)
    TextView mToDate;

    @BindView(R.id.approver)
    TextView mApprover;

    @BindView(R.id.status)
    TextView mStatus;

    String mUserId;
    String mDate;
    private DatabaseReference mMyLeavesDbRef;
    private Leaves mLeave;
    private DatabaseReference mLeavesRootDbRef;

    public static LeavesDetailDialogFragment getInstance(String userId, String date)
    {
        LeavesDetailDialogFragment fragment = new LeavesDetailDialogFragment();
        fragment.mUserId = userId;
        fragment.mDate = date;
        return fragment;
    }

    public LeavesDetailDialogFragment()
    {

    }

    @Override
    public void onCreateView(View parentView)
    {
        ButterKnife.bind(this, parentView);
        mLeavesRootDbRef = FirebaseDatabase.getInstance().getReference().child(Leaves.LEAVES);
        mMyLeavesDbRef = mLeavesRootDbRef.child(mUserId).child(MY_LEAVES).child(mDate);
        mMyLeavesDbRef.addListenerForSingleValueEvent(this);
        if (mUserId.equals(NavigationUtil.mCurrentUser.getUserId()))
        {
            setSubmitBtnTxt(R.string.delete);
            setSubmitBtnImg(R.mipmap.cancel_all_button);
        }
    }

    @Override
    protected int getContentViewLayoutRes()
    {
        return R.layout.fragment_leave_details;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        setDialogTitle(R.string.leaveDetails);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot)
    {
        mLeave = dataSnapshot.getValue(Leaves.class);
        if (mLeave != null)
        {
            mReason.setText(mLeave.getReason());
            mFromDate.setText(mLeave.getFromDate());
            mToDate.setText(mLeave.getToDate());
            mApprover.setText(mLeave.getApproverId());
            mStatus.setText(getString(mLeave.statusText()));
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError)
    {

    }

    @Override
    public void submit(View view)
    {
        super.submit(view);
        Progress.show(R.string.deleting);
        mLeave.setStatus(Leaves.STATUS_CANCELLED);
        NotificationDialogFragment.getInstance(mLeave).sendLeavesRelatedNotification(getActivity());

        DatabaseReference requestedLeavesDbRef = mLeavesRootDbRef.child(mLeave.getApproverId()).child(REQUESTED_LEAVES)
                .child(mLeave.requestedLeaveKey());
        requestedLeavesDbRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    mMyLeavesDbRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                Progress.hide();
                                dismiss();
                            }
                        }
                    });
                }
            }
        });
    }
}