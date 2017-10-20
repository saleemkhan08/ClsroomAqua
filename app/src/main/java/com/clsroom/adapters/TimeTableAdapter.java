package com.clsroom.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import com.clsroom.R;
import com.clsroom.dialogs.AddOrEditPeriodDialogFragment;
import com.clsroom.listeners.FragmentLauncher;
import com.clsroom.model.Progress;
import com.clsroom.model.TimeTable;
import com.clsroom.model.ToastMsg;
import com.clsroom.utils.ActionBarUtil;
import com.clsroom.utils.ImageUtil;
import com.clsroom.utils.NavigationDrawerUtil;
import com.clsroom.utils.Otto;
import com.clsroom.viewholders.TimeTableViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import static com.clsroom.utils.ActionBarUtil.SHOW_INDEPENDENT_TIME_TABLE_MENU;

public class TimeTableAdapter extends FirebaseRecyclerAdapter<TimeTable, TimeTableViewHolder>
{
    private static final String TAG = "TimeTableAdapter";
    private FragmentLauncher launcher;
    public static boolean isSelectionEnabled;
    public ArrayList<String> mSelectedPeriods;
    public Query mTimeTableRef;

    private boolean isSelectAll;

    public static TimeTableAdapter getInstance(DatabaseReference reference, FragmentLauncher launcher)
    {
        Log.d(TAG, "TimeTableAdapter getInstance: reference : " + reference);
        TimeTableAdapter fragment = new TimeTableAdapter(TimeTable.class,
                R.layout.time_table_row, TimeTableViewHolder.class, reference, launcher);
        fragment.mTimeTableRef = reference.orderByChild(TimeTable.START_TIME);
        return fragment;
    }

    private TimeTableAdapter(Class<TimeTable> modelClass, int modelLayout, Class<TimeTableViewHolder> viewHolderClass,
                             Query ref, FragmentLauncher launcher)
    {
        super(modelClass, modelLayout, viewHolderClass, ref);
        Log.d(TAG, "TimeTableAdapter Constructor");
        this.launcher = launcher;
    }

    @Override
    protected void populateViewHolder(final TimeTableViewHolder viewHolder, final TimeTable model, int position)
    {
        Log.d(TAG, "populateViewHolder : " + position);
        String imageUrl = model.getTeacherPhotoUrl();
        ImageUtil.loadCircularImg(viewHolder.itemView.getContext(), imageUrl, viewHolder.mTeacherImage);

        viewHolder.mSubjectName.setText(model.getSubjectName());
        viewHolder.mClassTeacherName.setText(model.getTeacherName());

        viewHolder.mPeriodTime.setText(model.getStartTime() + " - " + model.getEndTime());
        viewHolder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "onClick");
            }
        });

        if (NavigationDrawerUtil.isAdmin)
        {
            Log.d(TAG, this + " : isSelectionEnabled : " + isSelectionEnabled);
            viewHolder.mCheckBox.setVisibility(isSelectionEnabled ? View.VISIBLE : View.GONE);
            viewHolder.mOptionsIconContainer.setVisibility(isSelectionEnabled ? View.GONE : View.VISIBLE);
            viewHolder.mCheckBox.setChecked(isSelectAll);
            viewHolder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked)
                {
                    Log.d(TAG, "onCheckedChanged : " + isChecked);
                    if (isChecked)
                    {
                        mSelectedPeriods.add(model.getStartTimeKey());
                    }
                    else
                    {
                        mSelectedPeriods.remove(model.getStartTimeKey());
                    }
                }
            });
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View view)
                {
                    Log.d(TAG, this + ", onLongClick, isSelectionEnabled : " + isSelectionEnabled);
                    Otto.post(ActionBarUtil.SHOW_MULTIPLE_TIME_TABLE_MENU);
                    enableSelection();
                    return true;
                }
            });
            configureOptions(viewHolder, model);
        }
        else
        {
            viewHolder.mCheckBox.setVisibility(View.GONE);
            viewHolder.mOptionsIconContainer.setVisibility(View.GONE);
        }
    }

    private void enableSelection()
    {
        isSelectionEnabled = true;
        mSelectedPeriods = new ArrayList<>();
        Otto.register(TimeTableAdapter.this);
        notifyDataSetChanged();
    }

    @Subscribe
    public void reload(String str)
    {
        Log.d(TAG, "reload : " + str);
        if (str.equals(SHOW_INDEPENDENT_TIME_TABLE_MENU))
        {
            notifyDataSetChanged();
            Otto.unregister(this);
        }
    }

    private void configureOptions(final TimeTableViewHolder holder, final TimeTable timeTable)
    {
        holder.mOptionsIconContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                PopupMenu popup = new PopupMenu(launcher.getActivity(), v);
                popup.getMenuInflater()
                        .inflate(R.menu.classes_options, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    public boolean onMenuItemClick(MenuItem item)
                    {
                        switch (item.getItemId())
                        {
                            case R.id.action_edit:
                                editClasses(timeTable);
                                break;
                            case R.id.action_delete:
                                confirmDelete(timeTable);
                                break;
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });
    }

    public void setSelectAll()
    {
        isSelectAll = !isSelectAll;
        notifyDataSetChanged();
    }

    private void confirmDelete(TimeTable timeTable)
    {
        Progress.show(R.string.deleting);
        mTimeTableRef.getRef().child(timeTable.getStartTimeKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
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

    private void editClasses(TimeTable timeTable)
    {
        AddOrEditPeriodDialogFragment fragment = AddOrEditPeriodDialogFragment.getInstance(timeTable);
        fragment.show(launcher.getSupportFragmentManager(), AddOrEditPeriodDialogFragment.TAG);
    }
}