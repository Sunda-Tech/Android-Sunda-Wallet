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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.androidnetworking.common.Priority;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ibnux.nuxwallet.Aplikasi;
import com.ibnux.nuxwallet.R;
import com.ibnux.nuxwallet.data.Dompet;
import com.ibnux.nuxwallet.data.ObjectBox;
import com.ibnux.nuxwallet.databinding.FragmentAddWalletBinding;
import com.ibnux.nuxwallet.utils.JsonCallback;
import com.ibnux.nuxwallet.utils.NuxCoin;
import com.ibnux.nuxwallet.utils.Utils;
import com.scottyab.aescrypt.AESCrypt;
import org.json.JSONObject;

import static android.app.Activity.RESULT_OK;

public class AddWalletFragment extends BottomSheetDialogFragment implements View.OnClickListener {
    FragmentAddWalletBinding binding;

    public static AddWalletFragment newInstance() {
        final AddWalletFragment fragment = new AddWalletFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddWalletBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.btnScanBarcode.setOnClickListener(this);
        binding.btnGenerate.setOnClickListener(this);
        binding.btnScan.setOnClickListener(this);
        binding.btnAddAddress.setOnClickListener(this);
        binding.btnAddAddress2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(binding.btnScanBarcode==v){
            startActivityForResult(new Intent(getContext(), ScanActivity.class), 2345);
        }else  if(binding.btnGenerate==v){
            startActivity(new Intent(getContext(),WalletGeneratorActivity.class));
            dismiss();
        }else  if(binding.btnAddAddress==v){
            askAddress();
        }else  if(binding.btnAddAddress2==v){
            askAddress2();
        }else  if(binding.btnScan==v){
            startActivityForResult(new Intent(getContext(), ScanActivity.class), 2346);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==2345){
                if(data.hasExtra("result")) {
                    String qr = data.getStringExtra("result");
                    if(qr.startsWith("SECRET:")){
                        try {
                            String result = AESCrypt.decrypt(Aplikasi.getPin(), qr.substring("SECRET:".length()));
                            Intent i = new Intent(getContext(),WalletGeneratorActivity.class);
                            i.putExtra("data",result);
                            startActivity(i);
                            dismiss();
                        }catch (Exception e){
                            Toast.makeText(getContext(), getString(R.string.error_decrypting)+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Intent i = new Intent(getContext(),WalletGeneratorActivity.class);
                        i.putExtra("data",qr);
                        startActivity(i);
                        dismiss();
                    }
                }
            }else if(requestCode==2346){
                if(data.hasExtra("result")) {
                    String dt = data.getStringExtra("result");
                    if(dt.startsWith("{")){
                        try{
                            JSONObject json = new JSONObject(dt);
                            Intent i = new Intent(getContext(), SendMoneyActivity.class);
                            i.putExtra("to",json.getString("address"));
                            i.putExtra("public_key",json.getString("public_key"));
                            startActivity(i);
                            dismiss();
                        }catch (Exception e){
                            e.printStackTrace();
                            Utils.showToast(getString(R.string.unknown_qrcode),getActivity());
                        }
                    }else if(dt.startsWith("APK:")){
                        dt = dt.substring(4);
                        String[] dts = dt.split("APKAPKAPK");
                        Intent i = new Intent(getContext(), SendMoneyActivity.class);
                        i.putExtra("to",dts[0]);
                        i.putExtra("public_key",dts[1]);
                        startActivity(i);
                        dismiss();
                    }else {
                        Intent i = new Intent(getContext(), SendMoneyActivity.class);
                        i.putExtra("to",dt.toUpperCase());
                        startActivity(i);
                        dismiss();
                    }
                }
            }

        }
    }

    public void askAddress(){
        String paste = "";
        try{
            ClipData.Item item = ((ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE)).getPrimaryClip().getItemAt(0);
            paste = item.getText().toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.wallet_address_ask);
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        input.setHint(R.string.wallet_address_hint);
        builder.setView(input);
        input.setText(paste);
        input.setSelectAllOnFocus(true);
        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String alamat = input.getText().toString();
                if(alamat.length()==24){
                    askName(alamat);
                }else{
                    Utils.showToast(getString(R.string.address_not_valid),getActivity());
                }
            }
        });
        builder.setNegativeButton(R.string.cancel,null);
        builder.show();
    }


    public void askAddress2(){
        String paste = "";
        try{
            ClipData.Item item = ((ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE)).getPrimaryClip().getItemAt(0);
            paste = item.getText().toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.wallet_address_ask);
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        input.setHint(R.string.wallet_address_hint);
        builder.setView(input);
        input.setText(paste);
        input.setSelectAllOnFocus(true);
        builder.setPositiveButton(R.string.send_coin, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String alamat = input.getText().toString();
                if(alamat.length()==24){
                    Intent i = new Intent(getContext(), SendMoneyActivity.class);
                    i.putExtra("to",alamat);
                    startActivity(i);
                    dismiss();
                }else{
                    Utils.showToast(R.string.address_not_valid,getActivity());
                }
            }
        });
        builder.setNegativeButton(R.string.cancel,null);
        builder.show();
    }

    public void askName(String alamat){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.wallet_address_ask);
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        input.setHint(R.string.wallet_note);
        builder.setView(input);
        input.setSelectAllOnFocus(true);
        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nm = input.getText().toString();
                if(nm.isEmpty())
                    nm = alamat;
                final String nama = nm;
                NuxCoin.getAccount(alamat, Priority.HIGH, new JsonCallback() {
                    @Override
                    public void onJsonCallback(JSONObject jsonObject) {
                        Dompet dompet = new Dompet();
                        try{
                            if(jsonObject.has("balanceNQT")){
                                dompet.alamat = alamat;
                                dompet.nama = nama;
                                dompet.dompetID = jsonObject.getString("account");
                                dompet.saldo = jsonObject.getLong("balanceNQT");
                                dompet.isMe = false;
                                dompet.publicKey = jsonObject.getString("publicKey");
                            }else{
                                dompet.alamat = alamat;
                                dompet.nama = nama;
                                dompet.isMe = false;
                            }
                        }catch (Exception e){
                            dompet.alamat = alamat;
                            dompet.nama = nama;
                            dompet.isMe = false;
                        }
                        ObjectBox.addDompet(dompet);
                        Utils.showToast(getString(R.string.wallet_added), getActivity());
                        AddWalletFragment.this.dismiss();
                    }

                    @Override
                    public void onErrorCallback(int errorCode, String errorMessage) {
                        Dompet dompet = new Dompet();
                        dompet.alamat = alamat;
                        dompet.nama = nama;
                        dompet.isMe = false;
                        ObjectBox.addDompet(dompet);
                        Utils.showToast(getString(R.string.wallet_added), getActivity());
                        AddWalletFragment.this.dismiss();
                    }
                });
            }
        });
        builder.setNegativeButton(R.string.cancel,null);
        builder.show();
    }
}