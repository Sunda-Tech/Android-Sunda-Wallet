package com.ibnux.nuxwallet.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.androidnetworking.widget.ANImageView;
import com.ibnux.nuxwallet.R;
import com.ibnux.nuxwallet.data.Navigasi;
import com.ibnux.nuxwallet.utils.NavCallback;
import com.ibnux.nuxwallet.utils.Utils;

import java.util.List;

public class NavigasiAdapter extends RecyclerView.Adapter<NavigasiAdapter.MyViewHolder> {
    List<Navigasi> datas;
    NavCallback callback;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NavigasiAdapter.MyViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_navigasi, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Navigasi nav = datas.get(position);
        Utils.log("Nav "+nav.url);
        holder.imgIkon.setErrorImageResId(R.drawable.ic_launcher);
        holder.imgIkon.setDefaultImageResId(R.drawable.ic_launcher);
        if(nav.icon!=null && nav.icon.startsWith("https")){
            holder.imgIkon.setImageUrl(nav.icon);
        }else{
            holder.imgIkon.setImageResource(R.drawable.ic_launcher);
        }
        holder.txtTitle.setText(nav.title);
        if(!nav.description.isEmpty()) {
            holder.txtDescription.setText(nav.description);
            holder.txtDescription.setVisibility(View.VISIBLE);
        }else{
            holder.txtDescription.setVisibility(View.GONE);
        }
        holder.layoutNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(callback!=null){
                    callback.onNavCallback(nav);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if(datas==null) return 0;
        return datas.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle,txtDescription;
        ANImageView imgIkon;
        LinearLayout layoutNav;
        public MyViewHolder(View v) {
            super(v);
            txtTitle = v.findViewById(R.id.txtTitle);
            txtDescription = v.findViewById(R.id.txtDescription);
            imgIkon = v.findViewById(R.id.imgIkon);
            layoutNav = v.findViewById(R.id.layoutNav);
        }
    }

    public NavigasiAdapter(List<Navigasi> datas, NavCallback callback) {
        Utils.log("nav "+datas.size()+" masuk");
        this.callback = callback;
        this.datas = datas;
        notifyDataSetChanged();
    }

}
