package com.clsroom.model;

import android.text.TextUtils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public abstract class User
{
    public static final String FULL_NAME = "fullName";
    public static final String PHOTO_URL = "photoUrl";
    public static final String PASSWORD = "password";
    public static final String NOT_SET = "Not Set..";
    public static final String TOKEN = "token";
    private String userId;
    private String fullName;
    private String photoUrl;
    private String password;
    private String address;
    private String email;
    private String phone;
    private String token;

    public String getDob()
    {
        if (dob == null)
        {
            dob = NOT_SET;
        }
        return dob;
    }

    public void setDob(String dob)
    {
        this.dob = dob;
    }

    public String getPhone()
    {
        if (phone == null)
        {
            phone = NOT_SET;
        }
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getEmail()
    {
        if (email == null)
        {
            email = NOT_SET;
        }
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getAddress()
    {
        if (address == null)
        {
            address = NOT_SET;
        }
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    private String dob;

    public static final String STUDENTS = "students";
    public static final String STAFF = "staff";
    public static final String ADMIN = "admin";
    public static final String USER_ID = "userId";

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }

    public void setPhotoUrl(String photoUrl)
    {
        this.photoUrl = photoUrl;
    }

    public String getUserId()
    {
        return userId;
    }

    public String getFullName()
    {
        if (fullName == null || TextUtils.isEmpty(fullName))
        {
            return "Name isn't updated..";
        }
        return fullName;
    }

    public String getPhotoUrl()
    {
        if (photoUrl == null)
        {
            return "";
        }
        return photoUrl;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public abstract String userType();

    public static DatabaseReference getRef(String userId)
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        if (isStaff(userId))
        {
            return ref.child(User.STAFF).child(userId);
        }
        else
        {
            return ref.child(User.STUDENTS).child(userId.substring(0, 3)).child(userId);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof User && ((User) obj).getUserId().equals(getUserId());
    }

    @Override
    public String toString()
    {
        return getUserId() + " : " + getFullName();
    }

    public boolean validateEmail()
    {
        return !TextUtils.isEmpty(email) || !email.equals(User.NOT_SET) ||
                (email.matches("^[0-9a-zA-Z!#$%&;'*+\\-/\\=\\?\\^_`\\.{|}~]{1,64}@[0-9a-zA-Z]{1,255}\\.[a-zA-Z]{1,10}")
                        && email.length() <= 320);
    }

    public static boolean isStaff(String userId)
    {
        return userId.charAt(0) == 'a' || userId.charAt(0) == 's';
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }
}
