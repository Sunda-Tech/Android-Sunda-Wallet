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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.webkit.JavascriptInterface;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.ibnux.nuxwallet.R;
import com.ibnux.nuxwallet.data.Dompet;
import com.ibnux.nuxwallet.data.ObjectBox;
import com.ibnux.nuxwallet.databinding.ActivityWalletGeneratorBinding;
import im.delight.android.webview.AdvancedWebView;

public class WalletGeneratorActivity extends AppCompatActivity implements AdvancedWebView.Listener {
    ActivityWalletGeneratorBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWalletGeneratorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(R.string.add_new_wallet);
        String secret = "";
        Intent i = getIntent();
        if(i.hasExtra("data")){
            secret = "?pass="+i.getStringExtra("data");
        }
        binding.webview.setListener(this, this);
        binding.webview.setMixedContentAllowed(false);
        binding.webview.loadUrl("file:///android_asset/index.html"+secret);
        binding.webview.addJavascriptInterface(new WebAppInterface(this), "Wallet");

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
        public void pilihDompet(String pass,String wallet,String accountid,String publickey) {
            if(pass.length()>10 && wallet.length()>10){
                Dompet dompet = new Dompet();
                dompet.alamat = wallet;
                dompet.isMe = true;
                dompet.secretPhrase = pass;
                dompet.saldo = 0;
                dompet.publicKey = publickey;
                dompet.dompetID = accountid;
                //Ask Name
                AlertDialog.Builder builder = new AlertDialog.Builder(WalletGeneratorActivity.this);
                builder.setTitle(R.string.ask_wallet_name);
                final EditText input = new EditText(WalletGeneratorActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                input.setGravity(Gravity.CENTER_HORIZONTAL);
                input.setHint(R.string.optional);
                builder.setView(input);
                builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dompet.nama = input.getText().toString();
                        ObjectBox.addDompet(dompet);
                        dialog.dismiss();
                        finish();
                    }
                });
                builder.setNegativeButton(R.string.wallet_no_name, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ObjectBox.addDompet(dompet);
                        dialog.dismiss();
                        finish();
                    }
                });
                builder.show();
            }
        }
    }


}