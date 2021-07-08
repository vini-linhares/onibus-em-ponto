package br.com.virtualartsa.onibusemponto;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by Schiavetto on 16/08/2017.
 */

public interface apiRetrofitService {
    @GET("latest")
    Call<apiRespostaServidor> converterUnidade2(@Query("base") String base);

    @GET("onibusemponto/api/linha")
    Call<apiRespostaServidor> converterUnidade(@Query("busca") String base);

    @GET("onibusemponto/api/info")
    Call<DetalhesApiRespostaServidor> pegarInfo(@Query("linha") String base);

    @GET("onibusemponto/api/itinerario")
    Call<DetalhesItinerarioApiRespostaServidor> pegarItinerario(@Query("linha") String base);

    @GET("onibusemponto/api/pegaLinhasComp")
    Call<PegaLinhaApiRespostaServidor> pegarLinhas2(@Query("latlng") String base);
    @GET("onibusemponto/api2/pegaLinhasPonto")
    Call<PegaLinhaApiRespostaServidor> pegarLinhas(@Query("ponto") String base);

    @GET("onibusemponto/api/proximoBusComp")
    Call<MeuOnibusApiRespostaServidor> pegarProximoOnibus2(@Query("linha") String linha, @Query("sl") String sl, @Query("latlng") String latlng);
    @GET("onibusemponto/api2/proximoBusComp")
    Call<MeuOnibusApiRespostaServidor> pegarProximoOnibus(@Query("linha") String linha, @Query("sl") String sl, @Query("ponto") String latlng);

    @GET("onibusemponto/api/proximoBus")
    Call<ProximoOnibusApiRespostaServidor> pegarProximo2(@Query("linha") String linha, @Query("sl") String sl, @Query("latlng") String latlng);
    @GET("onibusemponto/api2/proximoBus")
    Call<ProximoOnibusApiRespostaServidor> pegarProximo(@Query("linha") String linha, @Query("sl") String sl, @Query("ponto") String ponto);

    @GET("onibusemponto/api/pegaEnd")
    Call<RuasApiRespostaServidor> pegarEnd(@Query("latlng") String latlng);
    @GET("onibusemponto/api2/pegaPontos")
    Call<apiPontos> pegarPonto(@Query("lat") String lat, @Query("lng") String lng);

    @GET("onibusemponto/api/PegaLatlng")
    Call<PontoApiRepostaServidor> pegarEndCompl(@Query("end") String end);

    //Criar classe na api
    //Criar resposta servidor
    //Acessar a api classe que vou criar
    @GET("onibusemponto/api/calcDist")
    Call<PontoDistApiRespostaServidor> calculaDist(@Query("inicio") String inicio, @Query("fim") String fim);
}