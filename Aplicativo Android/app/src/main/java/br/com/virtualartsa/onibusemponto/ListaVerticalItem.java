package br.com.virtualartsa.onibusemponto;

import android.app.Activity;

/**
 * Created by Schiavetto on 31/07/2017.
 */

public class ListaVerticalItem {

    //private String nome;
    private int drawableId;
    private Class activity;

    public ListaVerticalItem(){}
    /*
    public String getNome() {return nome;}
    public void setNome(String nome) {  this.nome = nome;  }
    */
    public int getDrawableId() {
        return drawableId;
    }
    public void setDrawableId(int drawableId) {
        this.drawableId = drawableId;
    }

    public Class getActivity() {
        return activity;
    }
    public void setActivity(Class activity) {
        this.activity = activity;
    }
}