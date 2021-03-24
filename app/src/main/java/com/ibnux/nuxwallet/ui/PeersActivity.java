package com.ibnux.nuxwallet.ui;

/******************************************************************************\
 * GNU GENERAL PUBLIC LICENSE                                                 *
 * Version 3, 29 June 2007                                                    *
 * Ibnu Maksum https://github.com/ibnux/                                      *
 ******************************************************************************
 * This source and program come as is, WITHOUT ANY WARRANTY and/or WITHOUT    *
 * ANY IMPLIED WARRANTY.                                                      *
 \******************************************************************************/

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.ibnux.nuxwallet.Aplikasi;
import com.ibnux.nuxwallet.R;
import com.ibnux.nuxwallet.adapter.ServerAdapter;
import com.ibnux.nuxwallet.data.ObjectBox;
import com.ibnux.nuxwallet.data.Server;
import com.ibnux.nuxwallet.databinding.ActivityPeersBinding;
import com.ibnux.nuxwallet.utils.LongCallback;
import com.ibnux.nuxwallet.utils.NuxCoin;
import com.ibnux.nuxwallet.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

public class PeersActivity extends AppCompatActivity implements ServerAdapter.ServerCallback, View.OnClickListener{
    ActivityPeersBinding binding;
    ServerAdapter adapter;
    AlertDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPeersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("SERVER");

        adapter = new ServerAdapter(this);
        binding.recycleView.setHasFixedSize(true);
        binding.recycleView.setLayoutManager(new LinearLayoutManager(this));
        binding.recycleView.setAdapter(adapter);

        binding.fabAddPeer.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if(v==binding.fabAddPeer){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.add_peer);
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT);
            input.setHint(R.string.peer_hint);
            input.setText(Aplikasi.sp.getString("cachepeer",""));
            input.setSelectAllOnFocus(true);
            builder.setView(input);
            builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String url = input.getText().toString();
                    if(!url.isEmpty()) {
                        if (url.endsWith("/")) {
                            url = url.substring(0, url.length() - 1);
                        }
                        if(!url.startsWith("http")){
                            Utils.showToast(R.string.add_peer_error_nohttp,PeersActivity.this);
                            return;
                        }
                        Aplikasi.sp.edit().putString("cachepeer",url).apply();
                        final String urlf = url;
                        NuxCoin.getTime(url, new LongCallback() {
                            @Override
                            public void onLongCallback(long time) {
                                if(time>0L){
                                    ObjectBox.addServer(urlf);
                                    adapter.reload();
                                }else{
                                    Utils.showToast(R.string.add_peer_error_failed1,PeersActivity.this);
                                }
                            }

                            @Override
                            public void onErrorCallback(int errorCode, String errorMessage) {
                                Utils.showToast(getString(R.string.add_peer_error_failed2,errorMessage),PeersActivity.this);
                            }
                        });
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel,null);
//            builder.setNeutralButton("Load Peers", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    String url = input.getText().toString();
//                    if(!url.isEmpty()) {
//                        if (url.endsWith("/")) {
//                            url = url.substring(0, url.length() - 1);
//                        }
//                        if(url.startsWith("http")){
//                            AlertDialog.Builder ab = Utils.progressDialog("Download Peers data",PeersActivity.this);
//                            ab.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                                @Override
//                                public void onDismiss(DialogInterface dialog) {
//                                    progress = null;
//                                }
//                            });
//                            progress = ab.show();
//                            NuxCoin.getPeers(url, new JsonCallback() {
//                                @Override
//                                public void onJsonCallback(JSONObject jsonObject) {
//                                    if(progress.isShowing()){
//                                        new ProcessJsonPeer().execute(jsonObject.toString());
//                                    }
//                                }
//
//                                @Override
//                                public void onErrorCallback(int errorCode, String errorMessage) {
//                                    if(progress==null) return;
//                                    progress.setTitle("Failed to download peers");
//                                    progress.setMessage(errorMessage);
//                                    progress.setButton(AlertDialog.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            progress.dismiss();
//                                        }
//                                    });
//                                    Utils.vibrate();
//                                }
//                            });
//                        }else{
//                            Utils.showToast("URL invalid",PeersActivity.this);
//                        }
//                    }
//                }
//            });
            builder.show();
        }
    }



    @Override
    public void onServerClicked(Server server) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.change_peer);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT);
        input.setHint(R.string.peer_hint);
        input.setText(server.url);
        input.setSelectAllOnFocus(true);
        builder.setView(input);
        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String url = input.getText().toString();
                if(!url.isEmpty()) {
                    if (url.endsWith("/")) {
                        url = url.substring(0, url.length() - 1);
                    }
                    server.url = url;
                    ObjectBox.getBoxServer().put(server);
                    adapter.reload();
                }
            }
        });
        builder.setNegativeButton(R.string.peer_set_default, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ObjectBox.setServer(server.url);
                Aplikasi.unixtime=0L;
                finish();
            }
        });
        builder.setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ObjectBox.getBoxServer().remove(server);
                adapter.reload();
            }
        });
        builder.show();
    }


    private class ProcessJsonPeer extends AsyncTask<String,String,String>{
        @Override
        protected String doInBackground(String... strings) {
            int total = 0;
            Utils.log("ProcessJsonPeer: "+ strings[0]);
            try{
                JSONObject json = new JSONObject(strings[0]);
                JSONArray peers = json.getJSONArray("peers");
                int jml = peers.length();
                Utils.log("ProcessJsonPeer: "+ jml);
                for(int n=0;n<jml;n++){
                    JSONObject peer = peers.getJSONObject(n);
                    if(peer.getBoolean("isApiConnectable") && peer.getBoolean("isPeerConnectable") && peer.getBoolean("isApiSupported")){
                        String server ="http://"+
                                peer.getJSONObject("latestPeerData").getString("announcedAddress")+":"+
                                peer.getJSONObject("latestPeerData").getString("apiPort");
                        ObjectBox.addServer(server);
                        publishProgress(server);
                        total++;
                    }
                }
            }catch (Exception e){
                return e.getMessage();
            }
            return getString(R.string.peer_success_added, total);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if(progress!=null)
                progress.setTitle(values[0]);
        }

        @Override
        protected void onPostExecute(String string) {
            if(progress==null) return;
            progress.dismiss();
            Utils.vibrate();
            adapter.reload();
        }
    }
}