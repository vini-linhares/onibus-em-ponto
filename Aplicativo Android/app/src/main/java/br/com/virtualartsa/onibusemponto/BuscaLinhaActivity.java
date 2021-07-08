package br.com.virtualartsa.onibusemponto;

import android.app.Activity;
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
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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

public class BuscaLinhaActivity extends AppCompatActivity {

    private static final int REQ_CODE_SPEECH_INPUT_EDIT = 101;
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    public final static String GRUPO_SALVO = "br.com.virtualartsa.onibusemponto.GRUPO_SALVO";
    public final static String VOZ = "br.com.virtualartsa.onibusemponto.VOZ";
    public LinearLayout bots;
    int comandoEstagio;
    public boolean isVoice;

    TTSManagerMsg ttsManager = null;

    private String linhaBusca;
    apiRespostaServidor resposta = new apiRespostaServidor();

    public LinearLayout lnlMensagem;
    public TextView txtMensagem;
    ProgressBar prgBarMensagem;

    Call<apiRespostaServidor> call;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    BuscaLinhaListaRecyclerAdapter adapter;
    private List<BuscaLinhaListaItem> linhasListas = new ArrayList<>();
    SwipeRefreshLayout mSwipeRefreshLayout;


    EditText edtEnd;
    TextView txtEnd;
    ImageButton btnEnd;
    LinearLayout llTextEnd;
    LinearLayout llEditEnd;
    ProgressBar prgBar;
    ImageButton btnMic;
    boolean txtIsVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_busca_linha);

        txtMensagem = (TextView)findViewById(R.id.txtMensagem);
        lnlMensagem = (LinearLayout)findViewById(R.id.lnlMensagem);
        prgBarMensagem = (ProgressBar) findViewById(R.id.prgBarMensagem);
        prgBarMensagem.getIndeterminateDrawable().setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.MULTIPLY);


        bots = (LinearLayout)findViewById(R.id.layBotsBL);
        SharedPreferences sharedPref = this.getSharedPreferences( GRUPO_SALVO, Context.MODE_PRIVATE);
        String tutorialVisto = sharedPref.getString(VOZ, "0");
        if(tutorialVisto.equals("0")){
            bots.setVisibility(View.INVISIBLE);
            isVoice = false;
        }else{
            isVoice = true;
        }
        comandoEstagio = 1;


        if(isVoice) {
            ttsManager = new TTSManagerMsg();
            ttsManager.init(this, "Diga o número de uma linha para realizar a busca.");
        }

        setaRecyclerView();

        edtEnd = (EditText)findViewById(R.id.edtEnd);
        edtEnd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    hideKeyboard(v);
                    btnPesquisar(v);
                }
                return false;
            }
        });
        txtEnd = (TextView)findViewById(R.id.txtEnd);
        txtEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPesquisar(v);
            }
        });
        btnEnd = (ImageButton)findViewById(R.id.btnEnd);
        llEditEnd = (LinearLayout)findViewById(R.id.llEdtEnd);
        llTextEnd = (LinearLayout) findViewById(R.id.llTextEnd);
        prgBar = (ProgressBar) findViewById(R.id.prgBar);
        prgBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#3F51B5"), PorterDuff.Mode.MULTIPLY);
        txtIsVisible = false;
        edtEnd.requestFocus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(isVoice) {
            ttsManager.shutDown();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(isVoice) {
            ttsManager.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //if(linhasListas.size() > 1){
            //comandoEstagio = 1;
        //}
        //hideKeyboard(this, txtLinha);
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void btnPesquisar(View v){
        mSwipeRefreshLayout.setRefreshing(false);
        finalizaChamadas();
        if (txtIsVisible) {
            if(isVoice) {
                ttsManager.stop();
            }
            llTextEnd.setVisibility(View.GONE);
            llEditEnd.setVisibility(View.VISIBLE);
            edtEnd.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(edtEnd, InputMethodManager.SHOW_IMPLICIT);
            txtIsVisible = false;
            btnEnd.setImageResource(R.drawable.pes);
            //btnEnd.setText("V");
        } else {
            linhaBusca = edtEnd.getText().toString();
            int tamanho = linhaBusca.length();
            if(tamanho <= 2){
                Toast.makeText(this, "Digite pelo menos 3 caracteres para realizar a pesquisa.", Toast.LENGTH_LONG).show();
                txtEnd.requestFocus();
            }else{
                hideKeyboard(this, edtEnd);
                llTextEnd.setVisibility(View.VISIBLE);
                llEditEnd.setVisibility(View.GONE);
                txtIsVisible = true;
                //btnEnd.setText("E");
                btnEnd.setImageResource(R.drawable.ed);
                txtEnd.setText("Buscando: " + linhaBusca);
                retrofitConverter(linhaBusca);
            }
        }
    }

    public void finalizaChamadas(){
        if(call != null){
            call.cancel();
        }
    }

    public void retrofitConverter(String busca) {
        mSwipeRefreshLayout.setRefreshing(true);

        apiRetrofitService service = apiServiceGenerator.createService(apiRetrofitService.class);
        call = service.converterUnidade(subString.destrocaAbrev(busca));

        call.enqueue(new Callback<apiRespostaServidor>() {
            @Override
            public void onResponse(Call<apiRespostaServidor> call, Response<apiRespostaServidor> response) {

                mSwipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful()) {
                    apiRespostaServidor respostaServidor = response.body();
                    //verifica aqui se o corpo da resposta não é nulo
                    if (respostaServidor != null) {

                        lnlMensagem.setVisibility(View.GONE);

                        resposta.setLinha(respostaServidor.getLinha());
                        //resposta.setRates(respostaServidor.getRates());
                        //resposta.setTeste(respostaServidor.getTeste());
                        //progress.dismiss();
                        //Toast.makeText(getApplicationContext(),resposta.getRates().getUSD().toString(), Toast.LENGTH_SHORT).show();
                        //prencheLista();
                        //atualizaBanco();
                        //prencheLista();
                        List<apiLinha> teste = resposta.getLinha();

                        if(teste.get(0).getLt().equals("0000")){
                            Toast.makeText(getApplicationContext(), "Nenhuma linha encontrada com esse termo de busca.", Toast.LENGTH_SHORT).show();
                            if (isVoice) {
                                ttsManager.addQueue("Nenhuma linha encontrada com esse termo de busca.");
                            }
                        }else if(teste.get(0).getLt().equals("0001")){
                            Toast.makeText(getApplicationContext(), "Servidores da SPtrans não responderam. Tente novamente mais tarde.", Toast.LENGTH_SHORT).show();
                            if (isVoice) {
                                ttsManager.addQueue("Desculpe, Servidores da SPtrans não responderam. Tente novamente mais tarde.");
                            }
                        }else{
                            preencherLista(teste);
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
                    }
                } else {
                    //Toast.makeText(getApplicationContext(),"Resposta não foi sucesso", Toast.LENGTH_SHORT).show();
                    // segura os erros de requisição
                    msgErroServidor();
                    ResponseBody errorBody = response.errorBody();
                }
                //progress.dismiss();
            }
            @Override
            public void onFailure(Call<apiRespostaServidor> call, Throwable t) {
                //Toast.makeText(getApplicationContext(),"Erro na chamada ao servidor", Toast.LENGTH_SHORT).show();
                msgErroServidor();
            }
        });
    }

    public void msgErroServidor(){
        lnlMensagem.setBackgroundColor(Color.parseColor("#444444"));
        lnlMensagem.setVisibility(View.VISIBLE);
        txtMensagem.setText("Erro nos servidores");
    }
//
//-----Lista
//
    public void setaRecyclerView(){
        //Aqui é instanciado o Recyclerview
        mRecyclerView = (RecyclerView) findViewById(R.id.rcv_lista_linhas);
        //mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false); // Fazer a Lista virar Horizontal
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        adapter = new BuscaLinhaListaRecyclerAdapter(this, linhasListas);
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

    void onItemsLoadComplete() {
        // Update the adapter and notify data set changed
        // ...

        // Stop refresh animation
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void preencherLista(List<apiLinha> linhas){
        String falarLinhas = "";
        linhasListas.clear();
        for (int i=0; i<linhas.size(); i++){
            BuscaLinhaListaItem linha = new BuscaLinhaListaItem();
            linha.setNum(linhas.get(i).getLt());
            int sl = linhas.get(i).getSl();
            if(sl == 1){
                linha.setSentido(linhas.get(i).getTp());
                linha.setSentido2(linhas.get(i).getTs());
            }else if(sl == 2) {
                linha.setSentido(linhas.get(i).getTs());
                linha.setSentido2(linhas.get(i).getTp());
            }
            linha.setCodLinha(linhas.get(i).getCl());
            linha.setModo(linhas.get(i).getTl());
            linhasListas.add(linha);
        }
        adapter.notifyDataSetChanged();
        comandoEstagio = 2;
        if(linhasListas.size() == 1){
            //Toast.makeText(v.getContext(), ""+cod, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, DetalhesActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("linha", linhasListas.get(0).getNum() + "-" + linhasListas.get(0).getModo());
            intent.putExtras(bundle);
            this.startActivity(intent);
            finish();
        }else{
            if (isVoice) {
                ttsManager.initQueue("Escolha uma das linhas dizendo o seu número:");
            for (int i=0; i<linhas.size(); i++){
                falarLinhas += "Linha " + linhas.get(i).getLt() + " " + linhas.get(i).getTl() + "De " + subString.trocarString(linhas.get(i).getTs()) + " a " + subString.trocarString(linhas.get(i).getTp()) + "\n";
            }
            ttsManager.addQueue(falarLinhas);
            }
        }
    }

    public void btnMic(View view){
        startEditVoiceInput();
    }

    private void startEditVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Diga a linha");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT_EDIT);
        } catch (ActivityNotFoundException a) {

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
            case REQ_CODE_SPEECH_INPUT_EDIT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    edtEnd.setText(result.get(0));
                    //hideKeyboard(btnMic);
                    btnPesquisar(btnMic);
                }
                break;
            }

        }
    }

    public void reconhecerComando(String comando) {
        if(comando.toLowerCase().equals("voltar")){
            finish();
        }else{
            if(comandoEstagio == 1){
                int tamanho = comando.length();

                if(tamanho <= 2){
                    if (isVoice) {
                        ttsManager.initQueue("Nenhuma linha Identificada.");
                    }
                }else{
                    //Toast.makeText(this, "Pesquisar: " + linhaBusca, Toast.LENGTH_LONG).show();
                    retrofitConverter(comando);
                }
            }else if(comandoEstagio == 2){
                    reconhecerLinha(comando);
                //Toast.makeText(this, comando, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void reconhecerLinha(String comando) {
        comando = subString.trocarString2(comando);
        boolean achouLinha = false;
        for (int i=0; i<linhasListas.size(); i++){
            if(comando.equals(linhasListas.get(i).getNum() + linhasListas.get(i).getModo())){
                Intent intent = new Intent(this, DetalhesActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("linha", linhasListas.get(i).getNum() + "-" + linhasListas.get(i).getModo());
                intent.putExtras(bundle);
                this.startActivity(intent);
                achouLinha = true;
                if(isVoice){
                    finish();
                }
                break;
            }
        }
        if(!achouLinha){
            for (int i=0; i<linhasListas.size(); i++){
                String linhaVez = linhasListas.get(i).getNum() + linhasListas.get(i).getModo();
                if(linhaVez.contains(comando)){
                    Intent intent = new Intent(this, DetalhesActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("linha", linhasListas.get(i).getNum() + "-" + linhasListas.get(i).getModo());
                    intent.putExtras(bundle);
                    this.startActivity(intent);
                    achouLinha = true;
                    if(isVoice){
                        finish();
                    }
                    break;
                }
            }
        }
        if(!achouLinha){
            for (int i=0; i<linhasListas.size(); i++){
                String linhaVez = linhasListas.get(i).getNum() + linhasListas.get(i).getModo() + subString.trocarString(linhasListas.get(i).getSentido()) + " a " + subString.trocarString(linhasListas.get(i).getSentido2());
                linhaVez = subString.formatarString(linhaVez);
                String comand =  subString.formatarString(comando);
                if(linhaVez.contains(comand)){
                    Intent intent = new Intent(this, DetalhesActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("linha", linhasListas.get(i).getNum() + "-" + linhasListas.get(i).getModo());
                    intent.putExtras(bundle);
                    this.startActivity(intent);
                    achouLinha = true;
                    if(isVoice){
                        finish();
                    }
                    break;
                }
            }
        }
        if(!achouLinha){
            if (isVoice) {
                ttsManager.initQueue("Desculpe, não entendi a linha.");
            }
        }
    }



    public void btnDireita(View view){
        ttsManager.stop();
        startVoiceInput();
        //ttsManager.initQueue("Nenhuma linha Identificada.");
    }


    public static void hideKeyboard(Context context, View editText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }
}
