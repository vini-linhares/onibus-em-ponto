package br.com.virtualartsa.onibusemponto;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Schiavetto on 24/08/2017.
 */

public class PegaLinha2ListaRecyclerAdapter extends RecyclerView.Adapter<PegaLinha2ViewHolder>{


    PegaLinha2Activity mctx;
    private List<PegaLinhaListaItem> mList;
    String testes = "-";
    List<String[]> info = new ArrayList<String[]>(); ;

    public PegaLinha2ListaRecyclerAdapter(Context ctx, List<PegaLinhaListaItem> list) {
        this.mctx = (PegaLinha2Activity)ctx;
        this.mList = list;
    }

    @Override
    public PegaLinha2ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_lista_pega_linha_2, viewGroup, false);
        return new PegaLinha2ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PegaLinha2ViewHolder viewHolder, final int position) {
        PegaLinhaListaItem linha = mList.get(position);
        final String numero = linha.getNumero() + "-" + linha.getOperacao();
        final int sentido = linha.getSentido();
        final String nome = linha.getNome();


        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mList.get(position).getSelecionado()) {
                    mList.get(position).setSelecionado(false);
                    for (int j = info.size() - 1; j >= 0; j--) {
                        if (info.get(j)[0].equals(numero)) {
                            if (info.get(j)[1].equals(String.valueOf(sentido))) {
                                info.remove(j);
                            }
                        }
                    }
                } else if((!mctx.isVoice && info.size() < 6) || (mctx.isVoice && info.size() < 3)) {
                    mList.get(position).setSelecionado(true);
                    String[] item = new String[3];
                    item[0] = numero;
                    item[1] = String.valueOf(sentido);
                    item[2] = nome;
                    info.add(item);
                }else{
                    if(mctx.isVoice){
                        Toast.makeText(v.getContext(), "Limite de 3 linhas selecionadas.", Toast.LENGTH_SHORT).show();
                        mctx.ttsManager.initQueue("Limite de 3 linhas selecionadas.");
                    }else{
                        Toast.makeText(v.getContext(), "Limite de 6 linhas selecionadas.", Toast.LENGTH_SHORT).show();
                    }
                }
                notifyDataSetChanged();
                if (info.size() > 0) {
                    mctx.fab.setVisibility(View.VISIBLE);
                } else {
                    mctx.fab.setVisibility(View.GONE);
                }

            }
        });

        if(linha.getSelecionado()){
            //viewHolder.pl2Ll.setBackgroundColor(Color.parseColor("#f9f7d4"));
            //viewHolder.pl2Ll.setBackgroundColor(Color.parseColor("#bfc4ff"));
            viewHolder.pl2Ll.setBackgroundColor(Color.parseColor("#e4f2ff"));
            //viewHolder.pl2Ll.setBackgroundColor(Color.parseColor("#ffdbb8"));
        }
        else
        {
            viewHolder.pl2Ll.setBackgroundColor(Color.parseColor("#fafafa"));
        }

        viewHolder.txtNumero.setText(numero);
        viewHolder.txtNome.setText(linha.getNome());
        /*
        viewHolder.cbSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    //testes += numero + "-";
                    String[] item = new String[3];
                    item[0] = numero;
                    item[1] = String.valueOf(sentido);
                    item[2] = nome;
                    info.add(item);
                }else{
                    for(int j = info.size()-1; j >= 0; j--){
                        if(info.get(j)[0].equals(numero)){
                            if(info.get(j)[1].equals(String.valueOf(sentido))){
                                info.remove(j);
                            }
                        }
                    }
                }
                if (info.size() > 0){
                    mctx.fab.setVisibility(View.VISIBLE);
                }else{
                    mctx.fab.setVisibility(View.GONE);
                }
            }
        });
        */
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

                //Toast.makeText(v.getContext(), numero, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(v.getContext(), MeuOnibusActivity.class);
                Bundle bundle = new Bundle();
                //bundle.putString("linha", numero);
                //bundle.putString("sentido", String.valueOf(sentido));
                intent.putExtras(bundle);
                v.getContext().startActivity(intent);
            }
        });
*/
    }

    public void teste(Context cont){
        if(info.size() == 0){
            Toast.makeText(cont, "Selecione pelo menos uma linha.", Toast.LENGTH_SHORT).show();
        }else if(info.size() == 1){
            Intent intent = new Intent(cont, MeuOnibusActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("linha", info.get(0)[0].split("-")[0]);
            bundle.putString("operacao", info.get(0)[0].split("-")[1]);
            bundle.putString("sentido", info.get(0)[1]);
            bundle.putString("nome", info.get(0)[2]);
            intent.putExtras(bundle);
            cont.startActivity(intent);
        }else{
            Intent intent = new Intent(cont, ProximoOnibusActivity.class);
            Bundle bundle = new Bundle();

            bundle.putString("quant", String.valueOf(info.size()));
            for(int i = 0; i<info.size(); i++){
                bundle.putString("numero"+i, info.get(i)[0]);
                bundle.putString("sentido"+i, info.get(i)[1]);
                bundle.putString("nome"+i, info.get(i)[2]);
            }

            intent.putExtras(bundle);
            cont.startActivity(intent);
        }
        //Toast.makeText(cont, info.get(info.size()-1)[0], Toast.LENGTH_SHORT).show();
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
