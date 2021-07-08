package br.com.virtualartsa.onibusemponto;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Schiavetto on 24/08/2017.
 */

public class PegaLinha2ViewHolder extends RecyclerView.ViewHolder{



    protected TextView txtNumero;
    protected TextView txtNome;
    //protected CheckBox cbSelect;
    protected LinearLayout pl2Ll;

    public PegaLinha2ViewHolder(final View itemView) {
        super(itemView);

        txtNumero = (TextView) itemView.findViewById(R.id.txtNumeroPL2);
        txtNome = (TextView) itemView.findViewById(R.id.txtNomePL2);
        //cbSelect = (CheckBox) itemView.findViewById(R.id.cbPL2);
        pl2Ll = (LinearLayout) itemView.findViewById(R.id.pl2Ll);

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
