package com.ibnux.nuxwallet.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;
import com.ibnux.nuxwallet.Aplikasi;
import com.ibnux.nuxwallet.R;
import com.ibnux.nuxwallet.data.ObjectBox;
import com.ibnux.nuxwallet.data.Transaksi;
import com.ibnux.nuxwallet.data.Transaksi_;
import com.ibnux.nuxwallet.utils.JsonCallback;
import com.ibnux.nuxwallet.utils.NuxCoin;
import com.ibnux.nuxwallet.utils.Utils;
import org.json.JSONObject;

import java.util.Iterator;


public class ViewTransactionFragment extends BottomSheetDialogFragment {
    String transaction;
    TableLayout tabel;
    LayoutInflater inflater;
    ViewGroup container;

    public static ViewTransactionFragment newInstance(String transaction){
        ViewTransactionFragment vtf = new ViewTransactionFragment();
        Bundle args = new Bundle();
        args.putString("transaction", transaction);
        vtf.setArguments(args);
        return vtf;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            transaction = getArguments().getString("transaction");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.inflater = inflater;
        this.container = container;
        View view = inflater.inflate(R.layout.fragment_view_transaction, container, false);
        tabel = view.findViewById(R.id.tabel);
        showTX();
        getupdate();
        return view;
    }

    public void showTX(){
        tabel.removeAllViews();
        Gson gson = new Gson();
        Transaksi ts = ObjectBox.getTransaksi().query().equal(Transaksi_.transaction,transaction).build().findFirst();
        String jsontxt = gson.toJson(ts);
        try {
            JSONObject json = new JSONObject(jsontxt);
            json.remove("id");
            Iterator<String> i = json.keys();
            while (i.hasNext()){
                String key = i.next();
                View item = inflater.inflate(R.layout.item_tx, container, false);
                ((TextView)item.findViewById(R.id.txtKey)).setText(key);
                if(key.equals("amountNQT") || key.equals("feeNQT")) {
                    ((TextView) item.findViewById(R.id.txtValue)).setText(Utils.nuxFormat(json.getLong(key)));
                }else if(key.equals("blockTimestamp") || key.equals("timestamp")){
                    long time = Aplikasi.unixtime + (json.getLong(key)*1000L);
                    ((TextView) item.findViewById(R.id.txtValue)).setText(Utils.toDate(time,"all"));
                }else if(key.equals("timestampInsert")){
                    ((TextView) item.findViewById(R.id.txtValue)).setText(Utils.toDate(json.getLong(key),"all"));
                }else
                    ((TextView)item.findViewById(R.id.txtValue)).setText(json.getString(key));
                tabel.addView(item);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void getupdate(){
        NuxCoin.getTransaction(transaction, new JsonCallback() {
            @Override
            public void onJsonCallback(JSONObject response) {
                try {
                    Transaksi tx = new Gson().fromJson(response.toString(), Transaksi.class);
                    tx.timestampInsert = System.currentTimeMillis();
                    tx.isRead = true;
                    if (response.has("attachment")) {
                        if (response.getJSONObject("attachment").has("message")) {
                            tx.message = response.getJSONObject("attachment").getString("message");
                        }
                    }
                    ObjectBox.addTransaksi(tx);
                    showTX();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onErrorCallback(int errorCode, String errorMessage) {

            }
        });
    }
}
