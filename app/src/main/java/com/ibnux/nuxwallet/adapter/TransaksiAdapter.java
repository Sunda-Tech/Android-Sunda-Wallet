package com.ibnux.nuxwallet.adapter;

/******************************************************************************\
 * GNU GENERAL PUBLIC LICENSE                                                 *
 * Version 3, 29 June 2007                                                    *
 * Ibnu Maksum https://github.com/ibnux/                                      *
 ******************************************************************************
 * This source and program come as is, WITHOUT ANY WARRANTY and/or WITHOUT    *
 * ANY IMPLIED WARRANTY.                                                      *
 \******************************************************************************/

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ibnux.nuxwallet.Aplikasi;
import com.ibnux.nuxwallet.R;
import com.ibnux.nuxwallet.data.ObjectBox;
import com.ibnux.nuxwallet.data.Transaksi;
import com.ibnux.nuxwallet.data.Transaksi_;
import com.ibnux.nuxwallet.utils.Utils;

import java.util.List;

public class TransaksiAdapter extends RecyclerView.Adapter<TransaksiAdapter.MyViewHolder> {
    TransaksiCallback callback;
    String alamat;
    private List<Transaksi> datas;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtWallet,txtBalance,txtTgl,txtCatatan,txtThn,txtJam;
        LinearLayout layout, layoutStatus;
        CardView cardTgl,cardTransaction;
        public MyViewHolder(View v) {
            super(v);
            txtWallet = v.findViewById(R.id.txtWallet);
            txtBalance = v.findViewById(R.id.txtBalance);
            txtTgl = v.findViewById(R.id.txtTgl);
            txtThn = v.findViewById(R.id.txtThn);
            txtJam = v.findViewById(R.id.txtJam);
            cardTgl = v.findViewById(R.id.cardTgl);
            txtCatatan = v.findViewById(R.id.txtCatatan);
            cardTransaction = v.findViewById(R.id.cardTransaction);
            layout = v.findViewById(R.id.layout);
            layoutStatus = v.findViewById(R.id.layoutStatus);
        }
    }

    public TransaksiAdapter(TransaksiCallback callback, String alamat){
        this.callback = callback;
        this.alamat = alamat;
        reload();
    }

    public void reload(){
        datas = ObjectBox.getTransaksi().query()
                .equal(Transaksi_.recipientRS,alamat)
                .or()
                .equal(Transaksi_.senderRS,alamat)
                .orderDesc(Transaksi_.timestamp).build().find();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TransaksiAdapter.MyViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Transaksi tx = datas.get(position);

        if(tx.isRead){
            holder.cardTransaction.setCardBackgroundColor(ContextCompat.getColor(Aplikasi.app,R.color.grey_5));
        }else{
            holder.cardTransaction.setCardBackgroundColor(ContextCompat.getColor(Aplikasi.app,R.color.amber_100));
        }

        if(tx.recipientRS.equals(alamat)){
            holder.layoutStatus.setBackgroundColor(ContextCompat.getColor(Aplikasi.app,R.color.green_A400));
            holder.txtBalance.setText("+ "+Utils.nuxFormat(Long.parseLong(tx.amountNQT)));
            holder.txtBalance.setTextColor(ContextCompat.getColor(Aplikasi.app,R.color.green_A700));
            holder.txtWallet.setText(ObjectBox.getNamaDompet(tx.senderRS).toUpperCase());
            holder.txtWallet.setTag(tx.senderRS);
        }else {
            holder.txtBalance.setText("- "+Utils.nuxFormat(Long.parseLong(tx.amountNQT)));
            holder.txtBalance.setTextColor(ContextCompat.getColor(Aplikasi.app,R.color.red_400));
            holder.layoutStatus.setBackgroundColor(ContextCompat.getColor(Aplikasi.app,R.color.red_500));
            holder.txtWallet.setText(ObjectBox.getNamaDompet(tx.recipientRS).toUpperCase());
            holder.txtWallet.setTag(tx.recipientRS);
        }
        holder.txtWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(callback!=null)
                    callback.onTransaksiWalletClicked(holder.txtWallet.getTag().toString());
            }
        });
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(callback!=null)
                    callback.onTransaksiClicked(tx);
            }
        });
        if(Aplikasi.unixtime!=0L) {
            holder.cardTgl.setVisibility(View.VISIBLE);
            Utils.log("UnixTime "+Aplikasi.unixtime);
            long time = Aplikasi.unixtime + (tx.timestamp*1000);
            Utils.log("BlockTime "+time);
            holder.txtTgl.setText(Utils.toDate(time, "d"));
            holder.txtThn.setText(Utils.toDate(time, "m")+"/"+Utils.toDate(time, "y"));
            holder.txtJam.setText(Utils.toDate(time, "H")+":"+Utils.toDate(time, "m"));
        }else{
            holder.cardTgl.setVisibility(View.GONE);
        }
        if(tx.message!=null){
            holder.txtCatatan.setVisibility(View.VISIBLE);
            holder.txtCatatan.setText(tx.message);
        }else{
            holder.txtCatatan.setVisibility(View.GONE);
        }
    }

    public Transaksi getData(int pos){
        return datas.get(pos);
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public interface TransaksiCallback {
        void onTransaksiClicked(Transaksi transaksi);
        void onTransaksiWalletClicked(String alamat);
    }
}
