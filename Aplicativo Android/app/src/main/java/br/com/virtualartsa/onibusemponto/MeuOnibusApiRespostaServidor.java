package br.com.virtualartsa.onibusemponto;

import java.util.List;

/**
 * Created by Schiavetto on 22/08/2017.
 */

public class MeuOnibusApiRespostaServidor {

    private String status;
    private String mensagem;
    private List<apiMeuOnibus> proximo_onibus;



    public void setStatus(String status){
        this.status = status;
    }
    public String getStatus(){
        return this.status;
    }

    public void setMensagem(String mensagem){
        this.mensagem = mensagem;
    }
    public String getMensagem(){return this.mensagem;}

    public void setProximoOnibus(List<apiMeuOnibus> proximo_onibus){this.proximo_onibus = proximo_onibus;}
    public List<apiMeuOnibus> getProximoOnibus(){
        return this.proximo_onibus;
    }

}
