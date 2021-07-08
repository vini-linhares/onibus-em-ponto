package br.com.virtualartsa.onibusemponto;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Schiavetto on 22/08/2017.
 */

public class MeuOnibusListaRecyclerAdapter extends RecyclerView.Adapter<MeuOnibusListaViewHolder>{

    Context mctx;
    private List<MeuOnibusListaItem> mList;

    public MeuOnibusListaRecyclerAdapter(Context ctx, List<MeuOnibusListaItem> list) {
        this.mctx = ctx;
        this.mList = list;
    }

    @Override
    public MeuOnibusListaViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_lista_meu_onibus, viewGroup, false);
        return new MeuOnibusListaViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MeuOnibusListaViewHolder viewHolder, int position) {
        MeuOnibusListaItem onibus = mList.get(position);
        String distancia = onibus.getDistanciaTexto();
        String end = onibus.getEnd();

        viewHolder.txtDistancia.setText(distancia);
        viewHolder.txtEnd.setText(end);

        if(onibus.getAtualizando()){
            viewHolder.moLl.setBackgroundColor(Color.parseColor("#f0f0f0"));
            viewHolder.txtDistancia.setTextColor(Color.parseColor("#939393"));
            viewHolder.txtEnd.setTextColor(Color.parseColor("#939393"));
            viewHolder.imgBus.setImageResource(R.drawable.minibus2);
        }else{
            viewHolder.moLl.setBackgroundColor(Color.parseColor("#fafafa"));
            viewHolder.txtDistancia.setTextColor(Color.parseColor("#737373"));
            viewHolder.txtEnd.setTextColor(Color.parseColor("#737373"));
            viewHolder.imgBus.setImageResource(R.drawable.minibus);
        }
    }

    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }

    public void removerItem(int position) {
        mList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mList.size());
    }

}
