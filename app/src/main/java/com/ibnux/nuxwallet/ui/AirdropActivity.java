package com.ibnux.nuxwallet.ui;

/******************************************************************************\
 * GNU GENERAL PUBLIC LICENSE                                                 *
 * Version 3, 29 June 2007                                                    *
 * Ibnu Maksum https://github.com/ibnux/                                      *
 ******************************************************************************
 * This source and program come as is, WITHOUT ANY WARRANTY and/or WITHOUT    *
 * ANY IMPLIED WARRANTY.                                                      *
 \******************************************************************************/

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.ibnux.nuxwallet.Aplikasi;
import com.ibnux.nuxwallet.Constants;
import com.ibnux.nuxwallet.R;
import com.ibnux.nuxwallet.adapter.DompetSpinnerAdapter;
import com.ibnux.nuxwallet.adapter.TransaksiAirdropAdapter;
import com.ibnux.nuxwallet.data.Dompet;
import com.ibnux.nuxwallet.data.Dompet_;
import com.ibnux.nuxwallet.data.ObjectBox;
import com.ibnux.nuxwallet.data.Transaksi;
import com.ibnux.nuxwallet.databinding.ActivityAirdropBinding;
import com.ibnux.nuxwallet.utils.JsonCallback;
import com.ibnux.nuxwallet.utils.NuxCoin;
import com.ibnux.nuxwallet.utils.Utils;
import org.json.JSONObject;

import java.util.List;

public class AirdropActivity extends AppCompatActivity {
    ActivityAirdropBinding binding;
    TransaksiAirdropAdapter adapter;
    DompetSpinnerAdapter adapterSpinner;
    Dompet dompetSelected;
    boolean isStarted = false;
    List<Dompet> dompets;
    int pos = -1;
    int jumlah = 10000;
    Dompet dompetTarget;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAirdropBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapter = new TransaksiAirdropAdapter();

        binding.listTransaksi.setHasFixedSize(true);
        binding.listTransaksi.setLayoutManager(new LinearLayoutManager(this));
        binding.listTransaksi.setAdapter(adapter);
        binding.progressBar.setVisibility(View.GONE);
        binding.txtStatus1.setVisibility(View.GONE);
        binding.txtStatus2.setVisibility(View.GONE);
        adapterSpinner = new DompetSpinnerAdapter(this, R.layout.item_dompet);
        binding.spinnerWallet.setAdapter(adapterSpinner);

        if(adapterSpinner.getCount()==0){
            Utils.showToast(R.string.wallet_dont_have, this);
            finish();
        }
        binding.spinnerWallet.setSelection(0);

        binding.spinnerWallet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dompetSelected = adapterSpinner.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        dompets = ObjectBox.getDompet().query().equal(Dompet_.isMe,false).build().find();
        binding.txtStatus2.setText("");
        prosesNomor();

        binding.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkData();
            }
        });
    }

    public void prosesNomor(){
        binding.txtStatus2.setText((pos+1)+" from "+dompets.size()+" address processed");
    }

    public void checkData(){
        if(binding.txtValue.getText().toString().isEmpty()) {
            binding.txtValue.setError(getString(R.string.dont_empty));
            return;
        }
        int jml = Integer.parseInt(binding.txtValue.getText().toString())*dompets.size();
        if(jml>dompetSelected.saldo){
            Utils.showToast(getString(R.string.insufficient_funds, Utils.nuxFormat(jml)), AirdropActivity.this);
            return;
        }
        new AlertDialog.Builder(this)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.airdrop_title)
                .setMessage(getString(R.string.airdrop_message,Utils.nuxFormat(jml),dompets.size()))
                .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pos = 0;
                        Utils.vibrate();
                        binding.progressBar.setVisibility(View.VISIBLE);
                        binding.txtStatus1.setVisibility(View.VISIBLE);
                        binding.txtStatus2.setVisibility(View.VISIBLE);
                        binding.progressBar.setIndeterminate(false);
                        binding.progressBar.setMax(dompets.size());
                        binding.layoutForm.setVisibility(View.GONE);
                        startAirdrop();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public void startAirdrop(){
        prosesNomor();
        isStarted = true;
        dompetTarget = dompets.get(pos);
        binding.txtStatus2.setText(getString(R.string.airdrop_processing,dompetTarget.alamat));

        if(binding.offlineSigning.isChecked()){
            NuxCoin.sendCoin(dompetSelected, dompetTarget.alamat, binding.txtValue.getText().toString(),
                    String.valueOf(Constants.default_fee), binding.txtNote.getText().toString(), dompetTarget.publicKey, null, new JsonCallback() {
                        @Override
                        public void onJsonCallback(JSONObject jsonObject) {
                            try {
                                if (jsonObject.has("unsignedTransactionBytes")) {
                                    signingTX(jsonObject.getString("unsignedTransactionBytes"), dompetSelected.secretPhrase);
                                } else {
                                    Utils.showToast(R.string.airdrop_failed, AirdropActivity.this);
                                }
                            } catch (Exception e) {
                                Utils.showToast(e.getMessage(), AirdropActivity.this);
                            }
                        }

                        @Override
                        public void onErrorCallback(int errorCode, String errorMessage) {
                            Utils.showToast(errorMessage, AirdropActivity.this);
                        }
                    });
        }else {
            NuxCoin.sendCoinOnline(dompetSelected, dompetTarget.alamat, binding.txtValue.getText().toString(),
                    String.valueOf(Constants.default_fee), binding.txtNote.getText().toString(), dompetTarget.publicKey,
                    null, new JsonCallback() {
                        @Override
                        public void onJsonCallback(JSONObject jsonObject) {
                            try{
                                if(jsonObject.has("SENDCOIN") && jsonObject.getString("SENDCOIN").equals("SUCCESS")){
                                    Transaksi tx = new Transaksi();
                                    tx.isRead = true;
                                    tx.transaction = String.valueOf(System.currentTimeMillis());
                                    tx.amountNQT = binding.txtValue.getText().toString();
                                    tx.timestamp = System.currentTimeMillis();
                                    tx.senderRS = dompetSelected.alamat;
                                    tx.recipientRS = dompetTarget.alamat;
                                    tx.message = binding.txtNote.getText().toString();
                                    adapter.addTX(tx);
                                    lanjutNext();
                                }else{
                                    Utils.showToast(R.string.airdrop_failed, AirdropActivity.this);
                                }
                            }catch (Exception e){
                                Utils.showToast(e.getMessage(), AirdropActivity.this);
                            }
                        }

                        @Override
                        public void onErrorCallback(int errorCode, String errorMessage) {
                            if(errorCode==10001){
                                Utils.showToast(errorMessage, AirdropActivity.this);
                            }else{
                                Utils.showToast(errorMessage, AirdropActivity.this);
                            }
                        }
                    });
        }
    }

    public void lanjutNext(){
        pos++;
        binding.progressBar.setProgress(pos);
        if(pos<dompets.size()){
            startAirdrop();
        }else{
            binding.txtStatus1.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.GONE);
            binding.txtStatus2.setVisibility(View.GONE);
            binding.layoutForm.setVisibility(View.VISIBLE);
            Utils.vibrate();
            Utils.showToast(R.string.airdrop_success, AirdropActivity.this);
            isStarted = false;
        }
    }

    public void signingTX(String unsignedTransactionBytes, String secretPhrase){
        Intent intent = new Intent(Aplikasi.app, OfflineSigningActivity.class);
        intent.putExtra("utb",unsignedTransactionBytes);
        intent.putExtra("secret",secretPhrase);
        startActivityForResult(intent,6868);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==6868) {
                if (data.hasExtra("success"))
                    NuxCoin.sendingMoney(data.getStringExtra("success"), null, new JsonCallback() {
                        @Override
                        public void onJsonCallback(JSONObject jsonObject) {
                            try {
                                if (jsonObject.has("SENDCOIN") && jsonObject.getString("SENDCOIN").equals("SUCCESS")) {
                                    Transaksi tx = new Transaksi();
                                    tx.isRead = true;
                                    tx.transaction = String.valueOf(System.currentTimeMillis());
                                    tx.amountNQT = binding.txtValue.getText().toString();
                                    tx.timestamp = System.currentTimeMillis();
                                    tx.senderRS = dompetSelected.alamat;
                                    tx.recipientRS = dompetTarget.alamat;
                                    tx.message = binding.txtNote.getText().toString();
                                    adapter.addTX(tx);
                                    lanjutNext();
                                } else {
                                    Utils.showToast(R.string.airdrop_failed, AirdropActivity.this);
                                }
                            } catch (Exception e) {
                                Utils.showToast(e.getMessage(), AirdropActivity.this);
                            }
                        }

                        @Override
                        public void onErrorCallback(int errorCode, String errorMessage) {
                            Utils.showToast(errorMessage, AirdropActivity.this);
                        }
                    });
                else
                    Utils.showToast(R.string.failed_signing_offline, this);
            }
        }else{
            Utils.showToast(R.string.failed_signing_offline, this);
        }
    }

    @Override
    public void finish() {
        if(!isStarted)
            super.finish();
    }

    @Override
    public void onBackPressed() {
        if(!isStarted)
            super.onBackPressed();
    }
}