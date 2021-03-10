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
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.common.Priority;
import com.google.gson.Gson;
import com.ibnux.nuxwallet.Aplikasi;
import com.ibnux.nuxwallet.Constants;
import com.ibnux.nuxwallet.R;
import com.ibnux.nuxwallet.adapter.TransaksiAdapter;
import com.ibnux.nuxwallet.data.Dompet;
import com.ibnux.nuxwallet.data.ObjectBox;
import com.ibnux.nuxwallet.data.Transaksi;
import com.ibnux.nuxwallet.data.Transaksi_;
import com.ibnux.nuxwallet.databinding.ActivityViewWalletBinding;
import com.ibnux.nuxwallet.utils.JsonCallback;
import com.ibnux.nuxwallet.utils.NuxCoin;
import com.ibnux.nuxwallet.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class ViewWalletActivity extends AppCompatActivity implements View.OnClickListener, TransaksiAdapter.TransaksiCallback,JsonCallback {
    ActivityViewWalletBinding binding;
    String alamat;
    TransaksiAdapter adapter;
    int pos = 0;
    Dompet dompet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewWalletBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("SUNDA COIN");
        Intent intent = getIntent();
        if(!intent.hasExtra("alamat")) finish();
        alamat = intent.getStringExtra("alamat");
        binding.txtWallet.setText(alamat);
        dompet = ObjectBox.getDompet(alamat);
        if(dompet==null) {
            binding.txtBalance.setText("Check artos...");
            NuxCoin.getAccount(alamat, Priority.HIGH, new JsonCallback() {
                @Override
                public void onJsonCallback(JSONObject jsonObject) {
                    try{
                        if(jsonObject.has("errorCode") && jsonObject.getInt("errorCode")==5){
                            binding.txtBalance.setText("DOmpet can didaftarkeun, kudu aya saldo");
                        }else if(jsonObject.has("errorDescription")){
                            binding.txtBalance.setText(jsonObject.getString("errorDescription"));
                        }else if(jsonObject.has("balanceNQT")){
                            dompet = new Dompet();
                            dompet.alamat = alamat;
                            dompet.dompetID = jsonObject.getString("account");
                            dompet.saldo = jsonObject.getLong("balanceNQT");
                            dompet.isMe = false;
                            dompet.publicKey = jsonObject.getString("publicKey");
                        }
                        uiProcess();
                    }catch (Exception e){
                        binding.txtBalance.setText("Dompet can di daftarkeun, kudu aya saldo");
                    }
                }

                @Override
                public void onErrorCallback(int errorCode, String errorMessage) {
                    Utils.showToast("Failed to connect to server\n"+errorMessage,ViewWalletActivity.this);
                }
            });
        }else {
            uiProcess();
        }

        if(intent.hasExtra("transaction")){
            ViewTransactionFragment.newInstance(intent.getStringExtra("transaction")).show(getSupportFragmentManager(),"viewtx");
        }

        adapter = new TransaksiAdapter(this,alamat);
        binding.listTransaksi.setHasFixedSize(true);
        binding.listTransaksi.setLayoutManager(new LinearLayoutManager(this));
        binding.listTransaksi.setAdapter(adapter);

        binding.btnBarcode.setOnClickListener(this);
        binding.btnSend.setOnClickListener(this);
        binding.txtWalletName.setOnClickListener(this);
        binding.txtWalletNote.setOnClickListener(this);
        binding.txtWallet.setOnClickListener(this);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(binding.listTransaksi);

    }

    public void uiProcess(){
        if(dompet!=null && dompet.isMe){
            binding.btnBarcode.setText("Barcode");
            binding.card.setCardBackgroundColor(ContextCompat.getColor(Aplikasi.app, R.color.blue_800));
        }else{
            if(dompet!=null && dompet.id>0){
                binding.btnBarcode.setVisibility(View.GONE);
            }else{
                binding.btnBarcode.setText("Save");
            }
            binding.card.setCardBackgroundColor(ContextCompat.getColor(Aplikasi.app,R.color.green_800));
        }
        if(dompet!=null && dompet.alamat.equals(dompet.nama)) {
            binding.txtWalletName.setVisibility(View.GONE);
        }else{
            binding.txtWalletName.setVisibility(View.VISIBLE);
            if(dompet.nama!=null)
                binding.txtWalletName.setText(dompet.nama);
            else if(dompet.id>0)
                binding.txtWalletName.setText("Nu boga dompet saha?");
            else
                binding.txtWalletName.setText("");
        }
        if(dompet!=null && dompet.catatan!=null)
            binding.txtWalletNote.setText(dompet.catatan);
        else if(dompet.id>0)
            binding.txtWalletNote.setText("bere catetan?");
        else
            binding.txtWalletNote.setText("");

        binding.txtBalance.setText(Utils.nuxFormat(dompet.saldo));
    }

    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN | ItemTouchHelper.UP) {

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            int position = viewHolder.getAdapterPosition();
            Transaksi tx = adapter.getData(position);
            new androidx.appcompat.app.AlertDialog.Builder(ViewWalletActivity.this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setMessage("Delete it?")
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            adapter.reload();
                        }
                    })
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            new androidx.appcompat.app.AlertDialog.Builder(ViewWalletActivity.this)
                                    .setIcon(R.mipmap.ic_launcher)
                                    .setTitle("Are You sure?")
                                    .setPositiveButton("Sure", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ObjectBox.getTransaksi().query().equal(Transaksi_.transaction,tx.transaction).build().remove();
                                            adapter.reload();
                                        }
                                    })
                                    .setNegativeButton("Nope", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            adapter.reload();
                                        }
                                    })
                                    .show();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            adapter.reload();
                        }
                    })
                    .show();
        }
    };

    @Override
    public void onClick(View v) {
        if(v==binding.btnBarcode){
            if(dompet !=null && dompet.isMe()) {
                Intent i = new Intent(this, QRCodeActivity.class);
                i.putExtra("alamat", alamat);
                startActivity(i);
            }else{
                //Ask Name
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewWalletActivity.this);
                builder.setTitle("Ngaran Dompetna?");
                final EditText input = new EditText(ViewWalletActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                input.setGravity(Gravity.CENTER_HORIZONTAL);
                input.setHint("Optional");
                input.setText(dompet.nama);
                input.setSelectAllOnFocus(true);
                builder.setView(input);
                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dompet.nama = input.getText().toString();
                        if (ObjectBox.addDompet(dompet) > 0) {
                            Utils.showToast("Wallet Saved", ViewWalletActivity.this);
                            uiProcess();
                        } else {
                            Utils.showToast("Wallet not Saved", ViewWalletActivity.this);
                        }
                    }
                });
                builder.setNegativeButton("No Name", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (ObjectBox.addDompet(dompet) > 0) {
                            Utils.showToast("Wallet Saved", ViewWalletActivity.this);
                            uiProcess();
                        } else {
                            Utils.showToast("Wallet not Saved", ViewWalletActivity.this);
                        }
                    }
                });
                builder.show();
            }
        }else if(v==binding.txtWallet){
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(getString(R.string.app_name), alamat);
            clipboard.setPrimaryClip(clip);
            Utils.showToast(alamat+" copied!",this);
        }else if(v==binding.btnSend){
            Intent i = new Intent(this, SendMoneyActivity.class);
            if(dompet.isMe) {
                if(dompet.saldo>0)
                    i.putExtra("from", alamat);
                else {
                    Utils.showToast("Duitna kurang", this);
                    return;
                }
            }else {
                i.putExtra("to", alamat);
                if(dompet!=null && dompet.publicKey!=null)
                    i.putExtra("public_key", dompet.publicKey);
            }
            startActivity(i);
        }else if(v==binding.txtWalletNote){
            if(dompet.id==0) return;
            //Ask Name
            AlertDialog.Builder builder = new AlertDialog.Builder(ViewWalletActivity.this);
            builder.setTitle("Bere catetan?");
            final EditText input = new EditText(ViewWalletActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            input.setGravity(Gravity.CENTER_HORIZONTAL);
            input.setHint("Catetanna naon");
            input.setText(dompet.catatan);
            input.setLines(2);
            input.setSelectAllOnFocus(true);
            builder.setView(input);
            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dompet.catatan = input.getText().toString();
                    if(ObjectBox.addDompet(dompet)>0){
                        Utils.showToast("Wallet Saved",ViewWalletActivity.this);
                        uiProcess();
                    }else{
                        Utils.showToast("Wallet not Saved",ViewWalletActivity.this);
                    }
                }
            });
            builder.setNegativeButton("Cancel",null);
            builder.show();
        }else if(v==binding.txtWalletName){
            if(dompet.id==0) return;
            //Ask Name
            AlertDialog.Builder builder = new AlertDialog.Builder(ViewWalletActivity.this);
            builder.setTitle("Wallet Name?");
            final EditText input = new EditText(ViewWalletActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            input.setGravity(Gravity.CENTER_HORIZONTAL);
            input.setHint("Wallet function");
            builder.setView(input);
            input.setText(dompet.nama);
            input.setSelectAllOnFocus(true);
            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dompet.nama = input.getText().toString();
                    if(ObjectBox.addDompet(dompet)>0){
                        Utils.showToast("Wallet Saved",ViewWalletActivity.this);
                        uiProcess();
                    }else{
                        Utils.showToast("Wallet not Saved",ViewWalletActivity.this);
                    }
                }
            });
            builder.setNegativeButton("Cancel",null);
            builder.show();
        }
    }

    public void getTransaksi(){
        NuxCoin.getTransactions(alamat, pos, Constants.limitGetTX, this);
    }

    @Override
    public void onJsonCallback(JSONObject jsonObject) {
        try{
            if(jsonObject.has("transactions")){
                JSONArray transactions = jsonObject.getJSONArray("transactions");
                int jml = transactions.length();
                if(jml>0) {
                    int ada = 0;
                    for (int n = 0; n < jml; n++) {
                        //TODO delete log
                        Utils.log(transactions.getJSONObject(n).toString());
                        JSONObject json = transactions.getJSONObject(n);
                        Transaksi tx = new Gson().fromJson(json.toString(),Transaksi.class);
                        if(ObjectBox.getTransaksi().query().equal(Transaksi_.transaction,tx.transaction).build().findFirst()==null) {
                            tx.timestampInsert = System.currentTimeMillis();
                            if (json.has("attachment")) {
                                if (json.getJSONObject("attachment").has("message")) {
                                    tx.message = json.getJSONObject("attachment").getString("message");
                                }
                            }
                            Intent intent = new Intent(ViewWalletActivity.this, ViewWalletActivity.class);
                            intent.putExtra("alamat",alamat);
                            intent.putExtra("transaction",tx.transaction);
                            if((tx.recipientRS.equals(alamat))) {
                                Utils.sendNotification(
                                        ObjectBox.getNamaDompet(tx.senderRS) + " ngirim duit",
                                        "Meunang " + tx.amountNQT + " SND ti " + ObjectBox.getNamaDompet(tx.recipientRS),
                                        intent,
                                        "transaction",
                                        "Transaction"

                                );
                            }else{
                                Utils.sendNotification(
                                        "You sent a coin from "+ObjectBox.getNamaDompet(tx.senderRS),
                                        tx.amountNQT + " SND ka " + ObjectBox.getNamaDompet(tx.recipientRS)+ " geus kakirim",
                                        intent,
                                        "transaction",
                                        "Transaction"

                                );
                            }
                            ObjectBox.addTransaksi(tx);
                        }else{
                            ada++;
                        }
                    }
                    if(ada==0) {
                        pos += Constants.limitGetTX;
                        getTransaksi();
                        adapter.reload();
                    }else{
                        binding.progressBar.setVisibility(View.GONE);
                    }
                }else{
                    binding.progressBar.setVisibility(View.GONE);
                }
            }else{
                binding.progressBar.setVisibility(View.GONE);
            }
        }catch (Exception e){
            e.printStackTrace();
            Utils.showToast("Failed to parsing data\n"+e.getMessage(),ViewWalletActivity.this);
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getTransaksi();
    }

    @Override
    public void onErrorCallback(int errorCode, String errorMessage) {
        binding.progressBar.setVisibility(View.GONE);
        Utils.showToast(errorMessage,this);
    }

    @Override
    public void onTransaksiClicked(Transaksi transaksi) {
        transaksi.isRead = true;
        ObjectBox.addTransaksi(transaksi);
        adapter.notifyDataSetChanged();
        ViewTransactionFragment.newInstance(transaksi.transaction).show(getSupportFragmentManager(),"viewtx");
    }

    @Override
    public void onTransaksiWalletClicked(String alamat) {
        if(!alamat.equals(this.alamat)) {
            Intent intent = new Intent(this, ViewWalletActivity.class);
            intent.putExtra("alamat", alamat);
            startActivity(intent);
        }
    }
}