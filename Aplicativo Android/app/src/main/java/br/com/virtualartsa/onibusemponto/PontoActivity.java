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
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PontoActivity extends AppCompatActivity {

    Timer timer;
    TimerTask timerTask;
    final Handler handler = new Handler();

    private static final int REQ_CODE_SPEECH_INPUT_EDIT = 101;
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    public final static String GRUPO_SALVO = "br.com.virtualartsa.onibusemponto.GRUPO_SALVO";
    public final static String VOZ = "br.com.virtualartsa.onibusemponto.VOZ";
    public LinearLayout bots;
    public boolean isVoice;

    TTSManagerMsg ttsManager = null;

    float latDestino;
    float lngDestino;

    public LinearLayout lnlMensagem;
    public TextView txtMensagem;
    ProgressBar prgBarMensagem;

    RuasApiRespostaServidor resposta = new RuasApiRespostaServidor();
    TextView txtEnd;
    PontoApiRepostaServidor resposta2 = new PontoApiRepostaServidor();
    boolean calcDist;
    TextView txtDist;
    PontoDistApiRespostaServidor resposta3 = new PontoDistApiRespostaServidor();

    private LocationCallback mLocationCallback;
    LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;

    String coord = "";

    boolean distancia1;
    boolean distancia2;

    int valorAntigo;

    private String linhaBusca;
    EditText edtEnd;
    TextView txtEndd;
    ImageButton btnEnd;
    LinearLayout llTextEnd;
    LinearLayout llEditEnd;
    ProgressBar prgBar;
    ImageButton btnMic;
    boolean txtIsVisible;
    ProgressBar prgBar2;

    Call<PontoApiRepostaServidor> call;
    Vibrator vib;

    boolean pegarLocalResume = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ponto);

        txtMensagem = (TextView)findViewById(R.id.txtMensagem);
        lnlMensagem = (LinearLayout)findViewById(R.id.lnlMensagem);
        prgBarMensagem = (ProgressBar) findViewById(R.id.prgBarMensagem);
        prgBarMensagem.getIndeterminateDrawable().setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.MULTIPLY);

        vib = (Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE);

        distancia1 = false;
        distancia2 = false;
        valorAntigo = -1;

        calcDist = false;
        latDestino = -1;
        lngDestino = -1;

        txtEnd = (TextView) findViewById(R.id.txtEndAtualP);
        txtEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Localização obtida automaticamente do GPS", Toast.LENGTH_SHORT).show();
            }
        });
        txtDist = (TextView) findViewById(R.id.txtDistP);

        bots = (LinearLayout)findViewById(R.id.layBotsP);
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
            ttsManager.init(this, "Diga o seu destino");
        }



        edtEnd = (EditText)findViewById(R.id.edtEnd);
        edtEnd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    hideKeyboard(v);
                    btnBuscar(v);
                }
                return false;
            }
        });
        txtEndd = (TextView)findViewById(R.id.txtEnd);
        txtEndd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnBuscar(v);
            }
        });
        btnEnd = (ImageButton)findViewById(R.id.btnEnd);
        llEditEnd = (LinearLayout)findViewById(R.id.llEdtEnd);
        llTextEnd = (LinearLayout) findViewById(R.id.llTextEnd);
        prgBar = (ProgressBar) findViewById(R.id.prgBar);
        prgBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#222222"), PorterDuff.Mode.MULTIPLY);
        prgBar2 = (ProgressBar) findViewById(R.id.prgBar2);
        prgBar2.getIndeterminateDrawable().setColorFilter(Color.parseColor("#222222"), PorterDuff.Mode.MULTIPLY);
        txtIsVisible = false;



        //retrofitPegarOnibus("-23.498888,-46.594659");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(getApplicationContext(), "Sem permissão", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        }else{
            rodar();
        }

        createLocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
            }
        });
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        //Toast.makeText(getApplicationContext(), String.valueOf(location.getLatitude()+","+location.getLongitude()), Toast.LENGTH_SHORT).show();
                        if(!coord.equals(location.getLatitude()+","+location.getLongitude())){
                            if(calcDist && latDestino != -1){
                                //Toast.makeText(getApplicationContext(), location.getLatitude()+","+location.getLongitude()+" - "+latDestino+","+lngDestino, Toast.LENGTH_SHORT).show();
                                retrofitCalcularDist(location.getLatitude()+","+location.getLongitude(), latDestino+","+lngDestino);
                            }
                            retrofitPegarOnibus(location.getLatitude()+","+location.getLongitude());
                            coord = location.getLatitude()+","+location.getLongitude();
                        }

                    } else {
                        //Toast.makeText(getApplicationContext(), "Location = null", Toast.LENGTH_SHORT).show();
                    }
                }
            };
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
        if(isVoice) {
            ttsManager.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(isVoice) {
            ttsManager.shutDown();
        }
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pegarLocalResume){
            pegarLocalResume = false;
            rodar();
        }
        startLocationUpdates();
        //}
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null /* Looper */);
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(8000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void rodar(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if (locationProviders == null || locationProviders.equals("")) {
                String[] itens = new String[2];
                itens[0] = "Desejo ativar!";
                itens[1] = "Agora não";

                AlertDialog.Builder builder = new AlertDialog.Builder(PontoActivity.this);
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
                                    retrofitPegarOnibus(location.getLatitude() + "," + location.getLongitude());
                                    coord = location.getLatitude() + "," + location.getLongitude();
                                } else {
                                    //Toast.makeText(getApplicationContext(), "Location = null", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
    }

    public void finalizaChamadas(){
        if(call != null){
            call.cancel();
        }
    }

    public void retrofitPegarOnibus(String latlgn) {
        apiRetrofitService service = apiServiceGenerator.createService(apiRetrofitService.class);
        Call<RuasApiRespostaServidor> call = service.pegarEnd(latlgn);

        final String ll = latlgn;
        //final String name = nome;
        //final String num = numLinha;

        call.enqueue(new Callback<RuasApiRespostaServidor>() {
            @Override
            public void onResponse(Call<RuasApiRespostaServidor> call, Response<RuasApiRespostaServidor> response) {

                if (response.isSuccessful()) {
                    RuasApiRespostaServidor respostaServidor = response.body();
                    //verifica aqui se o corpo da resposta não é nulo
                    if (respostaServidor != null) {

                        lnlMensagem.setVisibility(View.GONE);

                        resposta.setNum(respostaServidor.getNum());
                        resposta.setRua(respostaServidor.getRua());

                        if(resposta.getNum().equals("-1")){
                            Toast.makeText(getApplicationContext(), "Endereço não encontrado", Toast.LENGTH_SHORT).show();
                            //adicionarSemOnibus(name, num);
                        }else{
                            //Toast.makeText(getApplicationContext(), respostaIt.getIda().get(0).getRua(), Toast.LENGTH_SHORT).show();

                            //preencherLista(resposta.getIda());

                            //preencherLista(resposta.getLinhas());

                            //Toast.makeText(getApplicationContext(), resposta.getStatus(), Toast.LENGTH_SHORT).show();
                            //Toast.makeText(getApplicationContext(), String.valueOf(resposta.getProximoOnibus().getPrefixo()), Toast.LENGTH_SHORT).show();

                            //adicionarListaOrdem(resposta.getProximoOnibus(), name, num);

                            txtEnd.setText(resposta.getRua() + ", " +  resposta.getNum());

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
                        retrofitPegarOnibus(ll);
                    }
                } else {
                    Toast.makeText(getApplicationContext(),"Resposta não foi sucesso", Toast.LENGTH_SHORT).show();
                    msgErroServidor();
                    retrofitPegarOnibus(ll);
                    // segura os erros de requisição
                    ResponseBody errorBody = response.errorBody();
                }
                //progress.dismiss();
            }
            @Override
            public void onFailure(Call<RuasApiRespostaServidor> call, Throwable t) {
                Toast.makeText(getApplicationContext(),"Erro na chamada ao servidor - " + t.getMessage(), Toast.LENGTH_SHORT).show();
                msgErroServidor();
                retrofitPegarOnibus(ll);
            }
        });
    }
    public void retrofitPegarEndComp(final String ender) {

        prgBar.setVisibility(View.VISIBLE);

        distancia1 = false;
        distancia2 = false;
        valorAntigo = -1;

        apiRetrofitService service = apiServiceGenerator.createService(apiRetrofitService.class);
        call = service.pegarEndCompl(ender);

        //final String name = nome;
        //final String num = numLinha;

        call.enqueue(new Callback<PontoApiRepostaServidor>() {
            @Override
            public void onResponse(Call<PontoApiRepostaServidor> call, Response<PontoApiRepostaServidor> response) {

                prgBar.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    PontoApiRepostaServidor respostaServidor = response.body();
                    //verifica aqui se o corpo da resposta não é nulo
                    if (respostaServidor != null) {

                        lnlMensagem.setVisibility(View.GONE);

                        resposta2.setNum(respostaServidor.getNum());
                        resposta2.setRua(respostaServidor.getRua());
                        resposta2.setLat(respostaServidor.getLat());
                        resposta2.setLng(respostaServidor.getLng());

                        if(resposta2.getNum().equals("-1")){
                            Toast.makeText(getApplicationContext(), "Endereço não encontrado", Toast.LENGTH_SHORT).show();
                            //adicionarSemOnibus(name, num);
                        }else{
                            //Toast.makeText(getApplicationContext(), respostaIt.getIda().get(0).getRua(), Toast.LENGTH_SHORT).show();

                            //preencherLista(resposta.getIda());

                            //preencherLista(resposta.getLinhas());

                            //Toast.makeText(getApplicationContext(), resposta.getStatus(), Toast.LENGTH_SHORT).show();
                            //Toast.makeText(getApplicationContext(), String.valueOf(resposta.getProximoOnibus().getPrefixo()), Toast.LENGTH_SHORT).show();

                            //adicionarListaOrdem(resposta.getProximoOnibus(), name, num);

                            if (isVoice) {
                                ttsManager.initQueue("Seu destino é: " + resposta2.getRua() + ", número " + resposta2.getNum());
                            }

                            txtEndd.setText(resposta2.getRua() + ", " +  resposta2.getNum());
                            calcDist = true;
                            latDestino = resposta2.getLat();
                            lngDestino = resposta2.getLng();

                            if (!coord.equals("")) {
                                retrofitCalcularDist(coord, latDestino + "," + lngDestino);
                            }
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
                        retrofitPegarEndComp(ender);
                    }
                } else {
                    //Toast.makeText(getApplicationContext(),"Resposta não foi sucesso", Toast.LENGTH_SHORT).show();
                    // segura os erros de requisição
                    msgErroServidor();
                    retrofitPegarEndComp(ender);
                    ResponseBody errorBody = response.errorBody();
                }
                //progress.dismiss();
            }
            @Override
            public void onFailure(Call<PontoApiRepostaServidor> call, Throwable t) {
                //Toast.makeText(getApplicationContext(),"Erro na chamada ao servidor - " + t.getMessage(), Toast.LENGTH_SHORT).show();
                msgErroServidor();
                retrofitPegarEndComp(ender);
            }
        });
    }

    public void retrofitCalcularDist(final String localAtual, final String destino) {

        prgBar2.setVisibility(View.VISIBLE);

        apiRetrofitService service = apiServiceGenerator.createService(apiRetrofitService.class);
        Call<PontoDistApiRespostaServidor> call = service.calculaDist(localAtual, destino);

        //final String name = nome;
        //final String num = numLinha;

        call.enqueue(new Callback<PontoDistApiRespostaServidor>() {
            @Override
            public void onResponse(Call<PontoDistApiRespostaServidor> call, Response<PontoDistApiRespostaServidor> response) {

                prgBar2.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    PontoDistApiRespostaServidor respostaServidor = response.body();
                    //verifica aqui se o corpo da resposta não é nulo
                    if (respostaServidor != null) {

                        lnlMensagem.setVisibility(View.GONE);

                        resposta3.setTexto(respostaServidor.getTexto());
                        resposta3.setValor(respostaServidor.getValor());

                        if(resposta3.getValor() == -1){
                            Toast.makeText(getApplicationContext(), "Não foi possível calcular distância", Toast.LENGTH_SHORT).show();
                            //adicionarSemOnibus(name, num);
                        }else{
                            //Toast.makeText(getApplicationContext(), respostaIt.getIda().get(0).getRua(), Toast.LENGTH_SHORT).show();

                            //preencherLista(resposta.getIda());

                            //preencherLista(resposta.getLinhas());

                            //Toast.makeText(getApplicationContext(), resposta.getStatus(), Toast.LENGTH_SHORT).show();
                            //Toast.makeText(getApplicationContext(), String.valueOf(resposta.getProximoOnibus().getPrefixo()), Toast.LENGTH_SHORT).show();

                            //adicionarListaOrdem(resposta.getProximoOnibus(), name, num);

                            boolean falar = false;
                            if(valorAntigo == -1){
                                falar = true;
                            }

                            if(!distancia1 && resposta3.getValor() <= 500 && resposta3.getValor() > 250){
                                vib.vibrate(300);
                                if(isVoice){
                                    ttsManager.addQueue("Seu ponto está próximo");
                                }else{
                                    Toast.makeText(getApplicationContext(), "Seu ponto está próximo", Toast.LENGTH_SHORT).show();
                                }
                                distancia1 = true;
                            }else if(!distancia2 && resposta3.getValor() <= 250){
                                vib.vibrate(600);
                                if(isVoice){
                                    ttsManager.addQueue("Atendção, seu ponto está muito próximo");
                                }else{
                                    Toast.makeText(getApplicationContext(), "Atendção, seu ponto está muito próximo", Toast.LENGTH_SHORT).show();
                                }
                                distancia2 = true;
                                distancia1 = true;
                            }else if(falar || valorAntigo - resposta3.getValor() >= 500){
                                valorAntigo = resposta3.getValor();
                                if(isVoice){
                                    ttsManager.addQueue("Faltam: " + resposta3.getTexto());
                                }else{
                                    //Toast.makeText(getApplicationContext(), "Faltam: " + resposta3.getTexto(), Toast.LENGTH_SHORT).show();
                                }
                            }

                            txtDist.setText(resposta3.getTexto());
                            //calcDist = true;
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
                        retrofitCalcularDist(localAtual, destino);
                    }
                } else {
                    //Toast.makeText(getApplicationContext(),"Resposta não foi sucesso", Toast.LENGTH_SHORT).show();
                    msgErroServidor();
                    retrofitCalcularDist(localAtual, destino);
                    // segura os erros de requisição
                    ResponseBody errorBody = response.errorBody();
                }
                //progress.dismiss();
            }
            @Override
            public void onFailure(Call<PontoDistApiRespostaServidor> call, Throwable t) {
                //Toast.makeText(getApplicationContext(),"Erro na chamada ao servidor - " + t.getMessage(), Toast.LENGTH_SHORT).show();
                msgErroServidor();
                retrofitCalcularDist(localAtual, destino);
            }
        });
    }

    public void msgErroServidor(){
        lnlMensagem.setBackgroundColor(Color.parseColor("#444444"));
        lnlMensagem.setVisibility(View.VISIBLE);
        txtMensagem.setText("Erro nos servidores");
    }

    public void btnBuscar(View v){

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
                txtEndd.requestFocus();
            }else{
                hideKeyboard(edtEnd);
                llTextEnd.setVisibility(View.VISIBLE);
                llEditEnd.setVisibility(View.GONE);
                txtIsVisible = true;
                //btnEnd.setText("E");
                btnEnd.setImageResource(R.drawable.ed);
                txtEndd.setText("Buscando: " + linhaBusca);
                retrofitPegarEndComp(linhaBusca);
            }
        }


        if(!edtEnd.getText().toString().equals("")){
            retrofitPegarEndComp(edtEnd.getText().toString());
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
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Diga o Destino");
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
                    btnBuscar(btnMic);
                }
                break;
            }
        }
    }

    public void reconhecerComando(String comando) {
        if(comando.toLowerCase().equals("voltar")){
            finish();
        }else{
            reconhecerLinha(comando);
        }
    }


    public void reconhecerLinha(String comando){
        llTextEnd.setVisibility(View.VISIBLE);
        llEditEnd.setVisibility(View.GONE);
        txtIsVisible = true;
        //btnEnd.setText("E");
        btnEnd.setImageResource(R.drawable.ed);
        txtEndd.setText("Buscando: " + comando);
        retrofitPegarEndComp(comando);
    }

    public void btnDireita(View view){
        if(isVoice) {
            ttsManager.stop();
            startVoiceInput();
        }
        //ttsManager.initQueue("Nenhuma linha Identificada.");
    }

}
