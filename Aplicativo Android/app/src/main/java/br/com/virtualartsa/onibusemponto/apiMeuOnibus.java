package br.com.virtualartsa.onibusemponto;

/**
 * Created by Schiavetto on 22/08/2017.
 */

public class apiMeuOnibus {

    private int prefixo;
    private float latitude;
    private float longitude;
    private String rua;
    private String numero;
    private String distancia_texto;
    private int distancia_valor;


    public void setPrefixo(int prefixo){this.prefixo = prefixo;}
    public int getPrefixo(){
        return this.prefixo;
    }

    public void setLatitude(float latitude){
        this.latitude = latitude;
    }
    public float getLatitude(){
        return this.latitude;
    }

    public void setLongitude(float longitude){this.longitude = longitude;}
    public float getLongitude(){return this.longitude;}

    public void setRua(String rua){
        this.rua = rua;
    }
    public String getRua(){
        return this.rua;
    }

    public void setNumero(String numero){
        this.numero = numero;
    }
    public String getNumero(){return this.numero;}

    public void setDistanciaTexto(String distancia_texto){ this.distancia_texto = distancia_texto;}
    public String getDistanciaTexto(){return this.distancia_texto;}

    public void setDistanciaValor(int distancia_valor){
        this.distancia_valor = distancia_valor;
    }
    public int getDistanciaValor(){
        return this.distancia_valor;
    }
}
