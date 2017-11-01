package com.clsroom.adapters;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;

import com.clsroom.R;
import com.clsroom.dialogs.AddOrEditClassDialogFragment;
import com.clsroom.fragments.StudentsListFragment;
import com.clsroom.listeners.FragmentLauncher;
import com.clsroom.model.Classes;
import com.clsroom.model.Progress;
import com.clsroom.model.Snack;
import com.clsroom.model.ToastMsg;
import com.clsroom.utils.ConnectivityUtil;
import com.clsroom.utils.NavigationUtil;
import com.clsroom.viewholders.ClassesViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class ClassesAdapter extends FirebaseRecyclerAdapter<Classes, ClassesViewHolder>
{
    private FragmentLauncher launcher;
    private DatabaseReference mClassesDbRef;

    public static ClassesAdapter getInstance(DatabaseReference reference, FragmentLauncher launcher)
    {
        ClassesAdapter adapter = new ClassesAdapter(Classes.class,
                R.layout.class_list_row, ClassesViewHolder.class, reference.orderByChild(Classes.CODE), launcher);
        adapter.mClassesDbRef = reference;
        return adapter;
    }

    private ClassesAdapter(Class<Classes> modelClass, int modelLayout, Class<ClassesViewHolder> viewHolderClass,
                           Query ref, FragmentLauncher launcher)
    {
        super(modelClass, modelLayout, viewHolderClass, ref);
        this.launcher = launcher;
    }

    @Override
    protected void populateViewHolder(final ClassesViewHolder viewHolder, final Classes model, int position)
    {
        viewHolder.mStudentCount.setText(getString(R.string.studentCount) + " " + model.getStudentCount());
        viewHolder.mClassName.setText(model.getName());
        viewHolder.mClassTeacher.setText(getString(R.string.classTeacher) + " " + model.getClassTeacherName());
        viewHolder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                launcher.replaceFragment(StudentsListFragment.getInstance(model),
                        true, NavigationUtil.STUDENTS_LIST_FRAGMENT);
            }
        });
        configureOptions(viewHolder, model);
    }

    private void configureOptions(final ClassesViewHolder holder, final Classes classes)
    {
        holder.optionsIconContainer.setOnClickListener(new View.OnClickListener()
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
                                editClasses(classes);
                                break;
                            case R.id.action_delete:
                                confirmDelete(classes);
                                break;
//                            case R.id.action_send_notification:
//                                sendNotification(classes);
//                                break;
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });
    }

    private void confirmDelete(Classes classes)
    {
        Progress.show(R.string.deleting);
        mClassesDbRef.child(classes.getCode()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
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

    private void editClasses(Classes classes)
    {
        if (ConnectivityUtil.isConnected(launcher.getActivity()))
        {
            FragmentManager manager = launcher.getSupportFragmentManager();
            AddOrEditClassDialogFragment fragment = AddOrEditClassDialogFragment.getInstance(classes);
            fragment.show(manager, AddOrEditClassDialogFragment.TAG);
        }
        else
        {
            Snack.show(R.string.noInternet);
        }
    }

    private String getString(int resId)
    {
        if (launcher != null)
        {
            return launcher.getString(resId);
        }
        return "";
    }
}
