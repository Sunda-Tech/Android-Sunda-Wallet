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
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.ibnux.nuxwallet.Constants;
import com.ibnux.nuxwallet.R;
import com.ibnux.nuxwallet.data.ObjectBox;
import com.ibnux.nuxwallet.data.Server;
import com.ibnux.nuxwallet.data.Server_;
import com.ibnux.nuxwallet.utils.Utils;

import java.util.List;

public class ServerAdapter extends RecyclerView.Adapter<ServerAdapter.MyViewHolder> {
    ServerCallback callback;
    private List<Server> datas;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtServer;
        CardView cardServer;
        public MyViewHolder(View v) {
            super(v);
            txtServer = v.findViewById(R.id.txtServer);
            cardServer = v.findViewById(R.id.cardServer);
        }
    }

    public ServerAdapter(ServerCallback callback){
        this.callback = callback;
        reload();
    }

    public void reload(){
        datas = ObjectBox.getBoxServer().query().order(Server_.url).build().find();
        if(datas==null || (datas!=null && datas.size()==0)){
            Utils.log("DatasNull");
            ObjectBox.addServer(Constants.defaultServer);
            reload();
        }else
            notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ServerAdapter.MyViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_peer, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Server server = datas.get(position);
        Utils.log("Server "+server.url);
        holder.txtServer.setText(server.url+"");
        holder.cardServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(callback!=null)
                    callback.onServerClicked(server);
            }
        });
    }

    public Server getData(int pos){
        return datas.get(pos);
    }

    @Override
    public int getItemCount() {
        return (datas!=null)?datas.size():0;
    }

    public interface ServerCallback {
        void onServerClicked(Server server);
    }


}
