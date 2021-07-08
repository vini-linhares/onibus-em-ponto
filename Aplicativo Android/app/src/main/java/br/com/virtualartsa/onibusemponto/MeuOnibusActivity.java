package br.com.virtualartsa.onibusemponto;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MeuOnibusActivity extends AppCompatActivity {

    Context context;

    Call<MeuOnibusApiRespostaServidor> call;

    Timer timer;
    TimerTask timerTask;
    final Handler handler = new Handler();

    Timer timer2;
    TimerTask timerTask2;
    final Handler handler2 = new Handler();

    subString subs;

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    public final static String GRUPO_SALVO = "br.com.virtualartsa.onibusemponto.GRUPO_SALVO";
    public final static String VOZ = "br.com.virtualartsa.onibusemponto.VOZ";
    public final static String PONTOID = "br.com.virtualartsa.onibusemponto.PONTOID";
    public final static String PONTOEND = "br.com.virtualartsa.onibusemponto.PONTOEND";
    public LinearLayout bots;
    public boolean isVoice;

    TTSManagerMsg ttsManager = null;

    MeuOnibusApiRespostaServidor resposta = new MeuOnibusApiRespostaServidor();
    String numLinha;
    String sentLinha;
    String nometLinha;
    String optLinha;

    public LinearLayout lnlMensagem;
    public TextView txtMensagem;
    ProgressBar prgBarMensagem;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    MeuOnibusListaRecyclerAdapter adapter;
    private List<MeuOnibusListaItem> onibusListas = new ArrayList<>();
    SwipeRefreshLayout mSwipeRefreshLayout;

    MediaPlayer mediaPlayer;

    TextView txtLinha;
    TextView txtEndPonto;

    String pontoId;
    boolean primeiraVez;
    //int dist;
    String distT;

    Vibrator vib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meu_onibus);

        context = this;
        vib = (Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE);

        txtMensagem = (TextView)findViewById(R.id.txtMensagem);
        lnlMensagem = (LinearLayout)findViewById(R.id.lnlMensagem);
        prgBarMensagem = (ProgressBar) findViewById(R.id.prgBarMensagem);
        prgBarMensagem.getIndeterminateDrawable().setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.MULTIPLY);

        primeiraVez = true;
        distT = "";

        bots = (LinearLayout)findViewById(R.id.layBotsMO);
        SharedPreferences sharedPref = this.getSharedPreferences( GRUPO_SALVO, Context.MODE_PRIVATE);
        String tutorialVisto = sharedPref.getString(VOZ, "0");
        if(tutorialVisto.equals("0")){
            bots.setVisibility(View.INVISIBLE);
            isVoice = false;
        }else{
            isVoice = true;
        }

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        numLinha = bundle.getString("linha");
        sentLinha = bundle.getString("sentido");
        nometLinha = bundle.getString("nome");
        optLinha = bundle.getString("operacao");

        txtLinha = (TextView) findViewById(R.id.txtLinha);
        txtLinha.setText(numLinha+"-"+optLinha+"   "+nometLinha);
        txtEndPonto = (TextView)findViewById(R.id.txtEndPonto);

        subs = new subString();

        if(isVoice) {
            ttsManager = new TTSManagerMsg();
            ttsManager.init(this, "Buscando ônibus da linha " + numLinha + " " + optLinha + " no sentido " + subString.trocarString(nometLinha));
        }

        mediaPlayer = MediaPlayer.create(this, R.raw.load);
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(0.1f,0.1f);

        sharedPref = context.getSharedPreferences( GRUPO_SALVO, Context.MODE_PRIVATE);
        pontoId = sharedPref.getString(PONTOID, "0,0");
        String pontoEnd = sharedPref.getString(PONTOEND, "");
        txtEndPonto.setText(pontoEnd);

        setaRecyclerView();
        retrofitPegarOnibus();
        //Toast.makeText(this, numLinha, Toast.LENGTH_SHORT).show();
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
        stoptimertask();
        stoptimertask2();
        finalizaChamadas();
    }

    @Override
    public void onResume() {
        super.onResume();
        //ttsManager.shutDown();
    }

    public void setaRecyclerView(){
        //Aqui é instanciado o Recyclerview
        mRecyclerView = (RecyclerView) findViewById(R.id.rcv_meu_onibus);
        //mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false); // Fazer a Lista virar Horizontal
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        adapter = new MeuOnibusListaRecyclerAdapter(this, onibusListas);
        mRecyclerView.setAdapter(adapter);

        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
            }
        });
    }

    void refreshItems() {
            finalizaChamadas();
            defAtualizandoItens();
            stoptimertask2();
            retrofitPegarOnibus();
        //onItemsLoadComplete();
    }

    public void retrofitPegarOnibus() {
        //Img Carregar
        mSwipeRefreshLayout.setRefreshing(true);
        if(isVoice) {
            startTimer();
        }

        apiRetrofitService service = apiServiceGenerator.createService(apiRetrofitService.class);
        call = service.pegarProximoOnibus(numLinha+"-"+optLinha, sentLinha, pontoId);

        call.enqueue(new Callback<MeuOnibusApiRespostaServidor>() {
            @Override
            public void onResponse(Call<MeuOnibusApiRespostaServidor> call, Response<MeuOnibusApiRespostaServidor> response) {

                mSwipeRefreshLayout.setRefreshing(false);
                //
                mediaPlayer.pause();
                stoptimertask();

                if (response.isSuccessful()) {
                    MeuOnibusApiRespostaServidor respostaServidor = response.body();
                    //verifica aqui se o corpo da resposta não é nulo
                    if (respostaServidor != null) {

                        lnlMensagem.setVisibility(View.GONE);

                        resposta.setStatus(respostaServidor.getStatus());
                        resposta.setMensagem(respostaServidor.getMensagem());
                        resposta.setProximoOnibus(respostaServidor.getProximoOnibus());

                        if(resposta.getStatus().equals("erro")){
                            lnlMensagem.setVisibility(View.GONE);
                            if(primeiraVez) {
                                //Toast.makeText(getApplicationContext(), "Nenhum ônibus encontrado nesse sentido no momento.", Toast.LENGTH_SHORT).show();
                                adicionarSemOnibus();
                            }
                        }else if(resposta.getStatus().equals("erro1")){
                            msgErroServidor("Servidores da SPTrans não responderam.", "#660000");
                        }else if(resposta.getStatus().equals("erro2")){
                            //Toast.makeText(getApplicationContext(), "Nenhum ônibus encontrado nesse sentido no momento.", Toast.LENGTH_SHORT).show();
                            lnlMensagem.setVisibility(View.GONE);
                            if (resposta.getProximoOnibus().get(0).getDistanciaTexto().equals("-2:00")){
                                adicionarSemOnibus();
                            }else {
                                adicionarSemOnibusProximaSaida(resposta.getProximoOnibus().get(0));
                            }
                        }else{
                            lnlMensagem.setVisibility(View.GONE);
                            //Toast.makeText(getApplicationContext(), respostaIt.getIda().get(0).getRua(), Toast.LENGTH_SHORT).show();

                            //preencherLista(resposta.getIda());

                            //preencherLista(resposta.getLinhas());

                            //Toast.makeText(getApplicationContext(), resposta.getStatus(), Toast.LENGTH_SHORT).show();

                            preencherLista(resposta.getProximoOnibus());

                            //preencherLista(teste);
                            //Toast.makeText(getApplicationContext(), resposta.getIda(), Toast.LENGTH_SHORT).show();
                            //resposta.setVolta(respostaServidor.getVolta());
                            //resposta.setSemana(respostaServidor.getSemana());
                            //resposta.setSabado(respostaServidor.getSabado());
                            //resposta.setDomingo(respostaServidor.getDomingo());

                            //preencherInfo(resposta);
                        }
                        if(primeiraVez){
                            primeiraVez = false;
                        }
                    } else {
                        //Toast.makeText(getApplicationContext(),"Resposta nula do servidor", Toast.LENGTH_SHORT).show();
                        msgErroServidor("Ops! Há um problema nos servidores");
                        call.cancel();
                        retrofitPegarOnibus();
                    }
                } else {
                    //Toast.makeText(getApplicationContext(),"Resposta não foi sucesso", Toast.LENGTH_SHORT).show();
                    // segura os erros de requisição
                    msgErroServidor("Ops! Há um problema nos servidores");
                    call.cancel();
                    retrofitPegarOnibus();
                    ResponseBody errorBody = response.errorBody();
                }
                //progress.dismiss();
            }
            @Override
            public void onFailure(Call<MeuOnibusApiRespostaServidor> call, Throwable t) {
                //Toast.makeText(getApplicationContext(),"Erro na chamada ao servidor - " + t.getMessage(), Toast.LENGTH_SHORT).show();
                if(!t.getMessage().equals("Canceled")) {
                    msgErroServidor("Ops! Há um problema nos servidores");
                    call.cancel();
                    retrofitPegarOnibus();
                }
            }
        });
    }

    public void msgErroServidor(String msg){
        msgErroServidor(msg, "#444444");
    }
    public void msgErroServidor(String msg, String cor){
        lnlMensagem.setBackgroundColor(Color.parseColor(cor));
        lnlMensagem.setVisibility(View.VISIBLE);
        txtMensagem.setText(msg);
    }

    public void finalizaChamadas(){
        if(call != null){
            call.cancel();
        }
    }

    public void preencherLista(List<apiMeuOnibus> onibus){
        String proxOnibusRua = "";
        String proxOnibusDist = "";
        String demaisOnibus = "";
        onibusListas.clear();
        for (int i=0; i<onibus.size(); i++){
            if(i == 0){
                proxOnibusRua = subString.trocarString(onibus.get(i).getRua()) + ", " + onibus.get(i).getNumero();
                proxOnibusDist = onibus.get(i).getDistanciaTexto();
            }else if(onibus.size() == 2){
                demaisOnibus = onibus.get(i).getDistanciaTexto() + " na " + subString.trocarString(onibus.get(i).getRua()) + ", " + onibus.get(i).getNumero();
            }else if(onibus.size() > 2){
                if(i < 3) {
                    demaisOnibus += onibus.get(i).getDistanciaTexto() + " na " + subString.trocarString(onibus.get(i).getRua()) + ", " + onibus.get(i).getNumero();
                    if (i != 2) {
                        demaisOnibus += ". E a ";
                    }
                }
            }
            MeuOnibusListaItem bus = new MeuOnibusListaItem();
            bus.setDistanciaTexto(onibus.get(i).getDistanciaTexto());
            bus.setDistanciaValor(onibus.get(i).getDistanciaValor());
            bus.setEnd(onibus.get(i).getRua() + ", " + onibus.get(i).getNumero());
            onibusListas.add(bus);
        }
        adapter.notifyDataSetChanged();
        startTimer2();
        if(isVoice) {
            if(!proxOnibusDist.equals(distT)) {
                if(onibus.get(0).getDistanciaValor() <= 600){
                    vib.vibrate(150);
                    ttsManager.addQueue("Atenção, o ônibus está próximo do ponto, a " + proxOnibusDist + " na " + proxOnibusRua);
                }else {
                    ttsManager.addQueue("O próximo ônibus está a " + proxOnibusDist + " na " + proxOnibusRua);
                }
            }
            if (primeiraVez && onibus.size() == 2) {
                ttsManager.addQueue("Encontramos mais um ônibus que está a " + demaisOnibus);
            } else if (primeiraVez && onibus.size() > 2) {
                ttsManager.addQueue("Encontramos mais " + (onibus.size() - 1) + " ônibus. Os seguintes estão a " + demaisOnibus);
            }
            distT = proxOnibusDist;
        }
    }

    public void adicionarSemOnibus(){
        onibusListas.clear();
        MeuOnibusListaItem bus = new MeuOnibusListaItem();
        bus.setDistanciaTexto("");
        bus.setDistanciaValor(-1);
        bus.setEnd("Nenhum ônibus encontrado na linha nesse sentido no momento.");
        bus.setAtualizando(false);
        onibusListas.add(bus);
        adapter.notifyDataSetChanged();
        startTimer2();
        if (isVoice) {
            ttsManager.addQueue("Nenhum ônibus encontrado na linha " + numLinha + " " + optLinha + " em sentido a " + subString.trocarString(nometLinha) + " no momento");
        }
    }

    public void adicionarSemOnibusProximaSaida(apiMeuOnibus onibus){
        //onibusListas.clear();
        //
        //Criar Classe para a lista do proximo onibus, passar o nome junto para exibur na lista.
        //
        onibusListas.clear();
        MeuOnibusListaItem bus = new MeuOnibusListaItem();
        bus.setDistanciaTexto("");
        bus.setDistanciaValor(-2);
        String msg = "Ônibus no terminal.<br/>Próxima saída prevista: <b>" + onibus.getDistanciaTexto() + "</b>";
        bus.setEnd(Html.fromHtml(msg).toString());
        bus.setDistanciaTexto("");
        bus.setAtualizando(false);
        onibusListas.add(bus);
        adapter.notifyDataSetChanged();
        startTimer2();
        if (isVoice) {
            ttsManager.addQueue("Ônibus da linha " + numLinha + " " + optLinha + " em sentido a " + subString.trocarString(nometLinha) + "  no terminal. Próxima saída prevista para" + formataHora(onibus.getDistanciaTexto()));
        }
    }

    public String formataHora(String string) {
        String hor = "";
        String[] hExata = string.split(":");
        hor += Integer.parseInt(hExata[0]);
        if(hExata[1].equals("00")){
            hor += " horas";
        }else if(hExata[1].equals("30")){
            hor += " e meia";
        }else{
            hor += " e ";
            hor += Integer.parseInt(hExata[1]);;
        }
        return hor;
    }

    public void defAtualizandoItens(){
        int size = onibusListas.size();
        if(size > 0){
            for(int i =0; i<size; i++){
                onibusListas.get(i).setAtualizando(true);
            }
            adapter.notifyDataSetChanged();
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
        }else{
            if(isVoice) {
                ttsManager.initQueue("Nenhum comando identificado. \n Se Quiser retornar diga o comando Voltar.");
            }
        }
    }


    public void btnDireita(View view){
        if(isVoice) {
            ttsManager.stop();
            startVoiceInput();
        }
        //ttsManager.initQueue("Nenhuma linha Identificada.");
    }

    public void startTimer() {
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 100, 1000); //
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

                        if(!ttsManager.speaking()){
                            stoptimertask();
                            mediaPlayer.start(); // no need to call prepare(); create() does that for you
                        }

                    }
                });
            }
        };
    }

    public void startTimer2() {
        //set a new Timer
        timer2 = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask2();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer2.schedule(timerTask2, 30000, 30000); //
    }

    public void stoptimertask2() {
        //stop the timer, if it's not already null
        if (timer2 != null) {
            timer2.cancel();
            timer2 = null;
        }
    }

    public void initializeTimerTask2() {
        timerTask2 = new TimerTask() {
            public void run() {
                //use a handler to run a toast that shows the current timestamp
                handler2.post(new Runnable() {
                    public void run() {
                        stoptimertask2();
                        defAtualizandoItens();
                        retrofitPegarOnibus();
                    }
                });
            }
        };
    }

}
