package br.com.virtualartsa.onibusemponto;

import java.util.List;

/**
 * Created by Schiavetto on 17/08/2017.
 */

public class DetalhesApiRespostaServidor {


    private String ida;
    private String volta;
    private apiHorarios semana;
    private apiHorarios sabado;
    private apiHorarios domingo;


    public void setIda(String ida){
        this.ida = ida;
    }
    public String getIda(){
        return this.ida;
    }

    public void setVolta(String volta){
        this.volta = volta;
    }
    public String getVolta(){
        return this.volta;
    }

    public void setSemana(apiHorarios semana){
        this.semana = semana;
    }
    public apiHorarios getSemana(){
        return this.semana;
    }

    public void setSabado(apiHorarios sabado){
        this.sabado = sabado;
    }
    public apiHorarios getSabado(){
        return this.sabado;
    }

    public void setDomingo(apiHorarios domingo){
        this.domingo = domingo;
    }
    public apiHorarios getDomingo(){
        return this.domingo;
    }

}
