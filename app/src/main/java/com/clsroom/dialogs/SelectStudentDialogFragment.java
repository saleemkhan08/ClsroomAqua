package com.clsroom.dialogs;

import android.content.DialogInterface;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.otto.Subscribe;
import com.clsroom.R;
import com.clsroom.adapters.SimpleStudentsAdapter;
import com.clsroom.model.Classes;
import com.clsroom.model.Students;
import com.clsroom.model.ToastMsg;
import com.clsroom.utils.Otto;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SelectStudentDialogFragment extends CustomDialogFragment implements ValueEventListener
{
    public static final String TAG = "SelectStudentDialogFragment";

    @Bind(R.id.recyclerView)
    RecyclerView mStudentListRecyclerView;

    @Bind(R.id.errorMsg)
    TextView mErrorMsg;

    @Bind(R.id.recyclerProgress)
    View mProgress;


    public static SelectStudentDialogFragment getInstance()
    {
        return new SelectStudentDialogFragment();
    }

    public SelectStudentDialogFragment()
    {

    }

    @Override
    public void onCreateView(View parentView)
    {
        ButterKnife.bind(this, parentView);
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
        setDialogTitle(R.string.selectStudent);
        mErrorMsg.setText(R.string.noStudentsFound);
        showClassesTab();
        setContainerHeight();
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

    @Subscribe
    public void close(Students student)
    {
        dismiss();
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

    @Override
    public void onTabSelected(TabLayout.Tab tab)
    {
        super.onTabSelected(tab);
        Classes mClass = (Classes) tab.getTag();
        DatabaseReference mStudentsDbRef = FirebaseDatabase.getInstance().getReference().child(Students.STUDENTS).child(mClass.getCode());
        mStudentsDbRef.addValueEventListener(this);
        mStudentListRecyclerView.setAdapter(SimpleStudentsAdapter.getInstance(mStudentsDbRef));
        mStudentListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mProgress.setVisibility(View.VISIBLE);
    }
}