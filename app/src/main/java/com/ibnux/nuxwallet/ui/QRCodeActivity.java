package com.ibnux.nuxwallet.ui;

/******************************************************************************\
 * GNU GENERAL PUBLIC LICENSE                                                 *
 * Version 3, 29 June 2007                                                    *
 * Ibnu Maksum https://github.com/ibnux/                                      *
 ******************************************************************************
 * This source and program come as is, WITHOUT ANY WARRANTY and/or WITHOUT    *
 * ANY IMPLIED WARRANTY.                                                      *
 \******************************************************************************/

import android.Manifest;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.github.sumimakito.awesomeqr.AwesomeQrRenderer;
import com.github.sumimakito.awesomeqr.option.RenderOption;
import com.github.sumimakito.awesomeqr.option.color.Color;
import com.github.sumimakito.awesomeqr.option.logo.Logo;
import com.ibnux.nuxwallet.Aplikasi;
import com.ibnux.nuxwallet.Constants;
import com.ibnux.nuxwallet.R;
import com.ibnux.nuxwallet.data.Dompet;
import com.ibnux.nuxwallet.data.ObjectBox;
import com.ibnux.nuxwallet.databinding.ActivityQrCodeBinding;
import com.ibnux.nuxwallet.utils.Utils;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.scottyab.aescrypt.AESCrypt;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class QRCodeActivity extends AppCompatActivity implements View.OnClickListener{
    ActivityQrCodeBinding binding;
    String folderName = Constants.folderName;
    Dompet dompet;
    String alamat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQrCodeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(R.string.qrcode);
        Intent i = getIntent();
        if(!i.hasExtra("alamat")){
            finish();
        }
        alamat = i.getStringExtra("alamat");
        dompet = ObjectBox.getDompet(alamat);
        binding.txtAlamat.setText(alamat);
        createQR(alamat);

        binding.btnShareAddress.setOnClickListener(this);
        binding.btnShareImage.setOnClickListener(this);
        binding.btnAlamat.setOnClickListener(this);
        binding.btnPrivateKey.setOnClickListener(this);
        binding.btnPrivateKeyEncrypted.setOnClickListener(this);
        binding.btnPublicKey.setOnClickListener(this);
        binding.btnAlamatPublickey.setOnClickListener(this);
        binding.btnSaveImage.setOnClickListener(this);
        binding.btnCopyPublicKey.setOnClickListener(this);
        binding.btnCopyPrivateKey.setOnClickListener(this);
        if(dompet==null || dompet.secretPhrase.isEmpty()){
            binding.layoutTombol.setVisibility(View.GONE);
        }

        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
            @Override public void onPermissionsChecked(MultiplePermissionsReport report) {/* ... */}
            @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
        }).check();
    }

    @Override
    public void onClick(View v) {
        if(v==binding.btnShareAddress){
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(Constants.folderName, alamat);
            clipboard.setPrimaryClip(clip);
            Utils.showToast(getString(R.string.a_copied,alamat),this);
        }else if(v==binding.btnSaveImage){
            try {
                Utils.showToast(getString(R.string.qrcode_saved,saveBitMap().getPath()), this);
            }catch (Exception e){
                e.printStackTrace();
                Utils.showToast(R.string.qrcode_failed_saved, this);
            }
        }else if(v==binding.btnShareImage){
            shareFile(saveBitMap());
        }else if(v==binding.btnAlamatPublickey){
            binding.txtAlamat.setText(getString(R.string.a_public_key_with_address,alamat));
            try{
                JSONObject json = new JSONObject();
                json.put("address",dompet.alamat);
                json.put("public_key",dompet.publicKey);
                createQR(json.toString());
            }catch (Exception e){
                e.printStackTrace();
                createQR("APK:"+dompet.alamat+"APKAPKAPK"+dompet.publicKey);
            }
        }else if(v==binding.btnPublicKey){
            if(dompet!=null) {
                createQR(dompet.publicKey);
                binding.txtAlamat.setText(getString(R.string.a_public_key, alamat));
            }
        }else if(v==binding.btnPrivateKey){
            new AlertDialog.Builder(QRCodeActivity.this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.dialog_warning_title)
                    .setMessage(R.string.dialog_warning_body)
                    .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivityForResult(new Intent(QRCodeActivity.this,PinActivity.class), 4268);
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }else if(v==binding.btnPrivateKeyEncrypted){
            new AlertDialog.Builder(QRCodeActivity.this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.dialog_warning_title)
                    .setMessage(R.string.dialog_warning_body)
                    .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivityForResult(new Intent(QRCodeActivity.this,PinActivity.class), 4269);
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();

        }else if(v==binding.btnCopyPrivateKey){
            new AlertDialog.Builder(QRCodeActivity.this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.dialog_warning_title)
                    .setMessage(R.string.dialog_warning_body)
                    .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivityForResult(new Intent(QRCodeActivity.this,PinActivity.class), 4270);
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }else if(v==binding.btnCopyPublicKey){
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(Constants.folderName, dompet.publicKey);
            clipboard.setPrimaryClip(clip);
            Utils.showToast(getString(R.string.a_copied,getString(R.string.public_key)),this);
        }else if(v==binding.btnAlamat){
            createQR(alamat);
            binding.txtAlamat.setText(alamat);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==4268){
            if(resultCode==RESULT_OK) {
                //btnPrivateKey
                if (data.hasExtra("SUKSES")) {
                    if(dompet!=null) {
                        createQR(dompet.secretPhrase);
                        binding.txtAlamat.setText(getString(R.string.a_secret_passphrase,alamat));
                    }
                }
            }
        }else if(requestCode==4269){
            //btnPrivateKeyEncrypted
            if(resultCode==RESULT_OK) {
                if (data.hasExtra("SUKSES")) {
                    if(dompet!=null) {
                        try {
                            String result = AESCrypt.encrypt(Aplikasi.getPin(), dompet.secretPhrase);
                            createQR("SECRET:"+result);
                            binding.txtAlamat.setText(getString(R.string.a_encrypted_secret_assphrase,alamat));
                        }catch (Exception e){
                            Toast.makeText(this, getString(R.string.a_failed_encrypted_secret_assphrase,e.getMessage()), Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            }
        }else if(requestCode==4270){
            if (data.hasExtra("SUKSES")) {
                if(dompet!=null) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(Constants.folderName, dompet.secretPhrase);
                    clipboard.setPrimaryClip(clip);
                    Utils.showToast(getString(R.string.a_copied,getString(R.string.passphrase)),QRCodeActivity.this);
                }
            }
        }
    }

    public void createQR(String alamat){
        RenderOption renderOption = new RenderOption();
        renderOption.setContent(alamat); // content to encode
        renderOption.setSize(800); // size of the final QR code image
        renderOption.setBorderWidth(20); // width of the empty space around the QR code
        renderOption.setPatternScale(0.5f); // (optional) specify a scale for patterns
        renderOption.setRoundedPatterns(true); // (optional) if true, blocks will be drawn as dots instead
        renderOption.setClearBorder(true); // if set to true, the background will NOT be drawn on the border area
        Color color = new Color();
        color.setLight(0xB3FFFFFF); // for blank spaces
        color.setDark(0xFF000000);
        color.setBackground(0xFFFFFFFF); // for the background (will be overriden by background images, if set)
        color.setAuto(false);
        renderOption.setColor(color); // set a color palette for the QR code
        Logo logo = new Logo();
        logo.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.nux_logo));
        renderOption.setLogo(logo);
        try {
            binding.imgBarcode.setImageBitmap(AwesomeQrRenderer.render(renderOption).getBitmap());
        } catch (Exception e) {
//
        }
    }


    // https://stackoverflow.com/questions/17985646/android-sharing-files-by-sending-them-via-email-or-other-apps
    private void shareFile(Uri file) {
        try {
            Intent intentShareFile = new Intent(Intent.ACTION_SEND);
            intentShareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intentShareFile.putExtra(Intent.EXTRA_TEXT, alamat);
            intentShareFile.setDataAndType(file,"image/*");
            intentShareFile.putExtra(Intent.EXTRA_STREAM,file);
            startActivity(Intent.createChooser(intentShareFile, getString(R.string.share_wallet)));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.share_wallet_failed,e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    // https://stackoverflow.com/questions/10374547/how-to-convert-a-linearlayout-to-image
    private Uri saveBitMap() {

        String filename = binding.txtAlamat.getText().toString()
                .replace("\n","_")
                .replace(" ","_")
                .replace("-","_") + ".jpg";
        OutputStream fos;
        Uri imageUri = null;
        try {
            String imagesDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).toString() + File.separator + folderName;
            File file = new File(imagesDir);
            if (!file.exists()) {
                file.mkdir();
            }
            File img = new File(imagesDir, filename);
            imageUri = Uri.fromFile(img);
            if(img.exists()){
                img.delete();
            }
            fos = new FileOutputStream(img);
            Log.i("TAG", imageUri.getPath());
            Bitmap bitmap = getBitmapFromView(binding.layoutQR);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            scanGallery(imageUri.getPath());
        } catch (IOException e) {
            e.printStackTrace();
            imageUri = null;
            e.printStackTrace();
            Toast.makeText(this, R.string.failed_save_image, Toast.LENGTH_SHORT).show();
            Log.i("TAG", "There was an issue saving the image.");
        }

        return imageUri;
    }

    //create bitmap from view and returns it
    private Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        } else {
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(android.graphics.Color.WHITE);
        }
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }

    // used for scanning gallery
    private void scanGallery(String filename) {
        if (Build.VERSION.SDK_INT < 19) {
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://" + filename)));
        }
        else {
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.parse("file://" + filename)));
        }
    }

}