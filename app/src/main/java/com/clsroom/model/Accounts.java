package com.clsroom.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class Accounts implements Parcelable
{
    public static final String EMAIL = "email";
    public static final String NAME = "name";
    public static final String GOOGLE_ID = "googleId";
    public static final String FCM_TOKEN = "fcmToken";
    public static final String PHOTO_URL = "photoUrl";

    private String email;
    private String name;
    private String googleId;
    private String photoUrl;
    private String fcmToken;
    private String phoneNumber;

    public Accounts(GoogleSignInAccount user)
    {
        email = user.getEmail();
        name = user.getDisplayName();
        googleId = user.getId();
        photoUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";
        fcmToken = "XYZ";
        phoneNumber = "0";
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getGoogleId()
    {
        return googleId;
    }

    public void setGoogleId(String googleId)
    {
        this.googleId = googleId;
    }

    public String getPhotoUrl()
    {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl)
    {
        this.photoUrl = photoUrl;
    }

    public String getFcmToken()
    {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken)
    {
        this.fcmToken = fcmToken;
    }

    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(this.email);
        dest.writeString(this.name);
        dest.writeString(this.googleId);
        dest.writeString(this.photoUrl);
        dest.writeString(this.fcmToken);
        dest.writeString(this.phoneNumber);
    }

    public Accounts()
    {
    }

    protected Accounts(Parcel in)
    {
        this.email = in.readString();
        this.name = in.readString();
        this.googleId = in.readString();
        this.photoUrl = in.readString();
        this.fcmToken = in.readString();
        this.phoneNumber = in.readString();
    }

    public static final Creator<Accounts> CREATOR = new Creator<Accounts>()
    {
        @Override
        public Accounts createFromParcel(Parcel source)
        {
            return new Accounts(source);
        }

        @Override
        public Accounts[] newArray(int size)
        {
            return new Accounts[size];
        }
    };
}
