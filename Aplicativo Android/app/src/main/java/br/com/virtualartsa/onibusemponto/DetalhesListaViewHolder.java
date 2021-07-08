package br.com.virtualartsa.onibusemponto;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Schiavetto on 18/08/2017.
 */

public class DetalhesListaViewHolder extends RecyclerView.ViewHolder{
    protected TextView txtRua;
    protected TextView txtNumInicio;
    protected TextView txtNumFim;

    public DetalhesListaViewHolder(final View itemView) {
        super(itemView);

        txtRua = (TextView) itemView.findViewById(R.id.txtRuax);
        txtNumInicio = (TextView) itemView.findViewById(R.id.txtNumIniciox);
        txtNumFim = (TextView) itemView.findViewById(R.id.txtNumFimx);

        /*
        btnMais.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Nome = " + viewNome.getText() + "(" + String.valueOf(getAdapterPosition()) + ")", Toast.LENGTH_SHORT).show();
            }
        });
        */
        //btnDel.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        Toast.makeText(v.getContext(), "ITEM PRESSED = " + String.valueOf(getAdapterPosition()), Toast.LENGTH_SHORT).show();
        //    }
        //});
    }
}
