package br.com.virtualartsa.onibusemponto;

/**
 * Created by Schiavetto on 16/08/2017.
 */

public class apiLinha {

    private int cl;
    private boolean lc;
    private String lt;
    private int sl;
    private int tl;
    private String tp;
    private String ts;

    public void setCl(int cl){
        this.cl = cl;
    }
    public int getCl(){
        return this.cl;
    }

    public void setLc(boolean lc){
        this.lc = lc;
    }
    public boolean getLc(){
        return this.lc;
    }

    public void setLt(String lt){
        this.lt = lt;
    }
    public String getLt(){
        return this.lt;
    }

    public void setSl(int sl){
        this.sl = sl;
    }
    public int getSl(){
        return this.sl;
    }

    public void setTl(int tl){
        this.tl = tl;
    }
    public int getTl(){
        return this.tl;
    }

    public void setTp(String tp){
        this.tp = tp;
    }
    public String getTp(){
        return this.tp;
    }

    public void setTs(String ts){
        this.ts = ts;
    }
    public String getTs(){
        return this.ts;
    }
}
