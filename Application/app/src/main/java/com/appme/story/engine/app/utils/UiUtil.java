package com.appme.story.engine.app.utils;

import android.content.Context;

public class UiUtil {
    public static int getDimen(Context context, int dimen) {
        return context.getResources().getDimensionPixelOffset(dimen);
    }
}
