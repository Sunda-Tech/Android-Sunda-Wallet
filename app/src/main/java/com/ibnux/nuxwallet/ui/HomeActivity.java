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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.ibnux.nuxwallet.Aplikasi;
import com.ibnux.nuxwallet.Constants;
import com.ibnux.nuxwallet.R;
import com.ibnux.nuxwallet.adapter.DompetAdapter;
import com.ibnux.nuxwallet.data.Dompet;
import com.ibnux.nuxwallet.data.Dompet_;
import com.ibnux.nuxwallet.data.ObjectBox;
import com.ibnux.nuxwallet.data.Transaksi_;
import com.ibnux.nuxwallet.databinding.ActivityHomeBinding;
import com.ibnux.nuxwallet.layanan.BackgroundService;
import com.ibnux.nuxwallet.utils.NuxCoin;
import com.ibnux.nuxwallet.utils.Utils;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.scottyab.aescrypt.AESCrypt;

import java.io.File;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements
        View.OnClickListener, DompetAdapter.DompetCallback,
        TabLayout.OnTabSelectedListener, NavigationDrawerFragment.NavigationDrawerCallbacks {
    ActivityHomeBinding binding;
    DompetAdapter adapter;
    private Toolbar toolbar;
    private NavigationDrawerFragment mNavigationDrawerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.fabAddDompet.setOnClickListener(this);

        adapter = new DompetAdapter(this, true);

        binding.listDompet.setHasFixedSize(true);
        binding.listDompet.setLayoutManager(new LinearLayoutManager(this));
        binding.listDompet.setAdapter(adapter);
        binding.layoutCari.setVisibility(View.GONE);
        binding.txtServer.setOnClickListener(this);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(binding.listDompet);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // This will display an Up icon (<-), we will replace it with hamburger later
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);




        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));


        binding.tabLayout.addOnTabSelectedListener(this);
        int txtime = Aplikasi.sp.getInt("defaultTxTimeListener", Constants.defaultTxTimeListener);
        if(txtime>0) {
            startBackgroundServices();
        }
        new AppUpdater(this)
                .setDisplay(Display.NOTIFICATION)
                .setUpdateFrom(UpdateFrom.GITHUB)
                .setTitleOnUpdateAvailable(R.string.update_title)
                .setContentOnUpdateAvailable(R.string.update_message)
                .setGitHubUserAndRepo(getString(R.string.update_github_user), getString(R.string.update_github_repo))
                .init();

        binding.txtCari.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.searchData(binding.txtCari.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        startActivityForResult(new Intent(this, PinActivity.class), 4268);
    }

    public void startBackgroundServices(){
        if(!BackgroundService.isRunning()){
            Intent intent = new Intent(this,BackgroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Dexter.withContext(this)
                        .withPermission(Manifest.permission.FOREGROUND_SERVICE)
                        .withListener(new PermissionListener() {
                            @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                                Utils.log("startForegroundService Background services");
                                startForegroundService(intent);
                            }
                            @Override public void onPermissionDenied(PermissionDeniedResponse response) {
                                finish();
                            }
                            @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                            }
                        }).check();

            } else {
                startService(intent);
                Utils.log("startService Background services");
            }
        }else{
            Utils.log("Background is running");
        }
    }
    public void stopBackgroundServices(){
        if(BackgroundService.isRunning()){
            Intent intent = new Intent(this,BackgroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction("STOP");
                startService(intent);
            } else {
                stopService(intent);
                Utils.log("startService Background services");
            }
        }else{
            Utils.log("Background is running");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==4268){
            if(resultCode==RESULT_OK) {
                if (data.hasExtra("SUKSES")) {
                    Aplikasi.isLogin = true;
                } else {
                    finish();
                }
            }else {
                finish();
            }
        }else if(requestCode==4269){
            if(resultCode==RESULT_OK) {
                if (data.hasExtra("SUKSES")) {
                    Aplikasi.setPin(null);
                    startActivityForResult(new Intent(this, PinActivity.class), 4268);
                } else {
                    Utils.showToast(R.string.pin_not_change, this);
                }
            } else {
                Utils.showToast(R.string.pin_not_change, this);
            }

        }else if(requestCode==4270){
            if(resultCode==RESULT_OK) {
                if (data.hasExtra("SUKSES")) {
                    backupAll();
                } else {
                    Utils.showToast(R.string.pin_not_change, this);
                }
            } else {
                Utils.showToast(R.string.pin_not_change, this);
            }
        }else if(requestCode==4271){
            if(resultCode==RESULT_OK) {
                if (data.hasExtra("SUKSES")) {
                    restoreAll();
                } else {
                    Utils.showToast(R.string.pin_not_change, this);
                }
            } else {
                Utils.showToast(R.string.pin_not_change, this);
            }
        }

    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if(tab.getPosition() == 0){
            adapter.reload(true);
        }else{
            adapter.reload(false);
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onClick(View v) {
        if(binding.fabAddDompet==v){
            AddWalletFragment.newInstance().show(getSupportFragmentManager(),"AddWallet");
        }else if(binding.txtServer==v){
            startActivity(new Intent(this,PeersActivity.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(binding.txtServer!=null) binding.txtServer.setText(ObjectBox.getServer());
        if(adapter!=null){
            adapter.reload(binding.tabLayout.getSelectedTabPosition()==0);
        }
        if(Aplikasi.unixtime==0L){
            NuxCoin.getTime(null);
        }else{
            Utils.log(Aplikasi.unixtime);
        }
    }

    @Override
    public void onDompetClicked(Dompet dompet) {
        Intent intent = new Intent(this, ViewWalletActivity.class);
        intent.putExtra("alamat",dompet.alamat);
        startActivity(intent);
    }

    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN | ItemTouchHelper.UP) {

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            int position = viewHolder.getAdapterPosition();
            Dompet dompet = adapter.getData(position);
            String deskripsi = dompet.alamat+
                    ((dompet.nama!=null && !dompet.nama.equals(dompet.alamat))?"\n"+dompet.nama:"")+
                    ((dompet.catatan!=null)?"\n"+dompet.catatan:"");
            new AlertDialog.Builder(HomeActivity.this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setMessage(deskripsi)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            adapter.reload(binding.tabLayout.getSelectedTabPosition()==0);
                        }
                    })
                    .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            new AlertDialog.Builder(HomeActivity.this)
                                    .setIcon(R.mipmap.ic_launcher)
                                    .setTitle(R.string.ask_are_you_sure)
                                    .setMessage(deskripsi)
                                    .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ObjectBox.getTransaksi().query().equal(Transaksi_.senderRS,dompet.alamat).build().remove();
                                            ObjectBox.getTransaksi().query().equal(Transaksi_.recipientRS,dompet.alamat).build().remove();
                                            ObjectBox.getDompet().remove(dompet);
                                            adapter.reload(binding.tabLayout.getSelectedTabPosition()==0);
                                        }
                                    })
                                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            adapter.reload(binding.tabLayout.getSelectedTabPosition()==0);
                                        }
                                    })
                                    .show();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            adapter.reload(binding.tabLayout.getSelectedTabPosition()==0);
                        }
                    })
                    .show();
        }
    };

    @Override
    public void onNavigationDrawerItemSelected(int position) {

    }

    @Override
    public void onNavigationDrawerItemSelectedTitle(String title) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_nav_search:
                if(binding.layoutCari.getVisibility()==View.VISIBLE){
                    binding.tabLayout.setVisibility(View.VISIBLE);
                    binding.layoutCari.setVisibility(View.GONE);
                    item.setIcon(android.R.drawable.ic_menu_search);
                    adapter.reload();
                }else{
                    binding.tabLayout.setVisibility(View.GONE);
                    binding.layoutCari.setVisibility(View.VISIBLE);
                    binding.txtCari.setText("");
                    binding.txtCari.requestFocus();
                    item.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
                }
                return true;
            case R.id.menu_nav_changepin:
                startActivityForResult(new Intent(this,PinActivity.class), 4269);
                return true;
            case R.id.menu_nav_register:
                startActivity(new Intent(this,AirdropRequestActivity.class));
                return true;
            case R.id.menu_nav_airdrop:
                startActivity(new Intent(this,AirdropActivity.class));
                return true;
            case R.id.menu_nav_txListener:
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setTitle(R.string.dialog_tx_interval_title);
                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_PHONE|InputType.TYPE_CLASS_NUMBER);
                input.setGravity(Gravity.CENTER_HORIZONTAL);
                input.setHint(R.string.dialog_tx_interval_hint);
                builder.setView(input);
                input.setText(Aplikasi.sp.getInt("defaultTxTimeListener", Constants.defaultTxTimeListener)+"");
                input.setSelectAllOnFocus(true);
                builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String hasil = input.getText().toString();;
                        if(hasil.isEmpty()){
                            hasil = "0";
                        }
                        int interval = Integer.parseInt(hasil);
                        Aplikasi.sp.edit().putInt("defaultTxTimeListener",interval).apply();
                        if(interval>0){
                            stopBackgroundServices();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startBackgroundServices();
                                }
                            },2000);
                        }
                    }
                });
                builder.setNegativeButton(R.string.cancel,null);
                builder.show();
                return true;
            case R.id.menu_nav_backup:
                new AlertDialog.Builder(HomeActivity.this)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(R.string.dialog_backup_wallet_title)
                        .setMessage(R.string.dialog_backup_wallet_message)
                        .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Dexter.withContext(HomeActivity.this)
                                        .withPermissions(
                                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                                        ).withListener(new MultiplePermissionsListener() {
                                    @Override public void onPermissionsChecked(MultiplePermissionsReport report) {
                                        startActivityForResult(new Intent(HomeActivity.this,PinActivity.class), 4270);
                                    }
                                    @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
                                }).check();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();

                return true;
            case R.id.menu_nav_restore:
                new AlertDialog.Builder(HomeActivity.this)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(R.string.dialog_restore_wallet_title)
                        .setMessage(R.string.dialog_restore_wallet_message)
                        .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Dexter.withContext(HomeActivity.this)
                                        .withPermissions(
                                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                                        ).withListener(new MultiplePermissionsListener() {
                                    @Override public void onPermissionsChecked(MultiplePermissionsReport report) {
                                        startActivityForResult(new Intent(HomeActivity.this,PinActivity.class), 4271);
                                    }
                                    @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
                                }).check();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                return true;
            case R.id.menu_nav_update:
                new AppUpdater(this)
                        .setDisplay(Display.DIALOG)
                        .setUpdateFrom(UpdateFrom.GITHUB)
                        .setTitleOnUpdateAvailable(R.string.update_title)
                        .setContentOnUpdateAvailable(R.string.update_message)
                        .setGitHubUserAndRepo(getString(R.string.update_github_user), getString(R.string.update_github_repo))
                        .showAppUpdated(true)
                        .init();
                return true;
            case R.id.menu_nav_peers:
                startActivity(new Intent(this,PeersActivity.class));
                return true;
            case R.id.menu_nav_faq:
                startActivity(new Intent(this,IntroActivity.class));
                finish();
                return true;

            case R.id.menu_nav_share:
                /*Create an ACTION_SEND Intent*/
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                /*This will be the actual content you wish you share.*/
                String shareBody = "Here is the share content body";
                /*The type of the content is text, obviously.*/
                intent.setType("text/plain");
                /*Applying information Subject and Body.*/
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
                intent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.share_body));
                /*Fire!*/
                startActivity(Intent.createChooser(intent, getString(R.string.share_using)));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void backupAll(){
        String pin = Aplikasi.getPin();
        List<Dompet> dpts = ObjectBox.getDompet().query().equal(Dompet_.isMe,true).build().find();
        int jml = dpts.size();
        int done = 0;
        for(int n=0;n<jml;n++){
            done += (Utils.saveToFile(dpts.get(n),pin,this))?1:0;
        }
        dpts = ObjectBox.getDompet().query().equal(Dompet_.isMe,false).build().find();
        jml = dpts.size();
        for(int n=0;n<jml;n++){
            done += (Utils.saveToFile(dpts.get(n),pin,this))?1:0;
        }
        Utils.showToast(getString(R.string.backup_finished,done, Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS).toString() + File.separator + Constants.folderName),this);
    }

    public void restoreAll(){
        String pin = Aplikasi.getPin();
        String foldernux = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS).toString() + File.separator + Constants.folderName;
        File file = new File(foldernux);
        if (!file.exists()) {
            Utils.showToast(getString(R.string.folder_not_found,foldernux),this);
            Utils.vibrate();
        }else{
            for (String fil: file.list()){
                try {
                    if(fil.endsWith("."+Constants.currency.toLowerCase())) {
                        String hasil = Utils.getStringFromFile(new File(foldernux, fil));
                        try {
                            Utils.log(hasil);
                            hasil = AESCrypt.decrypt(pin, hasil);
                            Utils.log(hasil);
                            Dompet dompet = new Gson().fromJson(hasil, Dompet.class);
                            dompet.id = 0L;
                            Utils.log(dompet.alamat);
                            Utils.log(ObjectBox.addDompet(dompet)+"");;
                        } catch (Exception e) {
                            Toast.makeText(this, getString(R.string.failed_import_file, e.getMessage()), Toast.LENGTH_SHORT).show();
                        }
                    }
                }catch (Exception e){
                    Utils.showToast(getString(R.string.failed_import,foldernux),this);
                    Utils.vibrate();
                }
            }
            Utils.showToast(R.string.success_import_wallet,this);
            adapter.reload(binding.tabLayout.getSelectedTabPosition()==0);
        }
    }


}