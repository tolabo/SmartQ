package com.vaibhav.smartq.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;

import java.util.Locale;

/**
 * Created by vaibhav on 10/25/2016.
 */

public class MyFontTypeFaces {


    //returns typeface for numbers
    public static Typeface getNumberFont(Context context){

        return Typeface.createFromAsset(context.getApplicationContext().getAssets(),
                String.format(Locale.US, "fonts/%s", "Glegoo-Bold.ttf"));

    }


}
