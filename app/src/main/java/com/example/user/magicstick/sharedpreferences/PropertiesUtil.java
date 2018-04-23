package com.example.user.magicstick.sharedpreferences;

import android.content.SharedPreferences;

import com.example.user.magicstick.MFBApp;

/**
 * Created by user on 2018/4/20.
 */

public class PropertiesUtil {
    private static PropertiesUtil instance;
    private static String SPNAME = "property";
    private final SharedPreferences sp;
    private final SharedPreferences.Editor spEdit;

    public static PropertiesUtil getInstance() {
        if (instance == null) {
            instance = new PropertiesUtil();
        }
        return instance;
    }

    public PropertiesUtil() {
        sp = MFBApp.getApp().getSharedPreferences(SPNAME, 0);
        spEdit = sp.edit();
    }

    public void setValue(String key , String value){
        spEdit.putString(key,value).commit();
    }
    public String getString(String key,String defVal){
        return sp.getString(key,defVal);
    }
}
