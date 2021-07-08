package br.com.virtualartsa.onibusemponto;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Schiavetto on 19/12/2017.
 */

public class PegaLinha2PontosListaRecyclerAdapter extends RecyclerView.Adapter<PegaLinha2PontosViewHolder>{

    public final static String PONTOID = "br.com.virtualartsa.onibusemponto.PONTOID";
    public final static String PONTOEND = "br.com.virtualartsa.onibusemponto.PONTOEND";
    public final static String GRUPO_SALVO = "br.com.virtualartsa.onibusemponto.GRUPO_SALVO";

    PegaLinha2Activity mctx;
    private List<PegaLinha2PontosListaItem> mList;
    String testes = "-";
    List<String[]> info = new ArrayList<String[]>(); ;

    public PegaLinha2PontosListaRecyclerAdapter(Context ctx, List<PegaLinha2PontosListaItem> list) {
        this.mctx = (PegaLinha2Activity)ctx;
        this.mList = list;
    }

    @Override
    public PegaLinha2PontosViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_lista_pontos, viewGroup, false);
        return new PegaLinha2PontosViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PegaLinha2PontosViewHolder viewHolder, final int position) {
        PegaLinha2PontosListaItem linha = mList.get(position);
        final String end = linha.getEnd();
        final String id = linha.getId();

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(v.getContext(), mList.get(position).getEnd(), Toast.LENGTH_SHORT).show();
                mctx.retrofitPegarLinhas(id);
                for(int i = 0; i < mctx.resposta2.getPontos().size(); i++){
                    if(mctx.resposta2.getPontos().get(i).getId() == id){
                        mctx.preencherListaPontos(mctx.resposta2, i);
                        break;
                    }
                }

                SharedPreferences sharedPref = mctx.getSharedPreferences( GRUPO_SALVO, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(PONTOID, id);
                editor.putString(PONTOEND, end);
                editor.apply();

                mctx.txtEnd.setText(end);
                mctx.listaPonto.setVisibility(View.GONE);
                mctx.llTextEnd.setVisibility(View.VISIBLE);
                mctx.llEditEnd.setVisibility(View.GONE);
                mctx.txtIsVisible = true;
                mctx.btnEnd.setImageResource(R.drawable.editar);
                mctx.hideKeyboard(v);
            }
        });

        viewHolder.txtEnd.setText(end);
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
