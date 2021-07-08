package br.com.virtualartsa.onibusemponto;

import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Schiavetto on 16/08/2017.
 */

public class apiServiceGenerator {
    //URL base do endpoint. Deve sempre terminar com /
    //public static final String API_BASE_URL = "https://community-neutrino-currency-conversion.p.mashape.com/";
    //public static final String API_BASE_URL = "http://virtualartsa.com.br/";
    //public static final String API_BASE_URL = "http://api.fixer.io/";
    public static final String API_BASE_URL = "http://virtualartsa.com.br/";

    public static <S> S createService(Class<S> serviceClass) {

        //Instancia do interceptador das requisições
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS);

        httpClient.addInterceptor(loggingInterceptor);


        //Instância do retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .client(httpClient.build())
                .build();

        return retrofit.create(serviceClass);
    }
}
