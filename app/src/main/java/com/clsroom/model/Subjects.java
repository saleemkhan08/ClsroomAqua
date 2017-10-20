package com.clsroom.model;

public class Subjects
{
    public static final String SUBJECTS = "subjects";

    public Subjects()
    {

    }

    private String classCode;
    private String subjectName;
    private String subjectCode;
    private String teacherName;
    private String teacherImgUrl;
    private String teacherCode;

    public String getClassCode()
    {
        return classCode;
    }

    public void setClassCode(String classCode)
    {
        this.classCode = classCode;
    }

    public String getSubjectName()
    {
        return subjectName;
    }

    public void setSubjectName(String subjectName)
    {
        this.subjectName = subjectName;
    }

    public String getSubjectCode()
    {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode)
    {
        this.subjectCode = subjectCode;
    }

    public String getTeacherName()
    {
        return teacherName;
    }

    public void setTeacherName(String teacherName)
    {
        this.teacherName = teacherName;
    }

    public String getTeacherCode()
    {
        return teacherCode;
    }

    public void setTeacherCode(String teacherCode)
    {
        this.teacherCode = teacherCode;
    }

    public String getTeacherImgUrl()
    {
        return teacherImgUrl;
    }

    public void setTeacherImgUrl(String teacherImgUrl)
    {
        this.teacherImgUrl = teacherImgUrl;
    }
}
