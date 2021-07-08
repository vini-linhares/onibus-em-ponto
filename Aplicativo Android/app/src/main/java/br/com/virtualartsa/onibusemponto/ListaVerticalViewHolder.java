package br.com.virtualartsa.onibusemponto;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Schiavetto on 31/07/2017.
 */

public class ListaVerticalViewHolder extends RecyclerView.ViewHolder  {


    protected TextView viewNome;
    public ImageButton btnMais;
    public ImageButton btnDel;

    public ListaVerticalViewHolder(final View itemView) {
        super(itemView);

        //viewNome = (TextView) itemView.findViewById(R.id.txt_ilv_nome);
        //btnMais = (ImageButton) itemView.findViewById(R.id.btn_ilv_mais);
        btnDel = (ImageButton) itemView.findViewById(R.id.btn_ilv_del);
        /*
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Funcionalidade em Desenvolvimento - Estamos Desenvolvendo API Complementar", Toast.LENGTH_SHORT).show();
                //v.getContext().startActivity(intent);
            }
        });
*/
    }

}