package com.clsroom.model;

import android.text.TextUtils;
import android.util.Log;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.clsroom.R;
import com.clsroom.utils.NavigationUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Leaves
{
    public static final String LEAVES = "leaves";
    public static final String DB_DATE_FORMAT = "yyyyMMdd";
    public static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String TAG = "Leaves";
    public static final String MY_LEAVES = "myLeaves";
    public static final String REQUESTED_LEAVES = "requestedLeaves";
    public static final String REQUESTED_LEAVES_KEY = "requestedLeaveKey";
    public static final int STATUS_APPROVED_1 = 11;
    public static final int STATUS_PENDING_1 = 10;
    public static final int STATUS_REJECTED_1 = -11;
    public static final String STATUS = "status";
    public static final String LEAVE_FROM_DATE = "fromDate";
    public static final int STATUS_CANCELLED = 2;

    private String fromDate;
    private String toDate;
    private String reason;
    private String approverId;
    private int status;
    private String requesterId;

    public static final int STATUS_APPROVED = 1;
    public static final int STATUS_APPLIED = 0;
    public static final int STATUS_REJECTED = -1;
    private long requestedLeaveKey;

    public void setRequesterId(String requesterId)
    {
        this.requesterId = requesterId;
    }

    public String getRequesterId()
    {
        return this.requesterId;
    }

    public String getFromDate()
    {
        return fromDate.trim();
    }

    public String dbKeyDate()
    {
        return Leaves.getDbKeyDate(getFromDate());
    }

    public void setFromDate(String fromDate)
    {
        this.fromDate = fromDate;
    }

    public String getToDate()
    {
        return toDate.trim();
    }

    public void setToDate(String toDate)
    {
        this.toDate = toDate;
    }

    public String getReason()
    {
        return reason.trim();
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }

    public String getApproverId()
    {
        return approverId.trim();
    }

    public void setApproverId(String approverId)
    {
        this.approverId = approverId;
    }

    public boolean validate()
    {
        if (TextUtils.isEmpty(reason))
        {
            ToastMsg.show(R.string.pleaseEnterAValidReason);
            return false;
        }
        else if (TextUtils.isEmpty(fromDate) || !validateDate(fromDate))
        {
            ToastMsg.show(R.string.pleaseEnterAValidFromDate);
            return false;
        }
        else if ((TextUtils.isEmpty(toDate) || !validateDate(toDate)))
        {
            ToastMsg.show(R.string.pleaseEnterAToValidDate);
            return false;
        }
        else if (TextUtils.isEmpty(approverId))
        {
            ToastMsg.show(R.string.please_select_an_approver);
            return false;
        }
        else if (approverId.equals(NavigationUtil.mCurrentUser.getUserId()))
        {
            ToastMsg.show(R.string.you_cannot_select_yourself_as_an_approver);
            return false;
        }
        else
        {
            long numOfDays = numDaysBetweenDates();
            if (numOfDays < 0 || numOfDays >= 20)
            {
                ToastMsg.show(R.string.pleaseEnterAToValidDate);
                return false;
            }
        }
        return true;
    }

    public long numDaysBetweenDates()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        Date startDate, endDate;
        long numberOfDays = 0;
        try
        {
            startDate = dateFormat.parse(fromDate);
            endDate = dateFormat.parse(toDate);
            numberOfDays = getUnitBetweenDates(startDate, endDate, TimeUnit.DAYS);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        return numberOfDays;
    }

    private static long getUnitBetweenDates(Date startDate, Date endDate, TimeUnit unit)
    {
        long timeDiff = endDate.getTime() - startDate.getTime();
        return unit.convert(timeDiff, TimeUnit.MILLISECONDS);
    }

    private boolean validateDate(String testDate)
    {
        if (testDate.matches("([0-9]{2})-([0-9]{2})-([0-9]{4})"))
        {
            String[] dates = testDate.split("-");
            int day = Integer.parseInt(dates[0]);
            int month = Integer.parseInt(dates[1]);
            int year = Integer.parseInt(dates[2]);

            if (day > 31)
            {
                return false;
            }
            if (month > 12)
            {
                return false;
            }
            if (year < 1900 || year > 2100)
            {
                return false;
            }
            return true;
        }
        return false;
    }

    public static String getDbKeyDate(CalendarDay calendar)
    {
        return "" + calendar.getYear() + get2DigitNum(calendar.getMonth() + 1) + get2DigitNum(calendar.getDay());
    }

    public static String getDbKeyDate(Calendar calendar)
    {
        return "" + calendar.get(Calendar.YEAR) + get2DigitNum(calendar.get(Calendar.MONTH) + 1)
                + get2DigitNum(calendar.get(Calendar.DAY_OF_MONTH));
    }

    public static String getDisplayDate(String dbKeyDate)
    {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat(DB_DATE_FORMAT, Locale.ENGLISH);
        try
        {
            calendar.setTime(format.parse(dbKeyDate));
            return getDisplayDate(calendar);
        }
        catch (ParseException e)
        {
            return "";
        }
    }

    public static String getDbRetrieveKeyDate(CalendarDay calendar)
    {
        return "" + calendar.getYear() + get2DigitNum(calendar.getMonth()) + get2DigitNum(calendar.getDay());
    }

    public static String getDbRetrieveKeyDate(Calendar calendar)
    {
        return "" + calendar.get(Calendar.YEAR) + get2DigitNum(calendar.get(Calendar.MONTH))
                + get2DigitNum(calendar.get(Calendar.DAY_OF_MONTH));
    }

    public static String get2DigitNum(int num)
    {
        return (num < 10) ? "0" + num : "" + num;
    }

    public static String getDisplayDate(Calendar calendar)
    {
        return get2DigitNum(calendar.get(Calendar.DAY_OF_MONTH)) + "-"
                + get2DigitNum(calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.YEAR);
    }

    public static String getDbKeyDate(int year, int month, int day)
    {
        return get2DigitNum(day) + "-" + get2DigitNum(month) + "-" + year;
    }

    public static String getDisplayDate(int year, int month, int day)
    {
        return get2DigitNum(day) + "/" + get2DigitNum(month) + "/" + year;
    }

    public static String getDbKeyStartDate(Calendar calendar)
    {
        return "" + calendar.get(Calendar.YEAR) + get2DigitNum(calendar.get(Calendar.MONTH))
                + "20";
    }

    public static String getDbKeyEndDate(Calendar calendar)
    {
        return "" + calendar.get(Calendar.YEAR) + get2DigitNum(calendar.get(Calendar.MONTH) + 2)
                + "10";
    }

    public static String getDbKeyStartDate(CalendarDay calendar)
    {
        return "" + calendar.getYear() + get2DigitNum(calendar.getMonth())
                + "20";
    }

    public static String getDbKeyEndDate(CalendarDay calendar)
    {
        return "" + calendar.getYear() + get2DigitNum(calendar.getMonth() + 2)
                + "10";
    }

    public static Calendar getCalendar(String date)
    {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        try
        {
            calendar.setTime(format.parse(date));
        }
        catch (ParseException e)
        {
            Log.d(TAG, e.getMessage());
        }

        return calendar;
    }

    public static String getDbKeyDateTime(Calendar calendar)
    {
        return "" + calendar.get(Calendar.YEAR) + get2DigitNum(calendar.get(Calendar.MONTH) + 1)
                + get2DigitNum(calendar.get(Calendar.DAY_OF_MONTH)) + get2DigitNum(calendar.get(Calendar.HOUR_OF_DAY))
                + get2DigitNum(calendar.get(Calendar.MINUTE)) + get2DigitNum(calendar.get(Calendar.SECOND));
    }

    public int getStatus()
    {
        return status;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }

    public int statusText()
    {
        switch (getStatus())
        {
            default:
                return R.string.approvalPending;

            case STATUS_APPROVED:
                return R.string.approved;

            case STATUS_REJECTED:
                return R.string.rejected;
        }
    }

    public static String getFirstDateDbKey(HashMap<String, Leaves> mLeavesList, CalendarDay date)
    {
        Calendar key;
        for (Map.Entry<String, Leaves> entry : mLeavesList.entrySet())
        {
            key = getFirstDateDbKey(entry.getValue(), date);
            if (key != null)
            {
                return getDbKeyDate(key);
            }
        }
        return null;
    }

    public static String getDbKeyDate(String fromDate)
    {
        return getDbKeyDate(getCalendar(fromDate));
    }

    private static Calendar getFirstDateDbKey(Leaves leave, CalendarDay date)
    {
        Calendar currentDate = date.getCalendar();
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        try
        {
            startDate.setTime(format.parse(leave.getFromDate()));
            endDate.setTime(format.parse(leave.getToDate()));
        }
        catch (ParseException e)
        {
            return null;
        }
        if (!(currentDate.before(startDate) || currentDate.after(endDate)))
        {
            return startDate;
        }
        return null;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Leaves)
        {
            Leaves leave = (Leaves) obj;
            return leave.getFromDate().equals(getFromDate())
                    && leave.getToDate().equals(getToDate())
                    && leave.getRequesterId().equals(getRequesterId());
        }
        return false;
    }

    public long getRequestedLeaveKey()
    {
        return requestedLeaveKey;
    }

    public void setRequestedLeaveKey(long requestedLeaveKey)
    {
        this.requestedLeaveKey = requestedLeaveKey;
    }

    public String requestedLeaveKey()
    {
        return "" + (getRequestedLeaveKey() * (-1));
    }

    public void requestedLeaveKey(String requestedLeaveKey)
    {
        setRequestedLeaveKey((-1) * Long.parseLong(requestedLeaveKey));
    }
}
