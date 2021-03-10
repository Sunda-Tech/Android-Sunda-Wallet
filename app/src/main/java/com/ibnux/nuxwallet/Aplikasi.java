package com.ibnux.nuxwallet;

/******************************************************************************\
 * GNU GENERAL PUBLIC LICENSE                                                 *
 * Version 3, 29 June 2007                                                    *
 * Ibnu Maksum https://github.com/ibnux/                                      *
 ******************************************************************************
 * This source and program come as is, WITHOUT ANY WARRANTY and/or WITHOUT    *
 * ANY IMPLIED WARRANTY.                                                      *
 \******************************************************************************/


import android.content.SharedPreferences;
import android.os.StrictMode;

import androidx.multidex.MultiDexApplication;

import com.androidnetworking.AndroidNetworking;
import com.ibnux.nuxwallet.data.ObjectBox;

import cat.ereza.customactivityoncrash.config.CaocConfig;

public class Aplikasi extends MultiDexApplication {
    public static SharedPreferences sp;
    public static Aplikasi app;
    public static Boolean isLogin;
    public static long unixtime = 0L;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        ObjectBox.init(this);
        AndroidNetworking.initialize(getApplicationContext());
        CaocConfig.Builder.create()
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
                .enabled(true) //default: true
                .showErrorDetails(true) //default: true
                .showRestartButton(true) //default: true
                .logErrorOnRestart(true) //default: true
                .trackActivities(true) //default: false
                .minTimeBetweenCrashesMs(2000) //default: 3000
                .errorDrawable(R.mipmap.ic_launcher)
                .apply();
        sp = getSharedPreferences("settings",0);
        unixtime = sp.getLong("unixtime",0L);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }


    public static String getPin(){
        return sp.getString("PIN",null);
    }

    public static void setPin(String sha265){
        sp.edit().putString("PIN", sha265).apply();
    }

}
