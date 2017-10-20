package com.clsroom.model;

import java.util.Objects;

public class Students extends User
{
    private String className;

    public Students()
    {

    }

    @Override
    public String userType()
    {
        return STUDENTS;
    }

    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    public String classId()
    {
        return getUserId().substring(0, 3);
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof Students && Objects.equals(getUserId(), ((Students) obj).getUserId());
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(getUserId());
    }

    @Override
    public String toString()
    {
        return getUserId();
    }
}
