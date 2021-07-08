package br.com.virtualartsa.onibusemponto;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.speech.RecognizerIntent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalhesActivity extends AppCompatActivity {

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    public final static String GRUPO_SALVO = "br.com.virtualartsa.onibusemponto.GRUPO_SALVO";
    public final static String VOZ = "br.com.virtualartsa.onibusemponto.VOZ";
    public LinearLayout bots;
    public ImageButton bot;
    public boolean comandVoz;
    public boolean erroAvisado;
    public boolean erroItinerario;
    public boolean erroInfo;

    TTSManagerMsg ttsManager = null;

    TextView txtLinha, txtIda, txtVolta, txtSemanaInicial, txtSemanaFinal, txtSabadoInicial, txtSabadoFinal, txtDomingoInicial, txtDomingoFinal;
    DetalhesApiRespostaServidor resposta = new DetalhesApiRespostaServidor();
    DetalhesItinerarioApiRespostaServidor respostaIt = new DetalhesItinerarioApiRespostaServidor();

    ProgressBar prgBar;

    public LinearLayout lnlMensagem;
    public TextView txtMensagem;
    ProgressBar prgBarMensagem;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    DetalhesListaRecyclerAdapter adapter;
    private List<DetalhesListaItem> ruasListas = new ArrayList<>();
    SwipeRefreshLayout mSwipeRefreshLayout;

    String numLinha;

    boolean carregou;

    String falarItinerario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes);

        txtMensagem = (TextView)findViewById(R.id.txtMensagem);
        lnlMensagem = (LinearLayout)findViewById(R.id.lnlMensagem);
        prgBarMensagem = (ProgressBar) findViewById(R.id.prgBarMensagem);
        prgBarMensagem.getIndeterminateDrawable().setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.MULTIPLY);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        numLinha = bundle.getString("linha");

        txtLinha = (TextView) findViewById(R.id.txtNumLinha);
        txtLinha.setText(numLinha);

        txtIda = (TextView) findViewById(R.id.txtIda);
        txtVolta = (TextView) findViewById(R.id.txtVolta);
        txtSemanaInicial = (TextView) findViewById(R.id.txtSemanaInicial);
        txtSemanaFinal = (TextView) findViewById(R.id.txtSemanaFinal);
        txtSabadoInicial = (TextView) findViewById(R.id.txtSabadoInicial);
        txtSabadoFinal = (TextView) findViewById(R.id.txtSabadoFinal);
        txtDomingoInicial = (TextView) findViewById(R.id.txtDomingoInicial);
        txtDomingoFinal = (TextView) findViewById(R.id.txtDomingoFinal);

        prgBar = (ProgressBar) findViewById(R.id.prgBar);
        prgBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#222222"), PorterDuff.Mode.MULTIPLY);

        //bot = (ImageButton)findViewById(R.id.btnPesquisar);
        bots = (LinearLayout)findViewById(R.id.layBotsD);
        SharedPreferences sharedPref = this.getSharedPreferences( GRUPO_SALVO, Context.MODE_PRIVATE);
        String tutorialVisto = sharedPref.getString(VOZ, "0");
        if(tutorialVisto.equals("0")){
            bots.setVisibility(View.INVISIBLE);
            comandVoz = false;
        }else{
            ttsManager = new TTSManagerMsg();
            ttsManager.init(this, "Buscando informações da linha: " + numLinha.replace("-", " "));
            comandVoz = true;
            erroAvisado = false;
            erroItinerario = false;
            erroInfo = false;
        }



        setaRecyclerView();

        carregou = false;
        retrofitPegarInfo();
        retrofitPegarItinerario();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(comandVoz){
            ttsManager.shutDown();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(comandVoz){
            ttsManager.stop();
        }
    }

    public void retrofitPegarInfo() {
        prgBar.setVisibility(View.VISIBLE);

        apiRetrofitService service = apiServiceGenerator.createService(apiRetrofitService.class);
        Call<DetalhesApiRespostaServidor> call = service.pegarInfo(numLinha);

        call.enqueue(new Callback<DetalhesApiRespostaServidor>() {
            @Override
            public void onResponse(Call<DetalhesApiRespostaServidor> call, Response<DetalhesApiRespostaServidor> response) {

                prgBar.setVisibility(View.INVISIBLE);

                if (response.isSuccessful()) {
                    DetalhesApiRespostaServidor respostaServidor = response.body();
                    //verifica aqui se o corpo da resposta não é nulo
                    if (respostaServidor != null) {

                        lnlMensagem.setVisibility(View.GONE);

                        resposta.setIda(respostaServidor.getIda());
                        //resposta.setRates(respostaServidor.getRates());
                        //resposta.setTeste(respostaServidor.getTeste());
                        //progress.dismiss();
                        //Toast.makeText(getApplicationContext(),resposta.getRates().getUSD().toString(), Toast.LENGTH_SHORT).show();
                        //prencheLista();
                        //atualizaBanco();
                        //prencheLista();
                        //List<apiLinha> teste = resposta.getLinha();

                        if(resposta.getIda().equals("Linha0000-00") || resposta.getIda().equals("Linha0000-10")){
                            Toast.makeText(getApplicationContext(), "Servidores da SPTrans não responderam. Tente novamente mais tarde.", Toast.LENGTH_SHORT).show();
                            if(comandVoz && !erroAvisado){
                                ttsManager.addQueue("Servidores da SPTrans não responderam. Tente novamente mais tarde.");
                                erroAvisado = true;
                            }
                        }else{
                            //preencherLista(teste);
                            //Toast.makeText(getApplicationContext(), resposta.getIda(), Toast.LENGTH_SHORT).show();
                            resposta.setVolta(respostaServidor.getVolta());
                            resposta.setSemana(respostaServidor.getSemana());
                            resposta.setSabado(respostaServidor.getSabado());
                            resposta.setDomingo(respostaServidor.getDomingo());

                            preencherInfo(resposta);
                        }


                        //Toast.makeText(getApplicationContext(),teste.get(0).getLt(), Toast.LENGTH_SHORT).show();
                        //List<apiTeste> teste = resposta.getTeste();
                        //Toast.makeText(getApplicationContext(),teste.get(1).getTeste2(), Toast.LENGTH_SHORT).show();
                        //Toast.makeText(getApplicationContext(),resposta.getRates().getEUR().toString(), Toast.LENGTH_SHORT).show();
                        //Toast.makeText(getApplicationContext(),resposta.getLinha().size(), Toast.LENGTH_SHORT).show();
                        //Toast.makeText(getApplicationContext(),"OI", Toast.LENGTH_SHORT).show();
                    } else {
                        //Toast.makeText(getApplicationContext(),"Resposta nula do servidor", Toast.LENGTH_SHORT).show();
                        msgErroServidor();
                        retrofitPegarInfo();
                    }
                } else {
                    //Toast.makeText(getApplicationContext(),"Resposta não foi sucesso", Toast.LENGTH_SHORT).show();
                    msgErroServidor();
                    retrofitPegarInfo();
                    // segura os erros de requisição
                    ResponseBody errorBody = response.errorBody();
                }
                //progress.dismiss();
            }
            @Override
            public void onFailure(Call<DetalhesApiRespostaServidor> call, Throwable t) {
                //Toast.makeText(getApplicationContext(),"Erro na chamada ao servidor", Toast.LENGTH_SHORT).show();
                msgErroServidor();
                retrofitPegarInfo();
            }
        });
    }

    public void preencherInfo(DetalhesApiRespostaServidor resposta){
        txtIda.setText(resposta.getIda());
        txtVolta.setText(resposta.getVolta());
        txtSemanaInicial.setText(resposta.getSemana().getInicial());
        txtSemanaFinal.setText(resposta.getSemana().getFim());
        txtSabadoInicial.setText(resposta.getSabado().getInicial());
        txtSabadoFinal.setText(resposta.getSabado().getFim());
        txtDomingoInicial.setText(resposta.getDomingo().getInicial());
        txtDomingoFinal.setText(resposta.getDomingo().getFim());

        if(carregou){
            msgDetalhes();
        }else{
            carregou = true;
        }
    }


    public void retrofitPegarItinerario() {
        mSwipeRefreshLayout.setRefreshing(true);
        apiRetrofitService service = apiServiceGenerator.createService(apiRetrofitService.class);
        Call<DetalhesItinerarioApiRespostaServidor> call = service.pegarItinerario(txtLinha.getText().toString());

        call.enqueue(new Callback<DetalhesItinerarioApiRespostaServidor>() {
            @Override
            public void onResponse(Call<DetalhesItinerarioApiRespostaServidor> call, Response<DetalhesItinerarioApiRespostaServidor> response) {

                mSwipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful()) {
                    DetalhesItinerarioApiRespostaServidor respostaServidor = response.body();
                    //verifica aqui se o corpo da resposta não é nulo
                    if (respostaServidor != null) {

                        lnlMensagem.setVisibility(View.GONE);

                        respostaIt.setIda(respostaServidor.getIda());
                        respostaIt.setVolta(respostaServidor.getVolta());

                        if(respostaIt.getIda().get(0).getRua().equals("Rua0000") || respostaIt.getIda().get(0).getRua().equals("Rua0001")){
                            Toast.makeText(getApplicationContext(), "Servidores da SPTrans não responderam. Tente novamente mais tarde.", Toast.LENGTH_SHORT).show();
                            if(comandVoz && !erroAvisado){
                                ttsManager.addQueue("Servidores da SPTrans não responderam. Tente novamente mais tarde.");
                                erroAvisado = true;
                            }
                        }else{
                            //Toast.makeText(getApplicationContext(), respostaIt.getIda().get(0).getRua(), Toast.LENGTH_SHORT).show();
                            preencherLista(respostaIt.getIda());
                            //preencherLista(teste);
                            //Toast.makeText(getApplicationContext(), resposta.getIda(), Toast.LENGTH_SHORT).show();
                            //resposta.setVolta(respostaServidor.getVolta());
                            //resposta.setSemana(respostaServidor.getSemana());
                            //resposta.setSabado(respostaServidor.getSabado());
                            //resposta.setDomingo(respostaServidor.getDomingo());

                            //preencherInfo(resposta);
                        }

                    } else {
                        //Toast.makeText(getApplicationContext(),"Resposta nula do servidor", Toast.LENGTH_SHORT).show();
                        msgErroServidor();
                        retrofitPegarItinerario();
                    }
                } else {
                    //Toast.makeText(getApplicationContext(),"Resposta não foi sucesso", Toast.LENGTH_SHORT).show();
                    // segura os erros de requisição
                    msgErroServidor();
                    retrofitPegarItinerario();
                    ResponseBody errorBody = response.errorBody();
                }
                //progress.dismiss();
            }
            @Override
            public void onFailure(Call<DetalhesItinerarioApiRespostaServidor> call, Throwable t) {
                //Toast.makeText(getApplicationContext(),"Erro na chamada ao servidor", Toast.LENGTH_SHORT).show();
                msgErroServidor();
                retrofitPegarItinerario();
            }
        });
    }

    public void msgErroServidor(){
        lnlMensagem.setBackgroundColor(Color.parseColor("#444444"));
        lnlMensagem.setVisibility(View.VISIBLE);
        txtMensagem.setText("Erro nos servidores");
    }

    public void setaRecyclerView(){
        //Aqui é instanciado o Recyclerview
        mRecyclerView = (RecyclerView) findViewById(R.id.rcv_itinerario);
        //mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false); // Fazer a Lista virar Horizontal
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        adapter = new DetalhesListaRecyclerAdapter(this, ruasListas);
        mRecyclerView.setAdapter(adapter);

        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    public void preencherLista(List<apiEnd> ruas){
        falarItinerario = "";
        ruasListas.clear();
        for (int i=0; i<ruas.size(); i++){
            DetalhesListaItem rua = new DetalhesListaItem();
            rua.setRua(ruas.get(i).getRua());
            rua.setNumInicio(ruas.get(i).getNumInicio());
            rua.setNumFim(ruas.get(i).getNumFim());
            ruasListas.add(rua);
            falarItinerario += subString.trocarString(rua.getRua()) + " de " + rua.getNumInicio() + " a " + rua.getNumFim() + " \n ";
        }
        adapter.notifyDataSetChanged();
        if(carregou){
            msgDetalhes();
        }else{
            carregou = true;
        }
    }

    public void msgDetalhes(){
        if(comandVoz) {
            ttsManager.init(this, "Linha de " + subString.trocarString(txtIda.getText().toString()) + " a " + subString.trocarString(txtVolta.getText().toString()) + ". Para mais informações diga \n Horários de Operação \n ou \n Itinerário.");
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
            super.finish();
            finish();
        }else if(comando.toLowerCase().equals("horários de operação") || comando.toLowerCase().equals("horários")){
            //ttsManager.init(this, "Horários");
            if (erroInfo){
                ttsManager.addQueue("Desculpe, servidores da SPTrans não responderam. Volte e tente novamente");
            }else {
                falarHorarios();
            }
        } if(comando.toLowerCase().equals("itinerário")){
            if (erroItinerario){
                ttsManager.addQueue("Desculpe, servidores da SPTrans não responderam. Volte e tente novamente");
            }else {
                ttsManager.init(this, falarItinerario);
            }
        }
    }

    public void falarHorarios() {
        String hor = "";

        if (txtSemanaFinal.getText().toString().equals("-")){
            hor += "Em dias úteis essa linha opera " + extrairHora(txtSemanaInicial.getText().toString());
            ttsManager.addQueue(hor);
            hor = "";
            hor += "Nos sábados essa linha opera " + extrairHora(txtSabadoInicial.getText().toString());
            ttsManager.addQueue(hor);
            hor = "";
            hor += "E nos domingos ou feriádos essa linha opera " + extrairHora(txtDomingoInicial.getText().toString());
            ttsManager.addQueue(hor);
        }else{
            hor += "Em dias úteis essa linha opera no ponto inicial " + extrairHora(txtSemanaInicial.getText().toString()) + ". E no ponto final " + extrairHora(txtSemanaFinal.getText().toString());
            ttsManager.addQueue(hor);
            hor = "";
            hor += "Nos sábados essa linha opera no ponto inicial " + extrairHora(txtSabadoInicial.getText().toString()) + ". E no ponto final " + extrairHora(txtSabadoFinal.getText().toString());
            ttsManager.addQueue(hor);
            hor = "";
            hor += "E nos domingos ou feriádos essa linha opera no ponto inicial " + extrairHora(txtDomingoInicial.getText().toString()) + ". E no ponto final " + extrairHora(txtDomingoFinal.getText().toString());
            ttsManager.addQueue(hor);
        }
    }

    public String extrairHora(String string) {
        String hor = "das ";
        String[] hors = string.split("-");
        String[] hExata = hors[0].split(":");
        hor += Integer.parseInt(hExata[0]);
        if(hExata[1].equals("00")){
            hor += " horas";
        }else if(hExata[1].equals("30")){
            hor += " e meia";
        }else{
            hor += " e ";
            hor += Integer.parseInt(hExata[1]);;
        }
        hor += " as ";
        hExata = hors[1].split(":");
        if(hExata[0].equals("00")){
            hor += "meia noite";
        }else {
            hor += Integer.parseInt(hExata[0]);
        }
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



    public void btnDireita(View view){
        ttsManager.stop();
        startVoiceInput();
        //ttsManager.initQueue("Nenhuma linha Identificada.");
    }


}
