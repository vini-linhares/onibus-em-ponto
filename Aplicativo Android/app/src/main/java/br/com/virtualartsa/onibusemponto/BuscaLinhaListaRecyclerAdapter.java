package br.com.virtualartsa.onibusemponto;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Schiavetto on 16/08/2017.
 */

public class BuscaLinhaListaRecyclerAdapter extends RecyclerView.Adapter<BuscaLinhaListaViewHolder> {


    Context mctx;
    private List<BuscaLinhaListaItem> mList;

    public BuscaLinhaListaRecyclerAdapter(Context ctx, List<BuscaLinhaListaItem> list) {
        this.mctx = ctx;
        this.mList = list;
    }

    @Override
    public BuscaLinhaListaViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_lista_busca_linhas, viewGroup, false);
        return new BuscaLinhaListaViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(BuscaLinhaListaViewHolder viewHolder, int position) {
        BuscaLinhaListaItem linha = mList.get(position);
        final int pos = position;
        final int cod = linha.getCodLinha();

        final String linhaComp = linha.getNum()+"-"+linha.getModo();
        viewHolder.viewNum.setText(linhaComp);
        viewHolder.viewSentido.setText(linha.getSentido());
        viewHolder.viewSentido2.setText(linha.getSentido2());
        /*
        viewHolder.btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removerItem(pos);
            }
        });
        */
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Toast.makeText(v.getContext(), ""+cod, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(v.getContext(), DetalhesActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("linha", linhaComp);
                intent.putExtras(bundle);
                v.getContext().startActivity(intent);
            }
        });

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
