package com.clsroom.model;

import android.text.TextUtils;

import java.util.Objects;

public class Staff extends User
{
    public static final String IS_ADMIN = "isAdmin";
    private String qualification;
    private String designation;
    private boolean isAdmin;

    public Staff()
    {

    }

    public String getDesignation()
    {
        return designation;
    }

    public void setDesignation(String designation)
    {
        this.designation = designation;
    }

    public boolean getIsAdmin()
    {
        return isAdmin;
    }

    public void setAdmin(boolean admin)
    {
        isAdmin = admin;
    }


    public void setQualification(String qualification)
    {
        this.qualification = qualification;
    }

    public String getQualification()
    {
        if (qualification == null || TextUtils.isEmpty(qualification))
        {
            return "Qualification isn't updated..";
        }
        return qualification;
    }

    @Override
    public String userType()
    {
        if (isAdmin)
        {
            return ADMIN;
        }
        return STAFF;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof Staff && Objects.equals(getUserId(), ((Staff) obj).getUserId());
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
