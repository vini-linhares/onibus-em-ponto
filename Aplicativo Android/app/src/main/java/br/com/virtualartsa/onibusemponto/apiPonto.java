package br.com.virtualartsa.onibusemponto;

/**
 * Created by Schiavetto on 15/12/2017.
 */

public class apiPonto {

    private String id;
    private double lat;
    private double lng;
    private String rua;
    private String num;

    public void setId(String id){
        this.id = id;
    }
    public String getId(){
        return this.id;
    }

    public void setLat(double lat){this.lat = lat;}
    public double getLat(){
        return this.lat;
    }

    public void setLng(double lng){
        this.lng = lng;
    }
    public double getLng(){
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
    public String getNum(){
        return this.num;
    }

}
