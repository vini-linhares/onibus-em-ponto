package br.com.virtualartsa.onibusemponto;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Schiavetto on 16/08/2017.
 */

public class BuscaLinhaListaViewHolder extends RecyclerView.ViewHolder{


    protected TextView viewNum;
    protected TextView viewSentido;
    protected TextView viewSentido2;

    public BuscaLinhaListaViewHolder(final View itemView) {
        super(itemView);

        viewNum = (TextView) itemView.findViewById(R.id.txtNumLinha);
        viewSentido = (TextView) itemView.findViewById(R.id.txtSentido1);
        viewSentido2 = (TextView) itemView.findViewById(R.id.txtSentido2);



 

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
