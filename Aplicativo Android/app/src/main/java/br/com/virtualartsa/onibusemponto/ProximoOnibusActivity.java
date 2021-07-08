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
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProximoOnibusActivity extends AppCompatActivity {

    Context context;

    Call<ProximoOnibusApiRespostaServidor> call;

    Timer timer;
    TimerTask timerTask;
    final Handler handler = new Handler();

    Timer timer2;
    TimerTask timerTask2;
    final Handler handler2 = new Handler();

    Timer timer3;
    TimerTask timerTask3;
    final Handler handler3 = new Handler();

    subString subs;

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    public final static String GRUPO_SALVO = "br.com.virtualartsa.onibusemponto.GRUPO_SALVO";
    public final static String VOZ = "br.com.virtualartsa.onibusemponto.VOZ";
    public final static String PONTOID = "br.com.virtualartsa.onibusemponto.PONTOID";
    public final static String PONTOEND = "br.com.virtualartsa.onibusemponto.PONTOEND";
    public LinearLayout bots;
    public boolean isVoice;

    TTSManagerMsg ttsManager = null;

    int quant;

    List<String[]> info = new ArrayList<String[]>(); ;

    ProximoOnibusApiRespostaServidor resposta = new ProximoOnibusApiRespostaServidor();

    public LinearLayout lnlMensagem;
    public TextView txtMensagem;
    ProgressBar prgBarMensagem;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    ProximoOnibusListaRecyclerAdapter adapter;
    private List<ProximoOnibusListaItem> onibusListas = new ArrayList<>();
    SwipeRefreshLayout mSwipeRefreshLayout;
    ItemTouchHelper itemTouchHelper;

    MediaPlayer mediaPlayer;

    Intent intent;
    Bundle bundle;

    String pontoId;

    int icont;

    boolean atualizando = false;
    int quantAtualizando = 0;
    boolean listaPreenchida = false;


    TextView txtEndPonto;

    Vibrator vib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proximo_onibus);

        context = this;
        vib = (Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE);

        txtMensagem = (TextView)findViewById(R.id.txtMensagem);
        lnlMensagem = (LinearLayout)findViewById(R.id.lnlMensagem);
        prgBarMensagem = (ProgressBar) findViewById(R.id.prgBarMensagem);
        prgBarMensagem.getIndeterminateDrawable().setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.MULTIPLY);

        txtEndPonto = (TextView)findViewById(R.id.txtEndPonto);

        bots = (LinearLayout)findViewById(R.id.layBotsPO);
        SharedPreferences sharedPref = this.getSharedPreferences( GRUPO_SALVO, Context.MODE_PRIVATE);
        String tutorialVisto = sharedPref.getString(VOZ, "0");
        if(tutorialVisto.equals("0")){
            isVoice = false;
            bots.setVisibility(View.INVISIBLE);
        }else{
            isVoice = true;
        }
        subs = new subString();

        if(isVoice) {
            ttsManager = new TTSManagerMsg();
            ttsManager.init(this, "Localizando próximos ônibus");
        }

        mediaPlayer = MediaPlayer.create(this, R.raw.load);
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(0.1f,0.1f);

        sharedPref = context.getSharedPreferences( GRUPO_SALVO, Context.MODE_PRIVATE);
        pontoId = sharedPref.getString(PONTOID, "0,0");
        String pontoEnd = sharedPref.getString(PONTOEND, "");
        txtEndPonto.setText(pontoEnd);

        setaRecyclerView();

        intent = getIntent();
        bundle = intent.getExtras();

        quant = Integer.parseInt(bundle.getString("quant"));
        icont = 0;
        mSwipeRefreshLayout.setRefreshing(true);
        buscarOnibus();
        startTimer2();
    }

    public void buscarOnibus(){
        for(int i = 0; i<quant; i++){
            String[] item = new String[3];
            item[0] = bundle.getString("numero"+i);
            item[1] = bundle.getString("sentido"+i);
            item[2] = bundle.getString("nome"+i);
            info.add(item);
        }
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
        finalizaChamadas();
        stoptimertask();
        stoptimertask2();
        stoptimertask3();
    }

    public void setaRecyclerView(){
        //Aqui é instanciado o Recyclerview
        mRecyclerView = (RecyclerView) findViewById(R.id.rcv_proximo_onibus);
        //mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false); // Fazer a Lista virar Horizontal
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        adapter = new ProximoOnibusListaRecyclerAdapter(this, onibusListas);
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
        simpleItemTouchCallback.setDefaultSwipeDirs(0);
        itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    public void permiteArrastar(){
        //itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        //itemTouchHelper.attachToRecyclerView(mRecyclerView);
        simpleItemTouchCallback.setDefaultSwipeDirs(15);
    }
    public void despermiteArrastar(){
        //itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback2);
        //itemTouchHelper.attachToRecyclerView(mRecyclerView);
        simpleItemTouchCallback.setDefaultSwipeDirs(0);
    }

    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN | ItemTouchHelper.UP) {

        @Override
        public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            //if (viewHolder instanceof ProximoOnibusListaViewHolder) return 0;
            return super.getSwipeDirs(recyclerView, viewHolder);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            //Toast.makeText(ProximoOnibusActivity.this, "on Move", Toast.LENGTH_SHORT).show();
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            //Toast.makeText(ProximoOnibusActivity.this, "on Swiped ", Toast.LENGTH_SHORT).show();
            //Remove swiped item from list and notify the RecyclerView
            final int position = viewHolder.getAdapterPosition();


            for (int j = info.size() - 1; j >= 0; j--) {
                if (info.get(j)[0].equals(onibusListas.get(position).getNumero())) {
                    if (info.get(j)[2].equals(String.valueOf(onibusListas.get(position).getNome()))) {
                        info.remove(j);
                    }
                }
            }
            onibusListas.remove(position);
            //for (int i=position; i<info.size()-1; i++){
            //    info.set(i, info.get(i+1));
            //}
            //info.remove(info.size()-1);
            //Toast.makeText(ProximoOnibusActivity.this, String.valueOf(info.size()), Toast.LENGTH_SHORT).show();
            adapter.notifyItemRemoved(position);
            if(info.size() == 0){
                finish();
            }
        }
    };

    ItemTouchHelper.SimpleCallback simpleItemTouchCallback2 = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN | ItemTouchHelper.UP) {

        @Override
        public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            if (viewHolder instanceof ProximoOnibusListaViewHolder) return 0;
            return super.getSwipeDirs(recyclerView, viewHolder);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            //Toast.makeText(ProximoOnibusActivity.this, "on Move", Toast.LENGTH_SHORT).show();
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {

        }
    };

    void refreshItems() {
        if(!atualizando && listaPreenchida) {
            stoptimertask3();
            finalizaChamadas();
            atualizando = true;
            quantAtualizando = 0;
            defAtualizandoItens();
            startTimer2();
            despermiteArrastar();
        }
        //onItemsLoadComplete();
    }

    void onItemsLoadComplete() {
        // Update the adapter and notify data set changed
        // ...

        // Stop refresh animation
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void retrofitPegarOnibus(String numLinha, String sentLinha, String nome) {
        //Img Carregar

        apiRetrofitService service = apiServiceGenerator.createService(apiRetrofitService.class);
        call = service.pegarProximo(numLinha, sentLinha, pontoId);

        final String name = nome;
        final String num = numLinha;

        call.enqueue(new Callback<ProximoOnibusApiRespostaServidor>() {
            @Override
            public void onResponse(Call<ProximoOnibusApiRespostaServidor> call, Response<ProximoOnibusApiRespostaServidor> response) {
                if (response.isSuccessful()) {
                    ProximoOnibusApiRespostaServidor respostaServidor = response.body();
                    //verifica aqui se o corpo da resposta não é nulo
                    if (respostaServidor != null) {

                        //lnlMensagem.setVisibility(View.GONE);

                        resposta.setStatus(respostaServidor.getStatus());
                        resposta.setMensagem(respostaServidor.getMensagem());
                        resposta.setProximoOnibus(respostaServidor.getProximoOnibus());
                        //Toast.makeText(getApplicationContext(), resposta.getStatus() + " - " + name, Toast.LENGTH_SHORT).show();
                        if(resposta.getStatus().equals("erro")){
                            lnlMensagem.setVisibility(View.GONE);
                            //Toast.makeText(getApplicationContext(), "Nenhum ônibus encontrado nesse sentido no momento.", Toast.LENGTH_SHORT).show();
                            if(atualizando){
                                removerLista(resposta.getProximoOnibus(), name, num);
                            }
                            adicionarSemOnibus(name, num);
                        }else if(resposta.getStatus().equals("erro1")){
                            msgErroServidor("Servidores da SPTrans não responderam.", "#660000");
                            if(atualizando){
                                removerLista(resposta.getProximoOnibus(), name, num);
                            }
                            adicionarErro(name, num);
                        }else if(resposta.getStatus().equals("erro2")){
                            //Toast.makeText(getApplicationContext(),"erro2", Toast.LENGTH_SHORT).show();
                            lnlMensagem.setVisibility(View.GONE);
                            //Toast.makeText(getApplicationContext(), "Nenhum ônibus encontrado nesse sentido no momento.", Toast.LENGTH_SHORT).show();
                            if (resposta.getProximoOnibus().getDistanciaTexto().equals("-2:00")){
                                if(atualizando){
                                    removerLista(resposta.getProximoOnibus(), name, num);
                                }
                                adicionarSemOnibus(name, num);
                            }else {
                                if(atualizando){
                                    removerLista(resposta.getProximoOnibus(), name, num);
                                }
                                //Toast.makeText(getApplicationContext(),"Ad", Toast.LENGTH_SHORT).show();
                                adicionarSemOnibusProximaSaida(resposta.getProximoOnibus(), name, num);
                            }
                        }else{
                            lnlMensagem.setVisibility(View.GONE);
                            if(atualizando){
                                removerLista(resposta.getProximoOnibus(), name, num);
                            }
                            adicionarListaOrdem(resposta.getProximoOnibus(), name, num);
                        }
                    } else {
                        //Toast.makeText(getApplicationContext(),"Resposta nula do servidor", Toast.LENGTH_SHORT).show();
                        if(atualizando){
                            verifListaCompleta();
                        }else{
                            adicionarErro(name, num);
                            msgErroServidor("Ops! Há um problema nos servidores");
                        }
                    }
                } else {
                    //Toast.makeText(getApplicationContext(),"Resposta não foi sucesso", Toast.LENGTH_SHORT).show();
                    // segura os erros de requisição
                    if(atualizando){
                        verifListaCompleta();
                    }else{
                        adicionarErro(name, num);
                        msgErroServidor("Ops! Há um problema nos servidores");
                    }
                    ResponseBody errorBody = response.errorBody();
                }
                //progress.dismiss();
            }
            @Override
            public void onFailure(Call<ProximoOnibusApiRespostaServidor> call, Throwable t) {
                //Toast.makeText(getApplicationContext(),"Erro na chamada ao servidor - " + t.getMessage(), Toast.LENGTH_SHORT).show();
                if(!t.getMessage().equals("Canceled")) {
                    if (atualizando) {
                        verifListaCompleta();
                    } else {
                        adicionarErro(name, num);
                        msgErroServidor("Ops! Há um problema nos servidores");
                    }
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

    public void removerLista(apiMeuOnibus onibus, String nome, String numero){
        //onibusListas.clear();
        //
        //Criar Classe para a lista do proximo onibus, passar o nome junto para exibur na lista.
        //
        int size = onibusListas.size();
            for(int i =0; i<size; i++){
                if (onibusListas.get(i).getNumero().equals(numero) && onibusListas.get(i).getNome().equals(nome)){
                    onibusListas.remove(i);
                    adapter.notifyDataSetChanged();
                    break;
                }
            }
    }

    public void adicionarListaOrdem(apiMeuOnibus onibus, String nome, String numero){
        //onibusListas.clear();
        //
        //Criar Classe para a lista do proximo onibus, passar o nome junto para exibur na lista.
        //
        ProximoOnibusListaItem bus = new ProximoOnibusListaItem();
        bus.setDistanciaTexto(onibus.getDistanciaTexto());
        bus.setDistanciaValor(onibus.getDistanciaValor());
        bus.setEnd(onibus.getRua() + ", " + onibus.getNumero());
        bus.setNome(nome);
        bus.setNumero(numero);
        bus.setAtualizando(false);

        int size = onibusListas.size();
        boolean ad = true;
        if(size > 0){
            for(int i =0; i<size; i++){
                if(bus.getDistanciaValor() < onibusListas.get(i).getDistanciaValor()){
                    onibusListas.add(i, bus);
                    ad = false;
                    break;
                }
                if(onibusListas.get(i).getDistanciaValor() < 0){
                    onibusListas.add(i, bus);
                    ad = false;
                    break;
                }
            }
            if(ad){
                onibusListas.add(bus);
            }
        }else{
            onibusListas.add(bus);
        }
        adapter.notifyDataSetChanged();
        verifListaCompleta();
    }

    public void verifListaCompleta(){
        if(atualizando){
            quantAtualizando++;
        }
        if((!atualizando && onibusListas.size() == info.size()) || (atualizando && quantAtualizando == info.size())){
            mediaPlayer.pause();
            stoptimertask();
            if(isVoice){
                falarLista();
            }
            atualizando = false;
            startTimer3();
            onItemsLoadComplete();
            listaPreenchida = true;
            permiteArrastar();
        }
    }

    public void adicionarErro(String nome, String numero){
        //onibusListas.clear();
        //
        //Criar Classe para a lista do proximo onibus, passar o nome junto para exibur na lista.
        //
        ProximoOnibusListaItem bus = new ProximoOnibusListaItem();
        bus.setDistanciaTexto("");
        bus.setDistanciaValor(-1);
        bus.setEnd("Nenhum ônibus encontrado nesse sentido no momentoo.");
        bus.setNome(nome);
        bus.setNumero(numero);
        bus.setAtualizando(true);
        onibusListas.add(bus);
        adapter.notifyDataSetChanged();
        verifListaCompleta();
    }

    public void adicionarSemOnibus(String nome, String numero){
        //onibusListas.clear();
        //
        //Criar Classe para a lista do proximo onibus, passar o nome junto para exibur na lista.
        //
        ProximoOnibusListaItem bus = new ProximoOnibusListaItem();
        bus.setDistanciaTexto("");
        bus.setDistanciaValor(-1);
        bus.setEnd("Nenhum ônibus encontrado nesse sentido no momento.");
        bus.setNome(nome);
        bus.setNumero(numero);
        bus.setAtualizando(false);
        onibusListas.add(bus);
        adapter.notifyDataSetChanged();
        verifListaCompleta();
    }

    public void adicionarSemOnibusProximaSaida(apiMeuOnibus onibus, String nome, String numero){
        //onibusListas.clear();
        //
        //Criar Classe para a lista do proximo onibus, passar o nome junto para exibur na lista.
        //
        ProximoOnibusListaItem bus = new ProximoOnibusListaItem();
        bus.setDistanciaTexto("");
        bus.setDistanciaValor(-2);
        String msg = "Ônibus no terminal.<br/>Próxima saída prevista: <b>" + onibus.getDistanciaTexto() + "</b>";
        bus.setEnd(Html.fromHtml(msg).toString());
        bus.setNome(nome);
        bus.setNumero(numero);
        bus.setDistanciaTexto("");
        bus.setHora(onibus.getDistanciaTexto());
        bus.setAtualizando(false);

        int size = onibusListas.size();
        boolean ad = true;
        if(size > 0){
            for(int i =0; i<size; i++){
                if(onibusListas.get(i).getDistanciaValor() == -2){
                    String[] hora1Split = bus.getHora().split(":");
                    String[] hora2Split = onibusListas.get(i).getHora().split(":");
                    if(compHora(fH(hora1Split), fH(hora2Split))){
                        onibusListas.add(i, bus);
                        ad = false;
                        break;
                    }
                }
                if(onibusListas.get(i).getDistanciaValor() == -1){
                    onibusListas.add(i, bus);
                    ad = false;
                    break;
                }
            }
            if(ad){
                onibusListas.add(bus);
            }
        }else{
            onibusListas.add(bus);
        }

        adapter.notifyDataSetChanged();
        verifListaCompleta();
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

    public boolean compHora(String[] hora1Split, String[] hora2Split){
        int hora1 = Integer.parseInt(hora1Split[0]);
        int min1 = Integer.parseInt(hora1Split[1]);
        int hora2 = Integer.parseInt(hora2Split[0]);
        int min2 = Integer.parseInt(hora2Split[1]);
        //Toast.makeText(getApplicationContext(),"" + hora1 + ":" + min1 + " - " + hora2 + ":" + min2, Toast.LENGTH_SHORT).show();
        if(hora1 < hora2){
            //Toast.makeText(getApplicationContext(),"1-", Toast.LENGTH_SHORT).show();
            return true;
        }else if(hora1 == hora2){
            if(min1 < min2){
                //Toast.makeText(getApplicationContext(),"2-", Toast.LENGTH_SHORT).show();
                return true;
            }else{
                //Toast.makeText(getApplicationContext(),"3-", Toast.LENGTH_SHORT).show();
                return false;
            }
        }else{
            //Toast.makeText(getApplicationContext(),"4-", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public String[] fH(String[] horaAntiga){
        DateFormat df = new SimpleDateFormat("HH");
        DateFormat dm = new SimpleDateFormat("mm");
        if (Integer.parseInt(horaAntiga[0]) < Integer.parseInt(df.format(Calendar.getInstance().getTime()))){
            horaAntiga[0] = "" + (Integer.parseInt(horaAntiga[0]) + 24);
        }else if(Integer.parseInt(horaAntiga[0]) == Integer.parseInt(df.format(Calendar.getInstance().getTime()))){
            if (Integer.parseInt(horaAntiga[1]) < Integer.parseInt(dm.format(Calendar.getInstance().getTime()))){
                horaAntiga[0] = "" + (Integer.parseInt(horaAntiga[0]) + 24);
            }
        }
        return horaAntiga;
    }

    public void falarLista(){
        int quantFalar = 0;
        for(int i=0; i<onibusListas.size(); i++){
            if(!onibusListas.get(i).getDistanciaTexto().equals("") && onibusListas.get(i).getDistanciaValor() <= 5000 && quantFalar <= 10) {
                String[] numComp = onibusListas.get(i).getNumero().split("-");
                String num = numComp[0];
                String op = numComp[1];
                String nome = subs.trocarString(onibusListas.get(i).getNome());
                String dist = onibusListas.get(i).getDistanciaTexto();
                String rua = subString.trocarString(onibusListas.get(i).getEnd());
                if(onibusListas.get(i).getDistanciaValor() <= 600) {
                    vib.vibrate(150);
                    ttsManager.addQueue("Atenção! O ônibus da linha " + num + " " + op + " " + nome + " está próximo do ponto, a " + dist + " na " + rua);
                }else {
                    ttsManager.addQueue("O ônibus da linha " + num + " " + op + " " + nome + " está a " + dist + " na " + rua);
                }
                quantFalar++;
            }
        }
        if(quantFalar == 0){
            ttsManager.addQueue("Nenhum ônibus selecionado está se aproximando no momento.");
        }
    }

    public void adicionarLista(apiMeuOnibus onibus, String nome, String numero){
        //onibusListas.clear();
        //
        //Criar Classe para a lista do proximo onibus, passar o nome junto para exibur na lista.
        //
            ProximoOnibusListaItem bus = new ProximoOnibusListaItem();
            bus.setDistanciaTexto(onibus.getDistanciaTexto());
            bus.setDistanciaValor(onibus.getDistanciaValor());
            bus.setEnd(onibus.getRua() + ", " + onibus.getNumero());
            bus.setNome(nome);
            bus.setNumero(numero);
            onibusListas.add(bus);
        adapter.notifyDataSetChanged();
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
            //super.finish();
            finish();
        }else if(comando.toLowerCase().equals("buscar de novo") || comando.toLowerCase().equals("localizar de novo")){
            startTimer2();
        }
    }

    public void btnDireita(View view){
        ttsManager.stop();
        startVoiceInput();
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
        icont = 0; //Zera contador de ônibus
        //initialize the TimerTask's job
        initializeTimerTask2();

        if(isVoice) {
            startTimer();
        }
        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer2.schedule(timerTask2, 1000, 1000); //
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

                            retrofitPegarOnibus(info.get(icont)[0], info.get(icont)[1], info.get(icont)[2]);
                            icont++;
                       // }
                        if(icont >= info.size()){
                            stoptimertask2();
                        }
                    }
                });
            }
        };
    }





    public void startTimer3() {
        //set a new Timer
        timer3 = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask3();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer3.schedule(timerTask3, 30000, 30000); //
    }

    public void stoptimertask3() {
        //stop the timer, if it's not already null
        if (timer3 != null) {
            timer3.cancel();
            timer3 = null;
        }
    }

    public void initializeTimerTask3() {
        timerTask3 = new TimerTask() {
            public void run() {
                //use a handler to run a toast that shows the current timestamp
                handler3.post(new Runnable() {
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(true);
                        atualizando = true;
                        quantAtualizando = 0;
                        defAtualizandoItens();
                        startTimer2();
                        stoptimertask3();
                        despermiteArrastar();
                    }
                });
            }
        };
    }





}
