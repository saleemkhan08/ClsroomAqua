package com.clsroom.views;

import android.annotation.TargetApi;
import android.os.Build;
import android.transition.ChangeBounds;
import android.transition.TransitionSet;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;

public class DetailsTransition extends TransitionSet
{
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DetailsTransition()
    {
        setOrdering(ORDERING_TOGETHER);
        addTransition(new ChangeBounds()).
                addTransition(new ChangeTransform()).
                addTransition(new ChangeImageTransform());
    }
}
