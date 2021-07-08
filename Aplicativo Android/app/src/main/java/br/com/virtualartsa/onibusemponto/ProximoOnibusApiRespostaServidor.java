package br.com.virtualartsa.onibusemponto;

import java.util.List;

/**
 * Created by Schiavetto on 24/08/2017.
 */

public class ProximoOnibusApiRespostaServidor {

    private String status;
    private String mensagem;
    private apiMeuOnibus proximo_onibus;


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

    public void setProximoOnibus(apiMeuOnibus proximo_onibus){this.proximo_onibus = proximo_onibus;}
    public apiMeuOnibus getProximoOnibus(){
        return this.proximo_onibus;
    }
}
