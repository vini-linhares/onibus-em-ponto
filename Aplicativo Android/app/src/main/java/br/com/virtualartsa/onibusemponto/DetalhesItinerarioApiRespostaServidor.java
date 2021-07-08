package br.com.virtualartsa.onibusemponto;

import java.util.List;

/**
 * Created by Schiavetto on 18/08/2017.
 */

public class DetalhesItinerarioApiRespostaServidor {

    private List<apiEnd> ida;
    private List<apiEnd> volta;

    public void setIda(List<apiEnd> ida){
        this.ida = ida;
    }
    public List<apiEnd> getIda(){
        return this.ida;
    }

    public void setVolta(List<apiEnd> volta){
        this.volta = volta;
    }
    public List<apiEnd> getVolta(){
        return this.volta;
    }


}
