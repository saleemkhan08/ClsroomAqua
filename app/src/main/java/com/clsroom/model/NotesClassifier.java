package com.clsroom.model;

public class NotesClassifier
{
    public static final String RESULTS = "results";
    private String classId;
    private String testId;
    private String subjectId;
    private String subjectName;
    private String className;
    private boolean edit;
    private String teacherId;
    private boolean isReviewedNotesShown = true;

    public String getClassId()
    {
        return classId;
    }

    public void setClassId(String classId)
    {
        this.classId = classId;
    }

    public String getTestId()
    {
        return testId;
    }

    public void setTestId(String testId)
    {
        this.testId = testId;
    }

    public String getSubjectId()
    {
        return subjectId;
    }

    public void setSubjectId(String subjectId)
    {
        this.subjectId = subjectId;
    }

    public boolean isEdit()
    {
        return edit;
    }

    public void setEdit(boolean edit)
    {
        this.edit = edit;
    }

    public String getSubjectName()
    {
        return subjectName;
    }

    public void setSubjectName(String subjectName)
    {
        this.subjectName = subjectName;
    }

    public String getTeacherId()
    {
        return teacherId;
    }

    public void setTeacherId(String teacherId)
    {
        this.teacherId = teacherId;
    }

    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    public boolean isReviewedNotesShown()
    {
        return isReviewedNotesShown;
    }

    public void setReviewedNotesShown(boolean reviewedNotesShown)
    {
        isReviewedNotesShown = reviewedNotesShown;
    }
}
