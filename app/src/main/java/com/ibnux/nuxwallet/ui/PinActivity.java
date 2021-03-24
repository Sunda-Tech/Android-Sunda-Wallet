package com.ibnux.nuxwallet.ui;

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
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.ibnux.nuxwallet.Aplikasi;
import com.ibnux.nuxwallet.R;
import com.ibnux.nuxwallet.databinding.ActivityPinBinding;
import com.ibnux.nuxwallet.utils.Utils;


public class PinActivity extends AppCompatActivity implements View.OnClickListener {
    ActivityPinBinding binding;
    boolean isNew = false;
    String pinnew = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(R.string.pin_code_title);
        String pin = Aplikasi.getPin();
        if (pin == null) {
            binding.txtInfo.setText(R.string.pin_create_new);
            isNew = true;
        }
        binding.button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(isNew){
            if(pinnew==null){
                pinnew = binding.inputPin.getText().toString();
                binding.txtInfo.setText(R.string.pin_repeat_new);
                binding.inputPin.setText("");
                Utils.vibrate();
            }else{
                if(pinnew.equals(binding.inputPin.getText().toString())){
                    Aplikasi.setPin(Utils.SHA256(pinnew));
                    Intent i = getIntent();
                    i.putExtra("SUKSES","SUKSES");
                    setResult(RESULT_OK,i);
                    finish();
                }else{
                    Utils.vibrate();
                    Utils.showToast(R.string.pin_different_new,this);
                    binding.txtInfo.setText(R.string.pin_create_new);
                    binding.inputPin.setText("");
                    pinnew = null;
                }
            }
        }else{
            if(Aplikasi.getPin().equals(Utils.SHA256(binding.inputPin.getText().toString()))){
                Intent i = getIntent();
                i.putExtra("SUKSES","SUKSES");
                setResult(RESULT_OK,i);
                finish();
            }else{
                Utils.vibrate();
                Utils.showToast(R.string.pin_wrong,this);
                binding.inputPin.setText("");
                pinnew = null;
            }
        }
    }

    public void addpin(View view){
        String tv = ((TextView)view).getText().toString();
        if(tv.equals("clr")){
            binding.inputPin.setText("");
        }else if(tv.equals("del")){
            String txt =  binding.inputPin.getText().toString();
            if(txt.length()>0)
                binding.inputPin.setText(txt.substring(0,txt.length()-1));
        }else{
            binding.inputPin.append(tv);
        }
    }
}