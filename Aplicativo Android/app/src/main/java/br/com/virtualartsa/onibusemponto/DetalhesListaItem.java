package br.com.virtualartsa.onibusemponto;

/**
 * Created by Schiavetto on 18/08/2017.
 */

public class DetalhesListaItem {
    private String rua;
    private int numInicio;
    private int numFim;

    public DetalhesListaItem(){}

    public String getRua() {
        return rua;
    }
    public void setRua(String rua) {
        this.rua = rua;
    }

    public int getNumInicio() {
        return numInicio;
    }
    public void setNumInicio(int numInicio) {
        this.numInicio = numInicio;
    }

    public int getNumFim() {
        return numFim;
    }
    public void setNumFim(int numFim) {
        this.numFim = numFim;
    }
}
