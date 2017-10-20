package com.clsroom.utils;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

class VolleySingleton
{
    private static VolleySingleton sInstance = null;
    private RequestQueue mRequestQueue;

    private VolleySingleton(Context context)
    {
        mRequestQueue = Volley.newRequestQueue(context);
    }

    public static VolleySingleton getInstance(Context context)
    {
        if (sInstance == null)
        {
            sInstance = new VolleySingleton(context);
        }
        return sInstance;
    }

    RequestQueue getRequestQueue()
    {
        return mRequestQueue;
    }
}
