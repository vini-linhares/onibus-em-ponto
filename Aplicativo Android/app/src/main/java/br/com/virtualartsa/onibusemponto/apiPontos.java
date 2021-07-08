package br.com.virtualartsa.onibusemponto;

import java.util.List;

/**
 * Created by Schiavetto on 15/12/2017.
 */

public class apiPontos {
    private List<apiPonto> pontos;

    public void setPontos(List<apiPonto> pontos){
        this.pontos = pontos;
    }
    public List<apiPonto> getPontos(){
        return this.pontos;
    }
}
