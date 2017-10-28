package com.clsroom.model;

public class NotesClassifier
{
    private String classId;
    private String subjectId;
    private String subjectName;
    private String className;
    private boolean edit;
    private boolean isReviewedNotesShown = true;

    public String getClassId()
    {
        return classId;
    }

    public void setClassId(String classId)
    {
        this.classId = classId;
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
