package com.ibnux.nuxwallet.adapter;

/******************************************************************************\
 * GNU GENERAL PUBLIC LICENSE                                                 *
 * Version 3, 29 June 2007                                                    *
 * Ibnu Maksum https://github.com/ibnux/                                      *
 ******************************************************************************
 * This source and program come as is, WITHOUT ANY WARRANTY and/or WITHOUT    *
 * ANY IMPLIED WARRANTY.                                                      *
 \******************************************************************************/

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ibnux.nuxwallet.R;
import com.ibnux.nuxwallet.data.Dompet;
import com.ibnux.nuxwallet.data.Dompet_;
import com.ibnux.nuxwallet.data.ObjectBox;
import com.ibnux.nuxwallet.utils.Utils;

import java.util.List;

public class DompetSpinnerAdapter extends ArrayAdapter {
    private List<Dompet> datas;
    Context context;
    public static class ViewHolder {
        TextView txtWallet,txtBalance,txtWalletName,txtWalletNote;
        LinearLayout layout;
    }

    public DompetSpinnerAdapter(Context context, int resources){
        super(context, resources);
        this.context = context;
        reload();
    }

    public void reload(){
        datas = ObjectBox.getDompet().query().equal(Dompet_.isMe,true).orderDesc(Dompet_.saldo).build().find();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return prosesView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return prosesView(position, convertView, parent);
    }

    private  View prosesView(int position, @Nullable View view, @NonNull ViewGroup parent){
        ViewHolder holder;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_card_small, parent, false);
            holder = new ViewHolder();
            holder.txtWallet = view.findViewById(R.id.txtWallet);
            holder.txtBalance = view.findViewById(R.id.txtBalance);
            holder.txtWalletName = view.findViewById(R.id.txtWalletName);
            holder.txtWalletNote = view.findViewById(R.id.txtWalletNote);
            holder.layout = view.findViewById(R.id.layout);
            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }

        Dompet dompet = datas.get(position);
        holder.txtBalance.setText(Utils.nuxFormat(dompet.saldo));
        holder.txtWallet.setText(dompet.alamat);
        if(dompet.alamat.equals(dompet.nama)) {
            holder.txtWalletName.setVisibility(View.GONE);
        }else{
            holder.txtWalletName.setVisibility(View.VISIBLE);
            if(dompet.nama!=null)
                holder.txtWalletName.setText(dompet.nama);
            else
                holder.txtWalletName.setText("");
        }
        if(dompet.catatan!=null)
            holder.txtWalletNote.setText(dompet.catatan);
        else
            holder.txtWalletNote.setText("");

        return view;
    }

    public int getPosition(@Nullable String alamat) {
        int jml = getCount();
        for(int n=0;n<jml;n++){
            Dompet d = datas.get(n);
            if(d.alamat.equals(alamat)){
                return n;
            }
        }
        return 0;
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    public Dompet getItem(int position) {
        return datas.get(position);
    }
}
