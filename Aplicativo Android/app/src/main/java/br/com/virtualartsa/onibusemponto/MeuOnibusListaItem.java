package br.com.virtualartsa.onibusemponto;

/**
 * Created by Schiavetto on 22/08/2017.
 */

public class MeuOnibusListaItem {

    private String distanciaTexto;
    private int distanciaValor;
    private String end;
    private boolean atualizando;

    public void setDistanciaTexto(String distanciaTexto){
        this.distanciaTexto = distanciaTexto;
    }
    public String getDistanciaTexto(){
        return this.distanciaTexto;
    }

    public void setDistanciaValor(int distanciaValor){this.distanciaValor = distanciaValor;}
    public int getDistanciaValor(){
        return this.distanciaValor;
    }

    public void setEnd(String end){
        this.end = end;
    }
    public String getEnd(){
        return this.end;
    }

    public void setAtualizando(boolean atualizando){
        this.atualizando = atualizando;
    }
    public boolean getAtualizando(){
        return this.atualizando;
    }

}
