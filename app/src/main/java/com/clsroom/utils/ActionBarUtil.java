package com.clsroom.utils;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuInflater;

import com.clsroom.R;
import com.squareup.otto.Subscribe;

public class ActionBarUtil
{
    public static final String NO_MENU = "showClassesMenu";
    public static final String SHOW_STUDENTS_MENU_FOR_ADMIN = "showIndependentStudentsMenu";
    public static final String SHOW_MULTIPLE_STUDENT_MENU = "showMultipleStudentMenu";
    public static final String SHOW_INDEPENDENT_SUBJECT_MENU = "showIndependentSubjectMenu";
    public static final String SHOW_MULTIPLE_SUBJECT_MENU = "showMultipleSubjectMenu";
    public static final String SHOW_ATTENDANCE_MENU = "showAttendanceMenu";
    public static final String SHOW_ADMIN_LEAVES_MENU = "showAdminLeavesMenu";
    public static final String SHOW_INDEPENDENT_TIME_TABLE_MENU = "showIndependentTimeTableMenu";
    public static final String SHOW_MULTIPLE_TIME_TABLE_MENU = "showMultipleTimeTableMenu";
    public static final String SHOW_INDEPENDENT_NOTES_MENU = "showIndependentNotesMenu";
    public static final String SHOW_PENDING_NOTES_FRAGMENT_MENU = "showPendingNotesFragmentMenu";
    public static final String SHOW_NOTES_FRAGMENT_MENU = "showNotesFragmentMenu";
    public static final String SHOW_NOTIFICATIONS_MENU = "showNotificationsMenu";
    public static final String SHOW_SELECTED_NOTIFICATIONS_MENU = "showSelectedNotificationsMenu";
    public static final String SHOW_PROFILE_MENU = "showProfileMenu";
    public static final String SHOW_STUDENTS_MENU_FOR_TEACHERS = "showStudentsMenuForTeachers";
    public static final String SHOW_SINGLE_NOTES_MENU = "showSingleNotesMenu";
    public static final String SHOW_STAFF_TIME_TABLE_OPTION = "showStaffTimeTableOptions";
    public static final String SHOW_CLASS_TIME_TABLE_OPTION = "showClassTimeTableOptions";
    private MenuInflater mMenuInflater;
    private Menu mMenu;
    public static final String SHOW_MULTIPLE_STAFF_MENU = "showMultipleStaffMenu";
    public static final String SHOW_INDEPENDENT_STAFF_MENU = "showIndependentStaffMenu";

    public ActionBarUtil(Activity activity, Menu menu)
    {
        mMenuInflater = activity.getMenuInflater();
        mMenu = menu;
        Otto.register(this);
    }

    @Subscribe
    public void handleActionBar(String menuName)
    {
        switch (menuName)
        {
            case SHOW_MULTIPLE_STAFF_MENU:
                loadNewMenu(R.menu.multiple_staff_menu);
                break;
            case SHOW_INDEPENDENT_STAFF_MENU:
                loadNewMenu(R.menu.independent_staff_menu);
                break;
            case SHOW_MULTIPLE_STUDENT_MENU:
                loadNewMenu(R.menu.multiple_student_menu);
                break;
            case SHOW_STUDENTS_MENU_FOR_ADMIN:
                loadNewMenu(R.menu.student_menu_for_admin);
                break;
            case SHOW_STUDENTS_MENU_FOR_TEACHERS:
                loadNewMenu(R.menu.student_menu_for_teachers);
                break;
            case SHOW_MULTIPLE_SUBJECT_MENU:
                loadNewMenu(R.menu.multiple_subject_menu);
            case SHOW_MULTIPLE_TIME_TABLE_MENU:
                loadNewMenu(R.menu.multiple_period_menu);
                break;
            case SHOW_ATTENDANCE_MENU:
                loadNewMenu(R.menu.attendance_menu);
                break;
            case SHOW_ADMIN_LEAVES_MENU:
                loadNewMenu(R.menu.admin_leaves_menu);
                break;
            case SHOW_PENDING_NOTES_FRAGMENT_MENU:
                loadNewMenu(R.menu.menu_pending_notes);
                break;
            case SHOW_NOTES_FRAGMENT_MENU:
                loadNewMenu(R.menu.menu_notes);
                break;
            case SHOW_PROFILE_MENU:
                loadNewMenu(R.menu.menu_profile);
                break;
            case SHOW_SINGLE_NOTES_MENU:
                loadNewMenu(R.menu.classes_options);
                break;
            case SHOW_CLASS_TIME_TABLE_OPTION:
                loadNewMenu(R.menu.class_time_table_option);
                break;
            case SHOW_STAFF_TIME_TABLE_OPTION:
                loadNewMenu(R.menu.staff_time_table_option);
                break;
            case NO_MENU:
            case SHOW_INDEPENDENT_SUBJECT_MENU:
            case SHOW_INDEPENDENT_TIME_TABLE_MENU:
                mMenu.clear();
                break;
        }

    }

    private void loadNewMenu(int resId)
    {
        mMenu.clear();
        mMenuInflater.inflate(resId, mMenu);
    }

    public void unRegisterOtto()
    {
        Otto.unregister(this);
    }
}
