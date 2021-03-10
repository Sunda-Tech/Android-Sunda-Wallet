package com.ibnux.nuxwallet.ui;

/******************************************************************************\
 * GNU GENERAL PUBLIC LICENSE                                                 *
 * Version 3, 29 June 2007                                                    *
 * Ibnu Maksum https://github.com/ibnux/                                      *
 ******************************************************************************
 * This source and program come as is, WITHOUT ANY WARRANTY and/or WITHOUT    *
 * ANY IMPLIED WARRANTY.                                                      *
 \******************************************************************************/

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.JavascriptInterface;

import androidx.appcompat.app.AppCompatActivity;

import com.ibnux.nuxwallet.Aplikasi;
import com.ibnux.nuxwallet.databinding.ActivityOfflineSigningBinding;

import im.delight.android.webview.AdvancedWebView;

public class OfflineSigningActivity extends AppCompatActivity implements AdvancedWebView.Listener {
    ActivityOfflineSigningBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOfflineSigningBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Nandatangan Oppline");
        String secret = "";
        Intent i = getIntent();
        try{
            secret = "?utb="+i.getStringExtra("utb")+"TXTXTX"+i.getStringExtra("secret")+"TXTXTX"+ Aplikasi.unixtime;
        }catch (Exception e){
            setResult(RESULT_CANCELED);
            finish();
        }
        binding.webview.setListener(this, this);
        binding.webview.setMixedContentAllowed(false);
        binding.webview.loadUrl("file:///android_asset/sign.html"+secret);
        binding.webview.addJavascriptInterface(new OfflineSigningActivity.WebAppInterface(this), "Android");
    }


    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        binding.webview.onResume();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onPause() {
        binding.webview.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        binding.webview.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        binding.webview.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onBackPressed() {
        if (!binding.webview.onBackPressed()) { return; }
        super.onBackPressed();
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) { }

    @Override
    public void onPageFinished(String url) { }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) { }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) { }

    @Override
    public void onExternalPageRequest(String url) { }

    public class WebAppInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            mContext = c;
        }

        /** Show a toast from the web page */
        @JavascriptInterface
        public void finish(String status,String result) {
            Intent i = getIntent();
            if(status.equals("success")){
                i.putExtra("success",result);
            }else{
                i.putExtra("failed",result);
            }
            setResult(RESULT_OK,i);
            OfflineSigningActivity.this.finish();
        }
    }
}