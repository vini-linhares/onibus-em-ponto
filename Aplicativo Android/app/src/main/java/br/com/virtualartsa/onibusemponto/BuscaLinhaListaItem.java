package br.com.virtualartsa.onibusemponto;

/**
 * Created by Schiavetto on 16/08/2017.
 */

public class BuscaLinhaListaItem {

    private String num;
    private String sentido;
    private String sentido2;
    private int codLinha;
    private int modo;

    public BuscaLinhaListaItem(){}

    public String getNum() {
        return num;
    }
    public void setNum(String num) {
        this.num = num;
    }

    public String getSentido() {
        return sentido;
    }
    public void setSentido(String sentido) {
        this.sentido = sentido;
    }

    public String getSentido2() {
        return sentido2;
    }
    public void setSentido2(String sentido2) {
        this.sentido2 = sentido2;
    }

    public int getCodLinha() {
        return codLinha;
    }
    public void setCodLinha(int codLinha) {
        this.codLinha = codLinha;
    }

    public int getModo() {
        return modo;
    }
    public void setModo(int modo) {
        this.modo = modo;
    }
}
