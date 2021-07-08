package br.com.virtualartsa.onibusemponto;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RuasActivity extends AppCompatActivity {

    TextView txtEnd;
    RuasApiRespostaServidor resposta = new RuasApiRespostaServidor();

    private LocationCallback mLocationCallback;
    LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;

    String coord = "";

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    public final static String GRUPO_SALVO = "br.com.virtualartsa.onibusemponto.GRUPO_SALVO";
    public final static String VOZ = "br.com.virtualartsa.onibusemponto.VOZ";
    public LinearLayout bots;
    public boolean isVoice;

    TTSManager ttsManager = null;

    String ruaAntiga;
    int numAntigo;

    public LinearLayout lnlMensagem;
    public TextView txtMensagem;
    ProgressBar prgBarMensagem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ruas);

        numAntigo = -1;
        ruaAntiga = "";

        bots = (LinearLayout)findViewById(R.id.layBotsR);
        SharedPreferences sharedPref = this.getSharedPreferences( GRUPO_SALVO, Context.MODE_PRIVATE);
        String tutorialVisto = sharedPref.getString(VOZ, "0");
        if(tutorialVisto.equals("0")){
            bots.setVisibility(View.INVISIBLE);
            isVoice = false;
        }else{
            isVoice = true;
        }
        if(isVoice) {
            ttsManager = new TTSManager();
            ttsManager.init(this);
        }


        txtMensagem = (TextView)findViewById(R.id.txtMensagem);
        lnlMensagem = (LinearLayout)findViewById(R.id.lnlMensagem);
        prgBarMensagem = (ProgressBar) findViewById(R.id.prgBarMensagem);
        prgBarMensagem.getIndeterminateDrawable().setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.MULTIPLY);

        txtEnd = (TextView) findViewById(R.id.txtEndR);

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
        //if (mRequestingLocationUpdates) {
        startLocationUpdates();
        //}
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null /* Looper */);
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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
                                retrofitPegarOnibus(location.getLatitude()+","+location.getLongitude());
                                coord = location.getLatitude()+","+location.getLongitude();
                            } else {
                                //Toast.makeText(getApplicationContext(), "Location = null", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
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

                            boolean falar = false;
                            if(numAntigo == -1){
                                falar = true;
                            }

                            if(falar || !resposta.getRua().equals(ruaAntiga) || (Integer.parseInt(resposta.getNum()) - numAntigo >=500 || Integer.parseInt(resposta.getNum()) - numAntigo <= -500 )){
                                if(isVoice){
                                    ttsManager.addQueue(resposta.getRua() + ", número " +  resposta.getNum());
                                }
                                numAntigo = Integer.parseInt(resposta.getNum());
                                ruaAntiga = resposta.getRua();
                            }



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
                    //Toast.makeText(getApplicationContext(),"Resposta não foi sucesso", Toast.LENGTH_SHORT).show();
                    // segura os erros de requisição
                    msgErroServidor();
                    retrofitPegarOnibus(ll);
                    ResponseBody errorBody = response.errorBody();
                }
                //progress.dismiss();
            }
            @Override
            public void onFailure(Call<RuasApiRespostaServidor> call, Throwable t) {
                //Toast.makeText(getApplicationContext(),"Erro na chamada ao servidor - " + t.getMessage(), Toast.LENGTH_SHORT).show();
                msgErroServidor();
                retrofitPegarOnibus(ll);
            }
        });
    }

    public void msgErroServidor(){
        lnlMensagem.setBackgroundColor(Color.parseColor("#444444"));
        lnlMensagem.setVisibility(View.VISIBLE);
        txtMensagem.setText("Erro nos servidores");
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

        }
    }

    public void reconhecerComando(String comando) {
        if(comando.toLowerCase().equals("voltar")){
            finish();
        }
    }



    public void btnDireita(View view){
        if(isVoice) {
            ttsManager.stop();
            startVoiceInput();
        }
        //ttsManager.initQueue("Nenhuma linha Identificada.");
    }

    public void btnSumir(View view){
        if (lnlMensagem.getVisibility() == View.GONE) {
            lnlMensagem.setVisibility(View.VISIBLE);
        }else{
            lnlMensagem.setVisibility(View.GONE);
        }

    }




}
