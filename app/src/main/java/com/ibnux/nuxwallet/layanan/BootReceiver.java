package com.ibnux.nuxwallet.layanan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.ibnux.nuxwallet.Aplikasi;
import com.ibnux.nuxwallet.Constants;
import com.ibnux.nuxwallet.utils.Utils;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intentStart) {
        int txtime = Aplikasi.sp.getInt("defaultTxTimeListener", Constants.defaultTxTimeListener);
        if(txtime==0){
            return;
        }

        Intent intent = new Intent(context,BackgroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
        Utils.log("started");
    }
}
