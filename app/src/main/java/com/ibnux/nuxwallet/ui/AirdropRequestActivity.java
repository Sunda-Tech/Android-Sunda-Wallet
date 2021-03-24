package com.ibnux.nuxwallet.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import androidx.appcompat.app.AppCompatActivity;
import com.ibnux.nuxwallet.Aplikasi;
import com.ibnux.nuxwallet.R;
import com.ibnux.nuxwallet.adapter.DompetSpinnerAdapter;
import com.ibnux.nuxwallet.data.Dompet;
import com.ibnux.nuxwallet.databinding.ActivityAirdropRequestBinding;
import com.ibnux.nuxwallet.utils.NuxCoin;
import com.ibnux.nuxwallet.utils.TextCallback;
import com.ibnux.nuxwallet.utils.Utils;

public class AirdropRequestActivity extends AppCompatActivity implements View.OnClickListener{
    ActivityAirdropRequestBinding binding;
    Dompet dompet;
    DompetSpinnerAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAirdropRequestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(R.string.airdrop_title);
        adapter = new DompetSpinnerAdapter(this, R.layout.item_dompet);
        binding.spinnerWallet.setAdapter(adapter);

        if(adapter.getCount()==0){
            Utils.showToast(R.string.wallet_dont_have, this);
            finish();
        }

        binding.spinnerWallet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dompet = adapter.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.pesanServer.setText("");

        String nohp = Aplikasi.sp.getString("nohp",null);

        if(nohp==null) {
            binding.layoutOTP.setVisibility(View.GONE);
        }else{
            binding.txtPhone.setText(nohp);
        }
        binding.btnSend.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String otp = binding.txtOTP.getText().toString();
        String nohp = binding.txtPhone.getText().toString();
        if(nohp.isEmpty()){
            binding.txtPhone.setError("Wajib diisi");
            binding.txtPhone.requestFocus();
            return;
        }

        binding.btnSend.setVisibility(View.GONE);

        NuxCoin.requestAirdop(dompet, nohp, otp, new TextCallback() {
            @Override
            public void onTextCallback(String string) {
                binding.pesanServer.setText(string);
                if(string.startsWith("SUCCESS")) {
                    if (otp.isEmpty()) {
                        Aplikasi.sp.edit().putString("nohp",nohp).apply();
                        binding.layoutOTP.setVisibility(View.VISIBLE);
                        binding.txtOTP.requestFocus();
                    }
                }
                binding.btnSend.setVisibility(View.VISIBLE);
            }

            @Override
            public void onErrorCallback(int errorCode, String errorMessage) {
                binding.btnSend.setVisibility(View.VISIBLE);
                binding.pesanServer.setText(errorMessage);
            }
        });

    }
}