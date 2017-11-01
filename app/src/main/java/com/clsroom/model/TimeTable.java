package com.clsroom.model;

public class TimeTable
{

    public static final String TIME_TABLE = "timeTable";
    public static final String START_TIME = "startTime";
    public static final String TEACHER_CODE = "teacherCode";
    private String classCode;

    private String subjectCode;
    private String subjectName;

    private String teacherCode;
    private String teacherName;
    private String teacherPhotoUrl;

    private String startTime;
    private String endTime;

    private boolean isBreak;

    private String weekdayCode;

    public String getClassCode()
    {
        return classCode;
    }

    public void setClassCode(String classCode)
    {
        this.classCode = classCode;
    }

    public String getSubjectCode()
    {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode)
    {
        this.subjectCode = subjectCode;
    }

    public String getSubjectName()
    {
        return subjectName;
    }

    public void setSubjectName(String subjectName)
    {
        this.subjectName = subjectName;
    }

    public String getTeacherCode()
    {
        return teacherCode;
    }

    public void setTeacherCode(String teacherCode)
    {
        this.teacherCode = teacherCode;
    }

    public String getTeacherName()
    {
        return teacherName;
    }

    public void setTeacherName(String teacherName)
    {
        this.teacherName = teacherName;
    }

    public String getTeacherPhotoUrl()
    {
        return teacherPhotoUrl;
    }

    public void setTeacherPhotoUrl(String teacherPhotoUrl)
    {
        this.teacherPhotoUrl = teacherPhotoUrl;
    }

    public String getStartTime()
    {
        return startTime;
    }

    public String getStartTimeKey()
    {
        return startTime.replaceAll("[^a-zA-Z0-9]", "");
    }

    public void setStartTime(String startTime)
    {
        this.startTime = startTime;
    }

    public String getEndTime()
    {
        return endTime;
    }

    public void setEndTime(String endTime)
    {
        this.endTime = endTime;
    }

    public String getWeekdayCode()
    {
        return weekdayCode;
    }

    public void setWeekdayCode(String weekdayCode)
    {
        this.weekdayCode = weekdayCode;
    }

    public boolean isBreak()
    {
        return isBreak;
    }

    public void setBreak(boolean aBreak)
    {
        isBreak = aBreak;
    }
}
