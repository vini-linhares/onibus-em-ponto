package br.com.virtualartsa.onibusemponto;

/**
 * Created by Schiavetto on 30/08/2017.
 */

public class PontoDistApiRespostaServidor {


    private String texto;
    private int valor;


    public void setTexto(String texto){
        this.texto = texto;
    }
    public String getTexto(){
        return this.texto;
    }

    public void setValor(int valor){
        this.valor = valor;
    }
    public int getValor(){return this.valor;}


}