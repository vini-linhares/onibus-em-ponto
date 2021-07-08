package br.com.virtualartsa.onibusemponto;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Handler;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PegaLinha2Activity extends AppCompatActivity {

    Context context;

    Timer timer;
    TimerTask timerTask;
    final Handler handler = new Handler();

    private static final int REQ_CODE_SPEECH_INPUT_EDIT = 101;
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    public final static String GRUPO_SALVO = "br.com.virtualartsa.onibusemponto.GRUPO_SALVO";
    public final static String VOZ = "br.com.virtualartsa.onibusemponto.VOZ";
    public final static String PONTOID = "br.com.virtualartsa.onibusemponto.PONTOID";
    public final static String PONTOEND = "br.com.virtualartsa.onibusemponto.PONTOEND";
    public LinearLayout bots;
    public boolean isVoice;

    FloatingActionButton fab;

    public LinearLayout lnlMensagem;
    public TextView txtMensagem;
    ProgressBar prgBarMensagem;

    public LinearLayout lnlSemOnibus;
    public TextView txtSemOnibus;
    Timer timerAtualizar;
    TimerTask timerTaskAtualizar;
    final Handler handlerAtualizar = new Handler();
    Call<PegaLinhaApiRespostaServidor> callPegarLinha;
    Call<PontoApiRepostaServidor> callPegarEndComp;
    Call<apiPontos> callPegarPontos;


    TTSManagerMsg ttsManager = null;

    PegaLinhaApiRespostaServidor resposta = new PegaLinhaApiRespostaServidor();
    apiPontos resposta2 = new apiPontos();
    PontoApiRepostaServidor resposta3 = new PontoApiRepostaServidor();

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    PegaLinha2ListaRecyclerAdapter adapter;
    private List<PegaLinhaListaItem> linhasListas = new ArrayList<>();
    SwipeRefreshLayout mSwipeRefreshLayout;

    private RecyclerView mRecyclerViewPontos;
    private RecyclerView.LayoutManager mLayoutManagerPontos;
    PegaLinha2PontosListaRecyclerAdapter adapterPontos;
    private List<PegaLinha2PontosListaItem> pontosLista = new ArrayList<>();

    MediaPlayer mediaPlayer;

    EditText edtEnd;
    TextView txtEnd;
    ImageButton btnEnd;
    LinearLayout llTextEnd;
    LinearLayout llEditEnd;
    ProgressBar prgBar;
    ImageButton btnMic;
    boolean txtIsVisible;

    LinearLayout listaPonto;

    boolean primeiraBusa;

    private LocationCallback mLocationCallback;
    LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;

    double lat;
    double lng;

    boolean previneDuplaRequisicao = false;

    boolean pegarLocalResume = false;

    boolean primeiraBusca = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pega_linha2);

        context = this;

        txtMensagem = (TextView)findViewById(R.id.txtMensagem);
        lnlMensagem = (LinearLayout)findViewById(R.id.lnlMensagem);
        prgBarMensagem = (ProgressBar) findViewById(R.id.prgBarMensagem);
        prgBarMensagem.getIndeterminateDrawable().setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.MULTIPLY);
        txtSemOnibus = (TextView)findViewById(R.id.txtSemOnibus);
        lnlSemOnibus = (LinearLayout)findViewById(R.id.lnlSemOnibus);

        listaPonto = (LinearLayout)findViewById(R.id.listaPonto);

        edtEnd = (EditText)findViewById(R.id.edtEnd);
        edtEnd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if(!previneDuplaRequisicao) {
                        //hideKeyboard(v);
                        //clickEnd(v);
                    }else{
                        previneDuplaRequisicao = false;
                    }
                }
            }
        });
        edtEnd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    previneDuplaRequisicao = true;
                    hideKeyboard(v);
                    clickEnd(v);
                }
                return false;
            }
        });
        txtEnd = (TextView)findViewById(R.id.txtEnd);
        txtEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickEnd(v);
            }
        });
        btnEnd = (ImageButton)findViewById(R.id.btnEnd);
        llEditEnd = (LinearLayout)findViewById(R.id.llEdtEnd);
        llTextEnd = (LinearLayout) findViewById(R.id.llTextEnd);
        prgBar = (ProgressBar) findViewById(R.id.prgBar);
        prgBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#3F51B5"), PorterDuff.Mode.MULTIPLY);
        txtIsVisible = true;

        primeiraBusa = true;

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.teste(PegaLinha2Activity.this);
                if(isVoice){
                    finish();
                }
            }
        });

        bots = (LinearLayout)findViewById(R.id.layBotsPL);
        SharedPreferences sharedPref = this.getSharedPreferences( GRUPO_SALVO, Context.MODE_PRIVATE);
        String tutorialVisto = sharedPref.getString(VOZ, "0");
        if(tutorialVisto.equals("0")){
            bots.setVisibility(View.INVISIBLE);
            isVoice = false;
        }else{
            isVoice = true;
            btnMic = (ImageButton) findViewById(R.id.btnMic);
            btnMic.setVisibility(View.GONE);
        }
        if(isVoice) {
            ttsManager = new TTSManagerMsg();
            ttsManager.init(this, "");
        }

        mediaPlayer = MediaPlayer.create(PegaLinha2Activity.this, R.raw.load);
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(0.1f,0.1f);

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
    protected void onResume() {
        if (pegarLocalResume){
            pegarLocalResume = false;
            rodar();
        }
        super.onResume();
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    rodar();

                } else {
                    //
                    //
                    //
                    //
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void rodar(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if (locationProviders == null || locationProviders.equals("")) {
                String[] itens = new String[2];
                itens[0] = "Desejo ativar!";
                itens[1] = "Agora não";

                AlertDialog.Builder builder = new AlertDialog.Builder(PegaLinha2Activity.this);
                builder.setTitle("Esse app precsia do GPS para funcinar corretamente.")
                        .setItems(itens, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == 0){
                                    pegarLocalResume = true;
                                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }else{
                                    //
                                }
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }else {
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {

                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    //Toast.makeText(getApplicationContext(), String.valueOf(location.getLatitude()+","+location.getLongitude()), Toast.LENGTH_SHORT).show();
                                    lat = location.getLatitude();
                                    lng = location.getLongitude();

                                    retrofitPegarPonto(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));

                                    /*
                                    SharedPreferences sharedPref = context.getSharedPreferences(GRUPO_SALVO, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putString(LATLNG, location.getLatitude() + "," + location.getLongitude());
                                    editor.apply();
                                    */
                                    //coord = location.getLatitude()+","+location.getLongitude();

                                } else {
                                    //Toast.makeText(getApplicationContext(), "Location = null", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
    }

    public void setaRecyclerView(){
        //Aqui é instanciado o Recyclerview
        mRecyclerView = (RecyclerView) findViewById(R.id.rcv_pega_linha_2);
        //mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false); // Fazer a Lista virar Horizontal
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        adapter = new PegaLinha2ListaRecyclerAdapter(this, linhasListas);
        mRecyclerView.setAdapter(adapter);

        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });


        //Aqui é instanciado o RecyclerviewPontos
        mRecyclerViewPontos = (RecyclerView) findViewById(R.id.rcv_pontos);
        mLayoutManagerPontos = new LinearLayoutManager(this);
        mRecyclerViewPontos.setLayoutManager(mLayoutManagerPontos);
        adapterPontos = new PegaLinha2PontosListaRecyclerAdapter(this, pontosLista);
        mRecyclerViewPontos.setAdapter(adapterPontos);
        mRecyclerViewPontos.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }


    void onItemsLoadComplete() {
        // Update the adapter and notify data set changed
        // ...

        // Stop refresh animation
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void retrofitPegarLinhas(final String pontoId) {
        mSwipeRefreshLayout.setRefreshing(true);
        lnlSemOnibus.setVisibility(View.GONE);

        if (isVoice){
            startTimer();
        }

            apiRetrofitService service = apiServiceGenerator.createService(apiRetrofitService.class);
            callPegarLinha = service.pegarLinhas(pontoId);
            //"-23.498888,-46.594659"
            //Toast.makeText(getApplicationContext(), coord, Toast.LENGTH_SHORT).show();

            callPegarLinha.enqueue(new Callback<PegaLinhaApiRespostaServidor>() {
                @Override
                public void onResponse(Call<PegaLinhaApiRespostaServidor> call, Response<PegaLinhaApiRespostaServidor> response) {

                    primeiraBusca = false;
                    stoptimertask();
                    mediaPlayer.pause();
                    onItemsLoadComplete();

                    if (response.isSuccessful()) {
                        PegaLinhaApiRespostaServidor respostaServidor = response.body();
                        //verifica aqui se o corpo da resposta não é nulo
                        if (respostaServidor != null) {
                            resposta.setLinhas(respostaServidor.getLinhas());

                            if (resposta.getLinhas().get(0).getNumero().equals("0000")) {
                                //Toast.makeText(getApplicationContext(), "Nenhuma linha encontrada com esse termo de busca.", Toast.LENGTH_SHORT).show();
                                lnlSemOnibus.setVisibility(View.VISIBLE);
                                lnlMensagem.setVisibility(View.GONE);
                                //mRecyclerView.setVisibility(View.GONE);
                                //mSwipeRefreshLayout.setVisibility(View.GONE);
                                linhasListas.clear();
                                adapter.notifyDataSetChanged();
                                if (isVoice) {
                                    ttsManager.addQueue("Nenhuma linha encontrada nesse local. ");
                                    ttsManager.addQueue("Para buscar em outro lugar diga: \n Buscar; em. \n E o local desejado.");
                                }

                            } else if (resposta.getLinhas().get(0).getNumero().equals("0001")) {
                                //Toast.makeText(getApplicationContext(), "Nossos servidores não responderam.", Toast.LENGTH_SHORT).show();
                                lnlSemOnibus.setVisibility(View.GONE);
                                lnlMensagem.setVisibility(View.VISIBLE);
                                txtMensagem.setText("Nossos servidores não responderam.");
                                rodaAtualizar();
                                if (isVoice) {
                                    ttsManager.addQueue("Nossos servidores não responderam.");
                                }
                            } else if (resposta.getLinhas().get(0).getNumero().equals("0002")) {
                                //Toast.makeText(getApplicationContext(), "Servidores da SpTrans não responderam.", Toast.LENGTH_SHORT).show();
                                lnlSemOnibus.setVisibility(View.GONE);
                                lnlMensagem.setBackgroundColor(Color.parseColor("#660000"));
                                lnlMensagem.setVisibility(View.VISIBLE);
                                txtMensagem.setText("Servidores da SPTrans não responderam.");
                                contaAtualizar();
                                if (isVoice) {
                                    ttsManager.addQueue("Ops. Servidores da ÉssepeTrans não responderam. Aguarde.");
                                }
                            } else if (resposta.getLinhas().get(0).getNumero().equals("0003")) {
                                Toast.makeText(getApplicationContext(), "Faltam parâmetros necessarios para a busca.", Toast.LENGTH_SHORT).show();
                                lnlSemOnibus.setVisibility(View.GONE);
                                if (isVoice) {
                                    ttsManager.addQueue("Faltam parâmetros necessarios para a busca.");
                                }
                            } else {
                                lnlSemOnibus.setVisibility(View.GONE);
                                lnlMensagem.setVisibility(View.GONE);
                                //mRecyclerView.setVisibility(View.VISIBLE);
                               //mSwipeRefreshLayout.setVisibility(View.VISIBLE);
                                //Toast.makeText(getApplicationContext(), respostaIt.getIda().get(0).getRua(), Toast.LENGTH_SHORT).show();

                                //preencherLista(resposta.getIda());


                                //if (!isVoice) {
                                    preencherLista(resposta.getLinhas());
                                //} else {
                                    /*
                                    if(!primeiraBusa){
                                        pegarTodos(resposta.getLinhas());
                                    }
                                    */
                                    //pegarTodos(resposta.getLinhas());
                                //}

                                //Toast.makeText(getApplicationContext(), resposta.getLinhas().get(0).getNome(), Toast.LENGTH_SHORT).show();

                                //preencherLista(teste);
                                //Toast.makeText(getApplicationContext(), resposta.getIda(), Toast.LENGTH_SHORT).show();
                                //resposta.setVolta(respostaServidor.getVolta());
                                //resposta.setSemana(respostaServidor.getSemana());
                                //resposta.setSabado(respostaServidor.getSabado());
                                //resposta.setDomingo(respostaServidor.getDomingo());

                                //preencherInfo(resposta);
                            }
                            /*
                            if(primeiraBusa){
                                primeiraBusa = false;
                            }
                            */

                        } else {
                            //Toast.makeText(getApplicationContext(), "Ops! Nossos servidores não responderam.", Toast.LENGTH_SHORT).show();
                            msgErroServidor();
                            retrofitPegarLinhas(pontoId);
                        }
                    } else {
                        //Toast.makeText(getApplicationContext(), "Desculpe, houve algum erro ao entrar em contato com o servidor.", Toast.LENGTH_SHORT).show();
                        msgErroServidor();
                        retrofitPegarLinhas(pontoId);
                        // segura os erros de requisição
                        ResponseBody errorBody = response.errorBody();
                    }
                    //progress.dismiss();
                }

                @Override
                public void onFailure(Call<PegaLinhaApiRespostaServidor> call, Throwable t) {
                    if (!t.getMessage().equals("Canceled")) {
                        //Toast.makeText(getApplicationContext(), "Erro na chamada ao servidor - " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        msgErroServidor();
                        retrofitPegarLinhas(pontoId);
                    }
                }
            });

    }

    public void retrofitPegarPonto(String lat, String lng) {
        prgBar.setVisibility(View.VISIBLE);
        txtEnd.setText("Buscando Ponto mais próximo");
        apiRetrofitService service = apiServiceGenerator.createService(apiRetrofitService.class);
        final String llat = lat;
        final String llng = lng;
        callPegarPontos = service.pegarPonto(lat, lng);

        callPegarPontos.enqueue(new Callback<apiPontos>() {
            @Override
            public void onResponse(Call<apiPontos> call, Response<apiPontos> response) {

                prgBar.setVisibility(View.INVISIBLE);

                if (response.isSuccessful()) {
                    apiPontos respostaServidor = response.body();
                    //verifica aqui se o corpo da resposta não é nulo

                    if (respostaServidor != null) {
                        resposta2.setPontos(respostaServidor.getPontos());

                        if(resposta2.getPontos().get(0).getId().equals("-1")){
                            Toast.makeText(getApplicationContext(), "Nenhum ponto encontrado nas redondesas", Toast.LENGTH_SHORT).show();
                            if (isVoice) {
                                ttsManager.addQueue("Nenhum ponto encontrado nas redondesas");
                            }
                        }else{
                            if (isVoice) {
                                ttsManager.addQueue("O ponto mais próximo está em : " + resposta2.getPontos().get(0).getRua() + ", número " + resposta2.getPontos().get(0).getNum() + ". Se estiver correto diga: Buscar.");
                                ttsManager.addQueue("Ou para buscar em outro lugar diga: \n Buscar; em. \n E o local desejado.");
                            }

                            txtEnd.setText(resposta2.getPontos().get(0).getRua() + ", " +  resposta2.getPontos().get(0).getNum());
                            edtEnd.setHint(resposta2.getPontos().get(0).getRua() + ", " +  resposta2.getPontos().get(0).getNum());

                            SharedPreferences sharedPref = context.getSharedPreferences( GRUPO_SALVO, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString(PONTOID, resposta2.getPontos().get(0).getId());
                            editor.putString(PONTOEND, resposta2.getPontos().get(0).getRua() + ", " +  resposta2.getPontos().get(0).getNum());
                            editor.apply();

                            if (!isVoice) {
                                retrofitPegarLinhas(resposta2.getPontos().get(0).getId());
                            }

                            preencherListaPontos(resposta2, 0);
                        }

                    } else {
                        //Toast.makeText(getApplicationContext(),"Resposta nula do servidor", Toast.LENGTH_SHORT).show();
                        msgErroServidor();
                        retrofitPegarPonto(llat, llng);
                    }
                } else {
                    //Toast.makeText(getApplicationContext(),"Resposta não foi sucesso", Toast.LENGTH_SHORT).show();
                    msgErroServidor();
                    retrofitPegarPonto(llat, llng);
                    // segura os erros de requisição
                    ResponseBody errorBody = response.errorBody();
                }
                //progress.dismiss();
            }
            @Override
            public void onFailure(Call<apiPontos> call, Throwable t) {
                if(!t.getMessage().equals("Canceled")){
                    //Toast.makeText(getApplicationContext(),"Erro na chamada ao servidor - " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    msgErroServidor();
                    retrofitPegarPonto(llat, llng);
                }
            }
        });
    }

    public void retrofitPegarEndComp(String ender) {
        prgBar.setVisibility(View.VISIBLE);
        apiRetrofitService service = apiServiceGenerator.createService(apiRetrofitService.class);
        callPegarEndComp = service.pegarEndCompl(ender);
        final String ed = ender;
        //final String name = nome;
        //final String num = numLinha;

        callPegarEndComp.enqueue(new Callback<PontoApiRepostaServidor>() {
            @Override
            public void onResponse(Call<PontoApiRepostaServidor> call, Response<PontoApiRepostaServidor> response) {
                prgBar.setVisibility(View.INVISIBLE);
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
                            //adicionarSemOnibus(name, num);
                        }else{
                            //Toast.makeText(getApplicationContext(), respostaIt.getIda().get(0).getRua(), Toast.LENGTH_SHORT).show();

                            //preencherLista(resposta.getIda());

                            //preencherLista(resposta.getLinhas());

                            //Toast.makeText(getApplicationContext(), resposta.getStatus(), Toast.LENGTH_SHORT).show();
                            //Toast.makeText(getApplicationContext(), String.valueOf(resposta.getProximoOnibus().getPrefixo()), Toast.LENGTH_SHORT).show();

                            //adicionarListaOrdem(resposta.getProximoOnibus(), name, num);

                            /*
                            if (isVoice) {
                                ttsManager.addQueue("Buscando ônibus que passam por : " + resposta3.getRua() + ", número " + resposta3.getNum());
                            }
                            */

                            //txtEnd.setText(resposta3.getRua() + ", " +  resposta3.getNum());
                            //retrofitPegarLinhas(resposta3.getLat() + "," + resposta3.getLng());
                            //Toast.makeText(getApplicationContext(), resposta3.getLat() + "," + resposta3.getLng(), Toast.LENGTH_SHORT).show();

                            retrofitPegarPonto(String.valueOf(resposta3.getLat()), String.valueOf(resposta3.getLng()));

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
                        retrofitPegarEndComp(ed);
                    }
                } else {
                    //Toast.makeText(getApplicationContext(),"Resposta não foi sucesso", Toast.LENGTH_SHORT).show();
                    msgErroServidor();
                    retrofitPegarEndComp(ed);
                    // segura os erros de requisição
                    ResponseBody errorBody = response.errorBody();
                }
                //progress.dismiss();
            }
            @Override
            public void onFailure(Call<PontoApiRepostaServidor> call, Throwable t) {
                if(!t.getMessage().equals("Canceled")){
                    //Toast.makeText(getApplicationContext(),"Erro na chamada ao servidor - " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    msgErroServidor();
                    retrofitPegarEndComp(ed);
                }
            }
        });
    }

    public void msgErroServidor(){
        lnlSemOnibus.setVisibility(View.GONE);
        lnlMensagem.setBackgroundColor(Color.parseColor("#444444"));
        lnlMensagem.setVisibility(View.VISIBLE);
        txtMensagem.setText("Erro nos servidores");
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
            onibus += linha.getNumero() + " " + linha.getOperacao() + ", " + subString.trocarString(linha.getNome()) + "\n ";
            //Toast.makeText(getApplicationContext(), subString.trocarString(linha.getNome()), Toast.LENGTH_SHORT).show();
        }
        adapter.notifyDataSetChanged();
        if(isVoice) {
            if(linhas.size() == 1){
                ttsManager.addQueue("A linha " + onibus + " passa por esse ponto. Para buscar ôninbus dessa linha diga: \n Okey");
                linhasListas.get(0).setSelecionado(true);
                String[] item = new String[3];
                item[0] = linhasListas.get(0).getNumero()+"-"+linhasListas.get(0).getOperacao();
                item[1] = String.valueOf(linhasListas.get(0).getSentido());
                item[2] = linhasListas.get(0).getNome();
                adapter.info.add(item);
                fab.setVisibility(View.VISIBLE);
                adapter.notifyDataSetChanged();
            }else{
                ttsManager.addQueue("Escolha uma das linhas que passam por esse ponto. " + onibus);
            }
        }
    }

    public void preencherListaPontos(apiPontos pontos, int remover){
        pontosLista.clear();
        for (int i=0; i<pontos.getPontos().size(); i++){
            if( i != remover) {
                PegaLinha2PontosListaItem ponto = new PegaLinha2PontosListaItem();
                ponto.setEnd(pontos.getPontos().get(i).getRua() + ", " + pontos.getPontos().get(i).getNum());
                ponto.setId(pontos.getPontos().get(i).getId());
                pontosLista.add(ponto);
            }
        }
        adapterPontos.notifyDataSetChanged();
    }

    public void pegarTodos(List<apiPegaLinha> linhas){
        Intent intent = new Intent(this, ProximoOnibusActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("quant", String.valueOf(linhas.size()));
        for (int i=0; i<linhas.size(); i++){
            bundle.putString("numero"+i, linhas.get(i).getNumero() + "-" + linhas.get(i).getOperacao());
            bundle.putString("sentido"+i, String.valueOf(linhas.get(i).getSentido()));
            bundle.putString("nome"+i, linhas.get(i).getNome());
        }
        intent.putExtras(bundle);
        this.startActivity(intent);
        if(isVoice){
            finish();
        }
    }

    public void finalizaChamadas(){
        pararAtualizar();
        if(callPegarLinha != null){
            callPegarLinha.cancel();
        }
        if(callPegarEndComp != null){
            callPegarEndComp.cancel();
        }
        if(callPegarPontos != null){
            callPegarPontos.cancel();
        }
    }

    public void btnEnd(View view){
        previneDuplaRequisicao = true;
        clickEnd(view);
    }
    public void clickEnd(View view){
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
                btnEnd.setImageResource(R.drawable.certo);
                fab.setVisibility(View.GONE);
                adapter.info.clear();
                listaPonto.setVisibility(View.VISIBLE);
                //btnEnd.setText("V");
            } else {
                llTextEnd.setVisibility(View.VISIBLE);
                llEditEnd.setVisibility(View.GONE);
                txtIsVisible = true;
                //btnEnd.setText("E");
                btnEnd.setImageResource(R.drawable.editar);
                txtEnd.setText("Buscando Endereço");
                if (edtEnd.getText().toString().length() > 0) {
                    retrofitPegarEndComp(edtEnd.getText().toString());
                } else {
                    retrofitPegarEndComp(edtEnd.getHint().toString());
                }
                listaPonto.setVisibility(View.GONE);
            }
    }
    public void btnProximosOnibus(View v){
        adapter.teste(this);
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
                    clickEnd(btnMic);
                }
                break;
            }
        }
    }

    public void reconhecerComando(String comando) {
        if(comando.toLowerCase().equals("voltar")){
            finish();
        }else if(comando.toLowerCase().equals("buscar")){
            if(primeiraBusca){
                //retrofitPegarLinhas(resposta2.getPontos().get(0).getId());
                SharedPreferences sharedPref = context.getSharedPreferences( GRUPO_SALVO, Context.MODE_PRIVATE);
                String pontoId = sharedPref.getString(PONTOID, "");
                if(!pontoId.equals("")){
                    retrofitPegarLinhas(pontoId);
                }
            }
            //primeiraBusca = false;
        }else if(comando.toLowerCase().contains("buscar em ")){
            buscarEm(comando);
            primeiraBusca = true;
        }else  if(comando.toLowerCase().contains("ok") || comando.toLowerCase().contains("okey") || comando.toLowerCase().contains("oquei")){
            adapter.teste(PegaLinha2Activity.this);
            finish();
        }else{
            //ttsManager.initQueue("Nenhum comando identificado");
            reconhecerLinha(comando);
        }
    }

    public void reconhecerLinha(String comando){

        boolean encontrou = false;

        for (int i=0; i<linhasListas.size(); i++){

            String comp = linhasListas.get(i).getNumero() + linhasListas.get(i).getOperacao() + linhasListas.get(i).getNome();
            comp = subString.formatarString(comp);
            comando = subString.formatarString(comando);
            Log.e("error", comp + " - " + comando + " - " + comp.contains(comando));
            if (comp.contains(comando)){
                encontrou = true;
                if (!linhasListas.get(i).getSelecionado()){
                    linhasListas.get(i).setSelecionado(true);
                    String[] item = new String[3];
                    item[0] = linhasListas.get(i).getNumero()+"-"+linhasListas.get(i).getOperacao();
                    item[1] = String.valueOf(linhasListas.get(i).getSentido());
                    item[2] = linhasListas.get(i).getNome();
                    adapter.info.add(item);
                    fab.setVisibility(View.VISIBLE);
                }
                adapter.notifyDataSetChanged();
                if(adapter.info.size() == linhasListas.size()) {
                    ttsManager.addQueue("Linha " + linhasListas.get(i).getNumero() + " " + linhasListas.get(i).getOperacao() + " " + subString.trocarString(linhasListas.get(i).getNome()) + " selecionada. Para confirmar diga: Okey");
                }else if(adapter.info.size() < 3){
                    ttsManager.addQueue("Linha " + linhasListas.get(i).getNumero() + " " + linhasListas.get(i).getOperacao() + " " + subString.trocarString(linhasListas.get(i).getNome()) + " selecionada. Se desejar diga o nome de outra linha, senão diga: Okey");
                }else{
                    ttsManager.addQueue("Linha " + linhasListas.get(i).getNumero() + " " + linhasListas.get(i).getOperacao() + " " + subString.trocarString(linhasListas.get(i).getNome()) + " selecionada. Para confirmar diga: Okey");
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

    public void buscarEm(String comando) {
        llTextEnd.setVisibility(View.VISIBLE);
        llEditEnd.setVisibility(View.GONE);
        txtIsVisible = true;
        //btnEnd.setText("E");
        btnEnd.setImageResource(R.drawable.editar);
        txtEnd.setText("Buscando Endereço");
        fab.setVisibility(View.GONE);
        adapter.info.clear();
        retrofitPegarEndComp(comando.replace("buscar em ", ""));
    }




    public void btnDireita(View view){
        if(isVoice) {
            ttsManager.stop();
            //ttsManager.initQueue("OI");
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
                            mediaPlayer.start(); // no need to call prepare(); create() does that for you
                            stoptimertask();
                        }

                    }
                });
            }
        };
    }



    public void contaAtualizar() {
        timerAtualizar = new Timer();
        rodaAtualizar();
        timerAtualizar.schedule(timerTaskAtualizar, 5000, 5000); //
    }
    public void pararAtualizar() {
        //stop the timer, if it's not already null
        if (timerAtualizar != null) {
            timerAtualizar.cancel();
            timerAtualizar = null;
        }
    }
    public void rodaAtualizar() {

        timerTaskAtualizar = new TimerTask() {
            public void run() {

                //use a handler to run a toast that shows the current timestamp
                handlerAtualizar.post(new Runnable() {
                    public void run() {
                        pararAtualizar();
                        SharedPreferences sharedPref = context.getSharedPreferences( GRUPO_SALVO, Context.MODE_PRIVATE);
                        String latlng = sharedPref.getString(PONTOID, "");
                        if(!latlng.equals("")){
                            retrofitPegarLinhas(latlng);
                        }
                    }
                });
            }
        };
    }



}
