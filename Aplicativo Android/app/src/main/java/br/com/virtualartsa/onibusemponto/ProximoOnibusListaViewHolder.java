package br.com.virtualartsa.onibusemponto;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Schiavetto on 24/08/2017.
 */

public class ProximoOnibusListaViewHolder extends RecyclerView.ViewHolder  {

    protected TextView txtDistancia;
    protected TextView txtEnd;
    protected TextView txtNome;
    protected TextView txtNumero;
    protected LinearLayout poLl;
    protected ImageView imgMb;

    public ProximoOnibusListaViewHolder(final View itemView) {
        super(itemView);

        txtDistancia = (TextView) itemView.findViewById(R.id.txtDistanciaPO);
        txtEnd = (TextView) itemView.findViewById(R.id.txtEndPO);
        txtNome = (TextView) itemView.findViewById(R.id.txtNomePO);
        txtNumero = (TextView) itemView.findViewById(R.id.txtNumeroPO);
        poLl = (LinearLayout) itemView.findViewById(R.id.poLl);
        imgMb = (ImageView) itemView.findViewById(R.id.imgMb);
    }
}
