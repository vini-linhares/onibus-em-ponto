package br.com.virtualartsa.onibusemponto;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Schiavetto on 22/08/2017.
 */

public class MeuOnibusListaViewHolder extends RecyclerView.ViewHolder {

    protected TextView txtDistancia;
    protected TextView txtEnd;
    protected LinearLayout moLl;
    protected ImageView imgBus;

    public MeuOnibusListaViewHolder(final View itemView) {
        super(itemView);

        txtDistancia = (TextView) itemView.findViewById(R.id.txtDistanciaMO);
        txtEnd = (TextView) itemView.findViewById(R.id.txtEndMO);
        moLl = (LinearLayout) itemView.findViewById(R.id.moLl);
        imgBus = (ImageView) itemView.findViewById(R.id.imgBus);
    }

}
