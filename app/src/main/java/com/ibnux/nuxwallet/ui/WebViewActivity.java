package com.ibnux.nuxwallet.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.ibnux.nuxwallet.R;
import com.ibnux.nuxwallet.utils.Utils;
import im.delight.android.webview.AdvancedWebView;

public class WebViewActivity extends AppCompatActivity implements AdvancedWebView.Listener {
    private AdvancedWebView mWebView;
    SwipeRefreshLayout swipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        mWebView = findViewById(R.id.webview);
        swipe = findViewById(R.id.swipe);
        mWebView.setListener(this, this);
        mWebView.setMixedContentAllowed(false);
        Intent intent = getIntent();
        swipe.post(new Runnable() {
            @Override
            public void run() {
                swipe.setRefreshing(true);
                mWebView.loadUrl(intent.getStringExtra("url"));
            }
        });
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mWebView.reload();
            }
        });
    }

    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
        // ...
    }

    @SuppressLint("NewApi")
    @Override
    protected void onPause() {
        mWebView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mWebView.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        mWebView.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onBackPressed() {
        if (!mWebView.onBackPressed()) { return; }
        super.onBackPressed();
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {
        swipe.setRefreshing(true);
    }

    @Override
    public void onPageFinished(String url) {
        swipe.setRefreshing(false);
    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {
        swipe.setRefreshing(false);
        Utils.log(description);
        if(mWebView.canGoBack()){
            mWebView.goBack();
        }else{
            finish();
        }
    }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(i);
        }catch (Exception e){

        }
    }

    @Override
    public void onExternalPageRequest(String url) {
        swipe.setRefreshing(false);
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(i);
        }catch (Exception e){

        }
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            finish();
        }
        return super.onKeyLongPress(keyCode, event);
    }
}