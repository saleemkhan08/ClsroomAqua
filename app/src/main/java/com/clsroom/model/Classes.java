package com.clsroom.model;

public class Classes
{
    public static final String CLASSES = "classes";
    public static final String CODE = "code";

    private String code;
    private String name;
    private int studentCount;
    private String classTeacherName;
    private String classTeacherId;

    public int getStudentCount()
    {
        return studentCount;
    }

    public String getName()
    {
        if (name == null)
        {
            return "Class Name Not Set.";
        }
        return name;
    }

    public String getClassTeacherName()
    {
        if (classTeacherName == null)
        {
            return "Not assigned.";
        }
        return classTeacherName;
    }

    public Classes()
    {
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setStudentCount(int studentCount)
    {
        this.studentCount = studentCount;
    }

    public void setClassTeacherName(String classTeacherName)
    {
        this.classTeacherName = classTeacherName;
    }

    public void setClassTeacherId(String classTeacherId)
    {
        this.classTeacherId = classTeacherId;
    }

    public String getCode()
    {
        return code;
    }

    public String getClassTeacherId()
    {
        return classTeacherId;
    }
}
