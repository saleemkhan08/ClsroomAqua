package com.clsroom.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static com.clsroom.dialogs.MonthYearPickerDialog.MONTH_ARRAY;

public class Notes
{
    public static final String NOTES = "notes";
    public static final String DATE_TIME = "dateTime";
    public static final String REVIEW = "review";
    public static final String REJECTED = "rejected";
    public static final String APPROVED = "approved";
    public static final String SUBMITTER_ID = "submitterId";
    public static final String RE_SUBMITTED = "reSubmitted";
    private String notesTitle;
    private String notesDescription;
    private ArrayList<NotesImage> notesImages;
    private String reviewerId;
    private String submitterId;
    private String submitterName;
    private String submitterPhotoUrl;
    private long dateTime;
    private String notesStatus;
    private String reviewComment;
    private String classSubId;

    public Notes()
    {

    }

    public String getNotesStatus()
    {
        return notesStatus;
    }

    public void setNotesStatus(String status)
    {
        this.notesStatus = status;
    }

    public String getNotesTitle()
    {
        return notesTitle;
    }

    public void setNotesTitle(String notesTitle)
    {
        this.notesTitle = notesTitle;
    }

    public String getNotesDescription()
    {
        return notesDescription;
    }

    public void setNotesDescription(String notesDescription)
    {
        this.notesDescription = notesDescription;
    }

    public ArrayList<NotesImage> getNotesImages()
    {
        return notesImages;
    }

    public void setNotesImages(ArrayList<NotesImage> notesImages)
    {
        this.notesImages = notesImages;
    }

    public String getReviewerId()
    {
        return reviewerId;
    }

    public void setReviewerId(String reviewerId)
    {
        this.reviewerId = reviewerId;
    }

    public String getSubmitterId()
    {
        return submitterId;
    }

    public void setSubmitterId(String submitterId)
    {
        this.submitterId = submitterId;
    }

    public long getDateTime()
    {
        return dateTime;
    }

    public void setDateTime(long date)
    {
        this.dateTime = date;
    }

    public String getSubmitterName()
    {
        return submitterName;
    }

    public void setSubmitterName(String submitterName)
    {
        this.submitterName = submitterName;
    }

    public String getSubmitterPhotoUrl()
    {
        return submitterPhotoUrl;
    }

    public void setSubmitterPhotoUrl(String submitterPhotoUrl)
    {
        this.submitterPhotoUrl = submitterPhotoUrl;
    }

    public static final String[] AM_PM = {"AM", "PM"};

    public String displayDate()
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
        try
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(format.parse(dateTime()));
            return MONTH_ARRAY[calendar.get(Calendar.MONTH)] + "-" +
                    calendar.get(Calendar.DAY_OF_MONTH) + " "
                    + calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE)
                    + " " + AM_PM[calendar.get(Calendar.AM_PM)];

        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public String dateTime()
    {
        return "" + (getDateTime() * (-1));
    }

    public void dateTime(String key)
    {
        setDateTime((-1) * Long.parseLong(key));
    }

    public void setReviewComment(String reviewComment)
    {
        this.reviewComment = reviewComment;
    }

    public String getReviewComment()
    {
        return reviewComment;
    }

    public String getClassSubId()
    {
        return classSubId;
    }

    public void setClassSubId(String classSubId)
    {
        this.classSubId = classSubId;
    }
}
