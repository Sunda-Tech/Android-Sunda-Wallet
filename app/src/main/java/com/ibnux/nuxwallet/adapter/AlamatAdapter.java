package com.ibnux.nuxwallet.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.ibnux.nuxwallet.data.Dompet;
import com.ibnux.nuxwallet.data.Dompet_;
import com.ibnux.nuxwallet.data.ObjectBox;
import com.ibnux.nuxwallet.utils.Utils;

import java.util.List;

public class AlamatAdapter extends ArrayAdapter<Dompet> {
    List<Dompet> datas;
    Context context;
    public AlamatAdapter(Context context){
        super(context, android.R.layout.select_dialog_item);
        Utils.log("AlamatAdapter");
        this.context = context;
        datas = ObjectBox.getDompet().query().equal(Dompet_.isMe,false).build().find();
        addAll(datas);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            view = inflater.inflate(android.R.layout.select_dialog_item, parent, false);
            super.getView(position, convertView, parent);
        }
        Dompet dompet = getItem(position);
        ((TextView)view.findViewById(android.R.id.text1)).setText((dompet.alamat.equals(dompet.nama))?dompet.alamat:dompet.nama+" "+dompet.alamat);

        return view;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return dompetFilter;
    }

    private Filter dompetFilter = new Filter() {

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            Dompet dompet = (Dompet) resultValue;
            return dompet.alamat;
        }

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            if (charSequence != null) {
                clear();
                Utils.log("FilterResults: "+charSequence.toString().toLowerCase());
                for (Dompet dompet : datas) {
                    if (dompet.nama.toLowerCase().startsWith(charSequence.toString().toLowerCase())) {
                        Utils.log("FilterResults dompet: "+dompet.nama.toLowerCase());
                        add(dompet);
                    } else if (dompet.alamat.toLowerCase().startsWith(charSequence.toString().toLowerCase())) {
                        add(dompet);
                        Utils.log("FilterResults dompet: "+dompet.alamat.toLowerCase());
                    }
                }
                return new FilterResults();
            }else{
                addAll(datas);
                return new FilterResults();
            }
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            notifyDataSetChanged();
        }
    };
}
