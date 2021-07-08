package br.com.virtualartsa.onibusemponto;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PegaLinhaActivity extends AppCompatActivity {

    Context context;

    Timer timer;
    TimerTask timerTask;
    final Handler handler = new Handler();

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    public final static String GRUPO_SALVO = "br.com.virtualartsa.onibusemponto.GRUPO_SALVO";
    public final static String VOZ = "br.com.virtualartsa.onibusemponto.VOZ";
    public final static String LATLNG = "br.com.virtualartsa.onibusemponto.LATLNG";
    public LinearLayout bots;
    public boolean isVoice;

    TTSManagerMsg ttsManager = null;

    PegaLinhaApiRespostaServidor resposta = new PegaLinhaApiRespostaServidor();
    RuasApiRespostaServidor resposta2 = new RuasApiRespostaServidor();
    PontoApiRepostaServidor resposta3 = new PontoApiRepostaServidor();

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    PegaLinhaListaRecyclerAdapter adapter;
    private List<PegaLinhaListaItem> linhasListas = new ArrayList<>();

    ImageView load;
    MediaPlayer mediaPlayer;

    EditText edtEnd;
    TextView txtEnd;
    ImageButton btnEnd;
    boolean txtIsVisible;

    private LocationCallback mLocationCallback;
    LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pega_linha);

        context = this;

        edtEnd = (EditText)findViewById(R.id.edtEnd);
        txtEnd = (TextView)findViewById(R.id.txtEnd);
        btnEnd = (ImageButton)findViewById(R.id.btnEnd);
        txtIsVisible = true;

        bots = (LinearLayout)findViewById(R.id.layBotsPL);
        SharedPreferences sharedPref = this.getSharedPreferences( GRUPO_SALVO, Context.MODE_PRIVATE);
        String tutorialVisto = sharedPref.getString(VOZ, "0");
        if(tutorialVisto.equals("0")){
            bots.setVisibility(View.INVISIBLE);
            isVoice = false;
        }else{
            isVoice = true;
        }
        if(isVoice) {
            ttsManager = new TTSManagerMsg();
            //ttsManager.init(this, "Buscando linhas que passam por aqui");
            ttsManager.init(this, "");
        }

        mediaPlayer = MediaPlayer.create(PegaLinhaActivity.this, R.raw.load);
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(0.1f,0.1f);
        startTimer();
        load = (ImageView)findViewById(R.id.imgCarregandoPL);

        //retrofitPegarOnibus("-23.498888,-46.594659");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(getApplicationContext(), "Sem permissão", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }else{
            rodar();
        }

        //retrofitPegarLinhas();
        setaRecyclerView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(isVoice) {
            ttsManager.shutDown();
        }
        mediaPlayer.stop();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(isVoice) {
            ttsManager.stop();
        }
        mediaPlayer.stop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isVoice && linhasListas.size() > 0){
            //ttsManager.addQueue("Escolha uma das linhas que passam por aqui.");
        }
    }

    public void rodar(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {

                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {

                                //Toast.makeText(getApplicationContext(), String.valueOf(location.getLatitude()+","+location.getLongitude()), Toast.LENGTH_SHORT).show();
                                retrofitPegarLinhas(location.getLatitude()+","+location.getLongitude());
                                retrofitPegarEnd(location.getLatitude()+","+location.getLongitude());

                                SharedPreferences sharedPref = context.getSharedPreferences( GRUPO_SALVO, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString(LATLNG, location.getLatitude()+","+location.getLongitude());
                                editor.apply();


                                //coord = location.getLatitude()+","+location.getLongitude();
                            } else {
                                //Toast.makeText(getApplicationContext(), "Location = null", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public void setaRecyclerView(){
        //Aqui é instanciado o Recyclerview
        mRecyclerView = (RecyclerView) findViewById(R.id.rcv_pega_linha);
        //mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false); // Fazer a Lista virar Horizontal
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        adapter = new PegaLinhaListaRecyclerAdapter(this, linhasListas);
        mRecyclerView.setAdapter(adapter);

        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    public void retrofitPegarLinhas(String coord) {
        //Img Carregar
        load.setVisibility(View.VISIBLE);

        apiRetrofitService service = apiServiceGenerator.createService(apiRetrofitService.class);
        Call<PegaLinhaApiRespostaServidor> call = service.pegarLinhas(coord);
        //"-23.498888,-46.594659"

        call.enqueue(new Callback<PegaLinhaApiRespostaServidor>() {
            @Override
            public void onResponse(Call<PegaLinhaApiRespostaServidor> call, Response<PegaLinhaApiRespostaServidor> response) {

                //
                load.setVisibility(View.INVISIBLE);
                stoptimertask();
                mediaPlayer.stop();

                if (response.isSuccessful()) {
                    PegaLinhaApiRespostaServidor respostaServidor = response.body();
                    //verifica aqui se o corpo da resposta não é nulo
                    if (respostaServidor != null) {
                        resposta.setLinhas(respostaServidor.getLinhas());

                        if(resposta.getLinhas().get(0).getNumero().equals("0000")){
                            Toast.makeText(getApplicationContext(), "Nenhuma linha encontrada nesse local.", Toast.LENGTH_SHORT).show();
                            if(isVoice) {
                                ttsManager.addQueue("Nenhuma linha encontrada nesse local.");
                            }
                        }else if(resposta.getLinhas().get(0).getNumero().equals("0001")){
                            Toast.makeText(getApplicationContext(), "Servidores da SPTrans não responderam. Tente novamente mais tarde.", Toast.LENGTH_SHORT).show();
                            if(isVoice) {
                                ttsManager.addQueue("Desculpe, Servidores da S P Trans não responderam. Tente novamente mais tarde.");
                            }
                        }else{
                            preencherLista(resposta.getLinhas());
                        }

                    } else {
                        Toast.makeText(getApplicationContext(),"Resposta nula do servidor", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),"Resposta não foi sucesso", Toast.LENGTH_SHORT).show();
                    // segura os erros de requisição
                    ResponseBody errorBody = response.errorBody();
                }
                //progress.dismiss();
            }
            @Override
            public void onFailure(Call<PegaLinhaApiRespostaServidor> call, Throwable t) {
                Toast.makeText(getApplicationContext(),"Erro na chamada ao servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void retrofitPegarEnd(String latlgn) {
        apiRetrofitService service = apiServiceGenerator.createService(apiRetrofitService.class);
        Call<RuasApiRespostaServidor> call = service.pegarEnd(latlgn);

        //final String name = nome;
        //final String num = numLinha;

        call.enqueue(new Callback<RuasApiRespostaServidor>() {
            @Override
            public void onResponse(Call<RuasApiRespostaServidor> call, Response<RuasApiRespostaServidor> response) {

                if (response.isSuccessful()) {
                    RuasApiRespostaServidor respostaServidor = response.body();
                    //verifica aqui se o corpo da resposta não é nulo
                    if (respostaServidor != null) {
                        resposta2.setNum(respostaServidor.getNum());
                        resposta2.setRua(respostaServidor.getRua());

                        if(resposta2.getNum().equals("-1")){
                            Toast.makeText(getApplicationContext(), "Endereço não encontrado", Toast.LENGTH_SHORT).show();
                            //adicionarSemOnibus(name, num);
                            if (isVoice) {
                                ttsManager.addQueue("Endereço não encontrado");
                            }
                        }else{
                            //Toast.makeText(getApplicationContext(), respostaIt.getIda().get(0).getRua(), Toast.LENGTH_SHORT).show();

                            //preencherLista(resposta.getIda());

                            //preencherLista(resposta.getLinhas());

                            //Toast.makeText(getApplicationContext(), resposta.getStatus(), Toast.LENGTH_SHORT).show();
                            //Toast.makeText(getApplicationContext(), String.valueOf(resposta.getProximoOnibus().getPrefixo()), Toast.LENGTH_SHORT).show();

                            //adicionarListaOrdem(resposta.getProximoOnibus(), name, num);
                            if (isVoice) {
                                ttsManager.addQueue("Buscando ônibus que passam por : " + resposta2.getRua() + ", número " + resposta2.getNum());
                                ttsManager.addQueue("Para buscar linhas de outro lugar diga: \n Buscar; em \n Mais o local desejado.");
                            }

                            txtEnd.setText(resposta2.getRua() + ", " +  resposta2.getNum());
                            edtEnd.setHint(resposta2.getRua() + ", " +  resposta2.getNum());

                            //preencherLista(teste);
                            //Toast.makeText(getApplicationContext(), resposta.getIda(), Toast.LENGTH_SHORT).show();
                            //resposta.setVolta(respostaServidor.getVolta());
                            //resposta.setSemana(respostaServidor.getSemana());
                            //resposta.setSabado(respostaServidor.getSabado());
                            //resposta.setDomingo(respostaServidor.getDomingo());

                            //preencherInfo(resposta);
                        }

                    } else {
                        Toast.makeText(getApplicationContext(),"Resposta nula do servidor", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),"Resposta não foi sucesso", Toast.LENGTH_SHORT).show();
                    // segura os erros de requisição
                    ResponseBody errorBody = response.errorBody();
                }
                //progress.dismiss();
            }
            @Override
            public void onFailure(Call<RuasApiRespostaServidor> call, Throwable t) {
                Toast.makeText(getApplicationContext(),"Erro na chamada ao servidor - " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void retrofitPegarEndComp(String ender) {

        apiRetrofitService service = apiServiceGenerator.createService(apiRetrofitService.class);
        Call<PontoApiRepostaServidor> call = service.pegarEndCompl(ender);

        //final String name = nome;
        //final String num = numLinha;

        call.enqueue(new Callback<PontoApiRepostaServidor>() {
            @Override
            public void onResponse(Call<PontoApiRepostaServidor> call, Response<PontoApiRepostaServidor> response) {

                if (response.isSuccessful()) {
                    PontoApiRepostaServidor respostaServidor = response.body();
                    //verifica aqui se o corpo da resposta não é nulo
                    if (respostaServidor != null) {
                        resposta3.setNum(respostaServidor.getNum());
                        resposta3.setRua(respostaServidor.getRua());
                        resposta3.setLat(respostaServidor.getLat());
                        resposta3.setLng(respostaServidor.getLng());

                        if(resposta3.getNum().equals("-1")){
                            Toast.makeText(getApplicationContext(), "Endereço não encontrado", Toast.LENGTH_SHORT).show();
                            if (isVoice) {
                                ttsManager.addQueue("Endereço não encontrado");
                            }
                        }else{
                            //Toast.makeText(getApplicationContext(), respostaIt.getIda().get(0).getRua(), Toast.LENGTH_SHORT).show();

                            //preencherLista(resposta.getIda());

                            //preencherLista(resposta.getLinhas());

                            //Toast.makeText(getApplicationContext(), resposta.getStatus(), Toast.LENGTH_SHORT).show();
                            //Toast.makeText(getApplicationContext(), String.valueOf(resposta.getProximoOnibus().getPrefixo()), Toast.LENGTH_SHORT).show();

                            //adicionarListaOrdem(resposta.getProximoOnibus(), name, num);

                            if (isVoice) {
                                ttsManager.addQueue("Buscando ônibus que passam por : " + resposta3.getRua() + ", número " + resposta3.getNum());
                            }

                            txtEnd.setText(resposta3.getRua() + ", " +  resposta3.getNum());
                            retrofitPegarLinhas(resposta3.getLat() + "," + resposta3.getLng());
                            //Toast.makeText(getApplicationContext(), resposta3.getLat() + "," + resposta3.getLng(), Toast.LENGTH_SHORT).show();

                            SharedPreferences sharedPref = context.getSharedPreferences( GRUPO_SALVO, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString(LATLNG, resposta3.getLat() + "," + resposta3.getLng());
                            editor.apply();


                            //preencherLista(teste);
                            //Toast.makeText(getApplicationContext(), resposta.getIda(), Toast.LENGTH_SHORT).show();
                            //resposta.setVolta(respostaServidor.getVolta());
                            //resposta.setSemana(respostaServidor.getSemana());
                            //resposta.setSabado(respostaServidor.getSabado());
                            //resposta.setDomingo(respostaServidor.getDomingo());

                            //preencherInfo(resposta);
                        }

                    } else {
                        Toast.makeText(getApplicationContext(),"Resposta nula do servidor", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),"Resposta não foi sucesso", Toast.LENGTH_SHORT).show();
                    // segura os erros de requisição
                    ResponseBody errorBody = response.errorBody();
                }
                //progress.dismiss();
            }
            @Override
            public void onFailure(Call<PontoApiRepostaServidor> call, Throwable t) {
                Toast.makeText(getApplicationContext(),"Erro na chamada ao servidor - " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void preencherLista(List<apiPegaLinha> linhas){
        String onibus = "";
        linhasListas.clear();
        for (int i=0; i<linhas.size(); i++){
            PegaLinhaListaItem linha = new PegaLinhaListaItem();
            linha.setNumero(linhas.get(i).getNumero());
            linha.setNome(linhas.get(i).getNome());
            linha.setSentido(linhas.get(i).getSentido());
            linha.setOperacao(linhas.get(i).getOperacao());
            linhasListas.add(linha);
            onibus += linha.getNumero() + " " + linha.getOperacao() + ", " + linha.getNome() + "\n ";
            onibus = subString.trocarString(onibus);
        }
        adapter.notifyDataSetChanged();
        if(isVoice) {
            ttsManager.addQueue("Escolha uma das linhas que passam por aqui. " + onibus);
        }
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Diga a linha");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

        }
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

    public void reconhecerComando(String comando) {
        if(comando.toLowerCase().equals("voltar")){
            finish();
        }else if(comando.toLowerCase().contains("buscar em ")){
            buscarEm(comando);
        }else{
            reconhecerLinha(comando);
        }
    }

    public void buscarEm(String comando) {
        txtEnd.setVisibility(View.VISIBLE);
        edtEnd.setVisibility(View.GONE);
        txtIsVisible = true;
        btnEnd.setImageResource(R.drawable.editar);
        txtEnd.setText("Buscando Endereço");
        retrofitPegarEndComp(comando.replace("buscar em ", ""));
    }


    public void reconhecerLinha(String comando){

        boolean encontrou = false;

        for (int i=0; i<linhasListas.size(); i++){

            String comp = linhasListas.get(i).getNumero() + linhasListas.get(i).getOperacao() + linhasListas.get(i).getNome();

            /*String numero = linhasListas.get(i).getNumero() + linhasListas.get(i).getOperacao();
            numero = trocarString2(numero);
            numero = trocarString(numero);
            numero = numero.toLowerCase();
            String nomeLinhas = linhasListas.get(i).getNome();
            nomeLinhas = trocarString2(nomeLinhas);
            nomeLinhas = trocarString(nomeLinhas);
            nomeLinhas = nomeLinhas.toLowerCase();
            nomeLinhas = removerAcentos(nomeLinhas);*/

            /*
            comp = trocarString2(comp);
            comp = trocarString(comp);
            comp = comp.toLowerCase();
            comp = removerAcentos(comp);
            comando = trocarString2(comando);
            comando = comando.toLowerCase();
            comando = removerAcentos(comando);
            */
            comp = subString.formatarString(comp);
            comando = subString.formatarString(comando);

            //Log.e("error", comando);
            //String nomeComando = comando.split(numero)[1];
            //Log.e("error", nomeLinhas + " - " + nomeComando + " - " + nomeLinhas.contains(nomeComando));

            Log.e("error", comp + " - " + comando + " - " + comp.contains(comando));
            if (comp.contains(comando)){
                encontrou = true;
                Intent intent = new Intent(this, MeuOnibusActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("linha", linhasListas.get(i).getNumero());
                bundle.putString("sentido", String.valueOf(linhasListas.get(i).getSentido()));
                bundle.putString("operacao", String.valueOf(linhasListas.get(i).getOperacao()));
                bundle.putString("nome", String.valueOf(linhasListas.get(i).getNome()));
                intent.putExtras(bundle);
                this.startActivity(intent);
                if(isVoice){
                    finish();
                }
                break;
            }
        }
        if(!encontrou){
            if(isVoice) {
                ttsManager.initQueue("Nenhuma linha Identificada.");
            }
            Toast.makeText(getApplicationContext(), comando , Toast.LENGTH_SHORT).show();
        }
    }

    public void btnDireita(View view){
        if(isVoice) {
            ttsManager.stop();
            startVoiceInput();
        }
        //ttsManager.initQueue("Nenhuma linha Identificada.");
    }

    public void btnEnd(View view){
        if(txtIsVisible){
            txtEnd.setVisibility(View.GONE);
            edtEnd.setVisibility(View.VISIBLE);
            txtIsVisible = false;
            btnEnd.setImageResource(R.drawable.certo);
            //btnEnd.setText("V");
        }else{
            txtEnd.setVisibility(View.VISIBLE);
            edtEnd.setVisibility(View.GONE);
            txtIsVisible = true;
            //btnEnd.setText("E");
            btnEnd.setImageResource(R.drawable.editar);
            txtEnd.setText("Buscando Endereço");
            if(edtEnd.getText().toString().length() > 0) {
                retrofitPegarEndComp(edtEnd.getText().toString());
            }
        }
    }




    public void startTimer() {
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 3000, 2000); //
    }
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {

                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {

                        if(!isVoice || (isVoice && !ttsManager.speaking())){
                            stoptimertask();
                            mediaPlayer.start(); // no need to call prepare(); create() does that for you

                        }

                    }
                });
            }
        };
    }

}
