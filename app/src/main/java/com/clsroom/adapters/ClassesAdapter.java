package com.clsroom.adapters;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.VolleyError;
import com.clsroom.R;
import com.clsroom.dialogs.AddOrEditClassDialogFragment;
import com.clsroom.fragments.StudentsListFragment;
import com.clsroom.listeners.FragmentLauncher;
import com.clsroom.listeners.ResultListener;
import com.clsroom.model.Classes;
import com.clsroom.model.Progress;
import com.clsroom.model.Snack;
import com.clsroom.model.ToastMsg;
import com.clsroom.model.User;
import com.clsroom.utils.ConnectivityUtil;
import com.clsroom.utils.NavigationUtil;
import com.clsroom.utils.VolleyUtil;
import com.clsroom.viewholders.ClassesViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static com.clsroom.model.User.EMAIL_NOT_SENT;
import static com.clsroom.model.User.EMAIL_SENT;
import static com.clsroom.model.User.GENERATE_USER_LIST_URL;
import static com.clsroom.model.User.INVALID_EMAIL;
import static com.clsroom.model.User.INVALID_TOKEN;

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
                launcher.addFragment(StudentsListFragment.getInstance(model),
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
                            case R.id.generate_credential:
                                generateCredentialList(classes);
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

    private void generateCredentialList(Classes classes)
    {
        if (NavigationUtil.isAdmin)
        {
            Progress.show(R.string.generating);
            Map<String, String> data = new HashMap<>();
            data.put(User.UID, NavigationUtil.mCurrentUser.getUserId());
            data.put(User.CLASS_ID, classes.getCode());
            data.put(User.CLASS_NAME, classes.getName());
            data.put(User.TOKEN, NavigationUtil.mCurrentUser.getToken());

            try
            {
                VolleyUtil.sendGetData(launcher.getActivity(), GENERATE_USER_LIST_URL, data, new ResultListener<String>()
                {
                    @Override
                    public void onSuccess(String result)
                    {
                        Progress.hide();
                        switch (result.trim())
                        {
                            case EMAIL_NOT_SENT:
                                ToastMsg.show(R.string.there_was_some_issue_in_generating_user_credentials);
                                break;
                            case EMAIL_SENT:
                                ToastMsg.show(R.string.list_has_been_mailed_to_your_registered_mail_id);
                                break;
                            case INVALID_EMAIL:
                                ToastMsg.show(R.string.your_registered_mail_id_is_invalid);
                                break;
                            case INVALID_TOKEN:
                                ToastMsg.show(R.string.you_are_not_authorized_to_generate_the_list);
                                break;
                        }
                    }

                    @Override
                    public void onError(VolleyError error)
                    {
                        Progress.hide();
                        ToastMsg.show(R.string.you_are_not_authorized_to_generate_the_list);
                    }
                });
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            ToastMsg.show(R.string.you_are_not_authorized_to_generate_the_list);
        }
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
