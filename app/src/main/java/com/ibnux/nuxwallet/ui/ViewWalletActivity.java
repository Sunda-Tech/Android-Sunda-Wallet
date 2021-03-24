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
import android.content.*;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
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
        setTitle(R.string.app_name);
        Intent intent = getIntent();
        if(!intent.hasExtra("alamat")) finish();
        alamat = intent.getStringExtra("alamat");
        binding.txtWallet.setText(alamat);
        dompet = ObjectBox.getDompet(alamat);
        if(dompet==null) {
            binding.txtBalance.setText(R.string.getting_balance);
            NuxCoin.getAccount(alamat, Priority.HIGH, new JsonCallback() {
                @Override
                public void onJsonCallback(JSONObject jsonObject) {
                    try{
                        if(jsonObject.has("errorCode") && jsonObject.getInt("errorCode")==5){
                            binding.txtBalance.setText(R.string.account_not_registered);
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
                        binding.txtBalance.setText(R.string.account_not_registered);
                    }
                }

                @Override
                public void onErrorCallback(int errorCode, String errorMessage) {
                    Utils.showToast(getString(R.string.failed_to_connect_to_server,errorMessage),ViewWalletActivity.this);
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
            binding.btnBarcode.setText(R.string.qrcode);
            binding.card.setCardBackgroundColor(ContextCompat.getColor(Aplikasi.app, R.color.blue_800));
        }else{
            if(dompet!=null && dompet.id>0){
                binding.btnBarcode.setVisibility(View.GONE);
            }else{
                binding.btnBarcode.setText(R.string.save);
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
                binding.txtWalletName.setText(R.string.ask_wallet_name);
            else
                binding.txtWalletName.setText("");
        }
        if(dompet!=null && dompet.catatan!=null)
            binding.txtWalletNote.setText(dompet.catatan);
        else if(dompet.id>0)
            binding.txtWalletNote.setText(R.string.ask_wallet_note);
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
                    .setMessage(R.string.ask_delete)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            adapter.reload();
                        }
                    })
                    .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            new androidx.appcompat.app.AlertDialog.Builder(ViewWalletActivity.this)
                                    .setIcon(R.mipmap.ic_launcher)
                                    .setTitle(R.string.ask_are_you_sure)
                                    .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ObjectBox.getTransaksi().query().equal(Transaksi_.transaction,tx.transaction).build().remove();
                                            adapter.reload();
                                        }
                                    })
                                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            adapter.reload();
                                        }
                                    })
                                    .show();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
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
                builder.setTitle(R.string.ask_wallet_name);
                final EditText input = new EditText(ViewWalletActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                input.setGravity(Gravity.CENTER_HORIZONTAL);
                input.setHint(R.string.optional);
                input.setText(dompet.nama);
                input.setSelectAllOnFocus(true);
                builder.setView(input);
                builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dompet.nama = input.getText().toString();
                        if (ObjectBox.addDompet(dompet) > 0) {
                            Utils.showToast(R.string.wallet_saved, ViewWalletActivity.this);
                            uiProcess();
                        } else {
                            Utils.showToast(R.string.wallet_not_saved, ViewWalletActivity.this);
                        }
                    }
                });
                builder.setNegativeButton(R.string.wallet_no_name, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (ObjectBox.addDompet(dompet) > 0) {
                            Utils.showToast(R.string.wallet_saved, ViewWalletActivity.this);
                            uiProcess();
                        } else {
                            Utils.showToast(R.string.wallet_not_saved, ViewWalletActivity.this);
                        }
                    }
                });
                builder.show();
            }
        }else if(v==binding.txtWallet){
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(getString(R.string.app_name), alamat);
            clipboard.setPrimaryClip(clip);
            Utils.showToast(getString(R.string.a_copied,alamat),this);
        }else if(v==binding.btnSend){
            Intent i = new Intent(this, SendMoneyActivity.class);
            if(dompet.isMe) {
                if(dompet.saldo>0)
                    i.putExtra("from", alamat);
                else {
                    Utils.showToast(R.string.insufficient_funds, this);
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
            builder.setTitle(R.string.ask_wallet_note);
            final EditText input = new EditText(ViewWalletActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            input.setGravity(Gravity.CENTER_HORIZONTAL);
            input.setHint(R.string.ask_wallet_note);
            input.setText(dompet.catatan);
            input.setLines(2);
            input.setSelectAllOnFocus(true);
            builder.setView(input);
            builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dompet.catatan = input.getText().toString();
                    if(ObjectBox.addDompet(dompet)>0){
                        Utils.showToast(R.string.wallet_saved,ViewWalletActivity.this);
                        uiProcess();
                    }else{
                        Utils.showToast(R.string.wallet_not_saved,ViewWalletActivity.this);
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel,null);
            builder.show();
        }else if(v==binding.txtWalletName){
            if(dompet.id==0) return;
            //Ask Name
            AlertDialog.Builder builder = new AlertDialog.Builder(ViewWalletActivity.this);
            builder.setTitle(R.string.ask_wallet_name);
            final EditText input = new EditText(ViewWalletActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            input.setGravity(Gravity.CENTER_HORIZONTAL);
            input.setHint(R.string.ask_wallet_note);
            builder.setView(input);
            input.setText(dompet.nama);
            input.setSelectAllOnFocus(true);
            builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dompet.nama = input.getText().toString();
                    if(ObjectBox.addDompet(dompet)>0){
                        Utils.showToast(R.string.wallet_saved,ViewWalletActivity.this);
                        uiProcess();
                    }else{
                        Utils.showToast(R.string.wallet_not_saved,ViewWalletActivity.this);
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel,null);
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
//                            if((tx.recipientRS.equals(alamat))) {
//                                Utils.sendNotification(
//                                        getString(R.string.service_notification_received_title,ObjectBox.getNamaDompet(tx.senderRS)),
//                                        getString(R.string.service_notification_received_message, tx.amountNQT, ObjectBox.getNamaDompet(tx.recipientRS)),
//                                        intent,
//                                        "transaction",
//                                        "Transaction"
//
//                                );
//                            }else{
//                                Utils.sendNotification(
//                                        getString(R.string.service_notification_sending_title,ObjectBox.getNamaDompet(tx.senderRS)),
//                                        getString(R.string.service_notification_sending_message,tx.amountNQT , ObjectBox.getNamaDompet(tx.recipientRS)),
//                                        intent,
//                                        "transaction",
//                                        "Transaction"
//
//                                );
//                            }
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
            Utils.showToast(getString(R.string.failed_parsing_data_from_server,e.getMessage()),ViewWalletActivity.this);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.wallet_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_nav_referral:
                if(dompet.isMe) {
                    new androidx.appcompat.app.AlertDialog.Builder(ViewWalletActivity.this)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.dialog_share_referral_title)
                            .setMessage(
                                    getString(
                                            R.string.dialog_share_referral_body,
                                            getString(
                                                    R.string.dialog_share_referral_url,
                                                    dompet.alamat
                                            )
                                    )
                            )
                            .setPositiveButton(R.string.share, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                                    intent.setType("text/plain");
                                    intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
                                    intent.putExtra(android.content.Intent.EXTRA_TEXT, getString(
                                            R.string.dialog_share_referral_text,
                                            getString(
                                                    R.string.dialog_share_referral_url,
                                                    dompet.alamat
                                            )
                                    ));
                                    startActivity(Intent.createChooser(intent, getString(R.string.share_using)));
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                }else{
                    Utils.showToast(R.string.dialog_share_warning,this);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}