package br.com.virtualartsa.onibusemponto;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Schiavetto on 15/12/2017.
 */

public class PegaLinha2PontosViewHolder extends RecyclerView.ViewHolder{



    protected TextView txtEnd;
    protected LinearLayout pl2pLl;

    public PegaLinha2PontosViewHolder(final View itemView) {
        super(itemView);

        txtEnd = (TextView) itemView.findViewById(R.id.txtEndPL2P);
        pl2pLl = (LinearLayout) itemView.findViewById(R.id.pl2pLl);

    }
}
