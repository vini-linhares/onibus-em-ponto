 package br.com.virtualartsa.onibusemponto;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

 public class Tutorial2 extends AppCompatActivity {

     public final static String GRUPO_SALVO = "br.com.virtualartsa.onibusemponto.GRUPO_SALVO";
     public final static String VOZ = "br.com.virtualartsa.onibusemponto.VOZ";
     public final static String TUTORIAL_SALVO = "br.com.virtualartsa.onibusemponto.TUTORIAL_SALVO";
     Context context;

     public ImageButton tela;
     ImageButton btnDireta, btnEsquerda;

     int telaAtual;
     boolean decideVoz;
     boolean isVoice;

     MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial2);

        tela = (ImageButton)findViewById(R.id.imgbtnTela1);
        telaAtual = 1;

        btnDireta = (ImageButton)findViewById(R.id.btnDireita);
        btnEsquerda = (ImageButton)findViewById(R.id.btnEsquerda);
        decideVoz = true;
    }

     @Override
     protected void onPause(){
         mediaPlayer.stop();
         super.onPause();
     }

     @Override
     protected void onResume(){
         tela.setImageResource(R.drawable.a);
         telaAtual = 1;
         mediaPlayer = MediaPlayer.create(this, R.raw.a);
         mediaPlayer.start(); // no need to call prepare(); create() does that for you
         mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
         {
             @Override
             public void onCompletion(MediaPlayer mp)
             {
                 trocandoTela();
             }
         });
         super.onResume();
     }

     public void fechar(View view){
         fechar();
     }

     public void fechar(){
         mediaPlayer.stop();
         finish();
     }

     public void trocaTela(View view){
         mediaPlayer.stop();
         trocandoTela();
     }

     public void btnDireita(View view){
         SharedPreferences sharedPref = this.getSharedPreferences( GRUPO_SALVO, Context.MODE_PRIVATE);
         SharedPreferences.Editor editor = sharedPref.edit();
         editor.putString(VOZ, "1");
         editor.apply();
         telaAtual++;
         btnDireta.setVisibility(View.INVISIBLE);
         mediaPlayer.stop();
         trocandoTela();
         decideVoz = false;
         isVoice = true;
     }

     public void btnEsquerda(View view){
         if(decideVoz){
             SharedPreferences sharedPref = this.getSharedPreferences( GRUPO_SALVO, Context.MODE_PRIVATE);
             SharedPreferences.Editor editor = sharedPref.edit();
             editor.putString(VOZ, "0");
             editor.apply();
             telaAtual++;
             btnDireta.setVisibility(View.INVISIBLE);
             btnEsquerda.setVisibility(View.INVISIBLE);
             decideVoz = false;
             isVoice = false;
             mediaPlayer.stop();
             trocandoTela();
         }else{
             fechar();
         }
     }

     public void trocandoTela(){
         switch (telaAtual) {
             case 1:
                 tela.setImageResource(R.drawable.b);
                 btnDireta.setVisibility(View.VISIBLE);
                 btnEsquerda.setVisibility(View.VISIBLE);
                 break;
             case 2:
                 tela.setImageResource(R.drawable.c);
                 break;
             case 3:
                 tela.setImageResource(R.drawable.d);
                 break;
             case 4:
                 tela.setImageResource(R.drawable.e);
                 break;
             case 5:
                 tela.setImageResource(R.drawable.f);
                 break;
             case 6:
                 tela.setImageResource(R.drawable.g);
                 break;
             case 7:
                 tela.setImageResource(R.drawable.h);
                 break;
             case 8:
                 tela.setImageResource(R.drawable.i);
                 break;
             case 9:
                 tela.setImageResource(R.drawable.j);
                 break;
             case 10:
                 tela.setImageResource(R.drawable.k);
                 break;
             case 11:
                 tela.setImageResource(R.drawable.l);
                 break;
             case 12:
                 tela.setImageResource(R.drawable.m);
                 break;
             case 13:
                 tela.setImageResource(R.drawable.n);
                 break;
             case 14:
                 fechar();
         }
         if (isVoice || decideVoz){
             controlaAudios();
         }
         telaAtual++;
     }

     public void controlaAudios(){
         switch (telaAtual) {
             case 1:
                 mediaPlayer = MediaPlayer.create(this, R.raw.b);
                 mediaPlayer.start(); // no need to call prepare(); create() does that for you
                 mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                     @Override
                     public void onCompletion(MediaPlayer mp) {
                         //trocandoTela();
                     }
                 });
                 break;
             case 2:
                 mediaPlayer = MediaPlayer.create(this, R.raw.c);
                 mediaPlayer.start(); // no need to call prepare(); create() does that for you
                 mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                     @Override
                     public void onCompletion(MediaPlayer mp) {
                         trocandoTela();
                     }
                 });
                 break;
             case 3:
                 mediaPlayer = MediaPlayer.create(this, R.raw.d);
                 mediaPlayer.start(); // no need to call prepare(); create() does that for you
                 mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                     @Override
                     public void onCompletion(MediaPlayer mp) {
                         trocandoTela();
                     }
                 });
                 break;
             case 4:
                 mediaPlayer = MediaPlayer.create(this, R.raw.e);
                 mediaPlayer.start(); // no need to call prepare(); create() does that for you
                 mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                     @Override
                     public void onCompletion(MediaPlayer mp) {
                         trocandoTela();
                     }
                 });
                 break;
             case 5:
                 mediaPlayer = MediaPlayer.create(this, R.raw.f);
                 mediaPlayer.start(); // no need to call prepare(); create() does that for you
                 mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                     @Override
                     public void onCompletion(MediaPlayer mp) {
                         trocandoTela();
                     }
                 });
                 break;
             case 6:
                 mediaPlayer = MediaPlayer.create(this, R.raw.g);
                 mediaPlayer.start(); // no need to call prepare(); create() does that for you
                 mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                     @Override
                     public void onCompletion(MediaPlayer mp) {
                         trocandoTela();
                     }
                 });
                 break;
             case 7:
                 mediaPlayer = MediaPlayer.create(this, R.raw.h);
                 mediaPlayer.start(); // no need to call prepare(); create() does that for you
                 mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                     @Override
                     public void onCompletion(MediaPlayer mp) {
                         trocandoTela();
                     }
                 });
                 break;
             case 8:
                 mediaPlayer = MediaPlayer.create(this, R.raw.i);
                 mediaPlayer.start(); // no need to call prepare(); create() does that for you
                 mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                     @Override
                     public void onCompletion(MediaPlayer mp) {
                         trocandoTela();
                     }
                 });
                 break;
             case 9:
                 mediaPlayer = MediaPlayer.create(this, R.raw.j);
                 mediaPlayer.start(); // no need to call prepare(); create() does that for you
                 mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                     @Override
                     public void onCompletion(MediaPlayer mp) {
                         trocandoTela();
                     }
                 });
                 break;
             case 10:
                 mediaPlayer = MediaPlayer.create(this, R.raw.k);
                 mediaPlayer.start(); // no need to call prepare(); create() does that for you
                 mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                     @Override
                     public void onCompletion(MediaPlayer mp) {
                         trocandoTela();
                     }
                 });
                 break;
             case 11:
                 mediaPlayer = MediaPlayer.create(this, R.raw.l);
                 mediaPlayer.start(); // no need to call prepare(); create() does that for you
                 mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                     @Override
                     public void onCompletion(MediaPlayer mp) {
                         trocandoTela();
                     }
                 });
                 break;
             case 12:
                 mediaPlayer = MediaPlayer.create(this, R.raw.m);
                 mediaPlayer.start(); // no need to call prepare(); create() does that for you
                 mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                     @Override
                     public void onCompletion(MediaPlayer mp) {
                         trocandoTela();
                     }
                 });
                 break;
             case 13:
                 mediaPlayer = MediaPlayer.create(this, R.raw.n);
                 mediaPlayer.start(); // no need to call prepare(); create() does that for you
                 mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                     @Override
                     public void onCompletion(MediaPlayer mp) {
                         trocandoTela();
                     }
                 });
                 break;
         }
     }
}
