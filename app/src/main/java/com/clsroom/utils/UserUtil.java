package com.clsroom.utils;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.clsroom.model.Accounts;

public class UserUtil
{
    private static final String USERS = "users";
    private static UserUtil sUserUtil;

    public static UserUtil getInstance()
    {
        if (sUserUtil == null)
        {
            sUserUtil = new UserUtil();
        }
        return sUserUtil;
    }

    private UserUtil()
    {

    }

    public void saveUser(GoogleSignInAccount user)
    {
        Accounts account = new Accounts(user);
        DatabaseReference usersDbRef = getDbReference(account);
        if (usersDbRef != null)
        {
            usersDbRef.setValue(account);
        }
    }

    private DatabaseReference getDbReference(Accounts account)
    {
        DatabaseReference databaseReference = null;
        if (account.getGoogleId() != null && !account.getGoogleId().isEmpty())
        {
            databaseReference = FirebaseDatabase.getInstance().getReference().child(USERS)
                    .child(account.getGoogleId());
        }
        return databaseReference;
    }
}
