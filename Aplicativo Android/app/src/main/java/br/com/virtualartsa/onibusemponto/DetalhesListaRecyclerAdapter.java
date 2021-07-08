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
 * Created by Schiavetto on 18/08/2017.
 */

public class DetalhesListaRecyclerAdapter extends RecyclerView.Adapter<DetalhesListaViewHolder> {


    Context mctx;
    private List<DetalhesListaItem> mList;

    public DetalhesListaRecyclerAdapter(Context ctx, List<DetalhesListaItem> list) {
        this.mctx = ctx;
        this.mList = list;
    }

    @Override
    public DetalhesListaViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_lista_itinerario, viewGroup, false);
        return new DetalhesListaViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DetalhesListaViewHolder viewHolder, int position) {
        DetalhesListaItem end = mList.get(position);
        //final int pos = position;

        viewHolder.txtRua.setText(end.getRua());
        viewHolder.txtNumInicio.setText(String.valueOf(end.getNumInicio()));
        viewHolder.txtNumFim.setText(String.valueOf(end.getNumFim()));
        /*
        viewHolder.btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removerItem(pos);
            }
        });
        */
        /*
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Toast.makeText(v.getContext(), ""+cod, Toast.LENGTH_SHORT).show();
                //Intent intent = new Intent(v.getContext(), DetalhesActivity.class);
                //Bundle bundle = new Bundle();
                //bundle.putString("linha", linhaComp);
                //intent.putExtras(bundle);
                //v.getContext().startActivity(intent);
            }
        });
        */
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
