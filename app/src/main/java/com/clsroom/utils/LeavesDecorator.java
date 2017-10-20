package com.clsroom.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.clsroom.R;
import com.clsroom.model.Leaves;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.clsroom.model.Leaves.STATUS_APPROVED;
import static com.clsroom.model.Leaves.STATUS_APPROVED_1;
import static com.clsroom.model.Leaves.STATUS_PENDING_1;
import static com.clsroom.model.Leaves.STATUS_REJECTED;
import static com.clsroom.model.Leaves.STATUS_REJECTED_1;

public class LeavesDecorator implements DayViewDecorator
{
    private static final String TAG = "LeavesDecorator";
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private HashSet<String> approvedDates;
    private HashSet<String> rejectedDates;
    private HashSet<String> pendingDates;
    private Context mContext;
    private int status = Leaves.STATUS_APPLIED;
    private HashSet<String> approved1Dates;
    private HashSet<String> rejected1Dates;
    private HashSet<String> pending1Dates;


    private LeavesDecorator(HashMap<String, Leaves> leaves, Context context, int status)
    {
        Log.d("CountTest", "LeavesDecorator constructor : " + status);
        mContext = context;
        this.status = status;
        pendingDates = new HashSet<>();
        approvedDates = new HashSet<>();
        rejectedDates = new HashSet<>();
        pending1Dates = new HashSet<>();
        approved1Dates = new HashSet<>();
        rejected1Dates = new HashSet<>();
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat(Leaves.DATE_FORMAT, Locale.ENGLISH);

        for (Map.Entry<String, Leaves> leavesEntry : leaves.entrySet())
        {
            Leaves leave = leavesEntry.getValue();
            createLeaveDatesSet(leave);
        }
        Log.d("CountTest", "pendingDates : " + pendingDates + ", \n" +
                "approvedDates : " + approvedDates + ", \n" +
                "rejectedDates : " + rejectedDates + ", \n" +
                "pending1Dates : " + pending1Dates + ", \n" +
                "approved1Dates : " + approved1Dates + ", \n" +
                "rejected1Dates : " + rejected1Dates + ", \n");
    }

    private void createLeaveDatesSet(Leaves leave)
    {
        try
        {
            calendar.setTime(dateFormat.parse(leave.getFromDate()));
            for (int i = 0; i <= leave.numDaysBetweenDates(); i++)
            {
                String date = CalendarDay.from(calendar).toString();
                if (leave.getStatus() == STATUS_APPROVED)
                {
                    if (status == STATUS_APPROVED_1 && i == 0)
                    {
                        approved1Dates.add(date);
                    }
                    else if (status == STATUS_APPROVED && i != 0)
                    {
                        approvedDates.add(date);
                    }
                }
                else if (leave.getStatus() == Leaves.STATUS_APPLIED)
                {
                    if (status == Leaves.STATUS_PENDING_1 && i == 0)
                    {
                            pending1Dates.add(date);
                    }
                    else if (status == Leaves.STATUS_APPLIED && i != 0)
                    {
                        pendingDates.add(date);
                    }
                }
                else if (leave.getStatus() == Leaves.STATUS_REJECTED)
                {
                    if (status == Leaves.STATUS_REJECTED_1 && i == 0)
                    {
                        rejected1Dates.add(date);
                    }
                    else if (status == Leaves.STATUS_REJECTED && i != 0)
                    {
                        rejectedDates.add(date);
                    }
                }
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean shouldDecorate(CalendarDay day)
    {
        switch (status)
        {
            case STATUS_APPROVED_1:
                return approved1Dates.contains(day.toString());
            case STATUS_REJECTED_1:
                return rejected1Dates.contains(day.toString());
            case STATUS_PENDING_1:
                return pending1Dates.contains(day.toString());
            case STATUS_APPROVED:
                return approvedDates.contains(day.toString());
            case STATUS_REJECTED:
                return rejectedDates.contains(day.toString());
            default:
                return pendingDates.contains(day.toString());
        }
    }

    @Override
    public void decorate(DayViewFacade view)
    {
        int drawableResId = R.drawable.leave_applied_bg_drawable;
        Log.d("LeavesTest", "decorate : status : " + status);
        switch (status)
        {
            case Leaves.STATUS_APPROVED:
                drawableResId = R.drawable.leave_approved_bg_drawable;
                break;
            case Leaves.STATUS_APPLIED:
                drawableResId = R.drawable.leave_applied_bg_drawable;
                break;
            case Leaves.STATUS_REJECTED:
                drawableResId = R.drawable.leave_rejected_bg_drawable;
                break;
            case Leaves.STATUS_APPROVED_1:
                drawableResId = R.drawable.leave_approved_1_bg_drawable;
                break;
            case Leaves.STATUS_PENDING_1:
                drawableResId = R.drawable.leave_applied_1_bg_drawable;
                break;
            case Leaves.STATUS_REJECTED_1:
                drawableResId = R.drawable.leave_rejected_1_bg_drawable;
                break;
        }

        try
        {
            if (Build.VERSION.SDK_INT > 20)
            {
                view.setBackgroundDrawable(mContext.getDrawable(drawableResId));
            }
            else
            {
                view.setBackgroundDrawable(mContext.getResources().getDrawable(drawableResId));
            }
        }
        catch (NullPointerException e)
        {
            Log.d(TAG, e.getMessage());
        }
    }

    public static List<DayViewDecorator> getDecorators(HashMap<String, Leaves> dataSnapshot, Activity activity)
    {
        Log.d("CountTest", "getDecorators");
        List<DayViewDecorator> list = new ArrayList<>();
        list.add(new LeavesDecorator(dataSnapshot, activity, Leaves.STATUS_PENDING_1));
        list.add(new LeavesDecorator(dataSnapshot, activity, Leaves.STATUS_APPLIED));

        list.add(new LeavesDecorator(dataSnapshot, activity, STATUS_REJECTED_1));
        list.add(new LeavesDecorator(dataSnapshot, activity, Leaves.STATUS_REJECTED));

        list.add(new LeavesDecorator(dataSnapshot, activity, STATUS_APPROVED_1));
        list.add(new LeavesDecorator(dataSnapshot, activity, Leaves.STATUS_APPROVED));
        return list;
    }
}