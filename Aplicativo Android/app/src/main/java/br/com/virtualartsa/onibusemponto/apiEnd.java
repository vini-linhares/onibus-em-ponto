package br.com.virtualartsa.onibusemponto;

/**
 * Created by Schiavetto on 18/08/2017.
 */

public class apiEnd {

    private String rua;
    private int num_inicio;
    private int num_fim;

    public void setRua(String rua){
        this.rua = rua;
    }
    public String getRua(){
        return this.rua;
    }

    public void setNumInicio(int num_inicio){
        this.num_inicio = num_inicio;
    }
    public int getNumInicio(){
        return this.num_inicio;
    }

    public void setNumFim(int num_fim){
        this.num_fim = num_fim;
    }
    public int getNumFim(){
        return this.num_fim;
    }

}
