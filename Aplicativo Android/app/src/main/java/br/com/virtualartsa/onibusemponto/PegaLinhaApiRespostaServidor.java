package br.com.virtualartsa.onibusemponto;

import java.util.List;

/**
 * Created by Schiavetto on 21/08/2017.
 */

public class PegaLinhaApiRespostaServidor {

    private List<apiPegaLinha> linhas;

    public void setLinhas(List<apiPegaLinha> linhas){
        this.linhas = linhas;
    }
    public List<apiPegaLinha> getLinhas(){
        return this.linhas;
    }

}
