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
import com.ibnux.nuxwallet.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class TransaksiAirdropAdapter extends RecyclerView.Adapter<TransaksiAirdropAdapter.MyViewHolder> {
    String alamat;
    private List<Transaksi> datas = new ArrayList<>();

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

    public TransaksiAirdropAdapter(){
    }

    public void addTX(Transaksi tx){
        datas.add(tx);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TransaksiAirdropAdapter.MyViewHolder(LayoutInflater.from(parent.getContext())
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
        if(Aplikasi.unixtime!=0L) {
            holder.txtTgl.setText(Utils.toDate(tx.timestamp, "d"));
            holder.txtThn.setText(Utils.toDate(tx.timestamp, "m")+"/"+Utils.toDate(tx.timestamp, "y"));
            holder.txtJam.setText(Utils.toDate(tx.timestamp, "H")+":"+Utils.toDate(tx.timestamp, "m"));
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

    @Override
    public int getItemCount() {
        return datas.size();
    }

}
