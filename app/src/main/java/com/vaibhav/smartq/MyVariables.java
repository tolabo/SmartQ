package com.vaibhav.smartq;

import android.content.SharedPreferences;

/**
 * Created by vaibhav on 9/18/2016.
 */
public class MyVariables {

    public static final String BASEURL = "Your Firebase DB URL";
    public static final String TAG = "SmartQ";
    public static final String MYPREFERENCES = "myPrefs";
    public static final String HOSTQID ="hostQId";
    public static final String JOINQID = "joinQId";
    public static final String ISQHOSTED = "isQHosted";
    public static final String ISQJOINED = "isQJoined";
    public static final String JOINEDQTOKEN = "myTokenNmmber";
    public static final String MYEMAIL="userEmail";
    public static final String MYUSERNAME="userName";
    public static final String HOSTEDQUEUENAME = "hostedQName";
    public static final String HOSTEDQUEUEDESC = "hostedQDesc";

    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor editor;
}
