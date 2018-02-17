package com.lukasanda.popularmovies.utils;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by lukas on 2/17/2018.
 */

public class Utils {
    public static int calculateNoOfColumns(Context context) {
        //This function is from my mentor Rowland, thanks man
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int scalingFactor = 150;
        return (int) (dpWidth / scalingFactor);
    }
}
