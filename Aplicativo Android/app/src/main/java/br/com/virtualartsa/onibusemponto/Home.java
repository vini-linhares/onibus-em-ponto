package br.com.virtualartsa.onibusemponto;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Home extends AppCompatActivity  {

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    public final static String GRUPO_SALVO = "br.com.virtualartsa.onibusemponto.GRUPO_SALVO";
    public final static String TUTORIAL_SALVO = "br.com.virtualartsa.onibusemponto.TUTORIAL_SALVO";
    public final static String VOZ = "br.com.virtualartsa.onibusemponto.VOZ";
    public LinearLayout bots;
    public boolean comandVoz;
    Context context;

    TTSManager ttsManager = null;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    ListaVerticalRecyclerAdapter adapter;
    private List<ListaVerticalItem> pessoasListas = new ArrayList<>();
    private FloatingActionButton floatingActionButton;

    String listaNomes;

    Vibrator vib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        vib = (Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE);

        //Android toolbar
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setaRecyclerView();
        preencheLista();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(comandVoz){
            ttsManager.shutDown();
        }
    }

    @Override
    protected void onResume() {

        context = this;
        SharedPreferences sharedPref = context.getSharedPreferences( GRUPO_SALVO, Context.MODE_PRIVATE);
        String tutorialVisto = sharedPref.getString(TUTORIAL_SALVO, "0");
        if(tutorialVisto.equals("0")) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(TUTORIAL_SALVO, "1");
            editor.apply();
            Intent intent = new Intent(getApplicationContext(), Tutorial2.class);
            startActivity(intent);
        }else{
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(getApplicationContext(), "Sem permissão", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }

        if (comandVoz) {
            vib.vibrate(150);
        }
        bots = (LinearLayout)findViewById(R.id.layBotsH);
        sharedPref = this.getSharedPreferences( GRUPO_SALVO, Context.MODE_PRIVATE);
        tutorialVisto = sharedPref.getString(VOZ, "0");
        if(tutorialVisto.equals("0")){
            bots.setVisibility(View.INVISIBLE);
            comandVoz = false;
        }else{
            bots.setVisibility(View.VISIBLE);
            ttsManager = new TTSManager();
            ttsManager.init(this);
            comandVoz = true;
        }

        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    String[] itens = new String[2];
                    itens[0] = "Desejo Permitir!";
                    itens[1] = "Não quero usar esse aplicativo";

                    AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
                    builder.setTitle("Esse app precsia do GPS para funcinar corretamente.")
                            .setItems(itens, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    if(which == 0){
                                        ActivityCompat.requestPermissions(Home.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                                    }else{
                                        finish();
                                    }
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void setaRecyclerView(){
        //Aqui é instanciado o Recyclerview
        mRecyclerView = (RecyclerView) findViewById(R.id.rcv_lista_vertical);
        //mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false); // Fazer a Lista virar Horizontal
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        adapter = new ListaVerticalRecyclerAdapter(this, pessoasListas);
        mRecyclerView.setAdapter(adapter);

        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }




    public void preencheLista(){
            ListaVerticalItem pessoa1 = new ListaVerticalItem();
            //ListaVerticalItem pessoa2 = new ListaVerticalItem();
            ListaVerticalItem pessoa3 = new ListaVerticalItem();
            ListaVerticalItem pessoa4 = new ListaVerticalItem();
            ListaVerticalItem pessoa5 = new ListaVerticalItem();
            pessoa1.setDrawableId(R.drawable.btn1);
            pessoa1.setActivity(PegaLinha2Activity.class);
            pessoasListas.add(pessoa1);
            //pessoa2.setDrawableId(R.drawable.btn2);
            //pessoa2.setActivity(PegaLinha2Activity.class);
            //pessoasListas.add(pessoa2);
            pessoa3.setDrawableId(R.drawable.btn2);
            pessoa3.setActivity(PontoActivity.class);
            pessoasListas.add(pessoa3);
            pessoa4.setDrawableId(R.drawable.btn3);
            pessoa4.setActivity(RuasActivity.class);
            pessoasListas.add(pessoa4);
            pessoa5.setDrawableId(R.drawable.btn4);
            pessoa5.setActivity(BuscaLinhaActivity.class);
            pessoasListas.add(pessoa5);

        adapter.notifyDataSetChanged();
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Diga o Comando");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

        }
    }

    public void btnDireita(View view){
        startVoiceInput();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    reconhecerComando(result.get(0));
                }
                break;
            }

        }
    }

    public void reconhecerComando(String comando){
        if (comando.toLowerCase().equals("onde está meu ônibus")){
            Intent intent = new Intent(this, PegaLinha2Activity.class);
            this.startActivity(intent);
        }else if (comando.toLowerCase().equals("qual o próximo ônibus")){
            Intent intent = new Intent(this, PegaLinha2Activity.class);
            this.startActivity(intent);
        }else if (comando.toLowerCase().equals("avise meu ponto")){
            Intent intent = new Intent(this, PontoActivity.class);
            this.startActivity(intent);
        }else if (comando.toLowerCase().equals("onde estou passando")){
            Intent intent = new Intent(this, RuasActivity.class);
            this.startActivity(intent);
        }else if (comando.toLowerCase().equals("detalhes da linha")){
            Intent intent = new Intent(this, BuscaLinhaActivity.class);
            this.startActivity(intent);
        }else if (comando.toLowerCase().equals("desligar comandos de voz") || comando.toLowerCase().equals("desligar comando de voz") || comando.toLowerCase().equals("desativar comando de voz") || comando.toLowerCase().equals("desativar comandos de voz")){
            confgComandosVoz(false);
        }else if (comando.toLowerCase().equals("ligar comandos de voz") || comando.toLowerCase().equals("ligar comando de voz") || comando.toLowerCase().equals("ativar comandos de voz") || comando.toLowerCase().equals("ativar comando de voz")){
            confgComandosVoz(true);
        }else if (comando.toLowerCase().equals("tutorial")){
            Intent intent = new Intent(this, Tutorial2.class);
            this.startActivity(intent);
        }else if (comando.toLowerCase().equals("termos de uso")){
            Intent intent = new Intent(this, termos.class);
            this.startActivity(intent);
        }else if (comando.toLowerCase().equals("sair")){
            finish();
        }else{
            ttsManager.initQueue("Nenhum comando Identificado.");
            Toast.makeText(this, comando, Toast.LENGTH_LONG).show();
        }
    }



    public void confgComandosVoz(boolean ativado){
        SharedPreferences sharedPref = this.getSharedPreferences( GRUPO_SALVO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if(ativado){
            editor.putString(VOZ, "1");
            bots.setVisibility(View.VISIBLE);
            comandVoz = true;
            ttsManager = new TTSManager();
            ttsManager.init(this);
        }else{
            editor.putString(VOZ, "0");
            bots.setVisibility(View.INVISIBLE);
            comandVoz = false;
        }
        editor.apply();

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.icones, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.miCompose) {


            String[] itens = new String[2];
            itens[0] = "Ativado";
            itens[1] = "Desativado";

            AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
            builder.setTitle("Comandos de Voz")
                    .setItems(itens, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if(which == 0){
                                confgComandosVoz(true);
                            }else{
                                confgComandosVoz(false);
                            }
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();

            //Toast.makeText(getApplicationContext(), "Primeiro", Toast.LENGTH_SHORT).show();
            return true;
        }else if (id == R.id.miTut){
            Intent intent = new Intent(this, Tutorial2.class);
            this.startActivity(intent);
        } if (id == R.id.miTermos) {
            Intent intent = new Intent(this, termos.class);
            this.startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}