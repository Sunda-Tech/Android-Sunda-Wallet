package com.ibnux.nuxwallet.layanan;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANResponse;
import com.androidnetworking.common.Priority;
import com.google.gson.Gson;
import com.ibnux.nuxwallet.Aplikasi;
import com.ibnux.nuxwallet.Constants;
import com.ibnux.nuxwallet.R;
import com.ibnux.nuxwallet.data.Dompet;
import com.ibnux.nuxwallet.data.Dompet_;
import com.ibnux.nuxwallet.data.ObjectBox;
import com.ibnux.nuxwallet.data.Transaksi;
import com.ibnux.nuxwallet.data.Transaksi_;
import com.ibnux.nuxwallet.ui.HomeActivity;
import com.ibnux.nuxwallet.ui.SendMoneyActivity;
import com.ibnux.nuxwallet.ui.ViewWalletActivity;
import com.ibnux.nuxwallet.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class BackgroundService extends Service {
    NotificationManager notificationManager;
    private static boolean isRunning;

    int pos = 0;

    private void handleStart(Intent intent, int startId) {
        Utils.log("Background services initialized");
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        if (notificationManager == null)
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        int txtime = Aplikasi.sp.getInt("defaultTxTimeListener", Constants.defaultTxTimeListener);
//        if(txtime==0){
//            return;
//        }
        sendNotification("Next update at "+nextUpdate(),
                "Waiting transaction every "+txtime+" minute(s)");
        startCheck();
    }

    public String nextUpdate(){
        long waktu = System.currentTimeMillis() + (Aplikasi.sp.getInt("defaultTxTimeListener", Constants.defaultTxTimeListener)*60*1000);
        return Utils.toTime(waktu);
    }

    public void startCheck(){
        Utils.log("BackgroundService startCheck");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getAddress();
            }
        },Aplikasi.sp.getInt("defaultTxTimeListener", Constants.defaultTxTimeListener)*60*1000);
    }

    public void getAddress(){
        if(Aplikasi.sp.getInt("defaultTxTimeListener", Constants.defaultTxTimeListener)==0){
            stopNotification();
            stopSelf();
            return;
        }
        Utils.log("BackgroundService getAddress");
        List<Dompet> datas = ObjectBox.getDompet().query().equal(Dompet_.isMe,true).build().find();
        for (Dompet data: datas){
            pos = 0;
            checkTransactions(data.alamat);
        }
        sendNotification("Next update at "+nextUpdate(),
                "Waiting transaction every "+Aplikasi.sp.getInt("defaultTxTimeListener", Constants.defaultTxTimeListener)+" minute(s)");
        startCheck();
    }

    public void checkTransactions(String alamat){
        sendNotification(null, "Checking "+alamat);
        Utils.log("BackgroundService checkTransactions "+pos+" "+Constants.limitGetTX+" "+alamat);
        String server = ObjectBox.getServer();
        ANResponse response = AndroidNetworking.get(server+"/nxt")
                .addQueryParameter("requestType","getBlockchainTransactions")
                .addQueryParameter("account",alamat)
                .addQueryParameter("firstIndex",pos+"")
                .addQueryParameter("lastIndex",Constants.limitGetTX+"")
                .setPriority(Priority.LOW)
                .build()
                .executeForString();
        if(response.isSuccess()){
            try {
                JSONObject jsonObject = new JSONObject(response.getResult().toString());
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
                            sendNotification(alamat, "Checking "+tx.transaction);
                            if(ObjectBox.getTransaksi().query().equal(Transaksi_.transaction,tx.transaction).build().findFirst()==null) {
                                tx.timestampInsert = System.currentTimeMillis();
                                if (json.has("attachment")) {
                                    if (json.getJSONObject("attachment").has("message")) {
                                        tx.message = json.getJSONObject("attachment").getString("message");
                                    }
                                }
                                Intent intent = new Intent(getApplicationContext(), ViewWalletActivity.class);
                                intent.putExtra("alamat",alamat);
                                intent.putExtra("transaction",tx.transaction);
                                if((tx.recipientRS.equals(alamat))) {
                                    Utils.sendNotification(
                                            ObjectBox.getNamaDompet(tx.senderRS) + " ngirim artos",
                                            "Meunang " + tx.amountNQT + " SND ti si " + ObjectBox.getNamaDompet(tx.recipientRS),
                                            intent,
                                            "transaction",
                                            "Transaction"

                                    );
                                }else{
                                    Utils.sendNotification(
                                            "Ngirim "+ObjectBox.getNamaDompet(tx.senderRS),
                                             tx.amountNQT + " SND ka " + ObjectBox.getNamaDompet(tx.recipientRS)+ " tos dikirim",
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
                            checkTransactions(alamat);
                        }else{
                            Utils.log("Sudah tidak ada data baru");
                        }
                    }else{
                        Utils.log("transactions empty");
                    }
                }else{
                    Utils.log("No transactions");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            Utils.log("ERROR "+response.getError().getErrorDetail());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

    public static boolean isRunning() {
        return isRunning;
    }

    public BackgroundService() {
        Utils.log("BackgroundService instance");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Utils.log("onStartCommand");
        if (intent.getAction()!=null && intent.getAction().equals("STOP")) {
            stopNotification();
            stopSelf();
            return START_NOT_STICKY;
        }else {
            if (Aplikasi.sp.getInt("defaultTxTimeListener", Constants.defaultTxTimeListener) != 0) {
                isRunning = true;
                handleStart(intent, startId);
                return START_NOT_STICKY;
            } else {
                stopSelf();
                return START_NOT_STICKY;
            }
        }
    }

    public void stopNotification(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        }else
            notificationManager.cancel(42689);
    }

    public void sendNotification(String title, String description){
        Utils.log("BGServicesSendNotification");
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(Aplikasi.app);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("TxListener", "Transaction Listener", importance);
            notificationManager.createNotificationChannel(channel);
        }
        Intent intent = new Intent(this, SendMoneyActivity.class);
        intent.putExtra("askpin",true);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(Aplikasi.app, "TxListener")
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(BitmapFactory.decodeResource(Aplikasi.app.getResources(),R.mipmap.ic_launcher))
                .setContentTitle((title==null)?Aplikasi.app.getString(R.string.app_name):title)
                .setContentText((description==null)?"Waiting for incoming payment":description)
                .setOngoing(true)
                .setContentIntent(PendingIntent.getActivity(this,0,
                        new Intent(this, HomeActivity.class),PendingIntent.FLAG_CANCEL_CURRENT))
                .addAction(R.drawable.ic_send,"Ngirim Artos",
                        PendingIntent.getActivity(this,0, intent
                                ,PendingIntent.FLAG_CANCEL_CURRENT))
                .setPriority(NotificationCompat.PRIORITY_LOW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(42689,builder.build());
        }else {
            notificationManager.notify(42689, builder.build());
        }
    }

}
