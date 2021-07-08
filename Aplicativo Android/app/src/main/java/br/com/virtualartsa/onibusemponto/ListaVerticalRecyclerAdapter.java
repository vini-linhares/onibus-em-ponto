package br.com.virtualartsa.onibusemponto;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

/**
 * Created by Schiavetto on 31/07/2017.
 */

public class ListaVerticalRecyclerAdapter extends RecyclerView.Adapter<ListaVerticalViewHolder> {

    Context mctx;
    private List<ListaVerticalItem> mList;

    public ListaVerticalRecyclerAdapter(Context ctx, List<ListaVerticalItem> list) {
        this.mctx = ctx;
        this.mList = list;
    }

    @Override
    public ListaVerticalViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_lista, viewGroup, false);
        return new ListaVerticalViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ListaVerticalViewHolder viewHolder, int position) {
        ListaVerticalItem pessoa = mList.get(position);
        final Class classe = pessoa.getActivity();
        final int pos = position;

        //viewHolder.viewNome.setText(pessoa.getNome());
        viewHolder.btnDel.setImageResource(pessoa.getDrawableId());

        viewHolder.btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //removerItem(pos);
                //Toast.makeText(v.getContext(), pos, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(v.getContext(), classe);
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
