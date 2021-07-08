package br.com.virtualartsa.onibusemponto;

/**
 * Created by Schiavetto on 21/08/2017.
 */

public class apiPegaLinha {

    private String numero;
    private int operacao;
    private int sentido;
    private String nome;

    public void setNumero(String numero){
        this.numero = numero;
    }
    public String getNumero(){
        return this.numero;
    }

    public void setOperacao(int operacao){this.operacao = operacao;}
    public int getOperacao(){
        return this.operacao;
    }

    public void setSentido(int sentido){
        this.sentido = sentido;
    }
    public int getSentido(){
        return this.sentido;
    }

    public void setNome(String nome){
        this.nome = nome;
    }
    public String getNome(){
        return this.nome;
    }

}
