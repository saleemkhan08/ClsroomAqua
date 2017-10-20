package com.clsroom.listeners;

import com.android.volley.VolleyError;

public interface ResultListener<T>
{
    String ACTION = "action";
    void onSuccess(T result);
    void onError(VolleyError error);
}
