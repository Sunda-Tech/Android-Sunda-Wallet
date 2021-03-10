package com.ibnux.nuxwallet;

/******************************************************************************\
 * GNU GENERAL PUBLIC LICENSE                                                 *
 * Version 3, 29 June 2007                                                    *
 * Ibnu Maksum https://github.com/ibnux/                                      *
 ******************************************************************************
 * This source and program come as is, WITHOUT ANY WARRANTY and/or WITHOUT    *
 * ANY IMPLIED WARRANTY.                                                      *
 \******************************************************************************/

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.ibnux.nuxwallet.ui.HomeActivity;
import com.ibnux.nuxwallet.ui.IntroActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(Aplikasi.sp.getBoolean("isFirst",true)) {
                    startActivity(new Intent(MainActivity.this, IntroActivity.class));
                    finish();
                }else{
                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    finish();
                }
            }
        },1500);

    }
}