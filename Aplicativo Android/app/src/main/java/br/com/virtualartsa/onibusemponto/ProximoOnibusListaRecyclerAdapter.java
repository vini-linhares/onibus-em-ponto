package br.com.virtualartsa.onibusemponto;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Schiavetto on 24/08/2017.
 */

public class ProximoOnibusListaRecyclerAdapter extends RecyclerView.Adapter<ProximoOnibusListaViewHolder>{

    Context mctx;
    private List<ProximoOnibusListaItem> mList;

    public ProximoOnibusListaRecyclerAdapter(Context ctx, List<ProximoOnibusListaItem> list) {
        this.mctx = ctx;
        this.mList = list;
    }

    @Override
    public ProximoOnibusListaViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_lista_proximo_onibus, viewGroup, false);
        return new ProximoOnibusListaViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ProximoOnibusListaViewHolder viewHolder, int position) {
        ProximoOnibusListaItem onibus = mList.get(position);
        String distancia = onibus.getDistanciaTexto();
        String end = onibus.getEnd();
        String numero = onibus.getNumero();
        String nome = onibus.getNome();

        viewHolder.txtDistancia.setText(distancia);
        viewHolder.txtEnd.setText(end);
        viewHolder.txtNome.setText(nome);
        viewHolder.txtNumero.setText(numero);

        if(onibus.getAtualizando()){
            viewHolder.poLl.setBackgroundColor(Color.parseColor("#f0f0f0"));
            viewHolder.txtDistancia.setTextColor(Color.parseColor("#939393"));
            viewHolder.txtEnd.setTextColor(Color.parseColor("#939393"));
            viewHolder.txtNome.setTextColor(Color.parseColor("#939393"));
            viewHolder.txtNumero.setTextColor(Color.parseColor("#939393"));
            viewHolder.imgMb.setImageResource(R.drawable.minibus2);
        }else{
            viewHolder.poLl.setBackgroundColor(Color.parseColor("#fafafa"));
            viewHolder.txtDistancia.setTextColor(Color.parseColor("#737373"));
            viewHolder.txtEnd.setTextColor(Color.parseColor("#737373"));
            viewHolder.txtNome.setTextColor(Color.parseColor("#737373"));
            viewHolder.txtNumero.setTextColor(Color.parseColor("#737373"));
            viewHolder.imgMb.setImageResource(R.drawable.minibus);
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
