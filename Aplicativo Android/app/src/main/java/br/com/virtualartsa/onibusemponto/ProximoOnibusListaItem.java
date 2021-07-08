package br.com.virtualartsa.onibusemponto;

/**
 * Created by Schiavetto on 25/08/2017.
 */

public class ProximoOnibusListaItem {

    private String distanciaTexto;
    private int distanciaValor;
    private String end;
    private String nome;
    private String numero;
    private String hora;
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

    public void setNome(String nome){
        this.nome = nome;
    }
    public String getNome(){
        return this.nome;
    }

    public void setNumero(String numero){
        this.numero = numero;
    }
    public String getNumero(){return this.numero;}

    public void setHora(String hora){this.hora = hora;}
    public String getHora(){return this.hora;}

    public void setAtualizando(boolean atualizando){
        this.atualizando = atualizando;
    }
    public boolean getAtualizando(){
        return this.atualizando;
    }
}
