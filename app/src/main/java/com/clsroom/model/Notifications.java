package com.clsroom.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static com.clsroom.dialogs.MonthYearPickerDialog.MONTH_ARRAY;
import static com.clsroom.model.Notes.AM_PM;
import static com.clsroom.utils.DateTimeUtil.get2DigitNum;

public class Notifications implements Parcelable
{
    public static final String NOTIFICATIONS = "notifications";
    private String message;
    private String senderName;
    private String senderPhotoUrl;
    private String senderId;
    private long dateTime;
    private String leaveId;
    private String notesId;
    private String leaveRefType;

    public static final String MESSAGE = "message";
    public static final String SENDER_NAME = "senderName";
    public static final String SENDER_PHOTO_URL = "senderPhotoUrl";
    public static final String SENDER_ID = "senderId";
    public static final String DATE_TIME = "dateTime";
    public static final String LEAVE_ID = "leaveId";
    public static final String NOTES_ID = "notesId";
    public static final String LEAVE_REF_TYPE = "leaveRefType";

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getSenderName()
    {
        return senderName;
    }

    public void setSenderName(String senderName)
    {
        this.senderName = senderName;
    }

    public String getSenderPhotoUrl()
    {
        return senderPhotoUrl;
    }

    public void setSenderPhotoUrl(String senderPhotoUrl)
    {
        this.senderPhotoUrl = senderPhotoUrl;
    }

    public String getSenderId()
    {
        return senderId;
    }

    public void setSenderId(String senderId)
    {
        this.senderId = senderId;
    }

    public long getDateTime()
    {
        return dateTime;
    }

    public void setDateTime(long dateTime)
    {
        this.dateTime = dateTime;
    }

    public String displayDate()
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
        try
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(format.parse(((-1) * dateTime) + ""));
            return MONTH_ARRAY[calendar.get(Calendar.MONTH)] + "-" +
                    calendar.get(Calendar.DAY_OF_MONTH) + " "
                    + get2DigitNum(calendar.get(Calendar.HOUR))
                    + ":" + get2DigitNum(calendar.get(Calendar.MINUTE))
                    + " " + AM_PM[calendar.get(Calendar.AM_PM)];
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public String getLeaveId()
    {
        return leaveId;
    }

    public void setLeaveId(String leaveId)
    {
        this.leaveId = leaveId;
    }

    public String getNotesId()
    {
        return notesId;
    }

    public void setNotesId(String notesId)
    {
        this.notesId = notesId;
    }

    public String dateTime()
    {
        return "" + (getDateTime() * (-1));
    }

    public void dateTime(String key)
    {
        setDateTime((-1) * Long.parseLong(key));
    }

    public String getLeaveRefType()
    {
        return leaveRefType;
    }

    public void setLeaveRefType(String leaveRefType)
    {
        this.leaveRefType = leaveRefType;
    }


    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(this.message);
        dest.writeString(this.senderName);
        dest.writeString(this.senderPhotoUrl);
        dest.writeString(this.senderId);
        dest.writeLong(this.dateTime);
        dest.writeString(this.leaveId);
        dest.writeString(this.notesId);
        dest.writeString(this.leaveRefType);
    }

    public Notifications()
    {
    }

    protected Notifications(Parcel in)
    {
        this.message = in.readString();
        this.senderName = in.readString();
        this.senderPhotoUrl = in.readString();
        this.senderId = in.readString();
        this.dateTime = in.readLong();
        this.leaveId = in.readString();
        this.notesId = in.readString();
        this.leaveRefType = in.readString();
    }

    public static final Parcelable.Creator<Notifications> CREATOR = new Parcelable.Creator<Notifications>()
    {
        @Override
        public Notifications createFromParcel(Parcel source)
        {
            return new Notifications(source);
        }

        @Override
        public Notifications[] newArray(int size)
        {
            return new Notifications[size];
        }
    };
}
