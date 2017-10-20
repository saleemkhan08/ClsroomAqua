package com.clsroom.utils;

import android.util.Log;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public class Otto
{
    private static final Bus BUS = new Bus(ThreadEnforcer.ANY);

    public static void register(Object object)
    {
        try
        {
            BUS.register(object);
            Log.d("Otto", "Registered : " + object);
        }
        catch (Exception e)
        {
            Log.d("Otto", e.getMessage());
        }
    }

    public static void unregister(Object object)
    {
        try
        {
            BUS.unregister(object);
            Log.d("Otto", "unRegistered : " + object);
        }
        catch (Exception e)
        {
            Log.d("Otto", e.getMessage());
        }
    }

    public static void post(Object object)
    {
        try
        {
            BUS.post(object);
            Log.d("Otto", "Posted : " + object);
        }
        catch (Exception e)
        {
            Log.d("Otto", e.getMessage());
        }
    }

    private Otto()
    {
    }
}
