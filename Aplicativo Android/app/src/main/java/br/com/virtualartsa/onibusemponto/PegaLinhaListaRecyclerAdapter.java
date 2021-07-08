package br.com.virtualartsa.onibusemponto;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

/**
 * Created by Schiavetto on 21/08/2017.
 */

public class PegaLinhaListaRecyclerAdapter extends RecyclerView.Adapter<PegaLinhaViewHolder>{


    Context mctx;
    private List<PegaLinhaListaItem> mList;

    public PegaLinhaListaRecyclerAdapter(Context ctx, List<PegaLinhaListaItem> list) {
        this.mctx = ctx;
        this.mList = list;
    }

    @Override
    public PegaLinhaViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_lista_pega_linha, viewGroup, false);
        return new PegaLinhaViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PegaLinhaViewHolder viewHolder, int position) {
        PegaLinhaListaItem linha = mList.get(position);
        final String numero = linha.getNumero();
        final String operacao = String.valueOf(linha.getOperacao());
        final String sentido = String.valueOf(linha.getSentido());
        final String nome = linha.getNome();

        viewHolder.txtNumero.setText(numero+"-"+operacao);
        viewHolder.txtNome.setText(linha.getNome());
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

                //Toast.makeText(v.getContext(), numero, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(v.getContext(), MeuOnibusActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("linha", numero);
                bundle.putString("operacao", operacao);
                bundle.putString("sentido", sentido);
                bundle.putString("nome", nome);
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
