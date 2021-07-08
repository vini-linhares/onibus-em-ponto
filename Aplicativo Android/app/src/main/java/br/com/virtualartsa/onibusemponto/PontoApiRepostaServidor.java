package br.com.virtualartsa.onibusemponto;

/**
 * Created by Schiavetto on 29/08/2017.
 */

public class PontoApiRepostaServidor {

    private float lat;
    private float lng;
    private String rua;
    private String num;

    public void setLat(float lat){
        this.lat = lat;
    }
    public float getLat(){
        return this.lat;
    }

    public void setLng(float lng){
        this.lng = lng;
    }
    public float getLng(){
        return this.lng;
    }

    public void setRua(String rua){
        this.rua = rua;
    }
    public String getRua(){
        return this.rua;
    }

    public void setNum(String num){
        this.num = num;
    }
    public String getNum(){return this.num;}


}