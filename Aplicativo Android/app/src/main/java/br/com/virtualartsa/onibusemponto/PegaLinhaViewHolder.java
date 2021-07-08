package br.com.virtualartsa.onibusemponto;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Schiavetto on 21/08/2017.
 */

public class PegaLinhaViewHolder  extends RecyclerView.ViewHolder{



        protected TextView txtNumero;
        protected TextView txtNome;

        public PegaLinhaViewHolder(final View itemView) {
                super(itemView);

                txtNumero = (TextView) itemView.findViewById(R.id.txtNumeroPL);
                txtNome = (TextView) itemView.findViewById(R.id.txtNomePL);

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
